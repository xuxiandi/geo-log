package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.Battery;

import android.content.Context;
import android.os.BatteryManager;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TInt16ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TInt32ContainerType;
import com.geoscope.GeoEye.R;

public class TBatteryPlugTypeDataType extends TDataType {

	public static String ID() {
		return "BatteryPlugType";
	}

	
	public TBatteryPlugTypeDataType() {
		super();
	}
	
	public TBatteryPlugTypeDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TBatteryPlugTypeDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
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
	    case BatteryManager.BATTERY_PLUGGED_AC:
	        Result = context.getString(R.string.SAC);
	        break;
	    case BatteryManager.BATTERY_PLUGGED_USB:
	        Result = context.getString(R.string.SUSB);
	        break;
	    }					
		return Result;
	}
}
