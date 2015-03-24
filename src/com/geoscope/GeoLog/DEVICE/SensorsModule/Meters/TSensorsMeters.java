package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters;

import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;

public class TSensorsMeters {

	public String ProfileFolder;
	//.
	public ArrayList<TSensorMeter> Items = new ArrayList<TSensorMeter>();
	
	public TSensorsMeters(String pProfileFolder) {
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
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++)
			Items.get(I).Destroy();
		Items.clear();
	}

	private void CreateMeters() {
	}
	
	private void AddItem(TSensorMeter Meter) {
		Items.add(Meter);
	}
	
	public TSensorMeter GetItem(String MeterID) {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			if (Meter.Descriptor.ID.equals(MeterID))
				return Meter; //. ->
		}
		return null;
	}
	
	public String GetItemsList(int Version) {
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
}
