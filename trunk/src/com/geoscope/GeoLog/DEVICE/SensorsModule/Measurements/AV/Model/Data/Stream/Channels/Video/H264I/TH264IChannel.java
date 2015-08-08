package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.os.SystemClock;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.TStreamChannel;

public class TH264IChannel extends TStreamChannel {

	public static final String TypeID = "Video.H264I";

	
	public static class TOutputStream extends OutputStream {
		
		private static final int BufferDescriptorSize = 4;
		
		
		private TH264IChannel Channel;
		//.
		private int  	Index = 0;
		private long	TimestampBase = -1;
		//.
		private byte[] IndexBA = new byte[4];
		private byte[] TimestampBA = new byte[4];
				
		public TOutputStream(TH264IChannel pChannel) {
			Channel = pChannel;
		}
		
		@Override
		public void write(byte[] buffer, int byteOffset, int byteCount) throws IOException {
			if (TimestampBase == -1)
				TimestampBase = SystemClock.elapsedRealtime();
			//.
			do {
				int DataSize = TDataConverter.ConvertLEByteArrayToInt32(buffer, byteOffset); byteOffset += BufferDescriptorSize; byteCount -= (BufferDescriptorSize+DataSize);
				//.
				short Descriptor = TDataConverter.ConvertLEByteArrayToInt16(buffer, byteOffset); byteOffset += com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.TagSize; DataSize -= com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.TagSize;
				//.
				switch (Descriptor) {
				
				case com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.DataTag:
					Channel.VideoFrameBufferedStream.write(buffer, byteOffset, DataSize); 
					//.
					Index += DataSize;
					//.
					Channel.Packets++;
					//.
					break; //. >
					
				case com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel.IndexTag:
					TDataConverter.ConvertInt32ToLEByteArray(Index, IndexBA, 0);
					Channel.VideoFrameIndexBufferedStream.write(IndexBA);
					//.
					TDataConverter.ConvertInt32ToLEByteArray((int)(SystemClock.elapsedRealtime()-TimestampBase), TimestampBA, 0);
					Channel.VideoFrameTimestampBufferedStream.write(TimestampBA);
					//.
					break; //. >
				}
				//.
				byteOffset += DataSize;
				//.
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
