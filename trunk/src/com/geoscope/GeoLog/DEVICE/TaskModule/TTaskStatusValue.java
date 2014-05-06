/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.TaskModule;

import java.io.IOException;

import android.content.Context;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.Utils.TDataConverter;

/**
 * @author ALXPONOM
 */
public class TTaskStatusValue extends TComponentValue {
	
	//. import from SpaceHTTPSOAPServer
	public static final int MODELUSER_TASK_STATUS_Originated            =  0; //. a new task is originated
	public static final int MODELUSER_TASK_STATUS_Preprocessed          =  1; //. task is preprocessed by the server
	public static final int MODELUSER_TASK_STATUS_Dispatching           =  2; //. task is being dispatched to the user
	public static final int MODELUSER_TASK_STATUS_Dispatched            =  3; //. task is dispatched to the user
	public static final int MODELUSER_TASK_STATUS_Assigned              =  4; //. task is assigned
	public static final int MODELUSER_TASK_STATUS_Received              =  5; //. task is received by the expert side
	public static final int MODELUSER_TASK_STATUS_Accepted              =  6; //. task is accepted to process
	public static final int MODELUSER_TASK_STATUS_Processing            =  7; //. task is in process (work in progress)
	public static final int MODELUSER_TASK_STATUS_Processed             =  8; //. task is processed with result
	public static final int MODELUSER_TASK_STATUS_NotNeeded             =  9; //. task is not needed to be processed
	public static final int MODELUSER_TASK_STATUS_Deferred              = 10; //. task is deferred
	public static final int MODELUSER_TASK_STATUS_NeedInfo              = 11; //. task process requires more information
	public static final int MODELUSER_TASK_STATUS_OutOfExperience       = 12; //. task is out of experience of the user
	public static final int MODELUSER_TASK_STATUS_ExpertIsNotAvailable  = 13; //. task expert is not available
	public static final int MODELUSER_TASK_STATUS_Redirected            = 14; //. task is redirected to the another expert
	public static final int MODELUSER_TASK_STATUS_Rejected              = 15; //. task is rejected
	public static final int MODELUSER_TASK_STATUS_Cancelled             = 16; //. task is cencelled
	public static final int MODELUSER_TASK_STATUS_Errored               = 17; //. task data error
	
	public static String Status_String(int Status, Context context) {
		switch (Status) {
		
		case MODELUSER_TASK_STATUS_Originated:
			return context.getString(R.string.SStatusOriginated); //. ->
			
		case MODELUSER_TASK_STATUS_Preprocessed:
			return context.getString(R.string.SStatusPreprocessed); //. ->
			
		case MODELUSER_TASK_STATUS_Dispatching:
			return context.getString(R.string.SStatusDispatching); //. ->
			
		case MODELUSER_TASK_STATUS_Dispatched:
			return context.getString(R.string.SStatusDispatched); //. ->
			
		case MODELUSER_TASK_STATUS_Assigned:
			return context.getString(R.string.SStatusAssigned); //. ->
			
		case MODELUSER_TASK_STATUS_Received:
			return context.getString(R.string.SStatusReceived); //. ->
			
		case MODELUSER_TASK_STATUS_Accepted:
			return context.getString(R.string.SStatusAccepted); //. ->
			
		case MODELUSER_TASK_STATUS_Processing:
			return context.getString(R.string.SStatusProcessing); //. ->
			
		case MODELUSER_TASK_STATUS_Processed:
			return context.getString(R.string.SStatusProcessed); //. ->
			
		case MODELUSER_TASK_STATUS_NotNeeded:
			return context.getString(R.string.SStatusNotNeeded); //. ->
			
		case MODELUSER_TASK_STATUS_Deferred:
			return context.getString(R.string.SStatusDeferred); //. ->
			
		case MODELUSER_TASK_STATUS_NeedInfo:
			return context.getString(R.string.SStstusNeedInfo); //. ->
			
		case MODELUSER_TASK_STATUS_OutOfExperience:
			return context.getString(R.string.SStatusOutOfExperience); //. ->
			
		case MODELUSER_TASK_STATUS_ExpertIsNotAvailable:
			return context.getString(R.string.SStatusExpertIsNotAvailable); //. ->
			
		case MODELUSER_TASK_STATUS_Redirected:
			return context.getString(R.string.SStatusRedirected); //. ->
			
		case MODELUSER_TASK_STATUS_Rejected:
			return context.getString(R.string.SStatusRejected); //. ->
			
		case MODELUSER_TASK_STATUS_Cancelled:
			return context.getString(R.string.SStatusCancelled); //. ->
			
		case MODELUSER_TASK_STATUS_Errored:
			return context.getString(R.string.SStatusErrored); //. ->
		
		default:
			return "?"; //. ->
		}
	}
	
	public static final int[] OriginatorStatuses = new int[] {
		MODELUSER_TASK_STATUS_Dispatching,
		MODELUSER_TASK_STATUS_Processing,
		MODELUSER_TASK_STATUS_Processed,
		MODELUSER_TASK_STATUS_NotNeeded,
		MODELUSER_TASK_STATUS_Deferred,
		MODELUSER_TASK_STATUS_Redirected,
		MODELUSER_TASK_STATUS_Cancelled,
		MODELUSER_TASK_STATUS_Errored
	};

	public static final int[] ExpertStatuses = new int[] {
		MODELUSER_TASK_STATUS_Received,
		MODELUSER_TASK_STATUS_Accepted,
		MODELUSER_TASK_STATUS_Processing,
		MODELUSER_TASK_STATUS_Processed,
		MODELUSER_TASK_STATUS_NotNeeded,
		MODELUSER_TASK_STATUS_Deferred,
		MODELUSER_TASK_STATUS_NeedInfo,
		MODELUSER_TASK_STATUS_OutOfExperience,
		MODELUSER_TASK_STATUS_ExpertIsNotAvailable,
		MODELUSER_TASK_STATUS_Redirected,
		MODELUSER_TASK_STATUS_Rejected,
		MODELUSER_TASK_STATUS_Cancelled,
		MODELUSER_TASK_STATUS_Errored
	};

	public static class TUserTaskStatusDescriptor {
		
		public long 	idUser;
		public long 	idTask;
		//.
		public double 	Timestamp;
		public int 		Status;
		public int		Reason;
		public String 	Comment = "";
		
		public TUserTaskStatusDescriptor() {
		}
		
		public TUserTaskStatusDescriptor(long pidUser, long pidTask, double pTimestamp, int pStatus, int pReason, String pComment) {
			idUser = pidUser;
			idTask = pidTask;
			//.
			Timestamp = pTimestamp;
			Status = pStatus;
			Reason = pReason;
			Comment = pComment;
		}
		
		public String[] FromIncomingCommandMessage(String Command) throws Exception {
			if (!Command.startsWith(TGeoScopeServerUser.TUserTaskStatusCommandMessage.Prefix))
				throw new Exception("incorrect command prefix"); //. =>
			String ParamsString = Command.substring(TGeoScopeServerUser.TUserTaskStatusCommandMessage.Prefix.length()+1/*skip space*/);
			String[] Params = ParamsString.split(";");
			int Version = Integer.parseInt(Params[0]);
			switch (Version) {
			
			case 1:
				idUser = Long.parseLong(Params[1]);
				idTask = Long.parseLong(Params[2]);
				Timestamp = Double.parseDouble(Params[3]);
				Status = Integer.parseInt(Params[4]);
				Reason = Integer.parseInt(Params[5]);
				Comment = Params[6];
				//.
				return Params; //. ->
				
			default:
				throw new Exception("unknown command parameters version"); //. =>
			}
		}		
		
		public String ToIncomingCommandMessage(int Version, int Session) {
			String Result = TGeoScopeServerUser.TUserTaskStatusCommandMessage.Prefix+" "+Integer.toString(Version)/*Parameters version*/+";"+
				Long.toString(idUser)+";"+
				Long.toString(idTask)+";"+
				Double.toString(Timestamp)+";"+
				Integer.toString(Status)+";"+
				Integer.toString(Reason)+";"+
				Comment.replace(';',',')+";"+
				Integer.toString(Session);
			return Result;
		}		
	}
	
	public static class TStatusDescriptor {
		
		public double 	Timestamp;
		public int 		Status;
		public int		Reason;
		public String 	Comment = "";
		
		public TStatusDescriptor() {
		}
		
		public TStatusDescriptor(double pTimestamp, int pStatus, int pReason, String pComment) {
			Timestamp = pTimestamp;
			Status = pStatus;
			Reason = pReason;
			Comment = pComment;
		}
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
			Status = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			Reason = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			Timestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
			byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		Comment = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Comment = "";
	    	//.
	    	return Idx;
		}
	}
	
	public static class TStatusDescriptors {
		
		public TStatusDescriptor[] Items;
		
		public TStatusDescriptors(byte[] BA, int Idx) throws IOException {
			FromByteArray(BA, Idx);
		}
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
			int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			Items = new TStatusDescriptor[ItemsCount];
			for (int I = 0; I < ItemsCount; I++) {
				Items[I] = new TStatusDescriptor();
				Idx = Items[I].FromByteArray(BA, Idx);
			}
			return Idx;
		}
	}
		
	public static class TStatusIsChangedHandler {
		
		public void DoOnStatusIsChanged(TStatusDescriptor Status) {
		}
	}
	
	public static class TStatusHistoryIsReceivedHandler {
		
		public void DoOnStatusHistoryIsReceived(TStatusDescriptors History) {
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
    public int		Int32Value1;
    public String	StringValue;
    //.
    public TStatusIsChangedHandler				StatusIsChangedHandler = null;
    public TStatusHistoryIsReceivedHandler		StatusHistoryIsReceivedHandler = null;
	public TDoneHandler							DoneHandler = null;
	//.
	public TExceptionHandler					ExceptionHandler = null;

    public TTaskStatusValue() {
    }
    
	public TTaskStatusValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
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
    
    public TTaskStatusValue Clone() {
    	TTaskStatusValue Result = new TTaskStatusValue();
    	//.
    	return Result;
    }
    
    @Override
    public synchronized void Assign(TComponentValue pValue) {
        TTaskStatusValue Src = (TTaskStatusValue)pValue.getValue();
        Timestamp = Src.Timestamp;
        Int32Value = Src.Int32Value;
        Int32Value1 = Src.Int32Value1;
        StringValue = Src.StringValue;
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
    	
    	case 1: //. get status by task id
        	super.FromByteArrayByAddressData(BA, Idx, AddressData);
    		//.
    		if (StatusIsChangedHandler != null) 
    			StatusIsChangedHandler.DoOnStatusIsChanged(new TStatusDescriptor(Timestamp, Int32Value, Int32Value1, StringValue));
            break; //. >
            
    	case 2: //. get status history by task id
    		TStatusDescriptors History = new TStatusDescriptors(BA, Idx.Value);
    		//.
    		if (StatusHistoryIsReceivedHandler != null) 
    			StatusHistoryIsReceivedHandler.DoOnStatusHistoryIsReceived(History);
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
}
