package com.geoscope.GeoEye.UserAgentService;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingCommandMessage;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingCommandResponseMessage;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TIncomingMessage;
import com.geoscope.GeoLog.Application.TGeoLogApplication;


public class TUserAgentService extends Service {

    public static final int CheckUserAgentInterval = 60*1000; 
    public static final int UserAgentStartDelay = 10*1000; 
    public static final int REQUEST_CODE = 2;

    public static void PendingRestart(Context context) {
    	try {
    		TUserAgent.FreeUserAgent();
    	}
        finally {
        	DoPendingRestart(context);
        }
    }
    
    public static void DoPendingRestart(Context context) {
		Intent serviceLauncher = new Intent(context, TUserAgentService.class);
		PendingIntent ServicePendingIntent = PendingIntent.getService(context, 0, serviceLauncher, serviceLauncher.getFlags());
		//.
    	AlarmManager AM = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
    	AM.set(AlarmManager.RTC, System.currentTimeMillis()+1000, ServicePendingIntent);
    }
    
    private static TUserAgentService Service = null;
    
    public static synchronized TUserAgentService GetService() {
    	return Service;
    }
    
    public static synchronized void SetService(TUserAgentService pService) {
    	Service = pService;
    }
    
	private class TUserAgentIncomingMessageReceiver extends TGeoScopeServerUser.TIncomingMessages.TReceiver {
		
		private TGeoScopeServerUser MyUser;
		
		public TUserAgentIncomingMessageReceiver(TGeoScopeServerUser pUser) throws Exception {
			MyUser = pUser;
			//.
			MyUser.IncomingMessages.AddReceiver(this,true,true);
		}
		
		public void Destroy() {
			if (MyUser.IncomingMessages != null)
				MyUser.IncomingMessages.RemoveReceiver(this);
		}
		
		@Override
		public boolean DoOnCommand(TGeoScopeServerUser User, TIncomingCommandMessage Message) {
			return false;
		}

		@Override
		public boolean DoOnCommandResponse(TGeoScopeServerUser User, TIncomingCommandResponseMessage Message) {
			return false;
		}
		
		@Override
		public boolean DoOnMessage(TGeoScopeServerUser User, TIncomingMessage Message) {
			if ((TReflector.GetReflector() == null) || (!TReflector.flScreenIsOn)) {
				ShowNotification(Message);
			}
			return false; 
		}
		
		@SuppressWarnings("deprecation")
		private void ShowNotification(TIncomingMessage Message) {
	        Intent intent = new Intent(getApplicationContext(), TReflector.class);
	        //.
	        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	        //.
	        PendingIntent ContentIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
	        //.
	        CharSequence TickerText = getString(R.string.SYouHaveUnreadMessage);
	        long Timestamp = System.currentTimeMillis();
	        int Icon = R.drawable.icon;
			Notification notification = new Notification(Icon,TickerText,Timestamp);
	        CharSequence ContentTitle = getString(R.string.SNewMessageFromUser)+Message.Sender.UserName;
	        CharSequence ContentText = getString(R.string.SClickHereToSee);
	        notification.setLatestEventInfo(getApplicationContext(), ContentTitle, ContentText, ContentIntent);
	        notification.defaults = (notification.defaults | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
	        notification.flags = (notification.flags | Notification.FLAG_AUTO_CANCEL);
	        //.
	        int NotificationID = 1;
	        nm.notify(NotificationID, notification);
		}	
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
							RestartUserAgentService();
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
    private TUserAgentIncomingMessageReceiver	UserAgentIncomingMessageReceiver;
    private TUserAgentChecking 					UserAgentChecking = null;
    
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
			StartUserAgentService();
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

    public synchronized void StartUserAgentService() throws Exception {
    	TUserAgent UserAgent = TUserAgent.GetUserAgent(); 
		if (UserAgent == null) 
			UserAgent = TUserAgent.CreateUserAgent(this);
		//.
		if (UserAgentIncomingMessageReceiver != null) {
			UserAgentIncomingMessageReceiver.Destroy();
			UserAgentIncomingMessageReceiver = null;
		}
		UserAgentIncomingMessageReceiver = new TUserAgentIncomingMessageReceiver(UserAgent.User());
    }
    
    public synchronized void StopUserAgentService() {
		if (UserAgentIncomingMessageReceiver != null) {
			UserAgentIncomingMessageReceiver.Destroy();
			UserAgentIncomingMessageReceiver = null;
		}
		//.
		TUserAgent.FreeUserAgent();
    }
    
    public synchronized void RestartUserAgentService() throws Exception {
    	StopUserAgentService();
    	StartUserAgentService();
    }
    
    public synchronized boolean UserAgentServiceIsStarted() throws Exception {
    	return (TUserAgent.GetUserAgent() != null);
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
    
    public void StopServicing() {
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
    	StopUserAgentService();
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
