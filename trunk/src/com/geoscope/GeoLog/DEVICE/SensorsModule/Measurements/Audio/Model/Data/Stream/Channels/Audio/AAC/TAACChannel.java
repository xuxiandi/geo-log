package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.Stream.Channels.Audio.AAC;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Audio.Model.Data.TStreamChannel;

public class TAACChannel extends TStreamChannel {

	public static final String TypeID = "Audio.AAC";

	
	public static class TOutputStream extends FileOutputStream {
		
		private static final int BufferDescriptorSize = 2;
		
		
		private TAACChannel Channel;
		
		public TOutputStream(TAACChannel pChannel, String FileName) throws FileNotFoundException {
			super(FileName);
			//.
			Channel = pChannel;
		}
		
		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
			super.write(buffer, byteOffset+BufferDescriptorSize, byteCount-BufferDescriptorSize); //. write buffer without descriptor
			//.
			Channel.Packets++;
		}
	}
	
	
	private String MeasurementFolder;
	//.
	public TOutputStream DestinationStream;
	//.
	public int		SampleRate = -1;
	public int		Packets = -1;
	
	public TAACChannel() {
		Kind = TChannel.CHANNEL_KIND_OUT;
	}
	
	public TAACChannel(String pMeasurementFolder) {
		this();
		//.
		MeasurementFolder = pMeasurementFolder;
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
	
	@Override
	public void Start() throws Exception {
		super.Start();
		//.
		DestinationStream = new TOutputStream(this, MeasurementFolder+"/"+com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor.AudioAACADTSFileName);
		//.
		Packets = 0;
	}
	
	@Override
	public void Stop() throws Exception {
		if (DestinationStream != null) {
			DestinationStream.close();
			DestinationStream = null;
		}
		//.
		super.Stop();
	}
}
