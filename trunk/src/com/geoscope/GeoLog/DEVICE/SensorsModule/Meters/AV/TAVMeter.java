package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.AV;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorder;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.Camera;

public class TAVMeter extends TSensorMeter {

	public static final String TypeID = "AV.FRAME";
	public static final String ContainerTypeID = "";
	//.
	public static final String Name = "Video-recorder";
	public static final String Info = "Camera";
	
	public static final int VideoRecorderCheckInterval = 1000*1; // seconds
	
	public static class TMyProfile extends TProfile {
	}
	
	public TAVMeter(TSensorsModule pSensorsModule, String pID, String pProfileFolder) throws Exception {
		super(pSensorsModule, new TSensorMeterDescriptor(TypeID+"."+pID, TypeID,ContainerTypeID, Name,Info), TMyProfile.class, pProfileFolder);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void run() {
		try {
			SetStatus(STATUS_RUNNING);
			try {
				try {
					Camera.TCameraMeasurementInfo LastMeasurementInfo = null;
					while (!Canceller.flCancel) {
						if (!SensorsModule.Device.VideoRecorderModule.IsRecording())
							SensorsModule.Device.VideoRecorderModule.SetRecorderState(true);
						//.
						Thread.sleep(VideoRecorderCheckInterval);
						//.
				    	TVideoRecorder VideoRecorder = TVideoRecorder.GetVideoRecorder();
				    	if (VideoRecorder != null) {
				    		Camera.TCameraMeasurementInfo MeasurementInfo = VideoRecorder.Recording_GetMeasurementInfo();
				    		if ((MeasurementInfo == null) || !MeasurementInfo.Equals(LastMeasurementInfo)) {
				    			if (LastMeasurementInfo != null) {
									TMeasurement Measurement = new TMeasurement(SensorsModule.Device.idGeographServerObject, LastMeasurementInfo.DatabaseFolder, LastMeasurementInfo.MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
									DoOnMeasurementFinish(Measurement);
				    			}
								//.
			    				LastMeasurementInfo = MeasurementInfo;
				    		}
				    	}
					}
				}
				finally {
					SensorsModule.Device.VideoRecorderModule.SetRecorderState(false);
				}
			}
			finally {
				SetStatus(STATUS_NOTRUNNING);
			}
		}
		catch (InterruptedException IE) {
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
