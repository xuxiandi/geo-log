package com.geoscope.Classes.Data.Stream.Channel.DataTypes;

import java.io.IOException;
import java.util.Locale;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;

public class TDoubleDataType extends TDataType {
	
	public static String ContainerTypeID() {
		return "Double";
	}
	

	public double Value;
	
	public TDoubleDataType() {
		super();
	}
	
	public TDoubleDataType(String pTypeID, int pID, String pName, String pInfo, String pValueUnit) {
		super(pTypeID, pID, pName,pInfo, pValueUnit);
	}
	
	public String GetContainerTypeID() {
		return ContainerTypeID();
	}

	@Override
	public void SetValue(Object pValue) {
		Value = (Double)pValue;
	}
	
	@Override
	public Object GetValue() {
		return Double.valueOf(Value);
	}
	
	@Override
	public String GetValueString() {
		return String.format(Locale.ENGLISH,"%.2f",Value);
	}
	
	@Override
	public int ByteArraySize() {
		return 8; //. SizeOf(Value)
	}
	
	@Override
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		Value = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8; //. SizeOf(Value)
		return Idx;
	}

	@Override
	public byte[] ToByteArray() throws IOException {
		return TDataConverter.ConvertDoubleToLEByteArray(Value);
	}
}
