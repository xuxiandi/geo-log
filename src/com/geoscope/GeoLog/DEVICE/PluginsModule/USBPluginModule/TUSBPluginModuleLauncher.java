package com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule;

import android.app.Activity;
import android.hardware.usb.UsbAccessory;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TUSBPluginModuleLauncher extends Activity {

	private TextView tvStatus;
	
	@Override
	protected void onDestroy() {
		super.onDestroy();  
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
		setContentView(R.layout.usbpluginmodule_panel);
        //.
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        //.
    	UsbAccessory Accessory = null;
		//.
		try {
			//. initialize an application instance and start services
			TGeoLogApplication.InitializeInstance(getApplicationContext()).StartServices(getApplicationContext());
			//. check connected accessory
			TTracker Tracker = TTracker.GetTracker();
			if (Tracker != null) 
				Accessory = Tracker.GeoLog.PluginsModule.USBPluginModule.CheckConnectedAccesory();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
			return; // . ->
		}
		//.
		if (Accessory != null) 
			tvStatus.setText(getString(R.string.SConnectingToUSBPlugin)+" "+Accessory.getModel()+"("+Accessory.getDescription()+", "+Accessory.getManufacturer()+")");
    	//.
		TAsyncProcessing Processing = new TAsyncProcessing() {
			@Override
			public void Process() throws Exception {
				Thread.sleep(5000);
			}
			@Override
			public void DoOnCompleted() throws Exception {
				finish();
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TUSBPluginModuleLauncher.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
