package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.LANConnectionRepeaterDefines;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStopHandler;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerViewer extends Activity {
    
	public static final int AudioServerPort = 10003;
	public static final int AudioPort = 10001;
	public static final int VideoServerPort = 10002;
	public static final int VideoPort = 10002;
	
	private static final int MESSAGE_SHOWEXCEPTION = 1;
	
	private TReflector Reflector;
	
	private String 	GeographProxyServerAddress = "";
	private int 	GeographProxyServerPort = 0;
	private int		UserID;
	private String	UserPassword;
	private int								ObjectIndex = -1;
	private TReflectorCoGeoMonitorObject 	Object;
	
	public final Handler MessageHandler = new Handler() {
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
	
	private TLANConnectionRepeater AudioLocalServer;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.video_recorder_server_viewer);
        //.
        Reflector = TReflector.GetReflector();
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	GeographProxyServerAddress = extras.getString("GeographProxyServerAddress");
        	GeographProxyServerPort = extras.getInt("GeographProxyServerPort");
        	UserID = extras.getInt("UserID");
        	UserPassword = extras.getString("UserPassword");
        	ObjectIndex = extras.getInt("ObjectIndex");
        	Object = Reflector.CoGeoMonitorObjects.Items[ObjectIndex]; 
        }
        //.
        try {
			Initialize();
		} catch (Exception E) {
			DoOnException(E);
		}
    }
	
    public void onDestroy() {
    	try {
			Finalize();
		} catch (IOException E) {
			DoOnException(E);
		}
    	//.
		super.onDestroy();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void onStart() {
    	super.onStart();
    }
	
	private void Initialize() throws Exception {
		TLANConnectionExceptionHandler ExceptionHandler = new TLANConnectionExceptionHandler() {
			@Override
			public void DoOnException(Throwable E) {
				TVideoRecorderServerViewer.this.DoOnException(E);
			}
		};		
		TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model();
		TLANConnectionStartHandler StartHandler = ObjectModel.TLANConnectionStartHandler_Create(Object);
		TLANConnectionStopHandler StopHandler = ObjectModel.TLANConnectionStopHandler_Create(Object); 
		//.
		AudioLocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",AudioServerPort, AudioPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Object.idGeographServerObject, ExceptionHandler, StartHandler,StopHandler);
		/////////////////////////////////
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					Socket socket = new Socket("127.0.0.1", AudioLocalServer.GetPort());
					try {
						InputStream IS = socket.getInputStream();
						byte[] D = new byte[4];
						while (true) { 
							IS.read(D);
						}
					}
					finally {
						socket.close();
					}
				} catch (Exception e) {}
			}
		});
		t.start();
	}

	private void Finalize() throws IOException {
		if (AudioLocalServer != null) {
			AudioLocalServer.Destroy();
			AudioLocalServer = null;
		}
	}
	
	private void DoOnException(Throwable E) {
		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
}
