package com.geoscope.GeoLog.DEVICE.PluginsModule.WIFIPluginModule;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TWIFIPluginModule extends TModule {

    public TWIFIPluginModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    }
    
    public void Destroy() {
    }
}
