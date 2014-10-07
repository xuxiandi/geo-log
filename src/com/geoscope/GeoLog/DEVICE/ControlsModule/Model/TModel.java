package com.geoscope.GeoLog.DEVICE.ControlsModule.Model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;

public class TModel {

    public String Name = "";
    public String Info = "";
    //.
    public TStreamDescriptor ControlStream;
    
	public TModel(byte[] BA) throws Exception {
		FromByteArray(BA);	
	}
	
	public void FromByteArray(byte[] BA) throws Exception {
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
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
		int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			try {
    			Name = TMyXML.SearchNode(RootNode,"Name").getFirstChild().getNodeValue();
    			Info = TMyXML.SearchNode(RootNode,"Info").getFirstChild().getNodeValue();
    			//.
    			Node StreamNode = TMyXML.SearchNode(RootNode,"ControlStream");
    			if (StreamNode != null)
    				ControlStream = new TStreamDescriptor(StreamNode, com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider.Instance);
    			else
    				ControlStream = null;
			}
			catch (Exception E) {
    			throw new Exception("error of parsing model data: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown model data version, version: "+Integer.toString(Version)); //. =>
		}
	}

    public byte[] ToByteArray() throws Exception {
		int Version = 1;
	    XmlSerializer Serializer = Xml.newSerializer();
	    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
	    try {
	        Serializer.setOutput(BOS,"UTF-8");
	        Serializer.startDocument("UTF-8",true);
	        Serializer.startTag("", "ROOT");
	        //. Version
            Serializer.startTag("", "Version");
            Serializer.text(Integer.toString(Version));
            Serializer.endTag("", "Version");
	        //. Name
            Serializer.startTag("", "Name");
            Serializer.text(Name);
            Serializer.endTag("", "Name");
	        //. Info
            Serializer.startTag("", "Info");
            Serializer.text(Info);
            Serializer.endTag("", "Info");
	        //. ControlStream
            Serializer.startTag("", "ControlStream");
            if (ControlStream != null)
            	ControlStream.ToXMLSerializer(Serializer);
            Serializer.endTag("", "ControlStream");
	        //.
	        Serializer.endTag("", "ROOT");
	        Serializer.endDocument();
	        //.
			return BOS.toByteArray(); //. ->
	    }
	    finally {
	    	BOS.close();
	    }
    }
}
