package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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
import android.graphics.Bitmap.CompressFormat;
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
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.Classes.Data.Types.Image.Color.ColorPicker;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawing;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawing.TRectangle;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawingNode;
import com.geoscope.Classes.Data.Types.Image.Drawing.TLineDrawing;
import com.geoscope.Classes.Data.Types.Image.Drawing.TPictureDrawing;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.File.TFileSystemFileSelector;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowActualityInterval;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.BaseVisualizationFunctionality.TBase2DVisualizationFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TRWLevelTileContainer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTile;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImageryDataServer.TTilesPlace;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TReflectionWindowEditorPanel extends Activity implements OnTouchListener {

	public static String 	DrawingsFolder() {
		return TReflector.ProfileFolder()+"/"+"Drawings";
	}
	//.
	public static String 	DrawingsFile_Name() {
		return DrawingsFolder()+"/"+"Current";
	}
	//.
	public static String 	CommittedDrawingsFolder() {
		return DrawingsFolder()+"/"+"Committed";
	}
	//.
	public static String	UncommittedDrawingsFolder() {
		return DrawingsFolder()+"/"+"Uncommitted";
	}
	//.
	public File[] 			UncommittedDrawingsFolder_GetItemsList() {
		File DF = new File(UncommittedDrawingsFolder());
		if (!DF.exists())
			return null; //. ->
		File[] Result = DF.listFiles();
		Arrays.sort(Result, new Comparator<File>(){
		    public int compare(File f1, File f2) {
		        return Double.valueOf(Double.parseDouble(f2.getName())).compareTo(Double.parseDouble(f1.getName()));
		    }}
		);		
		return Result;
	}

	public static final int MODE_NONE 		= 0;
	public static final int MODE_DRAWING 	= 1;
	public static final int MODE_MOVING 	= 2;
	public static final int MODE_SETTINGS 	= 3;
	//.
	public static final int REQUEST_COMMITTING 					= 1;
	public static final int REQUEST_ADDPICTURE 					= 2;
	public static final int REQUEST_ADDPICTUREFROMFILE 			= 3;
	
	public static final int 	DrawingsVisualizationPrototypeTypeID = 2015;
	public static final long 	DrawingsVisualizationPrototypeID = 1740;
	
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
    				Background = Component.WorkSpace.BackgroundImage_ReCreate(width, height);
    			} catch (Exception E) {
    				Background = null;
    			}
    			if (BackgroundImage != null) 
    				BackgroundImage.recycle();
    			BackgroundImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    			if (ForegroundImage != null) 
    				ForegroundImage.recycle();
    			ForegroundImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    			if (OriginDrawableImage != null) 
    				OriginDrawableImage.recycle();
    			OriginDrawableImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    			if (DrawableImage != null)
    				DrawableImage.recycle();
    			DrawableImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    			DrawableImageCanvas = new Canvas(DrawableImage);			
    			//.
				Drawings_RepaintImage();
				//.
	        	if (flStartInitialContainer) {
	        		flStartInitialContainer = false;
	        		//.
	        		if (DrawingsFile_Exists()) {
	        			if (flAskForLastDrawings) {
	        				String DrawingsName = getString(R.string.SDoYouWantToContinueTheLastDrawing);
        					TDrawingsDescriptor Descriptor = DrawingsFile_GetDescriptor(DrawingsFile_Name());
        					if ((Descriptor != null) && (!Descriptor.Name.equals("")))
        						DrawingsName = "( "+Descriptor.Name+" )"+"\n"+DrawingsName;
		        		    new AlertDialog.Builder(TReflectionWindowEditorPanel.this)
		        	        .setIcon(android.R.drawable.ic_dialog_alert)
		        	        .setTitle(R.string.SConfirmation)
		        	        .setMessage(DrawingsName)
		        		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
		        		    	@Override
		        		    	public void onClick(DialogInterface dialog, int id) {
		        		    		TAsyncProcessing Processing = new TAsyncProcessing(TReflectionWindowEditorPanel.this,getString(R.string.SWaitAMoment)) {
		        		    			@Override
		        		    			public void Process() throws Exception {
			        	        			DrawingsFile_Load();
			    				    		Thread.sleep(100); 
		        		    			}
		        		    			@Override 
		        		    			public void DoOnCompleted() throws Exception {
	        		    					if (Containers.size() > 0) {
	        		    						TImageContainer LastContainer = Containers.get(Containers.size()-1);
	        		    						//.
	        		    						TReflectionWindowStruc RWS = LastContainer.RW; 
	        		    				    	Component.SetReflectionWindow(RWS,false);
	        		    				    	Containers.remove(LastContainer);
	        		    				    	//.
	        		    				    	TImageContainer StartedContainer = Containers_StartCurrentContainer();
	        		    				    	StartedContainer.RW = LastContainer.RW;
	        		    				    	StartedContainer.LevelTileContainer = LastContainer.LevelTileContainer;
	        		    				    	StartedContainer.flModified = LastContainer.flModified;
	        		    					}
	        		    					else
		    	        		    			Containers_StartCurrentContainer();
		        		    			}
		        		    			@Override
		        		    			public void DoOnException(Exception E) {
		        		    				Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		        		    			}
		        		    		};
		        		    		Processing.Start();
		        		    	}
		        		    })
		        		    .setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
		        		    	@Override
		        		    	public void onClick(DialogInterface dialog, int id) {
		        					try {
		        	        			if (!Component.flOffline) {
		        	        				AskForUncommittedDrawings();
		        	        			}
		        	        			else
		        	        				Containers_StartCurrentContainer();
		        			        } 
		        			        catch (Exception E) {
		        						Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        			        }  
		        		    	}
		        		    })
		        		    .setOnCancelListener(new OnCancelListener() {
								@Override
								public void onCancel(DialogInterface dialog) {
									TReflectionWindowEditorPanel.this.finish();
								}
							})
		        		    .show();
	        			}
	        			else {
        		    		TAsyncProcessing Processing = new TAsyncProcessing(TReflectionWindowEditorPanel.this,getString(R.string.SWaitAMoment)) {
        		    			@Override
        		    			public void Process() throws Exception {
	        	        			DrawingsFile_Load();
	    				    		Thread.sleep(100); 
        		    			}
        		    			@Override 
        		    			public void DoOnCompleted() throws Exception {
    		    					if (Containers.size() > 0) {
    		    						TImageContainer LastContainer = Containers.get(Containers.size()-1);
    		    						//.
    		    						TReflectionWindowStruc RWS = LastContainer.RW; 
    		    				    	Component.SetReflectionWindow(RWS,false);
    		    				    	Containers.remove(LastContainer);
    		    				    	//.
    		    				    	TImageContainer StartedContainer = Containers_StartCurrentContainer();
    		    				    	StartedContainer.RW = LastContainer.RW;
    		    				    	StartedContainer.LevelTileContainer = LastContainer.LevelTileContainer;
    		    				    	StartedContainer.flModified = LastContainer.flModified;
    		    					}
    		    					else
    	        		    			Containers_StartCurrentContainer();
        		    			}
        		    			@Override
        		    			public void DoOnException(Exception E) {
        		    				Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
        		    			}
        		    		};
        		    		Processing.Start();
	        			}
	        		}
	        		else {
	        			if (!Component.flOffline) {
	        				AskForUncommittedDrawings();
	        			}
	        			else
	        				Containers_StartCurrentContainer();
	        		}
	        	}
				//.
				SurfaceUpdating.Start();
			} catch (Exception E) {
				Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
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
		
		private void AskForUncommittedDrawings() throws Exception {
			UncommittedDrawingsList = UncommittedDrawingsFolder_GetItemsList();
			if ((UncommittedDrawingsList != null) && (UncommittedDrawingsList.length > 0)) {
    			if (flAskForUncommittedDrawings) {
					UncommittedDrawings = UncommittedDrawingsList[0];    				
					//.
    				final CharSequence[] _items = new CharSequence[UncommittedDrawingsList.length];
    				for (int I = 0; I < UncommittedDrawingsList.length; I++) {
    					TDrawingsDescriptor Descriptor = DrawingsFile_GetDescriptor(UncommittedDrawingsList[I].getAbsolutePath());
    					if (Descriptor != null)
    						_items[I] = Descriptor.Name;
    					else
    						_items[I] = UncommittedDrawingsList[I].getName();
    				}
    				AlertDialog.Builder builder = new AlertDialog.Builder(TReflectionWindowEditorPanel.this);
    				builder.setIcon(android.R.drawable.ic_dialog_alert);
    				builder.setTitle(R.string.SYouHaveIncompletedDrawings);
    				builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							UncommittedDrawings = UncommittedDrawingsList[which];
						}
					});
    				builder.setPositiveButton(R.string.SOpen, new DialogInterface.OnClickListener() {
        		    	@Override
        		    	public void onClick(DialogInterface dialog, int id) {
        		    		if (UncommittedDrawings == null)
        		    			return; //. ->
        		    		//.
        		    		TAsyncProcessing Processing = new TAsyncProcessing(TReflectionWindowEditorPanel.this,getString(R.string.SWaitAMoment)) {
        		    			
        		    			@Override
        		    			public void Process() throws Exception {
        	        				DrawingsFile_MoveFrom(UncommittedDrawings);
        	        				//.
	        	        			DrawingsFile_Load();
	    				    		Thread.sleep(100); 
        		    			}
        		    			@Override 
        		    			public void DoOnCompleted() throws Exception {
    		    					if (Containers.size() > 0) {
    		    						TImageContainer LastContainer = Containers.get(Containers.size()-1);
    		    						//.
    		    						TReflectionWindowStruc RWS = LastContainer.RW; 
    		    				    	Component.SetReflectionWindow(RWS,false);
    		    				    	Containers.remove(LastContainer);
    		    				    	//.
    		    				    	TImageContainer StartedContainer = Containers_StartCurrentContainer();
    		    				    	StartedContainer.RW = LastContainer.RW;
    		    				    	StartedContainer.LevelTileContainer = LastContainer.LevelTileContainer;
    		    				    	StartedContainer.flModified = LastContainer.flModified;
    		    					}
    		    					else
    	        		    			Containers_StartCurrentContainer();
        		    			}
        		    			@Override
        		    			public void DoOnException(Exception E) {
        		    				Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
        		    			}
        		    		};
        		    		Processing.Start();
        		    	}
        		    });
    				builder.setNegativeButton(R.string.SCreateANew, new DialogInterface.OnClickListener() {
        		    	@Override
        		    	public void onClick(DialogInterface dialog, int id) {
        					try {
	        	    			Containers_StartCurrentContainer();
        			        } 
        			        catch (Exception E) {
        						Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
        			        }  
        		    	}
        		    });
        		    builder.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							TReflectionWindowEditorPanel.this.finish();
						}
					});
    				AlertDialog alert = builder.create();
    				alert.show();
    			}
    			else {
		    		TAsyncProcessing Processing = new TAsyncProcessing(TReflectionWindowEditorPanel.this,getString(R.string.SWaitAMoment)) {
		    			@Override
		    			public void Process() throws Exception {
	        				DrawingsFile_MoveFrom(UncommittedDrawings);
	        				//.
    	        			DrawingsFile_Load();
				    		Thread.sleep(100); 
		    			}
		    			@Override 
		    			public void DoOnCompleted() throws Exception {
	    					if (Containers.size() > 0) {
	    						TImageContainer LastContainer = Containers.get(Containers.size()-1);
	    						//.
	    						TReflectionWindowStruc RWS = LastContainer.RW; 
	    				    	Component.SetReflectionWindow(RWS,false);
	    				    	Containers.remove(LastContainer);
	    				    	//.
	    				    	TImageContainer StartedContainer = Containers_StartCurrentContainer();
	    				    	StartedContainer.RW = LastContainer.RW;
	    				    	StartedContainer.LevelTileContainer = LastContainer.LevelTileContainer;
	    				    	StartedContainer.flModified = LastContainer.flModified;
	    					}
	    					else
        		    			Containers_StartCurrentContainer();
		    			}
		    			@Override
		    			public void DoOnException(Exception E) {
		    				Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    			}
		    		};
		    		Processing.Start();
    			}
			}
			else
				Containers_StartCurrentContainer();
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
									canvas.drawBitmap(Background, 0,0, null);
									canvas.drawBitmap(BackgroundImage, dX,dY, null);
									canvas.drawBitmap(DrawableImage, dX,dY, null);
									canvas.drawBitmap(ForegroundImage, dX,dY, null);
								}
								//. draw "drawing" marker
								if (LineDrawingProcess_flProcessing) 
									canvas.drawCircle(LineDrawingProcess_LastX,LineDrawingProcess_LastY,LineDrawingProcess_Brush.getStrokeWidth()/2.0F,LineDrawingProcess_MarkerPaint);
								//. draw ColorPickerBar and BrushWidthPickerBar  
								float OfsY = ReflectionWindowEditorSurfaceControlLayout.getHeight();
								int CurrentColor = Settings_Brush.getColor();
								ColorPickerBar.Paint(canvas, 0,OfsY, Surface_Width,Surface_Height-OfsY, CurrentColor);
								float CurrentBrushWidth = Settings_Brush.getStrokeWidth(); 
								BrushWidthPickerBar.Paint(canvas, 0,OfsY, Surface_Width,Surface_Height-OfsY, CurrentBrushWidth,CurrentColor);
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
            String S;
        	int TextColor;
        	switch (_Mode) {
        	
        	case MODE_DRAWING:
        		S = getApplicationContext().getString(R.string.SDrawing);
            	TextColor = Color.GREEN;
        		break; //. >
        		
        	case MODE_MOVING:
        		S = getApplicationContext().getString(R.string.SMoving);
            	TextColor = Color.BLUE;
        		break; //. >
        		
        	default:
        		S = "?"; 
            	TextColor = Color.BLACK;
        		break; //. >
        	}
        	//.
        	if (!Drawings_Descriptor.Name.equals("")) 
            	S = S+" - "+Drawings_Descriptor.Name;
            //.
            if (Containers_CurrentContainer_flUpdating) {
            	S = S+": "+getApplicationContext().getString(R.string.SImageUpdating);
            	TextColor = Color.RED;
            }
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
	private TReflectorComponent Component;
	//.
	private boolean flAskForLastDrawings = false;
	private boolean flAskForUncommittedDrawings = false;
	//.
	private File[] UncommittedDrawingsList = null;
	private File UncommittedDrawings = null;
	//.
	private TReflectionWindowActualityInterval 	Reflector_ReflectionWindowLastActualityInterval;
	//.
	private boolean flStartInitialContainer = true;
	//.
	private TTileImagery 					TileImagery = null;
	private TTileServerProviderCompilation 	UserDrawableCompilation = null;
	//.
	private Paint paint = new Paint();
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
	private SurfaceView 					Surface;
	private int								Surface_Width;
	private int								Surface_Height;
	private TSurfaceHolderCallbackHandler 	SurfaceHolderCallbackHandler = new TSurfaceHolderCallbackHandler();
	private TSurfaceUpdating 				SurfaceUpdating = null;
	//.
	private int Mode = MODE_NONE;
	//.
	private List<TDrawing> 		Drawings;
	private	int					Drawings_HistoryIndex;
	private boolean 			Drawings_flChanged;
	private boolean				Drawings_flSaved;
	//.
	private RelativeLayout 	ReflectionWindowEditorSurfaceLayout;
	private LinearLayout	ReflectionWindowEditorSurfaceControlLayout;
	private CheckBox 		cbReflectionWindowEditorMode;
	private Button 			btnReflectionWindowEditorBrushSelector;
	private Button 			btnReflectionWindowEditorUndo;
	private Button 			btnReflectionWindowEditorRedo;
	private Button 			btnReflectionWindowEditorClear;
	private Button 			btnReflectionWindowEditorOperations;
	private Button 			btnReflectionWindowEditorCommit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			ComponentID = extras.getInt("ComponentID");
		Component = TReflectorComponent.GetComponent(ComponentID);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);		
		//.
        extras = getIntent().getExtras(); 
        if (extras != null) {
        	flAskForLastDrawings = extras.getBoolean("AskForLastDrawings");
        	flAskForUncommittedDrawings = extras.getBoolean("AskForUncommittedDrawings");
        }
        //.
    	metrics = getApplicationContext().getResources().getDisplayMetrics();
        //.
        try {
    		TileImagery = Component.SpaceTileImagery;
    		if (TileImagery != null) 
    			UserDrawableCompilation = TileImagery.ActiveCompilationSet_GetUserDrawableItem();
    		if (UserDrawableCompilation == null)
    			throw new Exception(getString(R.string.SThereIsNoVisibleUserDrawableTilesLayer)); //. =>
        } 
        catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  
        	finish();
        	return; //. ->
        }  
		//. reset view to max time
    	try {
			Reflector_ReflectionWindowLastActualityInterval = Component.ReflectionWindow.GetActualityInterval();
	    	Component.ReflectionWindow.SetActualityInterval(TReflectionWindowActualityInterval.MaxRealTimestamp,TReflectionWindowActualityInterval.MaxRealTimestamp);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_SHORT).show();  						
			finish();
			return; //. ->
		}
        //.
        setContentView(R.layout.reflectionwindow_editor_panel);
        //.
		ReflectionWindowEditorSurfaceLayout = (RelativeLayout)findViewById(R.id.ReflectionWindowEditorSurfaceLayout);
		ReflectionWindowEditorSurfaceControlLayout = (LinearLayout)findViewById(R.id.ReflectionWindowEditorSurfaceControlLayout);
		//.
		Surface = (SurfaceView) findViewById(R.id.ReflectionWindowEditorSurfaceView);
		Surface.setOnTouchListener(this);
		//.
		cbReflectionWindowEditorMode = (CheckBox)findViewById(R.id.cbReflectionWindowEditorMode);
		cbReflectionWindowEditorMode.setChecked(true);
		cbReflectionWindowEditorMode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cbReflectionWindowEditorMode.isChecked())
					SetMode(MODE_DRAWING);
				else
					SetMode(MODE_MOVING);
			}
		});
        //.
		btnReflectionWindowEditorBrushSelector = (Button)findViewById(R.id.btnReflectionWindowEditorBrushSelector);
		btnReflectionWindowEditorBrushSelector.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	SetMode(MODE_SETTINGS);
            }
        });
		//.
		btnReflectionWindowEditorUndo = (Button)findViewById(R.id.btnReflectionWindowEditorUndo);
		btnReflectionWindowEditorUndo.setEnabled(false);
		btnReflectionWindowEditorUndo.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	try {
            		boolean R = Drawings_Undo();
            		//.
            		btnReflectionWindowEditorUndo.setEnabled(R);
            		btnReflectionWindowEditorRedo.setEnabled(true);
        			btnReflectionWindowEditorClear.setEnabled(R);
		        } 
		        catch (Exception E) {
					Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
            }
        });
		//.
		btnReflectionWindowEditorRedo = (Button)findViewById(R.id.btnReflectionWindowEditorRedo);
		btnReflectionWindowEditorRedo.setEnabled(false);
		btnReflectionWindowEditorRedo.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
            	try {
            		boolean R = Drawings_Redo();
            		//.
            		btnReflectionWindowEditorRedo.setEnabled(R);
            		btnReflectionWindowEditorUndo.setEnabled(true);
        			btnReflectionWindowEditorClear.setEnabled(true);
		        } 
		        catch (Exception E) {
					Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
            }
        });
		//.
		btnReflectionWindowEditorClear = (Button)findViewById(R.id.btnReflectionWindowEditorClear);
		btnReflectionWindowEditorClear.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
        		if (Drawings_HistoryIndex > 0) {
        		    new AlertDialog.Builder(TReflectionWindowEditorPanel.this)
        	        .setIcon(android.R.drawable.ic_dialog_alert)
        	        .setTitle(R.string.SConfirmation)
        	        .setMessage(R.string.SCancelChanges)
        		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
        		    	public void onClick(DialogInterface dialog, int id) {
        		    		try {
        		    			Drawings_UndoAll();
        		    			btnReflectionWindowEditorUndo.setEnabled(false);
        		    			btnReflectionWindowEditorRedo.setEnabled(true);
        			        } 
        			        catch (Exception E) {
        						Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
        			        }  
        		    	}
        		    })
        		    .setNegativeButton(R.string.SNo, null)
        		    .show();
        		}
            }
        });
		//.
		btnReflectionWindowEditorOperations = (Button)findViewById(R.id.btnReflectionWindowEditorOperations);
		btnReflectionWindowEditorOperations.setEnabled(false);
		btnReflectionWindowEditorOperations.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
        		if (GetMode() != MODE_DRAWING) {
        			Toast.makeText(TReflectionWindowEditorPanel.this, R.string.SViewIsNotInDrawingMode, Toast.LENGTH_LONG).show();
        			return; //. ->
        		}
        		//.
        		final CharSequence[] _items;
    			_items = new CharSequence[2];
    			_items[0] = getString(R.string.SAddImage);
    			_items[1] = getString(R.string.SAddImageFromFile);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TReflectionWindowEditorPanel.this);
        		builder.setTitle(R.string.SOperations);
        		builder.setNegativeButton(TReflectionWindowEditorPanel.this.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
	    					switch (arg1) {
	    					case 0: //. take a picture
	    		      		    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    		      		    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(PictureDrawingProcess_GetPictureTempFile(TReflectionWindowEditorPanel.this))); 
	    		      		    startActivityForResult(intent, REQUEST_ADDPICTURE);    		
	    						break; //. >
	    						
	    					case 1: //. take a picture form file
	    				    	TFileSystemFileSelector FileSelector = new TFileSystemFileSelector(TReflectionWindowEditorPanel.this)
	    				        .setFilter(".*\\.bmp|.*\\.png|.*\\.gif|.*\\.jpg|.*\\.jpeg")
	    				        .setOpenDialogListener(new TFileSystemFileSelector.OpenDialogListener() {
	    				        	
	    				            @Override
	    				            public void OnSelectedFile(String fileName) {
					                    File ChosenFile = new File(fileName);
					                    //.
										try {
						                    PictureDrawingProcess_AddPictureFromFile(ChosenFile);
										}
										catch (Throwable E) {
											String S = E.getMessage();
											if (S == null)
												S = E.getClass().getName();
						        			Toast.makeText(TReflectionWindowEditorPanel.this, S, Toast.LENGTH_LONG).show();  						
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
		        			Toast.makeText(TReflectionWindowEditorPanel.this, TReflectionWindowEditorPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
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
		btnReflectionWindowEditorCommit = (Button)findViewById(R.id.btnReflectionWindowEditorCommit);
		btnReflectionWindowEditorCommit.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View v) {
                try {
                	if (Drawings_HistoryIndex > 0)
                		new TUserSecurityFileGettingAndCommitting();
                	else
                		TReflectionWindowEditorPanel.this.finish();
                } 
                catch (Exception E) {
        			Toast.makeText(TReflectionWindowEditorPanel.this, E.toString(), Toast.LENGTH_LONG).show();  
                }        
            }
        });
		//.
		ColorPickerBar = new TColorPickerBar(ColorPickerBarSize*metrics.density, TColorPickerBar.LAYOUT_LEFTALIGN);
		BrushWidthPickerBar = new TBrushWidthPickerBar(BrushWidthPickerBarSize*metrics.density, BrushWidthPickerBarMinWidth*metrics.density,BrushWidthPickerBarMaxWidth*metrics.density, 1.1F, TBrushWidthPickerBar.LAYOUT_RIGHTALIGN);
		//.
		try {
			Containers_Initialize();
			//.
			Settings_Initialize();
			Drawings_Initialize();
			//.
			LineDrawingProcess_Initialize();
			PictureDrawingProcess_Initialize();
			//.
			Moving_Reset();
			//.
			SetMode(MODE_DRAWING);
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
		flExists = true;
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
		try {
			if (Drawings_flChanged && (!Drawings_flSaved)) 
	    		DrawingsFile_Save();
			//.
			PictureDrawingProcess_Finalize();
			LineDrawingProcess_Finalize();
			//.
			Containers_CurrentContainer_Updater_Stop();
			Drawings_Finalize();
			Settings_Finalize();
			//.
			Containers_Finalize();
		}
		catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(this, getString(R.string.SError)+S, Toast.LENGTH_LONG).show();
		}
		//. restore last time
		try {
			if (Reflector_ReflectionWindowLastActualityInterval != null)
				Component.ReflectionWindow.SetActualityInterval(Reflector_ReflectionWindowLastActualityInterval);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
		}
		//.
		super.onDestroy();
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
	        .setMessage(R.string.SSaveTheCurrentDrawingsToContinueEditingLater)
		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int id) {
		    		TAsyncProcessing Processing = new TAsyncProcessing(TReflectionWindowEditorPanel.this,getString(R.string.SWaitAMoment)) {
		    			@Override
		    			public void Process() throws Exception {
				    		DrawingsFile_Save();
				    		//.
				    		Thread.sleep(100); 
		    			}
		    			@Override 
		    			public void DoOnCompleted() throws Exception {
	    		    		TReflectionWindowEditorPanel.this.finish();
		    			}
		    			@Override
		    			public void DoOnException(Exception E) {
		    				Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		    			}
		    		};
		    		Processing.Start();
		    	}
		    })
		    .setNegativeButton(R.string.SNo, new DialogInterface.OnClickListener() {
		    	@Override
		    	public void onClick(DialogInterface dialog, int id) {
		    		DrawingsFile_ResetChanges();
		    		//.
		    		TReflectionWindowEditorPanel.this.finish();
		    	}
		    })
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

        case REQUEST_COMMITTING: 
        	if (resultCode == RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
            		int ResultCode = extras.getInt("ResultCode");
            		switch (ResultCode) {
            		
            		case TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT:
            		case TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT_ENQUEUECHANGEDTILES:
            		case TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_DEFER:
            			//.
                		String PlaceName = extras.getString("PlaceName");
                		int UserSecurityFileID = extras.getInt("UserSecurityFileID");
                		boolean flReSet = extras.getBoolean("flReSet");
                		double ReSetInterval = extras.getDouble("ReSetInterval");
                		String DataFileName = extras.getString("DataFileName");
                		//. setting the drawing params
                		Drawings_Descriptor.Name = PlaceName;
                		Drawings_Descriptor.UserSecurityFileID = UserSecurityFileID;
                		Drawings_Descriptor.ReSetInterval = ReSetInterval;
                		Drawings_Descriptor.DataFileName = DataFileName;
                		//.
                    	try {
                    		TReflectionWindowStruc RW = Component.ReflectionWindow.GetWindow();
                    		TTilesPlace Place = new TTilesPlace(PlaceName,RW);
                    		//. set timestamp as drawing start timestamp
                    		Place.Timestamp = Drawings_Descriptor.Timestamp;
                    		//.
                    		boolean flCommitToTheServer = ((ResultCode == TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT) || (ResultCode == TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT_ENQUEUECHANGEDTILES));
                    		//.
                    		boolean Commit_flEnqueueChangedTiles = false;
                    		if (flCommitToTheServer)
                    			Commit_flEnqueueChangedTiles = (ResultCode == TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT_ENQUEUECHANGEDTILES);
                    		//.
                        	new TChangesCommitting(UserSecurityFileID,flReSet,ReSetInterval,Place,flCommitToTheServer,Commit_flEnqueueChangedTiles,true);
        				}
        				catch (Exception E) {
        					String S = E.getMessage();
        					if (S == null)
        						S = E.getClass().getName();
                			Toast.makeText(TReflectionWindowEditorPanel.this, TReflectionWindowEditorPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
        				}
            			break; //. >

            		case TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT_VISUALIZATION:
            		case TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT_VISUALIZATION_ENQUEUEDRAWING:
            			//.
                		UserSecurityFileID = extras.getInt("UserSecurityFileID");
                		DataFileName = extras.getString("DataFileName");
                		//. setting the drawing params
                		Drawings_Descriptor.UserSecurityFileID = UserSecurityFileID;
                		Drawings_Descriptor.DataFileName = DataFileName;
                		//.
                    	try {
                    		//.
                    		boolean flCommitToTheServer = ((ResultCode == TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT_VISUALIZATION) || (ResultCode == TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT_VISUALIZATION_ENQUEUEDRAWING));
                    		//.
                    		boolean Commit_flEnqueueChangedTiles = false;
                    		if (flCommitToTheServer)
                    			Commit_flEnqueueChangedTiles = (ResultCode == TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_COMMIT_VISUALIZATION_ENQUEUEDRAWING);
                    		//.
                        	new TChangesCommittingForVisualization(UserSecurityFileID,DataFileName, flCommitToTheServer,Commit_flEnqueueChangedTiles, true);
        				}
        				catch (Exception E) {
        					String S = E.getMessage();
        					if (S == null)
        						S = E.getClass().getName();
                			Toast.makeText(TReflectionWindowEditorPanel.this, TReflectionWindowEditorPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
        				}
            			break; //. >

            		case TReflectionWindowEditorCommittingPanel.COMMITTING_RESULT_DELETE:
            			DrawingsFile_Delete();
            			//.
	            		TReflectionWindowEditorPanel.this.finish();
        				break; //. >
            		}
                }
        	}  
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void Update() {
		btnReflectionWindowEditorUndo.setEnabled(Drawings_HistoryIndex > 0);
		btnReflectionWindowEditorClear.setEnabled(Drawings_HistoryIndex > 0);
		btnReflectionWindowEditorOperations.setEnabled(true);
    }
    
	public byte[] ToByteArray() throws Exception {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			short Version = 2;
			byte[] BA = TDataConverter.ConvertInt16ToLEByteArray(Version);
			BOS.write(BA);
			BA = Containers_ToByteArray();
			BOS.write(BA);
			BA = Drawings_ToByteArrayV2();
			BOS.write(BA);
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws Exception {
		short Version = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
		switch (Version) {
		
		case 0:
			Idx = Containers_FromByteArray(BA, Idx);
			Idx = Drawings_FromByteArray(BA, Idx);
			break; //. >
			
		case 1:
			Idx = Containers_FromByteArray(BA, Idx);
			Idx = Drawings_FromByteArrayV1(BA, Idx);
			break; //. >
			
		case 2:
			Idx = Containers_FromByteArray(BA, Idx);
			Idx = Drawings_FromByteArrayV2(BA, Idx);
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
	
	public static TDrawingsDescriptor DrawingsFile_GetDescriptor(String FN) throws IOException {
		File F = new File(FN);
		if (F.exists()) { 
	    	FileInputStream FIS = new FileInputStream(FN);
	    	try {
	    			byte[] BA = new byte[(int)F.length()];
	    			FIS.read(BA);
	    			//.
	    			int Idx = 0;
	    			short Version = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
	    			switch (Version) {
	    			
	    			case 0:
	    				return null; //. ->
	    				
	    			case 1:
	    				//. skip containers
	    		    	int ContainersCount = (int)TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
    					TImageContainer Container = new TImageContainer();
	    				for (int I = 0; I < ContainersCount; I++) 
	    					Idx = Container.FromByteArray(BA, Idx);
	    				//.
	    				TDrawingsDescriptor Result = new TDrawingsDescriptor();
	    				Result.FromByteArrayV1(BA, Idx);
	    				return Result; //. ->
	    				
	    			case 2:
	    				//. skip containers
	    		    	ContainersCount = (int)TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
    					Container = new TImageContainer();
	    				for (int I = 0; I < ContainersCount; I++) 
	    					Idx = Container.FromByteArray(BA, Idx);
	    				//.
	    				Result = new TDrawingsDescriptor();
	    				Result.FromByteArrayV2(BA, Idx);
	    				return Result; //. ->
	    				
	    			default:
	    				throw new IOException("unknown data version: "+Short.toString(Version)); //. =>
	    			}
	    	}
			finally
			{
				FIS.close(); 
			}
		}
		else
			return null; //. ->
	}
	
	public boolean DrawingsFile_MoveFrom(File F) throws IOException {
		boolean R = F.renameTo(new File(DrawingsFile_Name()));
		return R;
	}
	
	public boolean DrawingsFile_Load() throws Exception {
		return FromFile(DrawingsFile_Name());
	}
	
	public void DrawingsFile_Save() throws Exception {
		ToFile(DrawingsFile_Name());
		//.
		Drawings_flSaved = true;
		Drawings_flChanged = false;
	}
	
	public String DrawingsFile_MoveToUncommitted() throws IOException {
		String FileName = Double.toString(Drawings_Descriptor.Timestamp);
		String FN = UncommittedDrawingsFolder()+"/"+FileName;
		//.
    	File FF = new File(UncommittedDrawingsFolder()); FF.mkdirs();
		(new File(DrawingsFile_Name())).renameTo(new File(FN));
		//.
		return FileName;
	}
	
	public boolean DrawingsFile_Exists() {
		return (new File(DrawingsFile_Name())).exists();
	}
	
	public boolean DrawingsFile_Delete(String FN) {
		return (new File(FN)).delete();
	}
	
	public boolean DrawingsFile_Delete() {
		boolean R = (new File(DrawingsFile_Name())).delete();
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
		cbReflectionWindowEditorMode.setChecked(pMode == MODE_DRAWING);
		//.
		if (SurfaceUpdating != null)
			SurfaceUpdating.StartUpdate();
	}
	
	public double CommitChanges(int SecurityFileID, boolean flReSet, double ReSetInterval, TTilesPlace TilesPlace, boolean flCommitToTheServer, boolean Commit_flEnqueueChangedTiles) throws Throwable {
		double Result = 0.0;
		//. committing ...
		if (flCommitToTheServer) {
			if (Component.flOffline)
				throw new TGeoScopeServer.ServerIsOfflineException(getString(R.string.SServerIsOffline));
			if (Containers_CurrentContainer_Updater_flProcessing) 
				throw new Exception(getString(R.string.SViewIsUpdating)); //. =>
			//.
			DrawingsFile_Save();
			String DrawingsFileName = DrawingsFile_MoveToUncommitted();
			//.
			try {
				//. commit drawings into tiles locally
				Drawings_Commit();
				//. committing on the server
				if (Commit_flEnqueueChangedTiles)
					TilesPlace.Timestamp = -TilesPlace.Timestamp; //. use TilesPlace.Timestamp as changed tiles timestamp 
				Result = TileImagery.ActiveCompilationSet_CommitModifiedTiles(SecurityFileID,flReSet,ReSetInterval,TilesPlace, Commit_flEnqueueChangedTiles);
			}
			catch (Throwable T) {
				Containers_RestoreTileModifications();
				//.
				throw T; //. =>
			}
			//.
			Drawings_flChanged = false;
			Drawings_flSaved = true;
			//.
			DrawingsFile_Delete(UncommittedDrawingsFolder()+"/"+DrawingsFileName);
		}
		else {
			DrawingsFile_Save();
			DrawingsFile_MoveToUncommitted();
		}
		//.
		return Result;
	}
	
	public long CommitChangesForVisualization(int SecurityFileID, String DataFileName, boolean flCommitToTheServer, boolean Commit_flEnqueueChangedTiles) throws Throwable {
		long Result = 0;
		//. committing ...
		if (flCommitToTheServer) {
			if (Component.flOffline)
				throw new TGeoScopeServer.ServerIsOfflineException(getString(R.string.SServerIsOffline));
			//.
			DrawingsFile_Save();
			String DrawingsFileName = DrawingsFile_MoveToUncommitted();
			//.
			try {
				TRectangle DrawingsRectangle = Drawings_GetRectangle();
				if (DrawingsRectangle == null)
					return Result; //. ->
				//.
				String ContentType = "png";
				byte[] ContentData;
				Bitmap BMP = Bitmap.createBitmap((int)DrawingsRectangle.Width()+1,(int)DrawingsRectangle.Height()+1, Bitmap.Config.ARGB_8888);
				try {
					Canvas BMPCanvas = new Canvas(BMP);
					//.
					float dX = DrawingsRectangle.Xmn-1.0F;
					float dY = DrawingsRectangle.Ymn-1.0F;
					//.
					Drawings_Translate(-dX,-dY);
					try {
						BMP.eraseColor(Color.TRANSPARENT);
						//.
						for (int I = 0; I < Drawings_HistoryIndex; I++) 
							Drawings.get(I).Paint(BMPCanvas);
					}
					finally {
						Drawings_Translate(dX,dY);
					}
					//.
					ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
					try {
						BMP.compress(CompressFormat.PNG, 0, BOS);
						ContentData = BOS.toByteArray(); 
					}
					finally {
						BOS.close();
					}
				}
				finally {
					BMP.recycle();
				}
				//. 
                TReflectionWindowStruc RW = Component.ReflectionWindow.GetWindow();
                TXYCoord P0 = RW.ConvertToReal(DrawingsRectangle.Xmn,DrawingsRectangle.Ymn);
                TXYCoord P1 = RW.ConvertToReal(DrawingsRectangle.Xmx,DrawingsRectangle.Ymn);
                TXYCoord P2 = RW.ConvertToReal(DrawingsRectangle.Xmx,DrawingsRectangle.Ymx);
                TXYCoord P3 = RW.ConvertToReal(DrawingsRectangle.Xmn,DrawingsRectangle.Ymx);
                //.
                TBase2DVisualizationFunctionality.TData VisualizationData = new TBase2DVisualizationFunctionality.TData();
                VisualizationData.Width = Math.sqrt(Math.pow(P3.X-P0.X,2)+Math.pow(P3.Y-P0.Y,2));
                VisualizationData.Nodes = new TBase2DVisualizationFunctionality.TData.TNode[2];
                VisualizationData.Nodes[0] = new TBase2DVisualizationFunctionality.TData.TNode((P0.X+P3.X)/2.0,(P0.Y+P3.Y)/2.0);
                VisualizationData.Nodes[1] = new TBase2DVisualizationFunctionality.TData.TNode((P1.X+P2.X)/2.0,(P1.Y+P2.Y)/2.0);
                VisualizationData.Content = new TBase2DVisualizationFunctionality.TData.TContent(ContentType,ContentData);
                //.
				TComponentFunctionality CF = Component.User.Space.TypesSystem.TComponentFunctionality_Create(Component.Server, DrawingsVisualizationPrototypeTypeID,DrawingsVisualizationPrototypeID);
				if (CF != null) 
					try {
						CF.VisualizationData = VisualizationData;
						Result = CF.Clone();
						//.
    					if ((DataFileName != null) && (DataFileName.length() > 0)) {
        					TComponentFunctionality CCF = Component.User.Space.TypesSystem.TComponentFunctionality_Create(Component.User.Server, CF.idTComponent(),Result);
        					if (CCF == null)
        						throw new Exception("could not get a clone functionality"); //. => 
        					try {
        						long idDATAFile = CCF.GetComponent(SpaceDefines.idTDATAFile);
        						if (idDATAFile == 0)
        							throw new Exception("unable to get TDATAFile component of the clone"); //. =>
        						//. prepare and send datafile
                		    	TTracker Tracker = TTracker.GetTracker();
                		    	if (Tracker == null)
                		    		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
        				    	double Timestamp = OleDate.UTCCurrentTimestamp();
        						String NFN = TGPSModule.MapPOIComponentFolder()+"/"+Double.toString(Timestamp)+"_"+TUIDGenerator.Generate()+"_File"+"."+TFileSystem.FileName_GetExtension(DataFileName);
        						File NF = new File(NFN);
        						TFileSystem.CopyFile(new File(DataFileName), NF);
                				String _DataFileName = NFN;
        						try {
        							Tracker.GeoLog.ComponentFileStreaming.AddItem(SpaceDefines.idTDATAFile,idDATAFile, _DataFileName);
        						} catch (Exception E) {
        					    	File F = new File(DataFileName);
        					    	F.delete();
        					    	//.
        				    		throw E; //. =>
        						}
        					} finally {
        						CCF.Release();
        					}
    					}
					}
					finally {
						CF.Release();
					}
			}
			catch (Throwable T) {
				Containers_RestoreTileModifications();
				//.
				throw T; //. =>
			}
			//.
			Drawings_flChanged = false;
			Drawings_flSaved = true;
			//.
			DrawingsFile_Delete(UncommittedDrawingsFolder()+"/"+DrawingsFileName);
		}
		else {
			DrawingsFile_Save();
			DrawingsFile_MoveToUncommitted();
		}
		//.
		return Result;
	}
	
    private class TChangesCommitting extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION	 			= 0;
    	private static final int MESSAGE_COMMITTED 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private int 	SecurityFileID;
    	private boolean flReSet;
    	private double ReSetInterval;
    	private TTilesPlace Place;
    	private boolean flCommitToTheServer;
    	private boolean Commit_flEnqueueChangedTiles;
    	private boolean flCloseEditor;
    	//.
    	private double Timestamp;
    	
        private ProgressDialog progressDialog; 
    	
    	public TChangesCommitting(int pSecurityFileID, boolean pflReSet, double pReSetInterval, TTilesPlace pPlace, boolean pflCommitToTheServer, boolean pCommit_flEnqueueChangedTiles, boolean pflCloseEditor) {
    		SecurityFileID = pSecurityFileID;
    		flReSet = pflReSet;
    		ReSetInterval = pReSetInterval;
    		Place = pPlace;
    		flCommitToTheServer = pflCommitToTheServer;
    		Commit_flEnqueueChangedTiles = pCommit_flEnqueueChangedTiles;
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
    				Timestamp = CommitChanges(SecurityFileID,flReSet,ReSetInterval,Place,flCommitToTheServer,Commit_flEnqueueChangedTiles);
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
		                Toast.makeText(TReflectionWindowEditorPanel.this, S, Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMMITTED:
		            	if (flCommitToTheServer) {
			                try {
				                //. add timestamped place to "ElectedPlaces"
				                TLocation TP = new TLocation();
				                TP.Name = Place.Name;
				                TP.RW = Component.ReflectionWindow.GetWindow();
				                TP.RW.BeginTimestamp = TReflectionWindowActualityInterval.NullTimestamp;
				                TP.RW.EndTimestamp = Timestamp;
								Component.ElectedPlaces.AddPlace(TP);
							} catch (Exception Ex) {
				                Toast.makeText(TReflectionWindowEditorPanel.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
				            	//.
				            	break; //. >
							}
			            	//.
			            	if (flReSet && (ReSetInterval < 1.0/*1 day*/))
				                Toast.makeText(Component.ParentActivity, getString(R.string.SImageHasBeenReset)+Place.Name+"'", Toast.LENGTH_LONG).show();
		            	}
		            	//.
		            	if (flCloseEditor)
		            		TReflectionWindowEditorPanel.this.finish();
		            	//.
		            	if (flCommitToTheServer) {
			            	if (flReSet && (ReSetInterval < 1.0/*1 day*/))
				                Toast.makeText(Component.ParentActivity, getString(R.string.SImageHasBeenReset)+Place.Name+"'", Toast.LENGTH_LONG).show();
		            	}
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TReflectionWindowEditorPanel.this);    
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
	
    private class TChangesCommittingForVisualization extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION	 			= 0;
    	private static final int MESSAGE_COMMITTED 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private int 	SecurityFileID;
    	private String  DataFileName;
    	private boolean flCommitToTheServer;
    	private boolean Commit_flEnqueueChangedTiles;
    	private boolean flCloseEditor;
    	
        private ProgressDialog progressDialog; 
    	
    	public TChangesCommittingForVisualization(int pSecurityFileID, String pDataFileName, boolean pflCommitToTheServer, boolean pCommit_flEnqueueChangedTiles, boolean pflCloseEditor) {
    		SecurityFileID = pSecurityFileID;
    		DataFileName = pDataFileName;
    		flCommitToTheServer = pflCommitToTheServer;
    		Commit_flEnqueueChangedTiles = pCommit_flEnqueueChangedTiles;
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
    				CommitChangesForVisualization(SecurityFileID,DataFileName,  flCommitToTheServer,Commit_flEnqueueChangedTiles);
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
		                Toast.makeText(TReflectionWindowEditorPanel.this, S, Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMMITTED:
		            	//.
		            	if (flCloseEditor)
		            		TReflectionWindowEditorPanel.this.finish();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TReflectionWindowEditorPanel.this);    
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
	
    private class TUserSecurityFileGettingAndCommitting extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION	 					= 0;
    	private static final int MESSAGE_USERSECURITYFILESARELOADED 	= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 				= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 				= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 			= 4;

    	private TGeoScopeServerUser.TUserSecurityFiles UserSecurityFiles = null;
    	
        private ProgressDialog progressDialog; 
    	
    	public TUserSecurityFileGettingAndCommitting() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				if (!Component.flOffline) {
    					try {
    						UserSecurityFiles = Component.User.GetUserSecurityFiles();
    					}
    		        	catch (IOException E) {
        					UserSecurityFiles = null;
    		        	}
    				}
    				else
    					UserSecurityFiles = null;
    				//.
        			MessageHandler.obtainMessage(MESSAGE_USERSECURITYFILESARELOADED).sendToTarget();
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
        	}
        	catch (InterruptedException E) {
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    @SuppressLint("HandlerLeak")
		private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		        	if (!flExists)
		        		return; //. ->
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_USERSECURITYFILESARELOADED:
	            		int UserSecurityFileID = 0;
	            		if (UserSecurityFiles != null)
	            			UserSecurityFileID = UserSecurityFiles.idSecurityFileForPrivate;
	            		//.
	                	Intent intent = new Intent(TReflectionWindowEditorPanel.this, TReflectionWindowEditorCommittingPanel.class);
	                	//.
	                	intent.putExtra("UserSecurityFileID",UserSecurityFileID);
	                	//.
	                	intent.putExtra("PlaceName",Drawings_Descriptor.Name);
	                	intent.putExtra("UserSecurityFileIDForCommit",Drawings_Descriptor.UserSecurityFileID);
	                	intent.putExtra("ReSetInterval",Drawings_Descriptor.ReSetInterval);
	                	intent.putExtra("DataFileName",Drawings_Descriptor.DataFileName);
	                	//.
	                	startActivityForResult(intent,REQUEST_COMMITTING);
	            		break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TReflectionWindowEditorPanel.this);    
		            	progressDialog.setMessage(getString(R.string.SWaitAMoment));    
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
	
    private static class TImageContainer {
    	
    	public float 					dX = 0.0F;
    	public float 					dY = 0.0F;
    	public TReflectionWindowStruc 	RW = null;
    	public TRWLevelTileContainer 	LevelTileContainer = null;
    	public boolean 					flModified = false;
    	
    	public TImageContainer() {
    	}
    	
    	public void Assign(TImageContainer Container) {
    		dX = Container.dX;
    		dY = Container.dY;
    		if (RW != null)
    			RW.Assign(Container.RW);
    		else
    			RW = new TReflectionWindowStruc(Container.RW);
    		if (LevelTileContainer != null)
    			LevelTileContainer.AssignContainer(Container.LevelTileContainer);
    		else
    			LevelTileContainer = new TRWLevelTileContainer(Container.LevelTileContainer);
    		flModified = Container.flModified;
    	}

    	public byte[] ToByteArray() throws IOException {
    		byte[] Result = new byte[8/*SizeOf(dX)*/+8/*SiizeOf(dY)*/+TReflectionWindowStruc.ByteArraySize()+1];
    		int Idx = 0;
    		byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(dX);
    		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    		BA = TDataConverter.ConvertDoubleToLEByteArray(dY);
    		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    		BA = RW.ToByteArray();
    		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
    		if (flModified)
    			Result[Idx] = 1;
    		else
    			Result[Idx] = 0;
    		return Result;
    	}
    	
    	public int FromByteArray(byte[] BA, int Idx) throws IOException {
    		dX = (float)TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
    		dY = (float)TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
    		RW = new TReflectionWindowStruc();
    		Idx = RW.FromByteArrayV1(BA, Idx);
    		LevelTileContainer = null;
    		flModified = (BA[Idx] != 0); Idx++;
    		return Idx;
    	}
    	
    	public void Translate(float pdX, float pdY) {
    		dX += pdX;
    		dY += pdY;
    	}
    }
    
    private ArrayList<TImageContainer> 	Containers;
    private TImageContainer 			Containers_CurrentContainer;
	public boolean 						Containers_CurrentContainer_flUpdating = true;
	public int							Containers_CurrentContainer_Updating_ProgressPercentage = -1;
    
    public void Containers_Initialize() throws Exception {
    	Containers = new ArrayList<TImageContainer>(10);
    	Containers_CurrentContainer = null;
    }

    public void Containers_Finalize() {
    	if (Containers_CurrentContainer != null)
    		Containers_CancelCurrentContainer();
    }
    
    public void Containers_Translate(float dX, float dY) {
    	int Size = Containers.size();
    	for (int I = 0; I < Size; I++) 
    		Containers.get(I).Translate(dX,dY);
    }
    
	public byte[] Containers_ToByteArray() throws Exception {
		Containers_FinishCurrentContainer();
		Containers_AddCurrentContainer();
		//.
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			byte[] BA;
			int ContainersCount;
			if (Containers != null)
				ContainersCount = Containers.size();
			else
				ContainersCount = 0;
			BA = TDataConverter.ConvertInt32ToLEByteArray(ContainersCount);
			BOS.write(BA);
			if (ContainersCount > 0)
				for (int I = 0; I < ContainersCount; I++) {
					BA = Containers.get(I).ToByteArray();
					BOS.write(BA);
				}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public int Containers_FromByteArray(byte[] BA, int Idx) throws Exception {
    	Containers.clear();
    	Containers_CurrentContainer = null;
    	//.
    	int ContainersCount = (int)TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < ContainersCount; I++) {
			TImageContainer Container = new TImageContainer();
			Idx = Container.FromByteArray(BA, Idx);
			//.
			Container.LevelTileContainer = UserDrawableCompilation.ReflectionWindow_GetLevelTileContainer(Container.RW);
			//.
			Containers.add(Container);
		}
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
            				SpaceImageUpdating = Component.GetSpaceImageUpdating();
            			} catch (Exception E) {
            				return; //. ->
            			} 
                    	if (SpaceImageUpdating != null) {
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
    					Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
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

    public TImageContainer Containers_StartCurrentContainer(float dX, float dY) throws Exception {
    	Containers_CompleteCurrentContainer();
    	Containers_CurrentContainer = new TImageContainer();
    	//.
    	Containers_CurrentContainer_Updating_ProgressPercentage = -1;
    	Containers_CurrentContainer_flUpdating = true;
    	Component.TranslateReflectionWindow(dX,dY);
    	//.
		Containers_CurrentContainer_Updater_Start();
		//.
		return Containers_CurrentContainer;
    }
    
    public TImageContainer Containers_StartCurrentContainer() throws Exception {
    	return Containers_StartCurrentContainer(0.0F,0.0F);
    }
    
    public void Containers_FinishCurrentContainer() throws Exception {
    	if (Containers_CurrentContainer != null) {
        	Containers_CurrentContainer_Updater_Stop();
        	//.
        	Containers_CurrentContainer.RW = Component.ReflectionWindow.GetWindow();
			Containers_CurrentContainer.LevelTileContainer = UserDrawableCompilation.ReflectionWindow_GetLevelTileContainer(Containers_CurrentContainer.RW);
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
    
    public TImageContainer Containers_GetCurrentContainer() {
    	return Containers_CurrentContainer;
    }
    
    public void Containers_SetCurrentContainer(TImageContainer Container) {
    	if (Containers_CurrentContainer != null)
    		Containers_CurrentContainer.Assign(Container);
    }
    
    public void Containers_SetCurrentContainerAsModified() {
    	if (Containers_CurrentContainer != null) 
    		Containers_CurrentContainer.flModified = true;
    }
    
    public void Containers_CompleteCurrentContainer() {
    	if (Containers_CurrentContainer != null) 
    		if ((Containers_CurrentContainer.LevelTileContainer != null/*finished*/) && Containers_CurrentContainer.flModified) 
    			Containers_AddCurrentContainer();
    		else
    			Containers_CancelCurrentContainer();
    }

	public void Containers_RestoreTileModifications() throws Exception {
		if (Containers_CurrentContainer_flUpdating)
			throw new Exception(getString(R.string.SCannotCommitImageIsUpdating)); //. =>
		for (int I = 0; I < Containers.size(); I++) {
			TImageContainer C = Containers.get(I);
			if (C.LevelTileContainer != null)
				C.LevelTileContainer.TileLevel.Container_RestoreTileModifications(C.LevelTileContainer);
			else
				throw new Exception(getString(R.string.SThereIsNoVisibleUserDrawableTilesLayer)); //. =>
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
		if (SurfaceUpdating != null)
			SurfaceUpdating.StartUpdate();
	}
	
	private void LineDrawingProcess_End() {
		if (LineDrawingProcess_flProcessing) {
			LineDrawingProcess_flProcessing = false;
			//.
			Drawings_Add(LineDrawing);
			Containers_SetCurrentContainerAsModified();
			//.
			LineDrawingProcess_LastX = -1;
			//.
			if (SurfaceUpdating != null)
				SurfaceUpdating.StartUpdate();
			//.
			btnReflectionWindowEditorUndo.setEnabled(true);
			btnReflectionWindowEditorRedo.setEnabled(false);
			btnReflectionWindowEditorClear.setEnabled(true);
		}
	}
	
	private void LineDrawingProcess_Draw(float X, float Y) {
		if (LineDrawingProcess_flProcessing) {
			if (DrawableImageCanvas != null)
				DrawableImageCanvas.drawLine(X,Y, LineDrawingProcess_LastX,LineDrawingProcess_LastY, LineDrawingProcess_Brush);
			//.
			TDrawingNode DN = new TDrawingNode(X,Y);
			LineDrawing.Nodes.add(DN); 
			//.
			LineDrawingProcess_LastX = X;
			LineDrawingProcess_LastY = Y;
			//.
			if (SurfaceUpdating != null)
				SurfaceUpdating.StartUpdate();
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
  	  return new File(TGeoLogApplication.TempFolder,"picture.jpg");
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
		TImageContainer PictureContainer = Containers_GetCurrentContainer();
		if (PictureContainer == null)
			return; //. ->
		TReflectionWindowStruc RW = Component.ReflectionWindow.GetWindow();
		int Xmn = RW.Xmn;
		if (PictureDrawing.Node.X < Xmn) 
			Xmn = (int)PictureDrawing.Node.X;
		int Ymn = RW.Ymn;
		if (PictureDrawing.Node.Y < Ymn) 
			Ymn = (int)PictureDrawing.Node.Y;
		int Xmx = RW.Xmx;
		if ((PictureDrawing.Node.X+PictureDrawing.Picture.getWidth()) > Xmx) 
			Xmx = (int)(PictureDrawing.Node.X+PictureDrawing.Picture.getWidth());
		int Ymx = RW.Ymx;
		if ((PictureDrawing.Node.Y+PictureDrawing.Picture.getHeight()) > Ymx) 
			Ymx = (int)(PictureDrawing.Node.Y+PictureDrawing.Picture.getHeight());
		TXYCoord P0 = RW.ConvertToReal(Xmn,Ymn);
		TXYCoord P1 = RW.ConvertToReal(Xmx,Ymn);
		TXYCoord P2 = RW.ConvertToReal(Xmx,Ymx);
		TXYCoord P3 = RW.ConvertToReal(Xmn,Ymx);
		//.
		RW.Xmn = Xmn; RW.Ymn = Ymn; 
		RW.Xmx = Xmx; RW.Ymx = Ymx; 
		RW.X0 = P0.X; RW.Y0 = P0.Y;
		RW.X1 = P1.X; RW.Y1 = P1.Y;
		RW.X2 = P2.X; RW.Y2 = P2.Y;
		RW.X3 = P3.X; RW.Y3 = P3.Y;
		RW.Normalize();
		//.
		PictureContainer.LevelTileContainer = UserDrawableCompilation.ReflectionWindow_GetLevelTileContainer(RW);
		//.
		Drawings_Add(PictureDrawing);
		PictureDrawing = null;
		//.
		Containers_SetCurrentContainerAsModified();
		//.
		btnReflectionWindowEditorUndo.setEnabled(true);
		btnReflectionWindowEditorRedo.setEnabled(false);
		btnReflectionWindowEditorClear.setEnabled(true);
	}
	
	private void PictureDrawingProcess_Draw() throws Exception {
		if (DrawableImageCanvas != null) {
			PictureDrawing.Paint(DrawableImageCanvas);
			//.
			if (SurfaceUpdating != null)
				SurfaceUpdating.StartUpdate();
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
		
		private static double GetCurrentTimestamp() {
			return ((long)(OleDate.UTCCurrentTimestamp()*TTile.TileTimestampResolution))/TTile.TileTimestampResolution;
		}
		
		public double 	Timestamp = GetCurrentTimestamp();
		public String 	Name = "";
		public int 		UserSecurityFileID = 0;
		public double 	ReSetInterval = 0.0;
		public String	DataFileName = "";
		
		public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
	    	Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		Name = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Name = "";
	    	UserSecurityFileID = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 8; //. SizeOf(Int64)
	    	ReSetInterval = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
	    	return Idx;
		}

		public int FromByteArrayV2(byte[] BA, int Idx) throws IOException {
	    	Timestamp = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		Name = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Name = "";
	    	UserSecurityFileID = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 8; //. SizeOf(Int64)
	    	ReSetInterval = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx += 8;
	    	short SL = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2;
	    	if (SL > 0) {
	    		DataFileName = new String(BA, Idx,SL, "windows-1251");
	    		Idx += SL;
	    	}
	    	else
	    		DataFileName = "";
	    	return Idx;
		}

		public byte[] ToByteArrayV2() throws IOException {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				byte[] BA = TDataConverter.ConvertDoubleToLEByteArray(Timestamp);
				BOS.write(BA);
				//.
				short SL = (short)Name.length();
				byte[] BA_SL = new byte[1];
				BA_SL[0] = (byte)SL;
				BOS.write(BA_SL);
				if (SL > 0)
					BOS.write(Name.getBytes("windows-1251"));
				//.
				BA = TDataConverter.ConvertInt32ToLEByteArray(UserSecurityFileID);
				byte[] BA_32TO64 = new byte[4];
				BOS.write(BA);
				BOS.write(BA_32TO64);
			    //.
				BA = TDataConverter.ConvertDoubleToLEByteArray(ReSetInterval);
				BOS.write(BA);
				//.
				SL = (short)DataFileName.length();
				BA_SL = TDataConverter.ConvertInt16ToLEByteArray(SL);
				BOS.write(BA_SL);
				if (SL > 0)
					BOS.write(DataFileName.getBytes("windows-1251"));
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
	
	public int Drawings_FromByteArray(byte[] BA, int Idx) throws IOException {
		Drawings.clear();
		Drawings_HistoryIndex = 0;
		//.
		int DrawingsCount = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < DrawingsCount; I++) {
			short DrawingTypeID = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
			TDrawing Drawing = TDrawing.CreateInstance(DrawingTypeID);
			if (Drawing == null)
				throw new IOException("unknown drawing type: "+Short.toString(DrawingTypeID)); //. =>
			Idx = Drawing.FromByteArray(BA, Idx);
			Drawings.add(Drawing);
		}
		Drawings_HistoryIndex = DrawingsCount; 
		//.
 		Drawings_flSaved = true;
		Drawings_flChanged = false;
		//.
		return Idx;
	}
	
	public int Drawings_FromByteArrayV1(byte[] BA, int Idx) throws IOException {
		Idx = Drawings_Descriptor.FromByteArrayV1(BA, Idx);
		//.
		Drawings.clear();
		Drawings_HistoryIndex = 0;
		//.
		int DrawingsCount = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < DrawingsCount; I++) {
			short DrawingTypeID = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
			TDrawing Drawing = TDrawing.CreateInstance(DrawingTypeID);
			if (Drawing == null)
				throw new IOException("unknown drawing type: "+Short.toString(DrawingTypeID)); //. =>
			Idx = Drawing.FromByteArray(BA, Idx);
			Drawings.add(Drawing);
		}
		Drawings_HistoryIndex = DrawingsCount; 
		//.
 		Drawings_flSaved = true;
		Drawings_flChanged = false;
		//.
		return Idx;
	}
	
	public int Drawings_FromByteArrayV2(byte[] BA, int Idx) throws IOException {
		Idx = Drawings_Descriptor.FromByteArrayV2(BA, Idx);
		//.
		Drawings.clear();
		Drawings_HistoryIndex = 0;
		//.
		int DrawingsCount = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < DrawingsCount; I++) {
			short DrawingTypeID = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
			TDrawing Drawing = TDrawing.CreateInstance(DrawingTypeID);
			if (Drawing == null)
				throw new IOException("unknown drawing type: "+Short.toString(DrawingTypeID)); //. =>
			Idx = Drawing.FromByteArray(BA, Idx);
			Drawings.add(Drawing);
		}
		Drawings_HistoryIndex = DrawingsCount; 
		//.
 		Drawings_flSaved = true;
		Drawings_flChanged = false;
		//.
		return Idx;
	}
	
	public byte[] Drawings_ToByteArrayV2() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			BOS.write(Drawings_Descriptor.ToByteArrayV2());
			//.
			byte[] BA;
			int DrawingsCount;
			if (Drawings != null) 
				DrawingsCount = Drawings_HistoryIndex;
			else
				DrawingsCount = 0;
			BA = TDataConverter.ConvertInt32ToLEByteArray(DrawingsCount);
			BOS.write(BA);
			for (int I = 0; I < DrawingsCount; I++) {
				TDrawing Drawing = Drawings.get(I);
				short DrawingTypeID = Drawing.TypeID();
				BA = TDataConverter.ConvertInt16ToLEByteArray(DrawingTypeID);
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
	
	private void Drawings_Show() {
		ReflectionWindowEditorSurfaceLayout.setVisibility(View.VISIBLE);
	}
	
	private void Drawings_Hide() {
		ReflectionWindowEditorSurfaceLayout.setVisibility(View.GONE);
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
		int Cnt = Drawings.size();
		for (int I = 0; I < Cnt; I++) 
			Drawings.get(I).Translate(dX,dY);
	}
	
	public TRectangle Drawings_GetRectangle() {
		int Cnt = Drawings.size();
		if (Cnt == 0)
			return null; //. ->
		TRectangle Result = Drawings.get(0).GetRectangle(); 
		for (int I = 1; I < Cnt; I++) {
			TRectangle Rectangle = Drawings.get(I).GetRectangle();
			if (Rectangle.Xmn < Result.Xmn)
				Result.Xmn = Rectangle.Xmn; 
			if (Rectangle.Xmx > Result.Xmx)
				Result.Xmx = Rectangle.Xmx; 
			if (Rectangle.Ymn < Result.Ymn)
				Result.Ymn = Rectangle.Ymn; 
			if (Rectangle.Ymx > Result.Ymx)
				Result.Ymx = Rectangle.Ymx; 
		}
		return Result;
	}
	
	public void Drawings_UpdateImage() throws Exception {
		if (DrawableImage == null)
			return; //. ->
		synchronized (Drawings_ImageLock) {
			DrawableImage.eraseColor(Color.TRANSPARENT);
			DrawableImageCanvas.drawBitmap(OriginDrawableImage,0,0,null);
			//.
			for (int I = 0; I < Drawings_HistoryIndex; I++) 
				Drawings.get(I).Paint(DrawableImageCanvas);
			//.
			SurfaceUpdating.StartUpdate();
		}
	}
	
	public void Drawings_RepaintImage() throws Exception {
		if (DrawableImage == null)
			return; //. ->
		Moving_Reset();
		//. repaint bitmap from current ReflectionWindow
		if (BackgroundImage == null)
			return; //. ->
		synchronized (Drawings_ImageLock) {
			TReflectionWindowStruc RW = Component.ReflectionWindow.GetWindow();
			try {
				BackgroundImage.eraseColor(Color.TRANSPARENT);
				Canvas canvas = new Canvas(BackgroundImage);
				TileImagery.ActiveCompilationSet_ReflectionWindow_DrawOnCanvasTo(RW, 0,canvas,paint,null, null,null, UserDrawableCompilation);
				//.
				ForegroundImage.eraseColor(Color.TRANSPARENT);
				canvas = new Canvas(ForegroundImage);
				TileImagery.ActiveCompilationSet_ReflectionWindow_DrawOnCanvasFrom(RW, 0,canvas,paint,null, null,null, UserDrawableCompilation);
				if (Component.Configuration.ReflectionWindow_flShowHints) 
					Component.SpaceHints.DrawOnCanvas(RW, Component.DynamicHintVisibleFactor, canvas);
				//.
				OriginDrawableImage.eraseColor(Color.TRANSPARENT);
				canvas = new Canvas(OriginDrawableImage);
				UserDrawableCompilation.ReflectionWindow_DrawOnCanvas(RW, 0,canvas,paint,null, false, null,null, null);
			}
			catch (TimeIsExpiredException E) {}
			//.
			DrawableImage.eraseColor(Color.TRANSPARENT);
			DrawableImageCanvas.drawBitmap(OriginDrawableImage,0,0,null);
			//.
			for (int I = 0; I < Drawings_HistoryIndex; I++) 
				Drawings.get(I).Paint(DrawableImageCanvas);
		}
		//.
		SurfaceUpdating.StartUpdate();
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
	
	public void Drawings_Commit() throws Exception {
		if (Drawings_HistoryIndex == 0)
			return; //. ->
		if (Containers_CurrentContainer_flUpdating)
			throw new Exception(getString(R.string.SCannotCommitImageIsUpdating)); //. =>
		Containers_CompleteCurrentContainer();
		List<TDrawing> _Drawings = Drawings.subList(0,Drawings_HistoryIndex);
		for (int I = 0; I < Containers.size(); I++) {
			TImageContainer C = Containers.get(I);
			if (C.LevelTileContainer != null) {
				C.LevelTileContainer.TileLevel.Container_PaintDrawings(C.LevelTileContainer,_Drawings, true, C.dX,C.dY);
				//.
				TGeoLogApplication.Instance().GarbageCollector.Collect();
			}
			else
				throw new Exception(getString(R.string.SThereIsNoVisibleUserDrawableTilesLayer)); //. =>
		}
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
		}
		Containers_Translate(dX,dY);
		Drawings_Translate(dX,dY);
		//.
		Containers_StartCurrentContainer(dX,dY);
		//.
		SurfaceUpdating.StartUpdate();
	}
	
	private void Moving_Move(float X, float Y) {
		synchronized (Moving_Lock) {
			if (Moving_flProcessing) {
				Moving_X = X;
				Moving_Y = Y;
				//.
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
	private	RelativeLayout 	ReflectionWindowEditorSettingsLayout;
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
		Settings_Brush.setColor(Preferences.getInt("Settings_Brush_Color", Color.RED));
		Settings_Brush.setStrokeWidth(Preferences.getFloat("Settings_Brush_Width", 3.0F*metrics.density));
		int BS = Preferences.getInt("Settings_Brush_Blur_Style", 0);
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
		Settings_Brush_Blur_Radius = Preferences.getFloat("Settings_Brush_Blur_Radius", 0.0F);
		if (Settings_Brush.getColor() != Color.TRANSPARENT)
			Settings_Brush.setXfermode(Settings_DefaultBrush.getXfermode());
		else
			Settings_Brush.setXfermode(new android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_OUT)); 
		if (Settings_Brush_Blur_Radius > 0.0F)
			Settings_Brush.setMaskFilter(new BlurMaskFilter(Settings_Brush_Blur_Radius,Settings_Brush_Blur_Style));
		else 
			Settings_Brush.setMaskFilter(null);
		//.
		ReflectionWindowEditorSettingsLayout = (RelativeLayout)findViewById(R.id.ReflectionWindowEditorSettingsLayout);
		//.
		Settings_TestImage = (TSettingsTestImage)findViewById(R.id.ivRWESettingsTest);
		//.
		btnBrushColor = (Button)findViewById(R.id.btnRWEBrushColor);
		btnBrushColor.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		ColorPicker ColorDialog = new ColorPicker(TReflectionWindowEditorPanel.this, new ColorPicker.OnColorChangedListener() {
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
		ReflectionWindowEditorSettingsLayout.setVisibility(View.VISIBLE);
	}
	
	private void Settings_Hide() {
		ReflectionWindowEditorSettingsLayout.setVisibility(View.GONE);
	}
	
	private void Settings_Apply() {
		SharedPreferences Preferences = getPreferences(MODE_PRIVATE);
		Editor editor = Preferences.edit();
		editor.putInt("Settings_Brush_Color", Settings_Brush.getColor());
		editor.putFloat("Settings_Brush_Width", Settings_Brush.getStrokeWidth());
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
    	editor.putInt("Settings_Brush_Blur_Style", BS);
    	editor.putFloat("Settings_Brush_Blur_Radius", Settings_Brush_Blur_Radius);
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
		editor.putInt("Settings_Brush_Color", Settings_Brush.getColor());
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
		editor.putFloat("Settings_Brush_Width", Settings_Brush.getStrokeWidth());
    	editor.commit();
		//.
    	LineDrawingProcess_Brush = new Paint();
		LineDrawingProcess_Brush.set(Settings_Brush);
	}
}
