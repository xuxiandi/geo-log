package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TNetworkConnection;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.jcraft.jzlib.ZInputStream;


public class TReflectorUser {

	public static final double DefaultUserOnlineTimeout = (1.0/(24.0*3600.0))*5; //. seconds
	
	public static class TUserDescriptor {
		public int 		UserID;
		public boolean 	UserIsDisabled;
		public boolean 	UserIsOnline;
		public String 	UserName;
		public String 	UserFullName;
		public String 	UserContactInfo;
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
			UserID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
			return FromByteArrayV1(BA, Idx);
		}
	
		public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
			UserIsDisabled = (BA[Idx] != 0); Idx++;
			UserIsOnline = (BA[Idx] != 0); Idx++;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserName = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserName = "";
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserFullName = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserFullName = "";
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserContactInfo = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserContactInfo = "";
			return Idx;
		}
	}
	
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
		
		public boolean IsNone() {
			return ((idSecurityFileForPrivate == 0) && (idSecurityFileForClone == 0));
		}
	}
	
	public static class TIncomingMessage {
		public int 				SenderID;
		public TUserDescriptor 	Sender = null;
		public double 			Timestamp;
		public String 			Message;
	}
	
	@SuppressLint("HandlerLeak")
	public static class TIncomingMessageReceiving extends TCancelableThread {
		
		public static abstract class TReceiver {
			
			public abstract boolean DoOnMessage(TReflectorUser User, TIncomingMessage Message);
			public abstract boolean DoOnCommand(TReflectorUser User, TIncomingMessage Message);
			public abstract boolean DoOnCommandResponse(TReflectorUser User, TIncomingMessage Message);
		}
		
		public static final int DefaultCheckInterval = 60; //. seconds
		public static final int MESSAGE_RECEIVED = 1;
		
		private TReflector Reflector;
		private TReflectorUser User;
		//.
		private int CheckInterval = DefaultCheckInterval;
		//.
		private ArrayList<TReceiver> Receivers = new ArrayList<TReceiver>();
		
		public TIncomingMessageReceiving(TReflector pReflector, TReflectorUser pUser) {
			Reflector = pReflector;
			User = pUser;
			//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
    	@Override
        public void run() {
    		try {
        		while (!Canceller.flCancel) {
        			try {
            			int[] MessagesIDs = User.IncomingMessages_GetUnread(Reflector);
        				//.
        				if (Canceller.flCancel)
        					throw new CancelException(); //. =>
        				//.
            			if (MessagesIDs != null) {
                			for (int I = 0; I < MessagesIDs.length; I++) {
                				TIncomingMessage Message = User.IncomingMessages_GetMessage(Reflector, MessagesIDs[I]);
                				//. supply message with sender info
                				Message.Sender = User.GetUserInfo(Reflector, Message.SenderID);
                    			//. dispatch message
                    			MessageHandler.obtainMessage(MESSAGE_RECEIVED,Message).sendToTarget();
                				//.
                				if (Canceller.flCancel)
                					throw new CancelException(); //. =>
                			}
            			}
        			}
                	catch (CancelException CE) {
                		throw CE; //. =>
                	}
        			catch (Exception E) {
                		String S = E.getMessage();
                		if (S == null)
                			S = E.getClass().getName();
            			Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION,Reflector.getString(R.string.SErrorOfImcomingMessageReceiving)+S).sendToTarget();
        			}
        			//.
        			for (int I = 0; I < GetCheckInterval(); I++)
            			Thread.sleep(1000);
        		}
    		}
        	catch (InterruptedException E) {
        	}
        	catch (CancelException CE) {
        	}
        	catch (Throwable E) {
        		///- TDEVICEModule.Log_WriteCriticalError(E);
        		String S = E.getMessage();
        		if (S == null)
        			S = E.getClass().getName();
    			Reflector.MessageHandler.obtainMessage(TReflector.MESSAGE_SHOWEXCEPTION,Reflector.getString(R.string.SErrorOfImcomingMessageReceiving)+S).sendToTarget();
        	}
    	}
    	
		private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_RECEIVED:
	            	TIncomingMessage Message = (TIncomingMessage)msg.obj;
    				DispatchMessage(Message);
	            	//.
	            	break; //. >
	            }
	        }
	    };
	    
    	public synchronized int GetCheckInterval() {
    		return CheckInterval;
    	}
    	
    	public synchronized void SetCheckInterval(int Value) {
    		CheckInterval = Value;
    	}
    	
    	public void AddReceiver(TReceiver Receiver) {
    		synchronized (Receivers) {
        		Receivers.add(Receiver);
			}
    	}
    	
    	public void RemoveReceiver(TReceiver Receiver) {
    		synchronized (Receivers) {
        		Receivers.remove(Receiver);
			}
    	}
    	
    	public void DispatchMessage(TIncomingMessage Message) {
    		synchronized (Receivers) {
    			for (int I = 0; I < Receivers.size(); I++) 
    				if (Message.Message.startsWith("#")) { //. #COMMAND
    	    				if (Receivers.get(I).DoOnCommand(User, Message))
    	    					break; //. >
    				}
    				else 
        				if (Message.Message.startsWith("@")) { //. @COMMANDRESPONSE
    	    				if (Receivers.get(I).DoOnCommandResponse(User, Message))
    	    					break; //. >
    				}
        				else { //. user message
    	    				if (Receivers.get(I).DoOnMessage(User, Message))
    	    					break; //. >
        				}
			}    		
    	}
	}
	
	public int 	  UserID = 0;		
	public String UserPassword = "";
	public String UserPasswordHash = "";
	//.
	public TUserSecurityFiles SecurityFiles;
	//.
	public TIncomingMessageReceiving IncomingMessageReceiving;
	
	public TReflectorUser(int pUserID, String pUserPassword) {
		UserID = pUserID;
		UserPassword = pUserPassword;
		//.
		UserPasswordHash = "";
		//.
		SecurityFiles = null;
		//.
		IncomingMessageReceiving = null;
	}
	
	public void Destroy() {
		if (IncomingMessageReceiving != null) {
			IncomingMessageReceiving.CancelAndWait();
			IncomingMessageReceiving = null;
		}
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
				int Size = in.read(Data);
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
	
	public void InitializeIncomingMessageReceiving(TReflector Reflector) {
		IncomingMessageReceiving = new TIncomingMessageReceiving(Reflector,this);
	}
	
	private String IncomingMessages_PrepareSendNewURL(TReflector Reflector, int RecepientID) {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(RecepientID)+"/"+"IM.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(UserID);
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
	
	public void IncomingMessages_SendNew(TReflector Reflector, int RecepientID, String Message) throws Exception {
		byte[] MessageBA = Message.getBytes("windows-1251");
		String CommandURL = IncomingMessages_PrepareSendNewURL(Reflector,RecepientID);
        //.
		URL url = new URL(CommandURL); 
		//.
		HttpURLConnection HttpConnection = (HttpURLConnection)url.openConnection();           
		try {
	        if (!(HttpConnection instanceof HttpURLConnection))                     
	            throw new IOException(Reflector.getString(R.string.SNoHTTPConnection));
			HttpConnection.setDoOutput(true);
			HttpConnection.setDoInput(false);
			HttpConnection.setInstanceFollowRedirects(false); 
			HttpConnection.setRequestMethod("POST"); 
			HttpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
			HttpConnection.setRequestProperty("Content-Length", "" + Integer.toString(MessageBA.length));
			HttpConnection.setUseCaches(false);
			//. request
			DataOutputStream DOS = new DataOutputStream(HttpConnection.getOutputStream());
			try {
				DOS.write(MessageBA);
				DOS.flush();
			}
			finally {
				DOS.close();			
			}
            //. response
            int response = HttpConnection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) 
            	throw new IOException(Reflector.getString(R.string.SServerError)+HttpConnection.getResponseMessage());                          
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
	private String IncomingMessages_PrepareGetMessageURL(TReflector Reflector, int MessageID) {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(UserID)+"/"+"IM.dat";
		//. add command parameters
		URL2 = URL2+"?"+"2"/*command version*/+","+Integer.toString(MessageID);
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
	
	public TIncomingMessage IncomingMessages_GetMessage(TReflector Reflector, int MessageID) throws Exception {
		String CommandURL = IncomingMessages_PrepareGetMessageURL(Reflector,MessageID);
		//.
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(CommandURL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[HttpConnection.getContentLength()];
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				TIncomingMessage Result = new TIncomingMessage();
				int Idx = 0;
				Result.SenderID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 8; //. Int64
				Result.Timestamp = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx); Idx += 8;
				int MDS = (Data.length-Idx); 
				if (MDS > 0) {
					Result.Message = new String(Data, Idx,MDS, "windows-1251"); Idx += MDS;
				}
				else
					Result.Message = "";
				return Result; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
	private String IncomingMessages_PrepareGetUnreadURL(TReflector Reflector) {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(UserID)+"/"+"IM.dat";
		//. add command parameters
		URL2 = URL2+"?"+"3"/*command version*/;
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
	
	public int[] IncomingMessages_GetUnread(TReflector Reflector) throws Exception {
		String CommandURL = IncomingMessages_PrepareGetUnreadURL(Reflector);
		//.
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(CommandURL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				int DS = HttpConnection.getContentLength(); 
				if (DS == 0)
					return null; //. ->
				byte[] Data = new byte[DS];
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				int ItemsCount = (int)(Data.length/4/*SizeOf(Int32)*/);
				int[] Result = new int[ItemsCount];
				int Idx = 0;
				for (int I = 0; I < ItemsCount; I++) {
					Result[I] = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4; 
				}
				return Result; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
	private String PrepareUserListURL(TReflector Reflector, String NameContext, double OnLineTimeout) {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"InstanceList.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+NameContext+","+Double.toString(OnLineTimeout);
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
	
	public TUserDescriptor[] GetUserList(TReflector Reflector, String NameContext) throws Exception {
		TUserDescriptor[] Result = null;
		//.
		String CommandURL = PrepareUserListURL(Reflector, NameContext,DefaultUserOnlineTimeout);
		//.
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(CommandURL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[HttpConnection.getContentLength()];
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				ByteArrayInputStream BIS = new ByteArrayInputStream(Data);
				try {
					ZInputStream ZIS = new ZInputStream(BIS);
					try {
						byte[] Buffer = new byte[8192];
						int ReadSize;
						ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
						try {
							while ((ReadSize = ZIS.read(Buffer)) > 0) 
								BOS.write(Buffer, 0,ReadSize);
							//.
							Data = BOS.toByteArray();
							int Idx = 0;
							int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4;
							Result = new TUserDescriptor[ItemsCount];
							for (int I = 0; I < ItemsCount; I++) {
								TUserDescriptor UD = new TUserDescriptor();
								Idx = UD.FromByteArray(Data, Idx);
								Result[I] = UD;
							}
						}
						finally {
							BOS.close();
						}
					}
					finally {
						ZIS.close();
					}
				}
				finally {
					BIS.close();
				}
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
		return Result;
	}
	
	private String PrepareUserInfoURL(TReflector Reflector, int pUserID, double OnLineTimeout) {
		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Info.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Double.toString(OnLineTimeout);
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
	
	public TUserDescriptor GetUserInfo(TReflector Reflector, int pUserID) throws Exception {
		TUserDescriptor Result = null;
		//.
		String CommandURL = PrepareUserInfoURL(Reflector, pUserID,DefaultUserOnlineTimeout);
		//.
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(CommandURL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[HttpConnection.getContentLength()];
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				Result = new TUserDescriptor();
				Result.FromByteArrayV1(Data, 0);
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
		return Result;
	}	
}
