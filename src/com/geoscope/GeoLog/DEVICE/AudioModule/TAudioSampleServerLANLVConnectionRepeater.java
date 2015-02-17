package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TAudioSampleServerLANLVConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+1;

	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		return ((UserAccessKey == null) || LANModule.Device.AudioModule.UserAccessKey.Check(UserAccessKey));
	}

	public TAudioSampleServerLANLVConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, long pUserID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID, pUserID,pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TAudioSampleServerLANLVConnectionRepeater> RepeatersToCancel = new ArrayList<TAudioSampleServerLANLVConnectionRepeater>(1);
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TAudioSampleServerLANLVConnectionRepeater))
        			RepeatersToCancel.add(((TAudioSampleServerLANLVConnectionRepeater)CR));
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
		return false;
	}
	
	@Override
	public void DoReceiving(Thread ReceivingThread) throws Exception {
		TAudioModule AudioModule = LANModule.Device.AudioModule;
		AudioModule.AudioSampleServer_Connect();
		try {
			AudioModule.AudioSampleServer_Capturing(DestinationConnectionInputStream,DestinationConnectionOutputStream, Canceller);
		}
		finally {
			AudioModule.AudioSampleServer_Disconnect();
		}
	}
}
