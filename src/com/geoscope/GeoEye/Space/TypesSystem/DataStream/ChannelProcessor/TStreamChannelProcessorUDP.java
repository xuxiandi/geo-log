package com.geoscope.GeoEye.Space.TypesSystem.DataStream.ChannelProcessor;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;

import android.content.Context;

import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.Memory.TMemoryStream;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.TypesSystem.TDataStreamServer;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater.TInternetUDPEndpoint;

public abstract class TStreamChannelProcessorUDP extends TStreamChannelProcessorAbstract {

    public static class TDataStreamChannelReading extends TDataStreamChannelReadingAbstract {
		
		private static TInternetUDPEndpoint GetInternetEndpoint(String ServerAddress, int ServerPort) throws IOException {
			int TryCount = 3;
			//.
			String UID = TUIDGenerator.Generate();
			byte[] UIDBA = UID.getBytes("US-ASCII");
			//.
			byte[] SendPacketBuffer = new byte[2/*SizeOf(Version)*/+UIDBA.length];
			System.arraycopy(UIDBA,0, SendPacketBuffer,2, UIDBA.length);
			//.
			DatagramPacket SendPacket = new DatagramPacket(SendPacketBuffer,SendPacketBuffer.length,InetAddress.getByName(ServerAddress),ServerPort);
			//.
			byte[] ReceivePacketBuffer = new byte[TConnectionUDPRepeater.MTU_MAX_SIZE];
			DatagramPacket ReceivePacket = new DatagramPacket(ReceivePacketBuffer,ReceivePacketBuffer.length);
			//.
			for (int I = 0; I < TryCount; I++) {
				int LocalPort = TConnectionUDPRepeater.GetUDPLocalPort();
				DatagramSocket _Socket = new DatagramSocket(LocalPort,InetAddress.getByName("0.0.0.0"));
				try {
					_Socket.setSoTimeout(2000);
					//. send ECHO_TYPE_SYMMETRIC
					SendPacketBuffer[0] = 2; //. Version
					_Socket.send(SendPacket); 				
					//. listen for answer
					while (true) {
						try {
							_Socket.receive(ReceivePacket);
							int ReceivePacketBufferSize = ReceivePacket.getLength();
							if (ReceivePacketBufferSize == (SendPacketBuffer.length+TInternetUDPEndpoint.ByteArraySize)) {
								boolean flMatched = true;
								for (int J = 2/*skip Version*/; J < SendPacketBuffer.length; J++)
									if (ReceivePacketBuffer[J] != SendPacketBuffer[J]) {
										flMatched = false;
										break; //. >
									}
								if (flMatched) {
									TInternetUDPEndpoint Endpoint = new TInternetUDPEndpoint(LocalPort,ReceivePacketBuffer,SendPacketBuffer.length);
									Endpoint.Socket = _Socket;
									_Socket = null;
									//.
									return Endpoint; //. ->
								}
							}
						}
						catch (SocketTimeoutException STE) {
							break; //. >
						};
					}
				}
				finally {
					if (_Socket != null)
						_Socket.close();
				}
			}
			return null; //. ->
		}
		
		
		public static class TUDPReceiver extends TCancelableThread {

			public static final int MTU_MAX_SIZE = 1500;
			
			private TDataStreamChannelReading Reading;
			//.
			private DatagramSocket UDPSocket = null;
			
			public TUDPReceiver(TDataStreamChannelReading pReading, DatagramSocket pUDPSocket) throws Exception {
	    		super();
	    		//.
				Reading = pReading;
				UDPSocket = pUDPSocket;
				//.
				_Thread = new Thread(this);
				_Thread.start();
			}
			
			@Override
			public void Destroy() throws Exception {
				Cancel();
				if (UDPSocket != null) 
					UDPSocket.close(); //. cancel socket blocking reading
				Wait();
			}
			
			@Override
			public void run() {
				try {
					byte[] ReceivePacketBuffer = new byte[MTU_MAX_SIZE];
					DatagramPacket ReceivePacket = new DatagramPacket(ReceivePacketBuffer,ReceivePacketBuffer.length);
					//.
					int ActualSize;
			        TMemoryStream Stream = new TMemoryStream(8192);
			        try {
						UDPSocket.setSoTimeout(DefaultReadingTimeout);
						while (!Canceller.flCancel) {
							try {
								UDPSocket.receive(ReceivePacket);
							    ActualSize = ReceivePacket.getLength();
							    ///test Log.i("UDP packet", "<- TS: "+Long.toString(System.currentTimeMillis())+", size: "+Integer.toString(ActualSize));
							}
							catch (SocketTimeoutException E) {
								Reading.DoOnIdle(Canceller);
								continue; //. ^
							}
							//.
						    if (ActualSize > 0) {
						    	Stream.Position = 0;
						    	Stream.Write(ReceivePacketBuffer,ActualSize);
						    	//.
						    	Stream.Position = 0;
						    	Reading.DoOnRead(Stream,ActualSize, Canceller);
						    }
						}
			        }
			        finally {
			        	Stream.Close();
			        }
				}
				catch (Throwable T) {
					if (!Canceller.flCancel) {
			    	    Exception ThreadException = new Exception(T.getMessage());
			    	    Reading.DoOnException(ThreadException);
					}
				}
			}
		}
		
				
    	private int 					UDPSenderPort;
    	private TInternetUDPEndpoint 	UDPEndpoint;
    	private TUDPReceiver 			UDPReceiver = null;
    	
	    public TDataStreamChannelReading(TStreamChannelProcessorAbstract pProcessor) {
	    	super(pProcessor);
	    }
	    
	    @Override
	    public void Destroy(boolean flWaitForTermination) throws InterruptedException {
	    	super.Destroy(flWaitForTermination);
	    }
	    
	    @Override
	    public void run() {
	    	try {
	    		TDataStreamServer DataStreamServer =  new TDataStreamServer(Processor.context, Processor.ServerAddress,Processor.ServerPort, Processor.UserID,Processor.UserPassword);
	    		try {
	    	    	DataStreamServer.ComponentStreamServer_GetDataStreamV3_Begin(Processor.idTComponent,Processor.idComponent);
		    		try {
		    			DoOnStart();
		    			try {
		    				UDPReceiver = null;
		    				try {
			    		    	DataStreamServer.ComponentStreamServer_GetDataStreamV3_Read(TDataStreamServer.DATASTREAM_TRANSMISSIONTYPE_UDP, new TDataStreamServer.TStreamSenderPortHandler() {
			    		    		@Override
			    		    		public void DoOnPortRead(int Port) throws Exception {
			    		    			UDPSenderPort = Port;
			    		    			//. get this side UDP Internet end-point
			    		    			UDPEndpoint = GetInternetEndpoint(Processor.ServerAddress,UDPSenderPort);
			    		    		}
			    		    	}, new TDataStreamServer.TGetDataStreamTransmissionParamsHandler() {
			    		    		@Override
			    		    		public String GetParams() {
			    		    			return UDPEndpoint.Address+":"+UDPEndpoint.Port;
			    		    		};
			    		    	}, new TDataStreamServer.TDoBeforeStreamingHandler() {
			    		    		@Override
			    		    		public void DoBeforeStreaming() throws Exception {
			    		    			UDPReceiver = new TUDPReceiver(TDataStreamChannelReading.this, UDPEndpoint.Socket);
			    		    		};
			    		    	},
			    		    	Processor.ChannelID, (short)2, Processor.ReadingTimeout, Canceller);
		    				}
		    				finally {
		    					if (UDPReceiver != null) {
		    						UDPReceiver.Destroy();
		    						UDPReceiver = null;
		    					}
		    				}
		    			}
		    			finally {
		    				DoOnFinish();
		    			}
		    		}
		    		finally {
				    	DataStreamServer.ComponentStreamServer_GetDataStreamV3_End();
		    		}
	    		}
	    		finally {
	    			DataStreamServer.Destroy();
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
	
    public TStreamChannelProcessorUDP(Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidTComponent, long pidComponent, int pChannelID, String pTypeID, int pDataFormat, String pName, String pInfo, String pConfiguration, String pParameters, TOnProgressHandler pOnProgressHandler, TOnIdleHandler pOnIdleHandler, TOnExceptionHandler pOnExceptionHandler) throws Exception {
    	super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidTComponent,pidComponent, pChannelID, pTypeID, pDataFormat, pName,pInfo, pConfiguration, pParameters, pOnProgressHandler, pOnIdleHandler, pOnExceptionHandler);
    }

    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
    	Reading = new TDataStreamChannelReading(this);
    }
}
