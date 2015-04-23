package com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.PropsPanel;

import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.util.Xml;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamPropsPanel;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.DataStream.TURL.TypeID+"."+"PropsPanel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode));
	}
	
	public static void ConstructURLFile(long idComponent, String URLFileName) throws IOException {
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
	        //. idComponent
	        Serializer.startTag("", "idComponent");
	        Serializer.text(Long.toString(idComponent));
	        Serializer.endTag("", "idComponent");
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


	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}
		
	@Override
	public void Open(Context context) throws Exception {
    	Intent intent = new Intent(context, TDataStreamPropsPanel.class);
		intent.putExtra("idTComponent",SpaceDefines.idTDataStream);
		intent.putExtra("idComponent",idComponent);
    	context.startActivity(intent);
	}
}
