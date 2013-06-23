package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographDataServerClient;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TSystemTGeographServerObject;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.Utils.TFileSystem;

@SuppressLint("HandlerLeak")
public class TVideoRecorderServerArchive extends Activity {

	public static final int ARCHIVEITEM_LOCATION_DEVICE = 0;
	public static final int ARCHIVEITEM_LOCATION_SERVER = 1;
	public static final int ARCHIVEITEM_LOCATION_CLIENT = 2;
	
	private static class TArchiveItem {
		public String ID;
		public double StartTimestamp;
		public double FinishTimestamp;
		public double CPC;
		public int Location;
	}
	
	private TReflector Reflector;
	
	private String 	GeographDataServerAddress = "";
	private int 	GeographDataServerPort = 0;
	private int		UserID;
	private String	UserPassword;
	private int								ObjectIndex = -1;
	private TReflectorCoGeoMonitorObject 	Object;
	//.
	private boolean flRunning = false;
	//.
	private ListView lvVideoRecorderServerArchive;
	public TArchiveItem[] Items = null;
	private TUpdating Updating = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
		Reflector = TReflector.GetReflector();  
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	GeographDataServerAddress = extras.getString("GeographDataServerAddress");
        	GeographDataServerPort = extras.getInt("GeographDataServerPort");
        	UserID = extras.getInt("UserID");
        	UserPassword = extras.getString("UserPassword");
        	ObjectIndex = extras.getInt("ObjectIndex");
        	Object = Reflector.CoGeoMonitorObjects.Items[ObjectIndex];
        }
        //.
        setContentView(R.layout.video_recorder_server_archive);
        setTitle(R.string.SVideoRecorderArchive);
        lvVideoRecorderServerArchive = (ListView)findViewById(R.id.lvVideoRecorderServerArchive);
        lvVideoRecorderServerArchive.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvVideoRecorderServerArchive.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        	}              
        });
        lvVideoRecorderServerArchive.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				lvVideoRecorderServerArchive.setItemChecked(arg2,true);
				//.
				TArchiveItem Item = Items[arg2];
				switch (Item.Location) {

				case ARCHIVEITEM_LOCATION_DEVICE:
					new TDeviceMeasurementDownloading(Item.ID, Item.StartTimestamp, Item.FinishTimestamp);				
					break; //. >
					
				case ARCHIVEITEM_LOCATION_SERVER:
					if (Item.CPC >= 1.0)
						new TGeographServerMeasurementDownloading(Double.parseDouble(Item.ID), Item.StartTimestamp, Item.FinishTimestamp);
					else
						new TDeviceMeasurementDownloading(Item.ID, Item.StartTimestamp, Item.FinishTimestamp);				
					break; //. >
				}
            	//.
            	return true; 
			}
		}); 
	}

    @Override
	protected void onDestroy() {
		super.onDestroy();
	}

    @Override
    protected void onStart() {
		super.onStart();
		//.
		flRunning = true;
		//.
		StartUpdating();
    }
    
    @Override
    protected void onStop() {
    	flRunning = false;
    	//.
		StopUpdating();
		//.
		super.onStop();
    }
    
    @Override
	public void onResume() {
		super.onResume();
	}

    @Override
	public void onPause() {
		super.onPause();
	}
	
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

    public void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating();
    }
    
    public void StopUpdating() {
    	if (Updating != null) {
    		Updating.CancelAndWait();
    		Updating = null;
    	}
    }
    
    private String NormalizeNumberString(String S, int Length) {
    	if (S.length() >= Length)
    		return S; //. ->
    	StringBuilder SB = new StringBuilder(S);
    	int Diff = Length-S.length();
    	for (int I = 0; I < Diff; I++)
    		SB.insert(0,"0");
    	return SB.toString();
    }
    
	private void UpdateList(TArchiveItem[] pItems) throws Exception {
		synchronized (this) {
			Items = pItems;				
		}
		if (Items.length == 0) {
			lvVideoRecorderServerArchive.setAdapter(null);
    		return; //. ->
		}
		String SelectedMeasurementID = "";
		int SIP = lvVideoRecorderServerArchive.getCheckedItemPosition();
		if (SIP != AdapterView.INVALID_POSITION)
			SelectedMeasurementID = Items[SIP].ID;
		int SelectedIdx = -1;
		final String[] lvItems = new String[Items.length];
		for (int I = 0; I < Items.length; I++) {
			OleDate DT = new OleDate(Items[I].StartTimestamp);
			String DTS = NormalizeNumberString(Integer.toString(DT.year),4)+"/"+NormalizeNumberString(Integer.toString(DT.month),2)+"/"+NormalizeNumberString(Integer.toString(DT.date),2)+" "+NormalizeNumberString(Integer.toString(DT.hrs),2)+":"+NormalizeNumberString(Integer.toString(DT.min),2)+":"+NormalizeNumberString(Integer.toString(DT.sec),2);
			int TimeInterval = (int)((Items[I].FinishTimestamp-Items[I].StartTimestamp)*24.0*3600.0);
			String TIS;
			if (TimeInterval < 60)
				TIS = Integer.toString(TimeInterval)+getString(R.string.SSec);
			else
				TIS = Integer.toString((int)(TimeInterval/60))+getString(R.string.SMin)+" "+Integer.toString(TimeInterval % 60)+getString(R.string.SSec);
			String SideS = "";
			switch (Items[I].Location) {
			case ARCHIVEITEM_LOCATION_SERVER:
				SideS = getString(R.string.SAtServer);
				break; //. >
			}
			String RS = DTS+"   "+TIS+"  "+SideS;
			if (LocalArchive_IsMeasurementExist(Items[I].ID))
				RS = RS+" "+getString(R.string.SAtClient);
			lvItems[I] = RS; 
			//.
			if (Items[I].ID.equals(SelectedMeasurementID))
				SelectedIdx = I;
		}
		ArrayAdapter<String> lvItemsAdapter =new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvItems);             
		lvVideoRecorderServerArchive.setAdapter(lvItemsAdapter);
		if (SelectedIdx >= 0) {
			lvVideoRecorderServerArchive.setItemChecked(SelectedIdx,true);
			lvVideoRecorderServerArchive.setSelection(SelectedIdx);
		}
	}
	
	private TArchiveItem[] GetItemsList(TCanceller Canceller) throws Exception {
		TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model();
		TGeoMonitoredObject1Model.TVideoRecorderMeasurementDescriptor[] DVRMs = ObjectModel.VideoRecorder_Measurements_GetList(Object);
		//.
		TGeographDataServerClient.TVideoRecorderMeasurementDescriptor[] SVRMs;
		TGeographDataServerClient GeographDataServerClient = new TGeographDataServerClient(TVideoRecorderServerArchive.this, GeographDataServerAddress,GeographDataServerPort, UserID,UserPassword, Object.idGeographServerObject);
		try {
			SVRMs = GeographDataServerClient.SERVICE_GETVIDEORECORDERDATA_GetMeasurementList(Canceller);
		}
		finally {
			GeographDataServerClient.Destroy();
		}
		//.
		TArchiveItem[] Result = new TArchiveItem[DVRMs.length+SVRMs.length];
		int Idx = 0;
		//.
		for (int I = 0; I < DVRMs.length; I++) {
			Result[Idx] = new TArchiveItem();
			Result[Idx].ID = DVRMs[I].ID;
			Result[Idx].StartTimestamp = DVRMs[I].StartTimestamp;
			Result[Idx].FinishTimestamp = DVRMs[I].FinishTimestamp;
			Result[Idx].CPC = 1.0;
			Result[Idx].Location = ARCHIVEITEM_LOCATION_DEVICE;
			//.
			Idx++;
		}
		//.
		for (int I = 0; I < SVRMs.length; I++) {
			Result[Idx] = new TArchiveItem();
			Result[Idx].ID = SVRMs[I].ID;
			Result[Idx].StartTimestamp = SVRMs[I].StartTimestamp;
			Result[Idx].FinishTimestamp = SVRMs[I].FinishTimestamp;
			Result[Idx].CPC = SVRMs[I].CPC;
			Result[Idx].Location = ARCHIVEITEM_LOCATION_SERVER;
			//.
			Idx++;
		}
		//.
		Arrays.sort(Result, new Comparator<TArchiveItem>() {
			@Override
			public int compare(TArchiveItem lhs, TArchiveItem rhs) {
				return Double.valueOf(rhs.StartTimestamp).compareTo(lhs.StartTimestamp);
			}}
		);				
		return Result;
	}
	
	public String LocalArchive_GetMeasurementFolder(String MeasurementID) {
		return TSystemTGeographServerObject.ContextFolder+"/"+Integer.toString(Object.idGeographServerObject)+"/"+"VideoRecorder"+"/"+"0"+"/"+MeasurementID;
	}
	
	public String LocalArchive_CreateMeasurementFolder(String MeasurementID) {
		String Result = LocalArchive_GetMeasurementFolder(MeasurementID);
		File F = new File(Result);
		F.mkdirs();
		//.
		return Result;
	}
	
	public String LocalArchive_CreateTempMeasurementFolder(String MeasurementID) {
		String Result = TReflector.TempFolder+"/"+"GeographServerObject"+"/"+Integer.toString(Object.idGeographServerObject)+"/"+"VideoRecorder"+"/"+"0"+"/"+MeasurementID;
		File F = new File(Result);
		F.mkdirs();
		//.
		return Result;
	}
	
	public boolean LocalArchive_IsMeasurementExist(String MeasurementID) {
		File F = new File(LocalArchive_GetMeasurementFolder(MeasurementID));
		return F.exists();
	}
	
    private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESS 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;
    	
        private ProgressDialog progressDialog; 
    	
    	public TUpdating() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
		    	TArchiveItem[] _Items = null;
				//.
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				_Items = GetItemsList(Canceller);
    			}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
				if (Canceller.flCancel)
					return; //. ->
	    		//.
    			MessageHandler.obtainMessage(MESSAGE_SUCCESS,_Items).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
			catch (CancelException CE) {
			}
        	catch (NullPointerException NPE) { 
        		if (!Reflector.isFinishing()) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        	}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_SUCCESS:
                	TArchiveItem[] _Items = (TArchiveItem[])msg.obj;
                	try {
                    	UpdateList(_Items);
                	}
                	catch (Exception E) {
                		Toast.makeText(TVideoRecorderServerArchive.this, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TVideoRecorderServerArchive.this, TVideoRecorderServerArchive.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TVideoRecorderServerArchive.this);    
	            	progressDialog.setMessage(TVideoRecorderServerArchive.this.getString(R.string.SLoading));    
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
	            	if (flRunning)
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

    private class TDeviceMeasurementDownloading extends TCancelableThread {

    	public static final int CheckCompletionInterval = 1000*15; //. seconds
    	
    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESS 				= 1;
    	private static final int MESSAGE_DOONITEMSISUPDATED 	= 2;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 			= 3;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 			= 4;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 		= 5;
   	
    	public String	MeasurementID;
		public double 	MeasurementStartTimestamp;
		public double 	MeasurementFinishTimestamp;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TDeviceMeasurementDownloading(String pMeasurementID, double pMeasurementStartTimestamp, double pMeasurementFinishTimestamp) {
    		MeasurementID = pMeasurementID;
    		MeasurementStartTimestamp = pMeasurementStartTimestamp;
    		MeasurementFinishTimestamp = pMeasurementFinishTimestamp;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				while (!Canceller.flCancel) {
    					boolean flFound = false;
    					boolean flDone = false;
    					TArchiveItem[] _Items = GetItemsList(Canceller);
    					for (int I = 0; I < _Items.length; I++)
    						if ((_Items[I].Location == ARCHIVEITEM_LOCATION_SERVER) && (Math.abs(Double.parseDouble(_Items[I].ID)-Double.parseDouble(MeasurementID)) < 1.0/(24.0*3600.0))) {
    							flFound = true;
    							if (_Items[I].CPC >= 1.0) {
    								MeasurementID = _Items[I].ID;
    								flDone = true;
    								break; //. ->
    							}
    							else
    				    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,(int)(_Items[I].CPC*100.0)).sendToTarget();
    							
    						}
    	    			MessageHandler.obtainMessage(MESSAGE_DOONITEMSISUPDATED,_Items).sendToTarget();
    					if (flDone)
    						break; //. >
    					if (!flFound) {
    	    				TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model();
    	    				ObjectModel.VideoRecorder_Measurements_MoveToDataServer(Object, MeasurementID);
    		    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,0).sendToTarget();
    					}
    					//.
    					Thread.sleep(CheckCompletionInterval);
    				}
    			}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
				if (Canceller.flCancel)
					return; //. ->
	    		//.
    			MessageHandler.obtainMessage(MESSAGE_SUCCESS).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
			catch (CancelException CE) {
			}
        	catch (NullPointerException NPE) { 
        		if (!Reflector.isFinishing()) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        	}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_SUCCESS:
					new TGeographServerMeasurementDownloading(Double.parseDouble(MeasurementID), MeasurementStartTimestamp, MeasurementFinishTimestamp);					
                	//.
	            	break; //. >
	            	
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TVideoRecorderServerArchive.this, TVideoRecorderServerArchive.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_DOONITEMSISUPDATED:
                	TArchiveItem[] _Items = (TArchiveItem[])msg.obj;
                	try {
                    	UpdateList(_Items);
                	}
                	catch (Exception Ex) {
                		Toast.makeText(TVideoRecorderServerArchive.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
                	}
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TVideoRecorderServerArchive.this);    
	            	progressDialog.setMessage(TVideoRecorderServerArchive.this.getString(R.string.SLoadingFromDeviceToServer));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);    
	            	progressDialog.setIndeterminate(true); 
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
	            	if (flRunning)
	            		progressDialog.dismiss(); 
	            	//.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	int Progress = (Integer)msg.obj;
	            	if (Progress > 0) {
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setProgress(Progress);
	            	}
	            	//.
	            	break; //. >
	            }
	        }
	    };
    }
    
    private class TGeographServerMeasurementDownloading extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESS 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 			= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 			= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 		= 4;
    	private static final int MESSAGE_PROGRESSBAR_ITEMISSTARTED 	= 5;
    	private static final int MESSAGE_PROGRESSBAR_ITEMISFINISHED = 6;
    	private static final int MESSAGE_PROGRESSBAR_ITEMPROGRESS 	= 7;
   	
    	public double MeasurementID;
    	public double MeasurementStartTimestamp;
    	public double MeasurementFinishTimestamp;
    	//.
    	public String MeasurementTempFolder = null;
    	public String MeasurementFolder = null;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TGeographServerMeasurementDownloading(double pMeasurementID, double pMeasurementStartTimestamp, double pMeasurementFinishTimestamp) {
    		MeasurementID = pMeasurementID;
    		MeasurementStartTimestamp = pMeasurementStartTimestamp;
    		MeasurementFinishTimestamp = pMeasurementFinishTimestamp;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				MeasurementFolder = LocalArchive_GetMeasurementFolder(Double.toString(MeasurementID));
    				if (!LocalArchive_IsMeasurementExist(Double.toString(MeasurementID))) {
        				MeasurementTempFolder = LocalArchive_CreateTempMeasurementFolder(Double.toString(MeasurementID));
        				//.
        				TGeographDataServerClient GeographDataServerClient = new TGeographDataServerClient(TVideoRecorderServerArchive.this, GeographDataServerAddress,GeographDataServerPort, UserID,UserPassword, Object.idGeographServerObject);
        				try {
    						GeographDataServerClient.SERVICE_GETVIDEORECORDERDATA_GetMeasurementData(MeasurementID, 0, MeasurementStartTimestamp,MeasurementFinishTimestamp, MeasurementTempFolder, MeasurementItemProgressor,Canceller);
        				}
        				finally {
        					GeographDataServerClient.Destroy();
        				}
        				//. complete measurement folder
        				File MF = new File(MeasurementTempFolder);
        				File NMF = new File(MeasurementFolder);
        				NMF.getParentFile().mkdirs();
        				MF.renameTo(NMF);
    				}
    			}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
				if (Canceller.flCancel)
					return; //. ->
	    		//.
    			MessageHandler.obtainMessage(MESSAGE_SUCCESS,MeasurementFolder).sendToTarget();
        	}
        	catch (InterruptedException E) {
    			RemoveTempMeasurementFolder();
        	}
			catch (CancelException CE) {
    			RemoveTempMeasurementFolder();
			}
        	catch (NullPointerException NPE) { 
    			RemoveTempMeasurementFolder();
        		if (!Reflector.isFinishing()) 
	    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        	}
        	catch (Exception E) {
    			RemoveTempMeasurementFolder();
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			RemoveTempMeasurementFolder();
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

		private void RemoveTempMeasurementFolder() {
			if (MeasurementTempFolder != null)
				TFileSystem.RemoveFolder(new File(MeasurementTempFolder));
		}
		
	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_SUCCESS:
                	String _MeasurementFolder = (String)msg.obj;
                	try {
    	            	File MF = new File(_MeasurementFolder);
    	            	String MeasurementDatabaseFolder = MF.getParent(); 
    	            	String MeasurementID = MF.getName(); 
    	            	TVideoRecorderServerPlayer Player = new TVideoRecorderServerPlayer(MeasurementDatabaseFolder,MeasurementID);
    	            	Intent PI = Player.GetPlayer(TVideoRecorderServerArchive.this);
                    	startActivity(PI);	            	
                	}
                	catch (Exception E) {
    	                Toast.makeText(TVideoRecorderServerArchive.this, E.getMessage(), Toast.LENGTH_SHORT).show();
                	}
                	//.
	            	break; //. >
	            	
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TVideoRecorderServerArchive.this, TVideoRecorderServerArchive.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TVideoRecorderServerArchive.this);    
	            	progressDialog.setMessage(TVideoRecorderServerArchive.this.getString(R.string.SLoadingFromServer));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);    
	            	progressDialog.setIndeterminate(true); 
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
	            	if (flRunning)
	            		progressDialog.dismiss(); 
	            	//.
	            	break; //. >
	            
	            case MESSAGE_PROGRESSBAR_PROGRESS:
	            	progressDialog.setProgress((Integer)msg.obj);
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_ITEMISSTARTED:
	            	String S = (String)msg.obj;
	            	progressDialog.setMessage(S);    
	            	progressDialog.setIndeterminate(false); 
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_ITEMISFINISHED:
	            	progressDialog.setIndeterminate(true); 
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_ITEMPROGRESS:
	            	progressDialog.setProgress((Integer)msg.obj);
	            	//.
	            	break; //. >
	            }
	        }
	    };
	    
	    private final TGeographDataServerClient.TItemProgressor MeasurementItemProgressor = new TGeographDataServerClient.TItemProgressor() {
	    	@Override
	    	public void DoOnItemIsStarted(String ItemName, int ItemIndex, int ItemCount) {
	    		String S = TVideoRecorderServerArchive.this.getString(R.string.SFile)+Integer.toString(ItemIndex)+"/"+Integer.toString(ItemCount)+": "+ItemName;
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_ITEMISSTARTED,S).sendToTarget();
	    	}
	    	@Override
	    	public void DoOnItemIsFinished() {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_ITEMISFINISHED).sendToTarget();
	    	}
	    	@Override
	    	public void DoOnItemProgress(int Progress) {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_ITEMPROGRESS,Progress).sendToTarget();
	    	}
	    };
    }
}
