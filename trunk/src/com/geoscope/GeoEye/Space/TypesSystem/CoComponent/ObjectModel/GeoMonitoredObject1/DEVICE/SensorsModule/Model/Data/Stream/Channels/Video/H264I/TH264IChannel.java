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
	
	public static final short ChannelStreamSessionTag 	= 0;
	public static final short DataTag 					= 1;
	public static final short IndexTag 					= 2;
	public static final short TimestampTag 				= 3;
	public static final short DataTimestampTag 			= 4;
	
	public static class TDoOnH264FramesHandler {
		
		public void DoOnH264Packet(int Timestamp, byte[] Packet, int PacketOffset, int PacketSize) throws Exception {
		}
	}	
	
	
	public int FrameRate = -1;
	//.
	private int LastDataTimestamp = 0;
	//.
	private TDoOnH264FramesHandler OnH264FramesHandler = null;
	//.
	private volatile TH264IChannelFlowControl FlowControl = null;
	
	public TH264IChannel() throws IOException {
		super();
	}
	
	@Override
	public void Close() throws Exception {
		FlowControl_Finalize();
		//.
		super.Close();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void Parse() throws Exception {
		super.Parse();
		//. defaults
		FrameRate = -1;
		//.
		TConfigurationParser CP = new TConfigurationParser(Configuration);
		if (CP.DecoderConfiguration != null) {
			int Version = Integer.parseInt(CP.DecoderConfiguration[0]);
			if (Version != 1)
				throw new Exception("unknown configuration version"); //. =>
			//. byte[] ConfigurationBuffer = Base64.decode(CP.DecoderConfiguration[1], Base64.NO_WRAP);
			if (CP.DecoderConfiguration.length > 2)
				FrameRate= Integer.parseInt(CP.DecoderConfiguration[2]);
		}
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
		//.
		FlowControl_Start();
		try {
			while (!Canceller.flCancel) {
				try {
					if (Connection != null)
						Connection.setSoTimeout(StreamingTimeout);
	                BytesRead = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, DescriptorSize);
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
		finally {
			FlowControl_Stop();
		}
	}		
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx, int Size) throws Exception {
		short Tag = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += TagSize; Size -= TagSize;
		switch (Tag) {
		
		case DataTimestampTag:
			LastDataTimestamp = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); 
			break; //. >
			
		case DataTag:
			synchronized (this) {
				if (OnH264FramesHandler != null)
					OnH264FramesHandler.DoOnH264Packet(LastDataTimestamp, BA, Idx, Size);
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
	
	public void FlowControl_Initialize(TH264IChannelFlowControl pFlowControl) {
		FlowControl = pFlowControl;
	}
	
	private void FlowControl_Finalize() throws Exception {
		if (FlowControl != null) {
			FlowControl.Destroy();
			FlowControl = null;
		}
	}
	
	private void FlowControl_Start() throws Exception {
		if (FlowControl != null)
			FlowControl.Start();
	}
	
	private void FlowControl_Stop() throws Exception {
		if (FlowControl != null) 
			FlowControl.Stop();
	}
}
