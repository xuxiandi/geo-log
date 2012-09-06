/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.GPIModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPIFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPIValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.Utils.OleDate;

/**
 *
 * @author ALXPONOM
 */
public class TGPIModule extends TModule 
{
    public TComponentTimestampedInt16Value LastValue = new TComponentTimestampedInt16Value();
    public TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value();
    
    public TGPIModule(TDEVICEModule pDevice)
    {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
    	try {
			LoadConfiguration();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SGPIModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
		Value.Assign(LastValue);
    }
    
    public void Destroy()
    {
    	LastValue.Assign(Value);
    }

    @Override
    public synchronized void LoadConfiguration() throws Exception {
		String CFN = ConfigurationFile();
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
				Node node = RootNode.getElementsByTagName("GPILastValue").item(0).getFirstChild();
				if (node != null) {
					int _LastValue = Integer.parseInt(node.getNodeValue());
					LastValue.SetValue(OleDate.UTCCurrentTimestamp(),(short)_LastValue);
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
	public synchronized void SaveConfigurationTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        Serializer.startTag("", "GPIModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        Serializer.startTag("", "GPILastValue");
        Serializer.text(Integer.toString(LastValue.GetValue()));
        Serializer.endTag("", "GPILastValue");
        //. 
        Serializer.endTag("", "GPIModule");
    }
    
    public synchronized void SetValue(short pValue) throws Exception
    {
        Value.SetValue(OleDate.UTCCurrentTimestamp(),pValue);
        if (!Value.IsValueTheSame(LastValue)) {
            DoOnGPIValueHasSignalled(Value);
            LastValue.Assign(Value);
            //.
            SaveConfiguration();
        }
    }
    
    public synchronized int GetIntValue() {
    	return Value.Value;
    }
    
    private void DoOnGPIValueHasSignalled(TComponentTimestampedInt16Value Value)
    {
        TObjectSetComponentDataServiceOperation SO;
        TGPSFixValue GPSFix = Device.GPSModule.GetCurrentFix();
        if (GPSFix.IsAvailable())
        {
            TGPIFixValue GPIFix = new TGPIFixValue(Value,GPSFix);
            SO = new TObjectSetGPIFixSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
            ((TObjectSetGPIFixSO)SO).setValue(GPIFix);
        }
        else
        {
            SO = new TObjectSetGPIValueSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
            ((TObjectSetGPIValueSO)SO).setValue(Value);
        }
        try
        {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
            Device.BackupMonitor.BackupImmediate();
        }
        catch (Exception E) {}
    }
}
