package com.geoscope.GeoLog.DEVICE.GPSModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.Utils.OleDate;

public class TGPSModuleConfigurationDataValue extends TComponentTimestampedDataValue {

	private TGPSModule GPSModule;
	
	public TGPSModuleConfigurationDataValue(TGPSModule pVideoRecorderModule) {
		GPSModule = pVideoRecorderModule;
	}
	
	@Override
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException {
    	super.FromByteArray(BA, Idx);
    	//.
    	try {
        	Document XmlDoc;
    		ByteArrayInputStream BIS = new ByteArrayInputStream(Value);
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
    		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("ROOT").item(0);
    		int Version = 1; //. todo
    		switch (Version) {
    		case 1:
    			try {
    				Node node;
    				try {
        				node = RootNode.getElementsByTagName("Provider_ReadInterval").item(0).getFirstChild();
        				if (node != null)
        					GPSModule.Provider_ReadInterval = Integer.parseInt(node.getNodeValue());
    				}
    				catch (Exception E) {}
    				try {
    					node = RootNode.getElementsByTagName("IgnoreImpulseModeSleepingOnMovement").item(0).getFirstChild();
    					if (node != null)
    						GPSModule.flIgnoreImpulseModeSleepingOnMovement = (Integer.parseInt(node.getNodeValue()) != 0);
    				}
    				catch (Exception E) {}
    				/* try {
        				node = RootNode.getElementsByTagName("Threshold").item(0).getFirstChild();
        				if (node != null) {
        					int _Threshold = Integer.parseInt(node.getNodeValue());
        					GPSModule.Threshold.SetValue((short)_Threshold);
        				}
    				}
    				catch (Exception E) {}*/
    				node = RootNode.getElementsByTagName("MapID").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapID = Integer.parseInt(node.getNodeValue());
    				//.
    				node = RootNode.getElementsByTagName("IResX").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.Image_ResX = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("IResY").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.Image_ResY = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("IQuality").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.Image_Quality = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("IFormat").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.Image_Format = node.getNodeValue();
    				node = RootNode.getElementsByTagName("MFSampleRate").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.MediaFragment_Audio_SampleRate = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("MFABitRate").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.MediaFragment_Audio_BitRate = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("MFResX").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.MediaFragment_Video_ResX = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("MFResY").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.MediaFragment_Video_ResY = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("MFFrameRate").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.MediaFragment_Video_FrameRate = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("MFVBitRate").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.MediaFragment_Video_BitRate = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("MFMaxDuration").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.MediaFragment_MaxDuration = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("MFFormat").item(0).getFirstChild();
    				if (node != null)
    					GPSModule.MapPOIConfiguration.MediaFragment_Format = node.getNodeValue();
    				//.
    				GPSModule.SaveProfile();
    				GPSModule.Device.ControlModule.RestartDeviceProcessAfterDelay(1000*1/*seconds*/);
    			}
    			catch (Exception E) {
        			throw new Exception("error of parsing configuration: "+E.getMessage()); //. =>
    			}
    			break; //. >
    		default:
    			throw new Exception("unknown local configuration version, version: "+Integer.toString(Version)); //. =>
    		}
    	}
    	catch (Exception E) {
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_SetValueError,E.getMessage()); //. =>
    	}
    }
	
	@Override
    public synchronized byte[] ToByteArray() throws IOException, OperationException {
		int Version = 1;
	    XmlSerializer Serializer = Xml.newSerializer();
	    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
	    try {
	        Serializer.setOutput(BOS,"UTF-8");
	        Serializer.startDocument("UTF-8",true);
	        Serializer.startTag("", "ROOT");
	        //. Version
            Serializer.startTag("", "Version");
            Serializer.text(Integer.toString(Version));
            Serializer.endTag("", "Version");
	        //. 
            Serializer.startTag("", "Provider_ReadInterval");
            Serializer.text(Integer.toString(GPSModule.Provider_ReadInterval));
            Serializer.endTag("", "Provider_ReadInterval");
            //.
            int V = 0;
            if (GPSModule.flIgnoreImpulseModeSleepingOnMovement)
            	V = 1;
            Serializer.startTag("", "IgnoreImpulseModeSleepingOnMovement");
            Serializer.text(Integer.toString(V));
            Serializer.endTag("", "IgnoreImpulseModeSleepingOnMovement");
            //. 
            Serializer.startTag("", "Threshold");
            Serializer.text(Integer.toString(GPSModule.Threshold.GetValue()));
            Serializer.endTag("", "Threshold");
	        //. 
            Serializer.startTag("", "MapID");
            Serializer.text(Integer.toString(GPSModule.MapID));
            Serializer.endTag("", "MapID");
	        //.
            Serializer.startTag("", "MapPOI");
	        //. 
            Serializer.startTag("", "Image");
	        //. 
            Serializer.startTag("", "IResX");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.Image_ResX));
            Serializer.endTag("", "IResX");
	        //. 
            Serializer.startTag("", "IResY");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.Image_ResY));
            Serializer.endTag("", "IResY");
	        //. 
            Serializer.startTag("", "IQuality");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.Image_Quality));
            Serializer.endTag("", "IQuality");
	        //. 
            Serializer.startTag("", "IFormat");
            Serializer.text(GPSModule.MapPOIConfiguration.Image_Format);
            Serializer.endTag("", "IFormat");
            //.
            Serializer.endTag("", "Image");
	        //.
            Serializer.startTag("", "MediaFragment");
	        //.
            Serializer.startTag("", "Audio");
	        //. 
            Serializer.startTag("", "MFSampleRate");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.MediaFragment_Audio_SampleRate));
            Serializer.endTag("", "MFSampleRate");
	        //. 
            Serializer.startTag("", "MFABitRate");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.MediaFragment_Audio_BitRate));
            Serializer.endTag("", "MFABitRate");
            //.
            Serializer.endTag("", "Audio");
	        //.
            Serializer.startTag("", "Video");
	        //. 
            Serializer.startTag("", "MFResX");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.MediaFragment_Video_ResX));
            Serializer.endTag("", "MFResX");
	        //. 
            Serializer.startTag("", "MFResY");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.MediaFragment_Video_ResY));
            Serializer.endTag("", "MFResY");
	        //. 
            Serializer.startTag("", "MFFrameRate");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.MediaFragment_Video_FrameRate));
            Serializer.endTag("", "MFFrameRate");
	        //. 
            Serializer.startTag("", "MFVBitRate");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.MediaFragment_Video_BitRate));
            Serializer.endTag("", "MFVBitRate");
            //.
            Serializer.endTag("", "Video");
	        //. 
            Serializer.startTag("", "MFMaxDuration");
            Serializer.text(Integer.toString(GPSModule.MapPOIConfiguration.MediaFragment_MaxDuration));
            Serializer.endTag("", "MFMaxDuration");
	        //. 
            Serializer.startTag("", "MFFormat");
            Serializer.text(GPSModule.MapPOIConfiguration.MediaFragment_Format);
            Serializer.endTag("", "MFFormat");
            //.
            Serializer.endTag("", "MediaFragment");
            //.
            Serializer.endTag("", "MapPOI");
	        //.
	        Serializer.endTag("", "ROOT");
	        Serializer.endDocument();
	        //.
    		Timestamp = OleDate.UTCCurrentTimestamp();
			Value = BOS.toByteArray();
			//.
			return super.ToByteArray(); //. ->
	    }
	    finally {
	    	BOS.close();
	    }
    }
}
