package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessor.TChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessors.Audio.AAC.TAACChannelProcessor;
import com.geoscope.GeoLog.DEVICE.AudioModule.Codecs.AAC.TAACADTSDecoder;

public class TAACChannel extends TStreamChannel {

	public static final String TypeID = "Audio.AAC";

	public static final int DescriptorSize = 2;
	//.
	private static final int DefaultSampleRate = 16000;
	
	public static class TDoOnSamplesHandler {
		
		public void DoOnConfiguration(int SampleRate, int ChannelCount) throws IOException {
		}	

		public void DoOnSamplesPacket(byte[] Packet, int PacketSize) {
		}
	}	
	
	
	private TAACADTSDecoder AACADTSDecoder;
	//.
	public volatile TDoOnSamplesHandler DoOnSamplesHandler = null;
	
	public TAACChannel() throws IOException {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void Start() throws Exception {
		super.Start();
		//.
		AACADTSDecoder = new TAACADTSDecoder(DefaultSampleRate) {
			
			@Override
			public void DoOnConfiguration(int SampleRate, int ChannelCount) throws IOException {
				if (DoOnSamplesHandler != null)
					DoOnSamplesHandler.DoOnConfiguration(SampleRate, ChannelCount);
			}
			
			@Override
			public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
				if (DoOnSamplesHandler != null)
					DoOnSamplesHandler.DoOnSamplesPacket(input, input_size);
				
			}
		};
	}
	
	
	@Override
	public void Stop() throws Exception {
		if (AACADTSDecoder != null) {
			AACADTSDecoder.Destroy();
			AACADTSDecoder = null;
		}
		//.
		super.Stop();
	}
	
	@Override
	public void DoStreaming(Socket Connection, InputStream pInputStream, int pInputStreamSize, OutputStream pOutputStream, TOnProgressHandler OnProgressHandler, int StreamingTimeout, int IdleTimeoutCounter, TOnIdleHandler OnIdleHandler, TCanceller Canceller) throws Exception {
		byte[] TransferBuffer = new byte[DescriptorSize];
		short Size;
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
			Size = (short)(((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF));
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
		AACADTSDecoder.DoOnAACADTSPAcket(BA, Idx, Size); Idx += Size; 
		return Idx;
	}

	@Override
	public TChannelProcessor GetProcessor() throws Exception {
		if (Processor != null)
			Processor.Destroy();
		//.
		Processor = new TAACChannelProcessor(this);
		//.
		return Processor;
	}
}
