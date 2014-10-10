package com.geoscope.GeoLog.DEVICE.SensorsModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetSensorsDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TSensorsModule extends TModule {

	public TInternalSensorsModule InternalSensorsModule;
	//.
	public TSensorsDataValue Data;
	//.
	private TModel Model;
	
    public TSensorsModule(TDEVICEModule pDevice) throws Exception {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        InternalSensorsModule = new TInternalSensorsModule(this);
        //.
        Data = new TSensorsDataValue(this);
        //.
        Model = null;
    }
    
    public void Destroy() {
    }
    
    public synchronized void BuildModel() throws Exception {
    	TModel NewModel = new TModel();
    	NewModel.Stream.Name = "Sensors";
    	NewModel.Stream.Info = "Sensor's channels of the device";
    	TChannelsProvider ChannelsProvider = new TChannelsProvider();
    	//. build InternalSensorsModule
    	if (InternalSensorsModule.Model != null)
        	for (int I = 0; I < InternalSensorsModule.Model.Stream.Channels.size(); I++) {
        		com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel SourceChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.TStreamChannel)InternalSensorsModule.Model.Stream.Channels.get(I); 
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
    	//. build PluginModule modules
    	/* USBPluginModule */
    	if (Device.PluginsModule.USBPluginModule.PIOModel != null)
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
    }
    
    public void BuildModelAndPublish() throws Exception {
    	byte[] ModelBA;
    	if (InternalSensorsModule.IsEnabled() || (Device.PluginsModule.USBPluginModule.PIOModel != null)) {
        	BuildModel();
        	//.
        	ModelBA = Model.ToByteArray();
    	}
    	else
    		ModelBA = null;
    	//.
        Data.SetValue(OleDate.UTCCurrentTimestamp(),ModelBA);
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetSensorsDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetSensorsDataSO)SO).setValue(Data);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        }
        catch (Exception E) {}
    }
    
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_OK 					= 0;
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_UNKNOWN_ERROR 		= -1;
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR 	= -2;
    public static final int SENSORSSTREAMINGSERVER_MESSAGE_UNAVAILABLE_ERROR	= -3;
    
    public void SensorsStreamingServer_Connect() {
    }
    
    public void SensorsStreamingServer_Disconnect() {
    }
    
    public void SensorsStreamingServer_Streaming(InputStream DestinationConnectionInputStream, OutputStream DestinationConnectionOutputStream, TCanceller Canceller) throws IOException {
    	byte[] Descriptor = new byte[4];
  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  			throw new IOException("error of reading connection"); //. =>
  		int Version = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  		int ChannelID = 0;
  		switch (Version) {
  		
  		case 1: 
  	  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  	  			throw new IOException("error of reading connection"); //. =>
  	  		ChannelID = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  			break; //. >
  			
  		default:
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR);
  			DestinationConnectionOutputStream.write(Descriptor);		
  			return; //. ->
  		}
  		//.
  		TStreamChannel Channel = (TStreamChannel)Model.Stream.Channels_GetOneByID(ChannelID);
  		if (Channel == null) {
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR);
  			DestinationConnectionOutputStream.write(Descriptor);		
  			return; //. ->
  		}
  		//.
		Descriptor = TDataConverter.ConvertInt32ToLEByteArray(SENSORSSTREAMINGSERVER_MESSAGE_OK);
  		DestinationConnectionOutputStream.write(Descriptor);		
  		//. streaming ...
		Channel.DoStreaming(DestinationConnectionOutputStream, Canceller);
    }
}
