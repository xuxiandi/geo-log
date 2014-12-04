package com.geoscope.GeoLog.DEVICE.LANModule;

public class TLANDeviceConnectionRepeater extends TLANLocalVirtualConnectionRepeater {

	public TLANDeviceConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, long pUserID, String pUserAccessKey) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID, pUserID,pUserAccessKey);
	}
}
