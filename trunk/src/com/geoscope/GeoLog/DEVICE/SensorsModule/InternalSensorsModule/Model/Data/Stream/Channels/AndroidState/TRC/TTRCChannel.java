package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.TRC;

import java.io.IOException;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedTaggedDataContainerType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.IO.Log.TRollingLogFile;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

public class TTRCChannel extends TTLRChannel {

	public static final String TypeID = TTLRChannel.TypeID+"."+"AndroidState.TRC";
	
	public static final int DATATYPE_MESSAGE_ID = 1;
	
	public static final short TYPE_INFO 	= 1;
	public static final short TYPE_WARNING 	= 2;
	public static final short TYPE_ERROR 	= 3;
	
	public static final short TAG_NONE = 0;
	
	public static class TMyProfile extends TChannel.TProfile {
		
	}
	
	
	@SuppressWarnings("unused")
	private TMyProfile MyProfile;
	//.
	private TDataType Message;
	//.
	private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel _DestinationChannel = null;
	
	public TTRCChannel(TInternalSensorsModule pInternalSensorsModule, int pID) throws Exception {
		super(pInternalSensorsModule, pID, "System", TMyProfile.class);
		MyProfile = (TMyProfile)Profile;
		//.
		Kind = TChannel.CHANNEL_KIND_OUT;
		DataFormat = 0;
		Name = "Android tracing";
		Info = "log messages";
		Size = 8192;
		Configuration = "";
		Parameters = "";
		//.
		DataTypes = new TDataTypes();
		//.
		Message = DataTypes.AddItem(new TDataType(new TTimestampedTypedTaggedDataContainerType(),	"Message",	this, 1, "","", "")); 	
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	private TRollingLogFile.TListener Listener = new TRollingLogFile.TListener() {
		
		@Override
		public void DoOnInfo(String Source, String Info) {
			try {
				AddMessage(TYPE_INFO, TAG_NONE, Source+", "+Info);
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
		
		@Override
		public void DoOnWarning(String Source, String Warning) {
			try {
				AddMessage(TYPE_WARNING, TAG_NONE, Source+", "+Warning);
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
		
		@Override
		public void DoOnError(String Source, String Error) {
			try {
				AddMessage(TYPE_ERROR, TAG_NONE, Source+", "+Error);
			} catch (Exception E) {
				E.printStackTrace();
			}
		}
		
		private void AddMessage(short pType, short pTag, String pMessage) throws Exception {
			TTimestampedTypedTaggedDataContainerType.TValue Value = new TTimestampedTypedTaggedDataContainerType.TValue(OleDate.UTCCurrentTimestamp(), pType, pTag, pMessage.getBytes("utf-8"));
			//.
			synchronized (TTRCChannel.this) {
				Message.SetContainerTypeValue(Value);
				//.
				_DestinationChannel.DoOnData(Message);
			}
		}
	};
	
	public void StartSource() throws Exception {
		com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel __DestinationChannel = DestinationChannel_Get();
		if (!(__DestinationChannel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel))
        	throw new IOException("No destination channel"); //. ->
		_DestinationChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel)__DestinationChannel;
		//.
		InternalSensorsModule.Device.Log.SetListener(Listener);
	}

	@Override
	public void StopSource() throws Exception {
		InternalSensorsModule.Device.Log.SetListener(null);
		//.
		_DestinationChannel = null;
	}
	
	@Override
	public boolean IsActive() {
		return (InternalSensorsModule.Device.Log.GetListener() == Listener);
	}
}
