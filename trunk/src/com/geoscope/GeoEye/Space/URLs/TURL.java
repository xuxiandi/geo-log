package com.geoscope.GeoEye.Space.URLs;

import org.w3c.dom.Element;

import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URL.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URL.TURL.TypeID+"."+"Space";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.Internet.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.Internet.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		if (com.geoscope.GeoEye.Space.URLs.Reflector.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.Reflector.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		if (com.geoscope.GeoEye.Space.URLs.Functionality.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.Functionality.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return null; //. ->
	}
	
	
	public TURL() {
		super();
	}
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}

	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
