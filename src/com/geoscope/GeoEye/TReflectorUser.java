package com.geoscope.GeoEye;

import java.security.MessageDigest;


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
	
	public int 	  UserID = 0;		
	public String UserPassword = "";
	public String UserPasswordHash = "";
	
	public TReflectorUser(int pUserID, String pUserPassword)
	{
		UserID = pUserID;
		UserPassword = pUserPassword;
		//.
		UserPasswordHash = "";
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
