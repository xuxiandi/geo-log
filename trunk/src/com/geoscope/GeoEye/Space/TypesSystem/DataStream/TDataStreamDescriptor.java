package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.util.Base64;
import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TDataStreamDescriptor {

	private byte[] SourceByteArray = null;
	//.
    public String Name = "";
    public String Info = "";
    //.
    public ArrayList<TChannel> Channels = new ArrayList<TChannel>();
    
	public TDataStreamDescriptor(byte[] ByteArray) throws Exception {
		FromByteArray(ByteArray);
	}
	
	public TDataStreamDescriptor(String Base64String) throws Exception {
		FromBase64String(Base64String);
	}
	
	public void FromByteArray(byte[] ByteArray) throws Exception {
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(ByteArray);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		Element ANode = XmlDoc.getDocumentElement();
		//.
		FromXMLNode(ANode);
		//.
		SourceByteArray = ByteArray;
	}
	
    public byte[] ToByteArray() throws Exception {
	    XmlSerializer Serializer = Xml.newSerializer();
	    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
	    try {
	        Serializer.setOutput(BOS,"UTF-8");
	        Serializer.startDocument("UTF-8",true);
	        Serializer.startTag("", "ROOT");
	        //. 
	        ToXMLSerializer(Serializer);
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
    
	public void FromXMLNode(Node ANode) throws Exception {
		int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			try {
    			Name = TMyXML.SearchNode(ANode,"Name").getFirstChild().getNodeValue();
    			Info = TMyXML.SearchNode(ANode,"Info").getFirstChild().getNodeValue();
    			//.
    			Channels.clear();
				NodeList ChannelsNode = TMyXML.SearchNode(ANode,"Channels").getChildNodes();
				int Cnt = ChannelsNode.getLength();
				for (int I = 0; I < Cnt; I++) {
					Node ChannelNode = ChannelsNode.item(I);
					//.
					if (ChannelNode.getLocalName() != null) {
						TChannel Channel = new TChannel();
						//.
						Channel.FromXMLNode(ChannelNode);
						//.
	    				Channels.add(Channel);
					}
				}
			}
			catch (Exception E) {
    			throw new Exception("error of parsing configuration: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown local configuration version, version: "+Integer.toString(Version)); //. =>
		}
	}
	
	public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
		int Version = 1;
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
        //. Channels
        Serializer.startTag("", "Channels");
        int Cnt = Channels.size();
        for (int I = 0; I < Cnt; I++) {
        	String ChannelNodeName = "C"+Integer.toString(I);
            Serializer.startTag("", ChannelNodeName);
            //.
        	TChannel Channel = Channels.get(I);
        	Channel.ToXMLSerializer(Serializer);
            //.
            Serializer.endTag("", ChannelNodeName);
        }
        Serializer.endTag("", "Channels");
	}
	
	public void FromBase64String(String S) throws Exception {
		FromByteArray(Base64.decode(S, Base64.NO_WRAP));
	}
	
	public String ToBase64String() throws IOException {
		if (SourceByteArray == null)
			return null; //. ->
		return Base64.encodeToString(SourceByteArray, 0,SourceByteArray.length, Base64.NO_WRAP);
	}
}
