package com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor;

import android.content.Context;
import android.view.SurfaceHolder;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.TDataStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Audio.TAudioChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor.Video.TVideoChannelProcessor;

public class TStreamChannelProcessor {

    public static TStreamChannelProcessor GetProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters) throws Exception {
    	TStreamChannelProcessor Result;
    	//.
    	Result = TAudioChannelProcessor.GetProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters);
    	if (Result != null)
    		return Result; //. ->
    	//.
    	Result = TVideoChannelProcessor.GetProcessor(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters);
    	if (Result != null)
    		return Result; //. ->
    	//.
		return Result; //. ->
    }
    
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
	    
	    public void Destroy() {
	    	CancelAndWait();
	    }
	    
	    @Override
	    public void run() {
	    	try {
	    		int Timeout = 100;
	    		//.
	    		TDataStreamServer DataStreamServer =  new TDataStreamServer(Processor.context, Processor.ServerAddress,Processor.ServerPort, Processor.UserID,Processor.UserPassword);
	    		try {
	    	    	DataStreamServer.ComponentStreamServer_GetDataStreamV1_Begin(Processor.idTComponent,Processor.idComponent);
		    		try {
		    			DoOnStart();
		    			try {
		    		    	DataStreamServer.ComponentStreamServer_GetDataStreamV1_Read(Processor.ChannelID, Timeout, Canceller, new TDataStreamServer.TStreamReadHandler() {
		    		    		@Override
		    		    		public void DoOnRead(byte[] Buffer,	int BufferSize, TCanceller Canceller) {
		    		    			TDataStreamChannelReading.this.DoOnRead(Buffer,BufferSize, Canceller);
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
	    
	    private void DoOnRead(byte[] Buffer, int BufferSize, TCanceller Canceller) {
	    	  Processor.DoOnStreamChannelRead(Buffer,BufferSize, Canceller);
	    	  //.
	    	  StreamReadSize = BufferSize;
	    	  DoOnProcessed();
	    }
	    
	    private void DoOnProcessed() {
	    	Processor.DoOnStreamChannelReadProcessed(StreamReadSize);
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
    protected int 	idTComponent;
    protected long 	idComponent;
    //.
    protected int ChannelID;
    //.
    protected String 	TypeID;
    protected int 		DataFormat;
    protected String 	Name;
    protected String 	Info;
    protected String 	Configuration;
    protected String 	Parameters;
    //.
    private TDataStreamChannelReading Reading = null; 

    public TStreamChannelProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters) throws Exception {
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
    	ParseConfiguration();
    }
    
    public void Destroy() {
    	Stop();
    }
    
    public void ParseConfiguration() throws Exception {
    }

    public void Open() throws Exception {
    }
        
    public void Close() {
    }
        
    public void Start() {
    	Reading = new TDataStreamChannelReading(this);
    }
    
    public void Stop() {
    	if (Reading != null) {
    		Reading.Destroy();
    		Reading = null;
    	}
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
    
    public void DoOnStreamChannelRead(byte[] Buffer, int BufferSize, TCanceller Canceller) {
    }
    
    public void DoOnStreamChannelReadProcessed(long ReadSize) {
    }
    
    public void DoOnStreamChannelIdle(TCanceller Canceller) {
    }
    
    public void DoOnStreamChannelException(Exception E) {
    }
}
