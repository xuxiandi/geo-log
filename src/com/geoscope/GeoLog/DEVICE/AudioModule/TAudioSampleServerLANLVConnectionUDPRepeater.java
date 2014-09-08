package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TAudioSampleServerLANLVConnectionUDPRepeater extends TLANLocalVirtualConnectionUDPRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+1;

	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		return ((UserAccessKey == null) || LANModule.Device.AudioModule.UserAccessKey.Check(UserAccessKey));
	}

	public TAudioSampleServerLANLVConnectionUDPRepeater(TLANModule pLANModule, String pServerAddress, int pServerPort, String pDestinationUDPAddress, int pDestinationUDPPort, int pDestinationUDPProxyType, String pAddressData, int pConnectionID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule, pServerAddress,pServerPort, pDestinationUDPAddress,pDestinationUDPPort,pDestinationUDPProxyType, pAddressData, pConnectionID, pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TAudioSampleServerLANLVConnectionUDPRepeater> RepeatersToCancel = new ArrayList<TAudioSampleServerLANLVConnectionUDPRepeater>(1);
    	synchronized (TConnectionUDPRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionUDPRepeater.Repeaters.size(); I++) {
        		TConnectionUDPRepeater CR = TConnectionUDPRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TAudioSampleServerLANLVConnectionUDPRepeater))
        			RepeatersToCancel.add(((TAudioSampleServerLANLVConnectionUDPRepeater)CR));
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
	public void DoReceiving(Thread ReceivingThread) throws IOException, InterruptedException {
		TAudioModule AudioModule = LANModule.Device.AudioModule;
		AudioModule.AudioSampleServer_Connect();
		try {
			AudioModule.AudioSampleServer_Capturing(AddressData, SourceUDPSocket, DestinationUDPAddress,DestinationUDPPort,DestinationUDPProxyType, ServerAddress,ServerPort, Canceller);
		}
		finally {
			AudioModule.AudioSampleServer_Disconnect();
		}
	}
}
