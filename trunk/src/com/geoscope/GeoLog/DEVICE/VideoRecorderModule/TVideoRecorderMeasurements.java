package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xmlpull.v1.XmlSerializer;

import android.os.Environment;
import android.util.Xml;

import com.geoscope.GeoLog.Utils.OleDate;

public class TVideoRecorderMeasurements {

	public static final String MyDataBaseFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"Geo.Log.VideoRecorder";
	public static final String TempDataBaseFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+"Temp";
	public static final String DescriptorFileName = "Data.xml";
	public static final String AudioFileName = "Audio.rtp";
	public static final String VideoFileName = "Video.rtp";
	public static final String MediaMPEG4FileName = "Data.mp4";
	public static final String Media3GPFileName = "Data.3gp";
	public static final String AudioSampleFileName = "Audio.samples";
	public static final String VideoFrameFileName = "Video.frames";
	///? public static final double MaxMeasurementDuration = (1.0/24)*1; //. hours
	public static final int MeasurementDataTransferableLimit = 5*1024*1024;
	
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
	
	public static synchronized String CreateNewMeasurement(String DataBaseFolder, String NewMeasurementID, short Mode) throws IOException {
		TMeasurementDescriptor Descriptor = new TMeasurementDescriptor(NewMeasurementID);
		Descriptor.Mode = Mode;
		return CreateNewMeasurement(DataBaseFolder,NewMeasurementID,Descriptor);
	}
	
	public static synchronized String CreateNewMeasurement(String DataBaseFolder, String NewMeasurementID, TMeasurementDescriptor Descriptor) throws IOException {
		String MeasurementFolder = DataBaseFolder+"/"+NewMeasurementID;
		File Folder = new File(MeasurementFolder);
		if (!Folder.exists())
			Folder.mkdirs();
		//.
		SetMeasurementDescriptor(DataBaseFolder, NewMeasurementID, Descriptor);
		//.
		return NewMeasurementID;
	}
	
	public static synchronized String CreateNewMeasurement(String NewMeasurementID, short Mode) throws IOException {
		return CreateNewMeasurement(MyDataBaseFolder,NewMeasurementID,Mode);
	}
	
	public static synchronized double GetCurrentTime() {
		return OleDate.UTCCurrentTimestamp();
	}
	
	public static String TimestampToMeasurementID(double Timestamp) {
		return Double.toString(Timestamp);
	}
	
	public static synchronized String CreateNewMeasurementID() {
		return TimestampToMeasurementID(GetCurrentTime());
	}
	
	public static synchronized String CreateNewMeasurement(short Mode) throws IOException {
		return CreateNewMeasurement(MyDataBaseFolder,CreateNewMeasurementID(),Mode);
	}
	
	public static synchronized void DeleteMeasurement(String DataBaseFolder, String MeasurementID) throws IOException {
		String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
		File Folder = new File(MeasurementFolder);
		if (!Folder.exists())
			return; //. ->
		RemoveFolder(Folder);
	}
	
	public static synchronized void DeleteMeasurement(String MeasurementID) throws IOException {
		DeleteMeasurement(MyDataBaseFolder, MeasurementID);
	}
	
	public static synchronized String GetMeasurementsList(String DataBaseFolder) {
		String Result = "";
		File DF = new File(DataBaseFolder);
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
				String ItemStr = MeasurementID;
				TMeasurementDescriptor MeasurementDescriptor = null;
				try {
					MeasurementDescriptor = GetMeasurementDescriptor(MeasurementID);
					if (MeasurementDescriptor != null) 
						ItemStr = ItemStr+","+Double.toString(MeasurementDescriptor.StartTimestamp)+","+Double.toString(MeasurementDescriptor.FinishTimestamp);
					else 
						ItemStr = ItemStr+",0.0,0.0";
				}
				catch (Exception E) {
					ItemStr = ItemStr+",0.0,0.0";
				}
				if (MeasurementDescriptor != null) {
					switch (MeasurementDescriptor.Mode) {
					case TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1:
					case TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1:
						File AudioFile = new File(MeasurementFolders[I].getAbsolutePath()+"/"+AudioFileName);
						if (AudioFile.exists()) 
							ItemStr = ItemStr+","+Long.toString(AudioFile.length());
						else
							ItemStr = ItemStr+",0";
						//.
						File VideoFile = new File(MeasurementFolders[I].getAbsolutePath()+"/"+VideoFileName);
						if (VideoFile.exists()) 
							ItemStr = ItemStr+","+Long.toString(VideoFile.length());
						else
							ItemStr = ItemStr+",0";
						break; //. >
						
					case TVideoRecorderModule.MODE_MPEG4:
						File MediaFile = new File(MeasurementFolders[I].getAbsolutePath()+"/"+MediaMPEG4FileName);
						if (MediaFile.exists()) {
							long SS = MediaFile.length();
							int VS = 0;
							if (MeasurementDescriptor.VideoPackets != 0)
								VS = (int)(0.9*SS);
							int AS = 0;
							if (MeasurementDescriptor.AudioPackets != 0)
								AS = (int)(SS-VS);
							ItemStr = ItemStr+","+Integer.toString(AS)+","+Long.toString(VS);
						}
						else
							ItemStr = ItemStr+",0,0";
						break; //. >
						
					case TVideoRecorderModule.MODE_3GP:
						MediaFile = new File(MeasurementFolders[I].getAbsolutePath()+"/"+Media3GPFileName);
						if (MediaFile.exists()) {
							long SS = MediaFile.length();
							int VS = 0;
							if (MeasurementDescriptor.VideoPackets != 0)
								VS = (int)(0.9*SS);
							int AS = 0;
							if (MeasurementDescriptor.AudioPackets != 0)
								AS = (int)(SS-VS);
							ItemStr = ItemStr+","+Integer.toString(AS)+","+Long.toString(VS);
						}
						else
							ItemStr = ItemStr+",0,0";
						break; //. >
						
					default:
						ItemStr = ItemStr+",0,0";
						break; //. >
					}
				}
				else
					ItemStr = ItemStr+",0,0";
				//.
				if (!Result.equals("")) 
					Result = Result+";"+ItemStr;
				else
					Result = ItemStr;
			}
		return Result;
	}
	
	public static synchronized String GetMeasurementsList() {
		return GetMeasurementsList(MyDataBaseFolder);
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
		return GetMeasurementsIDs(MyDataBaseFolder);
	}
	
	public static synchronized File[] GetMeasurementsFolderList() {
		File DF = new File(MyDataBaseFolder);
		if (!DF.exists())
			return null; //. ->
		File[] MeasurementFolders = DF.listFiles();
		Arrays.sort(MeasurementFolders, new Comparator<File>(){
		    public int compare(File f1, File f2) {
		        return Long.valueOf(f1.lastModified()).compareTo(f2.lastModified());
		    }}
		);		
		int Count = 0;
		for (int I = 0; I < MeasurementFolders.length; I++)
			if (MeasurementFolders[I].isDirectory()) 
				Count++;
		File[] Result = new File[Count];
		for (int I = 0; I < MeasurementFolders.length; I++)
			if (MeasurementFolders[I].isDirectory())
				if (I < Count)
					Result[I] = MeasurementFolders[I];
		return Result;
	}
	
	public static synchronized File[] GetMeasurementFolderContent(String MeasurementID) {
		File MF = new File(MyDataBaseFolder+"/"+MeasurementID);
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
	
	public static synchronized int GetMeasurementSize(String DataBaseFolder, String MeasurementID, boolean flDescriptor, boolean flAudio, boolean flVideo) throws IOException {
		int Result = 0;
		File DF = new File(DataBaseFolder);
		if (!DF.exists())
			return Result; //. ->
		String MID = DataBaseFolder+"/"+MeasurementID;
		//.
		if (flDescriptor) {
			File DescriptorFile = new File(MID+"/"+DescriptorFileName);
			if (DescriptorFile.exists())
				Result += DescriptorFile.length(); 
		}
		//.
		if (flAudio) {
			File AudioFile = new File(MID+"/"+AudioFileName);
			if (AudioFile.exists())
				Result += AudioFile.length(); 
		}
		//.
		if (flVideo) {
			File VideoFile = new File(MID+"/"+VideoFileName);
			if (VideoFile.exists()) 
				Result += VideoFile.length();
		}
		//.
		return Result;
	}
	
	public static synchronized int GetMeasurementSize(String MeasurementID, boolean flDescriptor, boolean flAudio, boolean flVideo) throws IOException {
		return GetMeasurementSize(MyDataBaseFolder, MeasurementID, flDescriptor,flAudio,flVideo);
	}
	
	public static synchronized void SetMeasurementDescriptor(String DataBaseFolder, String MeasurementID, TMeasurementDescriptor Descriptor) throws IOException {
		int Version = 1;
		String MeasurementFolder = DataBaseFolder+"/"+MeasurementID;
		File Folder = new File(MeasurementFolder);
		if (!Folder.exists())
			return; //. ->
		String TDFN = MeasurementFolder+"/"+DescriptorFileName+".tmp";
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
	        //. ID
            String SID = "";
            if (Descriptor.ID != null)
            	SID = Descriptor.ID; 
            serializer.startTag("", "ID");
            serializer.text(SID);
            serializer.endTag("", "ID");
	        //. Mode
            serializer.startTag("", "Mode");
            serializer.text(Short.toString(Descriptor.Mode));
            serializer.endTag("", "Mode");
	        //. StartTimestamp
            serializer.startTag("", "StartTimestamp");
            serializer.text(Double.toString(Descriptor.StartTimestamp));
            serializer.endTag("", "StartTimestamp");
	        //. FinishTimestamp
            serializer.startTag("", "FinishTimestamp");
            serializer.text(Double.toString(Descriptor.FinishTimestamp));
            serializer.endTag("", "FinishTimestamp");
            //. AudioFormat
            if (Descriptor.AudioFormat > 0) {
                serializer.startTag("", "AudioFormat");
                serializer.text(Integer.toString(Descriptor.AudioFormat));
                serializer.endTag("", "AudioFormat");
            }
            //. AudioSPS
            if (Descriptor.AudioSPS > 0) {
                serializer.startTag("", "AudioSPS");
                serializer.text(Integer.toString(Descriptor.AudioSPS));
                serializer.endTag("", "AudioSPS");
            }
	        //. AudioPackets
            serializer.startTag("", "AudioPackets");
            serializer.text(Integer.toString(Descriptor.AudioPackets));
            serializer.endTag("", "AudioPackets");
            //. VideoFormat
            if (Descriptor.VideoFormat > 0) {
                serializer.startTag("", "VideoFormat");
                serializer.text(Integer.toString(Descriptor.VideoFormat));
                serializer.endTag("", "VideoFormat");
            }
            //. VideoFPS
            if (Descriptor.VideoFPS > 0) {
                serializer.startTag("", "VideoFPS");
                serializer.text(Integer.toString(Descriptor.VideoFPS));
                serializer.endTag("", "VideoFPS");
            }
	        //. VideoPackets
            serializer.startTag("", "VideoPackets");
            serializer.text(Integer.toString(Descriptor.VideoPackets));
            serializer.endTag("", "VideoPackets");
            //.
	        serializer.endTag("", "ROOT");
	        serializer.endDocument();
	    }
	    finally {
	    	writer.close();
	    }
		String DFN = MeasurementFolder+"/"+DescriptorFileName;
		File TF = new File(TDFN);
		File F = new File(DFN);
		TF.renameTo(F);
	}

	public static synchronized void SetMeasurementDescriptor(String MeasurementID, TMeasurementDescriptor Descriptor) throws IOException {
		SetMeasurementDescriptor(MyDataBaseFolder, MeasurementID, Descriptor);
	}
	
	public static synchronized TMeasurementDescriptor GetMeasurementDescriptor(String DataBaseFolder, String MeasurementID) throws Exception {
		TMeasurementDescriptor Descriptor = null;
		String _SFN = DataBaseFolder+"/"+MeasurementID+"/"+DescriptorFileName;
		File F = new File(_SFN);
		if (!F.exists()) 
			return Descriptor; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(_SFN);
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
			Descriptor = new TMeasurementDescriptor(MeasurementID);
			//.
			Descriptor.ID = XmlDoc.getDocumentElement().getElementsByTagName("ID").item(0).getFirstChild().getNodeValue();
			Descriptor.Mode = Short.parseShort(XmlDoc.getDocumentElement().getElementsByTagName("Mode").item(0).getFirstChild().getNodeValue());
			Descriptor.StartTimestamp = Double.parseDouble(XmlDoc.getDocumentElement().getElementsByTagName("StartTimestamp").item(0).getFirstChild().getNodeValue());
			Descriptor.FinishTimestamp = Double.parseDouble(XmlDoc.getDocumentElement().getElementsByTagName("FinishTimestamp").item(0).getFirstChild().getNodeValue());
			try {
				Descriptor.AudioFormat = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("AudioFormat").item(0).getFirstChild().getNodeValue());
			}
			catch (NullPointerException NPE) {}
			try {
				Descriptor.AudioSPS = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("AudioSPS").item(0).getFirstChild().getNodeValue());
			}
			catch (NullPointerException NPE) {}
			Descriptor.AudioPackets = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("AudioPackets").item(0).getFirstChild().getNodeValue());
			try {
				Descriptor.VideoFormat = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("VideoFormat").item(0).getFirstChild().getNodeValue());
			}
			catch (NullPointerException NPE) {}
			try {
				Descriptor.VideoFPS = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("VideoFPS").item(0).getFirstChild().getNodeValue());
			}
			catch (NullPointerException NPE) {}
			Descriptor.VideoPackets = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("VideoPackets").item(0).getFirstChild().getNodeValue());
			break; //. >
		default:
			throw new Exception("unknown descriptor data version, version: "+Integer.toString(Version)); //. =>
		}
		return Descriptor;
	}

	public static synchronized TMeasurementDescriptor GetMeasurementDescriptor(String MeasurementID) throws Exception {
		return GetMeasurementDescriptor(MyDataBaseFolder, MeasurementID);
	}
	
	public static synchronized void SetMeasurementStartTimestamp(String DataBaseFolder, String MeasurementID) throws Exception {
		TMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.StartTimestamp = OleDate.UTCCurrentTimestamp();
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static synchronized void SetMeasurementStartTimestamp(String MeasurementID) throws Exception {
		SetMeasurementStartTimestamp(MyDataBaseFolder, MeasurementID);
	}
	
	public static synchronized double GetMeasurementStartTimestamp(String DataBaseFolder, String MeasurementID) throws Exception {
		TMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID);
		if (Descriptor == null)
			return 0.0; //. ->
		return Descriptor.StartTimestamp;
	}
	
	public static synchronized double GetMeasurementStartTimestamp(String MeasurementID) throws Exception {
		return GetMeasurementStartTimestamp(MyDataBaseFolder, MeasurementID);
	}
	
	public static synchronized void SetMeasurementFinishTimestamp(String DataBaseFolder, String MeasurementID) throws Exception {
		TMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static synchronized void SetMeasurementFinishTimestamp(String MeasurementID) throws Exception {
		SetMeasurementFinishTimestamp(MyDataBaseFolder, MeasurementID);
	}
	
	public static synchronized double GetMeasurementFinishTimestamp(String DataBaseFolder, String MeasurementID) throws Exception {
		TMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID);
		if (Descriptor == null)
			return 0.0; //. ->
		return Descriptor.FinishTimestamp;
	}
	
	public static synchronized double GetMeasurementFinishTimestamp(String MeasurementID) throws Exception {
		return GetMeasurementFinishTimestamp(MyDataBaseFolder, MeasurementID);
	}
	
	public static synchronized void SetMeasurementFinish(String DataBaseFolder, String MeasurementID, int AudioPackets, int VideoPackets) throws Exception {
		TMeasurementDescriptor Descriptor = GetMeasurementDescriptor(DataBaseFolder, MeasurementID);
		if (Descriptor == null)
			throw new Exception("measurement descriptor is not found, ID:"+MeasurementID); //. =>
		//.
		Descriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		Descriptor.AudioPackets = AudioPackets;
		Descriptor.VideoPackets = VideoPackets;
		SetMeasurementDescriptor(DataBaseFolder, MeasurementID, Descriptor);
	}

	public static synchronized void SetMeasurementFinish(String MeasurementID, int AudioPackets, int VideoPackets) throws Exception {
		SetMeasurementFinish(MyDataBaseFolder, MeasurementID, AudioPackets,VideoPackets);
	}
	
	public static synchronized byte[] GetMeasurementData(String DataBaseFolder, String MeasurementID, boolean flDescriptor, boolean flAudio, boolean flVideo) throws IOException {
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
		    		if ((FileName.equals(DescriptorFileName) && flDescriptor) || (FileName.equals(AudioFileName) && flAudio)  || (FileName.equals(VideoFileName) && flVideo)) {
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
	
	public static synchronized byte[] GetMeasurementData(String MeasurementID, boolean flDescriptor, boolean flAudio, boolean flVideo) throws IOException {
		return GetMeasurementData(MyDataBaseFolder, MeasurementID, flDescriptor,flAudio,flVideo);
	}
	
	public static synchronized byte[] GetMeasurementDataFragment(String MeasurementID, TMeasurementDescriptor CurrentMeasurement, double StartTimestamp, double FinishTimestamp, boolean flDescriptor, boolean flAudio, boolean flVideo) throws Exception {
		TMeasurementDescriptor Measurement = GetMeasurementDescriptor(MeasurementID);
		if (Measurement == null)
			throw new MeasurementDataIsNotFoundException(); //. =>
		//. if requested measurement is current so use current one
		if ((CurrentMeasurement != null) && (Measurement.ID.equals(CurrentMeasurement.ID))) 
			Measurement = CurrentMeasurement;
		//.
		if (!Measurement.IsFinished())
			throw new MeasurementDataIsNotFoundException(); //. =>
		if ((StartTimestamp >= Measurement.FinishTimestamp) || (FinishTimestamp <= Measurement.StartTimestamp))
			throw new MeasurementDataIsNotFoundException(); //. =>
		if (FinishTimestamp == 0.0)
			FinishTimestamp = Measurement.FinishTimestamp;
		if (StartTimestamp >= FinishTimestamp)
			throw new MeasurementDataIsNotFoundException(); //. =>
		String TempMeasurementID = CreateNewMeasurement(TempDataBaseFolder, TimestampToMeasurementID(StartTimestamp),Measurement.Mode);
		try {
			int SummarySize = 0;
			//. copy descriptor
			if (flDescriptor) {
				File SrcFile = new File(MyDataBaseFolder+"/"+MeasurementID+"/"+DescriptorFileName);
				File DestFile = new File(TempDataBaseFolder+"/"+TempMeasurementID+"/"+DescriptorFileName); 
				CopyFile(SrcFile, DestFile);
				SummarySize += DestFile.length()+100/*additive for inscreasing*/;
			}
			//.
			double AudioPacketTimeDelta = 0;
			int AudioStartPos = 0;
			int AudioFinishPos = 0;
			if (flAudio && (Measurement.AudioPackets > 0)) {
				AudioPacketTimeDelta = (Measurement.FinishTimestamp-Measurement.StartTimestamp)/Measurement.AudioPackets;
				if (StartTimestamp < Measurement.StartTimestamp)
					StartTimestamp = Measurement.StartTimestamp;
				AudioStartPos = (int)((StartTimestamp-Measurement.StartTimestamp)/AudioPacketTimeDelta);
				if (FinishTimestamp > Measurement.FinishTimestamp)
					FinishTimestamp = Measurement.FinishTimestamp;
				AudioFinishPos = Measurement.AudioPackets-(int)((Measurement.FinishTimestamp-FinishTimestamp)/AudioPacketTimeDelta);
			}
			int AudioPacketCounter = 0;
			//.
			double VideoPacketTimeDelta = 0;
			int VideoStartPos = 0;
			int VideoFinishPos = 0;
			if (flVideo && (Measurement.VideoPackets > 0)) {
				VideoPacketTimeDelta = (Measurement.FinishTimestamp-Measurement.StartTimestamp)/Measurement.VideoPackets;
				if (StartTimestamp < Measurement.StartTimestamp)
					StartTimestamp = Measurement.StartTimestamp;
				VideoStartPos = (int)((StartTimestamp-Measurement.StartTimestamp)/VideoPacketTimeDelta);
				if (FinishTimestamp > Measurement.FinishTimestamp)
					FinishTimestamp = Measurement.FinishTimestamp;
				VideoFinishPos = Measurement.VideoPackets-(int)((Measurement.FinishTimestamp-FinishTimestamp)/VideoPacketTimeDelta);
			}
			int VideoPacketCounter = 0;
			//.
			FileInputStream AIS = null;
	        DataInputStream ADIS = null;
			FileOutputStream AOS = null;
			if (flAudio && (Measurement.AudioPackets > 0)) {
				AIS = new FileInputStream(MyDataBaseFolder+"/"+MeasurementID+"/"+AudioFileName);
		        ADIS = new DataInputStream(AIS);
				AOS = new FileOutputStream(TempDataBaseFolder+"/"+TempMeasurementID+"/"+AudioFileName);
			}
			try {
				FileInputStream VIS = null;
		        DataInputStream VDIS = null;
				FileOutputStream VOS = null;
				if (flVideo && (Measurement.VideoPackets > 0)) {
					VIS = new FileInputStream(MyDataBaseFolder+"/"+MeasurementID+"/"+VideoFileName);
			        VDIS = new DataInputStream(VIS);
					VOS = new FileOutputStream(TempDataBaseFolder+"/"+TempMeasurementID+"/"+VideoFileName);
				}
				try {
					int PacketDescriptor = 0;
					byte[] PacketDescriptorBA = new byte[4]; 
					byte[] Packet = new byte[1*1024*1024];
					//.
					int AudioPacketTS,AudioPacketTSBase = -1;
					if (flAudio && (Measurement.AudioPackets > 0)) {
						for (int I = 0; I < Measurement.AudioPackets; I++) {
							PacketDescriptor = Integer.reverseBytes(ADIS.readInt());
							if (PacketDescriptor > 0) {
								if ((AudioStartPos <= I) && (I <= AudioFinishPos)) {
									ADIS.read(Packet, 0,PacketDescriptor);
									//.
									PacketDescriptorBA[0] = (byte)(PacketDescriptor & 0xff);
									PacketDescriptorBA[1] = (byte)(PacketDescriptor >> 8 & 0xff);
									PacketDescriptorBA[2] = (byte)(PacketDescriptor >> 16 & 0xff);
									PacketDescriptorBA[3] = (byte)(PacketDescriptor >>> 24);
									AOS.write(PacketDescriptorBA);
									//.
									AudioPacketTS = (Packet[4] << 24)+((Packet[5] & 0xFF) << 16)+((Packet[6] & 0xFF) << 8)+(Packet[7] & 0xFF);
									if (AudioPacketTSBase < 0)
										AudioPacketTSBase = AudioPacketTS;
									AudioPacketTS = AudioPacketTS-AudioPacketTSBase;
									Packet[7] = (byte)(AudioPacketTS & 0xff);
									Packet[6] = (byte)(AudioPacketTS >> 8 & 0xff);
									Packet[5] = (byte)(AudioPacketTS >> 16 & 0xff);
									Packet[4] = (byte)(AudioPacketTS >>> 24);
									AOS.write(Packet, 0,PacketDescriptor);
									//.
									AudioPacketCounter++;
									//.
									SummarySize += 4/*SizeOf(PacketDescriptor)*/+PacketDescriptor;
									if (SummarySize > MeasurementDataTransferableLimit)
										throw new MeasurementDataIsTooBigException(); //. => 
									//.
									if (I == AudioFinishPos)
										break; //. >
								}
								else {
									ADIS.skip(PacketDescriptor);
								}
								
							}
						}
					}
					//.
					int VideoPacketTS,VideoPacketTSBase = -1;
					if (flVideo && (Measurement.VideoPackets > 0)) {
						for (int I = 0; I < Measurement.VideoPackets; I++) {
							PacketDescriptor = Integer.reverseBytes(VDIS.readInt());
							if (PacketDescriptor > 0) {
								if ((VideoStartPos <= I) && (I <= VideoFinishPos)) {
									VDIS.read(Packet, 0,PacketDescriptor);
									//.
									PacketDescriptorBA[0] = (byte)(PacketDescriptor & 0xff);
									PacketDescriptorBA[1] = (byte)(PacketDescriptor >> 8 & 0xff);
									PacketDescriptorBA[2] = (byte)(PacketDescriptor >> 16 & 0xff);
									PacketDescriptorBA[3] = (byte)(PacketDescriptor >>> 24);
									VOS.write(PacketDescriptorBA);
									//.
									VideoPacketTS = (Packet[4] << 24)+((Packet[5] & 0xFF) << 16)+((Packet[6] & 0xFF) << 8)+(Packet[7] & 0xFF);
									if (VideoPacketTSBase < 0)
										VideoPacketTSBase = VideoPacketTS;
									VideoPacketTS = VideoPacketTS-VideoPacketTSBase;
									Packet[7] = (byte)(VideoPacketTS & 0xff);
									Packet[6] = (byte)(VideoPacketTS >> 8 & 0xff);
									Packet[5] = (byte)(VideoPacketTS >> 16 & 0xff);
									Packet[4] = (byte)(VideoPacketTS >>> 24);
									VOS.write(Packet, 0,PacketDescriptor);
									//.
									VideoPacketCounter++;
									//.
									SummarySize += 4/*SizeOf(PacketDescriptor)*/+PacketDescriptor;
									if (SummarySize > MeasurementDataTransferableLimit)
										throw new MeasurementDataIsTooBigException(); //. => 
									//.
									if (I == VideoFinishPos)
										break; //. >
								}
								else {
									VDIS.skip(PacketDescriptor);
								}
							}
						}
					}
				}
				finally {
					if (VOS != null)
						VOS.close();
					if (VDIS != null)
						VDIS.close();
					if (VIS != null)
						VIS.close();
				}
			}
			finally {
				if (AOS != null) 
					AOS.close();
				if (ADIS != null)
					ADIS.close();
				if (AIS != null)
					AIS.close();
			}
			//.
			TMeasurementDescriptor TempMeasurementDescriptor = GetMeasurementDescriptor(TempDataBaseFolder, TempMeasurementID);
			if (TempMeasurementDescriptor == null)
				throw new Exception("temp measurement descriptor is not found, ID:"+MeasurementID); //. =>
			//.
			TempMeasurementDescriptor.ID = TimestampToMeasurementID(StartTimestamp);
			TempMeasurementDescriptor.StartTimestamp = StartTimestamp;
			TempMeasurementDescriptor.FinishTimestamp = FinishTimestamp;
			TempMeasurementDescriptor.AudioPackets = AudioPacketCounter;
			TempMeasurementDescriptor.VideoPackets = VideoPacketCounter;
			//.
			SetMeasurementDescriptor(TempDataBaseFolder, TempMeasurementID, TempMeasurementDescriptor);
			//.
			if (GetMeasurementSize(TempDataBaseFolder, TempMeasurementID, flDescriptor,flAudio,flVideo) > MeasurementDataTransferableLimit)
				throw new MeasurementDataIsTooBigException(); //. => 
			//.
			return GetMeasurementData(TempDataBaseFolder, TempMeasurementID, flDescriptor,flAudio,flVideo);
		}
		finally {
			DeleteMeasurement(TempDataBaseFolder, TempMeasurementID);
		}
	}
	
	public static synchronized void ValidateMeasurements(String DataBaseFolder) throws Exception {
		File DF = new File(DataBaseFolder);
		if (!DF.exists())
			return; //. ->
		File[] MeasurementFolders = DF.listFiles();
		for (int I = 0; I < MeasurementFolders.length; I++)
			if (MeasurementFolders[I].isDirectory()) {
				String MeasurementID = MeasurementFolders[I].getName();
				TMeasurementDescriptor MeasurementDescriptor = GetMeasurementDescriptor(MeasurementID);
				if ((MeasurementDescriptor != null) && MeasurementDescriptor.IsStarted() && !MeasurementDescriptor.IsFinished())
					ValidateMeasurement(DataBaseFolder,MeasurementID);
			}
	}
	
	public static synchronized void ValidateMeasurements() throws Exception {
		ValidateMeasurements(MyDataBaseFolder);
	}
	
	public static synchronized void ValidateMeasurement(String DataBaseFolder, String MeasurementID) throws Exception {
		File AudioFile = new File(DataBaseFolder+"/"+MeasurementID+"/"+AudioFileName);
		long AudioFileLastModifiedTimestamp = 0;
		int AudioPacketCounter = 0;
		if (AudioFile.exists()) {
			AudioFileLastModifiedTimestamp = AudioFile.lastModified();
			//.
			FileInputStream AIS = new FileInputStream(MyDataBaseFolder+"/"+MeasurementID+"/"+AudioFileName);
			DataInputStream ADIS = new DataInputStream(AIS);
	        try {
	        	while (ADIS.available() > 4/*SizeOf(Descriptor)*/) {
					int  PacketDescriptor = Integer.reverseBytes(ADIS.readInt());
					if (PacketDescriptor > 0) {
						if (ADIS.available() < PacketDescriptor)
							break; //. >
						ADIS.skip(PacketDescriptor);
					}	        		
					AudioPacketCounter++;
	        	}
	        }
	        finally {
				ADIS.close();
				AIS.close();
	        }
		}
		File VideoFile = new File(DataBaseFolder+"/"+MeasurementID+"/"+VideoFileName);
		long VideoFileLastModifiedTimestamp = 0;
		int VideoPacketCounter = 0;
		if (VideoFile.exists()) {
			VideoFileLastModifiedTimestamp = VideoFile.lastModified();
			//.
			FileInputStream VIS = new FileInputStream(MyDataBaseFolder+"/"+MeasurementID+"/"+VideoFileName);
			DataInputStream VDIS = new DataInputStream(VIS);
	        try {
	        	while (VDIS.available() > 4/*SizeOf(Descriptor)*/) {
					int  PacketDescriptor = Integer.reverseBytes(VDIS.readInt());
					if (PacketDescriptor > 0) {
						if (VDIS.available() < PacketDescriptor)
							break; //. >
						VDIS.skip(PacketDescriptor);
					}	        		
					VideoPacketCounter++;
	        	}
	        }
	        finally {
				VDIS.close();
				VIS.close();
	        }
		}
		//.
		if ((AudioPacketCounter > 0) || (VideoPacketCounter > 0)) {
			long FT = AudioFileLastModifiedTimestamp;
			if (VideoFileLastModifiedTimestamp > FT)
				FT = VideoFileLastModifiedTimestamp;
			OleDate TS = new OleDate();
			TS.SetTimeStamp(FT);
			double FinishTimeStamp = TS.toDouble();
			//.
			TMeasurementDescriptor MeasurementDescriptor = GetMeasurementDescriptor(MeasurementID);
			if (MeasurementDescriptor != null) {
				MeasurementDescriptor.AudioPackets = AudioPacketCounter;
				MeasurementDescriptor.VideoPackets = VideoPacketCounter;
				MeasurementDescriptor.FinishTimestamp = FinishTimeStamp;
				SetMeasurementDescriptor(MeasurementID,MeasurementDescriptor);
			}
		}
	}
	
	private static boolean RemoveFolder(File path) {
	    if( path.exists() ) {
		      File[] files = path.listFiles();
		      if (files == null) 
		          return true; //. ->
		      for(int i=0; i<files.length; i++) {
		         if(files[i].isDirectory()) {
		           RemoveFolder(files[i]);
		         }
		         else 
		           files[i].delete();
		      }
		    }
		    return (path.delete());
	}
	
	private static void CopyFile(File SrcFile, File DestFile) throws IOException {
		FileInputStream inStream = new FileInputStream(SrcFile);
		FileOutputStream outStream = new FileOutputStream(DestFile);
		try {
		    byte[] buffer = new byte[2048];
		    int length;
		    while ((length = inStream.read(buffer)) > 0)
		    	outStream.write(buffer, 0, length);
		}
		finally {
		    outStream.close();	
		    inStream.close();
		}
	}
}
