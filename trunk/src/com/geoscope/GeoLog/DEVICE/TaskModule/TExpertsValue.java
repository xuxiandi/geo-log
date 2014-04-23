/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.TaskModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.Utils.TDataConverter;

/**
 * @author ALXPONOM
 */
public class TExpertsValue extends TComponentValue {

	public static class TExpertDescriptorV1 {
		
		public int 		ID;
		public boolean  flOnline;
		public String 	Name;
		public String 	Domains;
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
			ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			flOnline = (BA[Idx] != 0); Idx++;
	    	int SS = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
	    	if (SS > 0) {
	    		Name = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Name = "";
	    	SS = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
	    	if (SS > 0) {
	    		Domains = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Domains = "";
			//.
			return Idx;
		}
	}

	public static class TExpertDescriptorsV1 {
		
		public TExpertDescriptorV1[] Items;
		
		public TExpertDescriptorsV1() {
		}
		
		public TExpertDescriptorsV1(byte[] BA, int Idx) throws IOException {
			FromByteArray(BA, Idx);
		}
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
			int Version = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			if (Version != 1)
				throw new IOException("unknown data version, version: "+Integer.toString(Version)); //. =>
			int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			Items = new TExpertDescriptorV1[ItemsCount];
			for (int I = 0; I < ItemsCount; I++) {
				Items[I] = new TExpertDescriptorV1();
				Idx = Items[I].FromByteArray(BA, Idx);
			}
			return Idx;
		}
	}
	
	public static class TExpertsIsReceivedHandler {
		
		public void DoOnExpertsIsReceived(TExpertDescriptorsV1 Experts) {
		}
	}
	
	public static class TExceptionHandler {
		
		public void DoOnException(Exception E) {
		}
	}
	
	
	public TExpertsIsReceivedHandler 	ExpertsIsReceivedHandler = null;
	//.
	public TExceptionHandler			ExceptionHandler = null;
	
    public TExpertsValue() {
        flSet = true;
    }
    
    public TExpertsValue(byte[] BA, TIndex Idx) throws IOException {
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
    	case 1:  
    		TExpertDescriptorsV1 Experts = new TExpertDescriptorsV1();
    		Idx.Value = Experts.FromByteArray(BA, Idx.Value);
    		//.
			if (ExpertsIsReceivedHandler != null)
				ExpertsIsReceivedHandler.DoOnExpertsIsReceived(Experts);
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
    public int ByteArraySize() throws Exception { 
        return 0;
    }
}
