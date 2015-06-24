package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerSaverPanel extends Activity {

	public static final int UpdateInterval = 1000*2; //. seconds
	
	
	private boolean flVisible = false;
	//.
	private TVideoRecorderModule.TServerSaver ServerSaver;
	//.
	private ProgressBar pbProcessProgress;
	//.
	private TextView tvProcessProgress;
	//.
	private Button btnStartTransfer;
	//.
	private ListView lvSchedulerPlan;
	//.
	private Timer Updater;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		TTracker Tracker = TTracker.GetTracker();
		if (Tracker == null) {
			finish();
			return; //. ->
		}
		ServerSaver = Tracker.GeoLog.VideoRecorderModule.GetServerSaver();
		if (ServerSaver == null) {
			finish();
			return; //. ->
		}
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.video_recorder_serversaver_panel);
        //.
        pbProcessProgress = (ProgressBar)findViewById(R.id.pbProcessProgress);
        //.
        tvProcessProgress = (TextView)findViewById(R.id.tvProcessProgress);
        //.
        btnStartTransfer = (Button)findViewById(R.id.btnStartTransfer);
        btnStartTransfer.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		StartTransfer();
        		//.
	            Toast.makeText(TVideoRecorderServerSaverPanel.this, R.string.SDataTransferHasBeenStarted, Toast.LENGTH_LONG).show();
            }
        });
        //.
        lvSchedulerPlan = (ListView)findViewById(R.id.lvSchedulerPlan);
        lvSchedulerPlan.setOnItemClickListener(new OnItemClickListener() {
        	
        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		try {
            		ListView lv = (ListView)arg0;
            		ServerSaver.Scheduler.Plan.Items.get(arg2).SetEnabled(lv.isItemChecked(arg2));
            	}
            	catch (Exception E) {
            		Toast.makeText(TVideoRecorderServerSaverPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
        	}
		});
        lvSchedulerPlan.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
		    	AlertDialog.Builder alert = new AlertDialog.Builder(TVideoRecorderServerSaverPanel.this);
		    	//.
		    	alert.setTitle("");
		    	alert.setMessage(R.string.SEnterTime);
		    	//.
		    	final EditText input = new EditText(TVideoRecorderServerSaverPanel.this);
		    	alert.setView(input);
		    	//.
		    	alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
		    		
		    		@Override
		        	public void onClick(DialogInterface dialog, int whichButton) {
		    			//. hide keyboard
		        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
		        		//.
		        		try {
			        		String TimeStr = input.getText().toString();
			        		String[] SA = TimeStr.split(":");
			        		if (SA.length != 2)
			        			throw new Exception("incorrect time format"); //. ->
			        		int Hours = Integer.parseInt(SA[0]);
			        		int Mins = Integer.parseInt(SA[1]);
			        		double DayTime = Hours/24.0+Mins/(24.0*60.0);
		            		ServerSaver.Scheduler.Plan.Items.get(arg2).SetDayTime(DayTime);
		            		//.
		            		UpdateScheduler();
		        		}
		            	catch (Exception E) {
		            		Toast.makeText(TVideoRecorderServerSaverPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		            	}
		        			
		          	}
		    	});
		    	//.
		    	alert.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
		    		
		    		@Override
		    		public void onClick(DialogInterface dialog, int whichButton) {
		    			//. hide keyboard
		        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
		    		}
		    	});
		    	//.
		    	alert.show();   
            	//.
            	return true; 
			}
		}); 
        //.
        Updater = new Timer();
        Updater.schedule(new TUpdaterTask(),0,UpdateInterval);
        //.
        UpdateScheduler();
    }
	
    @Override
    protected void onDestroy() {
        if (Updater != null) {
        	Updater.cancel();
        	Updater = null;
        }
    	//.
    	super.onDestroy();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	//.
    	flVisible = true;
    }
    
    @Override
    protected void onPause() {
    	flVisible = false;
    	//.
    	super.onPause();
    }
    
	private void Update() {
		int ProcessCounter = ServerSaver.MeasurementsCount();
		if (ProcessCounter > 0)
			pbProcessProgress.setProgress((int)ServerSaver.ProcessProgressPercentage());
		else
			pbProcessProgress.setProgress(0);
		tvProcessProgress.setText(getString(R.string.SItemsToTransfer)+Integer.toString(ProcessCounter));
	}
	
	private void UpdateScheduler() {
		int Cnt = ServerSaver.Scheduler.Plan.Items.size();
		String[] lvItems = new String[Cnt];
		for (int I = 0; I < Cnt; I++) {
			TVideoRecorderModule.TServerSaver.TScheduler.TDailyPlan.TItem Item = ServerSaver.Scheduler.Plan.Items.get(I); 
			int Hours = (int)(Item.DayTime*24.0);
			int Mins = (int)((Item.DayTime*24.0-Hours)*60.0);
			String HoursStr = Integer.toString(Hours);
			String MinsStr = Integer.toString(Mins);
			if (MinsStr.length() == 1)
				MinsStr = "0"+MinsStr;
			//.
			lvItems[I] = getString(R.string.STime)+Integer.toString(I+1)+" -  "+HoursStr+":"+MinsStr;
		}
		ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvItems);             
		lvSchedulerPlan.setAdapter(lvAdapter);
		for (int I = 0; I < Cnt; I++) {
			TVideoRecorderModule.TServerSaver.TScheduler.TDailyPlan.TItem Item = ServerSaver.Scheduler.Plan.Items.get(I); 
			lvSchedulerPlan.setItemChecked(I, Item.flEnabled);
		}
	}
	
	private void StartTransfer() {
		TTracker Tracker = TTracker.GetTracker();
		if (Tracker == null)
			return; //. ->
		TVideoRecorderModule.TServerSaver ServerSaver = Tracker.GeoLog.VideoRecorderModule.GetServerSaver();
		if (ServerSaver == null)
			return; //. ->
		//.
		ServerSaver.StartProcess();
	}
	
	private static final int MESSAGE_UPDATE = 1; 
	
    private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_UPDATE:
            		if (flVisible)
            			Update();  
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };

    private class TUpdaterTask extends TimerTask {

        public void run() {
        	try {
            	MessageHandler.obtainMessage(MESSAGE_UPDATE).sendToTarget();
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    }
}
