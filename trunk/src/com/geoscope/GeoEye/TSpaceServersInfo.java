package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.geoscope.GeoLog.Utils.TMyXML;

public class TSpaceServersInfo {

	public static class TInfo {
		public String 	SpaceDataServerAddress = "";
		public int 		SpaceDataServerPort = 0;
		//.
		public String 	GeographDataServerAddress = "";
		public int 		GeographDataServerPort = 0;
		//.
		public String 	GeographProxyServerAddress = "";
		public int 		GeographProxyServerPort = 0;
		
		public boolean IsSpaceDataServerValid() {
			return (SpaceDataServerPort >= 0);
		}

		public boolean IsGeographDataServerValid() {
			return (GeographDataServerPort >= 0);
		}

		public boolean IsGeographProxyServerValid() {
			return (GeographProxyServerPort >= 0);
		}
	}
	
	private TReflector Reflector;
	//.
	public boolean flInitialized = false;
	//.
	private TInfo Info = null;
	
	public TSpaceServersInfo(TReflector pReflector) {
		Reflector = pReflector;
	}
	
	private synchronized void LoadData() throws Exception {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
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
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
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
		HttpURLConnection Connection = Reflector.Server.OpenConnection(URL);
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
	                	throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
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
	    			_Info.SpaceDataServerAddress = Reflector.Server.HostAddress;
	    		Node SpaceDataServerPortNode = TMyXML.SearchNode(SpaceDataServerNode, "Port").getFirstChild();
	    		if (SpaceDataServerPortNode != null)
	    			_Info.SpaceDataServerPort = Integer.parseInt(SpaceDataServerPortNode.getNodeValue());
	    		Node GeographDataServerNode = TMyXML.SearchNode(RootNode, "GeographDataServer");
	    		Node GeographDataServerAddressNode = TMyXML.SearchNode(GeographDataServerNode, "Address").getFirstChild();
	    		if (GeographDataServerAddressNode != null)
	    			_Info.GeographDataServerAddress = GeographDataServerAddressNode.getNodeValue();
	    		else
	    			_Info.GeographDataServerAddress = Reflector.Server.HostAddress;
	    		Node GeographDataServerPortNode = TMyXML.SearchNode(GeographDataServerNode, "Port").getFirstChild();
	    		if (GeographDataServerPortNode != null)
	    			_Info.GeographDataServerPort = Integer.parseInt(GeographDataServerPortNode.getNodeValue());
	    		Node GeographProxyServerNode = TMyXML.SearchNode(RootNode, "GeographProxyServer");
	    		Node GeographProxyServerAddressNode = TMyXML.SearchNode(GeographProxyServerNode, "Address").getFirstChild();
	    		if (GeographProxyServerAddressNode != null)
	    			_Info.GeographProxyServerAddress = GeographProxyServerAddressNode.getNodeValue();
	    		else 
	    			_Info.GeographProxyServerAddress = Reflector.Server.HostAddress;
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
	
	public synchronized boolean CheckIntialized() throws Exception {
		if (flInitialized)
			return false; //. ->
		//.
		LoadData();
		//.
		flInitialized = true;
		return true;
	}
	
	public synchronized TInfo GetInfo() throws Exception {
		CheckIntialized();
		return Info;
	}
}
