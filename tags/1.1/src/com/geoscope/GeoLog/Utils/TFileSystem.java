package com.geoscope.GeoLog.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class TFileSystem {

	public static boolean RemoveFolder(File path) {
	    if (path.exists()) {
		      File[] files = path.listFiles();
		      if (files == null) 
		          return true; //. ->
		      for(int i = 0; i < files.length; i++) 
		         if(files[i].isDirectory()) 
		           RemoveFolder(files[i]);
		         else 
		           files[i].delete();
		    }
		    return (path.delete());
	}
	
	public static void EmptyFolder(File path) {
	    if (path.exists()) {
		      File[] files = path.listFiles();
		      if (files == null) 
		          return; //. ->
		      for(int i = 0; i < files.length; i++) 
		    	  if(files[i].isDirectory()) 
		    		  RemoveFolder(files[i]);
		    	  else 
		    		  files[i].delete();
	    }
	}
	
	public static void CopyFile(File SrcFile, File DestFile) throws IOException {
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
