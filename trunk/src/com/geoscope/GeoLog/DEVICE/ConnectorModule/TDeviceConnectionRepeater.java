package com.geoscope.GeoLog.DEVICE.ConnectorModule;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule.TProcessIncomingOperationResult;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TOperationSession;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TComponentUserAccessList;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANDeviceConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TDeviceConnectionRepeater extends TLANDeviceConnectionRepeater {

	public static final int Port = 0;
	//.
	private TComponentUserAccessList CUAL;
	//.
    public boolean flProcessingOperation = false;
	
	public TDeviceConnectionRepeater(TLANModule pLANModule, TComponentUserAccessList pCUAL, String pDestinationAddress, int pDestinationPort, int pConnectionID, String pUserAccessKey) {
		super(pLANModule,pDestinationAddress,pDestinationPort,pConnectionID,pUserAccessKey);
		CUAL = pCUAL;
		//.
		Start();
	}
	
	@Override
	public void run() {
		try {
			ConnectDestination();
			try {
                //. receive incoming operations
                TIndex OperationMessageOrigin = new TIndex();
                TOperationSession OperationSession = new TOperationSession();
            	byte[] OperationMessage = null;
            	TProcessIncomingOperationResult ProcessIncomingOperationResult = new TProcessIncomingOperationResult();
				while (!Canceller.flCancel) {
                    if (LANModule.Device.State == TDEVICEModule.DEVICEModuleState_Running) {
                        OperationMessageOrigin.Reset();
                        OperationSession.New();
                        OperationMessage = TGeographServerServiceOperation.CheckReceiveMessage(LANModule.Device.UserID,LANModule.Device.UserPassword,DestinationConnection,DestinationConnectionInputStream,DestinationConnectionOutputStream,/*out*/ OperationSession,/*out*/ OperationMessageOrigin, LANModule.Device.ConnectorModule.LoopSleepTime);
                        if (OperationMessage != null) {
                            //. process incoming operation 
                            flProcessingOperation = true;
                            try {
                            	LANModule.Device.ConnectorModule.ProcessIncomingOperation(OperationSession.ID,OperationMessage,/*ref*/ OperationMessageOrigin, CUAL, DestinationConnectionInputStream,DestinationConnectionOutputStream, ProcessIncomingOperationResult);
                            	if (ProcessIncomingOperationResult.ResultCode >= 0) {
                            		TObjectComponentServiceOperation ObjectOperation = ProcessIncomingOperationResult.Operation.GetObjectComponentServiceOperation();
                            		if (ObjectOperation instanceof TObjectSetComponentDataServiceOperation) {
                                		LANModule.Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation((TObjectSetComponentDataServiceOperation)ObjectOperation);
                                		LANModule.Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                            		}
                            	}
                            }
                            finally {
                            	flProcessingOperation = false;
                            }
                        }
                    }
                    else
                    	Thread.sleep(LANModule.Device.ConnectorModule.LoopSleepTime);
            	}
			}
			finally {
				DisconnectDestination();
			}
		}
		catch (InterruptedException E) {
		}
		catch (Throwable TE) {
        	//. log errors
    		LANModule.Device.Log.WriteError("ConnectorModule.DeviceConnectionRepeater",TE.getMessage());
        	if (!(TE instanceof Exception))
        		TDEVICEModule.Log_WriteCriticalError(TE);
		}
		//.
		Repeaters.remove(this);
	}	
}
