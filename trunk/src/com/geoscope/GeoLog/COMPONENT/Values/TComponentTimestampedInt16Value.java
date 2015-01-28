/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT.Values;

import java.io.IOException;

import org.w3c.dom.Node;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.GeoLog.COMPONENT.TComponent;
import com.geoscope.GeoLog.COMPONENT.TComponentTimestampedValue;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */

public class TComponentTimestampedInt16Value extends TComponentTimestampedValue
{
    public static final int ValueSize = 10;
    
    public short 	Value;
    
	public TComponentTimestampedInt16Value(TComponent pOwner, int pID, String pName) {
		super(pOwner, pID, pName);
	}

    public TComponentTimestampedInt16Value()
    {
    	Timestamp = 0.0;
        Value = 0;
    }
    
    public TComponentTimestampedInt16Value(double pTimestamp, short pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }

    public TComponentTimestampedInt16Value(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public synchronized void SetValue(double pTimestamp, short pValue)
    {
    	Timestamp = pTimestamp;
        Value = pValue;
        flSet = true;
    }
       
    public synchronized short GetValue()
    {
        return Value;
    }
       
    public synchronized void Assign(TComponentValue pValue)
    {
        TComponentTimestampedInt16Value Src = (TComponentTimestampedInt16Value)pValue.getValue();
        Timestamp = Src.Timestamp;
        Value = Src.Value;
        super.Assign(pValue);
    }
       
    public synchronized TComponentValue getValue()
    {
        return new TComponentTimestampedInt16Value(Timestamp,Value);
    }
       
    public synchronized boolean IsValueTheSame(TComponentValue AValue)
    {
        TComponentTimestampedInt16Value V = (TComponentTimestampedInt16Value)AValue.getValue();
        return ((V.Timestamp == Timestamp) && (V.Value == Value));
    }
    
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
    	Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
    	Value = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA, Idx.Value); Idx.Value+=2;
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized void FromXMLNode(Node node) {
    	Node ValueNode = TMyXML.SearchNode(node,Name);
    	String ValueString = ValueNode.getFirstChild().getNodeValue();
    	String[] SA = ValueString.split(";");
    	if (SA.length == 2) {
    		Timestamp = Double.parseDouble(SA[0]);
    		Value = Short.parseShort(SA[1]);
    	}
    }

    public synchronized byte[] ToByteArray() throws IOException
    {
    	byte[] Result = new byte[ValueSize];
    	byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
    	System.arraycopy(BA,0, Result,0, BA.length); 
    	BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(Value);
    	System.arraycopy(BA,0, Result,8, BA.length); 
        return Result;
    }
}
