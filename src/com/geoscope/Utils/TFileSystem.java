package com.geoscope.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.os.Environment;

public class TFileSystem {

	public static class TExternalStorage {
		
		public static final int MountingDefaultTimeout = 1000*30; //. seconds
		
		public static void WaitForMounted(int Timeout) throws Exception {
			int SleepTime = 100;
			int TimeCounter = (Timeout/SleepTime);
			for (int I = 0; I < TimeCounter; I++) {
				String State = Environment.getExternalStorageState();
				if (State.equals(Environment.MEDIA_MOUNTED)) 
					return; //. ->
			    Thread.sleep(SleepTime);
			}
			throw new Exception("external storage mounting timeout"); //. =>
		}
		
		public static void WaitForMounted() throws Exception {
			WaitForMounted(MountingDefaultTimeout);
		}
	}

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
	
	public static void CopyFolder(File src, File dest) throws IOException {
		if (src.isDirectory()) {
			if (!dest.exists()) 
				dest.mkdirs();
			//.
			String[] files = src.list();
			for (String file : files) {
				File srcFile = new File(src, file);
				File destFile = new File(dest, file);
				//.
				CopyFolder(srcFile,destFile);
			}
		}
		else {
			InputStream in = new FileInputStream(src);
			try {
		        OutputStream out = new FileOutputStream(dest);
		        try {
			        byte[] buffer = new byte[8192];
			        int length;
			        while ((length = in.read(buffer)) > 0)
			    	   out.write(buffer, 0, length);
		        }
		        finally {
		        	out.close();
		        }
			}
			finally {
				in.close();
			}
		}
	}
}
