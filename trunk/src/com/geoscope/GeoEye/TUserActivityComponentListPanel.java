package com.geoscope.GeoEye;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Defines.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity.TComponent;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserLocation;
import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.Positioner.TPositionerFunctionality;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TProgressor;

@SuppressLint("HandlerLeak")
public class TUserActivityComponentListPanel extends Activity {

	private static final int 	MESSAGE_TYPEDDATAFILE_LOADED = 1;
	
	private ListView lvActivityComponentList;
	//.
	private int 		UserID = 0;	
	private int 		ActivityID = 0;	
    private TActivity.TComponents ActivityComponents = null;
	//.
	private TUpdating	Updating = null;
	//.
	private TComponentTypedDataFileLoading ComponentTypedDataFileLoading = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	UserID = extras.getInt("UserID");
        	ActivityID = extras.getInt("ActivityID");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.user_activitycomponentlist_panel);
        //.
        lvActivityComponentList = (ListView)findViewById(R.id.lvUserActivityComponentList);
        lvActivityComponentList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvActivityComponentList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if ((ActivityComponents == null) || (ActivityComponents.Items[arg2].TypedDataFiles.Count() == 0))
					return; //. ->
				if (ActivityComponents.Items[arg2].TypedDataFiles.Count() > 1)
					ComponentTypedDataFiles_CreateSelectorPanel(ActivityComponents.Items[arg2].TypedDataFiles).show();
				else {
					TComponentTypedDataFile ComponentTypedDataFile = ActivityComponents.Items[arg2].TypedDataFiles.Items[0];
					ComponentTypedDataFile_Process(ComponentTypedDataFile);
				}

        	}              
        });         
        lvActivityComponentList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (ActivityComponents == null)
					return false; //. ->
            	//.
				TComponent Component = ActivityComponents.Items[arg2];
				if (Component.GeoLocation != null)
					ShowComponentGeoLocation(Component);
				else
					ShowComponentVisualizationPosition(Component);
            	//.
            	return true; 
			}
		}); 
        //.
        setResult(RESULT_CANCELED);
        //.
        StartUpdating();
	}

	@Override
	protected void onDestroy() {
		if (ComponentTypedDataFileLoading != null) {
			ComponentTypedDataFileLoading.CancelAndWait();
			ComponentTypedDataFileLoading = null;
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
	}
	
	protected void FilterActivityComponents(TActivity.TComponents ActivityComponents) {
		if (ActivityComponents == null)
			return; //. ->
		ArrayList<TActivity.TComponent> FilteredList = new ArrayList<TActivity.TComponent>(ActivityComponents.Items.length);
		for (int I = 0; I < ActivityComponents.Items.length; I++)
			if (ActivityComponents.Items[I].TypedDataFiles != null)
				switch (ActivityComponents.Items[I].idTComponent) {
				
				case SpaceDefines.idTCoComponent:
				case SpaceDefines.idTDATAFile:
				case SpaceDefines.idTPositioner:
				case SpaceDefines.idTMapFormatObject:
					FilteredList.add(ActivityComponents.Items[I]);
					break; //. >
				}
		ActivityComponents.Items = new TActivity.TComponent[FilteredList.size()];
		for (int I = 0; I < FilteredList.size(); I++)
			ActivityComponents.Items[I] = FilteredList.get(I); 
	}
	
	private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION = -1;
    	private static final int MESSAGE_COMPLETED = 0;
    	private static final int MESSAGE_FINISHED = 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 4;
    	private static final int MESSAGE_PROGRESSBAR_MESSAGE = 5;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
        private ProgressDialog progressDialog;
        //.
        private TActivity.TComponents ActivityComponents = null;
    	
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
	    				ActivityComponents = UserAgent.Server.User.GetUserActivityComponentList(UserID, ActivityID);
	    				if (ActivityComponents != null)
		    				for (int I = 0; I < ActivityComponents.Items.length; I++) {
		    					if (Canceller.flCancel)
		    						return; //. ->
		    					try {
			    					TComponentTypedDataFiles TypedDataFiles = new TComponentTypedDataFiles(TUserActivityComponentListPanel.this, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_AllName);
			    					TypedDataFiles.PrepareForComponent(ActivityComponents.Items[I].idTComponent,ActivityComponents.Items[I].idComponent, true, UserAgent.Server);
			    					//.
			    					ActivityComponents.Items[I].TypedDataFiles = TypedDataFiles;
			    					//.
			    					String S = ActivityComponents.Items[I].GetName(); 
		    		    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_MESSAGE,S).sendToTarget();
			    					
		    					}
		    					catch (Exception E) {
		    		    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
		    					}
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
		                Toast.makeText(TUserActivityComponentListPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            	FilterActivityComponents(ActivityComponents);
		            	//.
		            	TUserActivityComponentListPanel.this.ActivityComponents = ActivityComponents;
	           		 	//.
	           		 	TUserActivityComponentListPanel.this.Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
		            	TUserActivityComponentListPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TUserActivityComponentListPanel.this);    
		            	progressDialog.setMessage(TUserActivityComponentListPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserActivityComponentListPanel.this.finish();
								else
					    			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TUserActivityComponentListPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TUserActivityComponentListPanel.this.finish();
								else
					    			MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
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

		            case MESSAGE_PROGRESSBAR_MESSAGE:
		            	String S = (String)msg.obj;
		            	//.
		            	if ((S != null) && (!S.equals(""))) 
		            		progressDialog.setMessage(TUserActivityComponentListPanel.this.getString(R.string.SLoading)+"  "+S);
		            	else
		            		progressDialog.setMessage(TUserActivityComponentListPanel.this.getString(R.string.SLoading));
		            	break; //. >
		            }
	        	}
	        	catch (Exception E) {
	        	}
	        }
	    };
    }   
	
    private void Update() {
    	if (ActivityComponents == null) {
    		lvActivityComponentList.setAdapter(null);
    		return; //. ->
    	}
		String[] lvItems = new String[ActivityComponents.Items.length];
		for (int I = 0; I < ActivityComponents.Items.length; I++) {
			TActivity.TComponent Component = ActivityComponents.Items[I];
			String S = Component.GetName().split("\n")[0];
			if (Component.TypedDataFiles.Items.length > 0)
				S = S+" "+"/"+SpaceDefines.TYPEDDATAFILE_TYPE_String(Component.TypedDataFiles.Items[0].DataType)+"/";
			lvItems[I] = S;
		}
		ArrayAdapter<String> lvAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvItems);             
		lvActivityComponentList.setAdapter(lvAdapter);
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,false);
    }    
    
	public AlertDialog ComponentTypedDataFiles_CreateSelectorPanel(TComponentTypedDataFiles pComponentTypedDataFiles) {
		final TComponentTypedDataFiles ComponentTypedDataFiles = pComponentTypedDataFiles;
		final CharSequence[] _items = new CharSequence[ComponentTypedDataFiles.Items.length];
		for (int I = 0; I < ComponentTypedDataFiles.Items.length; I++)
			_items[I] = ComponentTypedDataFiles.Items[I].DataName+" "+"/"+SpaceDefines.TYPEDDATAFILE_TYPE_String(ComponentTypedDataFiles.Items[I].DataType)+"/";
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.SFiles);
		builder.setNegativeButton(getString(R.string.SCancel), null);
		builder.setSingleChoiceItems(_items, -1,
				new DialogInterface.OnClickListener() {

					private TComponentTypedDataFiles _ComponentTypedDataFiles = ComponentTypedDataFiles;

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						TComponentTypedDataFile ComponentTypedDataFile = _ComponentTypedDataFiles.Items[arg1];
						ComponentTypedDataFile_Process(ComponentTypedDataFile);
					}
				});
		AlertDialog alert = builder.create();
		return alert;
	}
	
	private class TComponentTypedDataFileLoading extends TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;

		private TComponentTypedDataFile ComponentTypedDataFile;
		private int OnCompletionMessage;
		// .
		int SummarySize = 0;
		private ProgressDialog progressDialog;

		public TComponentTypedDataFileLoading(TComponentTypedDataFile pComponentTypedDataFile, int pOnCompletionMessage) {
			ComponentTypedDataFile = pComponentTypedDataFile;
			OnCompletionMessage = pOnCompletionMessage;
			// .
			_Thread = new Thread(this);
			_Thread.start();
		}

		@Override
		public void run() {
			try {
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				//.
				switch (ComponentTypedDataFile.DataComponentType) {

				case SpaceDefines.idTDATAFile:
					TGeoScopeServerInfo.TInfo ServersInfo = UserAgent.Server.Info.GetInfo();
					TComponentStreamServer CSS = new TComponentStreamServer(TUserActivityComponentListPanel.this, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, UserAgent.Server.User.UserID, UserAgent.Server.User.UserPassword);
					try {
						String CFN = TTypesSystem.TypesSystem.SystemTDATAFile.Context_GetFolder()+"/"+ComponentTypedDataFile.FileName();
						//.
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
						try {
							CSS.ComponentStreamServer_GetComponentStream_Begin(ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
							try {
								File CF = new File(CFN);
								RandomAccessFile ComponentStream = new RandomAccessFile(CF,"rw");
								try {
									ComponentStream.seek(ComponentStream.length());
									//.
									CSS.ComponentStreamServer_GetComponentStream_Read(Integer.toString(ComponentTypedDataFile.DataComponentID),ComponentStream, Canceller, new TProgressor() {
										@Override
										public synchronized boolean DoOnProgress(int Percentage) {
											MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,Percentage).sendToTarget();
											return true;
										}
									});
								}
								finally {
									ComponentStream.close();
								}
							}
							finally {
								CSS.ComponentStreamServer_GetComponentStream_End();						
							}
						}
						finally {
							MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
						}
						//.
						ComponentTypedDataFile.PrepareFullFromFile(CFN);
						//.
						TUserActivityComponentListPanel.this.MessageHandler.obtainMessage(OnCompletionMessage,ComponentTypedDataFile).sendToTarget();
					}
					finally {
						CSS.Destroy();
					}
					break; //. >

				default:
					String URL1 = UserAgent.Server.Address;
					// . add command path
					URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
							+ "/" + Integer.toString(UserAgent.Server.User.UserID);
					String URL2 = "Functionality" + "/"
							+ "ComponentDataDocument.dat";
					// . add command parameters
					int WithComponentsFlag = 0;
					URL2 = URL2
							+ "?"
							+ "1"/* command version */
							+ ","
							+ Integer
									.toString(ComponentTypedDataFile.DataComponentType)
							+ ","
							+ Integer
									.toString(ComponentTypedDataFile.DataComponentID)
							+ ","
							+ Integer
									.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)
							+ ","
							+ Integer.toString(ComponentTypedDataFile.DataType+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)
							+ "," + Integer.toString(WithComponentsFlag);
					// .
					byte[] URL2_Buffer;
					try {
						URL2_Buffer = URL2.getBytes("windows-1251");
					} catch (Exception E) {
						URL2_Buffer = null;
					}
					byte[] URL2_EncryptedBuffer = UserAgent.Server.User.EncryptBufferV2(URL2_Buffer);
					// . encode string
					StringBuffer sb = new StringBuffer();
					for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
						String h = Integer
								.toHexString(0xFF & URL2_EncryptedBuffer[I]);
						while (h.length() < 2)
							h = "0" + h;
						sb.append(h);
					}
					URL2 = sb.toString();
					// .
					String URL = URL1 + "/" + URL2 + ".dat";
					// .
					if (Canceller.flCancel)
						return; // . ->
					// .
					MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW)
							.sendToTarget();
					try {
						HttpURLConnection Connection = UserAgent.Server.OpenConnection(URL);
						try {
							if (Canceller.flCancel)
								return; // . ->
							// .
							InputStream in = Connection.getInputStream();
							try {
								if (Canceller.flCancel)
									return; // . ->
								// .
								int RetSize = Connection.getContentLength();
								if (RetSize == 0) {
									ComponentTypedDataFile.Data = null;
									return; // . ->
								}
								byte[] Data = new byte[RetSize];
								int Size;
								SummarySize = 0;
								int ReadSize;
								while (SummarySize < Data.length) {
									ReadSize = Data.length - SummarySize;
									Size = in.read(Data, SummarySize, ReadSize);
									if (Size <= 0)
										throw new Exception(TUserActivityComponentListPanel.this.getString(R.string.SConnectionIsClosedUnexpectedly)); // =>
									SummarySize += Size;
									// .
									if (Canceller.flCancel)
										return; // . ->
									// .
									MessageHandler
											.obtainMessage(
													MESSAGE_PROGRESSBAR_PROGRESS,
													(Integer) (100 * SummarySize / Data.length))
											.sendToTarget();
								}
								// .
								ComponentTypedDataFile.PrepareFromByteArrayV0(Data);
								// .
								TUserActivityComponentListPanel.this.MessageHandler
										.obtainMessage(OnCompletionMessage,
												ComponentTypedDataFile)
										.sendToTarget();
							} finally {
								in.close();
							}
						} finally {
							Connection.disconnect();
						}
					} finally {
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE)
								.sendToTarget();
					}
					break; //. >
				}
			} catch (InterruptedException E) {
			} catch (CancelException E) {
			} catch (IOException E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E)
						.sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,
						new Exception(E.getMessage())).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {

				case MESSAGE_SHOWEXCEPTION:
					Exception E = (Exception) msg.obj;
					Toast.makeText(
							TUserActivityComponentListPanel.this,
							TUserActivityComponentListPanel.this.getString(R.string.SErrorOfDataLoading)
									+ E.getMessage(), Toast.LENGTH_SHORT)
							.show();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_SHOW:
					progressDialog = new ProgressDialog(TUserActivityComponentListPanel.this);
					progressDialog.setMessage(TUserActivityComponentListPanel.this.getString(R.string.SLoading));
					progressDialog
							.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					progressDialog.setIndeterminate(false);
					progressDialog.setCancelable(true);
					progressDialog.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface arg0) {
							Cancel();
						}
					});
					progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
							TUserActivityComponentListPanel.this.getString(R.string.SCancel),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Cancel();
								}
							});
					// .
					progressDialog.show();
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_HIDE:
	                if ((!isFinishing()) && progressDialog.isShowing()) 
	                	progressDialog.dismiss(); 
					// .
					break; // . >

				case MESSAGE_PROGRESSBAR_PROGRESS:
					progressDialog.setProgress((Integer) msg.obj);
					// .
					break; // . >
				}
			}
		};
	}

	public void ComponentTypedDataFile_Process(TComponentTypedDataFile ComponentTypedDataFile) {
		if (ComponentTypedDataFile.IsLoaded()) {
			ComponentTypedDataFile_Open(ComponentTypedDataFile);
		} else {
			if (ComponentTypedDataFileLoading != null)
				ComponentTypedDataFileLoading.Cancel();
			ComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(ComponentTypedDataFile, MESSAGE_TYPEDDATAFILE_LOADED);
		}
	}
	
	public void ComponentTypedDataFile_Open(TComponentTypedDataFile ComponentTypedDataFile) {
		try {
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
			//.
			Intent intent = null;
			switch (ComponentTypedDataFile.DataType) {

			case SpaceDefines.TYPEDDATAFILE_TYPE_Document:
				try {
					File F = ComponentTypedDataFile.GetFile();
					byte[] Data = new byte[(int)F.length()];
					FileInputStream FIS = new FileInputStream(F);
					try {
						FIS.read(Data);
					}
					finally {
						FIS.close();
					}
					//.
					if (ComponentTypedDataFile.DataFormat.toUpperCase(Locale.ENGLISH).equals(".TXT")) {
						String Text = new String(Data,"windows-1251");
						byte[] TextData = Text.getBytes("utf-16");
						// .
						File TempFile = ComponentTypedDataFile.GetTempFile();
						FileOutputStream fos = new FileOutputStream(TempFile);
						try {
							fos.write(TextData, 0, TextData.length);
						} finally {
							fos.close();
						}
						// . open appropriate extent
						intent = new Intent();
						intent.setDataAndType(Uri.fromFile(TempFile), "text/plain");
					}
					else
						if (ComponentTypedDataFile.DataFormat.toUpperCase(Locale.ENGLISH).equals(".XML")) {
							TComponentFunctionality CF = TComponentFunctionality.Create(UserAgent.Server, ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
							if (CF != null)
								try {
									int Version = CF.ParseFromXMLDocument(Data);
									if (Version > 0) 
										switch (CF.TypeFunctionality.idType) {
										
										case SpaceDefines.idTPositioner:
											TPositionerFunctionality PF = (TPositionerFunctionality)CF;
											//.
											TReflector Reflector = TReflector.GetReflector();
											if (Reflector == null) 
												throw new Exception(getString(R.string.SReflectorIsNull)); //. =>
											//.
											TLocation P = new TLocation(PF._Name);
											P.RW.Assign(Reflector.ReflectionWindow.GetWindow());
											P.RW.X0 = PF._X0; P.RW.Y0 = PF._Y0;
											P.RW.X1 = PF._X1; P.RW.Y1 = PF._Y1;
											P.RW.X2 = PF._X2; P.RW.Y2 = PF._Y2;
											P.RW.X3 = PF._X3; P.RW.Y3 = PF._Y3;
											P.RW.BeginTimestamp = PF._Timestamp; P.RW.EndTimestamp = PF._Timestamp;
											P.RW.Normalize();
											//.
											Reflector.SetReflectionWindowByLocation(P);
											//.
									        setResult(RESULT_OK);
									        //.
											finish();
											return; // . ->
										}
								}
							finally {
								CF.Release();
							}
						}
				} catch (Exception E) {
					Toast.makeText(
							TUserActivityComponentListPanel.this,
							getString(R.string.SErrorOfPreparingDataFile)
									+ ComponentTypedDataFile.FileName(),
							Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			case SpaceDefines.TYPEDDATAFILE_TYPE_Image:
				try {
					// . open appropriate extent
					intent = new Intent();
					intent.setDataAndType(
							Uri.fromFile(ComponentTypedDataFile.GetFile()),
							"image/*");
				} catch (Exception E) {
					Toast.makeText(
							TUserActivityComponentListPanel.this,
							getString(R.string.SErrorOfPreparingDataFile)
									+ ComponentTypedDataFile.FileName(),
							Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			case SpaceDefines.TYPEDDATAFILE_TYPE_Audio:
				try {
					// . open appropriate extent
					intent = new Intent();
					intent.setDataAndType(
							Uri.fromFile(ComponentTypedDataFile.GetFile()),
							"audio/*");
				} catch (Exception E) {
					Toast.makeText(
							TUserActivityComponentListPanel.this,
							getString(R.string.SErrorOfPreparingDataFile)
									+ ComponentTypedDataFile.FileName(),
							Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			case SpaceDefines.TYPEDDATAFILE_TYPE_Video:
				try {
					// . open appropriate extent
					intent = new Intent();
					intent.setDataAndType(
							Uri.fromFile(ComponentTypedDataFile.GetFile()),
							"video/*");
				} catch (Exception E) {
					Toast.makeText(
							TUserActivityComponentListPanel.this,
							getString(R.string.SErrorOfPreparingDataFile)
									+ ComponentTypedDataFile.FileName(),
							Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			default:
				Toast.makeText(TUserActivityComponentListPanel.this, R.string.SUnknownDataFileFormat,
						Toast.LENGTH_LONG).show();
				return; // . ->
			}
			if (intent != null) {
				intent.setAction(android.content.Intent.ACTION_VIEW);
				startActivity(intent);
			}
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(),Toast.LENGTH_SHORT).show();
		}
	}

	private void ShowComponentVisualizationPosition(TActivity.TComponent Component) {
		final TActivity.TComponent _Component = Component;
		//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private TXYCoord VisualizationPosition = null;
			@Override
			public void Process() throws Exception {
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				//.
				TComponentFunctionality CF = new TComponentFunctionality(UserAgent.Server, _Component.idTComponent,_Component.idComponent);
				try {
					VisualizationPosition = CF.GetVisualizationPosition(); 
				}
				finally {
					CF.Release();
				}
			}
			@Override 
			public void DoOnCompleted() throws Exception {
				if (VisualizationPosition != null) {
					TReflector Reflector = TReflector.GetReflector();
					if (Reflector == null) 
						throw new Exception(TUserActivityComponentListPanel.this.getString(R.string.SReflectorIsNull)); //. =>
					Reflector.MoveReflectionWindow(VisualizationPosition);
					//.
			        setResult(RESULT_OK);
			        //.
					TUserActivityComponentListPanel.this.finish();
				}
				else
					throw new Exception(TUserActivityComponentListPanel.this.getString(R.string.SCouldNotGetPosition)); //. =>
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TUserActivityComponentListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
	}
	
	private void ShowComponentGeoLocation(TActivity.TComponent Component) {
		final TUserLocation GeoLocation = Component.GeoLocation;
		//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private TXYCoord Crd = null;
			@Override
			public void Process() throws Exception {
				TReflector Reflector = TReflector.GetReflector();
				if (Reflector == null) 
					throw new Exception(TUserActivityComponentListPanel.this.getString(R.string.SReflectorIsNull)); //. =>
				Crd = Reflector.ConvertGeoCoordinatesToXY(GeoLocation.Datum,GeoLocation.Latitude,GeoLocation.Longitude);
			}
			@Override 
			public void DoOnCompleted() throws Exception {
				TReflector Reflector = TReflector.GetReflector();
				if (Reflector == null) 
					throw new Exception(TUserActivityComponentListPanel.this.getString(R.string.SReflectorIsNull)); //. =>
				Reflector.MoveReflectionWindow(Crd);
				//.
		        setResult(RESULT_OK);
		        //.
				TUserActivityComponentListPanel.this.finish();
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TUserActivityComponentListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
	}
	
	public final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_TYPEDDATAFILE_LOADED:
				TComponentTypedDataFile ComponentTypedDataFile = (TComponentTypedDataFile) msg.obj;
				if (ComponentTypedDataFile != null)
					ComponentTypedDataFile_Open(ComponentTypedDataFile);
				// .
				break; // . >				
			}
		}
	};
}
