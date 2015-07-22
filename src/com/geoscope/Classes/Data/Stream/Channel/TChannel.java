package com.geoscope.Classes.Data.Stream.Channel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.app.Activity;
import android.content.Intent;
import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.UI.TChannelProfilePanel;
import com.geoscope.Classes.MultiThreading.TCanceller;


public class TChannel {
	
	public static final int NextID = 15; //. a next unique channel ID
	
	public static boolean TypeIsTypeOfChannel(String TypeID, String ChannelTypeID) {
		return (TypeID.startsWith(ChannelTypeID));
	}
	
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
	
	public static final int CHANNEL_KIND_IN		= 0;
	public static final int CHANNEL_KIND_OUT	= 1;
	public static final int CHANNEL_KIND_INOUT	= 2; 
	
	public static final String ConfigurationFileName = "Configuration";
	
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
	
	public static final String ParametersFileName = "Parameters";
	
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

	public static class TProfile {
		
		private static final String FileName = "Profile.xml"; 
		
		
		public boolean Enabled = true;
		public boolean StreamableViaComponent = false;
		
		public TProfile() {
		}

		public TProfile(byte[] ProfileData) throws Exception {
			FromByteArray(ProfileData);
		}

		public void FromXMLNode(Node ANode) throws Exception {
			//. Enabled
			Node _Node = TMyXML.SearchNode(ANode,"Enabled");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					Enabled = (Integer.parseInt(ValueNode.getNodeValue()) != 0);
			}
			//. StreamableViaComponent
			_Node = TMyXML.SearchNode(ANode,"StreamableViaComponent");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					StreamableViaComponent = (Integer.parseInt(ValueNode.getNodeValue()) != 0);
			}
		}
		
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
	    	//. Enabled
	        Serializer.startTag("", "Enabled");
	        Serializer.text(Enabled ? "1" : "0");
	        Serializer.endTag("", "Enabled");
	    	//. StreamableViaComponent
	        Serializer.startTag("", "StreamableViaComponent");
	        Serializer.text(StreamableViaComponent ? "1" : "0");
	        Serializer.endTag("", "StreamableViaComponent");
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
			try {
				FromXMLNode(RootNode);
			}
			catch (Exception E) {
    			throw new Exception("error of parsing channel profile: "+E.getMessage()); //. =>
			}
		}
		
	    public byte[] ToByteArray() throws Exception {
		    XmlSerializer Serializer = Xml.newSerializer();
		    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		    try {
		        Serializer.setOutput(BOS,"UTF-8");
		        Serializer.startDocument("UTF-8",true);
		        Serializer.startTag("", "ROOT");
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
		
		public Intent GetProfilePanel(Activity Parent) throws Exception {
			Intent Result = new Intent(Parent, TChannelProfilePanel.class);
			Result.putExtra("ProfileData", ToByteArray());
			//.
			return Result;
		}
	}
	
	public static final class ChannelIsActiveException extends IOException {
		
		private static final long serialVersionUID = 1L;

		public ChannelIsActiveException() {
			super("channel is active");
		}
	}
	
	
	public String ProfilesFolder;
	//.
	public TProfile Profile;
	//.
	public int 	ID;
	public boolean Enabled = true;
	public int Kind = CHANNEL_KIND_OUT;
	public int 	DataFormat = 0;
	public String Name = "";
	public String Info = "";
	public int 	Size = 8192;
	public String Configuration = "";
	public String Parameters = "";
	//.
	public TDataTypes DataTypes = null;
	//.
	public long 	UserID = 0;
	public String 	UserAccessKey = null;
	
	public TChannel() {
		ID = -1;
		ProfilesFolder = null;
		Profile = null;
	}

	public TChannel(int pID, String pProfilesFolder, Class<?> ProfileClass) throws Exception {
		ID = pID;
		//.
		ProfilesFolder = pProfilesFolder;
		//.
		Profile = (TProfile)ProfileClass.newInstance();
		Profile_Load();
	}

	public void Close() throws Exception {
		Stop();
	}
	
	public String GetTypeID() {
		return "";
	}
	
	public boolean IsTypeOf(String TypeID) {
		return (GetTypeID().startsWith(TypeID));
	}
	
	public String Folder() {
		if (ProfilesFolder == null)
			return null; //. ->
		String Result = ProfilesFolder+"/"+GetTypeID()+"."+Integer.toString(ID);
		File RF = new File(Result);
		RF.mkdirs();
		return Result;
	}
	
	public void Profile_Load() throws Exception {
		String FN = Folder()+"/"+TProfile.FileName;
		File F = new File(FN);
		if (F.exists()) { 
	    	FileInputStream FIS = new FileInputStream(FN);
	    	try {
	    		byte[] BA = new byte[(int)F.length()];
	    		FIS.read(BA);
	    		//.
	    		Profile.FromByteArray(BA);
	    		//.
				Profile_ApplyToChannel();
	    	}
			finally
			{
				FIS.close(); 
			}
		}
	}
	
	public void Profile_Save() throws Exception {
		File F = new File(Folder());
		F.mkdirs();
		//.
		String FN = F.getAbsolutePath()+"/"+TProfile.FileName;
		FileOutputStream FOS = new FileOutputStream(FN);
        try
        {
        	FOS.write(Profile.ToByteArray());
        }
        finally
        {
        	FOS.close();
        }
	}

	public void Profile_FromByteArray(byte[] BA) throws Exception {
		if (Profile != null) {
			if (IsActive())
				throw new ChannelIsActiveException(); //. =>
			//.
			Profile.FromByteArray(BA);
			//.
			Profile_ApplyToChannel();
			//.
			Profile_Save();
		}
	}
	
	public void Profile_FromXMLNode(Node ANode) throws Exception {
		if (Profile != null) {
			if (IsActive())
				throw new ChannelIsActiveException(); //. =>
			//.
			Profile.FromXMLNode(ANode);
			//.
			Profile_ApplyToChannel();
			//.
			Profile_Save();
		}
	}
	
	public byte[] Profile_ToByteArray() throws Exception {
		if (Profile != null) 
			return Profile.ToByteArray(); //. ->
		else
			return null; //. ->
	}
	
	public void Profile_ToXMLSerializer(XmlSerializer Serializer) throws Exception {
		if (Profile != null) 
			Profile.ToXMLSerializer(Serializer); 
	}
	
	protected void Profile_ApplyToChannel() {
		Enabled = Profile.Enabled;
	}
	
	public void Initialize(Object pParameters) throws Exception {
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
		//.
		ProfilesFolder = AChannel.ProfilesFolder;
	}
	
	public void Parse() throws Exception {
	}

	public boolean Configuration_LoadFromConfigurationFile() throws IOException {
		String CFN = Folder()+"/"+ConfigurationFileName;
		File CF = new File(CFN);
		if (CF.exists()) { 
	    	FileInputStream FIS = new FileInputStream(CF);
	    	try {
	    			byte[] BA = new byte[(int)CF.length()];
	    			FIS.read(BA);
	    			//.
	    			String ConfigurationString = new String(BA, "utf-8");
	    			//.
	    			Configuration = ConfigurationString;
	    			//.
	    			return true; //. ->
	    	}
			finally
			{
				FIS.close(); 
			}
		}
		else
			return false; //. ->
	}
	
	public boolean Parameters_LoadFromParametersFile() throws IOException {
		String PFN = Folder()+"/"+ParametersFileName;
		File PF = new File(PFN);
		if (PF.exists()) { 
	    	FileInputStream FIS = new FileInputStream(PF);
	    	try {
	    			byte[] BA = new byte[(int)PF.length()];
	    			FIS.read(BA);
	    			//.
	    			String ParametersString = new String(BA, "utf-8");
	    			//.
	    			Parameters = ParametersString;
	    			//.
	    			return true; //. ->
	    	}
			finally
			{
				FIS.close(); 
			}
		}
		else
			return false; //. ->
	}
	
	public void Start() throws Exception {
	}

	public void Stop() throws Exception {
	}
	
	public void ReStart() throws Exception {
		Stop();
		Start();
	}

	public void Process(TCanceller Canceller) throws Exception {
	}
	
	public synchronized void FromXMLNode(Node ANode) throws Exception {
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
		Name = "";
		Node ValueNode = TMyXML.SearchNode(ANode,"Name").getFirstChild();
		if (ValueNode != null)
			Name = ValueNode.getNodeValue();
		//.
		Info = "";
		ValueNode = TMyXML.SearchNode(ANode,"Info").getFirstChild();
		if (ValueNode != null)
			Info = ValueNode.getNodeValue();
		//.
		Size = Integer.parseInt(TMyXML.SearchNode(ANode,"Size").getFirstChild().getNodeValue());
		//.
		ValueNode = TMyXML.SearchNode(ANode,"Configuration").getFirstChild();
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
    
	public void StartSource() {
	}

	public void StopSource() {
	}
	
	public boolean IsActive() { //. true if the channel started or the channel source started
		return true;
	}
	
	public boolean DestinationIsConnected() {
		return false;
	}
}