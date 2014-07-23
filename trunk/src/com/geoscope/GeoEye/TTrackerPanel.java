package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOIDataFileSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOIJPEGImageSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOITextSO;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIDataFileValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIImageValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOITextValue;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TProgressor;
import com.geoscope.Utils.TFileSystem;
import com.geoscope.Utils.TUIDGenerator;

@SuppressLint({ "HandlerLeak", "HandlerLeak" })
public class TTrackerPanel extends Activity {

	public static final int SHOW_NEWPOIPANEL			= 1;
	public static final int SHOW_LASTPOICAMERA 			= 2;
	public static final int SHOW_LASTPOITEXTEDITOR		= 3;
	public static final int SHOW_LASTPOIVIDEOCAMERA	 	= 4;
	public static final int SHOW_LASTPOIVIDEOCAMERA1 	= 5;
	public static final int SHOW_LASTPOIDRAWINGEDITOR 	= 6;
	public static final int SHOW_LASTPOIFILEDIALOG 		= 7;
	//.
	public static final int LOG_MENU = 1;
	public static final int POI_SUBMENU = 100; 
	public static final int POI_SUBMENU_NEWPOI = 101; 
	public static final int POI_SUBMENU_ADDTEXT = 102; 
	public static final int POI_SUBMENU_ADDIMAGE = 103; 
	public static final int POI_SUBMENU_ADDVIDEO = 104; 
    
    public static class TCurrentFixObtaining extends TCancelableThread {

    	public static class TDoOnFixIsObtainedHandler {
    		
    		public void DoOnFixIsObtained(TGPSFixValue Fix) {
    		}
    	}
    	
    	private static final int MESSAGE_EXCEPTION = 0;
    	private static final int MESSAGE_COMPLETED = 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
    	
    	private class TObtainProgressor extends TProgressor {
    		@Override
    		public synchronized boolean DoOnProgress(int Percentage) {
				if (super.DoOnProgress(Percentage)) {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,this.Percentage).sendToTarget();
					return true; //. ->
				}
				else 
					return false; //. -> 
    		}
    	}
    	
    	private Context context;
    	private TGPSModule GPSModule;
    	private TDoOnFixIsObtainedHandler DoOnFixIsObtainedHandler;
    	//. 
    	private TGPSFixValue CurrentFix = null;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TCurrentFixObtaining(Context pcontext, TGPSModule pGPSModule, TDoOnFixIsObtainedHandler pDoOnFixIsObtainedHandler) {
    		context = pcontext;
    		GPSModule = pGPSModule;
    		DoOnFixIsObtainedHandler = pDoOnFixIsObtainedHandler;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				CurrentFix = GPSModule.ObtainCurrentFix(Canceller, new TObtainProgressor(), true);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_COMPLETED,CurrentFix).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(context, context.getString(R.string.SErrorOfGettingCoordinates)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
						if (Canceller.flCancel)
			            	break; //. >
						//.
		            	TGPSFixValue Fix = (TGPSFixValue)msg.obj;
		            	if (DoOnFixIsObtainedHandler != null)
		            		DoOnFixIsObtainedHandler.DoOnFixIsObtained(Fix);
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(context);    
		            	progressDialog.setMessage(context.getString(R.string.SCoordinatesGettingForNow));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, context.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
		            		} 
		            	}); 
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		                if (progressDialog.isShowing()) 
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
    
    public static class TCurrentPositionObtaining extends TCancelableThread {

    	public static class TDoOnPositionIsObtainedHandler {
    		
    		public void DoOnPositionIsObtained(TXYCoord Crd) {
    		}
    	}
    	
    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_COMPLETED 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private TTrackerPanel context;
    	private TGPSModule GPSModule;
    	private TReflector Reflector;
    	private TDoOnPositionIsObtainedHandler DoOnPositionIsObtainedHandler;
        private ProgressDialog progressDialog; 
    	
    	public TCurrentPositionObtaining(TTrackerPanel pcontext, TGPSModule pGPSModule, TReflector pReflector, TDoOnPositionIsObtainedHandler pDoOnPositionIsObtainedHandler) {
    		context = pcontext;
    		GPSModule = pGPSModule;
    		Reflector = pReflector;
    		DoOnPositionIsObtainedHandler = pDoOnPositionIsObtainedHandler;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			TXYCoord Crd;
    			try {
    		    	TGPSFixValue Fix;
    				Fix = GPSModule.GetCurrentFix();
    				if (!Fix.IsSet()) 
    					throw new Exception(context.getString(R.string.SCurrentPositionIsUnavailable)); //. =>
    				if (Fix.IsEmpty()) 
    					throw new Exception(context.getString(R.string.SCurrentPositionIsUnknown)); //. =>
    				Crd = Reflector.ConvertGeoCoordinatesToXY(TGPSModule.DatumID, Fix.Latitude,Fix.Longitude,Fix.Altitude);
    				//.
    				Thread.sleep(100);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_COMPLETED,Crd).sendToTarget();
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
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
						if (Canceller.flCancel)
			            	break; //. >
						//.
		            	TXYCoord Crd = (TXYCoord)msg.obj;
		            	if (DoOnPositionIsObtainedHandler != null)
		            		DoOnPositionIsObtainedHandler.DoOnPositionIsObtained(Crd);
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(context);    
		            	progressDialog.setMessage(context.getString(R.string.SWaitAMoment));    
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
		                if (context.flVisible && progressDialog.isShowing()) 
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
    
    public boolean flExists = false;
    //.
	private Timer Updater;
	private Menu MainMenu;
	private TextView lbTitle;
	private ToggleButton tbTrackerIsOn;
    private EditText edFix;
    private EditText edFixSpeed;
    private EditText edFixPrecision;
    private Button btnObtainCurrentFix;
    private Button btnShowLocation;	
    private Button btnNewPOI;	
    private Button btnAddPOIText;	
    private Button btnAddPOIImage;	
    private Button btnAddPOIVideo;	
    private Button btnAddPOIDrawing;	
    private Button btnAddPOIFile;	
	private ToggleButton tbAlarm;
    private EditText edConnectorInfo;
    private Button btnConnectorCommands;	
    private EditText edCheckpoint;
    private EditText edOpQueueTransmitInterval;
    private EditText edPositionReadInterval;
    private CheckBox cbIgnoreImpulseModeSleepingOnMovement;
    private EditText edGeoThreshold;
    private EditText edOpQueue;
    private Button btnOpQueueCommands;	
    private EditText edComponentFileStreaming;
    private Button btnComponentFileStreamingCommands;
    //.
    private boolean flVisible = false;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
		if ((android.os.Build.VERSION.SDK_INT < 14) || ViewConfiguration.get(this).hasPermanentMenuKey()) { 
			requestWindowFeature(Window.FEATURE_NO_TITLE);
		}
		//.
        setContentView(R.layout.tracker_panel);
        //.
        tbTrackerIsOn = (ToggleButton)findViewById(R.id.tbTrackerIsOn);
        tbTrackerIsOn.setTextOn(getString(R.string.STrackerIsON));
        tbTrackerIsOn.setTextOff(getString(R.string.STrackerIsOff));
        tbTrackerIsOn.setChecked(TTracker.TrackerIsEnabled());
        tbTrackerIsOn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((ToggleButton)arg0).setChecked(!((ToggleButton)arg0).isChecked());
			}
		});
        tbTrackerIsOn.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View arg0) {
    			try {
    				((ToggleButton)arg0).setChecked(!((ToggleButton)arg0).isChecked());
    				//.
    				boolean flOn = ((ToggleButton)arg0).isChecked();
					TTracker.EnableDisableTracker(flOn);
					EnableDisablePanelItems(flOn);
					//.
					UpdateInfo();
					//.
					if (!flOn) {
						Thread.sleep(333); 
						TTrackerPanel.this.finish();
					}
    	    	}
    	    	catch (Exception E) {
    	            Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.STrackerError)+E.getMessage(), Toast.LENGTH_LONG).show();
    	    	}
    	    	return true; //. ->
			}
		});
        //.
        lbTitle = (TextView)findViewById(R.id.lbTitle);
        edFix = (EditText)findViewById(R.id.edFix);
        edFixSpeed = (EditText)findViewById(R.id.edFixSpeed);
        edFixPrecision = (EditText)findViewById(R.id.edFixPrecision);
        btnObtainCurrentFix = (Button)findViewById(R.id.btnObtainCurrentFix);
        btnObtainCurrentFix.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	StartObtainingCurrentFix();
            }
        });
        btnShowLocation = (Button)findViewById(R.id.btnShowLocation);
        btnShowLocation.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	StartObtainingCurrentPosition();
            }
        });
        btnNewPOI = (Button)findViewById(R.id.btnNewPOI);
        btnNewPOI.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOIPanel.class);
                startActivityForResult(intent,SHOW_NEWPOIPANEL);
            }
        });
        btnAddPOIText = (Button)findViewById(R.id.btnAddPOIText);
        btnAddPOIText.setEnabled(false);
        btnAddPOIText.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOITextPanel.class);
                startActivityForResult(intent,SHOW_LASTPOITEXTEDITOR);
            }
        });
        btnAddPOIImage = (Button)findViewById(R.id.btnAddPOIImage);
        btnAddPOIImage.setEnabled(false);
        btnAddPOIImage.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
      		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.getImageTempFile(TTrackerPanel.this))); 
      		    startActivityForResult(intent, SHOW_LASTPOICAMERA);    		
            }
        });
        btnAddPOIVideo = (Button)findViewById(R.id.btnAddPOIVideo);
        btnAddPOIVideo.setEnabled(false);
        btnAddPOIVideo.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
      		    Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.getVideoTempFile(TTrackerPanel.this))); 
      		    startActivityForResult(intent, SHOW_LASTPOIVIDEOCAMERA1);    		
            }
        });
        btnAddPOIVideo.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOIVideoPanel.class);
                startActivityForResult(intent,SHOW_LASTPOIVIDEOCAMERA);
				return true;
			}
        });
        btnAddPOIDrawing = (Button)findViewById(R.id.btnAddPOIDrawing);
        btnAddPOIDrawing.setEnabled(false);
        btnAddPOIDrawing.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				Intent intent = new Intent(TTrackerPanel.this, TDrawingEditor.class);
	    		File F = getDrawingTempFile(TTrackerPanel.this);
	    		F.delete();
	  		    intent.putExtra("FileName", F.getAbsolutePath()); 
	  		    intent.putExtra("ReadOnly", false); 
	  		    startActivityForResult(intent, SHOW_LASTPOIDRAWINGEDITOR);    		
            }
        });
        btnAddPOIFile = (Button)findViewById(R.id.btnAddPOIFile);
        btnAddPOIFile.setEnabled(false);
        btnAddPOIFile.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				final String[] FileList;
				final File FileSelectorPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
			    if(FileSelectorPath.exists()) {
			        FilenameFilter filter = new FilenameFilter() {
			        	@Override
			            public boolean accept(File dir, String filename) {
			                return (!(new File(dir.getAbsolutePath()+"/"+filename)).isDirectory());
			            }
			        };
			        FileList = FileSelectorPath.list(filter);
			    }
			    else 
			        FileList= new String[0];
			    //.
			    AlertDialog.Builder builder = new AlertDialog.Builder(TTrackerPanel.this);
		        builder.setTitle(R.string.SChooseFile);
		        builder.setItems(FileList, new DialogInterface.OnClickListener() {
		        	
		            public void onClick(DialogInterface dialog, int which) {
		                File ChosenFile = new File(FileSelectorPath.getAbsolutePath()+"/"+FileList[which]);
		                //.
						try {
		            		long DataFileSize = EnqueueFileDataFile(ChosenFile.getAbsolutePath());
					    	//.
		            		Toast.makeText(TTrackerPanel.this, getString(R.string.SFileIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
						}
						catch (Throwable E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, S, Toast.LENGTH_LONG).show();  						
						}
		            }
		        });
		        Dialog FileDialog = builder.create();
		        FileDialog.show();	    						
            }
        });
        tbAlarm = (ToggleButton)findViewById(R.id.tbAlarm);
        tbAlarm.setChecked(GetAlarm() > 0);
        tbAlarm.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((ToggleButton)arg0).setChecked(!((ToggleButton)arg0).isChecked());
			}
		});
        tbAlarm.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View arg0) {
    			try {
    				((ToggleButton)arg0).setChecked(!((ToggleButton)arg0).isChecked());
    				SetAlarm(((ToggleButton)arg0).isChecked());
    	    	}
    	    	catch (Exception E) {
    	            Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.STrackerError)+E.getMessage(), Toast.LENGTH_LONG).show();
    	    	}
    	    	return true;
			}
		});
        edConnectorInfo = (EditText)findViewById(R.id.edConnectorInfo);
        btnConnectorCommands = (Button)findViewById(R.id.btnConnectorCommands);
        btnConnectorCommands.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		final CharSequence[] _items;
    			_items = new CharSequence[2];
    			_items[0] = getString(R.string.SReconnect);
    			_items[1] = getString(R.string.SForceReconnect);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TTrackerPanel.this);
        		builder.setTitle(R.string.SQueueOperations);
        		builder.setNegativeButton(TTrackerPanel.this.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
					    	TTracker Tracker = TTracker.GetTracker();
					    	if (Tracker == null)
					    		throw new Exception(TTrackerPanel.this.getString(R.string.STrackerIsNotInitialized)); //. =>
					    	//.
	    					switch (arg1) {
	    					
	    					case 0:
    					    	Tracker.GeoLog.ConnectorModule.Reconnect();
    	                		Toast.makeText(TTrackerPanel.this, R.string.SDone, Toast.LENGTH_SHORT).show();
    	                		break; //. >
    						
	    					case 1:
    					    	Tracker.GeoLog.ConnectorModule.ForceReconnect();
    	                		Toast.makeText(TTrackerPanel.this, R.string.SDone, Toast.LENGTH_SHORT).show();
	    						break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
						//.
						arg0.dismiss();
        			}
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
            }
        });
        edCheckpoint = (EditText)findViewById(R.id.edCheckpoint);
        edOpQueueTransmitInterval = (EditText)findViewById(R.id.edOpQueueTransmitInterval);
        edPositionReadInterval = (EditText)findViewById(R.id.edPositionReadInterval);
        cbIgnoreImpulseModeSleepingOnMovement =  (CheckBox)findViewById(R.id.cbIgnoreImpulseModeSleepingOnMovement);
        cbIgnoreImpulseModeSleepingOnMovement.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			}
		});
        cbIgnoreImpulseModeSleepingOnMovement.setOnLongClickListener(new OnLongClickListener() {			
			@Override
			public boolean onLongClick(View arg0) {
            	try {
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(TTrackerPanel.this.getString(R.string.STrackerIsNotInitialized)); //. =>
			    	((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			    	Tracker.GeoLog.GPSModule.flIgnoreImpulseModeSleepingOnMovement = ((CheckBox)arg0).isChecked();
			    	Tracker.GeoLog.SaveProfile();
				}
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
				}
    	    	return true;
			}
		});
        edGeoThreshold = (EditText)findViewById(R.id.edGeoThreshold);
        edOpQueue = (EditText)findViewById(R.id.edOpQueue);
        btnOpQueueCommands = (Button)findViewById(R.id.btnOpQueueCommands);
        btnOpQueueCommands.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		final CharSequence[] _items;
    			_items = new CharSequence[3];
    			_items[0] = getString(R.string.STransmiteImmediately);
    			_items[1] = getString(R.string.SSave);
    			_items[2] = getString(R.string.SClear);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TTrackerPanel.this);
        		builder.setTitle(R.string.SQueueOperations);
        		builder.setNegativeButton(TTrackerPanel.this.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
					    	TTracker Tracker = TTracker.GetTracker();
					    	if (Tracker == null)
					    		throw new Exception(TTrackerPanel.this.getString(R.string.STrackerIsNotInitialized)); //. =>
					    	//.
	    					switch (arg1) {
	    					
	    					case 0:
    					    	Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
    	                		Toast.makeText(TTrackerPanel.this, R.string.SImmediateTransmissionStarted, Toast.LENGTH_SHORT).show();
    	                		break; //. >
    						
	    					case 1:
	    					    	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.Save();
	    	                		Toast.makeText(TTrackerPanel.this, R.string.SQueueIsSavedToDisk, Toast.LENGTH_SHORT).show();
	    						break; //. >
	    						
	    					case 2:
    					    	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.Clear();
    					    	UpdateInfo();
    	                		Toast.makeText(TTrackerPanel.this, R.string.SQueueIsCleared, Toast.LENGTH_SHORT).show();
	    						break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
						//.
						arg0.dismiss();
        			}
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
            }
        });
        edComponentFileStreaming = (EditText)findViewById(R.id.edComponentFileStreaming);
        btnComponentFileStreamingCommands = (Button)findViewById(R.id.btnComponentFileStreamingCommands);
        btnComponentFileStreamingCommands.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		final CharSequence[] _items;
    			_items = new CharSequence[5];
    			_items[0] = getString(R.string.SSuspend);
    			_items[1] = getString(R.string.SResume);
    			_items[2] = getString(R.string.STransmiteImmediately);
    			_items[3] = getString(R.string.SRemoveLastItem);
    			_items[4] = getString(R.string.SClear);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TTrackerPanel.this);
        		builder.setTitle(R.string.SQueueOperations);
        		builder.setNegativeButton(TTrackerPanel.this.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
					    	TTracker Tracker = TTracker.GetTracker();
					    	if (Tracker == null)
					    		throw new Exception(TTrackerPanel.this.getString(R.string.STrackerIsNotInitialized)); //. =>
					    	//.
	    					switch (arg1) {
	    					
	    					case 0:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							Tracker.GeoLog.ComponentFileStreaming.SetEnabledStreaming(false);
    	                			Toast.makeText(TTrackerPanel.this, R.string.SStreamingHasBeenSuspended, Toast.LENGTH_SHORT).show();
	    						}
    	                		break; //. >
    						
	    					case 1:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							Tracker.GeoLog.ComponentFileStreaming.SetEnabledStreaming(true);
    	                			Toast.makeText(TTrackerPanel.this, R.string.SStreamingHasBeenResumed, Toast.LENGTH_SHORT).show();
	    						}
    	                		break; //. >
    						
	    					case 2:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							Tracker.GeoLog.ComponentFileStreaming.Process();
    	                			Toast.makeText(TTrackerPanel.this, R.string.SImmediateTransmissionStarted, Toast.LENGTH_SHORT).show();
	    						}
    	                		break; //. >
    						
	    					case 3:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							Tracker.GeoLog.ComponentFileStreaming.RemoveLastItem();
    					    		UpdateInfo();
	    	                		Toast.makeText(TTrackerPanel.this, R.string.SLastItemHasBeenRemoved, Toast.LENGTH_SHORT).show();
    							}    							
	    						break; //. >
	    						
	    					case 4:
	    						if (Tracker.GeoLog.ComponentFileStreaming != null) {
	    							Tracker.GeoLog.ComponentFileStreaming.Clear();
	    					    	UpdateInfo();
    		                		Toast.makeText(TTrackerPanel.this, R.string.SQueueIsCleared, Toast.LENGTH_SHORT).show();
    							}    							
	    						break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerPanel.this, TTrackerPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
						//.
						arg0.dismiss();
        			}
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
            }
        });
        //.
        EnableDisablePanelItems(TTracker.TrackerIsEnabled());
        //.
        setResult(Activity.RESULT_CANCELED);
        //.
        flExists = true;
        //.
        Updater = new Timer();
        Updater.schedule(new TUpdaterTask(this),100,1000);
	}

    @Override
	protected void onDestroy() {
    	flExists = false;
    	//.
        if (Updater != null) {
        	Updater.cancel();
        	Updater = null;
        }
        //.
		super.onDestroy();
	}

    @Override
    public void onResume() {
    	super.onResume();
    	//.
        flVisible = true;
        //. start tracker position fixing immediately if it is in impulse mode
        TTracker Tracker = TTracker.GetTracker();
    	if ((Tracker != null) && (Tracker.GeoLog.GPSModule != null) && Tracker.GeoLog.GPSModule.IsEnabled() && Tracker.GeoLog.GPSModule.flImpulseMode) 
			Tracker.GeoLog.GPSModule.LocationMonitor.flProcessImmediately = true;
    }

    @Override
    public void onPause() {
    	super.onPause();
    	//.
    	flVisible = false;
    }    
    
    private TReflector Reflector() throws Exception {
    	TReflector Reflector = TReflector.GetReflector();
    	if (Reflector == null)
    		throw new Exception(getString(R.string.SReflectorIsNull)); //. =>
		return Reflector;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tracker_panel_menu, menu);
        //.
        menu.add(Menu.NONE,LOG_MENU,Menu.NONE,R.string.SLog);
        //.
        /*SubMenu fileMenu = menu.addSubMenu(1,POI_SUBMENU,1,R.string.SPOI);
        fileMenu.add(1, POI_SUBMENU_NEWPOI, 0, R.string.SNewPOI);
        fileMenu.add(1, POI_SUBMENU_ADDTEXT, 1, R.string.SAddText);
        fileMenu.add(1, POI_SUBMENU_ADDIMAGE, 1, R.string.SAddImage);
        fileMenu.add(1, POI_SUBMENU_ADDVIDEO, 1, R.string.SAddVideo);*/
        //.
        MainMenu = menu;
        //.
        MainMenu.setGroupEnabled(1,TTracker.TrackerIsEnabled());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
    	case LOG_MENU:
    		Intent intent = new Intent(this, TTrackerLogPanel.class);
            startActivity(intent);
    		//.
    		return true; //. >
    
    	case POI_SUBMENU_NEWPOI:
    		intent = new Intent(this, TTrackerPOIPanel.class);
            startActivityForResult(intent,SHOW_NEWPOIPANEL);
    		//.
    		return true; //. >
    
    	case POI_SUBMENU_ADDTEXT:
    		intent = new Intent(this, TTrackerPOITextPanel.class);
            startActivityForResult(intent,SHOW_LASTPOITEXTEDITOR);
    		//.
    		return true; //. >

    	case POI_SUBMENU_ADDIMAGE:
  		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getImageTempFile(this))); 
  		    startActivityForResult(intent, SHOW_LASTPOICAMERA);    		
    		//.
    		return true; //. >

    	case POI_SUBMENU_ADDVIDEO:
    		intent = new Intent(this, TTrackerPOIVideoPanel.class);
            startActivityForResult(intent,SHOW_LASTPOIVIDEOCAMERA);
    		//.
    		return true; //. >
        }
        return false;
    }
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case SHOW_NEWPOIPANEL: 
        	if (resultCode == RESULT_OK) {  
                btnAddPOIText.setEnabled(true);
                btnAddPOIImage.setEnabled(true);
                btnAddPOIVideo.setEnabled(true);
                btnAddPOIDrawing.setEnabled(true);
                btnAddPOIFile.setEnabled(true);
        	}  
            break; //. >
        
        case SHOW_LASTPOITEXTEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	String POIText = extras.getString("Text");
                	try {
                		if (POIText.equals(""))
                			throw new Exception(getString(R.string.STextIsNull)); //. =>
                		//.
                		byte[] TextBA = POIText.getBytes("windows-1251");
                		//.
	                	double Timestamp = OleDate.UTCCurrentTimestamp();
                		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_Text.txt";
                		File NF = new File(NFN);
                		FileOutputStream FOS = new FileOutputStream(NF);
                		try {
                			FOS.write(TextBA);
                		}
                		finally {
                			FOS.close();
                		}
                		//. last version: POI_AddText(Timestamp,NFN);
                		POI_AddDataFile(Timestamp,NFN);
                		//.
                		Toast.makeText(this, getString(R.string.STextIsAdded)+Integer.toString(TextBA.length), Toast.LENGTH_LONG).show();
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >

        case SHOW_LASTPOICAMERA: 
        	if (resultCode == RESULT_OK) {  
				File F = getImageTempFile(this);
				if (F.exists()) {
					try {
						//. try to gc
						TGeoLogApplication.Instance().GarbageCollector.Collect();
						//.
				    	TTracker Tracker = TTracker.GetTracker();
				    	if (Tracker == null)
				    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
				    	//.
						FileInputStream fs = new FileInputStream(F);
						try
						{
							byte[] PictureBA;
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inDither=false;
							options.inPurgeable=true;
							options.inInputShareable=true;
							options.inTempStorage=new byte[1024*1024*3]; 							
							Rect rect = new Rect();
							Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), rect, options);
							try {
								int ImageMaxSize = options.outWidth;
								if (options.outHeight > ImageMaxSize)
									ImageMaxSize = options.outHeight;
								float MaxSize = Tracker.GeoLog.GPSModule.MapPOIConfiguration.Image_ResX;
								float Scale = MaxSize/ImageMaxSize; 
								Matrix matrix = new Matrix();     
								matrix.postScale(Scale,Scale);
								//.
								Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
								try {
									ByteArrayOutputStream bos = new ByteArrayOutputStream();
									try {
										if (!resizedBitmap.compress(CompressFormat.JPEG, Tracker.GeoLog.GPSModule.MapPOIConfiguration.Image_Quality, bos)) 
											throw new Exception(getString(R.string.SErrorOfSavingJPEG)); //. =>
										PictureBA = bos.toByteArray();
									}
									finally {
										bos.close();
									}
								}
								finally {
									resizedBitmap.recycle();
								}
							}
							finally {
								bitmap.recycle();
							}
							//.
		                	double Timestamp = OleDate.UTCCurrentTimestamp();
	                		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_Image.jpg";
	                		File NF = new File(NFN);
	                		FileOutputStream FOS = new FileOutputStream(NF);
	                		try {
	                			FOS.write(PictureBA);
	                		}
	                		finally {
	                			FOS.close();
	                		}
							//. last version: POI_AddImage(Timestamp,NFN);
	                		POI_AddDataFile(Timestamp,NFN);
				        	//.
				        	Toast.makeText(this, getString(R.string.SImageIsAdded)+Integer.toString(PictureBA.length), Toast.LENGTH_LONG).show();
						}
						finally
						{
							fs.close();
						}
					}
					catch (Throwable E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
	        			Toast.makeText(this, S, Toast.LENGTH_LONG).show();  						
					}
				}
				else
        			Toast.makeText(this, R.string.SImageWasNotPrepared, Toast.LENGTH_SHORT).show();  
        	}  
            break; //. >

        case SHOW_LASTPOIVIDEOCAMERA: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	double Timestamp = OleDate.UTCCurrentTimestamp();
                	String FileName = extras.getString("FileName");
                	try {
                		File F = new File(FileName);
                		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_"+F.getName();
                		File NF = new File(NFN);
                		F.renameTo(NF);
                		FileName = NFN;
                		//.
                		POI_AddDataFile(Timestamp,FileName);
                		//.
                		long DataSize = 0;
                		F = new File(FileName);
                		if (F.exists())
                			DataSize = F.length();
                		Toast.makeText(this, getString(R.string.SDataIsAdded)+Integer.toString((int)(DataSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >

        case SHOW_LASTPOIVIDEOCAMERA1: 
        	if (resultCode == RESULT_OK) {  
            	try {
                	double Timestamp = OleDate.UTCCurrentTimestamp();
    				File F = getVideoTempFile(this);
    				if (F.exists()) {
						//. try to gc
						TGeoLogApplication.Instance().GarbageCollector.Collect();
						//.
	            		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_"+F.getName();
	            		File NF = new File(NFN);
	            		F.renameTo(NF);
	            		String FileName = NFN;
	            		//.
	            		POI_AddDataFile(Timestamp,FileName);
	            		//.
	            		long DataSize = 0;
	            		F = new File(FileName);
	            		if (F.exists())
	            			DataSize = F.length();
	            		Toast.makeText(this, getString(R.string.SDataIsAdded)+Integer.toString((int)(DataSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
    				}
    				else
            			Toast.makeText(this, R.string.SVideoWasNotPrepared, Toast.LENGTH_SHORT).show();  
				}
				catch (Exception E) {
        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
			}
            break; //. >

        case SHOW_LASTPOIDRAWINGEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	String DrawingFileName = extras.getString("FileName");
                	try {
	                	double Timestamp = OleDate.UTCCurrentTimestamp();
                		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_Drawing"+"."+TDrawingDefines.FileExtension;
                		File NF = new File(NFN);
                		if (!(new File(DrawingFileName)).renameTo(NF))
                			throw new IOException("could not rename file: "+DrawingFileName); //. =>
                		String FileName = NFN;
                		//.
                		POI_AddDataFile(Timestamp,FileName);
                		//.
                		long DataSize = 0;
                		File F = new File(FileName);
                		if (F.exists())
                			DataSize = F.length();
                		Toast.makeText(this, getString(R.string.SDrawingIsAdded)+Integer.toString((int)(DataSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >

        case SHOW_LASTPOIFILEDIALOG: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	String FileName = extras.getString("FileName");
                	try {
                		long DataSize = EnqueueFileDataFile(FileName);
                		Toast.makeText(this, getString(R.string.SFileIsAdded)+Integer.toString((int)(DataSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected File getImageTempFile(Context context) {
    	  return new File(TReflector.GetTempFolder(),"Image.jpg");
    }
    
    protected File getVideoTempFile(Context context) {
  	  return new File(TReflector.GetTempFolder(),"Video.3gp");
  }
  
    protected File getDrawingTempFile(Context context) {
    	return new File(TReflector.GetTempFolder(),"Drawing"+"."+TDrawingDefines.FileExtension);
    }

    private long EnqueueFileDataFile(String FileName) throws Exception {
    	double Timestamp = OleDate.UTCCurrentTimestamp();
		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_File"+"."+TFileSystem.FileName_GetExtension(FileName);
		File NF = new File(NFN);
		TFileSystem.CopyFile(new File(FileName), NF);
		FileName = NFN;
		//.
		POI_AddDataFile(Timestamp,FileName);
		//.
		long DataSize = 0;
		File F = new File(FileName);
		if (F.exists())
			DataSize = F.length();
		//.
		return DataSize;
    }
    
    @SuppressWarnings("unused")
	private void POI_AddText(double Timestamp, String FileName) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_LONG).show();
			return; //. ->
		}
        if ((new File(FileName)).exists())
        {
            TMapPOITextValue MapPOIText = new TMapPOITextValue(Timestamp,FileName);
            TObjectSetGetMapPOITextSO SO = new TObjectSetGetMapPOITextSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null);
            SO.setValue(MapPOIText);
            try {
                TTracker Tracker = TTracker.GetTracker(); 
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, getString(R.string.SErrorOfSendingText)+E.toString(), Toast.LENGTH_LONG).show();  
            }        
        }
        else
			Toast.makeText(this, R.string.SEmptyText, Toast.LENGTH_LONG).show();  
    }
    
    @SuppressWarnings("unused")
	private void POI_AddImage(double Timestamp, String FileName) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_LONG).show();
			return; //. ->
		}
        if ((new File(FileName)).exists())
        {
            TMapPOIImageValue MapPOIImage = new TMapPOIImageValue(Timestamp,FileName);
            TObjectSetGetMapPOIJPEGImageSO SO = new TObjectSetGetMapPOIJPEGImageSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null);
            SO.setValue(MapPOIImage);
            try {
                TTracker Tracker = TTracker.GetTracker(); 
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, getString(R.string.SErrorOfSendingImage)+E.toString(), Toast.LENGTH_LONG).show();  
            }        
        }
        else
			Toast.makeText(this, R.string.SImageIsNull, Toast.LENGTH_LONG).show();  
    }
    
    private void POI_AddDataFile(double Timestamp, String FileName) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_LONG).show();
			return; //. ->
		}
        if ((new File(FileName)).exists())
        {
            String Params = "1"; //. Version
            byte[] AddressData = Params.getBytes("windows-1251");
			//.
            TMapPOIDataFileValue MapPOIDataFile = new TMapPOIDataFileValue(Timestamp,FileName);
            TObjectSetGetMapPOIDataFileSO SO = new TObjectSetGetMapPOIDataFileSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null, AddressData);
            SO.setValue(MapPOIDataFile);
            try {
                TTracker Tracker = TTracker.GetTracker(); 
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, getString(R.string.SErrorOfSendingData)+E.toString(), Toast.LENGTH_LONG).show();  
            }        
        }
        else
			Toast.makeText(this, R.string.SEmptyData, Toast.LENGTH_LONG).show();  
    }
    
    public void StartObtainingCurrentFix() {
        TTracker Tracker = TTracker.GetTracker();
        if ((Tracker == null) || (Tracker.GeoLog.GPSModule == null)) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_LONG).show();
			return; //. ->
		}
    	new TCurrentFixObtaining(this, Tracker.GeoLog.GPSModule, new TCurrentFixObtaining.TDoOnFixIsObtainedHandler() {
    		@Override
    		public void DoOnFixIsObtained(TGPSFixValue Fix) {
    			UpdateInfo();
    		}
    	});
    }

    public void StartObtainingCurrentPosition() {
        TTracker Tracker = TTracker.GetTracker();
        if ((Tracker == null) || (Tracker.GeoLog.GPSModule == null)) {
			Toast.makeText(this, R.string.SErrorOfGettingCurrentPositionTrackerIsNotAvailable, Toast.LENGTH_LONG).show();
			return; //. ->
		}
    	try {
        	new TCurrentPositionObtaining(this, Tracker.GeoLog.GPSModule, Reflector(), new TCurrentPositionObtaining.TDoOnPositionIsObtainedHandler() {
        		@Override
        		public void DoOnPositionIsObtained(TXYCoord Crd) {
                	try {
    					Reflector().MoveReflectionWindow(Crd);
    				} catch (Exception Ex) {
    	                Toast.makeText(TTrackerPanel.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
    				}
                	//.
	                setResult(Activity.RESULT_OK);
            		TTrackerPanel.this.finish();
        		}
        	});
		} catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public TXYCoord ObtainCurrentPosition() throws Exception {
    	TGPSFixValue Fix;
		Fix = TTracker.GetTracker().GeoLog.GPSModule.GetCurrentFix();
		if (!Fix.IsSet()) 
			throw new Exception(getString(R.string.SCurrentPositionIsUnavailable)); //. =>
		if (Fix.IsEmpty()) 
			throw new Exception(getString(R.string.SCurrentPositionIsUnknown)); //. =>
		TXYCoord Crd = Reflector().ConvertGeoCoordinatesToXY(TGPSModule.DatumID, Fix.Latitude,Fix.Longitude,Fix.Altitude);
		return Crd;
    }
    
    public int GetAlarm() {
    	TTracker Tracker = TTracker.GetTracker();
    	if ((Tracker != null) && (Tracker.GeoLog.GPIModule != null))
    		return (Tracker.GeoLog.GPIModule.GetIntValue() & 3); //. ->
    	else
    		return 0;
    }
    
    public void SetAlarm(boolean flSet) throws Exception {
    	short V;
    	if (flSet)
    		V = 2; //. major alert priority
    	else 
    		V = 0; //. no alert
    	TTracker.GetTracker().GeoLog.GPIModule.SetValue(V);
    }
    
    private void EnableDisablePanelItems(boolean flEnabled) {
    	edFix.setEnabled(flEnabled);
        edFixSpeed.setEnabled(flEnabled);
        edFixPrecision.setEnabled(flEnabled);
        btnObtainCurrentFix.setEnabled(flEnabled);
        btnShowLocation.setEnabled(flEnabled);
        btnNewPOI.setEnabled(flEnabled);
        btnAddPOIText.setEnabled(flEnabled);
        btnAddPOIImage.setEnabled(flEnabled);
        btnAddPOIVideo.setEnabled(flEnabled);
        btnAddPOIDrawing.setEnabled(flEnabled);
        btnAddPOIFile.setEnabled(flEnabled);
        tbAlarm.setEnabled(flEnabled);
        edConnectorInfo.setEnabled(flEnabled);
        edCheckpoint.setEnabled(flEnabled);
        edOpQueueTransmitInterval.setEnabled(flEnabled);
        edPositionReadInterval.setEnabled(flEnabled);
        cbIgnoreImpulseModeSleepingOnMovement.setEnabled(flEnabled);
        edGeoThreshold.setEnabled(flEnabled);
        edOpQueue.setEnabled(flEnabled);
        btnOpQueueCommands.setEnabled(flEnabled);
        if (MainMenu != null) 
        	MainMenu.setGroupEnabled(1,flEnabled);
        edComponentFileStreaming.setEnabled(flEnabled);
        btnComponentFileStreamingCommands.setEnabled(flEnabled);
    }
    
    public static String DoubleToString(double Value) {
        int ValueInteger = (int)Value;
        long ValueDecimals = (int)((Value-ValueInteger)*100000);

        String ValueDecString = String.valueOf(ValueDecimals);
        while(ValueDecString.length()<5) {
            ValueDecString = "0" + ValueDecString;
        }
        return String.valueOf(ValueInteger) + "." + ValueDecString;
    }

    private void UpdateInfo() {
    	TTracker Tracker = TTracker.GetTracker(); 
    	if ((Tracker != null) && Tracker.GeoLog.IsEnabled()) {
            String S;
            //.
            tbAlarm.setChecked(GetAlarm() > 0);
            //. connector info
            if (Tracker.GeoLog.ConnectorModule.flProcessing) {
            	S = getString(R.string.SConnected);
            	if (Tracker.GeoLog.ConnectorModule.IsSecure())
            		S += "("+getString(R.string.SSecure)+")";
            	edConnectorInfo.setText(S);
            	edConnectorInfo.setTextColor(Color.GREEN);
            	if (Tracker.GeoLog.ConnectorModule.flReconnect) {
            		edConnectorInfo.setText(R.string.SReconnect);
                	edConnectorInfo.setTextColor(Color.RED);
            	}
            }
            else
            {
            	if (Tracker.GeoLog.ConnectorModule.flServerConnectionEnabled) {
            		edConnectorInfo.setText(R.string.SNoConnection);
                	edConnectorInfo.setTextColor(Color.RED);
            	}
            	else {
            		edConnectorInfo.setText(R.string.SDisabledConnection);
                	edConnectorInfo.setTextColor(Color.GRAY);
            	}
            	//.
        		Exception E = Tracker.GeoLog.ConnectorModule.GetProcessException();
                if (E != null)
                    Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
            }
            //. GPS module info
            if (Tracker.GeoLog.GPSModule.flProcessing) {
                if (Tracker.GeoLog.GPSModule.flGPSFixing) {
                    TGPSFixValue fix = Tracker.GeoLog.GPSModule.GetCurrentFix();
                    double TimeDelta = OleDate.UTCCurrentTimestamp()-fix.ArrivedTimeStamp;
                    int Seconds = (int)(TimeDelta*24.0*3600.0);
                    if (Seconds > 60)
                    	S = Integer.toString((int)(Seconds/60))+getString(R.string.SMinsAgo)+Integer.toString((int)(Seconds % 60))+getString(R.string.SSecsAgo);
                    else
                    	S = Integer.toString(Seconds)+getString(R.string.SSecsAgo);
                    lbTitle.setText(getString(R.string.SGeoPosition)+S);
                    S = DoubleToString(fix.Latitude)+"; "+DoubleToString(fix.Longitude)+"; "+DoubleToString(fix.Altitude);
                    edFix.setText(S);
                    edFix.setTextColor(Color.GREEN);
                    edFixSpeed.setText(Double.toString(fix.Speed));
                    edFixSpeed.setTextColor(Color.GREEN);
                    edFixPrecision.setText(Double.toString(fix.Precision));
                    edFixPrecision.setTextColor(Color.GREEN);
                    ///? btnObtainCurrentFix.setEnabled(true);
                    ///? btnShowLocation.setEnabled(true);
                    ///? btnNewPOI.setEnabled(true);
                    //.
                    /*///? if (MainMenu != null) 
                    	MainMenu.setGroupEnabled(1,true);*/
                }
                else {
                    TGPSFixValue fix = Tracker.GeoLog.GPSModule.GetCurrentFix();
                    double TimeDelta = OleDate.UTCCurrentTimestamp()-fix.ArrivedTimeStamp;
                    int Seconds = (int)(TimeDelta*24.0*3600.0);
                    if (Seconds > 60)
                    	S = Integer.toString((int)(Seconds/60))+getString(R.string.SMinsAgo)+Integer.toString((int)(Seconds % 60))+getString(R.string.SSecs);
                    else
                    	S = Integer.toString(Seconds)+getString(R.string.SSecs);
                    lbTitle.setText(getString(R.string.SGeoPositionIsUnknown)+S);
                    S = getString(R.string.SLastCoordinates)+DoubleToString(fix.Latitude)+"; "+DoubleToString(fix.Longitude)+"; "+DoubleToString(fix.Altitude)+")";
                    edFix.setText(S);
                    edFix.setTextColor(Color.RED);
                    edFixSpeed.setText(Double.toString(fix.Speed));
                    edFixSpeed.setTextColor(Color.RED);
                    edFixPrecision.setText(Double.toString(fix.Precision));
                    edFixPrecision.setTextColor(Color.RED);
                    ///? btnObtainCurrentFix.setEnabled(true);
                    ///? btnShowLocation.setEnabled(false);
                    ///? btnNewPOI.setEnabled(false);
                    //.
                    /*///?  if (MainMenu != null) 
                    	MainMenu.setGroupEnabled(1,false);*/
                }
            }
            else {
                lbTitle.setText(R.string.SGeoCoordinatesIsNull);
                edFix.setText(R.string.SGeoCoordinatesAreNotAvailable);
                edFix.setTextColor(Color.GRAY);
                edFixSpeed.setText("?");
                edFixSpeed.setTextColor(Color.GRAY);
                edFixPrecision.setText("?");
                edFixPrecision.setTextColor(Color.GRAY);
            	//.
        		Exception E = Tracker.GeoLog.GPSModule.GetProcessException();
                if (E != null)
                    Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
                //.
                if (MainMenu != null) 
                	MainMenu.setGroupEnabled(1,false);
            }
            edCheckpoint.setText(Short.toString(Tracker.GeoLog.ConnectorModule.CheckpointInterval.Value)+" ");
            edOpQueueTransmitInterval.setText(Integer.toString(Tracker.GeoLog.ConnectorModule.TransmitInterval/1000)+" ");
            edPositionReadInterval.setText(Integer.toString(Tracker.GeoLog.GPSModule.Provider_ReadInterval/1000)+" ");
            cbIgnoreImpulseModeSleepingOnMovement.setChecked(Tracker.GeoLog.GPSModule.flIgnoreImpulseModeSleepingOnMovement);
            edGeoThreshold.setText(Short.toString(Tracker.GeoLog.GPSModule.Threshold.Value)+" ");
            int POC = Tracker.GeoLog.ConnectorModule.PendingOperationsCount();
            if (POC > 0)
            	edOpQueue.setTextColor(Color.RED);
            else
            	edOpQueue.setTextColor(Color.GREEN);
            edOpQueue.setText(Integer.toString(POC)+" ");
            if (Tracker.GeoLog.ComponentFileStreaming != null) {
                TDEVICEModule.TComponentFileStreaming.TItemsStatistics ItemsStatistics = Tracker.GeoLog.ComponentFileStreaming.GetItemsStatistics();
                if (ItemsStatistics.Count > 0)
                	edComponentFileStreaming.setTextColor(Color.RED);
                else
                	edComponentFileStreaming.setTextColor(Color.GREEN);
                String SS = null;
                int Size = (int)(ItemsStatistics.Size/1024);
                if (Size < 1024) {
                	if (Size > 0)
                		SS = Integer.toString(Size)+getString(R.string.SKb);
                }
                else {
                	Size = (int)(Size/1024);
                	SS = Integer.toString(Size)+getString(R.string.SMb);
                }
                if (SS != null)
                	S = Integer.toString(ItemsStatistics.Count)+" ("+SS+")";
                else
                	S = Integer.toString(ItemsStatistics.Count);
                if (!Tracker.GeoLog.ComponentFileStreaming.flEnabledStreaming)
                	S = S+" "+"/"+getString(R.string.SSuspended)+"/";
            	edComponentFileStreaming.setText(S);
            }
            else {
            	edComponentFileStreaming.setTextColor(Color.GRAY);
                edComponentFileStreaming.setText("?");
            }
            //. error handling
            Exception E = Tracker.GeoLog.ConnectorModule.GetProcessOutgoingOperationException();
            if (E != null)
                Toast.makeText(this, getString(R.string.SServerSideError)+E.getMessage(), Toast.LENGTH_LONG).show();
            
    	}
    	else {
        	edConnectorInfo.setText("");
            lbTitle.setText(R.string.SGeoCoordinates);
            edFix.setTextColor(Color.GRAY);
            edFix.setText(R.string.SDisabled1);
            edFixSpeed.setTextColor(Color.GRAY);
            edFixSpeed.setText("?");
            edFixPrecision.setTextColor(Color.GRAY);
            edFixPrecision.setText("?");
            edCheckpoint.setTextColor(Color.GRAY);
            edCheckpoint.setText("?");
            edOpQueueTransmitInterval.setTextColor(Color.GRAY);
            edOpQueueTransmitInterval.setText("?");
            edPositionReadInterval.setTextColor(Color.GRAY);
            edPositionReadInterval.setText("?");
            cbIgnoreImpulseModeSleepingOnMovement.setChecked(false);
            edGeoThreshold.setTextColor(Color.GRAY);
            edGeoThreshold.setText("?");
            edOpQueue.setTextColor(Color.GRAY);
            edOpQueue.setText(Integer.toString(Tracker.GeoLog.ConnectorModule.PendingOperationsCount())+" ");
            edComponentFileStreaming.setTextColor(Color.GRAY);
            if (Tracker.GeoLog.ComponentFileStreaming != null) {
                TDEVICEModule.TComponentFileStreaming.TItemsStatistics ItemsStatistics = Tracker.GeoLog.ComponentFileStreaming.GetItemsStatistics();
                String SS = null;
                int Size = (int)(ItemsStatistics.Size/1024);
                if (Size < 1024) {
                	if (Size > 0)
                		SS = Integer.toString(Size)+getString(R.string.SKb);
                }
                else {
                	Size = (int)(Size/1024);
                	SS = Integer.toString(Size)+getString(R.string.SMb);
                }
                String S;
                if (SS != null)
                	S = Integer.toString(ItemsStatistics.Count)+" ("+SS+")";
                else
                	S = Integer.toString(ItemsStatistics.Count);
                if (!Tracker.GeoLog.ComponentFileStreaming.flEnabledStreaming)
                	S = S+" "+"/"+getString(R.string.SSuspended)+"/";
            	edComponentFileStreaming.setText(S);
            }
            else 
                edComponentFileStreaming.setText("?");
            //.
            if (MainMenu != null) 
            	MainMenu.setGroupEnabled(1,false);
    	}
    }
    
	public static final int MESSAGE_UPDATEINFO = 1;
	
    private final Handler UpdaterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_UPDATEINFO:
                	if (flVisible)
                		UpdateInfo();  
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };

    private class TUpdaterTask extends TimerTask {
    	
        private TTrackerPanel _TrackerPanel;
        
        public TUpdaterTask(TTrackerPanel pTrackerPanel) {
            _TrackerPanel = pTrackerPanel;
        }
        
        @Override
        public void run() {
        	try {
            	_TrackerPanel.UpdaterHandler.obtainMessage(TTrackerPanel.MESSAGE_UPDATEINFO).sendToTarget();
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    }   
}
