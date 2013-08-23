package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TVideoRecorderServerVideoPhoneServer.TSession;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerVideoPhoneCallNotificationPanel extends Activity {

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
	    		Thread.sleep(3000);
			}
		}

	    private void DoCalling() throws InterruptedException, CancelException {
	    	Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
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
	
	public static TVideoRecorderServerVideoPhoneServer.TSession Session = null;
	
	private TVideoRecorderServerVideoPhoneServer.TSession _Session;
	//.
	private TextView tvCallingUserName;	
	private CheckBox cbCallingUserWithAudio;
	private CheckBox cbCallingUserWithVideo;
	private Button btnAcceptCallingUser;
	private Button btnRejectCallingUser;
	//.
	private boolean flAccepted = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
        setContentView(R.layout.video_recorder_server_videophone_call_notification_panel);
        //.
        if (Session.GetStatus() != TSession.SESSION_STATUS_CALL) {
        	finish();
        	return; //. ->
        }
        _Session = Session; Session = null;
        _Session.SetPanel(this);
        //.
        Bundle extras = getIntent().getExtras();
        //.
        String Name = extras.getString("InitiatorName");
        if (Name.length() == 0)
        	Name = "?";
		//.
		setTitle(R.string.SAttentionIncomingCall);
		//.
		tvCallingUserName = (TextView)findViewById(R.id.tvCallingUserName);
		tvCallingUserName.setText(Name+getString(R.string.SIsCalling));
		//.
        cbCallingUserWithAudio = (CheckBox)findViewById(R.id.cbCallingUserWithAudio);
        cbCallingUserWithAudio.setChecked(_Session.flAudio);
        cbCallingUserWithAudio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_Session.flAudio = ((CheckBox)arg0).isChecked();
			}
		});
		//.
        cbCallingUserWithVideo = (CheckBox)findViewById(R.id.cbCallingUserWithVideo);
        cbCallingUserWithVideo.setChecked(_Session.flVideo);
        cbCallingUserWithVideo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				_Session.flVideo = ((CheckBox)arg0).isChecked();
			}
		});
		//.
        btnAcceptCallingUser = (Button)findViewById(R.id.btnAcceptCallingUser);
        btnAcceptCallingUser.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	flAccepted = true;
            	//.
            	finish();
            }
        });
		//.
        btnRejectCallingUser = (Button)findViewById(R.id.btnRejectCallingUser);
        btnRejectCallingUser.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	finish();
            }
        });
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	//.
    	if (flAccepted)
    		TVideoRecorderServerVideoPhoneServer.SessionServer.AcceptSession(_Session);
    	else
    		TVideoRecorderServerVideoPhoneServer.SessionServer.RejectSession(_Session);
    }
}
