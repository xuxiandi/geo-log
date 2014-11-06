package com.geoscope.Classes.Data.Stream.Channel;

import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;

public class TDataTypes {

	public ArrayList<TDataType> Items = new ArrayList<TDataType>();

	public TDataTypes() {
	}

	public void FromXMLNode(Node ANode) throws Exception {
		try {
			Items.clear();
			NodeList DataTypesNode = ANode.getChildNodes();
			int Cnt = DataTypesNode.getLength();
			for (int I = 0; I < Cnt; I++) {
				Node DataTypeNode = DataTypesNode.item(I);
				//.
				if (DataTypeNode.getLocalName() != null) {
					String ContainerTypeID = TMyXML.SearchNode(DataTypeNode,"ContainerTypeID").getFirstChild().getNodeValue();
					//.
					TContainerType ContainerType = TContainerType.GetInstance(ContainerTypeID);
					//.
					if (ContainerType != null) {
						String TypeID = TMyXML.SearchNode(DataTypeNode,"TypeID").getFirstChild().getNodeValue();
						//.
						TDataType DataType = ContainerType.GetDataType(TypeID);
						//.
						DataType.ID = Short.parseShort(TMyXML.SearchNode(DataTypeNode,"ID").getFirstChild().getNodeValue());
						//.
						Node _Node = TMyXML.SearchNode(DataTypeNode,"Name");
						if (_Node != null) {
							Node ValueNode = _Node.getFirstChild();
							if (ValueNode != null)
								DataType.Name = ValueNode.getNodeValue();
						}
						//.
						_Node = TMyXML.SearchNode(DataTypeNode,"Info");
						if (_Node != null) {
							Node ValueNode = _Node.getFirstChild();
							if (ValueNode != null)
								DataType.Info = ValueNode.getNodeValue();
						}
						//.
						_Node = TMyXML.SearchNode(DataTypeNode,"ValueUnit");
						if (_Node != null) {
							Node ValueNode = _Node.getFirstChild();
							if (ValueNode != null)
								DataType.ValueUnit = ValueNode.getNodeValue();
						}
						//.
						DataType.Index = Items.size(); 
	    				Items.add(DataType);
					}
				}
			}
		}
		catch (Exception E) {
			throw new Exception("error of parsing stream descriptor: "+E.getMessage()); //. =>
		}
	}

	public void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
        int Cnt = Items.size();
        for (int I = 0; I < Cnt; I++) {
        	String DataTypeNodeName = "DT"+Integer.toString(I);
            Serializer.startTag("", DataTypeNodeName);
            //.
        	TDataType DataType = Items.get(I);
        	//. ContainerTypeID
            Serializer.startTag("", "ContainerTypeID");
            Serializer.text(DataType.GetContainerTypeID());
            Serializer.endTag("", "ContainerTypeID");
        	//. TypeID
            Serializer.startTag("", "TypeID");
            Serializer.text(DataType.TypeID);
            Serializer.endTag("", "TypeID");
        	//. ID
            Serializer.startTag("", "ID");
            Serializer.text(Short.toString(DataType.ID));
            Serializer.endTag("", "ID");
        	//. Name
            if (DataType.Name.length() > 0) {
                Serializer.startTag("", "Name");
                Serializer.text(DataType.Name);
                Serializer.endTag("", "Name");
            }
        	//. Info
            if (DataType.Info.length() > 0) {
                Serializer.startTag("", "Info");
                Serializer.text(DataType.Info);
                Serializer.endTag("", "Info");
            }
        	//. ValueUnit
            if (DataType.ValueUnit.length() > 0) {
                Serializer.startTag("", "ValueUnit");
                Serializer.text(DataType.ValueUnit);
                Serializer.endTag("", "ValueUnit");
            }
            //.
            Serializer.endTag("", DataTypeNodeName);
        }
	}
	
	public TDataType AddItem(TDataType Item) {
		Item.Index = Items.size(); 
		Items.add(Item);
		return Item;
	}
	
	public TDataType GetItemByIndex(int Index) {
		return Items.get(Index);
	}
	
	public TDataType GetItemByID(short ID) {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TDataType Item = Items.get(I);
			if (Item.ID == ID)
				return Item; //. ->
		}
		return null;
	}
}
