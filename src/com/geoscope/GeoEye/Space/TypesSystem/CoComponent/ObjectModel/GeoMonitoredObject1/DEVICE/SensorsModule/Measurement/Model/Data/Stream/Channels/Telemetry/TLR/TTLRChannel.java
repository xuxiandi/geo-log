package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.TStreamChannel;
import com.jcraft.jzlib.ZInputStream;

public class TTLRChannel extends TStreamChannel {

	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel.TypeID;
	
	
	private String InFile;
	//.
	private FileInputStream InFileStream;
	//.
	public com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel.TDoOnDataHandler OnDataHandler = null;
	
	public TTLRChannel() {
		Kind = TChannel.CHANNEL_KIND_IN;
		//.
		InFileStream = null;
	}

	public TTLRChannel(String pMeasurementFolder) {
		this();
		//.
		InFile = pMeasurementFolder+"/"+com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel.FileName;
	}

	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void Start() throws IOException {
		super.Start();
		//.
		InFileStream = new FileInputStream(InFile);
	}
	
	@Override
	public void Stop() throws IOException {
		if (InFileStream != null) {
			InFileStream.close();
			InFileStream = null;
		}
		//.
		super.Close();
	}

	@Override
	public void Process(TCanceller Canceller) throws Exception {
		ZInputStream ZIS = new ZInputStream(InFileStream);
		try {
			byte[] Buffer = new byte[8192];
			int ReadSize;
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
			try {
				while ((ReadSize = ZIS.read(Buffer)) > 0) 
					BOS.write(Buffer, 0,ReadSize);
				//.
				ByteArrayInputStream BIS = new ByteArrayInputStream(BOS.toByteArray());
				try {
					com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel DestinationChannel = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel();
					DestinationChannel.Assign(this);
					DestinationChannel.OnDataHandler = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel.TDoOnDataHandler() {
						@Override
						public void DoOnData(TDataType DataType) {
							TTLRChannel.this.DoOnData(DataType);
						}
					};
					com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TOnProgressHandler OnProgressHandler = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TOnProgressHandler() {
						
						@Override
						public void DoOnProgress(int ReadSize, TCanceller Canceller) {
						}
					};
					com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TOnIdleHandler OnIdleHandler = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel.TOnIdleHandler() {
						
						@Override
						public void DoOnIdle(TCanceller Canceller) throws Exception {
							throw new IOException("file reading timeout, "+InFile); //. =>
						}
					};
					//. processing ...
					DestinationChannel.DoStreaming(BIS,BOS.size(), null, OnProgressHandler, 1000, 5, OnIdleHandler, Canceller);
				}
				finally {
					BIS.close();
				}
			}
			finally {
				BOS.close();
			}
		}
		finally {
			ZIS.close();
		}
	}
	
	private void DoOnData(TDataType DataType) {
		if (OnDataHandler != null)
			OnDataHandler.DoOnData(DataType);
	}
}
