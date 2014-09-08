package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Exception.TExceptionHandler;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.LANConnectionRepeaterDefines;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionStopHandler;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographServerClient;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoPhoneServerLANLVConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderPanel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerVideoPhoneServer extends TVideoRecorderPanel {
    
	public static class TSession extends TUserAccessKey {

		public static final int SessionTimeout = 1000*50; //. seconds
		public static final int SessionMaxTimeout = 1000*600; //. seconds
		public static final int SessionMaxLength = 256;
		
		public static final int SESSION_STATUS_CONTACT 		= 5;
		public static final int SESSION_STATUS_OPENED 		= 4;
		public static final int SESSION_STATUS_ACCEPTED 	= 3;
		public static final int SESSION_STATUS_CALL 		= 2;
		public static final int SESSION_STATUS_STARTED 		= 1;
		public static final int SESSION_STATUS_ZERO 		= 0;
		public static final int SESSION_STATUS_ERROR 		= -1;
		public static final int SESSION_STATUS_CANCELLED 	= -2;
		public static final int SESSION_STATUS_REJECTED 	= -3;
		public static final int SESSION_STATUS_CLOSED 		= -4;
		public static final int SESSION_STATUS_FINISHED 	= -5;
		
		public Object 	StatusSignal = new Object();
		public int		Status = SESSION_STATUS_ZERO;
		//.
		public int 		InitiatorID;
		public String 	InitiatorName;
		public int idTComponent;
		public int idComponent;
		//.
		public boolean flAudio;
		public boolean flVideo;
		//.
		public TDEVICEModule Device;
		public TUserAgent UserAgent; 
		public TCoGeoMonitorObject Object;
		public TSessionServerClient SessionServerClient;
		public Activity Panel;
		//.
		public TAsyncProcessing AudioCalling;
		
		public TSession(String SessionID, int pInitiatorID, String pInitiatorName, int pidTComponent, int pidComponent, boolean pflAudio, boolean pflVideo, TDEVICEModule pDevice, TUserAgent pUserAgent) {
			super(SessionID);
			//.
			InitiatorID = pInitiatorID;
			InitiatorName = pInitiatorName;
			idTComponent = pidTComponent;
			idComponent = pidComponent;
			//.
			flAudio = pflAudio;
			flVideo = pflVideo;
			//.
			Device = pDevice;
			UserAgent = pUserAgent;
			Object = null;
			SessionServerClient = null;
			//.
			AudioCalling = null;
		}
		
		public String GetSessionID() {
			return Value;
		}
		
		public void SetSessionID(String pValue) {
			Value = pValue;
		}
		
		@Override
		public void Clear() throws Exception {
			FreeSessionServerClient();
		}
		
		@Override
		public synchronized void Assign(TUserAccessKey UAC) {
			super.Assign(UAC);
			//.
			if (UAC instanceof TSession) {
				TSession O = (TSession)UAC;
				//.
				InitiatorID = O.InitiatorID;
				InitiatorName = O.InitiatorName;
				idTComponent = O.idTComponent;
				idComponent = O.idComponent;
				//.
				flAudio = O.flAudio;
				flVideo = O.flVideo;
				//.
				Device = O.Device;
				UserAgent = O.UserAgent;
				Object = O.Object;
				Panel = O.Panel;
				//.
				AudioCalling = O.AudioCalling;
			}
		}
		
		public synchronized Activity GetPanel() {
			return Panel;
		}
		
		public synchronized void SetPanel(Activity Value) {
			Panel = Value;
		}
		
		public void FreeSessionServerClient() throws Exception {
			if (SessionServerClient != null) {
				SessionServerClient.Destroy();
				SessionServerClient = null;
			}
		}
		
		public int GetStatus() {
			synchronized (StatusSignal) {
				return Status;
			}
		}
		
		public void SetStatus(int pResult) {
			synchronized (StatusSignal) {
				Status = pResult;
			}
		}
		
		public int WaitForStatus(int pStatus, int Delay) throws InterruptedException {
			int WaitInterval = 100;
			int WaitCount = Delay/WaitInterval;
			for (int I = 0; I < WaitCount; I++) {
				synchronized (StatusSignal) {
					if (!((TSession.SESSION_STATUS_ZERO <= Status) && (Status < pStatus)))
						return Status; //. ->
				}
				Thread.sleep(WaitInterval); 
			}
			synchronized (StatusSignal) {
				return Status; //. ->
			}
		}
		
		public int WaitForStatus(int pStatus) throws InterruptedException {
			return WaitForStatus(pStatus,SessionTimeout);
		}
	}
		
	public static class TSessionServer {
		
		private static final int IncomingCallNotificationID = 160813;
		
		private static final int MESSAGE_CALL_SESSION 		= 1;	
		private static final int MESSAGE_REJECT_SESSION		= 2;	
		private static final int MESSAGE_ACCEPT_SESSION 	= 3;	
		private static final int MESSAGE_CONTACT_SESSION 	= 4;	
		private static final int MESSAGE_FINISH_SESSION		= 5;	
		
		private TSession _Session = null;
		
		public TSessionServer() {
		}

		public synchronized TSession Session_Get() {
			return _Session;
		}
		
		public synchronized TSession Session_Get(String SessionID) {
			if (_Session == null)
				return null; //. ->
			if (_Session.IsTheSame(SessionID))
				return _Session; //. ->
			else
				return null; //. ->
		}
		
		public synchronized TSession Session_Set(TSession pSession) {
			TSession Last = _Session;
			_Session = pSession;
			return Last;
		}
		
		public synchronized boolean Session_SetForStatus(TSession pSession, int pSessionStatus) {
			if (pSession != _Session) {
				if (_Session != null) {
					if (pSessionStatus < _Session.GetStatus())
						return false; //. ->
					else
						_Session.SetStatus(TSession.SESSION_STATUS_CANCELLED);
				}
				_Session = pSession;
			}
			_Session.SetStatus(pSessionStatus);
			return true;
		}
		
		public synchronized TSession Session_SetIfItIsNotTheSame(TSession pSession) {
			if (_Session != pSession) {
				if (_Session != null) {
					if (_Session.IsTheSame(pSession)) 
						_Session.Assign(pSession);
					else
						_Session = pSession;
				}
				else
					_Session = pSession;
			}
			return _Session; //. ->
		}
		
		public synchronized boolean Session_Clear(TSession pSession) throws Exception {
			if (_Session != null) {
				if (_Session == pSession) {
					_Session.Clear();
					_Session = null;
					return true; //. ->
				}
				else 
					if (_Session.IsTheSame(pSession)) {
						_Session.Clear();
						_Session = null;
						return true; //. ->
					}
			}
			return false;
		}
		
		public synchronized boolean Session_IsActive() {
			return (_Session != null);
		}
		
		public synchronized boolean Session_IsTheSameTo(TSession Session) {
			if (_Session == null)
				return false; //. ->
			return (_Session.IsTheSame(Session));
		}
		
		
		@SuppressLint("HandlerLeak")
		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
	        	try {
					switch (msg.what) {

					case MESSAGE_CALL_SESSION:
						TSession Session = (TSession)msg.obj;
						//.
						StartSessionUserCalling(Session);
			        	//.
						break; // . >
						
					case MESSAGE_ACCEPT_SESSION:
						Session = (TSession)msg.obj; 
						//.
			        	StopSessionUserCalling(Session);
						//.
						TGeoScopeServerUser User;
						TGeoScopeServerInfo.TInfo ServersInfo;
			    		try {
			    			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			    			if (UserAgent == null)
			    				throw new Exception(Session.Device.context.getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    			User = UserAgent.User();
							ServersInfo = UserAgent.Server.Info.GetInfo();
							if (!ServersInfo.IsGeographProxyServerValid()) 
								throw new Exception(Session.Device.context.getString(R.string.SInvalidGeographProxyServer)); //. =>
					        //.
					        Session.UserAgent = UserAgent;
					        Session.Object = new TCoGeoMonitorObject(UserAgent.Server, Session.idComponent);
						} catch (Exception E) {
					    	Toast.makeText(Session.Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
					    	return; //. ->
						}
				        //.
						TVideoRecorderServerVideoPhoneServer.Session = Session;
						Intent intent = new Intent(Session.Device.context, TVideoRecorderServerVideoPhoneServer.class);
	            		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	            		//.
			        	intent.putExtra("GeographProxyServerAddress",ServersInfo.GeographProxyServerAddress);
			        	intent.putExtra("GeographProxyServerPort",ServersInfo.GeographProxyServerPort);
			        	intent.putExtra("UserID",User.UserID);
			        	intent.putExtra("UserPassword",User.UserPassword);
			        	//.
			        	Session.Device.context.startActivity(intent);
			        	//.
						break; // . >

					case MESSAGE_CONTACT_SESSION:
						Session = (TSession)msg.obj;
						//.
						TVideoRecorderServerVideoPhoneServer ContactPanel = (TVideoRecorderServerVideoPhoneServer)Session.GetPanel();
						if (ContactPanel != null)
				    		try {
				    			ContactPanel.ContactSession();
				    			//.
				    			Session_SetForStatus(Session,TSession.SESSION_STATUS_CONTACT);
							} catch (Exception E) {
						    	Toast.makeText(Session.Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
						    	return; //. ->
							}
						//.
						break; // . >
						
					case MESSAGE_FINISH_SESSION:
						Session = (TSession)msg.obj;
						//.
			        	StopSessionUserCalling(Session);
						//.
						Activity Panel = Session.GetPanel();
						if (Panel != null)
							Panel.finish();
						//.
						break; // . >

					case MESSAGE_REJECT_SESSION:
						Session = (TSession)msg.obj;
						//.
			        	StopSessionUserCalling(Session);
						//.
						Panel = Session.GetPanel();
						if (Panel != null)
							Panel.finish();
						//.
						break; // . >
					}
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
			}
		};
		
		public boolean StartSession(TSession Session) {
			return Session_SetForStatus(Session,TSession.SESSION_STATUS_STARTED);
		}
		
		public boolean CallSession(TSession Session) {
			Session.SetStatus(TSession.SESSION_STATUS_CALL);
			MessageHandler.obtainMessage(MESSAGE_CALL_SESSION,Session).sendToTarget();
			return true;
		}
		
		@SuppressWarnings("deprecation")
		private void StartSessionUserCalling(TSession Session) {
			//. start audio calling
			Session.AudioCalling = new TVideoRecorderServerVideoPhoneCallNotificationPanel.TAudioCalling(Session.Device.context);
			//. start visual calling
			TReflector Reflector = TReflector.GetReflector(); 
			if (Reflector == null) {
		        Intent intent = new Intent(Session.Device.context, TVideoRecorderServerVideoPhoneCallNotificationPanel.class);
	        	intent.putExtra("InitiatorID",Session.InitiatorID);
	        	intent.putExtra("InitiatorName",Session.InitiatorName);
	        	intent.putExtra("AudioNotification",0);
	        	//.
	        	TVideoRecorderServerVideoPhoneCallNotificationPanel.Session = Session;
		        //.
		        PendingIntent ContentIntent = PendingIntent.getActivity(Session.Device.context, 0, intent, 0);
		        //.
		        CharSequence TickerText = Session.Device.context.getString(R.string.SAttentionIncomingCall);
		        long Timestamp = System.currentTimeMillis();
		        int Icon = R.drawable.icon;
				Notification notification = new Notification(Icon,TickerText,Timestamp);
		        CharSequence ContentTitle = Session.Device.context.getString(R.string.SAttentionIncomingCall)+" "+Session.InitiatorName+".";
		        CharSequence ContentText = Session.Device.context.getString(R.string.SClickHereToSee);
		        notification.setLatestEventInfo(Session.Device.context, ContentTitle, ContentText, ContentIntent);
		        notification.defaults = (notification.defaults | Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
		        notification.flags = (notification.flags | Notification.FLAG_AUTO_CANCEL);
		        //.
		        NotificationManager nm = (NotificationManager)Session.Device.context.getSystemService(Context.NOTIFICATION_SERVICE);
		        nm.notify(IncomingCallNotificationID, notification);
			}
			else {
				Intent intent = new Intent(Reflector, TVideoRecorderServerVideoPhoneCallNotificationPanel.class);
	        	intent.putExtra("InitiatorID",Session.InitiatorID);
	        	intent.putExtra("InitiatorName",Session.InitiatorName);
	        	intent.putExtra("AudioNotification",1);
	        	//.
	        	TVideoRecorderServerVideoPhoneCallNotificationPanel.Session = Session;
	        	//.
	        	Reflector.startActivity(intent);
			}
		}
		
		private void StopSessionUserCalling(TSession Session) throws InterruptedException {
        	if (Session.AudioCalling != null) {
        		Session.AudioCalling.Destroy();
        		Session.AudioCalling = null;
        	}
        	//.
        	NotificationManager nm = (NotificationManager)Session.Device.context.getSystemService(Context.NOTIFICATION_SERVICE);
	        nm.cancel(IncomingCallNotificationID);
		}
		
		public void AcceptSession(TSession Session) {
			Session.SetStatus(TSession.SESSION_STATUS_ACCEPTED);
			MessageHandler.obtainMessage(MESSAGE_ACCEPT_SESSION,Session).sendToTarget();
		}
		
		public TSession OpenSession(TSession Session) throws Exception {
	    	InitializeSessionAudioVideo(Session);
	    	//.
	    	if (Session.flAudio)
	    		Session.Device.AudioModule.UserAccessKey.Assign(Session);
	    	else
	    		Session.Device.AudioModule.UserAccessKey.Clear();
	    	if (Session.flVideo)
	    		Session.Device.VideoModule.UserAccessKey.Assign(Session);
	    	else
	    		Session.Device.VideoModule.UserAccessKey.Clear();
	    	//.
	    	Session = Session_SetIfItIsNotTheSame(Session);
	    	//.
			Session.SetStatus(TSession.SESSION_STATUS_OPENED);
			//.
			return Session;
		}
		
		public boolean ContactSession(TSession Session) {
			MessageHandler.obtainMessage(MESSAGE_CONTACT_SESSION,Session).sendToTarget();
			return true;
		}
		
		public void InitializeSessionAudioVideo(TSession Session) throws Exception {
			Session.Device.VideoRecorderModule.LoadVideoPhoneProfile();
	    	Session.Device.VideoRecorderModule.SetupRecordingLocally(TVideoRecorderModule.MODE_FRAMESTREAM, Session.flAudio,Session.flVideo, true, false);
		}
		
		public void FinalizeSessionAudioVideo(TSession Session) throws Exception {
			Session.Device.VideoRecorderModule.CancelRecordingLocally();
			Session.Device.VideoRecorderModule.LoadProfile();
		}
		
		public void CloseSession(TSession Session) throws Exception {
	    	Session_Clear(Session);
	    	//.
	    	Session.Device.AudioModule.UserAccessKey.Clear();
	    	Session.Device.VideoModule.UserAccessKey.Clear();
	    	//.
			FinalizeSessionAudioVideo(Session);
			//.
	    	Session.Device.LANModule.ConnectionRepeaters_CancelByUserAccessKey(Session.GetValue());
			//.
			Session.SetStatus(TSession.SESSION_STATUS_CLOSED);
		}
		
		public void RejectSession(TSession Session) throws Exception {
			Session.SetStatus(TSession.SESSION_STATUS_REJECTED);
	    	if (Session_Clear(Session))
	    		MessageHandler.obtainMessage(MESSAGE_REJECT_SESSION,Session).sendToTarget();
		}		

		public void FinishSession(TSession Session) throws Exception {
			Session.SetStatus(TSession.SESSION_STATUS_FINISHED);
	    	if (Session_Clear(Session))
	    		MessageHandler.obtainMessage(MESSAGE_FINISH_SESSION,Session).sendToTarget();
		}		
		
		public String StartRemoteSessionForObject0(Context context, TCoGeoMonitorObject Object, int InitiatorID, String InitiatorName, int InitiatorComponentType, int InitiatorComponentID, boolean flAudio, boolean flVideo) throws Exception {
			String SessionID = TVideoRecorderServerVideoPhoneServer.TSession.GenerateValue();
			//.
			int AV = 0;
			if (flAudio)
				AV = 1;
			int VV = 0;
			if (flVideo)
				VV = 1;
			//. start session request
			String Params = "201,"+"1"/*Version*/+","+Integer.toString(InitiatorID)+","+InitiatorName+","+Integer.toString(InitiatorComponentType)+","+Integer.toString(InitiatorComponentID)+","+SessionID+","+Integer.toString(AV)+","+Integer.toString(VV);
			int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
			byte[] Data = Params.getBytes("windows-1251");
			try {
				Object.SetData(DataType, Data);
			}
			catch (Exception E) {
				String ES = E.getMessage();
				String RCPrefix = "RC: ";
				int RCP = ES.indexOf(RCPrefix);
				if (RCP >= 0) {
					String RCS  = ES.substring(RCP+RCPrefix.length());
					int RC = Integer.parseInt(RCS);
					switch (RC) {
					
					case TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable:
						throw new Exception(context.getString(R.string.SSubscriberIsUnavailable)); //. =>
						
					case TGetControlDataValueSO.OperationErrorCode_SourceAccessIsDenied:
						throw new Exception(context.getString(R.string.SSubscriberAccessIsDenied)); //. =>
						
					case TGetControlDataValueSO.OperationErrorCode_SourceIsBusy:
						throw new Exception(context.getString(R.string.SSubscriberIsBusy)); //. =>
						
					case TGetControlDataValueSO.OperationErrorCode_SourceIsTimedout:
						throw new Exception(context.getString(R.string.SSubscriberIsNotRespond)); //. =>

					default:
						throw new Exception(context.getString(R.string.SUnknownSubscriberError)+Integer.toString(RC)); //. =>
					}
				}
				else
					throw E; //. =>
			}
			//.
			return SessionID;
		}
		
		public String StartRemoteSessionForObject(Context context, TCoGeoMonitorObject Object, int InitiatorID, String InitiatorName, int InitiatorComponentType, int InitiatorComponentID, boolean flAudio, boolean flVideo) throws Exception {
			String SessionID = TVideoRecorderServerVideoPhoneServer.TSession.GenerateValue();
			//.
			int AV = 0;
			if (flAudio)
				AV = 1;
			int VV = 0;
			if (flVideo)
				VV = 1;
			//. start session request
			String Params = "201,"+"1"/*Version*/+","+Integer.toString(InitiatorID)+","+InitiatorName+","+Integer.toString(InitiatorComponentType)+","+Integer.toString(InitiatorComponentID)+","+SessionID+","+Integer.toString(AV)+","+Integer.toString(VV);
			//.
			byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
			byte[] _AddressData = Params.getBytes("windows-1251");
			try {
				Object.GeographServerClient().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
			}
			catch (OperationException OE) {
				switch (OE.Code) {

				case TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable:
					throw new Exception(context.getString(R.string.SSubscriberIsUnavailable)); //. =>
					
				case TGetControlDataValueSO.OperationErrorCode_SourceAccessIsDenied:
					throw new Exception(context.getString(R.string.SSubscriberAccessIsDenied)); //. =>
					
				case TGetControlDataValueSO.OperationErrorCode_SourceIsBusy:
					throw new Exception(context.getString(R.string.SSubscriberIsBusy)); //. =>
					
				case TGetControlDataValueSO.OperationErrorCode_SourceIsTimedout:
					throw new Exception(context.getString(R.string.SSubscriberIsNotRespond)); //. =>

				default:
					throw OE; //. =>
				}
			}
			//.
			return SessionID;
		}
		
		public void FinishRemoteSessionForObject0(TCoGeoMonitorObject Object, String SessionID) throws Exception {
			String Params = "202,"+"1"/*Version*/+","+SessionID;
			int DataType = 1000000/*ObjectModel base*/+101/*GMO1 Object Model*/*1000+1/*ControlModule.ControlDataValue.ReadDeviceByAddressDataCUAC(Data)*/;
			byte[] Data = Params.getBytes("US-ASCII");
			Object.SetData(DataType, Data);
		}

		public void FinishRemoteSessionForObject(Context context, TCoGeoMonitorObject Object, String SessionID) throws Exception {
			String Params = "202,"+"1"/*Version*/+","+SessionID;
			//.
			byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
			byte[] _AddressData = Params.getBytes("US-ASCII");
			try {
				Object.GeographServerClient().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
			}
			catch (OperationException OE) {
				switch (OE.Code) {

				case TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable:
					throw new Exception(context.getString(R.string.SSubscriberIsUnavailable)); //. =>
					
				case TGetControlDataValueSO.OperationErrorCode_SourceAccessIsDenied:
					throw new Exception(context.getString(R.string.SSubscriberAccessIsDenied)); //. =>
					
				case TGetControlDataValueSO.OperationErrorCode_SourceIsBusy:
					throw new Exception(context.getString(R.string.SSubscriberIsBusy)); //. =>
					
				case TGetControlDataValueSO.OperationErrorCode_SourceIsTimedout:
					throw new Exception(context.getString(R.string.SSubscriberIsNotRespond)); //. =>

				default:
					throw OE; //. =>
				}
			}
		}
	}
	
	public static TSessionServer SessionServer = new TSessionServer();
	
	public static class TSessionServerClient {

		public static final int ServerPort = TVideoPhoneServerLANLVConnectionRepeater.Port;
		private static final int LocalPort = 3000;

		public static class SessionException extends IOException {
			
			private static final long serialVersionUID = 1L;
			
			public int Code;
			
			public SessionException(int pCode, String pMessage) {
				super(pMessage);
				Code = pCode;
			}
		}
		
		public class TLocalClient {
			
			public static final int ConnectionTimeout = 1000*30; //. seconds

			private int Port;
			//.
			private Socket 			Connection = null;
			private InputStream 	ConnectionInputStream = null;
			private OutputStream 	ConnectionOutputStream = null;
			//.
			private boolean flDisconnectGracefully;

			public TLocalClient(int pPort) {
				Port = pPort;
			}
			
			public void Destroy() throws IOException {
				Disconnect();
			}
			
			private int InputStream_ReadDescriptor(InputStream IS) throws IOException {
				final byte[] DescriptorBA = new byte[4];
                int ActualSize = IS.read(DescriptorBA,0,DescriptorBA.length);
		    	if (ActualSize == 0)
	    			throw new IOException("connection is closed unexpectedly"); //. =>
		    		else 
				    	if (ActualSize < 0) {
					    	if (ActualSize == -1)
				    			throw new IOException("connection is EOF"); //. =>
					    	else
					    		throw new IOException("error of reading server socket descriptor, RC: "+Integer.toString(ActualSize)); //. =>
				    	}
				if (ActualSize != DescriptorBA.length)
					throw new IOException("wrong data descriptor"); //. =>
				int Descriptor = (DescriptorBA[3] << 24)+((DescriptorBA[2] & 0xFF) << 16)+((DescriptorBA[1] & 0xFF) << 8)+(DescriptorBA[0] & 0xFF);
				return Descriptor;
			}
			
			public int Connect() throws Exception {
				flDisconnectGracefully = false;
				//.
				Connection = new Socket("127.0.0.1", Port);
				Connection.setSoTimeout(ConnectionTimeout);
				//.
				ConnectionInputStream = Connection.getInputStream();
				ConnectionOutputStream = Connection.getOutputStream();
				//.
				String SessionID = Session.GetValue();
				int SessionIDSize = SessionID.length();
				byte[] InitBuffer = new byte[4/*SizeOf(Version)*/+4/*SizeOf(SessionIDSize)*/+SessionIDSize+4/*SizeOf(SessionTimeout)*/];
				int Idx = 0;
				//.
				int Version = 1;
				byte[] DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(Version);
				System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
				//. 
				DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(SessionIDSize);
				System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
				//. 
				if (SessionIDSize > 0) {
					byte[] BA = SessionID.getBytes("US-ASCII");
					System.arraycopy(BA,0, InitBuffer,Idx, SessionIDSize); Idx += SessionIDSize;
				}
				//.
				int SessionTimeout = TVideoRecorderServerVideoPhoneServer.TSession.SessionTimeout;
				DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(SessionTimeout);
				System.arraycopy(DescriptorBA,0, InitBuffer,Idx, DescriptorBA.length); Idx += DescriptorBA.length;
				//.
				ConnectionOutputStream.write(InitBuffer);
				//. get login result
				int RC = InputStream_ReadDescriptor(ConnectionInputStream);
				if (RC != TVideoPhoneServerLANLVConnectionRepeater.SuccessCode_OK) {
					switch (RC) {
					
					case TVideoPhoneServerLANLVConnectionRepeater.ErrorCode_SessionIsNotFound:
						return TSession.SESSION_STATUS_REJECTED; //. ->
						
					default:
						throw new SessionException(RC,"error of logging-in the session server"); //. =>
					}
				}
				//. get session status
				Connection.setSoTimeout(TSession.SessionMaxTimeout);
				RC = InputStream_ReadDescriptor(ConnectionInputStream);
				flDisconnectGracefully = (RC >= TSession.SESSION_STATUS_ZERO);
				return RC; //. ->
			}
			
			public void Disconnect() throws IOException {
				if (ConnectionOutputStream != null) {
					if (flDisconnectGracefully) {
						//. disconnect the server
						byte[] DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(TVideoPhoneServerLANLVConnectionRepeater.ClientCommand_Disconnect);
						ConnectionOutputStream.write(DescriptorBA);
					}
					//.
					ConnectionOutputStream.close();
					ConnectionOutputStream = null;
				}
				if (ConnectionInputStream != null) {
					ConnectionInputStream.close();
					ConnectionInputStream = null;
				}
				if (Connection != null) {
					Connection.close();
					Connection = null;
				}
			}
			
			public boolean IsConnected() {
				return (Connection != null);
			}
			
			public void ContactSession() throws IOException {
				byte[] DescriptorBA = TDataConverter.ConvertInt32ToLEByteArray(TVideoPhoneServerLANLVConnectionRepeater.ClientCommand_ContactSession);
				ConnectionOutputStream.write(DescriptorBA);
				//.
				int RC = InputStream_ReadDescriptor(ConnectionInputStream);
				if (RC != TSession.SESSION_STATUS_CONTACT) 
					throw new SessionException(RC,"error of setting the session to contact state"); //. =>
			}
		}
		
		private Context context;
		//.
		private String 	GeographProxyServerAddress = "";
		private int 	GeographProxyServerPort = 0;
		private int		UserID;
		private String	UserPassword;
		//.
		private TSession Session;
		//.
		private TExceptionHandler ExceptionHandler;
		//.
		private TLANConnectionRepeater 	LocalServer = null;
		private TLocalClient			LocalClient = null;
		
		public TSessionServerClient(Context pcontext, String pGeographProxyServerAddress, int pGeographProxyServerPort, int pUserID, String pUserPassword, TSession pSession, TExceptionHandler pExceptionHandler) throws Exception {
	    	context = pcontext;
	    	//.
	    	GeographProxyServerAddress = pGeographProxyServerAddress;
	    	GeographProxyServerPort = pGeographProxyServerPort;
	    	UserID = pUserID;
	    	UserPassword = pUserPassword;
	    	//.
	    	Session = pSession;  
	    	//.
	    	ExceptionHandler = pExceptionHandler;
	    	//.
	    	Connect();
		}
		
		public void Destroy() throws IOException, InterruptedException {
			Disconnect();
		}
		
		private void Connect() throws Exception {
			TLANConnectionExceptionHandler ExceptionHandler = new TLANConnectionExceptionHandler() {
				@Override
				public void DoOnException(Throwable E) {
					TSessionServerClient.this.ExceptionHandler.DoOnException(E);
				}
			};		
			TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model();
			TLANConnectionStartHandler StartHandler = ObjectModel.TLANConnectionStartHandler_Create(Session.Object);
			TLANConnectionStopHandler StopHandler = ObjectModel.TLANConnectionStopHandler_Create(Session.Object); 
			//.
			LocalServer = new TLANConnectionRepeater(LANConnectionRepeaterDefines.CONNECTIONTYPE_NORMAL, "127.0.0.1",ServerPort, LocalPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Session.Object.GeographServerObjectID(), Session.GetValue(), ExceptionHandler, StartHandler,StopHandler);
			//.
			LocalClient = new TLocalClient(LocalServer.GetPort());
		}
		
		private void Disconnect() throws IOException, InterruptedException {
			if (LocalClient != null) {
				LocalClient.Destroy();
				LocalClient = null;
			}
			//.
			if (LocalServer != null) {
				LocalServer.Destroy();
				LocalServer = null;
			}
		}
		
		public void WaitForSessionOpen() throws Exception {
			if (LocalClient.IsConnected())
				throw new Exception("session server client is already connected"); //. =>
			int SessionStatus = LocalClient.Connect();
			switch (SessionStatus) {
			
			case TSession.SESSION_STATUS_FINISHED:
				throw new Exception(context.getString(R.string.SSessionIsFinished)); //. =>
				
			case TSession.SESSION_STATUS_CLOSED:
				throw new Exception(context.getString(R.string.SSessionIsClosed)); //. =>
				
			case TSession.SESSION_STATUS_OPENED:
				break; //. ->
				
			case TSession.SESSION_STATUS_ACCEPTED:
				throw new Exception(context.getString(R.string.SSessionIsAcceptedButNotOpened)); //. =>
				
			case TSession.SESSION_STATUS_CALL:
				throw new Exception(context.getString(R.string.SSessionIsCallingButNoAnswer)); //. =>
			
			case TSession.SESSION_STATUS_STARTED:
				throw new Exception(context.getString(R.string.SSessionIsStartedButNoChange)); //. =>
			
			case TSession.SESSION_STATUS_ZERO:
				throw new Exception(context.getString(R.string.SSessionTimeout)); //. =>
			
			case TSession.SESSION_STATUS_ERROR:
				throw new Exception(context.getString(R.string.SSessionError)); //. =>
				
			case TSession.SESSION_STATUS_CANCELLED:
				throw new Exception(context.getString(R.string.SSessionIsCancelled)); //. =>
				
			case TSession.SESSION_STATUS_REJECTED:
				throw new Exception(context.getString(R.string.SSessionIsRejected)); //. =>
				
			default:
				throw new Exception("unknown session status, status: "+Integer.toString(SessionStatus)); //. =>
			}
		}
		
		public void ContactSession() throws Exception {
			if (!LocalClient.IsConnected())
				throw new Exception("session server client is not connected"); //. =>
			LocalClient.ContactSession();
		}
	}
	
	public static TSession Session = null;
	//.
	private TVideoRecorderServerView VideoRecorderServerView;
	//.
	private boolean IsInFront = false;
	//.
	private TAsyncProcessing Initializing = null;
	private TAsyncProcessing Finalizing = null;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
        svSurface.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ShowInitializationDialog();
			}
		});
        //.
        Session.Panel = this;
        //.
        Bundle extras = getIntent().getExtras();
    	//.
    	VideoRecorderServerView = new TVideoRecorderServerViewUDPRTP(this,extras.getString("GeographProxyServerAddress"), TUDPEchoServerClient.ServerDefaultPort, extras.getInt("UserID"), extras.getString("UserPassword"), Session.Object, Session.flAudio, Session.flVideo, Session.GetValue(), new TExceptionHandler() {
			@Override
			public void DoOnException(Throwable E) {
				TVideoRecorderServerVideoPhoneServer.this.DoOnException(E);
			}
		}, lbStatus);
    	//.
        SetSurface(true);
    }
	
    public void onDestroy() {
    	try {
			FinalizeSession(); //. finalize additionally if DoOnSurfaceIsDestroyed() is not called 
		} catch (Exception E) {
		}
    	//.
    	if (Finalizing != null) {
    		try {
				Finalizing.Destroy();
			} catch (InterruptedException IE) {
			}
    		Finalizing = null;
    	}
		if (VideoRecorderServerView != null) {
			VideoRecorderServerView.Destroy();
			VideoRecorderServerView = null;
		}
    	//.
		super.onDestroy();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		IsInFront = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		IsInFront = true;
	}

    private TAsyncProcessing StartInitializing() {
		TAsyncProcessing Processing = new TAsyncProcessing(this) {
			@Override
			public void Process() throws Exception {
				Session.Object.CheckData();
	    		//.
				InitializeSession();
				//. if we are the caller so send a "contact" command to the remote side 
				if (Session.SessionServerClient != null) {
					Session.SessionServerClient.ContactSession();
	    			Session.FreeSessionServerClient();
					//.
		    		VideoRecorderServerView.Initialize();
				}
			}
			@Override 
			public void DoOnCompleted() throws Exception {
	    		VideoRecorderServerView.UpdateInfo();
			}
			@Override
			public void DoOnException(Exception E) {
				TVideoRecorderServerVideoPhoneServer.this.DoOnException(E);
			}
		};
		Processing.Start();
		//.
		return Processing;
    }
    
    private TAsyncProcessing StartFinalizing() {
		TAsyncProcessing Processing = new TAsyncProcessing(null) {
			@Override
		    public void DoOnStart() throws Exception {
				CancelSession();
		    }
			@Override
			public void Process() throws Exception {
				FinalizeSession();
				//.
				VideoRecorderServerView.Finalize();
			}
			@Override 
			public void DoOnCompleted() throws Exception {
			}
			@Override
			public void DoOnException(Exception E) {
				TVideoRecorderServerVideoPhoneServer.this.DoOnException(E);
			}
		};
		Processing.Start();
		//.
		return Processing;
    }
    
    private void InitializeSession() throws Exception {
    	Session = SessionServer.OpenSession(Session);
    	//.
    	Session.SetPanel(this);
    }
    
    private void FinalizeSession() throws Exception {
    	SessionServer.CloseSession(Session);
    }
    
	private void CancelSession() throws Exception {
		if (Session.Object == null)
			return; //. ->
		TAsyncProcessing Processing = new TAsyncProcessing(null) {
			@Override
			public void Process() throws Exception {
				TVideoRecorderServerVideoPhoneServer.SessionServer.FinishRemoteSessionForObject(TVideoRecorderServerVideoPhoneServer.this, Session.Object, Session.GetValue());
			}
			@Override 
			public void DoOnCompleted() throws Exception {
			}
			@Override
			public void DoOnException(Exception E) {
				TVideoRecorderServerVideoPhoneServer.this.DoOnException(E);
			}
		};
		Processing.Start();
	}
	
    public void ContactSession() throws Exception {
		VideoRecorderServerView.Initialize();
		//.
		VideoRecorderServerView.UpdateInfo();
    }
    
	@Override
	public void DoOnSurfaceIsCreated(SurfaceHolder SH) {
	}
	
	@Override
	public void DoOnSurfaceIsChanged(SurfaceHolder SH, int Format, int Width, int Height) {
		VideoRecorderServerView.VideoSurface_Set(SH, Width,Height);
		//.
		if (Initializing == null)
			Initializing = StartInitializing();
	}
	
	@Override
	public void DoOnSurfaceIsDestroyed(SurfaceHolder SH) {
    	if (Initializing != null) {
    		Initializing.Cancel();
    		Initializing = null;
    	}
    	//.
    	if (Finalizing == null)
    		Finalizing = StartFinalizing();
		//.
		VideoRecorderServerView.VideoSurface_Clear(SH);
	}
	
	private void ShowInitializationDialog() {
		int IC = 0;
		if (Session.flAudio)
			IC++;
		if (Session.flVideo)
			IC++;
    	final CharSequence[] _items = new CharSequence[IC];
    	final boolean[] Mask = new boolean[_items.length];
    	IC = 0;
    	if (Session.flAudio) {
    		_items[IC] = getString(R.string.SAudio);
    		Mask[IC] = VideoRecorderServerView.flAudio;
    		//.
    		IC++;
    	}
    	if (Session.flVideo) {
    		_items[IC] = getString(R.string.SVideo);
    		Mask[IC] = VideoRecorderServerView.flVideo;
    		//.
    		IC++;
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.SMode);
    	builder.setPositiveButton(getString(R.string.SOk), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					VideoRecorderServerView.Reinitialize();
					//.
					VideoRecorderServerView.UpdateInfo();
				} catch (Exception E) {
					DoOnException(E);
				}
			}
		});
    	builder.setNegativeButton(getString(R.string.SClose),null);
    	builder.setMultiChoiceItems(_items, Mask, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				switch (arg1) {
				
				case 0:
					if (Session.flAudio)
						VideoRecorderServerView.flAudio = arg2;
					else
						if (Session.flVideo)
							VideoRecorderServerView.flVideo = arg2;
					break; //. >
					
				case 1:
					if (Session.flVideo)
						VideoRecorderServerView.flVideo = arg2;
					break; //. >
				}
			}
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
	}
	
	private static final int MESSAGE_SHOWEXCEPTION 	= 1;
	
	@SuppressLint("HandlerLeak")
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_SHOWEXCEPTION:
    				Throwable E = (Throwable)msg.obj;
    				String EM = E.getMessage();
    				if (EM == null) 
    					EM = E.getClass().getName();
    				//.
    				Toast.makeText(TVideoRecorderServerVideoPhoneServer.this,EM,Toast.LENGTH_LONG).show();
    				// .
    				break; // . >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}	
}
