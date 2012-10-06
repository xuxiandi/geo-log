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
public class TMapPOIImageByteArrayValue extends TComponentValue
{

	public double Timestamp;
    public byte[] ImageData;

    public TMapPOIImageByteArrayValue()
    {
    }
    
    public TMapPOIImageByteArrayValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TMapPOIImageByteArrayValue(double pTimestamp, byte[] pImageData)
    {
    	Timestamp = pTimestamp;
        ImageData = pImageData;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
        TMapPOIImageByteArrayValue Src = (TMapPOIImageByteArrayValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        ImageData = Src.ImageData;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TMapPOIImageByteArrayValue(Timestamp,ImageData);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TMapPOIImageByteArrayValue MapPOIImage = (TMapPOIImageByteArrayValue)AValue.getValue();
        return ((Timestamp == MapPOIImage.Timestamp) && (ImageData == MapPOIImage.ImageData));
    }
    
    public synchronized void setValues(double pTimestamp, byte[] pImageData)
    {
    	Timestamp = pTimestamp;
        ImageData = pImageData;
        //.
        flSet = true;
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int DataSize = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        int ImageDataSize = DataSize-8;
        ImageData = new byte[ImageDataSize];
        System.arraycopy(BA,Idx.Value, ImageData,0, ImageDataSize); Idx.Value += ImageDataSize;
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	int DataSize = 8;
        int ImageDataSize = 0;
        if (ImageData != null)
            ImageDataSize = ImageData.length;
        DataSize += ImageDataSize; 
        byte[] Result = new byte[4/*SizeOf(DataSize)*/+DataSize];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(DataSize);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        if (ImageDataSize > 0) {
            System.arraycopy(ImageData,0,Result,Idx,ImageDataSize); Idx+=ImageDataSize;
        }
        return Result;
    }

    @Override
    public int ByteArraySize()
    {
        int ImageDataSize = 0;
        if (ImageData != null)
            ImageDataSize = ImageData.length;
        return (4/*SizeOf(DataSize)*/+8+ImageDataSize);
    }
}
