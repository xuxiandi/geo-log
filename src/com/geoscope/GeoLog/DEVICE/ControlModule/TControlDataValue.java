package com.geoscope.GeoLog.DEVICE.ControlModule;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
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
            
    	case 101: //. start LAN connection
    		String Address = SA[1];
        	int Port = Integer.parseInt(SA[2]);
    		String ServerAddress = SA[3];
        	int ServerPort = Integer.parseInt(SA[4]);
        	int ConnectionID = Integer.parseInt(SA[5]);
        	int ConnectionTimeout = Integer.parseInt(SA[6]);
        	//.
        	TConnectionRepeater CR = ControlModule.Device.LANModule.ConnectionRepeaters_Add(Address,Port, ServerAddress,ServerPort,ConnectionID);
        	if (CR == null)
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable); //. =>
        	if (!CR.WaitForDestinationConnectionResult(ConnectionTimeout))
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_TimeoutIsExpired); //. =>
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = Integer.toString(ConnectionID).getBytes("windows-1251");
            return ToByteArray(); //. ->
            
    	case 102: //. stop LAN connection
        	ConnectionID = Integer.parseInt(SA[1]);
        	ControlModule.Device.LANModule.ConnectionRepeaters_Cancel(ConnectionID);
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 103: //. get LAN connection's status
    		//. to-do
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
    	}
    }
}
