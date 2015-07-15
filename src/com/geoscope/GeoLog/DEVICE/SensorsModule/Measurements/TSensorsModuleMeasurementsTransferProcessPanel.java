package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements;

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
import android.view.View.OnLongClickListener;
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
import com.geoscope.GeoLog.Application.THintManager;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TSensorsModuleMeasurementsTransferProcessPanel extends Activity {

	public static final int UpdateInterval = 1000*2; //. seconds
	
	
	private boolean flVisible = false;
	//.
	private TSensorsModuleMeasurementsTransferProcess TransferProcess;
	//.
	private ProgressBar pbProcessProgress;
	//.
	private TextView tvProcessProgress;
	//.
	private Button btnStartTransfer;
	//.
	private Button btnStopTransfer;
	//.
	private ListView lvSchedulerPlan;
	//.
	private Timer Updater;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		try {
			TTracker Tracker = TTracker.GetTracker();
			if (Tracker == null) 
				throw new Exception("Tracker is null"); //. =>
			TransferProcess = Tracker.GeoLog.SensorsModule.Measurements_GetTransferProcess();
			if (TransferProcess == null) 
				throw new Exception("TransferProcess is null"); //. =>
		} catch (Exception E) {
			finish();
			return; //. ->
		}
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.sensorsmodule_measurements_transferprocess_panel);
        //.
        pbProcessProgress = (ProgressBar)findViewById(R.id.pbProcessProgress);
        //.
        tvProcessProgress = (TextView)findViewById(R.id.tvProcessProgress);
        //.
        btnStartTransfer = (Button)findViewById(R.id.btnStartTransfer);
        btnStartTransfer.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		try {
            		StartTransfer();
            		//.
    	            Toast.makeText(TSensorsModuleMeasurementsTransferProcessPanel.this, R.string.SDataTransferHasBeenStarted, Toast.LENGTH_LONG).show();
            	}
            	catch (Exception E) {
            		Toast.makeText(TSensorsModuleMeasurementsTransferProcessPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
            }
        });
        //.
        btnStopTransfer = (Button)findViewById(R.id.btnStopTransfer);
        btnStopTransfer.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		try {
            		StopTransfer();
            		//.
    	            Toast.makeText(TSensorsModuleMeasurementsTransferProcessPanel.this, R.string.SDataTransferHasBeenStopped, Toast.LENGTH_LONG).show();
            	}
            	catch (Exception E) {
            		Toast.makeText(TSensorsModuleMeasurementsTransferProcessPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
            }
        });
        //.
        lvSchedulerPlan = (ListView)findViewById(R.id.lvSchedulerPlan);
        lvSchedulerPlan.setOnItemClickListener(new OnItemClickListener() {
        	
        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		try {
            		ListView lv = (ListView)arg0;
            		TransferProcess.Scheduler.Plan.Items.get(arg2).SetEnabled(lv.isItemChecked(arg2));
            	}
            	catch (Exception E) {
            		Toast.makeText(TSensorsModuleMeasurementsTransferProcessPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
            	}
        	}
		});
        lvSchedulerPlan.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
		    	AlertDialog.Builder alert = new AlertDialog.Builder(TSensorsModuleMeasurementsTransferProcessPanel.this);
		    	//.
		    	alert.setTitle("");
		    	alert.setMessage(R.string.SEnterTime);
		    	//.
		    	final EditText input = new EditText(TSensorsModuleMeasurementsTransferProcessPanel.this);
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
			        		if ((Hours < 0) || (Hours > 23))
			        			throw new Exception("wrong hour value"); //. ->
			        		int Mins = Integer.parseInt(SA[1]);
			        		if ((Mins < 0) || (Mins > 59))
			        			throw new Exception("wrong minute value"); //. ->
			        		double DayTime = Hours/24.0+Mins/(24.0*60.0);
		            		TransferProcess.Scheduler.Plan.Items.get(arg2).SetDayTime(DayTime);
		            		//.
		            		UpdateScheduler();
		        		}
		            	catch (Exception E) {
		            		Toast.makeText(TSensorsModuleMeasurementsTransferProcessPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
        final int HintID = THintManager.HINT__VideoRecorderServerSaverPanel;
        final TextView lbHint = (TextView)findViewById(R.id.lbHint);
        String Hint = THintManager.GetHint(HintID, this);
        if (Hint != null) {
        	lbHint.setText(Hint);
            lbHint.setOnLongClickListener(new OnLongClickListener() {
            	
    			@Override
    			public boolean onLongClick(View v) {
    				THintManager.SetHintAsDisabled(HintID);
    	        	lbHint.setVisibility(View.GONE);
    	        	//.
    				return true;
    			}
    		});
            //.
        	lbHint.setVisibility(View.VISIBLE);
        }
        else
        	lbHint.setVisibility(View.GONE);
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
		int DatabaseMeasurementsCount = TSensorsModuleMeasurements.GetMeasurementsCount();
		int ProcessCounter = TransferProcess.MeasurementsCount();
		//.
		if (ProcessCounter > 0)
			pbProcessProgress.setProgress((int)TransferProcess.ProcessProgressPercentage());
		else
			pbProcessProgress.setProgress(0);
		tvProcessProgress.setText(getString(R.string.SItemsToTransfer)+Integer.toString(ProcessCounter)+"  "+"["+getString(R.string.SSummaryInLocalDatabase)+Integer.toString(DatabaseMeasurementsCount)+"]");
		//.
		btnStartTransfer.setEnabled(DatabaseMeasurementsCount > 0);
		btnStopTransfer.setEnabled(ProcessCounter > 0);
	}
	
	private void UpdateScheduler() {
		int Cnt = TransferProcess.Scheduler.Plan.Items.size();
		String[] lvItems = new String[Cnt];
		for (int I = 0; I < Cnt; I++) {
			TSensorsModuleMeasurementsTransferProcess.TScheduler.TDailyPlan.TItem Item = TransferProcess.Scheduler.Plan.Items.get(I); 
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
			TSensorsModuleMeasurementsTransferProcess.TScheduler.TDailyPlan.TItem Item = TransferProcess.Scheduler.Plan.Items.get(I); 
			lvSchedulerPlan.setItemChecked(I, Item.flEnabled);
		}
	}
	
	private void StartTransfer() throws Exception {
		TransferProcess.StartProcess();
	}
	
	private void StopTransfer() throws Exception {
		TransferProcess.StopProcess();
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
