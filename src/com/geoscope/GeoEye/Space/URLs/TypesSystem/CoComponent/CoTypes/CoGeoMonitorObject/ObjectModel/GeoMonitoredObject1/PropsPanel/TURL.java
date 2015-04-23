package com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.PropsPanel;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.content.Intent;

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
	

	public long 	ObjectID;
	public String 	ObjectName;
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}
		
	@Override
	protected void Parse() throws Exception {
		super.Parse();
		//.
		ObjectID = Long.parseLong(Value);
		//.
		switch (URLVersion) {
		case 1:
			try {
				Node node;
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
	
	public void Open() throws Exception {
    	Intent intent = new Intent(User.Server.context, TCoGeoMonitorObjectPanel.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("ComponentID", 0);
    	intent.putExtra("ParametersType", TCoGeoMonitorObjectPanel.PARAMETERS_TYPE_OID);
    	intent.putExtra("ObjectID", ObjectID);
    	intent.putExtra("ObjectName", ObjectName);
    	User.Server.context.startActivity(intent);
	}
}
