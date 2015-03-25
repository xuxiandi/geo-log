package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements;

import java.io.File;

import android.app.Activity;
import android.content.Intent;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerPlayer;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerArchive.TMeasurementPlayHandler;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TSystemTGeographServerObject;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;

public class TSensorsModuleMeasurements {

	public static String Context_Folder(long GeographServerObjectID) throws Exception {
		return TSystemTGeographServerObject.ContextFolder+"/"+Long.toString(GeographServerObjectID)+"/"+"SensorsModule"+"/"+"Measurements";		
	}
	
	public static String Context_GetMeasurementFolder(long GeographServerObjectID, String MeasurementID) throws Exception {
		return Context_Folder(GeographServerObjectID)+"/"+MeasurementID;
	}
	
	public static String Context_CreateMeasurementFolder(long GeographServerObjectID, String MeasurementID) throws Exception {
		String Result = Context_GetMeasurementFolder(GeographServerObjectID, MeasurementID);
		File F = new File(Result);
		F.mkdirs();
		//.
		return Result;
	}
	
	public static void Context_RemoveMeasurement(long GeographServerObjectID, String MeasurementID) throws Exception {
		com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.DeleteMeasurement(Context_Folder(GeographServerObjectID), MeasurementID);
	}
	
	public static String Context_GetMeasurementTempFolder(long GeographServerObjectID, String MeasurementID) throws Exception {
		return TGeoLogApplication.GetTempFolder()+"/"+TGeoLogApplication.ProfileName()+"/"+"GeographServerObject"+"/"+Long.toString(GeographServerObjectID)+"/"+"SensorsModule"+"/"+"Measurements"+"/"+MeasurementID;
	}
	
	public static String Context_CreateMeasurementTempFolder(long GeographServerObjectID, String MeasurementID) throws Exception {
		String Result = Context_GetMeasurementTempFolder(GeographServerObjectID, MeasurementID);
		File F = new File(Result);
		F.mkdirs();
		//.
		return Result;
	}
	
	public static TSensorMeasurementDescriptor[] Context_GetMeasurementsList(long GeographServerObjectID, double BeginTimestamp, double EndTimestamp) throws Exception {
		String ResultString = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.GetMeasurementsList(Context_Folder(GeographServerObjectID), BeginTimestamp,EndTimestamp, (short)1/*Version*/);
		TSensorMeasurementDescriptor[] Result;
		if ((ResultString != null) && (!ResultString.equals(""))) {
			String[] Items = ResultString.split(";");
			Result = new TSensorMeasurementDescriptor[Items.length];
			for (int I = 0; I < Items.length; I++) {
				String[] Properties = Items[I].split(",");
				Result[I] = new TSensorMeasurementDescriptor();
				//.
				Result[I].ID = Properties[0];
				//.
				Result[I].StartTimestamp = Double.parseDouble(Properties[1]);
				Result[I].FinishTimestamp = Double.parseDouble(Properties[2]);
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel Model = new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel();
				Model.TypeID = Properties[3];
				Model.ContainerTypeID = Properties[4];
				Result[I].Model = Model;
				//.
				Result[I].Location = TSensorMeasurementDescriptor.LOCATION_CLIENT;
			}
		}
		else
			Result = new TSensorMeasurementDescriptor[0];
		return Result;
	}
	
	public static TSensorMeasurementDescriptor[] Context_GetMeasurementsList(long GeographServerObjectID) throws Exception {
		return Context_GetMeasurementsList(GeographServerObjectID, -Double.MAX_VALUE,Double.MAX_VALUE);
	}
	
	public static boolean Context_IsMeasurementExist(long GeographServerObjectID, String MeasurementID) throws Exception {
		File F = new File(Context_GetMeasurementFolder(GeographServerObjectID, MeasurementID));
		return F.exists();
	}
	
	public static void Context_PlayMeasurementByFolder(TMeasurementPlayHandler PlayHandler, int PlayerRequest, long GeographServerObjectID, String MeasurementFolder, double MeasurementStartPosition, Activity context) throws Exception {
    	File MF = new File(MeasurementFolder);
    	String MeasurementDatabaseFolder = MF.getParent(); 
    	String MeasurementID = MF.getName();
    	//.
		boolean flPlayAsDefault = true;
		if (PlayHandler != null) {
			TSensorMeasurementDescriptor MeasurementDescriptor = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.GetMeasurementDescriptor(MeasurementDatabaseFolder, MeasurementID, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
			flPlayAsDefault = (!PlayHandler.PlayMeasurement(MeasurementDescriptor, MeasurementStartPosition));
		}
		if (flPlayAsDefault) {
        	TVideoRecorderServerPlayer Player = new TVideoRecorderServerPlayer(MeasurementDatabaseFolder,MeasurementID, MeasurementStartPosition);
        	Intent PI = Player.GetPlayer(context);
        	if (PI != null)
        		context.startActivityForResult(PI, PlayerRequest);	            	
		}
	}
	
	public static void Context_PlayMeasurement(TMeasurementPlayHandler PlayHandler, int PlayerRequest, long GeographServerObjectID, String MeasurementID, double MeasurementStartPosition, Activity context) throws Exception {
		Context_PlayMeasurementByFolder(PlayHandler,PlayerRequest, GeographServerObjectID, Context_GetMeasurementFolder(GeographServerObjectID, MeasurementID), MeasurementStartPosition, context);
	}
}
