package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;

public class TSensorsMetersValue extends TComponentTimestampedDataValue {

	protected TSensorsModule SensorsModule;
	
	public TSensorsMetersValue() {
		super();
	}
	
	public TSensorsMetersValue(TSensorsModule pSensorsModule) {
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
    	
    	case 1: //. set meter 
        	int Version = Integer.parseInt(SA[1]);
        	switch (Version) {
        	
        	case 1: //. set profile
            	String MeterID = SA[2];
            	//.
            	TSensorMeter Meter = SensorsModule.Meters.Items_GetItem(MeterID);
            	if (Meter != null) 
                	Meter.SetProfile(Value, true);
        		break; //. >

        	case 2: //. start/stop meter(s)
            	int SubVersion = Integer.parseInt(SA[2]);
            	switch (SubVersion) {
            	
            	case 1: //. set meters activity = true
            		int Cnt = SA.length-3;
            		if ((Value == null) || (Value.length == 0)) {
                		for (int I = 0; I < Cnt; I++) {
                        	Meter = SensorsModule.Meters.Items_GetItem(SA[I+3]);
                        	if (Meter != null) 
                            	Meter.SetActive(true);
                		}
            		}
            		else {
            			//. set meter profiles
            			int Index = 0;
                		for (int I = 0; I < Cnt; I++) {
                        	Meter = SensorsModule.Meters.Items_GetItem(SA[I+3]);
                        	//.
                			int ProfileSize = TDataConverter.ConvertLEByteArrayToInt32(Value, Index); Index += 4; //. SizeOf(Int32)
                			if (ProfileSize > 0) {
                    			byte[] Profile = new byte[ProfileSize];
                    			System.arraycopy(Value,Index, Profile,0, ProfileSize); Index += ProfileSize;
                    			//.
                            	if (Meter != null) 
                                	Meter.SetProfile(Profile, false);
                			}
                		}
                		//. activate meters
                		for (int I = 0; I < Cnt; I++) {
                        	Meter = SensorsModule.Meters.Items_GetItem(SA[I+3]);
                        	if (Meter != null) 
                            	Meter.SetActive(true);
                		}
            		}
            		break; //. >

            	case 2: //. set meters activity = false
            		Cnt = SA.length-3;
            		for (int I = 0; I < Cnt; I++) {
                    	Meter = SensorsModule.Meters.Items_GetItem(SA[I+3]);
                    	if (Meter != null) 
                        	Meter.SetActive(false);
            		}
            		break; //. >

            	case 3: //. validate meters activity
            		Cnt = SA.length-3;
            		if (Cnt > 0) {
                		String[] MeterIDs = new String[Cnt];
                		for (int I = 0; I < Cnt; I++) 
                			MeterIDs[I] = SA[I+3];
                		SensorsModule.Meters.Items_ValidateActivity(MeterIDs);
            		}
            		else 
            			SensorsModule.Meters.Items_ValidateActivity(new String[0]);
            		break; //. >
            	}            	
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
    	
    	case 1: //. get meter
        	int Version = Integer.parseInt(SA[1]);
        	switch (Version) {
        	
        	case 1: //. get profile
            	String MeterID = SA[2];
            	//.
            	TSensorMeter Meter = SensorsModule.Meters.Items_GetItem(MeterID);
        		Timestamp = OleDate.UTCCurrentTimestamp();
        		if (Meter != null)
        			Value = Meter.GetProfile();
        		else
        			Value = null;
        		//.
                return ToByteArray(); //. ->

        	case 2: //. get meters list
            	int SubVersion = Integer.parseInt(SA[2]);
            	//.
        		Timestamp = OleDate.UTCCurrentTimestamp();
        		String _Value = SensorsModule.Meters.Items_GetList(SubVersion);
        		if (_Value != null)
        			Value = _Value.getBytes("windows-1251");
        		else
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
