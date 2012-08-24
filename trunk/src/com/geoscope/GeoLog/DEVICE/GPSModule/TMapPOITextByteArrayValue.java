/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TMapPOITextByteArrayValue extends TComponentValue
{

	public double Timestamp;
    public byte[] TextData;

    public TMapPOITextByteArrayValue()
    {
    }
    
    public TMapPOITextByteArrayValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TMapPOITextByteArrayValue(double pTimestamp, byte[] pTextData)
    {
    	Timestamp = pTimestamp;
        TextData = pTextData;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
        TMapPOITextByteArrayValue Src = (TMapPOITextByteArrayValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        TextData = Src.TextData;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TMapPOITextByteArrayValue(Timestamp,TextData);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TMapPOITextByteArrayValue MapPOIText = (TMapPOITextByteArrayValue)AValue.getValue();
        return ((Timestamp == MapPOIText.Timestamp) && (TextData == MapPOIText.TextData));
    }
    
    public synchronized void setValues(byte[] pTextData)
    {
        TextData = pTextData;
        //.
        flSet = true;
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int DataSize = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        int TextDataSize = DataSize-8;
        TextData = new byte[TextDataSize];
        System.arraycopy(BA,Idx.Value, TextData,0, TextDataSize); Idx.Value += TextDataSize;
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	int DataSize = 8;
        int TextDataSize = 0;
        if (TextData != null)
            TextDataSize = TextData.length;
        DataSize += TextDataSize; 
        byte[] Result = new byte[4/*SizeOf(DataSize)*/+DataSize];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(DataSize);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        if (TextDataSize > 0) {            
            System.arraycopy(TextData,0,Result,Idx,TextDataSize); Idx+=TextDataSize;
        }
        return Result;
    }

    @Override
    public int ByteArraySize()
    {
        int TextDataSize = 0;
        if (TextData != null)
            TextDataSize = TextData.length;
        return (4/*SizeOf(DataSize)*/+8+TextDataSize);
    }
}
