package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1DeviceSchema;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TSourceStreamChannel;

@SuppressLint("HandlerLeak")
public class TDataStreamPropsPanel extends Activity {

	public static final int PARAMETERS_TYPE_OID 	= 1;
	public static final int PARAMETERS_TYPE_OIDX 	= 2;
	
	public static final int PanelUpdatinginterval = 1000*900; //. seconds
	
	public static final int REQUEST_EDITCHANNELPROFILE = 1;
	
	private int ParametersType;
	//.
	private long				ObjectID = -1;
	private int					ObjectIndex = -1;
	//.
	private TStreamDescriptor 					DataStreamDescriptor = null;
    private ArrayList<TChannel>					DataStreamChannels = null;
	//.
	private TObjectModel.TSensorChannelStatus[] DataStreamChannelsStatus = null;
	//.
	private boolean flDenyOpening = false;
	//.
	private TextView lbStreamName;
	private TextView lbStreamInfo;
	private TextView lbStreamChannels;
	//.
	private TChannelIDs ChannelIDs = null;
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
    	try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
            	ParametersType = extras.getInt("ParametersType");
            	switch (ParametersType) {
            	
            	case PARAMETERS_TYPE_OID:
                	ObjectID = extras.getLong("ObjectID");
            		break; //. >
            		
            	case PARAMETERS_TYPE_OIDX:
                	ObjectIndex = extras.getInt("ObjectIndex");
                	//.
            		TReflector Reflector = TReflector.GetReflector();
                	TCoGeoMonitorObject Object = Reflector.Component.CoGeoMonitorObjects.Items[ObjectIndex];
                	ObjectID = Object.ID;
            		break; //. >
            	}
            	byte[] _ChannelIDs = extras.getByteArray("ChannelIDs");
            	if (_ChannelIDs != null) 
            		ChannelIDs = new TChannelIDs(_ChannelIDs);
            	flDenyOpening = extras.getBoolean("DenyOpening");
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
            btnOpenStream.setVisibility(flDenyOpening ? View.GONE : View.VISIBLE);
            //.
            btnGetStreamDescriptor = (Button)findViewById(R.id.btnGetStreamDescriptor);
            btnGetStreamDescriptor.setVisibility(((ChannelIDs == null) && TGeoLogApplication.DebugOptions_IsDebugging()) ? View.VISIBLE : View.GONE);
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
        	Updating = new TUpdating(PanelUpdatinginterval);
        	//.
        	Updating.StartUpdate();
		} catch (Exception E) {
			Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    @Override
    protected void onDestroy() {
    	if (Updating != null) {
			try {
				Updating.Destroy(false);
			} catch (InterruptedException IE) {
			}
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
                    	final TChannel Channel = DataStreamChannels.get(lvChannels_SelectedIndex);
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
                    		    	Updating.StartUpdate();
                    		    	//.
                    				Toast.makeText(TDataStreamPropsPanel.this, R.string.SNewProfileHasBeenSet, Toast.LENGTH_LONG).show();
                    			}
                    			
                    			@Override
                    			public void DoOnException(Exception E) {
                    		    	Updating.StartUpdate();
                    		    	//.
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
    	
    	
    	private int UpdateInterval;
    	//.
    	private TAutoResetEvent UpdateSignal = new TAutoResetEvent();
    	//.
    	private TCanceller ProcessingCanceller = new TCanceller();
        //.
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
        private ProgressDialog progressDialog;
        //.
    	private TStreamDescriptor 					DataStreamDescriptor = null;
    	//.
    	private TObjectModel.TSensorChannelStatus[] DataStreamChannelsStatus = null;
        
    	public TUpdating(int pUpdateInterval) {
    		super();
    		//.
    		UpdateInterval = pUpdateInterval;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

    	public void Destroy(boolean flWaitForTermination) throws InterruptedException {
			Cancel();
			//.
			StartUpdate();
			//.
			if (flWaitForTermination)
				Wait();
    	}
    	
		@Override
		public void run() {
			try {
				try {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    				TCoGeoMonitorObject	Object = new TCoGeoMonitorObject(UserAgent.Server, ObjectID);
    				//.
					int UpdateCount = 0;
					while (!Canceller.flCancel) {
						//. wait for an update signal
						boolean flStartedByUser = UpdateSignal.WaitOne(UpdateInterval);
						Canceller.Check();
						//.
						try {
							flShowProgress = flStartedByUser; 
							flClosePanelOnCancel = (UpdateCount == 0);
							//.
							ProcessingCanceller.Reset();
			    			try {
								if (flShowProgress)
									MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
								//.
		        				byte[] ObjectModelData = Object.GetData(1000001);
		        				if (ObjectModelData != null) {
				    				Canceller.Check();
				    				ProcessingCanceller.Check();
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
				    							synchronized (GSOC) {
				    								boolean flKeepConnectionLast = GSOC.KeepConnection();
				    								try {
					    								GSOC.Connect();
					    								try {
					    				    				Canceller.Check();
					    				    				ProcessingCanceller.Check();
					    				    				//.
					    									byte[] ObjectSchemaData = GSOC.Component_ReadAllCUAC(new int[] {1}/*object side*/);
					    									//.
					    				    				Canceller.Check();
					    				    				ProcessingCanceller.Check();
					    				    				//.
					    									if (ObjectSchemaData == null)
					    										throw new Exception("there is no object schema data"); //. =>
					    									ObjectModel.ObjectSchema.RootComponent.FromByteArray(ObjectSchemaData,new TIndex());
					    									//.
					    									byte[] ObjectDeviceSchemaData = GSOC.Component_ReadAllCUAC(new int[] {2/*device side*/});
					    									//.
					    				    				Canceller.Check();
					    				    				ProcessingCanceller.Check();
					    				    				//.
					    									if (ObjectDeviceSchemaData == null)
					    										throw new Exception("there is no device schema data"); //. =>
					    									ObjectModel.ObjectDeviceSchema.RootComponent.FromByteArray(ObjectDeviceSchemaData,new TIndex());
					    									//.
					        								TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent DC = (TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent)ObjectModel.BusinessModel.ObjectModel.ObjectDeviceSchema.RootComponent;
					        								byte[] ModelData = DC.SensorsModule.SensorsDataValue.Value;
					        								com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.TModel Model = new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.TModel(ModelData);
					        								DataStreamDescriptor = Model.Stream;
					        					        	if (ChannelIDs == null)
					        					        		DataStreamChannels = DataStreamDescriptor.Channels;
					        					        	else {
					        					        		DataStreamChannels = new ArrayList<TChannel>();
					        					        		int Cnt = ChannelIDs.Count();
					        					        		for (int I = 0; I < Cnt; I++) {
					        					        			TChannel Channel = Model.StreamChannels_GetOneByID(ChannelIDs.Items.get(I));
					        					        			if (Channel != null) 
					        					        				DataStreamChannels.add(Channel);
					        					        		}
					        					        	}
					        								//.
					        			    				Canceller.Check();
					        			    				ProcessingCanceller.Check();
					        								//.
					        								int Cnt = DataStreamChannels.size();
					        								int[] ChannelIDs = new int[Cnt];
					        								for (int I = 0; I < Cnt; I++)
					        									ChannelIDs[I] = DataStreamChannels.get(I).ID;
					    									ObjectModel.ObjectController = GSOC;
					        								DataStreamChannelsStatus = ObjectModel.Sensors_Channels_GetStatus(ChannelIDs); 
					    								}
				    									finally {
						    								GSOC.Disconnect();
				    									}
				    								}
				    								finally {
				    									GSOC.Connection_flKeepAlive = flKeepConnectionLast;
				    								}
												}
		        							}
		        							finally {
		        								ObjectModel.Destroy();
		        							}								
		        					}
		        				}
							}
							finally {
								if (flShowProgress)
									MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
							}
		    				//.
			    			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
			    			UpdateCount++;
						}
			        	catch (InterruptedException E) {
			        		return; //. ->
			        	}
						catch (CancelException CE) {
							if (CE.Canceller != ProcessingCanceller)
								throw CE; //. =>
						}
			        	catch (IOException E) {
			    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
			        	}
			        	catch (Throwable E) {
			    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
			        	}
					}
	        	}
	        	catch (InterruptedException E) {
	        	}
				catch (CancelException CE) {
				}
	        	catch (Exception E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
	        	}
			}
			finally {
    			MessageHandler.obtainMessage(MESSAGE_FINISHED).sendToTarget();
			}
		}

		public void StartUpdate() {
			ProcessingCanceller.Cancel();
			//.
			UpdateSignal.Set();
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
		            	TDataStreamPropsPanel.this.DataStreamChannelsStatus = DataStreamChannelsStatus;
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
    			String[] lvChannelsItems = new String[DataStreamChannels.size()];
    			for (int I = 0; I < DataStreamChannels.size(); I++) {
    				TChannel Channel = DataStreamChannels.get(I);
    				lvChannelsItems[I] = Channel.Name;
    				if (Channel.Info.length() > 0)
    					lvChannelsItems[I] += " "+"/"+Channel.Info+"/";
    				//.
    				if ((DataStreamChannelsStatus != null) && (I < DataStreamChannelsStatus.length)) {
        				if (DataStreamChannelsStatus[I].Enabled) {
        					if (DataStreamChannelsStatus[I].Active)
            					lvChannelsItems[I] += "   "+"["+getString(R.string.SActive3)+"]";
        				}
        				else
        					lvChannelsItems[I] += "  "+"("+getString(R.string.SDisabled2)+")";
    				}
    			}
    			int ListType = android.R.layout.simple_list_item_multiple_choice; 
    			if (flDenyOpening)
    				ListType = android.R.layout.simple_list_item_single_choice;
    			ArrayAdapter<String> lvChannelsAdapter = new ArrayAdapter<String>(this,ListType,lvChannelsItems);             
    			lvChannels.setAdapter(lvChannelsAdapter);
    			/* for (int I = 0; I < DataStreamChannels.size(); I++)
    				lvChannels.setItemChecked(I,true); */
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

    private void OpenStream() {
    	if (DataStreamDescriptor == null)
    		return; //. ->
    	final TChannelIDs Channels = new TChannelIDs();
		for (int I = 0; I < DataStreamChannels.size(); I++)
			if (lvChannels.isItemChecked(I))
				Channels.AddID(DataStreamChannels.get(I).ID);
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
				DescriptorData = DataStreamDescriptor.ToByteArray();
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
					byte[] BA = DataStreamDescriptor.ToByteArray();
					fos.write(BA, 0,BA.length);
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
    	final TChannel Channel = DataStreamChannels.get(Idx);
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
