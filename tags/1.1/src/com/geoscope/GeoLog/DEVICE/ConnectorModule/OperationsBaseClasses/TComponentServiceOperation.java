/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TComponentServiceOperation extends TObjectServiceOperation
{
        public static final int ComponentOperationErrorCode_ComponentObjectModelIsNotExist = ErrorCode_CustomOperationError-1;
        public static final int ComponentOperationErrorCode_AddressIsNotFound = ErrorCode_CustomOperationError-2;

        public static short[] GetAddress(byte[] Message, TIndex Idx) throws IOException
        {
            int AddressCount = ConvertBEByteArrayToInt16(Message,Idx.Value); Idx.Value+=2;
            short[] Result = new short[AddressCount];
            for (int I = 0; I < AddressCount; I++)
            {
                Result[I] = ConvertBEByteArrayToInt16(Message,Idx.Value); Idx.Value+=2;
            }
            return Result;
        }        
        
        public short[] 	SubAddress = null;
        public byte[]	AddressData = null;
        private int _RefCount = 0;
        
        public TComponentServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
        {
            super(pConnector,pUserID,pUserPassword,pObjectID);
            SubAddress = pSubAddress;
        }
        
        public void Destroy()
        {
            //.
        }
        
        public synchronized void AddRef()
        {
            _RefCount++;
        }
        
        public synchronized void Release()
        {
            _RefCount--;
            if (_RefCount == 0)
                Destroy();
        }
        
        public synchronized int RefCount()
        {
            return _RefCount;
        }
        
        public TElementAddress Address()
        {
            return new TElementAddress(SubAddress);
        }
        
        protected synchronized void setValue(TComponentValue Value)
        {
        }
        
        protected synchronized void setValueBySubAddress(TComponentValue Value) throws OperationException
        {
        }
        
        public synchronized TComponentValue getValue() throws Exception
        {
            return null;
        }
        
        public synchronized TComponentValue getValueBySubAddress() 
        {
            return null;
        }
        
        protected synchronized int ParseData(byte[] Message, TIndex Origin)  
        {
            try
            {
                getValue().FromByteArray(Message,/*ref*/ Origin);
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
                return ErrorCode_DataOutOfMemory; //. =>
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
                V.FromByteArray(Message,/*ref*/ Origin);
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
                return ErrorCode_DataOutOfMemory; //. =>
            }
            return SuccessCode_OK;
        }
        
        protected synchronized byte[] PrepareData() throws Exception
        {
            return getValue().ToByteArray();
        }

        protected synchronized byte[] Saving_PrepareData() throws Exception
        {
            return PrepareData();
        }

        protected synchronized byte[] PrepareDataFromSubAddress() throws Exception
        {
            TComponentValue V = getValueBySubAddress();
            if (V == null)
                throw new OperationException(ComponentOperationErrorCode_AddressIsNotFound); //. ->
            return V.ToByteArray();
        }

        protected synchronized byte[] Saving_PrepareDataFromSubAddress() throws Exception
        {
            return PrepareDataFromSubAddress();
        }

        public synchronized int DoOnOperationCompletion() throws Exception
        {
        	return SuccessCode_OK;
        }

        public synchronized void DoOnOperationException(OperationException E) 
        {
        }
}
