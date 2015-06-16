package com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.PropsPanel;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamPropsPanel;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.TURL.TypeID+"."+"PropsPanel";
	
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
		return R.drawable.user_activity_component_list_placeholder_component_datastream;
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() {
		return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_component_datastream))); 
	}
	
	@Override
	public void Open(Context context, Object Params) throws Exception {
    	Intent intent = new Intent(context, TDataStreamPropsPanel.class);
		intent.putExtra("idTComponent",SpaceDefines.idTDataStream);
		intent.putExtra("idComponent",idComponent);
    	context.startActivity(intent);
	}
}
