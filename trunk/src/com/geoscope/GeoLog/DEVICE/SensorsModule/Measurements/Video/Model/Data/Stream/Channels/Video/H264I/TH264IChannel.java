package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.Stream.Channels.Video.H264I;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Video.Model.Data.TStreamChannel;

public class TH264IChannel extends TStreamChannel {

	public static final String TypeID = "Video.H264I";

	
	public static class TOutputStream extends OutputStream {
		
		private static final int BufferDescriptorSize = 4;
		
		
		private TH264IChannel Channel;
		//.
		public int	IndexBase = -1;
		public long TimestampBase;
		
		public TOutputStream(TH264IChannel pChannel) {
			Channel = pChannel;
		}
		
		private byte[] IndexBA = new byte[4];
		private byte[] TimestampBA = new byte[4];
		
		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
			do {
				int DataSize = TDataConverter.ConvertLEByteArrayToInt32(buffer, byteOffset); byteOffset += BufferDescriptorSize; byteCount -= (BufferDescriptorSize+DataSize);
				//.
				short Descriptor = TDataConverter.ConvertLEByteArrayToInt16(buffer, byteOffset); byteOffset += com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.TagSize; DataSize -= com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.TagSize;
				//.
				switch (Descriptor) {
				
				case com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.DataTag:
					Channel.VideoFrameBufferedStream.write(buffer, byteOffset, DataSize); byteOffset += DataSize; 
					//.
					Channel.Packets++;
					//.
					break; //. >
					
				case com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.IndexTag:
					//. make index as zero-based
					int Index = TDataConverter.ConvertLEByteArrayToInt32(buffer, byteOffset); byteOffset += DataSize; 
					if (IndexBase < 0)
						IndexBase = Index;
					Index -= IndexBase;
					TDataConverter.ConvertInt32ToLEByteArray(Index, IndexBA, 0);
					//.
					Channel.VideoFrameIndexBufferedStream.write(IndexBA);
					break; //. >
					
				case com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.TimestampTag:
					//. make timestamp as zero-based
					int Timestamp = TDataConverter.ConvertLEByteArrayToInt32(buffer, byteOffset); byteOffset += DataSize;
					Timestamp -= TimestampBase; 
					TDataConverter.ConvertInt32ToLEByteArray(Timestamp, TimestampBA, 0);
					//.
					Channel.VideoFrameTimestampBufferedStream.write(TimestampBA);
					break; //. >
				}
			} while (byteCount > 0);
		}

		@Override
		public void write(int arg0) throws IOException {
		}
	}
	
	private String MeasurementFolder;
	//.
	private FileOutputStream 				VideoFrameFileStream = null;
	private BufferedOutputStream			VideoFrameBufferedStream = null;
	private FileOutputStream 				VideoFrameIndexFileStream = null;
	private BufferedOutputStream			VideoFrameIndexBufferedStream = null;
	private FileOutputStream 				VideoFrameTimestampFileStream = null;
	private BufferedOutputStream			VideoFrameTimestampBufferedStream = null;
	//.
	public TOutputStream DestinationStream;
	//.
	public int		FrameRate = -1;
	public int		Packets = -1;
	
	public TH264IChannel() {
		Kind = TChannel.CHANNEL_KIND_OUT;
	}
	
	public TH264IChannel(String pMeasurementFolder) {
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
	
	@Override
	public void Start() throws Exception {
		super.Start();
		//.
		VideoFrameFileStream = new FileOutputStream(MeasurementFolder+"/"+TMeasurementDescriptor.VideoH264FileName);
		VideoFrameBufferedStream = new BufferedOutputStream(VideoFrameFileStream, 256*1024);
		VideoFrameIndexFileStream = new FileOutputStream(MeasurementFolder+"/"+TMeasurementDescriptor.VideoIndex32FileName);
		VideoFrameIndexBufferedStream = new BufferedOutputStream(VideoFrameIndexFileStream, 65535);
		VideoFrameTimestampFileStream = new FileOutputStream(MeasurementFolder+"/"+TMeasurementDescriptor.VideoTS32FileName);
		VideoFrameTimestampBufferedStream = new BufferedOutputStream(VideoFrameTimestampFileStream, 65535);
		//.
		Packets = 0;
		//.
		DestinationStream = new TOutputStream(this);
		//.
		DestinationStream.TimestampBase = System.nanoTime()/1000000;
	}
	
	@Override
	public void Stop() throws Exception {
		if (DestinationStream != null) {
			DestinationStream.close();
			DestinationStream = null;
		}
		//.
		if (VideoFrameTimestampBufferedStream != null) {
			VideoFrameTimestampBufferedStream.close();
			VideoFrameTimestampBufferedStream = null;
		}
		if (VideoFrameTimestampFileStream != null) {
			VideoFrameTimestampFileStream.close();
			VideoFrameTimestampFileStream = null;
		}
		if (VideoFrameIndexBufferedStream != null) {
			VideoFrameIndexBufferedStream.close();
			VideoFrameIndexBufferedStream = null;
		}
		if (VideoFrameIndexFileStream != null) {
			VideoFrameIndexFileStream.close();
			VideoFrameIndexFileStream = null;
		}
		if (VideoFrameBufferedStream != null) {
			VideoFrameBufferedStream.close();
			VideoFrameBufferedStream = null;
		}
		if (VideoFrameFileStream != null) {
			VideoFrameFileStream.close();
			VideoFrameFileStream = null;
		}
		//.
		super.Stop();
	}
}
