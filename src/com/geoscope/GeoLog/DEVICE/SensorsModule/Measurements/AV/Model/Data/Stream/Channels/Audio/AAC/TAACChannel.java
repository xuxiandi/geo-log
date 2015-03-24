package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.TStreamChannel;

public class TAACChannel extends TStreamChannel {

	public static final String TypeID = "Audio.AAC";
	

	public int		SampleRate = -1;
	public int		Packets = -1;
	
	public TAACChannel() {
		Kind = TChannel.CHANNEL_KIND_OUT;
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}

	@Override
	public void FromXMLNode(Node ANode) throws Exception {
		super.FromXMLNode(ANode);
		//.
		SampleRate = Integer.parseInt(TMyXML.SearchNode(ANode,"SampleRate").getFirstChild().getNodeValue());
		Packets = Integer.parseInt(TMyXML.SearchNode(ANode,"Packets").getFirstChild().getNodeValue());
	}
	
	@Override
	public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
		super.ToXMLSerializer(Serializer);
    	//. SampleRate
        Serializer.startTag("", "SampleRate");
        Serializer.text(Integer.toString(SampleRate));
        Serializer.endTag("", "SampleRate");
    	//. Packets
        Serializer.startTag("", "Packets");
        Serializer.text(Integer.toString(Packets));
        Serializer.endTag("", "Packets");
	}
}
