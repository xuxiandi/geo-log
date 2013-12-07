package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivities;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TUserActivityListPanel extends Activity {

	public static final int REQUEST_SHOWONREFLECTOR = 1;
	
	private ListView lvUserActivityList;
	//.
	private int 		UserID = 0;	
    private TActivities UserActivities = null;
	//.
	private TUpdating	Updating = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) 
        	UserID = extras.getInt("UserID");
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_activitylist_panel);
        //.
        lvUserActivityList = (ListView)findViewById(R.id.lvUserActivityList);
        lvUserActivityList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvUserActivityList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (UserActivities == null)
					return; //. ->
            	Intent intent = new Intent(TUserActivityListPanel.this, TUserActivityComponentListPanel.class);
            	intent.putExtra("UserID",UserID);
            	intent.putExtra("ActivityID",UserActivities.Items[arg2].ID);
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
        	}              
        });
        //.
        setResult(RESULT_CANCELED);
        //.
        StartUpdating();
	}

	@Override
	protected void onDestroy() {
		if (Updating != null) {
			Updating.CancelAndWait();
			Updating = null;
		}
		//.
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_SHOWONREFLECTOR: 
        	if (resultCode == RESULT_OK) { 
                setResult(RESULT_OK);
                //.
        		finish();
        	}
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
	private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_COMPLETED = 0;
    	private static final int MESSAGE_FINISHED = 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
        private ProgressDialog progressDialog;
        //.
        private TActivities UserActivities = null;
    	
    	public TUpdating(boolean pflShowProgress, boolean pflClosePanelOnCancel) {
    		flShowProgress = pflShowProgress;
    		flClosePanelOnCancel = pflClosePanelOnCancel;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				try {
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    				//.
	    				UserActivities = UserAgent.Server.User.GetUserActivityList(UserID, TActivity.MinTimestamp,TActivity.MaxTimestamp);
					}
					finally {
						if (flShowProgress)
							MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
					}
    				//.
	    			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
	        	}
	        	catch (InterruptedException E) {
	        	}
	        	catch (IOException E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
	        	}
	        	catch (Throwable E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
	        	}
			}
			finally {
    			MessageHandler.obtainMessage(MESSAGE_FINISHED).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserActivityListPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	TUserActivityListPanel.this.UserActivities = UserActivities;
	           		 	//.
	           		 	TUserActivityListPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	TUserActivityListPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserActivityListPanel.this);    
		            	progressDialog.setMessage(TUserActivityListPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserActivityListPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserActivityListPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserActivityListPanel.this.finish();
		            		} 
		            	}); 
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		                if ((!isFinishing()) && progressDialog.isShowing()) 
		                	progressDialog.dismiss(); 
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	progressDialog.setProgress((Integer)msg.obj);
		            	//.
		            	break; //. >
		            }
	        	}
	        	catch (Exception E) {
	        	}
	        }
	    };
    }   
	
    private void Update() {
    	if (UserActivities == null) {
    		lvUserActivityList.setAdapter(null);
    		return; //. ->
    	}
		String[] lvItems = new String[UserActivities.Items.length];
		for (int I = 0; I < UserActivities.Items.length; I++) {
			TActivity Activity = UserActivities.Items[I]; 
			lvItems[I] = Activity.GetInfo(this);
		}
		ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvItems);             
		lvUserActivityList.setAdapter(lvAdapter);
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,true);
    }    
}
