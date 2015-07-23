package com.geoscope.GeoLog.DEVICE.DataStreamerModule;

import java.io.IOException;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetDataStreamerStreamingComponentsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TDataStreamerStreamingComponentsValue extends TComponentTimestampedDataValue {

	private TDataStreamerModule DataStreamerModule;
	
	public TDataStreamerStreamingComponentsValue(TDataStreamerModule pDataStreamerModule) {
		DataStreamerModule = pDataStreamerModule;
	}
	
	@Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
    	if (!DataStreamerModule.flEnabled)  
    		throw new OperationException(TSetDataStreamerStreamingComponentsValueSO.OperationErrorCode_DataStreamerIsDisabled); //. =>
    	//.
    	super.FromByteArray(BA, Idx);
    	//.
    	try {
    		TDataStreamerModule.TStreamingComponents NewStreamingComponents = new TDataStreamerModule.TStreamingComponents(Value);
    		//. supply the streaming components with its descriptors (stream descriptor datas)
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent != null) 
				NewStreamingComponents.SupplyComponentsWithStreamDescriptors(UserAgent.User());
    		//. set new streaming components
			DataStreamerModule.SetStreamingComponents(NewStreamingComponents);
		} catch (Exception E) {
			throw new IOException(E.getMessage()); //. =>
		}
    }
	
	@Override
	public synchronized byte[] ToByteArray() throws IOException, OperationException {
		TDataStreamerModule.TStreamingComponents StreamingComponents = DataStreamerModule.GetStreamingComponents();
		//.
		Timestamp = OleDate.UTCCurrentTimestamp();
    	try {
			Value = StreamingComponents.ToByteArray();
		} catch (Exception E) {
			throw new IOException(E); //. =>
		}
    	//.
		return super.ToByteArray();
	}
	
	@Override
    public synchronized byte[] ToByteArrayByAddressData(byte[] AddressData) throws Exception {
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
    	
    	case 1: //. get an own streaming component stream descriptor
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		TDataStreamerModule.TStreaming Streaming = DataStreamerModule.GetStreaming();
    		if (Streaming != null) {
    			TStreamDescriptor StreamDescriptor = Streaming.ComponentStreamDescriptor;
    			if (StreamDescriptor != null)
    				Value = StreamDescriptor.ToByteArray();
    			else
    				Value = null;
    		}
    		else
    			Value = null;
        	//.
            return super.ToByteArray(); //. ->

        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return super.ToByteArray(); //. ->
    	}
	}
}
