package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule;

import java.net.SocketException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.Exception.TExceptionHandler;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerVideoPhoneServer.TSession;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerVideoPhoneCallPanel extends Activity {
    
	public static class TAudioCalling extends TAsyncProcessing {

		private Context context;
		
		public TAudioCalling(Context pcontext) {
			super(null);
			context = pcontext;
			Start();
		}
		
		@Override
		public void Process() throws Exception {
			while (!Canceller.flCancel) { 
				DoCalling();
	    		Thread.sleep(1000);
			}
		}

	    private void DoCalling() throws InterruptedException, CancelException {
	    	Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
	    	Ringtone r = RingtoneManager.getRingtone(context.getApplicationContext(), notification);
	    	r.play();
	    	try {
		    	while (r.isPlaying()) {
		    		Thread.sleep(100);
		    		if (Canceller.flCancel)
		    			throw new CancelException(); //. =>
		    	}
	    	}
	    	finally {
	    		r.stop();
	    	}
	    }
	}
	
	private TVideoRecorderServerVideoPhoneServer.TSession Session;	
	//.
	private TextView tvCallUserName;	
	private CheckBox cbCallUserWithAudio;
	private CheckBox cbCallUserWithVideo;
	private Button btnCallUser;
	//.
	private Context context;
	//.
	private TTracker Tracker;
	private TUserAgent UserAgent;
	//.
	private int		InitiatorComponentType = 0;
	private long 	InitiatorComponentID = 0;
	//.
	private TGeoScopeServerInfo.TInfo ServersInfo;
	//.
	private TAudioCalling AudioCalling = null;
	//.
	private boolean flCancelSession = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
        setContentView(R.layout.video_recorder_server_videophone_call_panel);
        //.
        Bundle extras = getIntent().getExtras();
        //.
        long ObjectID = extras.getLong("idComponent");
        String Name = extras.getString("Name");
        if (Name.length() == 0)
        	Name = "?";
        //.
		try {
	    	Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			//.
			context = Tracker.GeoLog.context;
			//.
			InitiatorComponentType = Tracker.GeoLog.idTOwnerComponent;
			InitiatorComponentID = Tracker.GeoLog.idOwnerComponent;
			if (InitiatorComponentID == 0)
				throw new Exception(getString(R.string.SUnknownTrackerComponentID)); //. =>
			//.
			Session = new TSession("",0,"",SpaceDefines.idTCoComponent,ObjectID, true,false, Tracker.GeoLog,UserAgent);
			Session.Object = new TCoGeoMonitorObject(UserAgent.Server, ObjectID);
		} catch (Exception E) {
	    	Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
	    	return; //. ->
		}
		//.
		tvCallUserName = (TextView)findViewById(R.id.tvCallUserName);
		tvCallUserName.setText("  "+getString(R.string.SDoYouWantToContactTo)+Name+getString(R.string.SUsing));
		//.
        cbCallUserWithAudio = (CheckBox)findViewById(R.id.cbCallUserWithAudio);
        cbCallUserWithAudio.setChecked(Session.flAudio);
        cbCallUserWithAudio.setOnClickListener(new OnClickListener() {
			
        	@Override
			public void onClick(View arg0) {
				Session.flAudio = ((CheckBox)arg0).isChecked();
			}
		});
		//.
        cbCallUserWithVideo = (CheckBox)findViewById(R.id.cbCallUserWithVideo);
        cbCallUserWithVideo.setChecked(Session.flVideo);
        cbCallUserWithVideo.setOnClickListener(new OnClickListener() {
			
        	@Override
			public void onClick(View arg0) {
				Session.flVideo = ((CheckBox)arg0).isChecked();
			}
		});
		//.
        btnCallUser = (Button)findViewById(R.id.btnCallUser);
        btnCallUser.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
                try {
        			Call();
        		} catch (Exception E) {
        			DoOnException(E);
        		}
            }
        });
    }
	
    public void onDestroy() {
		super.onDestroy();
    }
    
	private void Call() throws Exception {
		AudioCalling = new TAudioCalling(this);
		//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SCallingUser)) {
			
			@Override
			public void Process() throws Exception {
				ServersInfo = Session.Object.Server.Info.GetInfo();
				if (!ServersInfo.IsGeographProxyServerValid()) 
					throw new Exception(getString(R.string.SInvalidGeographProxyServer)); //. =>
				//.
				Session.InitiatorID = Session.Object.Server.User.UserID;
				TUserDescriptor InitiatorInfo = Session.Object.Server.User.GetUserInfo(); 
				Session.InitiatorName = InitiatorInfo.UserFullName;
				if ((Session.InitiatorName == null) || (Session.InitiatorName.length() < InitiatorInfo.UserName.length()))
					Session.InitiatorName = InitiatorInfo.UserName;
				//.
				Session.SetSessionID(TVideoRecorderServerVideoPhoneServer.SessionServer.StartRemoteSessionForObject(TVideoRecorderServerVideoPhoneCallPanel.this, Session.Object, Session.InitiatorID,Session.InitiatorName, InitiatorComponentType,InitiatorComponentID, Session.flAudio,Session.flVideo));
				//.
				if (Canceller.flCancel)
					throw new CancelException(); //. =>
				//. wait for session status
				Session.SessionServerClient = new TVideoRecorderServerVideoPhoneServer.TSessionServerClient(TVideoRecorderServerVideoPhoneCallPanel.this, ServersInfo.GeographProxyServerAddress, ServersInfo.GeographProxyServerPort, Session.Object.Server.User.UserID, Session.Object.Server.User.UserPassword, Session, new TExceptionHandler() {
					@Override
					public void DoOnException(Throwable E) {
						TVideoRecorderServerVideoPhoneCallPanel.this.DoOnException(E);
					}
				});
				//.
				Session.SessionServerClient.WaitForSessionOpen();
				//.
				if (Canceller.flCancel)
					throw new CancelException(); //. =>
			}
			@Override 
		    public void DoOnCancel() throws Exception {
				//. stop session request
				synchronized (TVideoRecorderServerVideoPhoneCallPanel.this) {
					flCancelSession = (Session.GetSessionID() == null);
				}
				if (flCancelSession) {
					TVideoRecorderServerVideoPhoneServer.SessionServer.FinishRemoteSessionForObject(TVideoRecorderServerVideoPhoneCallPanel.this, Session.Object, Session.GetSessionID());
					synchronized (TVideoRecorderServerVideoPhoneCallPanel.this) {
						Session.SetPanel(null);
					}
				}
				//.
				throw new CancelException(); //. =>
		    }
			@Override 
			public void DoOnCompleted() throws Exception {
	        	if (AudioCalling != null) {
	        		AudioCalling.Destroy();
	        		AudioCalling = null;
	        	}
	        	//.
				TVideoRecorderServerVideoPhoneCallPanel.this.Start();
			}
			@Override
			public void DoOnException(Exception E) {
	        	if (AudioCalling != null) {
	        		try {
						AudioCalling.Destroy();
					} catch (InterruptedException IE) {
					}
	        		AudioCalling = null;
	        	}
	        	//.
				TVideoRecorderServerVideoPhoneCallPanel.this.DoOnException(E);
				TVideoRecorderServerVideoPhoneCallPanel.this.finish();
			}
			@Override
		    public void DoOnCancelIsOccured() {
	        	if (AudioCalling != null) {
	        		try {
		        		AudioCalling.Destroy();
					} catch (InterruptedException IE) {
					}
	        		AudioCalling = null;
	        	}
				//. stop session request
				TAsyncProcessing CancellingSession = new TAsyncProcessing() {
					@Override
					public void Process() throws Exception {
						String SessionID;
						synchronized (TVideoRecorderServerVideoPhoneCallPanel.this) {
							SessionID = Session.GetSessionID();
						}
						if (SessionID != null) {
							TVideoRecorderServerVideoPhoneServer.SessionServer.FinishRemoteSessionForObject(TVideoRecorderServerVideoPhoneCallPanel.this, Session.Object, SessionID);
							synchronized (TVideoRecorderServerVideoPhoneCallPanel.this) {
								Session.SetSessionID(null);
							}
						}
					}
					@Override
					public void DoOnException(Exception E) {
						///- TVideoRecorderServerVideoPhoneCallPanel.this.DoOnException(E);
						TVideoRecorderServerVideoPhoneCallPanel.this.finish();
					}
				};
				CancellingSession.Start();
				//.
				TVideoRecorderServerVideoPhoneCallPanel.this.finish();
		    }			
		};
		Processing.Start();
	}
	
	private void Start() {
        finish();
        //.
		if (!TVideoRecorderServerVideoPhoneServer.SessionServer.Session_IsTheSameTo(Session)) {
	        TVideoRecorderServerVideoPhoneServer.Session = Session;
	        Intent intent = new Intent(context, TVideoRecorderServerVideoPhoneServer.class);
    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    		//.
	    	intent.putExtra("GeographProxyServerAddress",ServersInfo.GeographProxyServerAddress);
	    	intent.putExtra("GeographProxyServerPort",ServersInfo.GeographProxyServerPort);
	    	intent.putExtra("UserID",Session.Object.Server.User.UserID);
	    	intent.putExtra("UserPassword",Session.Object.Server.User.UserPassword);
	    	//.
	    	context.startActivity(intent);
		}
		else { //. loopback test
			TVideoRecorderServerVideoPhoneServer.TSession _Session = TVideoRecorderServerVideoPhoneServer.SessionServer.Session_Get(Session.GetSessionID());	
			if ((_Session != null) && (_Session.Panel != null))
				try {
					((TVideoRecorderServerVideoPhoneServer)_Session.Panel).ContactSession();
				} catch (Exception E) {
				}
		}
	}
	
	private static final int MESSAGE_SHOWEXCEPTION = 1;
	
	@SuppressLint("HandlerLeak")
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_SHOWEXCEPTION:
    				Throwable E = (Throwable)msg.obj;
    				String EM = E.getMessage();
    				if (EM == null) 
    					EM = E.getClass().getName();
    				//.
    				Toast.makeText(TVideoRecorderServerVideoPhoneCallPanel.this,EM,Toast.LENGTH_LONG).show();
    				// .
    				break; // . >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	private void DoOnException(Throwable E) {
		if (flCancelSession)
			return; //. ->
		if (E instanceof SocketException) 
			return; //. ->
		if (E instanceof CancelException) 
			return; //. ->
		if ((E instanceof OperationException) && ((((OperationException)E).Code == TGeographServerServiceOperation.ErrorCode_ConnectionIsClosedByWorkerThreadTermination) || (((OperationException)E).Code == TGeographServerServiceOperation.ErrorCode_ConnectionIsClosedUnexpectedly) || (((OperationException)E).Code == TGeographServerServiceOperation.ErrorCode_ConnectionIsClosedGracefully))) 
			return; //. ->
		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
