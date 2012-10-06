package com.geoscope.GeoLog.DEVICE.ControlModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.Utils.OleDate;

public class TControlDataValue extends TComponentTimestampedDataValue {

	private TControlModule ControlModule;
	
	public TControlDataValue(TControlModule pControlModule) {
		ControlModule = pControlModule;
	}

	@Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception
    {
		if (!ControlModule.flEnabled)
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		//.
		FromByteArray(BA, Idx);
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
    	//.
    	switch (Operation) {
    	case 0: //. restart device process 
    		ControlModule.RestartDeviceProcessAfterDelay(1000*5/*seconds*/);
            break; //. >

    	case 1: //. restart device  
    		ControlModule.RestartDeviceAfterDelay(1000*5/*seconds*/);
            break; //. >

    	case 2: //.
            break; //. >
            
        default:
            break; //. >
    	}
        //.
        super.FromByteArrayByAddressData(BA,/*ref*/ Idx, AddressData);
    }
    
	@Override
    public synchronized byte[] ToByteArrayByAddressData(byte[] AddressData) throws Exception
    {
		if (!ControlModule.flEnabled)
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
    	case 1: //. get device configuration data 
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->

    	case 2: //. get device state data
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = ControlModule.Device.GetStateInfo().getBytes("windows-1251");
            return ToByteArray(); //. ->

    	case 3: //. get device log 
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = ControlModule.Device.Log.ToZippedByteArray();
            return ToByteArray(); //. ->
            
        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
    	}
    }
}
