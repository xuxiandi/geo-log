package com.geoscope.GeoLog.DEVICE.AlarmModule;

import java.io.IOException;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TAlarmProfileDataValue extends TComponentTimestampedDataValue {

	private TAlarmModule AlarmModule;
	
	public TAlarmProfileDataValue(TAlarmModule pAlarmModule) {
		AlarmModule = pAlarmModule;
	}
	
	@Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
    	super.FromByteArray(BA, Idx);
    	//.
    	try {
			AlarmModule.Profile.FromByteArray(Value);
			//. validate profile
			AlarmModule.Alarmers_Build();
    	}
    	catch (Exception E) {
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_SetValueError,E.getMessage()); //. =>
    	}
    }
	
	@Override
    public synchronized byte[] ToByteArray() throws IOException, OperationException {
		Timestamp = OleDate.UTCCurrentTimestamp();
		Value = AlarmModule.Profile.SourceByteArray;
		//.
		return super.ToByteArray(); //. ->
    }
}
