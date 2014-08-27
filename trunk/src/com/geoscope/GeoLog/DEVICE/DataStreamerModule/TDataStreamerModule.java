package com.geoscope.GeoLog.DEVICE.DataStreamerModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.widget.Toast;

import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentDataStreaming;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TDataStreamerModule extends TModule {

	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"DataStreamerModule";
	}
	
	public static TComponentDataStreaming.TStreamer GetStreamer(String TypeID, TDEVICEModule Device, int idTComponent, long idComponent, int ChannelID, byte[] Configuration, String Parameters) {
		TComponentDataStreaming.TStreamer Result;
		Result = TAudioModule.GetStreamer(TypeID, Device, idTComponent,idComponent, ChannelID, Configuration, Parameters);
		if (Result != null)
			return Result; //. ->
		Result = TVideoModule.GetStreamer(TypeID, Device, idTComponent,idComponent, ChannelID, Configuration, Parameters);
		//.
		return Result; //. ->
	}
	
    public TDataStreamerModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. 
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//. ////////////////////
		GetStreamer("Audio.AACRTP", Device, 2086,2, 0, null, "");
		GetStreamer("Video.H264", Device, 2086,2, 1, null, "");
    }
    
    public void Destroy() {
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
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
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("VideoModule").item(0);
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
    }
    
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
	    SaveConfigurationLocally();
		//.
		int Version = 1;
        Serializer.startTag("", "DataStreamerModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        Serializer.endTag("", "DataStreamerModule");
    }
    
    public void SaveConfigurationLocally() throws IOException {
	}    
}
