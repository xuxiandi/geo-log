package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes;

import java.io.IOException;
import java.util.Locale;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS.TGPSFixDataType;

public class TTimestampedInt166DoubleContainerType extends TContainerType {
	
	public static String ID() {
		return "TimestampedInt166Double";
	}
	
	public static class TValue {
		
		public double Timestamp;
		public short  Value;
		public double Value1;
		public double Value2;
		public double Value3;
		public double Value4;
		public double Value5;
		public double Value6;
		
		public TValue() {
		}

		public TValue(double pTimestamp, short pValue, double pValue1, double pValue2, double pValue3, double pValue4, double pValue5, double pValue6) {
			Timestamp = pTimestamp;
			Value = pValue;
			Value1 = pValue1;
			Value2 = pValue2;
			Value3 = pValue3;
			Value4 = pValue4;
			Value5 = pValue5;
			Value6 = pValue6;
		}
	}
	
	
	public TValue Value = new TValue();
	
	public TTimestampedInt166DoubleContainerType() {
		super();
	}
	
	@Override
	public TContainerType Clone() {
		TTimestampedInt166DoubleContainerType Result = new TTimestampedInt166DoubleContainerType();
		Result.Value.Timestamp = Value.Timestamp;
		Result.Value.Value = Value.Value;
		Result.Value.Value1 = Value.Value1;
		Result.Value.Value2 = Value.Value2;
		Result.Value.Value3 = Value.Value3;
		Result.Value.Value4 = Value.Value4;
		Result.Value.Value5 = Value.Value5;
		Result.Value.Value6 = Value.Value6;
		return Result;
	}
	
	@Override
	public String GetID() {
		return ID();
	}

	@Override
	public TDataType GetDataType(String DataTypeID, TChannel pChannel) {
		if (TGPSFixDataType.ID().equals(DataTypeID))
			return new TGPSFixDataType(this, pChannel); //. -> 
		else
			return super.GetDataType(DataTypeID, pChannel);  //. ->
	}
	
	@Override
	public void SetValue(Object pValue) {
		Value = (TValue)pValue;
	}
	
	@Override
	public Object GetValue() {
		return Value;
	}
	
	@Override
	public String GetValueString(Context context) {
		return String.format(Locale.ENGLISH,"%.2f",Value.Value1)+"; "+String.format(Locale.ENGLISH,"%.2f",Value.Value2)+"; "+String.format(Locale.ENGLISH,"%.2f",Value.Value3)+";  "+String.format(Locale.ENGLISH,"%.2f",Value.Value4)+"; "+String.format(Locale.ENGLISH,"%.2f",Value.Value5)+"; "+String.format(Locale.ENGLISH,"%.2f",Value.Value6);
	}
	
	@Override
	public int ByteArraySize() {
		return 8/*SizeOf(Timestamp)*/+2/*SizeOf(Short)*/+6*8/*SizeOf(Double)*/;
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value.Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Timestamp)
		Value.Value = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx);   Idx += 2; //. SizeOf(Value)
		Value.Value1 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value1)
		Value.Value2 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value2)
		Value.Value3 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value3)
		Value.Value4 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value4)
		Value.Value5 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value5)
		Value.Value6 = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value6)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[ByteArraySize()];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Timestamp);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt16ToLEByteArray(Value.Value);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value1);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value2);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value3);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value4);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value5);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length; 
		BA = TDataConverter.ConvertDoubleToLEByteArray(Value.Value6);
		System.arraycopy(BA,0, Result,Idx, BA.length); 
		return Result;
	}
}
