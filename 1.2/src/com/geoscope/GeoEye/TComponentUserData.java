package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.geoscope.GeoLog.Utils.TMyXML;

public class TComponentUserData {

	public static final String ComponentUserDataFileName = "ComponentUserData.xml";
	
	private String ComponentUserDataFile;
	private Document XmlDoc = null;
	public  Element RootNode = null;
	
	public TComponentUserData() throws Exception {
		ComponentUserDataFile = TReflector.ProfileFolder+"/"+ComponentUserDataFileName;
		//.
		Load();
	}
	
	public void Load() throws Exception {
		File F = new File(ComponentUserDataFile);
		if (!F.exists()) {
			RootNode = null;
			return; //. ->
		}
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(ComponentUserDataFile);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
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
		//.
		RootNode = XmlDoc.getDocumentElement();
		//.
		int Version = Integer.parseInt(RootNode.getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
		if (Version != 1) 
			throw new Exception("unknown "+ComponentUserDataFile+" file version, version: "+Integer.toString(Version)); //. =>
	}

	public void Save() throws IOException {
		if ((XmlDoc == null) || (RootNode == null))
			return; //. ->
        String xmlString = TMyXML.GetStringFromNode(RootNode);
        //.
        byte[] BA = xmlString.getBytes();
        FileOutputStream FOS = new FileOutputStream(ComponentUserDataFile);
        try {
        	FOS.write(BA);
        }
        finally {
        	FOS.close();
        }
	}
}
