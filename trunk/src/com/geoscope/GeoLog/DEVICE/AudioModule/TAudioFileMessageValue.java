/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

/**
 *
 * @author ALXPONOM
 */
public class TAudioFileMessageValue extends TComponentValue {
	
    public double 	TimeStamp;
    public int		FileID;
    public short	DestinationID;
    public short	Volume;
    public short	RepeatCount;
    public short	RepeatInterval;
    //.
    private TAudioModule AudioModule;

    public TAudioFileMessageValue(TAudioModule pAudioModule) {
    	AudioModule = pAudioModule;
    }
    
    public TAudioFileMessageValue(TAudioModule pAudioModule, byte[] BA, TIndex Idx) throws IOException, OperationException {
    	AudioModule = pAudioModule;
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TAudioFileMessageValue(TAudioModule pAudioModule, double pTimeStamp, int pCommandID, short pDestinationID, short pVolume, short pRepeatCount, short pRepeatInterval) {
    	AudioModule = pAudioModule;
    	//.
        FileID = pCommandID;
        DestinationID = pDestinationID;
        Volume = pVolume;
        RepeatCount = pRepeatCount;
        RepeatInterval = pRepeatInterval;
        //.
        flSet = true;
    }
    
    @Override
    public synchronized void Assign(TComponentValue pValue) {
    	TAudioFileMessageValue Src = (TAudioFileMessageValue)pValue;
        FileID = Src.FileID;
        DestinationID = Src.DestinationID;
        Volume = Src.Volume;
        RepeatCount = Src.RepeatCount;
        RepeatInterval = Src.RepeatInterval;
        //.
        super.Assign(pValue);
    }
    
    @Override
    public synchronized TComponentValue getValue() {
        return new TAudioFileMessageValue(AudioModule, TimeStamp,FileID,DestinationID,Volume,RepeatCount,RepeatInterval);
    }
    
    @Override
    public synchronized boolean IsValueTheSame(TComponentValue AValue) {
    	TAudioFileMessageValue V = (TAudioFileMessageValue)AValue.getValue();
        return ((TimeStamp == V.TimeStamp) && (FileID == V.FileID) && (DestinationID == V.DestinationID) && (Volume == V.Volume) && (RepeatCount == V.RepeatCount) && (RepeatInterval == V.RepeatInterval));
    }
    
    public synchronized void setValues(double pTimeStamp, int pCommandID, short pDestinationID, short pVolume, short pRepeatCount, short pRepeatInterval) {
        FileID = pCommandID;
        DestinationID = pDestinationID;
        Volume = pVolume;
        RepeatCount = pRepeatCount;
        RepeatInterval = pRepeatInterval;
        //.
        flSet = true;
    }
    
    public synchronized void setTimeStamp(double pTimeStamp) {
        TimeStamp = pTimeStamp;
        //.
        flSet = true;
    }
    
    @Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
        TimeStamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA,Idx.Value); Idx.Value+=8;
        FileID = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
        DestinationID = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        Volume = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        RepeatCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        RepeatInterval = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        //. play message
        for (int I = 0; I < RepeatCount; I++) {
            AudioModule.AudioFiles_Play(FileID, DestinationID, Volume);
            if (RepeatInterval > 0)
				try {
					Thread.sleep(RepeatInterval);
				} catch (InterruptedException e) {
				}
        }
        //.
        super.FromByteArray(BA,/*ref*/ Idx);
    }
    
    @Override
    public synchronized byte[] ToByteArray() throws IOException {
        byte[] Result = new byte[20];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(TimeStamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(FileID);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(DestinationID);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(Volume);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(RepeatCount);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(RepeatInterval);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        return Result;
    }
}
