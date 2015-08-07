package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TIncomingMessage;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TIncomingXMLDataMessage;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUserSession;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUserSession.TUserMessage;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

@SuppressLint("HandlerLeak")
public class TUserChatPanel extends Activity {

	public static Hashtable<Long, TUserChatPanel> Panels = new Hashtable<Long, TUserChatPanel>();

	public static final int ContactUserInfoUpdateInterval = 1000*60; //. seconds
	public static final int ContactUserStateUpdateCounter = 10; 
	public static final int MessageIsProcessedDelay = 1000*1; //. seconds
	//.
	public static final int CameraImageMaxSize = 1024;
	
	public static final String MessageTextFileName 		= "MessageText.txt";
	public static final String MessagePictureFileName 	= "MessagePicture.png";
	
	private static final int MESSAGE_SENT 					= 1;
	private static final int MESSAGE_RECEIVED 				= 2;
	
	private static final int REQUEST_DRAWINGEDITOR				= 1;
	private static final int REQUEST_ADDPICTUREFROMCAMERA		= 2;
	private static final int REQUEST_ADDEDITEDPICTUREFROMCAMERA	= 3;
	private static final int REQUEST_IMAGEDRAWINGEDITOR			= 4;
	
	private static class TMessageAsProcessedMarking extends TCancelableThread {
		
		private TIncomingMessage Message;
		private int Delay;
		
		public TMessageAsProcessedMarking(TIncomingMessage pMessage, int pDelay) {
    		super();
    		//.
			Message = pMessage;
			Delay = pDelay;
			//.
			_Thread = new Thread(this);
		}
		
		public void Start() {
			_Thread.start();
		}
		
		@Override
		public void run() {
			try {
				Thread.sleep(Delay);
				//.
				Message.SetAsProcessed();
			} catch (InterruptedException E) {
			} catch (Throwable E) {
			}
		}
	}
	
	private boolean flExists = false;
	//.
	private TUserAgent UserAgent;
	//.
	private TGeoScopeServerUser.TUserDescriptor 	ContactUser = new TGeoScopeServerUser.TUserDescriptor();
	private TContactUserUpdating    				ContactUserUpdating = null;
	private TContactUserStateUpdating    			ContactUserStateUpdating = null;
	//.
	private int UserIncomingMessages_LastCheckInterval = -1;
	private ArrayList<TMessageAsProcessedMarking> MessageAsProcessedMarkingList = new ArrayList<TUserChatPanel.TMessageAsProcessedMarking>();
	//.
	private TextView 		lbUserChatContactUser;
	private LinearLayout 	llUserChatContactUserState;
	private TextView 		lbUserChatContactUserState;
	private ScrollView svUserChatArea;
	private LinearLayout llUserChatArea;
	private EditText edUserChatComposeMessage;
	private Button btnUserChatComposeMessageSend;
	private Button btnUserChatTextEntry;
	private Button btnUserChatDrawingSend;
	private Button btnUserChatPictureSend;
	//.
	private BroadcastReceiver EventReceiver = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		try {
			UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
		} catch (Exception E) {
            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
            finish();
            return; //. ->
		}
    	TIncomingMessage Message = null;
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	ContactUser.UserID = extras.getLong("UserID");
        	ContactUser.UserIsDisabled = extras.getBoolean("UserIsDisabled");
        	ContactUser.UserIsOnline = extras.getBoolean("UserIsOnline");
        	ContactUser.UserName = extras.getString("UserName");
        	ContactUser.UserFullName = extras.getString("UserFullName");
        	ContactUser.UserContactInfo = extras.getString("UserContactInfo");
        	//.
        	long MessageID = extras.getLong("MessageID");
        	if ((MessageID != 0) && (UserAgent.Server.User != null) && (UserAgent.Server.User.IncomingMessages != null))
        		Message = UserAgent.Server.User.IncomingMessages.GetMessageByID(MessageID);
        }
        //.
        if (Message != null) {
    		TUserChatPanel UCP = Panels.get(Message.SenderID);
    		if (UCP != null) {
    			try {
        			UCP.PublishMessage(Message);
    			} catch (Exception E) {
    	            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
    	            finish();
    	            return; //. ->
    			}
    			//.
    			finish();
    			return; //. ->
    		}
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.userchat_panel);
        //.
        lbUserChatContactUser = (TextView)findViewById(R.id.lbUserChatContactUser);
        llUserChatContactUserState = (LinearLayout)findViewById(R.id.llUserChatContactUserState);
        lbUserChatContactUserState = (TextView)findViewById(R.id.lbUserChatContactUserState);
        //.
        svUserChatArea = (ScrollView)findViewById(R.id.svUserChatArea);
        llUserChatArea = (LinearLayout)findViewById(R.id.llUserChatArea);
        //.
        edUserChatComposeMessage = (EditText)findViewById(R.id.edUserChatComposeMessage);
        edUserChatComposeMessage.setOnEditorActionListener(new OnEditorActionListener() { 
        	
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if(arg1 == EditorInfo.IME_ACTION_DONE){
                	String Message = edUserChatComposeMessage.getText().toString();
                	if (!Message.equals(""))
                		SendMessage(Message);
                }
				return false;
			}
        });  
        btnUserChatComposeMessageSend = (Button)findViewById(R.id.btnUserChatComposeMessageSend);
        btnUserChatComposeMessageSend.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				String Message = edUserChatComposeMessage.getText().toString();
            	if (!Message.equals(""))
            		SendMessage(Message);
            }
        });
        //.
        btnUserChatTextEntry = (Button)findViewById(R.id.btnUserChatTextEntry);
        btnUserChatTextEntry.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(edUserChatComposeMessage, InputMethodManager.SHOW_FORCED);
			}
        });
        //.
        btnUserChatDrawingSend = (Button)findViewById(R.id.btnUserChatDrawingSend);
        btnUserChatDrawingSend.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				SendDrawing();
			}
        });
        //.
        btnUserChatPictureSend = (Button)findViewById(R.id.btnUserChatPictureSend);
        btnUserChatPictureSend.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				SendPicture();
			}
        });
        //.
        ContactUserUpdating = new TContactUserUpdating();
        //.
        TGeoScopeServerUserSession UserSession = UserAgent.User().GetSession(); 
        if (UserSession != null) {
            ContactUserStateUpdating = new TContactUserStateUpdating(UserSession);
			//.
            llUserChatContactUserState.setVisibility(View.VISIBLE);
        }
        else {
        	ContactUserStateUpdating = null;
            //.
            llUserChatContactUserState.setVisibility(View.GONE);
        }
        //.
        if (Message != null) 
			try {
	        	PublishMessage(Message);
			} catch (Exception E) {
	            Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
	            finish();
	            return; //. ->
			}
        //.
    	if ((UserAgent.Server.User != null) && (UserAgent.Server.User.IncomingMessages != null))
    		UserIncomingMessages_LastCheckInterval = UserAgent.Server.User.IncomingMessages.SetFastCheckInterval(); //. speed up messages updating
		//.
		EventReceiver = new BroadcastReceiver() {
			
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
					TUserChatPanel.this.finish();
					//.
					return; // . ->
				}
			}
		};
		IntentFilter ScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		getApplicationContext().registerReceiver(EventReceiver, ScreenOffFilter);
		//.
        UpdateContactUserInfo();
        //.
        Panels.put(ContactUser.UserID, this);
        //.
        flExists = true;
	}
	
    @Override
	protected void onDestroy() {
    	flExists = false;
    	//.
    	Panels.remove(ContactUser.UserID);
		//.
		if (EventReceiver != null) {
			getApplicationContext().unregisterReceiver(EventReceiver);
			EventReceiver = null;
		}
    	//.
    	if (MessageAsProcessedMarkingList != null) {
    		for (int I = 0; I < MessageAsProcessedMarkingList.size(); I++)
    			MessageAsProcessedMarkingList.get(I).Cancel();
    		//.
    		MessageAsProcessedMarkingList = null;
    	}
    	//.
    	if (UserIncomingMessages_LastCheckInterval >= 0) {
    		try {
            	if ((UserAgent.Server.User != null) && (UserAgent.Server.User.IncomingMessages != null))
            		UserAgent.Server.User.IncomingMessages.RestoreCheckInterval(UserIncomingMessages_LastCheckInterval);
    		} catch (Exception E) {
    		}
    		//.
    		UserIncomingMessages_LastCheckInterval = -1;
    	}
        //.
    	if (ContactUserStateUpdating != null) {
    		ContactUserStateUpdating.Cancel();
    		ContactUserStateUpdating = null;
    	}
        //.
    	if (ContactUserUpdating != null) {
    		ContactUserUpdating.Cancel();
    		ContactUserUpdating = null;
    	}
    	//.
		super.onDestroy();
	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        
        case REQUEST_DRAWINGEDITOR: 
        	if (resultCode == RESULT_OK) {  
                Bundle extras = data.getExtras(); 
                if (extras != null) {
                	String DrawingFileName = extras.getString("FileName");
                	File F = new File(DrawingFileName);
                	if (F.exists()) {
                    	try {
                	    	FileInputStream FIS = new FileInputStream(F);
                	    	try {
                        		byte[] DRW = new byte[(int)F.length()];
                    			FIS.read(DRW);
                    			//.
                            	TIncomingXMLDataMessage IDM = new TIncomingXMLDataMessage(TDrawingDefines.FileExtension,DRW);
                            	//.
                            	new TMessageSending(IDM,F,MESSAGE_SENT);
                	    	}
                	    	finally {
                	    		FIS.close();
                	    	}
    					}
    					catch (Exception E) {
    	        			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
    					}
                	}
                }
			}
            break; //. >
            
        case REQUEST_ADDPICTUREFROMCAMERA: 
        	if (resultCode == RESULT_OK) {  
				final File F = GetCameraPictureTempFile(this);
				TAsyncProcessing PictureProcessing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
					
					private TIncomingXMLDataMessage IDM;
					
					@Override
					public void Process() throws Exception {
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
	            					float MaxSize = CameraImageMaxSize;
	            					float Scale = MaxSize/ImageMaxSize; 
	            					Matrix matrix = new Matrix();     
	            					matrix.postScale(Scale,Scale);
	            					//.
	            					Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
	            					try {
	            						ByteArrayOutputStream bos = new ByteArrayOutputStream();
	            						try {
	            							if (!resizedBitmap.compress(CompressFormat.JPEG, 100, bos)) 
	            								throw new Exception("error of compressing the picture to JPEG format"); //. =>
	            							byte[] PictureBA = bos.toByteArray();
	            							//.
	                                    	IDM = new TIncomingXMLDataMessage(TFileSystem.FileName_GetExtension(F.getAbsolutePath()),PictureBA);
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
                    	new TMessageSending(IDM,null,MESSAGE_SENT);
					}
					
					@Override
					public void DoOnException(Exception E) {
						if (Canceller.flCancel)
							return; //. ->
						if (!flExists)
							return; //. ->
						//.
						Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					}
				};
				PictureProcessing.Start();
        	}  
            break; //. >
            
        case REQUEST_ADDEDITEDPICTUREFROMCAMERA: 
        	if (resultCode == RESULT_OK) {  
				final File F = GetCameraPictureTempFile(this);
				TAsyncProcessing PictureProcessing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
					
					private File ImageFile = null;
					
					@Override
					public void Process() throws Exception {
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
	            					float MaxSize = CameraImageMaxSize;
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
	            							ImageFile = GetCameraPictureTempFile(context);
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
					    	String FileName = TGeoLogApplication.GetTempFolder()+"/"+"UserChatImageDrawing"+"."+TDrawingDefines.FileExtension;
					    	//.
					    	Intent intent = new Intent(TUserChatPanel.this, TDrawingEditor.class);
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
						Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					}
				};
				PictureProcessing.Start();
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
        					
        					private TIncomingXMLDataMessage IDM;
        					
        					@Override
        					public void Process() throws Exception {
                    	    	FileInputStream FIS = new FileInputStream(F);
                    	    	try {
                            		byte[] DRW = new byte[(int)F.length()];
                        			FIS.read(DRW);
                                	//.
        							TDrawings Drawings = new TDrawings();
        							Drawings.LoadFromByteArray(DRW,0);
        							Bitmap Image = Drawings.ToBitmap(CameraImageMaxSize);
        							//.
            						ByteArrayOutputStream bos = new ByteArrayOutputStream();
            						try {
            							if (!Image.compress(CompressFormat.JPEG, 100, bos)) 
            								throw new Exception("error of compressing the picture to JPEG format"); //. =>
            							byte[] ImageData = bos.toByteArray();
                                    	IDM = new TIncomingXMLDataMessage("jpg", ImageData);
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
                            	new TMessageSending(IDM,null,MESSAGE_SENT);
        					}
        					
        					@Override
        					public void DoOnException(Exception E) {
        						if (Canceller.flCancel)
        							return; //. ->
        						if (!flExists)
        							return; //. ->
        						//.
        						Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
    
    private void UpdateContactUserInfo() {
		String State;
		if (ContactUser.UserIsOnline)
			State = "[ONLINE]";
		else
			State = "[offline]";
        lbUserChatContactUser.setText(getString(R.string.SUser)+" "+ContactUser.UserName+" "+State+" / "+ContactUser.UserFullName);
    }
    
    private void SendMessage(String Message) {
    	TIncomingMessage IM = new TIncomingMessage();
    	IM.Message = Message;
    	new TMessageSending(IM,null,MESSAGE_SENT);
    }
    
    private void SendDrawing() {
    	String FileName = TGeoLogApplication.GetTempFolder()+"/"+"UserChatDrawing"+"."+TDrawingDefines.FileExtension;
    	//.
    	Intent intent = new Intent(TUserChatPanel.this, TDrawingEditor.class);
    	intent.putExtra("FileName", FileName); 
    	intent.putExtra("ReadOnly", false); 
    	intent.putExtra("SpaceContainersAvailable", false); 
    	startActivityForResult(intent, REQUEST_DRAWINGEDITOR);    		
    }
    
    private static File GetCameraPictureTempFile(Context context) {
    	return new File(TGeoLogApplication.TempFolder,"picture.jpg");
    }
    
    private void SendPicture() {
		final CharSequence[] _items;
		_items = new CharSequence[3];
		_items[0] = getString(R.string.SAddImage);
		_items[1] = getString(R.string.STakePictureWithCameraAndEdit);
		_items[2] = getString(R.string.SAddImageFromFile);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
		      		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(GetCameraPictureTempFile(TUserChatPanel.this))); 
		      		    startActivityForResult(intent, REQUEST_ADDPICTUREFROMCAMERA);    		
						break; //. >
						
					case 1: //. take a picture and edit
		      		    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(GetCameraPictureTempFile(TUserChatPanel.this))); 
		      		    startActivityForResult(intent, REQUEST_ADDEDITEDPICTUREFROMCAMERA);    		
						break; //. >
						
					case 2: //. take a picture form file
						TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(TUserChatPanel.this, ".BMP,.PNG,.GIF,.JPG,.JPEG", new TFileSystemFileSelector.OpenDialogListener() {
				        	
				            @Override
				            public void OnSelectedFile(String fileName) {
				                File ChosenFile = new File(fileName);
				                //.
								try {
				                	File F = new File(ChosenFile.getAbsolutePath());
				                	if (F.exists()) {
				                    	try {
				                	    	FileInputStream FIS = new FileInputStream(F);
				                	    	try {
				                        		byte[] Data = new byte[(int)F.length()];
				                    			FIS.read(Data);
				                    			//.
				                            	TIncomingXMLDataMessage IDM = new TIncomingXMLDataMessage(TFileSystem.FileName_GetExtension(ChosenFile.getAbsolutePath()),Data);
				                            	//.
				                            	new TMessageSending(IDM,null,MESSAGE_SENT);
				                	    	}
				                	    	finally {
				                	    		FIS.close();
				                	    	}
				    					}
				    					catch (Exception E) {
				    	        			Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  						
				    					}
				                	}
								}
								catch (Throwable E) {
									String S = E.getMessage();
									if (S == null)
										S = E.getClass().getName();
				        			Toast.makeText(TUserChatPanel.this, S, Toast.LENGTH_LONG).show();  						
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
				catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
        			Toast.makeText(TUserChatPanel.this, TUserChatPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
				}
			}
		});
		AlertDialog alert = builder.create();
		alert.show();
    }
    
    public void ReceiveMessage(TIncomingMessage Message) {
		PanelHandler.obtainMessage(MESSAGE_RECEIVED,Message).sendToTarget();
    }
    
    private void PublishMessage(TIncomingMessage Message) throws Exception {
		ChatArea_AddMessage(ContactUser.UserName, Message, true);
		//.
		TMessageAsProcessedMarking MessageAsProcessedMarking = new TMessageAsProcessedMarking(Message, MessageIsProcessedDelay);
		MessageAsProcessedMarkingList.add(MessageAsProcessedMarking);
		MessageAsProcessedMarking.Start();
    }
    
    private void ChatArea_AddMessage(String SenderName, TIncomingMessage Message, boolean flContactUser) throws Exception {
    	if (Message instanceof TIncomingXMLDataMessage) {
        	TIncomingXMLDataMessage DataMessage = (TIncomingXMLDataMessage)Message;
        	if (DataMessage.Data != null) {
            	TextView tvMessage = new TextView(this);
            	tvMessage.setText((new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(Message.Timestamp)).GetDateTime())+" "+SenderName+": ");
            	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            	tvMessage.setLayoutParams(LP);
            	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
            	if (flContactUser)
            		tvMessage.setTextColor(Color.BLACK);
            	else
            		tvMessage.setTextColor(Color.LTGRAY);
            	llUserChatArea.addView(tvMessage);
            	tvMessage.setVisibility(View.VISIBLE);
            	//.
            	if (DataMessage.DataType.equals(TDrawingDefines.FileExtension)) {
                	TDrawings Drawings = new TDrawings();
                	try {
                		Bitmap _BMP;
                		try {
                    		Drawings.LoadFromByteArray(DataMessage.Data,0);
                    		byte[] BMPData = Drawings.SaveAsBitmapData("png"); 
                        	_BMP = BitmapFactory.decodeByteArray(BMPData, 0,BMPData.length);
                		}
                		catch (Throwable TE) {
                			_BMP = null;
                		}
                		final Bitmap BMP = _BMP;
                		//.
                    	ImageView ivMessage = new ImageView(this);
                    	if (BMP != null)
                    		ivMessage.setImageBitmap(BMP);
                    	ivMessage.setOnLongClickListener(new OnLongClickListener() {
							
							@Override
							public boolean onLongClick(View v) {
								if (BMP == null)
									return false; //. ->
								try {
									String FN = SaveMessagePicture(BMP);
									//.
					                Toast.makeText(TUserChatPanel.this, getString(R.string.SPictureHasBeenSaved)+": "+FN, Toast.LENGTH_LONG).show();
								}
								catch (Exception E) {
					                Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
								}
								return true;
							}
						});
                    	llUserChatArea.addView(ivMessage);
                    	ivMessage.setVisibility(View.VISIBLE);
                	}
                	finally {
                		Drawings.Destroy();
                	}
            	}
            	else
                	if (DataMessage.DataType.equals("png") || DataMessage.DataType.equals("jpg") || DataMessage.DataType.equals("jpeg") || DataMessage.DataType.equals("gif") || DataMessage.DataType.equals("bmp")) {
                		Bitmap _BMP;
                		try {
                        	_BMP = BitmapFactory.decodeByteArray(DataMessage.Data, 0,DataMessage.Data.length);
                		}
                		catch (Throwable TE) {
                			_BMP = null;
                		}
                		final Bitmap BMP = _BMP;
                		//.
                    	ImageView ivMessage = new ImageView(this);
                    	if (BMP != null)
                    		ivMessage.setImageBitmap(BMP);
                    	ivMessage.setOnLongClickListener(new OnLongClickListener() {
							
							@Override
							public boolean onLongClick(View v) {
								if (BMP == null)
									return false; //. ->
								try {
									String FN = SaveMessagePicture(BMP);
									//.
					                Toast.makeText(TUserChatPanel.this, getString(R.string.SPictureHasBeenSaved)+": "+FN, Toast.LENGTH_LONG).show();
								}
								catch (Exception E) {
					                Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
								}
								return true;
							}
						});
                    	llUserChatArea.addView(ivMessage);
                    	ivMessage.setVisibility(View.VISIBLE);
                	}
        	}
    	}
    	else {
        	TextView tvMessage = new TextView(this);
        	final String Text = (new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(Message.Timestamp)).GetDateTime())+" "+SenderName+": "+Message.Message; 
        	tvMessage.setText(Text);
        	LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        	tvMessage.setLayoutParams(LP);
        	tvMessage.setTextSize(TypedValue.COMPLEX_UNIT_DIP,18);
        	if (flContactUser)
        		tvMessage.setTextColor(Color.BLACK);
        	else
        		tvMessage.setTextColor(Color.LTGRAY);
        	tvMessage.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					try {
						String FN = SaveMessageText(Text);
						//.
		                Toast.makeText(TUserChatPanel.this, getString(R.string.STextHasBeenSaved)+": "+FN, Toast.LENGTH_LONG).show();
					}
					catch (Exception E) {
		                Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
					}
					return true;
				}
			});
        	llUserChatArea.addView(tvMessage);
        	tvMessage.setVisibility(View.VISIBLE);
    	}
    	svUserChatArea.postDelayed(new Runnable() {
    	    @Override
    	    public void run() {
            	try {
                    svUserChatArea.fullScroll(View.FOCUS_DOWN);
            	}
            	catch (Throwable E) {
            		TGeoLogApplication.Log_WriteError(E);
            	}
    	    }
		},100);
    }
    
    private String SaveMessageText(String Text) throws IOException {
		String FN = TGeoLogApplication.GetTempFolder()+"/"+MessageTextFileName;
		FileOutputStream FOS = new FileOutputStream(FN);
		try {
			FOS.write(Text.getBytes("UTF-8"));
		}
		finally {
			FOS.close();
		}
		return FN;
    }
    
    private String SaveMessagePicture(Bitmap Picture) throws IOException {
		String FN = TGeoLogApplication.GetTempFolder()+"/"+MessagePictureFileName;
		FileOutputStream FOS = new FileOutputStream(FN);
		try {
			Picture.compress(CompressFormat.PNG, 100, FOS);
		}
		finally {
			FOS.close();
		}
		return FN;
    }
    
    private class TMessageSending extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TIncomingMessage 	Message;
    	private File 				MessageSourceFile;
    	//.
    	private int OnCompletionMessage;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TMessageSending(TIncomingMessage pMessage, File pMessageSourceFile, int pOnCompletionMessage) {
    		super();
    		//.
    		Message = pMessage;
    		MessageSourceFile = pMessageSourceFile;
    		//.
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    	        	if (UserAgent.Server.User != null) {
    	        		UserAgent.Server.User.IncomingMessages_SendNewMessage(ContactUser.UserID, Message.Message);
    	        		Message.Timestamp = OleDate.UTCCurrentTimestamp();
    	        	}
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
	    		//.
				if (MessageSourceFile != null)
					MessageSourceFile.delete();
				//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,Message).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (NullPointerException NPE) {
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
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
						if (!flExists)
							return; //. ->
						//.
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserChatPanel.this);    
		            	progressDialog.setMessage(TUserChatPanel.this.getString(R.string.SSending));    
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
	                	try {
		                	progressDialog.dismiss(); 
	                	}
	                	catch (IllegalArgumentException IAE) {} 
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
		
    private class TContactUserUpdating extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 		= 0;
    	private static final int MESSAGE_UPDATECONTACTUSER 	= 1;
    	
    	public TContactUserUpdating() {
    		super();
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
		        	Thread.sleep(ContactUserInfoUpdateInterval);
		        	//.
					try {
						TGeoScopeServerUser.TUserDescriptor User = null;
	    	        	if (UserAgent.Server.User != null)
	    	        		User = UserAgent.Server.User.GetUserInfo(ContactUser.UserID); 
						//.
						if (Canceller.flCancel)
							return; //. ->
			    		//.
						if (User != null)
							MessageHandler.obtainMessage(MESSAGE_UPDATECONTACTUSER, User).sendToTarget();
		        	}
		        	catch (InterruptedException E) {
		        	}
		        	catch (NullPointerException NPE) { 
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
		                Toast.makeText(TUserChatPanel.this, TUserChatPanel.this.getString(R.string.SUpdatingContactUser)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
	                case MESSAGE_UPDATECONTACTUSER:
						if (Canceller.flCancel)
			            	break; //. >
	            		TGeoScopeServerUser.TUserDescriptor User = (TGeoScopeServerUser.TUserDescriptor)msg.obj;
	            		ContactUser.Assign(User);
	            		UpdateContactUserInfo();
	                	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
    private class TContactUserStateUpdating extends TCancelableThread {

    	public static final int OnlineColor = Color.GREEN; 
    	public static final int OfflineColor = Color.LTGRAY; 
    	
    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_UPDATECONTACTUSERSTATE	= 1;
    	
    	private TGeoScopeServerUserSession UserSession;
    	//.
    	private TGeoScopeServerUserSession.TUserMessageSubscribers.TUserMessageSubscriber UserMessageSubscriber = new TGeoScopeServerUserSession.TUserMessageSubscribers.TUserMessageSubscriber() {
			
			@Override
			protected void DoOnMessageUserNotConnected(long MessageID) {
				if (LastPingMessageID == MessageID)
					if (ContactUser.UserIsOnline)
						SetOnlinePerventage(20);
					else
						SetOnlinePerventage(0);
			}
			
			@Override
			protected void DoOnMessageUserNotAvailable(TUserMessage UserMessage) {
				if (UserMessage.SenderID == ContactUser.UserID)
					SetOnlinePerventage(0);
			}
			
			@Override
			protected void DoOnMessageSentToUser(long MessageID) {
				if (LastPingMessageID == MessageID) {
					int V = 50;
					if (GetOnlinePerventage() < V) 
						SetOnlinePerventage(V);
				}
			}
			
			@Override
			protected boolean DoOnMessageReceived(TUserMessage UserMessage) {
				return true;
			}
			
			@Override
			protected void DoOnMessageDelivered(TUserMessage UserMessage) {
				if (UserMessage.SenderID == ContactUser.UserID) 
					if (UserMessage.MessageID == LastPingMessageID)
						SetOnlinePerventage(100);
					else
						SetOnlinePerventage(90);
			}
		};
		//.
		private volatile long LastPingMessageID = 0;
		//.
		private volatile int OnlinePercentage = 0;
    	
    	public TContactUserStateUpdating(TGeoScopeServerUserSession pUserSession) {
    		super();
    		//.
    		UserSession = pUserSession;
    		//.
            UserSession.UserMessageSubscribers.Subscribe(UserMessageSubscriber);
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	@Override
    	public void Destroy() throws Exception {
    		super.Destroy();
    		//.
            UserSession.UserMessageSubscribers.Unsubscribe(UserMessageSubscriber);
    	}

		@Override
		public void run() {
			try {
		    	int Count = 0;
				while (!Canceller.flCancel) {
					if ((Count % ContactUserStateUpdateCounter) == 0) {
						Count = 0;
						//.
						try {
							LastPingMessageID = UserSession.SendUserPingMessage(ContactUser.UserID);
			        	}
			        	catch (NullPointerException NPE) { 
				    		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
			        	}
			        	catch (IOException E) {
			    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
			        	}
			        	catch (Throwable E) {
			    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
			        	}
					}
					else {
						int OP = GetOnlinePerventage();
						OP -= 2;
						if (OP >= 0)
							SetOnlinePerventage(OP);
					}
					Count++;
					//.
		        	Thread.sleep(1000);
				}
			}
        	catch (InterruptedException E) {
        	}
		}

		private void SetOnlinePerventage(int pValue) {
			OnlinePercentage = pValue;
			//.
			MessageHandler.obtainMessage(MESSAGE_UPDATECONTACTUSERSTATE).sendToTarget();
		}
		
		private int GetOnlinePerventage() {
			return OnlinePercentage;
		}
		
    	private int InterpolateColor(int C1, int C2, float proportion) {
    	    float[] hsva = new float[3];
    	    float[] hsvb = new float[3];
    	    Color.colorToHSV(C1, hsva);
    	    Color.colorToHSV(C2, hsvb);
    	    for (int i = 0; i < 3; i++) 
    	    	hsvb[i] = (hsva[i]+((hsvb[i]-hsva[i])*proportion));
    	    return Color.HSVToColor(hsvb);
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
		                Toast.makeText(TUserChatPanel.this, TUserChatPanel.this.getString(R.string.SUpdatingContactUser)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
	                	
	                case MESSAGE_UPDATECONTACTUSERSTATE:
						if (Canceller.flCancel)
			            	break; //. >
						int OP = GetOnlinePerventage();
		            	//.
		            	int C = InterpolateColor(OfflineColor,OnlineColor, OP/100.0F);
		            	lbUserChatContactUserState.setBackgroundColor(C);
	                	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
	public final Handler PanelHandler = new Handler() {
		
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_SENT: 
    				if (!flExists)
    	            	break; //. >
                	try {
                		TIncomingMessage Message = (TIncomingMessage)msg.obj;
                		ChatArea_AddMessage(getString(R.string.SMe), Message, false);
                		//.
                		edUserChatComposeMessage.setText("");
                	}
                	catch (Exception E) {
                		Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                	
                case MESSAGE_RECEIVED: 
    				if (!flExists)
    	            	break; //. >
                	try {
                		TIncomingMessage Message = (TIncomingMessage)msg.obj;
                		PublishMessage(Message);
                	}
                	catch (Exception E) {
                		Toast.makeText(TUserChatPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
