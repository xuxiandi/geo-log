package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.geoscope.GeoLog.Utils.TCancelableThread;

public class TDeviceConnectionRepeater {

	public static final int GeographProxyServerDefaultPort = 2010;
	
	public static final int ServerReadWriteTimeout = 1000; //. ms
	public static final int TransferBufferSize = 1024*1024;
	
	public static class EServerPortBindingException extends IOException {
		
		private static final long serialVersionUID = 1L;

		public EServerPortBindingException() {
		}
	}
	
    public static int InputStream_Read(InputStream Connection, byte[] Data, int DataSize) throws IOException {
        int SummarySize = 0;
        int ReadSize;
        int Size;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = Connection.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	return Size; //. ->
            SummarySize += Size;
        }
        return SummarySize;
    }
	
	private int MaxListeningPort = 65535;
	
	protected String CUAL;
	//.
	private int LocalPort;
	//.
	private TRepeaterServer RepeaterServer;
	//.
	protected String 	ServerAddress;
	protected int 		ServerPort; 
	protected int 		SecureServerPortShift = 2;
    protected int		SecureServerPort() {
    	return (ServerPort+SecureServerPortShift);
    }
	//.
	protected int 		UserID;
	protected String 	UserPassword;
	protected int idGeographServerObject;
	//.
	protected TLANConnectionExceptionHandler ExceptionHandler;
	//.
	protected TDeviceConnectionStartHandler	StartHandler;
	protected TDeviceConnectionStopHandler 	StopHandler;
	//.
	protected TLANConnectionOnBytesTransmiteHandler OnSourceBytesTransmiteHandler;
	protected TLANConnectionOnBytesTransmiteHandler OnDestinationBytesTransmiteHandler;
	//.
	private int ConnectionsCount;

	public TDeviceConnectionRepeater(String pCUAL, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TDeviceConnectionStartHandler pStartHandler, TDeviceConnectionStopHandler pStopHandler, TLANConnectionOnBytesTransmiteHandler pOnSourceBytesTransmiteHandler, TLANConnectionOnBytesTransmiteHandler pOnDestinationBytesTransmiteHandler) throws Exception {
		CUAL = pCUAL;
		//.
		LocalPort = pLocalPort;
		//.
		ServerAddress = pServerAddress;
		ServerPort = pServerPort;
		if (ServerPort == 0)
			ServerPort = GeographProxyServerDefaultPort;
		//.
		UserID = pUserID;
		UserPassword = pUserPassword;
		//.
		idGeographServerObject = pidGeographServerObject;
		//.
	  	ExceptionHandler = pExceptionHandler; 
	  	StartHandler = pStartHandler;
		StopHandler = pStopHandler;
		//.
		OnSourceBytesTransmiteHandler = pOnSourceBytesTransmiteHandler;
		OnDestinationBytesTransmiteHandler = pOnDestinationBytesTransmiteHandler;
		//.
		ConnectionsCount = 0;
		//.
		RepeaterServer = new TRepeaterServer();
		try {
			while (true) {
				try {
					RepeaterServer.Start(LocalPort);
					break; //. >
				}
				catch (EServerPortBindingException ESPBE) {
				      LocalPort++;
				      if (LocalPort > MaxListeningPort) 
				    	  throw ESPBE; //. =>
				}
			}
		}
		catch (Exception E) {
			Destroy();
			throw E; //. =>
		}
	}
	
	public TDeviceConnectionRepeater(String pCUAL, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TDeviceConnectionStartHandler pStartHandler, TDeviceConnectionStopHandler pStopHandler) throws Exception {
		this(pCUAL, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, pExceptionHandler, pStartHandler, pStopHandler, null,null);	
	}
	
	public void Destroy() throws IOException {
		if (RepeaterServer != null) {
			RepeaterServer.Destroy();
			RepeaterServer = null;
		}
	}
	
	public class TRepeaterServer extends TCancelableThread {
		
		private ServerSocket ListeningSocket = null;
		//.
		private ArrayList<TClientSession> ClientSessions = new ArrayList<TClientSession>();
		
		public TRepeaterServer() {
		}
		
		public void Destroy() throws IOException {
			if (ListeningSocket != null) 
				Stop();
		}
		
		public void Start(int Port) throws IOException {
			try {
				ListeningSocket = new ServerSocket(Port,100);
			}
			catch (IOException IOE) {
				throw new EServerPortBindingException(); //. =>
			}
			//.
    		_Thread = new Thread(this);
    		_Thread.start();
		}
		
		public void Stop() throws IOException {
			CancelAndWait();
			_Thread = null;
			//.
			ListeningSocket.close();
			ListeningSocket = null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				ListeningSocket.setSoTimeout(ServerReadWriteTimeout);
				//.
				try {
					while (!Canceller.flCancel) {
						Socket ClientSocket;
						try {
							ClientSocket = ListeningSocket.accept();
							//. start new session
							new TClientSession(this,ClientSocket);
						}
						catch (SocketTimeoutException E) {
						}
					}
				}
				finally {
					ArrayList<TClientSession> _ClientSessions;
					synchronized (ClientSessions) {
						_ClientSessions = (ArrayList<TClientSession>)ClientSessions.clone();
					}
					for (int I = 0; I < _ClientSessions.size(); I++) 
						_ClientSessions.get(I).CancelAndWait();
				}
			}
			catch (Throwable T) {
				if (!Canceller.flCancel) {
					if (ExceptionHandler != null)
						ExceptionHandler.DoOnException(T);
				}
			}
		}
	}
	
	public class TClientSession extends TCancelableThread {
		
		private TRepeaterServer Server;
		//.
		private Socket ClientSocket;
		
		public TClientSession(TRepeaterServer pServer, Socket pClientSocket) {
			Server = pServer;
			ClientSocket = pClientSocket;
			//.
			synchronized (Server.ClientSessions) {
				Server.ClientSessions.add(this);
			}
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();

		}
		
		@Override
		public void run() {
			try {
				try {
					synchronized (TDeviceConnectionRepeater.this) {
						ConnectionsCount++;
					}
					try {
						ClientSocket.setSoTimeout(ServerReadWriteTimeout);
						ClientSocket.setTcpNoDelay(true);
						//.
						InputStream IS = ClientSocket.getInputStream();
						try {
							OutputStream OS = ClientSocket.getOutputStream();
							try {
								TDeviceConnectionClient DeviceConnectionClient = new TDeviceConnectionClient(TDeviceConnectionRepeater.this, OS);
								try {
									byte[] TransferBuffer = new byte[TDeviceConnectionRepeater.TransferBufferSize];
									int ActualSize;
									while ((!Canceller.flCancel) && DeviceConnectionClient.flRunning) {
										try {
										    ActualSize = IS.read(TransferBuffer,0,TransferBuffer.length);
									    	if (ActualSize == 0)
									    		break; //. > connection is closed
									    		else 
											    	if (ActualSize < 0) {
												    	if (ActualSize == -1)
												    		break; //. > stream EOF, connection is closed
												    	else
												    		throw new IOException("error of reading Repeater socket data, RC: "+Integer.toString(ActualSize)); //. =>
											    	}
										}
										catch (SocketTimeoutException E) {
											continue; //. ^
										}
										//.
									    if (ActualSize > 0) {
									    	if (OnDestinationBytesTransmiteHandler != null)
									    		OnDestinationBytesTransmiteHandler.DoOnBytesTransmite(TransferBuffer,ActualSize);
									    	//.
									    	DeviceConnectionClient.ServerSocketOutputStream.write(TransferBuffer,0,ActualSize);
									    	//.
									    	DeviceConnectionClient.ServerSocketOutputStream.flush();
									    }
									}
								}
								finally {
									DeviceConnectionClient.Destroy();
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
						synchronized (TDeviceConnectionRepeater.this) {
							ConnectionsCount--;
						}
					}
				}
				finally {
					ClientSocket.close();
					//.
					synchronized (Server.ClientSessions) {
						Server.ClientSessions.remove(this);
					}
				}
			}
			catch (Throwable T) {
				if (!Canceller.flCancel) {
					if (ExceptionHandler != null)
						ExceptionHandler.DoOnException(T);
				}
			}
		}
	}
	
	public int GetPort() {
		return LocalPort;
	}
	
	public synchronized int GetConnectionsCount() {
		return ConnectionsCount;
	}
}
