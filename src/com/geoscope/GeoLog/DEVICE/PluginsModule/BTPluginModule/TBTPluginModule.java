package com.geoscope.GeoLog.DEVICE.PluginsModule.BTPluginModule;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginsModule;

public class TBTPluginModule extends TPluginModule {

    public TBTPluginModule(TPluginsModule pPluginsModule) {
    	super(pPluginsModule);
    	//.
        Device = PluginsModule.Device;
    }
    
    public void Destroy() {
    }
}
