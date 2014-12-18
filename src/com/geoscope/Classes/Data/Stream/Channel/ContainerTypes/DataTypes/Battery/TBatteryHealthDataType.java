package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.Battery;

import android.content.Context;
import android.os.BatteryManager;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TInt16ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TInt32ContainerType;
import com.geoscope.GeoEye.R;

public class TBatteryHealthDataType extends TDataType {

	public static String ID() {
		return "BatteryHealth";
	}

	
	public TBatteryHealthDataType() {
		super();
	}
	
	public TBatteryHealthDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TBatteryHealthDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public int ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TInt16ContainerType)
			return ((TInt16ContainerType)ContainerType).Value; //. ->
		if (ContainerType instanceof TInt32ContainerType)
			return ((TInt32ContainerType)ContainerType).Value; //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}

	@Override
	public String GetValueString(Context context) throws WrongContainerTypeException {
		String Result = "?"; 
		switch (ContainerValue()) {
		
	    case BatteryManager.BATTERY_HEALTH_DEAD:
	        Result = context.getString(R.string.SBad);
	        break; //. >
	        
	    case BatteryManager.BATTERY_HEALTH_GOOD:
	        Result = context.getString(R.string.SGoodCondition);
	        break; //. >
	        
	    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
	        Result = context.getString(R.string.SOverVoltage);
	        break; //. >
	        
	    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
	        Result = context.getString(R.string.SOverHeat);
	        break; //. >
	        
	    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
	        Result = context.getString(R.string.SFailure);
	        break; //. >
	    }	
		return Result;
	}
}
