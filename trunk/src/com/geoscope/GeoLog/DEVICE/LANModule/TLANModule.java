/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

/**
 *
 * @author ALXPONOM
 */
public class TLANModule extends TModule {
	
	public static final int LocalVirtualConnection_PortBase = 10000;
	
	public static TConnectionRepeater LocalVirtualConnection_GetRepeater() {
		return null;
	}
		
    public TLANModule(TDEVICEModule pDevice)
    {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
    	try {
			LoadConfiguration();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SLANModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() {
    	ConnectionRepeaters_RemoveAll();
    }
    
    public TConnectionRepeater ConnectionRepeaters_Add(String Address, int Port, String pServerAddress, int pServerPort, int ConnectionID) {
    	if (Address.equals("127.0.0.1") && (Port >= LocalVirtualConnection_PortBase))
    		return LocalVirtualConnection_GetRepeater(); //. ->
    	else
    		return (new TLANConnectionRepeater(this, Address,Port, pServerAddress,pServerPort,ConnectionID)); //. ->
    }
    
    public void ConnectionRepeaters_Remove(TConnectionRepeater CR) {
    	CR.Destroy();
    }

    public void ConnectionRepeaters_RemoveAll() {
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++)
        		TConnectionRepeater.Repeaters.get(I).Destroy();
		}
    }
    
    public void ConnectionRepeaters_Cancel(int ConnectionID) {
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if (CR.ConnectionID == ConnectionID)
        			CR.Cancel();
        	}
		}
    }

    public void ConnectionRepeaters_CheckForIdle() {
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if (CR.IsIdle()) 
        			CR.Destroy();
        	}
    	}
    }
    
    @Override
    public synchronized void LoadConfiguration() throws Exception {
		String CFN = ModuleFile();
		File F = new File(CFN);
		if (!F.exists()) 
			return; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(CFN);
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
		@SuppressWarnings("unused")
		Element RootNode = XmlDoc.getDocumentElement();
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("LANModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		case 1:
			try {
			}
			catch (Exception E) {
			}
			break; //. >
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
		return; 
    }
    
    @Override
	public synchronized void SaveConfigurationTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        Serializer.startTag("", "LANModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        Serializer.endTag("", "LANModule");
    }
}
