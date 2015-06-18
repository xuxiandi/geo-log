package com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
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

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Image.Color.ColorPicker;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawing;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawingNode;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.Data.Types.Image.Drawing.TImageDrawing;
import com.geoscope.Classes.Data.Types.Image.Drawing.TLineDrawing;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TSpaceContainer;
import com.geoscope.GeoEye.Space.Defines.TSpaceContainers;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

@SuppressLint("HandlerLeak")
public class TDrawingEditor extends Activity implements OnTouchListener {

	public static final int MODE_NONE 				= 0;
	public static final int MODE_DRAWING 			= 1;
	public static final int MODE_MOVINGANDSCALING	= 2;
	public static final int MODE_SETTINGS 			= 4;
	//.
	public static final int BACKGROUND_STYLE_COLOR	= 1;
	public static final int BACKGROUND_STYLE_SPACE	= 2;
	//.
	public static final int REQUEST_ADDIMAGE 			= 1;
	public static final int REQUEST_ADDIMAGEFROMFILE 	= 2;
	
	public class TSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {
		
		public SurfaceHolder _SurfaceHolder;
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        	try {
    			if (SurfaceUpdating != null) {
    				SurfaceUpdating.Destroy();
    				SurfaceUpdating = null;
    			}
    			//.
    			_SurfaceHolder = holder;
				SurfaceUpdating = new TSurfaceUpdating();
    			//.
    			Surface_Width = width;
    			Surface_Height = height;
    			//.
    			if (Background != null) 
    				Background.recycle();
    			try {
    				Background = Background_ReCreate(width, height);
    			} catch (Exception E) {
    				Background = null;
    			}
    			if (BackgroundImage != null) 
    				BackgroundImage.recycle();
    			BackgroundImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
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
				Drawings_RepaintImage();
				//.
				if (flLoadDrawings) {
					flLoadDrawings = false;
					//.
					if (DrawingsFile_Exists()) {
			    		TAsyncProcessing Processing = new TAsyncProcessing(TDrawingEditor.this,getString(R.string.SWaitAMoment)) {
			    			@Override
			    			public void Process() throws Exception {
			        			DrawingsFile_Load();
					    		Thread.sleep(100); 
			    			}
			    			@Override 
			    			public void DoOnCompleted() throws Exception {
			    				if (Containers_IsInitialized()) {
			    					BackgroundStyle = BACKGROUND_STYLE_SPACE;
			    					cbDrawingEditorSpaceBackground.setChecked(true);
			    					//.
			    					if (Containers.size() > 0) {
			    						TSpaceContainer LastContainer = Containers.get(Containers.size()-1);
			    						//.
			    						TReflectionWindowStruc RWS = LastContainer.RW; 
			    				    	Reflector().Component.SetReflectionWindow(RWS,false);
			    				    	Containers.remove(LastContainer);
			    				    	//.
			    				    	TSpaceContainer StartedContainer = Containers_StartCurrentContainer();
			    				    	StartedContainer.RW = LastContainer.RW;
			    				    	StartedContainer.LevelTileContainer = LastContainer.LevelTileContainer;
			    				    	StartedContainer.flModified = LastContainer.flModified;
			    					}
			    					else
		        		    			Containers_StartCurrentContainer();
			    				}
			    				//.
			    				Drawings_RepaintImage();
			    				//.
			    				Update();
			    			}
			    			@Override
			    			public void DoOnException(Exception E) {
			    				Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();
			    			}
			    		};
			    		Processing.Start();
					}
					else 
						if ((ImageFileName != null) && flLoadImageFile) {
							flLoadImageFile = false;
							//.
				    		TAsyncProcessing ImageLoading = new TAsyncProcessing(TDrawingEditor.this,getString(R.string.SWaitAMoment)) {
				    			
				    			private Bitmap Image = null;
				    			
				    			@Override
				    			public void Process() throws Exception {
				    				File F = new File(ImageFileName);
				    				if (F.exists()) {
					    				FileInputStream FS = new FileInputStream(F);
					    				try {
					    					BitmapFactory.Options options = new BitmapFactory.Options();
					    					options.inDither = false;
					    					options.inPurgeable = true;
					    					options.inInputShareable = true;
					    					options.inTempStorage = new byte[1024*256]; 							
					    					Rect rect = new Rect();
					    					Image = BitmapFactory.decodeFileDescriptor(FS.getFD(), rect, options);
					    				}
					    				finally {
					    					FS.close();
					    				}
				    				}
				    				//.
						    		Thread.sleep(100); 
				    			}
				    			
				    			@Override 
				    			public void DoOnCompleted() throws Exception {
				    				if (Image != null)
				    					ImageDrawingProcess_AddImage(Image, 0.0F,0.0F, Surface_Width,Surface_Height);
				    				//.
				    				if (Containers_IsInitialized())
				    					Containers_StartCurrentContainer();
				    				//.
				    				Update();
				    			}
				    			
				    			@Override
				    			public void DoOnException(Exception E) {
				    				Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();
				    			}
				    		};
				    		ImageLoading.Start();
						}
						else
		    				if (Containers_IsInitialized())
		    					Containers_StartCurrentContainer();
				}
				//.
				SurfaceUpdating.Start();
			} catch (Exception E) {
				Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();  
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			_SurfaceHolder = holder;
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
					Matrix Transformatrix = new Matrix(); 
					while (!flCancel) {
						ProcessSignal.WaitOne();
						if (flCancel)
							return; //. ->
						//. translating
						float dX;
						float dY;
						synchronized (Moving_Lock) {
							dX = Moving_dX;
							dY = Moving_dY;
							if (Moving_flProcessing) {
								dX += (Moving_X-Moving_OrgX);
								dY += (Moving_Y-Moving_OrgY);
							}
						}
						//. scaling
						float Xbind;
						float Ybind;
						float Scale = 1.0F;
						synchronized (Scaling_Lock) {
							Xbind = Scaling_Xbind;
							Ybind = Scaling_Ybind;
							if (Scaling_flProcessing) 
								Scale = Scaling_Scale;
						}
						//.
						Transformatrix.reset();
						Transformatrix.postScale(Scale,Scale, Xbind,Ybind);
						Transformatrix.postTranslate(dX,dY);
						//.
						Canvas canvas = SurfaceHolderCallbackHandler._SurfaceHolder.lockCanvas();
						try {
							synchronized (Drawings_ImageLock) {
								if (Background != null)
									canvas.drawBitmap(Background, 0,0, null);
								if (BackgroundImage != null)
									canvas.drawBitmap(BackgroundImage, Transformatrix, null);
								if (DrawableImage != null)
									canvas.drawBitmap(DrawableImage, Transformatrix, null);
								if (ForegroundImage != null)
									canvas.drawBitmap(ForegroundImage, Transformatrix, null);
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
							//. draw status string
							ShowStatus(canvas);
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
		
        private Paint ShowStatus_Paint = null;
        
        private void ShowStatus(Canvas canvas) {
        	int _Mode = GetMode();
            String S = null;
        	int TextColor;
        	switch (_Mode) {
        	
        	case MODE_DRAWING:
        		S = getApplicationContext().getString(R.string.SDrawing);
            	TextColor = Color.GREEN;
        		break; //. >
        		
        	case MODE_MOVINGANDSCALING:
        		if (!DrawingsFile_flReadOnly)
        			if (Scaling_IsAvailable())
        				S = getApplicationContext().getString(R.string.SMovingAndScaling);
        			else
        				S = getApplicationContext().getString(R.string.SMoving);
            	TextColor = Color.BLUE;
        		break; //. >
        		
        	default:
        		S = "?"; 
            	TextColor = Color.BLACK;
        		break; //. >
        	}
            //.
            if (Containers_IsInitialized() && Containers_CurrentContainer_flUpdating) {
            	if (S != null)
            		S = S+": "+getApplicationContext().getString(R.string.SImageUpdating);
            	else
            		S = getApplicationContext().getString(R.string.SImageUpdating);
            	TextColor = Color.RED;
            }
            //.
            if (S == null)
            	return; //. ->
        	//.
            if (ShowStatus_Paint == null) {
            	ShowStatus_Paint = new Paint();            	
                ShowStatus_Paint.setTextSize(16.0F*metrics.density);
                ShowStatus_Paint.setAntiAlias(true);
            }
            float W = ShowStatus_Paint.measureText(S);
            float H = ShowStatus_Paint.getTextSize();
            float Left = ((DrawableImage.getWidth()-W)/2.0F);
            float Top = (DrawableImage.getHeight()-H);
            ShowStatus_Paint.setColor(Color.GRAY);
            ShowStatus_Paint.setAlpha(100);
    		canvas.drawRect(Left,Top, Left+W,Top+H, ShowStatus_Paint);
			if (Containers_CurrentContainer_flUpdating && (Containers_CurrentContainer_Updating_ProgressPercentage > 0)) {
				ShowStatus_Paint.setColor(Color.WHITE);
				ShowStatus_Paint.setAlpha(150);
				float PW = W*Containers_CurrentContainer_Updating_ProgressPercentage/100.0F;
				canvas.drawRect(Left, Top, Left + PW, Top + H, ShowStatus_Paint);
			}    		
            ShowStatus_Paint.setStyle(Paint.Style.FILL);
            ShowStatus_Paint.setColor(Color.BLACK);
            ShowStatus_Paint.setAlpha(100);
            canvas.drawText(S, Left+1,Top+H-4+1, ShowStatus_Paint);
            ShowStatus_Paint.setColor(TextColor);
            canvas.drawText(S, Left,Top+H-4, ShowStatus_Paint);            
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
	
	private boolean flExists = false;
	//.
	private boolean flLoadImageFile = true;
	private String 	ImageFileName = null;
	//.
	private boolean flLoadDrawings = true;
	//.
	private Paint paint = new Paint();
	//.
	private Bitmap 		Background = null;
	private Bitmap 		BackgroundImage = null;
	//.
	private Bitmap ForegroundImage = null;
	//.
	private Bitmap OriginDrawableImage = null;
	//.
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
	private SurfaceView 					Surface;
	private int								Surface_Width;
	private int								Surface_Height;
	private TSurfaceHolderCallbackHandler 	SurfaceHolderCallbackHandler = new TSurfaceHolderCallbackHandler();
	private TSurfaceUpdating 				SurfaceUpdating = null;
	//.
	private int Mode = MODE_NONE;
	//.
	private int BackgroundStyle = BACKGROUND_STYLE_COLOR;
	//.
	private RelativeLayout 	DrawingEditorSurfaceLayout;
	private LinearLayout	DrawingEditorSurfaceControlLayout;
	private CheckBox 		cbDrawingEditorMode;
	private CheckBox 		cbDrawingEditorSpaceBackground;
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
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);		
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
			finish();
			return; //. ->
		}
		//.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	ImageFileName = extras.getString("ImageFileName");
        	//.
        	DrawingsFile_Name = extras.getString("FileName");
        	DrawingsFile_Format = extras.getString("FileFormat");
        	DrawingsFile_flReadOnly = extras.getBoolean("ReadOnly");
        	DrawingsFile_flSpaceContainersAvailable = extras.getBoolean("SpaceContainersAvailable");
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
					SetMode(MODE_MOVINGANDSCALING);
			}
		});
		cbDrawingEditorSpaceBackground = (CheckBox)findViewById(R.id.cbDrawingEditorSpaceBackground);        
		cbDrawingEditorSpaceBackground.setChecked(false);
		cbDrawingEditorSpaceBackground.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
            	try {
    				if (cbDrawingEditorSpaceBackground.isChecked())
    					SetBackgroundStyle(BACKGROUND_STYLE_SPACE);
    				else
    					SetBackgroundStyle(BACKGROUND_STYLE_COLOR);
		        } 
		        catch (Exception E) {
					Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
			}
		});
		cbDrawingEditorSpaceBackground.setVisibility(DrawingsFile_flSpaceContainersAvailable ? View.VISIBLE : View.GONE);
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
        		if (Drawings.Items_HistoryIndex > 0) {
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
						arg0.dismiss();
						//.
	                	try {
	    					switch (arg1) {
	    					case 0: //. take an image
	    		      		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    		      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(ImageDrawingProcess_GetImageTempFile(TDrawingEditor.this))); 
	    		      		    startActivityForResult(intent, REQUEST_ADDIMAGE);    		
	    						break; //. >
	    						
	    					case 1: //. take an image from file
	    						TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(TDrawingEditor.this, ".BMP,.PNG,.GIF,.JPG,.JPEG", new TFileSystemFileSelector.OpenDialogListener() {
	    				        	
	    				            @Override
	    				            public void OnSelectedFile(String fileName) {
					                    File ChosenFile = new File(fileName);
					                    //.
										try {
						                    ImageDrawingProcess_AddImageFromFile(ChosenFile);
										}
										catch (Throwable E) {
											String S = E.getMessage();
											if (S == null)
												S = E.getClass().getName();
						        			Toast.makeText(TDrawingEditor.this, S, Toast.LENGTH_SHORT).show();  						
										}
	    				            }

									@Override
									public void OnCancel() {
									}
	    				        });
	    				    	FileSelector.show();    	
					            break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TDrawingEditor.this, TDrawingEditor.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
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
				if (!Drawings_IsEmpty())
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
			ImageDrawingProcess_Initialize();
			//.
			Moving_Reset();
			Scaling_Reset();
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
				SetMode(MODE_MOVINGANDSCALING);
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
        //.
        flExists = true;        
	}

	@Override
	protected void onDestroy() {
        flExists = false;
        //.
		try {
			ImageDrawingProcess_Finalize();
			LineDrawingProcess_Finalize();
			//.
			if (Containers_IsInitialized())
				Containers_CurrentContainer_Updater_Stop();
			//.
			Drawings_Finalize();
			Settings_Finalize();
			//.
			if (Containers_IsInitialized())
				Containers_Finalize();
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
					SurfaceUpdating.StartUpdate();
				}
				//.
				return true; //. ->
			}
			if ((BrushWidthPickerBar != null) && BrushWidthPickerBar.Inside(X,Y)) {
				float NewWidth = BrushWidthPickerBar.PickAWidth(X,Y);
				if (NewWidth > 0.0F) {
					Settings_Brush_SetWidthAndApply(NewWidth);
					//.
					SurfaceUpdating.StartUpdate();
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
			
		case MODE_MOVINGANDSCALING:
			switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {
			
			case MotionEvent.ACTION_DOWN:
				Moving_Begin(pEvent.getX(),pEvent.getY());
				break; //. >
				
			case MotionEvent.ACTION_UP:
				try {
					if (Moving_flProcessing)
						Moving_End();
				} catch (Exception E) {
				}
				break;
				
			case MotionEvent.ACTION_POINTER_DOWN:
				switch (pEvent.getPointerCount()) {

				case 1:
					Moving_Begin(pEvent.getX(),pEvent.getY());
					break; // . >

				case 2:
					break; // . >
				}
				break; //. >

			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_CANCEL:
				switch (pEvent.getPointerCount()) {

				case 1:
				case 2:
					try {
						if (Moving_flProcessing)
							Moving_End();
						if (Scaling_flProcessing)
							Scaling_End();
					} catch (Exception E) {
					}
					break; //. >
				}
				break; //. >

			case MotionEvent.ACTION_MOVE:
				switch (pEvent.getPointerCount()) {

				case 1:
					Moving_Move(pEvent.getX(0),pEvent.getY(0));
					break; // . >

				case 2:
					if (Scaling_IsAvailable()) {
						Scaling_Begin(pEvent.getX(0),pEvent.getY(0));
						Scaling_Move(pEvent.getX(1),pEvent.getY(1));
						if (Moving_flProcessing)
							Moving_Reset();
					}
					break; // . >
				}
				break; //. >

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

        case REQUEST_ADDIMAGE: 
        	if (resultCode == RESULT_OK) {  
				File F = ImageDrawingProcess_GetImageTempFile(this);
				if (F.exists()) {
					try {
						ImageDrawingProcess_AddImageFromFile(F);
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
    
    private Bitmap Background_ReCreate(int width, int height) {
    	Bitmap Result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    	Result.eraseColor(Drawings.Descriptor.BackgroundColor);
    	return Result;
    }
    
    private void Update() {
		btnDrawingEditorUndo.setEnabled(Drawings.Items_HistoryIndex > 0);
		btnDrawingEditorClear.setEnabled(Drawings.Items_HistoryIndex > 0);
		btnDrawingEditorOperations.setEnabled(true);
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
			
		case MODE_MOVINGANDSCALING:
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
			
		case MODE_MOVINGANDSCALING:
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
			SurfaceUpdating.StartUpdate();
	}
	
	public void SetBackgroundStyle(int pBackgroundStyle) throws Exception {
		if (BackgroundStyle == pBackgroundStyle)
			return; //. ->
		BackgroundStyle = pBackgroundStyle;
		switch (BackgroundStyle) {
		
		case BACKGROUND_STYLE_COLOR:
			Containers_Finalize();
			//.
			Drawings_RepaintImage();
			//.
			return; //. ->

		case BACKGROUND_STYLE_SPACE:
			Containers_Initialize();
			Containers_StartCurrentContainer();
			//.
			Drawings_RepaintImage();
			//.
			return; //. ->
		}
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
    		super();
    		//.
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
    				if (Drawings_flChanged)
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
	        	try {
		        	if (!flExists)
		        		return; //. ->
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
		                	if (DrawingsFile_Format != null)
			                	intent.putExtra("FileFormat",DrawingsFile_Format);
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
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
	
    private TSpaceContainers 				Containers = null;
	private TTileImagery 					Containers_TileImagery = null;
	private TTileServerProviderCompilation 	Containers_Compilation = null;
    private TSpaceContainer 				Containers_CurrentContainer;
	public boolean 							Containers_CurrentContainer_flUpdating = true;
	public int								Containers_CurrentContainer_Updating_ProgressPercentage = -1;
    
    public void Containers_Initialize(TSpaceContainers pContainers) throws Exception {
    	Containers_Finalize();
    	//.
		Containers_TileImagery = Reflector().Component.SpaceTileImagery;
		if (Containers_TileImagery != null) 
			Containers_Compilation = Containers_TileImagery.ActiveCompilationSet_Get0Item();
		if (Containers_Compilation == null)
			throw new Exception(getString(R.string.SNoTileCompilation)); //. =>
		//.
		if (pContainers != null) {
			Containers = pContainers;
			Containers.PrepareLevelTileContainers(Containers_Compilation);
		}
		else
	    	Containers = new TSpaceContainers();
    	//.
    	Containers_CurrentContainer = null;
    }

    public void Containers_Initialize() throws Exception {
    	Containers_Initialize(null);
    }
    public void Containers_Finalize() {
    	if (Containers_CurrentContainer != null)
    		Containers_CancelCurrentContainer();
    	Containers = null;
    }
    
    public boolean Containers_IsInitialized() {
    	return (Containers != null);
    }
    
	public void Containers_Clear() {
    	Containers.clear();
    	Containers_CurrentContainer = null;
	}
    
	public byte[] Containers_ToByteArray() throws Exception {
		Containers_FinishCurrentContainer();
		Containers_AddCurrentContainer();
		//.
		if (Containers != null) 
			return Containers.ToByteArray(); //. =>
		else 
			return (TDataConverter.ConvertInt32ToLEByteArray(0/*ContainersCount*/)); //. =>
	}
	
	public int Containers_FromByteArray(byte[] BA, int Idx) throws Exception {
    	Containers.clear();
    	Containers_CurrentContainer = null;
    	//.
    	Idx = Containers.FromByteArray(BA, Idx);
    	Containers.PrepareLevelTileContainers(Containers_Compilation);
    	//.
    	return Idx;
	}
    
	private static final int 	Containers_CurrentContainer_Updater_Interval = 100; //. ms 
	private static final int 	Containers_CurrentContainer_Updater_ImageUpdateIntervalCount = 10; //. *Containers_CurrentContainerUpdater_Interval 
	private Timer 				Containers_CurrentContainer_Updater = null;
	private boolean				Containers_CurrentContainer_Updater_flProcessing = false; 
	private int 				Containers_CurrentContainer_Updater_ImageUpdateIntervalCounter; 
	
	public void Containers_CurrentContainer_Updater_Start() {
		Containers_CurrentContainer_Updater_Stop();
		//.
		Containers_CurrentContainer_Updater_ImageUpdateIntervalCounter = 1; 
		Containers_CurrentContainer_Updater_flProcessing = true;
        Containers_CurrentContainer_Updater = new Timer();
        Containers_CurrentContainer_Updater.schedule(new TContainersCurrentContainerUpdaterTask(),Containers_CurrentContainer_Updater_Interval,Containers_CurrentContainer_Updater_Interval);
	}
	
	public void Containers_CurrentContainer_Updater_Stop() {
		Containers_CurrentContainer_Updater_flProcessing = false;
		if (Containers_CurrentContainer_Updater != null) {
			Containers_CurrentContainer_Updater.cancel();
			Containers_CurrentContainer_Updater = null;
		}
	}

    private class TContainersCurrentContainerUpdaterTask extends TimerTask
    {
    	public static final int MESSAGE_UPDATE 	= 1;
    	
    	@Override
        public void run() {
        	try {
        		Containers_CurrentContainer_Updater_Handler.obtainMessage(MESSAGE_UPDATE).sendToTarget();
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    }   
	
    private final Handler Containers_CurrentContainer_Updater_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
            	if (!flExists)
            		return; //. ->
                switch (msg.what) {
                
                case TContainersCurrentContainerUpdaterTask.MESSAGE_UPDATE:
                	if (DrawingProcess_IsProcessing())
                		return; //. ->
        			try {
                    	TReflectorComponent.TSpaceImageUpdating SpaceImageUpdating;
            			try {
            				SpaceImageUpdating = Reflector().Component.GetSpaceImageUpdating();
            			} catch (Exception E) {
            				return; //. ->
            			} 
            			if (SpaceImageUpdating != null)
                        	if (!SpaceImageUpdating.flDone) {
                        		if ((Containers_CurrentContainer_Updater_ImageUpdateIntervalCounter % Containers_CurrentContainer_Updater_ImageUpdateIntervalCount) == 0) {
                        			int ProgressPercentage = SpaceImageUpdating.ImageProgressor.ProgressPercentage(); 
                    				Containers_CurrentContainer_Updater_DoOnUpdating(ProgressPercentage);  
                        		}
                        		//.
                        		Containers_CurrentContainer_Updater_ImageUpdateIntervalCounter++;
                        	}
                        	else 
                        		Containers_CurrentContainer_Updater_DoOnUpdated();  
    		        } 
    		        catch (Exception E) {
    					Toast.makeText(TDrawingEditor.this, E.getMessage(), Toast.LENGTH_LONG).show();  
    		        }  
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
    
    private void Containers_CurrentContainer_Updater_DoOnUpdating(int ProgressPercentage) throws Exception {
    	if (!Containers_CurrentContainer_Updater_flProcessing)
    		return; //. ->
    	Containers_CurrentContainerUpdating(ProgressPercentage);    
    }

    private void Containers_CurrentContainer_Updater_DoOnUpdated() throws Exception {
    	if (!Containers_CurrentContainer_Updater_flProcessing)
    		return; //. ->
    	if (Containers_CurrentContainer_flUpdating) {
    		Containers_CurrentContainer_Updating_ProgressPercentage = -1;
        	Containers_CurrentContainer_flUpdating = false;
        	//.
        	Containers_FinishCurrentContainer();
    	}
    }

    public TSpaceContainer Containers_StartCurrentContainerTranslated(float dX, float dY) throws Exception {
    	Containers_CompleteCurrentContainer();
    	Containers_CurrentContainer = new TSpaceContainer();
    	//.
    	Containers_CurrentContainer_Updating_ProgressPercentage = -1;
    	Containers_CurrentContainer_flUpdating = true;
    	Reflector().Component.TranslateReflectionWindow(dX,dY);
    	//.
		Containers_CurrentContainer_Updater_Start();
		//.
		return Containers_CurrentContainer;
    }
    
    public TSpaceContainer Containers_StartCurrentContainerScaled(float Scale) throws Exception {
    	Containers_CompleteCurrentContainer();
    	Containers_CurrentContainer = new TSpaceContainer();
    	//.
    	Containers_CurrentContainer_Updating_ProgressPercentage = -1;
    	Containers_CurrentContainer_flUpdating = true;
    	Reflector().Component.ScaleReflectionWindow(Scale);
    	//.
		Containers_CurrentContainer_Updater_Start();
		//.
		return Containers_CurrentContainer;
    }
    
    public TSpaceContainer Containers_StartCurrentContainer() throws Exception {
    	return Containers_StartCurrentContainerTranslated(0.0F,0.0F);
    }
    
    public void Containers_FinishCurrentContainer() throws Exception {
    	if (Containers_CurrentContainer != null) {
        	Containers_CurrentContainer_Updater_Stop();
        	//.
        	Containers_CurrentContainer.RW = Reflector().Component.ReflectionWindow.GetWindow();
			Containers_CurrentContainer.LevelTileContainer = Containers_Compilation.ReflectionWindow_GetLevelTileContainer(Containers_CurrentContainer.RW);
			//.
        	Drawings_RepaintImage();
    	}
    }

    public void Containers_CurrentContainerUpdating(int ProgressPercentage) throws Exception {
    	Containers_CurrentContainer_Updating_ProgressPercentage = ProgressPercentage;
    	//.
    	Drawings_RepaintImage();
    }

    public void Containers_CancelCurrentContainer() {
    	if (Containers_CurrentContainer != null) {
    		Containers_CurrentContainer_Updater_Stop();
    		//.
    		Containers_CurrentContainer = null;
    	}
    }
    
    public void Containers_AddCurrentContainer() {
    	if (Containers_CurrentContainer != null) {
    		Containers_CurrentContainer_Updater_Stop();
    		//.
    		Containers.add(Containers_CurrentContainer);
        	Containers_CurrentContainer =null;
    	}
    }
    
    public TSpaceContainer Containers_GetCurrentContainer() {
    	return Containers_CurrentContainer;
    }
    
    public void Containers_SetCurrentContainer(TSpaceContainer Container) {
    	if (Containers_CurrentContainer != null)
    		Containers_CurrentContainer.Assign(Container);
    }
    
    public void Containers_SetCurrentContainerAsModified() {
    	if (Containers_CurrentContainer != null) 
    		Containers_CurrentContainer.flModified = true;
    }
    
    public void Containers_CompleteCurrentContainer(boolean flFinish) throws Exception {
    	if (Containers_CurrentContainer != null) {
    		if (flFinish)
    			Containers_FinishCurrentContainer();
    		//.
    		if ((Containers_CurrentContainer.LevelTileContainer != null/*finished*/) && Containers_CurrentContainer.flModified) 
    			Containers_AddCurrentContainer();
    		else
    			Containers_CancelCurrentContainer();
    	}
    }

    public void Containers_CompleteCurrentContainer() {
    	try {
			Containers_CompleteCurrentContainer(false);
		} catch (Exception E) {
		}
    }
    
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
		SurfaceUpdating.StartUpdate();
	}
	
	private void LineDrawingProcess_End() {
		if (LineDrawingProcess_flProcessing) {
			LineDrawingProcess_flProcessing = false;
			//.
			if (Containers_IsInitialized())
				Containers_SetCurrentContainerAsModified();
			Drawings_Add(LineDrawing);
			//.
			LineDrawingProcess_LastX = -1;
			//.
			SurfaceUpdating.StartUpdate();
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
			SurfaceUpdating.StartUpdate();
		}
	}
	
	//. Image drawing 
	
	public static int		ImageDrawing_MaxBitmapResolution = 1024;
	private TImageDrawing	ImageDrawing;	
	
	private void ImageDrawingProcess_Initialize() {
		ImageDrawing = null;
	}
	
	private void ImageDrawingProcess_Finalize() {
		if (ImageDrawing != null) {
			ImageDrawing.Destroy();
			ImageDrawing = null;			
		}
	}
	
    private static File ImageDrawingProcess_GetImageTempFile(Context context) {
  	  return new File(TGeoLogApplication.TempFolder,"image.jpg");
    }
  
	private void ImageDrawingProcess_Begin(Bitmap Image, float X0, float Y0, float X1, float Y1) {
		if (ImageDrawing != null) {
			ImageDrawing.Destroy();
			ImageDrawing = null;			
		}
		//.
		ImageDrawing = new TImageDrawing(Image, X0,Y0, X1,Y1);
	}
	
	private void ImageDrawingProcess_Begin(Bitmap Image, float X, float Y) {
		ImageDrawingProcess_Begin(Image, X,Y, X+Image.getWidth(),Y+Image.getHeight());
	}
	
	private void ImageDrawingProcess_End() throws Exception {
		if (Containers_IsInitialized())
			Containers_SetCurrentContainerAsModified();
		Drawings_Add(ImageDrawing);
		ImageDrawing = null;
		//.
		btnDrawingEditorUndo.setEnabled(true);
		btnDrawingEditorRedo.setEnabled(false);
		btnDrawingEditorClear.setEnabled(true);
	}
	
	private void ImageDrawingProcess_Draw() throws Exception {
		if (DrawableImageCanvas != null) {
			ImageDrawing.Paint(DrawableImageCanvas);
			//.
			if (SurfaceUpdating != null)
				SurfaceUpdating.StartUpdate();
		}
	}
	
	private void ImageDrawingProcess_AddImage(Bitmap Image, float X0, float Y0, float X1, float Y1) throws Exception {
		ImageDrawingProcess_Begin(Image, X0,Y0, X1,Y1);
		ImageDrawingProcess_Draw();
		ImageDrawingProcess_End();
	}
	
	@SuppressWarnings("unused")
	private void ImageDrawingProcess_AddImage(Bitmap Image, float X, float Y) throws Exception {
		ImageDrawingProcess_Begin(Image, X,Y);
		ImageDrawingProcess_Draw();
		ImageDrawingProcess_End();
	}
	
	private void ImageDrawingProcess_AddImageFromFile(File F) throws Exception {
		FileInputStream fs = new FileInputStream(F);
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inDither=false;
			options.inPurgeable=true;
			options.inInputShareable=true;
			options.inTempStorage=new byte[1024*256]; 							
			Rect rect = new Rect();
			Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), rect, options);
			float Scale = (Surface_Width+0.0F)/options.outWidth;
			ImageDrawingProcess_AddImage(bitmap, 0.0F,0.0F, Surface_Width,options.outHeight*Scale);
			/* try {
				int ImageMaxSize = options.outWidth;
				if (options.outHeight > ImageMaxSize)
					ImageMaxSize = options.outHeight;
				float MaxSize = ImageDrawing_MaxBitmapResolution;
				float Scale = MaxSize/ImageMaxSize; 
				Matrix matrix = new Matrix();     
				matrix.postScale(Scale,Scale);
				//.
				Bitmap Image = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
				ImageDrawingProcess_AddImage(Image, 0.0F,0.0F);
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
	private TDrawings	Drawings;
	private boolean 	Drawings_flChanged;
	private boolean		Drawings_flSaved;
	private Object 		Drawings_ImageLock = new Object();
	
	private void Drawings_Initialize() {
		Drawings = new TDrawings();
		Drawings_flChanged = false;
		Drawings_flSaved = false;
	}
	
	private void Drawings_Finalize() {
		if (Drawings != null) {
			Drawings.Destroy();
			Drawings = null;
		}
	}
	
	private void Drawings_Show() {
		DrawingEditorSurfaceLayout.setVisibility(View.VISIBLE);
	}
	
	private void Drawings_Hide() {
		DrawingEditorSurfaceLayout.setVisibility(View.GONE);
	}
	
	public boolean Drawings_IsEmpty() {
		return Drawings.IsEmpty();
	}
	
	public void Drawings_Add(TDrawing Drawing) {
		Drawings.Add(Drawing);
		//.
		Drawings_flChanged = true;
		Drawings_flSaved = false;
	}
	
	public boolean Drawings_Undo() throws Exception {
		boolean flChange = (Drawings.Items_HistoryIndex > 0);
		boolean Result = Drawings.Undo();
		if (flChange) {
			Drawings_flChanged = true;
			Drawings_flSaved = false;
			//.
			Drawings_UpdateImage();
		}
		return Result;
	}
	
	public void Drawings_UndoAll() throws Exception {
		if (Drawings.UndoAll()) {
			Drawings_flChanged = true;
			Drawings_flSaved = false;
			//.
			Drawings_UpdateImage();
		}
	}
	
	public boolean Drawings_Redo() throws Exception {
		boolean flChange = (Drawings.Items_HistoryIndex < Drawings.Items.size());
		boolean Result = Drawings.Redo(); 
		if (flChange) {
			Drawings_flChanged = true;
			Drawings_flSaved = false;
			//.
			Drawings_UpdateImage();
		}
		return Result;
	}
	
	public void Drawings_UpdateImage() {
		if (DrawableImage == null)
			return; //. ->
		synchronized (Drawings_ImageLock) {
			if (!Containers_IsInitialized())
				DrawableImage.eraseColor(Drawings.Descriptor.BackgroundColor);
			else
				DrawableImage.eraseColor(Color.TRANSPARENT);
			if (OriginDrawableImage != null)
				DrawableImageCanvas.drawBitmap(OriginDrawableImage,0,0,null);
			//.
			Drawings.Paint(DrawableImageCanvas);
			//.
			SurfaceUpdating.StartUpdate();
		}
	}
	
	public void Drawings_RepaintImage() throws Exception {
		if (DrawableImage == null)
			return; //. ->
		Moving_Reset();
		Scaling_Reset();
		//. repaint bitmap from current Drawing
		synchronized (Drawings_ImageLock) {
			if (BackgroundImage != null) {
				BackgroundImage.eraseColor(Color.TRANSPARENT);
				if (Containers_IsInitialized()) {
					Canvas canvas = new Canvas(BackgroundImage);
					TReflectionWindowStruc RW = Reflector().Component.ReflectionWindow.GetWindow();
					Containers_TileImagery.ActiveCompilationSet_ReflectionWindow_DrawOnCanvas(RW, 0,canvas,paint,null, null,null);
					//.
					if (Reflector().Component.Configuration.ReflectionWindow_flShowHints) 
						Reflector().Component.SpaceHints.DrawOnCanvas(RW, Reflector().Component.DynamicHintVisibleFactor, canvas);
				}
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
			if (!Containers_IsInitialized())
				DrawableImage.eraseColor(Drawings.Descriptor.BackgroundColor);
			else
				DrawableImage.eraseColor(Color.TRANSPARENT);
			if (OriginDrawableImage != null)
				DrawableImageCanvas.drawBitmap(OriginDrawableImage,0,0,null);
			//.
			Drawings.Paint(DrawableImageCanvas);
		}
		//.
		SurfaceUpdating.StartUpdate();
	}

	public void Drawings_Clear() throws Exception {
		if (Drawings.Items.size() == 0)
			return; //. ->
		Drawings.ClearItems();
		Drawings_flChanged = true;
		Drawings_flSaved = false;
		//.
		Drawings_UpdateImage();
	}

	private String 	DrawingsFile_Name = "";
	private String 	DrawingsFile_Format = null;
	private boolean DrawingsFile_flReadOnly = false;
	private boolean DrawingsFile_flSpaceContainersAvailable = true;
	
	public boolean DrawingsFile_Load() throws Exception {
		boolean Result = Drawings.LoadFromFile(DrawingsFile_Name);
		if (Result) {
			Drawings_flSaved = true;
			Drawings_flChanged = false;
			//.
			if (Drawings.SpaceContainers != null)  
				Containers_Initialize(Drawings.SpaceContainers);
			else {
				TDrawing.TRectangle Bounds = Drawings.GetRectangle();
				if (Bounds != null) {
					float dX =  Surface_Width/2.0F-((Bounds.Xmn+Bounds.Xmx)/2.0F);
					float dY =  Surface_Height/2.0F-((Bounds.Ymn+Bounds.Ymx)/2.0F);
					//.
					Drawings.Translate(dX,dY);
				}
			}
		}
		//.
		return Result;
	}
	
	public void DrawingsFile_Save() throws Exception {
		if (Containers_IsInitialized()) {
			Containers.clear(); //. remove last containers except current
			Containers_CompleteCurrentContainer(true);
			Drawings.SpaceContainers = Containers;
		}
		//.
		Drawings.SaveToFile(DrawingsFile_Name);
		//.
		if (DrawingsFile_Format != null) {
			if (DrawingsFile_Format.equals("png") || DrawingsFile_Format.equals("jpg")) {
				String FN = DrawingsFile_Name+"."+DrawingsFile_Format;
				FileOutputStream FOS = new FileOutputStream(new File(FN));
				try {
					FOS.write(Drawings.SaveAsBitmapData(DrawingsFile_Format));
				}
				finally {
					FOS.close();
				}
			}
		}
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
			Moving_flProcessing = false;
			//.
			Moving_OrgX = -1.0F;
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
		if (Containers_IsInitialized())
			Containers_CompleteCurrentContainer();
		//.
		SurfaceUpdating.StartUpdate();
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
			Moving_dX = 0;
			Moving_dY = 0;
		}
		Drawings.Translate(dX,dY);
		Drawings_flChanged = true;
		//.
		if (Containers_IsInitialized())
			Containers_StartCurrentContainerTranslated(dX,dY);
		//.
		Drawings_RepaintImage();
		//.
		SurfaceUpdating.StartUpdate();
	}
	
	private void Moving_Move(float X, float Y) {
		synchronized (Moving_Lock) {
			if (Moving_flProcessing) {
				Moving_X = X;
				Moving_Y = Y;
				//.
				if (Containers_IsInitialized())
					Containers_CompleteCurrentContainer();
				//.
				SurfaceUpdating.StartUpdate();
			}
		}
	}
	
	private Object 			Scaling_Lock = new Object();
    public boolean 			Scaling_flProcessing = false;
	private float 			Scaling_X0;
	private float 			Scaling_Y0;
	private float 			Scaling_LastLength;
	private float 			Scaling_Xbind;
	private float 			Scaling_Ybind;
	private float 			Scaling_Scale;
	
	private void Scaling_Reset() {
		synchronized (Scaling_Lock) {
			Scaling_flProcessing = false;
			//.
			Scaling_LastLength = -1.0F;
			//.
			Scaling_Xbind = Surface_Width/2.0F;
			Scaling_Ybind = Surface_Height/2.0F;
			//.
			Scaling_Scale = 1.0F;
		}
	}
	
	private boolean Scaling_IsAvailable() {
		return (!Containers_IsInitialized());
	}
	
	private void Scaling_Begin(float X, float Y) {
		synchronized (Scaling_Lock) {
			Scaling_X0 = X;
			Scaling_Y0 = Y;
			//.
			Scaling_flProcessing = true;
		}
	}
	
	private void Scaling_End() throws Exception {
		float Scale;
		synchronized (Scaling_Lock) {
			Scaling_flProcessing = false;
			//.
			Scale = Scaling_Scale;
			//.
			Scaling_LastLength = -1.0F;
			Scaling_Scale = 1.0F;
		}
		//.
		Drawings.Scale(Scaling_Xbind,Scaling_Ybind, Scale);
		Drawings_flChanged = true;
		//.
		if (Containers_IsInitialized())
			Containers_StartCurrentContainerScaled(Scale);
		//.
		Drawings_RepaintImage();
		//.
		SurfaceUpdating.StartUpdate();
	}
	
	private void Scaling_Move(float X, float Y) {
		synchronized (Scaling_Lock) {
			if (Scaling_flProcessing) {
				float dX = (X-Scaling_X0);
				float dY = (Y-Scaling_Y0);
				float Length = dX*dX+dY*dY;
				if (Scaling_LastLength > 0.0F) 
					if (Length > 0.0F) 
						Scaling_Scale = Length/Scaling_LastLength;
					else
						Scaling_Scale = 1.0F;
				else
					Scaling_LastLength = Length;
				//.
				if (Containers_IsInitialized())
					Containers_CompleteCurrentContainer();
				//.
				SurfaceUpdating.StartUpdate();
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
