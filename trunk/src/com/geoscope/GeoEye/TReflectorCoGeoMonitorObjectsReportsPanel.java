package com.geoscope.GeoEye;

import java.io.FileOutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;

public class TReflectorCoGeoMonitorObjectsReportsPanel extends Activity {

	private static final String ReportFileName = "Report.html";
	
	private EditText	edReportDomains;
	private Button 		btnDoDomainsReport;
	private WebView wvReport;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.reflector_gmos_reports_panel);
        //.
        edReportDomains = (EditText)findViewById(R.id.edReportDomains);
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
		    	TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
		    	byte[] Report = TReflectorCoGeoMonitorObjects.GetReportForDomains(UserAgent.Server, ReportDomains, ReportParams);
		    	//.
		    	String ReportFile = TReflector.GetTempFolder()+"/"+ReportFileName;
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