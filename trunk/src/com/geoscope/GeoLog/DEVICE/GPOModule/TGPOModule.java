/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPOModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

/**
 *
 * @author ALXPONOM
 */
public class TGPOModule extends TModule  
{
    public class TGPOValueBit extends TComponentValue
    {
        public TGPOModule GPOModule;
        public int BitIndex;
        short Mask;
        
        public TGPOValueBit(TGPOModule pGPOModule, int pBitIndex)
        {
            GPOModule = pGPOModule;
            BitIndex = pBitIndex;
            Mask = (short)(1 << BitIndex);
        }
        
        public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
        {
        	double Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
            boolean flTrue = (BA[Idx.Value] != 0); Idx.Value++;
            short V = GPOModule.Value.GetValue();
            if (flTrue)
                V = (short)(V | Mask);
            else 
                V = (short)(V & ~Mask);
            GPOModule.Value.SetValue(Timestamp,V);
            //.
            super.FromByteArray(BA,/*ref*/ Idx);
        }
    
        public synchronized byte[] ToByteArray() throws IOException
        {
        	double Timestamp = GPOModule.Value.GetTimestamp();
            short V = GPOModule.Value.GetValue();
            byte[] Result = new byte[9];
        	byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        	System.arraycopy(BA,0, Result,0, BA.length); 
            if ((V & Mask) == Mask)
                Result[8] = 1;
            else 
                Result[8] = 0;
            return Result;
        }
    }
    
    public TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value();
    
    public TGPOModule(TDEVICEModule pDevice)
    {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SGPOModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy()
    {
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
		Element RootNode = XmlDoc.getDocumentElement();
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("GPSModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		case 1:
			try {
				Node node = RootNode.getElementsByTagName("GPOValue").item(0).getFirstChild();
				if (node != null) {
					int _Value = Integer.parseInt(node.getNodeValue());
					Value.SetValue(OleDate.UTCCurrentTimestamp(),(short)_Value);
				}
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
        Serializer.startTag("", "GPOModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        Serializer.startTag("", "GPOValue");
        Serializer.text(Integer.toString(Value.GetValue()));
        Serializer.endTag("", "GPOValue");
        //. 
        Serializer.endTag("", "GPOModule");
    }
    
    public TGPOValueBit getValueBit(int pBitIndex)
    {
        return new TGPOValueBit(this,pBitIndex);
    }
}
