package com.geoscope.GeoEye;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

import android.graphics.Canvas;
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TReflectorCoGeoMonitorObjects {

	public static final String CoGeoMonitorObjectsFileName = "CoGeoMonitorObjects.xml";
	
	private TReflector Reflector;
	public TReflectorCoGeoMonitorObject[] Items;
	public int UpdateInterval;
	
	public TReflectorCoGeoMonitorObjects(TReflector pReflector) {
		Reflector = pReflector;
		//.
		Items = new TReflectorCoGeoMonitorObject[0];
		UpdateInterval = 30;
		//.
		try {
			Load();
		} catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		}
	}

	private synchronized void Load() throws Exception {
		String FN = TReflector.ProfileFolder+"/"+CoGeoMonitorObjectsFileName;
		File F = new File(FN);
		if (!F.exists()) {
			Items = new TReflectorCoGeoMonitorObject[0];
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
				Items = new TReflectorCoGeoMonitorObject[ItemsNode.getLength()];
				for (int I = 0; I < Items.length; I++) {
					Node ItemNode = ItemsNode.item(I);
					NodeList ItemChildsNode = ItemNode.getChildNodes();
					//.
					int 	ID = Integer.parseInt(ItemChildsNode.item(0).getFirstChild().getNodeValue());
					String 	Name = ItemChildsNode.item(1).getFirstChild().getNodeValue();
					boolean flEnabled = (Integer.parseInt(ItemChildsNode.item(2).getFirstChild().getNodeValue()) != 0);
					//.
					Items[I] = new TReflectorCoGeoMonitorObject(Reflector, ID,Name,flEnabled);
				}
			}
			break; //. >
		default:
			throw new Exception("неизвестная версия данных, версия: "+Integer.toString(Version)); //. =>
		}
	}
	
	public synchronized void Save() throws IllegalArgumentException, IllegalStateException, IOException {
    	int Version = 1;
	    String FN = TDEVICEModule.ProfileFolder+"/"+CoGeoMonitorObjectsFileName;
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
		TReflectorCoGeoMonitorObject[] _Items = new TReflectorCoGeoMonitorObject[Items.length+1];
		for (int I = 0; I < Items.length; I++) 
			_Items[I] = Items[I];
		_Items[Items.length] = new TReflectorCoGeoMonitorObject(Reflector, pID,pName,pflEnabled);
		Items = _Items;
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
		TReflectorCoGeoMonitorObject[] _Items = new TReflectorCoGeoMonitorObject[ItemsCount]; 
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
	}
	
	public void RemoveDisabledItems() {
		int ItemsCount = 0;
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].flEnabled)
				ItemsCount++;
		TReflectorCoGeoMonitorObject[] _Items = new TReflectorCoGeoMonitorObject[ItemsCount]; 
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
	
	public void DrawOnCanvas(Canvas canvas) {
		for (int I = 0; I < Items.length; I++)
			if (Items[I].flEnabled)
				Items[I].DrawOnCanvas(canvas);
	}
	
	public int Select(float pX, float pY) {
		for (int I = 0; I < Items.length; I++) 
			if (Items[I].flEnabled && Items[I].Select(pX,pY))
				return I; 
		return -1;
	}
	
	public void UnSelectAll() {
		for (int I = 0; I < Items.length; I++)
			Items[I].UnSelect();
	}
	
	public void RecalculateVisualizationScreenLocation() {
		for (int I = 0; I < Items.length; I++) 
			Items[I].RecalculateVisualizationScreenLocation();
	}
}
