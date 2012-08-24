/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPIModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedShortValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;

/**
 *
 * @author ALXPONOM
 */
public class TGPIFixValue extends TComponentValue
{
    public static final int TGPIFixValueSize = TComponentTimestampedShortValue.ValueSize+TGPSFixValue.TGPSFixValueSize;
    
    public TComponentTimestampedShortValue	GPIValue = new TComponentTimestampedShortValue();
    public TGPSFixValue         			GPSFix = new TGPSFixValue();

    public TGPIFixValue()
    {
    }
    
    public TGPIFixValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TGPIFixValue(TComponentTimestampedShortValue pGPIValue, TGPSFixValue pGPSFix)
    {
        GPIValue.Assign(pGPIValue);
        GPSFix.Assign(pGPSFix);
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
        TGPIFixValue Src = (TGPIFixValue)pValue.getValue();
        GPIValue.Assign(Src.GPIValue);
        GPSFix.Assign(Src.GPSFix);
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TGPIFixValue(GPIValue,GPSFix);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TGPIFixValue Fix = (TGPIFixValue)AValue.getValue();
        return (GPIValue.IsValueTheSame(Fix.GPIValue) && GPSFix.IsValueTheSame(Fix.GPSFix));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        GPIValue.FromByteArray(BA,/*ref*/ Idx);
        GPSFix.FromByteArray(BA,/*ref*/ Idx);
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
        byte[] GPIValueData = GPIValue.ToByteArray();
        byte[] GPSFixData = GPSFix.ToByteArray();
        byte[] Result = new byte[GPIValueData.length+GPSFixData.length];
        System.arraycopy(GPIValueData,0,Result,0,GPIValueData.length); 
        System.arraycopy(GPSFixData,0,Result,GPIValueData.length,GPSFixData.length); 
        return Result;
    }
}
