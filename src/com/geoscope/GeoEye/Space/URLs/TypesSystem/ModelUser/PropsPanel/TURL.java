package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TUserPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL.TypeID+"."+"PropsPanel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.Messaging.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.Messaging.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.LiveMessaging.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.LiveMessaging.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.VideoPhone.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.VideoPhone.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return (new TURL(pUser,pXMLDocumentRootNode)); //. ->
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
	public Bitmap GetThumbnailImage() {
		try {
			TUserDescriptor UserInfo = User.GetUserInfo(idComponent);
			if (UserInfo.UserIsOnline)
				return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.onlineuser); //. ->
			else
				return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineuser); //. ->
		}
		catch (Exception E) {
			return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineuser); //. ->
		}
	}
	
	@Override
	public void Open(Context context) throws Exception {
    	Intent intent = new Intent(context, TUserPanel.class);
    	intent.putExtra("UserID",idComponent);
    	context.startActivity(intent);
	}
}
