/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.TaskModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue.TStatusDescriptor;

/**
 * @author ALXPONOM
 */
public class TTaskResultValue extends TComponentValue {

	public static class TResultDescriptor {
		
		public TStatusDescriptor CompletedStatus;
		//.
		public double 	Timestamp;
		public int 		ResultCode;
		public String 	Comment;
		
		public TResultDescriptor(TStatusDescriptor pCompletedStatus, double pTimestamp, int pResultCode, String pComment) {
			CompletedStatus = pCompletedStatus;
			//.
			Timestamp = pTimestamp;
			ResultCode = pResultCode;
			Comment = pComment;
		}
	}
		
	public static class TResultIsChangedHandler {
		
		public void DoOnResultIsChanged(TResultDescriptor Result) {
		}
	}
	
	public static class TDoneHandler {
		
		public void DoOnDone(double Timestamp) {
		}
	}
	
	public static class TExceptionHandler {
		
		public void DoOnException(Exception E) {
		}
	}
	
		
	public double 	Timestamp;
    public int		Int32Value;
    public String	StringValue;
    //.
    public TResultIsChangedHandler				ResultIsChangedHandler = null;
    //.
	public TDoneHandler							DoneHandler = null;
	//.
	public TExceptionHandler					ExceptionHandler = null;

    public TTaskResultValue() {
    }
    
	public TTaskResultValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
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
    
    public TTaskResultValue Clone() {
    	TTaskResultValue Result = new TTaskResultValue();
    	//.
    	return Result;
    }
    
    @Override
    public synchronized void Assign(TComponentValue pValue) {
        TTaskResultValue Src = (TTaskResultValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Int32Value = Src.Int32Value;
        StringValue = Src.StringValue;
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
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception {
		if (AddressData == null)
			return; //. ->
    	String Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	String[] SA = Params.split(",");
    	int Version = Integer.parseInt(SA[0]);
    	//.
    	switch (Version) {
    	
    	case 1: //. get result by task id
        	int CompletedStatusReason = Integer.parseInt(SA[3]);
            String CompletedStatusComment = "";
            if (SA.length > 4)
            	CompletedStatusComment = SA[4];
            //.
            super.FromByteArrayByAddressData(BA, Idx, AddressData);
            //. 
    		if (ResultIsChangedHandler != null) 
    			ResultIsChangedHandler.DoOnResultIsChanged(new TResultDescriptor(new TStatusDescriptor(Timestamp, TTaskStatusValue.MODELUSER_TASK_STATUS_Processed, CompletedStatusReason, CompletedStatusComment), Timestamp, Int32Value, StringValue));
            break; //. >
            
        default:
            break; //. >
    	}
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
    public synchronized int ByteArraySize() throws IOException {
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
}
