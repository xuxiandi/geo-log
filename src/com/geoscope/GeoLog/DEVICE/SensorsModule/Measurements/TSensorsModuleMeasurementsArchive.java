package com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Comparator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.String.TEnterStringDialog;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Lock.TNamedReadWriteLock;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TTrackerPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUserDataFile;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.TMeasurement;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentFileStreaming;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TSensorsModuleMeasurementsArchive extends Activity {

	public static class TArchiveItem {
		
		public String ID = "";
		//.
		public double StartTimestamp = 0.0;
		public double FinishTimestamp = 0.0;
		//.
		public String TypeID = "";
		public String ContainerTypeID = "";
		//.
		public String Name = "";
		public String Info = "";
		//.
		public int Location = TSensorMeasurementDescriptor.LOCATION_DEVICE;
		//.
		public double Position = 0.0;
	}
	

	private boolean flRunning = false;
	//.
	private TDEVICEModule Device;
	//.
	private ListView lvVideoRecorderServerArchive;
	public TArchiveItem[] Items = null;
	private TUpdating Updating = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
		try {
			TTracker Tracker = TTracker.GetTracker();
			if (Tracker == null) 
				throw new Exception("Tracker is null"); //. =>
			Device = Tracker.GeoLog;
		} catch (Exception E) {
			finish();
			return; //. ->
		}
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.sensorsmodule_measurements_archive);
        lvVideoRecorderServerArchive = (ListView)findViewById(R.id.lvVideoRecorderServerArchive);
        lvVideoRecorderServerArchive.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvVideoRecorderServerArchive.setOnItemClickListener(new OnItemClickListener() {         

        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (arg2 >= Items.length)
					return; //. ->
				//.
				TSensorsModuleMeasurementsArchive.StartOpeningItem(null, Device.idGeographServerObject, Items[arg2], null,0, TSensorsModuleMeasurementsArchive.this, new TSensorMeasurementDescriptor.TLocationUpdater() {
					
					@Override
					public void DoOnLocationUpdated(String MeasurementID, int Location) {
						Items_UpdateItemLocation(MeasurementID, Location);
					}
				});
        	}              
        });
        lvVideoRecorderServerArchive.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (arg2 >= Items.length)
					return true; //. ->
				//.
				lvVideoRecorderServerArchive.setItemChecked(arg2,true);
				//.
				final TArchiveItem Item = Items[arg2];
				//.
				TSensorMeasurementDescriptor _MeasurementDescriptor;
				if (Item.Location == TSensorMeasurementDescriptor.LOCATION_CLIENT)
					try {
						String MeasurementDatabaseFolder = com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_Folder(Device.idGeographServerObject); 
						_MeasurementDescriptor = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.GetMeasurementDescriptor(MeasurementDatabaseFolder, MeasurementDatabaseFolder, Item.ID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
					} catch (Exception E) {
						_MeasurementDescriptor = null;
					}
					else
						_MeasurementDescriptor = null;
				final TSensorMeasurementDescriptor MeasurementDescriptor = _MeasurementDescriptor;
				//.
	    		final CharSequence[] _items;
	    		int SelectedIdx = -1;
	    		if (MeasurementDescriptor != null) {
	    			if (MeasurementDescriptor.IsTypeOf(com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.ModelTypeID)) {
			    		_items = new CharSequence[5];	    		
			    		_items[0] = getString(R.string.SOpen); 
			    		_items[1] = getString(R.string.SRemove); 
			    		_items[2] = getString(R.string.SAttachToMyUserActivityAsDatafile); 
			    		_items[3] = getString(R.string.SExportToFile); 
			    		_items[4] = getString(R.string.SExportToFileAndAttachToMyActivityAsDataFile); 
	    			}
	    			else {
			    		_items = new CharSequence[3];	    		
			    		_items[0] = getString(R.string.SOpen); 
			    		_items[1] = getString(R.string.SRemove); 
			    		_items[2] = getString(R.string.SAttachToMyUserActivityAsDatafile); 
	    			}
	    		}
	    		else {
		    		_items = new CharSequence[2];	    		
		    		_items[0] = getString(R.string.SOpen); 
		    		_items[1] = getString(R.string.SRemove); 
	    		}
	    		//.
	    		AlertDialog.Builder builder = new AlertDialog.Builder(TSensorsModuleMeasurementsArchive.this);
	    		builder.setTitle(R.string.SSelect);
	    		builder.setNegativeButton(R.string.SClose,null);
	    		builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
	    			
	    			@Override
	    			public void onClick(DialogInterface arg0, int arg1) {
	    		    	try {
	    		    		switch (arg1) {
	    		    		
	    		    		case 0: //. open
	    						try {
	    							TSensorsModuleMeasurementsArchive.StartOpeningItem(null, Device.idGeographServerObject, Item, null,0, TSensorsModuleMeasurementsArchive.this, new TSensorMeasurementDescriptor.TLocationUpdater() {
	    								
	    								@Override
	    								public void DoOnLocationUpdated(String MeasurementID, int Location) {
	    									Items_UpdateItemLocation(MeasurementID, Location);
	    								}
	    							});
		    						//.
		        		    		arg0.dismiss();
	    						}
	    						catch (Exception E) {
	    			                Toast.makeText(TSensorsModuleMeasurementsArchive.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    						}
	        		    		//.
	    		    			break; //. >
	    		    			
	    		    		case 1: //. remove
	    						try {
	    			    		    new AlertDialog.Builder(TSensorsModuleMeasurementsArchive.this)
	    			    	        .setIcon(android.R.drawable.ic_dialog_alert)
	    			    	        .setTitle(R.string.SConfirmation)
	    			    	        .setMessage(R.string.SRemoveTheItem)
	    			    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
	    			    		    	
	    			    		    	@Override
	    			    		    	public void onClick(DialogInterface dialog, int id) {
	    			    		    		Items_Remove(Item.ID);
	    			    		    	}
	    			    		    })
	    			    		    .setNegativeButton(R.string.SNo, null)
	    			    		    .show();
		    						//.
		        		    		arg0.dismiss();
	    						}
	    						catch (Exception E) {
	    			                Toast.makeText(TSensorsModuleMeasurementsArchive.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    						}
	        		    		//.
	    		    			break; //. >
	    		    			
	    		    		case 2: //. attach the measurement to my user activity as a data-file
    		    				String _DataName = MeasurementDescriptor.TypeID()+"("+OleDate.Format("yyyy/MM/dd HH:mm:ss",OleDate.UTCToLocalTime(MeasurementDescriptor.StartTimestamp))+")"; 
    		    		    	//.
    		    		    	TEnterStringDialog.Dialog(TSensorsModuleMeasurementsArchive.this, getString(R.string.SDataName), getString(R.string.SEnterName), _DataName, TTrackerPanel.TConfiguration.TGPSModuleConfiguration.MapPOI_DataNameMaxSize, new TEnterStringDialog.TOnStringEnteredHandler() {
                        			
                        			@Override
                        			public void DoOnStringEntered(String Str)  throws Exception {
            		    		    	if ((Str != null) && (Str.length() > 0))
            		    		    		Str = "@"+TComponentFileStreaming.CheckAndEncodeFileNameString(Str);
            		    		    	else
            		    		    		Str = "";
            		    				//.
                        				final String DataName = Str;
                        				//.
        	    		    			TAsyncProcessing Processing = new TAsyncProcessing(TSensorsModuleMeasurementsArchive.this) {

        	    		    				@Override
        	    		    				public void Process() throws Exception {
        	    	    				    	TTracker Tracker = TTracker.GetTracker();
        	    	    				    	if (Tracker == null)
        	    	    				    		throw new Exception(TSensorsModuleMeasurementsArchive.this.getString(R.string.STrackerIsNotInitialized)); //. =>
        	    	    				    	//.
        	    	    				    	String MeasurementDatabaseFolder = com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_Folder(Tracker.GeoLog.idGeographServerObject);
        	    								TMeasurement Measurement = new TMeasurement(Tracker.GeoLog.idGeographServerObject, MeasurementDatabaseFolder, MeasurementDatabaseFolder, Item.ID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
        	        		    		    	byte[] Data = Measurement.ToByteArray();
        	        		    		    	double Timestamp = OleDate.UTCCurrentTimestamp();
        	        		    				String NFN = TGPSModule.MapPOIComponentFolder()+"/"+MeasurementDescriptor.GUID+DataName+TSensorMeasurement.DataFileFormat;
        	        		    				File NF = new File(NFN);
        	        		    				FileOutputStream FOS = new FileOutputStream(NF);
        	        		    				try {
        	        		    					FOS.write(Data);
        	        		    				}
        	        		    				finally {
        	        		    					FOS.close();
        	        		    				}
        	        		    				//. prepare and send data-file
        	        		    		    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(Timestamp,NFN);
        	        		    		    	DataFile.Create(Tracker.GeoLog);
        	    		    				}

        	    		    				@Override
        	    		    				public void DoOnCompleted() throws Exception {
        	    		    					if (!Canceller.flCancel && flRunning) 
        	    			    		    		Toast.makeText(TSensorsModuleMeasurementsArchive.this, "Datafile of the measurement has been added.", Toast.LENGTH_LONG).show();
        	    		    				}
        	    		    			};
        	    		    			Processing.Start();
                        			}
                        		});
	    						//.
	        		    		arg0.dismiss();
    		    		    	//.
	    		    			break; 
	    		    			
	    		    		case 3: //. export to a file
	    		    			TAsyncProcessing Exporting = new TAsyncProcessing(TSensorsModuleMeasurementsArchive.this) {

	    		    				private static final String ExpertFileName = "Clip.mp4";
	    		    				
	    		    				private String ExportFile;
	    		    				
	    		    				@Override
	    		    				public void Process() throws Exception {
	    		    					ExportFile = TGeoLogApplication.TempFolder+"/"+ExpertFileName;
	    		    					//.
	    								String MeasurementDatabaseFolder = com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_Folder(Device.idGeographServerObject); 
	    		    					com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.ExportMeasurementToMP4File(MeasurementDatabaseFolder, MeasurementDatabaseFolder, Item.ID, ExportFile);
	    		    				}

	    		    				@Override
	    		    				public void DoOnCompleted() throws Exception {
	    		    					if (!Canceller.flCancel && flRunning) {
	    		    					    new AlertDialog.Builder(TSensorsModuleMeasurementsArchive.this)
	    		    				        .setIcon(android.R.drawable.ic_dialog_info)
	    		    				        .setTitle(R.string.SOperationIsDone)
	    		    				        .setMessage(getString(R.string.SAVDataHasBeenExportedToFile)+ExportFile)
	    		    					    .setPositiveButton(R.string.SOpen, new DialogInterface.OnClickListener() {

	    		    					    	@Override
	    		    					    	public void onClick(DialogInterface dialog, int id) {
	    		    								try {
	    		    									Intent intent = new Intent();
	    		    									intent.setDataAndType(Uri.fromFile(new File(ExportFile)), "video/*");
	    		    									intent.setAction(android.content.Intent.ACTION_VIEW);
	    		    									startActivity(intent);
	    		    								} catch (Exception E) {
	    		    									Toast.makeText(TSensorsModuleMeasurementsArchive.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    		    									return; // . ->
	    		    								}
	    		    					    	}
	    		    					    })
	    		    					    .setNegativeButton(R.string.SClose, null)
	    		    					    .show();
	    		    					}
	    		    				}
	    		    			};
	    		    			Exporting.Start();
	    						//.
	        		    		arg0.dismiss();
	        		    		//.
	    		    			break; //. >

	    		    		case 4: //. export to a file and attach to my activity as data-file
	    		    			_DataName = MeasurementDescriptor.TypeID()+"("+OleDate.Format("yyyy/MM/dd HH:mm:ss",OleDate.UTCToLocalTime(MeasurementDescriptor.StartTimestamp))+")"; 
    		    		    	//.
    		    		    	TEnterStringDialog.Dialog(TSensorsModuleMeasurementsArchive.this, getString(R.string.SDataName), getString(R.string.SEnterName), _DataName, TTrackerPanel.TConfiguration.TGPSModuleConfiguration.MapPOI_DataNameMaxSize, new TEnterStringDialog.TOnStringEnteredHandler() {
                        			
                        			@Override
                        			public void DoOnStringEntered(String Str)  throws Exception {
            		    		    	if ((Str != null) && (Str.length() > 0))
            		    		    		Str = "@"+TComponentFileStreaming.CheckAndEncodeFileNameString(Str);
            		    		    	else
            		    		    		Str = "";
            		    				//.
                        				final String DataName = Str;
                        				//.
        	    		    			TAsyncProcessing Processing = new TAsyncProcessing(TSensorsModuleMeasurementsArchive.this) {

        	    		    				@Override
        	    		    				public void Process() throws Exception {
        	    	    				    	TTracker Tracker = TTracker.GetTracker();
        	    	    				    	if (Tracker == null)
        	    	    				    		throw new Exception(TSensorsModuleMeasurementsArchive.this.getString(R.string.STrackerIsNotInitialized)); //. =>
        	    								//.
        	        		    				String NFN = TGPSModule.MapPOIComponentFolder()+"/"+MeasurementDescriptor.GUID+DataName+".MP4";
        	    								String MeasurementDatabaseFolder = com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_Folder(Tracker.GeoLog.idGeographServerObject); 
        	        		    				com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.ExportMeasurementToMP4File(MeasurementDatabaseFolder, MeasurementDatabaseFolder, Item.ID, NFN);
        	    		    					//.
        	        		    		    	double Timestamp = OleDate.UTCCurrentTimestamp();
        	        		    				//. prepare and send data-file
        	        		    		    	TGeoScopeServerUserDataFile DataFile = new TGeoScopeServerUserDataFile(Timestamp,NFN);
        	        		    		    	DataFile.Create(Tracker.GeoLog);
        	    		    				}

        	    		    				@Override
        	    		    				public void DoOnCompleted() throws Exception {
        	    		    					if (!Canceller.flCancel && flRunning) 
        	    			    		    		Toast.makeText(TSensorsModuleMeasurementsArchive.this, "Datafile of the measurement has been added.", Toast.LENGTH_LONG).show();
        	    		    				}
        	    		    			};
        	    		    			Processing.Start();
                        			}
                        		});
	    						//.
	        		    		arg0.dismiss();
	        		    		//.
	    		    			break; //. >
	    		    		}
	    		    	}
	    		    	catch (Exception E) {
	    		    		Toast.makeText(TSensorsModuleMeasurementsArchive.this, E.getMessage(), Toast.LENGTH_LONG).show();
	    		    		//.
	    		    		arg0.dismiss();
	    		    	}
	    			}
	    		});
	    		AlertDialog alert = builder.create();
	    		alert.show();
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
		try {
			StopUpdating();
		} catch (InterruptedException E) {
		}
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
    
    public void StopUpdating() throws InterruptedException {
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
    
	public static TArchiveItem[] GetItemsList(long idGeographServerObject, double BeginTimestamp, double EndTimestamp, Context context, TCanceller Canceller) throws Exception {
		String ListString = TSensorsModuleMeasurements.GetMeasurementsList(BeginTimestamp,EndTimestamp, (short)1); 
		TSensorMeasurementDescriptor[] DVRMs;
		if ((ListString != null) && (ListString.length() > 0)) {
			String[] Items = ListString.split(";");
			DVRMs = new TSensorMeasurementDescriptor[Items.length];
			for (int I = 0; I < Items.length; I++) {
				String[] Properties = Items[I].split(",");
				DVRMs[I] = new TSensorMeasurementDescriptor();
				//.
				DVRMs[I].ID = Properties[0];
				//.
				DVRMs[I].StartTimestamp = Double.parseDouble(Properties[1]);
				DVRMs[I].FinishTimestamp = Double.parseDouble(Properties[2]);
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel Model = new com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel();
				Model.TypeID = Properties[3];
				Model.ContainerTypeID = Properties[4];
				DVRMs[I].Model = Model; 
				//.
				DVRMs[I].Location = TSensorMeasurementDescriptor.LOCATION_DEVICE;
			}
		}
		else
			DVRMs = new TSensorMeasurementDescriptor[0];
		//.
		TSensorMeasurementDescriptor[] CVRMs = com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_GetMeasurementsList(idGeographServerObject, BeginTimestamp,EndTimestamp); 
		//.
		int DVRMs_Count = 0;
		if (DVRMs != null)
			for (int I = 0; I < DVRMs.length; I++) {
				boolean flFound = false;
				for (int J = 0; J < CVRMs.length; J++) 
					if (TSensorMeasurementDescriptor.IDsAreTheSame(DVRMs[I].ID, CVRMs[J].ID)) {
						flFound = true;
						break; //. >
					}
				if (flFound) 
					DVRMs[I] = null;
				else
					DVRMs_Count++;
			}
		//.
		TArchiveItem[] Result = new TArchiveItem[DVRMs_Count+CVRMs.length];
		int Idx = 0;
		//.
		if (DVRMs != null)
			for (int I = 0; I < DVRMs.length; I++) 
				if (DVRMs[I] != null) {
					Result[Idx] = new TArchiveItem();
					//.
					Result[Idx].ID = DVRMs[I].ID;
					//.
					Result[Idx].StartTimestamp = DVRMs[I].StartTimestamp;
					Result[Idx].FinishTimestamp = DVRMs[I].FinishTimestamp;
					//.
					Result[Idx].TypeID = DVRMs[I].TypeID();
					Result[Idx].ContainerTypeID = DVRMs[I].ContainerTypeID();
					//.
					Result[Idx].Name = DVRMs[I].Name();
					Result[Idx].Info = DVRMs[I].Info();
					//.
					Result[Idx].Location = TSensorMeasurementDescriptor.LOCATION_DEVICE;
					//.
					Idx++;
				}
		//.
		for (int I = 0; I < CVRMs.length; I++) 
			if (CVRMs[I] != null) {
				Result[Idx] = new TArchiveItem();
				//.
				Result[Idx].ID = CVRMs[I].ID;
				//.
				Result[Idx].StartTimestamp = CVRMs[I].StartTimestamp;
				Result[Idx].FinishTimestamp = CVRMs[I].FinishTimestamp;
				//.
				Result[Idx].TypeID = CVRMs[I].TypeID();
				Result[Idx].ContainerTypeID = CVRMs[I].ContainerTypeID();
				//.
				Result[Idx].Name = CVRMs[I].Name();
				Result[Idx].Info = CVRMs[I].Info();
				//.
				Result[Idx].Location = TSensorMeasurementDescriptor.LOCATION_CLIENT;
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
	
	private void Items_SetAndUpdateList(TArchiveItem[] pItems) throws Exception {
		String SelectedMeasurementID = "";
		int SIP = lvVideoRecorderServerArchive.getCheckedItemPosition();
		if ((SIP != AdapterView.INVALID_POSITION) && (Items != null))
			SelectedMeasurementID = Items[SIP].ID;
		//.
		synchronized (this) {
			Items = pItems;				
		}
		if (Items.length == 0) {
			lvVideoRecorderServerArchive.setAdapter(null);
    		return; //. ->
		}
		//.
		int SaveIndex = lvVideoRecorderServerArchive.getFirstVisiblePosition();
		View V = lvVideoRecorderServerArchive.getChildAt(0);
		int SaveTop = (V == null) ? 0 : V.getTop();
		//.
		int SelectedIdx = -1;
		final String[] lvItems = new String[Items.length];
		for (int I = 0; I < Items.length; I++) {
			OleDate DT = new OleDate(OleDate.UTCToLocalTime(Items[I].StartTimestamp));
			String DTS = NormalizeNumberString(Integer.toString(DT.year),4)+"/"+NormalizeNumberString(Integer.toString(DT.month),2)+"/"+NormalizeNumberString(Integer.toString(DT.date),2)+" "+NormalizeNumberString(Integer.toString(DT.hrs),2)+":"+NormalizeNumberString(Integer.toString(DT.min),2)+":"+NormalizeNumberString(Integer.toString(DT.sec),2);
			int TimeInterval = (int)((Items[I].FinishTimestamp-Items[I].StartTimestamp)*24.0*3600.0);
			String TIS;
			if (TimeInterval < 60)
				TIS = Integer.toString(TimeInterval)+getString(R.string.SSec);
			else
				TIS = Integer.toString((int)(TimeInterval/60))+getString(R.string.SMin)+" "+Integer.toString(TimeInterval % 60)+getString(R.string.SSec);
			String TypeName = Items[I].Name;
			if (TypeName.equals("")) {
				com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.TModel.TTypeInfo TypeInfo = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.TModel.GetTypeInfo(Items[I].TypeID, this);
				if (TypeInfo != null)
					TypeName = TypeInfo.TypeName;
			}
			String SideS = "";
			switch (Items[I].Location) {
			case TSensorMeasurementDescriptor.LOCATION_CLIENT:
				SideS = getString(R.string.SAtClient);
				break; //. >
			}
			String RS = DTS+" ["+TypeName+"]   "+TIS+"  "+SideS;
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
		//.
		lvVideoRecorderServerArchive.setSelectionFromTop(SaveIndex, SaveTop);
	}
	
	private void Items_UpdateItemLocation(String MeasurementID, int Location) {
		int Cnt = Items.length;
		for (int I = 0; I < Cnt; I++) {
			TArchiveItem Item = Items[I];
			if (TSensorMeasurementDescriptor.IDsAreTheSame(Item.ID, MeasurementID)) {
				Item.Location = Location;
				return; //. ->
			}
		}
	}
	
	private static void Items_DoRemove(Context context, long idGeographServerObject, String MeasurementID, TCanceller Canceller) throws Exception {
		TSensorsModuleMeasurements.DeleteMeasurement(MeasurementID);
		//.
		com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_RemoveMeasurement(idGeographServerObject, MeasurementID);
	}
	
	private void Items_Remove(final String MeasurementID) {
		TAsyncProcessing Removing = new TAsyncProcessing(TSensorsModuleMeasurementsArchive.this) {

			@Override
			public void Process() throws Exception {
				Items_DoRemove(TSensorsModuleMeasurementsArchive.this, Device.idGeographServerObject, MeasurementID, Canceller);
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (!Canceller.flCancel && flRunning) 
					StartUpdating();
			}
		};
		Removing.Start();
	}
	
	public static void StartOpeningItem(TCanceller Canceller, final long idGeographServerObject, TArchiveItem Item, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsArchive.TMeasurementProcessHandler ProcessHandler, int ProcessorRequest, final double BeginTimestamp, final double EndTimestamp, final Activity context, TSensorMeasurementDescriptor.TLocationUpdater LocationUpdater) {
		switch (Item.Location) {

		case TSensorMeasurementDescriptor.LOCATION_DEVICE:
			new TSensorsModuleMeasurementsArchive.TDeviceMeasurementProcessing(Canceller, idGeographServerObject, Item.ID, Item.StartTimestamp, Item.FinishTimestamp, Item.Position, ProcessHandler,ProcessorRequest, context, LocationUpdater);
			break; //. >
			
		case TSensorMeasurementDescriptor.LOCATION_CLIENT:
			try {
				com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_ProcessMeasurement(ProcessHandler,ProcessorRequest, idGeographServerObject, Item.ID, Item.Position, context);
			} catch (Exception E) {
			}
			break; //. >
		}
	}
	
	public static void StartOpeningItem(TCanceller Canceller, final long idGeographServerObject, TArchiveItem Item, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsArchive.TMeasurementProcessHandler ProcessHandler, int ProcessorRequest, final Activity context, TSensorMeasurementDescriptor.TLocationUpdater LocationUpdater) {
		StartOpeningItem(Canceller, idGeographServerObject, Item, ProcessHandler,ProcessorRequest, -Double.MAX_VALUE,Double.MAX_VALUE, context, LocationUpdater);
	}
	
    private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESS 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;
    	
        private ProgressDialog progressDialog; 
    	
    	public TUpdating() {
    		super();
    		//.
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
    				_Items = GetItemsList(Device.idGeographServerObject, -Double.MAX_VALUE,Double.MAX_VALUE, TSensorsModuleMeasurementsArchive.this, Canceller);
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
        			if (!isFinishing()) 
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
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SUCCESS:
	                	TArchiveItem[] _Items = (TArchiveItem[])msg.obj;
	                	try {
	                    	Items_SetAndUpdateList(_Items);
	                	}
	                	catch (Exception E) {
	                		Toast.makeText(TSensorsModuleMeasurementsArchive.this, E.getMessage(), Toast.LENGTH_LONG).show();
	                	}
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_SHOWEXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TSensorsModuleMeasurementsArchive.this, TSensorsModuleMeasurementsArchive.this.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TSensorsModuleMeasurementsArchive.this);    
		            	progressDialog.setMessage(TSensorsModuleMeasurementsArchive.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
		            		
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (Items == null)
									TSensorsModuleMeasurementsArchive.this.finish();
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
	                	catch (IllegalArgumentException IAE) {} //. TODO
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

    public static class TDeviceMeasurementProcessing extends TCancelableThread {

    	public static final int CheckCompletionInterval = 1000*5; //. seconds
    	
    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESSLOCALLY 		= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 			= 4;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 			= 5;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 		= 6;
   	
    	private long idGeographServerObject;
    	//.
    	private String	MeasurementID;
    	@SuppressWarnings("unused")
		private double 	MeasurementStartTimestamp;
    	@SuppressWarnings("unused")
		private double 	MeasurementFinishTimestamp;
    	private double 	MeasurementPosition;
		//.
		private TSensorMeasurementDescriptor.TLocationUpdater LocationUpdater;
		//.
		private com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsArchive.TMeasurementProcessHandler 	ProcessHandler;
		//.
		private int 						ProcessorRequest;
		//.
		private Activity context;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TDeviceMeasurementProcessing(TCanceller pCanceller, long pidGeographServerObject, String pMeasurementID, double pMeasurementStartTimestamp, double pMeasurementFinishTimestamp, double pMeasurementPosition, com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsArchive.TMeasurementProcessHandler pProcessHandler, int pProcessorRequest, Activity pcontext, TSensorMeasurementDescriptor.TLocationUpdater pLocationUpdater) {
    		super(pCanceller);
    		//.
    		idGeographServerObject = pidGeographServerObject;
    		//.
    		MeasurementID = pMeasurementID;
    		MeasurementStartTimestamp = pMeasurementStartTimestamp;
    		MeasurementFinishTimestamp = pMeasurementFinishTimestamp;
    		MeasurementPosition = pMeasurementPosition;
    		//.
    		ProcessHandler = pProcessHandler;
    		ProcessorRequest = pProcessorRequest;
    		//.
    		LocationUpdater = pLocationUpdater;
    		//.
    		context = pcontext;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				Canceller.SetOwnerThread(this);
				//.
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
					String MeasurementFolder = com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_GetMeasurementFolder(idGeographServerObject, MeasurementID);
					if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.MeasurementExists(MeasurementFolder)) { 
	        			MessageHandler.obtainMessage(MESSAGE_SUCCESSLOCALLY,MeasurementFolder).sendToTarget();
	        			//.
	        			return; //. ->
					}
    				//.
					TNamedReadWriteLock MeasurementLock = TNamedReadWriteLock.ReadLock(com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Domain, MeasurementID);
					try {
    					String SrcMeasurementFolder = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.DataBaseFolder+"/"+MeasurementID;
    					String MeasurementTempFolder = com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_GetMeasurementTempFolder(idGeographServerObject, MeasurementID);
    					try {
    						byte[] CopyBuffer = new byte[1024*1024];
        					TFileSystem.CopyFolder(new File(SrcMeasurementFolder), new File(MeasurementTempFolder), CopyBuffer, Canceller);
            				//. complete measurement folder
            				File MF = new File(MeasurementTempFolder);
            				File NMF = new File(MeasurementFolder);
            				NMF.getParentFile().mkdirs();
            				MF.renameTo(NMF);
    					} catch (CancelException CE) {
    	    				return; //. ->
    					} catch (Exception E) {
    						TFileSystem.RemoveFolder(new File(MeasurementTempFolder));
    						throw E; //. =>
    					}
					}
					finally {
						MeasurementLock.ReadUnLock();
					}
    				//.
        			MessageHandler.obtainMessage(MESSAGE_SUCCESSLOCALLY,MeasurementFolder).sendToTarget();
    			}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
        	}
        	catch (InterruptedException E) {
        	}
			catch (CancelException CE) {
			}
        	catch (NullPointerException NPE) { 
        		try {
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
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SUCCESSLOCALLY:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
	                	String _MeasurementFolder = (String)msg.obj;
	                	//.
	                	if (LocationUpdater != null) 
	                		LocationUpdater.DoOnLocationUpdated(MeasurementID, TSensorMeasurementDescriptor.LOCATION_CLIENT);
	                	//.
	                	com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.Context_ProcessMeasurementByFolder(ProcessHandler,ProcessorRequest, idGeographServerObject, _MeasurementFolder, MeasurementPosition, context);
	                	//.
		            	break; //. >
		            	
		            case MESSAGE_SHOWEXCEPTION:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(context, context.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(context);    
		            	progressDialog.setMessage(context.getString(R.string.SLoadingFromDeviceToServer));    
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
	                	try {
		                	progressDialog.dismiss(); 
	                	}
	                	catch (IllegalArgumentException IAE) {} //. TODO
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
}
