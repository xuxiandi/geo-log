package com.geoscope.Classes.Data.Stream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;

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
	
	public void FromXMLNode(Node ANode, TChannelProvider pChannelProvider) throws Exception {
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
						TChannel Channel;
						if (pChannelProvider != null)
							Channel = pChannelProvider.GetChannel(TypeID);
						else 
							Channel = new TChannel();
						//.
						if (Channel != null) {
							Channel.ID = Integer.parseInt(TMyXML.SearchNode(ChannelNode,"ID").getFirstChild().getNodeValue());
							//.
							Channel.Enabled = true;
							Node _Node = TMyXML.SearchNode(ChannelNode,"Enabled");
							if (_Node != null) {
								Node ValueNode = _Node.getFirstChild();
								if (ValueNode != null)
									Channel.Enabled = (Integer.parseInt(ValueNode.getNodeValue()) != 0);
							}
							//.
							Channel.Kind = TChannel.CHANNEL_KIND_IN;
							_Node = TMyXML.SearchNode(ChannelNode,"Kind");
							if (_Node != null) {
								Node ValueNode = _Node.getFirstChild();
								if (ValueNode != null)
									Channel.Kind = Integer.parseInt(ValueNode.getNodeValue());
							}
							//.
							Channel.DataFormat = Integer.parseInt(TMyXML.SearchNode(ChannelNode,"DataFormat").getFirstChild().getNodeValue());
							//.
							Channel.Name = TMyXML.SearchNode(ChannelNode,"Name").getFirstChild().getNodeValue();
							//.
							Channel.Info = TMyXML.SearchNode(ChannelNode,"Info").getFirstChild().getNodeValue();
							//.
							Channel.Size = Integer.parseInt(TMyXML.SearchNode(ChannelNode,"Size").getFirstChild().getNodeValue());
							//.
							Node ValueNode = TMyXML.SearchNode(ChannelNode,"Configuration").getFirstChild();
							if (ValueNode != null)
								Channel.Configuration = ValueNode.getNodeValue();
							else
								Channel.Configuration = "";
							//.
							ValueNode = TMyXML.SearchNode(ChannelNode,"Parameters").getFirstChild();
							if (ValueNode != null)
								Channel.Parameters = ValueNode.getNodeValue();
							else
								Channel.Parameters = "";
							//. get channel DataTypes
							_Node = TMyXML.SearchNode(ChannelNode,"DataTypes");
							if (_Node != null) {
								Channel.DataTypes = new TDataTypes();
								Channel.DataTypes.FromXMLNode(_Node, Channel);
							}
							//.
							Channel.Parse();
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
        	//. TypeID
            Serializer.startTag("", "TypeID");
            Serializer.text(Channel.GetTypeID());
            Serializer.endTag("", "TypeID");
        	//. ID
            Serializer.startTag("", "ID");
            Serializer.text(Integer.toString(Channel.ID));
            Serializer.endTag("", "ID");
        	//. Enabled
            Serializer.startTag("", "Enabled");
            int V = 0;
            if (Channel.Enabled)
            	V = 1;
            Serializer.text(Integer.toString(V));
            Serializer.endTag("", "Enabled");
        	//. Kind
            Serializer.startTag("", "Kind");
            Serializer.text(Integer.toString(Channel.Kind));
            Serializer.endTag("", "Kind");
        	//. DataFormat
            Serializer.startTag("", "DataFormat");
            Serializer.text(Integer.toString(Channel.DataFormat));
            Serializer.endTag("", "DataFormat");
        	//. Name
            Serializer.startTag("", "Name");
            Serializer.text(Channel.Name);
            Serializer.endTag("", "Name");
        	//. Info
            Serializer.startTag("", "Info");
            Serializer.text(Channel.Info);
            Serializer.endTag("", "Info");
        	//. Size
            Serializer.startTag("", "Size");
            Serializer.text(Integer.toString(Channel.Size));
            Serializer.endTag("", "Size");
        	//. Configuration
            Serializer.startTag("", "Configuration");
            Serializer.text(Channel.Configuration);
            Serializer.endTag("", "Configuration");
        	//. Parameters
            Serializer.startTag("", "Parameters");
            Serializer.text(Channel.Parameters);
            Serializer.endTag("", "Parameters");
            //. DataTypes
            if (Channel.DataTypes != null) {
                Serializer.startTag("", "DataTypes");
                Channel.DataTypes.ToXMLSerializer(Serializer);
                Serializer.endTag("", "DataTypes");
            }
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
    
	public TChannel Channels_GetOneByID(int ChannelID) {
		int Cnt = Channels.size();
		for (int I = 0; I < Cnt; I++) {
			TChannel Channel = Channels.get(I); 
			if (Channel.ID == ChannelID)
				return Channel; //. ->
		}
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
