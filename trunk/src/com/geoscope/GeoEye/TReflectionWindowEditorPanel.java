package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import android.graphics.Matrix;
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
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowActualityInterval;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TRWLevelTileContainer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImageryDataServer.TTilesPlace;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;
import com.geoscope.GeoEye.Utils.ColorPicker;
import com.geoscope.GeoEye.Utils.Graphics.TDrawing;
import com.geoscope.GeoEye.Utils.Graphics.TDrawingNode;
import com.geoscope.GeoEye.Utils.Graphics.TLineDrawing;
import com.geoscope.GeoEye.Utils.Graphics.TPictureDrawing;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.Utils.TDataConverter;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;

@SuppressLint("HandlerLeak")
public class TReflectionWindowEditorPanel extends Activity implements OnTouchListener {

	public static final int MODE_NONE 		= 0;
	public static final int MODE_DRAWING 	= 1;
	public static final int MODE_MOVING 	= 2;
	public static final int MODE_SETTINGS 	= 3;
	//.
	public static final int REQUEST_ADDPICTURE 			= 1;
	public static final int REQUEST_ADDPICTUREFROMFILE 	= 2;
	public static final int REQUEST_COMMITTING 			= 3;
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
				Background = Reflector().WorkSpace.BackgroundImage_ReCreate(width, height);
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
        	try {
				Drawings_RepaintImage();
				//.
	        	if (flStartInitialContainer) {
	        		flStartInitialContainer = false;
	    			Containers_StartCurrentContainer();
	        	}
			} catch (Exception E) {
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
								canvas.drawBitmap(Background, 0,0, null);
								canvas.drawBitmap(BackgroundImage, dX,dY, null);
								canvas.drawBitmap(DrawableImage, dX,dY, null);
								canvas.drawBitmap(ForegroundImage, dX,dY, null);
								if (LineDrawingProcess_flProcessing) 
									canvas.drawCircle(LineDrawingProcess_LastX,LineDrawingProcess_LastY,LineDrawingProcess_Brush.getStrokeWidth()/2.0F,LineDrawingProcess_MarkerPaint);
								//.
								float OfsY = ReflectionWindowEditorSurfaceControlLayout.getHeight();
								ColorPickerBar.Paint(canvas, 0,OfsY, Surface_Width,Surface_Height-OfsY, Settings_Brush.getColor());
								//. show status
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
		
		public static final float CurrentColorIndicatorWidthFactor = 0.1F;
		
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
		private Paint paint;
		private Paint ColorBoxPaint;
		//.
		private float CurrentColorIndicatorWidth;
		
		public TColorPickerBar(float pSize, int pLayout) {
			Size = pSize;
			Layout = pLayout;
			//.
			paint = new Paint();
			//.
			ColorBoxPaint = new Paint();
			//.
			CurrentColorIndicatorWidth = Size*CurrentColorIndicatorWidthFactor;
		}
		
		public void Paint(Canvas _Canvas, float pLeft, float pTop, float pWidth, float pHeight, int CurrentColor) {
			if (Size == 0)
				return; //. ->
			switch (Layout) {
			
			case LAYOUT_LEFTALIGN:
				float ItemWidth = Size-CurrentColorIndicatorWidth;
				float ItemHeight = ((pHeight+0.0F)/Colors.length);
				float X0 = pLeft;
				float Y0 = pTop;
				float XC0 = X0;
				for (int I = 0; I < Colors.length; I++) {
					int ItemColor = Colors[I];
					ColorBoxPaint.setColor(ItemColor);
					float YC0 = Y0+I*ItemHeight;
					_Canvas.drawRect(XC0,YC0, XC0+ItemWidth,YC0+ItemHeight, ColorBoxPaint);
					//. 
					if (ItemColor == CurrentColor) {
						if (ItemColor != Color.BLACK)
							paint.setColor(Color.BLACK);
						else
							paint.setColor(Color.WHITE);
						float CW = ItemWidth/2.0F;
						float CH = ItemHeight/2.0F;
						float R;
						if (ItemWidth < ItemHeight)
							R = ItemWidth;
						else
							R = ItemHeight;
						R = R/10.0F;
						_Canvas.drawCircle(XC0+CW,YC0+CH, R, paint);
					}
				}
				//.
				ColorBoxPaint.setColor(CurrentColor);
				_Canvas.drawRect(X0+ItemWidth,Y0, X0+Size,Y0+pHeight, ColorBoxPaint);
				//.
				Left = pLeft;
				Top = pTop;
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
				float ItemWidth = Size-CurrentColorIndicatorWidth;
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
	private static final float 	ColorPickerBarSize = 24F;
	private TColorPickerBar 	ColorPickerBar = null;
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
    	metrics = getApplicationContext().getResources().getDisplayMetrics();
        //.
        try {
    		TileImagery = Reflector().SpaceTileImagery;
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
			Reflector_ReflectionWindowLastActualityInterval = Reflector().ReflectionWindow.GetActualityInterval();
	    	Reflector().ReflectionWindow.SetActualityInterval(Reflector_ReflectionWindowLastActualityInterval.BeginTimestamp,TReflectionWindowActualityInterval.MaxRealTimestamp);
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
		cbReflectionWindowEditorMode.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (arg1)
					SetMode(MODE_DRAWING);
				else
					SetMode(MODE_MOVING);
			}
        });
        //.
		btnReflectionWindowEditorBrushSelector = (Button)findViewById(R.id.btnReflectionWindowEditorBrushSelector);
		btnReflectionWindowEditorBrushSelector.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	SetMode(MODE_SETTINGS);
            }
        });
		//.
		btnReflectionWindowEditorUndo = (Button)findViewById(R.id.btnReflectionWindowEditorUndo);
		btnReflectionWindowEditorUndo.setEnabled(false);
		btnReflectionWindowEditorUndo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	try {
            		boolean R = Drawings_Undo();
            		btnReflectionWindowEditorUndo.setEnabled(R);
            		btnReflectionWindowEditorRedo.setEnabled(true);
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
            public void onClick(View v) {
            	try {
            		boolean R = Drawings_Redo();
            		btnReflectionWindowEditorRedo.setEnabled(R);
            		btnReflectionWindowEditorUndo.setEnabled(true);
		        } 
		        catch (Exception E) {
					Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
            }
        });
		//.
		btnReflectionWindowEditorClear = (Button)findViewById(R.id.btnReflectionWindowEditorClear);
		btnReflectionWindowEditorClear.setOnClickListener(new OnClickListener() {
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
		btnReflectionWindowEditorOperations.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
        		if (Containers_CurrentContainer_Updater_flProcessing) {
        			Toast.makeText(TReflectionWindowEditorPanel.this, R.string.SViewIsUpdating, Toast.LENGTH_LONG).show();
        			return; //. ->
        		}
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
	    						final String[] FileList;
    						    if(FileSelectorPath.exists()) {
    						        FilenameFilter filter = new FilenameFilter() {
    						            public boolean accept(File dir, String filename) {
    						                return filename.contains(".jpg");
    						            }
    						        };
    						        FileList = FileSelectorPath.list(filter);
    						    }
    						    else 
    						        FileList= new String[0];
    						    //.
    						    AlertDialog.Builder builder = new AlertDialog.Builder(TReflectionWindowEditorPanel.this);
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
						        			Toast.makeText(TReflectionWindowEditorPanel.this, S, Toast.LENGTH_SHORT).show();  						
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
		Containers_Initialize();
		//.
		Settings_Initialize();
		Drawings_Initialize();
		//.
		LineDrawingProcess_Initialize();
		PictureDrawingProcess_Initialize();
		Moving_Reset();
		SetMode(MODE_DRAWING);
		//.
		ColorPickerBar = new TColorPickerBar(ColorPickerBarSize*metrics.density, TColorPickerBar.LAYOUT_LEFTALIGN);
	}

	@Override
	protected void onDestroy() {
		PictureDrawingProcess_Finalize();
		LineDrawingProcess_Finalize();
		//.
		Containers_CurrentContainer_Updater_Stop();
		Drawings_Finalize();
		Settings_Finalize();
		//.
		Containers_Finalize();
		//. restore last time
		try {
			if (Reflector_ReflectionWindowLastActualityInterval != null)
				Reflector().ReflectionWindow.SetActualityInterval(Reflector_ReflectionWindowLastActualityInterval);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();  						
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
			break;

		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			if ((ColorPickerBar != null) && ColorPickerBar.Inside(pEvent.getX(),pEvent.getY())) 
				return true; //. ->
			break;
		}
		//.
		switch (GetMode()) {
		
		case MODE_DRAWING:
			if (Containers_CurrentContainer_flUpdating)
				return false; //. ->
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
		if (Drawings_HistoryIndex > 0) {
		    new AlertDialog.Builder(this)
	        .setIcon(android.R.drawable.ic_dialog_alert)
	        .setTitle(R.string.SConfirmation)
	        .setMessage(R.string.SChangesYouHaveMadeWillBeLost)
		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
		    	public void onClick(DialogInterface dialog, int id) {
		    		finish();
		    	}
		    })
		    .setNegativeButton(R.string.SNo, null)
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
            		int UserSecurityFileID = extras.getInt("UserSecurityFileID");
            		boolean flReSet = extras.getBoolean("flReSet");
            		double ReSetInterval = extras.getDouble("ReSetInterval");
            		String PlaceName = extras.getString("PlaceName");
            		//.
                	try {
                		TReflectionWindowStruc RW = Reflector().ReflectionWindow.GetWindow();
                		TTilesPlace Place = new TTilesPlace(PlaceName,RW);
                		//.
                    	new TChangesCommitting(UserSecurityFileID,flReSet,ReSetInterval,Place,true);
    				}
    				catch (Exception E) {
    					String S = E.getMessage();
    					if (S == null)
    						S = E.getClass().getName();
            			Toast.makeText(TReflectionWindowEditorPanel.this, TReflectionWindowEditorPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
    				}
                }
        	}  
            break; //. >
        }
        super.onActivityResult(requestCode, resultCode, data);
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
		if (SurfaceUpdating != null)
			SurfaceUpdating.Start();
	}
	
	public double CommitChanges(int SecurityFileID, boolean flReSet, double ReSetInterval, TTilesPlace TilesPlace) throws Exception {
		double Result;
		//. commit drawings into tiles locally
		Drawings_Commit();
		//. committing on the server
		Result = TileImagery.ActiveCompilationSet_CommitModifiedTiles(SecurityFileID,flReSet,ReSetInterval,TilesPlace);
		//.
		Drawings_Clear();
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
    	private boolean flCloseEditor;
    	//.
    	private double Timestamp;
    	
        private ProgressDialog progressDialog; 
    	
    	public TChangesCommitting(int pSecurityFileID, boolean pflReSet, double pReSetInterval, TTilesPlace pPlace, boolean pflCloseEditor) {
    		SecurityFileID = pSecurityFileID;
    		flReSet = pflReSet;
    		ReSetInterval = pReSetInterval;
    		Place = pPlace;
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
    				Timestamp = CommitChanges(SecurityFileID,flReSet,ReSetInterval,Place);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_COMMITTED).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_COMMITTED:
	                try {
		                //. add timestamped place to "ElectedPlaces"
		                TLocation TP = new TLocation();
		                TP.Name = Place.Name;
		                TP.RW = Reflector().ReflectionWindow.GetWindow();
		                TP.RW.BeginTimestamp = TReflectionWindowActualityInterval.NullTimestamp;
		                TP.RW.EndTimestamp = Timestamp;
						Reflector().ElectedPlaces.AddPlace(TP);
					} catch (Exception Ex) {
		                Toast.makeText(TReflectionWindowEditorPanel.this, Ex.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
					}
	            	//.
	            	if (flReSet && (ReSetInterval < 1.0/*1 day*/))
		                Toast.makeText(TReflectionWindowEditorPanel.this, getString(R.string.SImageHasBeenReset)+Place.Name+"'", Toast.LENGTH_LONG).show();
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
    				UserSecurityFiles = Reflector().User.GetUserSecurityFiles();
    				//.
        			MessageHandler.obtainMessage(MESSAGE_USERSECURITYFILESARELOADED).sendToTarget();
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_EXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_USERSECURITYFILESARELOADED:
            		int UserSecurityFileID = UserSecurityFiles.idSecurityFileForPrivate;
            		//.
                	Intent intent = new Intent(TReflectionWindowEditorPanel.this, TReflectionWindowEditorCommittingPanel.class);
                	intent.putExtra("UserSecurityFileID",UserSecurityFileID);
                	startActivityForResult(intent,REQUEST_COMMITTING);
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
	
    private class TImageContainer {
    	
    	public float 					dX = 0.0F;
    	public float 					dY = 0.0F;
    	public TRWLevelTileContainer 	LevelTileContainer = null;
    	public boolean 					flModified = false;
    	
    	public void Translate(float pdX, float pdY) {
    		dX += pdX;
    		dY += pdY;
    	}
    	
    	public void Assign(TImageContainer Container) {
    		dX = Container.dX;
    		dY = Container.dY;
    		if (LevelTileContainer != null)
    			LevelTileContainer.AssignContainer(Container.LevelTileContainer);
    		else
    			LevelTileContainer = new TRWLevelTileContainer(Container.LevelTileContainer);
    		flModified = Container.flModified;
    	}
    }
    
    private ArrayList<TImageContainer> 	Containers;
    private TImageContainer 			Containers_CurrentContainer;
	public boolean 						Containers_CurrentContainer_flUpdating = true;
	public int							Containers_CurrentContainer_Updating_ProgressPercentage = -1;
    
    public void Containers_Initialize() {
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
    	public static final int MESSAGE_DONE 	= 2;
    	
        public void run()
        {
        	TReflector.TSpaceImageUpdating SpaceImageUpdating;
			try {
				SpaceImageUpdating = Reflector().GetSpaceImageUpdating();
			} catch (Exception E) {
				return; //. ->
			} 
        	if (SpaceImageUpdating != null) {
        		if ((Containers_CurrentContainer_Updater_ImageUpdateIntervalCounter % Containers_CurrentContainer_Updater_ImageUpdateIntervalCount) == 0) {
        			int ProgressPercentage = SpaceImageUpdating.ImageProgressor.ProgressPercentage(); 
            		Containers_CurrentContainer_Updater_Handler.obtainMessage(MESSAGE_UPDATE,ProgressPercentage).sendToTarget();
        		}
        		//.
        		Containers_CurrentContainer_Updater_ImageUpdateIntervalCounter++;
        	}
        	else
        		Containers_CurrentContainer_Updater_Handler.obtainMessage(MESSAGE_DONE).sendToTarget();
        }
    }   
	
    private final Handler Containers_CurrentContainer_Updater_Handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            
            case TContainersCurrentContainerUpdaterTask.MESSAGE_UPDATE:
    			int ProgressPercentage = (Integer)msg.obj;
    			try {
    				Containers_CurrentContainer_Updater_DoOnUpdating(ProgressPercentage);  
		        } 
		        catch (Exception E) {
					Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
            	break; //. >

            case TContainersCurrentContainerUpdaterTask.MESSAGE_DONE:
            	try {
            		Containers_CurrentContainer_Updater_DoOnUpdated();  
		        } 
		        catch (Exception E) {
					Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
		        }  
            	break; //. >
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
    	Reflector().TranslateReflectionWindow(dX,dY);
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
			TReflectionWindowStruc RW = Reflector().ReflectionWindow.GetWindow();
			Containers_CurrentContainer.LevelTileContainer = UserDrawableCompilation.ReflectionWindow_GetLevelTileContainer(RW);
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
		LineDrawingProcess_flProcessing = false;
		//.
		Drawings_Add(LineDrawing);
		Containers_SetCurrentContainerAsModified();
		//.
		LineDrawingProcess_LastX = -1;
		//.
		SurfaceUpdating.Start();
		//.
		btnReflectionWindowEditorUndo.setEnabled(true);
		btnReflectionWindowEditorRedo.setEnabled(false);
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
		//.
		if (SurfaceUpdating != null)
			SurfaceUpdating.Start();
	}
	
	private void PictureDrawingProcess_End() throws Exception {
		TImageContainer PictureContainer = Containers_GetCurrentContainer();
		if (PictureContainer == null)
			return; //. ->
		TReflectionWindowStruc RW = Reflector().ReflectionWindow.GetWindow();
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
		if (SurfaceUpdating != null)
			SurfaceUpdating.Start();
		//.
		btnReflectionWindowEditorUndo.setEnabled(true);
		btnReflectionWindowEditorRedo.setEnabled(false);
	}
	
	private void PictureDrawingProcess_AddPicture(Bitmap Picture, float X, float Y) throws Exception {
		PictureDrawingProcess_Begin(Picture, X,Y);
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
			try {
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
			}
		}
		finally {
			fs.close();
		}
	}
	
	//. Summary drawings 
	
	private void Drawings_Initialize() {
		Drawings = new ArrayList<TDrawing>(10);
		Drawings_HistoryIndex = 0;
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
	
	public byte[] Drawings_ToByteArray() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			byte[] BA;
			int DrawingsCount;
			if (Drawings != null) 
				DrawingsCount = Drawings.size();
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
	
	public int Drawings_FromByteArray(byte[] BA, int Idx) throws IOException {
		int DrawingsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		Drawings.clear();
		for (int I = 0; I < DrawingsCount; I++) {
			short DrawingTypeID = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(Int16)
			TDrawing Drawing = TDrawing.CreateInstance(DrawingTypeID);
			if (Drawing == null)
				throw new IOException("unknown drawing type: "+Short.toString(DrawingTypeID)); //. =>
			Idx = Drawing.FromByteArray(BA, Idx);
			Drawings.add(Drawing);
		}
		return Idx;
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
	}
	
	public boolean Drawings_Undo() throws Exception {
		if (Drawings_HistoryIndex > 0) {
			Drawings_HistoryIndex--;
			//.
			Drawings_RepaintImage();
			//.
			return (Drawings_HistoryIndex > 0); //. ->
		}
		else
			return false;
	}
	
	public void Drawings_UndoAll() throws Exception {
		if (Drawings_HistoryIndex > 0) {
			Drawings_HistoryIndex = 0;
			//.
			Drawings_RepaintImage();
		}
	}
	
	public boolean Drawings_Redo() throws Exception {
		if (Drawings_HistoryIndex < Drawings.size()) {
			Drawings_HistoryIndex++;
			//.
			Drawings_RepaintImage();
			//.
			return (Drawings_HistoryIndex < Drawings.size()); //. ->
		}
		else
			return false;
	}
	
	public void Drawings_Translate(float dX, float dY) {
		for (int I = 0; I < Drawings.size(); I++) 
			Drawings.get(I).Translate(dX,dY);
	}
	
	public void Drawings_RepaintImage() throws Exception {
		Moving_Reset();
		//. repaint bitmap from current ReflectionWindow
		if (BackgroundImage == null)
			return; //. ->
		TReflectionWindowStruc RW = Reflector().ReflectionWindow.GetWindow();
		try {
			BackgroundImage.eraseColor(Color.TRANSPARENT);
			Canvas canvas = new Canvas(BackgroundImage);
			TileImagery.ActiveCompilationSet_ReflectionWindow_DrawOnCanvasTo(RW, 0,canvas,paint,null, null, UserDrawableCompilation);
			//.
			ForegroundImage.eraseColor(Color.TRANSPARENT);
			canvas = new Canvas(ForegroundImage);
			TileImagery.ActiveCompilationSet_ReflectionWindow_DrawOnCanvasFrom(RW, 0,canvas,paint,null, null, UserDrawableCompilation);
			if (Reflector().Configuration.ReflectionWindow_flShowHints) 
				Reflector().SpaceHints.DrawOnCanvas(RW, Reflector().DynamicHintVisibleFactor, canvas);
			//.
			OriginDrawableImage.eraseColor(Color.TRANSPARENT);
			canvas = new Canvas(OriginDrawableImage);
			UserDrawableCompilation.ReflectionWindow_DrawOnCanvas(RW, 0,canvas,paint,null, false, null, null);
		}
		catch (TimeIsExpiredException E) {}
		//.
		DrawableImage.eraseColor(Color.TRANSPARENT);
		DrawableImageCanvas.drawBitmap(OriginDrawableImage,0,0,null);
		//.
		for (int I = 0; I < Drawings_HistoryIndex; I++) 
			Drawings.get(I).Paint(DrawableImageCanvas);
		//.
		SurfaceUpdating.Start();
	}
	
	public void Drawings_Clear() throws Exception {
		if (Drawings.size() == 0)
			return; //. ->
		Drawings_ClearItems();
		//.
		Drawings_RepaintImage();
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
			if (C.LevelTileContainer != null)
				C.LevelTileContainer.TileLevel.Container_PaintDrawings(C.LevelTileContainer,_Drawings, C.dX,C.dY);
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
		Containers_Translate(dX,dY);
		Drawings_Translate(dX,dY);
		//.
		Containers_StartCurrentContainer(dX,dY);
		//.
		SurfaceUpdating.Start();
	}
	
	private void Moving_Move(float X, float Y) {
		synchronized (Moving_Lock) {
			if (Moving_flProcessing) {
				Moving_X = X;
				Moving_Y = Y;
				//.
				Containers_CompleteCurrentContainer();
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
		//.l
		SharedPreferences Preferences = getPreferences(MODE_PRIVATE);
		Editor editor = Preferences.edit();
		editor.putInt("Settings_Brush_Color", Settings_Brush.getColor());
    	editor.commit();
		//.
    	LineDrawingProcess_Brush = new Paint();
		LineDrawingProcess_Brush.set(Settings_Brush);
	}
}
