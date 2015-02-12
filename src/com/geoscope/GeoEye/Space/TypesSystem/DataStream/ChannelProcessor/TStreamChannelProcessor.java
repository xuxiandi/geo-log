package com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor;

import android.content.Context;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.TDataStreamServer;

public abstract class TStreamChannelProcessor extends TStreamChannelProcessorAbstract {

    public static class TDataStreamChannelReading extends TDataStreamChannelReadingAbstract {
		
	    public TDataStreamChannelReading(TStreamChannelProcessorAbstract pProcessor) {
	    	super(pProcessor);
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
	    	catch (Exception E) {
	    	    DoOnException(E);
	    	}
	    	catch (Throwable T) {
	    	    DoOnException(new Exception(T.getMessage()));
	    	}
	    }
	}
	
    public TStreamChannelProcessor(Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    }

    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
    	Reading = new TDataStreamChannelReading(this);
    }
}
