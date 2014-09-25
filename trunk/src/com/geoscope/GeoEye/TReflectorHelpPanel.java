package com.geoscope.GeoEye;

import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.webkit.WebView;

import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TReflectorHelpPanel extends Activity {

	private WebView wvContent;
	
    public void onCreate(Bundle savedInstanceState) {
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
    	return "file:///"+TGeoLogApplication.HelpFolder+"/"+Language+"/"+TGeoLogApplication.HelpFileName;
    }
}