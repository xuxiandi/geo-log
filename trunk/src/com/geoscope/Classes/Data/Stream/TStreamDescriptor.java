package com.geoscope.Classes.Data.Stream;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TStreamDescriptor {

    public String Name = "";
    public String Info = "";
    //.
    public ArrayList<TChannel> Channels = new ArrayList<TChannel>();
    
	public TStreamDescriptor(Node RootNode, TChannelProvider pChannelProvider) throws Exception {
		FromRootNode(RootNode,pChannelProvider);
	}
	
	public void FromRootNode(Node RootNode, TChannelProvider pChannelProvider) throws Exception {
		int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			try {
    			Name = TMyXML.SearchNode(RootNode,"Name").getFirstChild().getNodeValue();
    			Info = TMyXML.SearchNode(RootNode,"Info").getFirstChild().getNodeValue();
    			//.
    			Channels.clear();
				NodeList ChannelsNode = TMyXML.SearchNode(RootNode,"Channels").getChildNodes();
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
							Node ValueNode = TMyXML.SearchNode(ChannelNode,"Enabled").getFirstChild();
							if (ValueNode != null)
								Channel.Enabled = (Integer.parseInt(ValueNode.getNodeValue()) != 0);
							else
								Channel.Enabled = true;
							//.
							ValueNode = TMyXML.SearchNode(ChannelNode,"Kind").getFirstChild();
							if (ValueNode != null)
								Channel.Kind = Integer.parseInt(ValueNode.getNodeValue());
							else
								Channel.Kind = TChannel.CHANNEL_KIND_IN;
							//.
							Channel.DataFormat = Integer.parseInt(TMyXML.SearchNode(ChannelNode,"DataFormat").getFirstChild().getNodeValue());
							//.
							Channel.Name = TMyXML.SearchNode(ChannelNode,"Name").getFirstChild().getNodeValue();
							//.
							Channel.Info = TMyXML.SearchNode(ChannelNode,"Info").getFirstChild().getNodeValue();
							//.
							Channel.Size = Integer.parseInt(TMyXML.SearchNode(ChannelNode,"Size").getFirstChild().getNodeValue());
							//.
							ValueNode = TMyXML.SearchNode(ChannelNode,"Configuration").getFirstChild();
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
}
