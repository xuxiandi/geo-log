package com.geoscope.GeoLog.DEVICE.DataStreamerModule;

import java.io.IOException;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedBooleanValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetDataStreamerActiveValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TDataStreamerActiveValue extends TComponentTimestampedBooleanValue {

	private TDataStreamerModule DataStreamerModule;
	
	public TDataStreamerActiveValue(TDataStreamerModule pDataStreamerModule) {
		DataStreamerModule = pDataStreamerModule;
	}
	
	@Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
    	super.FromByteArray(BA, Idx);
    	//.
    	if ((Value != 0) && (!DataStreamerModule.flEnabled)) { 
    		Value = 0;
    		throw new OperationException(TSetDataStreamerActiveValueSO.OperationErrorCode_DataStreamerIsDisabled); //. =>
    	}
    	//.
    	try {
			DataStreamerModule.SetActive(Value != 0);
		} catch (TChannel.ConfigurationErrorException CEE) {
    		throw new OperationException(TSetDataStreamerActiveValueSO.OperationErrorCode_DataStreamerConfigurationError); //. =>
		} catch (TDataStreamerModule.TStreaming.ChannelsConflictError CCE) {
    		throw new OperationException(TSetDataStreamerActiveValueSO.OperationErrorCode_DataStreamerChannelsConflictError); //. =>
		} catch (Exception E) {
			throw new IOException(E.getMessage()); //. =>
		}
    }
}
