package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUserDataFile;
import com.geoscope.GeoEye.TTrackerPanel.TCurrentFixObtaining;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TUIDGenerator;

@SuppressLint("HandlerLeak")
public class TMyUserPanel extends Activity {

	public static final int REQUEST_SETUSERACTIVITY = 1;
	public static final int REQUEST_SHOWONREFLECTOR = 2;
	public static final int REQUEST_TEXTEDITOR		= 3;
	public static final int REQUEST_CAMERA 			= 4;
	public static final int REQUEST_VIDEOCAMERA		= 5;
	
	public static final int ACTIVITY_DATAFILE_TYPE_TEXT 	= 1;
	public static final int ACTIVITY_DATAFILE_TYPE_IMAGE 	= 2;
	public static final int ACTIVITY_DATAFILE_TYPE_VIDEO 	= 3;
		
    private static TUserDescriptor 	UserInfo = null; 
    private static TActivity 		UserCurrentActivity = null;
    
	private EditText edUserName;
	private EditText edUserFullName;
	private EditText edUserContactInfo;
	private EditText edUserConnectionState;
	private EditText edUserLocation;
	private Button btnGetUserLocation;
	private Button btnUserLocation;
	private Button btnUserCurrentActivity;
	private Button btnUserCurrentActivityAddDataFile;
	private Button btnUserCurrentActivityAddTextDataFile;
	private Button btnUserCurrentActivityAddImageDataFile;
	private Button btnUserCurrentActivityAddVideoDataFile;
	private Button btnUserCurrentActivityComponentList;
	private Button btnUserLastActivities;
    //.
    private boolean flVisible = false;
	//.
	private TUpdating	Updating = null;
	//.
	private Timer StatusUpdater = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.myuser_panel);
        //.
        edUserName = (EditText)findViewById(R.id.edUserName);
        edUserFullName = (EditText)findViewById(R.id.edUserFullName);
        edUserContactInfo = (EditText)findViewById(R.id.edUserContactInfo);
        edUserConnectionState = (EditText)findViewById(R.id.edUserConnectionState);
        edUserLocation = (EditText)findViewById(R.id.edUserLocation);
        btnGetUserLocation = (Button)findViewById(R.id.btnGetUserLocation);
        btnGetUserLocation.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		try {
        			GetUserLocation();
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserLocation = (Button)findViewById(R.id.btnUserLocationTracker);
        btnUserLocation.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		Intent intent = new Intent(TMyUserPanel.this, TTrackerPanel.class);
        		startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        btnUserCurrentActivity = (Button)findViewById(R.id.btnUserCurrentActivity);
        btnUserCurrentActivity.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
            	Intent intent = new Intent(TMyUserPanel.this, TUserActivityPanel.class);
            	startActivityForResult(intent,REQUEST_SETUSERACTIVITY);
            }
        });
        btnUserCurrentActivityAddDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddDataFile);
        btnUserCurrentActivityAddDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
        		AddDataFile();
            }
        });
        btnUserCurrentActivityAddTextDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddTextDataFile);
        btnUserCurrentActivityAddTextDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
        		try {
    				AddDataFile(ACTIVITY_DATAFILE_TYPE_TEXT);
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserCurrentActivityAddImageDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddImageDataFile);
        btnUserCurrentActivityAddImageDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
        		try {
    				AddDataFile(ACTIVITY_DATAFILE_TYPE_IMAGE);
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserCurrentActivityAddVideoDataFile = (Button)findViewById(R.id.btnUserCurrentActivityAddVideoDataFile);
        btnUserCurrentActivityAddVideoDataFile.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
        		try {
    				AddDataFile(ACTIVITY_DATAFILE_TYPE_VIDEO);
				}
				catch (Exception E) {
        			Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnUserCurrentActivityComponentList = (Button)findViewById(R.id.btnUserCurrentActivityComponentList);
        btnUserCurrentActivityComponentList.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if ((UserInfo == null) || (UserCurrentActivity == null))
        			return; //. ->
            	Intent intent = new Intent(TMyUserPanel.this, TUserActivityComponentListPanel.class);
            	intent.putExtra("UserID",UserInfo.UserID);
            	intent.putExtra("ActivityID",UserCurrentActivity.ID);
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        btnUserLastActivities = (Button)findViewById(R.id.btnUserLastActivities);
        btnUserLastActivities.setOnClickListener(new OnClickListener() {
        	@Override
            public void onClick(View v) {
        		if (UserInfo == null)
        			return; //. ->
            	Intent intent = new Intent(TMyUserPanel.this, TUserActivityListPanel.class);
            	intent.putExtra("UserID",UserInfo.UserID);
            	startActivityForResult(intent,REQUEST_SHOWONREFLECTOR);
            }
        });
        //.
    	if ((UserInfo == null) || (UserCurrentActivity == null))
    		StartUpdating();
    	else
    		Update();
        //.
        StatusUpdater = new Timer();
        StatusUpdater.schedule(new TUpdaterTask(this),100,1000);
	}

	@Override
	protected void onDestroy() {
        if (StatusUpdater != null) {
        	StatusUpdater.cancel();
        	StatusUpdater = null;
        }
		//.
		if (Updating != null) {
			Updating.CancelAndWait();
			Updating = null;
		}
		//.
		super.onDestroy();
	}

	@Override
	protected void onResume() {
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
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_SETUSERACTIVITY: 
        	if (resultCode == RESULT_OK) 
        		StartUpdating();
            break; //. >

        case REQUEST_SHOWONREFLECTOR: 
        	if (resultCode == RESULT_OK) 
        		finish();
            break; //. >

        case REQUEST_TEXTEDITOR: 
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
                		String NFN = TGPSModule.MapPOIComponentFolder+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_Text.txt";
                		File NF = new File(NFN);
                		FileOutputStream FOS = new FileOutputStream(NF);
                		try {
                			FOS.write(TextBA);
                		}
                		finally {
                			FOS.close();
                		}
                		//. prepare and send datafile
                		final String 	DataFileName = NFN;
                		final int 		DataFileSize = TextBA.length;
    		    		TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
    		    			@Override
    		    			public void Process() throws Exception {
    		    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    		    				if (UserAgent == null)
    		    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    					    	TTracker Tracker = TTracker.GetTracker();
    					    	if (Tracker == null)
    					    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    					    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, DataFileName);
    					    	DataFile.Send(Tracker.GeoLog);
    		    			}
    		    			@Override 
    		    			public void DoOnCompleted() throws Exception {
    	                		Toast.makeText(TMyUserPanel.this, getString(R.string.STextIsAdded)+Integer.toString(DataFileSize), Toast.LENGTH_LONG).show();
    		    			}
    		    			@Override
    		    			public void DoOnException(Exception E) {
    		    				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    		    			}
    		    		};
    		    		Processing.Start();
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >

        case REQUEST_CAMERA: 
        	if (resultCode == RESULT_OK) {  
				File F = getImageTempFile(this);
				if (F.exists()) {
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
		                	double Timestamp = OleDate.UTCCurrentTimestamp();
	                		String NFN = TGPSModule.MapPOIComponentFolder+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_Image.jpg";
	                		File NF = new File(NFN);
	                		FileOutputStream FOS = new FileOutputStream(NF);
	                		try {
	                			FOS.write(PictureBA);
	                		}
	                		finally {
	                			FOS.close();
	                		}
	                		//. prepare and send datafile
	                		final String 	DataFileName = NFN;
	                		final int 		DataFileSize = PictureBA.length;
	    		    		TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
	    		    			@Override
	    		    			public void Process() throws Exception {
	    		    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    		    				if (UserAgent == null)
	    		    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    					    	TTracker Tracker = TTracker.GetTracker();
	    					    	if (Tracker == null)
	    					    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    					    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, DataFileName);
	    					    	DataFile.Send(Tracker.GeoLog);
	    		    			}
	    		    			@Override 
	    		    			public void DoOnCompleted() throws Exception {
	    				        	Toast.makeText(TMyUserPanel.this, getString(R.string.SImageIsAdded)+Integer.toString(DataFileSize), Toast.LENGTH_LONG).show();
	    		    			}
	    		    			@Override
	    		    			public void DoOnException(Exception E) {
	    		    				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    		    			}
	    		    		};
	    		    		Processing.Start();
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

        case REQUEST_VIDEOCAMERA: 
        	if (resultCode == RESULT_OK) {  
            	try {
                	double Timestamp = OleDate.UTCCurrentTimestamp();
    				File F = getVideoTempFile(this);
    				if (F.exists()) {
						//. try to gc
						System.gc();
						//.
	            		String NFN = TGPSModule.MapPOIComponentFolder+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_"+F.getName();
	            		File NF = new File(NFN);
	            		F.renameTo(NF);
	            		String FileName = NFN;
                		//. prepare and send datafile
                		final String 	DataFileName = FileName;
	            		final long 		DataFileSize;
	            		F = new File(FileName);
	            		if (F.exists())
	            			DataFileSize = F.length();
	            		else
	            			DataFileSize = 0;
    		    		TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
    		    			@Override
    		    			public void Process() throws Exception {
    		    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    		    				if (UserAgent == null)
    		    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    					    	TTracker Tracker = TTracker.GetTracker();
    					    	if (Tracker == null)
    					    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    					    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, DataFileName);
    					    	DataFile.Send(Tracker.GeoLog);
    		    			}
    		    			@Override 
    		    			public void DoOnCompleted() throws Exception {
    		            		Toast.makeText(TMyUserPanel.this, getString(R.string.SDataIsAdded)+Integer.toString((int)(DataFileSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
    		    			}
    		    			@Override
    		    			public void DoOnException(Exception E) {
    		    				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    		    			}
    		    		};
    		    		Processing.Start();
    				}
    				else
            			Toast.makeText(this, R.string.SVideoWasNotPrepared, Toast.LENGTH_SHORT).show();  
				}
				catch (Exception E) {
        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
			}
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void GetUserLocation() throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
    	new TCurrentFixObtaining(this, Tracker.GeoLog.GPSModule, new TCurrentFixObtaining.TDoOnFixIsObtainedHandler() {
    		@Override
    		public void DoOnFixIsObtained(TGPSFixValue Fix) {
    		}
    	});
    }
    
    private void AddDataFile() {
		final CharSequence[] _items = new CharSequence[3];
		_items[0] = getString(R.string.SText); 
		_items[1] = getString(R.string.SImage); 
		_items[2] = getString(R.string.SVideo1); 
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.SCreateDataFile);
		builder.setNegativeButton(getString(R.string.SCancel), null);
		builder.setSingleChoiceItems(_items, -1,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						try {
							switch (arg1) {
							case 0:
								AddDataFile(ACTIVITY_DATAFILE_TYPE_TEXT);
								break; //. >

							case 1:
								AddDataFile(ACTIVITY_DATAFILE_TYPE_IMAGE);
								break; //. >

							case 2:
								AddDataFile(ACTIVITY_DATAFILE_TYPE_VIDEO);
								break; //. >
							}
							arg0.dismiss();
						} catch (Exception E) {
							Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
						}
					}
				});
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    private void AddDataFile(int DataFileType) throws IOException {
		if (!TTracker.TrackerIsEnabled()) 
			throw new IOException(getString(R.string.STrackerIsNotActive)); //. =>
		switch (DataFileType) {
		case ACTIVITY_DATAFILE_TYPE_TEXT:
    		Intent intent = new Intent(TMyUserPanel.this, TTrackerPOITextPanel.class);
            startActivityForResult(intent,REQUEST_TEXTEDITOR);
			break; //. >
			
		case ACTIVITY_DATAFILE_TYPE_IMAGE:
  		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TMyUserPanel.this.getImageTempFile(TMyUserPanel.this))); 
  		    startActivityForResult(intent, REQUEST_CAMERA);    		
			break; //. >
			
		case ACTIVITY_DATAFILE_TYPE_VIDEO:
  		    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TMyUserPanel.this.getVideoTempFile(TMyUserPanel.this))); 
  		    startActivityForResult(intent, REQUEST_VIDEOCAMERA);    		
			break; //. >
		}
    }
    
    protected File getImageTempFile(Context context) {
  	  	return new File(TReflector.TempFolder,"Image.jpg");
    }
  
    protected File getVideoTempFile(Context context) {
    	return new File(TReflector.TempFolder,"Video.3gp");
    }

    private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_STARTED = 0;
    	private static final int MESSAGE_COMPLETED = 1;
    	private static final int MESSAGE_FINISHED = 2;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 3;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 4;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 5;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
        private ProgressDialog progressDialog;
        //.
        private TUserDescriptor UserInfo = null; 
        private TActivity 		UserCurrentActivity = null;
    	
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
					MessageHandler.obtainMessage(MESSAGE_STARTED).sendToTarget();
					//.
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    				//.
	    				UserInfo = UserAgent.Server.User.GetUserInfo();
	    				UserCurrentActivity = UserAgent.Server.User.GetUserCurrentActivity();
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
		                Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_STARTED:
		            	TMyUserPanel.this.btnUserCurrentActivity.setText(R.string.SUpdating);
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	TMyUserPanel.UserInfo = UserInfo;
		            	TMyUserPanel.UserCurrentActivity = UserCurrentActivity;
	           		 	//.
	           		 	TMyUserPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	TMyUserPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TMyUserPanel.this);    
		            	progressDialog.setMessage(TMyUserPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TMyUserPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TMyUserPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TMyUserPanel.this.finish();
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
	        	catch (Exception E) {
	        	}
	        }
	    };
    }   
	
    private void Update() {
    	if (UserInfo != null) {
    		edUserName.setText(UserInfo.UserName);
    		edUserFullName.setText(UserInfo.UserFullName);
    		edUserContactInfo.setText(UserInfo.UserContactInfo);
    	}
    	else {
    		edUserName.setText("");
    		edUserFullName.setText("");
    		edUserContactInfo.setText("");
    	}
    	//.
    	if ((UserCurrentActivity != null) && (UserCurrentActivity.ID != 0)) { 
    		btnUserCurrentActivity.setText(UserCurrentActivity.GetInfo(this,false));
    		btnUserCurrentActivityAddDataFile.setEnabled(true);
    		btnUserCurrentActivityComponentList.setEnabled(true);
    	}
    	else {
    		btnUserCurrentActivity.setText(R.string.SNone1);
    		btnUserCurrentActivityAddDataFile.setEnabled(false);
    		btnUserCurrentActivityComponentList.setEnabled(false);
    	}
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.CancelAndWait();
    	Updating = new TUpdating(true,true);
    }
    
	public static final int MESSAGE_UPDATESTATUS = 1;
	
    private final Handler UpdaterHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_UPDATESTATUS:
            	if (flVisible)
            		UpdateStatus();  
            	break; //. >
            }
        }
    };

    private class TUpdaterTask extends TimerTask
    {
        private TMyUserPanel Panel;
        
        public TUpdaterTask(TMyUserPanel pPanel)
        {
            Panel = pPanel;
        }
        
        public void run() {
        	Panel.UpdaterHandler.obtainMessage(TMyUserPanel.MESSAGE_UPDATESTATUS).sendToTarget();
        }
    }
    
    private void UpdateStatus() {
    	if (UserInfo != null) {
        	if (UserInfo.UserIsOnline) {
        		edUserConnectionState.setText(getString(R.string.SOnline));
        		edUserConnectionState.setTextColor(Color.GREEN);
        	}
        	else {
        		edUserConnectionState.setText(getString(R.string.SOffline));
        		edUserConnectionState.setTextColor(Color.RED);
        	}
    	}
    	else {
    		edUserConnectionState.setText("?");
    		edUserConnectionState.setTextColor(Color.GRAY);
    	}
    	//.
    	TTracker Tracker = TTracker.GetTracker(); 
    	if ((Tracker != null) && Tracker.GeoLog.IsEnabled()) {
            //. GPS module info
            if (Tracker.GeoLog.GPSModule.flProcessing) {
                btnGetUserLocation.setEnabled(true);
                if (Tracker.GeoLog.GPSModule.flGPSFixing) {
                    TGPSFixValue fix = Tracker.GeoLog.GPSModule.GetCurrentFix();
                    double TimeDelta = OleDate.UTCCurrentTimestamp()-fix.ArrivedTimeStamp;
                    int Seconds = (int)(TimeDelta*24.0*3600.0);
                    String S;
                    if (Seconds > 60)
                    	S = Integer.toString((int)(Seconds/60))+getString(R.string.SMinsAgo)+Integer.toString((int)(Seconds % 60))+getString(R.string.SSecsAgo);
                    else
                    	S = Integer.toString(Seconds)+getString(R.string.SSecsAgo);
                    //.
                    edUserLocation.setText(getString(R.string.SAvailable1)+", "+S);
                    edUserLocation.setTextColor(Color.GREEN);
                }
                else {
                    TGPSFixValue fix = Tracker.GeoLog.GPSModule.GetCurrentFix();
                    double TimeDelta = OleDate.UTCCurrentTimestamp()-fix.ArrivedTimeStamp;
                    int Seconds = (int)(TimeDelta*24.0*3600.0);
                    String S;
                    if (Seconds > 60)
                    	S = Integer.toString((int)(Seconds/60))+getString(R.string.SMinsAgo)+Integer.toString((int)(Seconds % 60))+getString(R.string.SSecs);
                    else
                    	S = Integer.toString(Seconds)+getString(R.string.SSecs);
                    //.
                    edUserLocation.setText(getString(R.string.SUnknown)+", "+S);
                    edUserLocation.setTextColor(Color.RED);
                }
            }
            else {
                btnGetUserLocation.setEnabled(false);
            	edUserLocation.setText("?");
                edUserLocation.setTextColor(Color.RED);
            }
    	}
    	else {
        	edUserLocation.setText(getString(R.string.SDisabled1));
        	edUserLocation.setTextColor(Color.GRAY);
    	}
    }
}
