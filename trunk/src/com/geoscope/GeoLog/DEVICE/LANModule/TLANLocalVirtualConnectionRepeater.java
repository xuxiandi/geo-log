package com.geoscope.GeoLog.DEVICE.LANModule;


public class TLANLocalVirtualConnectionRepeater extends TConnectionRepeater {

	public TLANLocalVirtualConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID);
	}
}
