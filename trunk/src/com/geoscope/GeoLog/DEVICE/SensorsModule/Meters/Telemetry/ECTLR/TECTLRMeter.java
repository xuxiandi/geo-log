package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ECTLR;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ECTLR.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TECTLRMeter extends TSensorMeter {

	public static final String ID = "Telemetry.TLR.1";
	//.
	public static final String TypeID = "Telemetry.TLR";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Environmental conditions";
	public static final String Info = "Telemetry";
	
	public static class TMyProfile extends TProfile {
	}
	
	public TECTLRMeter(TSensorsModule pSensorsModule, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(ID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public void run() {
		try {
			if (SensorsModule.InternalSensorsModule.ECTLRChannel == null)
				return; //. ->
			TTLRChannel SourceChannel = (TTLRChannel)SensorsModule.InternalSensorsModule.ECTLRChannel.DestinationChannel_Get(); 	
			if (SourceChannel == null)
				return; //. ->
			int MeasurementMaxDuration = (int)(Profile.MeasurementMaxDuration*(24.0*3600.0*1000.0));
			while (!Canceller.flCancel) {
				TMeasurement Measurement = new TMeasurement(TSensorsModuleMeasurements.CreateNewMeasurement());
				Measurement.Start();
				try {
					SourceChannel.DoStreaming(Measurement.TLRChannel.DestrinationStream, Canceller, MeasurementMaxDuration);
				}
				finally {
					Measurement.Finish();
				}
			}
		}
		catch (InterruptedException IE) {
		} 
		catch (CancelException CE) {
		}
		catch (Exception E) {
		}
	}
}
