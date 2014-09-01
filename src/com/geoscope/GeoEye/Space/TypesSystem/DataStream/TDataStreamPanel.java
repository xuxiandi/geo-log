package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.TStreamChannelProcessor;
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
	private byte[] StreamDescriptor;
	//.
	private SurfaceView svSurface;
	@SuppressWarnings("unused")
	private TextView lbTitle;
	//.
	private boolean IsInFront = false;
	//.
	private ArrayList<TStreamChannelProcessor> StreamChannelProcessors = new ArrayList<TStreamChannelProcessor>();
	
    public void onCreate(Bundle savedInstanceState) {
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
        }
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.video_recorder_server_viewer);
        //.
        svSurface = (SurfaceView)findViewById(R.id.svVideoRecorderServerViewer);
        svSurface.getHolder().addCallback(this);
        //.
        lbTitle = (TextView)findViewById(R.id.lbVideoRecorderServer);
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
		for (int I = 0; I < SD.Channels.size(); I++) {
			TDataStreamDescriptor.TChannel Channel = SD.Channels.get(I);
			TStreamChannelProcessor ChannelProcessor = TStreamChannelProcessor.GetProcessor(this, ServerAddress,ServerPort, UserID,UserPassword, SpaceDefines.idTDataStream,idComponent, Channel.ID, Channel.TypeID, Channel.DataFormat, Channel.Name,Channel.Info, Channel.Configuration, Channel.Parameters);
			if (ChannelProcessor != null) {
				StreamChannelProcessors.add(ChannelProcessor);
				if (ChannelProcessor.IsVisual())
					ChannelProcessor.VisualSurface_Set(SH, Width,Height);
				//.
				ChannelProcessor.Open();
				ChannelProcessor.Start();
			}			  
		}
	}
	
	private void StreamChannelProcessors_Finalize() {
		for (int I = 0; I < StreamChannelProcessors.size(); I++) 
			StreamChannelProcessors.get(I).Destroy();
		StreamChannelProcessors.clear();
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
    				Toast.makeText(TDataStreamPanel.this,EM,Toast.LENGTH_LONG).show();
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
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
