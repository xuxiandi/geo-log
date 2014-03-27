package com.geoscope.GeoEye.Space.TypesSystem.Positioner;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;

public class TPositionerFunctionality extends TComponentFunctionality {

	public String _Name;
	//.
	public double _X0,_Y0;
	public double _X1,_Y1;
	public double _X2,_Y2;
	public double _X3,_Y3;
	//.
	public double _Timestamp;
	
	public TPositionerFunctionality(TTypeFunctionality pTypeFunctionality, int pidComponent) {
		super(pTypeFunctionality,pidComponent);
	}
	
	@Override
	public int ParseFromXMLDocument(byte[] XML) throws Exception {
    	//.
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
			int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
    		switch (Version) {
    		
    		case 1:
    			try {
    				Node node = RootNode.getElementsByTagName("Name").item(0).getFirstChild();
    				if (node != null)
    					_Name = node.getNodeValue();
    				//.
    				node = RootNode.getElementsByTagName("X0").item(0).getFirstChild();
    				if (node != null)
    					_X0 = Double.parseDouble(node.getNodeValue());
    				node = RootNode.getElementsByTagName("Y0").item(0).getFirstChild();
    				if (node != null)
    					_Y0 = Double.parseDouble(node.getNodeValue());
    				//.
    				node = RootNode.getElementsByTagName("X1").item(0).getFirstChild();
    				if (node != null)
    					_X1 = Double.parseDouble(node.getNodeValue());
    				node = RootNode.getElementsByTagName("Y1").item(0).getFirstChild();
    				if (node != null)
    					_Y1 = Double.parseDouble(node.getNodeValue());
    				//.
    				node = RootNode.getElementsByTagName("X2").item(0).getFirstChild();
    				if (node != null)
    					_X2 = Double.parseDouble(node.getNodeValue());
    				node = RootNode.getElementsByTagName("Y2").item(0).getFirstChild();
    				if (node != null)
    					_Y2 = Double.parseDouble(node.getNodeValue());
    				//.
    				node = RootNode.getElementsByTagName("X3").item(0).getFirstChild();
    				if (node != null)
    					_X3 = Double.parseDouble(node.getNodeValue());
    				node = RootNode.getElementsByTagName("Y3").item(0).getFirstChild();
    				if (node != null)
    					_Y3 = Double.parseDouble(node.getNodeValue());
    				//.
    				node = RootNode.getElementsByTagName("Timestamp").item(0).getFirstChild();
    				if (node != null)
    					_Timestamp = Double.parseDouble(node.getNodeValue());
    			}
    			catch (Exception E) {
        			throw new Exception("error of parsing xml document: "+E.getMessage()); //. =>
    			}
    			return Version; //. ->
    			
    		default:
    			return (-Version); //. ->
    		}
    	}
    	catch (Exception E) {
			throw new Exception("error of loading xml document: "+E.getMessage()); //. =>
    	}
	}
}
