package com.geoscope.Classes.Data.Stream;

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

import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;

public class TStreamDescriptor {

    public String Name = "";
    public String Info = "";
    //.
    public ArrayList<TChannel> Channels = new ArrayList<TChannel>();
    
	public TStreamDescriptor() {
	}
	
	public TStreamDescriptor(Node ANode, TChannelProvider pChannelProvider) throws Exception {
		FromXMLNode(ANode,pChannelProvider);
	}
	
	public TStreamDescriptor(byte[] BA, TChannelProvider pChannelProvider) throws Exception {
		FromByteArray(BA, pChannelProvider);
	}
	
	public TStreamDescriptor(byte[] BA) throws Exception {
		FromByteArray(BA);
	}
	
	public synchronized void FromXMLNode(Node ANode, TChannelProvider pChannelProvider) throws Exception {
		int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			try {
				Node node;
    			Name = "";
    			node = TMyXML.SearchNode(ANode,"Name").getFirstChild();
    			if (node != null)
    				Name = node.getNodeValue();
    			Info = "";
    			node = TMyXML.SearchNode(ANode,"Info").getFirstChild();
    			if (node != null)
    				Info = node.getNodeValue();
    			//.
    			Channels.clear();
				NodeList ChannelsNode = TMyXML.SearchNode(ANode,"Channels").getChildNodes();
				int Cnt = ChannelsNode.getLength();
				for (int I = 0; I < Cnt; I++) {
					Node ChannelNode = ChannelsNode.item(I);
					//.
					if (ChannelNode.getLocalName() != null) {
						String TypeID = TMyXML.SearchNode(ChannelNode,"TypeID").getFirstChild().getNodeValue();
						//.
						TChannel Channel = null;
						if (pChannelProvider != null)
							Channel = pChannelProvider.GetChannel(TypeID);
						if (Channel == null) 
							Channel = new TChannel();
						//.
						if (Channel != null) {
							Channel.FromXMLNode(ChannelNode);
							//.
		    				Channels.add(Channel);
						}
					}
				}
			}
			catch (Exception E) {
    			throw new Exception("error of parsing stream descriptor: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown stream descriptor version, version: "+Integer.toString(Version)); //. =>
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
	
	public void FromByteArray(byte[] BA, TChannelProvider pChannelProvider) throws Exception {
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
		FromXMLNode(RootNode, pChannelProvider);
	}
	
	public void FromByteArray(byte[] ByteArray) throws Exception {
		FromByteArray(ByteArray, null);
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
    
	public void Start() throws IOException {
		int Cnt = Channels.size();
		for (int I = 0; I < Cnt; I++) 
			Channels.get(I).Start();
	}
	
	public void Stop() throws IOException {
		int Cnt = Channels.size();
		for (int I = 0; I < Cnt; I++) 
			Channels.get(I).Stop();
	}
	
	public TChannel Channels_GetOneByID(int ChannelID) {
		int Cnt = Channels.size();
		for (int I = 0; I < Cnt; I++) {
			TChannel Channel = Channels.get(I); 
			if (Channel.ID == ChannelID)
				return Channel; //. ->
		}
		return null;
	}
	
	public TChannel Channels_GetOneByTypeID(String TypeID) {
		int Cnt = Channels.size();
		for (int I = 0; I < Cnt; I++) {
			TChannel Channel = Channels.get(I); 
			if (Channel.GetTypeID().equals(TypeID))
				return Channel; //. ->
		}
		return null;
	}
	
	public TChannel Channels_GetOneByDescriptor(byte[] ChannelDescriptor) {
		return null;
	}
	
	public TChannel Channels_GetOneByClass(Class<?> ChannelClass) {
		int Cnt = Channels.size();
		for (int I = 0; I < Cnt; I++) {
			TChannel Channel = Channels.get(I); 
			if (Channel.getClass() == ChannelClass)
				return Channel; //. ->
		}
		return null;
	}
}
