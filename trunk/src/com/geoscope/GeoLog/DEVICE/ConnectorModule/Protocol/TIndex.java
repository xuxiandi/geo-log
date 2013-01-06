/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol;

/**
 *
 * @author ALXPONOM
 */

public class TIndex
{
    public int Value;
    
    public TIndex() {
        Value = 0;
    }
    
    public TIndex(int pValue) {
        Value = pValue;
    }
    
    public void Reset() {
    	Value = 0;
    }
}
    
