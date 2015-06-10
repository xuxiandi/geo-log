package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Instance;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.TURL.TypeID+"."+"Instance";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Instance.ContentPanel.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Instance.ContentPanel.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return null; //. ->
	}
	
	
	public long 	ActivityID;
	public String 	ActivityInfo;
	
	public TURL(long pidComponent, long pActivityID, String pActivityInfo) {
		super(pidComponent);
		//.
		ActivityID = pActivityID;
		ActivityInfo = pActivityInfo;
	}

	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}

	@Override
	public String GetTypeID() {
		return TypeID;
	}

	@Override
	protected void Parse() throws Exception {
		super.Parse();
		//.
		switch (URLVersion) {
		case 1:
			try {
				Node node;
				//.
				ActivityID = 0;
    			node = TMyXML.SearchNode(URLNode,"ActivityID");
    			if (node != null) {
    				node = node.getFirstChild();
        			if (node != null)
        				ActivityID = Long.parseLong(node.getNodeValue());
    			}
    			//.
				ActivityInfo = "";
    			node = TMyXML.SearchNode(URLNode,"ActivityInfo");
    			if (node != null) {
    				node = node.getFirstChild();
        			if (node != null)
        				ActivityInfo = node.getNodeValue();
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
	
	@Override
	protected void ToXMLSerializer(XmlSerializer Serializer) throws IOException {
		super.ToXMLSerializer(Serializer);
        //. ActivityID
        Serializer.startTag("", "ActivityID");
        Serializer.text(Long.toString(ActivityID));
        Serializer.endTag("", "ActivityID");
        //. ActivityInfo
        if ((ActivityInfo != null) && (ActivityInfo.length() > 0)) {
            Serializer.startTag("", "ActivityInfo");
            Serializer.text(ActivityInfo);
            Serializer.endTag("", "ActivityInfo");
        }
	}
}