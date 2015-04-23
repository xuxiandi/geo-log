package com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream;

import org.w3c.dom.Element;

import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.TURL.TypeID+"."+"DataStream";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.PropsPanel.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.PropsPanel.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return null; //. ->
	}
	
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}
}
