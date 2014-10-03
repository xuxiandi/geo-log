package com.geoscope.GeoLog.DEVICE.PluginsModule.WIFIPluginModule;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TWIFIPluginModule extends TPluginModule {

    public TWIFIPluginModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    }
    
    public void Destroy() {
    }
}
