package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream;

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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TChannelIDs;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TSourceStreamChannel;

@SuppressLint("HandlerLeak")
public class TDataStreamPropsPanel extends Activity {

	public static final int PARAMETERS_TYPE_OID 	= 1;
	public static final int PARAMETERS_TYPE_OIDX 	= 2;
	
	public static final int REQUEST_EDITCHANNELPROFILE = 1;
	
	private int ParametersType;
	//.
	private long				ObjectID = -1;
	private int					ObjectIndex = -1;
	//.
	private byte[] 				DataStreamDescriptorData = null;
	private TStreamDescriptor 	DataStreamDescriptor = null;
	//.
	private TextView lbStreamName;
	private TextView lbStreamInfo;
	private TextView lbStreamChannels;
	//.
	private ListView 	lvChannels;
	private int			lvChannels_SelectedIndex = -1;
	//.
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
        	ParametersType = extras.getInt("ParametersType");
        	switch (ParametersType) {
        	
        	case PARAMETERS_TYPE_OID:
            	ObjectID = extras.getLong("ObjectID");
        		break; //. >
        		
        	case PARAMETERS_TYPE_OIDX:
            	ObjectIndex = extras.getInt("ObjectIndex");
        		break; //. >
        	}
        	//.
        	DataStreamDescriptorData = extras.getByteArray("DataStreamDescriptorData");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.sensorsmodule_datastream_props_panel);
        //.
        lbStreamName = (TextView)findViewById(R.id.lbStreamName);
        lbStreamInfo = (TextView)findViewById(R.id.lbStreamInfo);
        lbStreamChannels = (TextView)findViewById(R.id.lbStreamChannels);
        //.
        lvChannels = (ListView)findViewById(R.id.lvChannels);
        lvChannels.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        lvChannels.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
        	@Override
        	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            	try {
            		lvChannels_SelectedIndex = arg2;
            		return (OpenChannelProfile(lvChannels_SelectedIndex));
				} catch (Exception E) {
					Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					//.
					return false; //. ->
				}
        	}
		});
        //.
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
        btnGetStreamDescriptor.setVisibility(TGeoLogApplication.DebugOptions_IsDebugging() ? View.VISIBLE : View.GONE);
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
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_EDITCHANNELPROFILE: 
        	if (resultCode == RESULT_OK) {
            	try {
    				Bundle extras = data.getExtras();
					final byte[] ProfileData = extras.getByteArray("ProfileData");
					//.
            		if (lvChannels_SelectedIndex >= 0) {
                    	final TChannel Channel = DataStreamDescriptor.Channels.get(lvChannels_SelectedIndex);
                    	//.
                    	final TSourceStreamChannel SourceChannel = com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.TSourceChannelsProvider.Instance.GetChannel(Channel.GetTypeID());
                    	if (SourceChannel != null) {
                        	SourceChannel.ID = Channel.ID;
                        	//.
                    		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
                    			
                    			@Override
                    			public void Process() throws Exception {
                    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
                    				if (UserAgent == null)
                    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
                    				TCoGeoMonitorObject	Object = new TCoGeoMonitorObject(UserAgent.Server, ObjectID);
                    				byte[] ObjectModelData = Object.GetData(1000001);
                    				if (ObjectModelData != null) {
                        				Canceller.Check();
                        				//.
                    					int Idx = 0;
                    					int ObjectModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
                    					int BusinessModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
                    					//.
                    					if (ObjectModelID != 0) {
                    						TObjectModel ObjectModel = TObjectModel.GetObjectModel(ObjectModelID);
                    						if (ObjectModel != null) 
                    							try {
                    								ObjectModel.SetBusinessModel(BusinessModelID);
                    								//.
                    								TGeographServerObjectController GSOC = Object.GeographServerObjectController();
                    								ObjectModel.SetObjectController(GSOC, true);
                    								//.
                    								ObjectModel.Sensors_Channel_SetProfile(SourceChannel.ID, ProfileData); 
                    							}
                    							finally {
                    								ObjectModel.Destroy();
                    							}								
                    					}
                    				}
                    			}
                    			
                    			@Override 
                    			public void DoOnCompleted() throws Exception {
                    				Toast.makeText(TDataStreamPropsPanel.this, R.string.SNewProfileHasBeenSet, Toast.LENGTH_LONG).show();
                    			}
                    			
                    			@Override
                    			public void DoOnException(Exception E) {
                    				Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
                    			}
                    		};
                    		Processing.Start();
                    	}
            		}
				} catch (Exception E) {
					Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
        	}
            break; //. >
        }
    	//.
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
    	private TStreamDescriptor DataStreamDescriptor = null;
    	
    	public TUpdating(boolean pflShowProgress, boolean pflClosePanelOnCancel) {
    		super();
    		//.
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
	    					DataStreamDescriptor = new TStreamDescriptor(DataStreamDescriptorData,com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider.Instance);
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
    			//.
    			String S = getString(R.string.SInfo1);
    			lbStreamInfo.setText(S+DataStreamDescriptor.Info);
    			//.
    			lbStreamChannels.setText(getString(R.string.SChannels1));
    			String[] lvChannelsItems = new String[DataStreamDescriptor.Channels.size()];
    			for (int I = 0; I < DataStreamDescriptor.Channels.size(); I++) {
    				TChannel Channel = DataStreamDescriptor.Channels.get(I);
    				lvChannelsItems[I] = Channel.Name;
    				if (Channel.Info.length() > 0)
    					lvChannelsItems[I] += " "+"/"+Channel.Info+"/"; 
    			}
    			ArrayAdapter<String> lvChannelsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvChannelsItems);             
    			lvChannels.setAdapter(lvChannelsAdapter);
    			for (int I = 0; I < DataStreamDescriptor.Channels.size(); I++)
    				lvChannels.setItemChecked(I,true);
    		}
    		else {
    			lbStreamName.setText("?");
    			lbStreamInfo.setText(getString(R.string.SInfo1)+"?");
    			lbStreamChannels.setText(getString(R.string.SChannels1)+"?");
    			lvChannels.setAdapter(null);
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
    	if (DataStreamDescriptor == null)
    		return; //. ->
    	final TChannelIDs Channels = new TChannelIDs();
		for (int I = 0; I < DataStreamDescriptor.Channels.size(); I++)
			if (lvChannels.isItemChecked(I))
				Channels.AddID(DataStreamDescriptor.Channels.get(I).ID);
		if (Channels.Count() == 0)
			return; // ->
    	//.
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
				//.
				DescriptorData = DataStreamDescriptorData;
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
	        	switch (ParametersType) {
	        	
	        	case PARAMETERS_TYPE_OID:
					intent.putExtra("ParametersType", TDataStreamPanel.PARAMETERS_TYPE_OID);
					intent.putExtra("ObjectID", ObjectID);
	        		break; //. >
	        		
	        	case PARAMETERS_TYPE_OIDX:
					intent.putExtra("ParametersType", TDataStreamPanel.PARAMETERS_TYPE_OIDX);
					intent.putExtra("ObjectIndex", ObjectIndex);
	        		break; //. >
	        	}
				//.
				intent.putExtra("StreamDescriptorData", DescriptorData);
				//.
				intent.putExtra("StreamChannels", Channels.ToByteArray());
				//.
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
			
			private File DescriptorFile;
			
			@Override
			public void Process() throws Exception {
				DescriptorFile = new File(TGeoLogApplication.GetTempFolder()+"/"+"DataStreamDescriptor.xml");
				FileOutputStream fos = new FileOutputStream(DescriptorFile);
				try {
					fos.write(DataStreamDescriptorData, 0,DataStreamDescriptorData.length);
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

    private boolean OpenChannelProfile(int Idx) throws Exception {
    	final TChannel Channel = DataStreamDescriptor.Channels.get(Idx);
    	//.
    	final TSourceStreamChannel SourceChannel = com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.TSourceChannelsProvider.Instance.GetChannel(Channel.GetTypeID());
    	if (SourceChannel == null)
    		return false; //. ->
    	SourceChannel.ID = Channel.ID;
    	//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			@Override
			public void Process() throws Exception {
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				TCoGeoMonitorObject	Object = new TCoGeoMonitorObject(UserAgent.Server, ObjectID);
				byte[] ObjectModelData = Object.GetData(1000001);
				if (ObjectModelData != null) {
    				Canceller.Check();
    				//.
					int Idx = 0;
					int ObjectModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
					int BusinessModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
					//.
					if (ObjectModelID != 0) {
						TObjectModel ObjectModel = TObjectModel.GetObjectModel(ObjectModelID);
						if (ObjectModel != null) 
							try {
								ObjectModel.SetBusinessModel(BusinessModelID);
								//.
								TGeographServerObjectController GSOC = Object.GeographServerObjectController();
								ObjectModel.SetObjectController(GSOC, true);
								//.
								SourceChannel.Profile.FromByteArray(ObjectModel.Sensors_Channel_GetProfile(SourceChannel.ID)); 
							}
							finally {
								ObjectModel.Destroy();
							}								
					}
				}
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
		    	Intent ProfilePanel = SourceChannel.Profile.GetProfilePanel(TDataStreamPropsPanel.this);
		    	if (ProfilePanel != null) 
		    		startActivityForResult(ProfilePanel, REQUEST_EDITCHANNELPROFILE);
		    	else
		    		throw new Exception(getString(R.string.SThereIsNoProfilePanel)); //. ->
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
		//.
    	return true; //. ->
    }
}
