package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class TUserListPanel extends Activity {

	private static final int MESSAGE_SEARCHING_COMPLETED = 1;
	
	private TReflector Reflector;
	//.
	private EditText edUserListNameContext;
	private Button btnSearchByNameContext;
	private Button btnClosePanel;
	private TSearchingByNameContext SearchingByNameContext = null;
	private ListView lvUserList;
	public TReflectorUser.TUserDescriptor[] Items = null; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
		Reflector = TReflector.MyReflector;  
        //.
        setContentView(R.layout.userlist_panel);
        //.
        edUserListNameContext = (EditText)findViewById(R.id.edUserListNameContext); 
        btnSearchByNameContext = (Button)findViewById(R.id.btnSearchUsersByName); 
        btnSearchByNameContext.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	StartSearching();
            }
        });
        btnClosePanel = (Button)findViewById(R.id.btnCloseUserListPanel);
        btnClosePanel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        lvUserList = (ListView)findViewById(R.id.lvUserList);
        lvUserList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvUserList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	int UserID = Items[arg2].UserID;
            	Intent intent = TUserListPanel.this.getIntent();
            	intent.putExtra("UserID",UserID);
                //.
            	setResult(Activity.RESULT_OK,intent);
            	//.
            	finish();
        	}              
        });
        //.
        this.setResult(RESULT_CANCELED);
	}

    @Override
	protected void onDestroy() {
		super.onDestroy();
	}

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

    public void StartSearching() {
    	String NameContext = edUserListNameContext.getText().toString();
    	if (NameContext.length() < 2) {
    		Toast.makeText(TUserListPanel.this, R.string.STooShortSearchContext, Toast.LENGTH_SHORT).show();
    		return; //. ->
    	}
    	if (SearchingByNameContext != null)
    		SearchingByNameContext.Cancel();
    	SearchingByNameContext = new TSearchingByNameContext(NameContext,MESSAGE_SEARCHING_COMPLETED);
    }
    
	public void UpdateList(TReflectorUser.TUserDescriptor[] pItems) throws Exception {
		Items = pItems;
		//.
		if (Items.length == 0) {
			lvUserList.setAdapter(null);
			//.
    		Toast.makeText(TUserListPanel.this, R.string.SUsersAreNotFound, Toast.LENGTH_SHORT).show();
    		return; //. ->
		}
		final String[] lvUsersItems = new String[Items.length];
		for (int I = 0; I < Items.length; I++) {
			String S = Items[I].UserName;
			if (!Items[I].UserFullName.equals(""))
				S = S+"\n"+"  "+Items[I].UserFullName;
			lvUsersItems[I] = S; 
		}
		ArrayAdapter<String> lvUsersAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvUsersItems);             
		lvUserList.setAdapter(lvUsersAdapter);
	}
	
    private class TSearchingByNameContext implements Runnable {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private String NameContext;
    	private int OnCompletionMessage;
    	//.
    	private Thread _Thread;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TSearchingByNameContext(String pNameContext, int pOnCompletionMessage) {
    		NameContext = pNameContext;
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
		    	TReflectorUser.TUserDescriptor[] _Items = null;
		    	//.
				NameContext = NameContext+"%";
				//.
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				_Items = Reflector.User.GetUserList(Reflector, NameContext);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,_Items).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TUserListPanel.this, TUserListPanel.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TUserListPanel.this);    
	            	progressDialog.setMessage(TUserListPanel.this.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(false); 
	            	progressDialog.setCancelable(true);
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
	    
    	public void Cancel() {
    		_Thread.interrupt();
    	}
    }
		
	public Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_SEARCHING_COMPLETED: 
            	try {
                	//.
                	TReflectorUser.TUserDescriptor[] _Items = (TReflectorUser.TUserDescriptor[])msg.obj;
                	UpdateList(_Items);
            	}
            	catch (Exception E) {
            		Toast.makeText(TUserListPanel.this, TUserListPanel.this.getString(R.string.SErrorOfListUpdating)+E.getMessage(), Toast.LENGTH_SHORT).show();
            	}
            	break; //. >
            }
        }
    };
}
