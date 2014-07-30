package com.geoscope.GeoLog.COMPONENT.Values;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.TComponent;

public class TComponentTimestampedBooleanValue extends TComponentTimestampedByteValue {

	public TComponentTimestampedBooleanValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

	public TComponentTimestampedBooleanValue() {
	}
	
    public TComponentTimestampedBooleanValue(double pTimestamp, boolean pValue) {
    	super(pTimestamp,(byte)(pValue ? 1 : 0));
    }
    
    public synchronized void SetBooleanValue(double pTimestamp, boolean pValue)
    {
    	super.SetValue(pTimestamp,(byte)(pValue ? 1 : 0));
    }       
    
    public synchronized boolean BooleanValue()
    {
        return (Value != 0);
    }       
    
    public String ToString() {
    	if (BooleanValue())
    		return "1"; //. ->
    	else
    		return "0";
    }
    
    public void FromString(String S) {
    	boolean V = true;
    	if (S.equals("0"))
    		V = false;
    	SetBooleanValue(OleDate.UTCCurrentTimestamp(),V);
    }
}
