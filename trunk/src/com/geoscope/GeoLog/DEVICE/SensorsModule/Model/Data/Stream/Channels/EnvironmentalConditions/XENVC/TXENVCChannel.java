package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC;

import java.io.IOException;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

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
	
	public TXENVCChannel(TSensorsModule pSensorsModule) {
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
		return (new TXENVCChannelStreamer(this, pidTComponent,pidComponent, pChannelID, pConfiguration,pParameters));
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
	
	private byte[] Temperature_ToByteArray(double Temperature) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(TemperatureTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Temperature);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Pressure_ToByteArray(double Pressure) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(PressureTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Pressure);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] RelativeHumidity_ToByteArray(double RelativeHumidity) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(RelativeHumidityTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(RelativeHumidity);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Light_ToByteArray(double Light) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(LightTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Light);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] Acceleration_ToByteArray(double Acceleration) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)10/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(AccelerationTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Acceleration);
		System.arraycopy(BA,0, Result,Idx, BA.length);
		return Result;
	}
	
	private byte[] MagneticField_ToByteArray(double MagneticField_X, double MagneticField_Y, double MagneticField_Z) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+3*8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)26/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(MagneticFieldTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(MagneticField_X); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(MagneticField_Y); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(MagneticField_Z); System.arraycopy(BA,0, Result,Idx, BA.length); 
		return Result;
	}
	
	private byte[] Gyroscope_ToByteArray(double Gyroscope_X, double Gyroscope_Y, double Gyroscope_Z) throws IOException {
		byte[] Result = new byte[DescriptorSize+2/*SizeOf(Tag)*/+3*8/*SizeOf(Double)*/];
		int Idx = 0;
		TDataConverter.ConvertInt16ToLEByteArray((short)26/*SizeOf(Data)*/, Result, Idx); Idx += DescriptorSize;
		TDataConverter.ConvertInt16ToLEByteArray(GyroscopeTag, Result, Idx); Idx += DescriptorSize;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Gyroscope_X); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Gyroscope_Y); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Gyroscope_Z); System.arraycopy(BA,0, Result,Idx, BA.length); 
		return Result;
	}
	
	public void DoOnTimestamp(double Timestamp) throws IOException {
		byte[] BA = Timestamp_ToByteArray(Timestamp);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnTemperature(double Temperature) throws IOException {
		byte[] BA = Temperature_ToByteArray(Temperature);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnPressure(double Pressure) throws IOException {
		byte[] BA = Pressure_ToByteArray(Pressure);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnRelativeHumidity(double RelativeHumidity) throws IOException {
		byte[] BA = RelativeHumidity_ToByteArray(RelativeHumidity);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnLight(double Light) throws IOException {
		byte[] BA = Light_ToByteArray(Light);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnAcceleration(double Acceleration) throws IOException {
		byte[] BA = Acceleration_ToByteArray(Acceleration);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnMagneticField(double MagneticField_X, double MagneticField_Y, double MagneticField_Z) throws IOException {
		byte[] BA = MagneticField_ToByteArray(MagneticField_X,MagneticField_Y,MagneticField_Z);
		PacketSubscribers.DoOnPacket(BA);
	}

	public void DoOnGyroscope(double Gyroscope_X, double Gyroscope_Y, double Gyroscope_Z) throws IOException {
		byte[] BA = Gyroscope_ToByteArray(Gyroscope_X,Gyroscope_Y,Gyroscope_Z);
		PacketSubscribers.DoOnPacket(BA);
	}
}
