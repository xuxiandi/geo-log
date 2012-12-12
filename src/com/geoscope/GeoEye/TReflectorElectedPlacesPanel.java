package com.geoscope.GeoEye;

import java.io.IOException;

import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Defines.TElectedPlaces;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class TReflectorElectedPlacesPanel extends Activity  {

	public static final int REQUEST_ADDNEWPLACE = 1;
	public static final int REQUEST_SELECT_USER = 2;
	
	private TReflector Reflector;
	private TElectedPlaces ElectedPlaces;
	
	private Button btnNewPlace;
	private Button btnRemoveSelectedPlaces;
	private Button btnSendSelectedPlacesToUser;
	private Button btnClose;
	private ListView lvPlaces;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.MyReflector;
        ElectedPlaces = Reflector.ElectedPlaces;
        //.
        setContentView(R.layout.reflector_electedplaces_panel);
        //.
        btnNewPlace = (Button)findViewById(R.id.btnNewPlace);
        btnNewPlace.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AddNewPlace();
            }
        });
        //.
        btnRemoveSelectedPlaces = (Button)findViewById(R.id.btnRemoveSelectedPlaces);
        btnRemoveSelectedPlaces.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	RemoveSelectedPlaces();
            }
        });
        //.
        btnSendSelectedPlacesToUser = (Button)findViewById(R.id.btnSendSelectedPlacesToUser);
        btnSendSelectedPlacesToUser.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SendSelectedPlacesToUser();
            }
        });
        //.
        btnClose = (Button)findViewById(R.id.btnCloseElectedPlacesPanel);
        btnClose.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        //.
        lvPlaces = (ListView)findViewById(R.id.lvPlaces);
        lvPlaces.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvPlaces.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	}              
        });         
        lvPlaces.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Object_ShowPlace(arg2);
            	//.
            	finish();
            	//.
            	return true; 
			}
		}); 
        //.
        lvPlaces_Update();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_ADDNEWPLACE:
            if (resultCode == Activity.RESULT_OK) 
                lvPlaces_Update();
            break; //. >

        case REQUEST_SELECT_USER:
        	if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
            		int UserID = extras.getInt("UserID");
            		DoSendSelectedPlacesToUser(UserID);
            	}
        	}
            break; //. >
        }
		super.onActivityResult(requestCode, resultCode, data);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return true;
    }

	private void lvPlaces_Update() {
		String[] lvPlacesItems = new String[ElectedPlaces.Items.size()];
		for (int I = 0; I < ElectedPlaces.Items.size(); I++) {
			TLocation EP = ElectedPlaces.Items.get(I);
			String S = EP.Name;
			if (EP.RW.EndTimestamp != TReflectionWindowActualityInterval.MaxTimestamp) {
				OleDate DT = new OleDate(EP.RW.EndTimestamp);
				String DTS = Integer.toString(DT.year % 100)+"/"+Integer.toString(DT.month)+"/"+Integer.toString(DT.date)+" "+Integer.toString(DT.hrs)+":"+Integer.toString(DT.min)+":"+Integer.toString(DT.sec);
				S = S+"@"+DTS;
			}
			lvPlacesItems[I] = S;
		}
		ArrayAdapter<String> lvPlacesAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvPlacesItems);             
		lvPlaces.setAdapter(lvPlacesAdapter);
		for (int I = 0; I < ElectedPlaces.Items.size(); I++)
			lvPlaces.setItemChecked(I,false);
	}
	
	private void AddNewPlace() {
    	Intent intent = new Intent(this, TReflectorNewElectedPlacePanel.class);
    	startActivityForResult(intent,REQUEST_ADDNEWPLACE);
	}
	
	private void RemoveSelectedPlaces() {
		long[] RemoveItems = lvPlaces.getCheckItemIds();
		if (RemoveItems.length > 0) {
			int AddFactor = 0;
			for (int I = 0; I < RemoveItems.length; I++) {
				ElectedPlaces.RemovePlace((int)(RemoveItems[I]+AddFactor));
				AddFactor--;
			}
			try {
				ElectedPlaces.Save();
			}
			catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
			}
			//.
			lvPlaces_Update();
		}
	}
	
	public void Object_ShowPlace(int idxPlace) {
		try {
			TLocation P = ElectedPlaces.Items.get(idxPlace);
			Reflector.SetReflectionWindowByLocation(P);
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	}
	
	private long[] SelectedPlacesToUser;
	
	public void SendSelectedPlacesToUser() {
		SelectedPlacesToUser = lvPlaces.getCheckItemIds();
		if (SelectedPlacesToUser.length == 0)
			return; //. ->
    	Intent intent = new Intent(TReflectorElectedPlacesPanel.this, TUserListPanel.class);
    	intent.putExtra("Mode",TUserListPanel.MODE_FORLOCATION);    	
    	startActivityForResult(intent,REQUEST_SELECT_USER);		
	}
	
	public void DoSendSelectedPlacesToUser(int UserID) {
		if (SelectedPlacesToUser.length == 0)
			return; //. ->
		TLocation[] Places = new TLocation[SelectedPlacesToUser.length];
		for (int I = 0; I < SelectedPlacesToUser.length; I++) 
			Places[I] = ElectedPlaces.Items.get((int)SelectedPlacesToUser[I]);
		new TPlacesToUserSending(UserID,Places);
	}

    private class TPlacesToUserSending extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_DONE 					= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private int UserID;
    	private TLocation[] Places;
    	
        private ProgressDialog progressDialog; 
    	
    	public TPlacesToUserSending(int pUserID, TLocation[] pPlaces) {
    		UserID = pUserID;
    		Places = pPlaces;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				for (int I = 0; I < Places.length; I++) { 
    					Reflector.User.IncomingMessages_SendNew(Reflector, UserID,Places[I].ToIncomingMessageLocationCommand());
    	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,(Integer)(int)(100.0*I/Places.length)).sendToTarget();
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
	                Toast.makeText(TReflectorElectedPlacesPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_DONE:
                    Toast.makeText(Reflector, R.string.SPlacesHaveBeenSentToUser, Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TReflectorElectedPlacesPanel.this);    
	            	progressDialog.setMessage(TReflectorElectedPlacesPanel.this.getString(R.string.SSendingPlaces));    
	            	if (Places.length > 1) {
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
