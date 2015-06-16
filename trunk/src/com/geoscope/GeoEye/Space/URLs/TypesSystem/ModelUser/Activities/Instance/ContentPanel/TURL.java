package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Instance.ContentPanel;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TUserActivityComponentListPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Instance.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Instance.TURL.TypeID+"."+"ContentPanel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode)); //. ->
	}


	public TURL(long pidComponent, long pActivityID, String pActivityInfo) {
		super(pidComponent, pActivityID,pActivityInfo);
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
		return R.drawable.user_activity_content; //. ->
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() {
		return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_content))); 
	}
	
	@Override
	public void Open(Context context) throws Exception {
    	Intent intent = new Intent(context, TUserActivityComponentListPanel.class);
    	TReflectorComponent Component = TReflectorComponent.GetAComponent(); 
    	if (Component != null)
    		intent.putExtra("ComponentID", Component.ID);
    	intent.putExtra("UserID",idComponent);
    	intent.putExtra("ActivityID",ActivityID);
    	intent.putExtra("ActivityInfo",ActivityInfo);
    	context.startActivity(intent);
	}
}
