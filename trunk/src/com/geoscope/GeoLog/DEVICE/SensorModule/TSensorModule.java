package com.geoscope.GeoLog.DEVICE.SensorModule;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TSensorModule extends TModule {

    public TSensorModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    }
    
    public void Destroy() {
    }
}
