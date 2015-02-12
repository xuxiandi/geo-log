package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging.TUserStatusDataType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographServerClient;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("HandlerLeak")
public class TUserMessagingModule extends TModule {

	public static String Folder() {
		return TInternalSensorsModule.Folder()+"/"+"UserMessagingModule";
	}
	
	public static class TUserMessaging {
	
		private static int NextID = 0;
		//.
		private static synchronized int GetNextID() {
			NextID++;
			return NextID; 
		}
		
		private TUserMessagings UserMessagings;
		//.
		public int ID;
		//.
		public long 				ObjectID = 0;
		public TCoGeoMonitorObject 	Object = null;
		//.
		public com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM.TLUMChannel 											OutChannel = null;
		public com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM.TLUMChannel	InChannel = null;
		//.
		public int CallingNotificationID = 0;
		
		public TUserMessaging(TUserMessagings pUserMessagings, long pObjectID, TCoGeoMonitorObject pObject, String pSessionID) {
			UserMessagings = pUserMessagings;
			//.
			ID = GetNextID();
			//.
			ObjectID = pObjectID;
			Object = pObject;
			//.
			OutChannel = new com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM.TLUMChannel(UserMessagings.UserMessagingModule.InternalSensorsModule);
			if (pSessionID != null)
				OutChannel.UserAccessKey = pSessionID;
			else
				OutChannel.UserAccessKey = TUserAccessKey.GenerateValue(); //. new session
			//.
			InChannel = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.UserMessaging.LUM.TLUMChannel();  
		}

		public String SessionID() {
			return OutChannel.UserAccessKey;
		}
	}
	
	public static class TUserMessagings {
		
		protected TUserMessagingModule UserMessagingModule;
		//.
		ArrayList<TUserMessaging> Items = new ArrayList<TUserMessaging>();
		
		public TUserMessagings(TUserMessagingModule pUserMessagingModule) {
			UserMessagingModule = pUserMessagingModule;
		}
		
		public synchronized TUserMessaging AddMessaging(TUserMessaging UserMessaging) {
			Items.add(UserMessaging);
			return UserMessaging;
		}

		public synchronized void RemoveMessaging(TUserMessaging UserMessaging) {
			Items.remove(UserMessaging);
		}
		
		public synchronized TUserMessaging GetItemByID(int pID) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TUserMessaging Item = Items.get(I);
				if (Item.ID == pID)
					return Item; //. ->
			}
			return null; 
		}

		public synchronized TUserMessaging GetItemBySession(String SessionID) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TUserMessaging Item = Items.get(I);
				if (Item.SessionID().equals(SessionID))
					return Item; //. ->
			}
			return null; 
		}

		public synchronized TUserMessaging GetItemByOutChannelTypeAndSession(String OutChannelTypeID, String SessionID) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TUserMessaging Item = Items.get(I);
				if (Item.OutChannel.GetTypeID().equals(OutChannelTypeID) && (Item.SessionID().equals(SessionID)))
					return Item; //. ->
			}
			return null; 
		}
	}
		
	public TInternalSensorsModule InternalSensorsModule;
	//.
	public TUserMessagings UserMessagings;
	
    public TUserMessagingModule(TInternalSensorsModule pInternalSensorsModule) throws Exception {
    	super(pInternalSensorsModule);
    	//.
    	Device = pInternalSensorsModule.Device;
    	InternalSensorsModule = pInternalSensorsModule;
    	//.
    	UserMessagings = new TUserMessagings(this);
    }
    
    public void Destroy() {
    }

	public TUserMessaging StartUserMessagingForObject(TCoGeoMonitorObject Object, long InitiatorID, String InitiatorName, int ComponentType, long ComponentID) throws Exception {
		TUserMessaging UserMessaging = new TUserMessaging(UserMessagings, Object.ID,Object, null/*new session*/);
		UserMessagings.AddMessaging(UserMessaging);
		//. start session request
		String Params = "211,"+"1"/*Version*/+","+Long.toString(InitiatorID)+","+InitiatorName+","+Integer.toString(ComponentType)+","+Long.toString(ComponentID)+","+UserMessaging.SessionID();
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
		byte[] _AddressData = Params.getBytes("windows-1251");
		try {
			Object.GeographServerObjectController().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable:
				throw new Exception(Device.context.getString(R.string.SSubscriberIsUnavailable)); //. =>
				
			case TGetControlDataValueSO.OperationErrorCode_SourceAccessIsDenied:
				throw new Exception(Device.context.getString(R.string.SSubscriberAccessIsDenied)); //. =>
				
			case TGetControlDataValueSO.OperationErrorCode_SourceIsBusy:
				throw new Exception(Device.context.getString(R.string.SSubscriberIsBusy)); //. =>
				
			case TGetControlDataValueSO.OperationErrorCode_SourceIsTimedout:
				throw new Exception(Device.context.getString(R.string.SSubscriberIsNotRespond)); //. =>

			default:
				throw OE; //. =>
			}
		}
		//.
		MessageHandler.obtainMessage(MESSAGE_USERMESSAGING_START,UserMessaging).sendToTarget();
		//.
		return UserMessaging;
	}

	public TUserMessaging StopUserMessaging(TCoGeoMonitorObject Object, String SessionID) throws Exception {
		TUserMessaging UserMessaging = UserMessagings.GetItemBySession(SessionID);
		UserMessagings.RemoveMessaging(UserMessaging);
		//. stop session request
		String Params = "212,"+"1"/*Version*/+","+SessionID;
		//.
		byte[] _Address = TGeographServerClient.GetAddressArray(new int[] {2,11,1000});
		byte[] _AddressData = Params.getBytes("windows-1251");
		try {
			Object.GeographServerObjectController().Component_ReadDeviceByAddressDataCUAC(_Address,_AddressData);
		}
		catch (OperationException OE) {
			switch (OE.Code) {

			case TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable:
				throw new Exception(Device.context.getString(R.string.SSubscriberIsUnavailable)); //. =>
				
			case TGetControlDataValueSO.OperationErrorCode_SourceAccessIsDenied:
				throw new Exception(Device.context.getString(R.string.SSubscriberAccessIsDenied)); //. =>
				
			case TGetControlDataValueSO.OperationErrorCode_SourceIsBusy:
				throw new Exception(Device.context.getString(R.string.SSubscriberIsBusy)); //. =>
				
			case TGetControlDataValueSO.OperationErrorCode_SourceIsTimedout:
				throw new Exception(Device.context.getString(R.string.SSubscriberIsNotRespond)); //. =>

			default:
				throw OE; //. =>
			}
		}
		//.
		return UserMessaging;
	}

	public TUserMessaging OpenUserMessagingFromInitiator(int InitiatorID, String InitiatorName, int ComponentType, long ComponentID, String SessionID) throws Exception {
		TUserMessaging UserMessaging = UserMessagings.GetItemBySession(SessionID);
		if (UserMessaging == null) {
			UserMessaging = new TUserMessaging(UserMessagings, ComponentID,null, SessionID);
			final TUserMessaging _UserMessaging = UserMessaging; 
			UserMessaging.OutChannel.DestinationChannel_PacketSubscribersItemsNotifier_Set(new com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscribers.TItemsNotifier() {
				
				@Override
				protected void DoOnSubscribed(com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel.TPacketSubscriber Subscriber) {
					TTimestampedInt16ContainerType.TValue Status = new TTimestampedInt16ContainerType.TValue(OleDate.UTCCurrentTimestamp(),TUserStatusDataType.USERSTATUS_CALLING);
					//.
					synchronized (_UserMessaging.OutChannel) {
						_UserMessaging.OutChannel.UserStatus.SetContainerTypeValue(Status);
						try {
							_UserMessaging.OutChannel.DoOnData(_UserMessaging.OutChannel.UserStatus, Subscriber);
						} catch (IOException E) {
						}
					}
				};
			});
			UserMessagings.AddMessaging(UserMessaging);
		}
		//.
    	PowerManager powerManager = (PowerManager)Device.context.getSystemService(Activity.POWER_SERVICE);
    	if (!powerManager.isScreenOn())
    		MessageHandler.obtainMessage(MESSAGE_USERMESSAGING_CALL,UserMessaging).sendToTarget();
    	else
    		MessageHandler.obtainMessage(MESSAGE_USERMESSAGING_OPEN,UserMessaging).sendToTarget();
		//.
		return UserMessaging;
	}
	
	public TUserMessaging CloseUserMessaging(String SessionID) throws Exception {
		TUserMessaging UserMessaging = UserMessagings.GetItemBySession(SessionID);
		if (UserMessaging != null) {
			UserMessagings.RemoveMessaging(UserMessaging);
			//.
			MessageHandler.obtainMessage(MESSAGE_USERMESSAGING_CLOSE,UserMessaging).sendToTarget();
		}
		//.
		return UserMessaging;
	}
	
	public TUserMessaging GetUserMessagingByOutChannelTypeAndSession(String OutChannelTypeID, String SessionID) {
		return UserMessagings.GetItemByOutChannelTypeAndSession(OutChannelTypeID, SessionID);
	}	
	
	public static final int MESSAGE_USERMESSAGING_START 	= 1;
	public static final int MESSAGE_USERMESSAGING_CALL 		= 2;
	public static final int MESSAGE_USERMESSAGING_OPEN 		= 3;
	public static final int MESSAGE_USERMESSAGING_CLOSE 	= 4;
	
	public final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_USERMESSAGING_START:
                	TUserMessaging UserMessaging = (TUserMessaging)msg.obj;
                	try {
                    	//. show the user messaging panel
			            Intent intent = new Intent(Device.context.getApplicationContext(), TUserMessagingPanel.class);
	    	        	intent.putExtra("UserMessagingID",UserMessaging.ID);
	    	        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	        	//.
	    	        	Device.context.startActivity(intent);
                	}
                	catch (Exception E) {
                		Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_USERMESSAGING_CALL: 
                	UserMessaging = (TUserMessaging)msg.obj;
                	try {
                		UserMessaging.CallingNotificationID = TUserMessagingPanel.Calling_GetNextNotificationID();
    		            Intent intent = new Intent(Device.context.getApplicationContext(), TUserMessagingPanel.class);
        	        	intent.putExtra("UserMessagingID",UserMessaging.ID);
	    	        	intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        	    		//.
                		TUserMessagingPanel.Calling_ShowNotification(UserMessaging, Device.context.getApplicationContext(), intent, UserMessaging.CallingNotificationID);
	    	        }
                	catch (Exception E) {
                		Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_USERMESSAGING_OPEN: 
                	UserMessaging = (TUserMessaging)msg.obj;
                	try {
                    	//. show the user messaging panel
                		Intent intent = new Intent(Device.context.getApplicationContext(), TUserMessagingPanel.class);
	    	        	intent.putExtra("UserMessagingID",UserMessaging.ID);
	    	    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	        	//.
	    	        	Device.context.startActivity(intent);
	    	        }
                	catch (Exception E) {
                		Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_USERMESSAGING_CLOSE: 
                	UserMessaging = (TUserMessaging)msg.obj;
                	//.
                	if (UserMessaging.CallingNotificationID != 0) {
                		TUserMessagingPanel.Calling_HideNotification(Device.context.getApplicationContext(), UserMessaging.CallingNotificationID);
                		UserMessaging.CallingNotificationID = 0;
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
