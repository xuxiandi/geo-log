package com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.TrackerService.TTrackerService;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class TUSBPluginModuleLauncher extends Activity {

	@Override
	protected void onDestroy() {
		super.onDestroy();  
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		setContentView(R.layout.usbpluginmodule_panel);
		//.
    	//.
		Intent serviceLauncher = new Intent(this, TTrackerService.class);
		this.startService(serviceLauncher);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
