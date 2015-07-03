package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessor.TChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessors.Video.H264I.TH264IChannelProcessor;

public class TH264IChannel extends TStreamChannel {

	public static final String TypeID = "Video.H264I";

	public static final int DescriptorSize = 4;
	
	public static final int TagSize = 2;
	
	public static final short DataTag 		= 1;
	public static final short IndexTag 		= 2;
	public static final short TimestampTag 	= 3;
	
	public static class TDoOnH264FramesHandler {
		
		public void DoOnH264Packet(byte[] Packet, int PacketOffset, int PacketSize) {
		}
	}	
	
	
	private TDoOnH264FramesHandler OnH264FramesHandler = null;
	
	public TH264IChannel() throws IOException {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void Start() throws Exception {
		super.Start();
	}
	
	
	@Override
	public void Stop() throws Exception {
		super.Stop();
	}
	
	@Override
	public void DoStreaming(Socket Connection, InputStream pInputStream, int pInputStreamSize, OutputStream pOutputStream, TOnProgressHandler OnProgressHandler, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws Exception {
		byte[] TransferBuffer = new byte[DescriptorSize];
		int Size;
		int BytesRead,BytesRead1;
		int IdleTimeoutCount = 0; 
		int _StreamingTimeout = StreamingTimeout*IdleTimeoutCounter;
		while (!Canceller.flCancel) {
			try {
				if (Connection != null)
					Connection.setSoTimeout(StreamingTimeout);
                BytesRead = pInputStream.read(TransferBuffer,0,DescriptorSize);
                if (BytesRead <= 0) 
                	break; //. >
				IdleTimeoutCount = 0;
			}
			catch (SocketTimeoutException E) {
				IdleTimeoutCount++;
				if (IdleTimeoutCount >= IdleTimeoutCounter) {
					IdleTimeoutCount = 0;
					OnIdleHandler.DoOnIdle(Canceller);
				}
				//.
				continue; //. ^
			}
			if (BytesRead != DescriptorSize)
				throw new IOException("wrong data descriptor"); //. =>
			//.
			BytesRead1 = 0;
			Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
			if (Size > 0) { 
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				if (Connection != null)
					Connection.setSoTimeout(_StreamingTimeout);
				BytesRead1 = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, Size);	
                if (BytesRead1 <= 0) 
                	break; //. >
				//. parse and process
            	ParseFromByteArrayAndProcess(TransferBuffer, 0, Size);
            	//.
            	OnProgressHandler.DoOnProgress(BytesRead1, Canceller);
			}
			//.
			if (pInputStreamSize > 0) {
				pInputStreamSize -= (BytesRead+BytesRead1);
				if (pInputStreamSize <= 0)
                	break; //. >
			}
		}    	
	}		
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx, int Size) throws Exception {
		short Tag = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += TagSize; Size -= TagSize;
		switch (Tag) {
		
		case DataTag:
			synchronized (this) {
				if (OnH264FramesHandler != null)
					OnH264FramesHandler.DoOnH264Packet(BA, Idx, Size);
			}
			break; //. >
		}
		Idx += Size; 
		return Idx;
	}

	@Override
	public TChannelProcessor GetProcessor() throws Exception {
		if (Processor != null)
			Processor.Destroy();
		//.
		Processor = new TH264IChannelProcessor(this);
		//.
		return Processor;
	}
	
	public synchronized void SetOnH264FramesHandler(TDoOnH264FramesHandler pOnH264FramesHandler) {
		OnH264FramesHandler = pOnH264FramesHandler;
	}
}
