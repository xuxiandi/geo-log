package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.geoscope.Classes.MultiThreading.TCancelableThread;

public class TLANConnectionRepeater {

	public static final int GeographProxyServerDefaultPort = 2010;
	
	public static final int ServerConnectTimeout = 30000; //. ms
	public static final int ServerReadWriteTimeout = 30000; //. ms
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
	
	protected int ConnectionType;
	//.
	protected String 	Address;
	protected int 		Port;
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
	protected String UserAccessKey;
	//.
	protected TLANConnectionExceptionHandler ExceptionHandler;
	//.
	protected TLANConnectionStartHandler	StartHandler;
	protected TLANConnectionStopHandler 	StopHandler;
	//.
	protected TLANConnectionOnBytesTransmiteHandler OnSourceBytesTransmiteHandler;
	protected TLANConnectionOnBytesTransmiteHandler OnDestinationBytesTransmiteHandler;
	//.
	private int ConnectionsCount;

	public TLANConnectionRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, String pUserAccessKey, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionStartHandler pStartHandler, TLANConnectionStopHandler pStopHandler, TLANConnectionOnBytesTransmiteHandler pOnSourceBytesTransmiteHandler, TLANConnectionOnBytesTransmiteHandler pOnDestinationBytesTransmiteHandler) throws Exception {
		ConnectionType = pConnectionType;
		//.
		Address = pAddress;
		Port = pPort;
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
		UserAccessKey = pUserAccessKey; 
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
	
	public TLANConnectionRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionStartHandler pStartHandler, TLANConnectionStopHandler pStopHandler, TLANConnectionOnBytesTransmiteHandler pOnSourceBytesTransmiteHandler, TLANConnectionOnBytesTransmiteHandler pOnDestinationBytesTransmiteHandler) throws Exception {
		this(pConnectionType, pAddress,pPort, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, null, pExceptionHandler, pStartHandler, pStopHandler, pOnSourceBytesTransmiteHandler, pOnDestinationBytesTransmiteHandler);
	}
	
	public TLANConnectionRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, String pUserAccessKey, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionStartHandler pStartHandler, TLANConnectionStopHandler pStopHandler) throws Exception {
		this(pConnectionType, pAddress,pPort, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, pUserAccessKey, pExceptionHandler, pStartHandler, pStopHandler, null,null);	
	}
	
	public TLANConnectionRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionStartHandler pStartHandler, TLANConnectionStopHandler pStopHandler) throws Exception {
		this(pConnectionType, pAddress,pPort, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, null, pExceptionHandler, pStartHandler, pStopHandler, null,null);	
	}
	
	public void Destroy() throws IOException {
		if (RepeaterServer != null) {
			RepeaterServer.Destroy();
			RepeaterServer = null;
		}
	}
	
	public class TRepeaterServer extends TCancelableThread {
		
		public static final int ListeningTimeout = 1000; //. ms
		
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
			Cancel();
			ListeningSocket.close();
			Wait();
			_Thread = null;
			ListeningSocket = null;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			try {
				ListeningSocket.setSoTimeout(ListeningTimeout);
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
						_ClientSessions.get(I).Finalize();
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
		
		public void Finalize() throws IOException {
			Cancel();
			ClientSocket.close();  //. cancel socket blocking reading
			Wait();
		}
		
		@Override
		public void run() {
			try {
				try {
					synchronized (TLANConnectionRepeater.this) {
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
								TLANConnectionClient LANConnectionClient = new TLANConnectionClient(TLANConnectionRepeater.this, OS);
								try {
									byte[] TransferBuffer = new byte[TLANConnectionRepeater.TransferBufferSize];
									int ActualSize;
									switch (TLANConnectionRepeater.this.ConnectionType) {
									
									case LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL:
										while ((!Canceller.flCancel) && LANConnectionClient.flRunning) {
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
										    	LANConnectionClient.ServerSocketOutputStream.write(TransferBuffer,0,ActualSize);
										    	//.
												LANConnectionClient.ServerSocketOutputStream.flush();
										    }
										}
										break; //. >
										
									case LANConnectionRepeaterDefines.CONNECTIONTYPE_PACKETTED:
										byte[] PacketSizeBA = new byte[4];
										int PacketSize;
										while ((!Canceller.flCancel) && LANConnectionClient.flRunning) {
											try {
								                ActualSize = IS.read(PacketSizeBA,0,PacketSizeBA.length);
										    	if (ActualSize == 0)
										    		break; //. > connection is closed
										    		else 
												    	if (ActualSize < 0) {
													    	if (ActualSize == -1)
													    		break; //. > stream EOF, connection is closed
													    	else
													    		throw new IOException("error of reading server socket data descriptor, RC: "+Integer.toString(ActualSize)); //. =>
												    	}
											}
											catch (SocketTimeoutException E) {
												continue; //. ^
											}
											if (ActualSize != PacketSizeBA.length)
												throw new IOException("wrong data descriptor"); //. =>
											PacketSize = (PacketSizeBA[3] << 24)+((PacketSizeBA[2] & 0xFF) << 16)+((PacketSizeBA[1] & 0xFF) << 8)+(PacketSizeBA[0] & 0xFF);
											if (PacketSize > 0) {
												if (PacketSize > TransferBuffer.length)
													TransferBuffer = new byte[PacketSize];
												ActualSize = InputStream_Read(IS,TransferBuffer,PacketSize);	
										    	if (ActualSize == 0)
										    		break; //. > connection is closed
										    		else 
												    	if (ActualSize < 0) {
													    	if (ActualSize == -1)
													    		break; //. > stream EOF, connection is closed
													    	else
													    		throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
												    	}
											}
											//.
										    if (OnDestinationBytesTransmiteHandler != null)
										    	OnDestinationBytesTransmiteHandler.DoOnBytesTransmite(TransferBuffer,PacketSize);
										    //.
										    LANConnectionClient.ServerSocketOutputStream.write(PacketSizeBA,0,PacketSizeBA.length);
											if (PacketSize > 0) 
												LANConnectionClient.ServerSocketOutputStream.write(TransferBuffer,0,PacketSize);
											//.
											LANConnectionClient.ServerSocketOutputStream.flush();
										}
										break; //. >
									}
								}
								finally {
									LANConnectionClient.Destroy();
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
						synchronized (TLANConnectionRepeater.this) {
							ConnectionsCount--;
						}
					}
				}
				finally {
					synchronized (Server.ClientSessions) {
						Server.ClientSessions.remove(this);
					}
					//.
					ClientSocket.close();
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
