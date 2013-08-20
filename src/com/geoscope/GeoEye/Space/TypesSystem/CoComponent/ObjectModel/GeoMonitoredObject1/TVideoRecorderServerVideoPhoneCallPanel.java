package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;
import com.geoscope.GeoLog.Utils.TExceptionHandler;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerVideoPhoneCallPanel extends Activity {
    
	private TReflectorCoGeoMonitorObject Object = null;
	//.
	private TextView tvCallUserName;	
	private CheckBox cbCallUserWithAudio;
	private CheckBox cbCallUserWithVideo;
	private Button btnCallUser;
	//.
	private boolean flAudio = true;
	private boolean flVideo = true;
	//.
	private int		InitiatorID = 0;
	private String 	InitiatorName = null;
	//.
	private int		InitiatorComponentType = 0;
	private int		InitiatorComponentID = 0;
	//.
	private TGeoScopeServerInfo.TInfo ServersInfo;
	//.
	private String SessionID = null;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
        setContentView(R.layout.video_recorder_server_videophone_call_panel);
        //.
        Bundle extras = getIntent().getExtras();
        //.
        int ObjectID = extras.getInt("idComponent");
        String Name = extras.getString("Name");
        if (Name.length() == 0)
        	Name = "?";
        //.
		try {
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			//.
			InitiatorComponentType = Tracker.GeoLog.idTOwnerComponent;
			InitiatorComponentID = Tracker.GeoLog.idOwnerComponent;
			if (InitiatorComponentID == 0)
				throw new Exception(getString(R.string.SUnknownTrackerComponentID)); //. =>
			//.
	        Object = new TReflectorCoGeoMonitorObject(UserAgent.Server, ObjectID);
		} catch (Exception E) {
	    	Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
	    	return; //. ->
		}
		//.
		tvCallUserName = (TextView)findViewById(R.id.tvCallUserName);
		tvCallUserName.setText("  "+getString(R.string.SDoYouWantToContactTo)+Name+getString(R.string.SUsing));
		//.
        cbCallUserWithAudio = (CheckBox)findViewById(R.id.cbCallUserWithAudio);
        cbCallUserWithAudio.setChecked(flAudio);
        cbCallUserWithAudio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				flAudio = ((CheckBox)arg0).isChecked();
			}
		});
		//.
        cbCallUserWithVideo = (CheckBox)findViewById(R.id.cbCallUserWithVideo);
        cbCallUserWithVideo.setChecked(flVideo);
        cbCallUserWithVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				flVideo = ((CheckBox)arg0).isChecked();
			}
		});
		//.
        btnCallUser = (Button)findViewById(R.id.btnCallUser);
        btnCallUser.setOnClickListener(new OnClickListener() {
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
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SCallingUser)) {
			
			@Override
			public void Process() throws Exception {
				ServersInfo = Object.Server.Info.GetInfo();
				if (!ServersInfo.IsGeographProxyServerValid()) 
					throw new Exception(getString(R.string.SInvalidGeographProxyServer)); //. =>
				//.
				InitiatorID = TVideoRecorderServerVideoPhoneCallPanel.this.Object.Server.User.UserID;
				TUserDescriptor InitiatorInfo = TVideoRecorderServerVideoPhoneCallPanel.this.Object.Server.User.GetUserInfo(); 
				InitiatorName = InitiatorInfo.UserFullName;
				if ((InitiatorName == null) || (InitiatorName.length() < InitiatorInfo.UserName.length()))
					InitiatorName = InitiatorInfo.UserName;
				//.
				SessionID = TVideoRecorderServerVideoPhoneServer.SessionServer.StartRemoteSessionForObject(Object, InitiatorID,InitiatorName, InitiatorComponentType,InitiatorComponentID, flAudio,flVideo);
				//.
				if (Canceller.flCancel)
					throw new CancelException(); //. =>
				//. wait for session status
				TVideoRecorderServerVideoPhoneServer.TSessionServerClient SessionServerClient = new TVideoRecorderServerVideoPhoneServer.TSessionServerClient(TVideoRecorderServerVideoPhoneCallPanel.this, ServersInfo.GeographProxyServerAddress, ServersInfo.GeographProxyServerPort, Object.Server.User.UserID, Object.Server.User.UserPassword, Object, new TVideoRecorderServerVideoPhoneServer.TSession(SessionID), new TExceptionHandler() {
					@Override
					public void DoOnException(Throwable E) {
						TVideoRecorderServerVideoPhoneCallPanel.this.DoOnException(E);
					}
				});
				try {
					SessionServerClient.WaitForSessionAccept();
				}
				finally {
					SessionServerClient.Destroy();
				}
				//.
				if (Canceller.flCancel)
					throw new CancelException(); //. =>
			}
			@Override 
		    public void DoOnCancel() throws Exception {
				//. stop session request
				TVideoRecorderServerVideoPhoneServer.SessionServer.FinishRemoteSessionForObject(Object, SessionID);
				//.
				throw new CancelException(); //. =>
		    }
			@Override 
			public void DoOnCompleted() {
				TVideoRecorderServerVideoPhoneCallPanel.this.Start(InitiatorID,InitiatorName, SessionID);
			}
			@Override
			public void DoOnException(Exception E) {
				TVideoRecorderServerVideoPhoneCallPanel.this.DoOnException(E);
				TVideoRecorderServerVideoPhoneCallPanel.this.finish();
			}
			@Override
		    public void DoOnCancelIsOccured() {
				//. stop session request
				TAsyncProcessing CancellingSession = new TAsyncProcessing() {
					@Override
					public void Process() throws Exception {
						TVideoRecorderServerVideoPhoneServer.SessionServer.FinishRemoteSessionForObject(Object, SessionID);
					}
					@Override
					public void DoOnException(Exception E) {
						TVideoRecorderServerVideoPhoneCallPanel.this.DoOnException(E);
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
	
	private void Start(int InitiatorID, String InitiatorName, String SessionID) {
        finish();
        //.
		if (!TVideoRecorderServerVideoPhoneServer.Session_IsTheSameTo(SessionID)) {
			//.
	        Intent intent = new Intent(TVideoRecorderServerVideoPhoneCallPanel.this, TVideoRecorderServerVideoPhoneServer.class);
	    	intent.putExtra("InitiatorID",InitiatorID);
	    	intent.putExtra("InitiatorName",InitiatorName);
	    	intent.putExtra("idTComponent",SpaceDefines.idTCoComponent);
	    	intent.putExtra("idComponent",Object.ID);
	    	intent.putExtra("SessionID",SessionID);
	    	intent.putExtra("GeographProxyServerAddress",ServersInfo.GeographProxyServerAddress);
	    	intent.putExtra("GeographProxyServerPort",ServersInfo.GeographProxyServerPort);
	    	intent.putExtra("UserID",Object.Server.User.UserID);
	    	intent.putExtra("UserPassword",Object.Server.User.UserPassword);
	    	intent.putExtra("flAudio",flAudio);
	    	intent.putExtra("flVideo",flVideo);
	    	//.
	        startActivity(intent);
		}
	}
	
	private static final int MESSAGE_SHOWEXCEPTION = 1;
	
	@SuppressLint("HandlerLeak")
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
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
	};
	
	private void DoOnException(Throwable E) {
		if (E instanceof CancelException) 
			return; //. ->
		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
