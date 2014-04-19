package com.geoscope.GeoLog.DEVICE.TaskModule;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TTaskModule extends TModule {

    //. measurement object
    public String	ObjectGUID = "";
    public int		ObjectID = 0;
    public String	ObjectTitle = "?";
    //.
    public TTaskDataValue 	TaskData 	= new TTaskDataValue();
    public TTaskResultValue TaskResult 	= new TTaskResultValue();
    public TTaskStatusValue TaskStatus 	= new TTaskStatusValue();
    
    public TTaskModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    }
    
    public void Destroy() {
    }
}
