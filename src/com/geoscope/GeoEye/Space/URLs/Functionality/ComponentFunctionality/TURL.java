package com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.Functionality.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.Functionality.TURL.TypeID+"."+"Component";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return null; //. ->
	}
	
	
	public int	idTComponent;
	public long idComponent;
	
	public TURL(int	pidTComponent, long pidComponent) {
		super();
		//.
		idTComponent = pidTComponent;
		idComponent = pidComponent;
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
				idTComponent = 0;
    			node = TMyXML.SearchNode(URLNode,"idTComponent");
    			if (node != null) {
    				node = node.getFirstChild();
        			if (node != null)
        				idTComponent = Integer.parseInt(node.getNodeValue());
    			}
				//.
				idComponent = 0;
    			node = TMyXML.SearchNode(URLNode,"idComponent");
    			if (node != null) {
    				node = node.getFirstChild();
        			if (node != null)
        				idComponent = Long.parseLong(node.getNodeValue());
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
        //. idTComponent
        Serializer.startTag("", "idTComponent");
        Serializer.text(Integer.toString(idTComponent));
        Serializer.endTag("", "idTComponent");
        //. idComponent
        Serializer.startTag("", "idComponent");
        Serializer.text(Long.toString(idComponent));
        Serializer.endTag("", "idComponent");
	}
}