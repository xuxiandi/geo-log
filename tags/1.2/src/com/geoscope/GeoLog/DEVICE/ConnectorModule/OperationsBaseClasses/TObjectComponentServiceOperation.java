/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;

/**
 *
 * @author ALXPONOM
 */
public class TObjectComponentServiceOperation extends TComponentServiceOperation
{
    public TObjectComponentServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
        Session.ID = NewSessionID();
    }
    
    protected int ProcessOutgoingOperation(InputStream ConnectionInputStream, OutputStream ConnectionOutputStream) throws Exception
    {
        return 0;
    }

    protected int StartOutgoingOperation(OutputStream ConnectionOutputStream) throws Exception
    {
        return 0; 
    }

    protected int FinishOutgoingOperation(InputStream ConnectionInputStream, int CompletionTime) throws Exception
    {
        return 0;
    }
}
