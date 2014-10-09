package com.geoscope.GeoLog.DEVICE.SensorsModule;

import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TSensorsStreamingServerLANLVConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+9;
	
	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		return (UserAccessKey == null);
	}

	public TSensorsStreamingServerLANLVConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID,pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TSensorsStreamingServerLANLVConnectionRepeater> RepeatersToCancel = new ArrayList<TSensorsStreamingServerLANLVConnectionRepeater>(1);
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TSensorsStreamingServerLANLVConnectionRepeater))
        			RepeatersToCancel.add(((TSensorsStreamingServerLANLVConnectionRepeater)CR));
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
		TSensorsModule SensorsModule = LANModule.Device.SensorsModule;
		//.
		SensorsModule.SensorsStreamingServer_Connect();
		try {
			SensorsModule.SensorsStreamingServer_Streaming(DestinationConnectionInputStream,DestinationConnectionOutputStream, Canceller);
		}
		finally {
			SensorsModule.SensorsStreamingServer_Disconnect();
		}
	}
}
