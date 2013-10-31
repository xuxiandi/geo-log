package com.geoscope.GeoLog.COMPONENT;

import java.io.IOException;

import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController.TGetComponentDataResult;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.Utils.TDataConverter;

public class TComponentElement {

	public TComponent Owner = null;
	public int ID = 0;
	public String Name = "";
	
	public TComponentElement()
	{
	}
	
	public TComponentElement(TComponent pOwner, int pID, String pName)
	{
		Owner = pOwner;
		ID = pID;
		Name = pName;
	}	
	
	public TComponent RootOwner() {
		TComponent Result = null;
		TComponent _Owner = Owner;
		while (_Owner != null) {
			Result = _Owner;
			_Owner = _Owner.Owner;
		}
		return Result;
	}
	
	public int[] GetAddress() {
		int AddressCount = 1;
		TComponentElement CE = Owner;
		while (CE != null) {
			AddressCount++;
			CE = CE.Owner;
		}
		int[] Result = new int[AddressCount];
		AddressCount--;
		Result[AddressCount] = ID;
		CE = Owner;
		while (CE != null) {
			AddressCount--;
			Result[AddressCount] = CE.ID;
			CE = CE.Owner;
		}
		return Result;
	}
	
	public byte[] GetAddressArray() throws IOException {
		int[] Address = GetAddress();
		byte[] Result = new byte[2/*SizeOf(AddressCount)*/+Address.length*2/*SizeOf(AddressItem)*/];
		int Idx = 0;
		short AddressCount = (short)Address.length;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(AddressCount);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		for (int I = 0; I < AddressCount; I++) {
			BA = TDataConverter.ConvertInt16ToBEByteArray((short)Address[I]);
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		}
		return Result;
	}
	
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    }
    
    public synchronized byte[] ToByteArray() throws IOException, OperationException
    {
        return null;
    }
    
    public void ReadDeviceCUAC() throws Exception {
    	byte[] AddressArray = GetAddressArray();
    	TGetComponentDataResult Result = RootOwner().Schema.ObjectModel.ObjectController.DeviceOperation_GetComponentDataCommand2(AddressArray);
    	TIndex Index = new TIndex();
    	FromByteArray(Result.Value,Index);
    }

    public void ReadDeviceByAddressData(byte[] AddressData) throws Exception {
    	byte[] AddressArray = GetAddressArray();
    	TGetComponentDataResult Result = RootOwner().Schema.ObjectModel.ObjectController.DeviceOperation_AddressDataGetComponentDataCommand1(AddressArray,AddressData);
    	TIndex Index = new TIndex();
    	FromByteArray(Result.Value,Index);
    }
    
    public void WriteDeviceCUAC() throws Exception {
    	byte[] AddressArray = GetAddressArray();
    	byte[] Value = ToByteArray();
    	RootOwner().Schema.ObjectModel.ObjectController.DeviceOperation_SetComponentDataCommand2(AddressArray,Value);
    }

    public void WriteDeviceByAddressDataCUAC(byte[] AddressData) throws Exception {
    	byte[] AddressArray = GetAddressArray();
    	byte[] Value = ToByteArray();
    	RootOwner().Schema.ObjectModel.ObjectController.DeviceOperation_AddressDataSetComponentDataCommand2(AddressArray,AddressData,Value);
    }
}
