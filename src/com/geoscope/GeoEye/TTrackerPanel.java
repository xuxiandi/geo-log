package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOIDataFileSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOIJPEGImageSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOITextSO;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIDataFileValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIImageValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOITextValue;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TProgressor;

@SuppressLint({ "HandlerLeak", "HandlerLeak" })
public class TTrackerPanel extends Activity {

	public static final int MESSAGE_UPDATEINFO = 1;
	public static final int SHOW_NEWPOIPANEL = 1;
	public static final int SHOW_LASTPOICAMERA = 2;
	public static final int SHOW_LASTPOITEXTEDITOR = 3;
	public static final int SHOW_LASTPOIVIDEOCAMERA = 4;
	//.
	public static final int LOG_MENU = 1;
	public static final int POI_SUBMENU = 100; 
	public static final int POI_SUBMENU_NEWPOI = 101; 
	public static final int POI_SUBMENU_ADDTEXT = 102; 
	public static final int POI_SUBMENU_ADDIMAGE = 103; 
	public static final int POI_SUBMENU_ADDVIDEO = 104; 
    
    private class TCurrentFixObtaining extends TCancelableThread {

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
    	
    	private TGPSModule GPSModule;
    	//. 
    	private TGPSFixValue CurrentFix = null;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TCurrentFixObtaining(TGPSModule pGPSModule) {
    		GPSModule = pGPSModule;
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
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TTrackerPanel.this, Reflector.getString(R.string.SErrorOfGettingCoordinates)+E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_COMPLETED:
	            	//. TGPSFixValue Fix = (TGPSFixValue)msg.obj;
	            	UpdateInfo();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TTrackerPanel.this);    
	            	progressDialog.setMessage(Reflector.getString(R.string.SCoordinatesGettingForNow));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);    
	            	progressDialog.setIndeterminate(false); 
	            	progressDialog.setCancelable(true);
	            	progressDialog.setOnCancelListener( new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
						}
					});
	            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, Reflector.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
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
    
    private class TCurrentPositionObtaining extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_COMPLETED 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

        private ProgressDialog progressDialog; 
    	
    	public TCurrentPositionObtaining() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			TXYCoord Crd;
    			try {
    				Crd = ObtainCurrentPosition();
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
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TTrackerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_COMPLETED:
	            	TXYCoord Crd = (TXYCoord)msg.obj;
	            	Reflector.MoveReflectionWindow(Crd);
	            	//.
	                setResult(Activity.RESULT_OK);
	        		finish();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TTrackerPanel.this);    
	            	progressDialog.setMessage(TTrackerPanel.this.getString(R.string.SWaitAMoment));    
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
    
	private TReflector Reflector;
	private Timer Updater;
	private TableLayout _TableLayout;
	private Menu MainMenu;
	private TextView lbTitle;
    private EditText edFix;
    private EditText edFixSpeed;
    private EditText edFixPrecision;
    private Button btnObtainCurrentFix;
    private Button btnShowLocation;	
    private Button btnNewPOI;	
    private Button btnAddPOIText;	
    private Button btnAddPOIImage;	
    private Button btnAddPOIVideo;	
	private ToggleButton tbAlarm;
    private EditText edConnectorInfo;
    private EditText edCheckpoint;
    private EditText edOpQueueTransmitInterval;
    private EditText edPositionReadInterval;
    private CheckBox cbIgnoreImpulseModeSleepingOnMovement;
    private EditText edGeoThreshold;
    private EditText edOpQueue;
    private Button btnOpQueueCommands;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.GetReflector();
        //.
        setContentView(R.layout.tracker_panel);
        //.
        _TableLayout = (TableLayout)findViewById(R.id.TrackerPanelTableLayout);
        _TableLayout.setBackgroundColor(Color.blue(100));
        /*///? cbUseTracker = (CheckBox)findViewById(R.id.cbUseTracker);
        cbUseTracker.setChecked(TTracker.GetTracker() != null);
        cbUseTracker.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				try {
					TTracker.EnableDisableTracker(arg1,TTrackerPanel.this);
					EnableDisablePanelItems(arg1);
					Reflector.Configuration.Save();
					if (!arg1)
						finish(); 
		    	}
		    	catch (Exception E) {
		            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_SHORT).show();
		    	}
			}
        });*/      
        lbTitle = (TextView)findViewById(R.id.lbTitle);
        edFix = (EditText)findViewById(R.id.edFix);
        edFixSpeed = (EditText)findViewById(R.id.edFixSpeed);
        edFixPrecision = (EditText)findViewById(R.id.edFixPrecision);
        btnObtainCurrentFix = (Button)findViewById(R.id.btnObtainCurrentFix);
        btnObtainCurrentFix.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	StartObtainingCurrentFix();
            }
        });
        btnShowLocation = (Button)findViewById(R.id.btnShowLocation);
        btnShowLocation.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	StartObtainingCurrentPosition();
            }
        });
        btnNewPOI = (Button)findViewById(R.id.btnNewPOI);
        btnNewPOI.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOIPanel.class);
                startActivityForResult(intent,SHOW_NEWPOIPANEL);
            }
        });
        btnAddPOIText = (Button)findViewById(R.id.btnAddPOIText);
        btnAddPOIText.setEnabled(false);
        btnAddPOIText.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOITextPanel.class);
                startActivityForResult(intent,SHOW_LASTPOITEXTEDITOR);
            }
        });
        btnAddPOIImage = (Button)findViewById(R.id.btnAddPOIImage);
        btnAddPOIImage.setEnabled(false);
        btnAddPOIImage.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
      		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TTrackerPanel.this.getTempFile(TTrackerPanel.this))); 
      		    startActivityForResult(intent, SHOW_LASTPOICAMERA);    		
            }
        });
        btnAddPOIVideo = (Button)findViewById(R.id.btnAddPOIVideo);
        btnAddPOIVideo.setEnabled(false);
        btnAddPOIVideo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		Intent intent = new Intent(TTrackerPanel.this, TTrackerPOIVideoPanel.class);
                startActivityForResult(intent,SHOW_LASTPOIVIDEOCAMERA);
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
    	            Toast.makeText(Reflector, Reflector.getString(R.string.STrackerError)+E.getMessage(), Toast.LENGTH_SHORT).show();
    	    	}
    	    	return true;
			}
		});
        edConnectorInfo = (EditText)findViewById(R.id.edConnectorInfo);
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
			    		throw new Exception(Reflector.getString(R.string.STrackerIsNotInitialized)); //. =>
			    	((CheckBox)arg0).setChecked(!((CheckBox)arg0).isChecked());
			    	Tracker.GeoLog.GPSModule.flIgnoreImpulseModeSleepingOnMovement = ((CheckBox)arg0).isChecked();
			    	Tracker.GeoLog.SaveConfiguration();
				}
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TTrackerPanel.this, Reflector.getString(R.string.SSetError)+S, Toast.LENGTH_LONG).show();  						
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
        		builder.setNegativeButton(Reflector.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
					    	TTracker Tracker = TTracker.GetTracker();
					    	if (Tracker == null)
					    		throw new Exception(Reflector.getString(R.string.STrackerIsNotInitialized)); //. =>
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
		        			Toast.makeText(TTrackerPanel.this, Reflector.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
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
        Updater = new Timer();
        Updater.schedule(new TUpdaterTask(this),100,3000);
        //. start tracker position fixing immediately if it is in impulse mode
        TTracker Tracker = TTracker.GetTracker();
    	if ((Tracker != null) && (Tracker.GeoLog.GPSModule != null) && Tracker.GeoLog.GPSModule.IsEnabled() && Tracker.GeoLog.GPSModule.flImpulseMode) 
			Tracker.GeoLog.GPSModule.LocationMonitor.flProcessImmediately = true;
	}

    @Override
	protected void onDestroy() {
        if (Updater != null) {
        	Updater.cancel();
        	Updater = null;
        }
        //.
		super.onDestroy();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.tracker_panel_menu, menu);
        //.
        menu.add(Menu.NONE,LOG_MENU,Menu.NONE,R.string.SLog);
        //.
        SubMenu fileMenu = menu.addSubMenu(1,POI_SUBMENU,1,R.string.SPOI);
        fileMenu.add(1, POI_SUBMENU_NEWPOI, 0, R.string.SNewPOI);
        fileMenu.add(1, POI_SUBMENU_ADDTEXT, 1, R.string.SAddText);
        fileMenu.add(1, POI_SUBMENU_ADDIMAGE, 1, R.string.SAddImage);
        fileMenu.add(1, POI_SUBMENU_ADDVIDEO, 1, R.string.SAddVideo);
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
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(getTempFile(this))); 
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
        	}  
            break; //. >
        
        case SHOW_LASTPOITEXTEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	String POIText = extras.getString("POIText");
                	try {
                		if (POIText.equals(""))
                			throw new Exception(getString(R.string.STextIsNull)); //. =>
                		//.
                		byte[] TextBA = POIText.getBytes("windows-1251");
                		//.
                		POI_AddText(TextBA);
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
				File F = getTempFile(this);
				if (F.exists())
				{
					try {
						//. try to gc
						System.gc();
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
							POI_AddImage(PictureBA);
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
	        			Toast.makeText(this, S, Toast.LENGTH_SHORT).show();  						
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
                	String FileName = extras.getString("FileName");
                	try {
                		File F = new File(FileName);
                		if (!F.exists()) 
                			throw new Exception(getString(R.string.SVideoFileIsNotExist)); //. =>
                		//.
                		byte[] Data;
                    	long FileSize = F.length();
                    	FileInputStream FIS = new FileInputStream(FileName);
                    	try {
                    		Data = new byte[(int)FileSize];
                    		FIS.read(Data);
                    	}
                    	finally {
                    		FIS.close();
                    	}
                		//.
                		POI_AddDataFile(F.getName(),Data);
                		//.
                		Toast.makeText(this, Reflector.getString(R.string.SDataIsAdded)+Integer.toString((int)(Data.length/1024))+Reflector.getString(R.string.SKb), Toast.LENGTH_LONG).show();
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

    protected File getTempFile(Context context) {
    	  return new File(Environment.getExternalStorageDirectory(),"image.jpg");
    }
    
    private void POI_AddText(byte[] TextData) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_SHORT).show();
			return; //. ->
		}
        if (TextData != null)
        {
            TMapPOITextValue MapPOIText = new TMapPOITextValue(OleDate.UTCCurrentTimestamp(),TextData);
            TObjectSetMapPOITextSO SO = new TObjectSetMapPOITextSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null);
            SO.setValue(MapPOIText);
            try {
                TTracker Tracker = TTracker.GetTracker(); 
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, Reflector.getString(R.string.SErrorOfSendingText)+E.toString(), Toast.LENGTH_LONG).show();  
            }        
        }
        else
			Toast.makeText(this, R.string.SEmptyText, Toast.LENGTH_LONG).show();  
    }
    
    private void POI_AddImage(byte[] ImageJpegData) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_SHORT).show();
			return; //. ->
		}
        if (ImageJpegData != null)
        {
            TMapPOIImageValue MapPOIImage = new TMapPOIImageValue(OleDate.UTCCurrentTimestamp(),ImageJpegData);
            TObjectSetMapPOIJPEGImageSO SO = new TObjectSetMapPOIJPEGImageSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null);
            SO.setValue(MapPOIImage);
            try {
                TTracker Tracker = TTracker.GetTracker(); 
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, Reflector.getString(R.string.SErrorOfSendingImage)+E.toString(), Toast.LENGTH_LONG).show();  
            }        
        }
        else
			Toast.makeText(this, R.string.SImageIsNull, Toast.LENGTH_LONG).show();  
    }
    
    private void POI_AddDataFile(String FileName, byte[] Data) throws IOException {
		if (!TTracker.TrackerIsEnabled()) {
			Toast.makeText(this, R.string.STrackerIsNotActive, Toast.LENGTH_SHORT).show();
			return; //. ->
		}
        if (Data != null)
        {
            TMapPOIDataFileValue MapPOIDataFile = new TMapPOIDataFileValue(OleDate.UTCCurrentTimestamp(),FileName,Data);
            TObjectSetMapPOIDataFileSO SO = new TObjectSetMapPOIDataFileSO(TTracker.GetTracker().GeoLog.ConnectorModule,TTracker.GetTracker().GeoLog.UserID, TTracker.GetTracker().GeoLog.UserPassword, TTracker.GetTracker().GeoLog.ObjectID, null);
            SO.setValue(MapPOIDataFile);
            try {
                TTracker Tracker = TTracker.GetTracker(); 
            	Tracker.GeoLog.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                Tracker.GeoLog.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                Tracker.GeoLog.BackupMonitor.BackupImmediate();
            } 
            catch (Exception E) {
    			Toast.makeText(this, Reflector.getString(R.string.SErrorOfSendingData)+E.toString(), Toast.LENGTH_LONG).show();  
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
    	new TCurrentFixObtaining(Tracker.GeoLog.GPSModule);
    }

    public void StartObtainingCurrentPosition() {
    	if (!TTracker.TrackerIsEnabled()) { 
			Toast.makeText(this, R.string.SErrorOfGettingCurrentPositionTrackerIsNotAvailable, Toast.LENGTH_LONG).show();
			return; //. ->
		}
    	new TCurrentPositionObtaining();
    }
    
    public TXYCoord ObtainCurrentPosition() throws Exception {
    	TGPSFixValue Fix;
    	TXYCoord Crd = new TXYCoord();
		Fix = TTracker.GetTracker().GeoLog.GPSModule.GetCurrentFix();
		if (!Fix.IsSet()) 
			throw new Exception(getString(R.string.SCurrentPositionIsUnavailable)); //. =>
		if (Fix.IsEmpty()) 
			throw new Exception(getString(R.string.SCurrentPositionIsUnknown)); //. =>
		Crd = Reflector.ConvertGeoCoordinatesToXY(TTracker.DatumID,Fix.Latitude,Fix.Longitude);
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
    }
    
    private String DoubleToString(double Value) {
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
    	if ((Tracker != null) && Tracker.GeoLog.flEnabled)
    	{
            String S;
            //.
            tbAlarm.setChecked(GetAlarm() > 0);
            //. connector info
            if (TTracker.GetTracker().GeoLog.ConnectorModule.flProcessing)
            	edConnectorInfo.setText(R.string.SConnected);
            else
            {
            	if (Tracker.GeoLog.ConnectorModule.flServerConnectionEnabled)
            	{
            		S = getString(R.string.SNoConnection);
            		Exception E = Tracker.GeoLog.ConnectorModule.GetProcessException();
            		if (E != null)
            			S = S+", "+E.getMessage()+" ";
            		edConnectorInfo.setText(S);
            	}
            	else
            		edConnectorInfo.setText(R.string.SDisabledConnection);
            }
            //. GPS module info
            if (Tracker.GeoLog.GPSModule.flProcessing)
            {
                if (Tracker.GeoLog.GPSModule.flGPSFixing)
                {
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
            else
            {
                lbTitle.setText(R.string.SGeoCoordinatesIsNull);
                S = getString(R.string.SGeoCoordinatesAreNotAvailable);
                Exception E = Tracker.GeoLog.GPSModule.GetProcessException();
                if (E != null)
                    S = S+", "+E.getMessage();
                edFix.setText(S);
                edFix.setTextColor(Color.GRAY);
                edFixSpeed.setText("?");
                edFixSpeed.setTextColor(Color.GRAY);
                edFixPrecision.setText("?");
                edFixPrecision.setTextColor(Color.GRAY);
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
            //. error handling
            Exception E = Tracker.GeoLog.ConnectorModule.GetProcessOutgoingOperationException();
            if (E != null)
                Toast.makeText(this, getString(R.string.SServerSideError)+E.getMessage(), Toast.LENGTH_SHORT).show();
            
    	}
    	else {
        	edConnectorInfo.setText("");
            lbTitle.setText(R.string.SGeoCoordinates);
            edFix.setText(R.string.SDisabled1);
            edFix.setTextColor(Color.GRAY);
            edFixSpeed.setText("?");
            edFixSpeed.setTextColor(Color.GRAY);
            edFixPrecision.setText("?");
            edFixPrecision.setTextColor(Color.GRAY);
            edCheckpoint.setText("?");
            edOpQueueTransmitInterval.setText("?");
            edPositionReadInterval.setText("?");
            cbIgnoreImpulseModeSleepingOnMovement.setChecked(false);
            edGeoThreshold.setText("?");
            edOpQueue.setText("?");
            //.
            if (MainMenu != null) 
            	MainMenu.setGroupEnabled(1,false);
    	}
    }
    
    private final Handler UpdaterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_UPDATEINFO:            	
                UpdateInfo();  
            	break; //. >
            }
        }
    };

    private class TUpdaterTask extends TimerTask
    {
        private TTrackerPanel _TrackerPanel;
        
        public TUpdaterTask(TTrackerPanel pTrackerPanel)
        {
            _TrackerPanel = pTrackerPanel;
        }
        
        public void run()
        {
        	_TrackerPanel.UpdaterHandler.obtainMessage(TTrackerPanel.MESSAGE_UPDATEINFO).sendToTarget();
        }
    }   
}
