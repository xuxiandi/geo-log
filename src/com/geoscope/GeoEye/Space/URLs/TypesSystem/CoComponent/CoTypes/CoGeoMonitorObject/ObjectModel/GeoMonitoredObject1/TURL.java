package com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1;

import org.w3c.dom.Element;

import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TURL.TypeID+"."+"ObjectModel.GeoMonitoredObject1";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.PropsPanel.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.PropsPanel.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.Location.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.Location.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
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
