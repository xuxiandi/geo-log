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

import android.util.Xml;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.TSpace;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamFunctionality;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreaming;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TDataStreamerModule extends TModule {

	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"DataStreamerModule";
	}
	
	public static final String StreamingComponentsFileName = "StreamingComponents.xml";
	
	public static TComponentDataStreaming.TStreamer GetStreamer(String TypeID, TDEVICEModule Device, int idTComponent, long idComponent, int ChannelID, String Configuration, String Parameters) {
		TComponentDataStreaming.TStreamer Result;
		Result = TAudioModule.GetStreamer(TypeID, Device, idTComponent,idComponent, ChannelID, Configuration, Parameters);
		if (Result != null)
			return Result; //. ->
		Result = TVideoModule.GetStreamer(TypeID, Device, idTComponent,idComponent, ChannelID, Configuration, Parameters);
		//.
		return Result; //. ->
	}

	public static class TStreamingComponents {
		
		public static class TComponent {
			
			public boolean Enabled;
			//.
			public int 	idTComponent;
			public long	idComponent;
			//.
			public int[] 	Channels = null;
			public boolean 	Channels_ChannelExists(int ChannelID) {
				if (Channels == null)
					return false; //. ->
				for (int I = 0; I < Channels.length; I++)
					if (Channels[I] == ChannelID)
						return true; //. ->
				return false;
			}
			//.
			public TDataStreamDescriptor Descriptor = null;
		}
		
		public boolean Enabled = false;
		//.
		public ArrayList<TComponent> Components = new ArrayList<TComponent>();

		public TStreamingComponents() {
			Clear();
		}
		
		public TStreamingComponents(byte[] BA) throws Exception {
			FromByteArray(BA);
		}
		
		public void Clear() {
			Enabled = false;
			Components.clear();
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
	    			Enabled = (Integer.parseInt(TMyXML.SearchNode(RootNode,"Enabled").getFirstChild().getNodeValue()) != 0);
	    			//.
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
								String[] _Channels = ChannelsString.split(",");
								if (_Channels.length > 0) {
									Component.Channels = new int[_Channels.length];
									for (int J = 0; J < _Channels.length; J++)
										Component.Channels[J] = Integer.parseInt(_Channels[J]);
								}
								else 
									Component.Channels = null;
							}
							else 
								Component.Channels = null;
							//.
							String _Descriptor = null;
							ValueNode = TMyXML.SearchNode(ComponentNode,"Descriptor");
							if (ValueNode != null)
								_Descriptor = ValueNode.getFirstChild().getNodeValue();
							if ((_Descriptor != null) && (_Descriptor.length() > 0))
								Component.Descriptor = new TDataStreamDescriptor(_Descriptor);
							else
								Component.Descriptor = null;
							//.
							Components.add(Component);
						}
					}
				}
				catch (Exception E) {
	    			throw new Exception("error of parsing configuration: "+E.getMessage()); //. =>
				}
				break; //. >
			default:
				throw new Exception("unknown local configuration version, version: "+Integer.toString(Version)); //. =>
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
	            int V;
	            if (Enabled)
	            	V = 1;
	            else
	            	V = 0;
	            Serializer.startTag("", "Enabled");
	            Serializer.text(Integer.toString(V));
	            Serializer.endTag("", "Enabled");
		        //. 
	            Serializer.startTag("", "Components");
	            	for (int I = 0; I < Components.size(); I++) {
	    	            Serializer.startTag("", "C"+Integer.toString(I));
	    	            	TComponent Component = Components.get(I);
	    	            	//.
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
								for (int J = 0; J < Component.Channels.length; J++) {
									if (J != 0)
										SB.append(","+Integer.toString(Component.Channels[J]));
									else
										SB.append(Integer.toString(Component.Channels[J]));
								}
		    		            Serializer.startTag("", "Channels");
		    		            Serializer.text(SB.toString());
		    		            Serializer.endTag("", "Channels");
	    		            }
	    		            //.
	    		            if (Component.Descriptor != null) {
		    		            Serializer.startTag("", "Descriptor");
		    		            Serializer.text(Component.Descriptor.ToBase64String());
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
		
		public void SupplyComponentsWithDescriptors(TGeoScopeServer Server) throws Exception {
			for (int I = 0; I < Components.size(); I++) {
				TComponent Component = Components.get(I);
				//.
				if ((Component.Descriptor == null) && (Component.idTComponent == SpaceDefines.idTDataStream)) {
					TDataStreamFunctionality DSF = (TDataStreamFunctionality)TSpace.Space.TypesSystem.SystemTDataStream.TComponentFunctionality_Create(Server,Component.idComponent);
					try {
						Component.Descriptor = DSF.GetDescriptor();
					}
					finally {
						DSF.Release();
					}
				}
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
			DataStreamerModule = pDataStreamerModule;
			StreamingComponents = pStreamingComponents;
			//.
			Start();
		}
		
		public void Destroy() {
			Stop();
		}
		
    	public void Start() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public void Stop() {
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
						DataStreamerModule.Device.Log.WriteError("DataStreamerModule.Streaming",S);
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
				DataStreamerModule.Device.Log.WriteError("DataStreamerModule.Streaming",S);
        	}
		}

		private void StartStreamers() throws Exception {
			for (int I = 0; I < StreamingComponents.Components.size(); I++) {
				TStreamingComponents.TComponent Component = StreamingComponents.Components.get(I);
				//.
				if (Component.Enabled) {
					//. try to get descriptor
					if (Component.Descriptor == null) { 
						TUserAgent UserAgent = TUserAgent.GetUserAgent();
						if (UserAgent != null) {
							TDataStreamFunctionality DSF = (TDataStreamFunctionality)TSpace.Space.TypesSystem.SystemTDataStream.TComponentFunctionality_Create(UserAgent.Server,2);
							Component.Descriptor = DSF.GetDescriptor();
						}
					}
					//.
					if (Component.Descriptor != null) {
						for (int J = 0; J < Component.Descriptor.Channels.size(); J++) {
							TDataStreamDescriptor.TChannel Chanel = Component.Descriptor.Channels.get(J); 
							if ((Component.Channels == null) || Component.Channels_ChannelExists(Chanel.ID)) {
								TComponentDataStreaming.TStreamer Streamer = GetStreamer(Chanel.TypeID, DataStreamerModule.Device, Component.idTComponent,Component.idComponent, Chanel.ID, Chanel.Configuration, Chanel.Parameters);
								if (Streamer != null) {
									Streamers.add(Streamer);
									//.
									Streamer.Start();
								}
								else 
									DataStreamerModule.Device.Log.WriteWarning("DataStreamerModule.Streaming","Streamer not found for TypeID: "+Chanel.TypeID);
							}
						}
					}
				}
			}
		}
		
		private void StopStreamers() {
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
		//.
		if (StreamingComponents.Enabled)
			ReStartStreaming();
    }
    
    public void Destroy() {
    	StopStreaming();
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
    	GetStreamingComponents();
    }
    
    public synchronized void GetStreamingComponents() throws Exception {
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
    	if (StreamingComponents.Enabled)
    		ReStartStreaming();
    	else
    		StopStreaming();
    }
    
    private synchronized void SaveStreamingComponents() throws Exception {
    	SetStreamingComponents(StreamingComponents);
    }
    
    private synchronized void ReStartStreaming() {
    	StopStreaming();
    	Streaming = new TStreaming(this, StreamingComponents);
    }

    private synchronized void StopStreaming() {
    	if (Streaming != null) {
    		Streaming.Destroy();
    		Streaming = null;
    	}
    }
    
    public synchronized void SetActive(boolean Value) throws Exception {
    	if (StreamingComponents.Enabled == Value)
    		return; //. ->
    	StreamingComponents.Enabled = Value;
    	//.
    	SaveStreamingComponents();
    }
}
