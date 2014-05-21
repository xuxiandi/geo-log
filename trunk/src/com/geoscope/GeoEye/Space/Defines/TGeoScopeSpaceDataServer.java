package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.content.Context;

import com.geoscope.Network.TServerConnection;
import com.geoscope.Utils.TDataConverter;

public class TGeoScopeSpaceDataServer {

	public static final int CONNECTION_TYPE_PLAIN 		= 0;
	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
	
	public static final int DefaultPort = 5555;
	public static final int ServerReadWriteTimeout = 1000*60; //. Seconds
	
	public static final short SERVICE_NONE = 0;
	//.
	public static final int MESSAGE_DISCONNECT = 0;
	//. error messages
	public static final int MESSAGE_OK                    = 0;
	public static final int MESSAGE_ERROR                 = -1;
	public static final int MESSAGE_NOTFOUND              = -2;
	public static final int MESSAGE_UNKNOWNSERVICE        = -10;
	public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
	public static final int MESSAGE_ACCESSISDENIED        = -12;
	public static final int MESSAGE_TOOMANYCLIENTS        = -13;
	public static final int MESSAGE_UNKNOWNCOMMAND        = -14;
	public static final int MESSAGE_WRONGPARAMETERS       = -15;
	
	public static void CheckMessage(int Message) throws Exception {
		if (Message >= 0)
			return; //. ->
		switch (Message) {
		
		case MESSAGE_ERROR:
			throw new Exception("error"); //. =>
			
		case MESSAGE_NOTFOUND:
			throw new Exception("data is not found"); //. =>
			
		case MESSAGE_UNKNOWNSERVICE:
			throw new Exception("unknown service"); //. =>
			
		case MESSAGE_AUTHENTICATIONFAILED:
			throw new Exception("authentication is failed"); //. =>
			
		case MESSAGE_ACCESSISDENIED:
			throw new Exception("access is denied"); //. =>
			
		case MESSAGE_TOOMANYCLIENTS:
			throw new Exception("too many clients"); //. =>
			
		case MESSAGE_UNKNOWNCOMMAND:
			throw new Exception("unknown command"); //. =>
			
		case MESSAGE_WRONGPARAMETERS:
			throw new Exception("wrong parameters"); //. =>
			
		default:
			throw new Exception("unknown error, code: "+Integer.toString(Message)); //. =>
		}
	}

	private static Random rnd = new Random();
	
	protected Context context;
	//.
	public String 	ServerAddress;
	public int		ServerPort = DefaultPort;
	public int 		SecureServerPortShift = 2;
	public int		SecureServerPort() {
    	return (ServerPort+SecureServerPortShift);
    }
	//.
	protected int 		UserID;
	protected String 	UserPassword;
	//.
    public int			ConnectionType = (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
    protected Socket 	Connection;
    public InputStream 	ConnectionInputStream;
    public OutputStream ConnectionOutputStream;
    
	public TGeoScopeSpaceDataServer(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword) {
		context = pcontext;
		//.
		ServerAddress = pServerAddress;
		if (pServerPort > 0)
			ServerPort = pServerPort;
		//.
		UserID = pUserID;
		UserPassword = pUserPassword;
	}
	
	protected void Buffer_Encrypt(byte[] buffer, int Offset, int Size, String UserPassword) throws UnsupportedEncodingException {
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
	
	protected void Connect(short Service, int Command) throws Exception {
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
    	    SSLContext sslContext = SSLContext.getInstance("SSL");
    	    sslContext.init( null, _TrustAllCerts, new SecureRandom());
    	    //. create a ssl socket factory with our all-trusting manager
    	    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
    	    Connection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
    		break; //. >
    		
    	default:
    		throw new Exception("unknown connection type, type: "+Integer.toString(ConnectionType)); //. =>
    	}
        Connection.setSoTimeout(ServerReadWriteTimeout);
        Connection.setKeepAlive(true);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
        //. send login info
        String UserIDStr = Integer.toString(UserID);
        int UserIDStrSize = 2*UserIDStr.length(); //. UCS2(UTF-16) size
        int UserIDStr1Length = 16;
        StringBuilder SB = new StringBuilder(UserIDStr1Length);
        SB.append(UserID);
        final char[] CharSet = new char[] {'!','@','#','$','%','^','&','*','(',')'};
        while (SB.length() < UserIDStr1Length) 
        	SB.append(CharSet[rnd.nextInt(CharSet.length)]);
        String UserIDStr1 = SB.toString();
        int UserIDStr1Size = 2*UserIDStr1.length(); //. UCS2(UTF-16) size
    	byte[] LoginBuffer = new byte[2/*SizeOf(Service)*/+4/*SizeOf(UserIDStrSize)*/+UserIDStrSize+4/*SizeOf(UserIDStr1Size)*/+UserIDStr1Size+4/*SizeOf(Command)*/];
    	int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(Service);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(UserIDStrSize);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = UserIDStr.getBytes("UTF-16LE");
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(UserIDStr1Size);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = UserIDStr1.getBytes("UTF-16LE");
		Buffer_Encrypt(BA,0,BA.length,UserPassword);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Command);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		//.
		ConnectionOutputStream.write(LoginBuffer);
	}

	protected void Disconnect() throws IOException {
        //. close connection gracefully
		try {
	        byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(MESSAGE_DISCONNECT);
	        ConnectionOutputStream.write(BA);
	        ConnectionOutputStream.flush();
		}
		catch (Exception E) {}
        //.
        ConnectionOutputStream.close();
        ConnectionInputStream.close();
        Connection.close();
	}
	
	public void Destroy() throws IOException {
	}
}
