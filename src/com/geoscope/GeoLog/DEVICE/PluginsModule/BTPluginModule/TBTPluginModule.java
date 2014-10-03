package com.geoscope.GeoLog.DEVICE.PluginsModule.BTPluginModule;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TBTPluginModule extends TPluginModule {

    public TBTPluginModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    }
    
    public void Destroy() {
    }
}
