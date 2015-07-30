package com.geoscope.GeoLog.DEVICE.VideoModule;

import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TVideoFrameServerLANLVConnectionUDPRepeater extends TLANLocalVirtualConnectionUDPRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+2;
	//.
	public static final int RepeatersLimit = 100;
	
	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		return ((UserAccessKey == null) || LANModule.Device.VideoModule.UserAccessKey.Check(UserAccessKey));
	}

	public TVideoFrameServerLANLVConnectionUDPRepeater(TLANModule pLANModule, String pServerAddress, int pServerPort, String pDestinationUDPAddress, int pDestinationUDPPort, int pDestinationUDPProxyType, String pAddressData, int pConnectionID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule, pServerAddress,pServerPort, pDestinationUDPAddress,pDestinationUDPPort,pDestinationUDPProxyType, pAddressData, pConnectionID, pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TVideoFrameServerLANLVConnectionUDPRepeater> RepeatersToCancel = new ArrayList<TVideoFrameServerLANLVConnectionUDPRepeater>(1);
    	synchronized (TConnectionUDPRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionUDPRepeater.Repeaters.size(); I++) {
        		TConnectionUDPRepeater CR = TConnectionUDPRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TVideoFrameServerLANLVConnectionUDPRepeater))
        			RepeatersToCancel.add(((TVideoFrameServerLANLVConnectionUDPRepeater)CR));
        	}
		}
    	for (int I = RepeatersLimit; I < RepeatersToCancel.size(); I++)
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
			VideoModule.VideoFrameServer_Capturing(AddressData, SourceUDPSocket, DestinationUDPAddress,DestinationUDPPort,DestinationUDPProxyType, ServerAddress,ServerPort, Canceller);
		}
		finally {
			VideoModule.VideoFrameServer_Disconnect();
		}
	}
}
