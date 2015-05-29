package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TUserActivitiesComponentListComponent;
import com.geoscope.GeoEye.TUserPanel;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity.TComponent;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity.TComponents;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObjectTrack;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TGeoLocationRecord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.THistoryRecord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TObjectModel.TObjectHistoryRecords;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.TMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurementsArchive;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerMyPlayerComponent;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementModel.TTypeInfo;

@SuppressLint("HandlerLeak")
public class TObjectModelHistoryPanel extends Activity {

	public static final int UserActivitiesComponentList_SetPositionDelay = 100; //. ms
	//.
	public static final int ObjectTrackViewer_SetPositionDelay = 500; //. ms
	//.
	public static final int MeasurementProcessor_SetPositionDelay 		 = 500; //. ms
	public static final int MeasurementProcessor_MeasurementOpeningDelay = 500; //. ms
	
	public static class TTimeIntervalSlider extends SurfaceView implements OnTouchListener {
		
		public static class TOnTimeChangeHandler {
			
			public void DoOnTimeChanging(double Time, boolean flChanging, boolean flDelayAllowed) {
			}

			public void DoOnTimeChanged(double Time) {
				DoOnTimeChanging(Time, false, false);
			}
		}
		
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
	    private ArrayList<TTimeIntervalSliderTimeMark> TimeMarks 	= null;
	    private ArrayList<TTimeIntervalSliderTimeMark> TimeMarks1 	= null;
	    //.
	    private ArrayList<TTimeIntervalSliderTimeIntervalMark> TimeIntervalMarks;
	    private ArrayList<TTimeIntervalSliderTimeMarkInterval> TimeMarkIntervals;
	    private TOnTimeChangeHandler OnTimeChangeHandler;
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

		public void Setup(TObjectModelHistoryPanel pHistoryPanel, double pCurrentTime, double pTimeResolution, double pTimeIntervalBegin, double pTimeIntervalEnd, ArrayList<TTimeIntervalSliderTimeMark> pTimeMarks, ArrayList<TTimeIntervalSliderTimeIntervalMark> pTimeIntervalMarks, TOnTimeChangeHandler pDoOnTimeChangeHandler) {
			CurrentTime = pCurrentTime;
			TimeResolution = pTimeResolution;
			TimeIntervalBegin = pTimeIntervalBegin;
			TimeIntervalEnd = pTimeIntervalEnd;
			TimeMarks = pTimeMarks;
			TimeIntervalMarks = pTimeIntervalMarks;
			TimeMarkIntervals = null;
			OnTimeChangeHandler = pDoOnTimeChangeHandler;
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
		    paint.setAlpha(255);
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
			double _TimeIntervalBegin = OleDate.UTCToLocalTime(TimeIntervalBegin);
			if (_TimeIntervalBegin >= IntervalBegin) 
				TIB = _TimeIntervalBegin;
			else
				TIB = IntervalBegin;
			double _TimeIntervalEnd = OleDate.UTCToLocalTime(TimeIntervalEnd);
			if (_TimeIntervalEnd <= IntervalEnd) 
				TIE = _TimeIntervalEnd;
			else 
				TIE = IntervalEnd;
			paint.setColor(TimeIntervalColor);
		    paint.setAlpha(255);
			canvas.drawRect((float)(Mid+(TIB-_CurrentTime)/TimeResolution),0.0F,(float)(Mid+(TIE-_CurrentTime)/TimeResolution),Height, paint);
			double X,Y,L;
			double MinSpacing = 8*metrics.density;
			paint.setTextSize(12*metrics.density);
			//. draw selected interval
			if (SelectedInterval.Duration != 0.0) {
				Y = 0.0F;
				X = (Mid+(OleDate.UTCToLocalTime(SelectedInterval.Time)-_CurrentTime)/TimeResolution);
				L = (SelectedInterval.Duration/TimeResolution);
				//.
				paint.setColor(Color.RED);
			    paint.setAlpha(255);
				canvas.drawRect((float)X,(float)Y,(float)(X+L),Height, paint);
				//.
				String S = OleDate.Format("HH:mm:ss",OleDate.UTCToLocalTime(SelectedInterval.Time)); 
				paint.setColor(SelectedIntervalTimeMarkerColor);
				canvas.drawText(S, (float)(X+3.0),paint.getTextSize(), paint);
				S = OleDate.Format("HH:mm:ss",OleDate.UTCToLocalTime(SelectedInterval.Time+SelectedInterval.Duration))+" ("+OleDate.Format("HH:mm",SelectedInterval.Duration)+")"; 
				canvas.drawText(S, (float)((X+L)+3.0),2.0F*paint.getTextSize(), paint);
			}
			//. draw TimeIntervalMarks
			int Cnt = 0;
			if (TimeIntervalMarks != null)
				Cnt = TimeIntervalMarks.size();
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
				Y = (Height*(17.0/32));
				double Y1 = (Height*(27.0/32))-1;
				for (int I = 0; I < Cnt; I++) {
					TTimeIntervalSliderTimeIntervalMark Item = TimeIntervalMarks.get(I); 
				    X = (Mid+(OleDate.UTCToLocalTime(Item.Time)-_CurrentTime)/TimeResolution);
				    L = (Item.Duration/TimeResolution);
				    RectF Rect = new RectF((float)X,(float)Y, (float)(X+L),(float)Y1);
					canvas.save();
					try {
						canvas.clipRect(Rect);
						//.
						paint.setStyle(Paint.Style.FILL);
					    paint.setColor(Item.Color);
					    paint.setAlpha(96);
						canvas.drawRect(Rect, paint);
						//.
						paint.setStyle(Paint.Style.STROKE);
						paint.setStrokeWidth(2.0F*metrics.density);
					    paint.setColor(Color.BLACK);
					    paint.setAlpha(255);
						canvas.drawRect(Rect, paint);
						//.
						paint.setStyle(Paint.Style.FILL);
						paint.setTextSize(Rect.height()*0.50F);
						float TW = paint.measureText(Item.Text);
						if (TW < Rect.width()) {
							float TH = paint.getTextSize();
							float TX = Rect.left+((Rect.right-Rect.left)-TW)/2.0F;
							float TY = Rect.top+((Rect.bottom-Rect.top)+TH)/2.0F;
						    paint.setAlpha(196);
							canvas.drawText(Item.Text, TX,TY, paint);
						}
					}
					finally {
						canvas.restore();
					}
				}
			}
			//. draw TimeMarks
			Cnt = 0;
			if (TimeMarks != null)
				Cnt = TimeMarks.size();
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
			    paint.setAlpha(255);
				Y = (Height*(14.0/16));
				double Y1 = (Height*(15.0/16))-1;
				double MarkWidth = 3.0*metrics.density;
				for (int I = 0; I < Cnt; I++) {
					TTimeIntervalSliderTimeMark Mark = TimeMarks.get(I);
				    X = (Mid+(OleDate.UTCToLocalTime(Mark.Time)-_CurrentTime)/TimeResolution);
				    if ((0 < X) && (X < Width)) {
						paint.setColor(Mark.Color);
						canvas.drawRect((float)X,(float)Y, (float)(X+MarkWidth),(float)Y1, paint);
				    }
				}
			}
			//. draw TimeMarks1
			Cnt = 0;
			if (TimeMarks1 != null)
				Cnt = TimeMarks1.size();
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
			    paint.setAlpha(255);
				Y = (Height*(13.0/16));
				double Y1 = (Height*(14.0/16))-1;
				double Yc = (Y+Y1)/2.0;
				double R = (Y1-Y)/2.0;
				for (int I = 0; I < Cnt; I++) {
					TTimeIntervalSliderTimeMark Mark = TimeMarks1.get(I);
				    X = (Mid+(OleDate.UTCToLocalTime(Mark.Time)-_CurrentTime)/TimeResolution);
				    if ((0 < X) && (X < Width)) {
						paint.setColor(Mark.Color);
						canvas.drawCircle((float)X,(float)Yc, (float)R, paint);
				    }
				}
			}
			//. draw Day marks
			paint.setStrokeWidth(1.0F*metrics.density);
			paint.setColor(TimeMarkerColor);
		    paint.setAlpha(255);
			double DY = (Height*(1.0/4));
			double DLLP = -Double.MAX_VALUE;
			double Day = Math.floor(IntervalBegin/DayDelta)*DayDelta;
			while (Day < IntervalEnd) {
				  X = (Mid+(Day-_CurrentTime)/TimeResolution);
				  if ((0 < X) && (X < Width)) {
					  canvas.drawLine((float)X,Height, (float)X,(float)DY, paint);
					  String S = OleDate.Format("dd.MM",Day);
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
						  X = (Mid+(Hour-_CurrentTime)/TimeResolution);
						  if ((0 < X) && (X < Width)) {
							  canvas.drawLine((float)X,Height, (float)X,(float)HY, paint);
							  String S = OleDate.Format("HH",Hour);
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
							  X = (Mid+(M30-_CurrentTime)/TimeResolution);
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
								  X = (Mid+(M15-_CurrentTime)/TimeResolution);
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
		    paint.setAlpha(255);
			canvas.drawLine((float)Mid,0.0F, (float)Mid,Height, paint);
			paint.setStrokeWidth(1.0F*metrics.density);
			paint.setColor(CenterMarkerColorHigh);
			canvas.drawLine((float)Mid,0.0F, (float)Mid,Height, paint);
			paint.setColor(CenterMarkerColorHigh);
			String S = OleDate.Format("yyyy/MM/dd HH:mm:ss",_CurrentTime); 
			paint.setTextSize(18*metrics.density);
			canvas.drawText(S, (float)Mid+3.0F*metrics.density,0.0F+paint.getTextSize(), paint);
			//.
			Cnt = 0;
			if (TimeMarkIntervals != null)
				Cnt = TimeMarkIntervals.size();
			if (Cnt > 0) {
				paint.setStrokeWidth(0.0F);
				Y = (Height*(15.0/16));
				for (int I = 0; I < Cnt; I++) {
					TTimeIntervalSliderTimeMarkInterval Mark = TimeMarkIntervals.get(I); 
				    X = (Mid+(OleDate.UTCToLocalTime(Mark.Time)-_CurrentTime)/TimeResolution);
				    L = (Mark.Duration/TimeResolution);
					paint.setColor(Mark.Color);
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

		public void ValidateCurrentTime(boolean flEventActionDelayAllowed) {
			SetCurrentTime(CurrentTime, false, true, flEventActionDelayAllowed);
		}
		
		public void ValidateCurrentTime() {
			ValidateCurrentTime(true);
		}
		
		public void Validate() {
			ValidateCurrentTime();
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
			//.
			public ArrayList<TSensorMeasurementDescriptor> 	MeasurementTypes;
			public boolean[] 								MeasurementTypes_Enabled;
			//.
			public TSensorMeasurementDescriptor[] 				EnabledMeasurements;
			
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
				//.
				MeasurementTypes = GetMeasurementTypes();
				MeasurementTypes_Enabled = new boolean[MeasurementTypes.size()];
				Cnt = MeasurementTypes.size();
				for (int I = 0; I < Cnt; I++)
					MeasurementTypes_Enabled[I] = true;
				//.
				EnabledMeasurements = Measurements;
			}

			private ArrayList<TSensorMeasurementDescriptor> GetMeasurementTypes() {
				ArrayList<TSensorMeasurementDescriptor> Result = new ArrayList<TSensorMeasurementDescriptor>();
				int ICnt = Measurements.length;
				for (int I = 0; I < ICnt; I++) {
					TSensorMeasurementDescriptor Measurement = Measurements[I];
					boolean flFound = false;
					int JCnt = Result.size();
					for (int J = 0; J < JCnt; J++) 
						if (Result.get(J).Model.TypeID.equals(Measurement.Model.TypeID)) {
							flFound = true;
							break; //. >
						}
					if (!flFound) 
						Result.add(Measurement);
				}
				return Result;
			}
			
			public void EnabledMeasurements_Update() {
				int ICnt = Measurements.length;
				ArrayList<TSensorMeasurementDescriptor> _Result = new ArrayList<TSensorMeasurementDescriptor>(ICnt);
				for (int I = 0; I < ICnt; I++) {
					TSensorMeasurementDescriptor Measurement = Measurements[I]; 
					//.
					int JCnt = MeasurementTypes.size();
					for (int J = 0; J < JCnt; J++) {
						TSensorMeasurementDescriptor _Measurement = MeasurementTypes.get(J);
						if (_Measurement.IsTypeOf(Measurement.TypeID()) && MeasurementTypes_Enabled[J]) {
							_Result.add(Measurement);
							break; //. >
						}
					}
				}
				ICnt = _Result.size();
				EnabledMeasurements = new TSensorMeasurementDescriptor[ICnt]; 
				for (int I = 0; I < ICnt; I++) 
					EnabledMeasurements[I] = _Result.get(I);
			}
			
			public double Measurements_CentralTimestamp() {
				return (BeginTimestamp+EndTimestamp)/2.0;
			}
			
			public TSensorMeasurementDescriptor GetMeasurementByTimestamp(double Timestamp) {
				int Cnt = EnabledMeasurements.length;
				for (int I = 0; I < Cnt; I++) {
					TSensorMeasurementDescriptor Measurement = EnabledMeasurements[I];
					if ((Measurement.StartTimestamp <= Timestamp) && (Timestamp < Measurement.FinishTimestamp))
						return Measurement; //. ->
				}
				return null;
			}

			public TSensorMeasurementDescriptor GetTypedMeasurementByTimestamp(String TypeID, double Timestamp) {
				int Cnt = EnabledMeasurements.length;
				for (int I = 0; I < Cnt; I++) {
					TSensorMeasurementDescriptor Measurement = EnabledMeasurements[I];
					if (((Measurement.StartTimestamp <= Timestamp) && (Timestamp < Measurement.FinishTimestamp)) && Measurement.IsTypeOf(TypeID))
						return Measurement; //. ->
				}
				return null;
			}

			public TSensorMeasurementDescriptor GetNearestMeasurementToTimestamp(double Timestamp, TSensorMeasurementDescriptor ExceptMeasurement) {
				TSensorMeasurementDescriptor Result = null;
				int Cnt = EnabledMeasurements.length;
				double MinDistance = Double.MAX_VALUE;
				for (int I = 0; I < Cnt; I++) {
					TSensorMeasurementDescriptor Measurement = EnabledMeasurements[I];
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

		private Context context;
		//.
		public int ObjectGeoDatumID;
		//.
		public TObjectHistoryRecords Records;
		//.
		public TSensorMeasurements			SensorMeasurements;
		//.
		public double						BeginTimestamp;
		public double						EndTimestamp;
		//.
		public ArrayList<TTimeIntervalSlider.TTimeIntervalSliderTimeMark>			TimeIntervalSliderTimeMarks = new ArrayList<TTimeIntervalSlider.TTimeIntervalSliderTimeMark>();
		public ArrayList<TTimeIntervalSlider.TTimeIntervalSliderTimeIntervalMark>	TimeIntervalSliderTimeIntervalMarks = new ArrayList<TTimeIntervalSlider.TTimeIntervalSliderTimeIntervalMark>();
		//.
		public String[] BusinessModelRecords; 
		
		public THistory(Context pcontext, int pObjectGeoDatumID, TObjectHistoryRecords pRecords, TSensorMeasurementDescriptor[] pSensorMeasurements) {
			context = pcontext;
			ObjectGeoDatumID = pObjectGeoDatumID;
			Records = pRecords;
			SensorMeasurements = new TSensorMeasurements(pSensorMeasurements);
			//.
			Update();
		}
		
		private void Update() {
			BeginTimestamp = Double.MAX_VALUE;
			EndTimestamp = -Double.MAX_VALUE;
			//. object model records
			int Cnt = Records.ObjectModelRecords.size();
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.ObjectModelRecords.get(I);
				if (Record.Timestamp < BeginTimestamp)
					BeginTimestamp = Record.Timestamp; 
				if (Record.Timestamp > EndTimestamp)
					EndTimestamp = Record.Timestamp;
			}
			//. business model records
			Cnt = Records.BusinessModelRecords.size();
			BusinessModelRecords = new String[Cnt];
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.BusinessModelRecords.get(Cnt-I-1); 
				if (Record.Timestamp < BeginTimestamp)
					BeginTimestamp = Record.Timestamp; 
				if (Record.Timestamp > EndTimestamp)
					EndTimestamp = Record.Timestamp;
				//.
				String S = OleDate.Format("yyyy/MM/dd HH:mm:ss",OleDate.UTCToLocalTime(Record.Timestamp))+": "+Record.GetString(1/*only message*/);
				BusinessModelRecords[I] = S;
			}
			//. measurements
			if (SensorMeasurements.BeginTimestamp < BeginTimestamp)
				BeginTimestamp = SensorMeasurements.BeginTimestamp; 
			if (SensorMeasurements.EndTimestamp > EndTimestamp)
				EndTimestamp = SensorMeasurements.EndTimestamp;
			//.
			TimeIntervalSlider_Update();
		}

		public void TimeIntervalSlider_Update() {
			int Cnt = Records.ObjectModelRecords.size();
			TimeIntervalSliderTimeMarks.clear();
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
				TimeIntervalSliderTimeMarks.add(new TTimeIntervalSlider.TTimeIntervalSliderTimeMark(Record.Timestamp,MarkColor));
			}
			//.
			Cnt = SensorMeasurements.EnabledMeasurements.length;
			TimeIntervalSliderTimeIntervalMarks.clear(); 
			for (int I = 0; I < Cnt; I++) {
				TSensorMeasurementDescriptor Measurement = SensorMeasurements.EnabledMeasurements[I];
				//.
				TTypeInfo TypeInfo = Measurement.TypeInfo(context);
				int TypeColor;
				String TypeName;
				if (TypeInfo != null) {
					TypeColor = TypeInfo.TypeColor;
					TypeName = TypeInfo.TypeName;
				}
				else {
					TypeColor = Color.BLUE;
					TypeName = "";
				}
				TimeIntervalSliderTimeIntervalMarks.add(new TTimeIntervalSlider.TTimeIntervalSliderTimeIntervalMark(Measurement.StartTimestamp,Measurement.FinishTimestamp-Measurement.StartTimestamp, TypeColor, TypeName));
			}
		}
		
		public double CentralTimestamp() {
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

		public TGeoLocationRecord GetCalculatedGeoLocationRecord(double Timestamp) {
			TGeoLocationRecord LR = null;
			TGeoLocationRecord RR = null;
			int Cnt = Records.ObjectModelRecords.size();
			double LeftMinDistance = -Double.MAX_VALUE;
			double RightMinDistance = Double.MAX_VALUE;
			for (int I = 0; I < Cnt; I++) {
				THistoryRecord Record = Records.ObjectModelRecords.get(I);
				if (Record instanceof TGeoLocationRecord) {
					TGeoLocationRecord GeoLocationRecord = (TGeoLocationRecord)Record;
					if (GeoLocationRecord.IsAvailable()) {
						double Distance = (Record.Timestamp-Timestamp); 
						if ((Distance < 0) && (Distance > LeftMinDistance)) {
							LeftMinDistance = Distance;
							LR = GeoLocationRecord;
						}
						if ((Distance >= 0) && (Distance < RightMinDistance)) {
							RightMinDistance = Distance;
							RR = GeoLocationRecord;
						}
					}
				}
			}
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
					TGeoLocationRecord Result = new TGeoLocationRecord(
							Timestamp,
							0,
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
	}
	
	private static class TMeasurementProcessorItem {
		
		public boolean 							flEnabled = true;
		public TMeasurementProcessor 			Processor = null;
		public TSensorMeasurementDescriptor		CurrentMeasurement = null;
		public TAsyncProcessing 				CurrentMeasurementOpening = null; 
		public boolean 							flUpdating = false; 
	}
	
	public static final int REQUEST_SHOWVIDEOMEASUREMENT = 1;
	
	private boolean flExists = false;
	//.
	private boolean flBigScreen = false;
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
	private long	UserID;
	@SuppressWarnings("unused")
	private String	UserPassword;
	//.
	private THistory History;
	//.
	private TTimeIntervalSlider TimeIntervalSlider;
	//.
	private Button btnFilterMeasurementsByType;
	//.
	private Button btnShowCurrentTimeInReflector;
	private Button btnShowCurrentTimeMeasurementViewer;
	//.
	private ListView 		lvBusinessModelRecords;
	ArrayAdapter<String> 	lvBusinessModelRecords_Adapter;
	private int				lvBusinessModelRecords_SelectedIndex = -1;
	private boolean 		lvBusinessModelRecords_flUpdating = false;
	//.
	private LinearLayout llBigScreenControls;
	private CheckBox cbShowUserActivitiesComponentList;
	private CheckBox cbShowReflector;
	private CheckBox cbShowMeasurementViewer;
	private CheckBox cbTimeAnimation;
	//.
	private TUserActivitiesComponentListComponent	UserActivitiesComponentList = null;
	private LinearLayout 							UserActivitiesComponentList_Layout = null;
	private TAsyncProcessing 						UserActivitiesComponentListPositioning = null;	
	private boolean 								UserActivitiesComponentList_flUpdating = false;
	//.
	private RelativeLayout 		ObjectTrackViewer_Layout = null;
	private TReflectorComponent ObjectTrackViewer = null;
	private	TGeoLocationRecord	ObjectTrackViewer_GeoLocationRecord = null; 
	private boolean 			ObjectTrackViewer_flTrackIsSet = false;
	private TAsyncProcessing 	ObjectTrackViewerShowing = null;	
	//.
	private LinearLayout 							MeasurementProcessors_Layout = null;
	private ArrayList<TMeasurementProcessorItem>	MeasurementProcessors = null;
	private boolean 								MeasurementProcessors_flEnabled = false;
	private boolean 								MeasurementProcessors_flSetup = false;
	private int										MeasurementProcessors_VisibleCounter = 0;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
    	flBigScreen = true; //. (getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
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
        	UserID = extras.getLong("UserID");
        	UserPassword = extras.getString("UserPassword");
        }
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //.
        setContentView(R.layout.objectmodel_history_panel);
        //.
        TimeIntervalSlider = (TTimeIntervalSlider)findViewById(R.id.svTimeIntervalSlider);
        TimeIntervalSlider.Initialize(this);
        //.
        btnFilterMeasurementsByType = (Button)findViewById(R.id.btnFilterMeasurementsByType);
        btnFilterMeasurementsByType.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				try {
					Measurements_FilterByType();
				} catch (Exception E) {
					Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        lvBusinessModelRecords = (ListView)findViewById(R.id.lvBusinessModelRecords);
        lvBusinessModelRecords.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
            	lvBusinessModelRecords_SetSelectedItem(position, true);
            	//.
            	lvBusinessModelRecords_flUpdating = true;
            	try {
                	TimeIntervalSlider.SetCurrentTime(History.Records.BusinessModelRecords.get(History.BusinessModelRecords.length-position-1).Timestamp, false, true, true);
            	}
            	finally {
            		lvBusinessModelRecords_flUpdating = false;
            	}
            }
        });        
        //.
        btnFilterMeasurementsByType = (Button)findViewById(R.id.btnFilterMeasurementsByType);
        btnFilterMeasurementsByType.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				try {
					Measurements_FilterByType();
				} catch (Exception E) {
					Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        btnShowCurrentTimeInReflector = (Button)findViewById(R.id.btnShowCurrentTimeInReflector);
        btnShowCurrentTimeInReflector.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				try {
					OpenCurrentTimeInReflector();
				} catch (Exception E) {
					Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        btnShowCurrentTimeMeasurementViewer = (Button)findViewById(R.id.btnShowCurrentTimeMeasurementViewer);
        btnShowCurrentTimeMeasurementViewer.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				OpenCurrentTimeAVMeasurementInProcessor();
            }
        });
        //.
        llBigScreenControls = (LinearLayout)findViewById(R.id.llBigScreenControls);
        llBigScreenControls.setVisibility(flBigScreen ? View.VISIBLE : View.GONE);
        //.
        UserActivitiesComponentList_Layout = (LinearLayout)findViewById(R.id.UserActivitiesComponentListLayout);
        //.
        ObjectTrackViewer_Layout = (RelativeLayout)findViewById(R.id.ReflectorLayout);
        if (flBigScreen)
        	try {
    			ObjectTrackViewer_Initialize();
    		} catch (Exception E) {
    			Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    			//.
    			finish();
    			//.
    			return; //. ->
    		}
        //.
        MeasurementProcessors_Layout = (LinearLayout)findViewById(R.id.MeasurementProcessorsLayout);
        //.
        cbShowUserActivitiesComponentList = (CheckBox)findViewById(R.id.cbShowUserActivitiesComponentList);
        cbShowUserActivitiesComponentList.setOnClickListener(new OnClickListener(){
        	
            @Override
            public void onClick(View v) {
            	//. start component
            	if (cbShowUserActivitiesComponentList.isChecked()) {
            		if (!UserActivitiesComponentList.flStarted)
            			UserActivitiesComponentList.Start();
            	}
            	//.
            	if (cbShowUserActivitiesComponentList.isChecked())
            		UserActivitiesComponentList.Show();
            	else
            		UserActivitiesComponentList.Hide();
                //. validation
            	TimeIntervalSlider.ValidateCurrentTime(true);
            }
        });        
        //.
        cbShowReflector = (CheckBox)findViewById(R.id.cbShowReflector);
        cbShowReflector.setOnClickListener(new OnClickListener(){
        	
            @Override
            public void onClick(View v) {
            	if (cbShowReflector.isChecked())
            		ObjectTrackViewer.Show();
            	else
            		ObjectTrackViewer.Hide();
                //. validation
            	TimeIntervalSlider.ValidateCurrentTime(true);
            }
        });        
        //.
        cbShowMeasurementViewer = (CheckBox)findViewById(R.id.cbShowMeasurementViewer);
        cbShowMeasurementViewer.setOnClickListener(new OnClickListener(){
        	
            @Override
            public void onClick(View v) {
            	MeasurementProcessors_flEnabled = cbShowMeasurementViewer.isChecked(); 
            	if (MeasurementProcessors_flEnabled) {
            		Animation_SetEnabled(true);
            	}
            	else {
            		MeasurementProcessors_Pause();
                	//.
            		Animation_SetEnabled(false);
            	}
            	//. validation
            	TimeIntervalSlider.ValidateCurrentTime(true);
            }
        });        
        //.
        cbTimeAnimation = (CheckBox)findViewById(R.id.cbTimeAnimation);
        cbTimeAnimation.setOnClickListener(new OnClickListener(){
        	
            @Override
            public void onClick(View v) {
            	if (Animation_IsAllowedAtTheMoment())
                	TimeIntervalSlider.ValidateCurrentTime(true);
            	else
            		MeasurementProcessors_Pause();
            }
        });        
        //.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SLoading)) {
			
			private TObjectModel _ObjectModel = null; 
			private THistory _History = null;
			
			@Override
			public boolean ProcessIsIndeterminate() {
				return false;
			}
			
			@Override
			public void Process() throws Exception {
				try {
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
											DoOnProgress(10);
											//.
						    				Canceller.Check();
						    				//.
											if (ObjectSchemaData != null)
												_ObjectModel.ObjectSchema.RootComponent.FromByteArray(ObjectSchemaData,new TIndex());
											//.
											byte[] ObjectDeviceSchemaData = GSOC.Component_ReadAllCUAC(new int[] {2/*device side*/});
											//.
											DoOnProgress(20);
											//.
						    				Canceller.Check();
						    				//.
											if (ObjectDeviceSchemaData != null)
												_ObjectModel.ObjectDeviceSchema.RootComponent.FromByteArray(ObjectDeviceSchemaData,new TIndex());
											//.
											_ObjectModel.SetObjectController(GSOC, false);
											TObjectHistoryRecords _HistoryRecords = _ObjectModel.History_GetRecords(DayDate,DaysCount, context);
											//.
											DoOnProgress(60);
											//.
						    				Canceller.Check();
											//.
											TSensorMeasurementDescriptor[] _SensorMeasurements = _ObjectModel.Sensors_Measurements_GetList(DayDate, DayDate+DaysCount, GeographDataServerAddress,GeographDataServerPort, TObjectModelHistoryPanel.this, Canceller);
											//.
											DoOnProgress(80);
											//.
						    				Canceller.Check();
											//.
											_History = new THistory(TObjectModelHistoryPanel.this, _ObjectModel.ObjectDatumID(), _HistoryRecords, _SensorMeasurements);
											//.
											DoOnProgress(100);
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
				catch (Exception E) {
					if (_ObjectModel != null)
						_ObjectModel.Destroy();
					throw E; //. =>
				}
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				if (!flExists)
					return; //. ->
				//.
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
			        TimeIntervalSlider.Setup(TObjectModelHistoryPanel.this, History.EndTimestamp, TimeResolution, History.BeginTimestamp,History.EndTimestamp, History.TimeIntervalSliderTimeMarks,History.TimeIntervalSliderTimeIntervalMarks, new TTimeIntervalSlider.TOnTimeChangeHandler() {

			        	@Override
			        	public void DoOnTimeChanging(double Time, boolean flChanging, boolean flDelayAllowed) {
			        		if (!lvBusinessModelRecords_flUpdating) {
				        		int ItemIndex = History.Records.BusinessModelRecords_GetNearestItemToTimestamp(Time);
				        		if (ItemIndex >= 0) {
				        			ItemIndex = History.BusinessModelRecords.length-ItemIndex-1;
				        			//.
				        			lvBusinessModelRecords.setItemChecked(ItemIndex, true);
				        			lvBusinessModelRecords.setSelection(ItemIndex);
				        			//.
				        			lvBusinessModelRecords_SetSelectedItem(ItemIndex, false);
				        		}
			        		}
			        		//.
			        		if (cbShowUserActivitiesComponentList.isChecked() && !UserActivitiesComponentList_flUpdating)
				        		try {
				        			UserActivitiesComponentList_SetCurrentTime(Time, (flDelayAllowed ? UserActivitiesComponentList_SetPositionDelay : 0));
								} catch (Exception E) {
									Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
								}
			        		//.
			        		if (cbShowReflector.isChecked())
				        		try {
									ObjectTrackViewer_SetCurrentTime(Time, (flDelayAllowed ? ObjectTrackViewer_SetPositionDelay : 0));
								} catch (Exception E) {
									Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
								}
			        		//.
							if (MeasurementProcessors_flEnabled || (MeasurementProcessors_VisibleCounter > 0)) 
								try {
									MeasurementProcessors_SetCurrentTime(Time, (flChanging || !Animation_IsAllowedAtTheMoment()), (flDelayAllowed ? MeasurementProcessor_SetPositionDelay : 0));			        		
								} catch (Exception E) {
									Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
		                    	int ItemColor = History.Records.BusinessModelRecords.get(History.BusinessModelRecords.length-position-1).GetSeverityColor();
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
				//.
		        if (flBigScreen)
		        	try {
                		MeasurementProcessors_Initialize();
                		//.
		        		if (UserActivitiesComponentList_IsAvailable())
		        			UserActivitiesComponentList_Initialize();
		        		else
		                	cbShowUserActivitiesComponentList.setEnabled(false);
		    		} catch (Exception E) {
		    			Toast.makeText(TObjectModelHistoryPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    			//.
		    			finish();
		    			//.
		    			return; //. ->
		    		}
			}
			
			@Override
			public void DoOnCancelIsOccured() {
				TObjectModelHistoryPanel.this.finish();
			}
			
			@Override
			public void DoOnException(Exception E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Toast.makeText(TObjectModelHistoryPanel.this, S, Toast.LENGTH_LONG).show();
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
		try {
	    	MeasurementProcessors_Finalize();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
		try {
			ObjectTrackViewer_Finalize();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
		try {
			UserActivitiesComponentList_Finalize();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    	//.
    	if (ObjectModel != null) {
    		ObjectModel.Destroy();
    		ObjectModel = null;
    	}
    	//.
		try {
	    	if (Object != null) {
				Object.Destroy();
	    		Object = null;
	    	}
		} catch (IOException E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
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
	protected void onStart() {
		super.onStart();
		//.
		if (ObjectTrackViewer != null)
			ObjectTrackViewer.DoOnStart();
	}

	@Override
	protected void onStop() {
		if (ObjectTrackViewer != null)
			ObjectTrackViewer.DoOnStop();
		//.
		super.onStop();
	}

    @Override
    protected void onResume() {
    	super.onResume();
		//.
		if (UserActivitiesComponentList != null)
			UserActivitiesComponentList.DoOnResume();
		//.
		if (ObjectTrackViewer != null)
			ObjectTrackViewer.DoOnResume();
		//.
		if (MeasurementProcessors == null) 
			TimeIntervalSlider.ValidateCurrentTime();
		//.
		TimeIntervalSlider.PostDraw();
    }
    
	@Override
	public void onPause() {
		if (ObjectTrackViewer != null)
			ObjectTrackViewer.DoOnPause();
		//.
		super.onPause();
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUEST_SHOWVIDEOMEASUREMENT:
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					double MeasurementCurrentPosition = extras.getDouble("MeasurementCurrentPosition");
					TimeIntervalSlider.SetCurrentTime(MeasurementCurrentPosition, false, true, false);
				}
			}
			break; // . >
			
		case TReflectorComponent.REQUEST_OPEN_USERSEARCH:
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					TGeoScopeServerUser.TUserDescriptor User = new TGeoScopeServerUser.TUserDescriptor();
					User.UserID = extras.getLong("UserID");
					User.UserIsDisabled = extras.getBoolean("UserIsDisabled");
					User.UserIsOnline = extras.getBoolean("UserIsOnline");
					User.UserName = extras.getString("UserName");
					User.UserFullName = extras.getString("UserFullName");
					User.UserContactInfo = extras.getString("UserContactInfo");
					// .
					Intent intent = new Intent(TObjectModelHistoryPanel.this, TUserPanel.class);
					int ObjectTrackViewerID = 0;
					if (ObjectTrackViewer != null)
						ObjectTrackViewerID = ObjectTrackViewer.ID;
					intent.putExtra("ComponentID", ObjectTrackViewerID);
					intent.putExtra("UserID", User.UserID);
					startActivity(intent);
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
	
	private boolean UserActivitiesComponentList_IsAvailable() {
		return ObjectModel.ObjectUserID() != 0;
	}
	
	private void UserActivitiesComponentList_Initialize() {
		TReflectorComponent Reflector = null; 
		TReflector RFL = TReflector.GetReflector();
		if (RFL != null)
			Reflector = RFL.Component;
		UserActivitiesComponentList = new TUserActivitiesComponentListComponent(this, UserActivitiesComponentList_Layout, ObjectModel.ObjectUserID(), History.BeginTimestamp,History.EndTimestamp, TUserActivitiesComponentListComponent.LIST_ROW_SIZE_SMALL_ID, Reflector, new TUserActivitiesComponentListComponent.TOnItemsLoadedHandler() {
			
			@Override
			public void DoOnItemsLoaded(TComponents ActivitiesComponents) {
				if ((ActivitiesComponents != null) && (ActivitiesComponents.Items != null)) {
					int Cnt = ActivitiesComponents.Items.length;
					ArrayList<TTimeIntervalSlider.TTimeIntervalSliderTimeMark> TimeIntervalSliderTimeMarks = new ArrayList<TTimeIntervalSlider.TTimeIntervalSliderTimeMark>(); 
					int MarkColor = Color.RED;
					for (int I = 0; I < Cnt; I++) 
						TimeIntervalSliderTimeMarks.add(new TTimeIntervalSlider.TTimeIntervalSliderTimeMark(ActivitiesComponents.Items[I].Timestamp,MarkColor));
					//.
					TimeIntervalSlider.TimeMarks1 = TimeIntervalSliderTimeMarks;
					TimeIntervalSlider.Validate();
				}
			}
		}, new TUserActivitiesComponentListComponent.TOnListItemClickHandler() {

			@Override
			public void DoOnListItemClick(TComponent Component) {
				UserActivitiesComponentList_flUpdating = true;
	    		try {
	    			double Timestamp = Component.Timestamp;
	    			//.
	    			TimeIntervalSlider.SetCurrentTime(Timestamp, false, true, false);
	    		}
	    		finally {
	    			UserActivitiesComponentList_flUpdating = false;
	    		}
			}
		});
		//.
		UserActivitiesComponentListPositioning = null;
	}
	
	private void UserActivitiesComponentList_Finalize() throws Exception {
		if (UserActivitiesComponentListPositioning != null) {
			UserActivitiesComponentListPositioning.Destroy();
			UserActivitiesComponentListPositioning = null;
		}
		if (UserActivitiesComponentList != null) {
			UserActivitiesComponentList.Destroy();
			UserActivitiesComponentList = null;
		}
	}
	
	private void UserActivitiesComponentList_SetCurrentTime(final double Timestamp, final int Delay) throws Exception {
    	if (UserActivitiesComponentList == null) 
    		return; //. ->
    	//.
		if (UserActivitiesComponentListPositioning != null)
			UserActivitiesComponentListPositioning.Cancel();
		UserActivitiesComponentListPositioning = new TAsyncProcessing() {

			@Override
			public void Process() throws Exception {
				Thread.sleep(Delay); 
			}

			@Override
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				//.
				if (UserActivitiesComponentList != null)
					UserActivitiesComponentList.ActivitiesComponents_LocateAnItemNearToTime(Timestamp);
			}
			
			@Override
			public void DoOnFinished() throws Exception {
				if (UserActivitiesComponentListPositioning == this)
					UserActivitiesComponentListPositioning = null;
			}
			
			@Override
			public void DoOnException(Exception E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Toast.makeText(TObjectModelHistoryPanel.this, S,	Toast.LENGTH_LONG).show();
			}
		};
		UserActivitiesComponentListPositioning.Start();
	}
	
	private void ObjectTrackViewer_Initialize() throws Exception {
		Intent Parameters = new Intent();
		Parameters.putExtra("Reason", TReflectorComponent.REASON_MONITORGEOLOCATION);
    	//.
		ObjectTrackViewer = new TReflectorComponent(this, ObjectTrackViewer_Layout, Parameters);
		//.
		ObjectTrackViewer_GeoLocationRecord = null; 
		ObjectTrackViewer_flTrackIsSet = false;
		ObjectTrackViewerShowing = null;	
		//.
		ObjectTrackViewer.Start();
	}
	
	private void ObjectTrackViewer_Finalize() throws Exception {
		if (ObjectTrackViewerShowing != null) {
			ObjectTrackViewerShowing.Destroy();
			ObjectTrackViewerShowing = null;
		}
    	//.
    	if (ObjectTrackViewer != null) {
			ObjectTrackViewer.Stop();
			//.
			ObjectTrackViewer.Destroy();
			//.
    		ObjectTrackViewer = null;
    	}
	}
	
	private void ObjectTrackViewer_SetCurrentTime(double Timestamp, final int Delay) throws Exception {
    	if (ObjectTrackViewer == null) 
    		return; //. ->
    	//.
    	TGeoLocationRecord GeoLocationRecord = History.GetCalculatedGeoLocationRecord(TimeIntervalSlider.CurrentTime);
    	if (GeoLocationRecord == ObjectTrackViewer_GeoLocationRecord)
    		return; //. ->
    	//.
    	ObjectTrackViewer_GeoLocationRecord = GeoLocationRecord;
    	//.
    	if (ObjectTrackViewer_GeoLocationRecord != null) 
	    	if (ObjectTrackViewer_flTrackIsSet) { 
	    		if (ObjectTrackViewerShowing != null)
	    			ObjectTrackViewerShowing.Cancel();
	    		//.
		    	if (ObjectTrackViewer_GeoLocationRecord.IsAvailable()) {
		    		if (Delay > 0) {
			    		ObjectTrackViewerShowing = new TAsyncProcessing() {

							@Override
							public void Process() throws Exception {
								Thread.sleep(Delay); 
							}

							@Override
							public void DoOnCompleted() throws Exception {
								if (Canceller.flCancel)
									return; //. ->
								//.
								if (ObjectTrackViewer != null) {
									TXYCoord LocationXY = ObjectTrackViewer.ConvertGeoCoordinatesToXY(History.ObjectGeoDatumID, ObjectTrackViewer_GeoLocationRecord.Latitude,ObjectTrackViewer_GeoLocationRecord.Longitude,ObjectTrackViewer_GeoLocationRecord.Altitude);
									//.
									if (LocationXY != null) 
										ObjectTrackViewer.MoveReflectionWindow(LocationXY);
								}
							}
							
							@Override
							public void DoOnFinished() throws Exception {
								if (ObjectTrackViewerShowing == this)
									ObjectTrackViewerShowing = null;
							}
							
							@Override
							public void DoOnException(Exception E) {
								String S = E.getMessage();
								if (S == null)
									S = E.getClass().getName();
								Toast.makeText(TObjectModelHistoryPanel.this, S,	Toast.LENGTH_LONG).show();
							}
						};
						ObjectTrackViewerShowing.Start();
		    		}
		    		else 
						if (ObjectTrackViewer != null) {
							TXYCoord LocationXY = ObjectTrackViewer.ConvertGeoCoordinatesToXY(History.ObjectGeoDatumID, ObjectTrackViewer_GeoLocationRecord.Latitude,ObjectTrackViewer_GeoLocationRecord.Longitude,ObjectTrackViewer_GeoLocationRecord.Altitude);
							//.
							if (LocationXY != null) 
								ObjectTrackViewer.MoveReflectionWindow(LocationXY);
						}
		    	}
	    	}
	    	else {
	    		ObjectTrackViewer_flTrackIsSet = true;
	    		//.
    			SendShowGeoLocationRecordMessage(0,ObjectTrackViewer_GeoLocationRecord, 2000/*delay, ms*/);
	    	}
	}
	
	private boolean[] Measurements_GetEnabledTypes() {
		return History.SensorMeasurements.MeasurementTypes_Enabled.clone();
	}
	
	private void Measurements_SetEnabledTypes(boolean[] EnabledTypes) throws Exception {
		if (EnabledTypes.length != History.SensorMeasurements.MeasurementTypes.size())
			throw new Exception("enabled types list size is differ than types list"); //. =>
		//.
		History.SensorMeasurements.MeasurementTypes_Enabled = EnabledTypes;
		//. validate measurements
		History.SensorMeasurements.EnabledMeasurements_Update();
		//.
		History.TimeIntervalSlider_Update();
		//.
		TimeIntervalSlider.ValidateCurrentTime();
	}
	
	private void Measurements_FilterByType() {
    	CharSequence[] _items = new CharSequence[History.SensorMeasurements.MeasurementTypes.size()];
    	int Cnt = History.SensorMeasurements.MeasurementTypes.size();
    	for (int I = 0; I < Cnt; I++) {
    		TSensorMeasurementDescriptor Measurement = History.SensorMeasurements.MeasurementTypes.get(I);
    		TTypeInfo TypeInfo = Measurement.TypeInfo(this);
    		if (TypeInfo != null)
    			_items[I] = TypeInfo.TypeName;
    		else
    			_items[I] = Measurement.TypeID();
    	}
    	final boolean[] Mask = Measurements_GetEnabledTypes();
    	//.
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setTitle(R.string.SFilterByType);
    	builder.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					Measurements_SetEnabledTypes(Mask);
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
					Toast.makeText(TObjectModelHistoryPanel.this, S, Toast.LENGTH_LONG).show();
				}
			}
		});
    	builder.setNegativeButton(R.string.SClose,null);
    	builder.setMultiChoiceItems(_items, Mask, new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
				Mask[arg1] = arg2;
			}
			
    	});
    	AlertDialog alert = builder.create();
    	alert.show();
	}
	
	private void MeasurementProcessor_Initialize(final TMeasurementProcessorItem MeasurementProcessor) throws Exception {
		MeasurementProcessor.Processor.OnProgressHandler = new TVideoRecorderServerMyPlayerComponent.TOnProgressHandler() {
			
			private static final double MinTriggerInterval = (1.0/(24.0*3600.0))*1.0; //. seconds
			
			
			private double LastTimestamp = 0.0;
			
			@Override
			public void DoOnProgress(double ProgressFactor) {
				if ((MeasurementProcessor.CurrentMeasurement != null) && (MeasurementProcessor.CurrentMeasurementOpening == null)) {
	    			double Timestamp = MeasurementProcessor.CurrentMeasurement.StartTimestamp+MeasurementProcessor.CurrentMeasurement.Duration()*ProgressFactor;
	    			double Delta = Math.abs(Timestamp-LastTimestamp);
	    			if (Delta >= MinTriggerInterval) {
	    				LastTimestamp = Timestamp;
		    			//.
	    				MeasurementProcessor.flUpdating = true;
			    		try {
			    			TimeIntervalSlider.SetCurrentTime(Timestamp, false, true, false);
			    		}
			    		finally {
			    			MeasurementProcessor.flUpdating = false;
			    		}
	    			}
				}
			}
		};
		//.
        LinearLayout ProcessorLayout = new LinearLayout(this);
        ProcessorLayout.setOrientation(LinearLayout.VERTICAL);
        ProcessorLayout.setVisibility(View.GONE);
        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f);
        MeasurementProcessors_Layout.addView(ProcessorLayout, LP);
        MeasurementProcessor.Processor.SetLayout(this, ProcessorLayout);
        //.
        MeasurementProcessor.Processor.Start();
    	//.
        MeasurementProcessor.CurrentMeasurement = null;
    	//.
        MeasurementProcessor.CurrentMeasurementOpening = null; 
	}
	
	private void MeasurementProcessor_Finalize(TMeasurementProcessorItem MeasurementProcessor) throws Exception {
		if (MeasurementProcessor.CurrentMeasurementOpening != null) {
			MeasurementProcessor.CurrentMeasurementOpening.Destroy();
			MeasurementProcessor.CurrentMeasurementOpening = null;
		}
		//.
    	if (MeasurementProcessor.Processor != null) {
    		MeasurementProcessor.Processor.Stop();
    		MeasurementProcessor.Processor.Destroy();
    		MeasurementProcessor.Processor = null;
    	}
	}
	
	private void MeasurementProcessor_SetCurrentTime(final TMeasurementProcessorItem MeasurementProcessor, double Timestamp, boolean flPause, int Delay) throws IOException, InterruptedException {
		TSensorMeasurementDescriptor AMeasurement = null;
		if (MeasurementProcessors_flEnabled && MeasurementProcessor.flEnabled)
			AMeasurement = History.SensorMeasurements.GetTypedMeasurementByTimestamp(MeasurementProcessor.Processor.GetTypeID(), Timestamp);
		//.
		if (MeasurementProcessor.CurrentMeasurementOpening != null) {
			MeasurementProcessor.CurrentMeasurementOpening.Cancel();
			MeasurementProcessor.CurrentMeasurementOpening = null;
		}
		//.
		if (AMeasurement != null) {
			MeasurementProcessor.CurrentMeasurement = AMeasurement;
			//.
			final TSensorsModuleMeasurementsArchive.TArchiveItem Item = new TSensorsModuleMeasurementsArchive.TArchiveItem();
			Item.ID = MeasurementProcessor.CurrentMeasurement.ID;
			Item.StartTimestamp = MeasurementProcessor.CurrentMeasurement.StartTimestamp;
			Item.FinishTimestamp = MeasurementProcessor.CurrentMeasurement.FinishTimestamp;
			Item.Location = MeasurementProcessor.CurrentMeasurement.Location;
			Item.Position = (Timestamp-MeasurementProcessor.CurrentMeasurement.StartTimestamp);
			//.
        	if (MeasurementProcessor.Processor.IsSetup() && (MeasurementProcessor.CurrentMeasurement != null) && TSensorMeasurementDescriptor.IDsAreTheSame(MeasurementProcessor.Processor.Measurement.Descriptor.ID, MeasurementProcessor.CurrentMeasurement.ID)) 
        		MeasurementProcessor.Processor.SetPosition(Item.Position, Delay, flPause);
        	else {
    			MeasurementProcessors_ClearSetupState();
				//.
        		MeasurementProcessor.CurrentMeasurementOpening = new TAsyncProcessing() {

        			@Override
        			public void Process() throws Exception {
        				Thread.sleep(MeasurementProcessor_MeasurementOpeningDelay);
        			}

        			@Override
        			public void DoOnCompleted() throws Exception {
        				if (!Canceller.flCancel) {
            				if (MeasurementProcessor.CurrentMeasurementOpening == this)
            					MeasurementProcessor.CurrentMeasurementOpening = null;
            				//.
        					TSensorsModuleMeasurementsArchive.TMeasurementProcessHandler ProcessHandler = new TSensorsModuleMeasurementsArchive.TMeasurementProcessHandler() {
        	    				
        	    				@Override
        	    				public boolean ProcessMeasurement(final TSensorMeasurement Measurement, double MeasurementPosition) throws Exception {
                    				if (Canceller.flCancel) 
                    					return true; //. ->
                    				//.
    			    	        	if (!MeasurementProcessor.Processor.IsSetup() || !((MeasurementProcessor.CurrentMeasurement != null) && TSensorMeasurementDescriptor.IDsAreTheSame(MeasurementProcessor.CurrentMeasurement.ID, MeasurementProcessor.Processor.Measurement.Descriptor.ID))) {  
    			    	        		MeasurementProcessor.Processor.Setup(Measurement);
    				    	        	//.
    				    	        	if (!MeasurementProcessor.Processor.IsVisible()) {
    				    					MeasurementProcessors_VisibleCounter++;
    				    					if (MeasurementProcessors_VisibleCounter == 1)
    				    						MeasurementProcessors_Layout.setVisibility(View.VISIBLE);
    				    					//.
    				    			        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f/MeasurementProcessors_VisibleCounter);
    			    						MeasurementProcessors_Layout.setLayoutParams(LP);
    			    						//.
    				    					MeasurementProcessor.Processor.Show();
    				    	        	}
    			    	        	}
    	    		    	        //. validation
    			    	        	MeasurementProcessor_SetCurrentTime(MeasurementProcessor, TimeIntervalSlider.CurrentTime, true, 0/*Delay*/);
    			    	        	//.
    			    	        	if (MeasurementProcessors_IsSetup())
    			    					TimeIntervalSlider.ValidateCurrentTime(true);
    			    	        	//.
        			    	        return true; //. ->
        	    				}
        	    			};
        	    			//.
        	    			TSensorsModuleMeasurementsArchive.StartOpeningItem(Canceller, Item, ProcessHandler,0, History.BeginTimestamp,History.EndTimestamp, Object, GeographDataServerAddress,GeographDataServerPort, TObjectModelHistoryPanel.this, new TSensorsModuleMeasurementsArchive.TArchiveItemsListUpdater() {

        	    				@Override
        	    				public void DoOnItemsListUpdated(TSensorsModuleMeasurementsArchive.TArchiveItem[] Items) {
        	    					if (MeasurementProcessor.CurrentMeasurement == null)
        	    						return; //. ->
        	    					int Cnt = Items.length;
        	    					for (int I = 0; I < Cnt; I++) 
        	    						if (TSensorMeasurementDescriptor.IDsAreTheSame(Items[I].ID, MeasurementProcessor.CurrentMeasurement.ID)) {
        	    							MeasurementProcessor.CurrentMeasurement.ID = Items[I].ID; //. correct ID from deviation  
        	    							MeasurementProcessor.CurrentMeasurement.Location = Items[I].Location;
        	    							return; //. ->
        	    						}
        	    				}
        					}, new TSensorMeasurementDescriptor.TLocationUpdater() {
        						
        						@Override
        						public void DoOnLocationUpdated(String MeasurementID, int Location) {
        	    					if ((MeasurementProcessor.CurrentMeasurement != null) && TSensorMeasurementDescriptor.IDsAreTheSame(MeasurementID, MeasurementProcessor.CurrentMeasurement.ID)) {
    	    							MeasurementProcessor.CurrentMeasurement.Location = Location;
    	    							return; //. ->
    	    						}
        						}
        					});
        				}
        			}
        			
        			@Override
        			public void DoOnCancelled() throws Exception {
        				if (MeasurementProcessor.CurrentMeasurementOpening == this)
        					MeasurementProcessor.CurrentMeasurementOpening = null;
        			}
        			
					@Override
					public void DoOnException(Exception E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
						Toast.makeText(TObjectModelHistoryPanel.this, S,	Toast.LENGTH_LONG).show();
					}
        		};
        		MeasurementProcessor.CurrentMeasurementOpening.Start();
        	}
		}
		else {
    		try {
				if (MeasurementProcessor.Processor.IsVisible()) {
					MeasurementProcessor.Processor.Hide();
					//.
					MeasurementProcessors_VisibleCounter--;
					if (MeasurementProcessors_VisibleCounter == 0)
						MeasurementProcessors_Layout.setVisibility(View.GONE);
					else {
    			        LinearLayout.LayoutParams LP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f/MeasurementProcessors_VisibleCounter);
						MeasurementProcessors_Layout.setLayoutParams(LP);
					}
				}
				//.
    			if (MeasurementProcessor.Processor.IsSetup())
    				MeasurementProcessor.Processor.Reset();
			} catch (Exception E) {
				String S = E.getMessage();
				if (S == null)
					S = E.getClass().getName();
				Toast.makeText(TObjectModelHistoryPanel.this, S,	Toast.LENGTH_LONG).show();
			}
    		//.
			MeasurementProcessor.CurrentMeasurement = null;
		}
	}
	
	private void MeasurementProcessors_Initialize() throws Exception {
		MeasurementProcessors_Finalize();
		//.
		int Cnt = History.SensorMeasurements.MeasurementTypes.size();
		MeasurementProcessors = new ArrayList<TMeasurementProcessorItem>();
		for (int I = 0; I < Cnt; I++) {
			TMeasurementProcessor P = TMeasurementProcessor.GetProcessor(History.SensorMeasurements.MeasurementTypes.get(I));
			if (P != null) {
				TMeasurementProcessorItem Processor = new TMeasurementProcessorItem();
				Processor.Processor = P;
				MeasurementProcessor_Initialize(Processor);
				//.
				MeasurementProcessors.add(Processor);
			}
		}
	}
	
	private void MeasurementProcessors_Finalize() throws Exception {
		if (MeasurementProcessors == null)
			return; //. ->
		int Cnt = MeasurementProcessors.size();
		for (int I = 0; I < Cnt; I++) 
			MeasurementProcessor_Finalize(MeasurementProcessors.get(I));
		MeasurementProcessors = null;
	}
	
	private void MeasurementProcessors_CheckSetupState() {
		MeasurementProcessors_flSetup = true;
		int Cnt = MeasurementProcessors.size();
		for (int I = 0; I < Cnt; I++) {
			TMeasurementProcessorItem Processor = MeasurementProcessors.get(I);
			if (Processor.CurrentMeasurement != null) {
				MeasurementProcessors_flSetup &= (Processor.Processor.IsSetup() && TSensorMeasurementDescriptor.IDsAreTheSame(Processor.CurrentMeasurement.ID, Processor.Processor.Measurement.Descriptor.ID));
				if (!MeasurementProcessors_flSetup)
					return; //. ->
			}
		}
	}
	
	private void MeasurementProcessors_ClearSetupState() {
		MeasurementProcessors_flSetup = false;
	}
	
	private boolean MeasurementProcessors_IsSetup() {
		if (!MeasurementProcessors_flSetup)
			MeasurementProcessors_CheckSetupState();
		return MeasurementProcessors_flSetup;
	}
	
	private void MeasurementProcessors_SetCurrentTime(double Timestamp, boolean flPause, int Delay) throws IOException, InterruptedException {
		int Cnt = MeasurementProcessors.size();
		for (int I = 0; I < Cnt; I++) {
			TMeasurementProcessorItem Processor = MeasurementProcessors.get(I);
			if (!Processor.flUpdating)
				MeasurementProcessor_SetCurrentTime(Processor, Timestamp, flPause, Delay);
		}
	}
	
	private void MeasurementProcessors_Pause() {
		int Cnt = MeasurementProcessors.size();
		for (int I = 0; I < Cnt; I++) 
			MeasurementProcessors.get(I).Processor.Pause();
	}
	
	private boolean Animation_IsActive() {
		return cbTimeAnimation.isChecked();
	}
	
	private boolean Animation_IsAllowedAtTheMoment() {
		return (Animation_IsActive() && MeasurementProcessors_IsSetup());
	}
	
	private void Animation_SetActive(boolean flActive) {
		if (Animation_IsActive() != flActive)
			cbTimeAnimation.performClick();
	}
	
	private void Animation_SetEnabled(boolean flEnabled) {
		cbTimeAnimation.setEnabled(flEnabled);
	}
	
    private void OpenCurrentTimeInReflector() throws Exception {
    	TGeoLocationRecord GeoLocationRecord = History.GetCalculatedGeoLocationRecord(TimeIntervalSlider.CurrentTime);
    	if (GeoLocationRecord != null) {
    		if (!GeoLocationRecord.IsAvailable()) {
				Toast.makeText(this, R.string.SLocationIsNotAvailable, Toast.LENGTH_LONG).show();
				return; //. ->
    		}
    		//.
    		int ReflectorID = TReflector.GetNextID();
    		//.
    		Intent intent = new Intent(this, TReflector.class);
			intent.putExtra("ID", ReflectorID);
			intent.putExtra("Reason", TReflectorComponent.REASON_MONITORGEOLOCATION);
			startActivity(intent);
			//.
			SendShowGeoLocationRecordMessage(ReflectorID,GeoLocationRecord, 2000/*delay, ms*/);
			//.
			if (Animation_IsActive())
				Animation_SetActive(false);
    	}
    }
    
    private double MeasurementSpacing = (1.0/(24.0*3600.0))*5; //. seconds
    private double MeasurementSkipInterval = (1.0/(24.0*3600.0))*1; //. seconds
    
    private void OpenCurrentTimeAVMeasurementInProcessor() {
    	TSensorMeasurementDescriptor _Measurement = History.SensorMeasurements.GetTypedMeasurementByTimestamp(com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.ModelTypeID, TimeIntervalSlider.CurrentTime);
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
			TimeIntervalSlider.SetCurrentTime(_Measurement.StartTimestamp, false, true, true);
		}
		final TSensorMeasurementDescriptor Measurement = _Measurement;
		//.
		if ((Measurement != null) && Measurement.IsTypeOf(com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.AV.Model.TModel.ModelTypeID)) {
			TSensorsModuleMeasurementsArchive.TArchiveItem Item = new TSensorsModuleMeasurementsArchive.TArchiveItem();
			Item.ID = Measurement.ID;
			Item.StartTimestamp = Measurement.StartTimestamp;
			Item.FinishTimestamp = Measurement.FinishTimestamp;
			Item.Location = Measurement.Location;
			Item.Position = (TimeIntervalSlider.CurrentTime-Measurement.StartTimestamp);
			TSensorsModuleMeasurementsArchive.StartOpeningItem(null, Item, null,REQUEST_SHOWVIDEOMEASUREMENT, History.BeginTimestamp,History.EndTimestamp, Object, GeographDataServerAddress,GeographDataServerPort, this, new TSensorsModuleMeasurementsArchive.TArchiveItemsListUpdater() {

				@Override
				public void DoOnItemsListUpdated(TSensorsModuleMeasurementsArchive.TArchiveItem[] Items) {
					int Cnt = Items.length;
					for (int I = 0; I < Cnt; I++) 
						if (TSensorMeasurementDescriptor.IDsAreTheSame(Items[I].ID, Measurement.ID)) {
							Measurement.Location = Items[I].Location;
							return; //. ->
						}
				}
			}, new TSensorMeasurementDescriptor.TLocationUpdater() {
				
				@Override
				public void DoOnLocationUpdated(String MeasurementID, int Location) {
					if (TSensorMeasurementDescriptor.IDsAreTheSame(MeasurementID, Measurement.ID)) {
						Measurement.Location = Location;
						return; //. ->
					}
				}
			});
			//.
			if (Animation_IsActive())
				Animation_SetActive(false);
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
					final TShowGeoLocationRecordParams ShowGeoLocationRecordParams = (TShowGeoLocationRecordParams)msg.obj;
					//.
					final TReflectorComponent Reflector;
					if (ShowGeoLocationRecordParams.ReflectorID != 0) {
						TReflector RFL = TReflector.GetReflector(ShowGeoLocationRecordParams.ReflectorID);
						if (RFL != null)
							Reflector = RFL.Component;
						else 
							Reflector = null;
					}
					else
						Reflector = ObjectTrackViewer;
					//.
					if (Reflector != null) {
						TAsyncProcessing Showing = new TAsyncProcessing() {

							private TXYCoord LocationXY;
							private byte[] TrackData;
							
							@Override
							public void Process() throws Exception {
								if (ShowGeoLocationRecordParams.GeoLocationRecord.IsAvailable())
									LocationXY = Reflector.ConvertGeoCoordinatesToXY(History.ObjectGeoDatumID, ShowGeoLocationRecordParams.GeoLocationRecord.Latitude,ShowGeoLocationRecordParams.GeoLocationRecord.Longitude,ShowGeoLocationRecordParams.GeoLocationRecord.Altitude);
								else
									LocationXY = null;
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
								if (Reflector.flExists)
									if (LocationXY != null)
										Reflector.MoveReflectionWindow(LocationXY);
									if (TrackData != null)
										Reflector.ObjectTracks_AddTrack(TrackData);
							}
							
							@Override
							public void DoOnException(Exception E) {
								String S = E.getMessage();
								if (S == null)
									S = E.getClass().getName();
								Toast.makeText(TObjectModelHistoryPanel.this, S,	Toast.LENGTH_LONG).show();
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