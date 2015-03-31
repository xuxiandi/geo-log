package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements;

import java.io.File;
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
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1Model;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographDataServerClient;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TSensorsModuleMeasurementsArchive extends Activity {

	public static final int PARAMETERS_TYPE_OID 	= 1;
	public static final int PARAMETERS_TYPE_OIDX 	= 2;
	
	public static class TMeasurementProcessHandler {
		
		public boolean ProcessMeasurement(final TSensorMeasurement Measurement, double MeasurementPosition) throws Exception {
			return false;
		}
	}
	
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
		public double CPC = 0.0;
		//.
		public int Location = TSensorMeasurementDescriptor.LOCATION_DEVICE;
		//.
		public double Position = 0.0;
	}
	
	public static class TArchiveItemsProvider {
		
		protected TArchiveItem[] GetItemsList(TCanceller Canceller) throws Exception {
			return null;
		}
	}
	
	public static class TArchiveItemsListUpdater {
		
		public void DoOnItemsListUpdated(TArchiveItem[] Items) throws Exception {
		}
	}
	

	private String 	GeographDataServerAddress = "";
	private int 	GeographDataServerPort = 0;
	@SuppressWarnings("unused")
	private long	UserID;
	@SuppressWarnings("unused")
	private String	UserPassword;
	//.
	private TCoGeoMonitorObject Object;
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
        	int ParametersType = extras.getInt("ParametersType");
        	GeographDataServerAddress = extras.getString("GeographDataServerAddress");
        	GeographDataServerPort = extras.getInt("GeographDataServerPort");
        	UserID = extras.getLong("UserID");
        	UserPassword = extras.getString("UserPassword");
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
				TSensorsModuleMeasurementsArchive.StartOpeningItem(null, Items[arg2], null,0, Object, GeographDataServerAddress,GeographDataServerPort, TSensorsModuleMeasurementsArchive.this, new TArchiveItemsListUpdater() {
					
					@Override
					public void DoOnItemsListUpdated(TArchiveItem[] Items) throws Exception {
	                	Items_SetAndUpdateList(Items);
					}
				}, new TSensorMeasurementDescriptor.TLocationUpdater() {
					
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
				TSensorMeasurementDescriptor MeasurementDescriptor = null;
				if (Item.Location == TSensorMeasurementDescriptor.LOCATION_CLIENT)
					try {
						MeasurementDescriptor = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.GetMeasurementDescriptor(TSensorsModuleMeasurements.Context_Folder(Object.GeographServerObjectID()),Item.ID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance);
					} catch (Exception E) {
					}
				//.
	    		final CharSequence[] _items;
	    		int SelectedIdx = -1;
	    		if ((MeasurementDescriptor != null) && MeasurementDescriptor.IsTypeOf(com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.ModelTypeID)) {
		    		_items = new CharSequence[3];	    		
		    		_items[0] = getString(R.string.SOpen); 
		    		_items[1] = getString(R.string.SRemove); 
		    		_items[2] = getString(R.string.SExportToFile); 
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
	    							TSensorsModuleMeasurementsArchive.StartOpeningItem(null, Item, null,0, Object, GeographDataServerAddress,GeographDataServerPort, TSensorsModuleMeasurementsArchive.this, new TArchiveItemsListUpdater() {
	    								
	    								@Override
	    								public void DoOnItemsListUpdated(TArchiveItem[] Items) throws Exception {
	    				                	Items_SetAndUpdateList(Items);
	    								}
	    							}, new TSensorMeasurementDescriptor.TLocationUpdater() {
	    								
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
	    		    			
	    		    		case 2: //. export to a file
	    		    			TAsyncProcessing Exporting = new TAsyncProcessing(TSensorsModuleMeasurementsArchive.this) {

	    		    				private static final String ExpertFileName = "Clip.mp4";
	    		    				
	    		    				private String ExportFile;
	    		    				
	    		    				@Override
	    		    				public void Process() throws Exception {
	    		    					ExportFile = TGeoLogApplication.TempFolder+"/"+ExpertFileName;
	    		    					//.
	    		    					com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoRecorderMeasurements.ExportMeasurementToMP4File(TSensorsModuleMeasurements.Context_Folder(Object.GeographServerObjectID()), Item.ID, ExportFile);
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
    
	public static TArchiveItem[] GetItemsList(double BeginTimestamp, double EndTimestamp, TCoGeoMonitorObject Object, String GeographDataServerAddress, int GeographDataServerPort, Context context, TCanceller Canceller) throws Exception {
		TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model(Object.GeographServerObjectController());
		//.
		TSensorMeasurementDescriptor[] DVRMs;
		try {
			DVRMs = ObjectModel.SensorsModule_Measurements_GetList(BeginTimestamp,EndTimestamp); 
		}
		catch (Exception E) {
			DVRMs = null;
		}
		//.
		TSensorMeasurementDescriptor[] SVRMs;
		TGeographDataServerClient GeographDataServerClient = new TGeographDataServerClient(context, GeographDataServerAddress,GeographDataServerPort, Object.Server.User.UserID,Object.Server.User.UserPassword, Object.GeographServerObjectID());
		try {
			SVRMs = GeographDataServerClient.SERVICE_GETSENSORDATA_GetMeasurementList(BeginTimestamp,EndTimestamp, Canceller);
		}
		finally {
			GeographDataServerClient.Destroy();
		}
		//.
		TSensorMeasurementDescriptor[] CVRMs;
		CVRMs = TSensorsModuleMeasurements.Context_GetMeasurementsList(Object.GeographServerObjectID(), BeginTimestamp,EndTimestamp); 
		//.
		int DVRMs_Count = 0;
		if (DVRMs != null)
			for (int I = 0; I < DVRMs.length; I++) {
				boolean flFound = false;
				for (int J = 0; J < SVRMs.length; J++) 
					if (TSensorMeasurementDescriptor.IDsAreTheSame(DVRMs[I].ID, SVRMs[J].ID)) {
						flFound = true;
						break; //. >
					}
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
		int SVRMs_Count = 0;
		if (SVRMs != null)
			for (int I = 0; I < SVRMs.length; I++) {
				boolean flFound = false;
				for (int J = 0; J < CVRMs.length; J++) 
					if (TSensorMeasurementDescriptor.IDsAreTheSame(SVRMs[I].ID, CVRMs[J].ID)) {
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
					Result[Idx].CPC = 1.0;
					//.
					Idx++;
				}
		//.
		if (SVRMs != null)
			for (int I = 0; I < SVRMs.length; I++) 
				if (SVRMs[I] != null) {
					Result[Idx] = new TArchiveItem();
					//.
					Result[Idx].ID = SVRMs[I].ID;
					//.
					Result[Idx].StartTimestamp = SVRMs[I].StartTimestamp;
					Result[Idx].FinishTimestamp = SVRMs[I].FinishTimestamp;
					//.
					Result[Idx].TypeID = SVRMs[I].TypeID();
					Result[Idx].ContainerTypeID = SVRMs[I].ContainerTypeID();
					//.
					Result[Idx].Name = SVRMs[I].Name();
					Result[Idx].Info = SVRMs[I].Info();
					//.
					Result[Idx].Location = TSensorMeasurementDescriptor.LOCATION_SERVER;
					//.
					Result[Idx].CPC = ((TGeographDataServerClient.TSensorMeasurementDescriptor)SVRMs[I]).CPC;
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
				Result[Idx].CPC = 1.0;
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
			case TSensorMeasurementDescriptor.LOCATION_SERVER:
				SideS = getString(R.string.SAtServer);
				break; //. >

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
	
	private static void Items_DoRemove(Context context, TCoGeoMonitorObject Object, String GeographDataServerAddress, int GeographDataServerPort, String MeasurementID, TCanceller Canceller) throws Exception {
		TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model(Object.GeographServerObjectController());
		//.
		ObjectModel.SensorsModule_Measurements_Delete(MeasurementID);
		Canceller.Check();
		//.
		TGeographDataServerClient GeographDataServerClient = new TGeographDataServerClient(context, GeographDataServerAddress,GeographDataServerPort, Object.Server.User.UserID,Object.Server.User.UserPassword, Object.GeographServerObjectID());
		try {
			GeographDataServerClient.SERVICE_GETSENSORDATA_DeleteMeasurements(MeasurementID);
			Canceller.Check();
		}
		finally {
			GeographDataServerClient.Destroy();
		}
		//.
		TSensorsModuleMeasurements.Context_RemoveMeasurement(Object.GeographServerObjectID(), MeasurementID);
	}
	
	private void Items_Remove(final String MeasurementID) {
		TAsyncProcessing Removing = new TAsyncProcessing(TSensorsModuleMeasurementsArchive.this) {

			@Override
			public void Process() throws Exception {
				Items_DoRemove(TSensorsModuleMeasurementsArchive.this, Object, GeographDataServerAddress,GeographDataServerPort, MeasurementID, Canceller);
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (!Canceller.flCancel && flRunning) 
					StartUpdating();
			}
		};
		Removing.Start();
	}
	
	public static void StartOpeningItem(TCanceller Canceller, TArchiveItem Item, TMeasurementProcessHandler ProcessHandler, int ProcessorRequest, final double BeginTimestamp, final double EndTimestamp, final TCoGeoMonitorObject Object, final String GeographDataServerAddress, final int GeographDataServerPort, final Activity context, TArchiveItemsListUpdater ArchiveItemsListUpdater, TSensorMeasurementDescriptor.TLocationUpdater LocationUpdater) {
		switch (Item.Location) {

		case TSensorMeasurementDescriptor.LOCATION_DEVICE:
			new TSensorsModuleMeasurementsArchive.TDeviceMeasurementDownloadingAndProcessing(Canceller, Object, Item.ID, Item.StartTimestamp, Item.FinishTimestamp, Item.Position, GeographDataServerAddress,GeographDataServerPort, Object.Server.User.UserID,Object.Server.User.UserPassword, ProcessHandler,ProcessorRequest, context, new TArchiveItemsProvider() {

				@Override
				protected TArchiveItem[] GetItemsList(TCanceller Canceller) throws Exception {
					return TSensorsModuleMeasurementsArchive.GetItemsList(BeginTimestamp,EndTimestamp, Object, GeographDataServerAddress,GeographDataServerPort, context, Canceller);
				}
			}, ArchiveItemsListUpdater, LocationUpdater);
			break; //. >
			
		case TSensorMeasurementDescriptor.LOCATION_SERVER:
			if (Item.CPC >= 1.0)
				new TSensorsModuleMeasurementsArchive.TGeographServerMeasurementDownloadingAndProcessing(Canceller, Object, Item.ID, Item.StartTimestamp, Item.FinishTimestamp, Item.Position, GeographDataServerAddress,GeographDataServerPort, Object.Server.User.UserID,Object.Server.User.UserPassword, ProcessHandler,ProcessorRequest, context, LocationUpdater);
			else
				new TSensorsModuleMeasurementsArchive.TDeviceMeasurementDownloadingAndProcessing(Canceller, Object, Item.ID, Item.StartTimestamp, Item.FinishTimestamp, Item.Position, GeographDataServerAddress,GeographDataServerPort, Object.Server.User.UserID,Object.Server.User.UserPassword, ProcessHandler,ProcessorRequest, context, new TArchiveItemsProvider() {

					@Override
					protected TArchiveItem[] GetItemsList(TCanceller Canceller) throws Exception {
						return TSensorsModuleMeasurementsArchive.GetItemsList(BeginTimestamp,EndTimestamp, Object, GeographDataServerAddress,GeographDataServerPort, context, Canceller);
					}
				}, ArchiveItemsListUpdater, LocationUpdater);				
			break; //. >
			
		case TSensorMeasurementDescriptor.LOCATION_CLIENT:
			try {
				TSensorsModuleMeasurements.Context_ProcessMeasurement(ProcessHandler,ProcessorRequest, Object.GeographServerObjectID(), Item.ID, Item.Position, context);
			} catch (Exception E) {
			}
			break; //. >
		}
	}
	
	public static void StartOpeningItem(TCanceller Canceller, TArchiveItem Item, TMeasurementProcessHandler ProcessHandler, int ProcessorRequest, final TCoGeoMonitorObject Object, final String GeographDataServerAddress, final int GeographDataServerPort, final Activity context, TArchiveItemsListUpdater ArchiveItemsListUpdater, TSensorMeasurementDescriptor.TLocationUpdater LocationUpdater) {
		StartOpeningItem(Canceller, Item, ProcessHandler,ProcessorRequest, -Double.MAX_VALUE,Double.MAX_VALUE, Object, GeographDataServerAddress,GeographDataServerPort, context, ArchiveItemsListUpdater, LocationUpdater);
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
    				_Items = GetItemsList(-Double.MAX_VALUE,Double.MAX_VALUE, Object, GeographDataServerAddress,GeographDataServerPort, TSensorsModuleMeasurementsArchive.this, Canceller);
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
								TSensorsModuleMeasurementsArchive.this.finish();
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }

    public static class TDeviceMeasurementDownloadingAndProcessing extends TCancelableThread {

    	public static final int CheckCompletionInterval = 1000*15; //. seconds
    	
    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESSLOCALLY 		= 1;
    	private static final int MESSAGE_SUCCESS 				= 2;
    	private static final int MESSAGE_DOONITEMSISUPDATED 	= 3;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 			= 4;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 			= 5;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 		= 6;
   	
    	private TCoGeoMonitorObject Object;
    	//.
    	private String	MeasurementID;
    	private double 	MeasurementStartTimestamp;
    	private double 	MeasurementFinishTimestamp;
    	private double 	MeasurementPosition;
    	//.
    	private String 	GeographDataServerAddress;
    	private int 	GeographDataServerPort;
    	private long 	UserID;
    	private String 	UserPassword; 
		//.
		private TArchiveItemsProvider 		ArchiveItemsProvider;
		private TArchiveItemsListUpdater 	ArchiveItemsListUpdater;
		//.
		private TSensorMeasurementDescriptor.TLocationUpdater LocationUpdater;
		//.
		private TMeasurementProcessHandler 	ProcessHandler;
		private int 						ProcessorRequest;
		//.
		private Activity context;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TDeviceMeasurementDownloadingAndProcessing(TCanceller pCanceller, TCoGeoMonitorObject pObject, String pMeasurementID, double pMeasurementStartTimestamp, double pMeasurementFinishTimestamp, double pMeasurementPosition, String pGeographDataServerAddress, int pGeographDataServerPort, long pUserID, String pUserPassword, TMeasurementProcessHandler pProcessHandler, int pProcessorRequest, Activity pcontext, TArchiveItemsProvider pArchiveItemsProvider, TArchiveItemsListUpdater pArchiveItemsListUpdater, TSensorMeasurementDescriptor.TLocationUpdater pLocationUpdater) {
    		super(pCanceller);
    		//.
    		Object = pObject;
    		//.
    		MeasurementID = pMeasurementID;
    		MeasurementStartTimestamp = pMeasurementStartTimestamp;
    		MeasurementFinishTimestamp = pMeasurementFinishTimestamp;
    		MeasurementPosition = pMeasurementPosition;
    		//.
    		GeographDataServerAddress = pGeographDataServerAddress;
    		GeographDataServerPort = pGeographDataServerPort;
    		UserID = pUserID;
    		UserPassword = pUserPassword;
    		//.
    		ProcessHandler = pProcessHandler;
    		ProcessorRequest = pProcessorRequest;
    		//.
    		ArchiveItemsProvider = pArchiveItemsProvider;
    		ArchiveItemsListUpdater = pArchiveItemsListUpdater;
    		//.
    		LocationUpdater = pLocationUpdater;
    		//.
    		context = pcontext;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

    	private static Object MeasurementCopyingLock = new Object(); 
    	
		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
					String MeasurementFolder = TSensorsModuleMeasurements.Context_GetMeasurementFolder(Object.GeographServerObjectID(), MeasurementID);
					if (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.MeasurementExists(MeasurementFolder)) { 
	        			MessageHandler.obtainMessage(MESSAGE_SUCCESSLOCALLY,MeasurementFolder).sendToTarget();
	        			//.
	        			return; //. ->
					}
    				//.
    				long ThisGeographServerObjectID = 0;
    				TTracker Tracker = TTracker.GetTracker();
    				if (Tracker != null)
    					ThisGeographServerObjectID = Tracker.GeoLog.idGeographServerObject;
    				//.
    				boolean flLocal = (Object.GeographServerObjectID() == ThisGeographServerObjectID);
    				if (flLocal)
    					flLocal = (com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.GetMeasurementDescriptor(com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.DataBaseFolder, MeasurementID, com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.TChannelsProvider.Instance) != null);
    				//.
    				if (!flLocal) {
        				while (!Canceller.flCancel) {
        					boolean flFound = false;
        					boolean flDone = false;
        					TArchiveItem[] _Items = ArchiveItemsProvider.GetItemsList(Canceller);
        					for (int I = 0; I < _Items.length; I++)
        						if ((_Items[I].Location == TSensorMeasurementDescriptor.LOCATION_SERVER) && TSensorMeasurementDescriptor.IDsAreTheSame(_Items[I].ID, MeasurementID)) {
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
        	    				TGeoMonitoredObject1Model ObjectModel = new TGeoMonitoredObject1Model(Object.GeographServerObjectController());
        	    				ObjectModel.SensorsModule_Measurements_MoveToDataServer(MeasurementID);
        		    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,0).sendToTarget();
        					}
        					//.
        					Thread.sleep(CheckCompletionInterval);
        				}
    				}
    				else {
    					synchronized (MeasurementCopyingLock) {
        					String SrcMeasurementFolder = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements.DataBaseFolder+"/"+MeasurementID;
        					String MeasurementTempFolder = TSensorsModuleMeasurements.Context_GetMeasurementTempFolder(Object.GeographServerObjectID(), MeasurementID);
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
	    				//.
	        			MessageHandler.obtainMessage(MESSAGE_SUCCESSLOCALLY,MeasurementFolder).sendToTarget();
	    				//.
	    				return; //. ->
    				}
    			}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
				Canceller.Check();
	    		//.
    			MessageHandler.obtainMessage(MESSAGE_SUCCESS).sendToTarget();
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
	                	TSensorsModuleMeasurements.Context_ProcessMeasurementByFolder(ProcessHandler,ProcessorRequest, Object.GeographServerObjectID(), _MeasurementFolder, MeasurementPosition, context);
	                	//.
		            	break; //. >
		            	
		            case MESSAGE_SUCCESS:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
	                	if (LocationUpdater != null) 
	                		LocationUpdater.DoOnLocationUpdated(MeasurementID, TSensorMeasurementDescriptor.LOCATION_SERVER);
	                	//.
						new TSensorsModuleMeasurementsArchive.TGeographServerMeasurementDownloadingAndProcessing(Canceller, Object, MeasurementID, MeasurementStartTimestamp, MeasurementFinishTimestamp, MeasurementPosition, GeographDataServerAddress,GeographDataServerPort, UserID,UserPassword, ProcessHandler,ProcessorRequest, context, LocationUpdater);					
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
		            	
		            case MESSAGE_DOONITEMSISUPDATED:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
		            	if (ArchiveItemsListUpdater != null) {
		                	TArchiveItem[] _Items = (TArchiveItem[])msg.obj;
		                	try {
		                    	ArchiveItemsListUpdater.DoOnItemsListUpdated(_Items);
		                	}
		                	catch (Exception Ex) {
		                		Toast.makeText(context, Ex.getMessage(), Toast.LENGTH_LONG).show();
		                	}
		            	}
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
		            	//. if (flRunning)
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
    
    public static class TGeographServerMeasurementDownloadingAndProcessing extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESS 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 			= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 			= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 		= 4;
    	private static final int MESSAGE_PROGRESSBAR_ITEMISSTARTED 	= 5;
    	private static final int MESSAGE_PROGRESSBAR_ITEMISFINISHED = 6;
    	private static final int MESSAGE_PROGRESSBAR_ITEMPROGRESS 	= 7;
   	
    	private TCoGeoMonitorObject Object;
    	//.
    	private String MeasurementID;
    	private double MeasurementPosition;
    	//.
    	private String 	GeographDataServerAddress;
    	private int 	GeographDataServerPort;
    	private long	UserID;
    	private String 	UserPassword; 
    	//.
    	private TMeasurementProcessHandler 	ProcessHandler;
    	private int 						ProcessorRequest;
    	//.
    	private Activity context;
    	//.
    	private TSensorMeasurementDescriptor.TLocationUpdater LocationUpdater;
    	//.
    	public String MeasurementTempFolder = null;
    	public String MeasurementFolder = null;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TGeographServerMeasurementDownloadingAndProcessing(TCanceller pCanceller, TCoGeoMonitorObject pObject, String pMeasurementID, double pMeasurementStartTimestamp, double pMeasurementFinishTimestamp, double pMeasurementPosition, String pGeographDataServerAddress, int pGeographDataServerPort, long pUserID, String pUserPassword, TMeasurementProcessHandler pProcessHandler, int pProcessorRequest, Activity pcontext, TSensorMeasurementDescriptor.TLocationUpdater pLocationUpdater) {
    		super(pCanceller);
    		//.
    		Object = pObject;
    		//.
    		MeasurementID = pMeasurementID;
    		MeasurementPosition = pMeasurementPosition;
    		//.
    		GeographDataServerAddress = pGeographDataServerAddress;
    		GeographDataServerPort = pGeographDataServerPort;
    		UserID = pUserID;
    		UserPassword = pUserPassword;
    		//.
    		ProcessHandler = pProcessHandler;
    		ProcessorRequest = pProcessorRequest;
    		context = pcontext;
    		//.
    		LocationUpdater = pLocationUpdater;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

    	private static Object MeasurementLoadingLock = new Object(); 
    	
		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				MeasurementFolder = TSensorsModuleMeasurements.Context_GetMeasurementFolder(Object.GeographServerObjectID(), MeasurementID);
    				synchronized (MeasurementLoadingLock) {
    					try {
            				if (!TSensorsModuleMeasurements.Context_IsMeasurementExist(Object.GeographServerObjectID(), MeasurementID)) {
                				MeasurementTempFolder = TSensorsModuleMeasurements.Context_CreateMeasurementTempFolder(Object.GeographServerObjectID(), MeasurementID);
                				//.
                				TGeographDataServerClient GeographDataServerClient = new TGeographDataServerClient(context, GeographDataServerAddress,GeographDataServerPort, UserID,UserPassword, Object.GeographServerObjectID());
                				try {
            						GeographDataServerClient.SERVICE_GETSENSORDATA_GetMeasurementData(MeasurementID, MeasurementTempFolder, MeasurementItemProgressor,Canceller);
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
    		        	catch (InterruptedException E) {
    		    			RemoveTempMeasurementFolder();
    		        	}
    					catch (CancelException CE) {
    		    			RemoveTempMeasurementFolder();
    					}
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
        	catch (NullPointerException NPE) { 
    			RemoveTempMeasurementFolder();
        		try {
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
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SUCCESS:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	//.
	                	if (LocationUpdater != null) 
	                		LocationUpdater.DoOnLocationUpdated(MeasurementID, TSensorMeasurementDescriptor.LOCATION_CLIENT);
	                	//.
	                	String _MeasurementFolder = (String)msg.obj;
	                	//.
	                	TSensorsModuleMeasurements.Context_ProcessMeasurementByFolder(ProcessHandler,ProcessorRequest, Object.GeographServerObjectID(), _MeasurementFolder, MeasurementPosition, context);
	                	//.
		            	break; //. >
		            	
		            case MESSAGE_SHOWEXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(context, context.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(context);    
		            	progressDialog.setMessage(context.getString(R.string.SLoadingFromServer));    
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
		            	//. if (flRunning)
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
	    
	    private final TGeographDataServerClient.TItemProgressor MeasurementItemProgressor = new TGeographDataServerClient.TItemProgressor() {
	    	@Override
	    	public void DoOnItemIsStarted(String ItemName, int ItemIndex, int ItemCount) {
	    		String S = context.getString(R.string.SFile)+Integer.toString(ItemIndex)+"/"+Integer.toString(ItemCount)+": "+ItemName;
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
