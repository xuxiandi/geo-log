package com.geoscope.GeoLog.DEVICE.LANModule;


public class TLANLocalVirtualConnectionUDPRepeater extends TConnectionUDPRepeater {

	public TLANLocalVirtualConnectionUDPRepeater(TLANModule pLANModule, String pServerAddress, int pServerPort, String pDestinationUDPAddress, int pDestinationUDPPort, int pConnectionID, String pUserAccessKey) {
		super(pLANModule,pServerAddress,pServerPort,pDestinationUDPAddress,pDestinationUDPPort,pConnectionID,pUserAccessKey);
	}
}
