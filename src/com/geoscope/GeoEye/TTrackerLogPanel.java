package com.geoscope.GeoEye;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.geoscope.GeoLog.TrackerService.TTracker;

public class TTrackerLogPanel extends Activity {

	private TextView tvContent;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.tracker_log_panel);
        //.
        tvContent = (TextView)findViewById(R.id.tvTrackerLogContent);
        //.
        TTracker Tracker = TTracker.GetTracker();
        if (Tracker != null) {
        	String LogString = Tracker.GeoLog.Log.ToString();
        	tvContent.setText(LogString);
        }
    }

}