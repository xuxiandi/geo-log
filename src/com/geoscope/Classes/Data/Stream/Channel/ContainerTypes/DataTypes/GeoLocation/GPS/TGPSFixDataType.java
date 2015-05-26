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
	
	public static class TValue {
		
		public short DatumID = 23; //. WGS-84
		//.
		public double Timestamp;
		//.
		public double Latitude;
		public double Longitude;
		public double Altitude;
		//.
		public double Speed;
		public double Bearing;
		public double Precision;
		
		public TValue(short pDatumID, double pTimestamp, double pLatitude, double pLongitude, double pAltitude, double pSpeed, double pBearing, double pPrecision) {
			DatumID = pDatumID;
			//.
			Timestamp = pTimestamp;
			//.
			Latitude = pLatitude;
			Longitude = pLongitude;
			Altitude = pAltitude;
			//.
			Speed = pSpeed;
			Bearing = pBearing;
			Precision = pPrecision;
		}

		public TValue(double pTimestamp, double pLatitude, double pLongitude, double pAltitude, double pSpeed, double pBearing, double pPrecision) {
			this((short)23/*WGS-84*/, pTimestamp, pLatitude,pLongitude,pAltitude, pSpeed,pBearing,pPrecision);
		}

	    public boolean IsAvailable() {
	        return ((Precision != UnknownFixPrecision) && (Precision != UnavailableFixPrecision));
	    }
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
	
	public TValue Value() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestamped6DoubleContainerType) {
			TTimestamped6DoubleContainerType.TValue CTV = ((TTimestamped6DoubleContainerType)ContainerType).Value; 
			return (new TValue(CTV.Timestamp, CTV.Value,CTV.Value1,CTV.Value2, CTV.Value3,CTV.Value4,CTV.Value5)); //. ->
		}
		if (ContainerType instanceof TTimestampedInt166DoubleContainerType) {
			TTimestampedInt166DoubleContainerType.TValue CTV = ((TTimestampedInt166DoubleContainerType)ContainerType).Value; 
			return (new TValue(CTV.Value, CTV.Timestamp, CTV.Value1,CTV.Value2,CTV.Value3, CTV.Value4,CTV.Value5,CTV.Value6)); //. ->
		}
		else
			throw new WrongContainerTypeException(); //. =>
	}
	
	public double Timestamp() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestamped6DoubleContainerType) 
			return ((TTimestamped6DoubleContainerType)ContainerType).Value.Timestamp; //. ->
		if (ContainerType instanceof TTimestampedInt166DoubleContainerType)
			return ((TTimestampedInt166DoubleContainerType)ContainerType).Value.Timestamp; //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}
}
