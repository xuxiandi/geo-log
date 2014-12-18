package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.Telemetry.TLR;

import java.util.HashMap;

import android.annotation.SuppressLint;

import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TBADCCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;

public class TTLRChannel extends TStreamChannel {

	public static final String TypeID = "Telemetry.TLR";
	
	
	public HashMap<Integer, Integer> DataIndexes = null;
	
	public TTLRChannel(TPluginModule pPluginModule) {
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
					DataIndexes.put(Index, (I-1));
				}
			}
		}
	}

	@Override
	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		boolean Result = false;
		com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel DC = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel)DestinationChannel;			
		if (Command instanceof PIO.TADCCommand) {
			PIO.TADCCommand ADCCommand = (PIO.TADCCommand)Command;
			//.
			Integer I;
			if (DataIndexes != null)
				I = DataIndexes.get(ADCCommand.Address);
			else
				I = ADCCommand.Address;
			//.
			if ((I != null) && (DataTypes != null) && (I < DataTypes.Items.size())) {
				TDataType DataType = DataTypes.Items.get(I);
				//.
				Double Value = Double.valueOf(ADCCommand.Value);
				DataType.SetContainerTypeValue(Value);
				//.
				if (DC != null) 
					DC.DoOnData(DataType);
				Result = true;
			}
		}
		else
			if (Command instanceof PIO.TBADCCommand) {
				PIO.TBADCCommand BADCCommand = (PIO.TBADCCommand)Command;
				//.
				if (BADCCommand.Items != null) {
					int Cnt = BADCCommand.Items.length; 
					for (int I = 0; I < Cnt; I++) {
						TBADCCommand.TItem Item = BADCCommand.Items[I];
						//.
						Integer J;
						if (DataIndexes != null)
							J = DataIndexes.get(Item.Address);
						else
							J = Item.Address;
						//.
						if ((J != null) && (DataTypes != null) && (J < DataTypes.Items.size())) {
							TDataType DataType = DataTypes.Items.get(J);
							//.
							Double Value = Double.valueOf(Item.Value);
							DataType.SetContainerTypeValue(Value);
							//.
							if (DC != null) 
								DC.DoOnData(DataType);
							Result |= true;
						}
					}
				}
			}
		return Result;
	}
}
