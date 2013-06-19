package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.IOException;

import android.content.Intent;
import android.net.Uri;

import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;

public class TVideoRecorderServerPlayer {

	private String MeasurementDatabaseFolder;
	private String MeasurementID;
	
	public TVideoRecorderServerPlayer(String pMeasurementDatabaseFolder, String pMeasurementID) {
		MeasurementDatabaseFolder = pMeasurementDatabaseFolder;
		MeasurementID = pMeasurementID;
	}
	
	public Intent GetPlayer() throws Exception {
    	Intent Result = new Intent(Intent.ACTION_VIEW);
    	Result.setDataAndType(Uri.parse(GetMediaFile()), "video/*");
    	return Result;
	}
	
	private String GetMediaFile() throws Exception {
		TMeasurementDescriptor MD = TVideoRecorderMeasurements.GetMeasurementDescriptor(MeasurementDatabaseFolder,MeasurementID);
		String FileName;
		switch (MD.Mode) {
		
		case TVideoRecorderModule.MODE_MPEG4:
			FileName = TVideoRecorderMeasurements.MediaMPEG4FileName;
			break; //. >
			
		case TVideoRecorderModule.MODE_3GP:
			FileName = TVideoRecorderMeasurements.Media3GPFileName;
			break; //. >
			
		default:
			throw new IOException("unknown media type"); //. =>
		}
		String FN = MeasurementDatabaseFolder+"/"+MeasurementID+"/"+FileName;
		return FN;
	}
}
