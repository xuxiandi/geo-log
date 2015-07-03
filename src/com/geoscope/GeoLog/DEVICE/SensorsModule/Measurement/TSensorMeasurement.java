package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.geoscope.Classes.Data.Stream.Channel.TChannelProvider;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;

public class TSensorMeasurement {
	
	public static final String DataFileFormat = SpaceDefines.TYPEDDATAFILE_TYPE_Measurement_FORMAT_SMR;
	
	private static Object LastIDLock = new Object();
	private static String LastID = "";
	//.
	public static String GetNewID() throws InterruptedException {
		while (true) {
			String NewID = Double.toString(OleDate.UTCCurrentTimestamp());
			synchronized (LastIDLock) {
				if (!NewID.equals(LastID))
					return NewID; //. ->
				Thread.sleep(10);
			}
		}
	}
	
	public static TSensorMeasurement FromByteArray(byte[] BA, String pDatabaseFolder, String pDomain, String pMeasurementID, TChannelProvider ChannelProvider, TCanceller Canceller) throws Exception {
		String MeasurementFolder = pDatabaseFolder+"/"+pMeasurementID;
		File MF = new File(MeasurementFolder);
		MF.mkdirs();
		//.
		ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
		try {
			ZipInputStream ZipStream = new ZipInputStream(BIS);		
			try {
				ZipEntry theEntry;
				while ((theEntry = ZipStream.getNextEntry()) != null) {
					String fileName = theEntry.getName();
					if (!fileName.equals("")) {
						File F = new File(MeasurementFolder+"/"+fileName);
						FileOutputStream out = new FileOutputStream(F);
						try {
							int size = 65535;
							byte[] _data = new byte[size];
							while (true) {
								size = ZipStream.read(_data, 0,_data.length);
								if (size > 0) 
									out.write(_data, 0,size);
								else 
									break; //. >
								//.
								Canceller.Check();
							}
						}
						finally {
							out.close();
						}
					}
				}
			}
			finally {
				ZipStream.close();
			}
		}
		finally {
			BIS.close();
		}
		return (new TSensorMeasurement(pDatabaseFolder, pDomain, pMeasurementID, ChannelProvider));
	}
	
	public static TSensorMeasurement FromDataFile(String DataFile, String pDatabaseFolder, String pDomain, String pMeasurementID, TChannelProvider ChannelProvider, TCanceller Canceller) throws Exception {
		File F = new File(DataFile);
		if (!F.exists()) 
			return null; //. ->
		//.
    	FileInputStream FIS = new FileInputStream(F);
    	try {
    			byte[] BA = new byte[(int)F.length()];
    			FIS.read(BA);
    			//.
    			return FromByteArray(BA, pDatabaseFolder, pDomain, pMeasurementID, ChannelProvider, Canceller); //. ->    	
    	}
		finally
		{
			FIS.close(); 
		}
	}
	
	public static TSensorMeasurementDescriptor[] Filter(TSensorMeasurementDescriptor[] Descriptors, String TypeIDPrefix) {
		int Cnt = Descriptors.length;
		ArrayList<TSensorMeasurementDescriptor> _Result = new ArrayList<TSensorMeasurementDescriptor>(Cnt);
		for (int I = 0; I < Cnt; I++) 
			if ((Descriptors[I].Model != null) && Descriptors[I].Model.TypeID.startsWith(TypeIDPrefix))
				_Result.add(Descriptors[I]);
		Cnt = _Result.size();
		TSensorMeasurementDescriptor[] Result = new TSensorMeasurementDescriptor[Cnt];
		for (int I = 0; I < Cnt; I++) 
			Result[I] = _Result.get(I);
		return Result;
	}
	
	
	protected long GeographServerObjectID = 0;
	//.
	public String DatabaseFolder;
	//.
	public String Domain;
	//.
	public TSensorMeasurementDescriptor Descriptor;
	
	public TSensorMeasurement(long pGeographServerObjectID, String pDatabaseFolder, String pDomain, String pMeasurementID, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		GeographServerObjectID = pGeographServerObjectID;
		//.
		DatabaseFolder = pDatabaseFolder;
		//.
		Domain = pDomain;
		//.
		Descriptor = GetMeasurementDescriptor(DatabaseFolder, Domain, pMeasurementID, DescriptorClass, ChannelProvider);
	}
	
	public TSensorMeasurement(String pDatabaseFolder, String pDomain, String pMeasurementID, TChannelProvider ChannelProvider) throws Exception {
		this(0, pDatabaseFolder, pDomain, pMeasurementID, TSensorMeasurementDescriptor.class, ChannelProvider);
	}
	
	public String Folder() {
		return (DatabaseFolder+"/"+Descriptor.ID);
	}
	
	public TSensorMeasurementDescriptor GetMeasurementDescriptor(String pDatabaseFolder, String pDomain, String pMeasurementID, Class<?> DescriptorClass, TChannelProvider ChannelProvider) throws Exception {
		TSensorMeasurementDescriptor _Descriptor = TSensorsModuleMeasurements.GetMeasurementDescriptor(pDatabaseFolder, pDomain, pMeasurementID, DescriptorClass, ChannelProvider);
		if (_Descriptor == null) {
			_Descriptor = (TSensorMeasurementDescriptor)DescriptorClass.newInstance();
			_Descriptor.ID = pMeasurementID; 
		}
		return _Descriptor;
	}

	public void Start() throws Exception {
		Descriptor.StartTimestamp = OleDate.UTCCurrentTimestamp();
		TSensorsModuleMeasurements.SetMeasurementDescriptor(DatabaseFolder, Domain, Descriptor.ID, Descriptor);
		//.
		Descriptor.Model.Start();
	}
	
	public void Finish() throws Exception {
		Descriptor.Model.Stop();
		//.
		Descriptor.FinishTimestamp = OleDate.UTCCurrentTimestamp();
		Descriptor.GeographServerObjectID = GeographServerObjectID;
		Descriptor.GUID = TUIDGenerator.GenerateWithTimestamp();
		//.
		TSensorsModuleMeasurements.SetMeasurementDescriptor(DatabaseFolder, Domain, Descriptor.ID, Descriptor);
	}
	
	public byte[] ToByteArray() throws IOException {
		ByteArrayOutputStream OutStream = new ByteArrayOutputStream();
	    try {
	    	BufferedOutputStream BufferedOutStream = new BufferedOutputStream(OutStream);
	    	try {
		    	ZipOutputStream ZipOutStream = new ZipOutputStream(BufferedOutStream);
		    	try {
		    		ZipOutStream.setLevel(Deflater.BEST_SPEED);
		    		//.
		    		int BUFFER = 65535;
			    	byte data[] = new byte[BUFFER];        
			    	File MF = new File(Folder());
					File[] Files = MF.listFiles();
					int Cnt = Files.length;
					for (int I = 0; I < Cnt; I++)
						if (!Files[I].isDirectory()) {
							File ContentFile = Files[I];
				    		FileInputStream InStream = new FileInputStream(ContentFile);
				    		try {
				    			BufferedInputStream BufferedInStream = new BufferedInputStream(InStream, BUFFER);
					    		try {
					    			ZipEntry entry = new ZipEntry(ContentFile.getName());         
					    			ZipOutStream.putNextEntry(entry);         
					    			int count;         
					    			while ((count = BufferedInStream.read(data, 0, BUFFER)) != -1) 
					    				ZipOutStream.write(data, 0, count);
					    		}
					    		finally {
					    			BufferedInStream.close();       
					    		}
				    		}
				    		finally {
				    			InStream.close();
				    		}
						}
		    	}
		    	finally {
		    		ZipOutStream.close(); 	
		    	}
	    	}
	    	finally {
	    		BufferedOutStream.close();
	    	}
	    	return OutStream.toByteArray(); //. =>
	    }
	    finally {
	    	OutStream.close();	    	
	    }
	}
}