/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

    //. object get data service operation
    public class TObjectSetGetComponentDataServiceOperation extends TObjectSetComponentDataServiceOperation
    {            
        //. operation's class SID base
        private static final short AddressDataSID = (short)10001; //. //. operation with CUAC (component user access check) service ID  

        public byte[] Result;
        
        public TObjectSetGetComponentDataServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
        {
            super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
            Session.ID = NewSessionID();
        }

        public int ProcessOutgoingOperation(InputStream ConnectionInputStream, OutputStream ConnectionOutputStream) throws Exception
        {
            ConcurrentOperationSessionID = 0;
            ConcurrentOperationMessage = null;
            ConcurrentOperationMessageOrigin = new TIndex();
            //.
            int ResultCode = SuccessCode_OK;
            SetProcessingFlag();
            try
            {
                try
                {
                    TElementAddress EA = Address();
                    //.
                    byte[] Data;
                    if (SubAddress == null)
                        Data = PrepareData();
                    else 
                        Data = PrepareDataFromSubAddress();
                    short BatchCount = (short)ValueCount();
                    //. send data to the client
                    int AddressDataSize = 0;
                    if (AddressData != null)
                    	AddressDataSize = AddressData.length;
                    //.
                    int MessageSize; 
                    if (AddressDataSize == 0)
                    	MessageSize = (MessageProtocolSize+2/*SizeOf(SID)*/+4/*SizeOf(ObjectID)*/+(EA.Value.length+1)*2/*SizeOf(AddressItem)*/+Data.length);
                    else
                    	MessageSize = (MessageProtocolSize+2/*SizeOf(SID)*/+4/*SizeOf(ObjectID)*/+(EA.Value.length+1)*2/*SizeOf(AddressItem)*/+4/*SizeOf(AddressDataSize)*/+AddressDataSize+Data.length);
                    //.
                    if (BatchCount > 1)
                        MessageSize+=2/*SizeOf(ValuesCounter)*/;
                    byte[] Message = new byte[MessageSize];
                    int Idx = MessageOrigin;
                    byte[] BA;
                    short ServiceID;
                    if ((BatchCount == 1) && (AddressDataSize != 0)) 
                    	ServiceID = AddressDataSID;
                    else 
                    	throw new IOException("unsupported operation in this mode"); //. =>
                    BA = ConvertInt16ToBEByteArray(ServiceID);
                    System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                    BA = ConvertInt32ToBEByteArray(ObjectID);
                    System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                    //.
                    BA = ConvertInt16ToBEByteArray((short)EA.Value.length);
                    System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                    for (int I = 0; I < EA.Value.length; I++)
                    {
                        BA = ConvertInt16ToBEByteArray(EA.Value[I]);
                        System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                    }
                    //.
                    if (AddressDataSize > 0) 
                    {
                        BA = ConvertInt32ToBEByteArray(AddressDataSize);
                        System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                        System.arraycopy(AddressData,0,Message,Idx,AddressData.length); Idx+=AddressData.length;
                    }
                    //.
                    if (BatchCount > 1)
                    {
                        BA = ConvertInt16ToBEByteArray(BatchCount);
                        System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                    }
                    if (Data != null) {
                    	System.arraycopy(Data,0,Message,Idx,Data.length); Idx+=Data.length;
                    }
                    //. encode and send message
                    SendMessage(Connector,UserID,UserPassword,ConnectionOutputStream,Session.ID,Message);
                    //.
                    int CompletionTime = TimeForCompletion(BatchCount);
                    //. receive response message
                    TOperationSession ResponseSession = new TOperationSession();
                    TIndex ResponseMessageOrigin = new TIndex();
                    byte[] ResponseMessage = ReceiveMessageWithinTime(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream,/*out*/ ResponseSession,/*out*/ ResponseMessageOrigin,CompletionTime);
                    //.
                    if (ResponseSession.ID == Session.ID)
                    {
                        CheckResponseMessage(ResponseMessage,/*ref*/ ResponseMessageOrigin);
                        //. copy result
                        int ResultSize = ResponseMessage.length-MessageProtocolSuffixSize-ResponseMessageOrigin.Value;
                        if (ResultSize > 0) {
                        	Result = new byte[ResultSize];
                        	System.arraycopy(ResponseMessage,ResponseMessageOrigin.Value, Result,0, ResultSize);
                        }
                        else
                        	Result = null; 
                    }
                    else
                    {
                        //. new concurrent incoming operation has arrived so deferre and execute it after 
                        ConcurrentOperationSessionID = ResponseSession.ID;
                        ConcurrentOperationMessage = ResponseMessage;
                        ConcurrentOperationMessageOrigin.Value = ResponseMessageOrigin.Value;
                        //.
                        ResponseMessage = ReceiveMessageWithinTime(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream,/*out*/ ResponseSession,/*out*/ ResponseMessageOrigin,CompletionTime);
                        if (ResponseSession.ID != Session.ID)
                            throw new OperationException(ErrorCode_OperationError,"too many concurrent operations"); //. =>
                        CheckResponseMessage(ResponseMessage,/*ref*/ ResponseMessageOrigin);
                    }
                }
                catch (OperationException E)
                {
                    ResultCode = E.Code;
                    if (E.IsCommunicationError())
                        throw E; //. =>
                }
                catch (Exception E)
                {
                	throw new OperationException(ErrorCode_Unknown); //. =>
                }
                catch (OutOfMemoryError E)
                {
                	throw new OperationException(ErrorCode_DataOutOfMemory); //. =>
                }
            }
            finally
            {
                ClearProcessingFlag();
            }
            //. execute delayed concurrent incoming operation if it exists
            if (ConcurrentOperationSessionID != 0)
                Connector.ProcessIncomingOperation(ConcurrentOperationSessionID,ConcurrentOperationMessage,ConcurrentOperationMessageOrigin, null, ConnectionInputStream,ConnectionOutputStream, Connector.ProcessIncomingOperationResult);
            return ResultCode;
        }
        
        public int StartOutgoingOperation(OutputStream ConnectionOutputStream) throws Exception
        {
            ConcurrentOperationSessionID = 0;
            ConcurrentOperationMessage = null;
            ConcurrentOperationMessageOrigin = new TIndex();
            //.
            int CompletionTime = 0;
            SetProcessingFlag();
            try
            {
                TElementAddress EA = Address();
                //.
                byte[] Data;
                if (SubAddress == null)
                    Data = PrepareData();
                else
                    Data = PrepareDataFromSubAddress();
                short BatchCount = (short)ValueCount();
                //. send data to the client
                int AddressDataSize = 0;
                if (AddressData != null)
                	AddressDataSize = AddressData.length;
                //.
                int MessageSize; 
                if (AddressDataSize == 0)
                	MessageSize = (MessageProtocolSize+2/*SizeOf(SID)*/+4/*SizeOf(ObjectID)*/+(EA.Value.length+1)*2/*SizeOf(AddressItem)*/+Data.length);
                else
                	MessageSize = (MessageProtocolSize+2/*SizeOf(SID)*/+4/*SizeOf(ObjectID)*/+(EA.Value.length+1)*2/*SizeOf(AddressItem)*/+4/*SizeOf(AddressDataSize)*/+AddressDataSize+Data.length);
                //.
                if (BatchCount > 1)
                    MessageSize+=2/*SizeOf(ValuesCounter)*/;
                byte[] Message = new byte[MessageSize];
                int Idx = MessageOrigin;
                byte[] BA;
                short ServiceID;
                if ((BatchCount == 1) && (AddressDataSize != 0)) 
                	ServiceID = AddressDataSID;
                else 
                	throw new IOException("unsupported operation in this mode"); //. =>
                BA = ConvertInt16ToBEByteArray(ServiceID);
                System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                BA = ConvertInt32ToBEByteArray(ObjectID);
                System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                BA = ConvertInt16ToBEByteArray((short)EA.Value.length);
                System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                for (int I = 0; I < EA.Value.length; I++)
                {
                    BA = ConvertInt16ToBEByteArray(EA.Value[I]);
                    System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                }
                //.
                if (AddressDataSize > 0) 
                {
                    BA = ConvertInt32ToBEByteArray(AddressDataSize);
                    System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                    System.arraycopy(AddressData,0,Message,Idx,AddressData.length); Idx+=AddressData.length;
                }
                //.
                if (BatchCount > 1)
                {
                    BA = ConvertInt16ToBEByteArray(BatchCount);
                    System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
                }
                if (Data != null) {
                	System.arraycopy(Data,0,Message,Idx,Data.length); Idx+=Data.length;
                }
                //. encode and send message
                SendMessage(Connector,UserID,UserPassword,ConnectionOutputStream,Session.ID,Message);
                //. 
                CompletionTime = TimeForCompletion(BatchCount);
            }
            catch (OperationException E)
            {
                ClearProcessingFlag();
                throw E; //. =>
            }
            catch (Exception E)
            {
                ClearProcessingFlag();
            	throw new OperationException(ErrorCode_Unknown); //. =>
            }
            catch (OutOfMemoryError E)
            {
                ClearProcessingFlag();
            	throw new OperationException(ErrorCode_DataOutOfMemory); //. =>
            }
            return CompletionTime;
        }
        
        public int FinishOutgoingOperation(InputStream ConnectionInputStream, OutputStream ConnectionOutputStream, int CompletionTime) throws OperationException,IOException,InterruptedException
        {
            int ResultCode = SuccessCode_OK;
            try
            {
                try
                {
                    //. receive response message
                    TOperationSession ResponseSession = new TOperationSession();
                    TIndex ResponseMessageOrigin = new TIndex();
                    byte[] ResponseMessage = ReceiveMessageWithinTime(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream,/*out*/ ResponseSession,/*out*/ ResponseMessageOrigin,CompletionTime);
                    //.
                    if (ResponseSession.ID == Session.ID)
                    {
                        CheckResponseMessage(ResponseMessage,/*ref*/ ResponseMessageOrigin);
                        //. copy result
                        int ResultSize = ResponseMessage.length-MessageProtocolSuffixSize-ResponseMessageOrigin.Value;
                        if (ResultSize > 0) {
                        	Result = new byte[ResultSize];
                        	System.arraycopy(ResponseMessage,ResponseMessageOrigin.Value, Result,0, ResultSize);
                        }
                        else
                        	Result = null; 
                    }
                    else
                    {
                        //. new concurrent incoming operation has arrived so deferre and execute it after 
                        ConcurrentOperationSessionID = ResponseSession.ID;
                        ConcurrentOperationMessage = ResponseMessage;
                        ConcurrentOperationMessageOrigin.Value = ResponseMessageOrigin.Value;
                        //.
                        ResponseMessage = ReceiveMessageWithinTime(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream,/*out*/ ResponseSession,/*out*/ ResponseMessageOrigin,CompletionTime);
                        if (ResponseSession.ID != Session.ID)
                            throw new OperationException(ErrorCode_OperationError,"too many concurrent operations"); //. =>
                        CheckResponseMessage(ResponseMessage,/*ref*/ ResponseMessageOrigin);
                    }
                }
                catch (OperationException E)
                {
                    ResultCode = E.Code;
                    if (E.IsCommunicationError())
                        throw E; //. =>
                }
                catch (Exception E)
                {
                	throw new OperationException(ErrorCode_Unknown); //. =>
                }
                catch (OutOfMemoryError E)
                {
                	throw new OperationException(ErrorCode_DataOutOfMemory); //. =>
                }
            }
            finally
            {
                ClearProcessingFlag();
            }
            return ResultCode;
        }
    }
    