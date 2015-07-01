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
import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedLock;
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
	
	public static synchronized String CreateNewMeasurement(String DataBaseFolder, String NewMeasurementID, TSensorMeasurementDescriptor Descriptor) throws Exception {
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
	
	public static synchronized String CreateNewMeasurement(String NewMeasurementID, TSensorMeasurementDescriptor Descriptor) throws Exception {
		return CreateNewMeasurement(DataBaseFolder, NewMeasurementID, Descriptor);
	}
	
	public static synchronized String CreateNewMeasurement(TSensorMeasurementDescriptor Descriptor) throws Exception {
		return CreateNewMeasurement(DataBaseFolder, TSensorMeasurement.GetNewID(), Descriptor);
	}
	
	public static synchronized String CreateNewMeasurement() throws Exception {
		return CreateNewMeasurement(null);
	}
	
	public static synchronized void DeleteMeasurement(String DataBaseFolder, String MeasurementID) throws IOException {
		TNamedLock MeasurementLock = TNamedLock.Lock(Domain, MeasurementID);
		try {
			String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
			File Folder = new File(MeasurementFolder);
			if (!Folder.exists())
				return; //. ->
			TFileSystem.RemoveFolder(Folder);
		}
		finally {
			MeasurementLock.UnLock();
		}
	}
	
	public static synchronized void DeleteMeasurement(String MeasurementID) throws IOException {
		DeleteMeasurement(DataBaseFolder, MeasurementID);
	}
	
	public static synchronized boolean MeasurementExists(String MeasurementFolder) throws IOException {
		File Folder = new File(MeasurementFolder);
		return Folder.exists();
	}
	
	public static synchronized boolean MeasurementExists(String DataBaseFolder, String MeasurementID) throws IOException {
		String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
		return MeasurementExists(MeasurementFolder);
	}
	
	public static synchronized String GetMeasurementsList(String pDataBaseFolder, double BeginTimestamp, double EndTimestamp, short Version) {
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
					MeasurementDescriptor = GetMeasurementDescriptor(pDataBaseFolder,MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
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
	
	public static synchronized String GetMeasurementsList(String DataBaseFolder, short Version) {
		return GetMeasurementsList(DataBaseFolder, -Double.MAX_VALUE,Double.MAX_VALUE, Version);
	}
	
	public static synchronized String GetMeasurementsList(double BeginTimestamp, double EndTimestamp, short Version) {
		return GetMeasurementsList(DataBaseFolder, BeginTimestamp, EndTimestamp, Version);
	}
	
	public static synchronized String GetMeasurementsList(short Version) {
		return GetMeasurementsList(DataBaseFolder, Version);
	}
	
	public static synchronized ArrayList<String> GetMeasurementsIDs(String DataBaseFolder) {
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
	
	public static synchronized ArrayList<String> GetMeasurementsIDs() {
		return GetMeasurementsIDs(DataBaseFolder);
	}
	
	public static synchronized File[] GetMeasurementsFolderList() {
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
	
	public static synchronized File[] GetMeasurementFolderContent(String MeasurementID) {
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
	
	public static synchronized long GetMeasurementSize(String DataBaseFolder, String MeasurementID) throws IOException {
		int Result = 0;
		File DF = new File(DataBaseFolder);
		if (!DF.exists())
			return Result; //. ->
		String MID = DataBaseFolder+"/"+MeasurementID;
		return TFileSystem.GetSize(MID);
	}
	
	public static synchronized long GetMeasurementSize(String MeasurementID) throws IOException {
		return GetMeasurementSize(DataBaseFolder, MeasurementID);
	}
	
	public static synchronized void SetMeasurementDescriptor(String DataBaseFolder, String MeasurementID, TSensorMeasurementDescriptor Descriptor) throws Exception {
		TNamedLock MeasurementLock = TNamedLock.Lock(Domain, MeasurementID);
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
			MeasurementLock.UnLock();
		}
	}

	public static synchronized void SetMeasurementDescriptor(String MeasurementID, TSensorMeasurementDescriptor Descriptor) throws Exception {
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}
	
	public static synchronized TSensorMeasurementDescriptor GetMeasurementDescriptor(String DataBaseFolder, String MeasurementID, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		TNamedLock MeasurementLock = TNamedLock.Lock(Domain, MeasurementID);
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
			MeasurementLock.UnLock();
		}
	}

	public static synchronized TSensorMeasurementDescriptor GetMeasurementDescriptor(String DataBaseFolder, String MeasurementID, TChannelProvider ChannelProvider) throws Exception {
		return GetMeasurementDescriptor(DataBaseFolder, MeasurementID, TSensorMeasurementDescriptor.class, ChannelProvider);
	}
	
	public static synchronized TSensorMeasurementDescriptor GetMeasurementDescriptor(String MeasurementID, TChannelProvider ChannelProvider) throws Exception {
		return GetMeasurementDescriptor(DataBaseFolder, MeasurementID, ChannelProvider);
	}
	
	public static synchronized void SetMeasurementStartTimestamp(String DataBaseFolder, String MeasurementID) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.StartTimestamp = OleDate.UTCCurrentTimestamp();
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static synchronized void SetMeasurementStartTimestamp(String MeasurementID) throws Exception {
		SetMeasurementStartTimestamp(DataBaseFolder, MeasurementID);
	}
	
	public static synchronized double GetMeasurementStartTimestamp(String DataBaseFolder, String MeasurementID) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			return 0.0; //. ->
		return Descriptor.StartTimestamp;
	}
	
	public static synchronized double GetMeasurementStartTimestamp(String MeasurementID) throws Exception {
		return GetMeasurementStartTimestamp(DataBaseFolder, MeasurementID);
	}
	
	public static synchronized void SetMeasurementFinishTimestamp(String DataBaseFolder, String MeasurementID) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static synchronized void SetMeasurementFinishTimestamp(String MeasurementID) throws Exception {
		SetMeasurementFinishTimestamp(DataBaseFolder, MeasurementID);
	}
	
	public static synchronized double GetMeasurementFinishTimestamp(String DataBaseFolder, String MeasurementID) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			return 0.0; //. ->
		return Descriptor.FinishTimestamp;
	}
	
	public static synchronized double GetMeasurementFinishTimestamp(String MeasurementID) throws Exception {
		return GetMeasurementFinishTimestamp(DataBaseFolder, MeasurementID);
	}
	
	public static synchronized void SetMeasurementFinish(String DataBaseFolder, String MeasurementID, double FinishTimestamp) throws Exception {
		TSensorMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.FinishTimestamp = FinishTimestamp;
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static synchronized void SetMeasurementFinish(String MeasurementID, double FinishTimestamp) throws Exception {
		SetMeasurementFinish(DataBaseFolder, MeasurementID, FinishTimestamp);
	}

	public static synchronized void SetMeasurementFinish(String MeasurementID) throws Exception {
		SetMeasurementFinish(DataBaseFolder, MeasurementID, OleDate.UTCCurrentTimestamp());
	}
	
	public static synchronized byte[] GetMeasurementData(String DataBaseFolder, String MeasurementID) throws IOException {
		TNamedLock MeasurementLock = TNamedLock.Lock(Domain, MeasurementID);
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
			MeasurementLock.UnLock();
		}
	}	
	
	public static synchronized byte[] GetMeasurementData(String MeasurementID) throws IOException {
		return GetMeasurementData(DataBaseFolder, MeasurementID);
	}
	
	public static synchronized void ValidateMeasurements(String DataBaseFolder) throws Exception {
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
	
	public static synchronized void ValidateMeasurements() throws Exception {
		ValidateMeasurements(DataBaseFolder);
	}
	
	public static synchronized void ValidateMeasurement(String DataBaseFolder, String MeasurementID) throws Exception {
	}
	
	public static synchronized boolean ExportMeasurementToMP4File(String DataBaseFolder, String MeasurementID, String ExportFile) throws Exception {
		TSensorMeasurementDescriptor SensorMeasurement = GetMeasurementDescriptor(DataBaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
		if (SensorMeasurement == null)
			throw new MeasurementDataIsNotFoundException(); //. =>
		if (!SensorMeasurement.IsValid())
			throw new MeasurementDataIsNotFoundException(); //. =>
		//.
		if (!(SensorMeasurement instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor))
			return false; //. ->
		com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor Measurement = (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurementDescriptor)SensorMeasurement;
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
}
