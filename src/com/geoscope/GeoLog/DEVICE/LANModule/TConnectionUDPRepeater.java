package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.geoscope.GeoLog.DEVICE.LANModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;

public class TConnectionUDPRepeater extends TCancelableThread {

	public static final int MTU_MAX_SIZE = 1500;
	
	public static final int UDPTimeout = 5000; //. ms
	
	private static int UDPLocalPortMin = 2014;
	private static int UDPLocalPortMax = 9999;
	private static int UDPLocalPort = UDPLocalPortMin;
	
	public static synchronized int GetUDPLocalPort() {
		while (true) {
			try {
				DatagramSocket Socket = new DatagramSocket(UDPLocalPort,InetAddress.getByName("0.0.0.0"));
				Socket.close();
				int Result = UDPLocalPort;
				UDPLocalPort++;
				if (UDPLocalPort > UDPLocalPortMax)
					UDPLocalPort = UDPLocalPortMin;
				return Result; //. ->
			}
			catch (Exception E) {
			}
		}
	}
	
	public static class TInternetUDPEndpoint {
		
		public static final int ByteArraySize = 6;
		
		public String 	Address;
		public int 		Port;
		//.
		public DatagramSocket Socket = null;
		
		public TInternetUDPEndpoint(String pAddress, int pPort) throws SocketException, UnknownHostException {
			Address = pAddress;
			Port = pPort;
		}
		
		public TInternetUDPEndpoint(byte[] BA, int Idx) {
			FromByteArray(BA, Idx);
		}
		
		public void Destroy() {
		}
		
		public int FromByteArray(byte[] BA, int Idx) {
			StringBuilder SB = new StringBuilder();
			SB.append(BA[Idx] & 0xFF).append(".").append(BA[Idx+1] & 0xFF).append(".").append(BA[Idx+2] & 0xFF).append(".").append(BA[Idx+3] & 0xFF); Idx += 4;
			Address = SB.toString();
			//.
			Port = (((BA[Idx] & 0xFF) << 8)+(BA[Idx+1] & 0xFF)); Idx += 2;
			//.
			return Idx;
		}
	}
	
    public static final List<TConnectionUDPRepeater> Repeaters = Collections.synchronizedList(new ArrayList<TConnectionUDPRepeater>());
    //.
    public static class TRepeatersStatistic {
    	
    	public int Connections = 0;
    	public int LocalConnections = 0;
    }
    
    public static TRepeatersStatistic Repeaters_GetStatistics() {
    	TRepeatersStatistic Result = new TRepeatersStatistic();
    	synchronized (Repeaters) {
    		Result.Connections = Repeaters.size();
    		for (int I = 0; I < Repeaters.size(); I++) {
    			if (Repeaters.get(I) instanceof TLANLocalVirtualConnectionUDPRepeater)
    				Result.LocalConnections++;
    		}
		}
    	return Result;
    }
	//.
	public TLANModule LANModule;
	//.
	protected String 	ServerAddress;
	protected int 		ServerPort;
	//.
	protected String 			DestinationUDPAddress;
	protected int 				DestinationUDPPort;
    private TAutoResetEvent 	DestinationConnectionResultSignal = new TAutoResetEvent();
    private Exception			DestinationConnectionResult = null;
	//.
	public int 					SourceUDPLocalPort;
	public String 				SourceUDPAddress = null;
	public int 					SourceUDPPort;
	protected DatagramSocket	SourceUDPSocket = null;
	//.
	public String AddressData; 
    //.
	///? private Date LastActivityTimestamp;
    //.
	public int	ConnectionID = 0;
    //.
    public String UserAccessKey = "";
	
	public TConnectionUDPRepeater(TLANModule pLANModule, String pServerAddress, int pServerPort, String pDestinationUDPAddress, int pDestinationUDPPort, String pAddressData, int pConnectionID, String pUserAccessKey) {
		LANModule = pLANModule;
		//.
		ServerAddress = pServerAddress;
		ServerPort = pServerPort;
		//.
		DestinationUDPAddress = pDestinationUDPAddress;
		DestinationUDPPort = pDestinationUDPPort;
		//.
		AddressData = pAddressData; 
		//.
		ConnectionID = pConnectionID;
		//.
		UserAccessKey = pUserAccessKey;
		//.
		Repeaters.add(this);
		//.
		_Thread = new Thread(this);
	}
	
	public void Destroy() {
		Cancel();
	}
	
	public boolean CheckUserAccessKey(String pUserAccessKey) {
		if (pUserAccessKey == null)
			return true; //. ->
		return (UserAccessKey.equals(pUserAccessKey));
	}
	
	protected void Start() {
		_Thread.start();
	}
	
	protected void ConnectSource() throws IOException {		
	}

	protected void DisconnectSource() throws IOException {
	}
	
	@SuppressWarnings("serial")
	private static class TSuccessException extends Exception {
		public TSuccessException() {
		}
	}
	
	private static TSuccessException Success = new TSuccessException();
	
	protected void ConnectDestination() throws Exception {
		Exception _ConnectionResult = null;
		try {
			SourceUDPLocalPort = TConnectionUDPRepeater.GetUDPLocalPort();
			TUDPEchoServerClient UDPEchoServerClient = new TUDPEchoServerClient(ServerAddress,ServerPort);
			TInternetUDPEndpoint UDPEndpoint = UDPEchoServerClient.GetInternetEndpoint(SourceUDPLocalPort);
			if (UDPEndpoint == null)
				throw new IOException("could not get an echo from the UDP server for a source UDP endpoint"); //. =>
			SourceUDPAddress = UDPEndpoint.Address;
			SourceUDPPort = UDPEndpoint.Port;
			SourceUDPSocket = UDPEndpoint.Socket;
			SourceUDPSocket.setSoTimeout(UDPTimeout);
			//. ping
			byte[] PingBuffer = new byte[1];
			DatagramPacket PingPacket = new DatagramPacket(PingBuffer,PingBuffer.length,InetAddress.getByName(DestinationUDPAddress),DestinationUDPPort);
			SourceUDPSocket.send(PingPacket);
		}
		catch (Exception E) {
			_ConnectionResult = E;
		}
		if (_ConnectionResult == null)
			DestinationConnectionResult = Success;
		else
			DestinationConnectionResult = _ConnectionResult;
		DestinationConnectionResultSignal.Set();
		if (_ConnectionResult != null)
			throw _ConnectionResult; //. =>
	}
	
	protected void DisconnectDestination() throws IOException {
		SourceUDPSocket.close();
	}
	
	public boolean WaitForDestinationConnectionResult(int Timeout) throws Exception {
		DestinationConnectionResultSignal.WaitOne(Timeout);
		if (DestinationConnectionResult == Success)
			return true; //. ->
		if (DestinationConnectionResult == null)
			return false; //. ->
		//.
		throw DestinationConnectionResult; //. =>
	}
	
	public boolean ReceivingIsAvaiable() {
		return true;
	}
	
	public boolean TransmittingIsAvaiable() {
		return true;
	}
	
	public void DoReceiving(Thread ReceivingThread) throws IOException {
	}
	
	public void DoTransmitting(Thread TransmittingThread) throws IOException {
	}
	
	private class TReceiving implements Runnable {

		public boolean flRunning = true;
		
		public void run() {
			try {
				try {
					DoReceiving(_Thread);
				}
				finally {
					flRunning = false;
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		LANModule.Device.Log.WriteError("LANModule.LANConnectionUDPRepeater.Receiving",S,TE.getStackTrace());
			}
		}		
	}
	
	private class TTransmitting implements Runnable {

		public boolean flRunning = true;
		
		public void run() {
			try {
				try {
					DoTransmitting(_Thread);
				}
				finally {
					flRunning = false;
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		LANModule.Device.Log.WriteError("LANModule.LANConnectionUDPRepeater.Transmitting",S,TE.getStackTrace());
			}
		}		
	}	
	
	@Override
	public void run() {
		try {
			ConnectSource();
			try {
				ConnectDestination();
				try {
					TReceiving Receiving = null;
					Thread ReceivingThread = null;
					if (ReceivingIsAvaiable()) {
						Receiving = new TReceiving();
						ReceivingThread = new Thread(Receiving);
					}
					try {
						if (ReceivingThread != null) {
							ReceivingThread.setPriority(Thread.MAX_PRIORITY);
							ReceivingThread.start();
						}
						//.
						TTransmitting Transmitting = null;
						Thread TransmittingThread = null;
						if (TransmittingIsAvaiable()) {
							Transmitting = new TTransmitting();
							TransmittingThread = new Thread(Transmitting);
						}
						try {
							if (TransmittingThread != null) {
								TransmittingThread.setPriority(Thread.MAX_PRIORITY);
								TransmittingThread.start();
							}
							//. working in Receiving&Transmitting threads
							while (!Canceller.flCancel) {
								Thread.sleep(100);
								if (((Receiving != null) && (!Receiving.flRunning)) || ((Transmitting != null) && (!Transmitting.flRunning)))
									break; //. >
							}
						}
						finally {
							if (TransmittingThread != null) 
								TransmittingThread.interrupt();
						}
					}
					finally {
						if (ReceivingThread != null) 
							ReceivingThread.interrupt();
					}
				}
				finally {
					DisconnectDestination();
				}
			}
			finally {
				DisconnectSource();
			}
		}
		catch (InterruptedException E) {
		}
		catch (Throwable TE) {
        	//. log errors
    		LANModule.Device.Log.WriteError("LANModule.LANConnectionUDPRepeater",TE.getMessage());
        	if (!(TE instanceof Exception))
        		TDEVICEModule.Log_WriteCriticalError(TE);
		}
		//.
		Repeaters.remove(this);
	}
	
	public synchronized boolean IsIdle() {
		return false;
	}
}
