package com.geoscope.GeoEye.UserAgentService;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TUserAgentWatcher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
		try {
			TUserAgentService UserAgentService = TUserAgentService.GetService();
			if ((UserAgentService != null) && (!UserAgentService.UserAgentServiceIsStarted()))
				UserAgentService.StartUserAgentService();
		} catch (Exception E) {
	        Toast.makeText(context, "error of user-agent service creating, "+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
}
