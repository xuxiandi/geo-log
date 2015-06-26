package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetVideoRecorderMeasurementDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderMeasurementDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.MeasurementsTransferProcess.TSensorsModuleMeasurementsTransferProcess;

public class TVideoRecorderMeasurementDataValue extends TComponentTimestampedDataValue {

	private TVideoRecorderModule VideoRecorderModule;
	
	public TVideoRecorderMeasurementDataValue(TVideoRecorderModule pVideoRecorderModule) {
		VideoRecorderModule = pVideoRecorderModule;
	}

	@Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception
    {
		if (!VideoRecorderModule.flEnabled)
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
    	String MeasurementID = SA[1]; 
    	//.
    	switch (Operation) {
    	case 1: //. delete measurement(s)
    		String _CurrentMeasurementID = null;
    		TMeasurementDescriptor CurrentMeasurement = TVideoRecorder.VideoRecorder_GetMeasurementDescriptor();
    		if (CurrentMeasurement != null)
    			_CurrentMeasurementID = CurrentMeasurement.ID;
    		//.
    		if (!MeasurementID.equals(_CurrentMeasurementID))
    			VideoRecorderModule.Measurements_Delete(MeasurementID);
    		else
        		if (SA.length == 2)
        			throw new OperationException(TSetVideoRecorderMeasurementDataValueSO.OperationErrorCode_DataIsLocked); //. =>
    		//.
    		for (int I = 2; I < SA.length; I++)
        		if (!SA[I].equals(_CurrentMeasurementID))
        			VideoRecorderModule.Measurements_Delete(SA[I]);
            break; //. >
            
    	case 2: //. move measurement(s) to data server
    		_CurrentMeasurementID = null;
    		CurrentMeasurement = TVideoRecorder.VideoRecorder_GetMeasurementDescriptor();
    		if (CurrentMeasurement != null)
    			_CurrentMeasurementID = CurrentMeasurement.ID;
    		//.
    		if (MeasurementID.equals(_CurrentMeasurementID)) {
    			VideoRecorderModule.PostRestartRecorder(); //. post restart message to the current measurement
    			do {
    				Thread.sleep(5000); //. wait for restarting
    				//.
    	    		CurrentMeasurement = TVideoRecorder.VideoRecorder_GetMeasurementDescriptor();
    			} while ((CurrentMeasurement != null) && (CurrentMeasurement.ID.equals(MeasurementID)));
    		}
    		String MIDs = MeasurementID;
    		for (int I = 2; I < SA.length; I++) {
    			MeasurementID = SA[I];
    			//.
        		if (MeasurementID.equals(_CurrentMeasurementID)) {
        			VideoRecorderModule.PostRestartRecorder(); //. post restart message to the current measurement
        			do {
        				Thread.sleep(5000); //. wait for restarting
        				//.
        	    		CurrentMeasurement = TVideoRecorder.VideoRecorder_GetMeasurementDescriptor();
        			} while ((CurrentMeasurement != null) && (CurrentMeasurement.ID.equals(MeasurementID)));
        		}
    			if (!MIDs.equals("")) 
    				MIDs = MIDs+","+MeasurementID;
    			else
    				MIDs = MeasurementID;
    		}
    		if (MIDs.equals(""))
    			break; //. >
    		//.
    		TSensorsModuleMeasurementsTransferProcess TransferProcess = VideoRecorderModule.Device.SensorsModule.Measurements_GetTransferProcess(); 
    		if (TransferProcess == null)
    			throw new OperationException(TSetVideoRecorderMeasurementDataValueSO.OperationErrorCode_ServerSaverIsDisabled); //. =>
    		TransferProcess.StartProcess(MIDs);
    		//.
            break; //. >
            
        default:
            break; //. >
    	}
    }
    
	@Override
    public synchronized byte[] ToByteArrayByAddressData(byte[] AddressData) throws Exception
    {
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
    	String MeasurementID = SA[1]; 
    	//.
    	switch (Operation) {
    	case 1: //. get measurement data
    		boolean flDecriptor = true;
    		boolean flAudio = true;
    		boolean flVideo = true;
    		if (!SA[2].equals("")) {
        		int Flags = Integer.parseInt(SA[2]);
        		if (Flags != 0) {
            		flDecriptor = 	((Flags & 1) == 1);
            		flAudio = 		((Flags & 2) == 2);
            		flVideo = 		((Flags & 4) == 4);
        		}
    		}
    		//.
    		if (SA.length == 3) {
    			TVideoRecorder.VideoRecorder_FlashMeasurement();
            	//.
            	int MS = VideoRecorderModule.Measurement_GetSize(MeasurementID, flDecriptor,flAudio,flVideo);
            	if (MS > TVideoRecorderMeasurements.MeasurementDataTransferableLimit)
            		throw new OperationException(TGetVideoRecorderMeasurementDataValueSO.OperationErrorCode_DataIsTooBig); //. =>
            	//.
	    		Timestamp = OleDate.UTCCurrentTimestamp();
            	Value = VideoRecorderModule.Measurement_GetData(MeasurementID, flDecriptor,flAudio,flVideo); 
    		}
    		else {
    			@SuppressWarnings("unused")
				double StartTimestamp = 0.0;
    			if (SA.length >= 4)
    				StartTimestamp = Double.parseDouble(SA[3]);
    			@SuppressWarnings("unused")
				double FinishTimestamp = 0.0;
    			if (SA.length >= 5)
    				FinishTimestamp = Double.parseDouble(SA[4]);
    			//.
    			try {
    	    		TMeasurementDescriptor CurrentMeasurement = TVideoRecorder.VideoRecorder_GetMeasurementDescriptor();
    	    		if (CurrentMeasurement != null)
    	    			CurrentMeasurement.FinishTimestamp = TVideoRecorderMeasurements.GetCurrentTime();
    	    		//.
    	    		Timestamp = OleDate.UTCCurrentTimestamp();
    				//. Value = VideoRecorderModule.Measurement_GetDataFragment(MeasurementID,CurrentMeasurement, StartTimestamp,FinishTimestamp, flDecriptor,flAudio,flVideo);
                	Value = VideoRecorderModule.Measurement_GetData(MeasurementID, flDecriptor,flAudio,flVideo); 
    			}
    			catch (TVideoRecorderMeasurements.MeasurementDataIsNotFoundException E) {
            		throw new OperationException(TGetVideoRecorderMeasurementDataValueSO.OperationErrorCode_DataIsNotFound); //. =>
    			}
    			catch (TVideoRecorderMeasurements.MeasurementDataIsTooBigException E) {
            		throw new OperationException(TGetVideoRecorderMeasurementDataValueSO.OperationErrorCode_DataIsTooBig); //. =>
    			}
    		}
            return ToByteArray(); //. ->

        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
    	}
    }
}
