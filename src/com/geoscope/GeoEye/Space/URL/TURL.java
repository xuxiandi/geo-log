package com.geoscope.GeoEye.Space.URL;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.R;
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
	
	public static TURL GetURLFromXmlData(byte[] XmlData, TGeoScopeServerUser pUser) throws Exception {
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XmlData);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		Element XMLDocumentRootNode = XmlDoc.getDocumentElement();
		Node node;
		String TypeID = "";
		node = TMyXML.SearchNode(XMLDocumentRootNode,"TypeID");
		if (node != null) {
			node = node.getFirstChild();
			if (node != null)
				TypeID = node.getNodeValue();
		}
		return GetURL(TypeID, pUser, XMLDocumentRootNode);
	}
	
	public static final String DefaultURLFileName = "URL.xml";
	
	
	protected TGeoScopeServerUser User = null;
	protected Element XMLDocumentRootNode = null;
	//.
	public String Name = null;
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
		Name = null;
		Node node = TMyXML.SearchNode(XMLDocumentRootNode,"Name");
		if (node != null) {
			node = node.getFirstChild();
			if (node != null)
				Name = node.getNodeValue();
		}
		//.
		URLNode = TMyXML.SearchNode(XMLDocumentRootNode,"URL");
		if (URLNode == null)
			throw new Exception("there is no URL data node"); //. =>
		URLVersion = Integer.parseInt(TMyXML.SearchNode(URLNode,"Version").getFirstChild().getNodeValue());
		switch (URLVersion) {
		case 1:
			try {
    			Value = "";
    			node = TMyXML.SearchNode(URLNode,"Value");
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
	        //. Name
	        if (Name != null) {
		        Serializer.startTag("", "Name");
		        Serializer.text(Name);
		        Serializer.endTag("", "Name");
	        }
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
	
	public boolean IsAvailable() {
		return true;
	}
	
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.user_activity_component_list_placeholder_text;
	}
	
	public Bitmap GetThumbnailImage() {
		return null;
	}
	
	public Bitmap GetThumbnailImage(int ImageMaxSize) {
		Bitmap Result = null;
		//.
		Bitmap BMP = GetThumbnailImage();
		if (BMP != null) 
			try {
				int Width = BMP.getWidth();
				int Height = BMP.getHeight();
				int MaxSize = Width;
				if (Height > MaxSize)
					MaxSize = Height;
				float Scale = (ImageMaxSize+0.0F)/MaxSize; 
				Matrix matrix = new Matrix();     
				matrix.postScale(Scale,Scale);
				//.
				Result = Bitmap.createBitmap(BMP, 0,0,Width,Height, matrix, true);
			}
			finally {
				if (Result != BMP)
					BMP.recycle();
			}
		return Result;
	}
	
	public void Open(Context context) throws Exception {
	}
}
