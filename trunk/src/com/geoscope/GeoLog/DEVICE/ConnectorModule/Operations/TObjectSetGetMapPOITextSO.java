/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOITextValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetGetMapPOITextSO extends TObjectSetGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,6,1001);
    
	public static final int idTDescription = 2001;
	//.
    private static final int 		MapPOITextValuesCapacity = 1;
    private TMapPOITextValue[] 		MapPOITextValues = new TMapPOITextValue[MapPOITextValuesCapacity];
    private short 					MapPOITextValues_Count = 0;
    
    public TObjectSetGetMapPOITextSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        //.
        flComponentFileStream = true;
        //.
        String Params = "1"; //. Version
        try {
			AddressData = Params.getBytes("windows-1251");
		} catch (UnsupportedEncodingException E) {}
		//.
        Name = "Set/Get Map POI Text";
    }
    
    @Override
    public void Destroy()
    {
    	Finalize();
    }
    
    public void Finalize() {
        for (int I = 0; I < MapPOITextValues_Count; I++)
        	MapPOITextValues[I].Finalize();
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TMapPOITextValue value = (TMapPOITextValue)Value;
        MapPOITextValues[0] = value;
        MapPOITextValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (MapPOITextValues_Count == 0)
            return null; //. ->
        return MapPOITextValues[0];
    }
        
    @Override
    protected synchronized boolean ValueIsVariableSized() {
    	return true;
    }
    
    protected synchronized int ValueSize()
    {
        return 0;
    }
    
    public synchronized int ValueCount()
    {
        return MapPOITextValues_Count;
    }
    
    public synchronized int BatchSize()
    {
        int DataSize = 0;
        for (int I = 0; I < MapPOITextValues_Count; I++)
            DataSize += MapPOITextValues[I].ByteArraySize();
        return (DataSize);
    }
        
    public synchronized int Saving_BatchSize() throws Exception
    {
        int DataSize = 0;
        for (int I = 0; I < MapPOITextValues_Count; I++)
            DataSize += MapPOITextValues[I].Saving_ByteArraySize();
        return (DataSize);
    }
        
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
    	TMapPOITextValue MapPOIDataFile = (TMapPOITextValue)Value;
        if ((MapPOITextValues_Count > 0) && (MapPOITextValues[MapPOITextValues_Count-1].IsValueTheSame(MapPOIDataFile)))
            return true; //. ->
        if (MapPOITextValues_Count >= MapPOITextValuesCapacity)
            return false; //. ->            
        MapPOITextValues[MapPOITextValues_Count] = MapPOIDataFile;
        MapPOITextValues_Count++;
        return true;
    }
    
    protected int TimeForCompletion(int Mult)
    {
        double MaxSecondsPerOperation = 30.0;
        return (super.TimeForCompletion(Mult)+(int)(Mult*MaxSecondsPerOperation));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > MapPOITextValuesCapacity)
            ValuesCount = MapPOITextValuesCapacity;
        MapPOITextValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
        	TMapPOITextValue Value = new TMapPOITextValue();
            Value.FromByteArray(BA,/*ref*/ Idx);
            //.
            MapPOITextValues[MapPOITextValues_Count] = Value;
            MapPOITextValues_Count++;
        }
    }
    
    public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > MapPOITextValuesCapacity)
            ValuesCount = MapPOITextValuesCapacity;
        MapPOITextValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
        	TMapPOITextValue Value = new TMapPOITextValue();
            Value.Saving_FromByteArray(BA,/*ref*/ Idx);
            //.
            if (Value.IsValid()) {
                MapPOITextValues[MapPOITextValues_Count] = Value;
                MapPOITextValues_Count++;
            }
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (MapPOITextValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOITextValues_Count; I++)
        {
            BA = MapPOITextValues[I].ToByteArray();
            if (BA == null)
            	BA = new byte[4]; //. DataSize = 0
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }

    protected synchronized byte[] Saving_PrepareData() throws Exception
    {
        if (MapPOITextValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Saving_BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOITextValues_Count; I++)
        {
            BA = MapPOITextValues[I].Saving_ToByteArray();
            if (BA == null)
            	BA = new byte[4]; //. DataSize = 0
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }
    
    @Override
    public synchronized int DoOnOperationCompletion() throws OperationException, InterruptedException, IOException
    {
    	if ((Result != null) && (Result.length >= 4)) {
    		String FileName = MapPOITextValues[0].FileName;
			int ComponentID = TDataConverter.ConvertLEByteArrayToInt32(Result,0);
			//.
			try {
				Connector.Device.ComponentFileStreaming.AddItem(idTDescription,ComponentID, FileName);
			} catch (Exception E) {
		    	File F = new File(FileName);
		    	F.delete();
		    	//.
	    		return ErrorCode_ObjectComponentOperation_SetValueError; //. ->
			}
			//.
    		return SuccessCode_OK; ///. ->
    	}
    	else
    		return ErrorCode_BadData; //. ->
    }  
    
    @Override
    public synchronized void DoOnOperationException(OperationException E) {
		String FileName = MapPOITextValues[0].FileName;
    	File F = new File(FileName);
    	F.delete();
    	//.
    	super.DoOnOperationException(E);
    }
}