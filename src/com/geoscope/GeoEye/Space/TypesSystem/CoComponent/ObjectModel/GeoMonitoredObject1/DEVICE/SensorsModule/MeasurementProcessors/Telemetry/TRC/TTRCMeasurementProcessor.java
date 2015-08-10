package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.TRC;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedTypedTaggedDataContainerType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.TMeasurementProcessor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Types.AndroidState.TTRCEvent;

public class TTRCMeasurementProcessor extends TMeasurementProcessor {
	
	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.TRC.Model.TModel.ModelTypeID;
	
	public static final int LIST_ROW_SIZE_SMALL_ID 	= 1;
	public static final int LIST_ROW_SIZE_NORMAL_ID = 2;
	public static final int LIST_ROW_SIZE_BIG_ID 	= 3;
	
	public static class TListAdapter extends BaseAdapter {

		private static class TViewHolder {
			
			@SuppressWarnings("unused")
			public TTRCEvent Item;
			//.
			public ImageView 	ivImage;
			public TextView 	lbName;
			public TextView 	lbInfo;
		}
		
		private TTRCMeasurementProcessor Processor;
		//.
		private int	Items_SelectedIndex = -1;
		//.
		private LayoutInflater layoutInflater;
		//.
		public OnClickListener ImageClickListener = new OnClickListener() {
			
			@Override
	        public void onClick(View v) {
	            final int position = Processor.lvList.getPositionForView((View)v.getParent());
	            //.
				@SuppressWarnings("unused")
				TTRCEvent Item = (TTRCEvent)Processor.List[position];
	        }
		};
	        
		public TListAdapter(TTRCMeasurementProcessor pProcessor) {
			Processor = pProcessor;
			//.
			layoutInflater = LayoutInflater.from(Processor.ParentActivity);
		}
		
		@Override
		public int getCount() {
			return Processor.List.length;
		}

		@Override
		public Object getItem(int position) {
			return Processor.List[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void Items_SetSelectedIndex(int Index, boolean flNotify) {
			Items_SelectedIndex = Index;
			//.
			if (flNotify)
				notifyDataSetChanged();
		}
		
		public int Items_GetSelectedIndex() {
			return Items_SelectedIndex;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TViewHolder holder;
			if (convertView == null) {
				int LayoutID = R.layout.measurement_processor_telemetrytrc_row_layout;
				switch (Processor.ListRowSizeID) {
				
				case LIST_ROW_SIZE_SMALL_ID:
					LayoutID = R.layout.measurement_processor_telemetrytrc_row_small_layout;
					break; //. >
					
				case LIST_ROW_SIZE_NORMAL_ID:
					LayoutID = R.layout.measurement_processor_telemetrytrc_row_layout;
					break; //. >
					
				case LIST_ROW_SIZE_BIG_ID:
					LayoutID = R.layout.measurement_processor_telemetrytrc_row_big_layout;
					break; //. >
				}
				convertView = layoutInflater.inflate(LayoutID, null);
				holder = new TViewHolder();
				holder.ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
				holder.lbName = (TextView) convertView.findViewById(R.id.lbName);
				holder.lbInfo = (TextView) convertView.findViewById(R.id.lbInfo);
				//.
				convertView.setTag(holder);
			} 
			else 
				holder = (TViewHolder)convertView.getTag();
			//. updating view
			TTRCEvent Item = (TTRCEvent)Processor.List[position];
			//.
			holder.Item = Item;
			//.
			holder.lbName.setText(Item.Message);
			//.
			holder.lbInfo.setText(OleDate.Format("yyyy/MM/dd HH:mm:ss",OleDate.UTCToLocalTime(Item.Timestamp)));
			//.
			///////holder.ivImage.setImageDrawable(Processor.ParentActivity.getResources().getDrawable(Item.Item.GetThumbnailImageResId()));
			holder.ivImage.setOnClickListener(null);
			//. show selection
			if (position == Items_SelectedIndex) {
	            convertView.setSelected(true);
	            convertView.setBackgroundColor(0xFFFFADB1);
	        }			
			else {
	            convertView.setSelected(false);
            	convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			//.
			return convertView;
		}
	}
	
	private Hashtable<String, TSensorMeasurement> MeasurementCache = null;
	//.
	private volatile TAsyncProcessing 	MeasurementInitializing = null;
	//.
	private TAsyncProcessing 			MeasurementPositioning = null;
	//.
	private volatile TTRCEvent[] 	List = new TTRCEvent[0];
	private int 					ListRowSizeID = LIST_ROW_SIZE_SMALL_ID;
	private TListAdapter			lvListAdapter;
	private ListView 				lvList;
	//.
	private TTLRChannel TLRChannel = null;

	public TTRCMeasurementProcessor() {
		super();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	

	@Override
	public void SetLayout(Activity pParentActivity, LinearLayout pParentLayout) throws Exception {
		super.SetLayout(pParentActivity, pParentLayout);
		//.
		LayoutInflater inflater = (LayoutInflater)ParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.measurement_processor_telemetrytrc_panel, ParentLayout);
        //.
        lvList = (ListView)ParentLayout.findViewById(R.id.lvList);
		lvList.setOnItemClickListener(new OnItemClickListener() {  
			
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	            int position = arg2;
	            //.
				TTRCEvent Item = (TTRCEvent)List[position];
				double ProgressFactor = (Item.Timestamp-Measurement.Descriptor.StartTimestamp)/(Measurement.Descriptor.FinishTimestamp-Measurement.Descriptor.StartTimestamp);
				if ((0.0 <= ProgressFactor) && (ProgressFactor <= 1.0)) {
					if (OnProgressHandler != null) 
						OnProgressHandler.DoOnProgress(ProgressFactor);
				}
				//.
				lvListAdapter.Items_SetSelectedIndex(position, true);
        	}              
        });         
	}

	@Override
	protected void Initialize(TSensorMeasurement pMeasurement) throws Exception {
		super.Initialize(pMeasurement);
		//.
		TSensorMeasurement CachedMeasurement = null;
		if (MeasurementCache != null) 
			CachedMeasurement = MeasurementCache.get(Measurement.Descriptor.ID);
		final boolean flUseCache = (CachedMeasurement != null);
		if (flUseCache)
			Measurement = CachedMeasurement;
		final TSensorMeasurement TheMeasurement = Measurement; 
		MeasurementInitializing = new TAsyncProcessing() {

			@Override
			public void Process() throws Exception {
				Canceller.Check();
				//.
				if (!flUseCache)
					TheMeasurement.Descriptor.Model.Process(Canceller);
				//.
				SetTLRChannel((TTLRChannel)TheMeasurement.Descriptor.Model.Stream.Channels_GetOneByClass(TTLRChannel.class));
				//.
				List_Create();
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				if (MeasurementInitializing == this)
					MeasurementInitializing = null;
				//. caching 
				if (!flUseCache) {
					if (MeasurementCache == null)
						MeasurementCache = new Hashtable<String, TSensorMeasurement>();
					MeasurementCache.put(TheMeasurement.Descriptor.ID, TheMeasurement);
				}
				//.
				flInitialized = true;
				//.
				Update();
			}
			
			@Override
			public void DoOnException(Exception E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Toast.makeText(ParentActivity, S, Toast.LENGTH_LONG).show();
			}
		};
		MeasurementInitializing.Start();
	}
	
	@Override
	protected void Finalize() throws Exception {
		flInitialized = false;
		//.
		if (MeasurementPositioning != null)
			MeasurementPositioning.Cancel();
		//.
		if (MeasurementInitializing != null)
			MeasurementInitializing.Cancel();
		//.
		super.Finalize();
	}

	@Override
	public void Start() throws Exception {
		SetPosition(0.0, 0, true);
	}
	
	@Override
	public void Stop() throws Exception {
		Finalize();
	}
	
	@Override
	public void Pause() {
	}
	
	@Override
	public void Resume() {
	}
	
	@Override
	public void Show() {
		ParentLayout.setVisibility(View.VISIBLE);
		lvList.setVisibility(View.VISIBLE);
		//.
		super.Show();
	}
	
	@Override
	public void Hide() {
		super.Hide();
		//.
		lvList.setVisibility(View.GONE);
		ParentLayout.setVisibility(View.GONE);
	}
	
	@Override
	public void SetPosition(final double Position, final int Delay, final boolean flPaused) throws InterruptedException {
		if (MeasurementPositioning != null) { 
			MeasurementPositioning.Cancel();
			MeasurementPositioning = null;
		}
		//.
		if (/* ignore delay (Delay > 0) || */!flInitialized) {
			MeasurementPositioning = new TAsyncProcessing() {

				@Override
				public void Process() throws Exception {
					/* ignore delay if (Delay > 0)
						Thread.sleep(Delay); */
					while (!Canceller.flCancel) {
						if (flInitialized)
							break; //. >
						Thread.sleep(10); 
					}
				}

				@Override
				public void DoOnCompleted() throws Exception {
					if (!Canceller.flCancel) 
						DoSetPosition(Position, flPaused);
				}
				
				@Override
				public void DoOnFinished() throws Exception {
					if (MeasurementPositioning == this)
						MeasurementPositioning = null;
				}
			};
			MeasurementPositioning.Start();
		}
		else
			DoSetPosition(Position, flPaused);
	}
	
	private void DoSetPosition(double Position, boolean pflPause) {
		if (Position < 0.0)
			return; //. ->
		SetCurrentTime(Measurement.Descriptor.StartTimestamp+Position, true, false);
	}	

	public void SetCurrentTime(double pCurrentTime, boolean flFireEvent, boolean flEventActionDelayAllowed) {
    	double 	MinDistance = Double.MAX_VALUE;
    	int 	MinDistanceIndex = -1;
    	int Cnt = List.length;
    	for (int I = 0; I < Cnt; I++) {
    		double Distance = Math.abs(List[I].Timestamp-pCurrentTime);
    		if (Distance < MinDistance) {
    			MinDistance = Distance;
    			MinDistanceIndex = I;
    		}
    	}
    	if (MinDistanceIndex >= 0) {
    		lvListAdapter.Items_SetSelectedIndex(MinDistanceIndex, false);
    		//.
    		lvList.setItemChecked(MinDistanceIndex, true);
    		lvList.setSelection(MinDistanceIndex);
    	}
	}

	private synchronized void SetTLRChannel(TTLRChannel pTLRChannel) {
		TLRChannel = pTLRChannel;
	}
	
	private synchronized TTLRChannel GetTLRChannel() {
		return TLRChannel;
	}
	
	@SuppressWarnings("unchecked")
	private void List_Create() throws Exception {
		TTRCEvent[] _List = null;
		TTLRChannel TLRChannel = GetTLRChannel();
		if (TLRChannel != null) {
			TDataType Message = TLRChannel.DataTypes.GetItemByID(com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.TRC.TTRCChannel.DATATYPE_MESSAGE_ID);
			if (Message != null) {
				ArrayList<TTimestampedTypedTaggedDataContainerType> Values = (ArrayList<TTimestampedTypedTaggedDataContainerType>)Message.Extra;
	        	if (Values != null) {
	        		int Cnt = Values.size();
	        		_List = new TTRCEvent[Cnt];
	        		for (int I = 0; I < Cnt; I++) { 
	        			TTimestampedTypedTaggedDataContainerType.TValue V = (TTimestampedTypedTaggedDataContainerType.TValue)Values.get(I).GetValue();
	        			_List[I] = new TTRCEvent(V.Timestamp, V.ValueType, V.ValueTag, (new String(V.Value, "utf-8")));
	        		}
	        	}
			}
		}
		//.
		if (_List != null)
			List = _List;
		else
			List = new TTRCEvent[0];
	}
	
	private void Update() {
    	if (List.length == 0) {
    		lvList.setAdapter(null);
    		return; //. ->
    	}
    	//.
		lvListAdapter = new TListAdapter(this);
		lvList.setAdapter(lvListAdapter);
	}
}
