package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule;

import android.app.Activity;
import android.content.Intent;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TSensorMeasurementDescriptor;

public class TVideoRecorderServerPlayer {

	public static boolean IsDefaultPlayer(TSensorMeasurementDescriptor MeasurementDescriptor) {
		return (MeasurementDescriptor.IsTypeOf(com.geoscope.GeoLog.DEVICE.VideoRecorderModule.Measurement.TMeasurementDescriptor.TypeIDPrefix));
	}
	
	
	private String 	MeasurementDatabaseFolder;
	private String 	MeasurementID;
	private double	MeasurementStartPosition = 0.0;
	//.
	private TSensorMeasurementDescriptor MeasurementDescriptor;
	
	public TVideoRecorderServerPlayer(String pMeasurementDatabaseFolder, String pMeasurementID, double pMeasurementStartPosition) throws Exception {
		MeasurementDatabaseFolder = pMeasurementDatabaseFolder;
		MeasurementID = pMeasurementID;
		MeasurementStartPosition = pMeasurementStartPosition;
		//.
		MeasurementDescriptor = TSensorsModuleMeasurements.GetMeasurementDescriptor(MeasurementDatabaseFolder,MeasurementID);
	}
	
	public Intent GetPlayer(Activity context) throws Exception {
		Intent Result = null;
		if (MeasurementDescriptor.Model != null) {
            Result = new Intent(context, TVideoRecorderServerMyPlayer.class);
            Result.putExtra("MeasurementDatabaseFolder",MeasurementDatabaseFolder);
            Result.putExtra("MeasurementID",MeasurementID);
            Result.putExtra("MeasurementStartPosition",MeasurementStartPosition);
		}
    	return Result;
	}
}
