/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TComponentTimestampedXMLDataValue extends TComponentTimestampedDataValue
{

    public TComponentTimestampedXMLDataValue()
    {
    	super();
    }
    
	public TComponentTimestampedXMLDataValue(TComponent pOwner, int pID, String pName, boolean pflDirectAccess) {
		super(pOwner, pID, pName);
	}

	public TComponentTimestampedXMLDataValue(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName, true);
	}

    public TComponentTimestampedXMLDataValue(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        super(BA,/*ref*/ Idx);
    }
    
    public TComponentTimestampedXMLDataValue(double pTimestamp, byte[] pValue)
    {
    	super(pTimestamp,pValue);
    }
}
