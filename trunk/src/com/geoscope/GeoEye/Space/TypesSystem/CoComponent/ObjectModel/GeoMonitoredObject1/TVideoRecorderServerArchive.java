package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import java.io.File;
import java.io.IOException;
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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model.TVideoRecorderMeasurementDescriptor;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographDataServerClient;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TSystemTGeographServerObject;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements;
import com.geoscope.GeoLog.TrackerService.TTracker;
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
		TReflector Reflector = TReflector.GetReflector();  
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.video_recorder_server_archive);
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
					
				case ARCHIVEITEM_LOCATION_CLIENT:
					try {
						LocalArchive_PlayMeasurement(Item.ID);
					} catch (IOException E) {
					}
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

    private TReflector Reflector() throws Exception {
    	TReflector Reflector = TReflector.GetReflector();
    	if (Reflector == null)
    		throw new Exception(getString(R.string.SReflectorIsNull)); //. =>
		return Reflector;
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
    		Updating.CancelAndWait(3000);
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

			case ARCHIVEITEM_LOCATION_CLIENT:
				SideS = getString(R.string.SAtClient);
				break; //. >
			}
			String RS = DTS+"   "+TIS+"  "+SideS;
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
		TVideoRecorderMeasurementDescriptor[] DVRMs = ObjectModel.VideoRecorder_Measurements_GetList1(Object);
		//.
		TGeographDataServerClient.TVideoRecorderMeasurementDescriptor[] SVRMs;
		TGeographDataServerClient GeographDataServerClient = new TGeographDataServerClient(TVideoRecorderServerArchive.this, GeographDataServerAddress,GeographDataServerPort, UserID,UserPassword, Object.GeographServerObjectID());
		try {
			SVRMs = GeographDataServerClient.SERVICE_GETVIDEORECORDERDATA_GetMeasurementList(Canceller);
		}
		finally {
			GeographDataServerClient.Destroy();
		}
		//.
		TVideoRecorderMeasurementDescriptor[] CVRMs;
		CVRMs = LocalArchive_GetMeasurementsList();
		//.
		int DVRMs_Count = 0;
		for (int I = 0; I < DVRMs.length; I++) {
			boolean flFound = false;
			for (int J = 0; J < SVRMs.length; J++) 
				if (Math.abs(Double.parseDouble(DVRMs[I].ID)-Double.parseDouble(SVRMs[J].ID)) < 1.0/(24.0*3600.0)) {
					flFound = true;
					break; //. >
				}
			if (flFound)
				DVRMs[I] = null;
			else
				DVRMs_Count++;
		}
		//.
		int SVRMs_Count = 0;
		for (int I = 0; I < SVRMs.length; I++) {
			boolean flFound = false;
			for (int J = 0; J < CVRMs.length; J++) 
				if (SVRMs[I].ID.equals(CVRMs[J].ID)) {
					flFound = true;
					break; //. >
				}
			if (flFound)
				SVRMs[I] = null;
			else
				SVRMs_Count++;
		}
		//.
		TArchiveItem[] Result = new TArchiveItem[DVRMs_Count+SVRMs_Count+CVRMs.length];
		int Idx = 0;
		//.
		for (int I = 0; I < DVRMs.length; I++) 
			if (DVRMs[I] != null) {
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
		for (int I = 0; I < SVRMs.length; I++) 
			if (SVRMs[I] != null) {
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
		for (int I = 0; I < CVRMs.length; I++) 
			if (CVRMs[I] != null) {
				Result[Idx] = new TArchiveItem();
				Result[Idx].ID = CVRMs[I].ID;
				Result[Idx].StartTimestamp = CVRMs[I].StartTimestamp;
				Result[Idx].FinishTimestamp = CVRMs[I].FinishTimestamp;
				Result[Idx].CPC = 1.0;
				Result[Idx].Location = ARCHIVEITEM_LOCATION_CLIENT;
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
	
	public String LocalArchive_Folder() throws IOException {
		return TSystemTGeographServerObject.ContextFolder+"/"+Integer.toString(Object.GeographServerObjectID())+"/"+"VideoRecorder"+"/"+"0";		
	}
	
	public String LocalArchive_GetMeasurementFolder(String MeasurementID) throws IOException {
		return LocalArchive_Folder()+"/"+MeasurementID;
	}
	
	public String LocalArchive_CreateMeasurementFolder(String MeasurementID) throws IOException {
		String Result = LocalArchive_GetMeasurementFolder(MeasurementID);
		File F = new File(Result);
		F.mkdirs();
		//.
		return Result;
	}
	
	public String LocalArchive_GetMeasurementTempFolder(String MeasurementID) throws IOException {
		return TReflector.TempFolder+"/"+"GeographServerObject"+"/"+Integer.toString(Object.GeographServerObjectID())+"/"+"VideoRecorder"+"/"+"0"+"/"+MeasurementID;
	}
	
	public String LocalArchive_CreateMeasurementTempFolder(String MeasurementID) throws IOException {
		String Result = LocalArchive_GetMeasurementTempFolder(MeasurementID);
		File F = new File(Result);
		F.mkdirs();
		//.
		return Result;
	}
	
	public TVideoRecorderMeasurementDescriptor[] LocalArchive_GetMeasurementsList() throws IOException {
		String ResultString = TVideoRecorderMeasurements.GetMeasurementsList(LocalArchive_Folder());
		TVideoRecorderMeasurementDescriptor[] Result;
		if ((ResultString != null) && (!ResultString.equals(""))) {
			String[] Items = ResultString.split(";");
			Result = new TVideoRecorderMeasurementDescriptor[Items.length];
			for (int I = 0; I < Items.length; I++) {
				String[] Properties = Items[I].split(",");
				Result[I] = new TVideoRecorderMeasurementDescriptor();
				Result[I].ID = Properties[0];
				Result[I].StartTimestamp = Double.parseDouble(Properties[1]);
				Result[I].FinishTimestamp = Double.parseDouble(Properties[2]);
				Result[I].AudioSize = Integer.parseInt(Properties[3]);
				Result[I].VideoSize = Integer.parseInt(Properties[4]);
			}
		}
		else
			Result = new TVideoRecorderMeasurementDescriptor[0];
		return Result;
	}
	
	public boolean LocalArchive_IsMeasurementExist(String MeasurementID) throws IOException {
		File F = new File(LocalArchive_GetMeasurementFolder(MeasurementID));
		return F.exists();
	}
	
	public void LocalArchive_PlayMeasurementByFolder(String MeasurementFolder) {
    	try {
        	File MF = new File(MeasurementFolder);
        	String MeasurementDatabaseFolder = MF.getParent(); 
        	String MeasurementID = MF.getName(); 
        	TVideoRecorderServerPlayer Player = new TVideoRecorderServerPlayer(MeasurementDatabaseFolder,MeasurementID);
        	Intent PI = Player.GetPlayer(TVideoRecorderServerArchive.this);
        	startActivity(PI);	            	
    	}
    	catch (Exception E) {
            Toast.makeText(TVideoRecorderServerArchive.this, E.getMessage(), Toast.LENGTH_SHORT).show();
    	}		
	}
	
	public void LocalArchive_PlayMeasurement(String MeasurementID) throws IOException {
		LocalArchive_PlayMeasurementByFolder(LocalArchive_GetMeasurementFolder(MeasurementID));
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
        		try {
        			if (!Reflector().isFinishing()) 
        				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        		}
        		catch (Exception E) {
        		}
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
	                Toast.makeText(TVideoRecorderServerArchive.this, TVideoRecorderServerArchive.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
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
    	private static final int MESSAGE_SUCCESSLOCALLY 		= 1;
    	private static final int MESSAGE_SUCCESS 				= 2;
    	private static final int MESSAGE_DOONITEMSISUPDATED 	= 3;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 			= 4;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 			= 5;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 		= 6;
   	
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
    				int idThisGeographServerObject = 0;
    				TTracker Tracker = TTracker.GetTracker();
    				if (Tracker != null)
    					idThisGeographServerObject = Tracker.GeoLog.idGeographServerObject;
    				//.
    				if (Object.GeographServerObjectID() != idThisGeographServerObject) {
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
        	    				ObjectModel.VideoRecorder_Measurements_MoveToDataServer1(Object, MeasurementID);
        		    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,0).sendToTarget();
        					}
        					//.
        					Thread.sleep(CheckCompletionInterval);
        				}
    				}
    				else {
    					String SrcMeasurementFolder = TVideoRecorderMeasurements.GetDatabaseFolder(TVideoRecorderMeasurements.Camera0)+"/"+MeasurementID;
    					//.
    					String MeasurementFolder = LocalArchive_GetMeasurementFolder(MeasurementID);
    					String MeasurementTempFolder = LocalArchive_GetMeasurementTempFolder(MeasurementID);
    					try {
        					TFileSystem.CopyFolder(new File(SrcMeasurementFolder), new File(MeasurementTempFolder));
            				//. complete measurement folder
            				File MF = new File(MeasurementTempFolder);
            				File NMF = new File(MeasurementFolder);
            				NMF.getParentFile().mkdirs();
            				MF.renameTo(NMF);
    					}
    					catch (Exception E) {
    						TFileSystem.RemoveFolder(new File(MeasurementTempFolder));
    						throw E; //. =>
    					}
	    				//.
	        			MessageHandler.obtainMessage(MESSAGE_SUCCESSLOCALLY,MeasurementFolder).sendToTarget();
    					//.
	    				///? TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model();
	    				///? ObjectModel.VideoRecorder_Measurements_MoveToDataServer(Object, MeasurementID);
	    				//.
	    				return; //. ->
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
        		try {
        			if (!Reflector().isFinishing()) 
        				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        		}
        		catch (Exception E) {
        		}
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
	            
	            case MESSAGE_SUCCESSLOCALLY:
                	String _MeasurementFolder = (String)msg.obj;
                	LocalArchive_PlayMeasurementByFolder(_MeasurementFolder);
                	//.
	            	break; //. >
	            	
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
        				MeasurementTempFolder = LocalArchive_CreateMeasurementTempFolder(Double.toString(MeasurementID));
        				//.
        				TGeographDataServerClient GeographDataServerClient = new TGeographDataServerClient(TVideoRecorderServerArchive.this, GeographDataServerAddress,GeographDataServerPort, UserID,UserPassword, Object.GeographServerObjectID());
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
        		try {
        			if (!Reflector().isFinishing()) 
        				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        		}
        		catch (Exception E) {
        		}
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
                	LocalArchive_PlayMeasurementByFolder(_MeasurementFolder);
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
