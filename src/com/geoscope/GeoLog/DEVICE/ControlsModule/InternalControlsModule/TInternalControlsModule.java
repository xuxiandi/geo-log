package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule;

import java.io.File;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Audio.VC.TVCChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("HandlerLeak")
public class TInternalControlsModule extends TModule {

	public static String Folder() {
		return TControlsModule.Folder()+"/"+"InternalControlsModule";
	}
		
	public static final int MESSAGE_START 	= 1;
	public static final int MESSAGE_STOP 	= 2;

	
	public TControlsModule ControlsModule;
	//.
	public boolean flStartOnDeviceStart = false;
	//.
	public boolean flInitialized = false;
	//.
	public TModel Model = null; 
	//.
	public TVCChannel 		VCChannel = null;
	public TVCTRLChannel 	VCTRLChannel = null;
	
    public TInternalControlsModule(TControlsModule pControlsModule) throws Exception {
    	super(pControlsModule);
    	//.
    	ControlsModule = pControlsModule;
    	//.
        Device = pControlsModule.Device;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
        //.
        Model_Build();
    }
    
    public void Destroy() {
    	Finalize();
    }

	private volatile int StartCount = 0;
    
    @Override
    public void Start() throws Exception {
    	if (StartCount == 0) {
            if (IsEnabled() && ((Device.ModuleState == MODULE_STATE_RUNNING) || flStartOnDeviceStart)) {
                super.Start();
            	Initialize();
            	//.
            	StartCount++;
            }
    	}
    	else
        	StartCount++;
    }
    
    @Override
    public void Stop() throws Exception {
    	if (StartCount > 0) {
    		if (StartCount == 1) {
    	    	Finalize();
    	    	//.
    	    	super.Stop();
    		}
    		StartCount--;
    	}
    }
    
    public boolean IsStarted() {
    	return (StartCount > 0);
    }
    
    public void PostStart() {
		MessageHandler.obtainMessage(MESSAGE_START).sendToTarget();
    }
    
    public void PostStop() {
		MessageHandler.obtainMessage(MESSAGE_STOP).sendToTarget();
    }
    
    private void Initialize() {
        flInitialized = true;
    }
    
    private void Finalize() {
    	flInitialized = false;
    }
    
    private void Model_Build() throws Exception {
    	Model = new TModel();
    	//. set model stream folder
    	Model.Stream.Folder = TControlsModule.Folder();
    	//.
    	VCChannel = new TVCChannel(this, 2/*ID*/);
    	Model.Stream.Channels.add(VCChannel);
    	//.
    	VCTRLChannel = new TVCTRLChannel(this, 3/*ID*/);
    	Model.Stream.Channels.add(VCTRLChannel);
    }
    
	private Handler MessageHandler = new Handler() {
		
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_START: 
                	try {
            			Start(); 
                	}
                	catch (Exception E) {
                		Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_STOP: 
                	try {
            			Stop(); 
                	}
                	catch (Exception E) {
                		Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
