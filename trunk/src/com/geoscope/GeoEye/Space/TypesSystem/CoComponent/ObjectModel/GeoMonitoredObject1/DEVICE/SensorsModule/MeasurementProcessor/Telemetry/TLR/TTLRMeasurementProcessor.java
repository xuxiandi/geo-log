package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.Telemetry.TLR;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TDoubleContainerType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.TMeasurementProcessor;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;

@SuppressLint("HandlerLeak")
public class TTLRMeasurementProcessor extends TMeasurementProcessor implements SurfaceHolder.Callback, OnTouchListener  {

	public static final int BackgroundColor = 0xFFC5C5C5;
	public static final int TimeIntervalColor = Color.WHITE;
	public static final int CenterMarkerColor = Color.MAGENTA;
	public static final int CenterMarkerColorHigh = Color.RED;
	
	public static class TOnTimeChangeHandler {
		
		public void DoOnTimeChanging(double Time, boolean flChanging, boolean flDelayAllowed) {
		}

		public void DoOnTimeChanged(double Time) {
			DoOnTimeChanging(Time, false, false);
		}
	}
	
	
    public class TSurfaceUpdating implements Runnable {
    	
    	private Thread _Thread;
    	private boolean flCancel = false;
    	public boolean flProcessing = false;
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	//.
	    private Paint paint = new Paint();
		private Path path = new Path();
        
    	public TSurfaceUpdating() {
    		_Thread = new Thread(this);
    		Start();
    	}
    	
    	public void Destroy() {
    		CancelAndWait();
    	}
    	
		public void Start() {
    		_Thread.start();
		}
		
		@Override
		public void run() {
			try {
				flProcessing = true;
				try {
						while (!flCancel) {
							ProcessSignal.WaitOne();
							if (flCancel)
								return; //. ->
							//.
							Canvas canvas = surface.lockCanvas();
							try {
								Draw(canvas);
							} 
							finally {
								surface.unlockCanvasAndPost(canvas);
							}
						}
				}
				finally {
					flProcessing = false;
				}
			}
        	catch (InterruptedException E) {
        	}
        	catch (CancelException CE) {
        	}
			catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
			}
		}
		
		public void StartUpdate() {
			ProcessSignal.Set();
		}
		
    	public void Join() {
    		try {
    			if (_Thread != null)
    				_Thread.join();
    		}
    		catch (Exception E) {}
    	}

		public void Cancel() {
			flCancel = true;
			//.
			ProcessSignal.Set();
    		//.
    		if (_Thread != null)
    			_Thread.interrupt();
		}
		
		public void CancelAndWait() {
    		Cancel();
    		Join();
		}
		
        private void Draw(Canvas canvas) throws CancelException {
			DoOnDraw(canvas, null/* DrawCanceller */);
        }
        
		protected void DoOnDraw(Canvas canvas, TCanceller Canceller) throws CancelException {
			//. draw background
        	canvas.drawBitmap(Graph_BkgBitmap, 0,0, paint);
			//.
			if (!flInitialized) {
				if (MeasurementInitializing != null) {
	    			paint.setColor(Color.RED);
	    			paint.setAlpha(255);
                    paint.setStyle(Paint.Style.FILL);
	    			String S = ParentActivity.getString(R.string.SLoading); 
	    			paint.setTextSize(14*DisplayMetrics.density);
	    			canvas.drawText(S, 3.0F*DisplayMetrics.density,0.0F+paint.getTextSize(), paint);
				}
				return; //. ->
			}
			//.
			double Mid = (Width/2.0);
			double _CurrentTime = OleDate.UTCToLocalTime(CurrentTime);
			double IntervalBegin = _CurrentTime-(Mid*TimeResolution);
			double IntervalEnd = _CurrentTime+(Mid*TimeResolution);
			//. draw TimeInterval
			double TIB,TIE;
			double _TimeIntervalBegin = OleDate.UTCToLocalTime(Measurement.Descriptor.StartTimestamp);
			if (_TimeIntervalBegin >= IntervalBegin) 
				TIB = _TimeIntervalBegin;
			else
				TIB = IntervalBegin;
			double _TimeIntervalEnd = OleDate.UTCToLocalTime(Measurement.Descriptor.FinishTimestamp);
			if (_TimeIntervalEnd <= IntervalEnd) 
				TIE = _TimeIntervalEnd;
			else 
				TIE = IntervalEnd;
			paint.setColor(TimeIntervalColor);
			paint.setAlpha(64);
            paint.setStyle(Paint.Style.FILL);
			canvas.drawRect((float)(Mid+(TIB-_CurrentTime)/TimeResolution),0.0F,(float)(Mid+(TIE-_CurrentTime)/TimeResolution),Height, paint);
			//. draw center marker
            paint.setStyle(Paint.Style.STROKE);
			paint.setAlpha(255);
			paint.setStrokeWidth(1.0F*DisplayMetrics.density);
			paint.setColor(CenterMarkerColorHigh);
			canvas.drawLine((float)Mid,0.0F, (float)Mid,Height, paint);
			if (flStandalone) {
    			paint.setColor(CenterMarkerColorHigh);
                paint.setStyle(Paint.Style.FILL);
    			String S = OleDate.Format("yyyy/MM/dd HH:mm:ss",_CurrentTime); 
    			paint.setTextSize(14*DisplayMetrics.density);
    			canvas.drawText(S, (float)Mid+3.0F*DisplayMetrics.density,0.0F+paint.getTextSize(), paint);
			}
			//.
    		synchronized (ChannelSamples_Lock) {
    			//. draw graphs
                paint.setAntiAlias(true);
                paint.setTextSize(12*DisplayMetrics.density);
                paint.setColor(Color.RED);
				paint.setAlpha(192);
                int _EnabledChannelIndex = 0;
            	for (int ChannelIndex = 0; ChannelIndex < ChannelSamples_ChannelsCount; ChannelIndex++)
            		if (ChannelSamples_Enabled[ChannelIndex]) {
                		float Y0 = Graph_OffsetY+Graph_ShiftY+(Graph_ChannelHeight*_EnabledChannelIndex)+Graph_ChannelHeight;
                		//.
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setStrokeWidth(0.5F*DisplayMetrics.density);
                        canvas.drawLine(0,Y0, Width,Y0, paint);
                		//.
                        paint.setStyle(Paint.Style.FILL);
                        canvas.drawText(ChannelSamples_ChannelNames[ChannelIndex],4.0F,Y0-2.0F, paint);
                    	//.
                    	_EnabledChannelIndex++;
            		}
            	//.
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2.0F*DisplayMetrics.density);
				paint.setAlpha(255);
        		double X0 = Mid+(Measurement.Descriptor.StartTimestamp-CurrentTime)/TimeResolution;
                int EnabledChannelIndex = 0;
            	for (int ChannelIndex = 0; ChannelIndex < ChannelSamples_ChannelsCount; ChannelIndex++)
            		if (ChannelSamples_Enabled[ChannelIndex]) {
                		double Y0 = Graph_OffsetY+Graph_ShiftY+(Graph_ChannelHeight*EnabledChannelIndex)+Graph_ChannelHeight;
                		int Graph_Polyline_Count = 0;
                		int Cnt = ChannelSamples[ChannelIndex].length;
                		if (Cnt > 0) {
                			double X = X0;
                			double StepX = ((Measurement.Descriptor.FinishTimestamp-Measurement.Descriptor.StartTimestamp)/Cnt)/TimeResolution; 
                    		for (int I = 0; I < Cnt; I++) {
                    			double Y = (Y0-ChannelSamples[ChannelIndex][I]*Graph_ChannelHeight);
                    			//.
                    			Graph_Polyline_Xs[Graph_Polyline_Count] = (float)X;
    	            			Graph_Polyline_Ys[Graph_Polyline_Count] = (float)Y;
        	        			Graph_Polyline_Count++;
        	        			//.
                    			X += StepX;
                    		}
                    		//.
                    		if (Graph_Polyline_Count > 1)
                    			Graph_DrawPolyline(canvas, paint, path, Graph_ChannelsColors[ChannelIndex], Graph_Polyline_Xs,Graph_Polyline_Ys, Graph_Polyline_Count);
                		}
                		//.
                		EnabledChannelIndex++;
            		}
            	//.
        		if (true) {
                    paint.setStyle(Paint.Style.FILL);
    				float R = 2.0F*DisplayMetrics.density;
                    EnabledChannelIndex = 0;
                	for (int ChannelIndex = 0; ChannelIndex < ChannelSamples_ChannelsCount; ChannelIndex++)
                		if (ChannelSamples_Enabled[ChannelIndex]) {
                    		int SamplesCount = ChannelSamples[ChannelIndex].length;
                    		double IndexValue = SamplesCount*((CurrentTime-Measurement.Descriptor.StartTimestamp)/(Measurement.Descriptor.FinishTimestamp-Measurement.Descriptor.StartTimestamp));
                    		int Index = (int)IndexValue;
                    		if (Index >= 0) {
                        		if (Index < SamplesCount) {
                        			double Value;
                        			int Index1 = Index+1;
                        			if (Index1 < SamplesCount) {
                        				double Y0 = ChannelSamples[ChannelIndex][Index];
                        				double Y1 = ChannelSamples[ChannelIndex][Index1];
                        				Value = Y0+(IndexValue-Index)*(Y1-Y0);
                        			}
                        			else 
                        				Value = ChannelSamples[ChannelIndex][Index];
                        			double RealValue = Value*ChannelSamples_Ranges[ChannelIndex]+ChannelSamples_MinValues[ChannelIndex];
                        			//.
                            		double Y0 = Graph_OffsetY+Graph_ShiftY+(Graph_ChannelHeight*EnabledChannelIndex)+Graph_ChannelHeight;
                        			double Y = (Y0-Value*Graph_ChannelHeight);
                        			//.
                    				paint.setColor(Graph_ChannelsColors[ChannelIndex]);
                    				paint.setAlpha(255);
                    				canvas.drawCircle((float)Mid,(float)Y, R, paint);
                    				//.
                    				paint.setAlpha(255);
                        			String S = String.format(Locale.getDefault(),"%.2f",RealValue)+" "+ChannelSamples_Units[ChannelIndex]; 
                        			paint.setTextSize(12*DisplayMetrics.density);
                        			canvas.drawText(S, (float)Mid+3.0F*DisplayMetrics.density,(float)(Y+paint.getTextSize()/2.0), paint);
                        		}
                    		}
                    		//.
                    		EnabledChannelIndex++;
                		}
        		}
    		}
		}
    }
    
	private Hashtable<String, TSensorMeasurement> MeasurementCache = null;
	//.
    private TOnTimeChangeHandler OnTimeChangeHandler = null;
	//.
	private SurfaceView 	svProcessor;
	@SuppressWarnings("unused")
	private TextView 		lbProcessor;
	//.
	private DisplayMetrics 	DisplayMetrics;
	//.
	private volatile TAsyncProcessing 	MeasurementInitializing = null;
	//.
	private TAsyncProcessing 			MeasurementPositioning = null;
	//.
	private TSurfaceUpdating SurfaceUpdating = null;
	//.
	private TTLRChannel TLRChannel = null;
	//.
    private volatile double CurrentTime;
    private volatile double TimeResolution;
    //.
    private boolean Pointer0_flMoving;
    private boolean Pointer0_flTimeSelecting;
    private double 	Pointer0_LastX;
    @SuppressWarnings("unused")
	private double 	Pointer0_LastY;
    @SuppressWarnings("unused")
    private double 	Pointer0_LastDownTime;
    private boolean Pointer1_flMoving;
    private double 	Pointer1_LastX;
    @SuppressWarnings("unused")
	private double 	Pointer1_LastY;
    @SuppressWarnings("unused")
    private double 	Pointer1_LastDownTime;
	//.
    private Object 		ChannelSamples_Lock = new Object();
    private int	 		ChannelSamples_ChannelsCount;
    private double[][] 	ChannelSamples;
    private double[]	ChannelSamples_MinValues;
    private double[]	ChannelSamples_Ranges;
    private boolean[]	ChannelSamples_Enabled;
    private String[]	ChannelSamples_ChannelNames;
    private String[]	ChannelSamples_Units;
    private int 		ChannelSamples_MaxSize;
	//.
	private int[]			Graph_ChannelsColors;
	private int           	Graph_OffsetY = 0;
	private int           	Graph_ShiftY = 0;
	private float 			Graph_ChannelHeight = 0;
	@SuppressWarnings("unused")
	private float			Graph_ChannelHeightHalf = 0;
    public Bitmap           Graph_BkgBitmap = null;
	private float[]	        Graph_Polyline_Xs = null;
	private float[]	        Graph_Polyline_Ys = null;
	private int[]           Graph_Polyline_Count = null;
	
	public TTLRMeasurementProcessor() {
		super();
		//.
		OnTimeChangeHandler = new TOnTimeChangeHandler() {
			
			@Override
			public void DoOnTimeChanging(double Time, boolean flChanging, boolean flDelayAllowed) {
				if (!flExists)
					return; //. ->
				double ProgressFactor = (Time-Measurement.Descriptor.StartTimestamp)/(Measurement.Descriptor.FinishTimestamp-Measurement.Descriptor.StartTimestamp);
				if ((0.0 <= ProgressFactor) && (ProgressFactor <= 1.0)) {
					if (OnProgressHandler != null) 
						OnProgressHandler.DoOnProgress(ProgressFactor);
				}
			}
		};
	}
	
	@Override
	public void Destroy() throws Exception {
		super.Destroy();
	}
	
	@Override
	public void SetLayout(Activity pParentActivity, LinearLayout pParentLayout) throws Exception {
		super.SetLayout(pParentActivity, pParentLayout);
		//.
		LayoutInflater inflater = (LayoutInflater)ParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.measurement_processor_telemetrytlr_panel, ParentLayout);
        //.
        svProcessor = (SurfaceView)ParentLayout.findViewById(R.id.svProcessor);
        svProcessor.getHolder().addCallback(this);
        svProcessor.setOnTouchListener(this);
        //.
        lbProcessor = (TextView)ParentLayout.findViewById(R.id.lbProcessor);
        //.
        DisplayMetrics = ParentActivity.getResources().getDisplayMetrics();
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
				ChannelSamples_Create();
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				if (MeasurementInitializing == this)
					MeasurementInitializing = null;
				//.
				Graph_Initialize();
				//.
				CurrentTime = Measurement.Descriptor.StartTimestamp;
				//. caching 
				if (!flUseCache) {
					if (MeasurementCache == null)
						MeasurementCache = new Hashtable<String, TSensorMeasurement>();
					MeasurementCache.put(TheMeasurement.Descriptor.ID, TheMeasurement);
				}
				//.
				flInitialized = true;
				//. 
				Draw();
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
		//. 
		Draw();
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
		Graph_Finalize();
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
		svProcessor.setVisibility(View.VISIBLE);
		//.
		super.Show();
	}
	
	@Override
	public void Hide() {
		super.Hide();
		//.
		svProcessor.setVisibility(View.GONE);
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
		SetCurrentTime(Measurement.Descriptor.StartTimestamp+Position, false, true, false);
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder arg0) {
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder arg0) {
		if (SurfaceUpdating != null) {
			SurfaceUpdating.Destroy();
			SurfaceUpdating = null;
		}
		//.
		Graph_Background_Finalize();
		//.
		surface = null;
	}
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		surface = arg0;
		Width = arg2;
		Height = arg3;
		//.
		if (Measurement != null)
			TimeResolution = (Measurement.Descriptor.FinishTimestamp-Measurement.Descriptor.StartTimestamp)/Width;
		else
			TimeResolution = 1.0;
		//.
		Graph_Background_Initialize();
		//.
		SurfaceUpdating = new TSurfaceUpdating();
		//.
		Draw();
		//.
		if (OnVideoSurfaceChangedHandler != null) 
			OnVideoSurfaceChangedHandler.DoOnSurfaceChanged(surface);
	}
	
	@Override
	public boolean onTouch(View pView, MotionEvent pEvent) {
		try {
			switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {

			case MotionEvent.ACTION_DOWN:
				Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
				break; // . >

			case MotionEvent.ACTION_POINTER_DOWN:
				switch (pEvent.getPointerCount()) {

				case 1:
					Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
					break; // . >

				case 2:
					Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
					Pointer1_Down(pEvent.getX(1), pEvent.getY(1));
					break; // . >

				case 3:
					Pointer0_Down(pEvent.getX(0), pEvent.getY(0));
					Pointer1_Down(pEvent.getX(1), pEvent.getY(1));
					Pointer2_Down(pEvent.getX(2), pEvent.getY(2));
					break; // . >
				}
				break; // . >

			case MotionEvent.ACTION_UP:
				Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
				break; // . >

			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
				switch (pEvent.getPointerCount()) {

				case 1:
					Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
					break; // . >

				case 2:
					Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
					Pointer1_Up(pEvent.getX(1), pEvent.getY(1));
					break; // . >

				case 3:
					Pointer0_Up(pEvent.getX(0), pEvent.getY(0));
					Pointer1_Up(pEvent.getX(1), pEvent.getY(1));
					Pointer2_Up(pEvent.getX(2), pEvent.getY(2));
					break; // . >
				}
				break; // . >

			case MotionEvent.ACTION_MOVE:
				switch (pEvent.getPointerCount()) {

				case 1:
					Pointer0_Move(pEvent.getX(0),
							pEvent.getY(0));
					break; // . >

				case 2:
					Pointer0_Move(pEvent.getX(0),
							pEvent.getY(0));
					Pointer1_Move(pEvent.getX(1),
							pEvent.getY(1));
					break; // . >

				case 3:
					Pointer0_Move(pEvent.getX(0),
							pEvent.getY(0));
					Pointer1_Move(pEvent.getX(1),
							pEvent.getY(1));
					Pointer2_Move(pEvent.getX(2),
							pEvent.getY(2));
					break; // . >
				}
				break; // . >

			default:
				return false; // . ->
			}
			return true; // . ->
		} catch (Throwable E) {
			TGeoLogApplication.Log_WriteError(E);
			// .
			return false; // . ->
		}
	}

	@SuppressWarnings("unused")
	protected void Pointer0_Down(double X, double Y) {
		try {
			Pointer0_flMoving = true;
			if (false) /*(Button = Left)*/ {
				double _CurrentTime = CurrentTime+((X-(Width/2.0))*TimeResolution);
			  	if (!((Measurement.Descriptor.StartTimestamp <= _CurrentTime) && (_CurrentTime <= Measurement.Descriptor.FinishTimestamp))) 
			  		return; //. ->
			  	//.
			  	Draw();
			}
			else
				Pointer0_flTimeSelecting = true;
		}
		finally {
			Pointer0_LastX = X;
			Pointer0_LastY = Y;
			Pointer0_LastDownTime = OleDate.UTCCurrentTimestamp();
		}
	}
	
	protected void Pointer0_Up(double X, double Y) {
		if (Pointer0_flMoving && !Pointer1_flMoving && Pointer0_flTimeSelecting) {
			Move(-(X-Pointer0_LastX));
			//.
			if (OnTimeChangeHandler != null) 
				OnTimeChangeHandler.DoOnTimeChanged(CurrentTime);
		}
		//.
		Pointer0_flMoving = false;
		//.
		Pointer0_flTimeSelecting = false;
	}
	
	protected void Pointer0_Move(double X, double Y) {
		if (Pointer0_flMoving) {
			if (!Pointer1_flMoving) {
				if (Pointer0_flTimeSelecting) {
					Move(-(X-Pointer0_LastX));
					//.
					if (OnTimeChangeHandler != null) 
						OnTimeChangeHandler.DoOnTimeChanging(CurrentTime, true, true);
				}
			}
			else {
				double dX0 = Pointer0_LastX-Pointer1_LastX; 
				double dX1 = X-Pointer1_LastX;
				if ((dX0 != 0) && (dX1 != 0)) {
					double Scale = Math.abs(dX0/dX1);
					ScaleTimeResolution(Scale);
				}
			} 
			//.
			Pointer0_LastX = X;
			Pointer0_LastY = Y;
		}
	}
	
	protected void Pointer1_Down(double X, double Y) {
		Pointer1_flMoving = true;
		//.
		Pointer1_LastX = X;
		Pointer1_LastY = Y;
		Pointer1_LastDownTime = OleDate.UTCCurrentTimestamp();
	}
	
	protected void Pointer1_Up(double X, double Y) {
		Pointer1_flMoving = false;
	}
	
	protected void Pointer1_Move(double X, double Y) {
		if (Pointer1_flMoving) {
			if (Pointer0_flMoving) {
				double dX0 = Pointer1_LastX-Pointer0_LastX; 
				double dX1 = X-Pointer0_LastX;
				if ((dX0 != 0) && (dX1 != 0)) {
					double Scale = Math.abs(dX0/dX1);
					ScaleTimeResolution(Scale);
				}
			} 
			//.
			Pointer1_LastX = X;
			Pointer1_LastY = Y;
		}
	}
	
	protected void Pointer2_Down(double X, double Y) {
	}
	
	protected void Pointer2_Up(double X, double Y) {
	}

	protected void Pointer2_Move(double X, double Y) {
	}

	public void Move(double dX) {
		double _CurrentTime = CurrentTime+(dX*TimeResolution);
		if (_CurrentTime < Measurement.Descriptor.StartTimestamp) 
			_CurrentTime = Measurement.Descriptor.StartTimestamp; 
		if (_CurrentTime > Measurement.Descriptor.FinishTimestamp) 
			_CurrentTime = Measurement.Descriptor.FinishTimestamp;
		if (_CurrentTime == CurrentTime) 
			return; //. ->
		//.
		CurrentTime = _CurrentTime;
		//.
		Draw();
	}
	
	private boolean IsTimeUserChanging() {
		return Pointer0_flMoving;
	}

	public void ScaleTimeResolution(double pScale) {
		TimeResolution = TimeResolution*pScale;
		//.
		Draw();
	}
	
	public void SetCurrentTime(double pCurrentTime, boolean flChanging, boolean flFireEvent, boolean flEventActionDelayAllowed) {
		if (IsTimeUserChanging())
			return; //. ->
		//.
		CurrentTime = pCurrentTime;
		//.
		Draw();
		//.
		if (flFireEvent && (OnTimeChangeHandler != null)) 
			OnTimeChangeHandler.DoOnTimeChanging(CurrentTime, flChanging, flEventActionDelayAllowed);
	}

	private synchronized void SetTLRChannel(TTLRChannel pTLRChannel) {
		TLRChannel = pTLRChannel;
	}
	
	private synchronized TTLRChannel GetTLRChannel() {
		return TLRChannel;
	}
	
	public void Draw() {
		StartDraw();
	}

	public void StartDraw() {
		if (SurfaceUpdating != null)
			SurfaceUpdating.StartUpdate();
	}

	private void ChannelSamples_Create() throws Exception {
		synchronized (ChannelSamples_Lock) {
	    	TTLRChannel TLRChannel = GetTLRChannel();
	    	if (TLRChannel == null) {
	    		ChannelSamples_ChannelsCount = 0;
	    		return; //. ->
	    	}
			ChannelSamples_ChannelsCount = TLRChannel.DataTypes.Items.size();
			ChannelSamples_Enabled = new boolean[ChannelSamples_ChannelsCount];
			ChannelSamples_ChannelNames = new String[ChannelSamples_ChannelsCount];
			for (int I = 0; I < ChannelSamples_ChannelsCount; I ++) {
				TDataType DT = TLRChannel.DataTypes.Items.get(I);
				//.
				ChannelSamples_ChannelNames[I] = DT.TypeID;
				ChannelSamples_Enabled[I] = (DT.ContainerType instanceof TDoubleContainerType);
			};
	        ChannelSamples = new double[ChannelSamples_ChannelsCount][];
	        ChannelSamples_MinValues = new double[ChannelSamples_ChannelsCount];
	        ChannelSamples_Ranges = new double[ChannelSamples_ChannelsCount];
	        ChannelSamples_Units = new String[ChannelSamples_ChannelsCount];
	        ChannelSamples_MaxSize = 0;
	        for (int I = 0; I < ChannelSamples_ChannelsCount; I++) {
				TDataType DT = TLRChannel.DataTypes.Items.get(I);
				//.
	        	ChannelSamples_Units[I] = DT.ValueUnit;				
				//.
				if (DT.ContainerType instanceof TDoubleContainerType) {
					@SuppressWarnings("unchecked")
					ArrayList<TContainerType> Values = (ArrayList<TContainerType>)DT.Extra;
		        	if (Values != null) {
			        	int Size = Values.size();
			        	ChannelSamples[I] = new double[Size];
			        	double MinValue = Double.MAX_VALUE;
			        	double MaxValue = Double.MIN_VALUE;
			        	for (int J = 0; J < Size; J++) {
			        		short V = ((Double)Values.get(J).GetValue()).shortValue();
			        		ChannelSamples[I][J] = V;
			        		if (V < MinValue)
			        			MinValue = V;
			        		if (V > MaxValue)
			        			MaxValue = V;
			        	}
			        	double Range = (MaxValue-MinValue);
			        	//.
			        	ChannelSamples_MinValues[I] = MinValue;
			        	ChannelSamples_Ranges[I] = Range;
			        	//.
			        	if (Range > 0)
			            	for (int J = 0; J < Size; J++) {
			            		double V = ChannelSamples[I][J];
			            		V = ((V-MinValue)/Range);
			            		ChannelSamples[I][J] = V;
			            	}
			        	else
			            	for (int J = 0; J < Size; J++) {
			            		double V = ChannelSamples[I][J];
			            		V = (V-MinValue);
			            		ChannelSamples[I][J] = V;
			            	}
			        	//.
			        	if (Size > ChannelSamples_MaxSize)
			        		ChannelSamples_MaxSize = Size;
		        	}
		        	else 
		            	ChannelSamples[I] = new double[0];
				}
				else 
	            	ChannelSamples[I] = new double[0];
	        }
	        if (ChannelSamples_MaxSize == 0)
	        	throw new Exception("there is no data to process"); //. =>
		}
	}
	
	private void Graph_Background_Initialize() {
		Graph_Background_Finalize();
		//.
        Graph_BkgBitmap = Bitmap.createBitmap(Width,Height, Bitmap.Config.RGB_565);
    	Canvas canvas = new Canvas();	
    	canvas.setBitmap(Graph_BkgBitmap);
    	Paint paint = new Paint();
    	paint.setColor(Color.rgb(0,0,70));
    	canvas.drawRect(0,0, Graph_BkgBitmap.getWidth(),Graph_BkgBitmap.getHeight(), paint);
    	paint.setColor(Color.rgb(0,0,150));
        paint.setStrokeWidth(1.0F*DisplayMetrics.density);
    	float MeshStep = 16*DisplayMetrics.density;
    	float X = 0;
    	float Y = Graph_OffsetY;
    	float W = Graph_BkgBitmap.getWidth();
    	float H = Graph_BkgBitmap.getHeight()-Graph_OffsetY;
    	int CntX = (int)(W/MeshStep)+1; 
    	int CntY = (int)(H/MeshStep)+1;
    	for (int I = 0; I < CntY; I++) {
    		canvas.drawLine(0,Y, W,Y, paint);
    		Y += MeshStep;
    	}
    	for (int I = 0; I < CntX; I++) {
    		canvas.drawLine(X,Graph_OffsetY, X,Graph_OffsetY+H, paint);
    		X += MeshStep;
    	}
	}
	
	private void Graph_Background_Finalize() {
		if (Graph_BkgBitmap != null) {
			Graph_BkgBitmap.recycle();
			Graph_BkgBitmap = null;
		}
	}
	
	private void Graph_Initialize() {
        synchronized (ChannelSamples_Lock) {
            Graph_OffsetY = 0;
            //.
            if (ChannelSamples_ChannelsCount > 0)
            	Graph_ChannelHeight = Height/ChannelSamples_ChannelsCount;
            //.
            int EnabledChannelsCount = 0;
            for (int I = 0; I < ChannelSamples_ChannelsCount; I++)
            	if (ChannelSamples_Enabled[I])
            		EnabledChannelsCount++;
            if (EnabledChannelsCount != 0)
                Graph_ChannelHeight = Height/EnabledChannelsCount;
            else
            	Graph_ChannelHeight = 0;
            Graph_ChannelHeightHalf = Graph_ChannelHeight/2;  
        	//.
            Graph_Polyline_Xs = new float[ChannelSamples_MaxSize];
            Graph_Polyline_Ys = new float[ChannelSamples_MaxSize];
            Graph_Polyline_Count = new int[ChannelSamples_ChannelsCount];
        	for (int I = 0; I < ChannelSamples_ChannelsCount; I++)
        		Graph_Polyline_Count[I] = 0;
            //.
            Graph_ChannelsColors = new int[ChannelSamples_ChannelsCount];
            for (int I = 0; I < ChannelSamples_ChannelsCount; I++)
            	Graph_ChannelsColors[I] = Color.BLUE;
            int CI = 0;
            if (ChannelSamples_ChannelsCount > CI)
            	Graph_ChannelsColors[CI] = Color.WHITE;
            CI++;
            if (ChannelSamples_ChannelsCount > CI)
            	Graph_ChannelsColors[CI] = Color.YELLOW;
            CI++;
            if (ChannelSamples_ChannelsCount > CI)
            	Graph_ChannelsColors[CI] = Color.MAGENTA;
            CI++;
            if (ChannelSamples_ChannelsCount > CI)
            	Graph_ChannelsColors[CI] = Color.CYAN;
            CI++;
            if (ChannelSamples_ChannelsCount > CI)
            	Graph_ChannelsColors[CI] = Color.GREEN;
            CI++;
            if (ChannelSamples_ChannelsCount > CI)
            	Graph_ChannelsColors[CI] = Color.LTGRAY;
            CI++;
            if (ChannelSamples_ChannelsCount > CI)
            	Graph_ChannelsColors[CI] = Color.RED;
		}
    }

	private void Graph_Finalize() {
	}
	
    public void Graph_DrawPolyline(Canvas G, Paint paint, Path path, int PenColor, float[] Xs,float[] Ys, int Count) {
        paint.setColor(PenColor);
        paint.setStrokeWidth(2.5F);
        path.rewind();
        path.moveTo(Xs[0],Ys[0]);
        for (int I = 1; I < Count; I++) 
        	path.lineTo(Xs[I],Ys[I]);
        G.drawPath(path,paint);
    }

	private static final int MESSAGE_EXCEPTION = -1;

	private Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
					if (!flExists)
		            	break; //. >
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TTLRMeasurementProcessor.this.ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
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
