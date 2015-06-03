package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.Messaging;

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

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.PropsPanel.TURL.TypeID+"."+"Messaging";
	
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
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.offlineusermessaging;
	}
	
	@Override
	public Bitmap GetThumbnailImage() {
		try {
			TUserDescriptor UserInfo = User.GetUserInfo(idComponent);
			if (UserInfo.UserIsOnline)
				return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.onlineusermessaging); //. ->
			else
				return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineusermessaging); //. ->
		}
		catch (Exception E) {
			return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineusermessaging); //. ->
		}
	}
	
	@Override
	public void Open(Context context) throws Exception {
    	Intent intent = new Intent(context, TUserPanel.class);
    	TReflectorComponent Component = TReflectorComponent.GetAComponent(); 
    	if (Component != null)
    		intent.putExtra("ComponentID", Component.ID);
    	intent.putExtra("UserID",idComponent);
    	intent.putExtra("Mode",TUserPanel.MODE_OPENMESSAGING);
    	context.startActivity(intent);
	}
}
