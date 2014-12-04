package com.geoscope.GeoLog.DEVICE.SensorsModule.Model;

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
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

public class TModel {

	private TSensorsModule SensorsModule;
	//.
    public String Name = "";
    public String Info = "";
    //.
    public TStreamDescriptor Stream;
    
	public TModel(TSensorsModule pSensorsModule) throws Exception {
		SensorsModule = pSensorsModule;
		//.
		Stream = new TStreamDescriptor();
	}
	
	public TModel(TSensorsModule pSensorsModule, byte[] BA) throws Exception {
		SensorsModule = pSensorsModule;
		//.
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
    				Stream = new TStreamDescriptor(StreamNode, (new com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider(SensorsModule)));
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
	        //. Stream
            Serializer.startTag("", "Stream");
            if (Stream != null)
            	Stream.ToXMLSerializer(Serializer);
            Serializer.endTag("", "Stream");
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
    
	public TChannel StreamChannels_GetOneByID(int ChannelID) {
		return Stream.Channels_GetOneByID(ChannelID);
	}
	
	public TChannel StreamChannels_GetOneByDescriptor(byte[] ChannelDescriptor) throws Exception {
		TChannel Result = Stream.Channels_GetOneByDescriptor(ChannelDescriptor);
		if (Result != null)
			return Result; //. ->
		//. 
		Result = TChannel.GetChannelFromByteArray(ChannelDescriptor, (new com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider(SensorsModule)));
		return Result;
	}
	
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(String pTypeID, int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		if (Stream == null)
			return null; //. ->
		int Cnt = Stream.Channels.size();
		for (int I = 0; I < Cnt; I++) {
			TStreamChannel Channel = (TStreamChannel)Stream.Channels.get(I);
			if (Channel.GetTypeID().equals(pTypeID))
				return Channel.GetStreamer(pidTComponent,pidComponent, pChannelID, pConfiguration,pParameters); //. ->
		}
		return null;
	}
}
