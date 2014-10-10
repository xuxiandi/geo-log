package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.Context;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.LANConnectionRepeaterDefines;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionStopHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;

public class TStreamChannelProcessor extends TStreamChannelProcessorAbstract {

	public static final int DeviceSensorsModuleStreamingServerPort = 10009;
	
    public static class TChannelProcessing extends TChannelProcessingAbstract {
		
    	public static final int LocalPort = 10008;
    	
		public static final int ConnectioningTimeout = 1000*60; //. seconds
		public static final int ProcessingTimeout = 1000*30; //. seconds
		
	    public TChannelProcessing(TStreamChannelProcessorAbstract pProcessor) {
	    	super(pProcessor);
	    }
	    
	    @Override
	    public void run() {
	    	try {
	    		TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model();
	    		//.
	    		TLANConnectionExceptionHandler ExceptionHandler = new TLANConnectionExceptionHandler() {
	    			@Override
	    			public void DoOnException(Throwable E) {
	    				Canceller.Cancel();
	    				//.
	    				TChannelProcessing.this.DoOnException(new Exception(E.getMessage()));	    			
	    			}
	    		};		
	    		TLANConnectionStartHandler StartHandler = ObjectModel.TLANConnectionStartHandler_Create(Processor.Object);
	    		TLANConnectionStopHandler StopHandler = ObjectModel.TLANConnectionStopHandler_Create(Processor.Object);
	    		//.
	    		TLANConnectionRepeater LocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",DeviceSensorsModuleStreamingServerPort, LocalPort, Processor.ServerAddress,Processor.ServerPort, Processor.UserID,Processor.UserPassword, Processor.Object.GeographServerObjectID(), ExceptionHandler, StartHandler,StopHandler);
	    		try {
	    			Socket socket = new Socket();
	    			socket.connect(new InetSocketAddress("127.0.0.1", LocalServer.GetPort()), ConnectioningTimeout);
					try {
						socket.setSoTimeout(ProcessingTimeout);
						//.
						InputStream IS = socket.getInputStream();
						try {
							OutputStream OS = socket.getOutputStream();
							try {
								TStreamChannel.TOnIdleHandler OnIdleHandler = new TStreamChannel.TOnIdleHandler() {
									@Override
									public void DoOnIdle(TCanceller Canceller) {
										TChannelProcessing.this.DoOnIdle(Canceller);
									}
								};
								//. processing ...
								Processor.Channel.DoStreaming(IS,OS, OnIdleHandler, Canceller);
							}
							finally {
								OS.close();
							}
						}
						finally {
							IS.close();
						}
					}
					finally {
						socket.close();
					}
	    		}
	    		finally {
    				LocalServer.Destroy();
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
	
    public TStreamChannelProcessor(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, TCoGeoMonitorObject pObject, TStreamChannel pChannel, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pObject, pChannel, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    }

    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
    	Processing = new TChannelProcessing(this);
    }
    
    @Override
    public boolean IsVisual() {
    	return false;
    }
}
