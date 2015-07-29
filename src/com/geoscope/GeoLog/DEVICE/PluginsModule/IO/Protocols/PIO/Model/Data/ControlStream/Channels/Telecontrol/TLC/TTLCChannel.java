package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.Telecontrol.TLC;

import java.util.HashMap;

import android.annotation.SuppressLint;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TControlStreamChannel;

public class TTLCChannel extends TControlStreamChannel {

	public static final String TypeID = "Telecontrol.TLC";


	public HashMap<Integer, Integer> DataIndexes = null;
	
	public TTLCChannel(TPluginModule pPluginModule) {
		super(pPluginModule);
	}	

	@Override
	public String GetTypeID() {
		return TypeID;
	}

	@SuppressLint("UseSparseArrays")
	@Override
	public void Parse() throws Exception {
		DataIndexes = null;
		//.
		TConfigurationParser CP = new TConfigurationParser(Configuration);
		if (CP.CoderConfiguration != null) { 
			if (CP.CoderConfiguration.length > 1) {
				DataIndexes = new HashMap<Integer, Integer>();
				for (int I = 1; I < CP.CoderConfiguration.length; I++) {
					int Index = Integer.parseInt(CP.CoderConfiguration[I]);
					DataIndexes.put(I, (Index-1));
				}
			}
		}
	}

	public void ProcessCommand(PIO.TCommand Command) throws Exception {
		PluginModule.OutgoingCommands_ProcessCommand(Command);
	}
	
	public void DataType_SetValue(TDataType DataType) throws Exception {
		Integer I;
		if (DataIndexes != null)
			I = DataIndexes.get(DataType.Index);
		else
			I = DataType.Index;
		if (I == null)
			return; //. ->
		int V = ((Double)DataType.ContainerType.GetValue()).intValue();
		PIO.TCommand Command = new PIO.TDACCommand(PluginModule, I, V);
		ProcessCommand(Command);
	}
}
