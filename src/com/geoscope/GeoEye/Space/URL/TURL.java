package com.geoscope.GeoEye.Space.URL;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL {

	public static final String TypeID = "URL";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return null; //. ->
	}
	
	
	protected TGeoScopeServerUser User;
	protected Element XMLDocumentRootNode;
	//.
	protected Node 	URLNode;
	protected int 	URLVersion;
	//.
	protected String Value;
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		User = pUser;
		XMLDocumentRootNode = pXMLDocumentRootNode;
		//.
		Parse();
	}
	
	protected void Parse() throws Exception {
		URLNode = TMyXML.SearchNode(XMLDocumentRootNode,"URL");
		if (URLNode == null)
			throw new Exception("there is no URL data node"); //. =>
		URLVersion = Integer.parseInt(TMyXML.SearchNode(URLNode,"Version").getFirstChild().getNodeValue());
		switch (URLVersion) {
		case 1:
			try {
				Node node;
    			Value = "";
    			node = TMyXML.SearchNode(URLNode,"Value");
    			if (node != null) {
    				node = node.getFirstChild();
        			if (node != null)
        				Value = node.getNodeValue();
    			}
			}
			catch (Exception E) {
    			throw new Exception("error of parsing URL data: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown URL data version, version: "+Integer.toString(URLVersion)); //. =>
		}
	}
	
	public void Open() throws Exception {
	}
}
