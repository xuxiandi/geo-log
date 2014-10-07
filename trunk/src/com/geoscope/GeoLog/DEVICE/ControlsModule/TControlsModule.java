package com.geoscope.GeoLog.DEVICE.ControlsModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetControlsDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TControlsModule extends TModule {

	public TControlsDataValue Data;
	//.
	public TModel Model;
	
    public TControlsModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        Data = new TControlsDataValue(this);
    }
    
    public void Destroy() {
    }

    public void BuildModelAndPublish() throws Exception {
    	byte[] BA = Model.ToByteArray();
    	//.
        Data.SetValue(OleDate.UTCCurrentTimestamp(),BA);
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetControlsDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetControlsDataSO)SO).setValue(Data);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        }
        catch (Exception E) {}
    }
}
