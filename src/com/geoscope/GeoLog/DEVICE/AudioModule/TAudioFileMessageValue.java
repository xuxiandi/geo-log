/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.AudioModule;

import java.io.IOException;

import com.geoscope.GeoLog.COMPONENT.TComponent;
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
    public String	FileName = null;
    public short	DestinationID;
    public short	Volume;
    public short	RepeatCount;
    public short	RepeatInterval;
    //.
    private TAudioModule AudioModule;

	public TAudioFileMessageValue(TComponent pOwner, int pID) {
    	super(pOwner, pID, "AudioFileMessage");
    	//.
    	flVirtualValue = true;
	}

	public TAudioFileMessageValue() {
	}

    public TAudioFileMessageValue(TAudioModule pAudioModule) {
    	AudioModule = pAudioModule;
    }
    
    public TAudioFileMessageValue(TAudioModule pAudioModule, byte[] BA, TIndex Idx) throws IOException, OperationException {
    	AudioModule = pAudioModule;
        FromByteArray(BA,/*ref*/ Idx);
    }
    
    public TAudioFileMessageValue(TAudioModule pAudioModule, double pTimeStamp, int pFileID, String pFileName, short pDestinationID, short pVolume, short pRepeatCount, short pRepeatInterval) {
    	AudioModule = pAudioModule;
    	//.
        FileID = pFileID;
        FileName = pFileName;
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
        FileName = Src.FileName;
        DestinationID = Src.DestinationID;
        Volume = Src.Volume;
        RepeatCount = Src.RepeatCount;
        RepeatInterval = Src.RepeatInterval;
        //.
        super.Assign(pValue);
    }
    
    @Override
    public synchronized TComponentValue getValue() {
        return new TAudioFileMessageValue(AudioModule, TimeStamp,FileID,FileName,DestinationID,Volume,RepeatCount,RepeatInterval);
    }
    
    @Override
    public synchronized boolean IsValueTheSame(TComponentValue AValue) {
    	TAudioFileMessageValue V = (TAudioFileMessageValue)AValue.getValue();
        return ((TimeStamp == V.TimeStamp) && (FileID == V.FileID) && ((FileName != null) && FileName.equals(V.FileName)) && (DestinationID == V.DestinationID) && (Volume == V.Volume) && (RepeatCount == V.RepeatCount) && (RepeatInterval == V.RepeatInterval));
    }
    
    public synchronized void setValues(double pTimeStamp, int pFileID, String pFileName, short pDestinationID, short pVolume, short pRepeatCount, short pRepeatInterval) {
        FileID = pFileID;
        FileName = pFileName;
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
    	short SS16 = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA, Idx.Value); Idx.Value+=2; 
    	if (SS16 > 0) {
    		FileName = new String(BA, Idx.Value,SS16, "windows-1251");
    		Idx.Value += SS16;
    	}
    	else
    		FileName = null;
        DestinationID = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        Volume = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        RepeatCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        RepeatInterval = TGeographServerServiceOperation.ConvertBEByteArrayToInt16(BA,Idx.Value); Idx.Value+=2;
        //.
        boolean flWaitForFinish = (RepeatCount > 0);
        //. play message
        RepeatCount = (short)Math.abs(RepeatCount);
        for (int I = 0; I < RepeatCount; I++) {
            AudioModule.AudioFiles_Play(FileID, FileName, DestinationID, Volume, flWaitForFinish);
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
    	short 	FileNameSize = 0;
    	byte[] 	FileNameBA = null;
    	if (FileName != null) {
    		FileNameBA = FileName.getBytes("windows-1251");
    		FileNameSize = (short)FileNameBA.length;
    	}
        byte[] Result = new byte[22+FileNameSize];
        int Idx = 0;
        byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(TimeStamp);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(FileID);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        BA = TGeographServerServiceOperation.ConvertInt16ToBEByteArray(FileNameSize);
        System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
        if (FileNameSize > 0) {
            System.arraycopy(FileNameBA,0,Result,Idx,FileNameBA.length); Idx+=FileNameBA.length;
        }
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
