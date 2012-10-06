/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.TConnectorModule;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TObjectServiceOperation extends TGeographServerServiceOperation
{
        public static void SetMessageObjectID(int ObjectID, byte[] Message,  TIndex Idx) throws IOException
        {
            byte[] BA = ConvertInt32ToBEByteArray(ObjectID);
            System.arraycopy(BA,0,Message,Idx.Value,BA.length); Idx.Value+=4;
        }
        
        public static int GetMessageObjectID(byte[] Message,  TIndex Idx) throws IOException
        {
            int Result = ConvertBEByteArrayToInt32(Message,Idx.Value); Idx.Value+=4;
            return Result;
        }
        
        
        protected int ObjectID;
        
        public TObjectServiceOperation(TConnectorModule pConnector, int pUserID, String pUserPassword, int pObjectID)
        {
            super(pConnector,pUserID,pUserPassword);
            ObjectID = pObjectID;
        }
}
