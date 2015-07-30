package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.SurfaceHolder;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.Camera;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.Camera.TCameraMeasurementInfo;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraRegistrator;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraStreamerFRAME;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraStreamerFRAME0;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraStreamerH263;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.CameraStreamerH264;

@SuppressLint("HandlerLeak")
public class TVideoRecorder {

	public abstract static interface IVideoRecorderPanel {
		
		public boolean 	RestartRecording(TReceiverDescriptor RD, short pMode, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, boolean pflPreview);
		public void 	StopRecording();
		public boolean 	IsRecording();
		//.
		public short GetMode();
		//.
	    public boolean IsAudio();
		public boolean IsVideo();
		public boolean IsTransmitting();
		public boolean IsSaving();
		//.
		public void StartTransmitting(long pidGeographServerObject);
		public void FinishTransmitting();
		//.
		public TMeasurementDescriptor Recording_GetMeasurementDescriptor() throws Exception;
	}

	private static TVideoRecorder _VideoRecorder = null;
	
	private static synchronized void SetVideoRecorder(TVideoRecorder VideoRecorder) {
		_VideoRecorder = VideoRecorder;
	}
	
	private static synchronized void ClearVideoRecorder(TVideoRecorder VideoRecorder) {
		if (_VideoRecorder == VideoRecorder)
			_VideoRecorder = null;
	}
	
	public static synchronized boolean IsVideoRecorderNull() {
		return (_VideoRecorder == null);
	}

	public static synchronized TVideoRecorder GetVideoRecorder() {
		return _VideoRecorder;
	}
	
	public static synchronized boolean VideoRecorder_IsSaving() {
		if (_VideoRecorder == null)
			return false; //. ->
		return _VideoRecorder.flSaving; 
	}	
		
	public static synchronized TCameraMeasurementInfo VideoRecorder_GetMeasurementInfo() throws Exception {
		if (_VideoRecorder == null)
			return null; //. ->
		return _VideoRecorder.Recording_GetMeasurementInfo();
	}

	public static synchronized TMeasurementDescriptor VideoRecorder_GetMeasurementDescriptor() throws Exception {
		if (_VideoRecorder == null)
			return null; //. ->
		return _VideoRecorder.Recording_GetMeasurementDescriptor(); 
	}	
		
	public static synchronized void VideoRecorder_FlashMeasurement() throws Exception {
		if (_VideoRecorder == null)
			return; //. ->
		_VideoRecorder.Recording_UpdateMeasurementDescriptor(); 
	}	
	
	
	private boolean flExists = false;
	//.
	private Context context;
	//.
	private TVideoRecorderModule VideoRecorderModule;
	//.
	public Camera     		camera;
    private boolean 		camera_flStarted;
	private SurfaceHolder 	camera_Surface = null;
	private TextView 		camera_lbStatus;
    //.
	private String Address = "";
	private int AudioPort = 0;
	private int VideoPort = 0;
	//.
    public short 				Mode;
    public boolean 				flAudio = false;
    public boolean 				flVideo = false;
    public boolean 				flTransmitting = false;
    public boolean 				flSaving = false;
    //.
    public boolean 				flPreview = true;
    //.
    private boolean 			Status_flVisible = false;
    //.
    private Object Lock = new Object();
    
	private PowerManager.WakeLock wl;
    
    @SuppressWarnings("deprecation")
	public TVideoRecorder(Context pcontext, TVideoRecorderModule pVideoRecorderModule, TextView pcamera_lbStatus) {
    	context = pcontext;
    	VideoRecorderModule = pVideoRecorderModule;
        camera_lbStatus = pcamera_lbStatus;
		//.
		camera_flStarted = false;
		camera = null;
    	//.
		PowerManager pm = (PowerManager)context.getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "DVRWakeLock");
		//.
		SetVideoRecorder(this);
		//.
		flExists = true;
		//. indicate that recording is active
		if (!VideoRecorderModule.Device.SensorsModule.Active) 
			VideoRecorderModule.Device.SensorsModule.MessageHandler.obtainMessage(TSensorsModule.MESSAGE_ACTIVE).sendToTarget();
    }
    
	public TVideoRecorder(Context pcontext, TVideoRecorderModule pVideoRecorderModule) {
		this(pcontext, pVideoRecorderModule, null);
	}
	
    public void Destroy() {
    	if (flExists) { //. indicate that recording is inactive
    		if (!VideoRecorderModule.Device.SensorsModule.Active) 
    			VideoRecorderModule.Device.SensorsModule.MessageHandler.obtainMessage(TSensorsModule.MESSAGE_INACTIVE).sendToTarget();
    	}
    	//.
    	flExists = false;
    	//.
    	ClearVideoRecorder(this);
    	//.
		FinalizeRecorder();
    }
	
	private void DoStartRecording() {
		synchronized (Lock) {
			if (camera_flStarted)
				return; //. ->
			//.
			try {
				try {
					switch (Mode) {
					
					case TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1:
						camera = new CameraStreamerH263(VideoRecorderModule);
						break; //. >
						
					case TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1:
						camera = new CameraStreamerH264(VideoRecorderModule);
						break; //. >
						
					case TVideoRecorderModule.MODE_MPEG4:
					case TVideoRecorderModule.MODE_3GP:
						camera = new CameraRegistrator(VideoRecorderModule);
						break; //. >
						
					case TVideoRecorderModule.MODE_FRAMESTREAM:
						if (VideoRecorderModule.MediaFrameServer.H264EncoderServer_IsAvailable() && (VideoRecorderModule.CameraConfiguration.Camera_Video_Source == 0))
							camera = new CameraStreamerFRAME(VideoRecorderModule);
						else
							camera = new CameraStreamerFRAME0(VideoRecorderModule);
						break; //. >
						
					default:
						throw new Exception("Unknown camera mode, Mode: "+Short.toString(Mode)); //. =>
					}
					//.
					camera.Setup(camera_Surface, Address, AudioPort,VideoPort, Mode, VideoRecorderModule.CameraConfiguration.Camera_Audio_Source,VideoRecorderModule.CameraConfiguration.Camera_Audio_SampleRate,VideoRecorderModule.CameraConfiguration.Camera_Audio_BitRate, VideoRecorderModule.CameraConfiguration.Camera_Video_Source,VideoRecorderModule.CameraConfiguration.Camera_Video_ResX,VideoRecorderModule.CameraConfiguration.Camera_Video_ResY,VideoRecorderModule.CameraConfiguration.Camera_Video_FrameRate,VideoRecorderModule.CameraConfiguration.Camera_Video_BitRate, VideoRecorderModule.Device.UserID,VideoRecorderModule.Device.UserPassword, VideoRecorderModule.Device.idGeographServerObject, flTransmitting, flSaving, flAudio,flVideo, flPreview, VideoRecorderModule.MeasurementConfiguration.MaxDuration);
				}
				catch (Camera.AudioSetupError AE) {
					VideoRecorderModule.SetAudio(false);
					VideoRecorderModule.PostUpdateRecorderState();
				}
				catch (Camera.VideoSetupError VE) {
					VideoRecorderModule.SetVideo(false);
					VideoRecorderModule.PostUpdateRecorderState();
				}
				//.
				camera.Start();
				//.
				camera_flStarted = true;
				//.
				Status_Update();
			} 
			catch (Exception E) {
				VideoRecorderModule.Device.Log.WriteError("VideoRecorder",E.getMessage());
				//.
	        	Toast.makeText(context, context.getString(R.string.SVideoRecorderInitializationError)+E.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public boolean RestartRecording(TReceiverDescriptor RD, short pMode, boolean pflTransmitting, boolean pflSaving, boolean pflAudio, boolean pflVideo, boolean pflPreview) {
		boolean Result = false;
		synchronized (Lock) {
			if (camera_flStarted)
				StopRecording();
			//.
			try {
				Address = "";
				AudioPort = 0;
				VideoPort = 0;
				if (RD != null) {
					if (RD.ReceiverType != TReceiverDescriptor.RECEIVER_NATIVE)
						throw new Exception("unknown receiver."); //. =>
					Address = RD.Address;
					AudioPort = RD.AudioPort;
					VideoPort = RD.VideoPort;
				}
				//.
				Mode = pMode;
				flAudio = pflAudio;
				flVideo = pflVideo;
				flTransmitting = (pflTransmitting && (VideoRecorderModule.Device.idGeographServerObject != 0));
				flSaving = pflSaving;
				//.
				flPreview = pflPreview;
				//.
				MessageHandler.obtainMessage(MESSAGE_STARTRECORDING).sendToTarget();
				//.
				Result = true;
			} 
			catch (Throwable T) {
				VideoRecorderModule.Device.Log.WriteError("VideoRecorder",T.getMessage());
				//.
	        	Toast.makeText(context, context.getString(R.string.SVideoRecorderInitializationError)+T.getMessage(), Toast.LENGTH_LONG).show();
	        	//.
	        	return Result; //. ->
			}
			//.
			Status_Update();
		}
		//.
		return Result;
	}
	
	public void StopRecording() {
		synchronized (Lock) {
			if (camera != null) 
				try {
					camera.Stop();
					//.
					camera.Finalize();
					//.
		    		camera.Destroy();
		    		camera = null;
				}
				catch (Exception E) {
		        	Toast.makeText(context, context.getString(R.string.SVideoRegistratorFinalizationError)+E.getMessage(), Toast.LENGTH_LONG).show();
				}
			//.
			camera_flStarted = false;
			//.
			Status_Update();
		}
	}
	
	public boolean IsRecording() {
		synchronized (Lock) {
			return camera_flStarted;
		}
	}
	
	public void StartTransmitting(long pidGeographServerObject) {
		synchronized (Lock) {
			if (camera != null)
				camera.StartTransmitting(pidGeographServerObject);
			//.
			flTransmitting = true;
		}
	}
	
	public void FinishTransmitting() {
		synchronized (Lock) {
			if (camera != null)
				camera.FinishTransmitting();
			//.
			flTransmitting = false;
		}
	}
	
	public void InitializeRecorder() {
		if (RecorderIsInitialized())
			return; //. ->
		if (VideoRecorderModule.Recording.BooleanValue()) {
			TReceiverDescriptor RD = VideoRecorderModule.GetReceiverDescriptor();
			if (RD != null) 
    			RestartRecording(RD, VideoRecorderModule.Mode.GetValue(), VideoRecorderModule.Transmitting.BooleanValue(),VideoRecorderModule.Saving.BooleanValue(), VideoRecorderModule.Audio.BooleanValue(),VideoRecorderModule.Video.BooleanValue(), VideoRecorderModule.Recording.flPreview);
		}
		else {
			if (IsRecording()) 
				StopRecording();
		}
		//.
		wl.acquire();
	}
	
	public void FinalizeRecorder() {
		if (wl.isHeld())
			wl.release();
		//.
		if (IsRecording())
			StopRecording();
	}
	
	public boolean RecorderIsInitialized() {
		return IsRecording();
	}
	
	public void camera_Surface_Set(SurfaceHolder SH) {
		camera_Surface = SH;
	}
	
	public SurfaceHolder camera_Surface_Get() {
		return camera_Surface;
	}
	
	public void camera_Surface_Clear(SurfaceHolder SH) {
		if (camera_Surface == SH)
			camera_Surface = null;
	}
	
	public void Status_SetVisibility(boolean Value) {
		if (Status_flVisible == Value)
			return; //. ->
		Status_flVisible = Value;
		//.
		Status_Update();
	}
	
	private void Status_Update() {
		if (camera_lbStatus == null)
			return; //. ->
		//.
		if (!Status_flVisible) {
			camera_lbStatus.setText("");			
			return; //. ->
		}
		//.
		if (camera_flStarted) {
			String S = context.getString(R.string.SCameraIsOn);
			//.
			S = S+context.getString(R.string.SCameraMode);
			switch (Mode) {
			
			case TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1:
				S = S+context.getString(R.string.SCameraStreamH263);
				break; //. >
				
			case TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1:
				S = S+context.getString(R.string.SCameraStreamH264);
				break; //. >
				
			case TVideoRecorderModule.MODE_MPEG4:
				S = S+context.getString(R.string.SCameraMPEG4);
				break; //. >
				
			case TVideoRecorderModule.MODE_3GP:
				S = S+context.getString(R.string.SCamera3GP);
				break; //. >
				
			case TVideoRecorderModule.MODE_FRAMESTREAM:
				S = S+context.getString(R.string.SCameraFRAME);
				break; //. >
				
			default:
				S = S+"?";
				break; //. >
			}
			if (flAudio) 
				S = S+context.getString(R.string.SCameraAudioChannel);
			if (flVideo) 
				S = S+context.getString(R.string.SCameraVideoChannel);
			if (flTransmitting) 
				S = S+context.getString(R.string.SCameraTransmitting);
			if (flSaving) 
				S = S+context.getString(R.string.SCameraSaving);
			camera_lbStatus.setText(S);
		}
		else {
			camera_lbStatus.setText(R.string.SCameraOff);
		}
	}
	
	private static final int MESSAGE_STARTRECORDING		= 1;
	
    private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_STARTRECORDING:
                	DoStartRecording();
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };

	public Camera.TCameraMeasurementInfo Recording_GetMeasurementInfo() throws Exception {
		synchronized (Lock) {
			if (camera != null)
				return camera.GetMeasurementInfo(); //. ->
			else
				return null; //. ->
		}
	}
	
	public TMeasurementDescriptor Recording_GetMeasurementDescriptor() throws Exception {
		synchronized (Lock) {
			if (camera != null)
				return camera.GetMeasurementCurrentDescriptor(); //. ->
			else
				return null; //. ->
		}
	}

	public void Recording_UpdateMeasurementDescriptor() throws Exception {
		synchronized (Lock) {
			if (camera != null)
				camera.UpdateMeasurementCurrentDescriptor();
		}
	}
}
