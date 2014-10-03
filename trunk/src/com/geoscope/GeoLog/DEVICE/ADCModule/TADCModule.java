/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ADCModule;

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

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.TComponentValue;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDoubleArrayValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetADCValueItemSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.Models.M0.TM0;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

/**
 *
 * @author ALXPONOM
 */
public class TADCModule extends TModule  
{
	public static int ValueSize = 16;
	
    public static class TADCValueItem extends TComponentValue {

        public static final int ValueSize = 16;
        
    	public TADCModule ADCModule;
        public int Address;
        
        public TADCValueItem(TADCModule pADCModule, int pAddress) {
            ADCModule = pADCModule;
            Address = pAddress;
        }
        
        public TADCValueItem(TADCModule pADCModule, int pItemIndex, byte[] BA, TIndex Idx) throws IOException, OperationException {
            ADCModule = pADCModule;
            Address = pItemIndex;
            //.
            FromByteArray(BA,/*ref*/ Idx);
        }
        
        public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
        	double Timestamp = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
            double V = TGeographServerServiceOperation.ConvertBEByteArrayToDouble(BA, Idx.Value); Idx.Value+=8;
            //. 
            ADCModule.Value.SetValueItem(Timestamp,Address,V);
            //.
            super.FromByteArray(BA,/*ref*/ Idx);
        }
    
        public synchronized byte[] ToByteArray() throws IOException {
        	double Timestamp = ADCModule.Value.GetTimestamp();
            double V = ADCModule.Value.GetValueItem(Address);
            //.
            byte[] Result = new byte[ValueSize];
        	byte[] BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(Timestamp);
        	System.arraycopy(BA,0, Result,0, BA.length); 
        	BA = TGeographServerServiceOperation.ConvertDoubleToBEByteArray(V);
        	System.arraycopy(BA,0, Result,8, BA.length); 
            return Result;
        }
    }
    
    public TComponentTimestampedDoubleArrayValue Value = new TComponentTimestampedDoubleArrayValue(ValueSize);
    
    public TADCModule(TDEVICEModule pDevice) {
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
				/*///? Node node = RootNode.getElementsByTagName("ADCValue").item(0).getFirstChild();
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
        Serializer.startTag("", "ADCModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        /*///? Serializer.startTag("", "ADCValue");
        Serializer.text(Integer.toString(Value.GetValue()));
        Serializer.endTag("", "ADCValue");*/
        //. 
        Serializer.endTag("", "ADCModule");
    }
    
    public synchronized void SetValueItem(int pItemAddress, double pItemValue) throws Exception {
        Value.SetValueItem(OleDate.UTCCurrentTimestamp(),pItemAddress, pItemValue);
        TADCValueItem ValueItem = new TADCValueItem(this,pItemAddress);
        DoOnValueItemIsSignalled(ValueItem);
        //.
        SaveProfile();
    }
    
    private void DoOnValueItemIsSignalled(TADCValueItem Value) {
        TObjectSetComponentDataServiceOperation SO;
        SO = new TObjectSetADCValueItemSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        SO.setValue(Value);
        try
        {
            Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
            Device.BackupMonitor.BackupImmediate();
        }
        catch (Exception E) {}
    }
    
    public TADCValueItem GetValueItem(int pItemAddress) {
        return new TADCValueItem(this,pItemAddress);
    }

    public TADCValueItem GetCurrentValueItem(int pItemAddress) throws IOException {
    	double Timestamp = OleDate.UTCCurrentTimestamp();
        double V;
        //. get from USB plugin module 
        if (Device.PluginsModule.USBPluginModule.PluginModel instanceof TM0) {
        	TM0 Model = (TM0)Device.PluginsModule.USBPluginModule.PluginModel;
            try {
            	PIO.TADCCommand GetCommand = Model.GetADCCommand_Process(pItemAddress, Device.PluginsModule.USBPluginModule);
            	V = GetCommand.Value;
            }
            catch (Exception E) {
            	throw new IOException(E.getMessage()); //. =>
            }
        }
        else 
        	throw new IOException("unknown plugin model"); //. =>
        //. 
        Value.SetValueItem(Timestamp,pItemAddress,V);
        //.
        return new TADCValueItem(this,pItemAddress);
    }
}
