package com.geoscope.GeoLog.DEVICE.TaskModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectGetTaskModuleExpertsSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectGetTaskModuleTaskStatusSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleDispatcherSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskResultSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskStatusSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TExpertsValue.TExpertsIsReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskActivitiesAreReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskDataIsReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskIsOriginatedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TUserActivityIsStartedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TUserTasksAreReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue.TStatusHistoryIsReceivedHandler;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TTaskModule extends TModule {

    public TTaskDataValue 	TaskData;
    public TTaskStatusValue TaskStatus;
    public TTaskResultValue TaskResult;
    
    public TTaskModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        TaskData 	= new TTaskDataValue	(this,1000,"TaskData");
        TaskStatus 	= new TTaskStatusValue	(this,1001,"TaskStatus");
        TaskResult 	= new TTaskResultValue	(this,1002,"TaskResult");
    }
    
    public void Destroy() {
    }
    
    public TComponentServiceOperation OriginateNewTask(long UserID, long pActivityID, int pTaskPriority, int pTaskType, int pTaskService, String pTaskComment, TTaskIsOriginatedHandler pTaskIsOriginatedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "1"/*Version*/+","+Long.toString(UserID)+","+Long.toString(pActivityID)+","+Integer.toString(pTaskPriority)+","+Integer.toString(pTaskType)+","+Integer.toString(pTaskService)+","+pTaskComment.replace(',',';');
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskDataValue _TaskData = TaskData.Clone(); 
    	_TaskData.Timestamp = OleDate.UTCCurrentTimestamp();
    	_TaskData.Value = null;
    	_TaskData.TaskIsOriginatedHandler = pTaskIsOriginatedHandler;
    	_TaskData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskDataSO SO = new TObjectSetGetTaskModuleTaskDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_TaskData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }
    
    public TComponentServiceOperation AssignActivityToTask(long UserID, long pidTask, long pActivityID, TTaskDataValue.TDoneHandler pDoneHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "2"/*Version*/+","+Long.toString(UserID)+","+Long.toString(pidTask)+","+Long.toString(pActivityID);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskDataValue _TaskData = TaskData.Clone(); 
    	_TaskData.Timestamp = OleDate.UTCCurrentTimestamp();
    	_TaskData.Value = null;
    	_TaskData.DoneHandler = pDoneHandler;
    	_TaskData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskDataSO SO = new TObjectSetGetTaskModuleTaskDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_TaskData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }
    
    public TComponentServiceOperation GetUserTasks(long UserID, boolean flOriginator, boolean flOnlyActive, TUserTasksAreReceivedHandler pUserTasksIsReceivedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	int DV = 1;
    	if (flOriginator)
    		DV = 2;
    	//.
    	int BV = 0;
    	if (flOnlyActive)
    		BV = 1;
    	//.
    	String Params = "3"/*Version*/+","+Long.toString(UserID)+","+Integer.toString(DV)+","+Integer.toString(BV);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskDataValue _TaskData = TaskData.Clone(); 
    	_TaskData.Timestamp = OleDate.UTCCurrentTimestamp();
    	_TaskData.Value = null;
    	_TaskData.UserTasksIsReceivedHandler = pUserTasksIsReceivedHandler;
    	_TaskData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskDataSO SO = new TObjectSetGetTaskModuleTaskDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_TaskData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }

    public TComponentServiceOperation GetTaskActivities(long UserID, long idTask, TTaskActivitiesAreReceivedHandler pTaskActivitiesAreReceivedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	short DataVersion = 1;
    	String Params = "4"/*Version*/+","+Long.toString(UserID)+","+Long.toString(idTask)+","+Integer.toString(DataVersion);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskDataValue _TaskData = TaskData.Clone(); 
    	_TaskData.Timestamp = OleDate.UTCCurrentTimestamp();
    	_TaskData.Value = null;
    	_TaskData.TaskActivitiesAreReceivedHandler = pTaskActivitiesAreReceivedHandler;
    	_TaskData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskDataSO SO = new TObjectSetGetTaskModuleTaskDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_TaskData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }

    public TComponentServiceOperation GetTaskData(long UserID, long idTask, TTaskDataIsReceivedHandler pTaskDataIsReceivedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	short DataVersion = 1;
    	String Params = "5"/*Version*/+","+Long.toString(UserID)+","+Long.toString(idTask)+","+Integer.toString(DataVersion);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskDataValue _TaskData = TaskData.Clone(); 
    	_TaskData.Timestamp = OleDate.UTCCurrentTimestamp();
    	_TaskData.Value = null;
    	_TaskData.TaskDataIsReceivedHandler = pTaskDataIsReceivedHandler;
    	_TaskData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskDataSO SO = new TObjectSetGetTaskModuleTaskDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_TaskData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }

    public TComponentServiceOperation StartUserActivity(String ActivityName, String ActivityInfo, TUserActivityIsStartedHandler pUserActivityIsStartedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	if (ActivityInfo == null)
    		ActivityInfo = "";
    	String Params = "6"/*Version*/+","+"1"/*SubVersion*/+","+ActivityName+","+ActivityInfo;
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskDataValue _TaskData = TaskData.Clone(); 
    	_TaskData.Timestamp = OleDate.UTCCurrentTimestamp();
    	_TaskData.Value = null;
    	_TaskData.UserActivityIsStartedHandler = pUserActivityIsStartedHandler;
    	_TaskData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskDataSO SO = new TObjectSetGetTaskModuleTaskDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_TaskData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }
    
    public TComponentServiceOperation RestartUserActivity(long pActivityID, TUserActivityIsStartedHandler pUserActivityIsStartedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "7"/*Version*/+","+"1"/*SubVersion*/+","+Long.toString(pActivityID);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskDataValue _TaskData = TaskData.Clone(); 
    	_TaskData.Timestamp = OleDate.UTCCurrentTimestamp();
    	_TaskData.Value = null;
    	_TaskData.UserActivityIsStartedHandler = pUserActivityIsStartedHandler;
    	_TaskData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskDataSO SO = new TObjectSetGetTaskModuleTaskDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_TaskData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }
    
    public TComponentServiceOperation GetTaskStatusHistory(long UserID, long idTask, TStatusHistoryIsReceivedHandler pStatusHistoryIsReceivedHandler, TTaskStatusValue.TExceptionHandler pExceptionHandler) throws Exception {
    	short DataVersion = 1;
    	String Params = "2"/*Version*/+","+Long.toString(UserID)+","+Long.toString(idTask)+","+Integer.toString(DataVersion);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskStatusValue _StatusData = TaskStatus.Clone(); 
    	_StatusData.Timestamp = OleDate.UTCCurrentTimestamp();
    	_StatusData.StatusHistoryIsReceivedHandler = pStatusHistoryIsReceivedHandler;
    	_StatusData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectGetTaskModuleTaskStatusSO SO = new TObjectGetTaskModuleTaskStatusSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_StatusData);
        //.
        Device.ConnectorModule.OutgoingGetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }

    public TComponentServiceOperation SetTaskStatus(long UserID, long idTask, double pStatusTimestamp, int pStatus, int pStatusReason, String pStatusComment, TTaskStatusValue.TStatusIsChangedHandler pStatusIsChangedHandler, TTaskStatusValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "1"/*Version*/+","+Long.toString(UserID)+","+Long.toString(idTask);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskStatusValue _StatusData = TaskStatus.Clone(); 
    	_StatusData.Timestamp = pStatusTimestamp;
    	_StatusData.Int32Value = pStatus;
    	_StatusData.Int32Value1 = pStatusReason;
    	_StatusData.StringValue = pStatusComment;
    	_StatusData.StatusIsChangedHandler = pStatusIsChangedHandler;
    	_StatusData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskStatusSO SO = new TObjectSetGetTaskModuleTaskStatusSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_StatusData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }
    
    public TComponentServiceOperation SetTaskResult(long UserID, long idTask, int pCompletedStatusReason, String pCompletedStatusComment, double pResultTimestamp, int pResultCode, String pResultComment, TTaskResultValue.TResultIsChangedHandler pResultIsChangedHandler, TTaskResultValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "1"/*Version*/+","+Long.toString(UserID)+","+Long.toString(idTask)+","+Integer.toString(pCompletedStatusReason)+","+pCompletedStatusComment.replace(',',';');
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TTaskResultValue _ResultData = TaskResult.Clone(); 
    	_ResultData.Timestamp = pResultTimestamp;
    	_ResultData.Int32Value = pResultCode;
    	_ResultData.StringValue = pResultComment;
    	_ResultData.ResultIsChangedHandler = pResultIsChangedHandler;
    	_ResultData.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleTaskResultSO SO = new TObjectSetGetTaskModuleTaskResultSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_ResultData);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }
    
    public TComponentServiceOperation GetExperts(boolean flActiveExpertsOnly, TExpertsIsReceivedHandler pExpertsIsReceivedHandler, TExpertsValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params;
    	if (flActiveExpertsOnly)
    		Params = "2"/*Version*/;
    	else
    		Params = "1"/*Version*/;
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TExpertsValue _Experts = new TExpertsValue(); 
    	_Experts.ExpertsIsReceivedHandler = pExpertsIsReceivedHandler;
    	_Experts.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectGetTaskModuleExpertsSO SO = new TObjectGetTaskModuleExpertsSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_Experts);
        //.
        Device.ConnectorModule.OutgoingGetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }

    public TComponentServiceOperation DispatchTask(long idTask, int DispatchPolicy, int WaitForDispatchingTime, int WaitForUserReceivedTime, TDispatcherValue.TExpertIsDispatchedHandler pExpertIsDispatchedHandler, TDispatcherValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "1"/*Version*/+","+Long.toString(idTask)+","+Integer.toString(WaitForDispatchingTime)+","+Integer.toString(DispatchPolicy)+","+Integer.toString(WaitForUserReceivedTime);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TDispatcherValue _DispatcherValue = new TDispatcherValue(); 
    	_DispatcherValue.ExpertIsDispatchedHandler = pExpertIsDispatchedHandler;
    	_DispatcherValue.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleDispatcherSO SO = new TObjectSetGetTaskModuleDispatcherSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_DispatcherValue);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }

    public TComponentServiceOperation DispatchTaskToTheSpecifiedExpert(long idTask, long SpecifiedExpertID, int WaitForUserReceivedTime, TDispatcherValue.TExpertIsDispatchedHandler pExpertIsDispatchedHandler, TDispatcherValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "2"/*Version*/+","+Long.toString(idTask)+","+Long.toString(SpecifiedExpertID)+","+Integer.toString(WaitForUserReceivedTime);
    	byte[] AddressData = Params.getBytes("windows-1251");
    	//.
    	TDispatcherValue _DispatcherValue = new TDispatcherValue(); 
    	_DispatcherValue.ExpertIsDispatchedHandler = pExpertIsDispatchedHandler;
    	_DispatcherValue.ExceptionHandler = pExceptionHandler;
    	//.
    	TObjectSetGetTaskModuleDispatcherSO SO = new TObjectSetGetTaskModuleDispatcherSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
    	SO.AddressData = AddressData;
        SO.setValue(_DispatcherValue);
        //.
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        //.
        return SO;
    }
}
