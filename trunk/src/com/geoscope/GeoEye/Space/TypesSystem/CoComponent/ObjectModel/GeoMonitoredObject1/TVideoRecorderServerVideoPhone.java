package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderPanel;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;
import com.geoscope.GeoLog.Utils.TExceptionHandler;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerVideoPhone extends TVideoRecorderPanel {
    
	private TReflectorCoGeoMonitorObject Object;
	//.
	private boolean 				flAudioEnabled = false;
	private boolean 				flVideoEnabled = false;
	//.
	public boolean					flConversation = false;
	//.
	private TVideoRecorderServerView VideoRecorderServerView;
	//.
	private boolean IsInFront = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        svSurface.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowInitializationDialog();
			}
		});
        //.
        TReflector Reflector = TReflector.GetReflector();
        //.
        Bundle extras = getIntent().getExtras();
    	//.
    	Object = Reflector.CoGeoMonitorObjects.Items[extras.getInt("ObjectIndex")];
        //.
    	flAudioEnabled = extras.getBoolean("flAudio");
    	flVideoEnabled = extras.getBoolean("flVideo");
    	//.
    	VideoRecorderServerView = new TVideoRecorderServerView(this,extras.getString("GeographProxyServerAddress"), extras.getInt("GeographProxyServerPort"), extras.getInt("UserID"), extras.getString("UserPassword"), Object, flAudioEnabled, flVideoEnabled, new TExceptionHandler() {
			@Override
			public void DoOnException(Throwable E) {
				TVideoRecorderServerVideoPhone.this.DoOnException(E);
			}
		}, lbStatus);
    	//.
    	try {
    		VideoRecorderServerView.Initialize();
		} catch (Exception E) {
			DoOnException(E);
		}
        SetSurface(true);
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
		//.
		Hangup();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
		IsInFront = true;
		//.
		Call();
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
	public void DoOnSurfaceIsCreated(SurfaceHolder SH) {
	}
	
	@Override
	public void DoOnSurfaceIsChanged(SurfaceHolder SH, int Format, int Width, int Height) {
		VideoRecorderServerView.VideoSurface_Set(SH, Width,Height);
		VideoRecorderServerView.VideoClient_Initialize();
	}
	
	@Override
	public void DoOnSurfaceIsDestroyed(SurfaceHolder SH) {
		VideoRecorderServerView.VideoClient_Finalize();
		VideoRecorderServerView.VideoSurface_Clear();
	}
	
	private void Call() {
		TAsyncProcessing Processing = new TAsyncProcessing(this) {
			@Override
			public void Process() throws Exception {
				int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+8/*Set VideoRecorderModule.Recording*/;
				byte[] Data = new byte[] {1};
				Object.SetData(DataType, Data);
			}
			
			@Override 
			public void DoOnCompleted() {
				flConversation = true;
			}
			
			@Override
			public void DoOnException(Exception E) {
				TVideoRecorderServerVideoPhone.this.DoOnException(E);
			}
		};
		Processing.Start();
	}
	
	private void Hangup() {
		TAsyncProcessing Processing = new TAsyncProcessing(null) {
			@Override
			public void Process() throws Exception {
				int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+8/*Set VideoRecorderModule.Recording*/;
				byte[] Data = new byte[] {0};
				Object.SetData(DataType, Data);
			}
			
			@Override 
			public void DoOnCompleted() {
				flConversation = false;
			}
			
			@Override
			public void DoOnException(Exception E) {
				TVideoRecorderServerVideoPhone.this.DoOnException(E);
			}
		};
		Processing.Start();
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
				Toast.makeText(TVideoRecorderServerVideoPhone.this,EM,Toast.LENGTH_LONG).show();
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
