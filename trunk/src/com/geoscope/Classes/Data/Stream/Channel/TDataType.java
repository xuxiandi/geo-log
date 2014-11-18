package com.geoscope.Classes.Data.Stream.Channel;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;


public class TDataType {

	public static class WrongContainerTypeException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public WrongContainerTypeException() {
			super("");
		}
	}

	public static class TDataTrigger {
		
		public static class THandler {
			
			public static String GetTypeID() {
				return "";
			}
			
			
			protected synchronized void DoOnValue(TDataType DataType) {
			}
		}
		
		public static class TAlarmer extends THandler {

			public int			AlarmLevel = -1; //. unknown
			public double		AlarmTimestamp = 0.0;
			public String  		AlarmSeverity = "";
			public String  		AlarmID = "";
			public TDataType 	AlarmDataType = null;
			public String  		AlarmNotification = "";
		}
		

		protected TDataType DataType = null;
		//.
		public String TypeID = "";
		//.
		public String 				HandlerTypeID = "";
		private THandler 			Handler = null;

		public TDataTrigger() {
		}
					
		public TDataTrigger(String pTypeID, String pHandlerTypeID) {
			TypeID = pTypeID;
			HandlerTypeID = pHandlerTypeID;
		}
		
		public TDataTrigger(TDataType pDataType) {
			DataType = pDataType;
		}
		
		public TDataTrigger(TDataType pDataType, String pTypeID, String pHandlerTypeID) {
			DataType = pDataType;
			//.
			TypeID = pTypeID;
			HandlerTypeID = pHandlerTypeID;
		}
		
		public void FromXMLNode(Node ANode) throws Exception {
			TypeID = TMyXML.SearchNode(ANode,"TypeID").getFirstChild().getNodeValue();
			HandlerTypeID = TMyXML.SearchNode(ANode,"HandlerTypeID").getFirstChild().getNodeValue();
		}

		public void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
	    	//. TypeID
	        Serializer.startTag("", "TypeID");
	        Serializer.text(TypeID);
	        Serializer.endTag("", "TypeID");
	    	//. HandlerTypeID
	        Serializer.startTag("", "HandlerTypeID");
	        Serializer.text(HandlerTypeID);
	        Serializer.endTag("", "HandlerTypeID");
		}		
		
		public synchronized void SetHandler(THandler pHandler) {
			Handler = pHandler;
		}
		
		public synchronized void Process() {
			if (Handler != null)
				Handler.DoOnValue(DataType);
		}
	}
	
	public static class TDataTriggers {
		
		private TDataType DataType;
		//.
		private ArrayList<TDataTrigger> Items = new ArrayList<TDataTrigger>();

		public TDataTriggers(TDataType pDataType) {
			DataType = pDataType;
		}
		
		public void Add(TDataTrigger Trigger) {
			Items.add(Trigger);
		}
		
		public ArrayList<TDataTrigger> GetItems() {
			return Items;
		}
		
		public void FromXMLNode(Node ANode) throws Exception {
			NodeList TriggerNodes = ANode.getChildNodes();
			int Cnt = TriggerNodes.getLength();
			for (int I = 0; I < Cnt; I++) {
				Node TriggerNode = TriggerNodes.item(I);
				//.
				TDataTrigger Trigger = new TDataTrigger(DataType);
				Trigger.FromXMLNode(TriggerNode);
				//.
				Items.add(Trigger);
			}
		}

		public void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
            int Cnt = Items.size();
            for (int I = 0; I < Cnt; I++) {
            	String TriggerNodeName = "TR"+Integer.toString(I);
                Serializer.startTag("", TriggerNodeName);
                //.
            	TDataTrigger Trigger = Items.get(I);
            	//.
            	Trigger.ToXMLSerializer(Serializer);
                //.
                Serializer.endTag("", TriggerNodeName);
            }
		}
		
		public void Process() {
            int Cnt = Items.size();
            for (int I = 0; I < Cnt; I++) 
            	Items.get(I).Process();
		}
	}
	
	public TContainerType ContainerType;
	//.
	public TChannel Channel;
	//.
	public String TypeID = "";
	//.
	public short ID = 0;
	//.
	public int Index = 0;
	//.
	public String Name = "";
	public String Info = "";
	//.
	public String ValueUnit = "";
	//.
	public TDataTriggers Triggers = null;
	
	public TDataType(TContainerType pContainerType, String pTypeID, TChannel pChannel) {
		ContainerType = pContainerType;
		TypeID = pTypeID;
		Channel = pChannel;
	}
	
	public TDataType(TContainerType pContainerType, String pTypeID, TChannel pChannel, int pID, String pName, String pInfo, String pValueUnit) {
		ContainerType = pContainerType;
		TypeID = pTypeID;
		Channel = pChannel;
		ID = (short)pID;
		Name = pName;
		Info = pInfo;
		ValueUnit = pValueUnit;
	}
	
	public TDataType Clone() {
		TDataType Result = new TDataType(ContainerType.Clone(), TypeID, Channel, ID, Name, Info, ValueUnit);
		return Result;
	}
	
	public void FromXMLNode(Node ANode) throws Exception {
		ID = Short.parseShort(TMyXML.SearchNode(ANode,"ID").getFirstChild().getNodeValue());
		//.
		Node _Node = TMyXML.SearchNode(ANode,"Name");
		if (_Node != null) {
			Node ValueNode = _Node.getFirstChild();
			if (ValueNode != null)
				Name = ValueNode.getNodeValue();
		}
		//.
		_Node = TMyXML.SearchNode(ANode,"Info");
		if (_Node != null) {
			Node ValueNode = _Node.getFirstChild();
			if (ValueNode != null)
				Info = ValueNode.getNodeValue();
		}
		//.
		_Node = TMyXML.SearchNode(ANode,"ValueUnit");
		if (_Node != null) {
			Node ValueNode = _Node.getFirstChild();
			if (ValueNode != null)
				ValueUnit = ValueNode.getNodeValue();
		}
		//.
		Node TriggersNode = TMyXML.SearchNode(ANode,"Triggers");
		if (TriggersNode != null) {
			Triggers = new TDataTriggers(this);
			Triggers.FromXMLNode(TriggersNode);
		}
	}

	public void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
    	//. ContainerTypeID
        Serializer.startTag("", "ContainerTypeID");
        Serializer.text(GetContainerTypeID());
        Serializer.endTag("", "ContainerTypeID");
    	//. TypeID
        Serializer.startTag("", "TypeID");
        Serializer.text(TypeID);
        Serializer.endTag("", "TypeID");
    	//. ID
        Serializer.startTag("", "ID");
        Serializer.text(Short.toString(ID));
        Serializer.endTag("", "ID");
    	//. Name
        if (Name.length() > 0) {
            Serializer.startTag("", "Name");
            Serializer.text(Name);
            Serializer.endTag("", "Name");
        }
    	//. Info
        if (Info.length() > 0) {
            Serializer.startTag("", "Info");
            Serializer.text(Info);
            Serializer.endTag("", "Info");
        }
    	//. ValueUnit
        if (ValueUnit.length() > 0) {
            Serializer.startTag("", "ValueUnit");
            Serializer.text(ValueUnit);
            Serializer.endTag("", "ValueUnit");
        }
        //. Triggers
        if (Triggers != null) {
            Serializer.startTag("", "Triggers");
        	Triggers.ToXMLSerializer(Serializer);
            Serializer.endTag("", "Triggers");
        }
	}
	
	public String GetContainerTypeID() {
		return ContainerType.GetID();
	}

	public void SetContainerTypeValue(Object Value) {
		ContainerType.SetValue(Value);
		//.
		Triggers_CheckValueFor();
	}
	
	public Object GetContainerTypeValue() {
		return ContainerType.GetValue();
	}
	
	public String GetName(Context context) {
		if (Name.length() > 0)
			return Name; //. ->
		else
			return TypeID; //. ->
	}

	public String GetValueString(Context context) throws WrongContainerTypeException {
		return ContainerType.GetValueString(context);
	}
	
	public String GetValueUnit(Context context) {
		return ValueUnit;
	}
	
	public String GetValueAndUnitString(Context context) throws WrongContainerTypeException {
		String VU = GetValueUnit(context);
		String Result = GetValueString(context);
		if (VU.length() > 0)
			Result += " "+VU;
		return Result;
	}
	
	public void Triggers_Add(TDataTrigger Trigger) {
		if (Triggers == null)
			Triggers = new TDataTriggers(this);
		Triggers.Add(Trigger);
		Trigger.DataType = this;
	}
	
	public TDataTriggers Triggers_Get() {
		return Triggers;
	}
	
	public void Triggers_CheckValueFor() {
		if (Triggers != null)
			Triggers.Process();
	}
}
