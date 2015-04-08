package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters;

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

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.Network.TServerConnection;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographDataServer.TGeographDataServerClient;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterInfo;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.AV.TAVMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ASTLR.TASTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ECTLR.TECTLRMeter;

public class TSensorsMeters {

    public static class TMeasurementSaver implements Runnable {
    	
    	public static final int CONNECTION_TYPE_PLAIN 		= 0;
    	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
    	
    	public static final int ConnectTimeout = 1000*10; //. seconds
    	
    	private class SavingDataErrorException extends Exception {
    		
			private static final long serialVersionUID = 1L;

			public SavingDataErrorException() {
    			super();
    		}
    	}
    	
    	private TSensorsMeters SensorsMeters;
    	//.
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
    	
    	public TMeasurementSaver(TSensorsMeters pSensorsMeters, String pServerAddress, int pServerPort) {
    		SensorsMeters = pSensorsMeters;
    		//.
    		ServerAddress = pServerAddress;
    		ServerPort = pServerPort;
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
						ProcessSignal.WaitOne();
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
								String[] Measurements = _MeasurementsIDsToProcess.split(",");
								for (int I = 0; I < Measurements.length; I++) 
									if (Measurements[I] != null) {
										String MeasurementID = Measurements[I];
										//.
										try {
											ProcessMeasurement(MeasurementID,TransferBuffer);
										}
										catch (SavingDataErrorException E) {}
									}
							}
							catch (Throwable TE) {
				            	//. log errors
								String S = TE.getMessage();
								if (S == null)
									S = TE.getClass().getName();
			            		SensorsMeters.SensorsModule.Device.Log.WriteError("SensorsMeters.MeasurementSaver",S);
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
			if (MIDs.length() == 0)
				return; //. ->
			synchronized (this) {
				if (MeasurementsIDsToProcess.length() == 0)
					MeasurementsIDsToProcess = MIDs;
				else {
					if (MeasurementsIDsToProcess.equals(MIDs))
						return; //. ->
					MeasurementsIDsToProcess += ","+MIDs;
				}
			}
			ProcessSignal.Set();
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
						throw new IOException(SensorsMeters.SensorsModule.Device.context.getString(R.string.SConnectionTimeoutError)); //. =>
					} catch (ConnectException CE) {
						throw new ConnectException(SensorsMeters.SensorsModule.Device.context.getString(R.string.SNoServerConnection)); //. =>
					} catch (Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.toString();
						throw new Exception(SensorsMeters.SensorsModule.Device.context.getString(R.string.SHTTPConnectionError)+S); //. =>
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
		        byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(TGeographDataServerClient.MESSAGE_DISCONNECT);
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
			byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(TGeographDataServerClient.SERVICE_SETSENSORDATA_V1);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray(SensorsMeters.SensorsModule.Device.UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToLEByteArray((int)SensorsMeters.SensorsModule.Device.idGeographServerObject);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,8);
			BA = TDataConverter.ConvertInt16ToLEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,18, BA.length);
			Buffer_Encrypt(LoginBuffer,10,10,SensorsMeters.SensorsModule.Device.UserPassword);
			//.
			ConnectionOutputStream.write(LoginBuffer);
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor != TGeographDataServerClient.MESSAGE_OK)
				throw new Exception(SensorsMeters.SensorsModule.Device.context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
	    }
	    
		private void ProcessMeasurement(String MeasurementID, byte[] TransferBuffer) throws Exception {
			File[] MeasurementContent = TSensorsModuleMeasurements.GetMeasurementFolderContent(MeasurementID);
			if (MeasurementContent == null)
				return; //. ->
			//.
			boolean flDisconnect = true;
			Connect();
			try {
				Login();
				//.
				byte[] DataIDBA = MeasurementID.getBytes("windows-1251");
				int Descriptor = DataIDBA.length;
				byte[] DecriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
				ConnectionOutputStream.write(DecriptorBA);
				if (Descriptor > 0)  
					ConnectionOutputStream.write(DataIDBA);
				ArrayList<File> ContentFiles = new ArrayList<File>(MeasurementContent.length);
				for (int I = 0; I < MeasurementContent.length; I++)
					if (!MeasurementContent[I].isDirectory()) 
						ContentFiles.add(MeasurementContent[I]);
				//.
				int ContentFilesCount = ContentFiles.size(); 
				DecriptorBA = TDataConverter.ConvertInt32ToLEByteArray(ContentFilesCount);
				ConnectionOutputStream.write(DecriptorBA);
				//.
				for (int I = 0; I < ContentFilesCount; I++) {
					File ContentFile = ContentFiles.get(I);
					//.
					byte[] FileNameBA = ContentFile.getName().getBytes("windows-1251");
					Descriptor = FileNameBA.length;
					DecriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Descriptor);
					ConnectionOutputStream.write(DecriptorBA);
					if (Descriptor > 0)  
						ConnectionOutputStream.write(FileNameBA);
					//.
					long ContentFileSize = ContentFile.length(); 
					byte[] ContentFileSizeBA = TDataConverter.ConvertInt64ToLEByteArray(ContentFileSize);
					ConnectionOutputStream.write(ContentFileSizeBA);
					//.
					if (ContentFileSize > 0) {
						FileInputStream FIS = new FileInputStream(ContentFile);
						try {
							while (ContentFileSize > 0) {
								int BytesRead = FIS.read(TransferBuffer);
								ConnectionOutputStream.write(TransferBuffer,0,BytesRead);
								//.
								ContentFileSize -= BytesRead;
								//.
								if ((ContentFileSize > 0) && (ConnectionInputStream.available() >= 4)) {
									ConnectionInputStream.read(DecriptorBA);
									Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
									if (Descriptor == TGeographDataServerClient.MESSAGE_SAVINGDATAERROR) { 
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
						Descriptor = TDataConverter.ConvertLEByteArrayToInt32(DecriptorBA,0);
						if (Descriptor != TGeographDataServerClient.MESSAGE_OK) {
							flDisconnect = false;
							throw new SavingDataErrorException(); //. =>
						}
					}
				}
				flDisconnect = false;
				//. remove transferred measurement
				TSensorsModuleMeasurements.DeleteMeasurement(MeasurementID);
			}
			finally {
				if (flDisconnect)
					Disconnect();
			}
		}
    }
    
	private static final int OldMeasurementRemovingInterval = 1000*3600*1; //. hours
	
    private static class TOldMeasurementRemovingTask extends TimerTask {
    	
    	private TSensorsMeters SensorsMeters;
    	
    	public TOldMeasurementRemovingTask(TSensorsMeters pSensorsMeters) {
    		SensorsMeters = pSensorsMeters;
    	}
    	
        public void run() {
        	try {
        		SensorsMeters.Measurements_RemoveOld();
        	}
        	catch (Throwable E) {
        		Throwable EE = new Error("error while removing old measurements, "+E.getMessage());
        		SensorsMeters.SensorsModule.Device.Log.WriteError("SensorsMeters",EE.getMessage());
        	}
        }
    }

	private TSensorsModule SensorsModule;
	//.
	public String ProfileFolder;
	//.
	public ArrayList<TSensorMeter> Items = new ArrayList<TSensorMeter>();
	//.
	private TMeasurementSaver MeasurementSaver = null;
	//.
	private Timer OldMeasurementRemoving;
	
	public TSensorsMeters(TSensorsModule pSensorsModule, String pProfileFolder) {
		SensorsModule = pSensorsModule;
		ProfileFolder = pProfileFolder;
	}
	
	public void Destroy() throws Exception {
		Finalize();
	}

	public void Initialize() throws Exception {
		CreateMeters();
		//.
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++)
			Items.get(I).Initialize();
		//.
		synchronized (this) {
			MeasurementSaver = null;
		}
    	//.
		OldMeasurementRemoving = new Timer();
		OldMeasurementRemoving.schedule(new TOldMeasurementRemovingTask(this),OldMeasurementRemovingInterval,OldMeasurementRemovingInterval);
	}
	
	public void Finalize() throws Exception {
		if (OldMeasurementRemoving != null) {
			OldMeasurementRemoving.cancel();
			OldMeasurementRemoving = null;
		}
		//.
		synchronized (this) {
			if (MeasurementSaver != null) {
				MeasurementSaver.Destroy();
				MeasurementSaver = null;
			}
		}
		//.
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++)
			Items.get(I).Destroy();
		Items.clear();
	}

	private void CreateMeters() throws Exception {
		TAVMeter 	AVMeter		= new TAVMeter(SensorsModule, ProfileFolder); 		Items_AddItem(AVMeter);
		TECTLRMeter ECTLRMeter 	= new TECTLRMeter(SensorsModule, ProfileFolder); 	Items_AddItem(ECTLRMeter);
		TASTLRMeter ASTLRMeter 	= new TASTLRMeter(SensorsModule, ProfileFolder); 	Items_AddItem(ASTLRMeter);
	}
	
	private void Items_AddItem(TSensorMeter Meter) {
		Items.add(Meter);
	}
	
	public TSensorMeter Items_GetItem(String MeterID) {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			if (Meter.Descriptor.ID.equals(MeterID))
				return Meter; //. ->
		}
		return null;
	}
	
	public String Items_GetList(int Version) {
		switch (Version) {
		
		case 1:
			StringBuilder SB = new StringBuilder();
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TSensorMeter Meter = Items.get(I);
				SB.append(Meter.Descriptor.ID);
				SB.append(","+Meter.Descriptor.TypeID);
				SB.append(","+Meter.Descriptor.ContainerTypeID);
				SB.append(","+Meter.Descriptor.Name);
				SB.append(","+Meter.Descriptor.Info);
				SB.append(","+Meter.Descriptor.Configuration);
				SB.append(","+Meter.Descriptor.Parameters);
				SB.append(","+(Meter.IsEnabled() ? "1" : "0"));
				SB.append(","+(Meter.IsActive() ? "1" : "0"));
				SB.append(","+Integer.toString(Meter.GetStatus()));
				if (I < (Cnt-1))
					SB.append(";");
			}
			return SB.toString(); //. >
			
		default:
			return null; //. ->
		}
	}

	public TSensorMeterInfo[] Items_GetList() {
		int Cnt = Items.size();
		TSensorMeterInfo[] Result = new TSensorMeterInfo[Cnt];
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			//.
			TSensorMeterDescriptor Descriptor = new TSensorMeterDescriptor();
			//.
			Descriptor.ID = Meter.Descriptor.ID;
			//.
			Descriptor.TypeID = Meter.Descriptor.TypeID;
			Descriptor.ContainerTypeID = Meter.Descriptor.ContainerTypeID;
			//.
			Descriptor.Name = Meter.Descriptor.Name;
			Descriptor.Info = Meter.Descriptor.Info;
			//.
			Descriptor.Configuration = Meter.Descriptor.Configuration;
			Descriptor.Parameters = Meter.Descriptor.Parameters;
			//.
			Result[I] = new TSensorMeterInfo(Descriptor, Meter.IsEnabled(), Meter.IsActive(), Meter.GetStatus());
		}
		return Result;
	}
	
	public void Items_ValidateActivity(String[] MeterIDs) throws Exception {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			boolean flFound = false;
			int Cnt1 = MeterIDs.length;
			for (int J = 0; J < Cnt1; J++)
				if (MeterIDs[J].equals(Meter.Descriptor.ID)) {
					flFound = true;
					break; //. >
				}
			if (flFound) {
				if (!Meter.IsActive())
					Meter.SetActive(true);
			}
			else {
				if (Meter.IsActive())
					Meter.SetActive(false);
			}
		}
	}

	public synchronized TMeasurementSaver Measurements_GetSaver() {
		if (MeasurementSaver != null)
			return MeasurementSaver; //. ->
		//.
		if ((SensorsModule.Device.ConnectorModule != null) && SensorsModule.Device.ConnectorModule.flProcessing) {
			String 	ServerAddress = SensorsModule.Device.ConnectorModule.GetGeographDataServerAddress();
			int 	ServerPort = SensorsModule.Device.ConnectorModule.GetGeographDataServerPort();
			//.
			MeasurementSaver = new TMeasurementSaver(this, ServerAddress,ServerPort);
			//.
			return MeasurementSaver; //. ->
		}
		else
			return null; //. ->
		
	}
	
	private void Measurements_RemoveOld() throws Exception {
    	ArrayList<String> MIDs = TSensorsModuleMeasurements.GetMeasurementsIDs();
    	if (MIDs == null)
    		return; //. ->
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			Meter.Measurements_RemoveOld(MIDs);
		}
	}	
}
