package com.geoscope.GeoEye;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

import com.geoscope.GeoLog.Application.TAssets;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TReflectorHelpPanel extends Activity {

	public static void CheckHelpFolder(Context context) throws IOException {
		int InstalledVersion = 0;
		File IVF = new File(TReflector.HelpFolder+"/"+TReflector.HelpVersionFileName);
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
		String IVFN = TReflector.HelpPath+"/"+TReflector.HelpVersionFileName;
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
		if (InstalledVersion < InstallatorVersion)
			TAssets.CopyFileOrFolder(context, TReflector.HelpPath, TGeoLogApplication.ApplicationBasePath);
	}
	
	private WebView wvContent;
	
    public void onCreate(Bundle savedInstanceState) {
    	try {
			CheckHelpFolder(this);
		} catch (IOException E) {}
		//.
        super.onCreate(savedInstanceState);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.reflector_help_panel);
        //.
        wvContent = (WebView) findViewById(R.id.wvHelpContent);
        //.
        wvContent.loadUrl(GetHelpURL());        
    }

    private String GetHelpURL() {
    	String Language = Locale.getDefault().getLanguage();
    	return "file:///"+TReflector.HelpFolder+"/"+Language+"/"+TReflector.HelpFileName;
    }
}