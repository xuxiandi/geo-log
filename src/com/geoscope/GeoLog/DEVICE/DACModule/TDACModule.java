/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.DACModule;

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

import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDoubleArrayValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.PluginsModule.Models.M0.TM0;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

/**
 *
 * @author ALXPONOM
 */
public class TDACModule extends TModule  
{
	public static int ValueSize = 16;
	
    public class TDACValueItem extends TComponentValue {

    	public TDACModule DACModule;
        public int Address;
        
        public TDACValueItem(TDACModule pDACModule, int pAddress) {
            DACModule = pDACModule;
            Address = pAddress;
        }
        
        public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
        	double Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
            double V = DACModule.Value.GetValue()[Address];
            //. set USB plugin module
            if (Device.PluginsModule.USBPluginModule.PluginModel instanceof TM0) {
            	TM0 Model = (TM0)Device.PluginsModule.USBPluginModule.PluginModel;
                try {
                	Model.SetDACCommand_Process(Address,(int)V, Device.PluginsModule.USBPluginModule);
                }
                catch (Exception E) {
                	throw new IOException(E.getMessage()); //. =>
                }
            }
            else 
            	throw new IOException("unknown plugin model"); //. =>
            //. 
            DACModule.Value.SetValueItem(Timestamp,Address,V);
            //.
            super.FromByteArray(BA,/*ref*/ Idx);
        }
    
        public synchronized byte[] ToByteArray() throws IOException {
        	double Timestamp = DACModule.Value.GetTimestamp();
            double V = DACModule.Value.GetValue()[Address];
            byte[] Result = new byte[16];
        	byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        	System.arraycopy(BA,0, Result,0, BA.length); 
        	BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(V);
        	System.arraycopy(BA,0, Result,8, BA.length); 
            return Result;
        }
    }
    
    public TComponentTimestampedDoubleArrayValue Value = new TComponentTimestampedDoubleArrayValue(ValueSize);
    
    public TDACModule(TDEVICEModule pDevice) {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
		}
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
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("GPSModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		case 1:
			try {
				/*///? Node node = RootNode.getElementsByTagName("DACValue").item(0).getFirstChild();
				if (node != null) {
					int _Value = Integer.parseInt(node.getNodeValue());
					Value.SetValue(OleDate.UTCCurrentTimestamp(),(short)_Value);
				}*/
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
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        Serializer.startTag("", "DACModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        /*///? Serializer.startTag("", "DACValue");
        Serializer.text(Integer.toString(Value.GetValue()));
        Serializer.endTag("", "DACValue");*/
        //. 
        Serializer.endTag("", "DACModule");
    }
    
    public TDACValueItem getValueItem(int pItemAddress) {
        return new TDACValueItem(this,pItemAddress);
    }
}
