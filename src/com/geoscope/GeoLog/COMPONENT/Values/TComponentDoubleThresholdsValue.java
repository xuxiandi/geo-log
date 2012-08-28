package com.geoscope.GeoLog.COMPONENT.Values;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.Utils.OleDate;

public class TComponentDoubleThresholdsValue extends TComponentTimestampedDataValue
{
	private int Size;

	public TComponentDoubleThresholdsValue(TComponent pOwner, int pID, String pName, int pSize) 
	{
		super(pOwner,pID,pName);
		//.
		Size = pSize;
	}

	public synchronized double[] GetThresholds() throws Exception
	{
		double[] Result;
		if (Value != null)
		{
			int Cnt = (int)(Value.length/8);
			if (Cnt != Size)
				throw new Exception("wrong thresholds size"); //. =>
			Result = new double[Size];
			int Idx = 0;
			for ( int I = 0; I < Size; I++)
			{
				Result[I] = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(Value,Idx); Idx+=8;
			};
		}
		else
			Result = null;
		if (Result == null)
			Result = new double[Size];
		return Result;
	}

	public void SetThresholds(double[] Thresholds) throws Exception
	{
		if (Thresholds.length != Size)
			throw new Exception("wrong thresholds size"); //. =>
		byte[] BA = new byte[Size*8];
		int Idx = 0;
		for ( int I = 0; I < Size; I++)
		{
			byte[] DoubleBA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Thresholds[I]);
			System.arraycopy(DoubleBA,0,BA,Idx,DoubleBA.length); Idx += 8;
		};
		synchronized (this)
		{
			Timestamp = OleDate.UTCCurrentTimestamp();
			Value = BA;
		};
	}
}
