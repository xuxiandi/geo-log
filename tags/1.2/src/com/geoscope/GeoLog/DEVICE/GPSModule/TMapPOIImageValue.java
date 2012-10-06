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
public class TMapPOIImageValue extends TComponentValue
{

	public static String Lock = "";
	
    public TMapPOIImageValue()
    {
    }
    
    public TMapPOIImageValue(double pTimestamp, byte[] pData) throws IOException
    {
    	setValues(pTimestamp,pData);
    }
    
    public TMapPOIImageValue(String pDataFileName) {
    	DataFileName = pDataFileName;
        //.
        flSet = true;
    }
    
    public synchronized void Assign(TComponentValue pValue)
    {
        TMapPOIImageValue Src = (TMapPOIImageValue)pValue.getValue();
        DataFileName = Src.DataFileName;
        //.
        super.Assign(pValue);
    }
    
    public synchronized TComponentValue getValue()
    {
        return new TMapPOIImageValue(DataFileName);
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TMapPOIImageValue MapPOIImage = (TMapPOIImageValue)AValue.getValue();
        return (DataFileName.equals(MapPOIImage.DataFileName));
    }
    
    public void setValues(double pTimestamp, byte[] pData) throws IOException
    {
    	synchronized (Lock) {
        	DataFileName_Assign(TConnectorModule.OutgoingSetOperationsQueueDataFolderName, pTimestamp);
        	FileOutputStream FOS = new FileOutputStream(DataFileName);
        	try {
            	int SummaryDataSize = 8;
                int DataSize = 0;
                if (pData != null)
                    DataSize = pData.length;
                SummaryDataSize += DataSize; 
                byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(SummaryDataSize);
                FOS.write(BA);
                BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(pTimestamp);
                FOS.write(BA);
                if (DataSize > 0) 
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
