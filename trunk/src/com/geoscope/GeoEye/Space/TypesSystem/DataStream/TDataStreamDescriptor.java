package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.util.Base64;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;

public class TDataStreamDescriptor {

	public static class TChannel {
		
		public static class TConfigurationParser {
			
			private static final String VersionDelimiter = "!";
			private static final String CoderDecoderDelimiter = ";";
			private static final String ParameterDelimiter = ",";
			
			public String[] CoderConfiguration = null;
			public String[] DecoderConfiguration = null;
			
			public TConfigurationParser(String Configuration) throws Exception {
				if ((Configuration != null) && (Configuration.length() > 0)) {
					String[] SA = Configuration.split(VersionDelimiter);
					//.
					int Version = Integer.parseInt(SA[0]);
					if (Version != 1)
						throw new Exception("unknown version"); //. =>
					String CS = SA[1];
					SA = CS.split(CoderDecoderDelimiter);
					//.
					String CoderConfigurationStr = SA[0];
					String DecoderConfigurationStr = SA[1];
					CoderConfiguration = CoderConfigurationStr.split(ParameterDelimiter);
					DecoderConfiguration = DecoderConfigurationStr.split(ParameterDelimiter);
				}
			}
		}
		
		public static class TParametersParser {
			
			private static final String VersionDelimiter = "!";
			private static final String CoderDecoderDelimiter = ";";
			private static final String ParameterDelimiter = ",";
			
			public String[] CoderParameters = null;
			public String[] DecoderParameters = null;
			
			public TParametersParser(String Parameters) throws Exception {
				if ((Parameters != null) && (Parameters.length() > 0)) {
					String[] SA = Parameters.split(VersionDelimiter);
					//.
					int Version = Integer.parseInt(SA[0]);
					if (Version != 1)
						throw new Exception("unknown version"); //. =>
					String PS = SA[1];
					SA = PS.split(CoderDecoderDelimiter);
					//.
					String CoderParametersStr = SA[0];
					String DecoderParametersStr = SA[1];
					CoderParameters = CoderParametersStr.split(ParameterDelimiter);
					DecoderParameters = DecoderParametersStr.split(ParameterDelimiter);
				}
			}
		}
		
		
		public int 	ID = -1;
		public String TypeID = "";
		public boolean Enabled = true;
		public int 	DataFormat = 0;
		public String Name = "";
		public String Info = "";
		public int 	Size = 0;
		public String Configuration = "";
		public String Parameters = "";
	}
	
	public static class TChannelIDs {
		
		public ArrayList<Integer> Items = new ArrayList<Integer>();
		
		public TChannelIDs() {
		}

		public TChannelIDs(byte[] BA) throws IOException {
			FromByteArray(BA);
		}
		
		public void AddID(int ID) {
			Items.add(ID);
		}
		
		public int Count() {
			return Items.size();
		}
		
		public boolean IDExists(int ID) {
    		for (int I = 0; I < Items.size(); I++) 
    			if (Items.get(I) == ID)
    				return true; //. ->
    		return false;
		}
		
		public void FromByteArray(byte[] BA) throws IOException {
			Items.clear();
			int ItemSize = 4; //. SizeOf(Int32)
			int Cnt = (BA.length/ItemSize);
			int Idx = 0;
			for (int I = 0; I < Cnt; I++) {
				int ID = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += ItemSize;
				Items.add(ID);
			}
		}
		
		public byte[] ToByteArray() throws IOException {
			ByteArrayOutputStream OS = new ByteArrayOutputStream();
		    try {
	    		for (int I = 0; I < Items.size(); I++) {
	    			byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(Items.get(I));
	    			OS.write(BA);
	    		}
	    		return OS.toByteArray(); //. ->
		    }
		    finally {
		    	OS.close();	    	
		    }
		}
	}
	
	private byte[] SourceByteArray = null;
	//.
    public String Name = "";
    public String Info = "";
    //.
    public ArrayList<TChannel> Channels = new ArrayList<TChannel>();
    
	public TDataStreamDescriptor(byte[] ByteArray) throws Exception {
		FromByteArray(ByteArray);
	}
	
	public TDataStreamDescriptor(String Base64String) throws Exception {
		FromBase64String(Base64String);
	}
	
	public void FromByteArray(byte[] ByteArray) throws Exception {
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(ByteArray);
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
    			Channels.clear();
				NodeList ChannelsNode = TMyXML.SearchNode(RootNode,"Channels").getChildNodes();
				int Cnt = ChannelsNode.getLength();
				for (int I = 0; I < Cnt; I++) {
					Node ChannelNode = ChannelsNode.item(I);
					//.
					if (ChannelNode.getLocalName() != null) {
						TChannel Channel = new TChannel();
						//.
						Channel.ID = Integer.parseInt(TMyXML.SearchNode(ChannelNode,"ID").getFirstChild().getNodeValue());
						Channel.TypeID = TMyXML.SearchNode(ChannelNode,"TypeID").getFirstChild().getNodeValue();
						//.
						Node _Node = TMyXML.SearchNode(ChannelNode,"Enabled");
						if (_Node != null) {
							Node ValueNode = _Node.getFirstChild();
							if (ValueNode != null)
								Channel.Enabled = (Integer.parseInt(ValueNode.getNodeValue()) != 0);
						}
						//.
						Channel.DataFormat = Integer.parseInt(TMyXML.SearchNode(ChannelNode,"DataFormat").getFirstChild().getNodeValue());
						Channel.Name = TMyXML.SearchNode(ChannelNode,"Name").getFirstChild().getNodeValue();
						Channel.Info = TMyXML.SearchNode(ChannelNode,"Info").getFirstChild().getNodeValue();
						Channel.Size = Integer.parseInt(TMyXML.SearchNode(ChannelNode,"Size").getFirstChild().getNodeValue());
						//.
						Node ValueNode = TMyXML.SearchNode(ChannelNode,"Configuration").getFirstChild();
						if (ValueNode != null)
							Channel.Configuration = ValueNode.getNodeValue();
						//.
						ValueNode = TMyXML.SearchNode(ChannelNode,"Parameters").getFirstChild();
						if (ValueNode != null)
							Channel.Parameters = ValueNode.getNodeValue();
						//.
	    				Channels.add(Channel);
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
		//.
		SourceByteArray = ByteArray;
	}
	
	public void FromBase64String(String S) throws Exception {
		FromByteArray(Base64.decode(S, Base64.NO_WRAP));
	}
	
	public String ToBase64String() throws IOException {
		if (SourceByteArray == null)
			return null; //. ->
		return Base64.encodeToString(SourceByteArray, 0,SourceByteArray.length, Base64.NO_WRAP);
	}
}
