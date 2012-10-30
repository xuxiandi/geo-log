package com.geoscope.GeoLog.DEVICE.LANModule;


public class TLANLocalVirtualConnectionRepeater extends TConnectionRepeater {

	public static int Port() {
		return TLANModule.LocalVirtualConnection_PortBase;
	}
	
	public TLANLocalVirtualConnectionRepeater(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID);
		//.
		Start();
	}
}
