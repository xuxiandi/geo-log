/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

public class TDeviceComponentByAddressDataServiceOperation extends TDeviceComponentServiceOperation { 

    public TDeviceComponentByAddressDataServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress, byte[] pAddressData)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
        AddressData = pAddressData;
    }
    
    public static byte[] GetAddressData(byte[] Message, TIndex Idx) throws IOException
    {
        int AddressDataSize = ConvertBEByteArrayToInt32(Message,Idx.Value); Idx.Value+=4;
        byte[] Result = new byte[AddressDataSize];
        if (AddressDataSize > 0) {
        	System.arraycopy(Message,Idx.Value, Result,0, AddressDataSize); Idx.Value += AddressDataSize;
        }
        return Result;
    }        

    protected synchronized int ParseData(byte[] Message, TIndex Origin) 
    {
        try
        {
            getValue().FromByteArrayByAddressData(Message,/*ref*/ Origin, AddressData);
        }
        catch (OperationException OE) 
        {
        	return OE.Code; //. ->
        }
        catch (Exception E)
        {
            return ErrorCode_Unknown; //. ->
        }
        catch (OutOfMemoryError E)
        {
            return ErrorCode_DataOutOfMemory; //. ->
        }
        return SuccessCode_OK;
    }
    
    protected synchronized int ParseDataToSubAddress(byte[] Message, TIndex Origin) 
    {
        TComponentValue V = getValueBySubAddress();
        if (V == null)
            return ComponentOperationErrorCode_AddressIsNotFound; //. ->
        try
        {
            V.FromByteArrayByAddressData(Message,/*ref*/ Origin, AddressData);
        }
        catch (OperationException OE) 
        {
        	return OE.Code; //. ->
        }
        catch (Exception E)
        {
            return ErrorCode_Unknown; //. ->
        }
        catch (OutOfMemoryError E)
        {
            return ErrorCode_DataOutOfMemory; //. ->
        }
        return SuccessCode_OK;
    }    

    protected synchronized byte[] PrepareData() throws Exception
    {
        return getValue().ToByteArrayByAddressData(AddressData);
    }

    protected synchronized byte[] PrepareDataFromSubAddress() throws Exception
    {
        TComponentValue V = getValueBySubAddress();
        if (V == null)
            throw new OperationException(ComponentOperationErrorCode_AddressIsNotFound); //. ->
        return V.ToByteArrayByAddressData(AddressData);
    }
}