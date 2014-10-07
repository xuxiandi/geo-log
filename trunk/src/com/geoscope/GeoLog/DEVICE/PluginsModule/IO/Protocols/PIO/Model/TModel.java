package com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.TStreamChannel;

public class TModel {

    public String Name = "";
    public String Info = "";
    //.
    public TStreamDescriptor Stream;
    public TStreamDescriptor ControlStream;
    
	public TModel(String S) throws Exception {
		FromString(S);	
	}
	
	public void FromString(String S) throws Exception {
		byte[] XML = S.getBytes("utf-8");
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
    			Name = TMyXML.SearchNode(RootNode,"Name").getFirstChild().getNodeValue();
    			Info = TMyXML.SearchNode(RootNode,"Info").getFirstChild().getNodeValue();
    			//.
    			Node StreamNode = TMyXML.SearchNode(RootNode,"Stream");
    			if (StreamNode != null)
    				Stream = new TStreamDescriptor(StreamNode, com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.TChannelsProvider.Instance);
    			else
    				Stream = null;
    			//.
    			Node ControlStreamNode = TMyXML.SearchNode(RootNode,"ControlStream");
    			if (ControlStreamNode != null)
    				ControlStream = new TStreamDescriptor(ControlStreamNode, com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.TChannelsProvider.Instance);
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

	public boolean DoOnCommandResponse(PIO.TCommand Command) throws Exception {
		if (Stream != null) {
			int Cnt = Stream.Channels.size();
			for (int I = 0; I < Cnt; I++) 
				if (((TStreamChannel)Stream.Channels.get(I)).DoOnCommandResponse(Command))
					return true; //. ->
		}
		if (ControlStream != null) {
			int Cnt = ControlStream.Channels.size();
			for (int I = 0; I < Cnt; I++) 
				if (((TStreamChannel)ControlStream.Channels.get(I)).DoOnCommandResponse(Command))
					return true; //. ->
		}
		return false;
	}
}
