package com.geoscope.GeoEye.UserAgentService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;


public class TUserAgentService extends Service {

    public static final int CheckUserAgentInterval = 60*1000; 
    public static final int UserAgentStartDelay = 10*1000; 
    public static final int REQUEST_CODE = 2;

    private static TUserAgentService Service = null;
    
    public static synchronized TUserAgentService GetService() {
    	return Service;
    }
    
    public static synchronized void SetService(TUserAgentService pService) {
    	Service = pService;
    }
    
    private class TUserAgentChecking implements Runnable {
    	
    	public static final int CheckingInterval = 1000*300; //. seconds
    	
    	protected Thread _Thread;
    	protected boolean flCancel = false;
    	
    	public TUserAgentChecking() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
		@Override
		public void run() {
			try {
				while (!flCancel) {
					Thread.sleep(CheckingInterval);
					//.
					TUserAgent UserAgent = TUserAgent.GetUserAgent();
					if (UserAgent != null)
						try {
							UserAgent.Check();
						}
						catch (Exception E) {
							TUserAgent.RestartUserAgent(TUserAgentService.this);
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
    
    private PendingIntent ServicePendingIntent = null;
    //.
    private boolean flStarted = false;
    private AlarmManager alarmManager;
    //.
    private TUserAgentChecking UserAgentChecking = null;
    

    @Override
    public void onCreate() {
        super.onCreate();
        //.
        setForeground(true);
        //.
    	Context context = getApplicationContext();
		Intent serviceLauncher = new Intent(context, TUserAgentService.class);
		ServicePendingIntent = PendingIntent.getService(context, 0, serviceLauncher, serviceLauncher.getFlags());
        //.
		try {
			TUserAgent.CreateUserAgent(this);
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
        TUserAgent.FreeUserAgent();
        //.
        super.onDestroy();
        //.
        DoPendingProcessRestart();    
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void RestartProcess() {
    	try {
    		TUserAgent.FreeUserAgent();
    	}
        finally {
        	DoPendingProcessRestart();
        	//.
        	System.exit(2);
        }
    }
    
    private void DoPendingProcessRestart() {
    	AlarmManager AM = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    	AM.set(AlarmManager.RTC, System.currentTimeMillis()+500, ServicePendingIntent);
    }
    
    private void StartServicing() {
        if (flStarted) 
        	return; //. ->
        //.
        UserAgentChecking = new TUserAgentChecking(); 
        //.
        Intent intent = new Intent(this, TUserAgentWatcher.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, REQUEST_CODE,intent,0);
        //.
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime()+UserAgentStartDelay,
                CheckUserAgentInterval,
                pendingIntent);
        alarmManager = null;
        //.
        flStarted = true;
    }
    
    private void StopServicing() {
    	if (!flStarted)
    		return; //. ->
    	//.
        Intent intent = new Intent(this, TUserAgentWatcher.class);
        //.
        alarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        alarmManager.cancel(PendingIntent.getBroadcast(this, REQUEST_CODE,intent,0));
        alarmManager = null;
        //.
        if (UserAgentChecking != null) {
        	UserAgentChecking.CancelAndWait();
        	UserAgentChecking = null;
        }
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
