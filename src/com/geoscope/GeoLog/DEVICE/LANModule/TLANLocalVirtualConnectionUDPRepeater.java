package com.geoscope.GeoLog.DEVICE.LANModule;


public class TLANLocalVirtualConnectionUDPRepeater extends TConnectionUDPRepeater {

	public TLANLocalVirtualConnectionUDPRepeater(TLANModule pLANModule, String pServerAddress, int pServerPort, String pDestinationUDPAddress, int pDestinationUDPPort, int pDestinationUDPProxyType, String pAddressData, int pConnectionID, String pUserAccessKey) {
		super(pLANModule, pServerAddress,pServerPort, pDestinationUDPAddress,pDestinationUDPPort,pDestinationUDPProxyType, pAddressData, pConnectionID, pUserAccessKey);
	}
}
