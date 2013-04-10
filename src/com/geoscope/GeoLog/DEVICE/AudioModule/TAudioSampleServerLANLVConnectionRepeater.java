package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TAudioSampleServerLANLVConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+1;
	//.
	public TAudioSampleServerLANLVConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID);
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
	public void DoReceiving() throws IOException {
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
