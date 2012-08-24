package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

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

public class TVideoRecorderConfigurationDataValue extends TComponentTimestampedDataValue {

	private TVideoRecorderModule VideoRecorderModule;
	
	public TVideoRecorderConfigurationDataValue(TVideoRecorderModule pVideoRecorderModule) {
		VideoRecorderModule = pVideoRecorderModule;
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
    				Node node = RootNode.getElementsByTagName("flVideoRecorderModuleIsEnabled").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
    				node = RootNode.getElementsByTagName("Name").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.Name = node.getNodeValue();
    				//. measurement configuration
    				node = RootNode.getElementsByTagName("MaxDuration").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.MeasurementConfiguration.MaxDuration = Double.parseDouble(node.getNodeValue());
    				node = RootNode.getElementsByTagName("LifeTime").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.MeasurementConfiguration.LifeTime = Double.parseDouble(node.getNodeValue());
    				node = RootNode.getElementsByTagName("AutosaveInterval").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.MeasurementConfiguration.AutosaveInterval = Double.parseDouble(node.getNodeValue());
    				//. camera configuration
    				node = RootNode.getElementsByTagName("SampleRate").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.CameraConfiguration.Camera_Audio_SampleRate = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("ABitRate").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.CameraConfiguration.Camera_Audio_BitRate = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("ResX").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.CameraConfiguration.Camera_Video_ResX = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("ResY").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.CameraConfiguration.Camera_Video_ResY = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("FrameRate").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.CameraConfiguration.Camera_Video_FrameRate = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("VBitRate").item(0).getFirstChild();
    				if (node != null)
    					VideoRecorderModule.CameraConfiguration.Camera_Video_BitRate = Integer.parseInt(node.getNodeValue());
    				//.
    				VideoRecorderModule.SaveConfiguration();
        			///- VideoRecorderModule.PostRestartRecorder(); //. post restart message to the current measurement
    				VideoRecorderModule.Device.ControlModule.RestartDeviceProcessAfterDelay(1000*1/*seconds*/);
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
            int V = 0;
            if (VideoRecorderModule.flEnabled)
            	V = 1;
            Serializer.startTag("", "flVideoRecorderModuleIsEnabled");
            Serializer.text(Integer.toString(V));
            Serializer.endTag("", "flVideoRecorderModuleIsEnabled");
	        //. 
            Serializer.startTag("", "Name");
            Serializer.text(VideoRecorderModule.Name);
            Serializer.endTag("", "Name");
	        //. 
	        Serializer.startTag("", "Measurement");
	        //.
            Serializer.startTag("", "MaxDuration");
            Serializer.text(Double.toString(VideoRecorderModule.MeasurementConfiguration.MaxDuration));
            Serializer.endTag("", "MaxDuration");
	        //.
            Serializer.startTag("", "LifeTime");
            Serializer.text(Double.toString(VideoRecorderModule.MeasurementConfiguration.LifeTime));
            Serializer.endTag("", "LifeTime");
	        //.
            Serializer.startTag("", "AutosaveInterval");
            Serializer.text(Double.toString(VideoRecorderModule.MeasurementConfiguration.AutosaveInterval));
            Serializer.endTag("", "AutosaveInterval");
            //.
	        Serializer.endTag("", "Measurement");
	        //. 
	        Serializer.startTag("", "Camera");
	        //.
	        Serializer.startTag("", "Audio");
	        //.
            Serializer.startTag("", "SampleRate");
            Serializer.text(Integer.toString(VideoRecorderModule.CameraConfiguration.Camera_Audio_SampleRate));
            Serializer.endTag("", "SampleRate");
	        //.
            Serializer.startTag("", "ABitRate");
            Serializer.text(Integer.toString(VideoRecorderModule.CameraConfiguration.Camera_Audio_BitRate));
            Serializer.endTag("", "ABitRate");
	        //.
	        Serializer.endTag("", "Audio");
	        //.
	        Serializer.startTag("", "Video");
	        //.
            Serializer.startTag("", "ResX");
            Serializer.text(Integer.toString(VideoRecorderModule.CameraConfiguration.Camera_Video_ResX));
            Serializer.endTag("", "ResX");
	        //.
            Serializer.startTag("", "ResY");
            Serializer.text(Integer.toString(VideoRecorderModule.CameraConfiguration.Camera_Video_ResY));
            Serializer.endTag("", "ResY");
	        //.
            Serializer.startTag("", "FrameRate");
            Serializer.text(Integer.toString(VideoRecorderModule.CameraConfiguration.Camera_Video_FrameRate));
            Serializer.endTag("", "FrameRate");
	        //.
            Serializer.startTag("", "VBitRate");
            Serializer.text(Integer.toString(VideoRecorderModule.CameraConfiguration.Camera_Video_BitRate));
            Serializer.endTag("", "VBitRate");
	        //.
	        Serializer.endTag("", "Video");
	        //.
	        Serializer.endTag("", "Camera");
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
