package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC;

import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TBADCCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Types.EnvironmentalConditions.TEnvironmentalConditions;

public class TENVCChannel extends TStreamChannel {

	public static final String TypeID = "EnvironmentalConditions.ENVC";
	
	
	public int Timestamp_ADC_Index = -1;
	public int Temperature_ADC_Index = -1;
	public int Pressure_ADC_Index = -1;
	public int Humidity_ADC_Index = -1;
	//.
	public TEnvironmentalConditions Value = new TEnvironmentalConditions();
	
	public TENVCChannel(TPluginModule pPluginModule) {
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
			if (CP.CoderConfiguration.length > 1) {
				Timestamp_ADC_Index = Integer.parseInt(CP.CoderConfiguration[1]);
				if (CP.CoderConfiguration.length > 2) {
					Temperature_ADC_Index = Integer.parseInt(CP.CoderConfiguration[2]);
					if (CP.CoderConfiguration.length > 3) {
						Pressure_ADC_Index = Integer.parseInt(CP.CoderConfiguration[3]);
						if (CP.CoderConfiguration.length > 4) {
							Humidity_ADC_Index = Integer.parseInt(CP.CoderConfiguration[4]);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		boolean Result = false;
		com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel DC = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel)DestinationChannel;			
		if (Command instanceof PIO.TADCCommand) {
			PIO.TADCCommand ADCCommand = (PIO.TADCCommand)Command;
			//.
			if (ADCCommand.Address == Timestamp_ADC_Index) {
				Value.Timestamp = ADCCommand.Value;
				if (DC != null)
					DC.DoOnTimestamp(Value.Timestamp);
				Result = true;
			}
			else
				if (ADCCommand.Address == Temperature_ADC_Index) {
					Value.Temperature = ADCCommand.Value; 
					if (DC != null)
						DC.DoOnTemperature(Value.Temperature);
					Result = true;
				}
				else
					if (ADCCommand.Address == Pressure_ADC_Index) {
						Value.Pressure = ADCCommand.Value;
						if (DC != null)
							DC.DoOnPressure(Value.Pressure);
						Result = true;
					}
					else
						if (ADCCommand.Address == Humidity_ADC_Index) {
							Value.Humidity = ADCCommand.Value;
							if (DC != null)
								DC.DoOnHumidity(Value.Humidity);
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
						if (Item.Address == Timestamp_ADC_Index) {
							Value.Timestamp = Item.Value;
							if (DC != null)
								DC.DoOnTimestamp(Value.Timestamp);
							Result = true;
						}
						else
							if (Item.Address == Temperature_ADC_Index) {
								Value.Temperature = Item.Value; 
								if (DC != null)
									DC.DoOnTemperature(Value.Temperature);
								Result = true;
							}
							else
								if (Item.Address == Pressure_ADC_Index) {
									Value.Pressure = Item.Value;
									if (DC != null)
										DC.DoOnPressure(Value.Pressure);
									Result = true;
								}
								else
									if (Item.Address == Humidity_ADC_Index) {
										Value.Humidity = Item.Value;
										if (DC != null)
											DC.DoOnHumidity(Value.Humidity);
										Result = true;
									}
					}
				}
			}
		return Result;
	}
}
