package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.geoscope.GeoEye.R;
import com.geoscope.Network.TServerConnection;
import com.geoscope.Utils.TDataConverter;

public class TGeoScopeServer {

	public static final int CONNECTION_TYPE_PLAIN 		= 0;
	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
	
	public static class ServerIsOfflineException extends IOException {
		
		private static final long serialVersionUID = 1L;

		public ServerIsOfflineException(String Message) {
			super(Message);
		}
		
		public ServerIsOfflineException() {
			this("");
		}
	}
	
	public static final int 	Connection_ConnectTimeout 	= 1000*30/*seconds*/;
	public static final int 	Connection_ReadTimeout 		= 1000*30/*seconds*/;

	public Context context;
	//.
	public String 	HostAddress = "";
	public int 		HostPort = 0;
	protected int 	HostSecurePortShift = 2;
    protected int	HostSecurePort() {
    	return (HostPort+HostSecurePortShift);
    }
	//.
	public String Address = "";
	public String SecureAddress = "";
    //.
    public int		ConnectionType() {
    	return (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
    }
	//.
	public boolean flOnline = false;
	//.
	public TGeoScopeServerUser User = null;
	//.
	public TGeoScopeServerInfo Info = new TGeoScopeServerInfo(this);
	
	public TGeoScopeServer(Context pcontext) {
		context = pcontext;
	}

	public TGeoScopeServer(Context pcontext, String pAddress, int pPort) throws Exception {
		context = pcontext;
		//.
		SetServerAddress(pAddress,pPort);
	}

	public void Destroy() throws Exception {
		FinalizeUser();
		//.
		Finalize();
	}
	
	public void Initialize() throws Exception {
		Info.Initialize();
	}
	
	public void Finalize() {
		Info.Finalize();
	}
	
	public void CheckInitialized() throws Exception {
		Info.CheckInitialized();
	}
	
	public void SetServerAddress(String pAddress, int pPort) throws Exception {
		HostAddress = pAddress;
		HostPort = pPort;
		//.
		Address = HostAddress+":"+Integer.toString(HostPort);
		SecureAddress = HostAddress+":"+Integer.toString(HostSecurePort());
		//.
		Info.Clear();
		//.
		FinalizeUser();
	}
	
	public TGeoScopeServerUser InitializeUser(int UserID, String UserPassword, boolean flUserSession) throws Exception {
		if (User != null) {
			if ((User.UserID == UserID) && User.UserPassword.equals(UserPassword) && (User.flUserSession == flUserSession))
				return User; //. ->
			FinalizeUser();
		}
		//.
		User = new TGeoScopeServerUser(this, UserID,UserPassword);
		User.Initialize(flUserSession);
		//.
		return User;
	}
	
	public void FinalizeUser() throws Exception {
		if (User != null) {
			User.Destroy();
			User = null;
		}
	}
	
	public void FinalizeUser(TGeoScopeServerUser pUser) throws Exception {
		if (User == pUser) {
			User.Destroy();
			User = null;
		}
	}
	
	public TGeoScopeServerUser ReinitializeUser(int UserID, String UserPassword, boolean flUserSession) throws Exception {
		FinalizeUser();
		//.
		User = new TGeoScopeServerUser(this, UserID,UserPassword);
		User.Initialize(flUserSession);
		//.
		return User;
	}
	
	public boolean IsNetworkAvailable() {
	    ConnectivityManager AConnectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo ActiveNetworkInfo = AConnectivityManager.getActiveNetworkInfo();
	    return ((ActiveNetworkInfo != null) && ActiveNetworkInfo.isAvailable() && ActiveNetworkInfo.isConnected());
	}
	
	public HttpURLConnection OpenHTTPConnection(String urlString) throws Exception {
    	switch (ConnectionType()) {
    	
    	case CONNECTION_TYPE_PLAIN:
    		URL url = new URL(urlString);
			URLConnection Connection = url.openConnection();
			if (!(Connection instanceof HttpURLConnection))
				throw new IOException(context.getString(R.string.SNoHTTPConnection)); //. =>
			//.
    		return (HttpURLConnection)Connection; //. ->
    		
    	case CONNECTION_TYPE_SECURE_SSL:
    		String HTTPPrefix = "http://"+Address;
    		String HTTPSPrefix = "https://"+SecureAddress;
    		urlString = urlString.replace(HTTPPrefix, HTTPSPrefix);
    		url = new URL(urlString);
			Connection = url.openConnection();
			if (!(Connection instanceof HttpsURLConnection))
				throw new IOException(context.getString(R.string.SNoHTTPConnection)); //. =>
			//.
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
    	    //.
    	    HttpsURLConnection SecureConnection = (HttpsURLConnection)Connection;
    	    SecureConnection.setSSLSocketFactory(sslSocketFactory);
    	    SecureConnection.setHostnameVerifier(new HostnameVerifier() {
				@Override
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			});
    	    //.
    		return (HttpURLConnection)Connection; //. ->
    		
    	default:
    		throw new Exception("unknown connection type"); //. =>
    	}
	}
	
	public HttpURLConnection OpenConnection(String urlString,int ReadTimeout) throws Exception {
		HttpURLConnection httpConn = OpenHTTPConnection(urlString);
		try {
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.setUseCaches(false);
			httpConn.setConnectTimeout(Connection_ConnectTimeout);
			httpConn.setReadTimeout(ReadTimeout);
			httpConn.connect();
			// .
			try {
				int response = httpConn.getResponseCode();
				if (response != HttpURLConnection.HTTP_OK) {
					String ErrorMessage = httpConn.getResponseMessage();
					byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
					ErrorMessage = new String(ErrorMessageBA,"windows-1251");
					throw new IOException(context.getString(R.string.SServerError)+ErrorMessage); //. =>
				}
			} catch (Exception E) {
				httpConn.disconnect();
				throw E; // . =>
			}
		} catch (SocketTimeoutException STE) {
			throw new IOException(context.getString(R.string.SConnectionTimeoutError)); //. =>
		} catch (ConnectException CE) {
			throw new ConnectException(context.getString(R.string.SNoServerConnection)); //. =>
		} catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.toString();
			throw new IOException(context.getString(R.string.SHTTPConnectionError) + S); //. =>
		}
		return httpConn;
	}
	
	public HttpURLConnection OpenConnection(String urlString) throws Exception {
		return OpenConnection(urlString,Connection_ReadTimeout);
	}
	
	private String PrepareGetCaptchaURL(TGeoScopeServerUser User) {
		String URL1 = Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTMODELServer)+"/"+"Co"+"/"+Integer.toString(0/*current server*/)+"/"+"Captcha.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command*/+","+"1"/*command version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	public byte[] GetCaptcha(TGeoScopeServerUser User) throws Exception {
		String CommandURL = PrepareGetCaptchaURL(User);
		//.
		HttpURLConnection Connection = OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	private String PrepareRegisterNewUserURL(TGeoScopeServerUser User, String pNewUserName, String pNewUserPassword, String pNewUserFullName, String pNewUserContactInfo, String pSignature) {
		String URL1 = Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTMODELServer)+"/"+"Co"+"/"+Integer.toString(0/*current server*/)+"/"+"NewUser.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command*/+","+"1"/*command version*/+","+pNewUserName+","+pNewUserPassword+","+pNewUserFullName+","+pNewUserContactInfo+","+pSignature;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	public int RegisterNewUser(TGeoScopeServerUser User, String pNewUserName, String pNewUserPassword, String pNewUserFullName, String pNewUserContactInfo, String pSignature) throws Exception {
		String CommandURL = PrepareRegisterNewUserURL(User, pNewUserName, pNewUserPassword, pNewUserFullName, pNewUserContactInfo, pSignature);
		//.
		HttpURLConnection Connection = OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				int Idx = 0;
				int NewUserID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 8; //. Int64
				return NewUserID; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}		
}
