package com.geoscope.Classes.Data.Stream;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

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
	
	public void FromXMLNode(Node ANode, TChannelProvider pChannelProvider) throws Exception {
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
						String TypeID = TMyXML.SearchNode(ChannelNode,"TypeID").getFirstChild().getNodeValue();
						//.
						TChannel Channel = pChannelProvider.GetChannel(TypeID);
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
            //.
            Serializer.endTag("", ChannelNodeName);
        }
        Serializer.endTag("", "Channels");
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
