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
    public class TDeviceGetComponentDataServiceOperation extends TDeviceComponentServiceOperation 
    {            
        public static final short SID = (short)35103; //. operation with CUAC (component user access check)  
        
        public TDeviceGetComponentDataServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
        {
            super(pConnector,pUserID,pUserPassword,pObjectID,pSession,pSubAddress);
        }
        
        public int ProcessIncomingOperation(short OperationSession, byte[] PreambleMessage, TIndex Index, InputStream ConnectionInputStream, OutputStream ConnectionOutputStream) throws OperationException,IOException
        {
            int ResultCode = SuccessCode_OK;
            try
            {
                byte[] Data = null;
                try
                {
                    if (SubAddress == null)
                        Data = PrepareData();
                    else
                        Data = PrepareDataFromSubAddress();
                    //.
                    if (Data == null)
                        ResultCode = ErrorCode_NullData;
                }
                catch (OperationException E)
                {
                    ResultCode = E.Code;
                }
                catch (Exception E)
                {
                    ResultCode = ErrorCode_Unknown;
                }
                catch (OutOfMemoryError E)
                {
                    ResultCode = ErrorCode_DataOutOfMemory;
                }
                //. sending result
                try {
                    int MessageSize = (MessageProtocolSize+4/*SizeOf(ResultCode)*/);
                    if ((ResultCode >= 0) && (Data != null))
                        MessageSize += Data.length;
                    byte[] Message = new byte[MessageSize];
                    int Idx = MessageOrigin;
                    byte[] BA = ConvertInt32ToBEByteArray(ResultCode);
                    System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                    if ((ResultCode >= 0) && (Data != null)) {
                        System.arraycopy(Data,0,Message,Idx,Data.length); Idx+=Data.length;
                    }
                    //. encode and send message
                    SendMessage(Connector,UserID,UserPassword,ConnectionOutputStream,Session.ID,Message);
                }
                catch (OutOfMemoryError E) {
                    SendResultCode(Connector,UserID,UserPassword,ConnectionOutputStream,Session.ID,ErrorCode_DataOutOfMemory);
                    //.
                    throw E; //. =>
                }
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