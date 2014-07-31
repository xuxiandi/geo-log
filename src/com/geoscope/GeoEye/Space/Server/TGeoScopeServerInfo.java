package com.geoscope.GeoEye.Space.Server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;

public class TGeoScopeServerInfo {

	public static class TInfo {
		public String 	SpaceDataServerAddress = "";
		public int 		SpaceDataServerPort = 0;
		//.
		public String 	SpaceUserSessionServerAddress = "";
		public int 		SpaceUserSessionServerPort = 0;
		//.
		public String 	GeographDataServerAddress = "";
		public int 		GeographDataServerPort = 0;
		//.
		public String 	GeographProxyServerAddress = "";
		public int 		GeographProxyServerPort = 0;
		
		public boolean IsSpaceDataServerValid() {
			return (SpaceDataServerPort >= 0);
		}

		public boolean IsSpaceUserSessionServerValid() {
			return (SpaceUserSessionServerPort >= 0);
		}

		public boolean IsGeographDataServerValid() {
			return (GeographDataServerPort >= 0);
		}

		public boolean IsGeographProxyServerValid() {
			return (GeographProxyServerPort >= 0);
		}
	}
	
	private TGeoScopeServer Server;
	//.
	public boolean flInitialized = false;
	//.
	private TInfo Info = null;
	
	public TGeoScopeServerInfo(TGeoScopeServer pServer) {
		Server = pServer;
	}
	
	public synchronized boolean Initialize() throws Exception {
		try {
			LoadData();
			//.
			Server.flOnline = true;
		}
		catch (IOException IOE) {
			Server.flOnline = false;
			//.
			throw IOE; //. ->
		}
		//.
		flInitialized = true;
		return flInitialized;
	}
	
	public void Finalize() {
		flInitialized = false;
	}
	
	public synchronized boolean CheckInitialized() throws Exception {
		if (flInitialized)
			return false; //. ->
		return Initialize();
	}
	
	public void Clear() {
		Finalize();
	}

	private synchronized void LoadData() throws Exception {
		if (Server.User == null)
			throw new Exception("User is not initialized"); //. =>
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "SpaceServers.xml";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		String URL = URL1+"/"+URL2+".xml";
		//.
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length) {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) 
	                	throw new Exception("connection is closed unexpectedly"); //. =>
	                SummarySize += Size;
	            }
	            //.
	        	Document XmlDoc = null;
	        	ByteArrayInputStream BIS = new ByteArrayInputStream(Data);
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
	    		Element RootNode = XmlDoc.getDocumentElement();
	    		//.
	    		int Version = Integer.parseInt(RootNode.getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
	    		if (Version != 1) 
	    			throw new Exception("unknown SpaceServersInfo version, version: "+Integer.toString(Version)); //. =>
	    		TInfo _Info = new TInfo();
	    		//.
	    		Node SpaceDataServerNode = TMyXML.SearchNode(RootNode, "SpaceDataServer");
	    		Node SpaceDataServerAddressNode = TMyXML.SearchNode(SpaceDataServerNode, "Address").getFirstChild();
	    		if (SpaceDataServerAddressNode != null)
	    			_Info.SpaceDataServerAddress = SpaceDataServerAddressNode.getNodeValue();
	    		else
	    			_Info.SpaceDataServerAddress = Server.HostAddress;
	    		Node SpaceDataServerPortNode = TMyXML.SearchNode(SpaceDataServerNode, "Port").getFirstChild();
	    		if (SpaceDataServerPortNode != null)
	    			_Info.SpaceDataServerPort = Integer.parseInt(SpaceDataServerPortNode.getNodeValue());
	    		Node SpaceUserSessionServerNode = TMyXML.SearchNode(RootNode, "SpaceUserSessionServer");
	    		Node SpaceUserSessionServerAddressNode = TMyXML.SearchNode(SpaceUserSessionServerNode, "Address").getFirstChild();
	    		if (SpaceUserSessionServerAddressNode != null)
	    			_Info.SpaceUserSessionServerAddress = SpaceUserSessionServerAddressNode.getNodeValue();
	    		else
	    			_Info.SpaceUserSessionServerAddress = Server.HostAddress;
	    		Node SpaceUserSessionServerPortNode = TMyXML.SearchNode(SpaceUserSessionServerNode, "Port").getFirstChild();
	    		if (SpaceUserSessionServerPortNode != null)
	    			_Info.SpaceUserSessionServerPort = Integer.parseInt(SpaceUserSessionServerPortNode.getNodeValue());
	    		Node GeographDataServerNode = TMyXML.SearchNode(RootNode, "GeographDataServer");
	    		Node GeographDataServerAddressNode = TMyXML.SearchNode(GeographDataServerNode, "Address").getFirstChild();
	    		if (GeographDataServerAddressNode != null)
	    			_Info.GeographDataServerAddress = GeographDataServerAddressNode.getNodeValue();
	    		else
	    			_Info.GeographDataServerAddress = Server.HostAddress;
	    		Node GeographDataServerPortNode = TMyXML.SearchNode(GeographDataServerNode, "Port").getFirstChild();
	    		if (GeographDataServerPortNode != null)
	    			_Info.GeographDataServerPort = Integer.parseInt(GeographDataServerPortNode.getNodeValue());
	    		Node GeographProxyServerNode = TMyXML.SearchNode(RootNode, "GeographProxyServer");
	    		Node GeographProxyServerAddressNode = TMyXML.SearchNode(GeographProxyServerNode, "Address").getFirstChild();
	    		if (GeographProxyServerAddressNode != null)
	    			_Info.GeographProxyServerAddress = GeographProxyServerAddressNode.getNodeValue();
	    		else 
	    			_Info.GeographProxyServerAddress = Server.HostAddress;
	    		Node GeographProxyServerPortNode = TMyXML.SearchNode(GeographProxyServerNode, "Port").getFirstChild();
	    		if (GeographProxyServerPortNode != null)
	    			_Info.GeographProxyServerPort = Integer.parseInt(GeographProxyServerPortNode.getNodeValue());
	    		Info = _Info;
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public synchronized TInfo GetInfo() throws Exception {
		CheckInitialized();
		return Info;
	}
}
