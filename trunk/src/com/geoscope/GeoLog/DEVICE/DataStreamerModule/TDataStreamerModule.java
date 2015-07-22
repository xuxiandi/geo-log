package com.geoscope.GeoLog.DEVICE.DataStreamerModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.content.Intent;
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamFunctionality;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetDataStreamerActiveFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreaming;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TDataStreamerModule extends TModule {

	public static final String FolderName = "DataStreamerModule"; 
	
	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+FolderName;
	}
	
	public static final String StreamingComponentsFileName = "StreamingComponents.xml";
	
	public static TComponentDataStreaming.TStreamer GetStreamer(String TypeID, TDEVICEModule Device, int idTComponent, long idComponent, int ChannelID, String Configuration, String Parameters) throws Exception {
		TComponentDataStreaming.TStreamer Result;
		Result = Device.SensorsModule.GetStreamer(TypeID, idTComponent,idComponent, ChannelID, Configuration,Parameters);
		if (Result != null)
			return Result; //. ->
		Result = Device.AudioModule.GetStreamer(TypeID, idTComponent,idComponent, ChannelID, Configuration,Parameters);
		if (Result != null)
			return Result; //. ->
		Result = Device.VideoModule.GetStreamer(TypeID, idTComponent,idComponent, ChannelID, Configuration,Parameters);
		//.
		return Result; //. ->
	}

	public static class TStreamingComponents {
		
		public static class TComponent {
			
			public boolean Enabled = false;
			//.
			public int 	idTComponent;
			public long	idComponent;
			//.
			public ArrayList<Integer> Channels = null;
			public boolean Channels_ChannelExists(int ChannelID) {
				if (Channels == null)
					return true; //. ->
				for (int I = 0; I < Channels.size(); I++)
					if (Channels.get(I) == ChannelID)
						return true; //. ->
				return false;
			}
			public void Channels_AddChannel(int ChannelID) {
				if (Channels == null)
					Channels = new ArrayList<Integer>();
				for (int I = 0; I < Channels.size(); I++)
					if (Channels.get(I) == ChannelID)
						return; //. ->
				Channels.add(ChannelID);
			}
			public void Channels_RemoveChannel(int ChannelID) {
				if (Channels == null)
					return; //. ->
				for (int I = 0; I < Channels.size(); I++)
					if (Channels.get(I) == ChannelID) {
						Channels.remove(I);
						return; //. ->
					}
			}
			public void Channels_SetupByStreamDescriptorChannels() {
				Channels = new ArrayList<Integer>();
				int CC = StreamDescriptor.Channels.size();
		    	for (int I = 0; I < CC; I++) 
		    		Channels.add(StreamDescriptor.Channels.get(I).ID);
			}
			//.
			public TStreamDescriptor StreamDescriptor = null;

			public void SupplyWithStreamDescriptor(TGeoScopeServerUser User) throws Exception {
				if (idTComponent == SpaceDefines.idTDataStream) {
					TDataStreamFunctionality DSF = (TDataStreamFunctionality)User.Space.TypesSystem.SystemTDataStream.TComponentFunctionality_Create(idComponent);
					try {
						StreamDescriptor = DSF.GetStreamDescriptor();
					}
					finally {
						DSF.Release();
					}
				}
			}
		}
		
		public ArrayList<TComponent> Components = new ArrayList<TComponent>();

		public TStreamingComponents() {
			Clear();
		}
		
		public TStreamingComponents(byte[] BA) throws Exception {
			FromByteArray(BA);
		}
		
		public void Clear() {
			Components.clear();
		}
		
		public void Add(TComponent Component) {
			Components.add(Component);
		}
		
		public void FromByteArray(byte[] BA) throws Exception {
			Clear();
			//.
	    	Document XmlDoc;
			ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
				factory.setNamespaceAware(true);     
				DocumentBuilder builder = factory.newDocumentBuilder(); 			
				XmlDoc = builder.parse(BIS); 
			}
			finally {
				BIS.close();
			}
			Element RootNode = XmlDoc.getDocumentElement();
			int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
			switch (Version) {
			case 1:
				try {
	    			Components.clear();
					NodeList ComponentsNode = TMyXML.SearchNode(RootNode,"Components").getChildNodes();
					int Cnt = ComponentsNode.getLength();
					for (int I = 0; I < Cnt; I++) {
						Node ComponentNode = ComponentsNode.item(I);
						//.
						if (ComponentNode.getLocalName() != null) {
							TComponent Component = new TComponent();
							//.
							Component.Enabled = (Integer.parseInt(TMyXML.SearchNode(ComponentNode,"Enabled").getFirstChild().getNodeValue()) != 0);
							//.
							Component.idTComponent = Integer.parseInt(TMyXML.SearchNode(ComponentNode,"idTComponent").getFirstChild().getNodeValue());
							Component.idComponent = Long.parseLong(TMyXML.SearchNode(ComponentNode,"idComponent").getFirstChild().getNodeValue());
							//.
							String ChannelsString = null;
							Node ValueNode = TMyXML.SearchNode(ComponentNode,"Channels");
							if (ValueNode != null)
								ChannelsString = ValueNode.getFirstChild().getNodeValue();
							if (ChannelsString != null) {
								Component.Channels = new ArrayList<Integer>();
								String[] _Channels = ChannelsString.split(",");
								if (_Channels.length > 0) 
									for (int J = 0; J < _Channels.length; J++)
										Component.Channels.add(Integer.parseInt(_Channels[J]));
							}
							else
								Component.Channels = null;
							//.
							String _Descriptor = null;
							ValueNode = TMyXML.SearchNode(ComponentNode,"Descriptor");
							if (ValueNode != null)
								_Descriptor = ValueNode.getFirstChild().getNodeValue();
							if ((_Descriptor != null) && (_Descriptor.length() > 0))
								Component.StreamDescriptor = new TStreamDescriptor(_Descriptor);
							else
								Component.StreamDescriptor = null;
							//.
							Components.add(Component);
						}
					}
				}
				catch (Exception E) {
	    			throw new Exception("error of parsing stream descriptor: "+E.getMessage()); //. =>
				}
				break; //. >
			default:
				throw new Exception("unknown stream descriptor version, version: "+Integer.toString(Version)); //. =>
			}
		}
		
		public byte[] ToByteArray() throws Exception {
			if (Components.size() == 0)
				return null; //. ->
			int Version = 1;
		    XmlSerializer Serializer = Xml.newSerializer();
		    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		    try {
		        Serializer.setOutput(BOS,"UTF-8");
		        Serializer.startDocument("UTF-8",true);
		        Serializer.startTag("", "ROOT");
		        //. Version
	            Serializer.startTag("", "Version");
	            Serializer.text(Integer.toString(Version));
	            Serializer.endTag("", "Version");
		        //. 
	            Serializer.startTag("", "Components");
	            	for (int I = 0; I < Components.size(); I++) {
	    	            Serializer.startTag("", "C"+Integer.toString(I));
	    	            	TComponent Component = Components.get(I);
	    			        //. 
	    		            int V;
	    		            if (Component.Enabled)
	    		            	V = 1;
	    		            else
	    		            	V = 0;
	    		            Serializer.startTag("", "Enabled");
	    		            Serializer.text(Integer.toString(V));
	    		            Serializer.endTag("", "Enabled");
	    		            //.
	    		            Serializer.startTag("", "idTComponent");
	    		            Serializer.text(Integer.toString(Component.idTComponent));
	    		            Serializer.endTag("", "idTComponent");
	    		            //.
	    		            Serializer.startTag("", "idComponent");
	    		            Serializer.text(Long.toString(Component.idComponent));
	    		            Serializer.endTag("", "idComponent");
	    		            //.
	    		            if (Component.Channels != null) {
	    		            	StringBuilder SB = new StringBuilder();
								for (int J = 0; J < Component.Channels.size(); J++) {
									if (J != 0)
										SB.append(","+Integer.toString(Component.Channels.get(J)));
									else
										SB.append(Integer.toString(Component.Channels.get(J)));
								}
		    		            Serializer.startTag("", "Channels");
		    		            Serializer.text(SB.toString());
		    		            Serializer.endTag("", "Channels");
	    		            }
	    		            //.
	    		            if (Component.StreamDescriptor != null) {
		    		            Serializer.startTag("", "Descriptor");
		    		            Serializer.text(Component.StreamDescriptor.ToBase64String());
		    		            Serializer.endTag("", "Descriptor");
	    		            }
	    	            Serializer.endTag("", "C"+Integer.toString(I));
	            	}
	            Serializer.endTag("", "Components");
		        //.
		        Serializer.endTag("", "ROOT");
		        Serializer.endDocument();
		        //.
				return BOS.toByteArray();
		    }
		    finally {
		    	BOS.close();
		    }
		}
		
		public void SupplyComponentsWithStreamDescriptors(TGeoScopeServerUser User) throws Exception {
			for (int I = 0; I < Components.size(); I++) {
				TComponent Component = Components.get(I);
				Component.SupplyWithStreamDescriptor(User);
			}
		}
	}
	
	private static class TStreaming extends TCancelableThread {
		
		public static final int RestartInterval = 1000*60; //. seconds
		
		private TDataStreamerModule DataStreamerModule;
		private TStreamingComponents StreamingComponents;
		//.
		private ArrayList<TComponentDataStreaming.TStreamer> Streamers = new ArrayList<TComponentDataStreaming.TStreamer>(); 
		                                          
		public TStreaming(TDataStreamerModule pDataStreamerModule, TStreamingComponents pStreamingComponents) {
    		super();
    		//.
			DataStreamerModule = pDataStreamerModule;
			StreamingComponents = pStreamingComponents;
			//.
			Start();
		}
		
		public void Destroy() throws InterruptedException {
			Stop();
		}
		
    	public void Start() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public void Stop() throws InterruptedException {
    		CancelAndWait();
    	}
    	
		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					try {
						StartStreamers();
						try {
							while (!Canceller.flCancel) {
								Thread.sleep(1000);
							}
						}
						finally {
							StopStreamers();
						}
					}
					catch (InterruptedException E) {
						return; //. ->
					}
		        	catch (Throwable E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
						DataStreamerModule.Device.Log.WriteError("DataStreamerModule.Streaming",S,E.getStackTrace());
		        	}
					//.
					Thread.sleep(RestartInterval);
				}
			}
			catch (InterruptedException IE) {
			}
        	catch (Throwable E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				DataStreamerModule.Device.Log.WriteError("DataStreamerModule.Streaming",S,E.getStackTrace());
        	}
		}

		private void StartStreamers() throws Exception {
			TGeoScopeServerUser User = TUserAgent.GetUserAgentUser();
			//.
			if (StreamingComponents.Components.size() == 0) { //. fetch a DataStream component from the device object 
				TComponentFunctionality CF = User.Space.TypesSystem.TComponentFunctionality_Create(SpaceDefines.idTCoComponent,DataStreamerModule.Device.idOwnerComponent);
				if (CF != null)
					try {
						long idDataStream = CF.GetComponent(SpaceDefines.idTDataStream);
						if (idDataStream != 0) {
							TModel Model = DataStreamerModule.Device.SensorsModule.Model;
							if (Model != null) {
								TStreamDescriptor StreamDescriptor = Model.Stream.Clone();
								//. suppress not stream-able-via-component channels
								int Cnt = StreamDescriptor.Channels.size();
								for (int J = 0; J < Cnt; J++) {
									TChannel StreamChannel = StreamDescriptor.Channels.get(J);
									//.
									com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel Channel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel)Model.Stream.Channels.get(J); 
		            				//.
									StreamChannel.Enabled = Channel.StreamableViaComponent();
									if (StreamChannel.Enabled) {
			            				TChannel SourceChannel = Channel.SourceChannel_Get();
			            				if (SourceChannel != null) {
											StreamChannel.ProfilesFolder = SourceChannel.ProfilesFolder; 
											StreamChannel.Configuration_LoadFromConfigurationFile(); //. supply the channel with configuration
			            				}
									}
								}
								StreamDescriptor.Channels_RemoveDisabledItems();
								//. update the DataStream component with descriptor 
								TDataStreamFunctionality DSF = (TDataStreamFunctionality)User.Space.TypesSystem.SystemTDataStream.TComponentFunctionality_Create(idDataStream);
								try {
									DSF.SetStreamDescriptor(StreamDescriptor);
									//.
									Cnt = StreamDescriptor.Channels.size();
									for (int J = 0; J < Cnt; J++) {
										TChannel StreamChannel = StreamDescriptor.Channels.get(J);
										if (StreamChannel.Enabled) {
											TComponentDataStreaming.TStreamer Streamer = GetStreamer(StreamChannel.GetTypeID(), DataStreamerModule.Device, DSF.idTComponent(),DSF.idComponent, StreamChannel.ID, StreamChannel.Configuration, StreamChannel.Parameters);
											if (Streamer != null) {
												Streamers.add(Streamer);
												//.
												Streamer.Start();
											}
											else 
												DataStreamerModule.Device.Log.WriteWarning("DataStreamerModule.Streaming","Streamer not found for TypeID: "+StreamChannel.GetTypeID());
										}
									}
								}
								finally {
									DSF.Release();
								}
							}
						}
					}
					finally {
						CF.Release();
					}
			}
			else
				for (int I = 0; I < StreamingComponents.Components.size(); I++) {
					TStreamingComponents.TComponent Component = StreamingComponents.Components.get(I);
					//.
					if (Component.Enabled) {
						//. supply component with its stream descriptor
						if (User != null)
							Component.SupplyWithStreamDescriptor(User);
						//.
						if (Component.StreamDescriptor != null)
							for (int J = 0; J < Component.StreamDescriptor.Channels.size(); J++) {
								TChannel Channel = Component.StreamDescriptor.Channels.get(J); 
								if (Component.Channels_ChannelExists(Channel.ID)) {
									TComponentDataStreaming.TStreamer Streamer = GetStreamer(Channel.GetTypeID(), DataStreamerModule.Device, Component.idTComponent,Component.idComponent, Channel.ID, Channel.Configuration, Channel.Parameters);
									if (Streamer != null) {
										Streamers.add(Streamer);
										//.
										Streamer.Start();
									}
									else 
										DataStreamerModule.Device.Log.WriteWarning("DataStreamerModule.Streaming","Streamer not found for TypeID: "+Channel.GetTypeID());
								}
							}
					}
				}
		}
		
		private void StopStreamers() throws Exception {
			for (int I = 0; I < Streamers.size(); I++) 
				Streamers.get(I).Destroy();
			Streamers.clear();
		}
	}
	
	
	private TStreamingComponents	StreamingComponents;
	private TStreaming 				Streaming = null;
	//
	public TDataStreamerStreamingComponentsValue 	StreamingComponentsValue;
	public TDataStreamerActiveValue 				ActiveValue;
	
    public TDataStreamerModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
        //.
		StreamingComponents = new TStreamingComponents();
		//.
		StreamingComponentsValue 	= new TDataStreamerStreamingComponentsValue(this);
		ActiveValue 				= new TDataStreamerActiveValue(this);
		//.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() throws InterruptedException {
    }
    
    @Override
    public void Start() throws Exception {
    }
    
    @Override
    public void Stop() throws Exception {
    	StopStreaming();
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
		File F = new File(ModuleFile());
		if (!F.exists()) 
			return; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(F);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		Element RootNode = XmlDoc.getDocumentElement();
		int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			try {
				Node DataStreamerNode = TMyXML.SearchNode(RootNode,"DataStreamerModule");
				//.
				Node node = TMyXML.SearchNode(DataStreamerNode,"Active").getFirstChild();
				if (node != null)
					ActiveValue.FromString(node.getNodeValue());
			}
			catch (Exception E) {
			}
			break; //. >
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
		//.
    	LoadStreamingComponents();
    }
    
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        Serializer.startTag("", "DataStreamerModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //.
        Serializer.startTag("", "Active");
        Serializer.text(ActiveValue.ToString());
        Serializer.endTag("", "Active");
        //.
        Serializer.endTag("", "DataStreamerModule");
    }
    
    public synchronized void LoadStreamingComponents() throws Exception {
    	StreamingComponents.Clear();
    	//.
    	File SCF = new File(Folder()+"/"+StreamingComponentsFileName);
    	if (SCF.exists()) {
    		FileInputStream FIS = new FileInputStream(SCF);
    		try {
    			byte[] Data = new byte[(int)SCF.length()];
    			FIS.read(Data);
    			//.
    			StreamingComponents.FromByteArray(Data);
    		}
    		finally {
    			FIS.close();
    		}
    	}
    }
    
    public synchronized void SetStreamingComponents(TStreamingComponents pStreamingComponents) throws Exception {
    	StreamingComponents = pStreamingComponents;
    	//.
    	File SCF = new File(Folder()+"/"+StreamingComponentsFileName);
    	byte[] Data = StreamingComponents.ToByteArray();
    	if (Data == null) {
    		SCF.delete();
    		return; //. ->
    	}
    	FileOutputStream FOS = new FileOutputStream(SCF);
    	try {
    		FOS.write(Data);
    	}
    	finally {
    		FOS.close();
    	}
    	//. validation
		ValidateStreaming();
    }
    
    public synchronized TStreamingComponents GetStreamingComponents() {
    	return StreamingComponents;
    }
    
    public synchronized int StreamingComponentsCount() {
    	return StreamingComponents.Components.size();
    }
    
    public synchronized void StartStreaming() throws InterruptedException {
    	Streaming = new TStreaming(this, StreamingComponents);
    }

    public synchronized void StopStreaming() throws InterruptedException {
    	if (Streaming != null) {
    		Streaming.Destroy();
    		Streaming = null;
    	}
    }
    
    public synchronized void ValidateStreaming() throws InterruptedException {
    	StopStreaming();
    	if (ActiveValue.BooleanValue())
    		Streaming = new TStreaming(this, StreamingComponents);
    }

    public synchronized boolean StreamingIsActive() throws InterruptedException {
    	return (ActiveValue.BooleanValue());
    }
    
    public synchronized void SetActive(boolean Value) throws Exception {
    	byte V = 0;
    	if (Value)
    		V = 1;
        ActiveValue.SetValue(OleDate.UTCCurrentTimestamp(),V);
    	//.
    	SaveProfile();
    	//. validation
		ValidateStreaming();
    }

    public void SetActiveValue(boolean flTrue, boolean flPostProcess) throws Exception
    {
    	if (flTrue == ActiveValue.BooleanValue())
    		return; //. ->
        //.
        SetActive(flTrue);
        //.
        TObjectSetComponentDataServiceOperation SO = new TObjectSetDataStreamerActiveFlagSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        ((TObjectSetDataStreamerActiveFlagSO)SO).setValue(ActiveValue);
        try {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            if (flPostProcess) {
                Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                //.
    			SaveProfile();
            }
        }
        catch (Exception E) {}
    }    

    public void SetActiveValue(boolean flActive) throws Exception {
    	SetActiveValue(flActive,true);
    }    

    public void ShowPropsPanel(Context context) {
    	Intent intent = new Intent(Device.context,TDataStreamerPropsPanel.class);
    	context.startActivity(intent);
    }
}
