package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.IOException;

public class TLANConnectionUDPRepeater extends TConnectionUDPRepeater {

	@SuppressWarnings("unused")
	private String 	SourceAddress;
	@SuppressWarnings("unused")
	private int 	SourcePort;
    
	public TLANConnectionUDPRepeater(TLANModule pLANModule, String pSourceAddress, int pSourcePort, String pServerAddress, int pServerPort, String pDestinationUDPAddress, int pDestinationUDPPort, String pAddressData, int pConnectionID, String pUserAccessKey) {
		super(pLANModule,pServerAddress,pServerPort,pDestinationUDPAddress,pDestinationUDPPort,pAddressData,pConnectionID,pUserAccessKey);
		//.
		SourceAddress = pSourceAddress;
		SourcePort = pSourcePort;
		//.
		Start();
	}
	
	@Override
	protected void ConnectSource() throws IOException {
	}
	
	@Override
	protected void DisconnectSource() throws IOException {
	}	
	
	@Override
	public void DoReceiving(Thread ReceivingThread) throws IOException {
	}
	
	@Override
	public void DoTransmitting(Thread TransmittingThread) throws IOException {
	}	
}
