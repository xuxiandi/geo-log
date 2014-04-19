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
public class TTaskStatusValue extends TComponentValue {
	//. import from SpaceHTTPSOAPServer
	public static final int MODELUSER_TASK_STATUS_Originated            = 0;  //. new task is originated
	public static final int MODELUSER_TASK_STATUS_Assigned              = 1;  //. task is assigned
	public static final int MODELUSER_TASK_STATUS_Accepted              = 2;  //. task accepted to process
	public static final int MODELUSER_TASK_STATUS_Rejected              = 3;  //. task rejected
	public static final int MODELUSER_TASK_STATUS_Processing            = 4;  //. task in process (work in progress)
	public static final int MODELUSER_TASK_STATUS_Completed             = 5;  //. task is completed with result
	public static final int MODELUSER_TASK_STATUS_Cancelled             = 6;  //. task is cencelled
	public static final int MODELUSER_TASK_STATUS_Errored               = 7;  //. task data error
	public static final int MODELUSER_TASK_STATUS_OutOfExperience       = 8;  //. task is out of experience of user
	public static final int MODELUSER_TASK_STATUS_NotNeeded             = 9;  //. task is not needed
	public static final int MODELUSER_TASK_STATUS_Deferred              = 10; //. task is deferred
	public static final int MODELUSER_TASK_STATUS_Redirected            = 11; //. task is redirected
	public static final int MODELUSER_TASK_STATUS_Dispatched            = 12; //. task has been dispatched to user
	public static final int MODELUSER_TASK_STATUS_UserNotAvailable      = 13; //. task user is not available
	public static final int MODELUSER_TASK_STATUS_ServerPreprocessed    = 14; //. task is preprocessed by server
	public static final int MODELUSER_TASK_STATUS_NeedInfo              = 15; //. task required more task information
	public static final int MODELUSER_TASK_STATUS_Consulting            = 16; //. task expert consulting with another expert
	public static final int MODELUSER_TASK_STATUS_ReceivedByExpert      = 17; //. expert received task

	private int		ObjectID;
	private String	MeasurementID;
	//.
	public double 	Timestamp;
    public int		Int32Value;
    public int		Int32Value1;
    public String	StringValue;
    private int		DataChangesCounter = 0;

    public TTaskStatusValue() {
    }
    
    public TTaskStatusValue(byte[] BA, TIndex Idx) throws IOException, OperationException {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TTaskStatusValue(double pTimestamp, int pInt32Value, int pInt32Value1, String pStringValue) {
    	Timestamp = pTimestamp;
    	//.
    	Int32Value = pInt32Value;
    	Int32Value1 = pInt32Value1;
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
    	Int32Value1 = 0;
    	StringValue = "";
        DataChangesCounter++;
    }
    
    @Override
    public synchronized void Assign(TComponentValue pValue) {
        TTaskStatusValue Src = (TTaskStatusValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Int32Value = Src.Int32Value;
        Int32Value1 = Src.Int32Value1;
        StringValue = Src.StringValue;
        DataChangesCounter++;
        //.
        super.Assign(pValue);
    }
    
    @Override
    public synchronized TComponentValue getValue() {
        return new TTaskStatusValue(Timestamp,Int32Value,Int32Value1,StringValue);
    }
    
    @Override
    public synchronized boolean IsValueTheSame(TComponentValue AValue) {
        TTaskStatusValue V = (TTaskStatusValue)AValue.getValue();
        return ((Timestamp == V.Timestamp) && (Int32Value == V.Int32Value) && (Int32Value1 == V.Int32Value1) && (StringValue.equals(V.StringValue)));
    }
    
    public synchronized void setValues(double pTimestamp, int pInt32Value, int pInt32Value1, String pStringValue) {
    	Timestamp = pTimestamp;
    	Int32Value = pInt32Value;
    	Int32Value1 = pInt32Value1;
    	StringValue = pStringValue;
        DataChangesCounter++;
        //.
        flSet = true;
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
    	Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        Int32Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        Int32Value1 = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
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
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception {
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
		Int32Value1 = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Index); Index+=4;
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
        byte[] BA2 = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(Int32Value1);
        int DataSize = 0;
        byte[] Data = StringValue.getBytes("windows-1251");
        if (Data != null)
            DataSize = Data.length;
        byte[] Result = new byte[8/*SizeOf(Timestamp)*/+4/*SizeOf(Int32Value)*/+4/*SizeOf(Int32Value1)*/+4/*SizeOf(DataSize)*/+DataSize];
        byte[] BA1 = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(DataSize);
        int Idx = 0;
        System.arraycopy(TSBA,0,Result,Idx,TSBA.length); Idx += TSBA.length;
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx += BA.length;
        System.arraycopy(BA2,0,Result,Idx,BA2.length); Idx += BA2.length;
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
        return (8/*SizeOf(Double)*/+4/*SizeOf(Int32Value)*/+4/*SizeOf(Int32Value1)*/+4/*SizeOf(DataSize)*/+DataSize);
    }
    
    public synchronized double GetTimestamp() {
    	return Timestamp;
    }
    
    public synchronized int GetInt32Value() {
    	return Int32Value;
    }
    
    public synchronized int GetInt32Value1() {
    	return Int32Value1;
    }
    
    public synchronized String GetStringValue() {
    	return StringValue;
    }
    
    public synchronized int GetDataChangesCounter() {
    	return DataChangesCounter;
    }
}
