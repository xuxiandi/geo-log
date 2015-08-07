package com.geoscope.GeoEye;

import java.io.FileOutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjects;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TReflectorCoGeoMonitorObjectsReportsPanel extends Activity {

	private static final String ReportFileName = "Report.html";
	
	private EditText	edReportDomains;
	private Button 		btnDoDomainsReport;
	private WebView wvReport;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//.
        setContentView(R.layout.reflector_gmos_reports_panel);
        //.
        edReportDomains = (EditText)findViewById(R.id.edReportDomains);
        edReportDomains.setOnEditorActionListener(new OnEditorActionListener() {        
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if(arg1 == EditorInfo.IME_ACTION_DONE){
                	btnDoDomainsReport.callOnClick();                
                }
				return false;
			}
        });        
        //.
        btnDoDomainsReport = (Button)findViewById(R.id.btnDoDomainsReport);
        btnDoDomainsReport.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	try {
					DoDomainsReport(edReportDomains.getText().toString(),"RU");
				} catch (Exception E) {
					Toast.makeText(TReflectorCoGeoMonitorObjectsReportsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        wvReport = (WebView)findViewById(R.id.wvReport);
    }

    private void DoDomainsReport(final String ReportDomains, final String ReportParams) throws Exception {
		TAsyncProcessing Processing = new TAsyncProcessing(TReflectorCoGeoMonitorObjectsReportsPanel.this,getString(R.string.SWaitAMoment)) {
			
			private String ReportURL;
			@Override
			public void Process() throws Exception {
		    	TUserAgent UserAgent = TUserAgent.GetUserAgent(context.getApplicationContext());
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
		    	byte[] Report = TCoGeoMonitorObjects.GetReportForDomains(UserAgent.Server, ReportDomains, ReportParams);
		    	//.
		    	String ReportFile = TGeoLogApplication.GetTempFolder()+"/"+ReportFileName;
		    	FileOutputStream FOS = new FileOutputStream(ReportFile);
		    	try {
		    		FOS.write(Report);
		    	}
		    	finally {
		    		FOS.close();
		    	}
		    	//.
		    	ReportURL = "file:///"+ReportFile;
				//.
	    		Thread.sleep(100); 
			}
			@Override 
			public void DoOnCompleted() throws Exception {
		        wvReport.loadUrl(ReportURL);        
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TReflectorCoGeoMonitorObjectsReportsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }
}