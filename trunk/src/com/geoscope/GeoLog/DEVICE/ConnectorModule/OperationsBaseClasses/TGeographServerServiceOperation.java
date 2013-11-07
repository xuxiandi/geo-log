/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TMessage;
import com.geoscope.Utils.TDataConverter;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZOutputStream;
/**
 *
 * @author ALXPONOM
 */
public class TGeographServerServiceOperation 
{
	//. descriptor specific values
	public static int Descriptor_ConnectionIsClosing = 0;
	public static int Descriptor_MessageIsOutOfMemory = -1;
	
    //. component operations
    public static int idReadOperation = 3; //. from Functionality.pas
    public static int idWriteOperation = 4; //. from Functionality.pas

    //. packing methods
    public static final byte PackingMethod_None = 0;
    public static final byte PackingMethod_ZIP = 1;
    public static final byte PackingMethod_RAR = 2;
    public static final byte PackingMethod_ZLIBZIP = 11;
    
    //. encryption methods
    public static final byte EncryptionMethod_None = 0;
    public static final byte EncryptionMethod_SimpleByPassword = 1;
    
    //. success codes
    public static final int SuccessCode_OK = 0;
    public static final int SuccessCode_OKWithHint = 1;
    public static final int SuccessCode_OKWithWarning = 2;
    public static final int SuccessCode_OKWithNewOperation = 3;
    
    //. error codes - abstract
    public static final int ErrorCode_Unknown = -1;
    public static final int ErrorCode_DataIsNotFound = -2;
    public static final int ErrorCode_NullData = -3;
    public static final int ErrorCode_BadData = -4;
    public static final int ErrorCode_DataOutOfMemory = -5;
    //. error codes - connection errors (ErrorBase: -1000)
    public static final int ErrorCode_ConnectionError = -1000;
    public static final int ErrorCode_ConnectionReadWriteTimeOut = -1001;
    public static final int ErrorCode_ConnectionIsClosedUnexpectedly = -1002;
    public static final int ErrorCode_ConnectionIsClosedGracefully = -1003;
    public static final int ErrorCode_ConnectionIsClosedByCheckpointTimeout = -1004;
    public static final int ErrorCode_ConnectionIsClosedByWorkerThreadTermination = -1005;
    public static final int ErrorCode_ConnectionNodeIsOutOfMemory = -1006;
    //. error codes - message errors (ErrorBase: -2000)
    public static final int ErrorCode_MessageError = -2000;
    public static final int ErrorCode_MessageUserIsUnknown = -2001;
    public static final int ErrorCode_MessageUserIsChanged = -2002;
    public static final int ErrorCode_MessageEncryptionIsUnknown = -2003;
    public static final int ErrorCode_MessagePackingIsUnknown = -2004;
    public static final int ErrorCode_MessageDecryptionIsFailed = -2005;
    public static final int ErrorCode_MessageUnpackingIsFailed = -2006;
    public static final int ErrorCode_MessageIntegrityCheckIsFailed = -2007;
    public static final int ErrorCode_MessageIsOutOfMemory = -2008;
    //. error codes - service operation errors (ErrorBase: -3000)
    public static final int ErrorCode_OperationError = -3000;
    public static final int ErrorCode_OperationUnknownService = -3001;
    public static final int ErrorCode_OperationServiceIsNotSupported = -3002;
    public static final int ErrorCode_OperationUserAccessIsDenied = -3003;
    public static final int ErrorCode_OperationSessionIsChanged = -3004;
    public static final int ErrorCode_OperationPollingTimeout = -3005;
    public static final int ErrorCode_OperationProcessingTimeout = -3006;
    public static final int ErrorCode_OperationPoolIsBusy = -3007;
    //. error codes - device control service operation errors (ErrorBase: -4000)
    //. error codes - object service operation errors (ErrorBase: -5000)
    public static final int ErrorCode_ObjectOperationError = -5000;
    public static final int ErrorCode_ObjectOperationUnknownObject = -5001;
    public static final int ErrorCode_ObjectOperationObjectUserIsBad = -5002;
    public static final int ErrorCode_ObjectOperationObjectIsChanged = -5003;
    public static final int ErrorCode_ObjectOperationObjectIsOffline = -5004;
    //. device service operation errors (ErrorBase: -6000)
    //.
    //. object-device component service operation errors (ErrorBase: -7000)
    //.
    //. object component service operation errors (ErrorBase: -8000)
    public static final int ErrorCode_ObjectComponentOperationError = -8000;
    public static final int ErrorCode_ObjectComponentOperation_ComponentObjectModelIsNotExist = -8001;
    public static final int ErrorCode_ObjectComponentOperation_AddressIsNotFound = -8002;
    public static final int ErrorCode_ObjectComponentOperation_AddressIsDisabled = -8003;
    public static final int ErrorCode_ObjectComponentOperation_SetValueError = -8004;
    public static final int ErrorCode_ObjectComponentOperation_GetValueError = -8005;
    public static final int ErrorCode_ObjectComponentOperation_SetGetValueError = -8006;
    public static final int ErrorCode_ObjectComponentOperation_GetSetValueError = -8007;
    public static final int ErrorCode_ObjectComponentOperation_ComponentObjectModelIsNotSupported = -8008;
    public static final int ErrorCode_ObjectComponentOperation_ValueDataIsInvalid = -8009;
    //. device component service operation errors (ErrorBase: -9000)
    //.
    //. error codes - object custom service operation errors (ErrorBase: -1000000)
    public static final int ErrorCode_CustomOperationError = -1000000;
    
    public static String ErrorCode_ToString(int Code, Context context) {
    	if (Code >= 0)
    		return null; //. ->
    	if (Code <= ErrorCode_CustomOperationError)
    		return context.getString(R.string.SCustomOperationError)+", #"+Integer.toString(Code); //. ->
    	switch (Code) {
    	
    	case ErrorCode_Unknown:
    		return context.getString(R.string.SErrorUnknown); //. ->
        
    	case ErrorCode_DataIsNotFound:
    		return context.getString(R.string.SErrorDataIsNotFound); //. ->
    		
    	case ErrorCode_NullData:
    		return context.getString(R.string.SErrorNullData); //. ->
    		
    	case ErrorCode_BadData:
    		return context.getString(R.string.SErrorBadData); //. ->
    		
    	case ErrorCode_DataOutOfMemory:
    		return context.getString(R.string.SErrorDataIsOutOfMemory); //. ->
    		
        //. error codes - connection errors (ErrorBase: -1000)
    	case ErrorCode_ConnectionError:
    		return context.getString(R.string.SErrorConnectionError); //. ->
    		
    	case ErrorCode_ConnectionReadWriteTimeOut:
    		return context.getString(R.string.SErrorConnectionReadWriteTimeout); //. ->
    		
    	case ErrorCode_ConnectionIsClosedUnexpectedly:
    		return context.getString(R.string.SErrorConnectionIsClosedUnexpectedly); //. ->
    		
    	case ErrorCode_ConnectionIsClosedGracefully:
    		return context.getString(R.string.SErrorConnectionIsClosedGracefully); //. ->
    		
    	case ErrorCode_ConnectionIsClosedByCheckpointTimeout:
    		return context.getString(R.string.SErrorConnectionIsClosedByCheckpointTimeout); //. ->
    		
    	case ErrorCode_ConnectionIsClosedByWorkerThreadTermination:
    		return context.getString(R.string.SErrorConnectionIsClosedByWorkerThreadTermination); //. ->
    		
    	case ErrorCode_ConnectionNodeIsOutOfMemory:
    		return context.getString(R.string.SErrorConnectionNodeIsOutOfMemory); //. ->
    		
        //. error codes - message errors (ErrorBase: -2000)
    	case ErrorCode_MessageError:
    		return context.getString(R.string.SErrorMessageUnknownError); //. ->
    		
    	case ErrorCode_MessageUserIsUnknown:
    		return context.getString(R.string.SErrorMessageUserIsUnknown); //. ->
    		
    	case ErrorCode_MessageUserIsChanged:
    		return context.getString(R.string.SErrorMessageUserIsChanged); //. ->
    		
    	case ErrorCode_MessageEncryptionIsUnknown:
    		return context.getString(R.string.SErrorMessageEncryptionIsUnknown); //. ->
    		
    	case ErrorCode_MessagePackingIsUnknown:
    		return context.getString(R.string.SErrorMessagePackingIsUnknown); //. ->
    		
    	case ErrorCode_MessageDecryptionIsFailed:
    		return context.getString(R.string.SErrorMessageDecryptionIsFailed); //. ->
    		
    	case ErrorCode_MessageUnpackingIsFailed:
    		return context.getString(R.string.SErrorMessageUnpackingIsFailed); //. ->
    		
    	case ErrorCode_MessageIntegrityCheckIsFailed:
    		return context.getString(R.string.SErrorMessageIntegrityCheckIsFailed); //. ->
    		
    	case ErrorCode_MessageIsOutOfMemory:
    		return context.getString(R.string.SErrorMessageIsOutOfMemory); //. ->
    		
        //. error codes - service operation errors (ErrorBase: -3000)
    	case ErrorCode_OperationError:
    		return context.getString(R.string.SErrorOperationUnknownError); //. ->
    		
    	case ErrorCode_OperationUnknownService:
    		return context.getString(R.string.SErrorOperationServiceIsUnknown); //. ->
    		
    	case ErrorCode_OperationServiceIsNotSupported:
    		return context.getString(R.string.SErrorOperationServiceIsNotSupported); //. ->
    		
    	case ErrorCode_OperationUserAccessIsDenied:
    		return context.getString(R.string.SErrorOperationAccessIsDenied); //. ->
    		
    	case ErrorCode_OperationSessionIsChanged:
    		return context.getString(R.string.SErrorOperationSessionIsChanged); //. ->
    		
    	case ErrorCode_OperationPollingTimeout:
    		return context.getString(R.string.SErrorOperationPollingTimeout); //. ->
    		
    	case ErrorCode_OperationProcessingTimeout:
    		return context.getString(R.string.SErrorOperationProcessingTimeout); //. ->
    		
    	case ErrorCode_OperationPoolIsBusy:
    		return context.getString(R.string.SErrorOperationPollIsBusy); //. ->
    		
        //. error codes - device control service operation errors (ErrorBase: -4000)
        //. error codes - object service operation errors (ErrorBase: -5000)
    	case ErrorCode_ObjectOperationError:
    		return context.getString(R.string.SErrorObjectOperationUnknownError); //. ->
    		
    	case ErrorCode_ObjectOperationUnknownObject:
    		return context.getString(R.string.SErrorOperationObjectIsUnknown); //. ->
    		
    	case ErrorCode_ObjectOperationObjectUserIsBad:
    		return context.getString(R.string.SErrorObjectOperationUserIsIncorrect); //. ->
    		
    	case ErrorCode_ObjectOperationObjectIsChanged:
    		return context.getString(R.string.SErrorObjectIsChangedDuringTheOperation); //. ->
    		
    	case ErrorCode_ObjectOperationObjectIsOffline:
    		return context.getString(R.string.SErrorObjectIsOffline); //. ->
    		
        //. device service operation errors (ErrorBase: -6000)
        //.
        //. object-device component service operation errors (ErrorBase: -7000)
        //.
        //. object component service operation errors (ErrorBase: -8000)
    	case ErrorCode_ObjectComponentOperationError:
    		return context.getString(R.string.SErrorComponentOperationUnknownError); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_ComponentObjectModelIsNotExist:
    		return context.getString(R.string.SErrorComponentModelIsNotExist); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_AddressIsNotFound:
    		return context.getString(R.string.SErrorComponentAddressIsNotFound); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_AddressIsDisabled:
    		return context.getString(R.string.SErrorComponentAddressIsDisabled); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_SetValueError:
    		return context.getString(R.string.SErrorComponentValueSetError); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_GetValueError:
    		return context.getString(R.string.SErrorComponentValueGetError); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_SetGetValueError:
    		return context.getString(R.string.SErrorComponentValueSetGetError); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_GetSetValueError:
    		return context.getString(R.string.SErrorComponentValueGetSetError); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_ComponentObjectModelIsNotSupported:
    		return context.getString(R.string.SErrorComponentModelIsNotSupported); //. ->
    		
    	case ErrorCode_ObjectComponentOperation_ValueDataIsInvalid:
    		return context.getString(R.string.SErrorComponentValueDataIsInvalid); //. ->
    		
    	default:
    		return context.getString(R.string.SErrorCode)+Integer.toString(Code); //. ->
    	}
    }
    
    //. connection routines
    public static int Connection_DataWaitingInterval = 60; //. seconds
    
    private static int Connection_WaitForData(InputStream Connection, int Seconds, Context context) throws OperationException,InterruptedException
    {
        for (int I = 0; I < Seconds; I++)
        {
            for (int J = 0; J < 40; J++)
            {
                try
                {
                    int R = Connection.available();
                    if (R > 0) return R; //. ->
                }
                catch (Exception E)
                {
                	if (context != null)
                		throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,context); //. =>
                	else
                		throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,"Connection_WaitForData: connection is closed unexpectedly, "+E.getMessage()); //. =>
                }
                Thread.sleep(25);
            }
        }
    	if (context != null)
    		throw new OperationException(ErrorCode_ConnectionReadWriteTimeOut,context); //. =>
    	else
    		throw new OperationException(ErrorCode_ConnectionReadWriteTimeOut,"Connection_WaitForData: timeout is expired in "+Integer.toString(Seconds)+" seconds"); //. =>
    }
    
    public static boolean Connection_DataIsArrived(InputStream Connection) throws OperationException
    {
        boolean Result;
	try
        {
            Result = (Connection.available() > 0);
        }
        catch (Exception E)
        {
            throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,"Connection_DataIsArrived: connection is closed unexpectedly, "+E.getMessage()); //. =>		
        }
        return Result;
    }

    public static void Connection_ReadData(InputStream Connection, byte[] Data, int WaitingInterval, Context context) throws OperationException,InterruptedException
    {
        try
        {
            int Size;
            int SummarySize = 0;
            int ReadSize,AvailableSize;
            while (SummarySize < Data.length)
            {
                ReadSize = Data.length-SummarySize;
                AvailableSize = Connection_WaitForData(Connection,WaitingInterval,context);
                if (AvailableSize < ReadSize)
                    ReadSize = AvailableSize;
                Size = Connection.read(Data,SummarySize,ReadSize);
                if (Size <= 0) { 
                	if (context != null)
                		throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,context); //. =>
                	else
                		throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,"Connection_ReadData: connection is closed unexpectedly"); //. =>
                }
                SummarySize += Size;
            }
        }
        catch (InterruptedException IE) {
        	if (context != null)
            	throw new OperationException(ErrorCode_ConnectionIsClosedByWorkerThreadTermination,context); //. =>
        	else
        		throw new OperationException(ErrorCode_ConnectionIsClosedByWorkerThreadTermination,"Connection_ReadData: connection is closed by worker thread termination"); //. =>
        }
        catch (OperationException OE)
        {
        	throw OE; //. =>
        }
        catch (Exception E)
        {
        	if (context != null)
                throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,context); //. =>		
        	else
        		throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,"Connection_ReadData: connection is closed unexpectedly, "+E.getMessage()); //. =>		
        }
    }
	
    public static void Connection_ReadData(InputStream Connection, byte[] Data, int WaitingInterval) throws OperationException,InterruptedException
    {
		Connection_ReadData(Connection,Data,WaitingInterval,null);
	}
    
	public static void Connection_ReadData(InputStream Connection, byte[] Data, Context context) throws OperationException,InterruptedException
    {
    	Connection_ReadData(Connection,Data,Connection_DataWaitingInterval/*data waiting interval*/,context);
    }

	public static void Connection_ReadData(InputStream Connection, byte[] Data) throws OperationException,InterruptedException
    {
    	Connection_ReadData(Connection,Data,null);
    }

    public static void Connection_CheckReadData(Socket Connection, InputStream ConnectionInputStream, byte[] Data, int CheckInterval) throws InterruptedIOException, OperationException,InterruptedException {
        try {
        	int LastTimeout = Connection.getSoTimeout();
        	try {
        		Connection.setSoTimeout(CheckInterval);
        		//.
                int Size;
                int SummarySize = 0;
                int ReadSize;
                while (SummarySize < Data.length) {
                    ReadSize = Data.length-SummarySize;
                    Size = ConnectionInputStream.read(Data,SummarySize,ReadSize);
                    if (Size <= 0) 
                    	throw new Exception("Connection_CheckReadData: connection is closed unexpectedly"); //. =>
                    SummarySize += Size;
                }
        	}
        	finally {
        		Connection.setSoTimeout(LastTimeout);
        	}
        }
        catch (InterruptedIOException IIOE) {
        	throw IIOE; //. =>
        }
        catch (Exception E) {
            throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,"Connection_CheckReadData: connection is closed unexpectedly, "+E.getMessage()); //. =>		
        }
    }
	
    public static void Connection_WriteData(OutputStream Connection, byte[] Data) throws OperationException
    {
        try
        {
            Connection.write(Data);
            //. if (RetSize != Data.length) throw new Exception("Connection_WriteData: connection was closed during write operation"); //. =>
        }
        catch (Exception E)
        {
            throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,"Connection_WriteData: connection was closed during write operation, "+E.getMessage()); //. =>
        }
    }
    
    private static void PackMessage(byte Packing,  TMessage Message, TIndex Origin) throws IOException,OperationException
    {
        //. packing message
        switch (Packing)
        {
            case PackingMethod_None: //. do nothing
                break; //. >
                
            case PackingMethod_ZLIBZIP: 
            {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try
                {
                    ZOutputStream out = new ZOutputStream(bos,JZlib.Z_BEST_SPEED);
                    try
                    {
                        out.write(Message.Array,Origin.Value,Message.Array.length-Origin.Value);
                    }
                    finally
                    {
                        out.close();
                    }
                    byte[] BA = bos.toByteArray();
                    byte[] Result = new byte[Origin.Value+BA.length];
                    System.arraycopy(Message.Array,0,Result,0,Origin.Value); 
                    System.arraycopy(BA,0,Result,Origin.Value,BA.length); 
                    Message.Array = Result;
                }
                finally
                {
                    bos.close();
                }
                break; //. >
            }
                
            default:
                throw new OperationException(ErrorCode_MessagePackingIsUnknown); //. =>
        }
        //. set Packing method
        Origin.Value = Origin.Value-1/*SizeOf(Packing)*/;
        Message.Array[Origin.Value] = Packing;
    }
    
    private static void EncryptMessage(byte Encryption, String UserPassword,  TMessage Message, TIndex Origin) throws OperationException
    {
        //. encrypting message
        switch (Encryption)
        {
            case EncryptionMethod_None: //. do nothing
            {
                break; //. >
            }
            
            case EncryptionMethod_SimpleByPassword:
            {
                int StartIdx = Origin.Value;
                byte[] UserPasswordArray;
                //.
                try
                {
                    UserPasswordArray = UserPassword.getBytes("windows-1251");
                }
                catch (Exception E)
                {
                    throw new OperationException(ErrorCode_Unknown,E.getMessage()); //. =>
                }
                //.
                if (UserPasswordArray.length > 0)
                {
                    int UserPasswordArrayIdx = 0;
                    for (int I = StartIdx; I < Message.Array.length; I++)
                    {
                        Message.Array[I] = (byte)(Message.Array[I]+UserPasswordArray[UserPasswordArrayIdx]);
                        UserPasswordArrayIdx++;
                        if (UserPasswordArrayIdx >= UserPasswordArray.length) UserPasswordArrayIdx = 0;
                    }
                }
                break; //. >
            }
            
            default:
                throw new OperationException(ErrorCode_MessageEncryptionIsUnknown); //. =>
        }
        //. set Encryption method
        Origin.Value = Origin.Value-1/*SizeOf(Encryption)*/;
        Message.Array[Origin.Value] = Encryption;
    }
    
    private static void DecryptMessage(String UserPassword, TMessage Message, TIndex Origin) throws OperationException
    {
        //. get Encryption method
        byte Encryption = Message.Array[Origin.Value]; Origin.Value+=1;
        //. decrypting message
        switch (Encryption)
        {
            case EncryptionMethod_None: //. do nothing
                break; //. >
                
            case EncryptionMethod_SimpleByPassword:
            {
                int StartIdx = Origin.Value;
                byte[] UserPasswordArray;
                //.
                try
                {
                    UserPasswordArray = UserPassword.getBytes("windows-1251");
                }
                catch (Exception E)
                {
                    throw new OperationException(ErrorCode_Unknown,E.getMessage()); //. =>
                }
                //.
                if (UserPasswordArray.length > 0)
                {
                    int UserPasswordArrayIdx = 0;
                    for (int I = StartIdx; I < Message.Array.length; I++)
                    {
                        Message.Array[I] = (byte)(Message.Array[I]-UserPasswordArray[UserPasswordArrayIdx]);
                        UserPasswordArrayIdx++;
                        if (UserPasswordArrayIdx >= UserPasswordArray.length) UserPasswordArrayIdx = 0;
                    }
                }
                break; //. >
            }
			
            default:
                throw new OperationException(ErrorCode_MessageEncryptionIsUnknown); //. =>
        }
    }
    
    private static void UnPackMessage(TMessage Message, TIndex Origin) throws OperationException 
    {
        //. get Packing method
        byte Packing = Message.Array[Origin.Value]; Origin.Value+=1;
        //. un-packing message
        switch (Packing)
        {
            case PackingMethod_None: //. do nothing
                break; //. >
            
            default:
                throw new OperationException(ErrorCode_MessagePackingIsUnknown); //. =>
        }
    }

    //. Big-Endian number's conversion
    public static byte[] ConvertInt16ToBEByteArray(short V) throws IOException
    {
    	byte[] Result = new byte[2];
    	Result[0] = (byte)(V & 0xff);
    	Result[1] = (byte)(V >>> 8 & 0xff);
    	return Result;
    }
    
    public static short ConvertBEByteArrayToInt16(byte[] V, int Idx) throws IOException
    {
		return (short)(((V[Idx+1] & 0xFF) << 8)+(V[Idx] & 0xFF));
    }
    
    public static byte[] ConvertInt32ToBEByteArray(int V) throws IOException
    {
    	byte[] Result = new byte[4];
    	Result[0] = (byte)(V & 0xff);
    	Result[1] = (byte)(V >> 8 & 0xff);
    	Result[2] = (byte)(V >> 16 & 0xff);
    	Result[3] = (byte)(V >>> 24);
        return Result;
    }
    
    public static int ConvertBEByteArrayToInt32(byte[] V, int Idx) throws IOException
    {
		return ((V[Idx+3] << 24)+((V[Idx+2] & 0xFF) << 16)+((V[Idx+1] & 0xFF) << 8)+(V[Idx] & 0xFF));
    }
    
    public static byte[] ConvertDoubleToBEByteArray(double V) throws IOException
    {
        byte[] R = new byte[8];
        ByteBuffer.wrap(R).putDouble(V);
        //.
        byte E;
        E = R[0]; R[0] = R[1]; R[1] = E;
        E = R[2]; R[2] = R[3]; R[3] = E;
        E = R[4]; R[4] = R[5]; R[5] = E;
        E = R[6]; R[6] = R[7]; R[7] = E;
        //.
        E = R[0]; R[0] = R[2]; R[2] = E; E = R[1]; R[1] = R[3]; R[3] = E;
        E = R[4]; R[4] = R[6]; R[6] = E; E = R[5]; R[5] = R[7]; R[7] = E;
        //.
        E = R[0]; R[0] = R[4]; R[4] = E; E = R[1]; R[1] = R[5]; R[5] = E; E = R[2]; R[2] = R[6]; R[6] = E; E = R[3]; R[3] = R[7]; R[7] = E;
        //.
        return R;
    }
    
    public static double ConvertBEByteArrayToDouble(byte[] V, int Idx) throws IOException
    {
        byte[] BA = {V[Idx+7],V[Idx+6],V[Idx+5],V[Idx+4],V[Idx+3],V[Idx+2],V[Idx+1],V[Idx+0]};
        return ByteBuffer.wrap(BA).getDouble();
    }
    
    public static byte[] ConvertLongToBEByteArray(long V) throws IOException
    {
        byte[] R = new byte[8];
        ByteBuffer.wrap(R).putLong(V);
        //.
        byte E;
        E = R[0]; R[0] = R[1]; R[1] = E;
        E = R[2]; R[2] = R[3]; R[3] = E;
        E = R[4]; R[4] = R[5]; R[5] = E;
        E = R[6]; R[6] = R[7]; R[7] = E;
        //.
        E = R[0]; R[0] = R[2]; R[2] = E; E = R[1]; R[1] = R[3]; R[3] = E;
        E = R[4]; R[4] = R[6]; R[6] = E; E = R[5]; R[5] = R[7]; R[7] = E;
        //.
        E = R[0]; R[0] = R[4]; R[4] = E; E = R[1]; R[1] = R[5]; R[5] = E; E = R[2]; R[2] = R[6]; R[6] = E; E = R[3]; R[3] = R[7]; R[7] = E;
        //.
        return R;
    }
    
    public static long ConvertBEByteArrayToLong(byte[] V, int Idx) throws IOException
    {
        byte[] BA = {V[Idx+7],V[Idx+6],V[Idx+5],V[Idx+4],V[Idx+3],V[Idx+2],V[Idx+1],V[Idx+0]};
        return ByteBuffer.wrap(BA).getLong();
    }
    
    //. server message parameters
    public static final int MessageOrigin = 4/*SizeOf(UserID)*/+1/*SizeOf(Encryption)*/+1/*Packing*/;
    public static final int MessageProtocolSize = 4/*SizeOf(UserID)*/+1/*SizeOf(Encryption)*/+1/*Packing*/+2/*SizeOf(Session)*/+4/*SizeOf(CRC)*/;
    public static final int MessageProtocolSuffixSize = 2/*SizeOf(Session)*/+4/*SizeOf(CRC)*/;
    public static final int MessageMaxSize = 1024*1024*10;
    public static final int MessageSizeForPacking = 256;
    public static final int MessageSizeForNoPacking = 1024*256; //. no packing due to a packer out of memmory error
    public static final byte MessageNoPacking = PackingMethod_None;
    public static final byte MessageDefaultPacking = MessageNoPacking;
    public static final byte MessageNormalPacking = PackingMethod_ZLIBZIP;
    public static final byte MessageDefaultEncryption = EncryptionMethod_SimpleByPassword;
    
    public static void SetMessageSession(short Session, TMessage Message) throws IOException
    {
        int Idx = Message.Array.length-4/*SizeOf(CRC)*/-2/*SizeOf(Session)*/;
        byte[] BA = ConvertInt16ToBEByteArray(Session);
        System.arraycopy(BA,0,Message.Array,Idx,BA.length); 
    }
    
    public static short GetMessageSession(TMessage Message) throws IOException
    {
        int Idx = Message.Array.length-4/*SizeOf(CRC)*/-2/*SizeOf(Session)*/;
        return (ConvertBEByteArrayToInt16(Message.Array,Idx));
    }
    
    public static void SetMessageIntegrity(TMessage Message, int Origin) throws IOException
    {
        //. calculate message CRC code and put it into the last dword of message
        int CRC = 0;
        int V;
        int Idx  = Origin;
        while (Idx < (Message.Array.length-4/*CRC*/))
        {
            V = (int)(Message.Array[Idx] & 0x000000FF);
            CRC = (((CRC+V) << 1)^V);
            //.
            Idx++;
        }
        byte[] CRCArray = ConvertInt32ToBEByteArray(CRC);
        System.arraycopy(CRCArray,0,Message.Array,Idx,CRCArray.length); 
    }
    
    public static void CheckMessageIntegrity(TMessage Message, int Origin) throws IOException,OperationException
    {
        //. check message body integrity
        int CRC = 0;
        int V;
        int Idx  = Origin;
        while (Idx < (Message.Array.length-4/*CRC*/))
        {
            V = (int)(Message.Array[Idx] & 0x000000FF);
            CRC = (((CRC+V) << 1)^V);
            //.
            Idx++;
        }
        int ResCRC = ConvertBEByteArrayToInt32(Message.Array,Idx);
        if (CRC != ResCRC)
            throw new OperationException(ErrorCode_MessageIntegrityCheckIsFailed,"message integrity checking is failed"); //. =>
    }
    
    public static short GetMessageSID(byte[] Message, TIndex Idx) throws IOException
    {
        short Result = ConvertBEByteArrayToInt16(Message,Idx.Value); Idx.Value+=2;
        return Result;
    }
    
    public static void EncodeMessage(short Session, byte Packing, int UserID, String UserPassword, byte Encryption,  TMessage Message) throws IOException,OperationException
    {
        //. set message operation session
        SetMessageSession(Session,Message);
        //. skip descriptors in message
        TIndex Idx = new TIndex(4/*SizeOf(UseID)*/+1/*SizeOf(Encryption)*/+1/*SizeOf(Packing)*/);
        //. set message integrity
        SetMessageIntegrity(/*ref*/ Message, /*ref*/ Idx.Value);
        //. pack message
        PackMessage(Packing, /*ref*/ Message, /*ref*/ Idx);
        //. encrypt message
        EncryptMessage(Encryption,UserPassword, /*ref*/ Message, /*ref*/ Idx);
        //. set UserID
        Idx.Value = Idx.Value-4/*SizeOf(UserID)*/;
        byte[] BA = ConvertInt32ToBEByteArray(UserID);
        System.arraycopy(BA,0,Message.Array,Idx.Value,BA.length); 
    }
    
    public static void DecodeMessage(int UserID, String UserPassword, TOperationSession Session, TMessage Message, TIndex Origin) throws IOException,OperationException
    {
        TIndex Idx = new TIndex(0);
        //. get UserID,Encryption method and fetch UserPassword
        int _UserID = ConvertBEByteArrayToInt32(Message.Array,Idx.Value); Idx.Value+=4;
        if (_UserID != UserID)
            throw new OperationException(ErrorCode_MessageUserIsUnknown,"user with ID: "+Integer.toString(_UserID)+" is not native"); //. =>
        //. decrypt message
        DecryptMessage(UserPassword,  /*ref*/ Message, /*ref*/ Idx);
        //. unpack message
        UnPackMessage(/*ref*/ Message, /*ref*/ Idx);
        //. check message integrity
        CheckMessageIntegrity(Message,Idx.Value);
        //.
        Origin.Value = Idx.Value;
        //. get message operation session
        Session.ID = GetMessageSession(Message);
    }
    
    public static void SendMessage(TConnectorModule Connector, int UserID, String UserPassword, OutputStream ConnectionOutputStream, short Session, byte[] Message) throws OperationException,IOException
    {
        //. encode sending message
        byte MessagePacking = MessageDefaultPacking;
        if (Message.length > MessageSizeForPacking) {
            if (Message.length > MessageSizeForNoPacking)
                MessagePacking = MessageNoPacking;
            else
                MessagePacking = MessageNormalPacking;
        }
        TMessage _Message = new TMessage(Message);
        EncodeMessage(Session,MessagePacking,UserID,UserPassword,MessageDefaultEncryption, /*ref*/ _Message);
        //. send message
        byte[] MessageSizeArray = ConvertInt32ToBEByteArray(_Message.Array.length);
        byte[] MessageToSend = new byte[MessageSizeArray.length+_Message.Array.length];
        System.arraycopy(MessageSizeArray,0,MessageToSend,0,MessageSizeArray.length);
        System.arraycopy(_Message.Array,0,MessageToSend,MessageSizeArray.length,_Message.Array.length);
        Connection_WriteData(ConnectionOutputStream,MessageToSend);
        ConnectionOutputStream.flush();
        //. set connector new checkpoint base
        if (Connector.ConnectionOutputStream == ConnectionOutputStream)
        	Connector.SetCheckpointBase();
    }
    
    public static byte[] ReceiveMessageWithinTime(int UserID, String UserPassword, InputStream ConnectionInputStream, OutputStream ConnectionOutputStream, TOperationSession Session, TIndex Origin, int WaitingInterval) throws OperationException,InterruptedException,IOException {
    	//. read message descriptor
        byte[] MessageSizeArray = new byte[4];
        int MessageSize = 0;
        do
        {
            Connection_ReadData(ConnectionInputStream,MessageSizeArray,WaitingInterval);
            MessageSize = ConvertBEByteArrayToInt32(MessageSizeArray,0);
        } while (MessageSize == 0); //. skip connection checkpoints
        //. read message
		///- boolean flDataRead = false;
        try {
            byte[] Message = new byte[MessageSize];
            Connection_ReadData(ConnectionInputStream,Message,WaitingInterval);
            ///- flDataRead = true;
            TMessage _Message = new TMessage(Message);
            DecodeMessage(UserID,UserPassword, /*out*/ Session, _Message, Origin);
            return _Message.Array; //. ->
        }
        catch (OutOfMemoryError E) {
        	//. send OutOfMemoryConnection descriptor
            byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(TGeographServerServiceOperation.Descriptor_MessageIsOutOfMemory);
            ConnectionOutputStream.write(BA);
            ConnectionOutputStream.flush();
            //.
            throw new OperationException(ErrorCode_DataOutOfMemory); //. =>
        	//. skip message
        	/*///- if (!flDataRead) { 
                int Size;
                int SummarySize = 0;
                int ReadSize;
                byte[] SkipBuffer = new byte[1024];
                while (SummarySize < MessageSize)
                {
                    ReadSize = MessageSize-SummarySize;
                    Size = ConnectionInputStream.read(SkipBuffer,SummarySize,ReadSize);
                    if (Size <= 0) throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,"ReceiveMessageWithinTime: connection is closed unexpectedly"); //. =>
                    SummarySize += Size;
                }
        	}
        	//. read next message
            Session.ID = 0;
            Origin.Value = 0;
            return ReceiveMessageWithinTime(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream, Session, Origin, WaitingInterval);*/
        }
    }
        
    public static byte[] ReceiveMessage(int UserID, String UserPassword, InputStream ConnectionInputStream, OutputStream ConnectionOutputStream, TOperationSession Session, TIndex Origin) throws OperationException,InterruptedException,IOException {
        return ReceiveMessageWithinTime(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream,Session,Origin,Connection_DataWaitingInterval);
    }

    public static byte[] CheckReceiveMessageWithinTime(int UserID, String UserPassword, Socket Connection, InputStream ConnectionInputStream, OutputStream ConnectionOutputStream, TOperationSession Session, TIndex Origin, int CheckInterval, int WaitingInterval) throws OperationException,InterruptedException,IOException { //. similar as for ReceiveMessageWithinTime() 
    	//. read message descriptor
        byte[] MessageSizeArray = new byte[4];
        int MessageSize = 0;
        try {
            do
            {
                Connection_CheckReadData(Connection,ConnectionInputStream,MessageSizeArray,CheckInterval);
                MessageSize = ConvertBEByteArrayToInt32(MessageSizeArray,0);
            } while (MessageSize == 0); //. skip connection checkpoints
        }
        catch (InterruptedIOException IIOE) {
        	//. timeout occurs
        	return null; //. -> 
        }
        //. read message
		///- boolean flDataRead = false;
        try {
            byte[] Message = new byte[MessageSize];
            Connection_ReadData(ConnectionInputStream,Message,WaitingInterval);
            ///- flDataRead = true;
            TMessage _Message = new TMessage(Message);
            DecodeMessage(UserID,UserPassword, /*out*/ Session, _Message, Origin);
            return _Message.Array; //. ->
        }
        catch (OutOfMemoryError E) {
        	//. send OutOfMemoryConnection descriptor
            byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(TGeographServerServiceOperation.Descriptor_MessageIsOutOfMemory);
            ConnectionOutputStream.write(BA);
            ConnectionOutputStream.flush();
            //.
            throw new OperationException(ErrorCode_DataOutOfMemory); //. =>
        	//. skip message
        	/*///- if (!flDataRead) { 
                int Size;
                int SummarySize = 0;
                int ReadSize;
                byte[] SkipBuffer = new byte[1024];
                while (SummarySize < MessageSize)
                {
                    ReadSize = MessageSize-SummarySize;
                    Size = ConnectionInputStream.read(SkipBuffer,SummarySize,ReadSize);
                    if (Size <= 0) throw new OperationException(ErrorCode_ConnectionIsClosedUnexpectedly,"ReceiveMessageWithinTime: connection is closed unexpectedly"); //. =>
                    SummarySize += Size;
                }
        	}
        	//. read next message
            Session.ID = 0;
            Origin.Value = 0;
            return ReceiveMessageWithinTime(UserID,UserPassword,ConnectionInputStream,ConnectionOutputStream, Session, Origin, WaitingInterval);*/
        }
    }
        
    public static byte[] CheckReceiveMessage(int UserID, String UserPassword, Socket Connection, InputStream ConnectionInputStream, OutputStream ConnectionOutputStream, TOperationSession Session, TIndex Origin, int CheckInterval) throws OperationException,InterruptedException,IOException {
        return CheckReceiveMessageWithinTime(UserID,UserPassword,Connection,ConnectionInputStream,ConnectionOutputStream,Session,Origin,CheckInterval,Connection_DataWaitingInterval);
    }

    public static void SendResultCode(TConnectorModule Connector, int UserID, String UserPassword, OutputStream ConnectionOutputStream, short Session, int ResultCode) throws OperationException,IOException
    {
        byte[] BA = ConvertInt32ToBEByteArray(ResultCode);
        int MessageSize = (MessageProtocolSize+BA.length);
        byte[] Message = new byte[MessageSize];
        int Idx = MessageOrigin;
        System.arraycopy(BA,0,Message,Idx,BA.length); Idx+=BA.length;
        //. encode and send message
        SendMessage(Connector,UserID,UserPassword,ConnectionOutputStream,Session,Message);        
    }
    
    private static short _SessionID = 0;
    synchronized public static short NewSessionID()
    {
        _SessionID++;
        return _SessionID;
    }
    
    protected TConnectorModule Connector;
    public Date 				TimeStamp;
    public String 				Name = this.getClass().getName();
    protected int 				UserID;
    protected String 			UserPassword;
    public TOperationSession 	Session = new TOperationSession();
    
    public TGeographServerServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword)
    {
        super();
        Connector = pConnector;
        UserID = pUserID;
        UserPassword = pUserPassword;
        TimeStamp = Calendar.getInstance().getTime();
    }
    
    public void CheckResponseMessage(byte[] Message, TIndex Idx) throws IOException,OperationException
    {
        int ResultCode = ConvertBEByteArrayToInt32(Message,Idx.Value); Idx.Value+=4;
        if (ResultCode < 0)
            throw new OperationException(ResultCode,"operation error, code: "+Integer.toString(ResultCode)); //. =>
    }        
}
