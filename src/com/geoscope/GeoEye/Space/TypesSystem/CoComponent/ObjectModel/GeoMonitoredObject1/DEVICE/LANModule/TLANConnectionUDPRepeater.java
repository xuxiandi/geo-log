package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.LANModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

import com.geoscope.Classes.MultiThreading.TCancelableThread;

public class TLANConnectionUDPRepeater {

	public static final int ServerReadWriteTimeout = 30000; //. ms
	public static final int TransferBufferSize = 1500; //. >= MTU
	
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
	protected long 		UserID;
	protected String 	UserPassword;
	protected int idGeographServerObject;
	//.
	protected String AddressData;
	//.
	protected String UserAccessKey;
	//.
	protected TLANConnectionExceptionHandler ExceptionHandler;
	//.
	protected TLANConnectionUDPStartHandler	StartHandler;
	protected TLANConnectionUDPStopHandler 	StopHandler;
	//.
	protected TLANConnectionOnBytesTransmiteHandler OnSourceBytesTransmiteHandler;
	protected TLANConnectionOnBytesTransmiteHandler OnDestinationBytesTransmiteHandler;
	//.
	private int ConnectionsCount;

	public TLANConnectionUDPRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidGeographServerObject, String pAddressData, String pUserAccessKey, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionUDPStartHandler pStartHandler, TLANConnectionUDPStopHandler pStopHandler, TLANConnectionOnBytesTransmiteHandler pOnSourceBytesTransmiteHandler, TLANConnectionOnBytesTransmiteHandler pOnDestinationBytesTransmiteHandler) throws Exception {
		ConnectionType = pConnectionType;
		//.
		Address = pAddress;
		Port = pPort;
		//.
		LocalPort = pLocalPort;
		//.
		ServerAddress = pServerAddress;
		ServerPort = pServerPort;
		//.
		UserID = pUserID;
		UserPassword = pUserPassword;
		//.
		idGeographServerObject = pidGeographServerObject;
		//.
		AddressData = pAddressData;
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
	
	public TLANConnectionUDPRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionUDPStartHandler pStartHandler, TLANConnectionUDPStopHandler pStopHandler, TLANConnectionOnBytesTransmiteHandler pOnSourceBytesTransmiteHandler, TLANConnectionOnBytesTransmiteHandler pOnDestinationBytesTransmiteHandler) throws Exception {
		this(pConnectionType, pAddress,pPort, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, null, null, pExceptionHandler, pStartHandler, pStopHandler, pOnSourceBytesTransmiteHandler, pOnDestinationBytesTransmiteHandler);
	}
	
	public TLANConnectionUDPRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidGeographServerObject, String pAddressData, String pUserAccessKey, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionUDPStartHandler pStartHandler, TLANConnectionUDPStopHandler pStopHandler) throws Exception {
		this(pConnectionType, pAddress,pPort, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, pAddressData, pUserAccessKey, pExceptionHandler, pStartHandler, pStopHandler, null,null);	
	}
	
	public TLANConnectionUDPRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionUDPStartHandler pStartHandler, TLANConnectionUDPStopHandler pStopHandler) throws Exception {
		this(pConnectionType, pAddress,pPort, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, null, null, pExceptionHandler, pStartHandler, pStopHandler, null,null);	
	}
	
	public void Destroy() throws IOException, InterruptedException {
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
		
		public void Destroy() throws IOException, InterruptedException {
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
		
		public void Stop() throws IOException, InterruptedException {
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
		
		public void Finalize() throws IOException, InterruptedException {
			CancelByCanceller();
			//.
			ClientSocket.close();  //. cancel socket blocking reading
			//.
			Wait();
		}
		
		@Override
		public void run() {
			try {
				try {
					synchronized (TLANConnectionUDPRepeater.this) {
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
								TLANConnectionUDPClient LANConnectionClient = new TLANConnectionUDPClient(TLANConnectionUDPRepeater.this, OS);
								try {
									byte[] TransmitePacketBuffer = new byte[TLANConnectionUDPClient.MTU_MAX_SIZE];
									DatagramPacket TransmitePacket = new DatagramPacket(TransmitePacketBuffer,TransmitePacketBuffer.length,InetAddress.getByName(LANConnectionClient.SourceUDPAddress),LANConnectionClient.SourceUDPPort);
									//.
									int ActualSize;
									byte[] PacketSizeBA = new byte[4];
									int PacketSize;
									switch (TLANConnectionUDPRepeater.this.ConnectionType) {
									
									case LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL:
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
												if (PacketSize > TransmitePacketBuffer.length)
													PacketSize = TransmitePacketBuffer.length;
												ActualSize = InputStream_Read(IS,TransmitePacketBuffer,PacketSize);	
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
										    	OnDestinationBytesTransmiteHandler.DoOnBytesTransmite(TransmitePacketBuffer,PacketSize);
										    //.
											if (PacketSize > 0) { 
												TransmitePacket.setLength(PacketSize);
												LANConnectionClient.DestinationUDPSocket.send(TransmitePacket);
											}
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
						synchronized (TLANConnectionUDPRepeater.this) {
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
