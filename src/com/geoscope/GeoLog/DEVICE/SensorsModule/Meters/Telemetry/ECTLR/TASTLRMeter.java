package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ECTLR;

import java.io.IOException;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.ASTLR.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TASTLRMeter extends TSensorMeter {

	public static final String ID = "Telemetry.ASTLR.1";
	//.
	public static final String TypeID = "Telemetry.TLR";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Android state";
	public static final String Info = "Telemetry";
	
	public static class TMyProfile extends TProfile {
	}
	
	public TASTLRMeter(TSensorsModule pSensorsModule, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(ID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public void run() {
		try {
			SetStatus(STATUS_RUNNING);
			try {
				if (SensorsModule.InternalSensorsModule.ASTLRChannel == null)
					throw new IOException("no origin channel"); //. =>
				TTLRChannel SourceChannel = (TTLRChannel)SensorsModule.InternalSensorsModule.ASTLRChannel.DestinationChannel_Get(); 	
				if (SourceChannel == null)
					throw new IOException("no source channel"); //. =>
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
			finally {
				SetStatus(STATUS_NOTRUNNING);
			}
		}
		catch (InterruptedException IE) {
		} 
		catch (CancelException CE) {
		}
    	catch (Throwable E) {
			SetStatus(STATUS_ERROR);
			//.
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			SensorsModule.Device.Log.WriteError("Sensors meter: "+TypeID,S);
    	}
	}
}
