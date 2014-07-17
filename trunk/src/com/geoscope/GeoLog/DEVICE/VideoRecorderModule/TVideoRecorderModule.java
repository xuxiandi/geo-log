package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TVideoRecorderServerVideoPhoneServer;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographDataServer.TGeographDataServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TLoadVideoRecorderConfigurationSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderActiveFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderAudioFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderModeSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderRecordingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderSavingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderTransmittingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderVideoFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.Camera;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.Network.TServerConnection;
import com.geoscope.Utils.TDataConverter;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;

@SuppressLint("HandlerLeak")
public class TVideoRecorderModule extends TModule {

	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"VideoRecorderModule";
	}
	
	public static final String VideoPhoneProfileFileName = "VideoPhone.xml";
	
	public static final short MODE_UNKNOWN					= 0;
	public static final short MODE_H264STREAM1_AMRNBSTREAM1 = 1;
	public static final short MODE_MPEG4					= 2;
	public static final short MODE_3GP						= 3;
	public static final short MODE_H263STREAM1_AMRNBSTREAM1 = 4;
	public static final short MODE_FRAMESTREAM 				= 5;

	public static final boolean 	RecorderIsHidden = false;
	public static final int 		RecorderWatcherInterval = 1000*60; //. seconds
	public static final int 		RecorderMeasurementFlashCounter = 2; //. minutes
	public static final int 		RecorderMeasurementCheckCounter = 1; //. minutes
	public static final int 		RecorderMeasurementRemovingCounter = 60; //. minutes
	
	public static final int MESSAGE_OPERATION_COMPLETED 					= 1;
    public static final int MESSAGE_OPERATION_ERROR 						= 2;
	public static final int MESSAGE_CONFIGURATION_RECEIVED 					= 3;
	public static final int MESSAGE_SETSAVINGSERVER_OPERATION_COMPLETED 	= 4;
    public static final int MESSAGE_UPDATERECORDERSTATE 					= 5;
    public static final int MESSAGE_CHECKRECORDERMEASUREMENT 				= 6;
    public static final int MESSAGE_RESTARTRECORDER 						= 7;
	
    public class TMeasurementConfiguration {
        public double MaxDuration = (1.0/(24*60))*60; //. minutes
        public double LifeTime  = 2.0; //. days
        public double AutosaveInterval = 1000; //. days
    }
    
    public class TCameraConfiguration {
        public int Camera_Audio_Source = 0;
        public int Camera_Audio_SampleRate = -1;
    	public int Camera_Audio_BitRate = -1;
    	public int Camera_Video_Source = 0;
    	public int Camera_Video_ResX = 640;
    	public int Camera_Video_ResY = 480;
    	public int Camera_Video_FrameRate = 10;
    	public int Camera_Video_BitRate = -1;
    }
    
    public class TServerSaver implements Runnable {
    	
    	public static final int CONNECTION_TYPE_PLAIN 		= 0;
    	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
    	
    	///? public static final int AutoSavingDefaultInterval = 1000*3600*24*1000/*days*/;
    	public static final int ConnectTimeout = 1000*10; //. seconds
    	
    	private class SavingDataErrorException extends Exception {
    		
			private static final long serialVersionUID = 1L;

			public SavingDataErrorException() {
    			super();
    		}
    	}
    	
        public String 	ServerAddress = "127.0.0.1";
        public int		ServerPort = 5000;
    	protected int 	SecureServerPortShift = 2;
        protected int	SecureServerPort() {
        	return (ServerPort+SecureServerPortShift);
        }
        //.
        public int			ConnectionType = (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
        private Socket 		Connection;
        public InputStream 	ConnectionInputStream;
        public OutputStream ConnectionOutputStream;
        //.
    	protected Thread _Thread;
    	private boolean flCancel = false;
    	public boolean flProcessing = false;
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	private String MeasurementsIDsToProcess = "";
    	private boolean flProcessingMeasurement = false;
    	
    	public TServerSaver(TSavingServerDescriptor ServerDescriptor) {
    		ServerAddress = ServerDescriptor.Address;
    		ServerPort = ServerDescriptor.Port;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public void Destroy() throws IOException {
    		CancelAndWait();
    	}
    	
		@Override
		public void run() {
			flProcessing = true;
			try {
				try {
					while (!flCancel) {
						ProcessSignal.WaitOne((int)(TVideoRecorderModule.this.MeasurementConfiguration.AutosaveInterval*1000*3600*24));
						if (flCancel)
							return; //. ->
						//.
						String _MeasurementsIDsToProcess;
						synchronized (this) {
							_MeasurementsIDsToProcess = MeasurementsIDsToProcess;
							MeasurementsIDsToProcess = "";
						}
						//. processing measurement(s)
						flProcessingMeasurement = true;
						try {
							try {
								byte[] TransferBuffer = new byte[1024*64];
								if (_MeasurementsIDsToProcess.equals("")) {
									File[] Measurements = TVideoRecorderMeasurements.GetMeasurementsFolderList();
									for (int I = 0; I < Measurements.length; I++) 
										if (Measurements[I] != null) {
											String MeasurementID = Measurements[I].getName();
											TMeasurementDescriptor CurrentMeasurement = Camera.CurrentCamera_GetMeasurementDescriptor();
											if (!((CurrentMeasurement != null) && CurrentMeasurement.ID.equals(MeasurementID))) {
												try {
													ProcessMeasurement(MeasurementID,TransferBuffer);
												}
												catch (SavingDataErrorException E) {}
											}
										}
								}
								else {
									String[] Measurements = _MeasurementsIDsToProcess.split(",");
									for (int I = 0; I < Measurements.length; I++) 
										if (Measurements[I] != null) {
											String MeasurementID = Measurements[I];
											TMeasurementDescriptor CurrentMeasurement = Camera.CurrentCamera_GetMeasurementDescriptor();
											if (!((CurrentMeasurement != null) && CurrentMeasurement.ID.equals(MeasurementID))) {
												try {
													ProcessMeasurement(MeasurementID,TransferBuffer);
												}
												catch (SavingDataErrorException E) {}
											}
										}
								}
							}
							catch (Throwable TE) {
				            	//. log errors
								String S = TE.getMessage();
								if (S == null)
									S = TE.getClass().getName();
			            		Device.Log.WriteError("VideoRecorderModule.ServerSaver",S);
				            	if (!(TE instanceof Exception))
				            		TGeoLogApplication.Log_WriteCriticalError(TE);
							}
						}
						finally {
							flProcessingMeasurement = false;
						}
					}

				}
				catch (InterruptedException E) {
				}
			}
			finally {
				flProcessing = false;
			}
		}
		
		public void StartProcess(String MIDs) {
			synchronized (this) {
				MeasurementsIDsToProcess = MIDs;
			}
			ProcessSignal.Set();
		}
		
		public void StartProcess() {
			StartProcess("");		
		}
		
		public boolean IsProcessingMeasurement() {
			return flProcessingMeasurement;
		}
		
    	public void Join() {
    		try {
    			_Thread.join();
    		}
    		catch (Exception E) {}
    	}

		public void Cancel() {
			flCancel = true;
			//.
			ProcessSignal.Set();
    		//.
    		if (_Thread != null)
    			_Thread.interrupt();
		}
		
		public void CancelAndWait() throws IOException {
    		Cancel();
    		try {
    			if (_Thread != null)
    				_Thread.join();
			} catch (InterruptedException e) {
			}
    	}
		
		private final int Connect_TryCount = 3;
		
	    private void Connect() throws Exception
	    {
			int TryCounter = Connect_TryCount;
			while (true) {
				try {
					try {
						//. connect
				    	switch (ConnectionType) {
				    	
				    	case CONNECTION_TYPE_PLAIN:
				            Connection = new Socket(ServerAddress,ServerPort); 
				    		break; //. >
				    		
				    	case CONNECTION_TYPE_SECURE_SSL:
				    		TrustManager[] _TrustAllCerts = new TrustManager[] { new javax.net.ssl.X509TrustManager() {
				    	        @Override
				    	        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
				    	        }
				    	        @Override
				    	        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
				    	        }
				    	        @Override
				    	        public X509Certificate[] getAcceptedIssuers() {
				    	            return null;
				    	        }
				    	    } };
				    	    //. install the all-trusting trust manager
				    	    SSLContext sslContext = SSLContext.getInstance( "SSL" );
				    	    sslContext.init( null, _TrustAllCerts, new java.security.SecureRandom());
				    	    //. create a ssl socket factory with our all-trusting manager
				    	    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
				    	    Connection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
				    		break; //. >
				    		
				    	default:
				    		throw new Exception("unknown connection type"); //. =>
				    	}
				        Connection.setSoTimeout(ConnectTimeout);
				        Connection.setKeepAlive(true);
				        Connection.setSendBufferSize(10000);
				        ConnectionInputStream = Connection.getInputStream();
				        ConnectionOutputStream = Connection.getOutputStream();
						break; //. >
					} catch (SocketTimeoutException STE) {
						throw new IOException(Device.context.getString(R.string.SConnectionTimeoutError)); //. =>
					} catch (ConnectException CE) {
						throw new ConnectException(Device.context.getString(R.string.SNoServerConnection)); //. =>
					} catch (Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.toString();
						throw new Exception(Device.context.getString(R.string.SHTTPConnectionError)+S); //. =>
					}
				}
				catch (Exception E) {
					TryCounter--;
					if (TryCounter == 0)
						throw E; //. =>
				}
			}
	    }
	    
	    private void Disconnect(boolean flDisconnectGracefully) throws IOException
	    {
	    	if (flDisconnectGracefully) {
		        //. close connection gracefully
		        byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(TGeographDataServerClient.MESSAGE_DISCONNECT);
		        ConnectionOutputStream.write(BA);
		        ConnectionOutputStream.flush();
	    	}
	        //.
	        ConnectionOutputStream.close();
	        ConnectionInputStream.close();
	        Connection.close();
	    }
	    
	    private void Disconnect() throws IOException {
	    	Disconnect(true);
	    }
	    
		private short Buffer_GetCRC(byte[] buffer, int Offset, int Size) {
	        int CRC = 0;
	        int V;
	        int Idx  = Offset;
	        while (Idx < (Offset+Size))
	        {
	            V = (int)(buffer[Idx] & 0x000000FF);
	            CRC = (((CRC+V) << 1)^V);
	            //.
	            Idx++;
	        }
	        return (short)CRC;
		}
		
		private void Buffer_Encrypt(byte[] buffer, int Offset, int Size, String UserPassword) throws UnsupportedEncodingException {
	        int StartIdx = Offset;
	        byte[] UserPasswordArray;
	        UserPasswordArray = UserPassword.getBytes("windows-1251");
	        //.
	        if (UserPasswordArray.length > 0)
	        {
	            int UserPasswordArrayIdx = 0;
	            for (int I = StartIdx; I < (StartIdx+Size); I++)
	            {
	                buffer[I] = (byte)(buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
	                UserPasswordArrayIdx++;
	                if (UserPasswordArrayIdx >= UserPasswordArray.length) 
	                	UserPasswordArrayIdx = 0;
	            }
	        }
		}
		
	    private void Login() throws Exception {
	    	byte[] LoginBuffer = new byte[20];
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(TGeographDataServerClient.SERVICE_SETVIDEORECORDERDATA_V1);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(Device.UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(Device.idGeographServerObject);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,8);
			BA = TDataConverter.ConvertInt16ToBEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,18, BA.length);
			Buffer_Encrypt(LoginBuffer,10,10,Device.UserPassword);
			//.
			ConnectionOutputStream.write(LoginBuffer);
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor != TGeographDataServerClient.MESSAGE_OK)
				throw new Exception(Device.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
	    }
	    
		private void ProcessMeasurement(String MeasurementID, byte[] TransferBuffer) throws Exception {
			boolean flDisconnect = true;
			Connect();
			try {
				Login();
				//.
				double DataID = Double.parseDouble(MeasurementID);
				byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(DataID);
				ConnectionOutputStream.write(BA);
				//.
				File[] MeasurementContent = TVideoRecorderMeasurements.GetMeasurementFolderContent(MeasurementID);
				for (int I = 0; I < MeasurementContent.length; I++)
					if (!MeasurementContent[I].isDirectory()) {
						byte[] FileNameBA = MeasurementContent[I].getName().getBytes("windows-1251");
						byte[] DecriptorBA = new byte[4];
						int Descriptor = FileNameBA.length;
						DecriptorBA = TDataConverter.ConvertInt32ToBEByteArray(Descriptor);
						ConnectionOutputStream.write(DecriptorBA);
						//.
						if (Descriptor > 0)  
							ConnectionOutputStream.write(FileNameBA);
						//.
						Descriptor = (int)MeasurementContent[I].length(); 
						DecriptorBA = TDataConverter.ConvertInt32ToBEByteArray(Descriptor);
						ConnectionOutputStream.write(DecriptorBA);
						//.
						if (Descriptor > 0) {
							FileInputStream FIS = new FileInputStream(MeasurementContent[I]);
							try {
								while (Descriptor > 0) {
									int BytesRead = FIS.read(TransferBuffer);
									ConnectionOutputStream.write(TransferBuffer,0,BytesRead);
									//.
									Descriptor -= BytesRead;
									//.
									if ((Descriptor > 0) && (ConnectionInputStream.available() >= 4)) {
										ConnectionInputStream.read(DecriptorBA);
										int _Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
										if (_Descriptor == TGeographDataServerClient.MESSAGE_SAVINGDATAERROR) { 
											flDisconnect = false;
											throw new SavingDataErrorException(); //. =>
										}
									}
									//.
									if (flCancel) {
										Disconnect(false);
										flDisconnect = false;
										//.
										throw new InterruptedException(); //. =>
									}
								}
							}
							finally {
								FIS.close();
							}
							//. check ok
							ConnectionInputStream.read(DecriptorBA);
							Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
							if (Descriptor != TGeographDataServerClient.MESSAGE_OK) {
								flDisconnect = false;
								throw new SavingDataErrorException(); //. =>
							}
						}
					}
				//. remove transferred measurement
				TVideoRecorderMeasurements.DeleteMeasurement(MeasurementID);
			}
			finally {
				if (flDisconnect)
					Disconnect();
			}
		}
    }
    
	public String 	Profile_Name = "";
	public boolean	Profile_flDefault = true;
	//.
	public TMeasurementConfiguration 	MeasurementConfiguration;
	public TCameraConfiguration 		CameraConfiguration;
	//.
	public TComponentTimestampedInt16Value 		Mode;
	public TVideoRecorderActiveValue 			Active;
	public TComponentTimestampedBooleanValue 	Recording;
	public TComponentTimestampedBooleanValue 	Audio;
	public TComponentTimestampedBooleanValue 	Video;
	public TComponentTimestampedBooleanValue 	Transmitting;
	public TComponentTimestampedBooleanValue 	Saving;
	public TComponentTimestampedANSIStringValue	SDP;
	public TComponentTimestampedANSIStringValue	Receivers;
	public TComponentTimestampedANSIStringValue	SavingServer;
	//. virtual values
	public TVideoRecorderConfigurationDataValue	ConfigurationDataValue;
	//.
    private Timer RecorderWatcher = null;
    private TServerSaver ServerSaver = null;

    public TVideoRecorderModule(TDEVICEModule pDevice) throws Exception
    {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
        //.
        MeasurementConfiguration 	= new TMeasurementConfiguration();
        CameraConfiguration 		= new TCameraConfiguration();
        //.
        Mode 			= new TComponentTimestampedInt16Value();
        Active 			= new TVideoRecorderActiveValue(this);
        Recording 		= new TComponentTimestampedBooleanValue();
        Audio 			= new TComponentTimestampedBooleanValue();
        Video			= new TComponentTimestampedBooleanValue();
        Transmitting	= new TComponentTimestampedBooleanValue();
        Saving			= new TComponentTimestampedBooleanValue();
        SDP 			= new TComponentTimestampedANSIStringValue();
        Receivers 		= new TComponentTimestampedANSIStringValue();
        SavingServer	= new TComponentTimestampedANSIStringValue();
    	//. virtual values
        ConfigurationDataValue = new TVideoRecorderConfigurationDataValue(this);
		//. workaround for ClassNotFoundException of TVideoRecorderServerVideoPhone class 
        TVideoRecorderServerVideoPhoneServer.SessionServer.Session_Get();
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() throws Exception {
    	Stop();
    }
    
    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
        if (IsEnabled()) {
            try {
            	TVideoRecorderMeasurements.ValidateMeasurements();
            }
            catch (Exception E) {
                Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderMeasurementsCheckingFailed)+E.getMessage(), Toast.LENGTH_LONG).show();
            }
            //.
        	UpdateRecorderState();
			//.
        	StartReceivingConfiguration();
        	//.
            RecorderWatcher = new Timer();
            RecorderWatcher.schedule(new TRecorderWatcherTask(),RecorderWatcherInterval,RecorderWatcherInterval);
        }
    }
    
    @Override
    public void Stop() throws Exception {
    	if (ServerSaver != null) {
    		ServerSaver.Destroy();
    		ServerSaver = null;
    	}
    	if (RecorderWatcher != null) {
    		RecorderWatcher.cancel();
    		RecorderWatcher = null;
    	}
    	//.
    	super.Stop();
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
		String PFN = ModuleFile();
		if (LoadProfileFromFile(PFN))
			Profile_flDefault = true;
    }
    
    public synchronized boolean LoadProfileFromFile(String PFN) throws Exception {
		File F = new File(PFN);
		if (!F.exists()) 
			return false; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(PFN);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		Element RootNode = XmlDoc.getDocumentElement();
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("VideoRecorderModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		case 1:
			try {
				Node node = RootNode.getElementsByTagName("flVideoRecorderModuleIsEnabled").item(0).getFirstChild();
				if (node != null)
					flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
				node = RootNode.getElementsByTagName("Name").item(0).getFirstChild();
				if (node != null)
					Profile_Name = node.getNodeValue();
				node = RootNode.getElementsByTagName("Mode").item(0).getFirstChild();
				if (node != null)
					Mode.SetValue(OleDate.UTCCurrentTimestamp(),Short.parseShort(node.getNodeValue()));
				node = RootNode.getElementsByTagName("Active").item(0).getFirstChild();
				if (node != null)
					Active.FromString(node.getNodeValue());
				node = RootNode.getElementsByTagName("Recording").item(0).getFirstChild();
				if (node != null)
					Recording.FromString(node.getNodeValue());
				NodeList NL = RootNode.getElementsByTagName("Audio");
				for (int I = 0; I < NL.getLength(); I++) {
					node = NL.item(I).getFirstChild();
					if ((node != null) && (node.getNodeValue() != null)) { 
						Audio.FromString(node.getNodeValue());
						break; //. >
					}
				}
				NL = RootNode.getElementsByTagName("Video");
				for (int I = 0; I < NL.getLength(); I++) {
					node = NL.item(I).getFirstChild();
					if ((node != null) && (node.getNodeValue() != null)) { 
						Video.FromString(node.getNodeValue());						
						break; //. >
					}
				}
				node = RootNode.getElementsByTagName("Transmitting").item(0).getFirstChild();
				if (node != null)
					Transmitting.FromString(node.getNodeValue());
				node = RootNode.getElementsByTagName("Saving").item(0).getFirstChild();
				if (node != null)
					Saving.FromString(node.getNodeValue());
				node = RootNode.getElementsByTagName("SDP").item(0).getFirstChild();
				if (node != null)
					SDP.SetValue(node.getNodeValue());
				node = RootNode.getElementsByTagName("Receivers").item(0).getFirstChild();
				if (node != null)
					Receivers.SetValue(node.getNodeValue());
				node = RootNode.getElementsByTagName("SavingServer").item(0).getFirstChild();
				if (node != null)
					SavingServer.SetValue(node.getNodeValue());
				//. measurement configuration
				node = RootNode.getElementsByTagName("MaxDuration").item(0).getFirstChild();
				if (node != null)
					MeasurementConfiguration.MaxDuration = Double.parseDouble(node.getNodeValue());
				node = RootNode.getElementsByTagName("LifeTime").item(0).getFirstChild();
				if (node != null)
					MeasurementConfiguration.LifeTime = Double.parseDouble(node.getNodeValue());
				node = RootNode.getElementsByTagName("AutosaveInterval").item(0).getFirstChild();
				if (node != null)
					MeasurementConfiguration.AutosaveInterval = Double.parseDouble(node.getNodeValue());
				//. camera configuration
				CameraConfiguration.Camera_Audio_Source = MediaRecorder.AudioSource.DEFAULT;
				node = RootNode.getElementsByTagName("AudioSource").item(0);
				if (node != null) {
					node = node.getFirstChild();
					if (node != null)
						CameraConfiguration.Camera_Audio_Source = Integer.parseInt(node.getNodeValue());
				}
				node = RootNode.getElementsByTagName("SampleRate").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Audio_SampleRate = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("ABitRate").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Audio_BitRate = Integer.parseInt(node.getNodeValue());
				CameraConfiguration.Camera_Video_Source = MediaRecorder.VideoSource.DEFAULT;
				node = RootNode.getElementsByTagName("VideoSource").item(0);
				if (node != null) {
					node = node.getFirstChild();
					if (node != null)
						CameraConfiguration.Camera_Video_Source = Integer.parseInt(node.getNodeValue());
				}
				node = RootNode.getElementsByTagName("ResX").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Video_ResX = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("ResY").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Video_ResY = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("FrameRate").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Video_FrameRate = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("VBitRate").item(0).getFirstChild();
				if (node != null)
					CameraConfiguration.Camera_Video_BitRate = Integer.parseInt(node.getNodeValue());
			}
			catch (Exception E) {
			}
			break; //. >
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
		return true; 
    }
    
    public synchronized boolean LoadVideoPhoneProfile() throws Exception {
		String PFN = Folder()+"/"+VideoPhoneProfileFileName;
		if (LoadProfileFromFile(PFN)) {
			Profile_flDefault = false;
			return true; //. ->
		}
		else
			return false; //. ->			
    }
        
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
    	if (!Profile_flDefault)
    		LoadProfile();
		int Version = 1;
        Serializer.startTag("", "VideoRecorderModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //.
        int V = 0;
        if (flEnabled)
        	V = 1;
        Serializer.startTag("", "flVideoRecorderModuleIsEnabled");
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "flVideoRecorderModuleIsEnabled");
        //. 
        Serializer.startTag("", "Name");
        Serializer.text(Profile_Name);
        Serializer.endTag("", "Name");
        //. 
        Serializer.startTag("", "Mode");
        Serializer.text(Short.toString(Mode.GetValue()));
        Serializer.endTag("", "Mode");
        //. 
        Serializer.startTag("", "Active");
        Serializer.text(Active.ToString());
        Serializer.endTag("", "Active");
        //. 
        Serializer.startTag("", "Recording");
        Serializer.text(Recording.ToString());
        Serializer.endTag("", "Recording");
        //. 
        Serializer.startTag("", "Audio");
        Serializer.text(Audio.ToString());
        Serializer.endTag("", "Audio");
        //. 
        Serializer.startTag("", "Video");
        Serializer.text(Video.ToString());
        Serializer.endTag("", "Video");
        //. 
        Serializer.startTag("", "Transmitting");
        Serializer.text(Transmitting.ToString());
        Serializer.endTag("", "Transmitting");
        //. 
        Serializer.startTag("", "Saving");
        Serializer.text(Saving.ToString());
        Serializer.endTag("", "Saving");
        //. 
        Serializer.startTag("", "SDP");
        Serializer.text(SDP.GetValue());
        Serializer.endTag("", "SDP");
        //. 
        Serializer.startTag("", "Receivers");
        Serializer.text(Receivers.GetValue());
        Serializer.endTag("", "Receivers");
        //. 
        Serializer.startTag("", "SavingServer");
        Serializer.text(SavingServer.GetValue());
        Serializer.endTag("", "SavingServer");
        //. 
        Serializer.startTag("", "Measurement");
        //.
        Serializer.startTag("", "MaxDuration");
        Serializer.text(Double.toString(MeasurementConfiguration.MaxDuration));
        Serializer.endTag("", "MaxDuration");
        //.
        Serializer.startTag("", "LifeTime");
        Serializer.text(Double.toString(MeasurementConfiguration.LifeTime));
        Serializer.endTag("", "LifeTime");
        //.
        Serializer.startTag("", "AutosaveInterval");
        Serializer.text(Double.toString(MeasurementConfiguration.AutosaveInterval));
        Serializer.endTag("", "AutosaveInterval");
        //.
        Serializer.endTag("", "Measurement");
        //. 
        Serializer.startTag("", "Camera");
        //.
        Serializer.startTag("", "Audio");
        //.
        Serializer.startTag("", "SampleRate");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Audio_SampleRate));
        Serializer.endTag("", "SampleRate");
        //.
        Serializer.startTag("", "ABitRate");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Audio_BitRate));
        Serializer.endTag("", "ABitRate");
        //.
        Serializer.endTag("", "Audio");
        //.
        Serializer.startTag("", "Video");
        //.
        Serializer.startTag("", "ResX");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Video_ResX));
        Serializer.endTag("", "ResX");
        //.
        Serializer.startTag("", "ResY");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Video_ResY));
        Serializer.endTag("", "ResY");
        //.
        Serializer.startTag("", "FrameRate");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Video_FrameRate));
        Serializer.endTag("", "FrameRate");
        //.
        Serializer.startTag("", "VBitRate");
        Serializer.text(Integer.toString(CameraConfiguration.Camera_Video_BitRate));
        Serializer.endTag("", "VBitRate");
        //.
        Serializer.endTag("", "Video");
        //.
        Serializer.endTag("", "Camera");
        //.
        Serializer.endTag("", "VideoRecorderModule");
    }
    
    private void StartReceivingConfiguration() {
		TLoadVideoRecorderConfigurationSO GSO = new TLoadVideoRecorderConfigurationSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        try {
        	Device.ConnectorModule.OutgoingGetComponentDataOperationsQueue.AddNewOperation(GSO);
        } 
        catch (Exception E) {
			Toast.makeText(Device.context, Device.context.getString(R.string.SError)+E.toString(), Toast.LENGTH_LONG).show();
			return; //. ->
        }    	
    }
    
    public boolean Security_UserAccessCodeIsActive() {
    	return ((!Device.AudioModule.UserAccessKey.IsNull()) || (!Device.VideoModule.UserAccessKey.IsNull()));
    }
    
    public TReceiverDescriptor GetReceiverDescriptor() {
    	String S = Receivers.GetValue();
    	if ((S == null) || (S.equals("")))
    		return null; //. ->
    	String[] SA = S.split(";");
    	if (SA.length < 1)
    		return null; //. ->
    	String RS = SA[0];
    	SA = RS.split(",");
    	if (SA.length < 2)
    		return null; //. ->
    	TReceiverDescriptor Result = new TReceiverDescriptor();
    	if (!SA[0].equals(""))
    		Result.Address = SA[0];
    	else
    		if (Device.ConnectorModule != null)
    			Result.Address = Device.ConnectorModule.ServerAddress;
    		else
    			return null; //. ->
    	Result.ReceiverType = Integer.parseInt(SA[1]);
    	switch (Result.ReceiverType) {
    	case TReceiverDescriptor.RECEIVER_NATIVE: 
    		Result.AudioPort = TReceiverDescriptor.RECEIVER_NATIVE_AUDIOPORT; 
    		Result.VideoPort = TReceiverDescriptor.RECEIVER_NATIVE_VIDEOPORT; 
    		break; //. 
    	}
    	if (SA.length > 2)
    		Result.AudioPort = Integer.parseInt(SA[2]);
    	if (SA.length > 3)
    		Result.VideoPort = Integer.parseInt(SA[3]);
    	//.
    	return Result;
    }
    
    public TSavingServerDescriptor GetSavingServerDescriptor() {
    	String S = SavingServer.GetValue();
    	if ((S == null) || (S.equals("")))
    		return null; //. ->
    	String[] SA = S.split(";");
    	if (SA.length < 1)
    		return null; //. ->
    	String RS = SA[0];
    	SA = RS.split(",");
    	if (SA.length < 2)
    		return null; //. ->
    	TSavingServerDescriptor Result = new TSavingServerDescriptor();
    	if (!SA[0].equals(""))
    		Result.Address = SA[0];
    	else
    		if (Device.ConnectorModule != null)
    			Result.Address = Device.ConnectorModule.ServerAddress;
    		else
    			return null; //. ->
    	Result.ServerType = Integer.parseInt(SA[1]);
    	switch (Result.ServerType) {
    	case TSavingServerDescriptor.SAVINGSERVER_NATIVEFTP: 
    		Result.Port = TSavingServerDescriptor.SAVINGSERVER_NATIVEFTP_PORT; 
    		break; //. 
    	}
    	if (SA.length > 2)
    		Result.Port = Integer.parseInt(SA[2]);
    	if (SA.length > 3)
    		Result.BaseFolder = SA[3];
    	//.
    	return Result;
    }
    
    public Handler CompletionHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
            	if (!flEnabled) {
            		if (Active.BooleanValue())
            			DeActivate();
            		return; //. ->
            	}
                switch (msg.what) {
                
                case MESSAGE_CONFIGURATION_RECEIVED:
                case MESSAGE_OPERATION_COMPLETED:
                	try {
    					SaveProfile();
    				} catch (Exception E) {
    	        		Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderModuleLocalConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
    				}
    				//.
                	UpdateRecorderState();
    				//.
    		        if ((msg.what == MESSAGE_CONFIGURATION_RECEIVED) && (flEnabled) && (ServerSaver == null)) {
    		        	TSavingServerDescriptor SD = GetSavingServerDescriptor();
    		        	if (SD != null) 
    		        		synchronized (this) {
    			        		ServerSaver = new TServerSaver(SD);
    						}
    		        }
                	break; //. >

                case MESSAGE_OPERATION_ERROR: 
                	OperationException E = (OperationException)msg.obj;
                	//.
            		Toast.makeText(Device.context, Device.context.getString(R.string.SVideoRecorderModuleSettingConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
                	break; //. >
                	
                case MESSAGE_SETSAVINGSERVER_OPERATION_COMPLETED:
                	if (flEnabled) {
                		try {
                			synchronized (this) {
                        		if (ServerSaver != null)
                        			ServerSaver.Destroy();
            		        	TSavingServerDescriptor SD = GetSavingServerDescriptor();
            		        	if (SD != null)
            		        		ServerSaver = new TServerSaver(SD);
    						}
        				}
        				catch (Exception E1) {
        					Toast.makeText(Device.context, Device.context.getString(R.string.SErrorOfCreatingTransmissionService)+E1.getMessage(), Toast.LENGTH_LONG).show();
        				}
                	}
                	break; //. >
                	
                case MESSAGE_UPDATERECORDERSTATE:
                	UpdateRecorderState();
                	break; //. >
                	
                case MESSAGE_CHECKRECORDERMEASUREMENT:
                	TVideoRecorderPanel VideoRecorderPanel = TVideoRecorderPanel.GetVideoRecorderPanel();
        			if ((VideoRecorderPanel != null) && VideoRecorderPanel.IsRecording() && VideoRecorderPanel.IsSaving()) 
        				try {
        					TMeasurementDescriptor CurrentMeasurement = VideoRecorderPanel.Recording_GetMeasurementDescriptor();
        					if ((CurrentMeasurement != null) && CurrentMeasurement.IsStarted()) {
        						double NowTime = TVideoRecorderMeasurements.GetCurrentTime();
        						if ((NowTime-CurrentMeasurement.StartTimestamp) > MeasurementConfiguration.MaxDuration)
        							ReinitializeRecorder();
        					}
        				}
        				catch (Exception E1) {
        					Toast.makeText(Device.context, Device.context.getString(R.string.SMeasurementCheckingError)+E1.getMessage(), Toast.LENGTH_LONG).show();
        				}
                	break; //. >
                	
                case MESSAGE_RESTARTRECORDER: 
    				try {
    					ReinitializeRecorder();
    				}
    				catch (Exception E1) {
    					Toast.makeText(Device.context, Device.context.getString(R.string.SMeasurementRestartError)+E1.getMessage(), Toast.LENGTH_LONG).show();
    				}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
    
    public void UpdateRecorderState() {
    	if (!flEnabled) 
    		return; //. ->
    	//.
    	TVideoRecorderPanel VideoRecorderPanel = TVideoRecorderPanel.GetVideoRecorderPanel();
    	if (Active.BooleanValue() && (VideoRecorderPanel != null)) {
    		boolean flRestart = false;
    		boolean flStop = false;
			if (Recording.BooleanValue() && VideoRecorderPanel.IsRecording()) {
				flRestart = (!((Mode.GetValue() == VideoRecorderPanel.GetMode()) && (Transmitting.BooleanValue() == VideoRecorderPanel.IsTransmitting()) && (Saving.BooleanValue() == VideoRecorderPanel.IsSaving()) && (Audio.BooleanValue() == VideoRecorderPanel.IsAudio()) && (Video.BooleanValue() == VideoRecorderPanel.IsVideo())));
			}
			else
				if (Recording.BooleanValue())
					flRestart = true;
				else
					if (VideoRecorderPanel.IsRecording())
						flStop = true;
			if (flRestart) {
				boolean flSetTransmitting = ((Transmitting.BooleanValue() != VideoRecorderPanel.IsTransmitting()) && (Mode.GetValue() == VideoRecorderPanel.GetMode()) && (Saving.BooleanValue() == VideoRecorderPanel.IsSaving()) && (Audio.BooleanValue() == VideoRecorderPanel.IsAudio()) && (Video.BooleanValue() == VideoRecorderPanel.IsVideo()));
				if (flSetTransmitting) {
					if (Transmitting.BooleanValue()) {
						if (Device.idGeographServerObject != 0)
							VideoRecorderPanel.StartTransmitting(Device.idGeographServerObject);
					}
					else
						VideoRecorderPanel.FinishTransmitting();
				}
				else {
    				TReceiverDescriptor RD = GetReceiverDescriptor();
            		if (RD != null) {
        				TVideoRecorderPanel.flHidden = RecorderIsHidden;
            	    	if (TVideoRecorderPanel.flHidden) {
            	        	TReflector Reflector = TReflector.GetReflector();
            	        	if (((Reflector != null) && Reflector.flVisible) || Video.BooleanValue())
            	        		TVideoRecorderPanel.flHidden = false;
            	    	}
            	    	//.
            			if (VideoRecorderPanel.RestartRecording(RD, Mode.GetValue(), Transmitting.BooleanValue(), Saving.BooleanValue(), Audio.BooleanValue(),Video.BooleanValue()) && (!TVideoRecorderPanel.flHidden))
            				/*///? Toast.makeText(Device.context, Device.context.getString(R.string.SRecordingIsStarted)+RD.Address, Toast.LENGTH_LONG).show()*/;
            		}
				}
			}
			else
    			if (flStop)
    				FinishRecorder();
    	}
    	else
    		if (Active.BooleanValue()) {
    			if (!TVideoRecorderPanel.flStarting) {
            		TVideoRecorderPanel.flStarting = true;
            		//.
    				TVideoRecorderPanel.flHidden = RecorderIsHidden;
        	    	if (TVideoRecorderPanel.flHidden) {
        	        	TReflector Reflector = TReflector.GetReflector();
        	        	if (((Reflector != null) && Reflector.flVisible) || Video.BooleanValue())
        	        		TVideoRecorderPanel.flHidden = false;
        	    	}
        			//.
            		Intent intent = new Intent(Device.context,TVideoRecorderPanel.class);
            		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		Device.context.startActivity(intent);
    			}
    		}
    		else 
    			if (VideoRecorderPanel != null)
    				VideoRecorderPanel.finish();
    }
    
    public void PostUpdateRecorderState() {
    	CompletionHandler.obtainMessage(MESSAGE_UPDATERECORDERSTATE).sendToTarget();
    }
    
    public void FinishRecorder() {
    	TVideoRecorderPanel VideoRecorderPanel = TVideoRecorderPanel.GetVideoRecorderPanel();
    	if (VideoRecorderPanel != null)
    		VideoRecorderPanel.StopRecording();
		//.
		/*///??? synchronized (this) {
			if (ServerSaver != null)
				ServerSaver.StartProcess();
		}*/
    }
    
    public void ReinitializeRecorder() {
    	TVideoRecorderPanel VideoRecorderPanel = TVideoRecorderPanel.GetVideoRecorderPanel();
    	if ((VideoRecorderPanel != null) && VideoRecorderPanel.IsRecording())
    		FinishRecorder();
    	UpdateRecorderState();
    }
    
    public void PostRestartRecorder() {
    	CompletionHandler.obtainMessage(MESSAGE_RESTARTRECORDER).sendToTarget();
    }
    
    public synchronized TServerSaver GetServerSaver() {
    	return ServerSaver;
    }
    
    private class TRecorderWatcherTask extends TimerTask
    {
    	private int TicksCounter = 0;
    	
        public void UpdateRecorderState() {
        	CompletionHandler.obtainMessage(MESSAGE_UPDATERECORDERSTATE).sendToTarget();
        }
        
        public void FlashRecorderMeasurement() {
        	try {
				Camera.CurrentCamera_FlashMeasurement();
			} catch (Exception E) {
	        	CompletionHandler.obtainMessage(MESSAGE_OPERATION_ERROR,new Exception(Device.context.getString(R.string.SMeasurementUpdatingError)+E.getMessage())).sendToTarget();        	
			}
        }
        
        public void CheckRecorderMeasurement() {
        	CompletionHandler.obtainMessage(MESSAGE_CHECKRECORDERMEASUREMENT).sendToTarget();
        }
        
        public void RemoveOldRecorderMeasurements() throws IOException {
            	ArrayList<String> MIDs = TVideoRecorderMeasurements.GetMeasurementsIDs();
            	if (MIDs == null)
            		return; //. ->
            	double MinTimestamp = TVideoRecorderMeasurements.GetCurrentTime()-MeasurementConfiguration.LifeTime;
            	for (int I = 0; I < MIDs.size(); I++) {
            		String MeasurementID = MIDs.get(I); 
            		double MTID = Double.parseDouble(MeasurementID);
            		if (MTID < MinTimestamp) 
            			TVideoRecorderMeasurements.DeleteMeasurement(MeasurementID);
            	}
        }
        
        public void run() {
        	UpdateRecorderState();
        	//.
        	if ((TicksCounter % RecorderMeasurementFlashCounter) == 0)
        		FlashRecorderMeasurement();
        	//.
        	if ((TicksCounter % RecorderMeasurementCheckCounter) == 0)
        		CheckRecorderMeasurement();
        	//.
        	try {
				if (((TicksCounter % RecorderMeasurementRemovingCounter) == 0) && (Camera.CurrentCamera_IsSaving()/*remove on saving only*/))
					RemoveOldRecorderMeasurements();
        	}
        	catch (Throwable E) {
        		Throwable EE = new Error("error while removing videorecorder old measurements, "+E.getMessage());
        		Device.Log.WriteError("VideoRecorderModule",EE.getMessage());
        	}
        	//.
        	TicksCounter++;
        }
    }
    
    public void Activate() {
    	SetActive(true);
    	PostUpdateRecorderState();
    }
    
    public void DeActivate() {
    	SetActive(false);
    	PostUpdateRecorderState();
    }

    public void SetMode(short pMode, boolean flGlobal, boolean flPostProcess)
    {
        Mode.SetValue(OleDate.UTCCurrentTimestamp(),pMode);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderModeSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderModeSO)SO).setValue(Mode);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetMode(short pMode) {
        SetMode(pMode,true,true);
    }
    public void SetActive(boolean flActive, boolean flGlobal, boolean flPostProcess)
    {
    	if (flActive == Active.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flActive)
    		V = 1;
        Active.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderActiveFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderActiveFlagSO)SO).setValue(Active);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetActive(boolean flActive) {
    	SetActive(flActive,true,true);
    }
    
    public void CancelRecording(boolean flGlobal, boolean flPostProcess) {
    	SetActive(false,flGlobal,false);
    	SetRecording(false,flGlobal,flPostProcess);
    	//.
    	if (flPostProcess) {
        	//. to avoid connector queue loosing on crash
        	Device.BackupMonitor.BackupImmediate();
        	//.
        	PostUpdateRecorderState();
    	}
    }
    
    public void CancelRecording() {
    	CancelRecording(true,true);
    }
    
    public void SetupRecording() {
    	CancelRecording(true,false);
    	//.
    	SetRecording(true,true,false);
    	SetActive(true);
    	//. to avoid connector queue loosing on crash
    	Device.BackupMonitor.BackupImmediate();
    	//.
    	PostUpdateRecorderState();
    }
    
    public void CancelRecordingLocally() {
    	CancelRecording(false,false);
    }
    
    public void SetupRecordingLocally(short pMode, boolean pflAudio, boolean pflVideo, boolean pflTransmitting, boolean pflSaving) {
    	boolean flGlobal = false;
    	//.
    	CancelRecording(flGlobal,false);
    	//.
    	SetMode(pMode,flGlobal,false);
    	SetAudio(pflAudio,flGlobal,false);
    	SetVideo(pflVideo,flGlobal,false);
    	SetTransmitting(pflTransmitting,flGlobal,false);
    	SetSaving(pflSaving,flGlobal,false);
    	//.
    	SetRecording(true,flGlobal,false);
    	SetActive(true,flGlobal,false);
    	//. to avoid connector queue loosing on crash
    	Device.BackupMonitor.BackupImmediate();
    	//.
    	PostUpdateRecorderState();
    }
    
    public void SetRecording(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Recording.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Recording.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderRecordingFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderRecordingFlagSO)SO).setValue(Recording);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetRecording(boolean flTrue) {
    	SetRecording(flTrue,true,true);
    }
    
    public void SetAudio(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Audio.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Audio.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderAudioFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderAudioFlagSO)SO).setValue(Audio);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetAudio(boolean flTrue) {
    	SetAudio(flTrue,true,true);
    }
    
    public void SetVideo(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Video.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Video.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderVideoFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderVideoFlagSO)SO).setValue(Video);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetVideo(boolean flTrue) {
    	SetVideo(flTrue,true,true);
    }
    
    public void SetTransmitting(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Transmitting.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Transmitting.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderTransmittingFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderTransmittingFlagSO)SO).setValue(Transmitting);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetTransmitting(boolean flTrue) {
    	SetTransmitting(flTrue,true,true);
    }
    
    public void SetSaving(boolean flTrue, boolean flGlobal, boolean flPostProcess)
    {
    	if (flTrue == Saving.BooleanValue())
    		return ; //. ->
    	byte V = 0;
    	if (flTrue)
    		V = 1;
    	Saving.SetValue(OleDate.UTCCurrentTimestamp(),V);
        //.
        if (!flGlobal) {
            if (flPostProcess) 
                try {
                	SaveProfile();
                }
                catch (Exception E) {}
            return; //. ->
        }
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetVideoRecorderSavingFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetVideoRecorderSavingFlagSO)SO).setValue(Saving);
        try
        {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }
    
    public void SetSaving(boolean flTrue) {
    	SetSaving(flTrue,true,true);
    }
    
    public void ShowPropsPanel(Context context) {
    	Intent intent = new Intent(Device.context,TVideoRecorderPropsPanel.class);
    	context.startActivity(intent);
    }
    
	public String Measurements_GetList() {
		return TVideoRecorderMeasurements.GetMeasurementsList();
	}
	
	public void Measurements_Delete(String MeasurementID) throws IOException {
		TVideoRecorderMeasurements.DeleteMeasurement(MeasurementID);
	}
	
	public int Measurement_GetSize(String MeasurementID, boolean flDescriptor, boolean flAudio, boolean flVideo) throws IOException {
		return TVideoRecorderMeasurements.GetMeasurementSize(MeasurementID, flDescriptor,flAudio,flVideo);
	}
	
	public byte[] Measurement_GetData(String MeasurementID, boolean flDescriptor, boolean flAudio, boolean flVideo) throws IOException {
		return TVideoRecorderMeasurements.GetMeasurementData(MeasurementID, flDescriptor,flAudio,flVideo);
	}
	
	public byte[] Measurement_GetDataFragment(String MeasurementID, TMeasurementDescriptor CurrentMeasurement, double StartTimestamp, double FinishTimestamp, boolean flDescriptor, boolean flAudio, boolean flVideo) throws Exception {
		return TVideoRecorderMeasurements.GetMeasurementDataFragment(MeasurementID, CurrentMeasurement, StartTimestamp,FinishTimestamp, flDescriptor,flAudio,flVideo);
	}	
}
