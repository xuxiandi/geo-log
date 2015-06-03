package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Panel;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TUserActivityListPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.TURL.TypeID+"."+"Panel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
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
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.user_activities; //. ->
	}
	
	@Override
	public Bitmap GetThumbnailImage() {
		return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activities); 
	}
	
	@Override
	public void Open(Context context) throws Exception {
    	Intent intent = new Intent(context, TUserActivityListPanel.class);
    	TReflectorComponent Component = TReflectorComponent.GetAComponent(); 
    	if (Component != null)
    		intent.putExtra("ComponentID", Component.ID);
    	intent.putExtra("UserID",idComponent);
    	context.startActivity(intent);
	}
}
