package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TModel {

    public String Name = "";
    public String Info = "";
    //.
    public TStreamDescriptor Stream;
    
	public TModel() throws Exception {
		Stream = new TStreamDescriptor();
	}
	
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
				Node node;
    			Name = "";
    			node = TMyXML.SearchNode(RootNode,"Name").getFirstChild();
    			if (node != null)
    				Name = node.getNodeValue();
    			Info = "";
    			node = TMyXML.SearchNode(RootNode,"Info").getFirstChild();
    			if (node != null)
    				Info = node.getNodeValue();
    			//.
    			Node StreamNode = TMyXML.SearchNode(RootNode,"Stream");
    			if (StreamNode != null)
    				Stream = new TStreamDescriptor(StreamNode, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider.Instance);
    			else
    				Stream = null;
			}
			catch (Exception E) {
    			throw new Exception("error of parsing model data: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown model data version, version: "+Integer.toString(Version)); //. =>
		}
	}

	public TChannel StreamChannels_GetOneByID(int ChannelID) {
		return Stream.Channels_GetOneByID(ChannelID);
	}
}
