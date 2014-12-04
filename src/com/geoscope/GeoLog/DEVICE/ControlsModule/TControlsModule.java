package com.geoscope.GeoLog.DEVICE.ControlsModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetControlsDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.TChannelsProvider;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TControlsModule extends TModule {

	public TControlsDataValue Data;
	//.
	public TModel Model;
	
    public TControlsModule(TDEVICEModule pDevice) throws Exception {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        Model = new TModel(this);
        //.
        Data = new TControlsDataValue(this);
    }
    
    public void Destroy() {
    }

    public synchronized void BuildModel() throws Exception {
    	TModel NewModel = new TModel(this);
    	NewModel.ControlStream.Name = "Controls";
    	NewModel.ControlStream.Info = "Controls of the device";
    	TChannelsProvider ChannelsProvider = new TChannelsProvider(this);
    	//. build PluginModule modules
    	/* USBPluginModule */
    	if ((Device.PluginsModule.USBPluginModule.PIOModel != null) && (Device.PluginsModule.USBPluginModule.PIOModel.ControlStream != null))
        	for (int I = 0; I < Device.PluginsModule.USBPluginModule.PIOModel.ControlStream.Channels.size(); I++) {
        		com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel DestinationChannel = (com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel)Device.PluginsModule.USBPluginModule.PIOModel.ControlStream.Channels.get(I); 
        		TStreamChannel NewChannel = ChannelsProvider.GetChannel(DestinationChannel.GetTypeID());
        		NewChannel.Assign(DestinationChannel);
        		//. make a unique ID for new sensors stream channel
        		NewChannel.ID = TChannel.GetNextID();
        		//. attaching the channel to the source channel
        		NewChannel.DestinationChannel = DestinationChannel;
        		//.
        		NewModel.ControlStream.Channels.add(NewChannel);
        	}
    	//.
    	Model = NewModel;
    }
    
    public void BuildModelAndPublish() throws Exception {
    	byte[] ModelBA;
    	if (Device.PluginsModule.USBPluginModule.PIOModel != null) {
        	BuildModel();
        	//.
        	ModelBA = Model.ToByteArray();
    	}
    	else
    		ModelBA = null;
    	//.
        Data.SetValue(OleDate.UTCCurrentTimestamp(),ModelBA);
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetControlsDataSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetControlsDataSO)SO).setValue(Data);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        }
        catch (Exception E) {}
    }

    public static final int CONTROLSSTREAMINGSERVER_MESSAGE_OK 						= 0;
    public static final int CONTROLSTREAMINGSERVER_MESSAGE_ERROR 					= -1;
    public static final int CONTROLSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR 		= -2;
    public static final int CONTROLSSTREAMINGSERVER_MESSAGE_UNAVAILABLE_ERROR		= -3;
    public static final int CONTROLSSTREAMINGSERVER_MESSAGE_CHANNELNOTFOUND_ERROR	= -4;
    public static final int CONTROLSSTREAMINGSERVER_MESSAGE_ACCESSDENIED_ERROR		= -5;
    
    public void ControlsStreamingServer_Connect() {
    }
    
    public void ControlsStreamingServer_Disconnect() {
    }
    
    public void ControlsStreamingServer_Streaming(long DestinationUserID, String DestinationUserAccessKey, InputStream DestinationConnectionInputStream, OutputStream DestinationConnectionOutputStream, TCanceller Canceller) throws IOException {
    	byte[] Descriptor = new byte[4];
  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  			throw new IOException("error of reading connection"); //. =>
  		int Version = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  		switch (Version) {
  		
  		case 1: //. channel by its ChannelID 
  	  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  	  			throw new IOException("error of reading connection"); //. =>
  	  		int ChannelID = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  	  		TStreamChannel Channel = (TStreamChannel)Model.ControlStream.Channels_GetOneByID(ChannelID);
  	  		if (Channel == null) {
  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSSTREAMINGSERVER_MESSAGE_CHANNELNOTFOUND_ERROR);
  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  			return; //. ->
  	  		}
  	  		//.
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSSTREAMINGSERVER_MESSAGE_OK);
  	  		DestinationConnectionOutputStream.write(Descriptor);		
  	  		//. streaming ...
  	  		Channel.UserID = DestinationUserID;
  	  		Channel.UserAccessKey = DestinationUserAccessKey;
  	  		Channel.DoStreaming(DestinationConnectionInputStream, DestinationConnectionOutputStream, Canceller);
  			return; //. ->
  			
  		case 2: //. channel by its ChannelDescriptor
  	  		if (DestinationConnectionInputStream.read(Descriptor,0,Descriptor.length) != Descriptor.length)
  	  			throw new IOException("error of reading connection"); //. =>
  	  		int ChannelDescriptorSize = TDataConverter.ConvertLEByteArrayToInt32(Descriptor,0);
  	  		byte[] ChannelDescriptor = new byte[ChannelDescriptorSize];
  	  		if (ChannelDescriptorSize > 0)
  	  	  		if (TNetworkConnection.InputStream_ReadData(DestinationConnectionInputStream, ChannelDescriptor,ChannelDescriptorSize) != ChannelDescriptorSize)
  	  	  			throw new IOException("error of reading connection"); //. =>
  	  		//.
  	  		try {
  	  	  		Channel = (TStreamChannel)Model.StreamChannels_GetOneByDescriptor(ChannelDescriptor);
  	  	  		if (Channel == null) {
  	  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSSTREAMINGSERVER_MESSAGE_CHANNELNOTFOUND_ERROR);
  	  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  	  			return; //. ->
  	  	  		}
  	  		}
  	  		catch (Exception E) {
  	  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSTREAMINGSERVER_MESSAGE_ERROR);
  	  			DestinationConnectionOutputStream.write(Descriptor);		
  	  			return; //. ->
  	  		}
  	  		//.
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSSTREAMINGSERVER_MESSAGE_OK);
  	  		DestinationConnectionOutputStream.write(Descriptor);		
  	  		//. streaming ...
	  		Channel.UserID = DestinationUserID;
  	  		Channel.UserAccessKey = DestinationUserAccessKey;
  	  		Channel.DoStreaming(DestinationConnectionInputStream, DestinationConnectionOutputStream, Canceller);
  			return; //. ->
  			
  		default:
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR);
  			DestinationConnectionOutputStream.write(Descriptor);		
  			return; //. ->
  		}
    }
}
