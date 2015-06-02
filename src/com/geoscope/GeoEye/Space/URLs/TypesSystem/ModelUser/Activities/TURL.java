package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities;

import org.w3c.dom.Element;

import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL.TypeID+"."+"Activities";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Panel.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Panel.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Current.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Current.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return null; //. ->
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
}