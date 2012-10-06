package com.geoscope.GeoEye;

import java.io.IOException;
import java.util.ArrayList;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.geoscope.GeoLog.Utils.TMyXML;

public class TTileServerVisualizationUserData extends TComponentUserData {

	private static String TTileServerVisualizationTag = "T"+Integer.toString(SpaceDefines.idTTileServerVisualization);
	
	public class TTileServerVisualizationProvider {
		public int		ID;
		public String 	Name;
	}
	
	public class TTileServerVisualization {
		public int		ID;
		public String 	Name;
		public int		CurrentProvider;
		private Node	CurrentProviderNode;
		public ArrayList<TTileServerVisualizationProvider> Providers = new ArrayList<TTileServerVisualizationProvider>();
		
		public void SetCurrentProvider(int ProviderID) throws IOException {
			CurrentProvider = ProviderID;
			CurrentProviderNode.setNodeValue(Integer.toString(CurrentProvider));
			//.
			Save();
		}
	}
	
	public ArrayList<TTileServerVisualization> TileServerVisualizations;
	
	public TTileServerVisualizationUserData() throws Exception {
		super();
	}
	
	@Override
	public void Load() throws Exception {
		super.Load();
		if (RootNode == null)
			return; //. ->
		//.
		NodeList ItemsNL = RootNode.getElementsByTagName("Items");
		Node ItemsNode = ItemsNL.item(0);
		Node TileServerVisualizationNode = TMyXML.SearchNode(ItemsNode, TTileServerVisualizationTag);
		if (TileServerVisualizationNode == null)
			return; //. ->
		TileServerVisualizations = new ArrayList<TTileServerVisualization>();
		NodeList ComponentNodes = TileServerVisualizationNode.getChildNodes();
		for (int I = 0; I < ComponentNodes.getLength(); I++) {
			Node ComponentNode = ComponentNodes.item(I); 
			String CID = ComponentNode.getLocalName();
			if (CID != null) {
				TTileServerVisualization V = new TTileServerVisualization();
				V.ID = Integer.parseInt(CID.substring(1));
				V.Name = TMyXML.SearchNode(ComponentNode, "Name").getFirstChild().getNodeValue();
				Node CurrentProviderNode = TMyXML.SearchNode(ComponentNode, "CurrentProvider").getFirstChild();
				V.CurrentProvider = Integer.parseInt(CurrentProviderNode.getNodeValue());
				V.CurrentProviderNode = CurrentProviderNode;
				//. get Providers
				NodeList ProviderNodes = TMyXML.SearchNode(ComponentNode, "Providers").getChildNodes();
				for (int J = 0; J < ProviderNodes.getLength(); J++) {
					Node ProviderNode = ProviderNodes.item(J);
					String PID = ProviderNode.getLocalName();
					if (PID != null) {
						TTileServerVisualizationProvider P = new TTileServerVisualizationProvider();
						P.ID = Integer.parseInt(PID.substring(1));
						P.Name = TMyXML.SearchNode(ProviderNode, "Name").getFirstChild().getNodeValue();
						//.
						V.Providers.add(P);
					}
				}
				//.
				TileServerVisualizations.add(V);
			}
		}
	}
	
	public byte[] ToByteArrayV1() throws IOException {
		if (TileServerVisualizations == null)
			return null; //. ->
		int ItemDataSize = 6;
		int ItemSize = 8/*SizeOf(ItemID)*/+2/*SizeOf(ItemDataSize)*/+ItemDataSize;
		int ItemsDataSize = TileServerVisualizations.size()*ItemSize; 
		int UserDataSize = 2/*SizeOf(idTTileServerVisualization)*/+4/*SizeOf(ItemsDataSize)*/+ItemsDataSize;
		byte[] UserData = new byte[UserDataSize]; 
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt16ToBEByteArray((short)SpaceDefines.idTTileServerVisualization);
		System.arraycopy(BA,0, UserData,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertInt32ToBEByteArray(ItemsDataSize);
		System.arraycopy(BA,0, UserData,Idx, BA.length); Idx += BA.length;
		for (int I = 0; I < TileServerVisualizations.size(); I++) {
			TTileServerVisualization V = TileServerVisualizations.get(I);
			BA = TDataConverter.ConvertInt32ToBEByteArray(V.ID);
			System.arraycopy(BA,0, UserData,Idx, BA.length); Idx += BA.length+4/*extends to Int64*/;
			//.
			BA = TDataConverter.ConvertInt16ToBEByteArray((short)ItemDataSize);
			System.arraycopy(BA,0, UserData,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt16ToBEByteArray((short)1/*item version*/);
			System.arraycopy(BA,0, UserData,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(V.CurrentProvider); //. ProviderID
			System.arraycopy(BA,0, UserData,Idx, BA.length); Idx += BA.length;
		}
		return UserData;
	}
}
