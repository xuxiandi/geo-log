package com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.PropsPanel;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.R;
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
	
	public String 	ObjectName;
	
	public TURL(long pidComponent) throws Exception {
		super(pidComponent);
		//.
		ObjectName = "";
	}

	public TURL(long pidComponent, String pObjectName) throws Exception {
		super(pidComponent);
		//.
		ObjectName = pObjectName;
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
	public void ToXMLSerializer(XmlSerializer Serializer) throws IOException {
		super.ToXMLSerializer(Serializer);
        //. ObjectName
        Serializer.startTag("", "ObjectName");
        Serializer.text(ObjectName);
        Serializer.endTag("", "ObjectName");
	}

	@Override
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.user_activity_component_list_placeholder_cocomponent_geomonitorobject1;
	}
	
	@Override
	public Bitmap GetThumbnailImage() {
		return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_cocomponent_geomonitorobject1); 
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
