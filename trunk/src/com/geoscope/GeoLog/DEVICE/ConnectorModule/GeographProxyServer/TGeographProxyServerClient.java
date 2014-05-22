package com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import com.geoscope.Network.TServerConnection;
import com.geoscope.Utils.TDataConverter;

public class TGeographProxyServerClient {

	public static final int CONNECTION_TYPE_PLAIN 		= 0;
	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
	
	public static final short SERVICE_NONE                          = 0;
	public static final short SERVICE_AUDIOCHANNEL_V1               = 1;
	public static final short SERVICE_VIDEOCHANNEL_V1               = 2;
	public static final short SERVICE_AUDIOCHANNEL_V2               = 3;
	public static final short SERVICE_VIDEOCHANNEL_V2               = 4;
	public static final short SERVICE_LANCONNECTION_SOURCE          = 5;
	public static final short SERVICE_LANCONNECTION_SOURCE_V1       = 6;
	public static final short SERVICE_LANCONNECTION_DESTINATION     = 7;
	public static final short SERVICE_LANCONNECTION_DESTINATION_V1  = 8;
	public static final short SERVICE_INFO                          = 9;
	public static final short SERVICE_INFO_V1                       = 10;

	public static final short SERVICE_INFO_VERSION_0                = 0;
	public static final short SERVICE_INFO_VERSION_UDPECHOSERVER    = 1;

	public static final short MESSAGE_CHECKPOINT = 1;
	public static final short MESSAGE_DISCONNECT = 0;
	//. base error messages
	public static final short MESSAGE_OK                    = 0;
	public static final short MESSAGE_ERROR                 = -1;
	public static final short MESSAGE_UNKNOWNSERVICE        = -10;
	public static final short MESSAGE_AUTHENTICATIONFAILED  = -11;
	public static final short MESSAGE_ACCESSISDENIED        = -12;
	public static final short MESSAGE_TOOMANYCLIENTS        = -13;
	public static final short MESSAGE_WRONGPARAMETERS       = -14;
	//. custom error messages
	public static final short MESSAGE_LANCONNECTIONISNOTFOUND       = -101;
	
	public static final int ConnectionTimeout = 1000*30; //. seconds
	
	public static class TUDPEchoServerInfo {
		
		public int[] Ports;
	}
	
	public static class TServerInfo {
	
		public String 	Address;
		public int		Port;
		//.
		public TUDPEchoServerInfo UDPEchoServerInfo;
		
		public TServerInfo(String pAddress, int pPort) {
			Address = pAddress;
			Port = pPort;
		}
	}
	
	private String 	ServerAddress;
	private int		ServerPort;
	protected int 	SecureServerPortShift = 2;
    protected int	SecureServerPort() {
    	return (ServerPort+SecureServerPortShift);
    }
    //.
    public int			ConnectionType() {
    	return (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
    }
	//.
	private int 	UserID;
	private String 	UserPassword;
	//.
	private int idGeographServerObject;
	
	public TGeographProxyServerClient(String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject) {
		ServerAddress = pServerAddress;
		ServerPort = pServerPort;
		//.
		UserID = pUserID;
		UserPassword = pUserPassword;
		//.
		idGeographServerObject = pidGeographServerObject;		
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
	
	public TUDPEchoServerInfo GetUDPEchoServerInfo() throws Exception {
		Socket ServerConnection;
    	switch (ConnectionType()) {
    	
    	case CONNECTION_TYPE_PLAIN:
    		ServerConnection = new Socket(ServerAddress,ServerPort); 
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
    	    ServerConnection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
    		break; //. >
    		
    	default:
    		throw new Exception("unknown connection type"); //. =>
    	}
		try {
			ServerConnection.setSoTimeout(ConnectionTimeout);
			ServerConnection.setTcpNoDelay(true);
			ServerConnection.setKeepAlive(true);
			ServerConnection.setSendBufferSize(8192);
			InputStream ServerConnectionInputStream = ServerConnection.getInputStream();
			OutputStream ServerConnectionOutputStream = ServerConnection.getOutputStream();
	        //. 
	    	byte[] InputBuffer = new byte[22];
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_INFO_V1);
			System.arraycopy(BA,0, InputBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(UserID);
			System.arraycopy(BA,0, InputBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(idGeographServerObject);
			System.arraycopy(BA,0, InputBuffer,10, BA.length);
			short CRC = Buffer_GetCRC(InputBuffer, 10,8);
			BA = TDataConverter.ConvertInt16ToBEByteArray(CRC);
			System.arraycopy(BA,0, InputBuffer,18, BA.length);
			BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_INFO_VERSION_UDPECHOSERVER);
			System.arraycopy(BA,0, InputBuffer,20, BA.length);
			Buffer_Encrypt(InputBuffer,10,10,UserPassword);
			//. send login data
			ServerConnectionOutputStream.write(InputBuffer);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ServerConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor < 0)
				throw new Exception("GetUDPEchoServerInfo() error, RC: "+Integer.toString(Descriptor)); //. =>
			BA = new byte[Descriptor*2/*SizeOf(Port)*/];
			ServerConnectionInputStream.read(BA);
			//.
			TUDPEchoServerInfo Result = new TUDPEchoServerInfo();
			Result.Ports = new int[Descriptor];
			for (int I = 0; I < Descriptor; I++) 
				Result.Ports[I] = (int)TDataConverter.ConvertBEByteArrayToInt16(BA,(I << 1));
			//.
			return Result; //. ->
		}
		finally {
			ServerConnection.close();
		}
	}
	
	public TServerInfo GetServerInfo() throws Exception {
		TServerInfo Result = new TServerInfo(ServerAddress,ServerPort);
		Result.UDPEchoServerInfo = GetUDPEchoServerInfo();
		return Result;
	}
}
