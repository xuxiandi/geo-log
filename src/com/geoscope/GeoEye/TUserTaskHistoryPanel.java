package com.geoscope.GeoEye;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

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
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivities;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskDataValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue.TStatusDescriptor;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue.TStatusDescriptors;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.OleDate;

@SuppressLint("HandlerLeak")
public class TUserTaskHistoryPanel extends Activity {

	private static final int REQUEST_SHOWONREFLECTOR = 1;
	
	private static class THistoryItem {
		
		public double Timestamp = 0.0;
		public String Text = "";
	}
	
	private static class TStatusHistoryItem extends THistoryItem {

		public TStatusHistoryItem(double pTimestamp, String pText) {
			Timestamp = pTimestamp;
			Text = pText;
		}
	}
	
	private static class TActivityHistoryItem extends THistoryItem {

		public TActivity Activity;
		
		public TActivityHistoryItem(double pTimestamp, String pText, TActivity pActivity) {
			Timestamp = pTimestamp;
			Text = pText;
			Activity = pActivity;
		}
	}
	
	private static class THistoryItems {
		
		public ArrayList<THistoryItem> Items = new ArrayList<THistoryItem>();
		
		public THistoryItems() {
		}
		
		public void Add(THistoryItem Item) {
			Items.add(Item);
		}
		
		private class TTimestampComparator implements Comparator<THistoryItem> {
			@Override
		    public int compare(THistoryItem A, THistoryItem B) {
		        return Double.compare(A.Timestamp,B.Timestamp);
		    }
		}		
		
		public void SortByTime() {
			Collections.sort(Items, new TTimestampComparator());
		}
		
		public void Clear() {
			Items.clear();
		}
	}
	
	public boolean flExists = false;
	//. 
	@SuppressWarnings("unused")
	private TextView lbUserTaskHistoryList;
	private ListView lvUserTaskHistoryList;
	//.
	private TComponentServiceOperation ServiceOperation = null;
	//.
	private int UserID = 0;
	private int TaskID = 0;	
    private THistoryItems HistoryItems = new THistoryItems();
    //.
    private ProgressDialog progressDialog = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras();
        //.
        if (extras != null) {
        	UserID = extras.getInt("UserID");
        	//.
        	TaskID = extras.getInt("TaskID");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_taskhistory_panel);
        //.
        lbUserTaskHistoryList = (TextView)findViewById(R.id.lbUserTaskHistoryList);
        lvUserTaskHistoryList = (ListView)findViewById(R.id.lvUserTaskHistoryList);
        lvUserTaskHistoryList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvUserTaskHistoryList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (HistoryItems == null)
					return; //. ->
				THistoryItem HistoryItem = HistoryItems.Items.get(arg2);
				if (HistoryItem instanceof TActivityHistoryItem) {
					TActivityHistoryItem ActivityHistoryItem = (TActivityHistoryItem)HistoryItem;
					//.
	            	Intent intent = new Intent(TUserTaskHistoryPanel.this, TUserActivityComponentListPanel.class);
	            	intent.putExtra("UserID",ActivityHistoryItem.Activity.idUser);
	            	intent.putExtra("ActivityID",ActivityHistoryItem.Activity.ID);
	            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
				}
        	}              
        });
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
        //.
		try {
			Task_GetStatusHistory();
		}
		catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		flExists = false;
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
    
	private void ServiceOperation_Cancel() {
		if (ServiceOperation != null) {
			ServiceOperation.Cancel();
			ServiceOperation = null;
		}
	}
	
    private void Task_GetStatusHistory() throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.GetTaskStatusHistory(UserID, TaskID, new TTaskStatusValue.TStatusHistoryIsReceivedHandler() {
    		@Override
    		public void DoOnStatusHistoryIsReceived(TStatusDescriptors History) {
    			Task_StatusHistoryIsReceived(History);
    		}
    	}, new TTaskStatusValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Task_DoOnException(E);
    		}
    	});
    	//.
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Task_StatusHistoryIsReceived(TStatusDescriptors History) {
		//. MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_ONSTATUSHISTORY,History).sendToTarget();
    }
        
    private void Task_GetActivities() throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	ServiceOperation_Cancel();
    	ServiceOperation = Tracker.GeoLog.TaskModule.GetTaskActivities(UserID, TaskID, new TTaskDataValue.TTaskActivitiesAreReceivedHandler() {
    		@Override
    		public void DoOnTaskActivitiesAreReceived(TActivities Activities) {
    			Task_OnActivitiesAreReceived(Activities);
    		}
    	}, new TTaskDataValue.TExceptionHandler() {
    		@Override
    		public void DoOnException(Exception E) {
    			Task_DoOnException(E);
    		}
    	});
    	//.
		//. MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    }
    
    private void Task_OnActivitiesAreReceived(TActivities Activities) {
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_ONACTIVITYHISTORY,Activities).sendToTarget();
    }
        
    private void Task_DoOnException(Exception E){
		MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
		MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
    }
        
    private void Update() {
    	if (HistoryItems == null) {
    		lvUserTaskHistoryList.setAdapter(null);
    		return; //. ->
    	}
		String[] lvItems = new String[HistoryItems.Items.size()];
		for (int I = 0; I < HistoryItems.Items.size(); I++) {
			THistoryItem HistoryItem = HistoryItems.Items.get(I); 
			lvItems[I] = (new SimpleDateFormat("dd/MM/yyyy HH:mm:ss",Locale.US)).format((new OleDate(HistoryItem.Timestamp)).GetDateTime())+": "+HistoryItem.Text;
		}
		ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvItems);             
		lvUserTaskHistoryList.setAdapter(lvAdapter);
    }

	private static final int MESSAGE_EXCEPTION 				= -1;
	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 1;
	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 2;
	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 3;
	private static final int MESSAGE_ONSTATUSHISTORY 		= 4;
	private static final int MESSAGE_ONACTIVITYHISTORY 		= 5;

	private final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
					if (!flExists)
		            	break; //. >
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TUserTaskHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_ONSTATUSHISTORY:
					if (!flExists)
		            	break; //. >
					TStatusDescriptors StatusHistory = (TStatusDescriptors)msg.obj;
					HistoryItems.Clear();
					//.
					for (int I = 0; I < StatusHistory.Items.length; I++) {
						TStatusDescriptor Status = StatusHistory.Items[I]; 
						String Text = getString(R.string.SStatusIsChangedTo)+" "+TTaskStatusValue.Status_String(Status.Status,TUserTaskHistoryPanel.this);
						if (Status.Reason > 0)
							Text += ", "+getString(R.string.SReason)+Integer.toString(Status.Reason);
						if (!Status.Comment.equals(""))
							Text += ", "+Status.Comment;
						//.
						HistoryItems.Add(new TStatusHistoryItem(Status.Timestamp,Text));
					}
					//.
					try {
						Task_GetActivities();
					}
					catch (Exception Ex) {
						Task_DoOnException(Ex);
					}
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_ONACTIVITYHISTORY:
					if (!flExists)
		            	break; //. >
					TActivities TaskActivities = (TActivities)msg.obj;
					for (int I = 0; I < TaskActivities.Items.length; I++) {
						TActivity Activity = TaskActivities.Items[I];
						String AN = Activity.Name;
						if (Activity.Info != null)
							AN += " /"+Activity.Info+"/";
						String Text = getString(R.string.SActivityIsAttached)+AN;
						//.
						HistoryItems.Add(new TActivityHistoryItem(Activity.FinishTimestamp,Text, Activity));
					}
					//.
					HistoryItems.SortByTime();
					//.
			    	Update();    	
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TUserTaskHistoryPanel.this);    
	            	progressDialog.setMessage(TUserTaskHistoryPanel.this.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
	        			@Override
	        			public void onCancel(DialogInterface arg0) {
	        				TUserTaskHistoryPanel.this.finish();
	        			}
	        		});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserTaskHistoryPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
	            		@Override 
	            		public void onClick(DialogInterface dialog, int which) { 
	        				TUserTaskHistoryPanel.this.finish();
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
