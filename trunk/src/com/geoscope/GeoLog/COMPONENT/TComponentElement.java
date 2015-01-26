package com.geoscope.GeoLog.COMPONENT;

import java.io.IOException;

import org.w3c.dom.Node;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.THistoryRecord;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TComponentElement {

	public static int[] GetAddressFromString(String AddressString) {
		String[] SA = AddressString.split("\\.");
		int Cnt = SA.length;
		int[] Result = new int[Cnt];
		for (int I = 0; I < Cnt; I++) 
			Result[I] = Integer.parseInt(SA[I]);
		return Result;
	}
	
	
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
		byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(AddressCount);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		for (int I = 0; I < AddressCount; I++) {
			BA = TDataConverter.ConvertInt16ToLEByteArray((short)Address[I]);
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
    
    public synchronized void FromXMLNode(Node node) {
    }
    
    public synchronized void FromXMLNodeByAddress(int[] Address, TIndex AddressIndex, Node node) {
    	FromXMLNode(node);
    }
    
    public synchronized THistoryRecord ToHistoryRecord(double pTimestamp, long UserID, boolean flSetOperation, Context context) {
    	return null;
    }
    
    public void ReadDeviceCUAC() throws Exception {
    	byte[] AddressArray = GetAddressArray();
    	byte[] Result = RootOwner().Schema.ObjectModel.ObjectController.Component_ReadDeviceCUAC(AddressArray);
    	TIndex Index = new TIndex();
    	FromByteArray(Result,Index);
    }

    public void ReadDeviceByAddressDataCUAC(byte[] AddressData) throws Exception {
    	byte[] AddressArray = GetAddressArray();
    	byte[] Result = RootOwner().Schema.ObjectModel.ObjectController.Component_ReadDeviceByAddressDataCUAC(AddressArray,AddressData);
    	TIndex Index = new TIndex();
    	FromByteArray(Result,Index);
    }
    
    public void WriteDeviceCUAC() throws Exception {
    	byte[] AddressArray = GetAddressArray();
    	byte[] Value = ToByteArray();
    	RootOwner().Schema.ObjectModel.ObjectController.Component_WriteDeviceCUAC(AddressArray,Value);
    }

    public void WriteDeviceByAddressDataCUAC(byte[] AddressData) throws Exception {
    	byte[] AddressArray = GetAddressArray();
    	byte[] Value = ToByteArray();
    	RootOwner().Schema.ObjectModel.ObjectController.Component_WriteDeviceByAddressDataCUAC(AddressArray,AddressData,Value);
    }
}
