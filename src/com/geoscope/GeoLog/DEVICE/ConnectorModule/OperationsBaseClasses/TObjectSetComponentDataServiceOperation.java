/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

    //. object get data service operation
    public class TObjectSetComponentDataServiceOperation extends TObjectComponentServiceOperation 
    {            
        //. max delay of operation in queue waiting for transmitting
        public static final int QueueMaxDelay = 0; //. milliseconds
        //. operation's class SID base
        private static final short SID = (short)507; //. //. operation with CUAC (component user access check) service ID ///+ 
        private static final short BatchOperationSID = (short)508; //. //. operation with CUAC (component user access check) service ID ///+ 
        private static final short VariableSizedValueBatchOperationSID = (short)509; //. //. operation with CUAC (component user access check) service ID ///+
        private static final short AddressDataSID = (short)506; //. //. operation with CUAC (component user access check) service ID ///+ 
        
        public class QueueIsFullException extends Exception
        {
			private static final long serialVersionUID = 1L;

			public QueueIsFullException()
            {
                super(Connector.Device.context.getString(R.string.SQueueIsFull));
            }
        }
    
        public boolean flComponentFileStream = false;
        //.
        private long QueueMaxTime;
        //.
        public short 	ConcurrentOperationSessionID = 0;
        public byte[] 	ConcurrentOperationMessage = null;
        public TIndex 	ConcurrentOperationMessageOrigin = new TIndex();
        
        public TObjectSetComponentDataServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
        {
            super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
            Session.ID = NewSessionID();
            QueueMaxTime = Calendar.getInstance().getTime().getTime()+GetQueueMaxDelay();
        }

        public int GetQueueMaxDelay() {
        	return QueueMaxDelay;
        }
        
        public long GetQueueMaxTime() {
        	return QueueMaxTime;
        }
        
        protected synchronized boolean ValueIsVariableSized() {
        	return false;
        }
        
        protected synchronized int ValueSize()
        {
            return 0;
        }
        
        public synchronized int ValueCount()
        {
            return 0;
        }
        
        public synchronized int BatchSize() throws Exception 
        {
            return (ValueSize()*ValueCount());
        }
        
        public synchronized boolean AddNewValue(TComponentValue Value) 
        {
             return false;
        }
        
        protected int TimeForCompletion(int Mult) 
        {
            return Connection_DataWaitingInterval;
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
                    if (BatchCount > 1) {
                        if (AddressDataSize == 0)
                        {
                        	if (!ValueIsVariableSized())
                        		ServiceID = BatchOperationSID;
                        	else
                        		ServiceID = VariableSizedValueBatchOperationSID;
                        }
                        else throw new IOException("unsupported operation in batch mode"); //. =>
                    }
                    else 
                    {
                        if (AddressDataSize == 0)
                        	ServiceID = SID;
                        else
                        	ServiceID = AddressDataSID;
                    }
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
                if (BatchCount > 1) {
                	if (!ValueIsVariableSized())
                		ServiceID = BatchOperationSID;
                	else
                		ServiceID = VariableSizedValueBatchOperationSID;
                }
                else
                    ServiceID = SID;
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
        
        public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws Exception
        {
        	getValue().FromByteArray(BA,/*ref*/ Idx);
        }
    
        public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws Exception
        {
        	FromByteArray(BA,Idx);
        }
    
        @SuppressWarnings("unused")
		private synchronized byte[] ToByteArray() throws Exception {
            TElementAddress EA = Address();
            int AddressDataSize = 0;
            if (AddressData != null)
            	AddressDataSize = AddressData.length;
            //.
            short _BatchCount = (short)ValueCount();
            byte[] Data;
            if (SubAddress == null)
                Data = PrepareData();
            else 
                Data = PrepareDataFromSubAddress();
            //. 
            int ResultSize = ((1+EA.Value.length)*2/*SizeOf(AddressItem)*/+4/*SizeOf(AddressDataSize)*/+AddressDataSize+2/*SizeOf(_BatchCount)*/+Data.length);
            byte[] Result = new byte[ResultSize];
            int Idx = 0;
            byte[] BA;
            BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray((short)EA.Value.length);
            System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
            for (int I = 0; I < EA.Value.length; I++) {
                BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(EA.Value[I]);
                System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
            }
            BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(AddressDataSize);
            System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
            if (AddressDataSize > 0) {
            	System.arraycopy(AddressData,0,Result,Idx,AddressDataSize); Idx+=AddressDataSize;
            }
            BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(_BatchCount);
            System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
            if (Data != null) {
            	System.arraycopy(Data,0,Result,Idx,Data.length); Idx+=Data.length;
            }
            return Result;
    }
    
        public synchronized byte[] Saving_ToByteArray() throws Exception {
                TElementAddress EA = Address();
                int AddressDataSize = 0;
                if (AddressData != null)
                	AddressDataSize = AddressData.length;
                //.
                short _BatchCount = (short)ValueCount();
                byte[] Data;
                if (SubAddress == null)
                    Data = Saving_PrepareData();
                else 
                    Data = Saving_PrepareDataFromSubAddress();
                //. 
                int ResultSize = ((1+EA.Value.length)*2/*SizeOf(AddressItem)*/+4/*SizeOf(AddressDataSize)*/+AddressDataSize+2/*SizeOf(_BatchCount)*/+Data.length);
                byte[] Result = new byte[ResultSize];
                int Idx = 0;
                byte[] BA;
                BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray((short)EA.Value.length);
                System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
                for (int I = 0; I < EA.Value.length; I++) {
                    BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(EA.Value[I]);
                    System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
                }
                BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(AddressDataSize);
                System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
                if (AddressDataSize > 0) {
                	System.arraycopy(AddressData,0,Result,Idx,AddressDataSize); Idx+=AddressDataSize;
                }
                BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(_BatchCount);
                System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
                if (Data != null) {
                	System.arraycopy(Data,0,Result,Idx,Data.length); Idx+=Data.length;
                }
                return Result;
        }
        
        public synchronized int DoOnOperationCompletion() throws Exception
        {
        	return SuccessCode_OK;
        }

        public synchronized void DoOnOperationException(OperationException E) 
        {
        }
    }
    