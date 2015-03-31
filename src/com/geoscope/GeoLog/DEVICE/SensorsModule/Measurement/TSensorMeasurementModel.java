package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement;

import java.io.IOException;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.MultiThreading.TCanceller;

public class TSensorMeasurementModel {
	
	public static class TTypeInfo {
		
		public String 	TypeName;
		public int		TypeColor;
		
		public TTypeInfo(String pTypeName, int pTypeColor) {
			TypeName = pTypeName;
			TypeColor = pTypeColor;
		}
	}
	

	public String TypeID = ""; 				//. type
	public String ContainerTypeID = ""; 	//. container(format) type
	
    public String Name = "";
    public String Info = "";
    //.
    public TStreamDescriptor Stream;
    
	public TSensorMeasurementModel() {
		Stream = new TStreamDescriptor();
	}
	
	public TSensorMeasurementModel(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		FromXMLNode(ANode, ChannelProvider);
	}
	
	public void Initialize(Object Parameters) throws Exception {
		Stream.Initialize(Parameters);
	}
	
    public void Start() throws IOException {
    	Stream.Start();
    }

    public void Stop() throws IOException {
    	Stream.Stop();
    }

	public void Process(TCanceller Canceller) throws Exception {
		Stream.Process(Canceller);
	}
	
	public void FromXMLNode(Node ANode, com.geoscope.Classes.Data.Stream.Channel.TChannelProvider ChannelProvider) throws Exception {
		int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			try {
				Node node,valuenode;
				TypeID = "";
    			node = TMyXML.SearchNode(ANode,"TypeID");
    			if (node != null) {
    				valuenode = node.getFirstChild();
    				if (valuenode != null)
	    				TypeID = valuenode.getNodeValue();
    			} 
				ContainerTypeID = "";
    			node = TMyXML.SearchNode(ANode,"ContainerTypeID");
    			if (node != null) {
    				valuenode = node.getFirstChild();
    				if (valuenode != null)
    					ContainerTypeID = valuenode.getNodeValue();
    			}
    			Name = "";
    			node = TMyXML.SearchNode(ANode,"Name");
    			if (node != null) {
    				valuenode = node.getFirstChild();
    				if (valuenode != null)
    					Name = valuenode.getNodeValue();
    			}
    			Info = "";
    			node = TMyXML.SearchNode(ANode,"Info");
    			if (node != null) {
    				valuenode = node.getFirstChild();
    				if (valuenode != null)
    					Info = valuenode.getNodeValue();
    			}
    			//.
    			Node StreamNode = TMyXML.SearchNode(ANode,"Stream");
    			if (StreamNode != null)
    				Stream = new TStreamDescriptor(StreamNode, ChannelProvider);
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
	
    public void ToXMLNode(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. TypeID
        Serializer.startTag("", "TypeID");
        Serializer.text(TypeID);
        Serializer.endTag("", "TypeID");
        //. ContainerTypeID
        Serializer.startTag("", "ContainerTypeID");
        Serializer.text(ContainerTypeID);
        Serializer.endTag("", "ContainerTypeID");
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
    }
}