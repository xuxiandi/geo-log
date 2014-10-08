package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.DeviceRotator.DVRT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.Types.DeviceRotator.TDeviceRotator;

public class TDVRTChannel extends TStreamChannel {

	public static final String TypeID = "DeviceRotator.DVRT";
	
	public static final int DescriptorSize = 4;
	//.
	public static final int LatitudeTag		= 1;
	public static final int LongitudeTag	= 2;
	
	
	public TDeviceRotator Value = new TDeviceRotator();
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	

	@Override
	public void DoStreaming(InputStream pInputStream, OutputStream pOutputStream, TCanceller Canceller) throws IOException {
		byte[] TransferBuffer = new byte[DescriptorSize];
		int Size;
		while (!Canceller.flCancel) {
			try {
                Size = pInputStream.read(TransferBuffer,0,DescriptorSize);
                if (Size <= 0) 
                	break; //. >
			}
			catch (SocketTimeoutException E) {
				continue; //. ^
			}
			if (Size != DescriptorSize)
				throw new IOException("wrong data descriptor"); //. =>
			Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
			if (Size > 0) { 
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				Size = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, Size);	
                if (Size <= 0) 
                	break; //. >
				//. parse and process
                try {
                	ParseFromByteArrayAndProcess(TransferBuffer, 0);
                	//. success
          			byte[] Descriptor = TDataConverter.ConvertInt32ToLEByteArray(com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule.CONTROLSSTREAMINGSERVER_MESSAGE_OK);
          			pOutputStream.write(Descriptor);		
                }
                catch (Exception E) {
                	//. error
          			byte[] Descriptor = TDataConverter.ConvertInt32ToLEByteArray(com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule.CONTROLSTREAMINGSERVER_MESSAGE_ERROR);
          			pOutputStream.write(Descriptor);		
                }
			}
		}    	
	}		
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		int Descriptor = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += DescriptorSize;
		com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel DC = (com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel)DestinationChannel;
		switch (Descriptor) {
		
		case LatitudeTag:
			Value.Latitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (DC != null) 
				DC.Latitude_SetValue(Value.Latitude);
			break; //. >

		case LongitudeTag:
			Value.Longitude = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (DC != null) 
				DC.Longitude_SetValue(Value.Longitude);
			break; //. >
		}
		return Idx;
	}
}
