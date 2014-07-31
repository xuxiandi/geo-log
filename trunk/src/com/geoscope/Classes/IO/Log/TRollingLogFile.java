package com.geoscope.Classes.IO.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class TRollingLogFile {
	
    public static final int Capacity = 100;
    public static final int SavingThreshold = 1;
    public static final boolean flLogWarningMessages = true;
    public static final boolean flLogInfoMessages = true;
    //.
    private File LogFile;
    private String[] 	Items;
    private int			Items_Position;
    private int			Items_UnsavedCount = 0;

    public TRollingLogFile(String LogFileName) throws IOException {
    	LogFile = new File(LogFileName);
    	Items = new String[Capacity];
    	Items_Position = 0;
    	Items_UnsavedCount = 0;
    	//.
    	Load();
    }
    
    public void Destroy() throws IOException {
    	if (Items_UnsavedCount > 0)
    		Save();
    }
    
    private synchronized void Load() throws IOException {
    	if (!LogFile.exists())
    		return; //. ->
    	FileReader FR = new FileReader(LogFile);
    	try {
        	BufferedReader BR = new BufferedReader(FR);
        	try {
        		Items_Position = 0;
        		String S;
        		while ((S = BR.readLine()) != null) {
        			Items[Items_Position] = S;
        			Items_Position++;
        			if (Items_Position >= Capacity) 
        				Items_Position = 0;
        		}
        	}
        	finally {
        		BR.close();
        	}
    	}
    	finally {
    		FR.close();
    	}
    	Items_UnsavedCount = 0;
    }
    
    public synchronized void Save() throws IOException {
    	if (!LogFile.exists()) { 
        	String LogFolder = LogFile.getParent(); File LF = new File(LogFolder); LF.mkdirs();
        	LogFile.createNewFile();
    	}
    	FileWriter FW = new FileWriter(LogFile, false);
    	try {
        	BufferedWriter BW = new BufferedWriter(FW);
        	try {
        		int Pos = Items_Position;
        		for (int I = 0; I < Capacity; I++) {
        			if (Items[Pos] != null) {
        				BW.append(Items[Pos]);
        				BW.newLine();
        			}
        			Pos++;
        			if (Pos >= Capacity) 
        				Pos = 0;
        		}
        	}
        	finally {
        		BW.close();
        	}
    	}
    	finally {
    		FW.close();
    	}
    	Items_UnsavedCount = 0;
    }

    public String ToString() { 
    	StringBuilder SB = new StringBuilder();
		int Pos = Items_Position;
		for (int I = 0; I < Capacity; I++) {
			if (Items[Pos] != null) 
				SB.append(Items[Pos]+"\n");
			Pos++;
			if (Pos >= Capacity) 
				Pos = 0;
		}
		return SB.toString();
    }
    
    public synchronized byte[] ToZippedByteArray() throws IOException {
    	Save();
    	//.
		if (!LogFile.exists())
			return null; //. ->
		BufferedInputStream origin = null;       
		ByteArrayOutputStream dest = new ByteArrayOutputStream();
	    try {
	    	ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
	    	try {
	    		int BUFFER = 2048;
		    	byte data[] = new byte[BUFFER];        
	    		String FileName = LogFile.getName();
	    		FileInputStream fi = new FileInputStream(LogFile);
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
    
    private void AddItem(String S) {
    	Items[Items_Position] = S;
    	Items_Position++;
    	if (Items_Position >= Capacity)
    		Items_Position = 0;
    	//.
    	DoOnItemAdded();    
    }

    private void DoOnItemAdded() {
    	Items_UnsavedCount++;
    	if (Items_UnsavedCount > SavingThreshold)
			try {
				Save();
			} catch (IOException E) {
			}
    }
    	
    public synchronized void WriteInfo(String Source, String Info) {
    	if (!flLogInfoMessages)
    		return; //. ->
    	String S = (new SimpleDateFormat("dd/MM/yy HH:mm:ss",Locale.US)).format(new Date())+" "+"INFO: "+Source+", "+Info;
    	AddItem(S);
    }
    
    public synchronized void WriteWarning(String Source, String Warning) {
    	if (!flLogWarningMessages)
    		return; //. ->
    	String S = (new SimpleDateFormat("dd/MM/yy HH:mm:ss",Locale.US)).format(new Date())+" "+"WARNINIG: "+Source+", "+Warning;
    	AddItem(S);
    }
    
    public synchronized void WriteError(String Source, String Error) {
    	String S = (new SimpleDateFormat("dd/MM/yy HH:mm:ss",Locale.US)).format(new Date())+" "+"! ERROR: "+Source+", "+Error;
    	AddItem(S);
    }
    
    public synchronized void WriteError(String Source, String Error, StackTraceElement[] StackTrace) {
    	String S = (new SimpleDateFormat("dd/MM/yy HH:mm:ss",Locale.US)).format(new Date())+" "+"! ERROR: "+Source+", "+Error;
    	AddItem(S);
    	if (StackTrace != null)
        	for (int I = 0; I < StackTrace.length; I++) {
        		StackTraceElement Element = StackTrace[I];
        		S = "  "+Element.getClassName()+"."+Element.getMethodName()+" line: "+Integer.toString(Element.getLineNumber());
            	AddItem(S);
        	}
    }
}