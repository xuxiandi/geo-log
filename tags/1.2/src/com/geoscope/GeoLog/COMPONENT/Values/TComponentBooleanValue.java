package com.geoscope.GeoLog.COMPONENT.Values;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.Utils.OleDate;

public class TComponentBooleanValue extends TComponentByteValue {

	public TComponentBooleanValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

	public TComponentBooleanValue() {
	}
	
    public TComponentBooleanValue(boolean pValue) {
    	super((byte)(pValue ? 1 : 0));
    }
    
    public synchronized void SetBooleanValue(double pTimestamp, boolean pValue)
    {
    	super.SetValue((byte)(pValue ? 1 : 0));
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
