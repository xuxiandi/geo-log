package com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.Telemetry.TLR;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Telemetry.TLR.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TTLRMeter extends TSensorMeter {

	public TTLRMeter(TSensorsModule pSensorsModule, TSensorMeterDescriptor pDescriptor, Class<?> ProfileClass, String pProfileFolder) throws Exception {
		super(pSensorsModule, pDescriptor, ProfileClass, pProfileFolder);
	}
	
	protected TTLRChannel GetSourceTLRChannel() throws Exception {
		return null;
	}
	
	protected TMeasurement CreateTLRMeasurement() throws Exception {
		return null;
	}
	
	@Override
	protected void DoProcess() throws Exception {
		TTLRChannel SourceChannel = GetSourceTLRChannel(); 	
		if (SourceChannel == null)
			throw new IOException("no source channel"); //. =>
		//.
		SourceChannel.Suspend();
		try {
			SourceChannel.SourceChannel_Start();
			try {
				int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
				while (!Canceller.flCancel) {
					TMeasurement Measurement = CreateTLRMeasurement();
					Measurement.TLRChannel.Assign(SourceChannel);
					//.
					Measurement.Start();
					try {
						SourceChannel.DoStreaming(Measurement.TLRChannel.DestinationStream, Canceller, MeasurementMaxDuration);
					}
					finally {
						Measurement.Finish();
					}
					//.
					DoOnMeasurementFinish(Measurement);
				}
			}
			finally {
				SourceChannel.SourceChannel_Stop();
			}
		}
		finally {
			SourceChannel.Resume();
		}
	}
}
