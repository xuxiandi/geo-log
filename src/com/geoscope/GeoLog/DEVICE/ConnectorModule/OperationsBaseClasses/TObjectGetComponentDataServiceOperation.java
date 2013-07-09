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
    public class TObjectGetComponentDataServiceOperation extends TObjectComponentServiceOperation 
    {            
        //. operation's class SID base
        public static final short SID = (short)5503; //. //. operation with CUAC (component user access check) service ID ///+ 
        public static final short AddressDataSID = (short)5504; //. //. operation with CUAC (component user access check) service ID ///+ 
        
        public TObjectGetComponentDataServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
        {
            super(pConnector,pUserID,pUserPassword,pObjectID, pSubAddress);
            Session.ID = NewSessionID();
        }
        
        public int ProcessOutgoingOperation(InputStream ConnectionInputStream, OutputStream ConnectionOutputStream) throws OperationException, InterruptedException, IOException 
        {
            short ConcurrentOperationSessionID = 0;
            byte[] ConcurrentOperationMessage = null;
            TIndex ConcurrentOperationMessageOrigin = new TIndex();
            //.
            int ResultCode = SuccessCode_OK;
            try
            {
                TElementAddress EA = Address();
                //. send data to the client
                int AddressDataSize = 0;
                if (AddressData != null)
                	AddressDataSize = AddressData.length;
                //.
                int MessageSize;
                if (AddressDataSize == 0)
                	MessageSize = (MessageProtocolSize+2/*SizeOf(SID)*/+4/*SizeOf(ObjectID)*/+(EA.Value.length+1)*2/*SizeOf(AddressItem)*/);
                else
                	MessageSize = (MessageProtocolSize+2/*SizeOf(SID)*/+4/*SizeOf(ObjectID)*/+(EA.Value.length+1)*2/*SizeOf(AddressItem)*/+4/*SizeOf(AddressDataSize)*/+AddressDataSize);
                byte[] Message = new byte[MessageSize];
                int Idx = MessageOrigin;
                byte[] BA;
                if (AddressDataSize == 0)
                	BA = ConvertInt16ToBEByteArray(SID);
                else
                	BA = ConvertInt16ToBEByteArray(AddressDataSID);
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
                //. encode and send message
                SendMessage(Connector,UserID,UserPassword,ConnectionOutputStream,Session.ID,Message);
                //. receive response message
                TOperationSession ResponseSession = new TOperationSession();
                TIndex ResponseMessageOrigin = new TIndex();
                byte[] ResponseMessage = ReceiveMessage(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream,/*out*/ ResponseSession,/*out*/ ResponseMessageOrigin);
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
                    ResponseMessage = ReceiveMessage(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream,/*out*/ ResponseSession,/*out*/ ResponseMessageOrigin);
                    if (ResponseSession.ID != Session.ID)
                        throw new OperationException(ErrorCode_OperationError,"too many concurrent operations"); //. =>
                    CheckResponseMessage(ResponseMessage,/*ref*/ ResponseMessageOrigin);
                }
                //. parsing response
                if (SubAddress == null)
                    ResultCode = ParseData(ResponseMessage,/*ref*/ ResponseMessageOrigin);
                else
                    ResultCode = ParseDataToSubAddress(ResponseMessage,/*ref*/ ResponseMessageOrigin);
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
            //. execute delayed incoming operation if it exists
            if (ConcurrentOperationSessionID != 0)
                Connector.ProcessIncomingOperation(ConcurrentOperationSessionID,ConcurrentOperationMessage,ConcurrentOperationMessageOrigin, null, ConnectionInputStream,ConnectionOutputStream, Connector.ProcessIncomingOperationResult);
            return ResultCode;
        }
    }
