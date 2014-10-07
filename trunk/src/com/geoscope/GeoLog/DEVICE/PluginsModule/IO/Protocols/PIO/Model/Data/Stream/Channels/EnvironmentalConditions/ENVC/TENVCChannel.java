package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC;

import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Types.EnvironmentalConditions.TEnvironmentalConditions;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TENVCChannel extends TStreamChannel {

	public static final String TypeID = "EnvironmentalConditions.ENVC";
	
	
	public int Timestamp_ADC_Index = -1;
	public int Temperature_ADC_Index = -1;
	public int Pressure_ADC_Index = -1;
	public int Humidity_ADC_Index = -1;
	//.
	public TEnvironmentalConditions Value = new TEnvironmentalConditions();
	
	public TENVCChannel(TDEVICEModule pDevice) {
		super(pDevice);
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
				Timestamp_ADC_Index = Integer.parseInt(CP.CoderConfiguration[0]);
				if (CP.CoderConfiguration.length > 1) {
					Temperature_ADC_Index = Integer.parseInt(CP.CoderConfiguration[1]);
					if (CP.CoderConfiguration.length > 2) {
						Pressure_ADC_Index = Integer.parseInt(CP.CoderConfiguration[2]);
						if (CP.CoderConfiguration.length > 3) {
							Humidity_ADC_Index = Integer.parseInt(CP.CoderConfiguration[3]);
						}
					}
				}
			}
		}
	}

	@Override
	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		boolean Result = false;
		if (Command instanceof PIO.TADCCommand) {
			PIO.TADCCommand ADCCommand = (PIO.TADCCommand)Command;
			com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel DC = GetDestinationChannel();			
			//.
			if (ADCCommand.Address == Timestamp_ADC_Index) {
				Value.Timestamp = ADCCommand.Value;
				DC.DoOnTimestamp(Value.Timestamp);
				Result = true;
			}
			else
				if (ADCCommand.Address == Temperature_ADC_Index) {
					Value.Temperature = ADCCommand.Value; 
					DC.DoOnTemperature(Value.Temperature);
					Result = true;
				}
				else
					if (ADCCommand.Address == Pressure_ADC_Index) {
						Value.Pressure = ADCCommand.Value;
						DC.DoOnPressure(Value.Pressure);
						Result = true;
					}
					else
						if (ADCCommand.Address == Humidity_ADC_Index) {
							Value.Humidity = ADCCommand.Value;
							DC.DoOnHumidity(Value.Humidity);
							Result = true;
						}
		}
		return Result;
	}

	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel DestinationChannel = null;
	
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel GetDestinationChannel() {
		if (DestinationChannel == null) {
			if (Device.SensorsModule.Model.Stream != null)
				DestinationChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel)Device.SensorsModule.Model.Stream.Channels_GetOneByClass(com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel.class);
		}
		return DestinationChannel;
	}
}
