package com.geoscope.GeoLog.DEVICE.ControlModule;

import android.os.Handler;

import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TControlModule extends TModule {

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
        	@Override
            public void run() {
            	try {
					TTracker.RestartTracker(Device.context);
				} catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);				
	        	}
            }
        };
        RestartHandler.postDelayed(mRestart,Delay);
    }
    
    public void RestartDeviceProcessAfterDelay(int Delay) {
        final Runnable mRestart = new Runnable() {
        	@Override
            public void run() {
            	try {
					TGeoLogApplication.Instance().PendingRestart();
				} catch (Exception E) {
				}
            }
        };
        RestartHandler.postDelayed(mRestart,Delay);
    }
}
