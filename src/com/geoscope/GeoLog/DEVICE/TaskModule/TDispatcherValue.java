/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.TaskModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.Utils.TDataConverter;

/**
 * @author ALXPONOM
 */
public class TDispatcherValue extends TComponentValue {

    public TDispatcherValue() {
        flSet = true;
    }
    
    public TDispatcherValue(byte[] BA, TIndex Idx) throws IOException {
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized void Assign(TComponentValue pValue) {
        super.Assign(pValue);
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException {
    }
    
    @Override
    public synchronized byte[] ToByteArray() throws IOException {
        return (TDataConverter.ConvertInt32ToBEByteArray(0));
    }

    @Override
    public int ByteArraySize() throws Exception { 
        return (4/*SizeOf(DataSize)*/);
    }
}
