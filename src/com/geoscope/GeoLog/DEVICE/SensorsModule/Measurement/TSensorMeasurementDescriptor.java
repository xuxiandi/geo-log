package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement;


public class TSensorMeasurementDescriptor {
	
	public static final String DescriptorFileName = "Data.xml";
	
	public static final int LOCATION_DEVICE = 0;
	public static final int LOCATION_SERVER = 1;
	public static final int LOCATION_CLIENT = 2;
	
	public static class TLocationUpdater {
		
		public void DoOnLocationUpdated(String MeasurementID, int Location) {
		}
	}
	
	private static final double MaxMeasurementIDDeviation = 1.0/(24.0*3600.0);
	//.
	public static boolean IDsAreTheSame(String ID, String ID1) {
		return (Math.abs(Double.parseDouble(ID)-Double.parseDouble(ID1)) < MaxMeasurementIDDeviation);
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

	public TSensorMeasurementDescriptor(String pID) {
		ID = pID;
	}
	
	public boolean IsTypeOf(String TypeIDPrefix) {
		if (Model == null)
			return false; //. ->
		return (Model.TypeID.startsWith(TypeIDPrefix));
	}
	
	public boolean IsContainerTypeOf(String ContainerTypeIDPrefix) {
		if (Model == null)
			return false; //. ->
		return (Model.ContainerTypeID.startsWith(ContainerTypeIDPrefix));
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