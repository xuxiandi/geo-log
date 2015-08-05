package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;

public class TADSChannel extends TStreamChannel {

	public static final String TypeID = "AndroidState.ADS";

	public static final int DescriptorSize = 2;
	//.
	public static final short TimestampTag 							= 1;
	public static final short BatteryVoltageTag 					= 2;
	public static final short BatteryTemperatureTag 				= 3;
	public static final short BatteryLevelTag 						= 4;
	public static final short BatteryHealthTag 						= 5;
	public static final short BatteryStatusTag 						= 6;
	public static final short BatteryPlugTypeTag 					= 7;
	public static final short CellularConnectorSignalStrengthTag	= 8;
	
	public static class TDoOnDoubleValueHandler {
		
		public void DoOnValue(double Value) {
		}
	}	
	
	public static class TDoOnInt32ValueHandler {
		
		public void DoOnValue(int Value) {
		}
	}	
	
	
	public TDoOnDoubleValueHandler 	OnTimestampHandler = null;
	public TDoOnInt32ValueHandler 	OnBatteryVoltageHandler = null;
	public TDoOnInt32ValueHandler 	OnBatteryTemperatureHandler = null;
	public TDoOnInt32ValueHandler 	OnBatteryLevelHandler = null;
	public TDoOnInt32ValueHandler 	OnBatteryHealthHandler = null;
	public TDoOnInt32ValueHandler 	OnBatteryStatusHandler = null;
	public TDoOnInt32ValueHandler 	OnBatteryPlugTypeHandler = null;
	public TDoOnInt32ValueHandler 	OnCellularConnectorSignalStrengthHandler = null;
	
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
				if (Connection != null)
					Connection.setSoTimeout(StreamingTimeout);
                BytesRead = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, DescriptorSize);
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
				if (Connection != null)
					Connection.setSoTimeout(_StreamingTimeout);
				BytesRead1 = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, Size);	
                if (BytesRead1 <= 0) 
                	break; //. >
				//. parse and process
            	ParseFromByteArrayAndProcess(TransferBuffer, 0, Size);
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
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx, int Size) throws Exception {
		short Descriptor = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += DescriptorSize;
		switch (Descriptor) {
		
		case TimestampTag:
			double Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Double)
			if (OnTimestampHandler != null)
				OnTimestampHandler.DoOnValue(Timestamp);
			break; //. >

		case BatteryVoltageTag:
			int BatteryVoltage = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
			if (OnBatteryVoltageHandler != null)
				OnBatteryVoltageHandler.DoOnValue(BatteryVoltage);
			break; //. >

		case BatteryTemperatureTag:
			int BatteryTemperature = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
			if (OnBatteryTemperatureHandler != null)
				OnBatteryTemperatureHandler.DoOnValue(BatteryTemperature);
			break; //. >

		case BatteryLevelTag:
			int BatteryLevel = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
			if (OnBatteryLevelHandler != null)
				OnBatteryLevelHandler.DoOnValue(BatteryLevel);
			break; //. >

		case BatteryHealthTag:
			int BatteryHealth = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
			if (OnBatteryHealthHandler != null)
				OnBatteryHealthHandler.DoOnValue(BatteryHealth);
			break; //. >

		case BatteryStatusTag:
			int BatteryStatus = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
			if (OnBatteryStatusHandler != null)
				OnBatteryStatusHandler.DoOnValue(BatteryStatus);
			break; //. >

		case BatteryPlugTypeTag:
			int BatteryPlugType = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
			if (OnBatteryPlugTypeHandler != null)
				OnBatteryPlugTypeHandler.DoOnValue(BatteryPlugType);
			break; //. >

		case CellularConnectorSignalStrengthTag:
			int CellularConnectorSignalStrength = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
			if (OnCellularConnectorSignalStrengthHandler != null)
				OnCellularConnectorSignalStrengthHandler.DoOnValue(CellularConnectorSignalStrength);
			break; //. >
		}
		return Idx;
	}
}
