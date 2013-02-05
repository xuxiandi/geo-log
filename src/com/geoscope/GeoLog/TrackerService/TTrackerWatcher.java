package com.geoscope.GeoLog.TrackerService;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TTrackerWatcher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
		try {
	    	if (TTracker.TrackerIsNull())
				TTracker.CreateTracker(context);
		} catch (Exception E) {
	        Toast.makeText(context, "error of tracker creating, "+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
}
