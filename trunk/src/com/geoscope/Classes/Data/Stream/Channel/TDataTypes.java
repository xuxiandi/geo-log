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

	public void FromXMLNode(Node ANode, TChannel pChannel) throws Exception {
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
						TDataType DataType = ContainerType.GetDataType(TypeID, pChannel);
						//.
						DataType.FromXMLNode(DataTypeNode);
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
        	//.
        	DataType.ToXMLSerializer(Serializer);
            //.
            Serializer.endTag("", DataTypeNodeName);
        }
	}
	
	public TDataType AddItem(TDataType Item) {
		Item.Index = Items.size(); 
		Items.add(Item);
		return Item;
	}
	
	public int Count() {
		return Items.size();
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

	public TDataType GetItemByClass(Class<?> ItemClass) {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TDataType Item = Items.get(I);
			if (Item.getClass() == ItemClass)
				return Item; //. ->
		}
		return null;
	}
}
