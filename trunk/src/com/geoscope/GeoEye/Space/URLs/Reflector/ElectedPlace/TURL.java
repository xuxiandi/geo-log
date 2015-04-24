package com.geoscope.GeoEye.Space.URLs.Reflector.ElectedPlace;

import java.io.IOException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.Reflector.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.Reflector.TURL.TypeID+"."+"ElectedPlace";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		if (com.geoscope.GeoEye.Space.URLs.Reflector.ElectedPlace.Panel.TURL.IsTypeOf(TypeID))
			return com.geoscope.GeoEye.Space.URLs.Reflector.ElectedPlace.Panel.TURL.GetURL(TypeID, pUser,pXMLDocumentRootNode); //. ->
		else
			return null; //. ->
	}
	

	public static class TData {

		public String Name;
		//.
		public double RW_X0,RW_Y0;
		public double RW_X1,RW_Y1;
		public double RW_X2,RW_Y2;
		public double RW_X3,RW_Y3;
		//.
		public double RW_Timestamp;
		
		public TData() {
		}
		
		public TData(String pName, double pRW_X0, double pRW_Y0, double pRW_X1, double pRW_Y1, double pRW_X2, double pRW_Y2, double pRW_X3, double pRW_Y3, double pRW_Timestamp) {
			Name = pName;
			//.
			RW_X0 = pRW_X0; RW_Y0 = pRW_Y0;
			RW_X1 = pRW_X1; RW_Y1 = pRW_Y1;
			RW_X2 = pRW_X2; RW_Y2 = pRW_Y2;
			RW_X3 = pRW_X3; RW_Y3 = pRW_Y3;
			//.
			RW_Timestamp = pRW_Timestamp;
		}
		
		public int FromXMLNode(Node ANode) throws Exception {
			int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
			switch (Version) {
			
			case 1:
				try {
					Node node = TMyXML.SearchNode(ANode,"Name").getFirstChild();
					if (node != null)
						Name = node.getNodeValue();
					//.
					Node RWNode = TMyXML.SearchNode(ANode,"RW");
					//.
					node = TMyXML.SearchNode(RWNode,"X0").getFirstChild();
					if (node != null)
						RW_X0 = Double.parseDouble(node.getNodeValue());
					node = TMyXML.SearchNode(RWNode,"Y0").getFirstChild();
					if (node != null)
						RW_Y0 = Double.parseDouble(node.getNodeValue());
					//.
					node = TMyXML.SearchNode(RWNode,"X1").getFirstChild();
					if (node != null)
						RW_X1 = Double.parseDouble(node.getNodeValue());
					node = TMyXML.SearchNode(RWNode,"Y1").getFirstChild();
					if (node != null)
						RW_Y1 = Double.parseDouble(node.getNodeValue());
					//.
					node = TMyXML.SearchNode(RWNode,"X2").getFirstChild();
					if (node != null)
						RW_X2 = Double.parseDouble(node.getNodeValue());
					node = TMyXML.SearchNode(RWNode,"Y2").getFirstChild();
					if (node != null)
						RW_Y2 = Double.parseDouble(node.getNodeValue());
					//.
					node = TMyXML.SearchNode(RWNode,"X3").getFirstChild();
					if (node != null)
						RW_X3 = Double.parseDouble(node.getNodeValue());
					node = TMyXML.SearchNode(RWNode,"Y3").getFirstChild();
					if (node != null)
						RW_Y3 = Double.parseDouble(node.getNodeValue());
					//.
					node = TMyXML.SearchNode(RWNode,"Timestamp").getFirstChild();
					if (node != null)
						RW_Timestamp = Double.parseDouble(node.getNodeValue());
				}
				catch (Exception E) {
	    			throw new Exception("error of parsing XML: "+E.getMessage()); //. =>
				}
				return Version; //. ->
				
			default:
				return (-Version); //. ->
			}
		}
		
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws IOException {
			int Version = 1;
	        //. Version
	        Serializer.startTag("", "Version");
	        Serializer.text(Integer.toString(Version));
	        Serializer.endTag("", "Version");
	        //. Name
	        Serializer.startTag("", "Name");
	        Serializer.text(Name);
	        Serializer.endTag("", "Name");
	        //. RW
	        Serializer.startTag("", "RW");
	        //. X0
	        Serializer.startTag("", "X0");
	        Serializer.text(Double.toString(RW_X0));
	        Serializer.endTag("", "X0");
	        //. Y0
	        Serializer.startTag("", "Y0");
	        Serializer.text(Double.toString(RW_Y0));
	        Serializer.endTag("", "Y0");
	        //. X1
	        Serializer.startTag("", "X1");
	        Serializer.text(Double.toString(RW_X1));
	        Serializer.endTag("", "X1");
	        //. Y1
	        Serializer.startTag("", "Y1");
	        Serializer.text(Double.toString(RW_Y1));
	        Serializer.endTag("", "Y1");
	        //. X2
	        Serializer.startTag("", "X2");
	        Serializer.text(Double.toString(RW_X2));
	        Serializer.endTag("", "X2");
	        //. Y2
	        Serializer.startTag("", "Y2");
	        Serializer.text(Double.toString(RW_Y2));
	        Serializer.endTag("", "Y2");
	        //. X3
	        Serializer.startTag("", "X3");
	        Serializer.text(Double.toString(RW_X3));
	        Serializer.endTag("", "X3");
	        //. Y3
	        Serializer.startTag("", "Y3");
	        Serializer.text(Double.toString(RW_Y3));
	        Serializer.endTag("", "Y3");
	        //. Timestamp
	        Serializer.startTag("", "Timestamp");
	        Serializer.text(Double.toString(RW_Timestamp));
	        Serializer.endTag("", "Timestamp");
	        //.
	        Serializer.endTag("", "RW");
		}
	}
	
	
	public TData Data;
	
	public TURL(TData pData) {
		super();
		//.
		Data = pData;
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
		Data = new TData();
		//.
		switch (URLVersion) {
		case 1:
			try {
				Node node = TMyXML.SearchNode(URLNode,"Data");
    			if (node != null) 
    				Data.FromXMLNode(node);
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
        //. ComponentData
        Serializer.startTag("", "Data");
        Data.ToXMLSerializer(Serializer);
        Serializer.endTag("", "Data");
	}	

	@Override
	public boolean HasData() {
		return true;
	}
}