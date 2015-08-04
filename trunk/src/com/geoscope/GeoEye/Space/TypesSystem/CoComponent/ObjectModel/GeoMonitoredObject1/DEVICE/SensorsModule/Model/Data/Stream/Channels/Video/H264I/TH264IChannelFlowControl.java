package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I;

import android.content.Context;

import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel.TProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TStreamChannelConnector;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TStreamChannelConnectorAbstract;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TStreamChannelConnectorAbstract.TOnExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TStreamChannelConnectorAbstract.TOnIdleHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TStreamChannelConnectorAbstract.TOnProgressHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel;

public class TH264IChannelFlowControl {

	private TH264IChannel H264IChannel;
	//.
	private TVCTRLChannel ControlChannel = null;
	private TStreamChannelConnectorAbstract ControlChannelConnector = null;
	
    public TH264IChannelFlowControl(TH264IChannel pH264IChannel, Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, TCoGeoMonitorObject pObject, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	H264IChannel = pH264IChannel;
    	//.
    	ControlChannel = new TVCTRLChannel();
    	ControlChannel.ID = 3; //. design defined channel ID of Device.ControlsModule.InternalControlsModule.TVCTRLChannel 
    	//.
    	ControlChannelConnector = new TStreamChannelConnector(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pObject, ControlChannel, Processor, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
	}
    
    public void Destroy() throws Exception {
    	Stop();
    }
    
    public void Start() throws Exception {
    	ControlChannel.Start();
		//.
		ControlChannelConnector.Start();
    }
    
    public void Stop() throws Exception {
    	if (ControlChannelConnector != null) {
    		ControlChannelConnector.Stop(false);
    		//.
    		ControlChannel.Close();
    		//.
    		ControlChannelConnector.Destroy(false);
    		//.
    		ControlChannelConnector = null;
    	}
    }
    
    private TProcessor Processor = new TProcessor() {

    	private static final int 	Ping_Interval = 5000; //. ms
    	private static final int 	Ping_NormalDuration = 1000; //. ms
    	private static final double Ping_DurationDeviation = 0.2; 
    	
    	@Override
    	public void Process(com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel Channel, TCanceller Canceller) throws Exception {
    		while (!Canceller.flCancel) {
    			Thread.sleep(Ping_Interval);
    			//. ping the source
    			long LastTime = System.currentTimeMillis();
    			ControlChannel.Ping();
    			long PingDuration = System.currentTimeMillis()-LastTime;
    			//.
    			double PingDurationFactor = ((PingDuration+0.0)/Ping_NormalDuration)-1.0;
    			if (PingDurationFactor > Ping_DurationDeviation) {
    				try {
    					ControlChannel.MultiplyChannelBitrate(H264IChannel.ID, 0.5);
    				}
    				catch (com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ValueOutOfRangeError VOORE) {
    				}
    				catch (Exception E) {
    				}
    			}
    			else
        			if (PingDurationFactor < -Ping_DurationDeviation) {
        				try {
        					ControlChannel.MultiplyChannelBitrate(H264IChannel.ID, 2.0);
        				}
        				catch (com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ValueOutOfRangeError VOORE) {
        				}
        				catch (Exception E) {
        				}
        			}
    		}
    	};
    };
}
