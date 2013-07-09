package com.geoscope.GeoLog.DEVICE.VideoModule;

import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TVideoFrameServerLANLVConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+2;
	
	public TVideoFrameServerLANLVConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID);
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
	public void DoReceiving() throws IOException {
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
