package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.Measurement.TMeasurementDescriptor;

public class TVideoRecorderServerPlayer {

	public static boolean IsDefaultPlayer(TMeasurementDescriptor MeasurementDescriptor) {
		return ((MeasurementDescriptor.Model != null) || (MeasurementDescriptor.Mode == TVideoRecorderModule.MODE_FRAMESTREAM));
	}
	
	
	private String 	MeasurementDatabaseFolder;
	private String 	MeasurementID;
	private double	MeasurementStartPosition = 0.0;
	//.
	private TMeasurementDescriptor MeasurementDescriptor;
	
	public TVideoRecorderServerPlayer(String pMeasurementDatabaseFolder, String pMeasurementID, double pMeasurementStartPosition) throws Exception {
		MeasurementDatabaseFolder = pMeasurementDatabaseFolder;
		MeasurementID = pMeasurementID;
		MeasurementStartPosition = pMeasurementStartPosition;
		//.
		MeasurementDescriptor = TVideoRecorderMeasurements.GetMeasurementDescriptor(MeasurementDatabaseFolder,MeasurementID);
	}
	
	public Intent GetPlayer(Activity context) throws Exception {
		Intent Result = null;
		if (MeasurementDescriptor.Model != null) {
            Result = new Intent(context, TVideoRecorderServerMyPlayer.class);
            Result.putExtra("MeasurementDatabaseFolder",MeasurementDatabaseFolder);
            Result.putExtra("MeasurementID",MeasurementID);
            Result.putExtra("MeasurementStartPosition",MeasurementStartPosition);
		}
		else 
			switch (MeasurementDescriptor.Mode) {
			
			case TVideoRecorderModule.MODE_MPEG4:
			case TVideoRecorderModule.MODE_3GP:
		    	Result = new Intent(Intent.ACTION_VIEW);
		    	Result.setDataAndType(Uri.parse(GetMediaFile()), "video/*");
				break; //. >
			
			default:
				if (IsDefaultPlayer(MeasurementDescriptor)) {
		            Result = new Intent(context, TVideoRecorderServerMyPlayer.class);
		            Result.putExtra("MeasurementDatabaseFolder",MeasurementDatabaseFolder);
		            Result.putExtra("MeasurementID",MeasurementID);
		            Result.putExtra("MeasurementStartPosition",MeasurementStartPosition);
				}
				break; //. >
			}
    	return Result;
	}
	
	private String GetMediaFile() {
		String FileName;
		switch (MeasurementDescriptor.Mode) {
		
		case TVideoRecorderModule.MODE_MPEG4:
			FileName = TVideoRecorderMeasurements.MediaMPEG4FileName;
			break; //. >
			
		case TVideoRecorderModule.MODE_3GP:
			FileName = TVideoRecorderMeasurements.Media3GPFileName;
			break; //. >
			
		default:
			return null; //. ->
		}
		String FN = MeasurementDatabaseFolder+"/"+MeasurementID+"/"+FileName;
		return FN;
	}
}
