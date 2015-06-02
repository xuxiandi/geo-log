package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.LiveMessaging;

import org.w3c.dom.Element;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TUserPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL.TypeID+"."+"LiveMessaging";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode));
	}
	

	public TURL(long pidComponent) {
		super(pidComponent);
	}

	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}

	@Override
	public String GetTypeID() {
		return TypeID;
	}
		
	@Override
	public boolean IsAvailable() {
		return IsUserAvailable();
	}
	
	@Override
	public Bitmap GetThumbnailImage() {
		if (IsUserAvailable())
			return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.onlineuserlivemessaging); //. ->
		else
			return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineuserlivemessaging); //. ->
	}
	
	@Override
	public void Open(Context context) throws Exception {
		TAsyncProcessing Opening = new TAsyncProcessing(context) {
			
			private TUserPanel.TUserCoGeoMonitorObjects UserCoGeoMonitorObjects = null;
			
			@Override
			public void Process() throws Exception {
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent != null) {
					TUserDescriptor UserInfo = User.GetUserInfo(idComponent);
					if (UserInfo.UserIsOnline) {
						UserCoGeoMonitorObjects = new TUserPanel.TUserCoGeoMonitorObjects();
						//.
						UserCoGeoMonitorObjects.Update(UserAgent, idComponent);
					}
				}
				//.
	    		Thread.sleep(100); 
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				//.
				if ((UserCoGeoMonitorObjects != null) && (UserCoGeoMonitorObjects.CommunicationObject != null)) 
					if (UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserMessaging_IsSupported())
						UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserMessaging_Start(UserCoGeoMonitorObjects.CommunicationObject.Object, context);            	
					else
						throw new Exception(context.getString(R.string.SLiveChatIsNotSupportedForUser)); //. =>
				else
					throw new Exception(context.getString(R.string.SUserIsNotAvailableForMessaging)); //.=>
			}

			@Override
			public void DoOnException(Exception E) {
                Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Opening.Start();
	}
	
	public boolean IsUserAvailable() {
		boolean flOnline = false;
		try {
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent != null) {
				TUserDescriptor UserInfo = User.GetUserInfo(idComponent);
				if (UserInfo.UserIsOnline) {
					TUserPanel.TUserCoGeoMonitorObjects _UserCoGeoMonitorObjects = new TUserPanel.TUserCoGeoMonitorObjects();
					//.
					_UserCoGeoMonitorObjects.Update(UserAgent, idComponent);
					//.
					flOnline = ((_UserCoGeoMonitorObjects.CommunicationObject != null) && _UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserMessaging_IsSupported());
				}
			}
		}
		catch (Exception E) {
		}
		return flOnline;
	}
}
