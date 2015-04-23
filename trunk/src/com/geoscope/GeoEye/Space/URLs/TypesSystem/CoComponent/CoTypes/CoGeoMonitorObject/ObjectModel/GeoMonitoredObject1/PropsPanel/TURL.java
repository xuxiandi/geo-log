package com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.PropsPanel;

import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjectPanel;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.TURL.TypeID+"."+"PropsPanel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode));
	}
	
	public static void ConstructURLFile(long idComponent, String ObjectName, String URLFileName) throws IOException {
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
	        Serializer.text(TypeID);
	        Serializer.endTag("", "TypeID");
	        //. URL
	        Serializer.startTag("", "URL");
	        //. Version
			Version = 1;
	        Serializer.startTag("", "Version");
	        Serializer.text(Integer.toString(Version));
	        Serializer.endTag("", "Version");
	        //. ObjectID
	        Serializer.startTag("", "ObjectID");
	        Serializer.text(Long.toString(idComponent));
	        Serializer.endTag("", "ObjectID");
	        //. ObjectName
	        Serializer.startTag("", "ObjectName");
	        Serializer.text(ObjectName);
	        Serializer.endTag("", "ObjectName");
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

	public String 	ObjectName;
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
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
				ObjectName = "";
    			node = TMyXML.SearchNode(URLNode,"ObjectName");
    			if (node != null) {
    				node = node.getFirstChild();
        			if (node != null)
        				ObjectName = node.getNodeValue();
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
	public void Open(Context context) throws Exception {
    	Intent intent = new Intent(context, TCoGeoMonitorObjectPanel.class);
		intent.putExtra("ComponentID", 0);
    	intent.putExtra("ParametersType", TCoGeoMonitorObjectPanel.PARAMETERS_TYPE_OID);
    	intent.putExtra("ObjectID", idComponent);
    	intent.putExtra("ObjectName", ObjectName);
    	context.startActivity(intent);
	}
}
