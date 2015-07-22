package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream;

import android.content.Context;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.TDataStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.TStreamChannel;

public class TStreamChannelConnector extends TStreamChannelConnectorAbstract {

    public static class TDataStreamChannelReading extends TDataStreamChannelReadingAbstract {
		
	    public TDataStreamChannelReading(TStreamChannelConnectorAbstract pConnector) {
	    	super(pConnector);
	    }
	    
	    @Override
	    public void run() {
	    	try {
	    		TDataStreamServer DataStreamServer =  new TDataStreamServer(Connector.context, Connector.ServerAddress,Connector.ServerPort, Connector.UserID,Connector.UserPassword);
	    		try {
	    	    	DataStreamServer.ComponentStreamServer_GetDataStreamV1_Begin(Connector.idTComponent,Connector.idComponent);
		    		try {
		    			DoOnStart();
		    			try {
		    		    	DataStreamServer.ComponentStreamServer_GetDataStreamV1_Read(Connector.Channel.ID, Connector.ReadingTimeout, Canceller, new TDataStreamServer.TStreamReadHandler() {
		    		    		
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
	
    public TStreamChannelConnector(Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidTComponent, long pidComponent, TStreamChannel pChannel, TStreamChannel.TOnProgressHandler pOnProgressHandler, TStreamChannel.TOnIdleHandler pOnIdleHandler, TStreamChannel.TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannel, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    }

    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
    	Reading = new TDataStreamChannelReading(this);
    }
}
