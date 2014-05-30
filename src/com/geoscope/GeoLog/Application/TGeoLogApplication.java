package com.geoscope.GeoLog.Application;

import android.content.Context;
import android.content.Intent;

import com.geoscope.GeoEye.UserAgentService.TUserAgentService;
import com.geoscope.GeoLog.TrackerService.TTrackerService;

public class TGeoLogApplication {

	public static void Terminate(Context context) {
		Intent UserAgentServiceLauncher = new Intent(context, TUserAgentService.class);
		context.stopService(UserAgentServiceLauncher);
		Intent TrackerServiceLauncher = new Intent(context, TTrackerService.class);
		context.stopService(TrackerServiceLauncher);
		//.
		System.exit(0);
	}
}
