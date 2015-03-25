package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;

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
	public void DoStreaming(Socket Connection, InputStream pInputStream, int pInputStreamSize, OutputStream pOutputStream, TOnProgressHandler OnProgressHandler, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws Exception {
		byte[] TransferBuffer = new byte[DescriptorSize];
		short Size;
		int BytesRead,BytesRead1;
		int IdleTimeoutCount = 0; 
		int _StreamingTimeout = StreamingTimeout*IdleTimeoutCounter;
		while (!Canceller.flCancel) {
			try {
				Connection.setSoTimeout(StreamingTimeout);
                BytesRead = pInputStream.read(TransferBuffer,0,DescriptorSize);
                if (BytesRead <= 0) 
                	break; //. >
				IdleTimeoutCount = 0;
			}
			catch (SocketTimeoutException E) {
				IdleTimeoutCount++;
				if (IdleTimeoutCount >= IdleTimeoutCounter) {
					IdleTimeoutCount = 0;
					OnIdleHandler.DoOnIdle(Canceller);
				}
				//.
				continue; //. ^
			}
			if (BytesRead != DescriptorSize)
				throw new IOException("wrong data descriptor"); //. =>
			//.
			BytesRead1 = 0;
			Size = (short)(((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF));
			if (Size > 0) { 
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				Connection.setSoTimeout(_StreamingTimeout);
				BytesRead1 = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, Size);	
                if (BytesRead1 <= 0) 
                	break; //. >
				//. parse and process
            	ParseFromByteArrayAndProcess(TransferBuffer, 0);
            	//.
            	OnProgressHandler.DoOnProgress(BytesRead1, Canceller);
			}
			//.
			if (pInputStreamSize > 0) {
				pInputStreamSize -= (BytesRead+BytesRead1);
				if (pInputStreamSize <= 0)
                	break; //. >
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
