package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS;

import java.io.IOException;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

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
	
	public TADSChannel(TSensorsModule pSensorsModule) {
		super(pSensorsModule);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void DoStreaming(final OutputStream pOutputStream, final TCanceller Canceller, int MaxDuration) throws IOException {
		TStreamChannel.TPacketSubscriber PacketSubscriber  = new TStreamChannel.TPacketSubscriber() {
    		@Override
    		protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
    			try {
        			pOutputStream.write(Packet, 0,PacketSize);
        			pOutputStream.flush();
    			}
    			catch (Exception E) {
    				Canceller.Cancel();
    			}
    		}
    	};
    	PacketSubscribers.Subscribe(PacketSubscriber);
    	try {
    		try {
    			if (MaxDuration > 0)
    				Thread.sleep(MaxDuration);
    			else
    				while (!Canceller.flCancel) 
    					Thread.sleep(100);
    		}
    		catch (InterruptedException IE) {
    		}
    	}
    	finally {
    		PacketSubscribers.Unsubscribe(PacketSubscriber);
    	}
	}

	@Override
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		return (new TADSChannelStreamer(this, pidTComponent,pidComponent, pChannelID, pConfiguration,pParameters));
	}
	
	private byte[] Timestamp_ToByteArray(double Timestamp) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(TimestampTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] BatteryVoltage_ToByteArray(int BatteryVoltage) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+4/*SizeOf(Int32)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)6/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(BatteryVoltageTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(BatteryVoltage);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] BatteryTemperature_ToByteArray(int BatteryTemperature) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+4/*SizeOf(Int32)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)6/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(BatteryTemperatureTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(BatteryTemperature);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] BatteryLevel_ToByteArray(int BatteryLevel) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+4/*SizeOf(Int32)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)6/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(BatteryLevelTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(BatteryLevel);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] BatteryHealth_ToByteArray(int BatteryHealth) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+4/*SizeOf(Int32)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)6/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(BatteryHealthTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(BatteryHealth);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] BatteryStatus_ToByteArray(int BatteryStatus) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+4/*SizeOf(Int32)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)6/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(BatteryStatusTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(BatteryStatus);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] BatteryPlugType_ToByteArray(int BatteryPlugType) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+4/*SizeOf(Int32)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)6/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(BatteryPlugTypeTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(BatteryPlugType);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] CellularConnectorSignalStrength_ToByteArray(int CellularConnectorSignalStrength) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+4/*SizeOf(Int32)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)6/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(CellularConnectorSignalStrengthTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(CellularConnectorSignalStrength);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	public void DoOnTimestamp(double Timestamp) throws Exception {
		byte[] BA = Timestamp_ToByteArray(Timestamp);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnBatteryVoltage(int BatteryVoltage) throws Exception {
		byte[] BA = BatteryVoltage_ToByteArray(BatteryVoltage);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnBatteryTemperature(int BatteryTemperature) throws Exception {
		byte[] BA = BatteryTemperature_ToByteArray(BatteryTemperature);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnBatteryLevel(int BatteryLevel) throws Exception {
		byte[] BA = BatteryLevel_ToByteArray(BatteryLevel);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnBatteryHealth(int BatteryHealth) throws Exception {
		byte[] BA = BatteryHealth_ToByteArray(BatteryHealth);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnBatteryStatus(int BatteryStatus) throws Exception {
		byte[] BA = BatteryStatus_ToByteArray(BatteryStatus);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnBatteryPlugType(int BatteryPlugType) throws Exception {
		byte[] BA = BatteryPlugType_ToByteArray(BatteryPlugType);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnCellularConnectorSignalStrength(int CellularConnectorSignalStrength) throws Exception {
		byte[] BA = CellularConnectorSignalStrength_ToByteArray(CellularConnectorSignalStrength);
		PacketSubscribers.DoOnPacket(BA);
	}
}
