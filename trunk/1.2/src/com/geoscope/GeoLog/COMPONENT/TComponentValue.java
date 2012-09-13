/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TComponentValue extends TComponentItem
{

	public TComponentValue() {
		super();
	}

	public TComponentValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

	public boolean flSet = false;
    public String DataFileName = null;
    
    public void Finalize() {
    	File DF = null;
    	synchronized (this) {
        	if (DataFileName != null) 
        		DF = new File(DataFileName);
    	}
    	if ((DF != null) && DF.exists())
    		DF.delete();
    }
    public synchronized void Assign(TComponentValue pValue) //. TComponentValue fields copy
    {
        flSet = pValue.flSet;
    }
    
    public synchronized void setValue(TComponentValue pValue) //. TComponentValue fields set (copy and mark as "set")
    {
        Assign(pValue);
        flSet = true;
    }
    
    public synchronized TComponentValue getValue()
    {
        return null;
    }
    
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        return false;
    }
    
    public synchronized boolean IsValid() {
    	if (DataFileName == null)
    		return true; //. =>
		File DF = new File(DataFileName);
		return (DF.exists());
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        flSet = true;
    }
    
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception
    {
        flSet = true;
    }
    
    public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
    	FromByteArray(BA, Idx);
    }
    
    @Override
    public synchronized byte[] ToByteArray() throws IOException, OperationException
    {
        return null;
    }

    public synchronized byte[] ToByteArrayByAddressData(byte[] AddressData) throws Exception
    {
        return null;
    }
    
    public synchronized byte[] Saving_ToByteArray() throws IOException, OperationException
    {
        if (DataFileName == null)
        	return ToByteArray(); //. ->
        return DataFileName_ToByteArray();
    }

    public int ByteArraySize()
    {
        return 0;
    }
    
    public int Saving_ByteArraySize()
    {
		return DataFileName_Size(); 
    }
    
    public synchronized boolean IsSet() {
    	return flSet;
    }
    
    public synchronized void SetSetFlag() {
    	flSet = true;
    }
    
    public synchronized void ClearSetFlag() {
    	flSet = false;
    }
    
    public synchronized void DataFileName_Assign(String Folder, double Timestamp) {
    	DataFileName = Folder+"/"+Double.toString(Timestamp)+".dat";
    }
    
    public synchronized void DataFileName_FromByteArray(byte[] BA, TIndex Idx) throws IOException {
        int SS = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        byte[] SA = new byte[SS];
        System.arraycopy(BA,Idx.Value, SA,0, SS); Idx.Value += SS;
        DataFileName = new String(SA,"windows-1251");
    }

    public synchronized int DataFileName_Size() {
    	return (4/*SizeOf(DataSize)*/+DataFileName.length()/*windows-1251*/);
    }
    
    public synchronized byte[] DataFileName_ToByteArray() throws IOException {
        int SS = 0;
        byte[] SA = null;
        SA = DataFileName.getBytes("windows-1251");
        SS = SA.length;
        byte[] Result = new byte[4/*SizeOf(DataSize)*/+SS];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(SS);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        if (SS > 0) {            
            System.arraycopy(SA,0,Result,Idx,SS); Idx+=SS;
        }
        return Result;
    }
    
    public int DataFile_Size() {
		File DF = new File(DataFileName);
		return (int)DF.length();
    }

    public byte[] DataFile_ToByteArray() throws IOException {
		File DF = new File(DataFileName);
		if (!DF.exists())
			return null; //. ->
    	long FileSize = DF.length();
    	FileInputStream FIS = new FileInputStream(DF);
    	try {
    		byte[] BA = new byte[(int)FileSize];
    		FIS.read(BA);
    		return BA; //. ->
    	}
    	finally {
    		FIS.close();
    	}
    }
}
