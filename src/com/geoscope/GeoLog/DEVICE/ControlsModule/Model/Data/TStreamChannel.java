package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.MultiThreading.TCanceller;

public class TStreamChannel extends TChannel {

	public com.geoscope.Classes.Data.Stream.Channel.TChannel DestinationChannel = null;
	
	@Override
	public boolean StreamableViaComponent() {
		return ((DestinationChannel != null) && DestinationChannel.Profile.StreamableViaComponent);
	}
	
	@Override
	public void Profile_FromByteArray(byte[] BA) throws Exception {
		if (DestinationChannel != null)
			DestinationChannel.Profile_FromByteArray(BA);
	}
	
	@Override
	public void Profile_FromXMLNode(Node ANode) throws Exception {
		if (DestinationChannel != null)
			DestinationChannel.Profile_FromXMLNode(ANode);
	}
	
	@Override
	public byte[] Profile_ToByteArray() throws Exception {
		if (DestinationChannel != null)
			return DestinationChannel.Profile_ToByteArray(); //. ->
		else
			return null; //. ->
	}
	
	@Override
	public void Profile_ToXMLSerializer(XmlSerializer Serializer) throws Exception {
		if (DestinationChannel != null)
			DestinationChannel.Profile_ToXMLSerializer(Serializer);
	}
	
	public void DoStreaming(InputStream pInputStream, OutputStream pOutputStream, TCanceller Canceller) throws IOException {
	}	
	
	public byte[] ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		return null;
	}
}
