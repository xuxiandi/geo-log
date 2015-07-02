package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Comparator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import android.app.Activity;
import android.content.Intent;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.TMeasurementProcessorPanel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsArchive.TMeasurementProcessHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerMyPlayer;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TSystemTGeographServerObject;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel;

public class TSensorsModuleMeasurements {

	public static String Context_Folder(long GeographServerObjectID) throws Exception {
		return TSystemTGeographServerObject.ContextFolder+"/"+Long.toString(GeographServerObjectID)+"/"+"SensorsModule"+"/"+"Measurements";		
	}
	
	public static String Context_GetMeasurementFolder(long GeographServerObjectID, String MeasurementID) throws Exception {
		return Context_Folder(GeographServerObjectID)+"/"+MeasurementID;
	}
	
	public static String Context_CreateMeasurementFolder(long GeographServerObjectID, String MeasurementID) throws Exception {
		String Result = Context_GetMeasurementFolder(GeographServerObjectID, MeasurementID);
		File F = new File(Result);
		F.mkdirs();
		//.
		return Result;
	}
	
	public static void Context_RemoveMeasurement(long GeographServerObjectID, String MeasurementID) throws Exception {
		String MeasurementFolder = Context_Folder(GeographServerObjectID)+"/"+MeasurementID;
		File Folder = new File(MeasurementFolder);
		if (!Folder.exists())
			return; //. ->
		TFileSystem.RemoveFolder(Folder);
	}
	
	public static String Context_GetMeasurementTempFolder(long GeographServerObjectID, String MeasurementID) throws Exception {
		return TGeoLogApplication.GetTempFolder()+"/"+TGeoLogApplication.ProfileName()+"/"+"GeographServerObject"+"/"+Long.toString(GeographServerObjectID)+"/"+"SensorsModule"+"/"+"Measurements"+"/"+MeasurementID;
	}
	
	public static String Context_CreateMeasurementTempFolder(long GeographServerObjectID, String MeasurementID) throws Exception {
		String Result = Context_GetMeasurementTempFolder(GeographServerObjectID, MeasurementID);
		File F = new File(Result);
		F.mkdirs();
		//.
		return Result;
	}
	
	public static String Context_GetMeasurementsList(String pDataBaseFolder, double BeginTimestamp, double EndTimestamp, short Version) {
		String Result = "";
		File DF = new File(pDataBaseFolder);
		if (!DF.exists())
			return Result; //. ->
		File[] MeasurementFolders = DF.listFiles();
		Arrays.sort(MeasurementFolders, new Comparator<File>(){
		    public int compare(File f1, File f2) {
		        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
		    }}
		);		
		for (int I = 0; I < MeasurementFolders.length; I++)
			if (MeasurementFolders[I].isDirectory()) {
				String MeasurementID = MeasurementFolders[I].getName();
				//.
				String ItemStr = null;
				TSensorMeasurementDescriptor MeasurementDescriptor = null;
				try {
					MeasurementDescriptor = Context_GetMeasurementDescriptor(pDataBaseFolder,MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
					if (MeasurementDescriptor != null) {
						if (MeasurementDescriptor.IsValid() && !((MeasurementDescriptor.StartTimestamp > EndTimestamp) || (MeasurementDescriptor.FinishTimestamp < BeginTimestamp))) {
							String TypeID = "?";
							String ContainerTypeID = "?";
							if (MeasurementDescriptor.Model != null) {
								TypeID = MeasurementDescriptor.Model.TypeID;
								ContainerTypeID = MeasurementDescriptor.Model.ContainerTypeID;
							}
							//.
							ItemStr = MeasurementDescriptor.ID+","+Double.toString(MeasurementDescriptor.StartTimestamp)+","+Double.toString(MeasurementDescriptor.FinishTimestamp)+','+TypeID+','+ContainerTypeID;
						}
					}
				}
				catch (Exception E) {
					ItemStr = null;
				}
				//.
				if (ItemStr != null)
					if (!Result.equals("")) 
						Result = Result+";"+ItemStr;
					else
						Result = ItemStr;
			}
		return Result;
	}
	
	public static TSensorMeasurementDescriptor[] Context_GetMeasurementsList(long GeographServerObjectID, double BeginTimestamp, double EndTimestamp) throws Exception {
		String ResultString = Context_GetMeasurementsList(Context_Folder(GeographServerObjectID), BeginTimestamp,EndTimestamp, (short)1/*Version*/);
		TSensorMeasurementDescriptor[] Result;
		if ((ResultString != null) && (!ResultString.equals(""))) {
			String[] Items = ResultString.split(";");
			Result = new TSensorMeasurementDescriptor[Items.length];
			for (int I = 0; I < Items.length; I++) {
				String[] Properties = Items[I].split(",");
				Result[I] = new TSensorMeasurementDescriptor();
				//.
				Result[I].ID = Properties[0];
				//.
				Result[I].StartTimestamp = Double.parseDouble(Properties[1]);
				Result[I].FinishTimestamp = Double.parseDouble(Properties[2]);
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel Model = new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel();
				Model.TypeID = Properties[3];
				Model.ContainerTypeID = Properties[4];
				Result[I].Model = Model;
				//.
				Result[I].Location = TSensorMeasurementDescriptor.LOCATION_CLIENT;
			}
		}
		else
			Result = new TSensorMeasurementDescriptor[0];
		return Result;
	}
	
	public static TSensorMeasurementDescriptor[] Context_GetMeasurementsList(long GeographServerObjectID) throws Exception {
		return Context_GetMeasurementsList(GeographServerObjectID, -Double.MAX_VALUE,Double.MAX_VALUE);
	}
	
	public static TSensorMeasurementDescriptor Context_GetMeasurementDescriptor(String DataBaseFolder, String MeasurementID, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
		String _DFN = MeasurementFolder+"/"+TSensorMeasurementDescriptor.DescriptorFileName;
		File F = new File(_DFN);
		if (!F.exists())  
			return null; //. ->
		//.
		TSensorMeasurementDescriptor Descriptor = (TSensorMeasurementDescriptor)DescriptorClass.newInstance();
		Descriptor.ID = MeasurementID; 
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(_DFN);
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
			Node node = TMyXML.SearchNode(XmlDoc.getDocumentElement(),"ServerID");
			if (node != null) 
				Descriptor.ServerID = Integer.parseInt(node.getFirstChild().getNodeValue());
			else
				Descriptor.ServerID = 0;
			//.
			node = TMyXML.SearchNode(XmlDoc.getDocumentElement(),"GeographServerObjectID");
			if (node != null) 
				Descriptor.GeographServerObjectID = Long.parseLong(node.getFirstChild().getNodeValue());
			else
				Descriptor.GeographServerObjectID = 0;
			//.
			node = TMyXML.SearchNode(XmlDoc.getDocumentElement(),"GUID");
			if (node != null) {
				node = node.getFirstChild();
				if (node != null) 
					Descriptor.GUID = node.getNodeValue();
				else
					Descriptor.GUID = "";
			}
			else
				Descriptor.GUID = "";
			//.
			Descriptor.StartTimestamp = Double.parseDouble(XmlDoc.getDocumentElement().getElementsByTagName("StartTimestamp").item(0).getFirstChild().getNodeValue());
			Descriptor.FinishTimestamp = Double.parseDouble(XmlDoc.getDocumentElement().getElementsByTagName("FinishTimestamp").item(0).getFirstChild().getNodeValue());
			//.
			Node ModelNode = TMyXML.SearchNode(XmlDoc.getDocumentElement(),"Model");
			if (ModelNode != null) {
				Descriptor.Model = new TSensorMeasurementModel(ModelNode, ChannelProvider);
				Descriptor.Model.Initialize(MeasurementFolder);
			}
			else
				Descriptor.Model = null;
			break; //. >
		default:
			throw new Exception("unknown descriptor data version, version: "+Integer.toString(Version)); //. =>
		}
		return Descriptor;
	}

	public static TSensorMeasurementDescriptor Context_GetMeasurementDescriptor(String DataBaseFolder, String MeasurementID, TChannelProvider ChannelProvider) throws Exception {
		return Context_GetMeasurementDescriptor(DataBaseFolder,MeasurementID, TSensorMeasurementDescriptor.class, ChannelProvider);
	}
	
	public static boolean Context_IsMeasurementExist(long GeographServerObjectID, String MeasurementID) throws Exception {
		File F = new File(Context_GetMeasurementFolder(GeographServerObjectID, MeasurementID));
		return F.exists();
	}
	
	public static void Context_ProcessMeasurementByFolder(TMeasurementProcessHandler ProcessHandler, int ProcessorRequest, long GeographServerObjectID, String MeasurementFolder, double MeasurementStartPosition, Activity context) throws Exception {
    	File MF = new File(MeasurementFolder);
    	String MeasurementDatabaseFolder = MF.getParent(); 
    	String MeasurementID = MF.getName();
		TSensorMeasurement Measurement = new TSensorMeasurement(MeasurementDatabaseFolder, MeasurementDatabaseFolder, MeasurementID, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
    	//.
		boolean flProcessAsDefault = true;
		if (ProcessHandler != null) 
			flProcessAsDefault = (!ProcessHandler.ProcessMeasurement(Measurement, MeasurementStartPosition));
		if (flProcessAsDefault && (Measurement.Descriptor.Model != null)) {
			if (!Measurement.Descriptor.IsTypeOf(com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.ModelTypeID)) {
	            Intent ProcessorPanel = new Intent(context, TMeasurementProcessorPanel.class);
	            ProcessorPanel.putExtra("MeasurementDatabaseFolder",Measurement.DatabaseFolder);
	            ProcessorPanel.putExtra("MeasurementID",Measurement.Descriptor.ID);
	            ProcessorPanel.putExtra("MeasurementStartPosition",MeasurementStartPosition);
        		context.startActivityForResult(ProcessorPanel, ProcessorRequest);	            	
			}
			else {
	            Intent AVProcessorPanel = new Intent(context, TVideoRecorderServerMyPlayer.class);
	            AVProcessorPanel.putExtra("MeasurementDatabaseFolder",Measurement.DatabaseFolder);
	            AVProcessorPanel.putExtra("MeasurementID",Measurement.Descriptor.ID);
	            AVProcessorPanel.putExtra("MeasurementStartPosition",MeasurementStartPosition);
        		context.startActivityForResult(AVProcessorPanel, ProcessorRequest);	            	
			}
		}
	}
	
	public static void Context_ProcessMeasurement(TMeasurementProcessHandler PlayHandler, int PlayerRequest, long GeographServerObjectID, String MeasurementID, double MeasurementStartPosition, Activity context) throws Exception {
		Context_ProcessMeasurementByFolder(PlayHandler,PlayerRequest, GeographServerObjectID, Context_GetMeasurementFolder(GeographServerObjectID, MeasurementID), MeasurementStartPosition, context);
	}
}
