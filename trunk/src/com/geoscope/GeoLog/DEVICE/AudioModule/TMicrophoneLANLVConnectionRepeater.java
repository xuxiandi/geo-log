package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TMicrophoneLANLVConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+2;
	//.
	public static final int INITIALIZATION_SUCCESS 	= 0;
	public static final int INITIALIZATION_ERROR 	= 1;
	
	public TMicrophoneLANLVConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID);
		//. cancel the same repeaters
    	ArrayList<TMicrophoneLANLVConnectionRepeater> RepeatersToCancel = new ArrayList<TMicrophoneLANLVConnectionRepeater>(1);
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TMicrophoneLANLVConnectionRepeater))
        			RepeatersToCancel.add(((TMicrophoneLANLVConnectionRepeater)CR));
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
		int RC = INITIALIZATION_SUCCESS;
		//.
		byte[] Descriptor = new byte[4];
		Descriptor[0] = (byte)(RC & 0xff);
		Descriptor[1] = (byte)(RC >> 8 & 0xff);
		Descriptor[2] = (byte)(RC >> 16 & 0xff);
		Descriptor[3] = (byte)(RC >>> 24);
		//.
		DestinationConnectionOutputStream.write(Descriptor,0,Descriptor.length);
		//.
		TAudioModule AudioModule = LANModule.Device.AudioModule;
		AudioModule.Microphone_Initialize();
		try {
			AudioModule.Microphone_Recording(DestinationConnectionOutputStream, Canceller);
		}
		finally {
			AudioModule.Microphone_Finalize();
		}
	}	
}
