package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream;

import android.content.Context;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;

public abstract class TStreamChannelProcessorAbstract {

    public static abstract class TOnProgressHandler {
    	
    	protected TChannel Channel;
    	
    	public TOnProgressHandler(TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnProgress(int ReadSize, TCanceller Canceller);
    }
    
    public static abstract class TOnIdleHandler {
    	
    	protected TChannel Channel;
    	
    	public TOnIdleHandler(TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnIdle(TCanceller Canceller);
    }
    
    public static abstract class TOnExceptionHandler {
    	
    	protected TChannel Channel;
    	
    	public TOnExceptionHandler(TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnException(Exception E);
    }
    
    public static final int DefaultReadingTimeout = 1000*60; //. seconds
    
    public static class TChannelProcessingAbstract extends TCancelableThread {
		
	    protected TStreamChannelProcessorAbstract Processor;
	    protected int StreamReadSize;
		
	    public TChannelProcessingAbstract(TStreamChannelProcessorAbstract pProcessor) {
	    	Processor = pProcessor;
	    	//.
	    	_Thread = new Thread(this);
	    	_Thread.start();
	    }
	    
	    public void Destroy(boolean flWaitForTermination) throws InterruptedException {
	    	if (flWaitForTermination)
	    		CancelAndWait();
	    	else
	    		Cancel();
	    }
	    
	    @Override
	    public void run() {
	    }
	    
	    protected void DoOnStart() {
	    	Processor.DoOnStreamChannelStart();
	    }
	    
	    protected void DoOnFinish() {
	    	Processor.DoOnStreamChannelFinish();
	    	
	    }
	    
	    protected void DoOnRead(TStream Stream, int ReadSize, TCanceller Canceller) {
	    	  Processor.DoOnStreamChannelRead(Stream,ReadSize, Canceller);
	    	  //.
	    	  StreamReadSize = ReadSize;
	    	  DoOnProcessed(Canceller);
	    }
	    
	    protected void DoOnProcessed(TCanceller Canceller) {
	    	Processor.DoOnStreamChannelReadProcessed(StreamReadSize, Canceller);
	    }
	    
	    protected void DoOnIdle(TCanceller Canceller) {
	    	Processor.DoOnStreamChannelIdle(Canceller);
	    }
	    
	    protected void DoOnException(Exception E) {
	    	Processor.DoOnStreamChannelException(E);
	    }
	}
	
	protected Context context;
	//.
    protected String 	ServerAddress;
    protected int		ServerPort;
    //.
    protected int	 UserID;
    protected String UserPassword;
    //.
    public TCoGeoMonitorObject Object;
    //.
    public TStreamChannel Channel;
    //.
    public String 		TypeID;
    protected int 		DataFormat;
    public String 		Name;
    public String 		Info;
    protected String 	Configuration;
    protected String 	Parameters;
    //.
    protected int ReadingTimeout;
    //.
    protected TChannelProcessingAbstract Processing = null;
    //.
    protected TOnProgressHandler	OnProgressHandler;
    protected TOnIdleHandler 		OnIdleHandler;
    protected TOnExceptionHandler 	OnExceptionHandler;

    public TStreamChannelProcessorAbstract(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, TCoGeoMonitorObject pObject, TStreamChannel pChannel, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	context = pcontext;
    	//.
    	ServerAddress = pServerAddress;
    	ServerPort = pServerPort;
    	//.
    	UserID = pUserID;
    	UserPassword = pUserPassword;
    	//.
    	Object = pObject;
    	//.
    	Channel = pChannel;
    	//.
    	ReadingTimeout = DefaultReadingTimeout; 
    	//.
    	OnProgressHandler = pOnProgressHandler;
    	OnIdleHandler = pOnIdleHandler;
    	OnExceptionHandler = pOnExceptionHandler;
    	//.
    	ParseConfiguration();
    }
    
    public void Destroy(boolean flWaitForProcessingTermination) throws Exception {
    	Stop(flWaitForProcessingTermination);
    }
    
    public void ParseConfiguration() throws Exception {
    }

    protected void Open() throws Exception {
    }
    
    protected void Close() throws Exception {
    }
        
    public void Start() throws Exception {
    	Open();
    }
    
    public void Stop(boolean flWaitForProcessingTermination) throws Exception {
    	if (Processing != null) {
    		Processing.Destroy(flWaitForProcessingTermination);
    		Processing = null;
    	}
    	//.
    	Close();
    }

    public boolean IsVisual() {
    	return true;
    }
    
	public void VisualSurface_Set(SurfaceHolder SH, int Width, int Height) {
	}
	
	public void VisualSurface_Clear(SurfaceHolder SH) {
	}
	
    public void DoOnStreamChannelStart() {
    }
    
    public void DoOnStreamChannelFinish() {
    }
    
    public void DoOnStreamChannelRead(TStream Stream, int ReadSize, TCanceller Canceller) {
    }
    
    public void DoOnStreamChannelReadProcessed(int ReadSize, TCanceller Canceller) {
    	if (OnProgressHandler != null)
    		OnProgressHandler.DoOnProgress(ReadSize, Canceller);
    }
    
    public void DoOnStreamChannelIdle(TCanceller Canceller) {
    	if (OnIdleHandler != null)
    		OnIdleHandler.DoOnIdle(Canceller);
    }
    
    public void DoOnStreamChannelException(Exception E) {
    	if (OnExceptionHandler != null) 
    		OnExceptionHandler.DoOnException(E);
    }
}
