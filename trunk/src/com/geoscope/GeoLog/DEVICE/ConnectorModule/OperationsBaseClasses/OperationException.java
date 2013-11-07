/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import android.content.Context;


/**
 *
 * @author ALXPONOM
 */

public class OperationException extends Exception
{
	private static final long serialVersionUID = 1L;
	
	public int Code = TGeographServerServiceOperation.ErrorCode_Unknown;
    
    public OperationException(int pCode)
    {
        super("");
        Code = pCode;
    }
    
    public OperationException(int pCode, Context context)
    {
        super(TGeographServerServiceOperation.ErrorCode_ToString(pCode,context));
        Code = pCode;
    }
    
    public OperationException(int pCode, String pMessage)
    {
        super(pMessage);
        Code = pCode;
    }
    
    public boolean IsMessageError() 
    {
    	return ((Code == TGeographServerServiceOperation.ErrorCode_MessageError) || (Code == TGeographServerServiceOperation.ErrorCode_MessageUserIsUnknown) || (Code == TGeographServerServiceOperation.ErrorCode_MessageUserIsChanged) || (Code == TGeographServerServiceOperation.ErrorCode_MessageEncryptionIsUnknown) || (Code == TGeographServerServiceOperation.ErrorCode_MessagePackingIsUnknown) || (Code == TGeographServerServiceOperation.ErrorCode_MessageDecryptionIsFailed) || (Code == TGeographServerServiceOperation.ErrorCode_MessageUnpackingIsFailed) || (Code == TGeographServerServiceOperation.ErrorCode_MessageUnpackingIsFailed) || (Code == TGeographServerServiceOperation.ErrorCode_MessageIsOutOfMemory));
    }
    
    public boolean IsConnectionError() {
        return ((Code == TGeographServerServiceOperation.ErrorCode_ConnectionError) || (Code == TGeographServerServiceOperation.ErrorCode_ConnectionReadWriteTimeOut) || (Code == TGeographServerServiceOperation.ErrorCode_ConnectionIsClosedGracefully) || (Code == TGeographServerServiceOperation.ErrorCode_ConnectionIsClosedUnexpectedly) || (Code == TGeographServerServiceOperation.ErrorCode_ConnectionNodeIsOutOfMemory));
    }
    
    public boolean IsConnectionWorkerThreadError() {
    	return (Code == TGeographServerServiceOperation.ErrorCode_ConnectionIsClosedByWorkerThreadTermination);
    }
    
    public boolean IsCommunicationError()
    {
        return (IsMessageError() || IsConnectionError() || IsConnectionWorkerThreadError());
    }

}
    
