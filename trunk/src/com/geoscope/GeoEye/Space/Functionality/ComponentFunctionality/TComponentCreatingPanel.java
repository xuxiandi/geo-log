package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TMyUserPanel;
import com.geoscope.GeoEye.TTrackerPOITextPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUserDataFile;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoEye.TMyUserPanel.TConfiguration;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentFileStreaming;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TComponentCreatingPanel extends Activity {

	private static final int REQUEST_TEXTEDITOR			= 1;
	private static final int REQUEST_CAMERA 			= 2;
	private static final int REQUEST_CAMERAANDEDITOR 	= 3;
	private static final int REQUEST_VIDEOCAMERA		= 4;
	private static final int REQUEST_DRAWINGEDITOR		= 5;
	private static final int REQUEST_IMAGEDRAWINGEDITOR	= 6;
	
	private static final int DATAFILE_TYPE_TEXT 		= 1;
	private static final int DATAFILE_TYPE_IMAGE 		= 2;
	private static final int DATAFILE_TYPE_EDITEDIMAGE	= 3;
	private static final int DATAFILE_TYPE_VIDEO 		= 4;
	private static final int DATAFILE_TYPE_DRAWING 		= 5;
	private static final int DATAFILE_TYPE_FILE 		= 6;
	
    public boolean flExists = false;
    //.
    private int 	idTOwner;
    private long 	idOwner;
	//.
	private TMyUserPanel.TConfiguration Configuration;
    //.
	private CheckBox cbDataName;
	private Button btnAddTextDataFile;
	private Button btnAddImageDataFile;
	private Button btnAddVideoDataFile;
	private Button btnAddDrawingDataFile;
	private Button btnAddFileDataFile;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		Bundle extras = getIntent().getExtras();
		if (extras != null) { 
			idTOwner = extras.getInt("idTOwner");
			idOwner = extras.getLong("idOwner");
		}
		//.
		Configuration = new TMyUserPanel.TConfiguration();
        try {
			Configuration.Load();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
		}
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.componentcreating_panel);
        //.
        cbDataName = (CheckBox)findViewById(R.id.cbDataName);
        cbDataName.setChecked(Configuration.ActivityConfiguration.flDataParameters);
        cbDataName.setOnClickListener(new OnClickListener() {
        	
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
                //.
                Configuration.ActivityConfiguration.flDataParameters = checked;
                Configuration.flChanged = true;
            }
        });
        //.
        btnAddTextDataFile = (Button)findViewById(R.id.btnAddTextDataFile);
        btnAddTextDataFile.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(DATAFILE_TYPE_TEXT);
				}
				catch (Exception E) {
        			Toast.makeText(TComponentCreatingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnAddImageDataFile = (Button)findViewById(R.id.btnAddImageDataFile);
        btnAddImageDataFile.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(DATAFILE_TYPE_IMAGE);
				}
				catch (Exception E) {
        			Toast.makeText(TComponentCreatingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnAddImageDataFile.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				final CharSequence[] _items;
				_items = new CharSequence[2];
				_items[0] = getString(R.string.SAddImage);
				_items[1] = getString(R.string.STakePictureWithCameraAndEdit);
				AlertDialog.Builder builder = new AlertDialog.Builder(TComponentCreatingPanel.this);
				builder.setTitle(R.string.SOperations);
				builder.setNegativeButton(getString(R.string.SCancel),null);
				builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						//.
		            	try {
							switch (arg1) {
							
							case 0: //. take a picture
			    				AddDataFile(DATAFILE_TYPE_IMAGE);
								break; //. >
								
							case 1: //. take a picture and edit
			    				AddDataFile(DATAFILE_TYPE_EDITEDIMAGE);
								break; //. >
							}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TComponentCreatingPanel.this, TComponentCreatingPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
				//.
				return true;
			}
		});
        btnAddVideoDataFile = (Button)findViewById(R.id.btnAddVideoDataFile);
        btnAddVideoDataFile.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(DATAFILE_TYPE_VIDEO);
				}
				catch (Exception E) {
        			Toast.makeText(TComponentCreatingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnAddDrawingDataFile = (Button)findViewById(R.id.btnAddDrawingDataFile);
        btnAddDrawingDataFile.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(DATAFILE_TYPE_DRAWING);
				}
				catch (Exception E) {
        			Toast.makeText(TComponentCreatingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnAddFileDataFile = (Button)findViewById(R.id.btnAddFileDataFile);
        btnAddFileDataFile.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
        		try {
    				AddDataFile(DATAFILE_TYPE_FILE);
				}
				catch (Exception E) {
        			Toast.makeText(TComponentCreatingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
	}

	@Override
	protected void onDestroy() {
		flExists = false;
    	//.
    	if ((Configuration != null) && Configuration.flChanged) {
    		try {
				Configuration.Save();
			} catch (Exception E) {
    			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
			}
    		Configuration = null;
    	}
		//.
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}
	
    @Override
    public void onPause() {
    	super.onPause();
    }    
    
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_TEXTEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	final String POIText = extras.getString("Text");
                	try {
                    	if (Configuration.ActivityConfiguration.flDataParameters) 
                    		DataFileName_Dialog(TMyUserPanel.TConfiguration.TActivityConfiguration.DataNameMaxSize, new TOnDataFileNameHandler() {
                    			
                    			@Override
                    			public void DoOnDataFileNameHandler(String Name)  throws Exception {
                    	    		EnqueueDataFile(DATAFILE_TYPE_TEXT, POIText,Name);
                    			}
                    		});
                    	else 
            	    		EnqueueDataFile(DATAFILE_TYPE_TEXT, POIText,null);
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >

        case REQUEST_CAMERA: 
        	if (resultCode == RESULT_OK) {  
				final File F = GetImageTempFile(this);
				if (F.exists()) {
					try {
		            	if (Configuration.ActivityConfiguration.flDataParameters) 
		            		DataFileName_Dialog(TMyUserPanel.TConfiguration.TActivityConfiguration.DataNameMaxSize, new TOnDataFileNameHandler() {
		            			
		            			@Override
		            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
		            	    		EnqueueDataFile(DATAFILE_TYPE_IMAGE, F,Name);
		            			}
		            		});
		            	else 
		    	    		EnqueueDataFile(DATAFILE_TYPE_IMAGE, F,null);
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

        case REQUEST_CAMERAANDEDITOR: 
        	if (resultCode == RESULT_OK) {  
				final File F = GetImageTempFile(this);
				TAsyncProcessing PictureProcessing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
					
					private File ImageFile = null;
					
					@Override
					public void Process() throws Exception {
				    	TTracker Tracker = TTracker.GetTracker();
				    	if (Tracker == null)
				    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
						//.
		            	if (F.exists()) {
	            			FileInputStream fs = new FileInputStream(F);
	            			try {
	            				BitmapFactory.Options options = new BitmapFactory.Options();
	            				options.inDither=false;
	            				options.inPurgeable=true;
	            				options.inInputShareable=true;
	            				options.inTempStorage=new byte[1024*256]; 							
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
	            							Canceller.Check();
	            							//.
	            							if (!resizedBitmap.compress(CompressFormat.JPEG, 100, bos)) 
	            								throw new Exception("error of compressing the picture to JPEG format"); //. =>
	            							//.
	            							Canceller.Check();
	            							//.
	            							byte[] ImageData = bos.toByteArray();
	            							//.
	            							ImageFile = GetImageTempFile(context);
	            							FileOutputStream FOS = new FileOutputStream(ImageFile);
	            							try {
	            								FOS.write(ImageData);
	            							}
	            							finally {
	            								FOS.close();
	            							}
	            						}
	            						finally {
	            							bos.close();
	            						}
	            					}
	            					finally {
	            						if (resizedBitmap != bitmap)
	            							resizedBitmap.recycle();
	            					}
	            				}
	            				finally {
	            					bitmap.recycle();
	            				}
	            			}
	            			finally
	            			{
	            				fs.close();
	            			}
		            	}
						else
		        			throw new Exception(context.getString(R.string.SImageWasNotPrepared)); //. =>  
					}
					
					@Override
					public void DoOnCompleted() throws Exception {
						if (Canceller.flCancel)
							return; //. ->
						if (!flExists)
							return; //. ->
                    	//.
						if (ImageFile != null) {
					    	String FileName = TGeoLogApplication.GetTempFolder()+"/"+"ComponentCreatingImageDrawing"+"."+TDrawingDefines.FileExtension;
					    	//.
					    	Intent intent = new Intent(TComponentCreatingPanel.this, TDrawingEditor.class);
					    	//.
					    	intent.putExtra("ImageFileName", ImageFile.getAbsolutePath());
					    	//.
					    	intent.putExtra("FileName", FileName); 
					    	intent.putExtra("ReadOnly", false); 
					    	intent.putExtra("SpaceContainersAvailable", false); 
					    	startActivityForResult(intent, REQUEST_IMAGEDRAWINGEDITOR);    		
						}
					}
					
					@Override
					public void DoOnException(Exception E) {
						if (Canceller.flCancel)
							return; //. ->
						if (!flExists)
							return; //. ->
						//.
						Toast.makeText(TComponentCreatingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					}
				};
				PictureProcessing.Start();
        	}  
            break; //. >

        case REQUEST_VIDEOCAMERA: 
        	if (resultCode == RESULT_OK) {  
            	try {
    				final File F = GetVideoTempFile(this);
    				if (F.exists()) {
		            	if (Configuration.ActivityConfiguration.flDataParameters) 
		            		DataFileName_Dialog(TMyUserPanel.TConfiguration.TActivityConfiguration.DataNameMaxSize, new TOnDataFileNameHandler() {
		            			
		            			@Override
		            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
		            	    		EnqueueDataFile(DATAFILE_TYPE_VIDEO, F,Name);
		            			}
		            		});
		            	else 
		    	    		EnqueueDataFile(DATAFILE_TYPE_VIDEO, F,null);
    				}
    				else
            			Toast.makeText(this, R.string.SVideoWasNotPrepared, Toast.LENGTH_SHORT).show();  
				}
				catch (Exception E) {
        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
			}
            break; //. >

        case REQUEST_DRAWINGEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	final String DrawingFileName = extras.getString("FileName");
                	try {
		            	if (Configuration.ActivityConfiguration.flDataParameters) 
		            		DataFileName_Dialog(TMyUserPanel.TConfiguration.TActivityConfiguration.DataNameMaxSize, new TOnDataFileNameHandler() {
		            			
		            			@Override
		            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
		            	    		EnqueueDataFile(DATAFILE_TYPE_DRAWING, DrawingFileName,Name);
		            			}
		            		});
		            	else 
		    	    		EnqueueDataFile(DATAFILE_TYPE_DRAWING, DrawingFileName,null);
					}
					catch (Exception E) {
	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
					}
                }
			}
            break; //. >
            
        case REQUEST_IMAGEDRAWINGEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	String DrawingFileName = extras.getString("FileName");
                	final File F = new File(DrawingFileName);
                	if (F.exists()) {
        				TAsyncProcessing PictureProcessing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
        					
        					private File ImageFile;
        					
        					@Override
        					public void Process() throws Exception {
                    	    	FileInputStream FIS = new FileInputStream(F);
                    	    	try {
                            		byte[] DRW = new byte[(int)F.length()];
                        			FIS.read(DRW);
                                	//.
        							TDrawings Drawings = new TDrawings();
        							Drawings.LoadFromByteArray(DRW,0);
        							Bitmap Image = Drawings.ToBitmap(Drawings.GetOptimalSize());
        							//.
            						ByteArrayOutputStream bos = new ByteArrayOutputStream();
            						try {
            							if (!Image.compress(CompressFormat.JPEG, 100, bos)) 
            								throw new Exception("error of compressing the picture to JPEG format"); //. =>
            							byte[] ImageData = bos.toByteArray();
            							//.
            							ImageFile = GetImageTempFile(context);
            							FileOutputStream FOS = new FileOutputStream(ImageFile);
            							try {
            								FOS.write(ImageData);
            							}
            							finally {
            								FOS.close();
            							}
            						}
            						finally {
            							bos.close();
            						}
                    	    	}
                    	    	finally {
                    	    		FIS.close();
                    	    	}
        						//.
        						F.delete();
        					}
        					
        					@Override
        					public void DoOnCompleted() throws Exception {
        						if (Canceller.flCancel)
        							return; //. ->
        						if (!flExists)
        							return; //. ->
                            	//.
        						if (ImageFile.exists()) {
        							try {
        				            	if (Configuration.ActivityConfiguration.flDataParameters) 
        				            		DataFileName_Dialog(TConfiguration.TActivityConfiguration.DataNameMaxSize, new TOnDataFileNameHandler() {
        				            			
        				            			@Override
        				            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
        				            	    		EnqueueDataFile(DATAFILE_TYPE_EDITEDIMAGE, ImageFile,Name);
        				            			}
        				            		});
        				            	else 
        				    	    		EnqueueDataFile(DATAFILE_TYPE_EDITEDIMAGE, ImageFile,null);
        							}
        							catch (Throwable E) {
        								String S = E.getMessage();
        								if (S == null)
        									S = E.getClass().getName();
        			        			Toast.makeText(TComponentCreatingPanel.this, S, Toast.LENGTH_LONG).show();  						
        							}
        						}
        						else
        		        			Toast.makeText(TComponentCreatingPanel.this, R.string.SImageWasNotPrepared, Toast.LENGTH_SHORT).show();  
        					}
        					
        					@Override
        					public void DoOnException(Exception E) {
        						if (Canceller.flCancel)
        							return; //. ->
        						if (!flExists)
        							return; //. ->
        						//.
        						Toast.makeText(TComponentCreatingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
        					}
        				};
        				PictureProcessing.Start();
                	}
                }
			}
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void AddDataFile(final int DataFileType) throws IOException {
    	DoAddDataFile(DataFileType);
    }
    
    private void DoAddDataFile(int DataFileType) throws IOException {
		if (!TTracker.TrackerIsEnabled()) 
			throw new IOException(getString(R.string.STrackerIsNotActive)); //. =>
		switch (DataFileType) {
		
		case DATAFILE_TYPE_TEXT:
    		Intent intent = new Intent(TComponentCreatingPanel.this, TTrackerPOITextPanel.class);
            startActivityForResult(intent,REQUEST_TEXTEDITOR);
			break; //. >
			
		case DATAFILE_TYPE_IMAGE:
  		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TComponentCreatingPanel.this.GetImageTempFile(TComponentCreatingPanel.this))); 
  		    startActivityForResult(intent, REQUEST_CAMERA);    		
			break; //. >
			
		case DATAFILE_TYPE_EDITEDIMAGE:
  		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TComponentCreatingPanel.this.GetImageTempFile(TComponentCreatingPanel.this))); 
  		    startActivityForResult(intent, REQUEST_CAMERAANDEDITOR);    		
			break; //. >
			
		case DATAFILE_TYPE_VIDEO:
  		    intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
  		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(TComponentCreatingPanel.this.GetVideoTempFile(TComponentCreatingPanel.this))); 
  		    startActivityForResult(intent, REQUEST_VIDEOCAMERA);    		
			break; //. >

		case DATAFILE_TYPE_DRAWING:
    		intent = new Intent(TComponentCreatingPanel.this, TDrawingEditor.class);
    		File F = GetDrawingTempFile(this);
    		F.delete();
  		    intent.putExtra("FileName", F.getAbsolutePath()); 
  		    intent.putExtra("ReadOnly", false); 
  	    	intent.putExtra("SpaceContainersAvailable", true); 
  		    startActivityForResult(intent, REQUEST_DRAWINGEDITOR);    		
			break; //. >
			
		case DATAFILE_TYPE_FILE:
			TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(TComponentCreatingPanel.this, null, new TFileSystemFileSelector.OpenDialogListener() {
	        	
	        	@Override
	            public void OnSelectedFile(String fileName) {
	        		final String SelectedFileName = fileName; 
                    //.
					try {
		            	if (Configuration.ActivityConfiguration.flDataParameters) 
		            		DataFileName_Dialog(TMyUserPanel.TConfiguration.TActivityConfiguration.DataNameMaxSize, new TOnDataFileNameHandler() {
		            			
		            			@Override
		            			public void DoOnDataFileNameHandler(String Name)  throws Exception {
		            	    		EnqueueDataFile(DATAFILE_TYPE_FILE, SelectedFileName,Name);
		            			}
		            		});
		            	else 
		    	    		EnqueueDataFile(DATAFILE_TYPE_FILE, SelectedFileName,null);
					}
					catch (Throwable E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
	        			Toast.makeText(TComponentCreatingPanel.this, S, Toast.LENGTH_SHORT).show();  						
					}
	            }

				@Override
				public void OnCancel() {
				}
	        });
	    	FileSelector.show();    	
			break; //. >
		}
    }
    
    private void EnqueueDataFile(int DataFileType, Object Data, String DataName) throws Exception {
    	if ((DataName != null) && (DataName.length() > 0))
    		DataName = "@"+TComponentFileStreaming.CheckAndEncodeFileNameString(DataName);
    	else
    		DataName = "";
    	//.
		switch (DataFileType) {
		
		case DATAFILE_TYPE_TEXT: {
			String POIText = (String)Data;
    		if (POIText.equals(""))
    			throw new Exception(getString(R.string.STextIsNull)); //. =>
    		//.
    		byte[] TextBA = POIText.getBytes("windows-1251");
    		//.
        	double Timestamp = OleDate.UTCCurrentTimestamp();
    		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+".txt";
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
    		/*TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
    			@Override
    			public void Process() throws Exception {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
			    	DataFile.SendViaDevice(Tracker.GeoLog);
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
    		Processing.Start();*/
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
	    	DataFile.CreateAsComponent(idTOwner,idOwner, Tracker.GeoLog);
	    	//.
    		Toast.makeText(this, getString(R.string.STextIsAdded)+Integer.toString(DataFileSize), Toast.LENGTH_LONG).show();
			break; //. >
		}
			
		case DATAFILE_TYPE_IMAGE: 
		case DATAFILE_TYPE_EDITEDIMAGE: {
			//. try to gc
			TGeoLogApplication.Instance().GarbageCollector.Collect();
			//.
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	//.
			File F = (File)Data;
			FileInputStream fs = new FileInputStream(F);
			try
			{
				byte[] PictureBA;
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inDither=false;
				options.inPurgeable=true;
				options.inInputShareable=true;
				options.inTempStorage=new byte[1024*256]; 							
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
						if (resizedBitmap != bitmap)
							resizedBitmap.recycle();
					}
				}
				finally {
					bitmap.recycle();
				}
				//.
            	double Timestamp = OleDate.UTCCurrentTimestamp();
        		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+".jpg";
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
	    		/* TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
	    			@Override
	    			public void Process() throws Exception {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				    	TTracker Tracker = TTracker.GetTracker();
				    	if (Tracker == null)
				    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
				    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
				    	DataFile.SendViaDevice(Tracker.GeoLog);
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
	    		Processing.Start();*/
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
		    	Tracker = TTracker.GetTracker();
		    	if (Tracker == null)
		    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
		    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
		    	DataFile.CreateAsComponent(idTOwner,idOwner, Tracker.GeoLog);
		    	//.
	        	Toast.makeText(this, getString(R.string.SImageIsAdded)+Integer.toString(DataFileSize), Toast.LENGTH_LONG).show();
			}
			finally
			{
				fs.close();
			}
			break; //. >
		}
			
		case DATAFILE_TYPE_VIDEO: {
			//. try to gc
			TGeoLogApplication.Instance().GarbageCollector.Collect();
			//.
			File F = (File)Data;
			//.
        	double Timestamp = OleDate.UTCCurrentTimestamp();
    		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TFileSystem.FileName_GetExtension(F.getName());
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
    		/* TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
    			@Override
    			public void Process() throws Exception {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
			    	DataFile.SendViaDevice(Tracker.GeoLog);
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
    		Processing.Start(); */
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
	    	DataFile.CreateAsComponent(idTOwner,idOwner, Tracker.GeoLog);
	    	//.
    		Toast.makeText(this, getString(R.string.SDataIsAdded)+Integer.toString((int)(DataFileSize/1024))+getString(R.string.SKb), Toast.LENGTH_LONG).show();
			break; //. >
		}

		case DATAFILE_TYPE_DRAWING: {
			String DrawingFileName = (String)Data;
			//.
        	double Timestamp = OleDate.UTCCurrentTimestamp();
    		String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TDrawingDefines.FileExtension;
    		File NF = new File(NFN);
    		if (!(new File(DrawingFileName)).renameTo(NF))
    			throw new IOException("could not rename file: "+DrawingFileName); //. =>
    		//. prepare and send datafile
    		final String 	DataFileName = NFN;
    		final long		DataFileSize = NF.length();
    		/*TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
    			@Override
    			public void Process() throws Exception {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
			    	DataFile.SendViaDevice(Tracker.GeoLog);
    			}
    			@Override 
    			public void DoOnCompleted() throws Exception {
            		Toast.makeText(TMyUserPanel.this, getString(R.string.SDrawingIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
    			}
    			@Override
    			public void DoOnException(Exception E) {
    				Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    			}
    		};
    		Processing.Start();*/
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
	    	DataFile.CreateAsComponent(idTOwner,idOwner, Tracker.GeoLog);
	    	//.
    		Toast.makeText(this, getString(R.string.SDrawingIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
			break; //. >
		}
			
		case DATAFILE_TYPE_FILE: {
			String FileName = (String)Data;
			//.
	    	double Timestamp = OleDate.UTCCurrentTimestamp();
			String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+DataName+"."+TFileSystem.FileName_GetExtension(FileName);
			File NF = new File(NFN);
			TFileSystem.CopyFile(new File(FileName), NF);
			//. prepare and send datafile
			final String 	DataFileName = NFN;
			final long		DataFileSize = NF.length();
			/*TAsyncProcessing Processing = new TAsyncProcessing(TMyUserPanel.this,getString(R.string.SWaitAMoment)) {
				@Override
				public void Process() throws Exception {
					TUserAgent UserAgent = TUserAgent.GetUserAgent();
					if (UserAgent == null)
						throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			    	TTracker Tracker = TTracker.GetTracker();
			    	if (Tracker == null)
			    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
			    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User, Timestamp,DataFileName);
			    	DataFile.SendViaDevice(Tracker.GeoLog);
				}
				@Override 
				public void DoOnCompleted() throws Exception {
	        		Toast.makeText(TMyUserPanel.this, getString(R.string.SFileIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
				}
				@Override
				public void DoOnException(Exception E) {
					Toast.makeText(TMyUserPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
			};
			Processing.Start();*/
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    	TTracker Tracker = TTracker.GetTracker();
	    	if (Tracker == null)
	    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
	    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(UserAgent.User(), Timestamp,DataFileName);
	    	DataFile.CreateAsComponent(idTOwner,idOwner, Tracker.GeoLog);
	    	//.
			Toast.makeText(this, getString(R.string.SFileIsAdded)+Long.toString(DataFileSize), Toast.LENGTH_LONG).show();
			break; //. >
		}
		}
        //.
        setResult(RESULT_OK);
    }
    
    protected File GetImageTempFile(Context context) {
  	  	return new File(TGeoLogApplication.GetTempFolder(),"Image.jpg");
    }
  
    protected File GetVideoTempFile(Context context) {
    	return new File(TGeoLogApplication.GetTempFolder(),"Video.3gp");
    }

    protected File GetDrawingTempFile(Context context) {
    	return new File(TGeoLogApplication.GetTempFolder(),"Drawing"+"."+TDrawingDefines.FileExtension);
    }

    private static class TOnDataFileNameHandler {
    	
    	public void DoOnDataFileNameHandler(String Name) throws Exception {
    	}
    }
    
    private void DataFileName_Dialog(final int DataNameMaxSize, final TOnDataFileNameHandler OnDataFileNameHandler) {
		final EditText input = new EditText(this);
		input.setInputType(InputType.TYPE_CLASS_TEXT);
		//.
		final AlertDialog dlg = new AlertDialog.Builder(this)
		//.
		.setTitle(R.string.SDataName)
		.setMessage(R.string.SEnterName)
		//.
		.setView(input)
		.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				//. hide keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
				//.
				try {
					String Name = input.getText().toString();
    				if (Name.length() > DataNameMaxSize)
    					Name = Name.substring(0,DataNameMaxSize);
    				//.
					OnDataFileNameHandler.DoOnDataFileNameHandler(Name);
				} catch (Exception E) {
					Toast.makeText(TComponentCreatingPanel.this, E.getMessage(),	Toast.LENGTH_LONG).show();
				}
			}
		})
		//.
		.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int whichButton) {
				// . hide keyboard
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
			}
		}).create();
		//.
		input.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				dlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick(); 
				return false;
			}
        });        
		// .
		dlg.show();
    }
}
