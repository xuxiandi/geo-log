package com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.UserMessaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.util.Xml;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedDataContainerType;

public class TUserMessagingParametersDataType extends TDataType {

	public static String ID() {
		return "UserMessagingParameters";
	}
	
	public static final short TYPE_UNKNOWN 			= 0;
	public static final short TYPE_XML 				= 1;
	
	public static class TParameters {
		
		public long UserID = 0;
		public int CheckpointInterval = -1;
		
		public void FromXMLNode(Node ANode) throws Exception {
			int Version = Integer.parseInt(TMyXML.SearchNode(ANode,"Version").getFirstChild().getNodeValue());
			switch (Version) {
			case 1:
				try {
					Node _Node = TMyXML.SearchNode(ANode,"UserID");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							UserID = Long.parseLong(ValueNode.getNodeValue());
					}
					_Node = TMyXML.SearchNode(ANode,"CheckpointInterval");
					if (_Node != null) {
						Node ValueNode = _Node.getFirstChild();
						if (ValueNode != null)
							CheckpointInterval = Integer.parseInt(ValueNode.getNodeValue());
					}
				}
				catch (Exception E) {
	    			throw new Exception("error of parsing parameters: "+E.getMessage()); //. =>
				}
				break; //. >
			default:
				throw new Exception("unknown parameters version, version: "+Integer.toString(Version)); //. =>
			}
		}
		
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
			int Version = 1;
			//.
	        Serializer.startTag("", "ROOT");
	        //. Version
	        Serializer.startTag("", "Version");
	        Serializer.text(Integer.toString(Version));
	        Serializer.endTag("", "Version");
	    	//. UserID
	        Serializer.startTag("", "UserID");
	        Serializer.text(Long.toString(UserID));
	        Serializer.endTag("", "UserID");
	    	//. CheckpointInterval
	        Serializer.startTag("", "CheckpointInterval");
	        Serializer.text(Integer.toString(CheckpointInterval));
	        Serializer.endTag("", "CheckpointInterval");
	        //.
	        Serializer.endTag("", "ROOT");
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
			FromXMLNode(RootNode);
		}
		
	    public byte[] ToByteArray() throws Exception {
		    XmlSerializer Serializer = Xml.newSerializer();
		    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		    try {
		        Serializer.setOutput(BOS,"UTF-8");
		        Serializer.startDocument("UTF-8",true);
		        //. 
		        ToXMLSerializer(Serializer);
		        //.
		        Serializer.endDocument();
		        //.
				return BOS.toByteArray(); //. ->
		    }
		    finally {
		    	BOS.close();
		    }
	    }
	}
	
	public TUserMessagingParametersDataType() {
		super();
	}
	
	public TUserMessagingParametersDataType(TContainerType pContainerType, TChannel pChannel) {
		super(pContainerType, ID(), pChannel);
	}
	
	public TUserMessagingParametersDataType(TContainerType pContainerType, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		super(pContainerType, ID(), pChannel, pID, pName,pInfo, pValueUnit);
	}
	
	public TTimestampedTypedDataContainerType.TValue ContainerValue() throws WrongContainerTypeException {
		if (ContainerType instanceof TTimestampedTypedDataContainerType)
			return ((TTimestampedTypedDataContainerType)ContainerType).Value.Clone(); //. ->
		else
			throw new WrongContainerTypeException(); //. =>
	}

	@Override
	public String GetValueString(Context context) throws WrongContainerTypeException {
		String Result = "?";
		return Result;
	}
}
