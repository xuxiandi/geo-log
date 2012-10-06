package com.geoscope.GeoLog.DEVICE.ControlModule;

import android.os.Handler;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.TrackerService.TTrackerService;

public class TControlModule extends TModule {

	public boolean flEnabled = true;
    
    public TControlModule(TDEVICEModule pDevice)
    {
    	super(pDevice);
    	//.
        Device = pDevice;
    }
    
    public void Destroy()
    {
    }

	private final Handler RestartHandler = new Handler();
	
    public void RestartDeviceAfterDelay(int Delay) {
        final Runnable mRestart = new Runnable() {
            public void run() {
            	try {
					TTracker.RestartTracker(Device.context);
				} catch (Exception E) {
	        		Device.Log.WriteError("ControlModule",E.getMessage());
				}
            }
        };
        RestartHandler.postDelayed(mRestart,Delay);
    }
    
    public void RestartDeviceProcessAfterDelay(int Delay) {
    	final TTrackerService Service = TTrackerService.GetService();
    	if (Service == null)
    		return; //. ->
        final Runnable mRestart = new Runnable() {
            public void run() {
            	Service.RestartProcess();
            }
        };
        RestartHandler.postDelayed(mRestart,Delay);
    }
}
