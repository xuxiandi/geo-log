/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

    //. object get data service operation
    public class TDeviceSetComponentDataByAddressDataServiceOperation extends TDeviceComponentByAddressDataServiceOperation 
    {
        public static final short SID = (short)30506; //. operation with CUAC (component user access check) //. service ID ///+ 
        
        public TDeviceSetComponentDataByAddressDataServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress, byte[] pAddressData)
        {
            super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress,pAddressData);
        }
        
        public int ProcessIncomingOperation(short OperationSession, byte[] PreambleMessage, TIndex Index, InputStream ConnectionInputStream, OutputStream ConnectionOutputStream) throws OperationException,IOException
        {
            //. parse and set value
            int ResultCode;
            try {
                if (SubAddress == null)
                    ResultCode = ParseData(PreambleMessage,/*ref*/ Index);
                else
                    ResultCode = ParseDataToSubAddress(PreambleMessage,/*ref*/ Index);
            }
            catch (OutOfMemoryError E)
            {
                ResultCode = ErrorCode_DataOutOfMemory;
            }
            //. 
            try
            {
                SendResultCode(Connector,UserID,UserPassword,ConnectionOutputStream,OperationSession,ResultCode);
            }
            catch (OperationException E)
            {
                ResultCode = E.Code;
                if (E.IsCommunicationError())
                    throw E; //. =>
            }
            return ResultCode;
        }
    }