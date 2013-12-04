package com.geoscope.GeoLog.DEVICE.OSModule.IOModule;

import com.geoscope.GeoLog.DEVICE.OSModule.TOSModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TIOModule extends TModule {

	public TOSModule OSModule;
	
    public TIOModule(TOSModule pOSModule) {
    	super(pOSModule);
    	//.
    	OSModule = pOSModule;
    }
    
    public void Destroy() {
    }
}
