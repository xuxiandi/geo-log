package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.TStreamChannel;

public class TH264IChannel extends TStreamChannel {

	public static final String TypeID = "Video.H264I";

	
	public int		FrameRate = -1;
	public int		Packets = -1;
	
	public TH264IChannel() {
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
		FrameRate = Integer.parseInt(TMyXML.SearchNode(ANode,"FrameRate").getFirstChild().getNodeValue());
		Packets = Integer.parseInt(TMyXML.SearchNode(ANode,"Packets").getFirstChild().getNodeValue());
	}
	
	@Override
	public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
		super.ToXMLSerializer(Serializer);
    	//. FrameRate
        Serializer.startTag("", "FrameRate");
        Serializer.text(Integer.toString(FrameRate));
        Serializer.endTag("", "FrameRate");
    	//. Packets
        Serializer.startTag("", "Packets");
        Serializer.text(Integer.toString(Packets));
        Serializer.endTag("", "Packets");
	}
}
