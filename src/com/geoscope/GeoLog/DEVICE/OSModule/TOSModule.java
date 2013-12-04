package com.geoscope.GeoLog.DEVICE.OSModule;

import com.geoscope.GeoLog.DEVICE.OSModule.IOModule.TIOModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TOSModule extends TModule {

    public TIOModule IOModule = null;
    
    public TOSModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        IOModule = new TIOModule(this);
    }
    
    public void Destroy() {
        if (IOModule != null) {
        	IOModule.Destroy();
        	IOModule = null;
        }
    }
}
