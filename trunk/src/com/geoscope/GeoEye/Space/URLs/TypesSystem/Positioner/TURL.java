package com.geoscope.GeoEye.Space.URLs.TypesSystem.Positioner;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.Positioner.TPositionerFunctionality;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.TURL.TypeID+"."+"Positioner";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.Positioner.Panel.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.Positioner.Panel.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return null; //. ->
	}
	
	
	public TPositionerFunctionality PF;
	
	public TURL(long pidComponent) {
		super(pidComponent);
	}

	public TURL(long pidComponent, TPositionerFunctionality pPF) {
		super(pidComponent);
		//.
		PF = pPF;
		PF.AddRef();
	}

	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}

	@Override
	public void Release() {
		if (PF != null) {
			PF.Release();
			PF = null;
		}
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}

	@Override
	protected void Parse() throws Exception {
		super.Parse();
		//.
		if (PF != null) 
			PF.Release();
		PF = (TPositionerFunctionality)User.Space.TypesSystem.SystemTPositioner.TComponentFunctionality_Create(idComponent);
		PF.AddRef();
		//.
		switch (URLVersion) {
		case 1:
			try {
				Node node = TMyXML.SearchNode(URLNode,"ComponentData");
    			if (node != null) 
    				PF.FromXMLNode(node);
			}
			catch (Exception E) {
    			throw new Exception("error of parsing URL data: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown URL data version, version: "+Integer.toString(URLVersion)); //. =>
		}
	}
	
	@Override
	public void ToXMLSerializer(XmlSerializer Serializer) throws IOException {
		super.ToXMLSerializer(Serializer);
        //. ComponentData
        Serializer.startTag("", "ComponentData");
        PF.ToXMLSerializer(Serializer);
        Serializer.endTag("", "ComponentData");
	}
	
	@Override
	public boolean HasData() {
		return true;
	}
}
