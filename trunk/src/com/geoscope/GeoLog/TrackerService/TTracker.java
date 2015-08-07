package com.geoscope.GeoLog.TrackerService;

import android.content.Context;
import android.widget.Toast;

import com.geoscope.Classes.IO.File.TFileSystem;
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
    	if (Tracker != null) {
    		Tracker.Destroy();
    		Tracker = null;
    	}
    }
    
    public static synchronized TTracker GetTracker(Context context) throws Exception {
    	if (Tracker == null)
    		Tracker = new TTracker(context);
    	return Tracker;
    }
    
    public static synchronized boolean TrackerIsNull() {
    	return (Tracker == null);
    }
    
    public static synchronized void EnableDisableTracker(boolean flEnable) throws Exception {
		if (Tracker != null) 
			Tracker.GeoLog.SetEnabled(flEnable);
		else
			throw new Exception("Tracker is null"); //. =>
    }
    
    public static synchronized void RestartTracker(Context context) throws Exception {
		if (Tracker != null) 
			Tracker.Destroy();
		Tracker = new TTracker(context);
    }
    
    public static synchronized boolean TrackerIsEnabled() {
    	return ((Tracker != null) && (Tracker.GeoLog.IsEnabled()));
    }
    
    public static synchronized void Tracker_Log_WriteInfo(String Source, String Info) {
    	if (Tracker != null)
    		Tracker.GeoLog.Log.WriteInfo(Source, Info);
    }
    
    public static synchronized void Tracker_Log_WriteWarning(String Source, String Warning) {
    	if (Tracker != null)
    		Tracker.GeoLog.Log.WriteWarning(Source, Warning);
    }
    
    public static synchronized void Tracker_Log_WriteError(String Source, String Error) {
    	if (Tracker != null)
    		Tracker.GeoLog.Log.WriteError(Source, Error);
    }
    
    public Context context;
	public TDEVICEModule GeoLog = null;
	
	private TTracker(Context pcontext) throws Exception {
		TFileSystem.TExternalStorage.WaitForMounted();
		//.
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
