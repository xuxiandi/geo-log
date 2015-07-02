package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.util.Xml;

import com.coremedia.iso.boxes.Container;
import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedReadWriteLock;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderModule;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.H264TrackImpl;

public class TSensorsModuleMeasurements {

	public static final String DataBaseFolder = TSensorsModule.Measurements_Folder();
	
	public static final String Domain = "Sensors.Measurements"; 
	
	public static class MeasurementDataIsNotFoundException extends Exception {

		private static final long serialVersionUID = 1L;

		public MeasurementDataIsNotFoundException() {
			super();
		}
	}
	
	public static class MeasurementDataIsTooBigException extends Exception {

		private static final long serialVersionUID = 2L;

		public MeasurementDataIsTooBigException() { 
			super();
		}
	}
	
	public static String CreateNewMeasurement(String DataBaseFolder, String NewMeasurementID, TSensorMeasurementDescriptor Descriptor) throws Exception {
		String MeasurementFolder = DataBaseFolder+"/"+NewMeasurementID;
		File Folder = new File(MeasurementFolder);
		if (!Folder.exists())
			Folder.mkdirs();
		//.
		if (Descriptor != null)
			SetMeasurementDescriptor(DataBaseFolder, NewMeasurementID, Descriptor);
		//.
		return NewMeasurementID;
	}
	
	public static String CreateNewMeasurement(String NewMeasurementID, TSensorMeasurementDescriptor Descriptor) throws Exception {
		return CreateNewMeasurement(DataBaseFolder, NewMeasurementID, Descriptor);
	}
	
	public static String CreateNewMeasurement(TSensorMeasurementDescriptor Descriptor) throws Exception {
		return CreateNewMeasurement(DataBaseFolder, TSensorMeasurement.GetNewID(), Descriptor);
	}
	
	public static String CreateNewMeasurement() throws Exception {
		return CreateNewMeasurement(null);
	}
	
	public static void DeleteMeasurement(String DataBaseFolder, String MeasurementID) throws IOException {
		TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.WriteLock(Domain, MeasurementID);
		try {
			String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
			File Folder = new File(MeasurementFolder);
			if (!Folder.exists())
				return; //. ->
			TFileSystem.RemoveFolder(Folder);
		}
		finally {
			MeasurementLock.WriteUnLock();
		}
	}
	
	public static void DeleteMeasurement(String MeasurementID) throws IOException {
		DeleteMeasurement(DataBaseFolder, MeasurementID);
	}
	
	public static boolean MeasurementExists(String MeasurementFolder) throws IOException {
		File Folder = new File(MeasurementFolder);
		return Folder.exists();
	}
	
	public static boolean MeasurementExists(String DataBaseFolder, String MeasurementID) throws IOException {
		String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
		return MeasurementExists(MeasurementFolder);
	}
	
	public static String GetMeasurementsList(String pDataBaseFolder, String Domain, double BeginTimestamp, double EndTimestamp, short Version) {
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
				try {
					TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.TryReadLock(Domain, MeasurementID);
					if (MeasurementLock != null)
						try {
							TSensorMeasurementDescriptor MeasurementDescriptor = GetMeasurementDescriptor(pDataBaseFolder, Domain, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
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
						finally {
							MeasurementLock.ReadUnLock();
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
	
	public static String GetMeasurementsList(String DataBaseFolder, String Domain, short Version) {
		return GetMeasurementsList(DataBaseFolder, Domain, -Double.MAX_VALUE,Double.MAX_VALUE, Version);
	}
	
	public static String GetMeasurementsList(double BeginTimestamp, double EndTimestamp, short Version) {
		return GetMeasurementsList(DataBaseFolder, Domain, BeginTimestamp, EndTimestamp, Version);
	}
	
	public static String GetMeasurementsList(short Version) {
		return GetMeasurementsList(DataBaseFolder, Domain, Version);
	}
	
	public static ArrayList<String> GetMeasurementsIDs(String DataBaseFolder) {
		File DF = new File(DataBaseFolder);
		if (!DF.exists())
			return null; //. ->
		File[] MeasurementFolders = DF.listFiles();
		Arrays.sort(MeasurementFolders, new Comparator<File>(){
		    public int compare(File f1, File f2) {
		        return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
		    }}
		);
		ArrayList<String> Result = new ArrayList<String>();
		for (int I = 0; I < MeasurementFolders.length; I++)
			if (MeasurementFolders[I].isDirectory()) {
				String MeasurementID = MeasurementFolders[I].getName();
				Result.add(MeasurementID);
			}
		return Result;
	}
	
	public static ArrayList<String> GetMeasurementsIDs() {
		return GetMeasurementsIDs(DataBaseFolder);
	}
	
	public static File[] GetMeasurementsFolderList() {
		File DF = new File(DataBaseFolder);
		if (!DF.exists())
			return null; //. ->
		File[] MeasurementFolders = DF.listFiles();
		Arrays.sort(MeasurementFolders, new Comparator<File>(){
			
		    public int compare(File f1, File f2) {
		        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		    }}
		);		
		int Cnt = MeasurementFolders.length;
		int Count = 0;
		for (int I = 0; I < Cnt; I++)
			if (MeasurementFolders[I].isDirectory()) 
				Count++;
		File[] Result = new File[Count];
		Count = 0;
		for (int I = 0; I < Cnt; I++)
			if (MeasurementFolders[I].isDirectory()) {
				Result[Count] = MeasurementFolders[I];
				Count++;
			}
		return Result;
	}
	
	public static File[] GetMeasurementFolderContent(String MeasurementID) {
		TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.ReadLock(Domain, MeasurementID);
		try {
			File MF = new File(DataBaseFolder+"/"+MeasurementID);
			if (!MF.exists())
				return null; //. ->
			File[] Result = MF.listFiles();
			Arrays.sort(Result, new Comparator<File>(){
			    public int compare(File f1, File f2) {
			        return Long.valueOf(f1.length()).compareTo(f2.length());
			    }}
			);		
			return Result;
		}
		finally {
			MeasurementLock.ReadUnLock();
		}
	}
	
	public static long GetMeasurementSize(String DataBaseFolder, String MeasurementID) throws IOException {
		TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.ReadLock(Domain, MeasurementID);
		try {
			File DF = new File(DataBaseFolder);
			if (!DF.exists())
				return 0; //. ->
			String MID = DataBaseFolder+"/"+MeasurementID;
			return TFileSystem.GetSize(MID);
		}
		finally {
			MeasurementLock.ReadUnLock();
		}
	}
	
	public static long GetMeasurementSize(String MeasurementID) throws IOException {
		return GetMeasurementSize(DataBaseFolder, MeasurementID);
	}
	
	public static void SetMeasurementDescriptor(String DataBaseFolder, String MeasurementID, TSensorMeasurementDescriptor Descriptor) throws Exception {
		TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.WriteLock(Domain, MeasurementID);
		try {
			int Version = 1;
			String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
			File Folder = new File(MeasurementFolder);
			if (!Folder.exists())
				return; //. ->
			String TDFN = MeasurementFolder+"/"+TSensorMeasurementDescriptor.DescriptorFileName+".tmp";
		    XmlSerializer serializer = Xml.newSerializer();
		    FileWriter writer = new FileWriter(TDFN);
		    try {
		        serializer.setOutput(writer);
		        serializer.startDocument("UTF-8",true);
		        serializer.startTag("", "ROOT");
		        //. Version
	            serializer.startTag("", "Version");
	            serializer.text(Integer.toString(Version));
	            serializer.endTag("", "Version");
		        //. ServerID
	            serializer.startTag("", "ServerID");
	            serializer.text(Integer.toString(Descriptor.ServerID));
	            serializer.endTag("", "ServerID");
		        //. GeographServerObjectID
	            serializer.startTag("", "GeographServerObjectID");
	            serializer.text(Long.toString(Descriptor.GeographServerObjectID));
	            serializer.endTag("", "GeographServerObjectID");
		        //. ID
	            Descriptor.ID = MeasurementID; //. set ID
	            serializer.startTag("", "ID");
	            serializer.text(Descriptor.ID);
	            serializer.endTag("", "ID");
		        //. GUID
	            serializer.startTag("", "GUID");
	            serializer.text(Descriptor.GUID);
	            serializer.endTag("", "GUID");
		        //. StartTimestamp
	            serializer.startTag("", "StartTimestamp");
	            serializer.text(Double.toString(Descriptor.StartTimestamp));
	            serializer.endTag("", "StartTimestamp");
		        //. FinishTimestamp
	            serializer.startTag("", "FinishTimestamp");
	            serializer.text(Double.toString(Descriptor.FinishTimestamp));
	            serializer.endTag("", "FinishTimestamp");
	            //. Model
	            if (Descriptor.Model != null) {
	                serializer.startTag("", "Model");
	            	Descriptor.Model.ToXMLNode(serializer);
	                serializer.endTag("", "Model");
	            }
	            //.
		        serializer.endTag("", "ROOT");
		        serializer.endDocument();
		    }
		    finally {
		    	writer.close();
		    }
			String DFN = MeasurementFolder+"/"+TSensorMeasurementDescriptor.DescriptorFileName;
			File TF = new File(TDFN);
			File F = new File(DFN);
			TF.renameTo(F);
		}
		finally {
			MeasurementLock.WriteUnLock();
		}
	}

	public static void SetMeasurementDescriptor(String MeasurementID, TSensorMeasurementDescriptor Descriptor) throws Exception {
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}
	
	public static TSensorMeasurementDescriptor GetMeasurementDescriptor(String DataBaseFolder, String Domain, String MeasurementID, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.ReadLock(Domain, MeasurementID);
		try {
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
		finally {
			MeasurementLock.ReadUnLock();
		}
	}

	public static TSensorMeasurementDescriptor GetMeasurementDescriptor(String DataBaseFolder, String Domain, String MeasurementID, TChannelProvider ChannelProvider) throws Exception {
		return GetMeasurementDescriptor(DataBaseFolder, Domain, MeasurementID, TSensorMeasurementDescriptor.class, ChannelProvider);
	}
	
	public static TSensorMeasurementDescriptor GetMeasurementDescriptor(String MeasurementID, TChannelProvider ChannelProvider) throws Exception {
		return GetMeasurementDescriptor(DataBaseFolder, Domain, MeasurementID, ChannelProvider);
	}
	
	public static void SetMeasurementStartTimestamp(String DataBaseFolder, String Domain, String MeasurementID) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, Domain, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.StartTimestamp = OleDate.UTCCurrentTimestamp();
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static void SetMeasurementStartTimestamp(String MeasurementID) throws Exception {
		SetMeasurementStartTimestamp(DataBaseFolder, Domain, MeasurementID);
	}
	
	public static double GetMeasurementStartTimestamp(String DataBaseFolder, String Domain, String MeasurementID) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, Domain, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			return 0.0; //. ->
		return Descriptor.StartTimestamp;
	}
	
	public static double GetMeasurementStartTimestamp(String MeasurementID) throws Exception {
		return GetMeasurementStartTimestamp(DataBaseFolder, Domain, MeasurementID);
	}
	
	public static void SetMeasurementFinishTimestamp(String DataBaseFolder, String Domain, String MeasurementID) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, Domain, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static void SetMeasurementFinishTimestamp(String MeasurementID) throws Exception {
		SetMeasurementFinishTimestamp(DataBaseFolder, Domain, MeasurementID);
	}
	
	public static double GetMeasurementFinishTimestamp(String DataBaseFolder, String Domain, String MeasurementID) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, Domain, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			return 0.0; //. ->
		return Descriptor.FinishTimestamp;
	}
	
	public static double GetMeasurementFinishTimestamp(String MeasurementID) throws Exception {
		return GetMeasurementFinishTimestamp(DataBaseFolder, Domain, MeasurementID);
	}
	
	public static void SetMeasurementFinish(String DataBaseFolder, String Domain, String MeasurementID, double FinishTimestamp) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, Domain, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.FinishTimestamp = FinishTimestamp;
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static void SetMeasurementFinish(String MeasurementID, double FinishTimestamp) throws Exception {
		SetMeasurementFinish(DataBaseFolder, Domain, MeasurementID, FinishTimestamp);
	}

	public static void SetMeasurementFinish(String MeasurementID) throws Exception {
		SetMeasurementFinish(DataBaseFolder, Domain, MeasurementID, OleDate.UTCCurrentTimestamp());
	}
	
	public static byte[] GetMeasurementData(String DataBaseFolder, String MeasurementID) throws IOException {
		TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.ReadLock(Domain, MeasurementID);
		try {
			final int BUFFER = 2048;
			String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
			File Folder = new File(MeasurementFolder);
			if (!Folder.exists())
				return null; //. ->
			File[] Files = Folder.listFiles();
			BufferedInputStream origin = null;       
			ByteArrayOutputStream dest = new ByteArrayOutputStream();
		    try {
		    	ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		    	try {
			    	byte data[] = new byte[BUFFER];        
			    	for(int i=0; i < Files.length; i++) { 
			    		String FileName = Files[i].getName();
			    		FileInputStream fi = new FileInputStream(Files[i]);
			    		try {
				    		origin = new BufferedInputStream(fi, BUFFER);
				    		try {
				    			ZipEntry entry = new ZipEntry(MeasurementID+"/"+FileName);         
				    			out.putNextEntry(entry);         
				    			int count;         
				    			while ((count = origin.read(data, 0, BUFFER)) != -1) 
				    				out.write(data, 0, count);
				    		}
				    		finally {
				    			origin.close();       
				    		}
			    		}
			    		finally {
			    			fi.close();
			    		}
			    	}
		    	}
		    	finally {
		    		out.close(); 	
		    	}
		    	return dest.toByteArray(); //. =>
		    }
		    finally {
		    	dest.close();	    	
		    }
		}
		finally {
			MeasurementLock.ReadUnLock();
		}
	}	
	
	public static byte[] GetMeasurementData(String MeasurementID) throws IOException {
		return GetMeasurementData(DataBaseFolder, MeasurementID);
	}
	
	public static void ValidateMeasurements(String DataBaseFolder) throws Exception {
		File DF = new File(DataBaseFolder);
		if (!DF.exists())
			return; //. ->
		File[] MeasurementFolders = DF.listFiles();
		for (int I = 0; I < MeasurementFolders.length; I++)
			if (MeasurementFolders[I].isDirectory()) {
				String MeasurementID = MeasurementFolders[I].getName();
				TSensorMeasurementDescriptor MeasurementDescriptor = GetMeasurementDescriptor(MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
				if ((MeasurementDescriptor != null) && MeasurementDescriptor.IsStarted() && !MeasurementDescriptor.IsFinished())
					ValidateMeasurement(DataBaseFolder,MeasurementID);
			}
	}
	
	public static void ValidateMeasurements() throws Exception {
		ValidateMeasurements(DataBaseFolder);
	}
	
	public static void ValidateMeasurement(String DataBaseFolder, String MeasurementID) throws Exception {
	}
	
	public static boolean ExportMeasurementToMP4File(String DataBaseFolder, String Domain, String MeasurementID, String ExportFile) throws Exception {
		TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.ReadLock(Domain, MeasurementID);
		try {
			com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor Measurement = (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor)GetMeasurementDescriptor(DataBaseFolder, Domain, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor.class, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
			if (Measurement == null)
				throw new MeasurementDataIsNotFoundException(); //. =>
			if (!Measurement.IsValid())
				throw new MeasurementDataIsNotFoundException(); //. =>
			//.
			String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
			Movie movie = new Movie();
			if (Measurement.Model != null) {
				int Cnt = Measurement.Model.Stream.Channels.size();
				for (int C = 0; C < Cnt; C++) {
					TChannel Channel = Measurement.Model.Stream.Channels.get(C);
					//.
					if (Channel instanceof TAACChannel) {
						AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(MeasurementFolder+"/"+TMeasurementDescriptor.AudioAACADTSFileName));
						movie.addTrack(aacTrack);
					}
					//.
					if (Channel instanceof TH264IChannel) {
						TH264IChannel H264IChannel = (TH264IChannel)Channel;
						//.
						H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(MeasurementFolder+"/"+TMeasurementDescriptor.VideoH264FileName), "eng", H264IChannel.FrameRate, 1);
						movie.addTrack(h264Track);
					}
				}
			}
			else {
				switch (Measurement.Mode) {
				
				case TVideoRecorderModule.MODE_FRAMESTREAM:
					if (Measurement.AudioPackets > 0) {
						AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl(MeasurementFolder+"/"+TMeasurementDescriptor.AudioAACADTSFileName));
						movie.addTrack(aacTrack);
					}
					if (Measurement.VideoPackets > 0) {
						H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl(MeasurementFolder+"/"+TMeasurementDescriptor.VideoH264FileName), "eng", Measurement.VideoFPS, 1);
						movie.addTrack(h264Track);
					}
					break; //. >
				
				default:
					return false; //. ->
				}
			}
			//.
			Container mp4file = new DefaultMp4Builder().build(movie);
			//.
			FileChannel fc = new FileOutputStream(new File(ExportFile)).getChannel();
			try {
				mp4file.writeContainer(fc);
			}
			finally {
				fc.close();
			}
			return true; //. ->
		}
		finally {
			MeasurementLock.ReadUnLock();
		}
	}
}
