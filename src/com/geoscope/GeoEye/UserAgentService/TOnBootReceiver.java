package com.geoscope.GeoEye.UserAgentService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class TOnBootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String Action = intent.getAction();
		if (Action.equals(Intent.ACTION_BOOT_COMPLETED)
				|| Action.equals(Intent.ACTION_USER_PRESENT)) {
			Intent serviceLauncher = new Intent(context, TUserAgentService.class);
			context.startService(serviceLauncher);
		}
	}
}
