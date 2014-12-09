package com.geoscope.Classes.Data.Stream.Channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;


public class TChannel {
	
	public static TChannel GetChannelFromXMLNode(Node ANode, TChannelProvider pChannelProvider) throws Exception {
		int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			try {
				String TypeID = TMyXML.SearchNode(ANode,"TypeID").getFirstChild().getNodeValue();
				//.
				TChannel Channel;
				if (pChannelProvider != null)
					Channel = pChannelProvider.GetChannel(TypeID);
				else 
					Channel = new TChannel();
				//.
				return Channel; //. ->
			}
			catch (Exception E) {
    			throw new Exception("error of parsing channel descriptor: "+E.getMessage()); //. =>
			}
		default:
			throw new Exception("unknown channel descriptor version, version: "+Integer.toString(Version)); //. =>
		}
	}
	
	public static TChannel GetChannelFromByteArray(byte[] BA, TChannelProvider pChannelProvider) throws Exception {
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
		return GetChannelFromXMLNode(RootNode, pChannelProvider);
	}
	
	private static int 				NextID = 1;
	public static synchronized int 	GetNextID() {
		NextID++;
		return NextID;
	}
	
	public static final int CHANNEL_KIND_IN		= 0;
	public static final int CHANNEL_KIND_OUT	= 1;
	public static final int CHANNEL_KIND_INOUT	= 2; 
	
	public static class TConfigurationParser {
		
		private static final String VersionDelimiter = ":";
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
		
		private static final String VersionDelimiter = ":";
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
	
	
	public long 	UserID = 0;
	public String 	UserAccessKey = null;
	//.
	public int 	ID = -1;
	public boolean Enabled = true;
	public int Kind = CHANNEL_KIND_OUT;
	public int 	DataFormat = 0;
	public String Name = "";
	public String Info = "";
	public int 	Size = 0;
	public String Configuration = "";
	public String Parameters = "";
	//.
	public TDataTypes DataTypes = null;
	
	public String GetTypeID() {
		return null;
	}
	
	public void Assign(TChannel AChannel) {
		ID = AChannel.ID;
		Enabled = AChannel.Enabled;
		Kind = AChannel.Kind;
		DataFormat = AChannel.DataFormat;
		Name = AChannel.Name;
		Info = AChannel.Info;
		Size = AChannel.Size;
		Configuration = AChannel.Configuration;
		Parameters = AChannel.Parameters;
		DataTypes = AChannel.DataTypes;
	}
	
	public void FromXMLNode(Node ANode) throws Exception {
		ID = Integer.parseInt(TMyXML.SearchNode(ANode,"ID").getFirstChild().getNodeValue());
		//.
		Enabled = true;
		Node _Node = TMyXML.SearchNode(ANode,"Enabled");
		if (_Node != null) {
			Node ValueNode = _Node.getFirstChild();
			if (ValueNode != null)
				Enabled = (Integer.parseInt(ValueNode.getNodeValue()) != 0);
		}
		//.
		Kind = TChannel.CHANNEL_KIND_IN;
		_Node = TMyXML.SearchNode(ANode,"Kind");
		if (_Node != null) {
			Node ValueNode = _Node.getFirstChild();
			if (ValueNode != null)
				Kind = Integer.parseInt(ValueNode.getNodeValue());
		}
		//.
		DataFormat = Integer.parseInt(TMyXML.SearchNode(ANode,"DataFormat").getFirstChild().getNodeValue());
		//.
		Name = TMyXML.SearchNode(ANode,"Name").getFirstChild().getNodeValue();
		//.
		Info = TMyXML.SearchNode(ANode,"Info").getFirstChild().getNodeValue();
		//.
		Size = Integer.parseInt(TMyXML.SearchNode(ANode,"Size").getFirstChild().getNodeValue());
		//.
		Node ValueNode = TMyXML.SearchNode(ANode,"Configuration").getFirstChild();
		if (ValueNode != null)
			Configuration = ValueNode.getNodeValue();
		else
			Configuration = "";
		//.
		ValueNode = TMyXML.SearchNode(ANode,"Parameters").getFirstChild();
		if (ValueNode != null)
			Parameters = ValueNode.getNodeValue();
		else
			Parameters = "";
		//. get channel DataTypes
		_Node = TMyXML.SearchNode(ANode,"DataTypes");
		if (_Node != null) {
			DataTypes = new TDataTypes();
			DataTypes.FromXMLNode(_Node, this);
		}
		//.
		Parse();
	}
	
	public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
    	//. TypeID
        Serializer.startTag("", "TypeID");
        Serializer.text(GetTypeID());
        Serializer.endTag("", "TypeID");
    	//. ID
        Serializer.startTag("", "ID");
        Serializer.text(Integer.toString(ID));
        Serializer.endTag("", "ID");
    	//. Enabled
        Serializer.startTag("", "Enabled");
        int V = 0;
        if (Enabled)
        	V = 1;
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "Enabled");
    	//. Kind
        Serializer.startTag("", "Kind");
        Serializer.text(Integer.toString(Kind));
        Serializer.endTag("", "Kind");
    	//. DataFormat
        Serializer.startTag("", "DataFormat");
        Serializer.text(Integer.toString(DataFormat));
        Serializer.endTag("", "DataFormat");
    	//. Name
        Serializer.startTag("", "Name");
        Serializer.text(Name);
        Serializer.endTag("", "Name");
    	//. Info
        Serializer.startTag("", "Info");
        Serializer.text(Info);
        Serializer.endTag("", "Info");
    	//. Size
        Serializer.startTag("", "Size");
        Serializer.text(Integer.toString(Size));
        Serializer.endTag("", "Size");
    	//. Configuration
        Serializer.startTag("", "Configuration");
        Serializer.text(Configuration);
        Serializer.endTag("", "Configuration");
    	//. Parameters
        Serializer.startTag("", "Parameters");
        Serializer.text(Parameters);
        Serializer.endTag("", "Parameters");
        //. DataTypes
        if (DataTypes != null) {
            Serializer.startTag("", "DataTypes");
            DataTypes.ToXMLSerializer(Serializer);
            Serializer.endTag("", "DataTypes");
        }
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
				FromXMLNode(RootNode);
			}
			catch (Exception E) {
    			throw new Exception("error of parsing channel descriptor: "+E.getMessage()); //. =>
			}
			break; //. >
		default:
			throw new Exception("unknown channel descriptor version, version: "+Integer.toString(Version)); //. =>
		}
	}
	
    public byte[] ToByteArray() throws Exception {
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
	        ToXMLSerializer(Serializer);
	        //.
	        Serializer.endTag("", "ROOT");
	        Serializer.endDocument();
	        //.
			return BOS.toByteArray(); //. ->
	    }
	    finally {
	    	BOS.close();
	    }
    }
    
	public void Parse() throws Exception {
	}
	
	public void StartSource() {
	}

	public void StopSource() {
	}
	
	public boolean DestinationIsConnected() {
		return false;
	}
}