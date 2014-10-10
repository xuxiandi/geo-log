package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;

public class TXENVCChannel extends TStreamChannel {

	public static final String TypeID = "EnvironmentalConditions.XENVC";

	public static final int DescriptorSize = 2;
	//.
	public static final short TimestampTag 			= 1;
	public static final short TemperatureTag		= 2;
	public static final short PressureTag			= 3;
	public static final short RelativeHumidityTag	= 4;
	public static final short LightTag				= 5;
	public static final short AccelerationTag		= 6;
	public static final short MagneticFieldTag		= 7;
	public static final short GyroscopeTag			= 8;
	
	public static class TDoOnValueHandler {
		
		public void DoOnValue(double Value) {
		}
	}
	
	public static class TDoOn2ValueHandler {
		
		public void DoOn2Value(double Value, double Value1) {
		}
	}
	
	public static class TDoOn3ValueHandler {
		
		public void DoOn3Value(double Value, double Value1, double Value2) {
		}
	}
	
	
	public TDoOnValueHandler 	OnTimestampHandler = null;
	public TDoOnValueHandler 	OnTemperatureHandler = null;
	public TDoOnValueHandler 	OnPressureHandler = null;
	public TDoOnValueHandler 	OnRelativeHumidityHandler = null;
	public TDoOnValueHandler 	OnLightHandler = null;
	public TDoOnValueHandler 	OnAccelerationHandler = null;
	public TDoOn3ValueHandler	OnMagneticFieldHandler = null;
	public TDoOn3ValueHandler	OnGyroscopeHandler = null;
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(Socket Connection, InputStream pInputStream, OutputStream pOutputStream, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws IOException {
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
			throw new IOException("error of connecting to the sensors streaming server, RC: "+Integer.toString(RC)); //. =>
		//.
		byte[] TransferBuffer = new byte[DescriptorSize];
		short Size;
		int BytesRead;
		int IdleTimeoutCount = 0; 
		Connection.setSoTimeout(StreamingTimeout);
		while (!Canceller.flCancel) {
			try {
                BytesRead = pInputStream.read(TransferBuffer,0,DescriptorSize);
                if (BytesRead <= 0) 
                	break; //. >
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

		case RelativeHumidityTag:
			double Humidity = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnRelativeHumidityHandler != null)
				OnRelativeHumidityHandler.DoOnValue(Humidity);
			break; //. >
			
		case LightTag:
			double Light = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnLightHandler != null)
				OnLightHandler.DoOnValue(Light);
			break; //. >

		case AccelerationTag:
			double Acceleration = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnAccelerationHandler != null)
				OnAccelerationHandler.DoOnValue(Acceleration);
			break; //. >

		case MagneticFieldTag:
			double MagneticField_X = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			double MagneticField_Y = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			double MagneticField_Z = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnMagneticFieldHandler != null)
				OnMagneticFieldHandler.DoOn3Value(MagneticField_X,MagneticField_Y,MagneticField_Z);
			break; //. >

		case GyroscopeTag:
			double Gyroscope_X = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			double Gyroscope_Y = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			double Gyroscope_Z = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnGyroscopeHandler != null)
				OnGyroscopeHandler.DoOn3Value(Gyroscope_X,Gyroscope_Y,Gyroscope_Z);
			break; //. >
		}
		return Idx;
	}
}
