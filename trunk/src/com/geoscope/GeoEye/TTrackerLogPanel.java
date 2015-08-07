package com.geoscope.GeoEye;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoLog.TrackerService.TTracker;

public class TTrackerLogPanel extends Activity {

	private TextView tvContent;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        setContentView(R.layout.tracker_log_panel);
        //.
		try {
	        tvContent = (TextView)findViewById(R.id.tvTrackerLogContent);
	        //.
	        TTracker Tracker = TTracker.GetTracker(this.getApplicationContext());
	        if (Tracker != null) {
	        	String LogString = Tracker.GeoLog.Log.ToString();
	        	tvContent.setText(LogString);
	        }
		}
		catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(this, S, Toast.LENGTH_LONG).show();  						
		}
    }

}