package com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule;

import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANLocalVirtualConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;

public class TUSBPluginModuleLANLVConnectionRepeaterPacketted extends TLANLocalVirtualConnectionRepeater {

	public static final int Port = TLANModule.LocalVirtualConnection_PortBase+7;
	
	public static boolean CheckUserAccessKey(TLANModule LANModule, String UserAccessKey) {
		return (UserAccessKey == null);
	}
	
	public static final int INITIALIZATION_SUCCESS 	= 0;
	public static final int INITIALIZATION_ERROR 	= 1;
	
	public TUSBPluginModuleLANLVConnectionRepeaterPacketted(TLANModule pLANModule, String pDestinationAddress, int pDestinationPort, int pConnectionID, String pUserAccessKey) throws InterruptedException {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID,pUserAccessKey);
		//. cancel the same repeaters
    	ArrayList<TUSBPluginModuleLANLVConnectionRepeaterPacketted> RepeatersToCancel = new ArrayList<TUSBPluginModuleLANLVConnectionRepeaterPacketted>(1);
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if ((CR != this) && (CR instanceof TUSBPluginModuleLANLVConnectionRepeaterPacketted))
        			RepeatersToCancel.add(((TUSBPluginModuleLANLVConnectionRepeaterPacketted)CR));
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
		return true;
	}
	
	@Override
	public void DoReceiving(Thread ReceivingThread) throws IOException {
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
		while (!Canceller.flCancel) {
			try {
				Thread.sleep(1000000);
			} catch (InterruptedException e) {
				return; //. ->
			}
		}
	}
	
	@Override
	public void DoTransmitting(Thread TransmittingThread) throws IOException {
	}	
}
