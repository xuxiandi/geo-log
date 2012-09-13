package com.geoscope.GeoLog.Installator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class TGeoLogInstallator {

	public static String 		ProgramFolderName = "Geo.Log";
	public static final String 	ProgramInstallationPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String 		ProgramFolder = ProgramInstallationPath+"/"+ProgramFolderName;
	
	public static void CheckInstallation(Context context) throws IOException {
		File PF = new File(ProgramFolder);
		if (PF.exists())
			return; //. ->
		CopyFileOrDir(context,ProgramFolderName);
	}
	
	private static void CopyFileOrDir(Context context, String path) throws IOException {
	    AssetManager assetManager = context.getAssets();
	    String assets[] = null;
        assets = assetManager.list(path);
        if (assets.length == 0) 
            CopyFile(context,path);
        else {
            String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+path;
            File dir = new File(fullPath);
            if (!dir.exists())
                dir.mkdir();
            for (int i = 0; i < assets.length; ++i) 
                CopyFileOrDir(context,path+"/"+ assets[i]);
        }
	}

	private static void CopyFile(Context context, String filename) throws IOException {
	    AssetManager assetManager = context.getAssets();
	    InputStream in = assetManager.open(filename);
        try {
            String newFileName = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+filename;
            OutputStream out = new FileOutputStream(newFileName);
            try {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) 
                    out.write(buffer, 0, read);
                out.flush();
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
