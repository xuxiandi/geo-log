package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters;

import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterInfo;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.AV.TAVMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Audio.TAudioMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ASTLR.TASTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ECTLR.TECTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.GPSTLR.TGPSTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Video.TVideoMeter;

public class TSensorsMeters {

	private TSensorsModule SensorsModule;
	//.
	public String ProfileFolder;
	//.
	public ArrayList<TSensorMeter> Items = new ArrayList<TSensorMeter>();
	
	public TSensorsMeters(TSensorsModule pSensorsModule, String pProfileFolder) {
		SensorsModule = pSensorsModule;
		ProfileFolder = pProfileFolder;
	}
	
	public void Destroy() throws Exception {
		Finalize();
	}

	public void Initialize() throws Exception {
		CreateMeters();
		//.
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++)
			Items.get(I).Initialize();
	}
	
	public void Finalize() throws Exception {
		Items_Clear();
	}

	private void CreateMeters() throws Exception {
		Items_Clear();
		//.
		TECTLRMeter 	ECTLRMeter 	= new TECTLRMeter(SensorsModule, 	"0", 	ProfileFolder); 	Items_AddItem(ECTLRMeter);
		TASTLRMeter 	ASTLRMeter 	= new TASTLRMeter(SensorsModule, 	"0", 	ProfileFolder); 	Items_AddItem(ASTLRMeter);
		TGPSTLRMeter	GPSTLRMeter = new TGPSTLRMeter(SensorsModule, 	"0", 	ProfileFolder); 	Items_AddItem(GPSTLRMeter);
		TAudioMeter		AudioMeter	= new TAudioMeter(SensorsModule, 	"0", 	ProfileFolder); 	Items_AddItem(AudioMeter);
		TVideoMeter		VideoMeter	= new TVideoMeter(SensorsModule, 	"0", 	ProfileFolder); 	Items_AddItem(VideoMeter);
		TAVMeter 		AVMeter		= new TAVMeter(SensorsModule, 		"0",	ProfileFolder); 	Items_AddItem(AVMeter);
	}
	
	private void Items_AddItem(TSensorMeter Meter) {
		Items.add(Meter);
	}
	
	private void Items_Clear() throws Exception {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++)
			Items.get(I).Destroy();
		Items.clear();
	}
	
	public TSensorMeter Items_GetItem(String MeterID) {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			if (Meter.Descriptor.ID.equals(MeterID))
				return Meter; //. ->
		}
		return null;
	}
	
	public String Items_GetList(int Version) {
		switch (Version) {
		
		case 1:
			StringBuilder SB = new StringBuilder();
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TSensorMeter Meter = Items.get(I);
				SB.append(Meter.Descriptor.ID);
				SB.append(","+Meter.Descriptor.TypeID);
				SB.append(","+Meter.Descriptor.ContainerTypeID);
				SB.append(","+Meter.Descriptor.Name);
				SB.append(","+Meter.Descriptor.Info);
				SB.append(","+Meter.Descriptor.Configuration);
				SB.append(","+Meter.Descriptor.Parameters);
				SB.append(","+(Meter.IsEnabled() ? "1" : "0"));
				SB.append(","+(Meter.IsActive() ? "1" : "0"));
				SB.append(","+Integer.toString(Meter.GetStatus()));
				if (I < (Cnt-1))
					SB.append(";");
			}
			return SB.toString(); //. >
			
		default:
			return null; //. ->
		}
	}

	public TSensorMeterInfo[] Items_GetList() {
		int Cnt = Items.size();
		TSensorMeterInfo[] Result = new TSensorMeterInfo[Cnt];
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			//.
			TSensorMeterDescriptor Descriptor = new TSensorMeterDescriptor();
			//.
			Descriptor.ID = Meter.Descriptor.ID;
			//.
			Descriptor.TypeID = Meter.Descriptor.TypeID;
			Descriptor.ContainerTypeID = Meter.Descriptor.ContainerTypeID;
			//.
			Descriptor.Name = Meter.Descriptor.Name;
			Descriptor.Info = Meter.Descriptor.Info;
			//.
			Descriptor.Configuration = Meter.Descriptor.Configuration;
			Descriptor.Parameters = Meter.Descriptor.Parameters;
			//.
			Result[I] = new TSensorMeterInfo(Descriptor, Meter.IsEnabled(), Meter.IsActive(), Meter.GetStatus());
		}
		return Result;
	}
	
	public void Items_ValidateActivity(String[] MeterIDs) throws Exception {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			boolean flFound = false;
			int Cnt1 = MeterIDs.length;
			for (int J = 0; J < Cnt1; J++)
				if (MeterIDs[J].equals(Meter.Descriptor.ID)) {
					flFound = true;
					break; //. >
				}
			if (flFound) {
				if (!Meter.IsActive())
					Meter.SetActive(true);
			}
			else {
				if (Meter.IsActive())
					Meter.SetActive(false);
			}
		}
	}
	
	public void Measurements_RemoveOld() throws Exception {
    	ArrayList<String> MIDs = TSensorsModuleMeasurements.GetMeasurementsIDs();
    	if (MIDs == null)
    		return; //. ->
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			Meter.Measurements_RemoveOld(MIDs);
		}
	}	
}
