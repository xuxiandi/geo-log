package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TReflectorCoGeoMonitorObjectsPanel extends Activity  {

	public static final int REQUEST_ADDNEWOBJECT 	= 1;
	public static final int REQUEST_SELECT_USER 	= 2;
	
	private TReflectorCoGeoMonitorObjects CoGeoMonitorObjects;
	
	private Button btnNewObject;
	private Button btnRemoveInactiveObjects;
	private Button btnSendSelectedObjectsToUser;
	private Button btnClose;
	private ListView lvObjects;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		try {
			CoGeoMonitorObjects = Reflector().CoGeoMonitorObjects;
		}
    	catch (Exception E) {
			Toast.makeText(this,E.getMessage(),Toast.LENGTH_LONG).show();
			finish();
			return; //. ->
    	}
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.reflector_gmos_panel);
        //.
        btnNewObject = (Button)findViewById(R.id.btnNewObject);
        btnNewObject.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AddNewObject();
            }
        });
        //.
    	btnRemoveInactiveObjects = (Button)findViewById(R.id.btnRemoveInactiveObjects);
    	btnRemoveInactiveObjects.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
    		    new AlertDialog.Builder(TReflectorCoGeoMonitorObjectsPanel.this)
    	        .setIcon(android.R.drawable.ic_dialog_alert)
    	        .setTitle(R.string.SConfirmation)
    	        .setMessage(R.string.SRemoveInactiveObjects)
    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
    		    	public void onClick(DialogInterface dialog, int id) {
    	            	RemoveInactiveObjects();
    		    	}
    		    })
    		    .setNegativeButton(R.string.SNo, null)
    		    .show();
            }
        });
    	//.
        btnSendSelectedObjectsToUser = (Button)findViewById(R.id.btnSendSelectedObjectsToUser);
        btnSendSelectedObjectsToUser.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SendSelectedObjectsToUser();
            }
        });
        //.
        btnClose = (Button)findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        //.
        lvObjects = (ListView)findViewById(R.id.lvObjects);
		lvObjects.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvObjects.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int ID = CoGeoMonitorObjects.Items[arg2].ID;
				CoGeoMonitorObjects.EnableDisableItem(ID,!CoGeoMonitorObjects.Items[arg2].flEnabled);
        	}              
        });         
        lvObjects.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	Intent intent = new Intent(TReflectorCoGeoMonitorObjectsPanel.this, TReflectorCoGeoMonitorObjectPanel.class);
            	intent.putExtra("Index", arg2);
            	startActivity(intent);
            	//.
            	TReflectorCoGeoMonitorObjectsPanel.this.finish();
            	TReflectorCoGeoMonitorObjectsPanel.this.setResult(Activity.RESULT_OK);
				///? Object_ShowCurrentPosition(arg2);
            	//.
            	return true; 
			}
		}); 
        //.
        lvObjects_Update();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
    private TReflector Reflector() throws Exception {
    	TReflector Reflector = TReflector.GetReflector();
    	if (Reflector == null)
    		throw new Exception(getString(R.string.SReflectorIsNull)); //. =>
		return Reflector;
    }
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        
       
        case REQUEST_ADDNEWOBJECT:
        	if (resultCode == Activity.RESULT_OK) 
                lvObjects_Update();
            break; //. >

        case REQUEST_SELECT_USER:
        	if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
            		int UserID = extras.getInt("UserID");
            		DoSendSelectedObjectsToUser(UserID);
            	}
        	}
            break; //. >
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.reflector_gmos_panel_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

        case R.id.miAddNewObject:
        	AddNewObject();
        	//.
            return true; //. >
            
        case R.id.miRemoveDisabledObjects:
        	RemoveInactiveObjects();
        	//.
            return true; //. >
            
        case R.id.miGMOsConfiguration:
        	Intent intent = new Intent(TReflectorCoGeoMonitorObjectsPanel.this, TReflectorCoGeoMonitorObjectsConfigurationPanel.class);
        	startActivity(intent);
        	//.
            return true; //. >
    	}
    
        return false;
    }

	private void lvObjects_Update() {
		String[] lvObjectsItems = new String[CoGeoMonitorObjects.Items.length];
		for (int I = 0; I < CoGeoMonitorObjects.Items.length; I++)
			lvObjectsItems[I] = CoGeoMonitorObjects.Items[I].LabelText;
		ArrayAdapter<String> lvObjectsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvObjectsItems);             
		lvObjects.setAdapter(lvObjectsAdapter);
		for (int I = 0; I < CoGeoMonitorObjects.Items.length; I++)
			lvObjects.setItemChecked(I,CoGeoMonitorObjects.Items[I].flEnabled);
	}
	
	private void AddNewObject() {
    	Intent intent = new Intent(this, TReflectorNewCoGeoMonitorObjectPanel.class);
    	startActivityForResult(intent,REQUEST_ADDNEWOBJECT);
	}
	
	private void RemoveInactiveObjects() {
		CoGeoMonitorObjects.RemoveDisabledItems();
		lvObjects_Update();
	}
	
	public void Object_ShowCurrentPosition(int idxObject) {
		new TObjectCurrentPositionShowing(CoGeoMonitorObjects.Items[idxObject]);
	}	
	
    private class TObjectCurrentPositionShowing extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_COMPLETED 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private TReflectorCoGeoMonitorObject Object;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TObjectCurrentPositionShowing(TReflectorCoGeoMonitorObject pObject) {
    		Object = pObject;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				Object.UpdateVisualizationLocation(Reflector());
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflectorCoGeoMonitorObjectsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_COMPLETED:
	            	try {
	            		Reflector().MoveReflectionWindow(Object.VisualizationLocation);
	            		//.
	            		setResult(Activity.RESULT_OK);
	            		finish();
	            	}
	            	catch (Exception Ex) {
						Toast.makeText(TReflectorCoGeoMonitorObjectsPanel.this,Ex.getMessage(),Toast.LENGTH_LONG).show();
	            	}
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TReflectorCoGeoMonitorObjectsPanel.this);    
	            	progressDialog.setMessage(TReflectorCoGeoMonitorObjectsPanel.this.getString(R.string.SWaitAMoment));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(false);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
						}
					});
	            	//.
	            	progressDialog.show(); 	            	
	            	//.
	            	break; //. >

	            case MESSAGE_PROGRESSBAR_HIDE:
	            	progressDialog.dismiss(); 
	            	//.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	progressDialog.setProgress((Integer)msg.obj);
	            	//.
	            	break; //. >
	            }
	        }
	    };
    }
    
	private long[] SelectedObjectToUser;
	
	@SuppressWarnings("deprecation")
	public void SendSelectedObjectsToUser() {
		SelectedObjectToUser = lvObjects.getCheckItemIds();
		if (SelectedObjectToUser.length == 0)
			return; //. ->
    	Intent intent = new Intent(TReflectorCoGeoMonitorObjectsPanel.this, TUserListPanel.class);
    	intent.putExtra("Mode",TUserListPanel.MODE_FORGEOMONITOROBJECT);    	
    	startActivityForResult(intent,REQUEST_SELECT_USER);		
	}
	
	public void DoSendSelectedObjectsToUser(int UserID) {
		if (SelectedObjectToUser.length == 0)
			return; //. ->
		TReflectorCoGeoMonitorObject[] Objects = new TReflectorCoGeoMonitorObject[SelectedObjectToUser.length];
		for (int I = 0; I < SelectedObjectToUser.length; I++) 
			Objects[I] = CoGeoMonitorObjects.Items[(int)SelectedObjectToUser[I]];
		new TObjectsToUserSending(UserID,Objects);
	}

    private class TObjectsToUserSending extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_DONE 					= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private int UserID;
    	private TReflectorCoGeoMonitorObject[] Objects;
    	
        private ProgressDialog progressDialog; 
    	
    	public TObjectsToUserSending(int pUserID, TReflectorCoGeoMonitorObject[] pObjects) {
    		UserID = pUserID;
    		Objects = pObjects;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				for (int I = 0; I < Objects.length; I++) {
    					TGeoScopeServerUser.TGeoMonitorObjectCommandMessage CommandMessage = new TGeoScopeServerUser.TGeoMonitorObjectCommandMessage(TGeoScopeServerUser.TGeoMonitorObjectCommandMessage.Version_0,Objects[I]);
    					Reflector().User.IncomingMessages_SendNewCommand(UserID,CommandMessage);
    					//.
    	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,(Integer)(int)(100.0*I/Objects.length)).sendToTarget();
        				//.
        				if (Canceller.flCancel)
        					throw new CancelException(); //. =>
    				}
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_DONE).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (CancelException CE) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

		private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflectorCoGeoMonitorObjectsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_DONE:
                    Toast.makeText(TReflectorCoGeoMonitorObjectsPanel.this, TReflectorCoGeoMonitorObjectsPanel.this.getString(R.string.SObjectsHaveBeenSentToUser), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TReflectorCoGeoMonitorObjectsPanel.this);
	            	progressDialog.setMessage(TReflectorCoGeoMonitorObjectsPanel.this.getString(R.string.SSendingObjects));    
	            	if (Objects.length > 1) {
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	            		progressDialog.setIndeterminate(false);
	            		progressDialog.setMax(100);
	            	}
	            	else { 
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	            		progressDialog.setIndeterminate(true);
	            	}
	            	progressDialog.setCancelable(false);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
						}
					});
	            	//.
	            	progressDialog.show(); 	            	
	            	//.
	            	break; //. >

	            case MESSAGE_PROGRESSBAR_HIDE:
	            	progressDialog.dismiss(); 
	            	//.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	progressDialog.setProgress((Integer)msg.obj);
	            	//.
	            	break; //. >
	            }
	        }
	    };
    }		
}
