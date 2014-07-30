/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.TaskModule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Log.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivities;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

/**
 * @author ALXPONOM
 */
public class TTaskDataValue extends TComponentTimestampedDataValue {
	
	//. import from SpaceHTTPSOAPServer
    public static final int MODELUSER_TASK_PRIORITY_Normal      = 0;
    public static final int MODELUSER_TASK_PRIORITY_Minor       = 1;
    public static final int MODELUSER_TASK_PRIORITY_Major       = 2;
    public static final int MODELUSER_TASK_PRIORITY_Critical    = 3;

	public static class TTaskDescriptorV1V2 {
		
		public int 		ID;
		public int 		idUser;
		public int 		idOwner;
		public int 		Priority;
		public int 		TType;
		public int 		Service;
		public String 	Comment = "";
		//.
		public double 	StatusTimestamp;
		public int 		Status;
		public int 		StatusReason;
		public String 	StatusComment = "";
		//.
		public double 	ResultTimestamp;
		public int 		ResultCode;
		public String 	ResultComment = "";
		
		public int FromByteArray(byte[] BA, int Idx, boolean flOriginator) throws IOException {
			ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			if (flOriginator) {
				idUser = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			} else {
				idOwner = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			}
			Priority = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			TType = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			Service = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		Comment = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Comment = "";
			//.
			Status = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			StatusReason = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			StatusTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		StatusComment = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		StatusComment = "";
			//.
			ResultCode = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		ResultComment = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		ResultComment = "";
			ResultTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
			//.
			return Idx;
		}
		
		public byte[] ToByteArray(boolean flOriginator) throws IOException {
			byte[] BA;
			byte[] B1A = new byte[1];
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				BA = TDataConverter.ConvertInt32ToBEByteArray(ID);
				BOS.write(BA);
				if (flOriginator) 
					BA = TDataConverter.ConvertInt32ToBEByteArray(idUser);
				else
					BA = TDataConverter.ConvertInt32ToBEByteArray(idOwner);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(Priority);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(TType);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(Service);
				BOS.write(BA);
				B1A[0] = (byte)Comment.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(Comment.getBytes("windows-1251"));
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(Status);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(StatusReason);
				BOS.write(BA);
				BA = TDataConverter.ConvertDoubleToBEByteArray(StatusTimestamp);
				BOS.write(BA);
				B1A[0] = (byte)StatusComment.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(StatusComment.getBytes("windows-1251"));
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(ResultCode);
				BOS.write(BA);
				B1A[0] = (byte)ResultComment.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(ResultComment.getBytes("windows-1251"));
				BA = TDataConverter.ConvertDoubleToBEByteArray(ResultTimestamp);
				BOS.write(BA);
				//.
				return BOS.toByteArray(); //. ->
			}
			finally {
				BOS.close();
			}
		}

		public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
			ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			idUser = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			idOwner = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			Priority = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			TType = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			Service = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		Comment = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Comment = "";
			//.
			Status = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			StatusReason = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			StatusTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		StatusComment = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		StatusComment = "";
			//.
			ResultCode = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		ResultComment = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		ResultComment = "";
			ResultTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
			//.
			return Idx;
		}
		
		public byte[] ToByteArrayV1() throws IOException {
			byte[] BA;
			byte[] B1A = new byte[1];
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				BA = TDataConverter.ConvertInt32ToBEByteArray(ID);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(idUser);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(idOwner);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(Priority);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(TType);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(Service);
				BOS.write(BA);
				B1A[0] = (byte)Comment.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(Comment.getBytes("windows-1251"));
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(Status);
				BOS.write(BA);
				BA = TDataConverter.ConvertInt32ToBEByteArray(StatusReason);
				BOS.write(BA);
				BA = TDataConverter.ConvertDoubleToBEByteArray(StatusTimestamp);
				BOS.write(BA);
				B1A[0] = (byte)StatusComment.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(StatusComment.getBytes("windows-1251"));
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(ResultCode);
				BOS.write(BA);
				B1A[0] = (byte)ResultComment.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(ResultComment.getBytes("windows-1251"));
				BA = TDataConverter.ConvertDoubleToBEByteArray(ResultTimestamp);
				BOS.write(BA);
				//.
				return BOS.toByteArray(); //. ->
			}
			finally {
				BOS.close();
			}
		}
	}
	
	public static class TTaskDescriptorsV1V2 {
		
		public TTaskDescriptorV1V2[] Items;
		
		public TTaskDescriptorsV1V2(byte[] BA, int Idx, int UserID, boolean flOriginator) throws IOException {
			FromByteArray(BA, Idx, UserID, flOriginator);
		}
		
		public int FromByteArray(byte[] BA, int Idx, int UserID, boolean flOriginator) throws IOException {
			int Version = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			if (!((Version == 1) || (Version == 2)))
				throw new IOException("unknown data version, version: "+Integer.toString(Version)); //. =>
			int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			Items = new TTaskDescriptorV1V2[ItemsCount];
			for (int I = 0; I < ItemsCount; I++) {
				Items[I] = new TTaskDescriptorV1V2();
				if (flOriginator)
					Items[I].idOwner = UserID;
				else
					Items[I].idUser = UserID;
				Idx = Items[I].FromByteArray(BA, Idx, flOriginator);
			}
			return Idx;
		}
	}
	
	public static class TTaskIsOriginatedHandler {
		
		public void DoOnTaskIsOriginated(int idTask) {
		}
	}
	
	public static class TUserTasksAreReceivedHandler {
		
		public void DoOnUserTasksAreReceived(TTaskDescriptorsV1V2 Tasks) {
		}
	}
	
	public static class TTaskActivitiesAreReceivedHandler {
		
		public void DoOnTaskActivitiesAreReceived(TActivities Activities) {
		}
	}
	
	public static class TTaskDataIsReceivedHandler {
		
		public void DoOnTaskDataIsReceived(byte[] TaskData) {
		}
	}
	
	public static class TUserActivityIsStartedHandler {
		
		public void DoOnUserActivityIsStarted(int idActivity) {
		}
	}
	
	public static class TDoneHandler {
		
		public void DoOnDone(double Timestamp) {
			DoOnDone();
		}

		public void DoOnDone() {
		}
	}
	
	public static class TExceptionHandler {
		
		public void DoOnException(Exception E) {
		}
	}
	
	
	private TDEVICEModule Device = null;
	//.
	public TTaskIsOriginatedHandler 			TaskIsOriginatedHandler = null;
	public TUserTasksAreReceivedHandler 		UserTasksIsReceivedHandler = null;
	public TTaskActivitiesAreReceivedHandler 	TaskActivitiesAreReceivedHandler = null;
	public TTaskDataIsReceivedHandler			TaskDataIsReceivedHandler = null;
	public TUserActivityIsStartedHandler		UserActivityIsStartedHandler = null;
	public TDoneHandler							DoneHandler = null;
	//.
	public TExceptionHandler					ExceptionHandler = null;
	
	public TTaskDataValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
		//.
		Device = ((TTaskModule)Owner).Device;
	}

    public TTaskDataValue() {
    }
    
    public TTaskDataValue(byte[] BA, TIndex Idx) throws IOException, OperationException {
        super(BA,/*ref*/ Idx);
    }
    
    public TTaskDataValue Clone() {
    	TTaskDataValue Result = new TTaskDataValue();
    	//.
    	Result.Device = Device;
    	//.
    	return Result;
    }
    
    @Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception {
    	super.FromByteArrayByAddressData(BA, Idx, AddressData);
		//.
		if (AddressData == null)
			return; //. ->
    	String Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	String[] SA = Params.split(",");
    	int Version = Integer.parseInt(SA[0]);
    	//.
    	switch (Version) {
    	
    	case 1: //. new user task is originated 
    		if (TaskIsOriginatedHandler != null) {
    			int idNewTask = TDataConverter.ConvertBEByteArrayToInt32(Value,0);
        		TaskIsOriginatedHandler.DoOnTaskIsOriginated(idNewTask);
    		}
            break; //. >

    	case 2: //. the activity is assigned to the task
    		if (DoneHandler != null) 
    			DoneHandler.DoOnDone(Timestamp);
            break; //. >

    	case 3: //. user tasks or user-originated tasks
    		if (UserTasksIsReceivedHandler != null) {
    			int DataVersion = Integer.parseInt(SA[2]);
    			//.
    			int UserID = Device.UserID;
    			boolean flOriginator = (DataVersion == 2);
    			//.
    			TTaskDescriptorsV1V2 Tasks = new TTaskDescriptorsV1V2(Value, 0, UserID, flOriginator);
    			UserTasksIsReceivedHandler.DoOnUserTasksAreReceived(Tasks);
    		}
            break; //. >
            
    	case 4: //. user task activities
    		if (TaskActivitiesAreReceivedHandler != null) {
    			int idTask = Integer.parseInt(SA[2]);
    			//.
    			int _Idx = 0;
				short _Version = TDataConverter.ConvertBEByteArrayToInt16(Value, _Idx); _Idx += 2;
    			TActivities Activities = new TActivities();
				switch (_Version) {
				
				case 1:
					_Idx = Activities.FromByteArrayV1(idTask, Value, _Idx);
					TaskActivitiesAreReceivedHandler.DoOnTaskActivitiesAreReceived(Activities);
					break; //. >
				}
    		}
            break; //. >
            
    	case 5: //. user task data
    		if (TaskDataIsReceivedHandler != null) {
    			short DataVersion = Short.parseShort(SA[3]);
    			//.
				switch (DataVersion) {
				
				case 1:
					TaskDataIsReceivedHandler.DoOnTaskDataIsReceived(Value);
					break; //. >
				}
    		}
            break; //. >
            
    	case 6: //. new user activity is started
    	case 7: //. new user activity is restarted 
    		if (UserActivityIsStartedHandler != null) {
    			int idActivity = TDataConverter.ConvertBEByteArrayToInt32(Value,0);
    			UserActivityIsStartedHandler.DoOnUserActivityIsStarted(idActivity);
    		}
            break; //. >

        default:
            break; //. >
    	}
    }
}
