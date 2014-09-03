package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

@SuppressLint("HandlerLeak")
public class TDataStreamPropsPanel extends Activity {

	private int 	idTComponent;
	private long	idComponent;
	//.
	private TDataStreamDescriptor DataStreamDescriptor = null;
	//.
	private TextView lbStreamName;
	private TextView lbStreamInfo;
	private TextView lbStreamChannels;
	private Button btnOpenStream;
	private Button btnGetStreamDescriptor;
	//.
	private TUpdating	Updating = null;
	@SuppressWarnings("unused")
	private boolean flUpdate = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	idTComponent = extras.getInt("idTComponent");
        	idComponent = extras.getLong("idComponent");
        }
        //.
        setContentView(R.layout.datastream_props_panel);
        //.
        lbStreamName = (TextView)findViewById(R.id.lbStreamName);
        lbStreamInfo = (TextView)findViewById(R.id.lbStreamInfo);
        lbStreamChannels = (TextView)findViewById(R.id.lbStreamChannels);
        btnOpenStream = (Button)findViewById(R.id.btnOpenStream);
        btnOpenStream.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	try {
            		OpenStream();
				} catch (Exception E) {
					Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        btnGetStreamDescriptor = (Button)findViewById(R.id.btnGetStreamDescriptor);
        btnGetStreamDescriptor.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	try {
            		GetStreamDescriptor();
				} catch (Exception E) {
					Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        StartUpdating();
    }
    
    @Override
    protected void onDestroy() {
		if (Updating != null) {
			Updating.Cancel();
			Updating = null;
		}
		//.
    	super.onDestroy();
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
    	private TDataStreamDescriptor DataStreamDescriptor = null;
    	
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
	    				try {
		    				final TDataStreamFunctionality DSF = (TDataStreamFunctionality)UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(UserAgent.Server, idTComponent, idComponent);
		    				try {
		    					DataStreamDescriptor = DSF.GetDescriptor();
		    				}
		    				finally {
		    					DSF.Release();
		    				}
	    				}
	    				catch (Exception E) {
	    					DataStreamDescriptor = null;
	    					//.
	    	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
	    				}
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
		                Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TDataStreamPropsPanel.this.DataStreamDescriptor = DataStreamDescriptor;
	           		 	//.
		            	TDataStreamPropsPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	TDataStreamPropsPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TDataStreamPropsPanel.this);    
		            	progressDialog.setMessage(TDataStreamPropsPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TDataStreamPropsPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TDataStreamPropsPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TDataStreamPropsPanel.this.finish();
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
	
    private void Update() {
    	flUpdate = true; 
    	try {
    		if (DataStreamDescriptor != null) {
    			lbStreamName.setText(DataStreamDescriptor.Name);
    			String S = getString(R.string.SInfo1);
    			lbStreamInfo.setText(S+DataStreamDescriptor.Info);
    			S = getString(R.string.SChannels1);
    			StringBuilder SB = new StringBuilder();
    			for (int I = 0; I < DataStreamDescriptor.Channels.size(); I++) {
    				TDataStreamDescriptor.TChannel Channel = DataStreamDescriptor.Channels.get(I);
					if (I > 0)
						SB.append(", "+Channel.Name);
					else
						SB.append(Channel.Name);
    			}
    			lbStreamChannels.setText(S+SB.toString());
    		}
    		else {
    			lbStreamName.setText("?");
    			String S = getString(R.string.SInfo1);
    			lbStreamInfo.setText(S+"?");
    			S = getString(R.string.SChannels1);
    			lbStreamChannels.setText(S+"?");
    		}
    	}
    	finally {
    		flUpdate = false;
    	}
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,true);
    }    
    
    private void OpenStream() {
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private TUserAgent UserAgent;
			private TGeoScopeServerInfo.TInfo ServersInfo;
			private byte[] DescriptorData;
			
			@Override
			public void Process() throws Exception {
				UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				ServersInfo = UserAgent.Server.Info.GetInfo();
				if (!ServersInfo.IsSpaceDataServerValid()) 
					throw new Exception("Invalid space data server"); //. =>
				final TDataStreamFunctionality DSF = (TDataStreamFunctionality)UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(UserAgent.Server, idTComponent, idComponent);
				try {
					DescriptorData = DSF.GetDescriptorData();
				}
				finally {
					DSF.Release();
				}
			}
			@Override 
			public void DoOnCompleted() throws Exception {
				Intent intent = new Intent(TDataStreamPropsPanel.this, TDataStreamPanel.class);
				//.
				intent.putExtra("ServerAddress", ServersInfo.SpaceDataServerAddress); 
				intent.putExtra("ServerPort", ServersInfo.SpaceDataServerPort);
				//.
				intent.putExtra("UserID", UserAgent.Server.User.UserID); 
				intent.putExtra("UserPassword", UserAgent.Server.User.UserPassword);
				//.
				intent.putExtra("idComponent", idComponent);
				//.
				intent.putExtra("StreamDescriptor", DescriptorData); 
				startActivity(intent);
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }

    private void GetStreamDescriptor() {
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private TUserAgent UserAgent;
			private TGeoScopeServerInfo.TInfo ServersInfo;
			private File DescriptorFile;
			
			@Override
			public void Process() throws Exception {
				UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				ServersInfo = UserAgent.Server.Info.GetInfo();
				if (!ServersInfo.IsSpaceDataServerValid()) 
					throw new Exception("Invalid space data server"); //. =>
				byte[] DescriptorData;
				final TDataStreamFunctionality DSF = (TDataStreamFunctionality)UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(UserAgent.Server, idTComponent, idComponent);
				try {
					DescriptorData = DSF.GetDescriptorData();
				}
				finally {
					DSF.Release();
				}
				//.
				DescriptorFile = new File(TGeoLogApplication.TempFolder+"/"+"DataStreamDescriptor.xml");
				FileOutputStream fos = new FileOutputStream(DescriptorFile);
				try {
					fos.write(DescriptorData, 0,DescriptorData.length);
				} finally {
					fos.close();
				}
			}
			@Override 
			public void DoOnCompleted() throws Exception {
				Intent intent = new Intent();
				intent.setDataAndType(Uri.fromFile(DescriptorFile), "text/xml");
				intent.setAction(android.content.Intent.ACTION_VIEW);
				startActivity(intent);
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }
}
