package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.GeoLocation.GPS;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt166DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt16ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS.TGPSFixDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS.TGPSModeDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS.TGPSStatusDataType;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TGPSChannel extends TTLRChannel {

	public static final String TypeID = "GeoLocation.GPS";
	
	public static class TMyProfile extends TTLRChannel.TMyProfile {
		
	}
	

	public TDataType	GPSMode;
	public TDataType	GPSStatus;
	public TDataType	GPSFix;
	
	public TGPSChannel(TInternalSensorsModule pInternalSensorsModule) throws Exception {
		super(pInternalSensorsModule, TMyProfile.class);
		//.
		ID = 5;
		//.
		Enabled = true;
		Kind = TChannel.CHANNEL_KIND_OUT;
		DataFormat = 0;
		Name = "Geo-location";
		Info = "GPS data";
		Size = 0;
		Configuration = "";
		Parameters = "";
		//.
		DataTypes = new TDataTypes();
		GPSMode 	= DataTypes.AddItem(new TGPSModeDataType(new TTimestampedInt16ContainerType(), 			this, 	1, "","", ""));		
		GPSStatus 	= DataTypes.AddItem(new TGPSStatusDataType(new TTimestampedInt16ContainerType(), 		this, 	2, "","", ""));		
		GPSFix 		= DataTypes.AddItem(new TGPSFixDataType(new TTimestampedInt166DoubleContainerType(), 	this, 	3, "","", ""));		
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public void StartSource() {
		InternalSensorsModule.Device.GPSModule.DestinationChannel_Set(this);
	}

	@Override
	public void StopSource() {
		InternalSensorsModule.Device.GPSModule.DestinationChannel_Clear();
	}
}
