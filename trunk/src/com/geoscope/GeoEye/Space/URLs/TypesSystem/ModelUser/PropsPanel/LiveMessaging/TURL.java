package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.LiveMessaging;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TUserPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.TURL.TypeID+"."+"LiveMessaging";
	
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
	public int GetThumbnailImageResID() {
		return R.drawable.offlineuserlivemessaging;
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
    	Intent intent = new Intent(context, TUserPanel.class);
    	TReflectorComponent Component = TReflectorComponent.GetAComponent(); 
    	if (Component != null)
    		intent.putExtra("ComponentID", Component.ID);
    	intent.putExtra("UserID",idComponent);
    	intent.putExtra("Mode",TUserPanel.MODE_OPENLIVEMESSAGING);
    	context.startActivity(intent);
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
