/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.TaskModule;

import java.io.IOException;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.Utils.OleDate;

/**
 * @author ALXPONOM
 */
public class TTaskResultValue extends TComponentValue {
	private int		ObjectID;
	private String	MeasurementID;
	//.
	public double 	Timestamp;
    public int		Int32Value;
    public String	StringValue;
    private int		DataChangesCounter = 0;

    public TTaskResultValue() {
    }
    
    public TTaskResultValue(byte[] BA, TIndex Idx) throws IOException, OperationException {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TTaskResultValue(double pTimestamp, int pInt32Value, String pStringValue) {
    	Timestamp = pTimestamp;
    	//.
    	Int32Value = pInt32Value;
    	StringValue = pStringValue;
    }
    
    public synchronized int GetObjectID() {
    	return ObjectID;
    }
    
    public synchronized String GetObjectMeasurementID() {
    	return Integer.toString(ObjectID)+"/"+MeasurementID;
    }
    
    public synchronized void SetProperties(int pObjectID, String pMeasurementID) {
    	ObjectID = pObjectID;
    	MeasurementID = pMeasurementID;
    	//.
    	Timestamp = OleDate.UTCCurrentTimestamp();
    	Int32Value = 0;
    	StringValue = "";
        DataChangesCounter++;
    }
    
    @Override
    public synchronized void Assign(TComponentValue pValue) {
        TTaskResultValue Src = (TTaskResultValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Int32Value = Src.Int32Value;
        StringValue = Src.StringValue;
        DataChangesCounter++;
        //.
        super.Assign(pValue);
    }
    
    @Override
    public synchronized TComponentValue getValue() {
        return new TTaskResultValue(Timestamp,Int32Value,StringValue);
    }
    
    @Override
    public synchronized boolean IsValueTheSame(TComponentValue AValue) {
        TTaskResultValue V = (TTaskResultValue)AValue.getValue();
        return ((Timestamp == V.Timestamp) && (Int32Value == V.Int32Value) && (StringValue.equals(V.StringValue)));
    }
    
    public synchronized void setValues(double pTimestamp, int pInt32Value, String pStringValue) {
    	Timestamp = pTimestamp;
    	Int32Value = pInt32Value;
    	StringValue = pStringValue;
        DataChangesCounter++;
        //.
        flSet = true;
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
    	Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        Int32Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        int DataSize = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        byte[] Data = new byte[DataSize];
        if (DataSize > 0) {
        	System.arraycopy(BA,Idx.Value, Data,0, DataSize); Idx.Value += DataSize;
        	StringValue = new String(Data, 0,Data.length, "windows-1251");
        }
        else 
        	StringValue = "";
        DataChangesCounter++;
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception
    {
    	String OMID;
    	try {
    		OMID = new String(AddressData, 0,AddressData.length, "windows-1251");
    	}
    	catch (Exception E) {
    		OMID = "";
    	}
    	String[] SA = OMID.split("/");
    	@SuppressWarnings("unused")
		String OID = SA[0]; 
    	@SuppressWarnings("unused")
		String MID = SA[1]; 
    	//. save status into measurement folder
		/////// TDeviceMeasurement DM = new TDeviceMeasurement(TECGMeasurement.DataBase,Integer.parseInt(OID),MID);
		/////// TDeviceMeasurement.TStatus Status = DM.new TStatus();
		//.
		int Index = Idx.Value; 
		Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Index); Index+=8;
		Int32Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Index); Index+=4;
        int DataSize = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Index); Index+=4;
        byte[] Data = new byte[DataSize];
        if (DataSize > 0) {
        	System.arraycopy(BA,Index, Data,0, DataSize); Index += DataSize;
        	StringValue = new String(Data, 0,Data.length, "windows-1251");
        }
        else 
        	StringValue = "";
        //.
        /////// DM.SetStatus(Status);
    	//.
    	FromByteArray(BA,Idx);
        //.
        super.FromByteArrayByAddressData(BA,/*ref*/ Idx, AddressData);
    }
    
    @Override
    public synchronized byte[] ToByteArray() throws IOException
    {
        byte[] TSBA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(Int32Value);
        int DataSize = 0;
        byte[] Data = StringValue.getBytes("windows-1251");
        if (Data != null)
            DataSize = Data.length;
        byte[] Result = new byte[8/*SizeOf(Timestamp)*/+4/*SizeOf(Int32Value)*/+4/*SizeOf(DataSize)*/+DataSize];
        byte[] BA1 = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(DataSize);
        int Idx = 0;
        System.arraycopy(TSBA,0,Result,Idx,TSBA.length); Idx += TSBA.length;
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx += BA.length;
        System.arraycopy(BA1,0,Result,Idx,BA1.length); Idx += BA1.length;
        if (DataSize > 0) {           
            System.arraycopy(Data,0,Result,Idx,DataSize); Idx+=DataSize;
        }
        return Result;
    }

    @Override
    public synchronized int ByteArraySize() throws Exception {
        int DataSize = 0;
        byte[] Data = StringValue.getBytes("windows-1251");
        if (Data != null)
            DataSize = Data.length;
        return (8/*SizeOf(Double)*/+4/*SizeOf(Int32Value)*/+4/*SizeOf(DataSize)*/+DataSize);
    }
    
    public synchronized double GetTimestamp() {
    	return Timestamp;
    }
    
    public synchronized int GetInt32Value() {
    	return Int32Value;
    }
    
    public synchronized String GetStringValue() {
    	return StringValue;
    }
    
    public synchronized int GetDataChangesCounter() {
    	return DataChangesCounter;
    }
}
