package com.geoscope.GeoLog.DEVICE.LANModule;


public class TLANLocalVirtualConnectionRepeater extends TConnectionRepeater {

	public TLANLocalVirtualConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, long ppUserID, String pUserAccessKey) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID, ppUserID,pUserAccessKey);
	}
}
