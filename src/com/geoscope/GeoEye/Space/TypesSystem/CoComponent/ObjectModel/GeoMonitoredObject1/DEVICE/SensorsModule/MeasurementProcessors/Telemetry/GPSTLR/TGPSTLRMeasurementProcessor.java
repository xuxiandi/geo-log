package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.GPSTLR;

import java.util.ArrayList;
import java.util.Hashtable;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType.WrongContainerTypeException;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS.TGPSFixDataType;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjectTrack;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.TMeasurementProcessor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;

public class TGPSTLRMeasurementProcessor extends TMeasurementProcessor {
	
	public static final String TypeID = com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.Telemetry.GPSTLR.Model.TModel.ModelTypeID;

	private RelativeLayout ReflectorLayout; 
	private TReflectorComponent ReflectorComponent;
	//.
	private Hashtable<String, TSensorMeasurement> MeasurementCache = null;
	//.
	private volatile TTLRChannel 		TLRChannel = null;
	private volatile TGPSFixDataType 	TLRChannel_GPSFixDataType = null;
	//.
	private volatile TAsyncProcessing 	MeasurementInitializing = null;
	//.
	private TAsyncProcessing 			MeasurementPositioning = null;

	public TGPSTLRMeasurementProcessor() {
		super();
	}
	
	@Override
	public void Destroy() throws Exception {
		if (ReflectorComponent != null) {
			ReflectorComponent.Destroy();
			ReflectorComponent = null;
		}
		//.
		super.Destroy();
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
		inflater.inflate(R.layout.reflector, ParentLayout);
		ReflectorLayout = (RelativeLayout)ParentLayout.findViewById(R.id.ReflectorLayout);
		//. 
		Intent Parameters = new Intent();
		Parameters.putExtra("Reason", TReflectorComponent.REASON_MONITORGEOLOCATION);
		ReflectorComponent = new TReflectorComponent(ParentActivity, ReflectorLayout, Parameters);
	}

	@SuppressWarnings({ "unchecked" })
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
			
			private byte[] TrackData = null;

			@Override
			public void Process() throws Exception {
				Canceller.Check();
				//.
				if (!flUseCache)
					TheMeasurement.Descriptor.Model.Process(Canceller);
				//.
				TLRChannel = ((TTLRChannel)TheMeasurement.Descriptor.Model.Stream.Channels_GetOneByClass(TTLRChannel.class));
				if ((TLRChannel != null) && (TLRChannel.DataTypes != null))
					TLRChannel_GPSFixDataType = (TGPSFixDataType)TLRChannel.DataTypes.GetItemByClass(TGPSFixDataType.class);
				//.
				if ((TLRChannel_GPSFixDataType != null) && (ReflectorComponent != null)) {
		    		//. create the track 
					ArrayList<TGPSFixDataType.TValue> TrackFixes = new ArrayList<TGPSFixDataType.TValue>(1024);
					ArrayList<TContainerType> Values = (ArrayList<TContainerType>)TLRChannel_GPSFixDataType.Extra;
					if (Values != null) {
						TGPSFixDataType GPSFix = new TGPSFixDataType();
						TGPSFixDataType.TValue LastGPSFixValue = null; 
						int Cnt = Values.size();
						for (int I = 0; I < Cnt; I++) {
							GPSFix.ContainerType = Values.get(I);
							try {
								TGPSFixDataType.TValue GPSFixValue = GPSFix.Value();
								//.
								if (!GPSFixValue.CoordinatesAreEquals(LastGPSFixValue) && GPSFixValue.IsAvailable())
									TrackFixes.add(GPSFixValue);
								//.
								LastGPSFixValue = GPSFixValue;
							} catch (WrongContainerTypeException e) {
							}
						}
						//.
			    		Cnt = TrackFixes.size();
			    		TCoGeoMonitorObjectTrack Track = new TCoGeoMonitorObjectTrack(Color.RED);
			    		Track.NodesCount = Cnt;
			    		Track.Nodes = new double[Track.NodesCount*3];
			    		int Idx = 0;
			    		for (int I = 0; I < Track.NodesCount; I++) {
			    			TGPSFixDataType.TValue TrackFix = TrackFixes.get(I); 
							TXYCoord NodeXY = ReflectorComponent.ConvertGeoCoordinatesToXY(TrackFix.DatumID, TrackFix.Latitude,TrackFix.Longitude,TrackFix.Altitude);
							//.
							Track.Nodes[Idx] = TrackFix.Timestamp; Idx++;
							Track.Nodes[Idx] = NodeXY.X; Idx++;
							Track.Nodes[Idx] = NodeXY.Y; Idx++;
			    		}
			    		if (Track.NodesCount > 0)
			    			TrackData = Track.ToByteArrayV1();
			    		else
			    			TrackData = null;
					}
				}
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				if (MeasurementInitializing == this)
					MeasurementInitializing = null;
				//. 
				if (TrackData != null) {
					ReflectorComponent.ObjectTracks_Clear();
					ReflectorComponent.ObjectTracks_AddTrack(TrackData);
				}
				//. caching 
				if (!flUseCache) {
					if (MeasurementCache == null)
						MeasurementCache = new Hashtable<String, TSensorMeasurement>();
					MeasurementCache.put(TheMeasurement.Descriptor.ID, TheMeasurement);
				}
				//.
				flInitialized = true;
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
		if (ReflectorComponent != null)
			ReflectorComponent.Start();
	}
	
	@Override
	public void Stop() throws Exception {
		if (ReflectorComponent != null)
			ReflectorComponent.Stop();
		//.
		Finalize();
	}
	
	@Override
	public void Pause() {
		if (ReflectorComponent != null)
			ReflectorComponent.Pause();
	}
	
	@Override
	public void Resume() {
		if (ReflectorComponent != null)
			ReflectorComponent.Resume();
	}
	
	@Override
	public void Show() {
		ParentLayout.setVisibility(View.VISIBLE);
		if (ReflectorComponent != null)
			ReflectorComponent.Show();
		//.
		super.Show();
	}
	
	@Override
	public void Hide() {
		super.Hide();
		//.
		if (ReflectorComponent != null)
			ReflectorComponent.Hide();
		ParentLayout.setVisibility(View.GONE);
	}

	@Override
	public void SetPosition(final double Position, final int Delay, final boolean flPaused) throws InterruptedException {
		if (MeasurementPositioning != null) { 
			MeasurementPositioning.Cancel();
			MeasurementPositioning = null;
		}
		//.
		MeasurementPositioning = new TAsyncProcessing() {

			private TXYCoord LocationXY = null;
			
			@Override
			public void Process() throws Exception {
				/* ignore delay if (Delay > 0)
					Thread.sleep(Delay); */
				while (!Canceller.flCancel) {
					if (flInitialized)
						break; //. >
					Thread.sleep(10); 
				}
				//.
				TGPSFixDataType.TValue NearFix = TLRChannel_GPSFixes_GetCalculatedValue(Measurement.Descriptor.StartTimestamp+Position); 
				if ((ReflectorComponent != null) && (NearFix != null)) 
					LocationXY = ReflectorComponent.ConvertGeoCoordinatesToXY(NearFix.DatumID, NearFix.Latitude,NearFix.Longitude,NearFix.Altitude);
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel) 
					return; //. ->
				if ((ReflectorComponent != null) && (LocationXY != null)) 
						ReflectorComponent.MoveReflectionWindow(LocationXY);
			}
			
			@Override
			public void DoOnFinished() throws Exception {
				if (MeasurementPositioning == this)
					MeasurementPositioning = null;
			}
		};
		MeasurementPositioning.Start();
	}
	
	@SuppressWarnings("unchecked")
	private TGPSFixDataType.TValue TLRChannel_GPSFixes_GetCalculatedValue(double Timestamp) throws WrongContainerTypeException {
		TGPSFixDataType.TValue LR = null;
		TGPSFixDataType.TValue RR = null;
		if (TLRChannel_GPSFixDataType != null) {
			ArrayList<TContainerType> Values = (ArrayList<TContainerType>)TLRChannel_GPSFixDataType.Extra;
			if (Values != null) {
				TGPSFixDataType GPSFix = new TGPSFixDataType(); 
				double LeftMinDistance = -Double.MAX_VALUE;
				double RightMinDistance = Double.MAX_VALUE;
				int Cnt = Values.size();
				for (int I = 0; I < Cnt; I++) {
					GPSFix.ContainerType = Values.get(I);
					//.
					TGPSFixDataType.TValue V = GPSFix.Value();
					if (V.IsAvailable()) {
						double Distance = (V.Timestamp-Timestamp); 
						if ((Distance < 0) && (Distance > LeftMinDistance)) {
							LeftMinDistance = Distance;
							LR = V;
						}
						if ((Distance >= 0) && (Distance < RightMinDistance)) {
							RightMinDistance = Distance;
							RR = V;
						}
					}
				}
				//.
				Cnt = 0;
				if (LR != null)
					Cnt++;
				if (RR != null)
					Cnt++;
				if (Cnt == 0)
					return null; //. ->
				else
					if (Cnt == 1) {
						if (LR != null) 
							return LR; //. ->
						else
							return RR; //. ->
					}
					else {
						double dT = RR.Timestamp-LR.Timestamp;
						if (dT == 0.0)
							return LR; //. ->
						double dTS = Timestamp-LR.Timestamp;
						double F = dTS/dT; 
						TGPSFixDataType.TValue Result = new TGPSFixDataType.TValue(
								LR.DatumID,
								Timestamp,
								LR.Latitude+(RR.Latitude-LR.Latitude)*F, 
								LR.Longitude+(RR.Longitude-LR.Longitude)*F, 
								LR.Altitude+(RR.Altitude-LR.Altitude)*F, 
								LR.Speed+(RR.Speed-LR.Speed)*F, 
								LR.Bearing+(RR.Bearing-LR.Bearing)*F, 
								LR.Precision+(RR.Precision-LR.Precision)*F 
						);
						return Result; //. ->
					}
			}
			else
				return null; //. ->
		}
		else
			return null; //. ->
	}
	
	@SuppressWarnings({ "unchecked", "unused" })
	private TGPSFixDataType.TValue TLRChannel_GPSFixes_GetNearestToTime(double Time) {
		TGPSFixDataType.TValue Result = null;
		if (TLRChannel_GPSFixDataType != null) {
			ArrayList<TContainerType> Values = (ArrayList<TContainerType>)TLRChannel_GPSFixDataType.Extra;
			if (Values != null) {
				TGPSFixDataType GPSFix = new TGPSFixDataType(); 
				double MinTime = Double.MAX_VALUE;
				int Cnt = Values.size();
				for (int I = 0; I < Cnt; I++) {
					GPSFix.ContainerType = Values.get(I);
					try {
						double dT = Math.abs(GPSFix.Timestamp()-Time);
						if (dT < MinTime) {
							MinTime = dT;
							Result = GPSFix.Value();
						}
					} catch (WrongContainerTypeException e) {
					}
				}
			}
		}
		return Result; //.
	}
}
