package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestamped6DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt166DoubleContainerType;

public class TGPSFixDataType extends TDataType {

	public static String ID() {
		return "GPSFix";
	}

    public static final double 	UnavailableFixPrecision = 1000000000.0;
    public static final double 	UnknownFixPrecision = -1000000000.0;
    //.
    public static final double 	FixUnknownPrecision = -1.0;
    public static final double 	FixDefaultPrecision = 30.0;
    
	
	public TGPSFixDataType() {
		super();
	}
	
	public TGPSFixDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TGPSFixDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public Object ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestamped6DoubleContainerType)
			return ((TTimestamped6DoubleContainerType)ContainerType).Value; //. ->
		if (ContainerType instanceof TTimestampedInt166DoubleContainerType)
			return ((TTimestampedInt166DoubleContainerType)ContainerType).Value; //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}
}
