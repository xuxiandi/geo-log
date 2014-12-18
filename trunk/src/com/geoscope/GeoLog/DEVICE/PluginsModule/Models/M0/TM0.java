package com.geoscope.GeoLog.DEVICE.PluginsModule.Models.M0;

import com.geoscope.GeoLog.DEVICE.ADCModule.TADCModule;
import com.geoscope.GeoLog.DEVICE.GPIModule.TGPIModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginsModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TADCCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TGPICommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TMODELCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TBADCCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.Models.TPluginModel;

public class TM0 extends TPluginModel {

	public TM0(TPluginsModule pPluginsModule) {
		super(pPluginsModule,null);
	}

	public PIO.TADCCommand GetADCCommand_Process(int pItemAddress, TPluginModule PluginModule) throws Exception {
        PIO.TADCCommand Result = new PIO.TADCCommand(PluginModule, pItemAddress);
    	PluginModule.OutgoingCommands_ProcessCommand(Result);
    	return Result;
	}
	
	public PIO.TDACCommand SetDACCommand_Process(int pAddress, double pValue, TPluginModule PluginModule) throws Exception {
        PIO.TDACCommand Result = new PIO.TDACCommand(PluginModule, pAddress,(int)pValue);
        PluginModule.OutgoingCommands_ProcessCommand(Result);
    	return Result;
	}
	
	public void DoOnCommandResponse(TCommand Command) throws Exception {
		if (Command instanceof TMODELCommand) {
			TGPICommand GPICommand = (TGPICommand)Command;
			//.
			TGPIModule GPIModule = PluginsModule.Device.GPIModule;
			int V = GPIModule.GetIntValue();
			int BV = (1 << GPICommand.Address);
			if (GPICommand.Value > 0) 
				V = (V | BV);
			else {
				BV = ~BV;
				V = (V & BV);
			}
			PluginsModule.Device.GPIModule.SetValue((short)V);
		}
		else
			if (Command instanceof TADCCommand) {
				TADCCommand ADCCommand = (TADCCommand)Command;
				TADCModule ADCModule = PluginsModule.Device.ADCModule;
				ADCModule.SetValueItem(ADCCommand.Address,ADCCommand.Value);
				//.
				if (flDebug)
					PluginsModule.Device.Log.WriteInfo("PluginsMoules.Models.M0","command ADC: "+"ADCModule.SetValueItem() is DONE, Address: "+Integer.toString(ADCCommand.Address)+", Value: "+Double.toString(ADCCommand.Value));
			}
			else
				if (Command instanceof TBADCCommand) {
					TBADCCommand BADCCommand = (TBADCCommand)Command;
					if (BADCCommand.Items != null) {
						TADCModule ADCModule = PluginsModule.Device.ADCModule;
						int Cnt = BADCCommand.Items.length; 
						for (int I = 0; I < Cnt; I++) {
							TBADCCommand.TItem Item = BADCCommand.Items[I]; 
							ADCModule.SetValueItem(Item.Address,Item.Value);
							//.
							if (flDebug)
								PluginsModule.Device.Log.WriteInfo("PluginsMoules.Models.M0","command BADC: "+"ADCModule.SetValueItem() is DONE, Address: "+Integer.toString(Item.Address)+", Value: "+Double.toString(Item.Value));
						}
					}
				}
	}
}
