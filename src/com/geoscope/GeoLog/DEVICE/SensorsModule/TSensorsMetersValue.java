package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TSensorMeter;

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
            	TSensorMeter Meter = SensorsModule.Meters.GetItem(MeterID);
            	if (Meter != null) 
                	Meter.SetProfile(Value);
        		break; //. >

        	case 2: //. start/stop meter(s)
            	int SubVersion = Integer.parseInt(SA[2]);
            	switch (SubVersion) {
            	
            	case 1: //. start meters
            		int Cnt = SA.length;
            		for (int I = 3; I < Cnt; I++) {
                    	Meter = SensorsModule.Meters.GetItem(SA[I]);
                    	if (Meter != null) 
                        	Meter.SetActive(true);
            		}
            		break; //. >

            	case 2: //. stop meters
            		Cnt = SA.length;
            		for (int I = 3; I < Cnt; I++) {
                    	Meter = SensorsModule.Meters.GetItem(SA[I]);
                    	if (Meter != null) 
                        	Meter.SetActive(false);
            		}
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
            	TSensorMeter Meter = SensorsModule.Meters.GetItem(MeterID);
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
        		String _Value = SensorsModule.Meters.GetItemsList(SubVersion);
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
