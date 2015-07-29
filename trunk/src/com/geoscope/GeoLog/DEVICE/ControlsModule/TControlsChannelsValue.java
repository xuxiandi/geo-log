package com.geoscope.GeoLog.DEVICE.ControlsModule;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetControlsModuleChannelsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TStreamChannel;

public class TControlsChannelsValue extends TComponentTimestampedDataValue {

	protected TControlsModule ControlsModule;
	
	public TControlsChannelsValue() {
		super();
	}
	
	public TControlsChannelsValue(TControlsModule pControlsModule) {
		this();
		//.
		ControlsModule = pControlsModule;
	}

	@Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception
    {
		if (!ControlsModule.flEnabled)
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
            	TStreamChannel Channel = (TStreamChannel)ControlsModule.Model.StreamChannels_GetOneByID(ChannelID);
            	if (Channel != null) {
            		try {
                		Channel.Profile_FromByteArray(Value);
            		}
            		catch (TChannel.ChannelIsActiveException CAE) {
            			throw new OperationException(TSetControlsModuleChannelsValueSO.OperationErrorCode_ChannelIsActive); //. =>
            		}
            		//.
            		ControlsModule.Model_BuildAndPublish();
            		//.
            		//. ControlsModule.Device.DataStreamerModule.ValidateStreaming();
            	}
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
		if (!ControlsModule.flEnabled)
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
            	TStreamChannel Channel = (TStreamChannel)ControlsModule.Model.StreamChannels_GetOneByID(ChannelID);
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
        		
        	case 3: //. get channels status
            	int SubVersion = Integer.parseInt(SA[2]);
        		switch (SubVersion) {

        		case 1: //. get channel "enabled" status
            		int Cnt = SA.length-3;
            		byte[] EnabledArray = new byte[Cnt];
            		for (int I = 0; I < Cnt; I++) {
            			ChannelID = Integer.parseInt(SA[I+3]);
            			Channel = (TStreamChannel)ControlsModule.Model.StreamChannels_GetOneByID(ChannelID);
            			if (Channel != null) {
            				TChannel SourceChannel = Channel.DestinationChannel;
            				if (SourceChannel != null)
            					EnabledArray[I] = (Channel.Enabled ? (byte)1 : (byte)0);
                			else
                				EnabledArray[I] = 0;
            			}
            			else
            				EnabledArray[I] = 0;
            		}
            		//.
            		Timestamp = OleDate.UTCCurrentTimestamp();
            		Value = EnabledArray;
            		//.
            		break; //. >

        		case 2: //. get channel "active" status
            		Cnt = SA.length-3;
            		byte[] ActiveArray = new byte[Cnt];
            		for (int I = 0; I < Cnt; I++) {
            			ChannelID = Integer.parseInt(SA[I+3]);
            			Channel = (TStreamChannel)ControlsModule.Model.StreamChannels_GetOneByID(ChannelID);
            			if (Channel != null) {
            				TChannel SourceChannel = Channel.DestinationChannel;
            				if (SourceChannel != null)
            					ActiveArray[I] = (SourceChannel.IsActive() ? (byte)1 : (byte)0);
                			else
                				ActiveArray[I] = 0;
            			}
            			else
            				ActiveArray[I] = 0;
            		}
            		//.
            		Timestamp = OleDate.UTCCurrentTimestamp();
            		Value = ActiveArray;
            		//.
            		break; //. >

        		case 3: //. get channel "enabled" and "active" status
            		Cnt = SA.length-3;
            		byte[] StatusArray = new byte[Cnt << 1];
            		for (int I = 0; I < Cnt; I++) {
            			ChannelID = Integer.parseInt(SA[I+3]);
            			Channel = (TStreamChannel)ControlsModule.Model.StreamChannels_GetOneByID(ChannelID);
            			if (Channel != null) {
            				TChannel SourceChannel = Channel.DestinationChannel;
            				if (SourceChannel != null) {
                				StatusArray[(I << 1)] = (Channel.Enabled ? (byte)1 : (byte)0);
            					StatusArray[(I << 1)+1] = (SourceChannel.IsActive() ? (byte)1 : (byte)0);
            				}
                			else {
                				StatusArray[(I << 1)] = 0;
                				StatusArray[(I << 1)+1] = 0;
                			}
            			}
            			else {
            				StatusArray[(I << 1)] = 0;
            				StatusArray[(I << 1)+1] = 0;
            			}
            		}
            		//.
            		Timestamp = OleDate.UTCCurrentTimestamp();
            		Value = StatusArray;
            		//.
            		break; //. >
        		}
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
