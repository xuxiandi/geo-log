package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TSensorsChannelsValue extends TComponentTimestampedDataValue {

	protected TSensorsModule SensorsModule;
	
	public TSensorsChannelsValue() {
		super();
	}
	
	public TSensorsChannelsValue(TSensorsModule pSensorsModule) {
		this();
		//.
		SensorsModule = pSensorsModule;
	}

	@Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception
    {
		if (!SensorsModule.flEnabled)
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		//.
        super.FromByteArrayByAddressData(BA,/*ref*/ Idx, AddressData);
		//.
		if (AddressData == null)
			return; //. ->
    	String Params;
    	try {
    		Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	}
    	catch (Exception E) {
    		return; //. ->
    	}
    	String[] SA = Params.split(",");
    	int Operation = Integer.parseInt(SA[0]);
    	switch (Operation) {
    	
    	case 1:  
        	int Version = Integer.parseInt(SA[1]);
        	switch (Version) {
        	
        	case 1: //. set the channel profile 
            	int ChannelID = Integer.parseInt(SA[2]);
        		//.
            	TChannel Channel = SensorsModule.Model.StreamChannels_GetOneByID(ChannelID);
            	if (Channel != null)
            		Channel.Profile_FromByteArray(Value);
        		break; //. >
        		
        	case 2: //. set channels profiles 
        		break; //. >
        	}
            break; //. >
            
        default:
            break; //. >
    	}
    }
    
	@Override
    public synchronized byte[] ToByteArrayByAddressData(byte[] AddressData) throws Exception {
		if (!SensorsModule.flEnabled)
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		//.
		if (AddressData == null)
			return null; //. ->
    	String Params;
    	try {
    		Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	}
    	catch (Exception E) {
    		return null; //. ->
    	}
    	String[] SA = Params.split(",");
    	int Operation = Integer.parseInt(SA[0]);
    	//.
    	switch (Operation) {
    	
    	case 1: //. 
        	int Version = Integer.parseInt(SA[1]);
        	switch (Version) {
        	
        	case 1: //. get the channel profile
            	int ChannelID = Integer.parseInt(SA[2]);
        		//.
            	TChannel Channel = SensorsModule.Model.StreamChannels_GetOneByID(ChannelID);
        		Timestamp = OleDate.UTCCurrentTimestamp();
            	if (Channel != null)
            		Value = Channel.Profile_ToByteArray();
            	else
            		Value = null;
        		//.
                return ToByteArray(); //. ->
        		
        	case 2: //. get channels profiles
        		Timestamp = OleDate.UTCCurrentTimestamp();
        		Value = null;
        		//.
                return ToByteArray(); //. ->
        		
        	default:
        		Timestamp = OleDate.UTCCurrentTimestamp();
        		Value = null;
                return ToByteArray(); //. ->
        	}    	

        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
    	}
	}
}
