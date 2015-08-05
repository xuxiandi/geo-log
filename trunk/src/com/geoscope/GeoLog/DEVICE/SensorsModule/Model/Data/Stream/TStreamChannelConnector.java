package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream;

import android.content.Context;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.Memory.TMemoryInOutStreamAdapter;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TStreamChannelConnectorAbstract;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TStreamChannelConnector extends TStreamChannelConnectorAbstract {

    public static class TSourceChannelConnection extends TChannelConnectionAbstract {
		
	    public TSourceChannelConnection(TStreamChannelConnectorAbstract pConnector) {
	    	super(pConnector);
	    }
	    
	    @Override
	    public void run() {
	    	try {
	  	  		TStreamChannel SourceChannel = (TStreamChannel)(((TStreamChannelConnector)Connector).SensorsModule.Model.StreamChannels_GetOneByID(Connector.Channel.ID));
	  	  		//. streaming ...
	  	  		SourceChannel.DoStreaming(TUserAccessKey.GenerateValue(), ((TStreamChannelConnector)Connector).InOutStreamAdapter.OutStream, Canceller);
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
	
    public static class TChannelConnection extends TChannelConnectionAbstract {
		
	    public TChannelConnection(TStreamChannelConnectorAbstract pConnector) {
	    	super(pConnector);
	    }
	    
	    @Override
	    public void run() {
	    	try {
	    		com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TOnProgressHandler OnProgressHandler = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TOnProgressHandler() {
					
					@Override
					public void DoOnProgress(int ReadSize, TCanceller Canceller) {
						TChannelConnection.this.DoOnProgress(ReadSize, Canceller);
					}
				};
				com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TOnIdleHandler OnIdleHandler = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TOnIdleHandler() {
					
					@Override
					public void DoOnIdle(TCanceller Canceller) throws Exception {
						TChannelConnection.this.DoOnIdle(Canceller);
					}
				};
				//. processing ...
				Connector.Channel.DoStreaming(null, ((TStreamChannelConnector)Connector).InOutStreamAdapter.InStream,null, OnProgressHandler, 0, 0, OnIdleHandler, Canceller);
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
	
    
    private TSensorsModule SensorsModule;
    //.
    private TMemoryInOutStreamAdapter InOutStreamAdapter;
    //.
    private TSourceChannelConnection SourceChannelConnection = null; 
    
    public TStreamChannelConnector(Context pcontext, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel pChannel, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	super(pcontext, "",0, 0,"", null, pChannel, "", pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    	//.
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(context.getString(R.string.STrackerIsNotInitialized)); //. =>
    	if (Tracker.GeoLog.SensorsModule.Model == null)
    		throw new Exception("sensors module stream model is not defined"); //. => 
    	SensorsModule = Tracker.GeoLog.SensorsModule;
    	//.
    	InOutStreamAdapter = new TMemoryInOutStreamAdapter();
    }
    
    @Override
    public void Destroy(boolean flWaitForProcessingTermination)	throws Exception {
    	super.Destroy(flWaitForProcessingTermination);
    	//.
    	if (InOutStreamAdapter != null) {
    		InOutStreamAdapter.Destroy();
    		InOutStreamAdapter = null;
    	}
    }

    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
    	SourceChannelConnection = new TSourceChannelConnection(this);
    	//.
    	ChannelConnection = new TChannelConnection(this);
    }
    
    @Override
    public void Stop(boolean flWaitForProcessingTermination) throws Exception {
    	super.Stop(flWaitForProcessingTermination);
    	//.
    	if (SourceChannelConnection != null) {
    		SourceChannelConnection.Destroy(flWaitForProcessingTermination);
    		SourceChannelConnection = null;
    	}
    }
}
