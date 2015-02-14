package com.geoscope.GeoLog.DEVICE.VideoModule.Codecs.H264;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

import android.graphics.ImageFormat;

public class TH264Transcoder {

	private TDEVICEModule Device;
	//.
	protected int OutFrameWidth;
	protected int OutFrameHeight;
	protected int OutBitRate;
	protected int OutFrameRate;
	//.
	private TH264Decoder Decoder;
	private TH264Encoder Encoder;

	public TH264Transcoder(TDEVICEModule pDevice, int pInFrameWidth, int pInFrameHeight, int pOutFrameWidth, int pOutFrameHeight, int pOutBitRate, int pOutFrameRate, boolean pflParseParameters) throws IOException {
		Device = pDevice;
		//.
		OutFrameWidth = pOutFrameWidth;
		OutFrameHeight = pOutFrameHeight;
		OutBitRate = pOutBitRate;
		OutFrameRate = pOutFrameRate;
		//.
		Encoder = new TH264Encoder(OutFrameWidth,OutFrameHeight,OutBitRate,OutFrameRate, ImageFormat.UNKNOWN, pflParseParameters) {
			
			@Override
			public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws Exception {
				TH264Transcoder.this.DoOnOutputBuffer(Buffer,BufferSize, Timestamp,flSyncFrame);
			}
		};
		Decoder = new TH264Decoder(Device, pInFrameWidth,pInFrameHeight) {
			
			@Override
			public void DoOnOutputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
				Encoder.EncodeInputBuffer(input,input_size, Timestamp);
			}
		};
	}
	
	public void Destroy() throws Exception {
		if (Decoder != null) {
			Decoder.Destroy();
			Decoder = null;
		}
		if (Encoder != null) {
			Encoder.Destroy();
			Encoder = null;
		}
	}

	public void DoOnInputBuffer(byte[] input, int input_size, long Timestamp) throws IOException {
		Decoder.DoOnInputBuffer(input,input_size, Timestamp);
	}

	public void DoOnOutputBuffer(byte[] Buffer, int BufferSize, long Timestamp, boolean flSyncFrame) throws Exception {
	}
}
