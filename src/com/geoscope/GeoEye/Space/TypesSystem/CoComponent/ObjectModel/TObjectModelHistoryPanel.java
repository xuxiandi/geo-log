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
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
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
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjectTrack;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TGeoLocationRecord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.THistoryRecord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TObjectHistoryRecords;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TSensorMeasurementDescriptor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerArchive;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerArchive.TArchiveItem;
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
		public static final int TimeIntervalMarkerColor = Color.BLUE;
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

		public static class TTimeIntervalSliderTimeIntervalMark {
			
			public double 	Time;
			public double 	Duration;
			public int 		Color;
			public String	Text;
			
			public TTimeIntervalSliderTimeIntervalMark(double pTime, double pDuration, int pColor, String pText) {
				Time = pTime;
				Duration = pDuration;
				Color = pColor;
				Text = pText;
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
	    private TTimeIntervalSliderTimeIntervalMark[] TimeIntervalMarks;
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

		public void Setup(TObjectModelHistoryPanel pHistoryPanel, double pCurrentTime, double pTimeResolution, double pTimeIntervalBegin, double pTimeIntervalEnd, TTimeIntervalSliderTimeMark[] pTimeMarks, TTimeIntervalSliderTimeIntervalMark[] pTimeIntervalMarks, TOnTimeSelectedHandler pDoOnTimeSelectedHandler) {
			CurrentTime = pCurrentTime;
			TimeResolution = pTimeResolution;
			TimeIntervalBegin = pTimeIntervalBegin;
			TimeIntervalEnd = pTimeIntervalEnd;
			TimeMarks = pTimeMarks;
			TimeIntervalMarks = pTimeIntervalMarks;
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
			double X,Y,L;
			double MinSpacing = 8*metrics.density;
			paint.setTextSize(12*metrics.density);
			//. draw selected interval
			if (SelectedInterval.Duration != 0.0) {
				Y = 0.0F;
				X = (Mid+(SelectedInterval.Time-CurrentTime)/TimeResolution);
				L = (SelectedInterval.Duration/TimeResolution);
				//.
				paint.setColor(Color.RED);
				canvas.drawRect((float)X,(float)Y,(float)(X+L),Height, paint);
				//.
				String S = (new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(SelectedInterval.Time)).GetDateTime()); 
				paint.setColor(SelectedIntervalTimeMarkerColor);
				canvas.drawText(S, (float)(X+3.0),paint.getTextSize(), paint);
				S = (new SimpleDateFormat("HH:mm:ss",Locale.US)).format((new OleDate(SelectedInterval.Time+SelectedInterval.Duration)).GetDateTime())+" ("+(new SimpleDateFormat("HH:mm",Locale.US)).format((new OleDate(SelectedInterval.Duration)).GetDateTime())+")"; 
				canvas.drawText(S, (float)((X+L)+3.0),2.0F*paint.getTextSize(), paint);
			}
			//. draw TimeMarks
			int Cnt = 0;
			if (TimeMarks != null)
				Cnt = TimeMarks.length;
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
				Y = (Height*(7.0/8));
				double Y1 = (Height*(15.0/16))-1;
				double MarkWidth = 3.0*metrics.density;
				for (int I = 0; I < Cnt; I++) {
				    X = (Mid+(TimeMarks[I].Time-CurrentTime)/TimeResolution);
				    if ((0 < X) && (X < Width)) {
						paint.setColor(TimeMarks[I].Color);
						canvas.drawRect((float)X,(float)Y, (float)(X+MarkWidth),(float)Y1, paint);
				    }
				}
			}
			//. draw TimeIntervalMarks
			Cnt = 0;
			if (TimeIntervalMarks != null)
				Cnt = TimeIntervalMarks.length;
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
				Y = (Height*(17.0/32));
				double Y1 = (Height*(27.0/32))-1;
				for (int I = 0; I < Cnt; I++) {
					TTimeIntervalSliderTimeIntervalMark Item = TimeIntervalMarks[I]; 
				    X = (Mid+(Item.Time-CurrentTime)/TimeResolution);
				    L = (Item.Duration/TimeResolution);
				    RectF Rect = new RectF((float)X,(float)Y, (float)(X+L),(float)Y1);
					canvas.save();
					try {
						canvas.clipRect(Rect);
						//.
						paint.setStyle(Paint.Style.FILL);
					    paint.setColor(Item.Color);
						canvas.drawRect(Rect, paint);
						//.
						paint.setTextSize(Rect.height()*0.50F);
						float TW = paint.measureText(Item.Text);
						if (TW < Rect.width()) {
							float TH = paint.getTextSize();
							float TX = Rect.left+((Rect.right-Rect.left)-TW)/2.0F;
							float TY = Rect.top+((Rect.bottom-Rect.top)+TH)/2.0F;
						    paint.setColor(Color.WHITE);
							canvas.drawText(Item.Text, TX,TY, paint);
						}
					}
					finally {
						canvas.restore();
					}
				}
			}
			//. draw Day marks
			paint.setStrokeWidth(1.0F*metrics.density);
			paint.setColor(TimeMarkerColor);
			double DY = (Height*(1.0/4));
			double DLLP = -Double.MAX_VALUE;
			double Day = Math.floor(IntervalBegin/DayDelta)*DayDelta;
			while (Day < IntervalEnd) {
				  X = (Mid+(Day-CurrentTime)/TimeResolution);
				  if ((0 < X) && (X < Width)) {
					  canvas.drawLine((float)X,Height, (float)X,(float)DY, paint);
					  String S = (new SimpleDateFormat("dd.MM",Locale.US)).format((new OleDate(Day)).GetDateTime());
					  double TW = (paint.measureText(S)/2.0);
					  double LP = X-TW;
					  if (LP > DLLP) {
						  canvas.drawText(S, (float)LP,(float)DY, paint);
						  DLLP = X+TW;
					  }
				  }
				  if ((HourDelta/TimeResolution) > MinSpacing) {
					  //. draw Hour marks
					  double HY = (Height*(1.0/2));
					  double HLLP = -Double.MAX_VALUE;
					  double Hour = Day+HourDelta;
					  while (Hour < (Day+DayDelta)) {
						  X = (Mid+(Hour-CurrentTime)/TimeResolution);
						  if ((0 < X) && (X < Width)) {
							  canvas.drawLine((float)X,Height, (float)X,(float)HY, paint);
							  String S = (new SimpleDateFormat("HH",Locale.US)).format((new OleDate(Hour)).GetDateTime());
							  double TW = (paint.measureText(S)/2.0);
							  double LP = X-TW;
							  if (LP > HLLP) {
								  if ((0 < LP) && (LP < Width))
									  canvas.drawText(S, (float)LP,(float)HY, paint);
							      HLLP = X+2.0*TW;
							  }
						  }
					    //.
					    Hour = Hour+HourDelta;
					  }
					  if ((M30Delta/TimeResolution) > MinSpacing) {
						  //. draw M30 marks
						  double M30Y = (Height*(5.0/8));
						  //. double M30LLP = -Double.MAX_VALUE;
						  double M30 = Day+M30Delta;
						  while (M30 < (Hour+HourDelta)) {
							  X = (Mid+(M30-CurrentTime)/TimeResolution);
							  if ((0 < X) && (X < Width)) 
								  canvas.drawLine((float)X,Height, (float)X,(float)M30Y, paint);
							  //.
							  M30 = M30+HourDelta;
						  }
						  if ((M15Delta/TimeResolution) > MinSpacing) {
							  //. draw M15 marks
							  double M15Y = (Height*(3.0/4));
							  //. double M15LLP = -Double.MAX_VALUE;
							  double M15 = Day+M15Delta;
							  while (M15 < (M30+M30Delta)) {
								  X = (Mid+(M15-CurrentTime)/TimeResolution);
								  if ((0 < X) && (X < Width)) 
									  canvas.drawLine((float)X,Height, (float)X,(float)M15Y, paint);
								  //.
								  M15 = M15+M30Delta;
							  }
						  }
					  }
				  }
				  //.
				  Day = Day+DayDelta;
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
			//.
			Cnt = 0;
			if (TimeMarkIntervals != null)
				Cnt = TimeMarkIntervals.length;
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
				Y = (Height*(15.0/16));
				for (int I = 0; I < Cnt; I++) {
				    X = (Mid+(TimeMarkIntervals[I].Time-CurrentTime)/TimeResolution);
				    L = (TimeMarkIntervals[I].Duration/TimeResolution);
					paint.setColor(TimeMarkIntervals[I].Color);
					canvas.drawRect((float)X,(float)Y, (float)(X+L),Height, paint);
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
		
		public static class TSensorMeasurements {
			
			public TSensorMeasurementDescriptor[] 				Measurements;
			public double										BeginTimestamp;
			public double										EndTimestamp;
			
			public TSensorMeasurements(TSensorMeasurementDescriptor[] pMeasurements) {
				Measurements = pMeasurements;
				//.
				Update();
			}
			
			private void Update() {
				BeginTimestamp = Double.MAX_VALUE;
				EndTimestamp = -Double.MAX_VALUE;
				int Cnt = Measurements.length;
				for (int I = 0; I < Cnt; I++) {
					TSensorMeasurementDescriptor Measurement = Measurements[I];
					if (Measurement.IsValid()) {
						if (Measurement.StartTimestamp < BeginTimestamp)
							BeginTimestamp = Measurement.StartTimestamp; 
						if (Measurement.FinishTimestamp > EndTimestamp)
							EndTimestamp = Measurement.FinishTimestamp;
					}
				}
			}

			public double Measurements_CentralTimestamp() {
				return (BeginTimestamp+EndTimestamp)/2.0;
			}
			
			public TSensorMeasurementDescriptor GetMeasurementByTimestamp(double Timestamp) {
				int Cnt = Measurements.length;
				for (int I = 0; I < Cnt; I++) {
					TSensorMeasurementDescriptor Measurement = Measurements[I];
					if ((Measurement.StartTimestamp <= Timestamp) && (Timestamp < Measurement.FinishTimestamp))
						return Measurement; //. ->
				}
				return null;
			}

			public TSensorMeasurementDescriptor GetNearestMeasurementToTimestamp(double Timestamp, TSensorMeasurementDescriptor ExceptMeasurement) {
				TSensorMeasurementDescriptor Result = null;
				int Cnt = Measurements.length;
				double MinDistance = Double.MAX_VALUE;
				for (int I = 0; I < Cnt; I++) {
					TSensorMeasurementDescriptor Measurement = Measurements[I];
					if (Measurement != ExceptMeasurement) {
						double Distance = (Measurement.StartTimestamp-Timestamp);
						if ((Distance > 0) && (Distance < MinDistance)) {
							MinDistance = Distance;
							//.
							Result = Measurement; 
						}
					}
				}
				return Result;
			}
		}


		public int ObjectGeoDatumID;
		//.
		public TObjectHistoryRecords Records;
		//.
		public TSensorMeasurements			SensorMeasurements;
		//.
		public double						BeginTimestamp;
		public double						EndTimestamp;
		//.
		public TTimeIntervalSlider.TTimeIntervalSliderTimeMark[]			TimeIntervalSliderTimeMarks = null;
		public TTimeIntervalSlider.TTimeIntervalSliderTimeIntervalMark[]	TimeIntervalSliderTimeIntervalMarks = null;
		//.
		public String[] BusinessModelRecords; 
		
		public THistory(int pObjectGeoDatumID, TObjectHistoryRecords pRecords, TSensorMeasurementDescriptor[] pSensorMeasurements) {
			ObjectGeoDatumID = pObjectGeoDatumID;
			Records = pRecords;
			SensorMeasurements = new TSensorMeasurements(pSensorMeasurements);
			//.
			Update();
		}
		
		private void Update() {
			BeginTimestamp = Double.MAX_VALUE;
			EndTimestamp = -Double.MAX_VALUE;
			int Cnt = Records.ObjectModelRecords.size();
			TimeIntervalSliderTimeMarks = new TTimeIntervalSlider.TTimeIntervalSliderTimeMark[Cnt]; 
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.ObjectModelRecords.get(I);
				if (Record.Timestamp < BeginTimestamp)
					BeginTimestamp = Record.Timestamp; 
				if (Record.Timestamp > EndTimestamp)
					EndTimestamp = Record.Timestamp;
				//.
				int MarkColor = Color.BLACK;
				if (Record instanceof TGeoLocationRecord) {
					TGeoLocationRecord GeoLocationRecord = (TGeoLocationRecord)Record;
					if (GeoLocationRecord.Speed > 0)
						MarkColor = TGeoLocationRecord.SpeedColorer.GetColor(GeoLocationRecord.Speed);
				}
				TimeIntervalSliderTimeMarks[I] = new TTimeIntervalSlider.TTimeIntervalSliderTimeMark(Record.Timestamp,MarkColor);
			}
			Cnt = Records.BusinessModelRecords.size();
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.BusinessModelRecords.get(I);
				if (Record.Timestamp < BeginTimestamp)
					BeginTimestamp = Record.Timestamp; 
				if (Record.Timestamp > EndTimestamp)
					EndTimestamp = Record.Timestamp;
			}
			if (SensorMeasurements.BeginTimestamp < BeginTimestamp)
				BeginTimestamp = SensorMeasurements.BeginTimestamp; 
			if (SensorMeasurements.EndTimestamp > EndTimestamp)
				EndTimestamp = SensorMeasurements.EndTimestamp; 
			//. sensor measurements 
			Cnt = SensorMeasurements.Measurements.length;
			TimeIntervalSliderTimeIntervalMarks = new TTimeIntervalSlider.TTimeIntervalSliderTimeIntervalMark[Cnt]; 
			for (int I = 0; I < Cnt; I++) {
				TSensorMeasurementDescriptor Measurement = SensorMeasurements.Measurements[I];
				//.
				TimeIntervalSliderTimeIntervalMarks[I] = new TTimeIntervalSlider.TTimeIntervalSliderTimeIntervalMark(Measurement.StartTimestamp,Measurement.FinishTimestamp-Measurement.StartTimestamp, Color.BLUE, "video");
			}
			//.
			Cnt = Records.BusinessModelRecords.size();
			BusinessModelRecords = new String[Cnt];
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.BusinessModelRecords.get(I); 
				String S = (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",Locale.US)).format((new OleDate(Record.Timestamp)).GetDateTime())+": "+Record.GetString(1/*only message*/);
				BusinessModelRecords[I] = S;
			}
		}

		public double Records_CentralTimestamp() {
			return (BeginTimestamp+EndTimestamp)/2.0;
		}
		
		public TGeoLocationRecord GetNearestGeoLocationRecord(double Timestamp, boolean flAvailableOnly) {
			TGeoLocationRecord Result = null;
			int Cnt = Records.ObjectModelRecords.size();
			double MinDistance = Double.MAX_VALUE;
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.ObjectModelRecords.get(I);
				if (Record instanceof TGeoLocationRecord) {
					TGeoLocationRecord GeoLocationRecord = (TGeoLocationRecord)Record;
					if (!flAvailableOnly || GeoLocationRecord.IsAvailable()) {
						double Distance = Math.abs(Record.Timestamp-Timestamp); 
						if (Distance < MinDistance) {
							MinDistance = Distance;
							Result = GeoLocationRecord;
						}
					}
				}
			}
			return Result;
		}
	}
	
	public static final int REQUEST_SHOWVIDEOMEASUREMENT = 1;
	
	private boolean flExists = false;
	//.
	private long				ObjectID = -1;
	private TCoGeoMonitorObject Object = null;
	private TObjectModel		ObjectModel = null;
	//.
	private double DayDate;
	private short DaysCount;
	//.
	private String 	GeographDataServerAddress = "";
	private int 	GeographDataServerPort = 0;
	@SuppressWarnings("unused")
	private int		UserID;
	@SuppressWarnings("unused")
	private String	UserPassword;
	//.
	private THistory History;
	//.
	private TTimeIntervalSlider TimeIntervalSlider;
	//.
	private Button btnShowCurrentTimeInReflector;
	private Button btnShowCurrentTimeMeasurementViewer;
	//.
	private ListView 		lvBusinessModelRecords;
	ArrayAdapter<String> 	lvBusinessModelRecords_Adapter;
	private int				lvBusinessModelRecords_SelectedIndex = -1;
	
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
        	//.
        	DayDate = extras.getDouble("DayDate");
        	DaysCount = extras.getShort("DaysCount");
        	//.
        	GeographDataServerAddress = extras.getString("GeographDataServerAddress");
        	GeographDataServerPort = extras.getInt("GeographDataServerPort");
        	UserID = extras.getInt("UserID");
        	UserPassword = extras.getString("UserPassword");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.objectmodel_history_panel);
        //.
        TimeIntervalSlider = (TTimeIntervalSlider)findViewById(R.id.svTimeIntervalSlider);
        TimeIntervalSlider.Initialize(this);
        //.
        lvBusinessModelRecords = (ListView)findViewById(R.id.lvBusinessModelRecords);
        lvBusinessModelRecords.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
            	lvBusinessModelRecords_SetSelectedItem(position, true);
                //.
            	TimeIntervalSlider.SetCurrentTime(History.Records.BusinessModelRecords.get(position).Timestamp, false);
            }
        });        
        //.
        btnShowCurrentTimeInReflector = (Button)findViewById(R.id.btnShowCurrentTimeInReflector);
        btnShowCurrentTimeInReflector.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				ShowCurrentTimeInReflector();
            }
        });
        //.
        btnShowCurrentTimeMeasurementViewer = (Button)findViewById(R.id.btnShowCurrentTimeMeasurementViewer);
        btnShowCurrentTimeMeasurementViewer.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				ShowCurrentTimeMeasurementViewer();
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
										TObjectHistoryRecords _HistoryRecords = _ObjectModel.History_GetRecords(DayDate,DaysCount, context);
										//.
										TSensorMeasurementDescriptor[] _SensorMeasurements = _ObjectModel.Sensors_GetMeasurements(DayDate, DayDate+DaysCount, GeographDataServerAddress,GeographDataServerPort, TObjectModelHistoryPanel.this, Canceller);
										//.
										_History = new THistory(_ObjectModel.ObjectDatumID(), _HistoryRecords, _SensorMeasurements);
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
				if (History.Records.ObjectModelRecords.size() > 0) {
					int Width = TimeIntervalSlider.Width;
					if (Width == 0)
						Width = 1024;
					double TimeResolution = (History.EndTimestamp-History.BeginTimestamp)/Width;
					if (TimeResolution == 0.0)
						TimeResolution = 1.0/1024;
			        TimeIntervalSlider.Setup(TObjectModelHistoryPanel.this, History.Records_CentralTimestamp(), TimeResolution, History.BeginTimestamp,History.EndTimestamp, History.TimeIntervalSliderTimeMarks,History.TimeIntervalSliderTimeIntervalMarks, new TTimeIntervalSlider.TOnTimeSelectedHandler() {

			        	@Override
			        	public void DoOnTimeSelected(double Time) {
			        		int ItemIndex = History.Records.BusinessModelRecords_GetNearestItemToTimestamp(Time);
			        		if (ItemIndex >= 0) {
			        			lvBusinessModelRecords.setItemChecked(ItemIndex, true);
			        			lvBusinessModelRecords.setSelection(ItemIndex);
			        			//.
			        			lvBusinessModelRecords_SetSelectedItem(ItemIndex, false);
			        		}
			        	}
			        });
				}
				//.
				lvBusinessModelRecords_Adapter = new ArrayAdapter<String>(TObjectModelHistoryPanel.this, android.R.layout.simple_list_item_1, History.BusinessModelRecords) {

			        @Override
			        public View getView(int position, View convertView, ViewGroup parent) {
			            TextView view = (TextView) super.getView(position, convertView, parent);
			            if (view != null) {
			                view.setTextSize(TypedValue.COMPLEX_UNIT_DIP,16); 
			                view.setText(History.BusinessModelRecords[position]);
		                    if (lvBusinessModelRecords_SelectedIndex == position) 
		                        view.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
		                    else {
		                    	int ItemColor = History.Records.BusinessModelRecords.get(position).GetSeverityColor();
		                    	if (ItemColor == Color.TRANSPARENT)
		                    		ItemColor = getContext().getResources().getColor(android.R.color.white);
		                        view.setBackgroundColor(ItemColor);
		                    }
			                view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, (int)(view.getTextSize()*1.5)));
			            }
			            return view;
			        }
			    };
				lvBusinessModelRecords.setAdapter(lvBusinessModelRecords_Adapter);
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				//.
				finish();
			}
		};
		Processing.Start();
		//.
		flExists = true;
    }
    
    @Override
    protected void onDestroy() {
    	flExists = false;
    	//.
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
    
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_SHOWVIDEOMEASUREMENT:
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					double MeasurementCurrentPosition = extras.getDouble("MeasurementCurrentPosition");
					TimeIntervalSlider.SetCurrentTime(MeasurementCurrentPosition, true);
				}
			}
			break; // . >
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void lvBusinessModelRecords_SetSelectedItem(int Position, boolean flNotify) {
    	lvBusinessModelRecords_SelectedIndex = Position;
    	if (flNotify)
    		lvBusinessModelRecords_Adapter.notifyDataSetChanged();
	}
	
    private void ShowCurrentTimeInReflector() {
    	TGeoLocationRecord GeoLocationRecord = History.GetNearestGeoLocationRecord(TimeIntervalSlider.CurrentTime, true);
    	if (GeoLocationRecord != null) {
    		TimeIntervalSlider.SetCurrentTime(GeoLocationRecord.Timestamp, true);
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
    
    private double MeasurementSpacing = (1.0/(24.0*3600.0))*5; //. seconds
    private double MeasurementSkipInterval = (1.0/(24.0*3600.0))*1; //. seconds
    
    private void ShowCurrentTimeMeasurementViewer() {
    	TSensorMeasurementDescriptor _Measurement = History.SensorMeasurements.GetMeasurementByTimestamp(TimeIntervalSlider.CurrentTime);
    	if (_Measurement == null) 
    		return; //. ->
		double DistanceToFinish = (_Measurement.FinishTimestamp-TimeIntervalSlider.CurrentTime);
		if (DistanceToFinish < MeasurementSkipInterval) {
	    	_Measurement = History.SensorMeasurements.GetNearestMeasurementToTimestamp(TimeIntervalSlider.CurrentTime, _Measurement);
	    	if (_Measurement == null) 
	    		return; //. ->
			double DistanceToStart = (_Measurement.StartTimestamp-TimeIntervalSlider.CurrentTime);
			if (DistanceToStart > MeasurementSpacing)
	    		return; //. ->
			TimeIntervalSlider.SetCurrentTime(_Measurement.StartTimestamp, true);
		}
		final TSensorMeasurementDescriptor Measurement = _Measurement;
		TVideoRecorderServerArchive.TArchiveItem Item = new TArchiveItem();
		Item.ID = Measurement.ID;
		Item.StartTimestamp = Measurement.StartTimestamp;
		Item.FinishTimestamp = Measurement.FinishTimestamp;
		Item.Location = Measurement.Location;
		Item.Position = (TimeIntervalSlider.CurrentTime-Measurement.StartTimestamp);
		//.
		TVideoRecorderServerArchive.OpenItem(Item, REQUEST_SHOWVIDEOMEASUREMENT, History.BeginTimestamp,History.EndTimestamp, Object, GeographDataServerAddress,GeographDataServerPort, this, new TVideoRecorderServerArchive.TOnArchiveItemsListUpdater() {

			@Override
			public void DoOnItemsListUpdated(TArchiveItem[] Items) {
				int Cnt = Items.length;
				for (int I = 0; I < Cnt; I++) 
					if (Math.abs(Double.parseDouble(Items[I].ID)-Double.parseDouble(Measurement.ID)) < 1.0/(24.0*3600.0)) {
						Measurement.Location = Items[I].Location;
						return; //. ->
					}
			}
		});
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
					final TShowGeoLocationRecordParams ShowGeoLocationRecordParams = (TShowGeoLocationRecordParams)msg.obj;
					//.
					final TReflector Reflector = TReflector.GetReflector(ShowGeoLocationRecordParams.ReflectorID);
					if (Reflector != null) {
						TAsyncProcessing Showing = new TAsyncProcessing() {

							private TXYCoord LocationXY;
							private byte[] TrackData;
							
							@Override
							public void Process() throws Exception {
								LocationXY = Reflector.ConvertGeoCoordinatesToXY(History.ObjectGeoDatumID, ShowGeoLocationRecordParams.GeoLocationRecord.Latitude,ShowGeoLocationRecordParams.GeoLocationRecord.Longitude,ShowGeoLocationRecordParams.GeoLocationRecord.Altitude);
					    		//. create the object track for the date
					    		ArrayList<TGeoLocationRecord> TrackFixes = new ArrayList<TGeoLocationRecord>(1024);
					    		int Cnt = History.Records.ObjectModelRecords.size();
					    		for (int I = 0; I < Cnt; I++) {
					    			THistoryRecord Record = History.Records.ObjectModelRecords.get(I); 
					    			if (Record instanceof TGeoLocationRecord) {
					    				TGeoLocationRecord GeoLocationRecord = (TGeoLocationRecord)Record;
					    				if (GeoLocationRecord.IsAvailable())
					    					TrackFixes.add(GeoLocationRecord);
					    			}
					    		}
					    		Cnt = TrackFixes.size();
					    		TCoGeoMonitorObjectTrack Track = new TCoGeoMonitorObjectTrack(Color.RED);
					    		Track.NodesCount = Cnt;
					    		Track.Nodes = new double[Track.NodesCount*3];
					    		int Idx = 0;
					    		for (int I = 0; I < Track.NodesCount; I++) {
					    			TGeoLocationRecord TrackFix = TrackFixes.get(I); 
									TXYCoord NodeXY = Reflector.ConvertGeoCoordinatesToXY(History.ObjectGeoDatumID, TrackFix.Latitude,TrackFix.Longitude,TrackFix.Altitude);
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

							@Override
							public void DoOnCompleted() throws Exception {
								if (!flExists)
									return; //. ->
								//.
								if (LocationXY != null)
									Reflector.MoveReflectionWindow(LocationXY);
								if (TrackData != null)
									Reflector.ObjectTracks_AddTrack(TrackData);
							}
							
							@Override
							public void DoOnException(Exception E) {
								Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(),	Toast.LENGTH_LONG).show();
							}
						};
						Showing.Start();
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