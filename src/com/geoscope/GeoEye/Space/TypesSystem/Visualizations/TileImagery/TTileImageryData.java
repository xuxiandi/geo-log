package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;

public class TTileImageryData {

	public static final String TileImageryDataFileName = "Data.xml";
	
	public static class TTileServerProviderCompilation {
		public int		ID;
		public String 	Name;
		public String 	Info;
		public int 		LayGroup;
	}
	
	public static class TTileServerProvider {
		public int		ID;
		public String 	Name;
		public String 	Info;
		public ArrayList<TTileServerProviderCompilation> Compilations = new ArrayList<TTileServerProviderCompilation>();
		
		public int CompilationsCount() {
			return Compilations.size(); 
		}

		public TTileServerProviderCompilation GetCompilation(int CompilationID) {
			for (int C = 0; C < Compilations.size(); C++) {
				TTileServerProviderCompilation Compilation = Compilations.get(C); 
				if (Compilation.ID == CompilationID)
					return Compilation; //. ->
			}
			return null;
		}		
	}
	
	public static class TTileServer {
		public int		ID;
		public String 	Name;
		public String 	Info;
		public ArrayList<TTileServerProvider> Providers = new ArrayList<TTileServerProvider>();
		
		public int CompilationsCount() {
			int Result = 0;
			for (int P = 0; P < Providers.size(); P++)
				Result += Providers.get(P).CompilationsCount();
			return Result; 
		}

		public TTileServerProvider GetProvider(int ProviderID) {
			for (int P = 0; P < Providers.size(); P++) {
				TTileServerProvider Provider = Providers.get(P); 
				if (Provider.ID == ProviderID)
					return Provider; //. ->
			}
			return null;
		}
		
		public TTileServerProviderCompilation GetCompilation(int ProviderID, int CompilationID) {
			TTileServerProvider Provider = GetProvider(ProviderID);
			if (Provider == null)
				return null; //. ->
			return Provider.GetCompilation(CompilationID);
		}
	}
	
	private boolean flInitialized = false;
	//.
	public ArrayList<TTileServer> TileServers = null;
	//.
	private String TileImageryDataFile;
	private Document XmlDoc = null;
	public  Element RootNode = null;
	
	public TTileImageryData() throws Exception {
		TileImageryDataFile = TTileImagery.ImageryFolder+"/"+TileImageryDataFileName;
	}
	
	public void CheckInitialized() throws Exception {
		if (!flInitialized)
			Initialize();
	}
	
	public void Initialize() throws Exception {
		Load();
		//.
		flInitialized = true;
	}
	
	public boolean IsInitialized() {
		return flInitialized;
	}
	
	public void SetAsUninitialized() {
		flInitialized = false;
	}
	
	public void Load() throws Exception {
		File F = new File(TileImageryDataFile);
		if (!F.exists()) {
			RootNode = null;
			return; //. ->
		}
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(TileImageryDataFile);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
		//.	
		FromByteArray(XML);				
	}

	public void Save() throws IOException {
		if ((XmlDoc == null) || (RootNode == null))
			return; //. ->
        String xmlString = TMyXML.GetStringFromNode(RootNode);
        //.
        byte[] BA = xmlString.getBytes();
        FileOutputStream FOS = new FileOutputStream(TileImageryDataFile);
        try {
        	FOS.write(BA);
        }
        finally {
        	FOS.close();
        }
	}
	
	public void FromByteArray(byte[] BA) throws Exception {
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
		//.
		RootNode = XmlDoc.getDocumentElement();
		//.
		int Version = Integer.parseInt(RootNode.getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
		if (Version != 1) 
			throw new Exception("unknown "+TileImageryDataFile+" file version, version: "+Integer.toString(Version)); //. =>
		//.
		Node TileServersNode = TMyXML.SearchNode(RootNode, "TileServers");
		if (TileServersNode == null)
			return; //. ->
		TileServers = new ArrayList<TTileServer>();
		NodeList TileServerNodes = TileServersNode.getChildNodes();
		for (int I = 0; I < TileServerNodes.getLength(); I++) {
			Node TileServerNode = TileServerNodes.item(I); 
			String SID = TileServerNode.getLocalName();
			if (SID != null) {
				TTileServer S = new TTileServer();
				S.ID = Integer.parseInt(SID.substring(1));
				S.Name = TMyXML.SearchNode(TileServerNode, "Name").getFirstChild().getNodeValue();
				S.Info = TMyXML.SearchNode(TileServerNode, "Info").getFirstChild().getNodeValue();
				//. get Providers
				NodeList ProviderNodes = TMyXML.SearchNode(TileServerNode, "Providers").getChildNodes();
				for (int P = 0; P < ProviderNodes.getLength(); P++) {
					Node ProviderNode = ProviderNodes.item(P);
					String PID = ProviderNode.getLocalName();
					if (PID != null) {
						TTileServerProvider SP = new TTileServerProvider();
						SP.ID = Integer.parseInt(PID.substring(1));
						SP.Name = TMyXML.SearchNode(ProviderNode, "Name").getFirstChild().getNodeValue();
						SP.Info = TMyXML.SearchNode(ProviderNode, "Info").getFirstChild().getNodeValue();
						//. get Compilations
						NodeList CompilationNodes = TMyXML.SearchNode(ProviderNode, "Compilations").getChildNodes();
						for (int C = 0; C < CompilationNodes.getLength(); C++) {
							Node CompilationNode = CompilationNodes.item(C);
							String CID = CompilationNode.getLocalName();
							if (CID != null) {
								TTileServerProviderCompilation SPC = new TTileServerProviderCompilation();
								SPC.ID = Integer.parseInt(CID.substring(1));
								SPC.Name = TMyXML.SearchNode(CompilationNode, "Name").getFirstChild().getNodeValue();
								SPC.Info = TMyXML.SearchNode(CompilationNode, "Info").getFirstChild().getNodeValue();
								Node LGN = TMyXML.SearchNode(CompilationNode, "LayGroup");
								if (LGN != null)
									SPC.LayGroup = Integer.parseInt(LGN.getFirstChild().getNodeValue());
								else
									SPC.LayGroup = 0;
								//.
								SP.Compilations.add(SPC);
							}
						}
						//.
						S.Providers.add(SP);
					}
				}
				//.
				TileServers.add(S);
			}
		}
	}
	
	public void FromByteArrayAndSave(byte[] BA) throws Exception {
		FromByteArray(BA);
		//.
		Save();
	}
	
	public boolean TileServers_IsNull() {
		return (TileServers == null);
	}
}
