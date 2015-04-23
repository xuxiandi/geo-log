package com.geoscope.GeoEye.Space.URLs.Internet;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TURL.TypeID+"."+"Internet";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode));
	}
	
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}
	
	@Override
	public void Open(Context context) throws Exception {
    	Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(Uri.parse(Value));
    	context.startActivity(intent);
	}
}