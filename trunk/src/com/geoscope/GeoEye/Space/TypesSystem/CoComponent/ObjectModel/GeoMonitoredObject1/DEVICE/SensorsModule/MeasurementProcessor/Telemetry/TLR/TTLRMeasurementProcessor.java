package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.Telemetry.TLR;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geoscope.Classes.Data.Stream.Channel.TContainerType;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TDoubleContainerType;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurement.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.TMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;

public class TTLRMeasurementProcessor extends TMeasurementProcessor implements SurfaceHolder.Callback, OnTouchListener  {

	public static final int BackgroundColor = 0xFFC5C5C5;
	public static final int TimeIntervalColor = Color.WHITE;
	public static final int CenterMarkerColor = Color.MAGENTA;
	public static final int CenterMarkerColorHigh = Color.RED;
	
	public static class TOnSurfaceChangedHandler {
		
		public void DoOnSurfaceChanged(SurfaceHolder surface) {
		}
	}
	
	
    public class TSurfaceUpdating implements Runnable {
    	
    	private Thread _Thread;
    	private boolean flCancel = false;
    	public boolean flProcessing = false;
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	//.
	    private Paint paint = new Paint();
        
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
			catch (Throwable E) {
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
		
        private void Draw(Canvas canvas) {
			DoOnDraw(canvas, null/* DrawCanceller */, null/* DrawTimeLimit */);
        }
        
		protected void DoOnDraw(Canvas canvas, TCanceller Canceller, TTimeLimit TimeLimit) {
			//. draw background
			paint.setStrokeWidth(0);
			paint.setStyle(Paint.Style.FILL);
			paint.setColor(BackgroundColor);
			canvas.drawRect(0,0,Width,Height, paint);
			//.
			if (!flSetup)
				return; //. ->
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
			canvas.drawRect((float)(Mid+(TIB-_CurrentTime)/TimeResolution),0.0F,(float)(Mid+(TIE-_CurrentTime)/TimeResolution),Height, paint);
			//. draw center marker
			paint.setStrokeWidth(3.0F*Graph_DisplayMetrics.density);
			paint.setColor(CenterMarkerColor);
			canvas.drawLine((float)Mid,0.0F, (float)Mid,Height, paint);
			paint.setStrokeWidth(1.0F*Graph_DisplayMetrics.density);
			paint.setColor(CenterMarkerColorHigh);
			canvas.drawLine((float)Mid,0.0F, (float)Mid,Height, paint);
			paint.setColor(CenterMarkerColorHigh);
			String S = OleDate.Format("yyyy/MM/dd HH:mm:ss",_CurrentTime); 
			paint.setTextSize(14*Graph_DisplayMetrics.density);
			canvas.drawText(S, (float)Mid+3.0F*Graph_DisplayMetrics.density,0.0F+paint.getTextSize(), paint);
		}
    }
    
	private SurfaceHolder 	surface = null;
	private int				Width;
	private int				Height;
	//.
	private SurfaceView 	svProcessor;
	@SuppressWarnings("unused")
	private TextView 		lbProcessor;
	//.
	private TSurfaceUpdating SurfaceUpdating = null;
	//.
	private TTLRChannel TLRChannel = null;
	//.
    private double 			CurrentTime;
    private double 			TimeResolution;
    //.
    private double 	Pointer0_DownX;
    private double 	Pointer0_DownY;
    private double 	Pointer0_Down_ChannelSamples_Position;
    private float 	Pointer0_Down_ChannelSamples_ShiftY;
    private boolean	Pointer0_Down_ChannelSamples_flMoving = false;
    //////////////
    private boolean Pointer0_flMoving;
    private boolean Pointer0_flTimeSelecting;
    private double 	Pointer0_LastX;
	private double 	Pointer0_LastY;
    private double 	Pointer0_LastDownTime;
    private boolean Pointer1_flMoving;
    private double 	Pointer1_LastX;
    @SuppressWarnings("unused")
	private double 	Pointer1_LastY;
    @SuppressWarnings("unused")
    private double 	Pointer1_LastDownTime;
	//.
    private int	 		ChannelSamples_ChannelsCount;
    private int			ChannelSamples_MaxSize;
    private boolean[]	ChannelSamples_Enabled;
    private String[]	ChannelSamples_ChannelNames;
    private short[][] 	ChannelSamples;
    private short		ChannelSamples_MaxRange = 100;
    private int			ChannelSamples_Position;
	//.
	private int				Graph_ChannelsCount;
	private double[]		Graph_ChannelsXScales;
	private int[]			Graph_ChannelsColors;
	private int             Graph_Width = 0;
	private int             Graph_Height = 0;
	private DisplayMetrics 	Graph_DisplayMetrics;
	private Bitmap          Graph_Bitmap = null;
	private Bitmap          Graph_BkgBitmap = null;
	private Canvas         	Graph_Graphics = null;
	private Paint         	Graph_Paint = new Paint();
	private Paint         	Graph_GraphPaint = null;
	private Paint         	Graph_ScrollBarPaint;
	private int 			Graph_ScrollBarHeight = 8;
	private double          Graph_XScale = 1.0;
	private double          Graph_YScale = 1.0;
	private int             Graph_ZeroLevel = 0;
	private int           	Graph_OffsetY = 0;
	private int             Graph_ChannelHeight = 0;
	private int 			Graph_ChannelHeightHalf = 0;
	private int             Graph_ChannelLength = 0;
	private int             Graph_ChannelSize = 0;
	private float[]         Graph_Graphics_LastPosX = null;
	private float[]	        Graph_Polyline_Xs = null;
	private float[]	        Graph_Polyline_Ys = null;
	private int[]           Graph_Polyline_Count = null;
	private Path           	Graph_Path = new Path();
	private float           Graph_ShiftY = 0;
	
	public TTLRMeasurementProcessor() {
		super();
	}
	
	@Override
	public void SetLayout(Activity pParentActivity, LinearLayout pParentLayout) {
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
	}

	@Override
	public void Initialize(TSensorMeasurement pMeasurement, double pMeasurementStartPosition) throws Exception {
		super.Initialize(pMeasurement, pMeasurementStartPosition);
		//.
		CurrentTime = Measurement.Descriptor.StartTimestamp;
		//.
		SetTLRChannel((TTLRChannel)Measurement.Descriptor.Model.Stream.Channels_GetOneByClass(TTLRChannel.class));
		//.
		ChannelSamples_Create();
	}
	
	@Override
	public void Finalize() throws Exception {
		//.
		super.Finalize();
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
		surface = null;
	}
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		surface = arg0;
		Width = arg2;
		Height = arg3;
		//.
		TimeResolution = (Measurement.Descriptor.FinishTimestamp-Measurement.Descriptor.StartTimestamp)/Width;
		//.
		Graph_Init(Width,Height);
		//.
		SurfaceUpdating = new TSurfaceUpdating();
		//.
		Update();
	}
	
	/*@Override
	public boolean onTouch(View pView, MotionEvent pEvent) {
		switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {
		
		case MotionEvent.ACTION_DOWN:
			Pointer0_Down(pEvent.getX(0),pEvent.getY(0));
			break; //. >
			
		case MotionEvent.ACTION_UP:
			Pointer0_Up(pEvent.getX(0),pEvent.getY(0));
			break; //. >
			
		case MotionEvent.ACTION_MOVE:
			Pointer0_Move(pEvent.getX(0),pEvent.getY(0));
			break; //. >
		}
		return true;
	}
	
    private void Pointer0_Down(double X, double Y) {
    	Pointer0_DownX = X;
    	Pointer0_DownY = Y;
    	//.
		Pointer0_Down_ChannelSamples_flMoving = true;
    	Pointer0_Down_ChannelSamples_Position = ChannelSamples_Position; 
    	Pointer0_Down_ChannelSamples_ShiftY = Graph_ShiftY; 
	}

	private void Pointer0_Up(double X, double Y) {
		Pointer0_Down_ChannelSamples_flMoving = false;
	}
	
	private void Pointer0_Move(double X, double Y) {
		if (Pointer0_Down_ChannelSamples_flMoving) {
			double dX = X-Pointer0_DownX;
			double dY = Y-Pointer0_DownY;
			if (Math.abs(dX) > Math.abs(dY)) {
				if (Math.abs(dX) > 2) {
					double Delta = (dX/Graph_XScale);
					int Position = (int)(Pointer0_Down_ChannelSamples_Position-Delta);
					ChannelSamples_SetPosition(Position);
				}
			}
			else {
				if (Math.abs(dY) > 2) {
					float Value = (float)(Pointer0_Down_ChannelSamples_ShiftY+dY);
					Graph_SetShiftY(Value);
				}
			}
		}
	}*/
	
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
			///////if (OnTimeChangeHandler != null) 
				///////OnTimeChangeHandler.DoOnTimeChanged(CurrentTime);
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
					////////if (OnTimeChangeHandler != null) 
						/////////////OnTimeChangeHandler.DoOnTimeChanging(CurrentTime, true, true);
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
		///////if (flFireEvent && (OnTimeChangeHandler != null)) 
			///////OnTimeChangeHandler.DoOnTimeChanging(CurrentTime, flChanging, flEventActionDelayAllowed);
	}

	private synchronized void SetTLRChannel(TTLRChannel pTLRChannel) {
		TLRChannel = pTLRChannel;
	}
	
	private synchronized TTLRChannel GetTLRChannel() {
		return TLRChannel;
	}
	
	private void Update() {
		Graph_DrawPage();
	}
	
	public void Draw() {
		StartDraw();
	}

	public void StartDraw() {
		if (SurfaceUpdating != null)
			SurfaceUpdating.StartUpdate();
	}

	private void ChannelSamples_Create() throws Exception {
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
        ChannelSamples = new short[ChannelSamples_ChannelsCount][];
        ChannelSamples_MaxSize = 0;
        for (int I = 0; I < ChannelSamples_ChannelsCount; I++) {
			TDataType DT = TLRChannel.DataTypes.Items.get(I);
			if (!(DT.ContainerType instanceof TDoubleContainerType)) {
            	ChannelSamples[I] = new short[0];
				continue; //. ^
			}
			@SuppressWarnings("unchecked")
			ArrayList<TContainerType> Values = (ArrayList<TContainerType>)DT.Extra;
        	if (Values == null) {
            	ChannelSamples[I] = new short[0];
        		continue; //. ^
        	}
        	int Size = Values.size();
        	ChannelSamples[I] = new short[Size];
        	short MinValue = Short.MAX_VALUE;
        	short MaxValue = Short.MIN_VALUE;
        	for (int J = 0; J < Size; J++) {
        		short V = ((Double)Values.get(J).GetValue()).shortValue();
        		ChannelSamples[I][J] = V;
        		if (V < MinValue)
        			MinValue = V;
        		if (V > MaxValue)
        			MaxValue = V;
        	}
        	int Range = (MaxValue-MinValue);
        	if (Range > 0)
            	for (int J = 0; J < Size; J++) {
            		short V = ChannelSamples[I][J];
            		V = (short)((V-MinValue)*ChannelSamples_MaxRange/Range);
            		ChannelSamples[I][J] = V;
            	}
        	else
            	for (int J = 0; J < Size; J++) {
            		short V = ChannelSamples[I][J];
            		V = (short)(V-MinValue);
            		ChannelSamples[I][J] = V;
            	}
        	if (Size > ChannelSamples_MaxSize)
        		ChannelSamples_MaxSize = Size;
        }
        if (ChannelSamples_MaxSize == 0)
        	throw new Exception("there is no data to process"); //. =>
	}
	
	private int ChannelSamples_GetPageSize(int PageSize, int ChannelIndex) {
		int Result = PageSize;
		int PS = ChannelSamples[ChannelIndex].length-ChannelSamples_Position;
		if (PS < Result)
			Result = PS;
		return Result;
	}
	
	private void ChannelSamples_SetPosition(int Position) {
		ChannelSamples_Position = Position;
		int Limit = ChannelSamples_MaxSize-Graph_ChannelSize;
		if (ChannelSamples_Position > Limit)
			ChannelSamples_Position = Limit;
		if (ChannelSamples_Position < 0)
			ChannelSamples_Position = 0;
		//.
		Graph_DrawPage();
	}
	
	private void Graph_Init(int Width, int Height) {
    	Graph_Width = Width;
    	Graph_Height = Height;
    	Graph_ChannelsCount = ChannelSamples_ChannelsCount;
    	//.
        Graph_DisplayMetrics = ParentActivity.getResources().getDisplayMetrics();
        Graph_XScale = 1.0;
        Graph_YScale = 1.0;
        Graph_XScale *= Graph_DisplayMetrics.density; 
        Graph_YScale *= Graph_DisplayMetrics.density;
        //.
        Graph_ChannelsXScales = new double[Graph_ChannelsCount];
        for (int I = 0; I < ChannelSamples_ChannelsCount; I++)
        	if (ChannelSamples[I].length > 0)
        	Graph_ChannelsXScales[I] = Graph_Width/ChannelSamples[I].length;
    	//.
    	Graph_Graphics = new Canvas();
    	//.
    	Graph_GraphPaint = new Paint();
    	Graph_GraphPaint.setDither(false);   
    	Graph_GraphPaint.setStyle(Paint.Style.STROKE);
    	Graph_GraphPaint.setAntiAlias(false);
    	//.
    	Graph_ScrollBarPaint = new Paint();
    	//.
        Graph_Bitmap = Bitmap.createBitmap(Width, Height, Bitmap.Config.RGB_565);
        Graph_Graphics.setBitmap(Graph_Bitmap);
        Graph_BkgBitmap = Bitmap.createBitmap(Width, Height, Bitmap.Config.RGB_565);
        //.
        Graph_OffsetY = 0;
        //.
        int EnabledChannelsCount = 0;
        for (int I = 0; I < Graph_ChannelsCount; I++)
        	if (ChannelSamples_Enabled[I])
        		EnabledChannelsCount++;
        if (EnabledChannelsCount != 0)
        	Graph_ChannelHeight = (int)((Graph_Bitmap.getHeight()-Graph_OffsetY)/EnabledChannelsCount);
        else
        	Graph_ChannelHeight = 0;
        Graph_ChannelHeightHalf = Graph_ChannelHeight/2;  
        Graph_ChannelLength = Graph_Width;
        Graph_ChannelSize = (int)(Graph_ChannelLength/Graph_XScale);
        //.
    	Graph_Graphics_LastPosX = new float[Graph_ChannelsCount];
    	for (int I = 0; I < Graph_ChannelsCount; I++)
    		Graph_Graphics_LastPosX[I] = -1.0F;
    	//.
    	int L = Graph_ChannelSize+1;
        Graph_Polyline_Xs = new float[L];
        Graph_Polyline_Ys = new float[L];
        Graph_Polyline_Count = new int[Graph_ChannelsCount];
    	for (int I = 0; I < Graph_ChannelsCount; I++)
    		Graph_Polyline_Count[I] = 0;
        //.
        Graph_ChannelsColors = new int[Graph_ChannelsCount];
        for (int I = 0; I < Graph_ChannelsCount; I++)
        	Graph_ChannelsColors[I] = Color.WHITE;
        int CI = 0;
        if (Graph_ChannelsCount > CI)
        	Graph_ChannelsColors[CI] = Color.WHITE;
        CI++;
        if (Graph_ChannelsCount > CI)
        	Graph_ChannelsColors[CI] = Color.YELLOW;
        CI++;
        if (Graph_ChannelsCount > CI)
        	Graph_ChannelsColors[CI] = Color.MAGENTA;
        CI++;
        if (Graph_ChannelsCount > CI)
        	Graph_ChannelsColors[CI] = Color.GREEN;
        CI++;
        if (Graph_ChannelsCount > CI)
        	Graph_ChannelsColors[CI] = Color.LTGRAY;
        CI++;
        if (Graph_ChannelsCount > CI)
        	Graph_ChannelsColors[CI] = Color.BLUE;
        CI++;
        if (Graph_ChannelsCount > CI)
        	Graph_ChannelsColors[CI] = Color.CYAN;
        //.
        Graph_BkgBitmap_Init();
        Graph_Bitmap_Clear();
    }

    private void Graph_BkgBitmap_Init() {
    	Canvas canvas = new Canvas();	
    	canvas.setBitmap(Graph_BkgBitmap);
    	Graph_Paint.setColor(Color.rgb(0,0,70));
    	canvas.drawRect(0,0, Graph_Bitmap.getWidth(),Graph_Bitmap.getHeight(), Graph_Paint);
    	Graph_Paint.setColor(Color.rgb(0,0,150));
        Graph_Paint.setStrokeWidth(1.0F);
    	int MeshStep = 48;
    	int X = 0;
    	int Y = Graph_OffsetY;
    	int W = Graph_BkgBitmap.getWidth();
    	int H = Graph_BkgBitmap.getHeight()-Graph_OffsetY;
    	int CntX = (int)(W/MeshStep)+1; 
    	int CntY = (int)(H/MeshStep)+1;
    	for (int I = 0; I < CntY; I++) {
    		canvas.drawLine(0,Y, W,Y, Graph_Paint);
    		Y += MeshStep;
    	}
    	for (int I = 0; I < CntX; I++) {
    		canvas.drawLine(X,Graph_OffsetY, X,Graph_OffsetY+H, Graph_Paint);
    		X += MeshStep;
    	}
    }
    
    private void Graph_Bitmap_Clear() {
    	Graph_Graphics.drawBitmap(Graph_BkgBitmap, 0,0, Graph_Paint);
    }
    
    private void Graph_ClearRectangle(Canvas G, float X0, float Y0, float W, float H) {
        Rect R = new Rect((int)X0,(int)Y0,(int)(X0+W),(int)(Y0+H));
        G.drawBitmap(Graph_BkgBitmap, R, R, Graph_Paint);
    }
    
    
    public void Graph_DrawPolyline(Canvas G, int PenColor, float[] Xs,float[] Ys, int Count) {
        Graph_GraphPaint.setColor(PenColor);
        Graph_GraphPaint.setStrokeWidth(2.5F);
        Graph_Path.rewind();
        Graph_Path.moveTo(Xs[0],Ys[0]);
        for (int I = 1; I < Count; I++) 
            Graph_Path.lineTo(Xs[I],Ys[I]);
        G.drawPath(Graph_Path,Graph_GraphPaint);
    }
    
    private void Graph_DrawPage() {
    	TTLRChannel TLRChannel = GetTLRChannel();
        if ((Graph_Bitmap != null) && (TLRChannel != null)) 
        {
            Graph_ClearRectangle(Graph_Graphics,0,Graph_OffsetY,Graph_Width,Graph_Height-Graph_OffsetY);
            //.
        	Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setTextSize(24*Graph_DisplayMetrics.density);
            paint.setColor(Color.RED);
            int _EnabledChannelIndex = 0;
        	for (int ChannelIndex = 0; ChannelIndex < Graph_ChannelsCount; ChannelIndex++)
        		if (ChannelSamples_Enabled[ChannelIndex]) {
            		float Y0 = Graph_OffsetY+Graph_ShiftY+(Graph_ChannelHeight*_EnabledChannelIndex)+Graph_ChannelHeightHalf;
            		//.
                    paint.setStyle(Paint.Style.STROKE);
                    Graph_Paint.setStrokeWidth(0.1F);
                    Graph_Graphics.drawLine(0,Y0, Graph_Width,Y0, paint);
            		//.
                    paint.setStyle(Paint.Style.FILL);
                    Graph_Graphics.drawText(ChannelSamples_ChannelNames[ChannelIndex],0,Y0-2, paint);
                	//.
                	_EnabledChannelIndex++;
        		}
        	//.
        	int MaxPageSize = 0;
        	float Yuplimit = Graph_OffsetY+1; 
            int EnabledChannelIndex = 0;
        	for (int ChannelIndex = 0; ChannelIndex < Graph_ChannelsCount; ChannelIndex++)
        		if (ChannelSamples_Enabled[ChannelIndex]) {
                	int PageSize = Graph_ChannelSize;
                	PageSize = ChannelSamples_GetPageSize(PageSize, ChannelIndex);
                	if (PageSize == 0) {
                		EnabledChannelIndex++;
                		continue; //. ^
                	}
                	if (PageSize > MaxPageSize)
                		MaxPageSize = PageSize;
                	//.
            		float X;
            		float Y0;
            		float Y;
            		//.
            		Y0 = Graph_OffsetY+Graph_ShiftY+(Graph_ChannelHeight*EnabledChannelIndex)+Graph_ChannelHeightHalf;
            		int Graph_Polyline_Count = 0;
            		for (int I = 0; I < PageSize; I++)
            		{
            			X = (float)(I*Graph_ChannelsXScales[ChannelIndex]*Graph_XScale);
            			Y = (float)(Y0-(ChannelSamples[ChannelIndex][ChannelSamples_Position+I]-Graph_ZeroLevel)*Graph_YScale);
            			if (Y < Yuplimit)
            				Y = Yuplimit;
            			//.
            			if ((Graph_Polyline_Count > 0) && ((int)Graph_Polyline_Xs[Graph_Polyline_Count-1] == (int)X)) {
	            			Graph_Polyline_Ys[Graph_Polyline_Count-1] = (Graph_Polyline_Ys[Graph_Polyline_Count-1]+Y)/2.0F;
            			}
            			else {
                			Graph_Polyline_Xs[Graph_Polyline_Count] = X;
	            			Graph_Polyline_Ys[Graph_Polyline_Count] = Y;
    	        			Graph_Polyline_Count++;                        
            			}
            		}
            		//.
            		if (Graph_Polyline_Count > 1)
            			Graph_DrawPolyline(Graph_Graphics, Graph_ChannelsColors[ChannelIndex], Graph_Polyline_Xs,Graph_Polyline_Ys, Graph_Polyline_Count);
            		//.
            		EnabledChannelIndex++;
        		}
        	//.
        	Graph_ScrollBar(MaxPageSize);
        }
        SurfaceUpdating.StartUpdate();
    }
    
    public void Graph_ScrollBar(int PageSize) {
        if (PageSize >= ChannelSamples_MaxSize)
        	return; //. ->
    	float Pos = ((ChannelSamples_Position+0.0F)/ChannelSamples_MaxSize)*Graph_Width;
    	float W = 16;
    	if (PageSize > 0) 
    		W = ((PageSize+0.0F)/ChannelSamples_MaxSize)*Graph_Width;
    	//.
        Graph_ScrollBarPaint.setColor(Color.GRAY);
		Graph_Graphics.drawRect(0,Graph_Height-Graph_ScrollBarHeight, Graph_Width,Graph_Height, Graph_ScrollBarPaint);
		Graph_ScrollBarPaint.setColor(Color.WHITE);
		Graph_Graphics.drawRect(Pos,Graph_Height-Graph_ScrollBarHeight+2, Pos+W,Graph_Height-2, Graph_ScrollBarPaint);
    }
    
    public void Graph_SetShiftY(float Value) {
    	Graph_ShiftY = Value;
    	int DownLimit = Graph_Bitmap.getHeight();
    	if (Graph_ShiftY > DownLimit)
    		Graph_ShiftY = DownLimit;
    	int UpLimit = -Graph_Bitmap.getHeight();
    	if (Graph_ShiftY < UpLimit)
    		Graph_ShiftY = UpLimit;
    	Graph_DrawPage();
    }
}
