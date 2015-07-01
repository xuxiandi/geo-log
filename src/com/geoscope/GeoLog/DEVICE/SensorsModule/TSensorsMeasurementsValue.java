package com.geoscope.GeoLog.DEVICE.SensorsModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedLock;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetSensorsModuleMeasurementsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsTransferProcess;

public class TSensorsMeasurementsValue extends TComponentTimestampedDataValue {

	protected TSensorsModule SensorsModule;
	
	public TSensorsMeasurementsValue() {
		super();
	}
	
	public TSensorsMeasurementsValue(TSensorsModule pSensorsModule) {
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
    	
    	case 1: //. delete measurement(s)
    		for (int I = 1; I < SA.length; I++) {
    			String MeasurementID = SA[I];
    			//.
				TNamedLock MeasurementLock = TNamedLock.TryLock(TSensorsModuleMeasurements.Domain, MeasurementID);
				if (MeasurementLock == null)
					throw new OperationException(TSetSensorsModuleMeasurementsValueSO.OperationErrorCode_DataIsLocked, "MeasurementID: "+MeasurementID); //. =>
				try {
	        		SensorsModule.Measurements_Delete(MeasurementID);
				}
				finally {
					MeasurementLock.UnLock();
				}
    		}
            break; //. >
            
    	case 2: //. move measurement(s) to data server
    		String MIDs = "";
    		for (int I = 1; I < SA.length; I++) {
    			String MeasurementID = SA[I];
    			//.
				TNamedLock MeasurementLock = TNamedLock.TryLock(TSensorsModuleMeasurements.Domain, MeasurementID);
				if (MeasurementLock == null)
					throw new OperationException(TSetSensorsModuleMeasurementsValueSO.OperationErrorCode_DataIsLocked, "MeasurementID: "+MeasurementID); //. =>
				try {
	    			if (!MIDs.equals("")) 
	    				MIDs = MIDs+","+MeasurementID;
	    			else
	    				MIDs = MeasurementID;
				}
				finally {
					MeasurementLock.UnLock();
				}
    		}
    		if (MIDs.equals(""))
    			break; //. >
    		//.
    		TSensorsModuleMeasurementsTransferProcess MeasurementsTransferProcess = SensorsModule.Measurements_GetTransferProcess(); 
    		if (MeasurementsTransferProcess == null)
    			throw new OperationException(TSetSensorsModuleMeasurementsValueSO.OperationErrorCode_ServerSaverIsNotReady); //. =>
    		//.
    		MeasurementsTransferProcess.StartProcess(MIDs);
    		//.
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
    	
    	case 1: //. get measurements list
        	short Version = Short.parseShort(SA[1]); 
        	double BeginTimestamp = Double.parseDouble(SA[2]); 
        	double EndTimestamp = Double.parseDouble(SA[3]);
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
        	String _Value = SensorsModule.Measurements_GetList(BeginTimestamp,EndTimestamp, Version);
        	Value = _Value.getBytes("US-ASCII");
        	//.
            return super.ToByteArray(); //. ->

        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
    	}
	}
}
