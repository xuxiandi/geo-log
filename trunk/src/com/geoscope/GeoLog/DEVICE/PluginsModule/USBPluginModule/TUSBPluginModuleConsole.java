package com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule.TUSBPluginModule.TDoOnMessageIsReceivedHandler;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TUSBPluginModuleConsole extends Activity {

	private static final int RESPONSE_RECEIVED 	= 1;
	
	private static final int SENDERTYPE_INFORMER 		= 0;
	private static final int SENDERTYPE_COMMANDER 		= 1;
	private static final int SENDERTYPE_PROCESSOR 		= 2;
	private static final int SENDERTYPE_ERRORHANDLER	= 3;

	private boolean flExists = false;
	//.
	private TUSBPluginModule USBPluginModule;
	//.
	private ScrollView svConsoleArea;
	private LinearLayout llConsoleArea;
	private EditText edCommand;
	private Spinner spLastCommands;
	private Button btnCommandSend;
	//.
	private ArrayList<String> LastCommands = new ArrayList<String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		try {
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	//.
	    	USBPluginModule = Tracker.GeoLog.PluginsModule.USBPluginModule;
		} catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
            return; //. ->
		}
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.pluginsmodule_usbpluginmodule_console);
        //.
        svConsoleArea = (ScrollView)findViewById(R.id.svConsoleArea);
        llConsoleArea = (LinearLayout)findViewById(R.id.llConsoleArea);
        //.
        edCommand = (EditText)findViewById(R.id.edCommand);
        edCommand.setOnEditorActionListener(new OnEditorActionListener() {        
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if(arg1 == EditorInfo.IME_ACTION_DONE){
                	String Command = edCommand.getText().toString();
                	if (!Command.equals(""))
                		try {
                    		SendCommand(Command);
                		} catch (Exception E) {
                			ShowError(E.getMessage());
                		}
                }
				return false;
			}
        });
        //.
        spLastCommands = (Spinner)findViewById(R.id.spLastCommands);
        spLastCommands.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	edCommand.setText(LastCommands.get(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });        
        //.
        btnCommandSend = (Button)findViewById(R.id.btnCommandSend);
        btnCommandSend.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	String Command = edCommand.getText().toString();
            	if (!Command.equals(""))
            		try {
                		SendCommand(Command);
            		} catch (Exception E) {
            			ShowError(E.getMessage());
            		}
            }
        });
		//.
		USBPluginModule.SetDoOnMessageIsReceivedHandler(new TDoOnMessageIsReceivedHandler() {
			@Override
			public void DoOnMessageIsReceived(String Message) throws Exception {
		    	ReceiveResponse(Message);
			};
		});
		//.
		try {
			USBPluginModule.CheckConnectedAccesory();
		} catch (Exception E) {
			ShowError(E.getMessage());
		}
		if (USBPluginModule.Accessory != null)
			ShowInfo("USB accessory is attached: "+USBPluginModule.Accessory.getModel());
		else
			ShowError("USB accessory is not found");
        //.
        flExists = true;
	}
	
    @Override
	protected void onDestroy() {
    	flExists = false;
    	//.
    	if (USBPluginModule != null)
    		USBPluginModule.SetDoOnMessageIsReceivedHandler(null);
    	//.
		super.onDestroy();
	}

    private void SendCommand(String Command) throws Exception {
    	Command = Command.toUpperCase(Locale.ENGLISH);
    	//.
		ConsoleArea_AddMessage("CMD", Command, SENDERTYPE_COMMANDER);
		//.
		LastCommands.remove(Command);
		LastCommands.add(0, Command);
        String[] SA = new String[LastCommands.size()];
        for (int I = 0; I < LastCommands.size(); I++)
        	SA[I] = LastCommands.get(I);
        ArrayAdapter<String> saLastCommands = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saLastCommands.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spLastCommands.setAdapter(saLastCommands);
		//.
        USBPluginModule.SendMessage(Command);
    }
    
    public void ReceiveResponse(String Response) {
		PanelHandler.obtainMessage(RESPONSE_RECEIVED,Response).sendToTarget();
    }
    
    private void ShowInfo(String Info) {
		ConsoleArea_AddMessage("INFO", Info, SENDERTYPE_INFORMER);
    }
    
    private void ShowResponse(String Response) {
		ConsoleArea_AddMessage("RESP", Response, SENDERTYPE_PROCESSOR);
    }
    
    private void ShowError(String Error) {
		ConsoleArea_AddMessage("ERROR", Error, SENDERTYPE_ERRORHANDLER);
    }
    
    private void ConsoleArea_AddMessage(String Prefix, String Message, int SenderType) {
    	TextView tvMessage = new TextView(this);
    	tvMessage.setText((new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(OleDate.UTCCurrentTimestamp())).GetDateTime())+" "+Prefix+": "+Message);
    	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    	tvMessage.setLayoutParams(LP);
    	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
    	switch (SenderType) {
    	
    	case SENDERTYPE_INFORMER:
    		tvMessage.setTextColor(Color.GREEN);
    		break; //. >

    	case SENDERTYPE_COMMANDER:
    		tvMessage.setTextColor(Color.BLACK);
    		break; //. >

    	case SENDERTYPE_PROCESSOR:
    		tvMessage.setTextColor(Color.BLUE);
    		break; //. >

    	case SENDERTYPE_ERRORHANDLER:
    		tvMessage.setTextColor(Color.RED);
    		break; //. >
    	}
    	llConsoleArea.addView(tvMessage);
    	tvMessage.setVisibility(View.VISIBLE);
    	//.
    	svConsoleArea.postDelayed(new Runnable() {
    	    @Override
    	    public void run() {
            	try {
                    svConsoleArea.fullScroll(View.FOCUS_DOWN);
            	}
            	catch (Throwable E) {
            		TGeoLogApplication.Log_WriteError(E);
            	}
    	    }
		},100);
    }
    
	public final Handler PanelHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case RESPONSE_RECEIVED: 
    				if (!flExists)
    	            	break; //. >
                	try {
                		String Response = (String)msg.obj;
                		ShowResponse(Response);
                	}
                	catch (Exception E) {
                		Toast.makeText(TUSBPluginModuleConsole.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
