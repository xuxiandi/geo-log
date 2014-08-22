/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations;

import java.io.File;
import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIDataFileValue;
/**
 *
 * @author ALXPONOM
 */
public class TObjectSetGetMapPOIDataFileSO extends TObjectSetGetComponentDataServiceOperation
{
    public static TElementAddress _Address = new TElementAddress(2,4,6,1002);
    
	public static final int idTDATAFile = 2018;
	//.
    private static final int 		MapPOIDataFileValuesCapacity = 1;
    private TMapPOIDataFileValue[] 	MapPOIDataFileValues = new TMapPOIDataFileValue[MapPOIDataFileValuesCapacity];
    private short 					MapPOIDataFileValues_Count = 0;
    
    public TObjectSetGetMapPOIDataFileSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress, byte[] pAddressData) {
        super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
        //.
        flComponentFileStream = true;
        //.
        AddressData = pAddressData;
		//.
        Name = "Set/Get Map POI DataFile";
    }
    
    public TObjectSetGetMapPOIDataFileSO(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress) {
    	this(pConnector, pUserID,pUserPassword, pObjectID, pSubAddress, null);
	}

    @Override
    public void Destroy()
    {
    	Finalize();
    }
    
    public void Finalize() {
        for (int I = 0; I < MapPOIDataFileValues_Count; I++)
        	MapPOIDataFileValues[I].Finalize();
    }
    
    public TElementAddress Address()
    {
        return _Address.AddRight(super.Address());
    }
        
    public synchronized void setValue(TComponentValue Value)
    {
        TMapPOIDataFileValue value = (TMapPOIDataFileValue)Value;
        MapPOIDataFileValues[0] = value;
        MapPOIDataFileValues_Count = 1;
    }
        
    public synchronized TComponentValue getValue()
    {
        if (MapPOIDataFileValues_Count == 0)
            return null; //. ->
        return MapPOIDataFileValues[0];
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
        return MapPOIDataFileValues_Count;
    }
    
    @Override
    public synchronized int BatchSize() throws IOException
    {
        int DataSize = 0;
        for (int I = 0; I < MapPOIDataFileValues_Count; I++)
            DataSize += MapPOIDataFileValues[I].ByteArraySize();
        return (DataSize);
    }
        
    public synchronized int Saving_BatchSize()
    {
        int DataSize = 0;
        for (int I = 0; I < MapPOIDataFileValues_Count; I++)
            DataSize += MapPOIDataFileValues[I].Saving_ByteArraySize();
        return (DataSize);
    }
        
    public synchronized boolean AddNewValue(TComponentValue Value) 
    {
        TMapPOIDataFileValue MapPOIDataFile = (TMapPOIDataFileValue)Value;
        if ((MapPOIDataFileValues_Count > 0) && (MapPOIDataFileValues[MapPOIDataFileValues_Count-1].IsValueTheSame(MapPOIDataFile)))
            return true; //. ->
        if (MapPOIDataFileValues_Count >= MapPOIDataFileValuesCapacity)
            return false; //. ->            
        MapPOIDataFileValues[MapPOIDataFileValues_Count] = MapPOIDataFile;
        MapPOIDataFileValues_Count++;
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
        if (ValuesCount > MapPOIDataFileValuesCapacity)
            ValuesCount = MapPOIDataFileValuesCapacity;
        MapPOIDataFileValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TMapPOIDataFileValue Value = new TMapPOIDataFileValue();
            Value.FromByteArray(BA,/*ref*/ Idx);
            //.
            MapPOIDataFileValues[MapPOIDataFileValues_Count] = Value;
            MapPOIDataFileValues_Count++;
        }
    }
    
    public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        int ValuesCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        if (ValuesCount > MapPOIDataFileValuesCapacity)
            ValuesCount = MapPOIDataFileValuesCapacity;
        MapPOIDataFileValues_Count = 0;
        for (int I = 0; I < ValuesCount; I++)
        {
            TMapPOIDataFileValue Value = new TMapPOIDataFileValue();
            Value.Saving_FromByteArray(BA,/*ref*/ Idx);
            //.
            if (Value.IsValid()) {
                MapPOIDataFileValues[MapPOIDataFileValues_Count] = Value;
                MapPOIDataFileValues_Count++;
            }
        }
    }
    
    protected synchronized byte[] PrepareData() throws IOException
    {
        if (MapPOIDataFileValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOIDataFileValues_Count; I++)
        {
            BA = MapPOIDataFileValues[I].ToByteArray();
            if (BA == null)
            	BA = new byte[4]; //. DataSize = 0
            System.arraycopy(BA, 0, Result, Idx, BA.length); Idx+=BA.length;
        }
        return Result;
    }

    protected synchronized byte[] Saving_PrepareData() throws Exception
    {
        if (MapPOIDataFileValues_Count == 0)
            return null; //. =>
        byte[] Result = new byte[Saving_BatchSize()];
        byte[] BA;
        int Idx = 0;
        for (int I = 0; I < MapPOIDataFileValues_Count; I++)
        {
            BA = MapPOIDataFileValues[I].Saving_ToByteArray();
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
    		String FileName = MapPOIDataFileValues[0].FileName;
			int ComponentID = TDataConverter.ConvertLEByteArrayToInt32(Result,0);
			//.
			try {
				Connector.Device.ComponentFileStreaming.AddItem(idTDATAFile,ComponentID, FileName);
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
		String FileName = MapPOIDataFileValues[0].FileName;
    	File F = new File(FileName);
    	F.delete();
    	//.
    	super.DoOnOperationException(E);
    }
}