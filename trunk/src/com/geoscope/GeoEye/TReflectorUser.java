package com.geoscope.GeoEye;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.security.MessageDigest;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;


public class TReflectorUser {

	public static class TUserSecurity {
		
		public static String GetPasswordHash(String UserPassword) {
			String md5Pass;
			try {
		        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
		        digest.update(UserPassword.getBytes("windows-1251"));
		        byte passDigest[] = digest.digest();
		        //.
		        StringBuffer hexPass = new StringBuffer();
		        for (int i=0; i<passDigest.length; i++) {
		                String h = Integer.toHexString(0xFF & passDigest[i]);
		                while (h.length()<2) h = "0" + h;
		                        hexPass.append(h);
		        }
		        md5Pass = hexPass.toString();
			} 
			catch (Exception E) 
			{
				md5Pass = "";
			}
			return md5Pass;
		}
	}
	
	public static class TUserSecurityFiles {
		public int	idSecurityFileForPrivate;
		public int	idSecurityFileForClone;
	}
	
	public int 	  UserID = 0;		
	public String UserPassword = "";
	public String UserPasswordHash = "";
	//.
	public TUserSecurityFiles SecurityFiles;
	
	public TReflectorUser(int pUserID, String pUserPassword)
	{
		UserID = pUserID;
		UserPassword = pUserPassword;
		//.
		UserPasswordHash = "";
		//.
		SecurityFiles = null;
	}

	public TUserSecurityFiles GetUserSecurityFiles(TReflector Reflector) throws Exception {
		if (SecurityFiles != null)
			return SecurityFiles; //. =>
		//.
		TUserSecurityFiles _SecurityFiles;
		String CommandURL = PrepareSecurityFilesURL(Reflector);
		//.
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(CommandURL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[2*8/*SizeOf(Int64)*/];
				int Size= in.read(Data);
				if (Size != Data.length)
					throw new IOException(Reflector.getString(R.string.SErrorOfGettingUserSecurityFiles)); //. =>
				_SecurityFiles = new TUserSecurityFiles();
				int Idx = 0;
				_SecurityFiles.idSecurityFileForPrivate = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx+=8; //. Int64
				_SecurityFiles.idSecurityFileForClone = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); 
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
		//.
		SecurityFiles = _SecurityFiles;
		return SecurityFiles;
	}
	
	private String PrepareSecurityFilesURL(TReflector Reflector) {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(UserID)+"/"+"UserSecurityFiles.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/;
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
	
    public byte[] EncryptBufferV2(byte[] Buffer) 
    {
    	byte[] BA = new byte[Buffer.length];
    	byte[] UserPasswordArray;
    	//.
    	try {
    		UserPasswordArray = UserPassword.getBytes("windows-1251");
    	}
    	catch (Exception E)
    	{
    		UserPasswordArray = null;
    	}
    	//.
    	if ((UserPasswordArray != null) && (UserPasswordArray.length > 0))
    	{
    		int UserPasswordArrayIdx = 0;
    		for (int I = 0; I < Buffer.length; I++)
    		{
    			BA[I] = (byte)(Buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
    			UserPasswordArrayIdx++;
    			if (UserPasswordArrayIdx >= UserPasswordArray.length) UserPasswordArrayIdx = 0;
    		}
    	}
    	return BA;
    }
}
