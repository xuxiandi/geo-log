package com.geoscope.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;

public class TAssets {

	public static void CopyFileOrFolder(Context context, String SrcPath, String DestFolder) throws IOException {
	    AssetManager assetManager = context.getAssets();
	    String assets[] = null;
        assets = assetManager.list(SrcPath);
        if (assets.length == 0) 
            CopyFile(context,SrcPath,DestFolder);
        else {
            String fullPath = DestFolder+"/"+SrcPath;
            File dir = new File(fullPath);
            if (!dir.exists())
                dir.mkdir();
            for (int i = 0; i < assets.length; ++i) 
                CopyFileOrFolder(context,SrcPath+"/"+ assets[i],DestFolder);
        }
	}

	private static void CopyFile(Context context, String filename, String DestFolder) throws IOException {
	    AssetManager assetManager = context.getAssets();
	    InputStream in = assetManager.open(filename);
        try {
            String newFileName = DestFolder+"/"+filename;
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
