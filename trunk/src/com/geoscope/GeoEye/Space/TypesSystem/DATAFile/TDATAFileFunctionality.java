package com.geoscope.GeoEye.Space.TypesSystem.DATAFile;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.URL.TURL;

public class TDATAFileFunctionality extends TComponentFunctionality {

	public String TypeID = "";
	
	public TDATAFileFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
		super(pTypeFunctionality,pidComponent);
	}

	@Override
	public int ParseFromXMLDocument(byte[] XML) throws Exception {
    	try {
        	Document XmlDoc;
    		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
    		try {
    			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
    			factory.setNamespaceAware(true);     
    			DocumentBuilder builder = factory.newDocumentBuilder(); 			
    			XmlDoc = builder.parse(BIS); 
    		}
    		finally {
    			BIS.close();
    		}
    		XMLDocumentRootNode = XmlDoc.getDocumentElement();
			int Version = Integer.parseInt(XMLDocumentRootNode.getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
			switch (Version) {
			case 1:
				try {
					Node node;
	    			TypeID = "";
	    			node = TMyXML.SearchNode(XMLDocumentRootNode,"TypeID");
	    			if (node != null) {
	    				node = node.getFirstChild();
	        			if (node != null)
	        				TypeID = node.getNodeValue();
	    			}
				}
				catch (Exception E) {
	    			throw new Exception("error of parsing XML document: "+E.getMessage()); //. =>
				}
				break; //. >
			default:
				throw new Exception("unknown XML document version, version: "+Integer.toString(Version)); //. =>
			}
			return Version; //. ->
    	}
    	catch (Exception E) {
			throw new Exception("error of loading XML document: "+E.getMessage()); //. =>
    	}
	}
	
	public void Open(Context context) throws Exception {
		if (TURL.IsTypeOf(TypeID)) {
			TURL URL = TURL.GetURL(TypeID, Server.User, XMLDocumentRootNode);
			if (URL != null)
				URL.Open(context);
		}
	}
}
