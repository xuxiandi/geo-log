package com.geoscope.GeoLog.TrackerService;

import android.content.Context;
import android.widget.Toast;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TTracker {

	public static final int DatumID = 23;
	//.
	private static TTracker Tracker = null;
	
    public static synchronized TTracker CreateTracker(Context context) throws Exception {
    	if (Tracker == null)
    		Tracker = new TTracker(context);
    	return Tracker;
	}    
    
    public static synchronized void FreeTracker() {
    	if (Tracker != null)
    	{
    		Tracker.Destroy();
    		Tracker = null;
    	}
    }
    
    public static synchronized TTracker GetTracker() {
    	if (Tracker == null)
    		return null; //. ->
    	if (Tracker.GeoLog == null)
    		return null; //. ->
    	return Tracker;
    }
    
    public static synchronized void EnableDisableTracker(boolean flEnable, Context context) throws Exception {
    	if (flEnable) {
    		if (Tracker == null)
    			Tracker = new TTracker(context);
    	}
    	else {
    		if (Tracker != null) {
    			Tracker.Destroy();
    			Tracker = null;
    		}
    	}
    }
    
    public static synchronized void RestartTracker(Context context) throws Exception {
		if (Tracker != null) {
			Tracker.Destroy();
			Tracker = new TTracker(context);
		}
    }
    
    public static synchronized boolean TrackerIsEnabled() {
    	return ((Tracker != null) && (Tracker.GeoLog.flEnabled));
    }
    
    public Context context;
	public TDEVICEModule GeoLog = null;
	
	private TTracker(Context pcontext) throws Exception {
		context = pcontext;
		//.
        GeoLog = new TDEVICEModule(context);
	}
	
	private void Destroy() {
		if (GeoLog != null) {
			try {
				GeoLog.Destroy();
			}
			catch (Exception E) {
                Toast.makeText(context, "Error of freeing tracker, "+E.getMessage(), Toast.LENGTH_LONG).show();
			}
			GeoLog = null;
		}
	}
	
	public synchronized void Check() throws Exception {
	}
}
