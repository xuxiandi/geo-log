package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;

import android.content.Context;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.widget.TextView;

import com.geoscope.Classes.Exception.TExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;

public abstract class TVideoRecorderServerView {

	protected Context context;
	
	protected String 	GeographProxyServerAddress = "";
	protected int 	GeographProxyServerPort = 0;
	protected int		UserID;
	protected String	UserPassword;
	protected TCoGeoMonitorObject 	Object;
	
	protected TextView lbVideoRecorderServer;

	public boolean 					flActive = false;
	//.
	public boolean 						flAudio = false;
	//.
	public boolean 						flVideo = false;
	protected Surface					VideoSurface = null;
	protected int						VideoSurfaceWidth = 0;
	protected int						VideoSurfaceHeight = 0;
	//.
	protected String UserAccessKey;
	//.
	private TExceptionHandler ExceptionHandler;
	
    public TVideoRecorderServerView(Context pcontext, String pGeographProxyServerAddress, int pGeographProxyServerPort, int pUserID, String pUserPassword, TCoGeoMonitorObject pObject, boolean pflAudio, boolean pflVideo, String pUserAccessKey, TExceptionHandler pExceptionHandler, TextView plbVideoRecorderServer) {
    	context = pcontext;
    	//.
    	GeographProxyServerAddress = pGeographProxyServerAddress;
    	GeographProxyServerPort = pGeographProxyServerPort;
    	UserID = pUserID;
    	UserPassword = pUserPassword;
    	Object = pObject;
    	flAudio = pflAudio;
    	flVideo = pflVideo;
    	//.
    	UserAccessKey = pUserAccessKey;  
    	//.
    	ExceptionHandler = pExceptionHandler;
        //.
        lbVideoRecorderServer = plbVideoRecorderServer;
    	//.
        UpdateInfo();
    }
	
    public void Destroy() {
    	try {
			Finalize();
		} catch (IOException E) {
			if (ExceptionHandler != null)
				ExceptionHandler.DoOnException(E);
		}
    }
    
	public void AudioClient_Initialize() throws IOException {
		AudioClient_Finalize();
	}
	
	public void AudioClient_Finalize() throws IOException {
	}
	
	public void VideoClient_Initialize() throws IOException {
		VideoClient_Finalize();
	}
	
	public void VideoClient_Finalize() throws IOException {
	}
	
	public void Initialize() throws Exception {
	}

	public void Finalize() throws IOException {
	}
	
	public void Reinitialize() throws Exception {
		Finalize();
		Initialize();
	}
	
	public void VideoSurface_Set(SurfaceHolder SH, int Width, int Height) {
		VideoSurface = SH.getSurface();
		VideoSurfaceWidth = Width;
		VideoSurfaceHeight = Height;
	}
	
	public void VideoSurface_Clear(SurfaceHolder SH) {
		if (VideoSurface == SH)
			VideoSurface = null;		
	}
	
	public void DoOnProcessingException(Throwable E) {
		if (!flActive)
			return; //. ->
		if (ExceptionHandler != null)
			ExceptionHandler.DoOnException(E);
	}
	
	public void UpdateInfo() {
	}	
}
