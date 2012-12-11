package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;

public class TServer {

	private static String PrepareGetCaptchaURL(TReflector Reflector) {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
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
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
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
	
	public static byte[] GetCaptcha(TReflector Reflector) throws Exception {
		String CommandURL = PrepareGetCaptchaURL(Reflector);
		//.
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(CommandURL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[HttpConnection.getContentLength()];
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
	private static String PrepareRegisterNewUserURL(TReflector Reflector, String pNewUserName, String pNewUserPassword, String pNewUserFullName, String pNewUserContactInfo, String pSignature) {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
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
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
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
	
	public static int RegisterNewUser(TReflector Reflector, String pNewUserName, String pNewUserPassword, String pNewUserFullName, String pNewUserContactInfo, String pSignature) throws Exception {
		String CommandURL = PrepareRegisterNewUserURL(Reflector, pNewUserName, pNewUserPassword, pNewUserFullName, pNewUserContactInfo, pSignature);
		//.
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(CommandURL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[HttpConnection.getContentLength()];
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				int Idx = 0;
				int NewUserID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 8; //. Int64
				return NewUserID; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}	
}
