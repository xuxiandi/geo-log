package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TReflectorCoGeoMonitorObjects {

	public static byte[] GetDataForDomains(TGeoScopeServer Server, String Domains, String Params) throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"CoGeoMonitorObjectsData.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+"1"/*parameters version*/+','+Domains+","+Params;
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
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public static byte[] GetReportForDomains(TGeoScopeServer Server, String ReportDomains, String ReportParams) throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"CoGeoMonitorObjectsReport.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+"1"/*parameters version*/+","+ReportDomains+","+ReportParams;
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
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public static final String CoGeoMonitorObjectsFileName = "CoGeoMonitorObjects.xml";
	
	private TReflector Reflector;
	
	public TCoGeoMonitorObject[] Items;
	public int UpdateInterval;
	
	public TReflectorCoGeoMonitorObjects(TReflector pReflector) {
		Reflector = pReflector;
		//.
		Items = new TCoGeoMonitorObject[0];
		UpdateInterval = 30;
		//.
		try {
			Load();
		} catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private synchronized void Load() throws Exception {
		String FN = TReflector.ProfileFolder()+"/"+CoGeoMonitorObjectsFileName;
		File F = new File(FN);
		if (!F.exists()) {
			Items = new TCoGeoMonitorObject[0];
			return; //. ->
		}
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(FN);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
		switch (Version) {
		case 1:
			NodeList NL = XmlDoc.getDocumentElement().getElementsByTagName("UpdateInterval");
			UpdateInterval = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("Items");
			if (NL != null) {
				NodeList ItemsNode = NL.item(0).getChildNodes();
				Items = new TCoGeoMonitorObject[ItemsNode.getLength()];
				for (int I = 0; I < Items.length; I++) {
					Node ItemNode = ItemsNode.item(I);
					NodeList ItemChildsNode = ItemNode.getChildNodes();
					//.
					int 	ID = Integer.parseInt(ItemChildsNode.item(0).getFirstChild().getNodeValue());
					String 	Name = ItemChildsNode.item(1).getFirstChild().getNodeValue();
					boolean flEnabled = (Integer.parseInt(ItemChildsNode.item(2).getFirstChild().getNodeValue()) != 0);
					//.
					Items[I] = new TCoGeoMonitorObject(Reflector.Server, ID,Name,flEnabled);
					Items[I].Prepare(Reflector);
				}
			}
			break; //. >
		default:
			throw new Exception("unknown data version, version: "+Integer.toString(Version)); //. =>
		}
	}
	
	public synchronized void Save() throws IllegalArgumentException, IllegalStateException, IOException {
    	int Version = 1;
	    String FN = TDEVICEModule.ProfileFolder()+"/"+CoGeoMonitorObjectsFileName;
        File F = new File(FN);
	    if (!F.exists()) {
	    	F.getParentFile().mkdirs();
	    	F.createNewFile();
	    }
	    XmlSerializer serializer = Xml.newSerializer();
	    FileWriter writer = new FileWriter(FN);
	    try {
	        serializer.setOutput(writer);
	        serializer.startDocument("UTF-8",true);
	        serializer.startTag("", "ROOT");
	        //.
            serializer.startTag("", "Version");
            serializer.text(Integer.toString(Version));
            serializer.endTag("", "Version");
	        //.
            serializer.startTag("", "UpdateInterval");
            serializer.text(Integer.toString(UpdateInterval));
            serializer.endTag("", "UpdateInterval");
	        //. Items
            serializer.startTag("", "Items");
            	for (int I = 0; I < Items.length; I++) {
	            	serializer.startTag("", "Item"+Integer.toString(I));
	            		//. ID
	            		serializer.startTag("", "ID");
	            		serializer.text(Integer.toString(Items[I].ID));
	            		serializer.endTag("", "ID");
	            		//. Name
	            		serializer.startTag("", "Name");
	            		serializer.text(Items[I].Name);
	            		serializer.endTag("", "Name");
	            		//. Enabled
	            		serializer.startTag("", "Enabled");
	            		if (Items[I].flEnabled)
	            			serializer.text("-1");
	            		else
	            			serializer.text("0");
	            		serializer.endTag("", "Enabled");
	            		//. Lays
	            	serializer.endTag("", "Item"+Integer.toString(I));
            	}
            serializer.endTag("", "Items");
            //.
	        serializer.endTag("", "ROOT");
	        serializer.endDocument();
	    }
	    finally {
	    	writer.close();
	    }
	}	
	
	public synchronized int GetUpdateInterval() {
		return UpdateInterval;
	}
	
	public synchronized void SetUpdateInterval(int pUpdateInterval) {
		UpdateInterval = pUpdateInterval;
		//.
		try {
			Save();
		} catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void AddItem(int pID, String pName, boolean pflEnabled) {
		boolean flExists = false;
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].ID == pID) {
				Items[I] = new TCoGeoMonitorObject(Reflector.Server, pID,pName,pflEnabled);
				Items[I].Prepare(Reflector);
				flExists = true;
				break; //. >
			}
		//.
		if (!flExists) {
			TCoGeoMonitorObject[] _Items = new TCoGeoMonitorObject[Items.length+1];
			for (int I = 0; I < Items.length; I++) 
				_Items[I] = Items[I];
			_Items[Items.length] = new TCoGeoMonitorObject(Reflector.Server, pID,pName,pflEnabled);
			_Items[Items.length].Prepare(Reflector);
			Items = _Items;
		}
		//.
		try {
			Save();
		} catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void AddItem(TCoGeoMonitorObject Item) {
		boolean flExists = false;
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].ID == Item.ID) {
				Items[I] = Item;
				flExists = true;
				break; //. >
			}
		//.
		if (!flExists) {
			TCoGeoMonitorObject[] _Items = new TCoGeoMonitorObject[Items.length+1];
			for (int I = 0; I < Items.length; I++) 
				_Items[I] = Items[I];
			_Items[Items.length] = Item;
			Items = _Items;
		}
		//.
		try {
			Save();
		} catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void RemoveItem(int pID) {
		int ItemsCount = 0;
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].ID != pID)
				ItemsCount++;
		TCoGeoMonitorObject[] _Items = new TCoGeoMonitorObject[ItemsCount]; 
		ItemsCount = 0;
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].ID != pID) {
				_Items[ItemsCount] = Items[I];
				ItemsCount++;
			}
		Items = _Items;
		//.
		try {
			Save();
		} catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void EnableDisableItem(int pID, boolean pflEnable) {
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].ID == pID)
				Items[I].flEnabled = pflEnable;
		//.
		try {
			Save();
		} catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
		Reflector.StartUpdatingSpaceImage();
	}
	
	public void RemoveDisabledItems() {
		int ItemsCount = 0;
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].flEnabled)
				ItemsCount++;
		TCoGeoMonitorObject[] _Items = new TCoGeoMonitorObject[ItemsCount]; 
		ItemsCount = 0;
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].flEnabled) {
				_Items[ItemsCount] = Items[I];
				ItemsCount++;
			}
		Items = _Items;
		//.
		try {
			Save();
		} catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}
	
	public void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas) {
		for (int I = 0; I < Items.length; I++)
			if (Items[I].flEnabled)
				Items[I].DrawOnCanvas(RW, canvas);
	}
	
	public int Select(TReflectionWindowStruc RW, float pX, float pY) {
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].flEnabled && Items[I].Select(RW, pX,pY))
				return I; 
		return -1;
	}
	
	public void UnSelectAll() {
		for (int I = 0; I < Items.length; I++)
			Items[I].UnSelect();
	}
}
