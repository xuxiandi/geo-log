package com.geoscope.GeoLog.DEVICE.SensorsModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetSensorsDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule.TUserMessagingModule.TUserMessaging;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.TSensorsMeters;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreamingAbstract;

public class TSensorsModule extends TModule {

	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"SensorsModule";
	}
	
	public static String Meters_Folder() {
		return Folder()+"/"+"Meters";
	}
	
	public static String Measurements_Folder() {
		return Folder()+"/"+"Measurements";
	}
	
	
	public TInternalSensorsModule InternalSensorsModule;
	//.
	public TSensorsDataValue Data;
	//.
	private TModel Model;
	//.
	public TSensorsMeters Meters;
	
    public TSensorsModule(TDEVICEModule pDevice) throws Exception {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
		F = new File(Meters_Folder());
		if (!F.exists()) 
			F.mkdirs();
		F = new File(Measurements_Folder());
		if (!F.exists()) 
			F.mkdirs();
		//.
        InternalSensorsModule = new TInternalSensorsModule(this);
        //.
        Data = new TSensorsDataValue(this);
        //.
        Model = null;
        //.
        Meters = new TSensorsMeters(this, Meters_Folder());
    }
    
    public void Destroy() throws Exception {
    	if (Meters != null) {
    		Meters.Destroy();
    		Meters = null;
    	}
    }
    
    @Override
    public void Start() throws Exception {
    	super.Start();
    }
    
    @Override
    public void Stop() throws Exception {
    	Meters.Finalize();
    	//.
    	super.Stop();
    }
    
    public synchronized void Model_Build() throws Exception {
    	TModel NewModel = new TModel(this);
    	NewModel.Stream.Name = "Sensors";
    	NewModel.Stream.Info = "Sensor's channels of the device";
    	TChannelsProvider ChannelsProvider = new TChannelsProvider(this);
    	//. build InternalSensorsModule
    	if ((InternalSensorsModule.Model != null) && (InternalSensorsModule.Model.Stream != null))
        	for (int I = 0; I < InternalSensorsModule.Model.Stream.Channels.size(); I++) {
        		com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel SourceChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel)InternalSensorsModule.Model.Stream.Channels.get(I); 
        		TStreamChannel NewChannel = ChannelsProvider.GetChannel(SourceChannel.GetTypeID());
        		NewChannel.Assign(SourceChannel);
        		//. make a unique ID for new sensors stream channel
        		NewChannel.ID = TChannel.GetNextID();
        		//. attaching the channel to the source channel
        		SourceChannel.DestinationChannel_Set(NewChannel);
        		NewChannel.SourceChannels_Add(SourceChannel);
        		//.
        		NewModel.Stream.Channels.add(NewChannel);
        	}
    	//. build PluginModule modules
    	/* USBPluginModule */
    	if ((Device.PluginsModule.USBPluginModule.PIOModel != null) && (Device.PluginsModule.USBPluginModule.PIOModel.Stream != null))
        	for (int I = 0; I < Device.PluginsModule.USBPluginModule.PIOModel.Stream.Channels.size(); I++) {
        		com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel SourceChannel = (com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel)Device.PluginsModule.USBPluginModule.PIOModel.Stream.Channels.get(I); 
        		TStreamChannel NewChannel = ChannelsProvider.GetChannel(SourceChannel.GetTypeID());
        		NewChannel.Assign(SourceChannel);
        		//. make a unique ID for new sensors stream channel
        		NewChannel.ID = TChannel.GetNextID();
        		//. attaching the channel to the source channel
        		SourceChannel.DestinationChannel = NewChannel;
        		NewChannel.SourceChannels_Add(SourceChannel);
        		//.
        		NewModel.Stream.Channels.add(NewChannel);
        	}
    	//.
    	Model = NewModel;
    	//.
    	Meters.Initialize();
    }
    
    public void Model_BuildAndPublish() throws Exception {
    	byte[] ModelBA;
    	if (InternalSensorsModule.IsEnabled() || (Device.PluginsModule.USBPluginModule.PIOModel != null)) {
        	Model_Build();
        	//.
        	ModelBA = Model.ToByteArray();
    	}
    	else
    		ModelBA = null;
    	//.
        Data.SetValue(OleDate.UTCCurrentTimestamp(),ModelBA);
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetSensorsDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        SO.setValue(Data);
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
    }
    
	public TChannel Model_Channels_GetOneByID(int ID) throws Exception {
		if (Model == null)
			return null; //. ->
		return Model.StreamChannels_GetOneByID(ID); //. ->
	}
	
	public TChannel Model_Channels_GetOneByTypeID(String TypeID) throws Exception {
		if (Model == null)
			return null; //. ->
		return Model.StreamChannels_GetOneByTypeID(TypeID); //. ->
	}
	
	public TChannel Model_Channels_GetOneByDescriptor(String UserAccessKey, byte[] ChannelDescriptor) throws Exception {
		if (UserAccessKey == null)
			return Model.StreamChannels_GetOneByDescriptor(ChannelDescriptor); //. ->
		//. 
		TStreamChannel Result = (TStreamChannel)TChannel.GetChannelFromByteArray(ChannelDescriptor, (new com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider(this)));
		if (Result == null)
			return null; //. ->
		//. check the UserMessagings
		TUserMessaging UserMessaging = InternalSensorsModule.UserMessagingModule.GetUserMessagingByOutChannelTypeAndSession(Result.GetTypeID(), UserAccessKey);
		if (UserMessaging != null) {
			Result.Assign(UserMessaging.OutChannel);
			//. attaching the channel to the source channel
			UserMessaging.OutChannel.DestinationChannel_Set(Result);
			Result.SourceChannels_Add(UserMessaging.OutChannel);
			return Result;
		}
		//.
		return null;
	}
	
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_OK 						= 0;
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_ERROR 					= -1;
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR 		= -2;
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_UNAVAILABLE_ERROR		= -3;
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_CHANNELNOTFOUND_ERROR	= -4;
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_ACCESSDENIED_ERROR		= -5;
    
    public void SensorsStreamingServer_Connect() {
    }
    
    public void SensorsStreamingServer_Disconnect() {
    }
    
    public void SensorsStreamingServer_Streaming(long DestinationUserID, String DestinationUserAccessKey, InputStream DestinationConnectionInputStream, OutputStream DestinationConnectionOutputStream, TCanceller Canceller) throws IOException {
    	byte[] Descriptor = new byte[4];
  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  			throw new IOException("error of reading connection"); //. =>
  		int Version = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  		switch (Version) {
  		
  		case 1: //. get channel by its ChannelID
  	  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  	  			throw new IOException("error of reading connection"); //. =>
  	  		int ChannelID = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  	  		//.
  	  		TStreamChannel Channel = (TStreamChannel)Model.StreamChannels_GetOneByID(ChannelID);
  	  		if (Channel == null) {
  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_CHANNELNOTFOUND_ERROR);
  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  			return; //. ->
  	  		}
  	  		//.
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_OK);
  	  		DestinationConnectionOutputStream.write(Descriptor);		
  	  		//. streaming ...
	  		Channel.UserID = DestinationUserID;
	  		Channel.UserAccessKey = DestinationUserAccessKey;
  			Channel.DoStreaming(DestinationConnectionOutputStream, Canceller);
  			return; //. ->
  			
  		case 2: //. get channel by its ChannelDescriptor
  	  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  	  			throw new IOException("error of reading connection"); //. =>
  	  		int ChannelDescriptorSize = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  	  		byte[] ChannelDescriptor = new byte[ChannelDescriptorSize];
  	  		if (ChannelDescriptorSize > 0)
  	  	  		if (TNetworkConnection.InputStream_ReadData(DestinationConnectionInputStream, ChannelDescriptor,ChannelDescriptorSize) != ChannelDescriptorSize)
  	  	  			throw new IOException("error of reading connection"); //. =>
  	  		//.
  	  		try {
  	  	  		Channel = (TStreamChannel)Model_Channels_GetOneByDescriptor(DestinationUserAccessKey,ChannelDescriptor);
  	  	  		if (Channel == null) {
  	  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_CHANNELNOTFOUND_ERROR);
  	  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  	  			return; //. ->
  	  	  		}
  	  		}
  	  		catch (Exception E) {
  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_ERROR);
  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  			return; //. ->
  	  		}
  	  		//.
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_OK);
  	  		DestinationConnectionOutputStream.write(Descriptor);		
  	  		//. streaming ...
	  		Channel.UserID = DestinationUserID;
	  		Channel.UserAccessKey = DestinationUserAccessKey;
  			Channel.DoStreaming(DestinationConnectionOutputStream, Canceller);
  			return; //. ->
  			
  		case 3: //. get channel by its ChannelDescriptor, return a real ChannelDescriptor 
  	  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  	  			throw new IOException("error of reading connection"); //. =>
  	  		ChannelDescriptorSize = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  	  		ChannelDescriptor = new byte[ChannelDescriptorSize];
  	  		if (ChannelDescriptorSize > 0)
  	  	  		if (TNetworkConnection.InputStream_ReadData(DestinationConnectionInputStream, ChannelDescriptor,ChannelDescriptorSize) != ChannelDescriptorSize)
  	  	  			throw new IOException("error of reading connection"); //. =>
  	  		//.
  	  		try {
  	  	  		Channel = (TStreamChannel)Model_Channels_GetOneByDescriptor(DestinationUserAccessKey,ChannelDescriptor);
  	  	  		if (Channel == null) {
  	  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_CHANNELNOTFOUND_ERROR);
  	  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  	  			return; //. ->
  	  	  		}
  	  		}
  	  		catch (Exception E) {
  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_ERROR);
  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  			return; //. ->
  	  		}
  	  		//.
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_OK);
  	  		DestinationConnectionOutputStream.write(Descriptor);
  	  		//. return 
  	  		try {
  	  	  		ChannelDescriptor = Channel.ToByteArray();
  	  		}
  	  		catch (Exception E) {
  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_ERROR);
  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  			return; //. ->
  	  		}
  	  		ChannelDescriptorSize = ChannelDescriptor.length;
  	  		Descriptor = TDataConverter.ConvertInt32ToLEByteArray(ChannelDescriptorSize);
  	  		DestinationConnectionOutputStream.write(Descriptor);		
  	  		DestinationConnectionOutputStream.write(ChannelDescriptor);		
  	  		//. streaming ...
	  		Channel.UserID = DestinationUserID;
	  		Channel.UserAccessKey = DestinationUserAccessKey;
  			Channel.DoStreaming(DestinationConnectionOutputStream, Canceller);
  			return; //. ->
  			
  		default:
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR);
  			DestinationConnectionOutputStream.write(Descriptor);		
  			return; //. ->
  		}
    }
    
	public TComponentDataStreamingAbstract.TStreamer GetStreamer(String pTypeID, int pidTComponent, long pidComponent, int pChannelID, String pConfiguration, String pParameters) throws Exception {
		TModel _Model;
		synchronized (this) {
			_Model = Model;
		}
		if (Model != null)
			return _Model.GetStreamer(pTypeID, pidTComponent,pidComponent, pChannelID, pConfiguration,pParameters); //. ->
		else 
			return null; //. ->
	}

	public String Measurements_GetList(short Version) {
		return TSensorsModuleMeasurements.GetMeasurementsList(Version);
	}
	
	public String Measurements_GetList(double BeginTimestamp, double EndTimestamp, short Version) {
		return TSensorsModuleMeasurements.GetMeasurementsList(BeginTimestamp,EndTimestamp, Version);
	}
	
	public void Measurements_Delete(String MeasurementID) throws IOException {
		TSensorsModuleMeasurements.DeleteMeasurement(MeasurementID);
	}
	
	public long Measurement_GetSize(String MeasurementID) throws IOException {
		return TSensorsModuleMeasurements.GetMeasurementSize(MeasurementID);
	}
	
	public byte[] Measurement_GetData(String MeasurementID) throws IOException {
		return TSensorsModuleMeasurements.GetMeasurementData(MeasurementID);
	}
}
