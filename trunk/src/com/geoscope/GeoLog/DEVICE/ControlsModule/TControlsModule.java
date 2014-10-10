package com.geoscope.GeoLog.DEVICE.ControlsModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Types.Date.OleDate;
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
        Model = new TModel();
        //.
        Data = new TControlsDataValue(this);
    }
    
    public void Destroy() {
    }

    public synchronized void BuildModel() throws Exception {
    	TModel NewModel = new TModel();
    	NewModel.ControlStream.Name = "Controls";
    	NewModel.ControlStream.Info = "Controls of the device";
    	TChannelsProvider ChannelsProvider = new TChannelsProvider();
    	//. build PluginModule modules
    	/* USBPluginModule */
    	if (Device.PluginsModule.USBPluginModule.PIOModel != null)
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

    public static final int CONTROLSSTREAMINGSERVER_MESSAGE_OK 					= 0;
    public static final int CONTROLSTREAMINGSERVER_MESSAGE_ERROR 				= -1;
    public static final int CONTROLSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR 	= -2;
    public static final int CONTROLSSTREAMINGSERVER_MESSAGE_UNAVAILABLE_ERROR	= -3;
    
    public void ControlsStreamingServer_Connect() {
    }
    
    public void ControlsStreamingServer_Disconnect() {
    }
    
    public void ControlsStreamingServer_Streaming(InputStream DestinationConnectionInputStream, OutputStream DestinationConnectionOutputStream, TCanceller Canceller) throws IOException {
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
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR);
  			DestinationConnectionOutputStream.write(Descriptor);		
  			return; //. ->
  		}
  		//.
  		TStreamChannel Channel = (TStreamChannel)Model.ControlStream.Channels_GetOneByID(ChannelID);
  		if (Channel == null) {
  			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSSTREAMINGSERVER_MESSAGE_WRONGPARAM_ERROR);
  			DestinationConnectionOutputStream.write(Descriptor);		
  			return; //. ->
  		}
  		//.
		Descriptor = TDataConverter.ConvertInt32ToLEByteArray(CONTROLSSTREAMINGSERVER_MESSAGE_OK);
  		DestinationConnectionOutputStream.write(Descriptor);		
  		//. streaming ...
  		Channel.DoStreaming(DestinationConnectionInputStream, DestinationConnectionOutputStream, Canceller);
    }
}
