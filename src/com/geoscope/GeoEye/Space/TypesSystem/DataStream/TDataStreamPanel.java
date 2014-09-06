package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor.TChannelIDs;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessor.TOnExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessor.TOnIdleHandler;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessor.TOnProgressHandler;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TDataStreamPanel extends Activity implements SurfaceHolder.Callback {

	private String 	ServerAddress;
	private int 	ServerPort;
	//.
	private int 	UserID;
	private String 	UserPassword;
	//.
	private long 	idComponent;
	//.
	private byte[] 		StreamDescriptor;
	private TChannelIDs	StreamChannels = new TChannelIDs();
	//.
	private SurfaceView svSurface;
	private TextView lbTitle;
	private TextView lbStatus;
	//.
	private boolean IsInFront = false;
	//.
	private ArrayList<TStreamChannelProcessor> StreamChannelProcessors = new ArrayList<TStreamChannelProcessor>();
	
    public void onCreate(Bundle savedInstanceState) {
    	try {
            super.onCreate(savedInstanceState);
            //.
            Bundle extras = getIntent().getExtras(); 
            if (extras != null) {
            	ServerAddress = extras.getString("ServerAddress");
            	ServerPort = extras.getInt("ServerPort");
            	//.
            	UserID = extras.getInt("UserID");
            	UserPassword = extras.getString("UserPassword");
            	//.
            	idComponent = extras.getLong("idComponent");
            	//.
            	StreamDescriptor = extras.getByteArray("StreamDescriptor");
            	//.
            	byte[] StreamChannelsBA = extras.getByteArray("StreamChannels");
            	StreamChannels.FromByteArray(StreamChannelsBA);
            }
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		//.
            setContentView(R.layout.datastream_panel);
            //.
            svSurface = (SurfaceView)findViewById(R.id.svSurface);
            svSurface.getHolder().addCallback(this);
            //.
            lbTitle = (TextView)findViewById(R.id.lbTitle);
            lbStatus = (TextView)findViewById(R.id.lbStatus);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
    }
	
    public void onDestroy() {
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
    	try {
    		StreamChannelProcessors_Initialize(arg0, arg2,arg3);
		} catch (Exception E) {
			DoOnException(E);
		}
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
    	try {
    		StreamChannelProcessors_Finalize();
		} catch (Exception E) {
			DoOnException(E);
		}
	}
	
	private void StreamChannelProcessors_Initialize(SurfaceHolder SH, int Width, int Height) throws Exception {
		StreamChannelProcessors_Finalize();
		//.
		TDataStreamDescriptor SD = new TDataStreamDescriptor(StreamDescriptor);
		StringBuilder SB = new StringBuilder();
		for (int I = 0; I < SD.Channels.size(); I++) {
			TDataStreamDescriptor.TChannel Channel = SD.Channels.get(I);
			if (StreamChannels.IDExists(Channel.ID)) {
				TStreamChannelProcessor ChannelProcessor = TStreamChannelProcessor.GetProcessor(this, ServerAddress,ServerPort, UserID,UserPassword, SpaceDefines.idTDataStream,idComponent, Channel.ID, Channel.TypeID, Channel.DataFormat, Channel.Name,Channel.Info, Channel.Configuration, Channel.Parameters, new TOnProgressHandler(Channel) {
					@Override
					public void DoOnProgress(int ReadSize, TCanceller Canceller) {
						TDataStreamPanel.this.DoOnStatusMessage("");
					}
				}, new TOnIdleHandler(Channel) {
					@Override
					public void DoOnIdle(TCanceller Canceller) {
						TDataStreamPanel.this.DoOnStatusMessage(TDataStreamPanel.this.getString(R.string.SChannelIdle)+Channel.Name);
					}
				}, new TOnExceptionHandler(Channel) {
					@Override
					public void DoOnException(Exception E) {
						TDataStreamPanel.this.DoOnException(E);
					}
				});
				if (ChannelProcessor != null) {
					StreamChannelProcessors.add(ChannelProcessor);
					if (ChannelProcessor.IsVisual())
						ChannelProcessor.VisualSurface_Set(SH, Width,Height);
					//.
					ChannelProcessor.Start();
					//.
					if (I > 0)
						SB.append(", "+ChannelProcessor.Name);
					else
						SB.append(ChannelProcessor.Name);
				}			  
			}
		}
		//.
		String Title = getString(R.string.SChannels1)+SB.toString();
		lbTitle.setText(Title);
	}
	
	private void StreamChannelProcessors_Finalize() {
		for (int I = 0; I < StreamChannelProcessors.size(); I++) 
			StreamChannelProcessors.get(I).Destroy();
		StreamChannelProcessors.clear();
	}
	
	private static final int MESSAGE_SHOWSTATUSMESSAGE 	= 1;
	private static final int MESSAGE_SHOWEXCEPTION 		= 2;
	
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
    				Toast.makeText(TDataStreamPanel.this,EM,Toast.LENGTH_LONG).show();
    				// .
    				break; // . >

    			case MESSAGE_SHOWSTATUSMESSAGE:
    				String S = (String)msg.obj;
    				//.
    				if (S.length() > 0) {
    					lbStatus.setText(S);
    					lbStatus.setVisibility(View.VISIBLE);
    				}
    				else {
    					lbStatus.setText("");
    					lbStatus.setVisibility(View.GONE);
    				}
    				// .
    				break; // . >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	private void DoOnStatusMessage(String S) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWSTATUSMESSAGE,S).sendToTarget();
	}
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
