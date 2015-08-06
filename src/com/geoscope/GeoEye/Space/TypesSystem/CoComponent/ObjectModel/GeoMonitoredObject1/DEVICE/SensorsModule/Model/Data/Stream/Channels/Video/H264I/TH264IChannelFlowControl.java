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

    	private static final int 	Check_Interval = 5000; //. ms
    	private static final double Check_ReduceBitrateFillFactor = 0.50;
    	private static final double Check_ZeroFillFactor = 0.05;
    	private static final double Check_IncreaseBitrateZeroFillFactorCount = 6;
    	private static final int 	Check_PauseAfterBitrateChange = 5000; //. ms
    	
    	@Override
    	public void Process(com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel Channel, TCanceller Canceller) throws Exception {
    		double LastFillFactor = -1.0;
    		int ZeroFillFactorCount = 0;
    		while (!Canceller.flCancel) {
    			Thread.sleep(Check_Interval);
    			//. 
    			try {
    				TVCTRLChannel.TPacketsBufferInfo PacketsBufferInfo = ControlChannel.GetChannelSubscriberPacketsBufferInfo(H264IChannel.ID, H264IChannel.UserAccessKey);
    				if (PacketsBufferInfo.BuffersCount > 0) {
    					double FillFactor = (PacketsBufferInfo.PendingPackets+0.0)/PacketsBufferInfo.BuffersCount;
    					double FillFactorTrend = ((LastFillFactor >= 0) ? (FillFactor-LastFillFactor) : 0.0);
    					LastFillFactor = FillFactor;
    					//.
    					if ((FillFactor > Check_ReduceBitrateFillFactor) && (FillFactorTrend >= 0.0)) {
    						try {
            					ControlChannel.MultiplyChannelBitrate(H264IChannel.ID, 0.5);
            				}
            				catch (com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ValueOutOfRangeError VOORE) {
            				}
            				catch (Exception E) {
            				}    					
        					//.
        					Thread.sleep(Check_PauseAfterBitrateChange);
            			}
    					else 
        					if (FillFactor < Check_ZeroFillFactor) {
        						ZeroFillFactorCount++;
            					//.
            					if (ZeroFillFactorCount >= Check_IncreaseBitrateZeroFillFactorCount) {
            						ZeroFillFactorCount = 0;
            						//.
            						try {
                    					ControlChannel.MultiplyChannelBitrate(H264IChannel.ID, 2.0);
                    				}
                    				catch (com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ValueOutOfRangeError VOORE) {
                    				}
                    				catch (Exception E) {
                    				}    					
                					//.
                					Thread.sleep(Check_PauseAfterBitrateChange);
                    			}
        					}
        					else
        						ZeroFillFactorCount = 0;
    				}
    			}
    			catch (Exception E) {
    			}
    		}
    	};
    };
}
