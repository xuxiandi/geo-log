package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement;

import java.io.File;
import java.util.ArrayList;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
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
	
	public TSensorMeasurement(String pDatabaseFolder, String pMeasurementID, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		DatabaseFolder = pDatabaseFolder;
		//.
		Descriptor = GetMeasurementDescriptor(DatabaseFolder, pMeasurementID, DescriptorClass, ChannelProvider);
	}
	
	public TSensorMeasurement(String pDatabaseFolder, String pMeasurementID, TChannelProvider ChannelProvider) throws Exception {
		this(pDatabaseFolder, pMeasurementID, TSensorMeasurementDescriptor.class, ChannelProvider);
	}
	
	public TSensorMeasurement(String pMeasurementFolder, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		File MF = new File(pMeasurementFolder);
		String MeasurementFolder = MF.getParent(); 
		String MeasurementID = MF.getName();
		//.
		DatabaseFolder = MeasurementFolder;
		//.
		Descriptor = GetMeasurementDescriptor(DatabaseFolder, MeasurementID, DescriptorClass, ChannelProvider);
	}
	
	public TSensorMeasurement(String pMeasurementFolder, TChannelProvider ChannelProvider) throws Exception {
		this(pMeasurementFolder, TSensorMeasurementDescriptor.class, ChannelProvider);
	}
	
	public String Folder() {
		return (DatabaseFolder+"/"+Descriptor.ID);
	}
	
	public TSensorMeasurementDescriptor GetMeasurementDescriptor(String pDatabaseFolder, String pMeasurementID, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		TSensorMeasurementDescriptor _Descriptor = TSensorsModuleMeasurements.GetMeasurementDescriptor(pDatabaseFolder, pMeasurementID, DescriptorClass, ChannelProvider);
		if (_Descriptor == null) {
			_Descriptor = (TSensorMeasurementDescriptor)DescriptorClass.newInstance();
			_Descriptor.ID = pMeasurementID; 
		}
		return _Descriptor;
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