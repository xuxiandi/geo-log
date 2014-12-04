package com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule.TUSBPluginModule.TDoOnMessageIsReceivedHandler;

public class TUSBPluginModuleLANLVConnectionRepeaterPacketted extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+7;
	
	public static final int PROTOCOL_ECHO 			= 0;
	public static final int PROTOCOL_SIMPLESTRING 	= 1;
	
	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		return (UserAccessKey == null);
	}
	
	public static final int INITIALIZATION_SUCCESS 	= 0;
	public static final int INITIALIZATION_ERROR 	= -1;
	
	
	private int Protocol = PROTOCOL_ECHO;
	
	public TUSBPluginModuleLANLVConnectionRepeaterPacketted(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, long pUserID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID, pUserID,pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TUSBPluginModuleLANLVConnectionRepeaterPacketted> RepeatersToCancel = new ArrayList<TUSBPluginModuleLANLVConnectionRepeaterPacketted>(1);
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TUSBPluginModuleLANLVConnectionRepeaterPacketted))
        			RepeatersToCancel.add(((TUSBPluginModuleLANLVConnectionRepeaterPacketted)CR));
        	}
		}
    	for (int I = 0; I < RepeatersToCancel.size(); I++)
    		RepeatersToCancel.get(I).CancelAndWait();
		//.
		Start();
	}
	
	@Override
	public boolean ReceivingIsAvaiable() {
		return false;
	}
	
	@Override
	public boolean TransmittingIsAvaiable() {
		return true;
	}
	
    protected static int InputStream_Read(InputStream Connection, byte[] Data, int DataSize) throws IOException {
        int SummarySize = 0;
        int ReadSize;
        int Size;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = Connection.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	return Size; //. ->
            SummarySize += Size;
        }
        return SummarySize;
    }

    @Override
	public void DoTransmitting(Thread TransmittingThread) throws IOException {
		byte[] Descriptor = new byte[4];		
		//. read protocol version
		DestinationConnectionInputStream.read(Descriptor);
		Protocol = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
		//.
		try {
			switch (Protocol) {
			
			case PROTOCOL_ECHO: //. echo protocol
				Descriptor = TDataConverter.ConvertInt32ToLEByteArray(INITIALIZATION_SUCCESS);
				DestinationConnectionOutputStream.write(Descriptor);
				//.
				byte[] TransferBuffer = new byte[TransferBufferSize];
				int Size;
				while (!Canceller.flCancel) {
					try {
		                Size = DestinationConnectionInputStream.read(Descriptor);
		                if (Size <= 0) 
		                	break; //. >
					}
					catch (SocketTimeoutException E) {
						continue; //. ^
					}
					if (Size != Descriptor.length)
						throw new IOException("wrong data descrptor"); //. =>
					Size = (Descriptor[3] << 24)+((Descriptor[2] & 0xFF) << 16)+((Descriptor[1] & 0xFF) << 8)+(Descriptor[0] & 0xFF);
					if (Size > 0) {
						if (Size > TransferBuffer.length)
							TransferBuffer = new byte[Size];
						Size = InputStream_Read(DestinationConnectionInputStream,TransferBuffer,Size);	
		                if (Size <= 0) 
		                	break; //. >
		                //. write echo
		                DestinationConnectionOutputStream.write(Descriptor);
		                DestinationConnectionOutputStream.write(TransferBuffer,0,Size);
		                DestinationConnectionOutputStream.flush();
					}
				}
				break; //. >
				
			case PROTOCOL_SIMPLESTRING: //. simple string protocol
				TUSBPluginModule USBPluginModule = LANModule.Device.PluginsModule.USBPluginModule;
				//.
				Descriptor = TDataConverter.ConvertInt32ToLEByteArray(INITIALIZATION_SUCCESS);
				DestinationConnectionOutputStream.write(Descriptor);
				//.
				USBPluginModule.SetDoOnMessageIsReceivedHandler(new TDoOnMessageIsReceivedHandler() {
					@Override
					public void DoOnMessageIsReceived(String Message) throws Exception {
						byte[] BA = Message.getBytes("utf-8");
						byte[] Descriptor = TDataConverter.ConvertInt32ToLEByteArray(BA.length);
						//. send response to the repeater client
		                DestinationConnectionOutputStream.write(Descriptor);
		                DestinationConnectionOutputStream.write(BA);
		                DestinationConnectionOutputStream.flush();
					};
				});
				try {
					TransferBuffer = new byte[TransferBufferSize];
					while (!Canceller.flCancel) {
						try {
			                Size = DestinationConnectionInputStream.read(Descriptor);
			                if (Size <= 0) 
			                	break; //. >
						}
						catch (SocketTimeoutException E) {
							continue; //. ^
						}
						if (Size != Descriptor.length)
							throw new IOException("wrong data descrptor"); //. =>
						Size = (Descriptor[3] << 24)+((Descriptor[2] & 0xFF) << 16)+((Descriptor[1] & 0xFF) << 8)+(Descriptor[0] & 0xFF);
						if (Size > 0) {
							if (Size > TransferBuffer.length)
								TransferBuffer = new byte[Size];
							Size = InputStream_Read(DestinationConnectionInputStream,TransferBuffer,Size);	
			                if (Size <= 0) 
			                	break; //. >
			                //. send command to the USB controller
			                String CMD = new String(TransferBuffer, 0,Size, "utf-8");
			                USBPluginModule.SendMessage(CMD);
						}
					}
				}
				finally {
					USBPluginModule.SetDoOnMessageIsReceivedHandler(null);
				}
				break; //. >
				
			default:
				Descriptor = TDataConverter.ConvertInt32ToLEByteArray(INITIALIZATION_ERROR);
				DestinationConnectionOutputStream.write(Descriptor);
				break; //. >
			}
		}
		catch (Exception E) {
			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(INITIALIZATION_ERROR);
			DestinationConnectionOutputStream.write(Descriptor);
		}
	}	
}
