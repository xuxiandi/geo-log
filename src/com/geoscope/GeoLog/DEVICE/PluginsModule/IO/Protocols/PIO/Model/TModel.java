package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import android.util.Base64;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.TSourceModel;

public class TModel extends TSourceModel {

	private TPluginModule PluginModule;
	//.
    public String Name = "";
    public String Info = "";
    //.
    public TStreamDescriptor Stream;
    public TStreamDescriptor ControlStream;
    
	public TModel(TPluginModule pPluginModule) {
		PluginModule = pPluginModule;
		//.
		Stream = new TStreamDescriptor();
		ControlStream = new TStreamDescriptor();
	}
	
	public TModel(TPluginModule pPluginModule, String S) throws Exception {
		PluginModule = pPluginModule;
		//.
		FromBase64String(S);	
	}
	
	public void FromByteArray(byte[] BA) throws Exception {
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
				Node node;
    			Name = "";
    			node = TMyXML.SearchNode(RootNode,"Name").getFirstChild();
    			if (node != null)
    				Name = node.getNodeValue();
    			Info = "";
    			node = TMyXML.SearchNode(RootNode,"Info").getFirstChild();
    			if (node != null)
    				Info = node.getNodeValue();
    			//.
    			Node StreamNode = TMyXML.SearchNode(RootNode,"Stream");
    			if (StreamNode != null) {
    				com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.TChannelsProvider ChannelsProvider = new com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.TChannelsProvider(PluginModule);
    				Stream = new TStreamDescriptor(StreamNode, ChannelsProvider);
    			}
    			else
    				Stream = null;
    			//.
    			Node ControlStreamNode = TMyXML.SearchNode(RootNode,"ControlStream");
    			if (ControlStreamNode != null) {
    				com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.TChannelsProvider ChannelsProvider = new com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.TChannelsProvider(PluginModule); 
    				ControlStream = new TStreamDescriptor(ControlStreamNode, ChannelsProvider);
    			}
    			else
    				ControlStream = null;
			}
			catch (Exception E) {
    			throw new Exception("error of parsing model data: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown model data version, version: "+Integer.toString(Version)); //. =>
		}
	}

	public void FromString(String S) throws Exception {
		byte[] XML = S.getBytes("utf-8");
		FromByteArray(XML);
	}

	public void FromBase64String(String S) throws Exception {
		FromByteArray(Base64.decode(S, Base64.NO_WRAP));
	}
	
	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		if (Stream != null) {
			int Cnt = Stream.Channels.size();
			for (int I = 0; I < Cnt; I++) 
				((TStreamChannel)Stream.Channels.get(I)).DoOnCommandResponse(Command);
		}
		if (ControlStream != null) {
			int Cnt = ControlStream.Channels.size();
			for (int I = 0; I < Cnt; I++) 
				((TStreamChannel)ControlStream.Channels.get(I)).DoOnCommandResponse(Command);
		}
		return true;
	}
}
