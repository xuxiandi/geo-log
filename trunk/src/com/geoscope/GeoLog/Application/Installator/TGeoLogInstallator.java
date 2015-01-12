package com.geoscope.GeoLog.Application.Installator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

import com.geoscope.GeoLog.Application.TAssets;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TGeoLogInstallator {

	public static String 		ProgramFolderName = "Geo.Log";
	public static final String 	ProgramInstallationPath = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static String 		ProgramFolder = ProgramInstallationPath+"/"+ProgramFolderName;

	public static boolean ApplicationIsInstalled() {
		File PF = new File(ProgramFolder);
		return PF.exists();
	}
	
	public static void CheckInstallation(Context context) throws IOException {
		if (!ApplicationIsInstalled()) {
			CopyFileOrDir(context,ProgramFolderName);
			return; //. ->
		}
		//.
		Help_CheckInstallation(context);
		Resources_CheckInstallation(context);
	}
	
	public static boolean InstallationIsUpToDate(Context context) throws IOException {
		return (ApplicationIsInstalled() && Help_InstallationIsUpToDate(context) && Resources_InstallationIsUpToDate(context));
	}
	
	public static boolean Help_InstallationIsUpToDate(Context context) throws IOException {
		int InstalledVersion = 0;
		File IVF = new File(TGeoLogApplication.Help_Folder+"/"+TGeoLogApplication.Help_VersionFileName);
		if (IVF.exists()) {
			try {
				FileInputStream FIS = new FileInputStream(IVF);
				try {
					InputStreamReader ISR = new InputStreamReader(FIS);
					try {
						BufferedReader BR = new BufferedReader(ISR);
						try {
							InstalledVersion = Integer.parseInt(BR.readLine());
						}
						finally {
							BR.close();
						}
					}
					finally {
						ISR.close();
					}
				}
				finally {
					FIS.close();
				}
			}
			catch (Exception E) {}
		}
		int InstallatorVersion = 0;
		String IVFN = TGeoLogApplication.Help_Path+"/"+TGeoLogApplication.Help_VersionFileName;
		try {
			InputStream IS = context.getAssets().open(IVFN);
			try {
				InputStreamReader ISR = new InputStreamReader(IS);
				try {
					BufferedReader BR = new BufferedReader(ISR);
					try {
						InstallatorVersion = Integer.parseInt(BR.readLine());
					}
					finally {
						BR.close();
					}
				}
				finally {
					ISR.close();
				}
			}
			finally {
				IS.close();
			}
		}
		catch (Exception E) {}
		return (InstalledVersion >= InstallatorVersion);
	}
	
	public static void Help_CheckInstallation(Context context) throws IOException {
		if (!Help_InstallationIsUpToDate(context))
			TAssets.CopyFileOrFolder(context, TGeoLogApplication.Help_Path, TGeoLogApplication.ApplicationBasePath);
	}
	
	public static boolean Resources_InstallationIsUpToDate(Context context) throws IOException {
		int InstalledVersion = 0;
		File IVF = new File(TGeoLogApplication.Resources_Folder+"/"+TGeoLogApplication.Resources_VersionFileName);
		if (IVF.exists()) {
			try {
				FileInputStream FIS = new FileInputStream(IVF);
				try {
					InputStreamReader ISR = new InputStreamReader(FIS);
					try {
						BufferedReader BR = new BufferedReader(ISR);
						try {
							InstalledVersion = Integer.parseInt(BR.readLine());
						}
						finally {
							BR.close();
						}
					}
					finally {
						ISR.close();
					}
				}
				finally {
					FIS.close();
				}
			}
			catch (Exception E) {}
		}
		int InstallatorVersion = 0;
		String IVFN = TGeoLogApplication.Resources_Path+"/"+TGeoLogApplication.Resources_VersionFileName;
		try {
			InputStream IS = context.getAssets().open(IVFN);
			try {
				InputStreamReader ISR = new InputStreamReader(IS);
				try {
					BufferedReader BR = new BufferedReader(ISR);
					try {
						InstallatorVersion = Integer.parseInt(BR.readLine());
					}
					finally {
						BR.close();
					}
				}
				finally {
					ISR.close();
				}
			}
			finally {
				IS.close();
			}
		}
		catch (Exception E) {}
		return (InstalledVersion >= InstallatorVersion);
	}
	
	public static void Resources_CheckInstallation(Context context) throws IOException {
		if (!Resources_InstallationIsUpToDate(context))
			TAssets.CopyFileOrFolder(context, TGeoLogApplication.Resources_Path, TGeoLogApplication.ApplicationBasePath);
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
