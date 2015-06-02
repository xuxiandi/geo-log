package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.VideoPhone;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TUserPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.TURL.TypeID+"."+"VideoPhone";
	
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
			return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.onlineuservideophone); //. ->
		else
			return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineuservideophone); //. ->
	}
	
	@Override
	public void Open(Context context) throws Exception {
    	Intent intent = new Intent(context, TUserPanel.class);
    	intent.putExtra("UserID",idComponent);
    	intent.putExtra("Mode",TUserPanel.MODE_OPENVIDEOPHONE);
    	context.startActivity(intent);
	}

	public boolean IsUserAvailable() {
		boolean flOnline = false;
		try {
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent != null) {
				TUserDescriptor UserInfo = UserAgent.Server.User.GetUserInfo(idComponent);
				if (UserInfo.UserIsOnline) {
					TUserPanel.TUserCoGeoMonitorObjects _UserCoGeoMonitorObjects = new TUserPanel.TUserCoGeoMonitorObjects();
					//.
					_UserCoGeoMonitorObjects.Update(UserAgent, idComponent);
					//.
					flOnline = ((_UserCoGeoMonitorObjects.CommunicationObject != null) && _UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserVideoPhone_IsSupported());
				}
			}
		}
		catch (Exception E) {
		}
		return flOnline;
	}
}
