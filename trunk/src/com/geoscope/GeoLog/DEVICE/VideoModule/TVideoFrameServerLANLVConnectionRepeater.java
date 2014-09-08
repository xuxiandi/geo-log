package com.geoscope.GeoLog.DEVICE.VideoModule;

import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TVideoFrameServerLANLVConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+2;
	
	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		return ((UserAccessKey == null) || LANModule.Device.VideoModule.UserAccessKey.Check(UserAccessKey));
	}

	public TVideoFrameServerLANLVConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID,pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TVideoFrameServerLANLVConnectionRepeater> RepeatersToCancel = new ArrayList<TVideoFrameServerLANLVConnectionRepeater>(1);
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TVideoFrameServerLANLVConnectionRepeater))
        			RepeatersToCancel.add(((TVideoFrameServerLANLVConnectionRepeater)CR));
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
		TVideoModule VideoModule = LANModule.Device.VideoModule;
		VideoModule.VideoFrameServer_Connect();
		try {
			VideoModule.VideoFrameServer_Capturing(DestinationConnectionInputStream,DestinationConnectionOutputStream, Canceller);
		}
		finally {
			VideoModule.VideoFrameServer_Disconnect();
		}
	}
}
