package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;

import com.geoscope.GeoEye.R;
import com.geoscope.Utils.TDataConverter;

public class TGeoScopeServer {

	public static final int Connection_ConnectTimeout 	= 1000*30/*seconds*/;
	public static final int Connection_ReadTimeout 		= 1000*30/*seconds*/;

	public Context context;
	//.
	public String 	HostAddress = "";
	public int 		HostPort = 0;
	//.
	public String Address = "";
	//.
	public TGeoScopeServerUser User = null;
	
	public TGeoScopeServer(Context pcontext) {
		context = pcontext;
	}

	public TGeoScopeServer(Context pcontext, String pAddress, int pPort) {
		context = pcontext;
		//.
		SetServerAddress(pAddress,pPort);
	}

	public void Destroy() throws IOException {
		FinalizeUser();
	}
	
	public void SetServerAddress(String pAddress, int pPort) {
		HostAddress = pAddress;
		HostPort = pPort;
		//.
		Address = HostAddress+":"+Integer.toString(HostPort);
	}
	
	public TGeoScopeServerUser InitializeUser(int UserID, String UserPassword) throws Exception {
		if (User != null) {
			if ((User.UserID == UserID) && (User.UserPassword.equals(UserPassword)))
				return User; //. ->
			FinalizeUser();
		}
		//.
		User = new TGeoScopeServerUser(this, UserID,UserPassword);
		User.InitializeIncomingMessages();
		//.
		return User;
	}
	
	public TGeoScopeServerUser ReinitializeUser(int UserID, String UserPassword) throws Exception {
		FinalizeUser();
		//.
		User = new TGeoScopeServerUser(this, UserID,UserPassword);
		User.InitializeIncomingMessages();
		//.
		return User;
	}
	
	public void FinalizeUser() throws IOException {
		if (User != null) {
			User.Destroy();
			User = null;
		}
	}
	
	public void FinalizeUser(TGeoScopeServerUser pUser) throws IOException {
		if (User == pUser) {
			User.Destroy();
			User = null;
		}
	}
	
	public HttpURLConnection OpenConnection(String urlString,int ReadTimeout) throws IOException {
		int response = -1;
		// .
		URL url = new URL(urlString);
		URLConnection conn = url.openConnection();
		// .
		if (!(conn instanceof HttpURLConnection))
			throw new IOException(context.getString(R.string.SNoHTTPConnection)); //. =>
		// .
		HttpURLConnection httpConn;
		try {
			httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.setUseCaches(false);
			httpConn.setConnectTimeout(Connection_ConnectTimeout);
			httpConn.setReadTimeout(ReadTimeout);
			httpConn.connect();
			// .
			try {
				response = httpConn.getResponseCode();
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
		} catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.toString();
			throw new IOException(context.getString(R.string.SHTTPConnectionError) + S); //. =>
		}
		return httpConn;
	}
	
	public HttpURLConnection OpenConnection(String urlString) throws IOException {
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
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
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
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
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
