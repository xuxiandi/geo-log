package com.geoscope.GeoEye.Space.TypesSystem.Positioner;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.URL.TURL;

public class TPositionerFunctionality extends TComponentFunctionality {

	public String _Name;
	//.
	public double _X0,_Y0;
	public double _X1,_Y1;
	public double _X2,_Y2;
	public double _X3,_Y3;
	//.
	public double _Timestamp;
	
	public TPositionerFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
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
    		Element RootNode = XmlDoc.getDocumentElement();
    		//.
    		return FromXMLNode(RootNode); //. ->
    	}
    	catch (Exception E) {
			throw new Exception("error of loading xml document: "+E.getMessage()); //. =>
    	}
	}
	
	@Override
	public TURL GetDefaultURL() throws Exception {
		return (new com.geoscope.GeoEye.Space.URLs.TypesSystem.Positioner.Panel.TURL(idComponent,this));
	}
	
	public int FromXMLNode(Node ANode) throws Exception {
		int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		
		case 1:
			try {
				Node node = TMyXML.SearchNode(ANode,"Name").getFirstChild();
				if (node != null)
					_Name = node.getNodeValue();
				//.
				Node RWNode = TMyXML.SearchNode(ANode,"RW");
				//.
				node = TMyXML.SearchNode(RWNode,"X0").getFirstChild();
				if (node != null)
					_X0 = Double.parseDouble(node.getNodeValue());
				node = TMyXML.SearchNode(RWNode,"Y0").getFirstChild();
				if (node != null)
					_Y0 = Double.parseDouble(node.getNodeValue());
				//.
				node = TMyXML.SearchNode(RWNode,"X1").getFirstChild();
				if (node != null)
					_X1 = Double.parseDouble(node.getNodeValue());
				node = TMyXML.SearchNode(RWNode,"Y1").getFirstChild();
				if (node != null)
					_Y1 = Double.parseDouble(node.getNodeValue());
				//.
				node = TMyXML.SearchNode(RWNode,"X2").getFirstChild();
				if (node != null)
					_X2 = Double.parseDouble(node.getNodeValue());
				node = TMyXML.SearchNode(RWNode,"Y2").getFirstChild();
				if (node != null)
					_Y2 = Double.parseDouble(node.getNodeValue());
				//.
				node = TMyXML.SearchNode(RWNode,"X3").getFirstChild();
				if (node != null)
					_X3 = Double.parseDouble(node.getNodeValue());
				node = TMyXML.SearchNode(RWNode,"Y3").getFirstChild();
				if (node != null)
					_Y3 = Double.parseDouble(node.getNodeValue());
				//.
				node = TMyXML.SearchNode(RWNode,"Timestamp").getFirstChild();
				if (node != null)
					_Timestamp = Double.parseDouble(node.getNodeValue());
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
        Serializer.text(_Name);
        Serializer.endTag("", "Name");
        //. RW
        Serializer.startTag("", "RW");
        //. X0
        Serializer.startTag("", "X0");
        Serializer.text(Double.toString(_X0));
        Serializer.endTag("", "X0");
        //. Y0
        Serializer.startTag("", "Y0");
        Serializer.text(Double.toString(_Y0));
        Serializer.endTag("", "Y0");
        //. X1
        Serializer.startTag("", "X1");
        Serializer.text(Double.toString(_X1));
        Serializer.endTag("", "X1");
        //. Y1
        Serializer.startTag("", "Y1");
        Serializer.text(Double.toString(_Y1));
        Serializer.endTag("", "Y1");
        //. X2
        Serializer.startTag("", "X2");
        Serializer.text(Double.toString(_X2));
        Serializer.endTag("", "X2");
        //. Y2
        Serializer.startTag("", "Y2");
        Serializer.text(Double.toString(_Y2));
        Serializer.endTag("", "Y2");
        //. X3
        Serializer.startTag("", "X3");
        Serializer.text(Double.toString(_X3));
        Serializer.endTag("", "X3");
        //. Y3
        Serializer.startTag("", "Y3");
        Serializer.text(Double.toString(_Y3));
        Serializer.endTag("", "Y3");
        //. Timestamp
        Serializer.startTag("", "Timestamp");
        Serializer.text(Double.toString(_Timestamp));
        Serializer.endTag("", "Timestamp");
        //.
        Serializer.endTag("", "RW");
	}
}
