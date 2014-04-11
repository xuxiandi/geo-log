package com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Utils.ColorPicker;
import com.geoscope.GeoEye.Utils.Graphics.TDrawing;
import com.geoscope.GeoEye.Utils.Graphics.TDrawingNode;
import com.geoscope.GeoEye.Utils.Graphics.TLineDrawing;
import com.geoscope.GeoEye.Utils.Graphics.TPictureDrawing;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TAsyncProcessing;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TDataConverter;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;
import com.jcraft.jzlib.JZlib;
import com.jcraft.jzlib.ZInputStream;
import com.jcraft.jzlib.ZOutputStream;

@SuppressLint("HandlerLeak")
public class TDrawingEditor extends Activity implements OnTouchListener {

	public static final int MODE_NONE 		= 0;
	public static final int MODE_DRAWING 	= 1;
	public static final int MODE_MOVING 	= 2;
	public static final int MODE_SETTINGS 	= 3;
	//.
	public static final int REQUEST_ADDPICTURE 			= 1;
	public static final int REQUEST_ADDPICTUREFROMFILE 	= 2;
	//.
	private File FileSelectorPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
	
	public class TSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {
		
		public SurfaceHolder _SurfaceHolder;
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			Surface_Width = width;
			Surface_Height = height;
			//.
			if (Background != null) 
				Background.recycle();
			try {
				Background = BackgroundImage_ReCreate(width, height);
			} catch (Exception E) {
				Background = null;
			}
			if (BackgroundImage != null) 
				BackgroundImage.recycle();
			BackgroundImage = null; //. Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			if (ForegroundImage != null) 
				ForegroundImage.recycle();
			ForegroundImage = null; //. Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			if (OriginDrawableImage != null) 
				OriginDrawableImage.recycle();
			OriginDrawableImage = null; //. Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			if (DrawableImage != null)
				DrawableImage.recycle();
			DrawableImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			DrawableImageCanvas = new Canvas(DrawableImage);			
			//.
        	try {
				Drawings_RepaintImage();
				//.
				if (flLoadDrawings) {
					flLoadDrawings = false;
					//.
		    		TAsyncProcessing Processing = new TAsyncProcessing(TDrawingEditor.this,getString(R.string.SWaitAMoment)) {
		    			@Override
		    			public void Process() throws Exception {
		        			DrawingsFile_Load();
				    		Thread.sleep(100); 
		    			}
		    			@Override 
		    			public void DoOnCompleted() throws Exception {
		    			}
		    			@Override
		    			public void DoOnException(Exception E) {
		    				Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    			}
		    		};
		    		Processing.Start();
				}
			} catch (Exception E) {
				Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();  
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			_SurfaceHolder = holder;
			//.
			SurfaceUpdating = new TSurfaceUpdating();
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (DrawableImage != null) {
				DrawableImage.recycle();
				DrawableImage = null;
			}
			if (OriginDrawableImage != null) {
				OriginDrawableImage.recycle();
				OriginDrawableImage = null;
			}
			if (ForegroundImage != null) { 
				ForegroundImage.recycle();
				ForegroundImage = null;
			}
			if (BackgroundImage != null) { 
				BackgroundImage.recycle();
				BackgroundImage = null;
			}
			if (Background != null) { 
				Background.recycle();
				Background = null;
			}
			if (SurfaceUpdating != null) {
				SurfaceUpdating.Destroy();
				SurfaceUpdating = null;
			}
			_SurfaceHolder = null;
		}
	}
	
    public class TSurfaceUpdating implements Runnable {
    	
    	private Thread _Thread;
    	private boolean flCancel = false;
    	public boolean flProcessing = false;
    	private TAutoResetEvent ProcessSignal = new TAutoResetEvent();
    	
    	public TSurfaceUpdating() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
    	public void Destroy() {
    		CancelAndWait();
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
							float dX = 0.0F;
							float dY = 0.0F;
							synchronized (Moving_Lock) {
								dX = Moving_dX;
								dY = Moving_dY;
								if (Moving_flProcessing) {
									dX += (Moving_X-Moving_OrgX);
									dY += (Moving_Y-Moving_OrgY);
								}
							}
							//.
							Canvas canvas = SurfaceHolderCallbackHandler._SurfaceHolder.lockCanvas();
							try {
								synchronized (Drawings_ImageLock) {
									if (Background != null)
										canvas.drawBitmap(Background, 0,0, null);
									if (BackgroundImage != null)
										canvas.drawBitmap(BackgroundImage, dX,dY, null);
									if (DrawableImage != null)
										canvas.drawBitmap(DrawableImage, dX,dY, null);
									if (ForegroundImage != null)
										canvas.drawBitmap(ForegroundImage, dX,dY, null);
								}
								//. draw "drawing" marker
								if (LineDrawingProcess_flProcessing) 
									canvas.drawCircle(LineDrawingProcess_LastX,LineDrawingProcess_LastY,LineDrawingProcess_Brush.getStrokeWidth()/2.0F,LineDrawingProcess_MarkerPaint);
								//. draw ColorPickerBar and BrushWidthPickerBar  
								float OfsY = DrawingEditorSurfaceControlLayout.getHeight();
								int CurrentColor = Settings_Brush.getColor();
								if (ColorPickerBar != null)
									ColorPickerBar.Paint(canvas, 0,OfsY, Surface_Width,Surface_Height-OfsY, CurrentColor);
								if (BrushWidthPickerBar != null) {
									float CurrentBrushWidth = Settings_Brush.getStrokeWidth();
									BrushWidthPickerBar.Paint(canvas, 0,OfsY, Surface_Width,Surface_Height-OfsY, CurrentBrushWidth,CurrentColor);
								}
							} 
							finally {
								SurfaceHolderCallbackHandler._SurfaceHolder.unlockCanvasAndPost(canvas);
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
		
		public void Start() {
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
    }
    
	public static class TSettingsTestImage extends ImageView {

		public Paint Brush = null;
		
		public TSettingsTestImage(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}

		public TSettingsTestImage(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		public TSettingsTestImage(Context context) {
			super(context);
		}
		
        @Override
        protected void onDraw(Canvas canvas) {
        	if (Brush != null) {
            	float Spacing = getWidth()/20.0F; 
            	canvas.drawLine(Spacing, getHeight()/2.0F, getWidth()-Spacing, getHeight()/2.0F, Brush);
        	}
        }
    }
	
	public static class TColorPickerBar {

		public static final int LAYOUT_NONE 		= 0; 
		public static final int LAYOUT_LEFTALIGN 	= 1; 
		public static final int LAYOUT_RIGHTALIGN 	= 2;
		
		public static final int COLOR_UNKNOWN = 1;
		
		public static int[] Colors = new int[] {
			Color.BLACK,
			Color.DKGRAY,
			Color.GRAY,
			Color.LTGRAY,
			Color.WHITE,
			Color.RED,
			Color.GREEN,
			Color.BLUE,
			Color.YELLOW,
			Color.CYAN,
			Color.MAGENTA
		};
		
		private float	Size = 0;
		private int 	Layout = LAYOUT_NONE;
		//.
		private float Left = 0;
		private float Top = 0;
		private float Width = 0;
		private float Height = 0;
		//.
		private Paint ColorBoxPaint;
		private Paint ColorBoxSelectedMarkerPaint;
		
		public TColorPickerBar(float pSize, int pLayout) {
			Size = pSize;
			Layout = pLayout;
			//.
			ColorBoxPaint = new Paint();
			//.
			ColorBoxSelectedMarkerPaint = new Paint();
		}
		
		public void Paint(Canvas _Canvas, float pLeft, float pTop, float pWidth, float pHeight, int CurrentColor) {
			if (Size == 0)
				return; //. ->
			switch (Layout) {
			
			case LAYOUT_LEFTALIGN:
				float ItemWidth = Size;
				float ItemHeight = ((pHeight+0.0F)/Colors.length);
				float X0 = pLeft;
				float Y0 = pTop;
				float XC0 = X0;
				for (int I = 0; I < Colors.length; I++) {
					int ItemColor = Colors[I];
					float YC0 = Y0+I*ItemHeight;
					ColorBoxPaint.setColor(ItemColor);
					//.
					if (ItemColor == CurrentColor) {
						ColorBoxPaint.setAlpha(255);
						_Canvas.drawRect(XC0,YC0, XC0+ItemWidth,YC0+ItemHeight, ColorBoxPaint);
						if (ItemColor != Color.BLACK)
							ColorBoxSelectedMarkerPaint.setColor(Color.BLACK);
						else
							ColorBoxSelectedMarkerPaint.setColor(Color.WHITE);
						float CW = ItemWidth/2.0F;
						float CH = ItemHeight/2.0F;
						float R;
						if (ItemWidth < ItemHeight)
							R = ItemWidth;
						else
							R = ItemHeight;
						R = R/10.0F;
						_Canvas.drawCircle(XC0+CW,YC0+CH, R, ColorBoxSelectedMarkerPaint);
					}
					else {
						ColorBoxPaint.setAlpha(192);
						_Canvas.drawRect(XC0,YC0, XC0+ItemWidth,YC0+ItemHeight, ColorBoxPaint);
					}
				}
				//.
				Left = X0;
				Top = Y0;
				Width = Size;
				Height = pHeight;
				break; //. >
				
			default:
				Left = 0;
				Top = 0;
				Width = 0;
				Height = 0;
				break; //. >
			}
		}
		
		public boolean Inside(float X, float Y) {
			return (((Left <= X) && (X < Left+Width)) && ((Top <= Y) && (Y < Top+Height))); 
		}
		
		public int PickAColor(float X, float Y) {
			int Result = COLOR_UNKNOWN;
			if (Size == 0)
				return Result; //. ->
			switch (Layout) {
			
			case LAYOUT_LEFTALIGN:
				float ItemWidth = Size;
				float ItemHeight = ((Height+0.0F)/Colors.length);
				X = X-Left;
				Y = Y-Top;
				int Idx = (int)(Y/ItemHeight);
				if (((0 <= X) && (X <= ItemWidth)) && ((0 <= Idx) && (Idx < Colors.length)))
					return Colors[Idx]; //. ->
				break; //. >
			}
			return Result; 
		}
		
	}
	
	public static class TBrushWidthPickerBar {

		public static final int LAYOUT_NONE 		= 0; 
		public static final int LAYOUT_LEFTALIGN 	= 1; 
		public static final int LAYOUT_RIGHTALIGN 	= 2;
		
		private float 	Size = 0;
		private float 	MinWidth;
		private float 	MaxWidth;
		private float 	WidthDivider = 1.0F;
		private int 	Layout = LAYOUT_NONE;
		//.
		private float[] Widths = null;
		private float Left = 0;
		private float Top = 0;
		private float Width = 0;
		private float Height = 0;
		//.
		private Paint WidthBoxPaint;
		private Paint WidthBoxSelectedPaint;
		private Paint WidthBoxCirclePaint;
		private Paint WidthBoxSelectedCirclePaint;
		
		public TBrushWidthPickerBar(float pSize, float pMinWidth, float pMaxWidth, float pWidthDivider, int pLayout) {
			Size = pSize;
			MinWidth = pMinWidth;
			MaxWidth = pMaxWidth;
			WidthDivider = pWidthDivider;
			Layout = pLayout;
			//.
			WidthBoxPaint = new Paint();
			int C = 0xff999999;
			int r = Color.red(C);
			int g = Color.green(C);
			int b = Color.blue(C);
			int alpha = 128;
			WidthBoxPaint.setColor(Color.argb(alpha, r,g,b));
			//.
			WidthBoxSelectedPaint = new Paint();
			C = Color.GRAY;
			r = Color.red(C);
			g = Color.green(C);
			b = Color.blue(C);
			alpha = 192;
			r *= 1.5;
			if (r > 255)
				r = 255;
			WidthBoxSelectedPaint.setColor(Color.argb(alpha, r,g,b));
			//.
			WidthBoxCirclePaint = new Paint();
			WidthBoxCirclePaint.setColor(Color.DKGRAY);
			//.
			WidthBoxSelectedCirclePaint = new Paint();
		}
		
		public void Paint(Canvas _Canvas, float pLeft, float pTop, float pWidth, float pHeight, float CurrentWidth, int CurrentColor) {
			if (Size == 0)
				return; //. ->
			switch (Layout) {
			
			case LAYOUT_RIGHTALIGN:
				float ItemWidth = Size;
				float X0 = pLeft+pWidth-Size;
				float Y0 = pTop;
				float XC0 = X0;
				float CW = ItemWidth/2.0F;
				if (Widths == null) {
					float W = MaxWidth;
					int Count = (int)(pHeight/W);
					float MinValue = Math.abs(W-CurrentWidth);
					int CurrentWidthIndex = 0;
					for (int I = 1; I < Count; I++) {
						W = W/WidthDivider;
						float D = Math.abs(W-CurrentWidth);
						if (D < MinValue) {
							MinValue = D;
							CurrentWidthIndex = I;
						}
						if (W < MinWidth) {
							Count = I;
							break; //. >
						}
					}
					float ItemHeight = pHeight/Count;
					float CH = ItemHeight/2.0F;
					W = MaxWidth;
					if ((Widths == null) || (Widths.length != Count))
						Widths = new float[Count];
					for (int I = 0; I < Count; I++) {
						Widths[I] = W;
						float YC0 = Y0+(Count-I-1)*ItemHeight;
						float R = W/2.0F;
						if (CurrentWidthIndex == I) {
							WidthBoxSelectedCirclePaint.setColor(CurrentColor);
							_Canvas.drawRect(XC0,YC0, XC0+ItemWidth,YC0+ItemHeight, WidthBoxSelectedPaint);
							_Canvas.drawCircle(XC0+CW,YC0+CH, R, WidthBoxSelectedCirclePaint);
						}
						else {
							_Canvas.drawRect(XC0,YC0, XC0+ItemWidth,YC0+ItemHeight, WidthBoxPaint);
							_Canvas.drawCircle(XC0+CW,YC0+CH, R, WidthBoxCirclePaint);
						}
						//.
						W = W/WidthDivider;
					}
				}
				else {
					int Count = Widths.length;
					float MinValue = Math.abs(Widths[0]-CurrentWidth);
					int CurrentWidthIndex = 0;
					for (int I = 1; I < Count; I++) {
						float D = Math.abs(Widths[I]-CurrentWidth);
						if (D < MinValue) {
							MinValue = D;
							CurrentWidthIndex = I;
						}
					}
					float ItemHeight = pHeight/Count;
					float CH = ItemHeight/2.0F;
					for (int I = 0; I < Count; I++) {
						float YC0 = Y0+(Count-I-1)*ItemHeight;
						_Canvas.drawRect(XC0,YC0, XC0+ItemWidth,YC0+ItemHeight, WidthBoxPaint);
						float R = Widths[I]/2.0F;
						if (CurrentWidthIndex == I) {
							WidthBoxSelectedCirclePaint.setColor(CurrentColor);
							_Canvas.drawRect(XC0,YC0, XC0+ItemWidth,YC0+ItemHeight, WidthBoxSelectedPaint);
							_Canvas.drawCircle(XC0+CW,YC0+CH, R, WidthBoxSelectedCirclePaint);
						}
						else {
							_Canvas.drawRect(XC0,YC0, XC0+ItemWidth,YC0+ItemHeight, WidthBoxPaint);
							_Canvas.drawCircle(XC0+CW,YC0+CH, R, WidthBoxCirclePaint);
						}
					}
				}
				//.
				Left = X0;
				Top = Y0;
				Width = Size;
				Height = pHeight;
				break; //. >
				
			default:
				Left = 0;
				Top = 0;
				Width = 0;
				Height = 0;
				break; //. >
			}
		}
		
		public boolean Inside(float X, float Y) {
			return (((Left <= X) && (X < Left+Width)) && ((Top <= Y) && (Y < Top+Height))); 
		}
		
		public float PickAWidth(float X, float Y) {
			float Result = -1.0F;
			if (Size == 0)
				return Result; //. ->
			switch (Layout) {
			
			case LAYOUT_RIGHTALIGN:
				float ItemWidth = Size;
				float ItemHeight = ((Height+0.0F)/Widths.length);
				X = X-Left;
				Y = Y-Top;
				int Idx = (int)(Y/ItemHeight);
				if (((0 <= X) && (X <= ItemWidth)) && ((0 <= Idx) && (Idx < Widths.length)))
					return Widths[Widths.length-Idx-1]; //. ->
				break; //. >
			}
			return Result; 
		}
		
	}
	
	private boolean flLoadDrawings = true;
	//.
	private Bitmap Background = null;
	private Bitmap BackgroundImage = null;
	private Bitmap ForegroundImage = null;
	private Bitmap OriginDrawableImage = null;
	private Bitmap DrawableImage = null;
	private Canvas DrawableImageCanvas = null;
	//.
	private static final float 	ColorPickerBarSize = 24.0F;
	private TColorPickerBar 	ColorPickerBar = null;
	//.
	private static final float 		BrushWidthPickerBarSize = 24.0F;
	private static final float 		BrushWidthPickerBarMinWidth = 2.0F;
	private static final float 		BrushWidthPickerBarMaxWidth = 20.0F;
	private TBrushWidthPickerBar 	BrushWidthPickerBar = null;
	//.
	private DisplayMetrics metrics;
	private SurfaceView Surface;
	private int			Surface_Width;
	private int			Surface_Height;
	private TSurfaceHolderCallbackHandler SurfaceHolderCallbackHandler = new TSurfaceHolderCallbackHandler();
	private TSurfaceUpdating SurfaceUpdating = null;
	//.
	private int Mode = MODE_NONE;
	//.
	private List<TDrawing> 		Drawings;
	private	int					Drawings_HistoryIndex;
	private boolean 			Drawings_flChanged;
	private boolean				Drawings_flSaved;
	//.
	private RelativeLayout 	DrawingEditorSurfaceLayout;
	private LinearLayout	DrawingEditorSurfaceControlLayout;
	private CheckBox 		cbDrawingEditorMode;
	private Button 			btnDrawingEditorBrushSelector;
	private Button 			btnDrawingEditorUndo;
	private Button 			btnDrawingEditorRedo;
	private Button 			btnDrawingEditorClear;
	private Button 			btnDrawingEditorOperations;
	private Button 			btnDrawingEditorCommit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //.
		try {
			if (Reflector().flFullScreen) { //. small screen
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);		
			}
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
			finish();
			return; //. ->
		}
		//.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	DrawingsFile_Name = extras.getString("FileName");
        	DrawingsFile_flReadOnly = extras.getBoolean("ReadOnly");
        }
        //.
    	metrics = getApplicationContext().getResources().getDisplayMetrics();
        //.
        setContentView(R.layout.drawing_editor);
        //.
		DrawingEditorSurfaceLayout = (RelativeLayout)findViewById(R.id.DrawingEditorSurfaceLayout);
		DrawingEditorSurfaceControlLayout = (LinearLayout)findViewById(R.id.DrawingEditorSurfaceControlLayout);
		//.
		Surface = (SurfaceView) findViewById(R.id.DrawingEditorSurfaceView);
		Surface.setOnTouchListener(this);
		//.
		cbDrawingEditorMode = (CheckBox)findViewById(R.id.cbDrawingEditorMode);
		cbDrawingEditorMode.setChecked(true);
		cbDrawingEditorMode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cbDrawingEditorMode.isChecked())
					SetMode(MODE_DRAWING);
				else
					SetMode(MODE_MOVING);
			}
		});
        //.
		btnDrawingEditorBrushSelector = (Button)findViewById(R.id.btnDrawingEditorBrushSelector);
		btnDrawingEditorBrushSelector.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	SetMode(MODE_SETTINGS);
            }
        });
		//.
		btnDrawingEditorUndo = (Button)findViewById(R.id.btnDrawingEditorUndo);
		btnDrawingEditorUndo.setEnabled(false);
		btnDrawingEditorUndo.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	try {
            		boolean R = Drawings_Undo();
            		//.
            		btnDrawingEditorUndo.setEnabled(R);
            		btnDrawingEditorRedo.setEnabled(true);
        			btnDrawingEditorClear.setEnabled(R);
		        } 
		        catch (Exception E) {
					Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
            }
        });
		//.
		btnDrawingEditorRedo = (Button)findViewById(R.id.btnDrawingEditorRedo);
		btnDrawingEditorRedo.setEnabled(false);
		btnDrawingEditorRedo.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	try {
            		boolean R = Drawings_Redo();
            		//.
            		btnDrawingEditorRedo.setEnabled(R);
            		btnDrawingEditorUndo.setEnabled(true);
        			btnDrawingEditorClear.setEnabled(true);
		        } 
		        catch (Exception E) {
					Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
            }
        });
		//.
		btnDrawingEditorClear = (Button)findViewById(R.id.btnDrawingEditorClear);
		btnDrawingEditorClear.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
        		if (Drawings_HistoryIndex > 0) {
        		    new AlertDialog.Builder(TDrawingEditor.this)
        	        .setIcon(android.R.drawable.ic_dialog_alert)
        	        .setTitle(R.string.SConfirmation)
        	        .setMessage(R.string.SCancelChanges)
        		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
        		    	public void onClick(DialogInterface dialog, int id) {
        		    		try {
        		    			Drawings_UndoAll();
        		    			btnDrawingEditorUndo.setEnabled(false);
        		    			btnDrawingEditorRedo.setEnabled(true);
        			        } 
        			        catch (Exception E) {
        						Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();  
        			        }  
        		    	}
        		    })
        		    .setNegativeButton(R.string.SNo, null)
        		    .show();
        		}
            }
        });
		//.
		btnDrawingEditorOperations = (Button)findViewById(R.id.btnDrawingEditorOperations);
		btnDrawingEditorOperations.setEnabled(false);
		btnDrawingEditorOperations.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		if (GetMode() != MODE_DRAWING) {
        			Toast.makeText(TDrawingEditor.this, R.string.SViewIsNotInDrawingMode, Toast.LENGTH_LONG).show();
        			return; //. ->
        		}
        		//.
        		final CharSequence[] _items;
    			_items = new CharSequence[2];
    			_items[0] = getString(R.string.SAddImage);
    			_items[1] = getString(R.string.SAddImageFromFile);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TDrawingEditor.this);
        		builder.setTitle(R.string.SOperations);
        		builder.setNegativeButton(TDrawingEditor.this.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
	    					switch (arg1) {
	    					case 0: //. take a picture
	    		      		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    		      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(PictureDrawingProcess_GetPictureTempFile(TDrawingEditor.this))); 
	    		      		    startActivityForResult(intent, REQUEST_ADDPICTURE);    		
	    						break; //. >
	    						
	    					case 1: //. take a picture form file
	    						final String[] FileList;
    						    if(FileSelectorPath.exists()) {
    						        FilenameFilter filter = new FilenameFilter() {
    						            public boolean accept(File dir, String filename) {
    						                return (filename.contains(".jpg") || filename.contains(".bmp") || filename.contains(".png"));
    						            }
    						        };
    						        FileList = FileSelectorPath.list(filter);
    						    }
    						    else 
    						        FileList= new String[0];
    						    //.
    						    AlertDialog.Builder builder = new AlertDialog.Builder(TDrawingEditor.this);
					            builder.setTitle(R.string.SChooseFile);
					            builder.setItems(FileList, new DialogInterface.OnClickListener() {
					            	
					                public void onClick(DialogInterface dialog, int which) {
					                    File ChosenFile = new File(FileSelectorPath.getAbsolutePath()+"/"+FileList[which]);
					                    //.
										try {
						                    PictureDrawingProcess_AddPictureFromFile(ChosenFile);
										}
										catch (Throwable E) {
											String S = E.getMessage();
											if (S == null)
												S = E.getClass().getName();
						        			Toast.makeText(TDrawingEditor.this, S, Toast.LENGTH_SHORT).show();  						
										}
					                }
					            });
					            Dialog FileDialog = builder.create();
					            FileDialog.show();	    						
					            break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TDrawingEditor.this, TDrawingEditor.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
						//.
						arg0.dismiss();
        			}
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
            }
        });
		//.
		btnDrawingEditorCommit = (Button)findViewById(R.id.btnDrawingEditorCommit);
		btnDrawingEditorCommit.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
				if (Drawings_flChanged)
					new TChangesCommitting(true);
				else
					TDrawingEditor.this.finish();
            }
        });
		//.
		try {
			Settings_Initialize();
			Drawings_Initialize();
			//.
			LineDrawingProcess_Initialize();
			PictureDrawingProcess_Initialize();
			//.
			Moving_Reset();
			//.
			if (!DrawingsFile_flReadOnly) {
				ColorPickerBar = new TColorPickerBar(ColorPickerBarSize*metrics.density, TColorPickerBar.LAYOUT_LEFTALIGN);
				BrushWidthPickerBar = new TBrushWidthPickerBar(BrushWidthPickerBarSize*metrics.density, BrushWidthPickerBarMinWidth*metrics.density,BrushWidthPickerBarMaxWidth*metrics.density, 1.1F, TBrushWidthPickerBar.LAYOUT_RIGHTALIGN);
				//.
				SetMode(MODE_DRAWING);
				DrawingEditorSurfaceControlLayout.setVisibility(View.VISIBLE);
			}
			else {
				ColorPickerBar = null;
				BrushWidthPickerBar = null;
				//.
				SetMode(MODE_MOVING);
				DrawingEditorSurfaceControlLayout.setVisibility(View.GONE);
			}
			//.
			Update();		
		}
		catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(this, getString(R.string.SError)+S, Toast.LENGTH_LONG).show();
			//.
			finish();
			return; //. ->
		}
        //.
        setResult(Activity.RESULT_CANCELED);
	}

	@Override
	protected void onDestroy() {
		try {
			PictureDrawingProcess_Finalize();
			LineDrawingProcess_Finalize();
			//.
			Drawings_Finalize();
			Settings_Finalize();
		}
		catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(this, getString(R.string.SError)+S, Toast.LENGTH_LONG).show();
		}
		//.
		super.onDestroy();
	}

    private TReflector Reflector() throws Exception {
    	TReflector Reflector = TReflector.GetReflector();
    	if (Reflector == null)
    		throw new Exception(getString(R.string.SReflectorIsNull)); //. =>
		return Reflector;
    }
    
    @Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void onStart() {
    	super.onStart();
		//.
		SurfaceHolder sh = Surface.getHolder();
    	sh.addCallback(SurfaceHolderCallbackHandler);
    }
	
	@Override
	public boolean onTouch(View pView, MotionEvent pEvent) {
		switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {
		
		case MotionEvent.ACTION_DOWN:
			float X = pEvent.getX();
			float Y = pEvent.getY();
			if ((ColorPickerBar != null) && ColorPickerBar.Inside(X,Y)) {
				int NewColor = ColorPickerBar.PickAColor(X,Y);
				if (NewColor != TColorPickerBar.COLOR_UNKNOWN) {
					Settings_Brush_SetColorAndApply(NewColor);
					//.
					SurfaceUpdating.Start();
				}
				//.
				return true; //. ->
			}
			if ((BrushWidthPickerBar != null) && BrushWidthPickerBar.Inside(X,Y)) {
				float NewWidth = BrushWidthPickerBar.PickAWidth(X,Y);
				if (NewWidth > 0.0F) {
					Settings_Brush_SetWidthAndApply(NewWidth);
					//.
					SurfaceUpdating.Start();
				}
				//.
				return true; //. ->
			}
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if (LineDrawingProcess_flProcessing)
				LineDrawingProcess_End();
			//.
			X = pEvent.getX();
			Y = pEvent.getY();
			if ((ColorPickerBar != null) && ColorPickerBar.Inside(X,Y)) 
				return true; //. ->
			if ((BrushWidthPickerBar != null) && BrushWidthPickerBar.Inside(X,Y)) 
				return true; //. ->
			break;
		}
		//.
		switch (GetMode()) {
		
		case MODE_DRAWING:
			/* if (Containers_CurrentContainer_flUpdating)
				return false; //. ->*/
			switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {
			
			case MotionEvent.ACTION_DOWN:
				LineDrawingProcess_Begin(pEvent.getX(),pEvent.getY());
				break;
				
			case MotionEvent.ACTION_MOVE:
				LineDrawingProcess_Draw(pEvent.getX(),pEvent.getY());
				break;
				
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				LineDrawingProcess_End();
				break;
			}
			break; //. >
			
		case MODE_MOVING:
			switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {
			
			case MotionEvent.ACTION_DOWN:
				Moving_Begin(pEvent.getX(),pEvent.getY());
				break;
				
			case MotionEvent.ACTION_MOVE:
				Moving_Move(pEvent.getX(),pEvent.getY());
				break;
				
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				try {
					Moving_End();
		        } 
		        catch (Exception E) {
					Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
				break;
			}
			break; //. >
		}
		return true;
	}	
	
	@Override
	public void onBackPressed() {
		if (GetMode() == MODE_SETTINGS) {
			SetMode(MODE_DRAWING);
			return; //. ->
		}
		//.
		if (Drawings_flChanged && (!Drawings_flSaved)) {
		    new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.SConfirmation)
	        .setMessage(R.string.SDrawingIsNotSavedAreYouSureYouWantToQuit)
		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int id) {
		    		TDrawingEditor.this.finish();
		    	}
		    })
		    .setNegativeButton(R.string.SNo,null)
		    .show();
		}
		else
			finish();
	}	
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_ADDPICTURE: 
        	if (resultCode == RESULT_OK) {  
				File F = PictureDrawingProcess_GetPictureTempFile(this);
				if (F.exists()) {
					try {
						PictureDrawingProcess_AddPictureFromFile(F);
					}
					catch (Throwable E) {
						String S = E.getMessage();
						if (S == null)
							S = E.getClass().getName();
	        			Toast.makeText(this, S, Toast.LENGTH_SHORT).show();  						
					}
				}
				else
        			Toast.makeText(this, R.string.SImageWasNotPrepared, Toast.LENGTH_SHORT).show();  
        	}  
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private Bitmap BackgroundImage_ReCreate(int width, int height) {
    	Bitmap Result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    	Result.eraseColor(Drawings_Descriptor.BackgroundColor);
    	return Result;
    }
    
    private void Update() {
		btnDrawingEditorUndo.setEnabled(Drawings_HistoryIndex > 0);
		btnDrawingEditorClear.setEnabled(Drawings_HistoryIndex > 0);
		btnDrawingEditorOperations.setEnabled(true);
    }
    
    private byte[] ZipByteArray(byte[] BA) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ZOutputStream out = new ZOutputStream(bos,JZlib.Z_BEST_COMPRESSION);
            try {
                out.write(BA);
            }
            finally
            {
                out.close();
            }
            return bos.toByteArray(); //. ->
        }
        finally {
            bos.close();
        }
	}
	
	private byte[] UnzipByteArray(byte[] BA, int Idx, int Size) throws IOException {
		ByteArrayInputStream BIS = new ByteArrayInputStream(BA, Idx,Size);
		try {
			ZInputStream ZIS = new ZInputStream(BIS);
			try {
				byte[] Buffer = new byte[8192];
				int ReadSize;
				ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
				try {
					while ((ReadSize = ZIS.read(Buffer)) > 0) 
						BOS.write(Buffer, 0,ReadSize);
					//.
					return BOS.toByteArray();
				}
				finally {
					BOS.close();
				}
			}
			finally {
				ZIS.close();
			}
		}
		finally {
			BIS.close();
		}
	}
	
	public byte[] ToByteArray() throws Exception {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			short Version = 1;
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(Version);
			BOS.write(BA);
			BOS.write(ZipByteArray(Drawings_ToByteArrayV1()));
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws Exception {
		short Version = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
		switch (Version) {
		
		case 1:
			Drawings_FromByteArrayV1(UnzipByteArray(BA, Idx,BA.length-Idx),0);
			Idx = BA.length;
			break; //. >
			
		default:
			throw new IOException("unknown data version: "+Short.toString(Version)); //. =>
		}
		//.
		return Idx;
	}
	
	public void ToFile(String FN) throws Exception {
		File F = new File(FN);
    	String Folder = F.getParent(); File FF = new File(Folder); FF.mkdirs();
    	//.
		FileOutputStream FOS = new FileOutputStream(FN);
        try
        {
        	byte[] BA = ToByteArray();
        	FOS.write(BA);
        }
        finally
        {
        	FOS.close();
        }
	}	
	
	public boolean FromFile(String FN) throws Exception {
		File F = new File(FN);
		if (F.exists()) { 
	    	FileInputStream FIS = new FileInputStream(FN);
	    	try {
	    			byte[] BA = new byte[(int)F.length()];
	    			FIS.read(BA);
	    			//.
	    			int Idx = 0;
	    			FromByteArray(BA, Idx);
	    			//.
	    			return true; //. ->
	    	}
			finally
			{
				FIS.close(); 
			}
		}
		else
			return false; //. ->
	}
	
	private String 	DrawingsFile_Name = "";
	private boolean DrawingsFile_flReadOnly = false;
	
	public boolean DrawingsFile_Load() throws Exception {
		return FromFile(DrawingsFile_Name);
	}
	
	public void DrawingsFile_Save() throws Exception {
		ToFile(DrawingsFile_Name);
		//.
		Drawings_flSaved = true;
		Drawings_flChanged = false;
	}
	public boolean DrawingsFile_Exists() {
		return (new File(DrawingsFile_Name)).exists();
	}
	
	public boolean DrawingsFile_Delete() {
		boolean R = (new File(DrawingsFile_Name)).delete();
		//.
		Drawings_flSaved = true;
		Drawings_flChanged = false;
		//.
		return R;
	}
	
	public void DrawingsFile_ResetChanges() {
		Drawings_flSaved = true;
		Drawings_flChanged = false;
	}
	
	public synchronized int GetMode() {
		return Mode;
	}
	
	public void SetMode(int pMode) {
		if (Mode == pMode)
			return; //. ->
		//.
		int LastMode;
		synchronized (this) {
			LastMode = Mode;
		}
		switch (LastMode) {
		case MODE_DRAWING:
			Drawings_Hide();
			break; //. >
			
		case MODE_MOVING:
			Drawings_Hide();
			break; //. >
			
		case MODE_SETTINGS:
			Settings_Hide();
			break; //. >
		}
		//.
		switch (pMode) {
		case MODE_DRAWING:
			Drawings_Show();
			break; //. >
			
		case MODE_MOVING:
			Drawings_Show();
			break; //. >
			
		case MODE_SETTINGS:
			Settings_Show();
			break; //. >
		}
		synchronized (this) {
			Mode = pMode;
		}
		//.
		cbDrawingEditorMode.setChecked(pMode == MODE_DRAWING);
		//.
		if (SurfaceUpdating != null)
			SurfaceUpdating.Start();
	}
	
    private class TChangesCommitting extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION	 			= 0;
    	private static final int MESSAGE_COMMITTED 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private boolean flCloseEditor;
    	
        private ProgressDialog progressDialog; 
    	
    	public TChangesCommitting(boolean pflCloseEditor) {
    		flCloseEditor = pflCloseEditor;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				DrawingsFile_Save();
    				//.
    				Thread.sleep(100);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_COMMITTED).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (Exception E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getClass().getSimpleName())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	Exception E = (Exception)msg.obj;
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
	                Toast.makeText(TDrawingEditor.this, S, Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_COMMITTED:
	            	if (flCloseEditor) {
	                	Intent intent = TDrawingEditor.this.getIntent();
	                	intent.putExtra("FileName",DrawingsFile_Name);
	                    //.
	                	setResult(Activity.RESULT_OK,intent);
	            		TDrawingEditor.this.finish();
	            	}
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(TDrawingEditor.this);    
	            	progressDialog.setMessage(getString(R.string.SCommitting));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(true); 
	            	progressDialog.setCancelable(false);
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
	                if ((!isFinishing()) && progressDialog.isShowing()) 
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
	
	@SuppressWarnings("unused")
	private boolean DrawingProcess_IsProcessing() {
		return (LineDrawingProcess_flProcessing);
	}
	
    //. Line Drawing
    
	public boolean 			LineDrawingProcess_flProcessing;
	private float 			LineDrawingProcess_LastX;
	private float 			LineDrawingProcess_LastY;
	private Paint			LineDrawingProcess_Brush = null;
	private Blur			LineDrawingProcess_Brush_Blur_Style = Blur.SOLID;
	private float			LineDrawingProcess_Brush_Blur_Radius = 0.0F;
	private Paint			LineDrawingProcess_MarkerPaint;
	private TLineDrawing	LineDrawing;	
	
	private void LineDrawingProcess_Initialize() {
		LineDrawingProcess_flProcessing = false;
		LineDrawingProcess_LastX = -1;
		LineDrawingProcess_Brush = new Paint();
		LineDrawingProcess_Brush.set(Settings_Brush);
		LineDrawingProcess_Brush_Blur_Style = Settings_Brush_Blur_Style;
		LineDrawingProcess_Brush_Blur_Radius = Settings_Brush_Blur_Radius;
		if (LineDrawingProcess_MarkerPaint == null)  
			LineDrawingProcess_MarkerPaint = new Paint();
		LineDrawing = null;
	}
	
	private void LineDrawingProcess_Finalize() {
		LineDrawing = null;
	}
	
	private void LineDrawingProcess_Begin(float X, float Y) {
		LineDrawing = new TLineDrawing(LineDrawingProcess_Brush,new TLineDrawing.TBrushBlurMaskFilter(LineDrawingProcess_Brush_Blur_Style,LineDrawingProcess_Brush_Blur_Radius));
		//.
		DrawableImageCanvas.drawCircle(X,Y, LineDrawingProcess_Brush.getStrokeWidth()*0.5F, LineDrawingProcess_Brush);
		//.
		TDrawingNode DN = new TDrawingNode(X,Y);
		LineDrawing.Nodes.add(DN); 
		//.
		LineDrawingProcess_LastX = X;
		LineDrawingProcess_LastY = Y;
		//.
		LineDrawingProcess_MarkerPaint.setColor(LineDrawingProcess_Brush.getColor());
		int CC = LineDrawingProcess_Brush.getColor();
		if (CC != Color.TRANSPARENT)
			LineDrawingProcess_MarkerPaint.setColor(CC);
		else
			CC = Color.RED;
		LineDrawingProcess_MarkerPaint.setAlpha(127);
		//.
		LineDrawingProcess_flProcessing = true;
		//.
		SurfaceUpdating.Start();
	}
	
	private void LineDrawingProcess_End() {
		if (LineDrawingProcess_flProcessing) {
			LineDrawingProcess_flProcessing = false;
			//.
			Drawings_Add(LineDrawing);
			//.
			LineDrawingProcess_LastX = -1;
			//.
			SurfaceUpdating.Start();
			//.
			btnDrawingEditorUndo.setEnabled(true);
			btnDrawingEditorRedo.setEnabled(false);
			btnDrawingEditorClear.setEnabled(true);
		}
	}
	
	private void LineDrawingProcess_Draw(float X, float Y) {
		if (LineDrawingProcess_flProcessing) {
			DrawableImageCanvas.drawLine(X,Y, LineDrawingProcess_LastX,LineDrawingProcess_LastY, LineDrawingProcess_Brush);
			//.
			TDrawingNode DN = new TDrawingNode(X,Y);
			LineDrawing.Nodes.add(DN); 
			//.
			LineDrawingProcess_LastX = X;
			LineDrawingProcess_LastY = Y;
			//.
			SurfaceUpdating.Start();
		}
	}
	
	//. Picture drawing 
	
	public static int		PictureDrawing_MaxPictureResolution = 1024;
	private TPictureDrawing	PictureDrawing;	
	
	private void PictureDrawingProcess_Initialize() {
		PictureDrawing = null;
	}
	
	private void PictureDrawingProcess_Finalize() {
		if (PictureDrawing != null) {
			PictureDrawing.Destroy();
			PictureDrawing = null;			
		}
	}
	
    private static File PictureDrawingProcess_GetPictureTempFile(Context context) {
  	  return new File(Environment.getExternalStorageDirectory(),"picture.jpg");
    }
  
	private void PictureDrawingProcess_Begin(Bitmap Picture, float X, float Y) {
		if (PictureDrawing != null) {
			PictureDrawing.Destroy();
			PictureDrawing = null;			
		}
		//.
		PictureDrawing = new TPictureDrawing(Picture, X,Y);
	}
	
	private void PictureDrawingProcess_End() throws Exception {
		Drawings_Add(PictureDrawing);
		PictureDrawing = null;
		//.
		btnDrawingEditorUndo.setEnabled(true);
		btnDrawingEditorRedo.setEnabled(false);
		btnDrawingEditorClear.setEnabled(true);
	}
	
	private void PictureDrawingProcess_Draw() throws Exception {
		if (DrawableImageCanvas != null) {
			PictureDrawing.Paint(DrawableImageCanvas);
			//.
			if (SurfaceUpdating != null)
				SurfaceUpdating.Start();
		}
	}
	
	private void PictureDrawingProcess_AddPicture(Bitmap Picture, float X, float Y) throws Exception {
		PictureDrawingProcess_Begin(Picture, X,Y);
		PictureDrawingProcess_Draw();
		PictureDrawingProcess_End();
	}
	
	private void PictureDrawingProcess_AddPictureFromFile(File F) throws Exception {
		FileInputStream fs = new FileInputStream(F);
		try
		{
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither=false;
			options.inPurgeable=true;
			options.inInputShareable=true;
			options.inTempStorage=new byte[1024*1024*3]; 							
			Rect rect = new Rect();
			Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), rect, options);
			PictureDrawingProcess_AddPicture(bitmap, 0.0F,0.0F);
			/* try {
				int ImageMaxSize = options.outWidth;
				if (options.outHeight > ImageMaxSize)
					ImageMaxSize = options.outHeight;
				float MaxSize = PictureDrawing_MaxPictureResolution;
				float Scale = MaxSize/ImageMaxSize; 
				Matrix matrix = new Matrix();     
				matrix.postScale(Scale,Scale);
				//.
				Bitmap Picture = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
				PictureDrawingProcess_AddPicture(Picture, 0.0F,0.0F);
			}
			finally {
				bitmap.recycle();
			}*/
		}
		finally {
			fs.close();
		}
	}
	
	//. Summary drawings 
	
	private static class TDrawingsDescriptor {
		
		public double 	Timestamp = OleDate.UTCCurrentTimestamp();
		public String 	Name = "";
		public int 		BackgroundColor = Color.WHITE;
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
	    	Timestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		Name = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Name = "";
	    	BackgroundColor = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; 
	    	return Idx;
		}

		public byte[] ToByteArray() throws IOException {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(Timestamp);
				BOS.write(BA);
				//.
				byte[] BA_SL = new byte[1];
				BA_SL[0] = (byte)Name.length();
				BOS.write(BA_SL);
				if (BA_SL[0] > 0)
					BOS.write(Name.getBytes("windows-1251"));
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(BackgroundColor);
				BOS.write(BA);
				//.
				return BOS.toByteArray(); //. ->
			}
			finally {
				BOS.close();
			}
		}
	}
	
	private TDrawingsDescriptor Drawings_Descriptor = new TDrawingsDescriptor();
	private Object 				Drawings_ImageLock = new Object();
	
	private void Drawings_Initialize() {
		Drawings = new ArrayList<TDrawing>(10);
		Drawings_HistoryIndex = 0;
		Drawings_flChanged = false;
		Drawings_flSaved = false;
	}
	
	private void Drawings_Finalize() {
		if (Drawings != null) {
			Drawings_ClearItems();		
			Drawings = null;
		}
	}
	
	private void Drawings_ClearItems() {
		for (int I = 0; I < Drawings.size(); I++) 
			Drawings.get(I).Destroy();
		Drawings.clear();
		Drawings_HistoryIndex = 0;
	}
	
	public byte[] Drawings_ToByteArrayV1() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			BOS.write(Drawings_Descriptor.ToByteArray());
			//.
			byte[] BA;
			int DrawingsCount;
			if (Drawings != null) 
				DrawingsCount = Drawings_HistoryIndex;
			else
				DrawingsCount = 0;
			BA = TDataConverter.ConvertInt32ToBEByteArray(DrawingsCount);
			BOS.write(BA);
			for (int I = 0; I < DrawingsCount; I++) {
				TDrawing Drawing = Drawings.get(I);
				short DrawingTypeID = Drawing.TypeID();
				BA = TDataConverter.ConvertInt16ToBEByteArray(DrawingTypeID);
				BOS.write(BA);
				BA = Drawing.ToByteArray();
				BOS.write(BA);
			}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public int Drawings_FromByteArrayV1(byte[] BA, int Idx) throws IOException {
		Idx = Drawings_Descriptor.FromByteArray(BA, Idx);
		//.
		Drawings.clear();
		Drawings_HistoryIndex = 0;
		//.
		int DrawingsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < DrawingsCount; I++) {
			short DrawingTypeID = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
			TDrawing Drawing = TDrawing.CreateInstance(DrawingTypeID);
			if (Drawing == null)
				throw new IOException("unknown drawing type: "+Short.toString(DrawingTypeID)); //. =>
			Idx = Drawing.FromByteArray(BA, Idx);
			Drawings.add(Drawing);
		}
		Drawings_HistoryIndex = DrawingsCount;
		//.
		TDrawingNode AP = Drawings_GetAveragePosition();
		float dX =  Surface_Width/2.0F-AP.X;
		float dY =  Surface_Height/2.0F-AP.Y;
		Drawings_Translate(dX,dY);
		//.
 		Drawings_flSaved = true;
		Drawings_flChanged = false;
		//.
		return Idx;
	}
	
	private void Drawings_Show() {
		DrawingEditorSurfaceLayout.setVisibility(View.VISIBLE);
	}
	
	private void Drawings_Hide() {
		DrawingEditorSurfaceLayout.setVisibility(View.GONE);
	}
	
	public void Drawings_Add(TDrawing Drawing) {
		if (Drawings_HistoryIndex != Drawings.size()) {
			if (Drawings_HistoryIndex > 0) {
				List<TDrawing> L = Drawings.subList(0,Drawings_HistoryIndex);
				List<TDrawing> LastDrawings = Drawings;
				Drawings = L;
				//. free forgetting items 
				for (int I = Drawings_HistoryIndex; I < LastDrawings.size(); I++)
					LastDrawings.get(I).Destroy();
			}
			else 
				Drawings_ClearItems();
		}
		Drawings.add(Drawing); 
		Drawings_HistoryIndex++;
		Drawings_flChanged = true;
		Drawings_flSaved = false;
	}
	
	public boolean Drawings_Undo() throws Exception {
		if (Drawings_HistoryIndex > 0) {
			Drawings_HistoryIndex--;
			Drawings_flChanged = true;
			Drawings_flSaved = false;
			//.
			Drawings_UpdateImage();
			//.
			return (Drawings_HistoryIndex > 0); //. ->
		}
		else
			return false;
	}
	
	public void Drawings_UndoAll() throws Exception {
		if (Drawings_HistoryIndex > 0) {
			Drawings_HistoryIndex = 0;
			Drawings_flChanged = true;
			Drawings_flSaved = false;
			//.
			Drawings_UpdateImage();
		}
	}
	
	public boolean Drawings_Redo() throws Exception {
		if (Drawings_HistoryIndex < Drawings.size()) {
			Drawings_HistoryIndex++;
			Drawings_flChanged = true;
			Drawings_flSaved = false;
			//.
			Drawings_UpdateImage();
			//.
			return (Drawings_HistoryIndex < Drawings.size()); //. ->
		}
		else
			return false;
	}
	
	public void Drawings_Translate(float dX, float dY) {
		for (int I = 0; I < Drawings.size(); I++) 
			Drawings.get(I).Translate(dX,dY);
		Drawings_RepaintImage();
	}
	
	public TDrawingNode Drawings_GetAveragePosition() {
		TDrawingNode Result = new TDrawingNode();
		int Cnt = 0;
		for (int I = 0; I < Drawings.size(); I++) {
			TDrawingNode Node = Drawings.get(I).GetAveragePosition();
			if (Node != null) {
				Result.X += Node.X;
				Result.Y += Node.Y;
				//.
				Cnt++;
			}
		}
		if (Cnt > 0) {
			Result.X = Result.X/Cnt;
			Result.Y = Result.Y/Cnt;
		}
		return Result;
	}
	
	public void Drawings_UpdateImage() {
		if (DrawableImage == null)
			return; //. ->
		synchronized (Drawings_ImageLock) {
			DrawableImage.eraseColor(Drawings_Descriptor.BackgroundColor);
			if (OriginDrawableImage != null)
				DrawableImageCanvas.drawBitmap(OriginDrawableImage,0,0,null);
			//.
			for (int I = 0; I < Drawings_HistoryIndex; I++) 
				Drawings.get(I).Paint(DrawableImageCanvas);
			//.
			SurfaceUpdating.Start();
		}
	}
	
	public void Drawings_RepaintImage() {
		Moving_Reset();
		//. repaint bitmap from current Drawing
		synchronized (Drawings_ImageLock) {
			if (BackgroundImage != null) {
				BackgroundImage.eraseColor(Color.TRANSPARENT);
				@SuppressWarnings("unused")
				Canvas canvas = new Canvas(BackgroundImage);
				//. draw to background image
				//.
			}
			//.
			if (ForegroundImage != null) {
				ForegroundImage.eraseColor(Color.TRANSPARENT);
				@SuppressWarnings("unused")
				Canvas canvas = new Canvas(ForegroundImage);
				//. draw to foreground image
				//.
			}
			//.
			if (OriginDrawableImage != null) {
				OriginDrawableImage.eraseColor(Color.TRANSPARENT);
				@SuppressWarnings("unused")
				Canvas canvas = new Canvas(OriginDrawableImage);
				//. draw to origin image
				//.
			}
			//.
			DrawableImage.eraseColor(Drawings_Descriptor.BackgroundColor);
			if (OriginDrawableImage != null)
				DrawableImageCanvas.drawBitmap(OriginDrawableImage,0,0,null);
			//.
			for (int I = 0; I < Drawings_HistoryIndex; I++) 
				Drawings.get(I).Paint(DrawableImageCanvas);
		}
		//.
		SurfaceUpdating.Start();
	}

	public void Drawings_Clear() throws Exception {
		if (Drawings.size() == 0)
			return; //. ->
		Drawings_ClearItems();
		Drawings_flChanged = true;
		Drawings_flSaved = false;
		//.
		Drawings_UpdateImage();
	}
	
	private Object 			Moving_Lock = new Object();
    public boolean 			Moving_flProcessing = false;
	private float 			Moving_dX;
	private float 			Moving_dY;
	private float 			Moving_OrgX;
	private float 			Moving_OrgY;
	private float 			Moving_X;
	private float 			Moving_Y;
	
	private void Moving_Reset() {
		synchronized (Moving_Lock) {
			Moving_OrgX = -1;
			//.
			Moving_dX = 0;
			Moving_dY = 0;
		}
	}
	
	private void Moving_Begin(float X, float Y) {
		synchronized (Moving_Lock) {
			Moving_OrgX = X;
			Moving_OrgY = Y;
			Moving_X = Moving_OrgX;
			Moving_Y = Moving_OrgY;
			//.
			Moving_flProcessing = true;
		}
		//.
		SurfaceUpdating.Start();
	}
	
	private void Moving_End() throws Exception {
		float dX,dY;
		synchronized (Moving_Lock) {
			Moving_flProcessing = false;
			//.
			dX = (Moving_X-Moving_OrgX);
			dY = (Moving_Y-Moving_OrgY);
			Moving_dX += dX;
			Moving_dY += dY;
			//.
			Moving_OrgX = -1;
		}
		Drawings_Translate(dX,dY);
		//.
		SurfaceUpdating.Start();
	}
	
	private void Moving_Move(float X, float Y) {
		synchronized (Moving_Lock) {
			if (Moving_flProcessing) {
				Moving_X = X;
				Moving_Y = Y;
				//.
				SurfaceUpdating.Start();
			}
		}
	}
	
	private TSettingsTestImage 	Settings_TestImage;
	private Paint				Settings_DefaultBrush = new Paint();
	private Paint				Settings_Brush = new Paint();
	private float				Settings_BrushMaxWidth;
	private Blur				Settings_Brush_Blur_Style;
	private float				Settings_Brush_Blur_Radius;
	//.
	private	RelativeLayout 	DrawingEditorSettingsLayout;
	private	Button 			btnBrushColor;
	private Button 			btnBrushTransparentColor;
	private SeekBar 		sbBrushWidth;
	private SeekBar 		sbBrushBlurRadius;
    private Spinner 		spRWEBrushBlurStyle;
    private Button 			btnOK;
    private Button 			btnCancel;
	
	
	private void Settings_Initialize() {
		Settings_BrushMaxWidth = 96F*metrics.density;
		Settings_Brush.setAntiAlias(true);
		Settings_Brush.setStrokeCap(Cap.ROUND);
		SharedPreferences Preferences = getPreferences(MODE_PRIVATE);
		Settings_Brush.setColor(Preferences.getInt("DrawingEditor_Settings_Brush_Color", Color.RED));
		Settings_Brush.setStrokeWidth(Preferences.getFloat("DrawingEditor_Settings_Brush_Width", 3.0F*metrics.density));
		int BS = Preferences.getInt("DrawingEditor_Settings_Brush_Blur_Style", 0);
    	switch (BS) {
    	
    	case 0: 
        	Settings_Brush_Blur_Style = Blur.NORMAL;
    		break; //. >

    	case 1: 
        	Settings_Brush_Blur_Style = Blur.SOLID;
    		break; //. >

    	case 2: 
        	Settings_Brush_Blur_Style = Blur.INNER;
    		break; //. >

    	case 3: 
        	Settings_Brush_Blur_Style = Blur.OUTER;
    		break; //. >
    	}
		Settings_Brush_Blur_Radius = Preferences.getFloat("DrawingEditor_Settings_Brush_Blur_Radius", 0.0F);
		if (Settings_Brush.getColor() != Color.TRANSPARENT)
			Settings_Brush.setXfermode(Settings_DefaultBrush.getXfermode());
		else
			Settings_Brush.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OUT)); 
		if (Settings_Brush_Blur_Radius > 0.0F)
			Settings_Brush.setMaskFilter(new BlurMaskFilter(Settings_Brush_Blur_Radius,Settings_Brush_Blur_Style));
		else 
			Settings_Brush.setMaskFilter(null);
		//.
		DrawingEditorSettingsLayout = (RelativeLayout)findViewById(R.id.DrawingEditorSettingsLayout);
		//.
		Settings_TestImage = (TSettingsTestImage)findViewById(R.id.ivRWESettingsTest);
		//.
		btnBrushColor = (Button)findViewById(R.id.btnRWEBrushColor);
		btnBrushColor.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		ColorPicker ColorDialog = new ColorPicker(TDrawingEditor.this, new ColorPicker.OnColorChangedListener() {
        			@Override
        			public void colorChanged(int color) {
        				Settings_Brush.setXfermode(Settings_DefaultBrush.getXfermode()); 
        				Settings_Brush.setColor(color);
        				//.
    					Settings_TestImage.postInvalidate();
        			}
        		},Settings_Brush.getColor());    
        		ColorDialog.show();
            }
        });
		//.
		btnBrushTransparentColor = (Button)findViewById(R.id.btnRWEBrushTransparentColor);
		btnBrushTransparentColor.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
				Settings_Brush.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OUT)); 
				Settings_Brush.setColor(Color.TRANSPARENT);
				//.
				Settings_TestImage.postInvalidate();
            }
        });
		//.
		sbBrushWidth = (SeekBar)findViewById(R.id.sbRWEBrushSize);
		sbBrushWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					Settings_Brush.setStrokeWidth((float)progress);
					Settings_TestImage.postInvalidate();
				}
			}
		});
		//.
		sbBrushBlurRadius = (SeekBar)findViewById(R.id.sbRWEBrushBlurRadius);
		sbBrushBlurRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

			public void onStartTrackingTouch(SeekBar seekBar) {
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
			}

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (fromUser) {
					Settings_Brush_Blur_Radius = (float)progress;
					if (Settings_Brush_Blur_Radius > 0.0F)
						Settings_Brush.setMaskFilter(new BlurMaskFilter(Settings_Brush_Blur_Radius,Settings_Brush_Blur_Style));
					else 
						Settings_Brush.setMaskFilter(null);
					Settings_TestImage.postInvalidate();
				}
			}
		});
        //.
		spRWEBrushBlurStyle = (Spinner)findViewById(R.id.spRWEBrushBlurStyle);
        String[] SA = new String[4]; 
        SA[0] = getString(R.string.SNormal);
        SA[1] = getString(R.string.SSolid);
        SA[2] = getString(R.string.SInner);
        SA[3] = getString(R.string.SOuter);
        ArrayAdapter<String> saRWEBrushBlurStyle = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saRWEBrushBlurStyle.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spRWEBrushBlurStyle.setAdapter(saRWEBrushBlurStyle);
        spRWEBrushBlurStyle.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	switch (position) {
            	
            	case 0: 
                	Settings_Brush_Blur_Style = Blur.NORMAL;
            		break; //. >

            	case 1: 
                	Settings_Brush_Blur_Style = Blur.SOLID;
            		break; //. >

            	case 2: 
                	Settings_Brush_Blur_Style = Blur.INNER;
            		break; //. >

            	case 3: 
                	Settings_Brush_Blur_Style = Blur.OUTER;
            		break; //. >
            	}
            	//.
				if (Settings_Brush_Blur_Radius > 0.0F)
					Settings_Brush.setMaskFilter(new BlurMaskFilter(Settings_Brush_Blur_Radius,Settings_Brush_Blur_Style));
				else 
					Settings_Brush.setMaskFilter(null);
				Settings_TestImage.postInvalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            	Settings_Brush_Blur_Radius = 0.0F;
            	//.
				Settings_Brush.setMaskFilter(null);
				Settings_TestImage.postInvalidate();
            }
        });        
		//.
		btnOK = (Button)findViewById(R.id.btnRWESettingsOk);
		btnOK.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Settings_Apply();
        		SetMode(MODE_DRAWING);        		
            }
        });
		//.
		btnCancel = (Button)findViewById(R.id.btnRWESettingsCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		SetMode(MODE_DRAWING);        		
            }
        });
	}

	private void Settings_Finalize() {
	}
	
	private void Settings_Show() {
		Settings_Brush.set(LineDrawingProcess_Brush);
		Settings_TestImage.Brush = Settings_Brush;
		//.
		sbBrushWidth.setMax((int)Settings_BrushMaxWidth);
		sbBrushWidth.setProgress((int)Settings_Brush.getStrokeWidth());
		//.
		sbBrushBlurRadius.setMax((int)(Settings_BrushMaxWidth/2));
		sbBrushBlurRadius.setProgress((int)Settings_Brush_Blur_Radius);
        //.
    	int SI = 0;
    	switch (Settings_Brush_Blur_Style) {

    	case NORMAL: 
    		SI = 0;
    		break; //. >
    		
    	case SOLID: 
    		SI = 1;
    		break; //. >

    	case INNER: 
    		SI = 2;
    		break; //. >
    		
    	case OUTER: 
    		SI = 3;
    		break; //. >
    	}
    	spRWEBrushBlurStyle.setSelection(SI);
		//.
		DrawingEditorSettingsLayout.setVisibility(View.VISIBLE);
	}
	
	private void Settings_Hide() {
		DrawingEditorSettingsLayout.setVisibility(View.GONE);
	}
	
	private void Settings_Apply() {
		SharedPreferences Preferences = getPreferences(MODE_PRIVATE);
		Editor editor = Preferences.edit();
		editor.putInt("DrawingEditor_Settings_Brush_Color", Settings_Brush.getColor());
		editor.putFloat("DrawingEditor_Settings_Brush_Width", Settings_Brush.getStrokeWidth());
    	int BS = 0;
    	switch (Settings_Brush_Blur_Style) {

    	case NORMAL: 
    		BS = 0;
    		break; //. >
    		
    	case SOLID: 
    		BS = 1;
    		break; //. >

    	case INNER: 
    		BS = 2;
    		break; //. >
    		
    	case OUTER: 
    		BS = 3;
    		break; //. >
    	}
    	editor.putInt("DrawingEditor_Settings_Brush_Blur_Style", BS);
    	editor.putFloat("DrawingEditor_Settings_Brush_Blur_Radius", Settings_Brush_Blur_Radius);
    	editor.commit();
		//.
    	LineDrawingProcess_Brush = new Paint();
		LineDrawingProcess_Brush.set(Settings_Brush);
		LineDrawingProcess_Brush_Blur_Style = Settings_Brush_Blur_Style;
		LineDrawingProcess_Brush_Blur_Radius = Settings_Brush_Blur_Radius;
	}
	
	private void Settings_Brush_SetColorAndApply(int color) {
		Settings_Brush.setColor(color);
		//.
		SharedPreferences Preferences = getPreferences(MODE_PRIVATE);
		Editor editor = Preferences.edit();
		editor.putInt("DrawingEditor_Settings_Brush_Color", Settings_Brush.getColor());
    	editor.commit();
		//.
    	LineDrawingProcess_Brush = new Paint();
		LineDrawingProcess_Brush.set(Settings_Brush);
	}

	private void Settings_Brush_SetWidthAndApply(float width) {
		Settings_Brush.setStrokeWidth(width);
		//.
		SharedPreferences Preferences = getPreferences(MODE_PRIVATE);
		Editor editor = Preferences.edit();
		editor.putFloat("DrawingEditor_Settings_Brush_Width", Settings_Brush.getStrokeWidth());
    	editor.commit();
		//.
    	LineDrawingProcess_Brush = new Paint();
		LineDrawingProcess_Brush.set(Settings_Brush);
	}
}
