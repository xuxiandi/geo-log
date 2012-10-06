package com.geoscope.GeoLog.COMPONENT;


public class TComponentTimestampedValue extends TComponentValue
{
	public double Timestamp = 0.0;
	
	public TComponentTimestampedValue() 
	{
		super();
	}

	public TComponentTimestampedValue(TComponent pOwner, int pID, String pName) 
	{
		super(pOwner, pID, pName);
	}

    public synchronized double GetTimestamp() 
    {
        return Timestamp;
    }
}