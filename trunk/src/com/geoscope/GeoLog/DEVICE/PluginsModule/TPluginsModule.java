package com.geoscope.GeoLog.DEVICE.PluginsModule;

import com.geoscope.GeoLog.DEVICE.PluginsModule.BTPluginModule.TBTPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule.TUSBPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.WIFIPluginModule.TWIFIPluginModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TPluginsModule extends TModule {

	public TUSBPluginModule 	USBPluginModule;
	public TBTPluginModule 		BTPluginModule;
	public TWIFIPluginModule	WIFIPluginModule;
	
    public TPluginsModule(TDEVICEModule pDevice) throws Exception {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        USBPluginModule 	= new TUSBPluginModule(this);
        BTPluginModule		= new TBTPluginModule(this);
        WIFIPluginModule	= new TWIFIPluginModule(this);
    }
    
    public void Destroy() {
    }
}
