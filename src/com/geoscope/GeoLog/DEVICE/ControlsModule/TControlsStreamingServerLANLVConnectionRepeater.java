package com.geoscope.GeoLog.DEVICE.ControlsModule;

import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TControlsStreamingServerLANLVConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+8;
	//.
	public static final int RepeatersLimit = 10;
	
	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		return (UserAccessKey == null);
	}

	public TControlsStreamingServerLANLVConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID,pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TControlsStreamingServerLANLVConnectionRepeater> RepeatersToCancel = new ArrayList<TControlsStreamingServerLANLVConnectionRepeater>(1);
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TControlsStreamingServerLANLVConnectionRepeater))
        			RepeatersToCancel.add(((TControlsStreamingServerLANLVConnectionRepeater)CR));
        	}
		}
    	for (int I = RepeatersLimit; I < RepeatersToCancel.size(); I++)
    		RepeatersToCancel.get(I).Cancel();
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
		try {
			while (!Canceller.flCancel) 
				Thread.sleep(1000000);
		} catch (InterruptedException e) {
		}
	}
	
	@Override
	public void DoTransmitting(Thread TransmittingThread) throws IOException {
		TControlsModule ControlsModule = LANModule.Device.ControlsModule;
		//.
		ControlsModule.ControlsStreamingServer_Connect();
		try {
			ControlsModule.ControlsStreamingServer_Streaming(DestinationConnectionInputStream, DestinationConnectionOutputStream, Canceller);
		}
		finally {
			ControlsModule.ControlsStreamingServer_Disconnect();
		}
	}	
}
