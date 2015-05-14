package com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor;

import android.content.Context;
import android.view.SurfaceHolder;

import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Audio.TAudioChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Audio.TAudioChannelProcessorUDP;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Video.TVideoChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Video.TVideoChannelProcessorUDP;

public abstract class TStreamChannelProcessorAbstract {

    public static TStreamChannelProcessorAbstract GetProcessor(Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	TStreamChannelProcessorAbstract Result;
    	//.
    	Result = TAudioChannelProcessor.GetProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    	if (Result != null)
    		return Result; //. ->
    	//.
    	Result = TAudioChannelProcessorUDP.GetProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    	if (Result != null)
    		return Result; //. ->
    	//.
    	Result = TVideoChannelProcessor.GetProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    	if (Result != null)
    		return Result; //. ->
    	//.
    	Result = TVideoChannelProcessorUDP.GetProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    	if (Result != null)
    		return Result; //. ->
    	//.
		return Result; //. ->
    }
    
    public static abstract class TOnProgressHandler {
    	
    	protected TDataStreamDescriptor.TChannel Channel;
    	
    	public TOnProgressHandler(TDataStreamDescriptor.TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnProgress(int ReadSize, TCanceller Canceller);
    }
    
    public static abstract class TOnIdleHandler {
    	
    	protected TDataStreamDescriptor.TChannel Channel;
    	
    	public TOnIdleHandler(TDataStreamDescriptor.TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnIdle(TCanceller Canceller);
    }
    
    public static abstract class TOnExceptionHandler {
    	
    	protected TDataStreamDescriptor.TChannel Channel;
    	
    	public TOnExceptionHandler(TDataStreamDescriptor.TChannel pChannel) {
    		Channel = pChannel;
    	}
    	
    	public abstract void DoOnException(Exception E);
    }
    
    public static final int DefaultReadingTimeout = 1000; //. ms
    
    public static class TDataStreamChannelReadingAbstract extends TCancelableThread {
		
	    protected TStreamChannelProcessorAbstract Processor;
	    protected int StreamReadSize;
		
	    public TDataStreamChannelReadingAbstract(TStreamChannelProcessorAbstract pProcessor) {
    		super();
    		//.
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
    protected long	 UserID;
    protected String UserPassword;
    //.
    public int 	idTComponent;
    public long	idComponent;
    //.
    public int ChannelID;
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
    protected TDataStreamChannelReadingAbstract Reading = null;
    //.
    protected TOnProgressHandler	OnProgressHandler;
    protected TOnIdleHandler 		OnIdleHandler;
    protected TOnExceptionHandler 	OnExceptionHandler;

    public TStreamChannelProcessorAbstract(Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	context = pcontext;
    	//.
    	ServerAddress = pServerAddress;
    	ServerPort = pServerPort;
    	//.
    	UserID = pUserID;
    	UserPassword = pUserPassword;
    	//.
    	idTComponent = pidTComponent;
    	idComponent = pidComponent;
    	//.
    	ChannelID = pChannelID;
    	//.
    	TypeID = pTypeID;
    	DataFormat = pDataFormat; 
    	Name = pName;
    	Info = pInfo;
    	Configuration = pConfiguration;
    	//.
    	Parameters = pParameters;
    	//.
    	ReadingTimeout = DefaultReadingTimeout; 
    	//.
    	OnProgressHandler = pOnProgressHandler;
    	OnIdleHandler = pOnIdleHandler;
    	OnExceptionHandler = pOnExceptionHandler;
    	//.
    	ParseConfiguration();
    }
    
    public void Destroy() throws Exception {
    	Stop();
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
    
    public void Stop() throws Exception {
    	if (Reading != null) {
    		Reading.Destroy(false);
    		Reading = null;
    	}
    	//.
    	Close();
    }

    public boolean IsAudial() {
    	return false;
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
