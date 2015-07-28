package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Audio.VC;

import java.util.HashMap;

import android.annotation.SuppressLint;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.TInternalControlsModule;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.TStreamChannel;

public class TVCChannel extends TStreamChannel {

	public static final String TypeID = "Audio.VC";
		
	public static class TMyProfile extends TChannel.TProfile {
		
	}
	
	
	public HashMap<Integer, Integer> DataIndexes = null;
	
	public TVCChannel(TInternalControlsModule pInternalControlsModule, int pID, Class<?> ChannelProfile) throws Exception {
		super(pInternalControlsModule, pID, ChannelProfile);
	}
	
	public TVCChannel(TInternalControlsModule pInternalControlsModule, int pID) throws Exception {
		super(pInternalControlsModule, pID, TMyProfile.class);
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}

	@SuppressLint("UseSparseArrays")
	@Override
	public void Parse() throws Exception {
		DataIndexes = null;
		//.
		TConfigurationParser CP = new TConfigurationParser(Configuration);
		if (CP.CoderConfiguration != null) { 
			if (CP.CoderConfiguration.length > 1) {
				DataIndexes = new HashMap<Integer, Integer>();
				for (int I = 1; I < CP.CoderConfiguration.length; I++) {
					int Index = Integer.parseInt(CP.CoderConfiguration[I]);
					DataIndexes.put(I, (Index-1));
				}
			}
		}
	}

	public void DataType_SetValue(TDataType DataType) throws Exception {
		Integer I;
		if (DataIndexes != null)
			I = DataIndexes.get(DataType.Index);
		else
			I = DataType.Index;
		if (I == null)
			return; //. ->
		int V = ((Double)DataType.ContainerType.GetValue()).intValue();
	}
}
