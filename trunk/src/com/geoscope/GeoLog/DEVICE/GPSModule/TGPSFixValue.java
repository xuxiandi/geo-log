/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.IOException;

import android.content.Context;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TGPSFixValue extends TComponentValue
{

    public static final int TGPSFixValueSize = 7*8/*SizeOf(double)*/;
    //.
    public static final double 	UnavailableFixPrecision = 1000000000.0;
    public static final double 	UnknownFixPrecision = -1000000000.0;
    //.
    public static final double 	FixUnknownPrecision = -1.0;
    public static final double 	FixDefaultPrecision = 30.0;
    
    public double ArrivedTimeStamp;
    public double TimeStamp;
    public double Latitude;
    public double Longitude;
    public double Altitude;
    public double Speed;
    public double Bearing;
    public double Precision = UnavailableFixPrecision;

    public TGPSFixValue()
    {
    }
    
    public TGPSFixValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TGPSFixValue(double pArrivedTimeStamp, double pTimeStamp,double pLatitude,double pLongitude,double pAltitude,double pSpeed,double pBearing,double pPrecision)
    {
    	ArrivedTimeStamp = pArrivedTimeStamp;
        TimeStamp = pTimeStamp;
        Latitude = pLatitude;
        Longitude = pLongitude;
        Altitude = pAltitude;
        Speed = pSpeed;
        Bearing = pBearing;
        Precision = pPrecision;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
        TGPSFixValue Src = (TGPSFixValue)pValue;
        ArrivedTimeStamp = Src.ArrivedTimeStamp;
        TimeStamp = Src.TimeStamp;
        Latitude = Src.Latitude;
        Longitude = Src.Longitude;
        Altitude = Src.Altitude;
        Speed = Src.Speed;
        Bearing = Src.Bearing;
        Precision = Src.Precision;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TGPSFixValue(ArrivedTimeStamp,TimeStamp,Latitude,Longitude,Altitude,Speed,Bearing,Precision);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TGPSFixValue Fix = (TGPSFixValue)AValue.getValue();
        return ((TimeStamp == Fix.TimeStamp) && (Latitude == Fix.Latitude) && (Longitude == Fix.Longitude) && (Altitude == Fix.Altitude) && (Speed == Fix.Speed) && (Bearing == Fix.Bearing) && (Precision == Fix.Precision));
    }
    
    public synchronized void setValues(double pArrivedTimeStamp, double pTimeStamp,double pLatitude,double pLongitude,double pAltitude,double pSpeed,double pBearing,double pPrecision)
    {
    	ArrivedTimeStamp = pArrivedTimeStamp;
        TimeStamp = pTimeStamp;
        Latitude = pLatitude;
        Longitude = pLongitude;
        Altitude = pAltitude;
        Speed = pSpeed;
        Bearing = pBearing;
        Precision = pPrecision;
        //.
        flSet = true;
    }
    
    public synchronized void setTimeStamp(double pTimeStamp)
    {
        TimeStamp = pTimeStamp;
        //.
        flSet = true;
    }
    
    public synchronized void setPrecision(double pPrecision)
    {
        Precision = pPrecision;
        //.
        flSet = true;
    }
    
    public synchronized void SetFixAsUnknown() {
        Precision = UnknownFixPrecision;
        //.
        flSet = true;
    }
    
    public synchronized boolean IsUnknown()
    {
        return (flSet && (Precision == UnknownFixPrecision));
    }
    
    public synchronized void SetFixAsUnAvailable(double pArrivedTimeStamp, double pTimestamp) {
    	ArrivedTimeStamp = pArrivedTimeStamp;
    	TimeStamp = pTimestamp;
        Precision = UnavailableFixPrecision;
        //.
        flSet = true;
    }
    
    public synchronized boolean IsAvailable()
    {
        return (flSet && (Precision != UnknownFixPrecision) && (Precision != UnavailableFixPrecision));
    }
    
    public synchronized boolean IsEmpty()
    {
        return (!flSet || ((Latitude == 0.0) && (Longitude == 0.0)));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        TimeStamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        Latitude = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        Longitude = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        Altitude = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        Speed = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        Bearing = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        Precision = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
        byte[] Result = new byte[7*8/*SizeOf(double)*/];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(TimeStamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Latitude);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Longitude);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Altitude);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Speed);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Bearing);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Precision);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        return Result;
    }
    
    @Override
    public synchronized String GetName(Context context) {
    	return context.getString(R.string.SLocation1);
    }

    @Override
    public synchronized int GetImageResID(int pWidth, int pHeight) {
    	return R.drawable.gpsfix_value;
    }
}
