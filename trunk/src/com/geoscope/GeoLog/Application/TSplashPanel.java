package com.geoscope.GeoLog.Application;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoLog.Application.Installator.TGeoLogInstallator;

public class TSplashPanel extends Activity {

	public static final int MODE__UNKNOWN 					= 0;
	public static final int MODE__START_REFLECTOR_ON_FINISH = 1;
	
    private static final int SPLASH_DURATION = 2000; // milliseconds to show

    private int Mode = MODE__UNKNOWN;
    //.
	private TextView tvStatus;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			Mode = extras.getInt("Mode");
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.application_splash_panel);
        //.
        tvStatus = (TextView)findViewById(R.id.tvStatus);
        //.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	try {
        			TGeoLogInstallator.CheckInstallation(TSplashPanel.this.getApplicationContext());
        		} catch (Exception E) {
                    Toast.makeText(TSplashPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
        		}
        		if (Mode == MODE__START_REFLECTOR_ON_FINISH) {
            		Intent intent = new Intent(getApplicationContext(),TReflector.class);
    	    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		getApplicationContext().startActivity(intent);
        		}
                //.
        		finish();
            }
        }, SPLASH_DURATION);
        //.
		try {
			if (!TGeoLogInstallator.InstallationIsUpToDate(getApplicationContext())) {
				tvStatus.setText(R.string.SInstallingProgramFiles);
			}
			else
				tvStatus.setText("");
		} catch (IOException E) {
            Toast.makeText(TSplashPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            //.
    		finish();
    		return; //. ->
		}
        //.
        setResult(Activity.RESULT_CANCELED);
    }
}