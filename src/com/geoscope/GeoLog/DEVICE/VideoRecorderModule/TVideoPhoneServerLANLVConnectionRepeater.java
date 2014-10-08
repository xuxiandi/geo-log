package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerVideoPhoneServer;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TVideoPhoneServerLANLVConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+5;
	
	
	public static final int ClientCommand_Disconnect 					= 0;
	public static final int ClientCommand_DisconnectAndFinishSession	= 1;
	public static final int ClientCommand_Checkpoint 					= 2;
	public static final int ClientCommand_ContactSession 				= 3;
	public static final int ClientCommand_CheckSessionStatus 			= 4;
	//.
	public static final int SuccessCode_OK = 0;
	//.
	public static final int ErrorCode_Unknown 				= -1;
	public static final int ErrorCode_IncorrectParameters 	= -2;
	public static final int ErrorCode_SessionIsNotFound 	= -3;
	
	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		if (UserAccessKey == null)
			return true; //. ->
		TVideoRecorderServerVideoPhoneServer.TSession Session = TVideoRecorderServerVideoPhoneServer.SessionServer.Session_Get();
		if (Session == null)
			return false; // ->
		return (Session.Check(UserAccessKey));
	}
	
	public String SessionID;
	//.
	private byte[] DescriptorBA = new byte[4];
	private int Descriptor;
	
	public TVideoPhoneServerLANLVConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID,pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TVideoPhoneServerLANLVConnectionRepeater> RepeatersToCancel = new ArrayList<TVideoPhoneServerLANLVConnectionRepeater>(1);
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TVideoPhoneServerLANLVConnectionRepeater))
        			RepeatersToCancel.add(((TVideoPhoneServerLANLVConnectionRepeater)CR));
        	}
		}
    	for (int I = 0; I < RepeatersToCancel.size(); I++)
    		RepeatersToCancel.get(I).CancelAndWait();
		//.
		Start();
	}
	
	@Override
	public boolean ReceivingIsAvaiable() {
		return true;
	}
	
	@Override
	public boolean TransmittingIsAvaiable() {
		return true;
	}
	
	@Override
	public void DoReceiving(Thread ReceivingThread) throws IOException {
		while (!Canceller.flCancel) {
			try {
				Thread.sleep(1000000);
			} catch (InterruptedException e) {
				return; //. ->
			}
		}
	}

	@Override
	public void DoTransmitting(Thread TransmittingThread) throws Exception {
		int Version = ReadDescriptor();
		switch (Version) {
		
		case 1: 
			//. read SessionID
			Descriptor = ReadDescriptor();
			if (!((0 <= Descriptor) && (Descriptor <= TVideoRecorderServerVideoPhoneServer.TSession.SessionMaxLength))) {
				WriteDescriptor(ErrorCode_IncorrectParameters);
				return; //. ->
			}
			byte[] BA = new byte[Descriptor];
			int ActualSize = TLANConnectionRepeater.InputStream_Read(DestinationConnectionInputStream,BA,BA.length);	
	    	if (ActualSize < 0)
    			throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
			SessionID = new String(BA,"US-ASCII");
			int SessionTimeout = ReadDescriptor();
			if (SessionTimeout > TVideoRecorderServerVideoPhoneServer.TSession.SessionMaxTimeout) {
				WriteDescriptor(ErrorCode_IncorrectParameters);
				return; //. ->
			}
			TVideoRecorderServerVideoPhoneServer.TSession Session = TVideoRecorderServerVideoPhoneServer.SessionServer.Session_Get(SessionID);
			if (Session == null) {
				WriteDescriptor(ErrorCode_SessionIsNotFound);
				return; //. ->
			}
			WriteDescriptor(SuccessCode_OK);
			//. 
			int SessionStatus = Session.GetStatus();
			if (SessionStatus >= TVideoRecorderServerVideoPhoneServer.TSession.SESSION_STATUS_STARTED) {
				TVideoRecorderServerVideoPhoneServer.SessionServer.CallSession(Session);
				//. wait for calling result
				try {
					SessionStatus = Session.WaitForStatus(TVideoRecorderServerVideoPhoneServer.TSession.SESSION_STATUS_OPENED, SessionTimeout);
				} catch (InterruptedException E) {
					return; //. ->
				}
				//.
				if (SessionStatus == TVideoRecorderServerVideoPhoneServer.TSession.SESSION_STATUS_CALL) 
					TVideoRecorderServerVideoPhoneServer.SessionServer.RejectSession(Session);
			}
			WriteDescriptor(SessionStatus);
			//.
			if (SessionStatus < TVideoRecorderServerVideoPhoneServer.TSession.SESSION_STATUS_ZERO)
				return; //. ->
			boolean flFinishSession = true;
			try {
				//. processing ...
				while (!Canceller.flCancel) {
					try {
						Descriptor = ReadDescriptor(); 
					}
					catch (SocketTimeoutException E) {
						continue; //. ^
					}
					switch (Descriptor) {
					
					case ClientCommand_Disconnect:
						flFinishSession = false;
						return; //. ->

					case ClientCommand_DisconnectAndFinishSession:
						return; //. ->

					case ClientCommand_Checkpoint:
						break; //. >

					case ClientCommand_ContactSession:
						TVideoRecorderServerVideoPhoneServer.SessionServer.ContactSession(Session);
						//. wait for calling result
						try {
							SessionStatus = Session.WaitForStatus(TVideoRecorderServerVideoPhoneServer.TSession.SESSION_STATUS_CONTACT, SessionTimeout);
						} catch (InterruptedException E) {
							return; //. ->
						}
						WriteDescriptor(SessionStatus);
						break; //. >

					case ClientCommand_CheckSessionStatus:
						SessionStatus = Session.GetStatus();
						WriteDescriptor(SessionStatus);
						break; //. >
						
					default:
						return; //. ->
					}
		        }
			}
			finally {
				if (flFinishSession)
					TVideoRecorderServerVideoPhoneServer.SessionServer.FinishSession(Session);
			}
			break; //. >
		
		default:
			WriteDescriptor(ErrorCode_IncorrectParameters);
			break; //. >
		}
	}	

	private int ReadDescriptor() throws IOException {
		DestinationConnectionInputStream.read(DescriptorBA);
		return TDataConverter.ConvertLEByteArrayToInt32(DescriptorBA,0);
	}
	
	private void WriteDescriptor(int pDescriptor) throws IOException {
		Descriptor = pDescriptor;
		TDataConverter.ConvertInt32ToLEByteArray(Descriptor,/*ref*/ DescriptorBA);
		DestinationConnectionOutputStream.write(DescriptorBA);
	}
}
