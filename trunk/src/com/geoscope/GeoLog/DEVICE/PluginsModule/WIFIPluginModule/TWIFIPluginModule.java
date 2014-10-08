package com.geoscope.GeoLog.DEVICE.PluginsModule.WIFIPluginModule;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginsModule;

public class TWIFIPluginModule extends TPluginModule {

    public TWIFIPluginModule(TPluginsModule pPluginsModule) {
    	super(pPluginsModule);
    	//.
        Device = PluginsModule.Device;
    }
    
    public void Destroy() {
    }
}
