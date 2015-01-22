package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TGeoLocationRecord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.THistoryRecord;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

@SuppressLint("HandlerLeak")
public class TObjectModelHistoryPanel extends Activity {

	public static class TTimeIntervalSlider extends SurfaceView implements OnTouchListener {
		
		public static final double DblClickTime = (1.0/(24*3600*1000))*333/*milliseconds*/;
		//.
		public static final int BackgroundColor = 0xFFC5C5C5;
		public static final int TimeIntervalColor = Color.WHITE;
		public static final int CenterMarkerColor = Color.MAGENTA;
		public static final int CenterMarkerColorHigh = Color.RED;
		public static final int TimeMarkerColor = Color.BLACK;
		public static final int SelectedIntervalTimeMarkerColor = Color.BLACK;
		public static final double 	DayDelta = 1.0;
		public static final int 	HourMarkerColor = Color.BLACK;
		public static final double 	HourDelta = 1.0/24.0;
		public static final int 	M30MarkerColor = Color.BLACK;
		public static final double 	M30Delta = 30.0/(24.0*60.0);
		public static final int 	M15MarkerColor = Color.BLACK;
		public static final double 	M15Delta = 15.0/(24.0*60.0);
		
		public static class TTimeIntervalSliderTimeMark {
			
			public double 	Time;
			public int 		Color;
			
			public TTimeIntervalSliderTimeMark(double pTime, int pColor) {
				Time = pTime;
				Color = pColor;
			}
		}

		public static class TTimeInterval {
			
			public double 	Time;
			public double 	Duration;
			
			public TTimeInterval(double pTime, double pDuration) {
				Time = pTime;
				Duration = pDuration;
			}
		}

		public static class TTimeIntervalSliderTimeMarkInterval {
			
			public double 	Time;
			public double 	Duration; 	
			public int		Color;

			public TTimeIntervalSliderTimeMarkInterval(double pTime, double pDuration, int pColor) {
				Time = pTime;
				Duration = pDuration;
				Color = pColor;
			}
		}

		public static class TOnTimeSelectedHandler {
			
			public void DoOnTimeSelected(double Time) {
			}
		}
		
		public static class TOnIntervalSelectedHandler {
			
			public void DoOnIntervalSelected(TTimeInterval Interval) {
			}
		}
		
		public class TSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {

			public SurfaceHolder _SurfaceHolder = null;

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				if (SurfaceUpdating != null) {
					SurfaceUpdating.Destroy();
					SurfaceUpdating = null;
				}
				// .
				_SurfaceHolder = holder;
				if (flTransparent)
					_SurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
				//.
				SurfaceUpdating = new TSurfaceUpdating();
				// .
				DoOnSizeChanged(width,height);
				// .
				SurfaceUpdating.Start();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				_SurfaceHolder = holder;
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				if (SurfaceUpdating != null) {
					SurfaceUpdating.Destroy();
					SurfaceUpdating = null;
				}
				_SurfaceHolder = null;
			}
		}

		private static final int MESSAGE_DRAW = 1;

		private final Handler SurfaceMessageHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				try {
					switch (msg.what) {

					case MESSAGE_DRAW:
						DoDraw();
						break; // . >
					}
				} catch (Throwable E) {
					TGeoLogApplication.Log_WriteError(E);
				}
			}
		};

		public class TSurfaceUpdating implements Runnable {

			private Thread _Thread;
			private boolean flCancel = false;
			public boolean flProcessing = false;
			private TAutoResetEvent ProcessSignal = new TAutoResetEvent();

			public TSurfaceUpdating() {
				_Thread = new Thread(this);
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
								return; // . ->
							//.
							try {
								DoDraw();
							}
							catch (Exception E) {
							}
							//.
							TGeoLogApplication.Instance().GarbageCollector.Start();
						}
					} finally {
						flProcessing = false;
					}
				} catch (Throwable T) {
				}
			}

			public void StartUpdate() {
				ProcessSignal.Set();
			}

			public void Join() {
				try {
					if (_Thread != null)
						_Thread.join();
				} catch (Exception E) {
				}
			}

			public void Cancel() {
				flCancel = true;
				// .
				ProcessSignal.Set();
				// .
				if (_Thread != null)
					_Thread.interrupt();
			}

			public void CancelAndWait() {
				Cancel();
				Join();
			}
		}

		protected TObjectModelHistoryPanel HistoryPanel = null;
		//.
		private TSurfaceHolderCallbackHandler 	SurfaceHolderCallbackHandler = new TSurfaceHolderCallbackHandler();
		private TSurfaceUpdating 				SurfaceUpdating = null;
		//.
		public int Width = 0;
		public int Height = 0;
		//. 
		protected boolean flTransparent = false;
		//.
		private boolean flSetup = false;
		//.
	    private double CurrentTime;
	    private double TimeResolution;
	    private double TimeIntervalBegin;
	    private double TimeIntervalEnd;
	    private TTimeIntervalSliderTimeMark[] TimeMarks;
	    private TTimeIntervalSliderTimeMarkInterval[] TimeMarkIntervals;
	    private TOnTimeSelectedHandler OnTimeSelectedHandler;
	    private TOnIntervalSelectedHandler OnIntervalSelectedHandler;
	    private TTimeInterval SelectedInterval;
	    private boolean Pointer0_flMoving;
	    private boolean Pointer0_flTimeSelecting;
	    private boolean Pointer0_flIntervalSelecting;
	    private double 	Pointer0_LastX;
	    @SuppressWarnings("unused")
		private double 	Pointer0_LastY;
	    private double 	Pointer0_LastDownTime;
	    private boolean Pointer1_flMoving;
	    private double 	Pointer1_LastX;
	    @SuppressWarnings("unused")
		private double 	Pointer1_LastY;
	    @SuppressWarnings("unused")
	    private double 	Pointer1_LastDownTime;
	    //.
	    private DisplayMetrics metrics;
	    private Paint paint = new Paint();
		
		public TTimeIntervalSlider(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public TTimeIntervalSlider(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public TTimeIntervalSlider(Context context) {
			super(context);
		}
		
		public void Initialize(TObjectModelHistoryPanel pHistoryPanel) {
			HistoryPanel = pHistoryPanel;
			//.
			metrics = HistoryPanel.getResources().getDisplayMetrics();
			//.
			SurfaceHolder sh = getHolder();
			sh.addCallback(SurfaceHolderCallbackHandler);
			//.
			setOnTouchListener(this);
			//.
			setVisibility(View.VISIBLE);
		}

		public void Finalize() {
			setVisibility(View.GONE);
			clearAnimation();
			//.
			SurfaceHolder sh = getHolder();
			sh.removeCallback(SurfaceHolderCallbackHandler);
		}

		public void Reinitialize(TObjectModelHistoryPanel pHistoryPanel) {
			Finalize();
			//.
			Initialize(pHistoryPanel);
			//.
			DoOnSizeChanged(Width, Height);
		}

		public void Setup(TObjectModelHistoryPanel pHistoryPanel, double pCurrentTime, double pTimeResolution, double pTimeIntervalBegin, double pTimeIntervalEnd, TTimeIntervalSliderTimeMark[] pTimeMarks, TOnTimeSelectedHandler pDoOnTimeSelectedHandler) {
			CurrentTime = pCurrentTime;
			TimeResolution = pTimeResolution;
			TimeIntervalBegin = pTimeIntervalBegin;
			TimeIntervalEnd = pTimeIntervalEnd;
			TimeMarks = pTimeMarks;
			TimeMarkIntervals = null;
			OnTimeSelectedHandler = pDoOnTimeSelectedHandler;
			OnIntervalSelectedHandler = null;
			SelectedInterval = new TTimeInterval(0.0, 0.0);
			//.
			Pointer0_LastDownTime = 0.0;
			Pointer0_flMoving = false;
			Pointer0_flTimeSelecting = false;
			//.
			flSetup = true;
			//.
			PostDraw();
		}
		
		protected void DoOnSizeChanged(int w, int h) {
			Width = w;
			Height = h;
			//.
			setMinimumWidth(Width);
			setMinimumHeight(Height);
		}

		public void DoDraw() {
			if (SurfaceHolderCallbackHandler._SurfaceHolder == null)
				return; // . ->
			Canvas canvas = SurfaceHolderCallbackHandler._SurfaceHolder.lockCanvas();
			if (canvas == null)
				return; // . ->
			try {
				DoOnDraw(canvas, null/* DrawCanceller */, null/* DrawTimeLimit */);
			} finally {
				SurfaceHolderCallbackHandler._SurfaceHolder.unlockCanvasAndPost(canvas);
			}
		}

		public void Draw() {
			StartDraw();
		}

		public void StartDraw() {
			if (SurfaceUpdating != null)
				SurfaceUpdating.StartUpdate();
		}

		public void PostDraw() {
			SurfaceMessageHandler.obtainMessage(MESSAGE_DRAW).sendToTarget();
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
			double IntervalBegin = CurrentTime-(Mid*TimeResolution);
			double IntervalEnd = CurrentTime+(Mid*TimeResolution);
			//. draw TimeInterval
			double TIB,TIE;
			if (TimeIntervalBegin >= IntervalBegin) 
				TIB = TimeIntervalBegin;
			else
				TIB = IntervalBegin;
			if (TimeIntervalEnd <= IntervalEnd) 
				TIE = TimeIntervalEnd;
			else 
				TIE = IntervalEnd;
			paint.setColor(TimeIntervalColor);
			canvas.drawRect((float)(Mid+(TIB-CurrentTime)/TimeResolution),0.0F,(float)(Mid+(TIE-CurrentTime)/TimeResolution),Height, paint);
			float X,Y,L;
			paint.setTextSize(12*metrics.density);
			//. draw selected interval
			if (SelectedInterval.Duration != 0.0) {
				Y = 0.0F;
				X = (float)(Mid+(SelectedInterval.Time-CurrentTime)/TimeResolution);
				L = (float)(SelectedInterval.Duration/TimeResolution);
				//.
				paint.setColor(Color.RED);
				canvas.drawRect(X,Y,X+L,Height, paint);
				//.
				String S = (new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(SelectedInterval.Time)).GetDateTime()); 
				paint.setColor(SelectedIntervalTimeMarkerColor);
				canvas.drawText(S, X+3.0F,paint.getTextSize(), paint);
				S = (new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(SelectedInterval.Time+SelectedInterval.Duration)).GetDateTime())+" ("+(new SimpleDateFormat("HH:mm",Locale.US)).format((new OleDate(SelectedInterval.Duration)).GetDateTime())+")"; 
				canvas.drawText(S, (X+L)+3.0F,2.0F*paint.getTextSize(), paint);
			}
			//. draw Day marks
			paint.setStrokeWidth(1.0F*metrics.density);
			paint.setColor(TimeMarkerColor);
			double DY = (Height*(1.0F/4));
			double DLLP = -Double.MAX_VALUE;
			double Day = Math.floor(IntervalBegin/DayDelta)*DayDelta;
			while (Day < IntervalEnd) {
				  X = (float)(Mid+(Day-CurrentTime)/TimeResolution);
				  canvas.drawLine(X,Height, X,(float)DY, paint);
				  String S = (new SimpleDateFormat("dd.MM",Locale.US)).format((new OleDate(Day)).GetDateTime());
				  double TW = (paint.measureText(S)/2.0);
				  double LP = X-TW;
				  if (LP > DLLP) {
					  canvas.drawText(S, (float)LP,(float)DY, paint);
					  DLLP = X+TW;
				  }
				  //. draw Hour marks
				  double HY = (Height*(1.0/2));
				  double HLLP = -Double.MAX_VALUE;
				  double Hour = Day+HourDelta;
				  while (Hour < (Day+DayDelta)) {
					  X = (float)(Mid+(Hour-CurrentTime)/TimeResolution);
					  canvas.drawLine(X,Height, X,(float)HY, paint);
					  S = (new SimpleDateFormat("HH",Locale.US)).format((new OleDate(Hour)).GetDateTime());
					  TW = (paint.measureText(S)/2.0);
					  LP = X-TW;
					  if (LP > HLLP) {
						  canvas.drawText(S, (float)LP,(float)HY, paint);
					      HLLP = X+2.0*TW;
					  }
				    //.
				    Hour = Hour+HourDelta;
				  }
				  //.
				  Day = Day+DayDelta;
				  //. draw M30 marks
				  double M30Y = (Height*(5.0/8));
				  //. double M30LLP = -Double.MAX_VALUE;
				  double M30 = Day-DayDelta+M30Delta;
				  while (M30 < (Hour+HourDelta)) {
					  X = (float)(Mid+(M30-CurrentTime)/TimeResolution);
					  canvas.drawLine(X,Height, X,(float)M30Y, paint);
					  //.
					  M30 = M30+HourDelta;
				  }
				  //. draw M15 marks
				  double M15Y = (float)(Height*(3/4));
				  //. double M15LLP = -Double.MAX_VALUE;
				  double M15 = Day-DayDelta+M15Delta;
				  while (M15 < (M30+M30Delta)) {
					  X = (float)(Mid+(M15-CurrentTime)/TimeResolution);
					  canvas.drawLine(X,Height, X,(float)M15Y, paint);
					  //.
					  M15 = M15+M30Delta;
				  }
			}
			//. draw center marker
			paint.setStrokeWidth(3.0F*metrics.density);
			paint.setColor(CenterMarkerColor);
			canvas.drawLine((float)Mid,0.0F, (float)Mid,Height, paint);
			paint.setStrokeWidth(1.0F*metrics.density);
			paint.setColor(CenterMarkerColorHigh);
			canvas.drawLine((float)Mid,0.0F, (float)Mid,Height, paint);
			paint.setColor(CenterMarkerColorHigh);
			String S = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.US)).format((new OleDate(CurrentTime)).GetDateTime()); 
			paint.setTextSize(18*metrics.density);
			canvas.drawText(S, (float)Mid+3.0F*metrics.density,0.0F+paint.getTextSize(), paint);
			//. draw TimeMarks
			int Cnt = 0;
			if (TimeMarks != null)
				Cnt = TimeMarks.length;
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
				Y = (float)(Height*(7.0/8));
				float Y1 = (float)(Height*(15.0/16))-1;
				float MarkWidth = 3.0F*metrics.density;
				for (int I = 0; I < Cnt; I++) {
				    X = (float)(Mid+(TimeMarks[I].Time-CurrentTime)/TimeResolution);
					paint.setColor(TimeMarks[I].Color);
					canvas.drawRect(X,Y, X+MarkWidth,Y1, paint);
				}
			}
			Cnt = 0;
			if (TimeMarkIntervals != null)
				Cnt = TimeMarkIntervals.length;
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
				Y = (float)(Height*(15.0/16));
				for (int I = 0; I < Cnt; I++) {
				    X = (float)(Mid+(TimeMarkIntervals[I].Time-CurrentTime)/TimeResolution);
				    L = (float)(TimeMarkIntervals[I].Duration/TimeResolution);
					paint.setColor(TimeMarkIntervals[I].Color);
					canvas.drawRect(X,Y, X+L,Height, paint);
				}
			}
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
				  	if (!((TimeIntervalBegin <= _CurrentTime) && (_CurrentTime <= TimeIntervalEnd))) 
				  		return; //. ->
				  	//.
				  	if ((OleDate.UTCCurrentTimestamp()-Pointer0_LastDownTime) > DblClickTime) {
					    SelectedInterval.Time = _CurrentTime;
				  		SelectedInterval.Duration = 0.0;
				  	}
				  	else {
					    SelectedInterval.Time = TimeIntervalBegin;
					    SelectedInterval.Duration = (TimeIntervalEnd-TimeIntervalBegin);
					    //.
					    Pointer0_flMoving = false;
				  	}
				  	//.
				  	Draw();
				  	//.
				  	if (OnIntervalSelectedHandler != null) 
				  		OnIntervalSelectedHandler.DoOnIntervalSelected(SelectedInterval);
				  	//.
				  	Pointer0_flIntervalSelecting = true;
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
			Pointer0_flMoving = false;
			//.
			Pointer0_flTimeSelecting = false;
			Pointer0_flIntervalSelecting = false;
		}
		
		protected void Pointer0_Move(double X, double Y) {
			if (Pointer0_flMoving) {
				if (!Pointer1_flMoving) {
					if (Pointer0_flIntervalSelecting) {
						double _CurrentTime = CurrentTime+((X-(Width/2.0))*TimeResolution);
					    if (_CurrentTime < TimeIntervalBegin) 
					    	_CurrentTime = TimeIntervalBegin;
					    if (_CurrentTime > TimeIntervalEnd) 
					    	_CurrentTime = TimeIntervalEnd;
					    //.
					    SelectedInterval.Duration = _CurrentTime-SelectedInterval.Time;
					    //.
					    Draw();
					    //. fire event
					    if (OnIntervalSelectedHandler != null) {
					    	TTimeInterval Interval = SelectedInterval;
					    	if (Interval.Duration < 0.0) {
					    		Interval.Time = Interval.Time+Interval.Duration;
					    		Interval.Duration = -Interval.Duration;
					    	}
					    	OnIntervalSelectedHandler.DoOnIntervalSelected(Interval);
					    }
					}
					else
						if (Pointer0_flTimeSelecting) {
							Move(-(X-Pointer0_LastX));
							//.
							if (OnTimeSelectedHandler != null) 
								OnTimeSelectedHandler.DoOnTimeSelected(CurrentTime);
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
			if (_CurrentTime < TimeIntervalBegin) 
				_CurrentTime = TimeIntervalBegin; 
			if (_CurrentTime > TimeIntervalEnd) 
				_CurrentTime = TimeIntervalEnd;
			if (_CurrentTime == CurrentTime) 
				return; //. ->
			//.
			CurrentTime = _CurrentTime;
			//.
			Draw();
		}

		public void ScaleTimeResolution(double pScale) {
			TimeResolution = TimeResolution*pScale;
			//.
			Draw();
		}
		
		public void SetCurrentTime(double pCurrentTime, boolean flFireEvent) {
			CurrentTime = pCurrentTime;
			//.
			Draw();
			//.
			if (flFireEvent && (OnTimeSelectedHandler != null)) 
				OnTimeSelectedHandler.DoOnTimeSelected(CurrentTime);
		}

		public void SelectTimeInterval(TTimeInterval Interval, boolean flFireEvent) {
			SelectedInterval = Interval;
			//.
			Draw();
			//.
			if (flFireEvent && (OnIntervalSelectedHandler != null)) 
				OnIntervalSelectedHandler.DoOnIntervalSelected(SelectedInterval);
		}
	}

	public static class THistory {
		
		public int ObjectGeoDatumID;
		//.
		public ArrayList<THistoryRecord> 	Records;
		public double						Records_BeginTimestamp;
		public double						Records_EndTimestamp;
		//.
		public TTimeIntervalSlider.TTimeIntervalSliderTimeMark[]	TimeIntervalSliderTimeMarks = null;
		
		public THistory(int pObjectGeoDatumID, ArrayList<THistoryRecord> pRecords) {
			ObjectGeoDatumID = pObjectGeoDatumID;
			Records = pRecords;
			//.
			Update();
		}
		
		private void Update() {
			Records_BeginTimestamp = Double.MAX_VALUE;
			Records_EndTimestamp = -Double.MAX_VALUE;
			int Cnt = Records.size();
			TimeIntervalSliderTimeMarks = new TTimeIntervalSlider.TTimeIntervalSliderTimeMark[Cnt]; 
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.get(I);
				if (Record.Timestamp < Records_BeginTimestamp)
					Records_BeginTimestamp = Record.Timestamp; 
				if (Record.Timestamp > Records_EndTimestamp)
					Records_EndTimestamp = Record.Timestamp;
				//.
				int MarkColor = Color.BLACK;
				if (Record instanceof TGeoLocationRecord) {
					TGeoLocationRecord GeoLocationRecord = (TGeoLocationRecord)Record;
					if (GeoLocationRecord.Speed > 0)
						MarkColor = TGeoLocationRecord.SpeedColorer.GetColor(GeoLocationRecord.Speed);
				}
				TimeIntervalSliderTimeMarks[I] = new TTimeIntervalSlider.TTimeIntervalSliderTimeMark(Record.Timestamp,MarkColor);
			}
		}

		public double Records_CentralTimestamp() {
			return (Records_BeginTimestamp+Records_EndTimestamp)/2.0;
		}
		
		public TGeoLocationRecord GetNearestGeoLocationRecord(double Timestamp) {
			TGeoLocationRecord Result = null;
			int Cnt = Records.size();
			double MinDistance = Double.MAX_VALUE;
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.get(I);
				if (Record instanceof TGeoLocationRecord) {
					double Distance = Math.abs(Record.Timestamp-Timestamp); 
					if (Distance < MinDistance) {
						MinDistance = Distance;
						Result = (TGeoLocationRecord)Record;
					}
				}
			}
			return Result;
		}
	}
	
	
	private long				ObjectID = -1;
	private TCoGeoMonitorObject Object = null;
	private TObjectModel		ObjectModel = null;
	//.
	private double DayDate;
	private short DaysCount;
	//.
	private THistory History;
	//.
	private TTimeIntervalSlider TimeIntervalSlider;
	//.
	private Button btnShowCurrentTimeInReflector;
	//.
	private TextView tvHistory;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		TUserAgent UserAgent = TUserAgent.GetUserAgent();
		if (UserAgent == null) {
			Toast.makeText(this, R.string.SUserAgentIsNotInitialized, Toast.LENGTH_LONG).show();
			finish();
			return; //. ->
		}
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	ObjectID = extras.getLong("ObjectID");
        	Object = new TCoGeoMonitorObject(UserAgent.Server, ObjectID);
        	DayDate = extras.getDouble("DayDate");
        	DaysCount = extras.getShort("DaysCount");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.objectmodel_history_panel);
        //.
        TimeIntervalSlider = (TTimeIntervalSlider)findViewById(R.id.svTimeIntervalSlider);
        TimeIntervalSlider.Initialize(this);
        //.
        tvHistory = (TextView)findViewById(R.id.tvObjectModelHistory);
        //.
        btnShowCurrentTimeInReflector = (Button)findViewById(R.id.btnShowCurrentTimeInReflector);
        btnShowCurrentTimeInReflector.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				ShowCurrentTimeInReflector();
            }
        });
        //.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SLoading)) {
			
			private TObjectModel _ObjectModel = null; 
			private THistory _History = null;
			
			@Override
			public void Process() throws Exception {
				byte[] ObjectModelData = TObjectModelHistoryPanel.this.Object.GetData(1000001);
				if (ObjectModelData != null) {
    				Canceller.Check();
    				//.
					int Idx = 0;
					int ObjectModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
					int BusinessModelID = TDataConverter.ConvertLEByteArrayToInt32(ObjectModelData,Idx); Idx+=4;
					//.
					if (ObjectModelID != 0) {
						_ObjectModel = TObjectModel.GetObjectModel(ObjectModelID);
						if (_ObjectModel != null) {
							_ObjectModel.SetBusinessModel(BusinessModelID);
							//.
							TGeographServerObjectController GSOC = TObjectModelHistoryPanel.this.Object.GeographServerObjectController();
							synchronized (GSOC) {
								boolean flKeepConnectionLast = GSOC.KeepConnection();
								try {
									GSOC.Connect();
									try {
					    				Canceller.Check();
					    				//.
										byte[] ObjectSchemaData = GSOC.Component_ReadAllCUAC(new int[] {1}/*object side*/);
										//.
					    				Canceller.Check();
					    				//.
										if (ObjectSchemaData != null)
											_ObjectModel.ObjectSchema.RootComponent.FromByteArray(ObjectSchemaData,new TIndex());
										//.
										byte[] ObjectDeviceSchemaData = GSOC.Component_ReadAllCUAC(new int[] {2/*device side*/});
										//.
					    				Canceller.Check();
					    				//.
										if (ObjectDeviceSchemaData != null)
											_ObjectModel.ObjectDeviceSchema.RootComponent.FromByteArray(ObjectDeviceSchemaData,new TIndex());
										//.
										_ObjectModel.SetObjectController(GSOC, false);
										ArrayList<THistoryRecord> _HistoryRecords = _ObjectModel.History_GetRecords(DayDate,DaysCount, context);
										//.
										_History = new THistory(_ObjectModel.ObjectDatumID(), _HistoryRecords);
									}
									finally {
										GSOC.Disconnect();
									}
								}
								finally {
									GSOC.Connection_flKeepAlive = flKeepConnectionLast;
								}
							}
						}
					}
				}
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				ObjectModel = _ObjectModel;
				History = _History;
				//.
				StringBuilder SB = new StringBuilder();
				int Cnt = History.Records.size();
				for (int I = 0; I < Cnt; I++) {
					SB.append(History.Records.get(I).GetString());
					SB.append("\n");
				}
				tvHistory.setText(SB.toString());
				//.
				if (History.Records.size() > 0) {
					int Width = TimeIntervalSlider.Width;
					if (Width == 0)
						Width = 1024;
					double TimeResolution = (History.Records_EndTimestamp-History.Records_BeginTimestamp)/Width;
					if (TimeResolution == 0.0)
						TimeResolution = 1.0/1024;
			        TimeIntervalSlider.Setup(TObjectModelHistoryPanel.this, History.Records_CentralTimestamp(), TimeResolution, History.Records_BeginTimestamp,History.Records_EndTimestamp, History.TimeIntervalSliderTimeMarks, null);
				}
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				//.
				finish();
			}
		};
		Processing.Start();
    }
    
    @Override
    protected void onDestroy() {
    	if (ObjectModel != null) {
    		ObjectModel.Destroy();
    		ObjectModel = null;
    	}
    	//.
    	if (Object != null) {
    		try {
				Object.Destroy();
			} catch (IOException E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
    		Object = null;
    	}
    	//.
    	if (TimeIntervalSlider != null) {
    		TimeIntervalSlider.Finalize();
    		TimeIntervalSlider = null;
    	}
    	//.
    	super.onDestroy();
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	//.
    	TimeIntervalSlider.PostDraw();
    }
    
    private void ShowCurrentTimeInReflector() {
    	TGeoLocationRecord GeoLocationRecord = History.GetNearestGeoLocationRecord(TimeIntervalSlider.CurrentTime);
    	if (GeoLocationRecord != null) {
    		TimeIntervalSlider.SetCurrentTime(GeoLocationRecord.Timestamp, false);
    		//.
    		if (!GeoLocationRecord.IsAvailable()) {
				Toast.makeText(this, R.string.SLocationIsNotAvailable, Toast.LENGTH_LONG).show();
				return; //. ->
    		}
    		//.
    		int ReflectorID = TReflector.GetNextID();
    		//.
    		Intent intent = new Intent(this, TReflector.class);
			intent.putExtra("ID", ReflectorID);
			intent.putExtra("Reason", TReflector.REASON_MONITORGEOLOCATION);
			startActivity(intent);
			//.
			SendShowGeoLocationRecordMessage(ReflectorID,GeoLocationRecord, 2000/*delay, ms*/);
    	}
    }
    
	private static final int MESSAGE_SHOWGEOLOCATIONRECORD = 1;

	private static class TShowGeoLocationRecordParams {
		
		public int ReflectorID;
		public TGeoLocationRecord GeoLocationRecord;
		
		public TShowGeoLocationRecordParams(int pReflectorID, TGeoLocationRecord pGeoLocationRecord) {
			ReflectorID = pReflectorID;
			GeoLocationRecord = pGeoLocationRecord;
		}
	}
	
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			try {
				switch (msg.what) {

				case MESSAGE_SHOWGEOLOCATIONRECORD:
					TShowGeoLocationRecordParams ShowGeoLocationRecordParams = (TShowGeoLocationRecordParams)msg.obj;
					//.
					TReflector Reflector = TReflector.GetReflector(ShowGeoLocationRecordParams.ReflectorID);
					if (Reflector != null)
						try {
							TXYCoord LocationXY = Reflector.ConvertGeoCoordinatesToXY(History.ObjectGeoDatumID, ShowGeoLocationRecordParams.GeoLocationRecord.Latitude,ShowGeoLocationRecordParams.GeoLocationRecord.Longitude,ShowGeoLocationRecordParams.GeoLocationRecord.Altitude);
							//.
							Reflector.MoveReflectionWindow(LocationXY);
						} catch (Exception E) {
						}
					break; // . >
				}
			} catch (Throwable E) {
				TGeoLogApplication.Log_WriteError(E);
			}
		}
	};
	
	private void SendShowGeoLocationRecordMessage(int ReflectorID, TGeoLocationRecord GeoLocationRecord, int Delay) {
		MessageHandler.sendMessageDelayed(MessageHandler.obtainMessage(MESSAGE_SHOWGEOLOCATIONRECORD,new TShowGeoLocationRecordParams(ReflectorID,GeoLocationRecord)), Delay);
	}
}