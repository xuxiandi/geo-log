package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.TStreamChannel;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;

public class TTLRChannel extends TStreamChannel {

	public static final String TypeID = "Telemetry.TLR";
	//.
	public static final String FileName = "Telemetry.ztlr";

	
	private String OutFile;
	//.
	private ByteArrayOutputStream	OutFileStream;
	public ZOutputStream			DestrinationStream;
	
	public TTLRChannel() {
		Kind = TChannel.CHANNEL_KIND_OUT;
		//.
		OutFileStream = null;
		DestrinationStream = null;
	}

	public TTLRChannel(String pMeasurementFolder) {
		this();
		//.
		OutFile = pMeasurementFolder+"/"+FileName;
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}

	@Override
	public void Start() throws Exception {
		super.Start();
		//.
		OutFileStream = new ByteArrayOutputStream();
		DestrinationStream = new ZOutputStream(OutFileStream,JZlib.Z_BEST_SPEED);
	}
	
	@Override
	public void Stop() throws Exception {
		if (DestrinationStream != null) {
			DestrinationStream.close();
			DestrinationStream = null;
		}
		if (OutFileStream != null) {
			FileOutputStream FOS = new FileOutputStream(OutFile);
			try {
				FOS.write(OutFileStream.toByteArray());
			}
			finally {
				FOS.close();
			}
			//.
			OutFileStream.close();
			OutFileStream = null;
		}
		//.
		super.Close();
	}
}
