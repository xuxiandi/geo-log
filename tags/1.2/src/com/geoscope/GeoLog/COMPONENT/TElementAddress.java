/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.COMPONENT;

/**
 *
 * @author ALXPONOM
 */
public class TElementAddress 
{
    public short[] Value;
    
    public TElementAddress()
    {
        Value = null;
    }
    
    public TElementAddress(short[] pValue)
    {
        if (pValue != null)
        {
            Value = new short[pValue.length];
            for (int I = 0; I < Value.length; I++)
                Value[I] = pValue[I];
        }
    }    
    
    public TElementAddress(int V0)
    {
        Value = new short[1];
        Value[0] = (short)V0;
    }
    
    public TElementAddress(int V0, int V1)
    {
        Value = new short[2];
        Value[0] = (short)V0;
        Value[1] = (short)V1;
    }
    
    public TElementAddress(int V0, int V1, int V2)
    {
        Value = new short[3];
        Value[0] = (short)V0;
        Value[1] = (short)V1;
        Value[2] = (short)V2;
    }
    
    public TElementAddress(int V0, int V1, int V2, int V3)
    {
        Value = new short[4];
        Value[0] = (short)V0;
        Value[1] = (short)V1;
        Value[2] = (short)V2;
        Value[3] = (short)V3;
    }
    
    public TElementAddress(int V0, int V1, int V2, int V3, int V4)
    {
        Value = new short[5];
        Value[0] = (short)V0;
        Value[1] = (short)V1;
        Value[2] = (short)V2;
        Value[3] = (short)V3;
        Value[4] = (short)V4;
    }
    
    public boolean IsAddressTheSame(short[] pAddress,/*out*/ TElementAddress SubAddress)
    {
        if (pAddress.length < Value.length)
            return false; //. ->
        for (int I = 0; I < Value.length; I++)
            if (pAddress[I] != Value[I])
                return false; //. ->
        int SubAddressCount = pAddress.length-Value.length;
        if (SubAddressCount > 0)
        {
            SubAddress.Value = new short[SubAddressCount];
            for (int I = 0; I < SubAddressCount; I++)
                SubAddress.Value[I] = pAddress[Value.length+I];
        }
        else
            SubAddress.Value = null;
        return true;
    }
    
   public boolean IsAddressTheSame(TElementAddress pAddress)
    {
        if (Value.length != pAddress.Value.length)
            return false; //. ->
        for (int I = 0; I < pAddress.Value.length; I++)
            if (pAddress.Value[I] != Value[I])
                return false; //. ->
        return true;
    }
    
    public TElementAddress AddRight(TElementAddress AddAddress)
    {
        int NewSize;
        if (AddAddress.Value == null)
            return this; //. ->
        else
            NewSize = AddAddress.Value.length;
        if (Value != null)
            NewSize+=Value.length;
        TElementAddress Result = new TElementAddress();
        Result.Value = new short[NewSize];
        if (Value != null)
            System.arraycopy(Value,0, Result.Value,0, Value.length);
        if (AddAddress.Value != null)
            System.arraycopy(AddAddress.Value,0, Result.Value,Value.length, AddAddress.Value.length);
        return Result;
    }
}
