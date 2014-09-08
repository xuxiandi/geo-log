package com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor;

import android.content.Context;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.TDataStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Audio.TAudioChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Video.TVideoChannelProcessor;

public abstract class TStreamChannelProcessor {

    public static TStreamChannelProcessor GetProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	TStreamChannelProcessor Result;
    	//.
    	Result = TAudioChannelProcessor.GetProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    	if (Result != null)
    		return Result; //. ->
    	//.
    	Result = TVideoChannelProcessor.GetProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
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
    
    public static final int DefaultReadingTimeout = 100; //. ms
    
    public static class TDataStreamChannelReading extends TCancelableThread {
		
	    private TStreamChannelProcessor Processor;
	    private int StreamReadSize;
	    private Exception ThreadException;
		
	    public TDataStreamChannelReading(TStreamChannelProcessor pProcessor) {
	    	Processor = pProcessor;
	    	//.
	    	_Thread = new Thread(this);
	    	_Thread.start();
	    }
	    
	    public void Destroy() throws InterruptedException {
	    	CancelAndWait();
	    }
	    
	    @Override
	    public void run() {
	    	try {
	    		TDataStreamServer DataStreamServer =  new TDataStreamServer(Processor.context, Processor.ServerAddress,Processor.ServerPort, Processor.UserID,Processor.UserPassword);
	    		try {
	    	    	DataStreamServer.ComponentStreamServer_GetDataStreamV1_Begin(Processor.idTComponent,Processor.idComponent);
		    		try {
		    			DoOnStart();
		    			try {
		    		    	DataStreamServer.ComponentStreamServer_GetDataStreamV1_Read(Processor.ChannelID, Processor.ReadingTimeout, Canceller, new TDataStreamServer.TStreamReadHandler() {
		    		    		@Override
		    		    		public void DoOnRead(TStream Stream, int ReadSize, TCanceller Canceller) {
		    		    			TDataStreamChannelReading.this.DoOnRead(Stream,ReadSize, Canceller);
		    		    		}
		    		    	}, new TDataStreamServer.TStreamReadIdleHandler() {
		    		    		@Override
		    		    		public void DoOnIdle(TCanceller Canceller) {
		    		    			TDataStreamChannelReading.this.DoOnIdle(Canceller);
		    		    		}
		    		    	});
		    			}
		    			finally {
		    				DoOnFinish();
		    			}
		    		}
		    		finally {
				    	DataStreamServer.ComponentStreamServer_GetDataStreamV1_End();
		    		}
	    		}
	    		finally {
	    			DataStreamServer.Destroy();
	    		}
	    	}
	    	catch (InterruptedException IE) {
	    	}
	    	catch (CancelException CE) {
	    	}
	    	catch (Throwable T) {
	    	    ThreadException = new Exception(T.getMessage());
	    	    DoOnException();
	    	}
	    }
	    
	    private void DoOnStart() {
	    	Processor.DoOnStreamChannelStart();
	    }
	    
	    private void DoOnFinish() {
	    	Processor.DoOnStreamChannelFinish();
	    	
	    }
	    
	    private void DoOnRead(TStream Stream, int ReadSize, TCanceller Canceller) {
	    	  Processor.DoOnStreamChannelRead(Stream,ReadSize, Canceller);
	    	  //.
	    	  StreamReadSize = ReadSize;
	    	  DoOnProcessed(Canceller);
	    }
	    
	    private void DoOnProcessed(TCanceller Canceller) {
	    	Processor.DoOnStreamChannelReadProcessed(StreamReadSize, Canceller);
	    }
	    
	    private void DoOnIdle(TCanceller Canceller) {
	    	Processor.DoOnStreamChannelIdle(Canceller);
	    }
	    
	    private void DoOnException() {
	    	Processor.DoOnStreamChannelException(ThreadException);
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
    private TDataStreamChannelReading Reading = null;
    //.
    private TOnProgressHandler	OnProgressHandler;
    private TOnIdleHandler 		OnIdleHandler;
    private TOnExceptionHandler OnExceptionHandler;

    public TStreamChannelProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
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
    	//.
    	Reading = new TDataStreamChannelReading(this);
    }
    
    public void Stop() throws Exception {
    	if (Reading != null) {
    		Reading.Destroy();
    		Reading = null;
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
