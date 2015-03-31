package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement;

import android.content.Context;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel.TTypeInfo;


public class TSensorMeasurementDescriptor {
	
	public static final String DescriptorFileName = "Data.xml";
	
	public static final int LOCATION_DEVICE = 0;
	public static final int LOCATION_SERVER = 1;
	public static final int LOCATION_CLIENT = 2;
	
	public static class TLocationUpdater {
		
		public void DoOnLocationUpdated(String MeasurementID, int Location) {
		}
	}
	
	public static boolean IDsAreTheSame(String ID, String ID1) {
		return ID.equals(ID1);
	}

	
	public String ID = "";
	//.
	public double StartTimestamp = 0.0;
	public double FinishTimestamp = 0.0;
	//.
	public TSensorMeasurementModel Model = null;
	//.
	public int Location = LOCATION_DEVICE;

	public TSensorMeasurementDescriptor() {
	}

	public String TypeID() {
		if (Model == null)
			return ""; //. ->
		return (Model.TypeID);
	}
	
	public TTypeInfo TypeInfo(Context context) {
		if (Model == null)
			return null; //. ->
		return com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.TModel.GetTypeInfo(Model.TypeID, context);
	}
	
	public String ContainerTypeID() {
		if (Model == null)
			return ""; //. ->
		return (Model.ContainerTypeID);
	}
	
	public boolean IsTypeOf(String TypeID) {
		if (Model == null)
			return false; //. ->
		return (Model.TypeID.startsWith(TypeID));
	}
	
	public boolean IsContainerTypeOf(String ContainerTypeID) {
		if (Model == null)
			return false; //. ->
		return (Model.ContainerTypeID.startsWith(ContainerTypeID));
	}
	
	public String Name() {
		if (Model == null)
			return ""; //. ->
		return (Model.Name);
	}
	
	public String Info() {
		if (Model == null)
			return ""; //. ->
		return (Model.Info);
	}
	
	public boolean IsStarted() {
		return (StartTimestamp != 0.0);
	}

	public boolean IsFinished() {
		return (FinishTimestamp != 0.0);
	}
	
	public boolean IsValid() {
		return (IsStarted() && IsFinished());
	}
	
	public double Duration() {
		return (FinishTimestamp-StartTimestamp);
	}

	public int DurationInMs() {
		return (int)(Duration()*24.0*3600.0*1000.0);
	}

	public long DurationInNs() {
		return (long)(Duration()*24.0*3600.0*1000000000.0);
	}
}