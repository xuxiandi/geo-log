package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.geoscope.GeoLog.Utils.TCancelableThread;

public class TLANConnectionRepeater {

	public static final int GeographServerDefaultPort = 2010;
	
	public static final int ServerReadWriteTimeout = 1000; //. ms
	public static final int TransferBufferSize = 1024*1024;
	
	public static class EServerPortBindingException extends IOException {
		
		private static final long serialVersionUID = 1L;

		public EServerPortBindingException() {
		}
	}
	
    protected static int InputStream_Read(InputStream Connection, byte[] Data, int DataSize) throws IOException {
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
	protected int 		UserID;
	protected String 	UserPassword;
	protected int idGeographServerObject;
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

	public TLANConnectionRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionStartHandler pStartHandler, TLANConnectionStopHandler pStopHandler, TLANConnectionOnBytesTransmiteHandler pOnSourceBytesTransmiteHandler, TLANConnectionOnBytesTransmiteHandler pOnDestinationBytesTransmiteHandler) throws Exception {
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
			ServerPort = GeographServerDefaultPort;
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
	
	public TLANConnectionRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionStartHandler pStartHandler, TLANConnectionStopHandler pStopHandler) throws Exception {
		this(pConnectionType, pAddress,pPort, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, pExceptionHandler, pStartHandler, pStopHandler, null,null);	
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
				if (ExceptionHandler != null)
					ExceptionHandler.DoOnException(T);
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
					synchronized (TLANConnectionRepeater.this) {
						ConnectionsCount++;
					}
					try {
						ClientSocket.setSoTimeout(ServerReadWriteTimeout);
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
												    	if (ActualSize < 0)
											    			throw new IOException("error of reading Repeater socket data, RC: "+Integer.toString(ActualSize)); //. =>
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
												    	if (ActualSize < 0)
											    			throw new IOException("error of reading server socket data descriptor, RC: "+Integer.toString(ActualSize)); //. =>
											}
											catch (SocketTimeoutException E) {
												continue; //. ^
											}
											if (ActualSize != PacketSizeBA.length)
												throw new IOException("wrong data descriptor"); //. =>
											PacketSize = (PacketSizeBA[3] << 24)+((PacketSizeBA[2] & 0xFF) << 16)+((PacketSizeBA[1] & 0xFF) << 8)+(PacketSizeBA[0] & 0xFF);
											if (PacketSize > 0) {
												ActualSize = InputStream_Read(IS,TransferBuffer,PacketSize);	
										    	if (ActualSize == 0)
										    		break; //. > connection is closed
										    		else 
												    	if (ActualSize < 0)
											    			throw new IOException("unexpected error of reading server socket data, RC: "+Integer.toString(ActualSize)); //. =>
											}
											//.
										    if (OnDestinationBytesTransmiteHandler != null)
										    	OnDestinationBytesTransmiteHandler.DoOnBytesTransmite(TransferBuffer,PacketSize);
										    //.
										    LANConnectionClient.ServerSocketOutputStream.write(PacketSizeBA,0,PacketSizeBA.length);
											if (PacketSize > 0)
												LANConnectionClient.ServerSocketOutputStream.write(TransferBuffer,0,PacketSize);
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
					ClientSocket.close();
					//.
					synchronized (Server.ClientSessions) {
						Server.ClientSessions.remove(this);
					}
				}
			}
			catch (Throwable T) {
				if (ExceptionHandler != null)
					ExceptionHandler.DoOnException(T);
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
