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
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

@SuppressLint("HandlerLeak")
public class TUserListPanel extends Activity {

	public static final int MODE_UNKNOWN 				= 0;
	public static final int MODE_FORLOCATION 			= 1;
	public static final int MODE_FORGEOMONITOROBJECT	= 2;
	public static final int MODE_FORCHAT 				= 3;
	//.
	private static final int MESSAGE_UPDATELIST = 1;
	public static final int UpdateInterval = 1000*30; //. seconds
	//.
	public static String 	RecentUsersFileName() {
		return TReflector.ProfileFolder()+"/"+"UserListRecents.dat";
	}
	//.
	public static final int RecentUsersMaxCount = 10;
	 
	private boolean flExists = false;
	private int Mode = MODE_UNKNOWN;
	//.
	private TextView lbUserListTitle;
	private EditText edUserListNameContext;
	private Button btnSearchByNameContext;
	private Button btnClosePanel;
	private TSearchingByNameContext SearchingByNameContext = null;
	private ListView lvUserList;
	public TGeoScopeServerUser.TUserDescriptor[] 	Items = null; 
	public TUserListUpdating						ItemsUpdating = null;
	public TGeoScopeServerUser.TUserDescriptors 	RecentItems = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	Mode = extras.getInt("Mode");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.userlist_panel);
        //.
        lbUserListTitle = (TextView)findViewById(R.id.lbUserListTitle);
        switch (Mode) {
        
        case MODE_FORLOCATION:
        	lbUserListTitle.setText(R.string.SSelectUserForLocationSending);
        	break;
        	
        case MODE_FORGEOMONITOROBJECT:
        	lbUserListTitle.setText(R.string.SSelectUserForObjectsSending);
        	break;
        	
        case MODE_FORCHAT:
        	lbUserListTitle.setText(R.string.SSelectUserForChat);
        	break;
        	
        default:
        	lbUserListTitle.setText(R.string.SSelectUser);
        	break;
        }
        //.
        edUserListNameContext = (EditText)findViewById(R.id.edUserListNameContext); 
        edUserListNameContext.setOnEditorActionListener(new OnEditorActionListener() {        
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if ((arg1 == EditorInfo.IME_ACTION_NEXT) || (arg1 == EditorInfo.IME_ACTION_DONE)) {
                	StartSearching();
                }
				return false;
			}
        });        
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
				if (RecentItems != null) 
					RecentItems.Add(Items[arg2]);
				//.
            	Intent intent = TUserListPanel.this.getIntent();
            	intent.putExtra("UserID",Items[arg2].UserID);
            	intent.putExtra("UserIsDisabled",Items[arg2].UserIsDisabled);
            	intent.putExtra("UserIsOnline",Items[arg2].UserIsOnline);
            	intent.putExtra("UserName",Items[arg2].UserName);
            	intent.putExtra("UserFullName",Items[arg2].UserFullName);
            	intent.putExtra("UserContactInfo",Items[arg2].UserContactInfo);
                //.
            	TUserListPanel.this.setResult(RESULT_OK,intent);
            	//.
            	TUserListPanel.this.finish();
        	}              
        });
        lvUserList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (RecentItems != null) 
					RecentItems.Add(Items[arg2]);
				//.
            	Intent intent = new Intent(TUserListPanel.this, TUserPanel.class);
            	intent.putExtra("UserID",Items[arg2].UserID);
            	startActivity(intent);
            	//.
            	return true; 
			}
		}); 
        //.
        RecentItems = new TGeoScopeServerUser.TUserDescriptors();
        try {
			RecentItems.FromFile(RecentUsersFileName());
			//.
			TGeoScopeServerUser.TUserDescriptor[] Recents = RecentItems.GetItems();
			//. reset flags
			for (int I = 0; I < Recents.length; I++)
				Recents[I].UserIsOnline = false;
			//.
			UpdateList(Recents);
			//.
			if (Recents.length > 0)
				ItemsUpdating = new TUserListUpdating(MESSAGE_UPDATELIST,true);
		} catch (Exception E) {}
        //.
        this.setResult(RESULT_CANCELED);
        //.
        flExists = true;
	}

    @Override
	protected void onDestroy() {
        flExists = false;
        //.
    	if (RecentItems != null) {
    		if (RecentItems.IsChanged())
    	        try {
    				RecentItems.ToFile(RecentUsersFileName(),RecentUsersMaxCount);
    			} catch (IOException E) {}
    		RecentItems = null; 
    	}
    	if (SearchingByNameContext != null) {
    		SearchingByNameContext.Cancel();
    		SearchingByNameContext = null;
    	}
    	if (ItemsUpdating != null) {
    		ItemsUpdating.Cancel();
    		ItemsUpdating = null;
    	}
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
    	SearchingByNameContext = new TSearchingByNameContext(NameContext,MESSAGE_UPDATELIST);
    }
    
	public void UpdateList(TGeoScopeServerUser.TUserDescriptor[] pItems) throws Exception {
		synchronized (this) {
			Items = pItems;				
		}
		//.
		if (pItems.length == 0) {
			lvUserList.setAdapter(null);
    		return; //. ->
		}
		final String[] lvUsersItems = new String[pItems.length];
		for (int I = 0; I < pItems.length; I++) {
			String State;
			if (pItems[I].UserIsOnline)
				State = "[+]";
			else
				State = "";
			String S = State+" "+pItems[I].UserName;
			if (!pItems[I].UserFullName.equals(""))
				S = S+"\n"+"  "+pItems[I].UserFullName;
			lvUsersItems[I] = S; 
		}
		ArrayAdapter<String> lvUsersAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvUsersItems);             
		lvUserList.setAdapter(lvUsersAdapter);
	}
	
    private class TSearchingByNameContext extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESS 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;
    	
    	private String NameContext;
    	private int OnCompletionMessage;
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
		    	TGeoScopeServerUser.TUserDescriptor[] _Items = null;
		    	//.
				NameContext = NameContext+"%";
				//.
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
					TReflector Reflector = TReflector.GetReflector();
					if (Reflector == null) 
						throw new Exception(TUserListPanel.this.getString(R.string.SReflectorIsNull)); //. =>
    				_Items = Reflector.User.GetUserList(NameContext);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
				if (Canceller.flCancel)
					return; //. ->
	    		//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,_Items).sendToTarget();
	    		//.
    			MessageHandler.obtainMessage(MESSAGE_SUCCESS,_Items).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (NullPointerException NPE) { 
				TReflector Reflector = TReflector.GetReflector();
				if (!((Reflector != null) && (Reflector.isFinishing()))) 
		    		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
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
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SUCCESS:
						if (Canceller.flCancel)
			            	break; //. >
		            	if (ItemsUpdating != null) 
		            		ItemsUpdating.Cancel();
	                	TGeoScopeServerUser.TUserDescriptor[] _Items = (TGeoScopeServerUser.TUserDescriptor[])msg.obj;
						if ((_Items != null) && (_Items.length > 0))
							ItemsUpdating = new TUserListUpdating(MESSAGE_UPDATELIST);
						else
				    		Toast.makeText(TUserListPanel.this, R.string.SUsersAreNotFound, Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
    private class TUserListUpdating extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 3;
    	
    	private int OnCompletionMessage;
    	private boolean flUpdateImmediately;
    	
    	public TUserListUpdating(int pOnCompletionMessage) {
    		this(pOnCompletionMessage, false);
    	}

    	public TUserListUpdating(int pOnCompletionMessage, boolean pflUpdateImmediately) {
    		OnCompletionMessage = pOnCompletionMessage;
    		flUpdateImmediately = pflUpdateImmediately;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					if (!flUpdateImmediately) {
						Thread.sleep(UpdateInterval);
			        	//.
						if (Canceller.flCancel)
							return; //. ->
					}
					else
						flUpdateImmediately = false;
					//.
					try {
						TReflector Reflector = TReflector.GetReflector();
						if (Reflector == null) 
							throw new Exception(TUserListPanel.this.getString(R.string.SReflectorIsNull)); //. =>
						TGeoScopeServerUser.TUserDescriptor[] _Items;
						synchronized (TUserListPanel.this) {
							_Items = Items;
						}
						//.
						if ((_Items != null) && (_Items.length > 0))
							Reflector.User.UpdateUserInfos(_Items);
						//.
						if (Canceller.flCancel)
							return; //. ->
			    		//.
			    		PanelHandler.obtainMessage(OnCompletionMessage,_Items).sendToTarget();
		        	}
		        	catch (InterruptedException E) {
		        	}
		        	catch (NullPointerException NPE) { 
						TReflector Reflector = TReflector.GetReflector();
						if (!((Reflector != null) && (Reflector.isFinishing()))) 
			    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
		        	}
		        	catch (IOException E) {
		    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
		        	}
		        	catch (Throwable E) {
		    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
		        	}
				}
			}
        	catch (InterruptedException E) {
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserListPanel.this, TUserListPanel.this.getString(R.string.SUpdatingUserList)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	//.
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
	public Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_UPDATELIST: 
                	if (!flExists)
                		return; //. ->
                	try {
                    	TGeoScopeServerUser.TUserDescriptor[] _Items = (TGeoScopeServerUser.TUserDescriptor[])msg.obj;
                    	UpdateList(_Items);
                	}
                	catch (Exception E) {
                		Toast.makeText(TUserListPanel.this, TUserListPanel.this.getString(R.string.SUpdatingUserList)+E.getMessage(), Toast.LENGTH_SHORT).show();
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
