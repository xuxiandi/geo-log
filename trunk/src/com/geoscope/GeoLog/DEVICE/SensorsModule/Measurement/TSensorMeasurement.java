package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement;

import java.util.ArrayList;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;

public class TSensorMeasurement {
	
	public static Object LastIDLock = new Object();
	public static String LastID = "";
	
	public static String GetNewID() throws InterruptedException {
		while (true) {
			String NewID = Double.toString(OleDate.UTCCurrentTimestamp());
			synchronized (LastIDLock) {
				if (!NewID.equals(LastID))
					return NewID; //. ->
				Thread.sleep(10);
			}
		}
	}

	public static TSensorMeasurementDescriptor[] Filter(TSensorMeasurementDescriptor[] Descriptors, String TypeIDPrefix) {
		int Cnt = Descriptors.length;
		ArrayList<TSensorMeasurementDescriptor> _Result = new ArrayList<TSensorMeasurementDescriptor>(Cnt);
		for (int I = 0; I < Cnt; I++) 
			if ((Descriptors[I].Model != null) && Descriptors[I].Model.TypeID.startsWith(TypeIDPrefix))
				_Result.add(Descriptors[I]);
		Cnt = _Result.size();
		TSensorMeasurementDescriptor[] Result = new TSensorMeasurementDescriptor[Cnt];
		for (int I = 0; I < Cnt; I++) 
			Result[I] = _Result.get(I);
		return Result;
	}
	
	
	public String DatabaseFolder;
	//.
	public TSensorMeasurementDescriptor Descriptor;
	
	public TSensorMeasurement(String pDatabaseFolder, String pMeasurementID, Class<?> DescriptorClass) throws Exception {
		DatabaseFolder = pDatabaseFolder;
		//.
		Descriptor = TSensorsModuleMeasurements.GetMeasurementDescriptor(DatabaseFolder, pMeasurementID, DescriptorClass);
	}
	
	public TSensorMeasurement(String pMeasurementID, Class<?> DescriptorClass) throws Exception {
		this(TSensorsModuleMeasurements.DataBaseFolder, pMeasurementID, DescriptorClass);
	}
	
	public String Folder() {
		return (DatabaseFolder+"/"+Descriptor.ID);
	}
	
	public void Start() throws Exception {
		Descriptor.StartTimestamp = OleDate.UTCCurrentTimestamp();
		TSensorsModuleMeasurements.SetMeasurementDescriptor(DatabaseFolder, Descriptor.ID, Descriptor);
		//.
		Descriptor.Model.Start();
	}
	
	public void Finish() throws Exception {
		Descriptor.Model.Stop();
		//.
		Descriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		TSensorsModuleMeasurements.SetMeasurementDescriptor(DatabaseFolder, Descriptor.ID, Descriptor);
	}
}