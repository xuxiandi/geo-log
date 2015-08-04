package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream;

import java.io.File;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannelIDs;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Audio.VC.TVCChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Telecontrol.TLC.TTLCChannel;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioFileMessageValue;

@SuppressLint("HandlerLeak")
public class TDataStreamPanel extends Activity {

	public static final int PARAMETERS_TYPE_OID 	= 1;
	public static final int PARAMETERS_TYPE_OIDX 	= 2;
	
	private boolean flExists = false;
	//.
	private String 	ServerAddress;
	private int 	ServerPort;
	//.
	private long 	UserID;
	private String 	UserPassword;
	//.
	private TCoGeoMonitorObject 	Object;
	//.
	private byte[] 				StreamDescriptorData;
	private TStreamDescriptor 	StreamDescriptor;
	private TChannelIDs			StreamChannels = null;
	//.
	private ArrayList<TStreamChannelConnectorAbstract> StreamChannelConnectors = new ArrayList<TStreamChannelConnectorAbstract>();
	//.
	private boolean IsInFront = false;
	//.
	private TextView	lbStatus;
	//.
	private LinearLayout llDeviceRotatorDVRT;
	private LinearLayout llTelecontrolTLC;
	private LinearLayout llAudioVC;
	
    public void onCreate(Bundle savedInstanceState) {
    	try {
            super.onCreate(savedInstanceState);
            //.
            Bundle extras = getIntent().getExtras(); 
            if (extras != null) {
            	int ParametersType = extras.getInt("ParametersType");
            	//.
            	ServerAddress = extras.getString("ServerAddress");
            	ServerPort = extras.getInt("ServerPort");
            	//.
            	UserID = extras.getLong("UserID");
            	UserPassword = extras.getString("UserPassword");
            	//.
        		TReflector Reflector = TReflector.GetReflector();
            	switch (ParametersType) {
            	
            	case PARAMETERS_TYPE_OID:
                	long ObjectID = extras.getLong("ObjectID");
                	Object = new TCoGeoMonitorObject(Reflector.Component.Server, ObjectID);
            		break; //. >
            		
            	case PARAMETERS_TYPE_OIDX:
                	int ObjectIndex = extras.getInt("ObjectIndex");
                	Object = Reflector.Component.CoGeoMonitorObjects.Items[ObjectIndex];
            		break; //. >
            	}
            	//.
            	StreamDescriptorData = extras.getByteArray("StreamDescriptorData");
            	StreamDescriptor = new TStreamDescriptor(StreamDescriptorData,com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.TChannelsProvider.Instance);
            	//.
            	byte[] StreamChannelsBA = extras.getByteArray("StreamChannels");
            	if (StreamChannelsBA != null) 
            		StreamChannels = new TChannelIDs(StreamChannelsBA);
            	else
            		StreamChannels = null;
            }
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		//.
            setContentView(R.layout.controlsmodule_datastream_panel);
            //.
            lbStatus = (TextView)findViewById(R.id.lbStatus);
            llDeviceRotatorDVRT = (LinearLayout)findViewById(R.id.llDeviceRotatorDVRT);
            llTelecontrolTLC = (LinearLayout)findViewById(R.id.llTelecontrolTLC);
            llAudioVC = (LinearLayout)findViewById(R.id.llAudioVC);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
		//. 
		flExists = true;
    }
	
    public void onDestroy() {
    	flExists = false;
    	//.
		super.onDestroy();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		IsInFront = false;
		//.
    	try {
    		StreamChannelConnectors_Finalize();
		} catch (Exception E) {
			DoOnException(E);
		}
	}

	protected void onResume() {
		super.onResume();
		IsInFront = true;
    	//.
    	try {
    		StreamChannelConnectors_Initialize();
		} catch (Exception E) {
			DoOnException(E);
		}
	}

	private void StreamChannelConnectors_Initialize() throws Exception {
		StreamChannelConnectors_Finalize();
		//.
		Layout_Reset();
		for (int I = 0; I < StreamDescriptor.Channels.size(); I++) {
			TStreamChannel Channel = (TStreamChannel)StreamDescriptor.Channels.get(I);
			if ((StreamChannels == null) || StreamChannels.IDExists(Channel.ID)) {
				TStreamChannelConnectorAbstract ChannelConnector = new TStreamChannelConnector(this, ServerAddress,ServerPort, UserID,UserPassword, Object, Channel, null/*Processor*/, new TStreamChannelConnectorAbstract.TOnProgressHandler(Channel) {
					
					@Override
					public void DoOnProgress(int ReadSize, TCanceller Canceller) {
						TDataStreamPanel.this.DoOnStatusMessage("");
					}
				}, new TStreamChannelConnectorAbstract.TOnIdleHandler(Channel) {
					
					@Override
					public void DoOnIdle(TCanceller Canceller) {
						TDataStreamPanel.this.DoOnStatusMessage(TDataStreamPanel.this.getString(R.string.SChannelIdle)+Channel.Name);
					}
				}, new TStreamChannelConnectorAbstract.TOnExceptionHandler(Channel) {
					
					@Override
					public void DoOnException(Exception E) {
						TDataStreamPanel.this.DoOnException(E);
					}
				});
				if (ChannelConnector != null) {
					StreamChannelConnectors.add(ChannelConnector);
					//.
					Layout_UpdateForChannel(Channel);
					//.
					Channel.Start();
					//.
					ChannelConnector.Start();
				}			  
			}
		}
	}
	
	private void StreamChannelConnectors_Finalize() throws Exception {
		for (int I = 0; I < StreamChannelConnectors.size(); I++) {
			TStreamChannelConnectorAbstract ChannelConnector = StreamChannelConnectors.get(I);
			//.
			ChannelConnector.Stop(false);
			//.
			ChannelConnector.Channel.Close();
			//.
			ChannelConnector.Destroy(false);
		}
		StreamChannelConnectors.clear();
		//.
		Layout_Reset();
	}
	
	private void Layout_Reset() {
		llAudioVC.setVisibility(View.GONE);
		llDeviceRotatorDVRT.setVisibility(View.GONE);
		llTelecontrolTLC.setVisibility(View.GONE);
	}
	
	private void Layout_UpdateForChannel(TStreamChannel Channel) {
		if (Channel instanceof TDVRTChannel) {
			final TDVRTChannel DVRTChannel = (TDVRTChannel)Channel;
			//.
			final SeekBar sbLatutude = (SeekBar)findViewById(R.id.sbDeviceRotatorDVRTLatutude);
			sbLatutude.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

				private TAsyncProcessing Processing = null;
				
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				public void onStopTrackingTouch(SeekBar seekBar) {
				}

				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (fromUser) {
						final int _Progress = progress;
						//.
						if (Processing != null)
							Processing.Cancel();
						Processing = new TAsyncProcessing() {
							@Override
							public void Process() throws Exception {
								DVRTChannel.DoOnLatitude(_Progress-180.0);
							}
							@Override
							public void DoOnCompleted() throws Exception {
							}
							@Override
							public void DoOnException(Exception E) {
								TDataStreamPanel.this.DoOnException(E);						
							}
						};
						Processing.Start();
					}
				}
			});
			final SeekBar sbLongitude = (SeekBar)findViewById(R.id.sbDeviceRotatorDVRTLongitude);
			sbLongitude.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

				private TAsyncProcessing Processing = null;
				
				public void onStartTrackingTouch(SeekBar seekBar) {
				}

				public void onStopTrackingTouch(SeekBar seekBar) {
				}

				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					if (fromUser) {
						final int _Progress = progress;
						//.
						if (Processing != null)
							Processing.Cancel();
						Processing = new TAsyncProcessing() {
							@Override
							public void Process() throws Exception {
								DVRTChannel.DoOnLongitude(_Progress-180.0);
							}
							@Override
							public void DoOnCompleted() throws Exception {
							}
							@Override
							public void DoOnException(Exception E) {
								TDataStreamPanel.this.DoOnException(E);						
							}
						};
						Processing.Start();
					}
				}
			});
			//.
			llDeviceRotatorDVRT.setVisibility(View.VISIBLE);
			//.
			return; //. ->
		}
		if (Channel instanceof TTLCChannel) {
			final TTLCChannel TLCChannel = (TTLCChannel)Channel;
			//.
			TableLayout tlTelecontrolTLC = (TableLayout)findViewById(R.id.tlTelecontrolTLC);
			//.
			int TableTextSize = 18;
			//. channel header
			TableRow Row = new TableRow(this);
			//.
			TableRow.LayoutParams RowParams = new TableRow.LayoutParams();
			RowParams.height = TableRow.LayoutParams.WRAP_CONTENT;
			RowParams.width = TableRow.LayoutParams.MATCH_PARENT;
			//.
			TableRow.LayoutParams ColParams = new TableRow.LayoutParams();
			ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
			ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
			ColParams.gravity = Gravity.CENTER;
			//.			
			TextView Col = new TextView(this);
			String S = TLCChannel.Name;
			if (TLCChannel.Info.length() > 0)
				S += " "+"/"+TLCChannel.Info+"/";
			Col.setText(S);
			Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
			Col.setTextColor(0xFF0E4E26);
			Col.setTypeface(null, Typeface.BOLD);
			//.
			Row.addView(Col, ColParams);
			//.
			Row.setGravity(Gravity.CENTER);
			tlTelecontrolTLC.addView(Row, RowParams);		
			//.
			int DataTypesCount = ((TLCChannel.DataTypes != null) ? TLCChannel.DataTypes.Items.size() : 0);
			final View[] ValueControls = new View[DataTypesCount];  
			if (DataTypesCount > 0) {
				int Cnt = TLCChannel.DataTypes.Items.size();
				for (int I = 0; I < Cnt; I++) {
					final TDataType DataType = TLCChannel.DataTypes.Items.get(I); 
					//. row
					Row = new TableRow(this);
					//.
					RowParams = new TableRow.LayoutParams();
					RowParams.height = TableRow.LayoutParams.WRAP_CONTENT;
					RowParams.width = TableRow.LayoutParams.MATCH_PARENT;
					//.
					ColParams = new TableRow.LayoutParams();
					ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
					ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
					ColParams.weight = 1.0f;
					ColParams.gravity = Gravity.LEFT;
					//.			
					Col = new TextView(this);
					Col.setText(DataType.GetName(this));
					Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
					Col.setTextColor(Color.BLACK);
					//.
					Row.addView(Col, ColParams);
					//.
					tlTelecontrolTLC.addView(Row, RowParams);		
					//.
					Row = new TableRow(this);
					//.
					ColParams = new TableRow.LayoutParams();
					ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
					ColParams.width = TableRow.LayoutParams.WRAP_CONTENT;
					ColParams.weight = 1.0f;
					//.
					SeekBar _SeekBar = new SeekBar(this);
					_SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

						private TDataType MyDataType = DataType;
						//.
						private TAsyncProcessing Processing = null;
						
						public void onStartTrackingTouch(SeekBar seekBar) {
						}

						public void onStopTrackingTouch(SeekBar seekBar) {
						}

						public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
							if (fromUser) {
								final int _Progress = progress;
								//.
								if (Processing != null)
									Processing.Cancel();
								Processing = new TAsyncProcessing() {
									@Override
									public void Process() throws Exception {
										Double V = Double.valueOf(_Progress);
										MyDataType.ContainerType.SetValue(V);
										//.
										TLCChannel.DoOnData(MyDataType);
									}
									@Override
									public void DoOnCompleted() throws Exception {
									}
									@Override
									public void DoOnException(Exception E) {
										TDataStreamPanel.this.DoOnException(E);						
									}
								};
								Processing.Start();
							}
						}
					});
					//.
					Row.addView(_SeekBar, ColParams);
					ValueControls[I] = _SeekBar; 
					//.
					tlTelecontrolTLC.addView(Row, RowParams);		
				}
			}
			//.
			llTelecontrolTLC.setVisibility(View.VISIBLE);
			//.
			return; //. ->
		}
		if (Channel instanceof TVCChannel) {
			final TVCChannel VCChannel = (TVCChannel)Channel;
			//.
			Button btnAudioVCDoVoiceCommand = (Button)findViewById(R.id.btnAudioVCDoVoiceCommand);
			btnAudioVCDoVoiceCommand.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
			    	AlertDialog.Builder alert = new AlertDialog.Builder(TDataStreamPanel.this);
			    	//.
			    	alert.setTitle(R.string.SPlayAudioMessage);
			    	alert.setMessage(R.string.SEnterIDOfTheMessage);
			    	//.
			    	final EditText input = new EditText(TDataStreamPanel.this);
			    	alert.setView(input);
			    	//.
			    	alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
			    		
			    		@Override
			        	public void onClick(DialogInterface dialog, int whichButton) {
			    			//. hide keyboard
			        		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			        		imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			        		//.
			        		final String FileMessageID = input.getText().toString();
			        		try {
					    		TAsyncProcessing Processing = new TAsyncProcessing(TDataStreamPanel.this,getString(R.string.SWaitAMoment)) {
					    			
					    			@Override
					    			public void Process() throws Exception {
										TAudioFileMessageValue Value = new TAudioFileMessageValue();
										Value.TimeStamp = OleDate.UTCCurrentTimestamp();
										try {
											Value.FileID = Integer.parseInt(FileMessageID);
											Value.FileName = null;
										}
										catch (NumberFormatException NFE) {
											Value.FileID = 0;
											Value.FileName = FileMessageID;
										}
										Value.DestinationID = 1;
										Value.Volume = 100; //. %
										Value.RepeatCount = -1; //. minus means that there is no waiting for play finish
										Value.RepeatInterval = 0;
										//.
										VCChannel.DoVoiceCommand(Value.ToByteArray());
					    			}
					    			
					    			@Override 
					    			public void DoOnCompleted() throws Exception {
					    			}
					    			
					    			@Override
					    			public void DoOnException(Exception E) {
					    				Toast.makeText(TDataStreamPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					    			}
					    		};
					    		Processing.Start();
			        		}
			        		catch (Exception E) {
			        			Toast.makeText(TDataStreamPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			        			return; //. ->
			    		    }
			          	}
			    	});
			    	//.
			    	alert.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
			    		@Override
			    		public void onClick(DialogInterface dialog, int whichButton) {
			    		}
			    	});
			    	//.
			    	alert.show();    
				}
			});
			//.
			Button btnAudioVCSetVoiceCommands = (Button)findViewById(R.id.btnAudioVCSetVoiceCommands);
			btnAudioVCSetVoiceCommands.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(TDataStreamPanel.this, ".ZIP", new TFileSystemFileSelector.OpenDialogListener() {
			        	
			            @Override
			            public void OnSelectedFile(String fileName) {
		                    final File ChosenFile = new File(fileName);
		                    //.
							try {
					    		TAsyncProcessing Processing = new TAsyncProcessing(TDataStreamPanel.this,getString(R.string.SWaitAMoment)) {
					    			
					    			@Override
					    			public void Process() throws Exception {
										VCChannel.SetVoiceCommands(TFileSystem.File_ToByteArray(ChosenFile.getAbsolutePath()));
					    			}
					    			
					    			@Override 
					    			public void DoOnCompleted() throws Exception {
					        			Toast.makeText(TDataStreamPanel.this, R.string.SAudioFilesHaveBeenImportedToTheDevice, Toast.LENGTH_LONG).show();  						
					    			}
					    			
					    			@Override
					    			public void DoOnException(Exception E) {
					    				Toast.makeText(TDataStreamPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					    			}
					    		};
					    		Processing.Start();
							}
							catch (Throwable E) {
								String S = E.getMessage();
								if (S == null)
									S = E.getClass().getName();
			        			Toast.makeText(TDataStreamPanel.this, S, Toast.LENGTH_LONG).show();  						
							}
			            }

						@Override
						public void OnCancel() {
						}
			        });
			    	FileSelector.show();    	
				}
			});
			//.
			llAudioVC.setVisibility(View.VISIBLE);
			//.
			return; //. ->
		}
	}
	
	private static final int MESSAGE_SHOWSTATUSMESSAGE 	= 1;
	private static final int MESSAGE_SHOWEXCEPTION 		= 2;
	private static final int MESSAGE_EDITTEXT_WRITE 	= 3;
	
	public static class TEditTextString {
		
		public EditText ET;
		public String S;
		
		public TEditTextString(EditText pET, String pS) {
			ET = pET;
			S = pS;
		}
	}
	
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_SHOWEXCEPTION:
					if (!flExists)
						break; // . >
    				Throwable E = (Throwable)msg.obj;
    				String EM = E.getMessage();
    				if (EM == null) 
    					EM = E.getClass().getName();
    				//.
    				Toast.makeText(TDataStreamPanel.this,EM,Toast.LENGTH_LONG).show();
    				// .
    				break; // . >

    			case MESSAGE_SHOWSTATUSMESSAGE:
					if (!flExists)
						break; // . >
    				String S = (String)msg.obj;
    				//.
    				if (S.length() > 0) {
    					lbStatus.setText(S);
    					lbStatus.setVisibility(View.VISIBLE);
    				}
    				else {
    					lbStatus.setText("");
    					lbStatus.setVisibility(View.GONE);
    				}
    				// .
    				break; // . >

    			case MESSAGE_EDITTEXT_WRITE:
					if (!flExists)
						break; // . >
    				TEditTextString ETS = (TEditTextString)msg.obj;
    				//.
    				ETS.ET.setText(ETS.S);
    				//.
    				break; // . >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	private void DoOnStatusMessage(String S) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWSTATUSMESSAGE,S).sendToTarget();
	}
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
	
	@SuppressWarnings("unused")
	private void DoOnEditTextMessage(EditText ET, String Message) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_EDITTEXT_WRITE,new TEditTextString(ET, Message)).sendToTarget();
	}
}
