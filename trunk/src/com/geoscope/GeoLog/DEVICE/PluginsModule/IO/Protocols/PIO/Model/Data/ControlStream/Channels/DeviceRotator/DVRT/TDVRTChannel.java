package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.DeviceRotator.DVRT;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;

public class TDVRTChannel extends TStreamChannel {

	public static final String TypeID = "DeviceRotator.DVRT";


	public int Latitude_DAC_Index = -1;
	public int Longitude_DAC_Index = -1;
	
	public TDVRTChannel(TPluginModule pPluginModule) {
		super(pPluginModule);
	}	

	@Override
	public String GetTypeID() {
		return TypeID;
	}

	@Override
	public void Parse() throws Exception {
		TConfigurationParser CP = new TConfigurationParser(Configuration);
		if (CP.CoderConfiguration != null) {
			if (CP.CoderConfiguration.length > 0) {
				Latitude_DAC_Index = Integer.parseInt(CP.CoderConfiguration[1]);
				if (CP.CoderConfiguration.length > 1) {
					Longitude_DAC_Index = Integer.parseInt(CP.CoderConfiguration[2]);
				}
			}
		}
	}

	public void ProcessCommand(PIO.TCommand Command) throws Exception {
		PluginModule.OutgoingCommands_ProcessCommand(Command);
	}
	
	public void Latitude_SetValue(double Latitude) throws Exception {
		PIO.TCommand Command = new PIO.TDACCommand(PluginModule, Latitude_DAC_Index, (int)Latitude);
		ProcessCommand(Command);
	}
	
	public void Longitude_SetValue(double Longitude) throws Exception {
		PIO.TCommand Command = new PIO.TDACCommand(PluginModule, Longitude_DAC_Index, (int)Longitude);
		ProcessCommand(Command);
	}
}
