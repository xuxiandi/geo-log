package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;
import com.geoscope.GeoEye.R;

public class TGPSModeDataType extends TDataType {

	public static String ID() {
		return "GPSMode";
	}

    public static final short MODE_DISABLED	= 0;
    public static final short MODE_ENABLED	= 1;
    
	
	public TGPSModeDataType() {
		super();
	}
	
	public TGPSModeDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TGPSModeDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public int ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestampedInt16ContainerType)
			return ((TTimestampedInt16ContainerType)ContainerType).Value.Value; //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}

	@Override
	public String GetValueString(Context context) throws WrongContainerTypeException {
		String Result = "?";
	    switch (ContainerValue()) {
	    
	    case MODE_DISABLED:
	        Result = context.getString(R.string.SDisabled);
	        break; //. >
	        
	    case MODE_ENABLED:
	        Result = context.getString(R.string.SEnabled);
	        break; //. >
	    }
		return Result;
	}
}
