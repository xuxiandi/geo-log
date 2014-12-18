package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS;

import android.content.Context;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;
import com.geoscope.GeoEye.R;

public class TGPSStatusDataType extends TDataType {

	public static String ID() {
		return "GPSStatus";
	}

    public static final short STATUS_PERMANENTLYUNAVAILABLE	= -2;
    public static final short STATUS_TEMPORARILYUNAVAILABLE	= -1;
    public static final short STATUS_UNKNOWN              	= 0;
    public static final short STATUS_AVAILABLE       		= 1;

    
	public TGPSStatusDataType() {
		super();
	}
	
	public TGPSStatusDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TGPSStatusDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
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
	    
	    case STATUS_PERMANENTLYUNAVAILABLE:
	        Result = context.getString(R.string.SPermanentlyUnavailable);
	        break; //. >
	        
	    case STATUS_TEMPORARILYUNAVAILABLE:
	        Result = context.getString(R.string.STemporarilyUnavailable);
	        break; //. >
	        
	    case STATUS_UNKNOWN:
	        Result = "?";
	        break; //. >
	        
	    case STATUS_AVAILABLE:
	        Result = context.getString(R.string.SAvailable);
	        break; //. >
	    }
		return Result;
	}
}
