package com.geoscope.GeoLog.DEVICE.ConnectorModule;

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

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TConnectorModuleConfigurationDataValue extends TComponentTimestampedDataValue {

	private TConnectorModule ConnectorModule;
	
	public TConnectorModuleConfigurationDataValue(TConnectorModule pVideoRecorderModule) {
		ConnectorModule = pVideoRecorderModule;
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
    				/*Node node = RootNode.getElementsByTagName("flServerConnectionEnabled").item(0).getFirstChild();
    				if (node != null)
    					ConnectorModule.flServerConnectionEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
    				node = RootNode.getElementsByTagName("ServerAddress").item(0).getFirstChild();
    				if (node != null)
    					ConnectorModule.ServerAddress = node.getNodeValue();
    				node = RootNode.getElementsByTagName("ServerPort").item(0).getFirstChild();
    				if (node != null)
    					ConnectorModule.ServerPort = Integer.parseInt(node.getNodeValue());*/
    				Node node = RootNode.getElementsByTagName("LoopSleepTime").item(0).getFirstChild();
    				if (node != null)
    					ConnectorModule.LoopSleepTime = Integer.parseInt(node.getNodeValue());
    				node = RootNode.getElementsByTagName("TransmitInterval").item(0).getFirstChild();
    				if (node != null)
    					ConnectorModule.TransmitInterval = Integer.parseInt(node.getNodeValue());
    				/*node = RootNode.getElementsByTagName("flOutgoingSetOperationsQueueIsEnabled").item(0).getFirstChild();
    				if (node != null)
    					ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);*/
    				//.
    				ConnectorModule.SaveProfile();
    				ConnectorModule.Device.ControlModule.RestartDeviceProcessAfterDelay(1000*1/*seconds*/);
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
            if (ConnectorModule.flServerConnectionEnabled)
            	V = 1;
            Serializer.startTag("", "flServerConnectionEnabled");
            Serializer.text(Integer.toString(V));
            Serializer.endTag("", "flServerConnectionEnabled");
            //. 
            Serializer.startTag("", "ServerAddress");
            Serializer.text(ConnectorModule.ServerAddress);
            Serializer.endTag("", "ServerAddress");
            //. 
            Serializer.startTag("", "ServerPort");
            Serializer.text(Integer.toString(ConnectorModule.ServerPort));
            Serializer.endTag("", "ServerPort");
            //. 
            Serializer.startTag("", "GeographProxyServerAddress");
            if (ConnectorModule.GeographProxyServerAddress != null)
            	Serializer.text(ConnectorModule.GeographProxyServerAddress);
            Serializer.endTag("", "GeographProxyServerAddress");
            //. 
            Serializer.startTag("", "GeographProxyServerPort");
            Serializer.text(Integer.toString(ConnectorModule.GeographProxyServerPort));
            Serializer.endTag("", "GeographProxyServerPort");
            //. 
            Serializer.startTag("", "GeographDataServerAddress");
            if (ConnectorModule.GeographDataServerAddress != null)
            	Serializer.text(ConnectorModule.GeographDataServerAddress);
            Serializer.endTag("", "GeographDataServerAddress");
            //. 
            Serializer.startTag("", "GeographDataServerPort");
            Serializer.text(Integer.toString(ConnectorModule.GeographDataServerPort));
            Serializer.endTag("", "GeographDataServerPort");
            //. 
            Serializer.startTag("", "LoopSleepTime");
            Serializer.text(Integer.toString(ConnectorModule.LoopSleepTime));
            Serializer.endTag("", "LoopSleepTime");
            //. 
            Serializer.startTag("", "TransmitInterval");
            Serializer.text(Integer.toString(ConnectorModule.TransmitInterval));
            Serializer.endTag("", "TransmitInterval");
            //.
            V = 0;
            if (ConnectorModule.OutgoingSetComponentDataOperationsQueue_flEnabled)
            	V = 1;
            Serializer.startTag("", "flOutgoingSetOperationsQueueIsEnabled");
            Serializer.text(Integer.toString(V));
            Serializer.endTag("", "flOutgoingSetOperationsQueueIsEnabled");
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
