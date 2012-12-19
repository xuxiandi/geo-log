package com.geoscope.GeoEye.UserAgentService;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class TUserAgentWatcher extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
		try {
			TUserAgent.CreateUserAgent(context);
		} catch (Exception E) {
	        Toast.makeText(context, "error of user-agent creating, "+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
}
