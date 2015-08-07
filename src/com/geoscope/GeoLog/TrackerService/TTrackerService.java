package com.geoscope.GeoLog.TrackerService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

import com.geoscope.GeoLog.Application.TGeoLogApplication;


public class TTrackerService extends Service {

    public static final int CheckTrackerInterval = 1000*60; //. seconds 
    public static final int TrackerStartDelay = 1000*10; //. seconds 
    public static final int TrackerRestartOnFailureDelay = 100; //. milliseconds
    
    public static final int REQUEST_CODE = 1;

    public static void PendingRestart(Context context) {
    	try {
    		TTracker.FreeTracker();
    	}
        finally {
        	DoPendingRestart(context);
        }
    }
    
    private static void DoPendingRestart(Context context) {
		Intent serviceLauncher = new Intent(context, TTrackerService.class);
		PendingIntent ServicePendingIntent = PendingIntent.getService(context, 0, serviceLauncher, serviceLauncher.getFlags());
		//.
    	AlarmManager AM = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	AM.set(AlarmManager.RTC, System.currentTimeMillis()+TrackerRestartOnFailureDelay, ServicePendingIntent);
    }
    
    private static TTrackerService Service = null;
    
    public static synchronized TTrackerService GetService() {
    	return Service;
    }
    
    public static synchronized void SetService(TTrackerService pService) {
    	Service = pService;
    }
    
    private class TTrackerChecking implements Runnable {
    	
    	public static final int CheckingInterval = 1000*300; //. seconds
    	
    	protected Thread _Thread;
    	protected boolean flCancel = false;
    	
    	public TTrackerChecking() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
		@Override
		public void run() {
			try {
				while (!flCancel) {
					Thread.sleep(CheckingInterval);
					//.
					try {
						TTracker Tracker = TTracker.GetTracker(TTrackerService.this);
						Tracker.Check();
					}
					catch (Exception E) {
						TTracker.RestartTracker(TTrackerService.this);
					}
				}
			}
			catch (InterruptedException E) {
			}
			catch (Throwable E1) {
			}
		}
		
    	@SuppressWarnings("unused")
		public void Join() {
    		try {
    			_Thread.join();
    		}
    		catch (Exception E) {}
    	}

    	public void Cancel() {
    		flCancel = true;
    		//.
			_Thread.interrupt();
    	}
    
		public void CancelAndWait() {
    		Cancel();
    		try {
				_Thread.join();
			} catch (InterruptedException e) {
			}
    	}
    }
    
    private boolean flStarted = false;
    private AlarmManager alarmManager;
    //.
    private TTrackerChecking TrackerChecking = null;    

    @Override
    public void onCreate() {
        super.onCreate();
        //.
		try {
			TGeoLogApplication.InitializeInstance(getApplicationContext());
		}
		catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
        //.
		try {
			TTracker.CreateTracker(this);
		}
		catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
        StartServicing();
        //.
        SetService(this);
    }

    @Override
    public void onDestroy() {
        SetService(null);
        //.
    	StopServicing();
        //.
        super.onDestroy();
        //.
        DoPendingRestart(getApplicationContext());    
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void StartServicing() {
        if (flStarted) 
        	return; //. ->
        //.
        TrackerChecking = new TTrackerChecking(); 
        //.
        Intent intent = new Intent(this, TTrackerWatcher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE,intent,0);
        //.
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+TrackerStartDelay,
                CheckTrackerInterval,
                pendingIntent);
        alarmManager = null;
        //.
        flStarted = true;
    }
    
    public void StopServicing() {
    	if (!flStarted)
    		return; //. ->
    	//.
        Intent intent = new Intent(this, TTrackerWatcher.class);
        //.
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE,intent,0));
        alarmManager = null;
        //.
        if (TrackerChecking != null) {
        	TrackerChecking.CancelAndWait();
        	TrackerChecking = null;
        }
        //.
        TTracker.FreeTracker();
        //.
        flStarted = false;        
    }
    
    public void SetServicing(boolean Active) {
    	if (Active)
    		StartServicing();
    	else
    		StopServicing();
    }
}
