package com.geoscope.GeoLog.DEVICE.DataStreamerModule;

import java.io.IOException;

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
}
