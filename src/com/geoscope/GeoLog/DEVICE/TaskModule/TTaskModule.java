package com.geoscope.GeoLog.DEVICE.TaskModule;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectGetTaskModuleExpertsSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleDispatcherSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskResultSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskStatusSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TExpertsValue.TExpertsIsReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskActivitiesAreReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskDataIsReceivedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TTaskIsOriginatedHandler;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue.TUserTasksAreReceivedHandler;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.Utils.OleDate;

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
    
    public TComponentServiceOperation OriginateNewTask(int pActivityID, int pTaskPriority, int pTaskType, int pTaskService, String pTaskComment, TTaskIsOriginatedHandler pTaskIsOriginatedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "1"/*Version*/+","+Integer.toString(pActivityID)+","+Integer.toString(pTaskPriority)+","+Integer.toString(pTaskType)+","+Integer.toString(pTaskService)+","+pTaskComment.replace(',',';');
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
    
    public TComponentServiceOperation AssignActivityToTask(int pActivityID, int pidTask, TTaskDataValue.TDoneHandler pDoneHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "2"/*Version*/+","+Integer.toString(pActivityID)+","+Integer.toString(pidTask);
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
    
    public TComponentServiceOperation GetUserTasks(boolean flOriginator, boolean flOnlyActive, TUserTasksAreReceivedHandler pUserTasksIsReceivedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	int DV = 1;
    	if (flOriginator)
    		DV = 2;
    	//.
    	int BV = 0;
    	if (flOnlyActive)
    		BV = 1;
    	//.
    	String Params = "3"/*Version*/+","+Integer.toString(DV)+","+Integer.toString(BV);
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

    public TComponentServiceOperation GetTaskActivities(int idTask, TTaskActivitiesAreReceivedHandler pTaskActivitiesAreReceivedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	short DataVersion = 1;
    	String Params = "4"/*Version*/+","+Integer.toString(idTask)+","+Integer.toString(DataVersion);
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

    public TComponentServiceOperation GetTaskData(int idTask, TTaskDataIsReceivedHandler pTaskDataIsReceivedHandler, TTaskDataValue.TExceptionHandler pExceptionHandler) throws Exception {
    	short DataVersion = 1;
    	String Params = "5"/*Version*/+","+Integer.toString(idTask)+","+Integer.toString(DataVersion);
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

    public TComponentServiceOperation SetTaskStatus(int pidTask, double pStatusTimestamp, int pStatus, int pStatusReason, String pStatusComment, TTaskStatusValue.TStatusIsChangedHandler pStatusIsChangedHandler, TTaskStatusValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "1"/*Version*/+","+Integer.toString(pidTask);
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
    
    public TComponentServiceOperation SetTaskResult(int pidTask, int pCompletedStatusReason, String pCompletedStatusComment, double pResultTimestamp, int pResultCode, String pResultComment, TTaskResultValue.TResultIsChangedHandler pResultIsChangedHandler, TTaskResultValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "1"/*Version*/+","+Integer.toString(pidTask)+","+Integer.toString(pCompletedStatusReason)+","+pCompletedStatusComment.replace(',',';');
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

    public TComponentServiceOperation DispatchTask(int idTask, int DispatchPolicy, int WaitForDispatchingTime, int WaitForUserReceivedTime, TDispatcherValue.TExpertIsDispatchedHandler pExpertIsDispatchedHandler, TDispatcherValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "1"/*Version*/+","+Integer.toString(idTask)+","+Integer.toString(WaitForDispatchingTime)+","+Integer.toString(DispatchPolicy)+","+Integer.toString(WaitForUserReceivedTime);
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

    public TComponentServiceOperation DispatchTaskToTheSpecifiedExpert(int idTask, int SpecifiedExpertID, int WaitForUserReceivedTime, TDispatcherValue.TExpertIsDispatchedHandler pExpertIsDispatchedHandler, TDispatcherValue.TExceptionHandler pExceptionHandler) throws Exception {
    	String Params = "2"/*Version*/+","+Integer.toString(idTask)+","+Integer.toString(SpecifiedExpertID)+","+Integer.toString(WaitForUserReceivedTime);
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
