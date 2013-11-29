package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Random;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient.TGetInternetEndpointResult;
import com.geoscope.GeoLog.Utils.TCancelableThread;

public class TLANConnectionUDPClient extends TCancelableThread {

	public static final int MTU_MAX_SIZE = 1500;
	
	public static final int UDPTimeout = 5000; //. ms
	
	private static Random rnd = new Random();
	
	private TLANConnectionUDPRepeater Repeater; 
	//.
	protected int 				DestinationUDPLocalPort;
	public String 				DestinationUDPAddress = null;
	public int 					DestinationUDPPort;
	protected int 				DestinationUDPSocketProxyType;
	protected DatagramSocket	DestinationUDPSocket = null;
	//.
	public String 	SourceUDPAddress = null;
	public int 		SourceUDPPort;
	public int 		SourceUDPProxyType;
	//.
	private int ConnectionID = 0;
	//.
	private OutputStream DestinationSocketOutputStream;

	public boolean flActive = false;
	public boolean flRunning = false;

	public TLANConnectionUDPClient(TLANConnectionUDPRepeater pRepeater, OutputStream pDestinationSocketOutputStream) throws Exception {
		Repeater = pRepeater;
		//.
		DestinationSocketOutputStream = pDestinationSocketOutputStream;
		//.
		Connect();
		//.
		flRunning = true;
		//.
		_Thread = new Thread(this);
		_Thread.start();
	}
	
	public void Destroy() throws Exception {
		Cancel();
		if (DestinationUDPSocket != null) 
			DestinationUDPSocket.close(); //. cancel socket blocking reading
		Wait();
		//.
		if (flActive)
			Disconnect();
	}
	
	private void Connect() throws Exception {
		TUDPEchoServerClient UDPEchoServerClient = new TUDPEchoServerClient(Repeater.ServerAddress,Repeater.ServerPort);
		TGetInternetEndpointResult GetInternetEndpointResult = UDPEchoServerClient.GetInternetEndpoint();
		if (GetInternetEndpointResult == null)
			throw new IOException("could not get an echo from the UDP server for a source UDP endpoint"); //. =>
		DestinationUDPLocalPort = GetInternetEndpointResult.Endpoint.LocalPort;
		DestinationUDPAddress = GetInternetEndpointResult.Endpoint.Address;
		DestinationUDPPort = GetInternetEndpointResult.Endpoint.Port;
		DestinationUDPSocketProxyType = GetInternetEndpointResult.ProxyType;
		DestinationUDPSocket = GetInternetEndpointResult.Endpoint.Socket;
		DestinationUDPSocket.setSoTimeout(UDPTimeout);
		//.
		ConnectionID = (1+rnd.nextInt(Short.MAX_VALUE-1));
		//. make connection from device side
		String Result = Repeater.StartHandler.DoStartLANConnection(Repeater.ConnectionType, Repeater.Address,Repeater.Port, Repeater.ServerAddress,Repeater.ServerPort, DestinationUDPAddress,DestinationUDPPort,DestinationUDPSocketProxyType, Repeater.AddressData, ConnectionID, Repeater.UserAccessKey);
		String[] SA = Result.split(",");
		ConnectionID = Integer.parseInt(SA[0]);
		SourceUDPAddress = SA[1];
		SourceUDPPort = Integer.parseInt(SA[2]);
		if (SA.length > 3)
			SourceUDPProxyType = Integer.parseInt(SA[3]);
		else
			SourceUDPProxyType = TUDPEchoServerClient.ECHO_TYPE_ASYMMETRIC;
		//.
		flActive = true;
	}
	
	private void Disconnect() throws Exception {
		if (ConnectionID > 0) 
			Repeater.StopHandler.DoStopLANConnection(ConnectionID,Repeater.UserAccessKey);
		//.
		DestinationUDPSocket.close();
		//.
		ConnectionID = 0;
		flActive = false;
	}
	
	@Override
	public void run() {
		try {
			try {
				int ActualSize;
				switch (Repeater.ConnectionType) {
				
				case LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL:
					byte[] ReceivePacketBuffer = new byte[MTU_MAX_SIZE];
					DatagramPacket ReceivePacket = new DatagramPacket(ReceivePacketBuffer,ReceivePacketBuffer.length);
					byte[] PacketDescriptorBA = new byte[4];
					//.
					while (!Canceller.flCancel) {
						try {
							DestinationUDPSocket.receive(ReceivePacket);
						    ActualSize = ReceivePacket.getLength();
						    ///test Log.i("UDP packet", "<- TS: "+Long.toString(System.currentTimeMillis())+", size: "+Integer.toString(ActualSize));
						}
						catch (SocketTimeoutException E) {
							continue; //. ^
						}
						//.
					    if (ActualSize > 1/*more than "ping" packet*/) {
					    	if (Repeater.OnSourceBytesTransmiteHandler != null)
					    		Repeater.OnSourceBytesTransmiteHandler.DoOnBytesTransmite(ReceivePacketBuffer,ActualSize);
					    	//.
							PacketDescriptorBA[0] = (byte)(ActualSize & 0xff);
							PacketDescriptorBA[1] = (byte)(ActualSize >> 8 & 0xff);
							PacketDescriptorBA[2] = (byte)(ActualSize >> 16 & 0xff);
							PacketDescriptorBA[3] = (byte)(ActualSize >>> 24);
					    	DestinationSocketOutputStream.write(PacketDescriptorBA);
					    	//.
					    	DestinationSocketOutputStream.write(ReceivePacketBuffer,0,ActualSize);
					    	//.
					    	DestinationSocketOutputStream.flush();
					    }
					}
					break; //. >
				}
			}
			finally {
				flRunning = false;
			}
		}
		catch (Throwable T) {
			if (!Canceller.flCancel) {
				if (Repeater.ExceptionHandler != null)
					Repeater.ExceptionHandler.DoOnException(T);
			}
		}
	}
}
