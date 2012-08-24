package com.geoscope.GeoLog.DEVICE.FileSystemModule;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class TFileSystem {

	public static synchronized String GetDirList(String Dir) {
		String Result = "";
		File DF = new File(Dir);
		if (!DF.exists())
			return Result; //. ->
		File[] Files = DF.listFiles();
		for (int I = 0; I < Files.length; I++) {
			String ItemStr;
			if (Files[I].isDirectory()) 
				ItemStr = Files[I].getName()+",0";
			else {
				long FileSize = (new File(Files[I].getAbsolutePath())).length();
				ItemStr = Files[I].getName()+",1,"+Long.toString(FileSize);
			}
			//.
			if (!Result.equals("")) 
				Result = Result+";"+ItemStr;
			else
				Result = ItemStr;
		}
		return Result;
	}

	public static synchronized void CreateDir(String NewDir) throws IOException {
		File ND = new File(NewDir);
		ND.mkdirs();
		ND.createNewFile();
	}
	
	public static synchronized boolean IsFileExist(String FileFullName) throws IOException {
		File file = new File(FileFullName);
		return file.exists();
	}
	
	public static synchronized long GetFileSize(String FileFullName) throws IOException {
		File file = new File(FileFullName);
		if (!file.exists())
			return 0; //. ->
		return file.length();
	}
	
	public static synchronized byte[] GetFileData(String FileFullName) throws IOException {
		final int BUFFER = 2048;
		File file = new File(FileFullName);
		if (!file.exists())
			return null; //. ->
		BufferedInputStream origin = null;       
		ByteArrayOutputStream dest = new ByteArrayOutputStream();
	    try {
	    	ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
	    	try {
		    	byte data[] = new byte[BUFFER];        
	    		String FileName = file.getName();
	    		FileInputStream fi = new FileInputStream(file);
	    		try {
		    		origin = new BufferedInputStream(fi, BUFFER);
		    		try {
		    			ZipEntry entry = new ZipEntry(FileName);         
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
	    	finally {
	    		out.close(); 	
	    	}
	    	return dest.toByteArray(); //. =>
	    }
	    finally {
	    	dest.close();	    	
	    }
	}	
	
	public static synchronized void SetFileData(String FileFullName, byte[] Data) throws IOException {
		File file = new File(FileFullName);
		File ParentDir = file.getParentFile();
		ParentDir.mkdirs();
		ByteArrayInputStream BIS = new ByteArrayInputStream(Data);
		try
		{
			ZipInputStream ZipStream = new ZipInputStream(BIS);		
			try 
			{
				ZipEntry theEntry;
				while ((theEntry = ZipStream.getNextEntry()) != null) 
				{
					String fileName = theEntry.getName();
					if (!fileName.equals("")) {
						FileOutputStream out = new FileOutputStream(ParentDir.getAbsoluteFile()+"/"+fileName);
						try {
							int size = 2048;
							byte[] _data = new byte[size];
							while (true) 
							{
								size = ZipStream.read(_data, 0,_data.length);
								if (size > 0) 
									out.write(_data, 0,size);
								else 
									break; //. >
							}
						}
						finally {
							out.close();
						}
					}
				};
			}
			finally 
			{
				ZipStream.close();
			};
		}
		finally
		{
			BIS.close();
		};
	}
	
	public static synchronized boolean DeleteFile(String FileFullName) throws IOException {
		File file = new File(FileFullName);
		if (file.exists())
			return file.delete();
		else
			return false;
	}
}
