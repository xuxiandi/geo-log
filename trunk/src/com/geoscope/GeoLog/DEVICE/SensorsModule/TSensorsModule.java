package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetSensorsDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TSensorsModule extends TModule {

	public TSensorsDataValue Data;
	//.
	public TModel Model;
	
    public TSensorsModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        Data = new TSensorsDataValue(this);
    }
    
    public void Destroy() {
    }
    
    public void BuildModelAndPublish() throws Exception {
    	byte[] BA = Model.ToByteArray();
    	//.
        Data.SetValue(OleDate.UTCCurrentTimestamp(),BA);
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetSensorsDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetSensorsDataSO)SO).setValue(Data);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        }
        catch (Exception E) {}
    }
}
