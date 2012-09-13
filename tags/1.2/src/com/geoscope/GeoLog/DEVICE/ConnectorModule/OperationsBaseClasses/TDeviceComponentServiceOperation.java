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

public class TDeviceComponentServiceOperation extends TComponentServiceOperation
{
    public TDeviceComponentServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID, short pSession, short[] pSubAddress)
    {
        super(pConnector,pUserID,pUserPassword,pObjectID,pSubAddress);
        Session.ID = pSession;
    }
    
    public int ProcessIncomingOperation(short OperationSession, byte[] PreambleMessage, TIndex Index, InputStream ConnectionInputStream, OutputStream ConnectionOutputStream) throws OperationException,IOException
    {
        return 0;
    }
}