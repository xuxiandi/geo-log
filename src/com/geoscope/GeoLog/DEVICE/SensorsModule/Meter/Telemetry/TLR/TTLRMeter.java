package com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.Telemetry.TLR;

import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedReadWriteLock;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Telemetry.TLR.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TTLRMeter extends TSensorMeter {

	protected TTLRChannel SourceChannel;
	
	public TTLRMeter(TSensorsModule pSensorsModule, TSensorMeterDescriptor pDescriptor, Class<?> ProfileClass, String pProfileFolder) throws Exception {
		super(pSensorsModule, pDescriptor, ProfileClass, pProfileFolder);
	}
	
	protected TMeasurement CreateMeasurement() throws Exception {
		return null;
	}
	
	@Override
	protected void DoProcess() throws Exception {
		GetChannels();
		//.
		SourceChannel.Suspend();
		try {
			SourceChannel.SourceChannel_Start();
			try {
				int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
				while (!Canceller.flCancel) {
					TMeasurement Measurement = CreateMeasurement();
					//.
					TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.WriteLock(Measurement.Domain, Measurement.Descriptor.ID); //. lock new measurement
					try {
						//. setup measurement
						Measurement.TLRChannel.Assign(SourceChannel);
						//.
						Measurement.Start();
						try {
							SourceChannel.DoStreaming(Measurement.TLRChannel.DestinationStream, Canceller, MeasurementMaxDuration);
						}
						finally {
							Measurement.Finish();
						}
					}
					finally {
						MeasurementLock.WriteUnLock();
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
