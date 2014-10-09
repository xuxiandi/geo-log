package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;

public class TENVCChannel extends TStreamChannel {

	public static final String TypeID = "EnvironmentalConditions.ENVC";

	public static final int DescriptorSize = 2;
	//.
	public static final short TimestampTag 		= 1;
	public static final short TemperatureTag	= 2;
	public static final short PressureTag		= 3;
	public static final short HumidityTag		= 4;
	
	public static class TDoOnValueHandler {
		
		public void DoOnValue(double Value) {
		}
	}
	
	
	public TDoOnValueHandler OnTimestampHandler = null;
	public TDoOnValueHandler OnTemperatureHandler = null;
	public TDoOnValueHandler OnPressureHandler = null;
	public TDoOnValueHandler OnHumidityHandler = null;
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(InputStream pInputStream, OutputStream pOutputStream, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws IOException {
		//. send Version
		int Version = 1;
		byte[] Descriptor = TDataConverter.ConvertInt32ToLEByteArray(Version);
		pOutputStream.write(Descriptor);
		//. send ChannelID
		Descriptor = TDataConverter.ConvertInt32ToLEByteArray(ID);
		pOutputStream.write(Descriptor);
		//. get and check result
		pInputStream.read(Descriptor);
		int RC = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
		if (RC != TSensorsModule.SENSORSSTREAMINGSERVER_MESSAGE_OK)
			throw new IOException("error of connecting to the sernsors streaming server, RC: "+Integer.toString(RC)); //. =>
		//.
		byte[] TransferBuffer = new byte[DescriptorSize];
		short Size;
		int BytesRead;
		while (!Canceller.flCancel) {
			try {
                BytesRead = pInputStream.read(TransferBuffer,0,DescriptorSize);
                if (BytesRead <= 0) 
                	break; //. >
			}
			catch (SocketTimeoutException E) {
				OnIdleHandler.DoOnIdle(Canceller);
				//.
				continue; //. ^
			}
			if (BytesRead != DescriptorSize)
				throw new IOException("wrong data descriptor"); //. =>
			Size = (short)(((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF));
			if (Size > 0) { 
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				BytesRead = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, Size);	
                if (BytesRead <= 0) 
                	break; //. >
				//. parse and process
                try {
                	ParseFromByteArrayAndProcess(TransferBuffer, 0);
                }
                catch (Exception E) {
                	break; //. >
                }
			}
		}    	
	}		
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		short Descriptor = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += DescriptorSize;
		switch (Descriptor) {
		
		case TimestampTag:
			double Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnTimestampHandler != null)
				OnTimestampHandler.DoOnValue(Timestamp);
			break; //. >

		case TemperatureTag:
			double Temperature = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnTemperatureHandler != null)
				OnTemperatureHandler.DoOnValue(Temperature);
			break; //. >

		case PressureTag:
			double Pressure = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnPressureHandler != null)
				OnPressureHandler.DoOnValue(Pressure);
			break; //. >

		case HumidityTag:
			double Humidity = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnHumidityHandler != null)
				OnHumidityHandler.DoOnValue(Humidity);
			break; //. >
		}
		return Idx;
	}
}
