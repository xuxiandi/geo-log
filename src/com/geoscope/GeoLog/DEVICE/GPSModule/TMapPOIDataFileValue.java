/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.FileOutputStream;
import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TMapPOIDataFileValue extends TComponentValue
{

	public static String Lock = "";

    public TMapPOIDataFileValue()
    {
    }
    
    public TMapPOIDataFileValue(double pTimestamp, String pFileName, byte[] pData) throws IOException
    {
    	setValues(pTimestamp,pFileName,pData);
    }
    
    public TMapPOIDataFileValue(String pDataFileName) {
    	DataFileName = pDataFileName;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
    	TMapPOIDataFileValue Src = (TMapPOIDataFileValue)pValue.getValue();
        DataFileName = Src.DataFileName;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TMapPOIDataFileValue(DataFileName);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TMapPOIDataFileValue MapPOIDataFile = (TMapPOIDataFileValue)AValue.getValue();
        return (DataFileName.equals(MapPOIDataFile.DataFileName));
    }
    
    public synchronized void setValues(double pTimestamp, String pFileName, byte[] pData) throws IOException
    {
    	synchronized (Lock) {
        	DataFileName_Assign(TConnectorModule.OutgoingSetOperationsQueueDataFolderName, pTimestamp);
        	FileOutputStream FOS = new FileOutputStream(DataFileName);
        	try {
            	int Size = 8;
            	int FNS = pFileName.length();
            	if (FNS > 255)
            		FNS = 255;
            	Size += 1/*SizeOf(FileNameSize)*/+FNS;
                if (pData != null)
                    Size += pData.length;
                //.
                byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(Size);
                FOS.write(BA);
                //.
                BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(pTimestamp);
                FOS.write(BA);
                //.
                BA = new byte[1];
                BA[0] = (byte)FNS; 
                FOS.write(BA);
                if (FNS > 0) {
                	BA = pFileName.getBytes("windows-1251");
                    FOS.write(BA);
                }
                //.
                if (pData != null)             
                    FOS.write(pData);
        	}
        	finally {
        		FOS.close();
        	}
		}
        //.
        flSet = true;
    }
    
    public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    	DataFileName_FromByteArray(BA, Idx);
        //.
        flSet = true;
    }
    
    public synchronized byte[] ToByteArray() throws IOException
    {
    	synchronized (Lock) {
			return DataFile_ToByteArray(); 
		}
    }

    @Override
    public int ByteArraySize()
    {
    	synchronized (Lock) {
			return DataFile_Size(); 
		}
    }
}
