package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.TStreamChannel;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;

public class TTLRChannel extends TStreamChannel {

	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel.TypeID;
	
	
	private String InFile;
	//.
	private ByteArrayOutputStream	OutFileStream;
	public ZOutputStream			DestrinationStream;
	
	public TTLRChannel() {
		Kind = TChannel.CHANNEL_KIND_IN;
		//.
		OutFileStream = null;
		DestrinationStream = null;
	}

	public TTLRChannel(String pMeasurementFolder) {
		this();
		//.
		InFile = pMeasurementFolder+"/"+com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel.FileName;
	}

	@Override
	public void Start() throws IOException {
		super.Start();
		//.
		OutFileStream = new ByteArrayOutputStream();
		DestrinationStream = new ZOutputStream(OutFileStream,JZlib.Z_BEST_SPEED);
	}
	
	@Override
	public void Stop() throws IOException {
		if (DestrinationStream != null) {
			DestrinationStream.close();
			DestrinationStream = null;
		}
		if (OutFileStream != null) {
			FileOutputStream FOS = new FileOutputStream(InFile);
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
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
}
