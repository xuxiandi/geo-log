package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.Random;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TDataConverter;

@SuppressLint("HandlerLeak")
public class TGeoScopeServerUserSession extends TCancelableThread {

	public static final int ServerDefaultPort = 8888;
	public static final int ServerReadWriteTimeout 		= 1000*60; //. Seconds
	public static final int ServerCheckpointInterval 	= 1000*600; //. Seconds
	public static final int ServerReconnectInterval 	= 1000*300; //. Seconds
	
	public static final short SERVICE_NONE          = 0;
	public static final short SERVICE_MESSAGING    	= 1;
	//.
	public static final short SERVICE_MESSAGING_VERSION_1	= 1;
	public static final short SERVICE_MESSAGING_VERSION_2	= 2;
	//.
	public static final int MESSAGE_DISCONNECT = 0;
	public static final int MESSAGE_CHECKPOINT = 1;
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
	//.
	public static final int SERVICE_MESSAGING_SERVERMESSAGE_NEWUSERMESSAGE = 1;
	
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

	private TGeoScopeServerUser User;
	//.
	private String 	ServerAddress;
	private int  	ServerPort;
	//.
	private static Random rnd = new Random();
	//.
    private Socket 		Connection;
    private InputStream 	ConnectionInputStream;
    private OutputStream ConnectionOutputStream;
    //.
    public boolean flSessioning = false;
	
	public TGeoScopeServerUserSession(TGeoScopeServerUser pUser) {
		User = pUser;
		//.
		Initialize();
	}
	
	public void Destroy() {
		Finalize();
	}
	
	public void Initialize() {
		Finalize();
		//.
		_Thread = new Thread(this);
		_Thread.start();		
	}
	
	public void Finalize() {
		if (_Thread != null) {
			CancelAndWait();
			_Thread = null;
		}
	}
	
	@Override 
	public void CancelAndWait() {
		if (Connection != null)
			try {
				Connection.shutdownInput();
			} catch (IOException E) {
			}
		super.CancelAndWait();
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
	
	private void Connect() throws Exception {
        Connection = new Socket(ServerAddress,ServerPort); 
        Connection.setSoTimeout(ServerReadWriteTimeout);
        Connection.setKeepAlive(true);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
        //. send login info
        String UserIDStr = Integer.toString(User.UserID);
        int UserIDStrSize = 2*UserIDStr.length(); //. UCS2(UTF-16) size
        int UserIDStr1Length = 16;
        StringBuilder SB = new StringBuilder(UserIDStr1Length);
        SB.append(User.UserID);
        final char[] CharSet = new char[] {'!','@','#','$','%','^','&','*','(',')'};
        while (SB.length() < UserIDStr1Length) 
        	SB.append(CharSet[rnd.nextInt(CharSet.length)]);
        String UserIDStr1 = SB.toString();
        int UserIDStr1Size = 2*UserIDStr1.length(); //. UCS2(UTF-16) size
    	byte[] LoginBuffer = new byte[2/*SizeOf(Service)*/+2/*SizeOf(Version)*/+4/*SizeOf(UserIDStrSize)*/+UserIDStrSize+4/*SizeOf(UserIDStr1Size)*/+UserIDStr1Size];
    	int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_MESSAGING);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_MESSAGING_VERSION_2);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(UserIDStrSize);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = UserIDStr.getBytes("UTF-16LE");
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(UserIDStr1Size);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		BA = UserIDStr1.getBytes("UTF-16LE");
		Buffer_Encrypt(BA,0,BA.length,User.UserPassword);
		System.arraycopy(BA,0, LoginBuffer,Idx, BA.length); Idx += BA.length;
		//.
		ConnectionOutputStream.write(LoginBuffer);
		//. check login
		byte[] DecriptorBA = new byte[4];
		ConnectionInputStream.read(DecriptorBA);
		int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
		CheckMessage(Descriptor);
	}

	private void Disconnect() throws IOException {
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
	
    private static void Connection_CheckReadData(Socket Connection, InputStream ConnectionInputStream, byte[] Data, int CheckInterval) throws Exception {
    	int LastTimeout = Connection.getSoTimeout();
    	try {
    		Connection.setSoTimeout(CheckInterval);
    		//.
            int Size;
            int SummarySize = 0;
            int ReadSize;
            while (SummarySize < Data.length) {
                ReadSize = Data.length-SummarySize;
                Size = ConnectionInputStream.read(Data,SummarySize,ReadSize);
                if (Size <= 0) 
                	throw new Exception("Connection_CheckReadData: connection is closed unexpectedly"); //. =>
                SummarySize += Size;
            }
    	}
    	finally {
    		Connection.setSoTimeout(LastTimeout);
    	}
    }
	
	public void run() {
		try {
			while (!Canceller.flCancel) {
				try {
					TGeoScopeServerInfo.TInfo SI = User.Server.Info.GetInfo();
					if (!SI.IsSpaceUserSessionServerValid())
						return; //. ->
					ServerAddress = SI.SpaceUserSessionServerAddress;
					ServerPort = SI.SpaceUserSessionServerPort;
					if (ServerPort == 0)
						ServerPort = ServerDefaultPort;
					//.
					Connect();
					try {
						flSessioning = true;
						try {
							//. retransmit possible missed messages
			    			MessageHandler.obtainMessage(HANDLER_MESSAGE_NEWUSERMESSAGE).sendToTarget();
			    			//. processing
							byte[] CheckpointMessageBA = TDataConverter.ConvertInt32ToBEByteArray(MESSAGE_CHECKPOINT);
							byte[] MessageBA = new byte[4];
							int Message;
							while (!Canceller.flCancel) {
						        try {
						        	Connection_CheckReadData(Connection, ConnectionInputStream, MessageBA, ServerCheckpointInterval);
						        }
						        catch (InterruptedIOException IIOE) { //. timeout
						        	//. send checkpoint message
							        ConnectionOutputStream.write(CheckpointMessageBA);
							        ConnectionOutputStream.flush();
							        //.
							        continue; //. ^
						        }					
					        	//. process message
					        	Message = TDataConverter.ConvertBEByteArrayToInt32(MessageBA,0);
					        	switch (Message) {
					        	
					        	case SERVICE_MESSAGING_SERVERMESSAGE_NEWUSERMESSAGE:
					    			MessageHandler.obtainMessage(HANDLER_MESSAGE_NEWUSERMESSAGE).sendToTarget();
					        		break; //. >
					        	}
							}
						}
						finally {
							flSessioning = false;						
						}
					}
					finally {
						Disconnect();
					}
				} catch (InterruptedException E) {
					return; //. ->
				} catch (CancelException CE) {
					return; //. ->
				} catch (Throwable E) {
					MessageHandler.obtainMessage(HANDLER_MESSAGE_SHOWEXCEPTION,new Exception(User.Server.context.getString(R.string.SUserSessionError)+E.getMessage())).sendToTarget();
				}
				//. sleeping for reconnect...
				Thread.sleep(ServerReconnectInterval);
			}
		} catch (InterruptedException E) {
		} catch (Throwable E) {
			MessageHandler.obtainMessage(HANDLER_MESSAGE_SHOWEXCEPTION,new Exception(User.Server.context.getString(R.string.SUserSessionError)+E.getMessage())).sendToTarget();
		}
	}
	
	private static final int HANDLER_MESSAGE_SHOWEXCEPTION 	= -1;
	private static final int HANDLER_MESSAGE_SHOWMESSAGE 	= 0;
	private static final int HANDLER_MESSAGE_NEWUSERMESSAGE = 1;
	
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case HANDLER_MESSAGE_SHOWEXCEPTION:
				if (Canceller.flCancel)
					break; // . >
				Exception E = (Exception) msg.obj;
				Toast.makeText(User.Server.context,E.getMessage(), Toast.LENGTH_LONG).show();
				// .
				break; // . >
				
			case HANDLER_MESSAGE_SHOWMESSAGE:
				if (Canceller.flCancel)
					break; // . >
				String Msg  = (String)msg.obj;
				Toast.makeText(User.Server.context,Msg, Toast.LENGTH_SHORT).show();
				// .
				break; // . >

			case HANDLER_MESSAGE_NEWUSERMESSAGE:
				if (Canceller.flCancel)
					break; // . >
				if (User.IncomingMessages != null)
					User.IncomingMessages.Check();
				//.
				break; // . >
			}
		}
	};	
}
