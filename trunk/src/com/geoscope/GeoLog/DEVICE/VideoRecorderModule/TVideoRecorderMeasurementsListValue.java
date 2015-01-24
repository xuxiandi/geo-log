package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import java.io.IOException;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedANSIStringValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;

public class TVideoRecorderMeasurementsListValue extends TComponentTimestampedANSIStringValue {

	private TVideoRecorderModule VideoRecorderModule;
	
	public TVideoRecorderMeasurementsListValue(TVideoRecorderModule pVideoRecorderModule) {
		VideoRecorderModule = pVideoRecorderModule;
	}

	@Override
	public synchronized byte[] ToByteArray() throws IOException, OperationException  {
		if (!VideoRecorderModule.flEnabled)
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		//.
		Timestamp = OleDate.UTCCurrentTimestamp();
    	Value = VideoRecorderModule.Measurements_GetList();
    	//.
        return super.ToByteArray(); //. ->
	}
	
	@Override
    public synchronized byte[] ToByteArrayByAddressData(byte[] AddressData) throws Exception {
		if (!VideoRecorderModule.flEnabled)
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
        	double BeginTimestamp = Double.parseDouble(SA[1]); 
        	double EndTimestamp = Double.parseDouble(SA[2]);
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
        	Value = VideoRecorderModule.Measurements_GetList(BeginTimestamp,EndTimestamp);
        	//.
            return super.ToByteArray(); //. ->

        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
    	}
    }
}
