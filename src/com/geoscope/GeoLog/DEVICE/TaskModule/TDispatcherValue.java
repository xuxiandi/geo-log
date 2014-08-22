/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.TaskModule;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 * @author ALXPONOM
 */
public class TDispatcherValue extends TComponentValue {

	public static final int DISPATCH_POLICY_UNKNOWN 	= 0;
	public static final int DISPATCH_POLICY_FIRSTFREE 	= 1;
	
	public static class TExpertIsDispatchedHandler {
		
		public void DoOnExpertIsDispatched(int idUser) {
		}
	}
	
	public static class TExceptionHandler {
		
		public void DoOnException(Exception E) {
		}
	}
	
	
	public TExpertIsDispatchedHandler 	ExpertIsDispatchedHandler = null;
	//.
	public TExceptionHandler			ExceptionHandler = null;

	public TDispatcherValue() {
        flSet = true;
    }
    
    public TDispatcherValue(byte[] BA, TIndex Idx) throws IOException {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized void Assign(TComponentValue pValue) {
        super.Assign(pValue);
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException {
    }
    
    @Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception {
    	super.FromByteArrayByAddressData(BA, Idx, AddressData);
		//.
		if (AddressData == null)
			return; //. ->
    	String Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	String[] SA = Params.split(",");
    	int Version = Integer.parseInt(SA[0]);
    	//.
    	switch (Version) {
    	case 1: //. dispatching with waiting for expert 
			int idUser = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value += 8; //. SizeOf(Int64)
    		if (ExpertIsDispatchedHandler != null) 
    			ExpertIsDispatchedHandler.DoOnExpertIsDispatched(idUser);
            break; //. >

    	case 2: //. dispatching with waiting for specified expert
			idUser = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value += 8; //. SizeOf(Int64)
    		if (ExpertIsDispatchedHandler != null) 
    			ExpertIsDispatchedHandler.DoOnExpertIsDispatched(idUser);
            break; //. >
            
        default:
            break; //. >
    	}
    }
    
    @Override
    public synchronized byte[] ToByteArray() throws IOException {
        return null;
    }

    @Override
    public int ByteArraySize() throws IOException { 
        return 0;
    }
}
