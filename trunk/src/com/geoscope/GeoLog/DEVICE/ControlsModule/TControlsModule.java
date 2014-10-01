package com.geoscope.GeoLog.DEVICE.ControlsModule;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TControlsModule extends TModule {

    public TControlsModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    }
    
    public void Destroy() {
    }
}
