package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.Utils.TExceptionHandler;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerViewer extends Activity implements SurfaceHolder.Callback {
    
	private boolean 				flAudioEnabled = false;
	private boolean 				flVideoEnabled = false;
	//.
	private TVideoRecorderServerView VideoRecorderServerView;
	//.
	private SurfaceView svVideoRecorderServerViewer;
	private TextView lbVideoRecorderServer;
	//.
	private boolean IsInFront = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.video_recorder_server_viewer);
        //.
        svVideoRecorderServerViewer = (SurfaceView)findViewById(R.id.svVideoRecorderServerViewer);
        svVideoRecorderServerViewer.getHolder().addCallback(this);
        svVideoRecorderServerViewer.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowInitializationDialog();
			}
		});
        //.
        lbVideoRecorderServer = (TextView)findViewById(R.id.lbVideoRecorderServer);
        //.
        TReflector Reflector = TReflector.GetReflector();
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	flAudioEnabled = extras.getBoolean("flAudio");
        	flVideoEnabled = extras.getBoolean("flVideo");
        	//.
            //. TCP version VideoRecorderServerView = new TVideoRecorderServerViewTCP(this,extras.getString("GeographProxyServerAddress"), extras.getInt("GeographProxyServerPort"), extras.getInt("UserID"), extras.getString("UserPassword"), Reflector.CoGeoMonitorObjects.Items[extras.getInt("ObjectIndex")], flAudioEnabled, flVideoEnabled, null, new TExceptionHandler() {
        	VideoRecorderServerView = new TVideoRecorderServerViewUDPRTP(this,extras.getString("GeographProxyServerAddress"), TUDPEchoServerClient.ServerDefaultPort, extras.getInt("UserID"), extras.getString("UserPassword"), Reflector.CoGeoMonitorObjects.Items[extras.getInt("ObjectIndex")], flAudioEnabled, flVideoEnabled, null, new TExceptionHandler() {
				@Override
				public void DoOnException(Throwable E) {
					TVideoRecorderServerViewer.this.DoOnException(E);
				}
			}, lbVideoRecorderServer);
        	//.
        	try {
        		VideoRecorderServerView.Initialize();
        		//.
        		VideoRecorderServerView.UpdateInfo();
    		} catch (Exception E) {
    			DoOnException(E);
    		}
        }
    }
	
    public void onDestroy() {
    	try {
    		VideoRecorderServerView.Finalize();
		} catch (IOException E) {
			DoOnException(E);
		}
    	//.
		super.onDestroy();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		IsInFront = false;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IsInFront = true;
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	public void onStart() {
    	super.onStart();
    }
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		VideoRecorderServerView.VideoSurface_Set(arg0, arg2,arg3);
    	try {
    		VideoRecorderServerView.VideoClient_Initialize();
    		//.
    		VideoRecorderServerView.UpdateInfo();
		} catch (Exception E) {
			DoOnException(E);
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
    	try {
    		VideoRecorderServerView.VideoClient_Finalize();
    		VideoRecorderServerView.VideoSurface_Clear(arg0);
		} catch (Exception E) {
			DoOnException(E);
		}
	}
	
	private void ShowInitializationDialog() {
		int IC = 0;
		if (flAudioEnabled)
			IC++;
		if (flVideoEnabled)
			IC++;
    	final CharSequence[] _items = new CharSequence[IC];
    	final boolean[] Mask = new boolean[_items.length];
    	IC = 0;
    	if (flAudioEnabled) {
    		_items[IC] = getString(R.string.SAudio);
    		Mask[IC] = VideoRecorderServerView.flAudio;
    		//.
    		IC++;
    	}
    	if (flVideoEnabled) {
    		_items[IC] = getString(R.string.SVideo);
    		Mask[IC] = VideoRecorderServerView.flVideo;
    		//.
    		IC++;
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.SMode);
    	builder.setPositiveButton(getString(R.string.SOk), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					VideoRecorderServerView.Reinitialize();
					//.
					VideoRecorderServerView.UpdateInfo();
				} catch (Exception E) {
					DoOnException(E);
				}
			}
		});
    	builder.setNegativeButton(getString(R.string.SClose),null);
    	builder.setMultiChoiceItems(_items, Mask, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				switch (arg1) {
				
				case 0:
					if (flAudioEnabled)
						VideoRecorderServerView.flAudio = arg2;
					else
						if (flVideoEnabled)
							VideoRecorderServerView.flVideo = arg2;
					break; //. >
					
				case 1:
					if (flVideoEnabled)
						VideoRecorderServerView.flVideo = arg2;
					break; //. >
				}
			}
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
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
				Toast.makeText(TVideoRecorderServerViewer.this,EM,Toast.LENGTH_LONG).show();
				// .
				break; // . >
			}
		}
	};
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
