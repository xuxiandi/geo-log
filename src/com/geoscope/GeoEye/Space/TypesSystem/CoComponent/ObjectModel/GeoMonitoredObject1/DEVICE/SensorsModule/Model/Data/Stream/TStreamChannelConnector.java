package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
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
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;

public class TStreamChannelConnector extends TStreamChannelConnectorAbstract {

	public static final int VERSION_CHANNELBYID 			= 0;
	public static final int VERSION_CHANNELBYDESCRIPTOR 	= 1;
	
	private static final int DeviceSensorsModuleStreamingServerPort = 10009;
	
    public static class TChannelConnection extends TChannelConnectionAbstract {
		
    	public static final int LocalPort = 10008;
    	
		public static final int ConnectingTimeout = 1000*60; //. seconds
		public static final int StreamingTimeout = 100; //. milliseconds
		public static final int IdleTimeoutCounter = 10*30; //. counter of the ProcessingTimeout to fire the Idle event
		
	    public TChannelConnection(TStreamChannelConnectorAbstract pConnector) {
	    	super(pConnector);
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
	    				TChannelConnection.this.DoOnException(new Exception(E.getMessage()));	    			
	    			}
	    		};		
	    		TLANConnectionStartHandler StartHandler = ObjectModel.TLANConnectionStartHandler_Create(Connector.Object);
	    		TLANConnectionStopHandler StopHandler = ObjectModel.TLANConnectionStopHandler_Create(Connector.Object);
	    		//.
	    		TLANConnectionRepeater LocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",DeviceSensorsModuleStreamingServerPort, LocalPort, Connector.ServerAddress,Connector.ServerPort, Connector.UserID,Connector.UserPassword, Connector.Object.GeographServerObjectID(), Connector.UserAccessKey, ExceptionHandler, StartHandler,StopHandler);
	    		try {
	    			Socket Connection = new Socket();
	    			Connection.connect(new InetSocketAddress("127.0.0.1", LocalServer.GetPort()), ConnectingTimeout);
					try {
						Connection.setSoTimeout(ConnectingTimeout);
						//.
						InputStream IS = Connection.getInputStream();
						try {
							OutputStream OS = Connection.getOutputStream();
							try {
								switch (((TStreamChannelConnector)Connector).Version) {
								
								case VERSION_CHANNELBYID:
									//. send Version
									int Version = 1;
									byte[] Descriptor = TDataConverter.ConvertInt32ToLEByteArray(Version);
									OS.write(Descriptor);
									//. send ChannelID
									Descriptor = TDataConverter.ConvertInt32ToLEByteArray(Connector.Channel.ID);
									OS.write(Descriptor);
									//. get and check result
									IS.read(Descriptor);
									int RC = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
									if (RC != TSensorsModule.SENSORSSTREAMINGSERVER_MESSAGE_OK)
										throw new OperationException(RC,"error of connecting to the sensors streaming server, RC: "+Integer.toString(RC)); //. =>
									break; //. >

								case VERSION_CHANNELBYDESCRIPTOR:
									//. send Version
									Version = 2;
									Descriptor = TDataConverter.ConvertInt32ToLEByteArray(Version);
									OS.write(Descriptor);
									//. send ChannelDescriptor
									byte[] ChannelDescriptor = Connector.Channel.ToByteArray();
									Descriptor = TDataConverter.ConvertInt32ToLEByteArray(ChannelDescriptor.length);
									OS.write(Descriptor);
									OS.write(ChannelDescriptor);
									//. get and check result
									IS.read(Descriptor);
									RC = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
									if (RC != TSensorsModule.SENSORSSTREAMINGSERVER_MESSAGE_OK)
										throw new OperationException(RC,"error of connecting to the sensors streaming server, RC: "+Integer.toString(RC)); //. =>
									break; //. >
								}
								//.
								TStreamChannel.TOnProgressHandler OnProgressHandler = new TStreamChannel.TOnProgressHandler() {
									
									@Override
									public void DoOnProgress(int ReadSize, TCanceller Canceller) {
										TChannelConnection.this.DoOnProgress(ReadSize, Canceller);
									}
								};
								TStreamChannel.TOnIdleHandler OnIdleHandler = new TStreamChannel.TOnIdleHandler() {
									
									@Override
									public void DoOnIdle(TCanceller Canceller) throws Exception {
										TChannelConnection.this.DoOnIdle(Canceller);
									}
								};
								//. processing ...
								Connector.Channel.DoStreaming(Connection, IS,OS, OnProgressHandler, StreamingTimeout, IdleTimeoutCounter, OnIdleHandler, Canceller);
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
	
    
    private int Version;
    
    public TStreamChannelConnector(int pVersion, Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, TCoGeoMonitorObject pObject, TStreamChannel pChannel, String pUserAccessKey, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pObject, pChannel, pUserAccessKey, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    	//.
    	Version = pVersion;
    }

    public TStreamChannelConnector(int pVersion, Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, TCoGeoMonitorObject pObject, TStreamChannel pChannel, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	this(pVersion, pcontext, pServerAddress,pServerPort, pUserID, pUserPassword, pObject, pChannel, null, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    }
    
    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
    	ChannelConnection = new TChannelConnection(this);
    }
}
