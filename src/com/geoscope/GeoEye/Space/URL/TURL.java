package com.geoscope.GeoEye.Space.URL;

import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

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
	
	public static final String DefaultURLFileName = "URL.xml";
	
	
	protected TGeoScopeServerUser User = null;
	protected Element XMLDocumentRootNode = null;
	//.
	protected Node 	URLNode;
	protected int 	URLVersion;
	//.
	protected String Value = "";
	
	public TURL() {
	}
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		User = pUser;
		XMLDocumentRootNode = pXMLDocumentRootNode;
		//.
		if (XMLDocumentRootNode != null)
			Parse();
	}
	
	public void Release() {
	}
	
	public String GetTypeID() {
		return TypeID;
	}
	
	protected void Parse() throws Exception {
		URLNode = TMyXML.SearchNode(XMLDocumentRootNode,"URL");
		if (URLNode == null)
			throw new Exception("there is no URL data node"); //. =>
		URLVersion = Integer.parseInt(TMyXML.SearchNode(URLNode,"Version").getFirstChild().getNodeValue());
		switch (URLVersion) {
		case 1:
			try {
    			Value = "";
    			Node node = TMyXML.SearchNode(URLNode,"Value");
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
	
	protected void ToXMLSerializer(XmlSerializer Serializer) throws IOException {
	}
	
	public void ConstructURLFile(String URLFileName) throws IOException {
	    XmlSerializer Serializer = Xml.newSerializer();
	    FileOutputStream FOS = new FileOutputStream(URLFileName);
	    try {
	        Serializer.setOutput(FOS,"UTF-8");
	        Serializer.startDocument("UTF-8",true);
	        Serializer.startTag("", "ROOT");
	        //. Version
			int Version = 1;
	        Serializer.startTag("", "Version");
	        Serializer.text(Integer.toString(Version));
	        Serializer.endTag("", "Version");
	        //. TypeID
	        Serializer.startTag("", "TypeID");
	        Serializer.text(GetTypeID());
	        Serializer.endTag("", "TypeID");
	        //. URL
	        Serializer.startTag("", "URL");
	        //. Version
			Version = 1;
	        Serializer.startTag("", "Version");
	        Serializer.text(Integer.toString(Version));
	        Serializer.endTag("", "Version");
	        //.
	        ToXMLSerializer(Serializer);
	        //.
	        Serializer.endTag("", "URL");
	        //.
	        Serializer.endTag("", "ROOT");
	        Serializer.endDocument();
	    }
	    finally {
	    	FOS.close();
	    }
	}
	
	public boolean HasData() {
		return false;
	}
	
	public void Open(Context context) throws Exception {
	}
}
