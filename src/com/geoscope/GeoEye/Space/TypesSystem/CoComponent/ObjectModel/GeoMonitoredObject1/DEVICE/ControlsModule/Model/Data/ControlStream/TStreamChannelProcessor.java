package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.LANConnectionRepeaterDefines;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule.TLANConnectionStopHandler;
import com.geoscope.GeoLog.DEVICE.ControlsModule.TControlsModule;

public class TStreamChannelProcessor extends TStreamChannelProcessorAbstract {

	public static final int DeviceControlsModuleStreamingServerPort = 10008;
	
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
	    		TLANConnectionRepeater LocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",DeviceControlsModuleStreamingServerPort, LocalPort, Processor.ServerAddress,Processor.ServerPort, Processor.UserID,Processor.UserPassword, Processor.Object.GeographServerObjectID(), ExceptionHandler, StartHandler,StopHandler);
	    		try {
	    			Socket Connection = new Socket();
	    			Connection.connect(new InetSocketAddress("127.0.0.1", LocalServer.GetPort()), ConnectioningTimeout);
					try {
						Connection.setSoTimeout(ProcessingTimeout);
						//.
						InputStream IS = Connection.getInputStream();
						try {
							OutputStream OS = Connection.getOutputStream();
							try {
								//. send Version
								int Version = 1;
								byte[] Descriptor = TDataConverter.ConvertInt32ToLEByteArray(Version);
								OS.write(Descriptor);
								//. send ChannelID
								Descriptor = TDataConverter.ConvertInt32ToLEByteArray(Processor.Channel.ID);
								OS.write(Descriptor);
								//. get and check result
								IS.read(Descriptor);
								int RC = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
								if (RC != TControlsModule.CONTROLSSTREAMINGSERVER_MESSAGE_OK)
									throw new IOException("error of connecting to the controls streaming server, RC: "+Integer.toString(RC)); //. =>
								//.
								Processor.Channel.SetConnection(OS,IS);
								try {
									//. processing ...
									Processor.Channel.DoStreaming(IS,OS, Canceller);
								}
								finally {
									Processor.Channel.ClearConnection();
								}
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
						Connection.close();
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
	
    public TStreamChannelProcessor(Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, TCoGeoMonitorObject pObject, TStreamChannel pChannel, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
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
