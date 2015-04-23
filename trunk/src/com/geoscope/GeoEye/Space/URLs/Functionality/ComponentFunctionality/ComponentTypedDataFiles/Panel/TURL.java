package com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.Panel;

import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFilesPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.TURL.TypeID+"."+"Panel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode)); //. ->
	}
	
	public static void ConstructURLFile(int idTComponent, long idComponent, String URLFileName) throws IOException {
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
	        //. idTComponent
	        Serializer.startTag("", "idTComponent");
	        Serializer.text(Integer.toString(idTComponent));
	        Serializer.endTag("", "idTComponent");
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
		TAsyncProcessing Opening = new TAsyncProcessing(context) {
			
			private TComponentTypedDataFiles TypedDataFiles;
			
			@Override
			public void Process() throws Exception {
				TypedDataFiles = new TComponentTypedDataFiles(context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_AllName);
				TypedDataFiles.PrepareForComponent(idTComponent,idComponent, true, User.Server);
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				Intent intent = new Intent(context, TComponentTypedDataFilesPanel.class);
				intent.putExtra("ComponentID", 0);
				intent.putExtra("DataFiles", TypedDataFiles.ToByteArrayV0());
				context.startActivity(intent);
			}
			
			@Override
			public void DoOnException(Exception E) {
    			Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Opening.Start();
	}
}