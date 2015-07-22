package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream;

import android.content.Context;
import android.view.SurfaceHolder;

import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.TStreamChannel;

public abstract class TStreamChannelConnectorAbstract {

    public static final int DefaultReadingTimeout = 1000; //. ms
    
    public static class TDataStreamChannelReadingAbstract extends TCancelableThread {
		
	    protected TStreamChannelConnectorAbstract Connector;
	    protected int StreamReadSize;
		
	    public TDataStreamChannelReadingAbstract(TStreamChannelConnectorAbstract pConnector) {
    		super();
    		//.
	    	Connector = pConnector;
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
	    	Connector.DoOnStreamChannelStart();
	    }
	    
	    protected void DoOnFinish() {
	    	Connector.DoOnStreamChannelFinish();
	    	
	    }
	    
	    protected void DoOnRead(TStream Stream, int ReadSize, TCanceller Canceller) {
	    	  Connector.DoOnStreamChannelRead(Stream,ReadSize, Canceller);
	    	  //.
	    	  StreamReadSize = ReadSize;
	    	  DoOnProcessed(Canceller);
	    }
	    
	    protected void DoOnProcessed(TCanceller Canceller) {
	    	Connector.DoOnStreamChannelReadProcessed(StreamReadSize, Canceller);
	    }
	    
	    protected void DoOnIdle(TCanceller Canceller) {
	    	Connector.DoOnStreamChannelIdle(Canceller);
	    }
	    
	    protected void DoOnException(Exception E) {
	    	Connector.DoOnStreamChannelException(E);
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
    protected TDataStreamChannelReadingAbstract Reading = null;
    //.
    protected TStreamChannel.TOnProgressHandler		OnProgressHandler;
    protected TStreamChannel.TOnIdleHandler 		OnIdleHandler;
    protected TStreamChannel.TOnExceptionHandler 	OnExceptionHandler;

    public TStreamChannelConnectorAbstract(Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidTComponent, long pidComponent, TStreamChannel pChannel, TStreamChannel.TOnProgressHandler pOnProgressHandler, TStreamChannel.TOnIdleHandler pOnIdleHandler, TStreamChannel.TOnExceptionHandler pOnExceptionHandler) throws Exception {
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

	public void VisualSurface_Set(SurfaceHolder SH, int Width, int Height) {
	}
	
	public void VisualSurface_Clear(SurfaceHolder SH) {
	}
	
    public void DoOnStreamChannelStart() {
    }
    
    public void DoOnStreamChannelFinish() {
    }
    
    public void DoOnStreamChannelRead(TStream Stream, int ReadSize, TCanceller Canceller) {
    	Channel.DoOnRead(Stream, ReadSize, OnProgressHandler, OnIdleHandler, OnExceptionHandler, Canceller);
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
