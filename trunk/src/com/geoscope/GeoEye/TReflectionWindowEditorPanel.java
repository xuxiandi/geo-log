package com.geoscope.GeoEye;

import java.io.IOException;
import java.util.ArrayList;
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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TRWLevelTileContainer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;
import com.geoscope.GeoEye.Utils.ColorPicker;
import com.geoscope.GeoEye.Utils.Graphics.TDrawing;
import com.geoscope.GeoEye.Utils.Graphics.TDrawingNode;
import com.geoscope.GeoEye.Utils.Graphics.TLineDrawing;
import com.geoscope.GeoLog.Utils.TCancelableThread;

@SuppressLint("HandlerLeak")
public class TReflectionWindowEditorPanel extends Activity implements OnTouchListener {

	public static final int MODE_NONE 		= 0;
	public static final int MODE_DRAWING 	= 1;
	public static final int MODE_MOVING 	= 2;
	public static final int MODE_SETTINGS 	= 3;
	
	public class TSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {
		
		public SurfaceHolder _SurfaceHolder;
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (Background != null) 
				Background.recycle();
			Background = Reflector.WorkSpace.BackgroundBitmap_ReCreate(width, height);
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
			Containers_StartCurrentContainer();
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
    	private Object ProcessSignal = new Object();
    	
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
							synchronized (ProcessSignal) {
								ProcessSignal.wait();
							}
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
								if (Drawing_flProcessing) 
									canvas.drawCircle(Drawing_LastX,Drawing_LastY,Drawing_Brush.getStrokeWidth()/2.0F,Drawing_MarkerPaint);
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
			synchronized (ProcessSignal) {
				ProcessSignal.notify();
			}
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
			synchronized (ProcessSignal) {
				ProcessSignal.notify();
			}
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
                ShowStatus_Paint.setTextSize(16);
                ShowStatus_Paint.setAntiAlias(true);
            }
            float W = ShowStatus_Paint.measureText(S);
            float H = ShowStatus_Paint.getTextSize();
            int Left = (int)((DrawableImage.getWidth()-W)/2);
            int Top = (int)(DrawableImage.getHeight()-H);
            ShowStatus_Paint.setColor(Color.GRAY);
            ShowStatus_Paint.setAlpha(100);
    		canvas.drawRect(Left,Top, Left+W,Top+H, ShowStatus_Paint);
            ShowStatus_Paint.setStyle(Paint.Style.FILL);
            ShowStatus_Paint.setColor(Color.BLACK);
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
            	float Spacing = getWidth()/20F; 
            	canvas.drawLine(Spacing, getHeight()/2F, getWidth()-Spacing, getHeight()/2F, Brush);
        	}
        }
    }
	
	private TReflector Reflector;
	//.
	private TTileImagery 					TileImagery = null;
	private TTileServerProviderCompilation 	UserDrawableCompilation = null;
	//.
	private Bitmap Background = null;
	private Bitmap BackgroundImage = null;
	private Bitmap ForegroundImage = null;
	private Bitmap OriginDrawableImage = null;
	private Bitmap DrawableImage = null;
	private Canvas DrawableImageCanvas = null;
	//.
	private SurfaceView Surface;
	private TSurfaceHolderCallbackHandler SurfaceHolderCallbackHandler = new TSurfaceHolderCallbackHandler();
	private TSurfaceUpdating SurfaceUpdating = null;
	//.
	private int Mode = MODE_NONE;
	//.
	private List<TDrawing> 		Drawings = new ArrayList<TDrawing>(10);
	private	int					Drawings_HistoryIndex = 0;
	//.
	private RelativeLayout 	ReflectionWindowEditorSurfaceLayout;
	private CheckBox 		cbReflectionWindowEditorMode;
	private Button 			btnReflectionWindowEditorBrushSelector;
	private Button 			btnReflectionWindowEditorUndo;
	private Button 			btnReflectionWindowEditorRedo;
	private Button 			btnReflectionWindowEditorClear;
	private Button 			btnReflectionWindowEditorCommit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Reflector = TReflector.MyReflector;
        //.
        try {
    		TileImagery = Reflector.SpaceTileImagery;
    		if (TileImagery != null) 
    			UserDrawableCompilation = TileImagery.ActiveCompilation_GetUserDrawableItem();
    		if (UserDrawableCompilation == null)
    			throw new Exception(getString(R.string.SThereIsNoVisibleUserDrawableTilesLayer)); //. =>
        } 
        catch (Exception E) {
			Toast.makeText(TReflectionWindowEditorPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();  
        	finish();
        	return; //. ->
        }        
        //.
        setContentView(R.layout.reflectionwindow_editor_panel);
        //.
		ReflectionWindowEditorSurfaceLayout = (RelativeLayout)findViewById(R.id.ReflectionWindowEditorSurfaceLayout);
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
            	boolean R = Drawings_Undo();
        		btnReflectionWindowEditorUndo.setEnabled(R);
        		btnReflectionWindowEditorRedo.setEnabled(true);

            }
        });
		//.
		btnReflectionWindowEditorRedo = (Button)findViewById(R.id.btnReflectionWindowEditorRedo);
		btnReflectionWindowEditorRedo.setEnabled(false);
		btnReflectionWindowEditorRedo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	boolean R = Drawings_Redo();
        		btnReflectionWindowEditorRedo.setEnabled(R);
        		btnReflectionWindowEditorUndo.setEnabled(true);
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
        	            	Drawings_UndoAll();
        	        		btnReflectionWindowEditorUndo.setEnabled(false);
        	        		btnReflectionWindowEditorRedo.setEnabled(true);
        		    	}
        		    })
        		    .setNegativeButton(R.string.SNo, null)
        		    .show();
        		}
            }
        });
		//.
		btnReflectionWindowEditorCommit = (Button)findViewById(R.id.btnReflectionWindowEditorCommit);
		btnReflectionWindowEditorCommit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                	new TChangesCommitting(0,true);
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
		Moving_Reset();
		//.
		SetMode(MODE_DRAWING);
	}

	@Override
	protected void onDestroy() {
		Containers_CurrentContainer_Updater_Stop();
		Drawings_Finalize();
		Settings_Finalize();
		//.
		Containers_Finalize();
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
		switch (GetMode()) {
		
		case MODE_DRAWING:
			if (Containers_CurrentContainer_flUpdating)
				return false; //. ->
			switch (pEvent.getAction() & MotionEvent.ACTION_MASK) {
			
			case MotionEvent.ACTION_DOWN:
				Drawing_Begin(pEvent.getX(),pEvent.getY());
				break;
				
			case MotionEvent.ACTION_MOVE:
				Drawing_Draw(pEvent.getX(),pEvent.getY());
				break;
				
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				Drawing_End();
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
				Moving_End();
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
	
	public void CommitChanges(int SecurityFileID) throws Exception {
		//. commit drawings into tiles locally
		Drawings_Commit();
		//. committing on the server
		TileImagery.ActiveCompilation_CommitModifiedTiles(SecurityFileID);
		//. reset view
        Reflector.ReflectionWindow.ResetActualityInterval();
		//. update view
		Reflector.StartUpdatingSpaceImage();
	}
	
    private class TChangesCommitting extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION	 			= 0;
    	private static final int MESSAGE_COMMITTED 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private int 	SecurityFileID;
    	private boolean flCloseEditor;
    	
        private ProgressDialog progressDialog; 
    	
    	public TChangesCommitting(int pSecurityFileID, boolean pflCloseEditor) {
    		SecurityFileID = pSecurityFileID;
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
    				CommitChanges(SecurityFileID);
    				//.
        			MessageHandler.obtainMessage(MESSAGE_COMMITTED).sendToTarget();
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
	            	
	            case MESSAGE_COMMITTED:
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
	
    private class TImageContainer {
    	
    	public float 					dX = 0.0F;
    	public float 					dY = 0.0F;
    	public TRWLevelTileContainer 	LevelTileContainer = null;
    	public boolean 					flModified = false;
    	
    	public void Translate(float pdX, float pdY) {
    		dX += pdX;
    		dY += pdY;
    	}
    }
    
    private ArrayList<TImageContainer> 	Containers;
    private TImageContainer 			Containers_CurrentContainer;
	public boolean 						Containers_CurrentContainer_flUpdating = true;
    
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
	private static final int 	Containers_CurrentContainer_Updater_ImageUpdateIntervalCount = 50; //. *Containers_CurrentContainerUpdater_Interval 
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
        	if (Reflector.IsUpdatingSpaceImage()) {
        		if ((Containers_CurrentContainer_Updater_ImageUpdateIntervalCounter % Containers_CurrentContainer_Updater_ImageUpdateIntervalCount) == 0)
            		Containers_CurrentContainer_Updater_Handler.obtainMessage(MESSAGE_UPDATE).sendToTarget();
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
            	Containers_CurrentContainer_Updater_DoOnUpdating();  
            	break; //. >

            case TContainersCurrentContainerUpdaterTask.MESSAGE_DONE:
            	Containers_CurrentContainer_Updater_DoOnUpdated();  
            	break; //. >
            }
        }
    };
    
    private void Containers_CurrentContainer_Updater_DoOnUpdating() {
    	if (!Containers_CurrentContainer_Updater_flProcessing)
    		return; //. ->
    	Containers_CurrentContainerUpdating();    
    }

    private void Containers_CurrentContainer_Updater_DoOnUpdated() {
    	if (!Containers_CurrentContainer_Updater_flProcessing)
    		return; //. ->
    	if (Containers_CurrentContainer_flUpdating) {
        	Containers_CurrentContainer_flUpdating = false;
        	//.
        	Containers_FinishCurrentContainer();
    	}
    }

    public TImageContainer Containers_StartCurrentContainer(float dX, float dY) {
    	Containers_CompleteCurrentContainer();
    	Containers_CurrentContainer = new TImageContainer();
    	//.
    	Containers_CurrentContainer_flUpdating = true;
    	Reflector.TranslateReflectionWindow(dX,dY);
    	//.
		Containers_CurrentContainer_Updater_Start();
		//.
		return Containers_CurrentContainer;
    }
    
    public TImageContainer Containers_StartCurrentContainer() {
    	return Containers_StartCurrentContainer(0.0F,0.0F);
    }
    
    public void Containers_FinishCurrentContainer() {
    	if (Containers_CurrentContainer != null) {
        	Containers_CurrentContainer_Updater_Stop();
        	//.
			TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
			Containers_CurrentContainer.LevelTileContainer = UserDrawableCompilation.ReflectionWindow_GetLevelTileContainer(RW);
			//.
        	Drawings_RepaintImage();
    	}
    }

    public void Containers_CurrentContainerUpdating() {
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
    
	public boolean 			Drawing_flProcessing;
	private float 			Drawing_LastX;
	private float 			Drawing_LastY;
	private Paint			Drawing_Brush = null;
	private Paint			Drawing_MarkerPaint;
	private TLineDrawing	Drawing;	
	
	private void Drawing_Initialize() {
		Drawing_flProcessing = false;
		Drawing_LastX = -1;
		Drawing_Brush = new Paint();
		Drawing_Brush.set(Settings_Brush);
		if (Drawing_MarkerPaint == null)  
			Drawing_MarkerPaint = new Paint();
		Drawing = null;
	}
	
	private void Drawing_Finalize() {
	}
	
	private void Drawing_Begin(float X, float Y) {
		Drawing = new TLineDrawing(Drawing_Brush);
		//.
		DrawableImageCanvas.drawCircle(X,Y, Drawing_Brush.getStrokeWidth()*0.5F, Drawing_Brush);
		//.
		TDrawingNode DN = new TDrawingNode(X,Y);
		Drawing.Nodes.add(DN); 
		//.
		Drawing_LastX = X;
		Drawing_LastY = Y;
		//.
		Drawing_MarkerPaint.setColor(Drawing_Brush.getColor());
		int CC = Drawing_Brush.getColor();
		if (CC != Color.TRANSPARENT)
			Drawing_MarkerPaint.setColor(CC);
		else
			CC = Color.RED;
		Drawing_MarkerPaint.setAlpha(127);
		//.
		Drawing_flProcessing = true;
		//.
		SurfaceUpdating.Start();
	}
	
	private void Drawing_End() {
		Drawing_flProcessing = false;
		//.
		Drawings_Add(Drawing);
		Containers_SetCurrentContainerAsModified();
		//.
		Drawing_LastX = -1;
		//.
		SurfaceUpdating.Start();
		//.
		btnReflectionWindowEditorUndo.setEnabled(true);
		btnReflectionWindowEditorRedo.setEnabled(false);
	}
	
	private void Drawing_Draw(float X, float Y) {
		if (Drawing_flProcessing) {
			DrawableImageCanvas.drawLine(X,Y, Drawing_LastX,Drawing_LastY, Drawing_Brush);
			//.
			TDrawingNode DN = new TDrawingNode(X,Y);
			Drawing.Nodes.add(DN); 
			//.
			Drawing_LastX = X;
			Drawing_LastY = Y;
			//.
			SurfaceUpdating.Start();
		}
	}
	
	private void Drawings_Initialize() {
		Drawing_Initialize();
	}
	
	private void Drawings_Finalize() {
		Drawing_Finalize();
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
				Drawings = L;
			}
			else 
				Drawings.clear();
		}
		Drawings.add(Drawing); 
		Drawings_HistoryIndex++;
	}
	
	public boolean Drawings_Undo() {
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
	
	public void Drawings_UndoAll() {
		if (Drawings_HistoryIndex > 0) {
			Drawings_HistoryIndex = 0;
			//.
			Drawings_RepaintImage();
		}
	}
	
	public boolean Drawings_Redo() {
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
		for (int I = 0; I < Drawings.size(); I++) {
			if (Drawings.get(I) instanceof TLineDrawing) {
				TLineDrawing LD = (TLineDrawing)Drawings.get(I);
				for (int J = 0; J < LD.Nodes.size(); J++) {
    				TDrawingNode Node = LD.Nodes.get(J);
    				Node.X += dX;
    				Node.Y += dY;
				}
			}
		}
	}
	
	public void Drawings_RepaintImage() {
		Moving_Reset();
		//. repaint bitmap from current ReflectionWindow
		TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
		try {
			BackgroundImage.eraseColor(Color.TRANSPARENT);
			Canvas canvas = new Canvas(BackgroundImage);
			TileImagery.ActiveCompilation_ReflectionWindow_DrawOnCanvasTo(RW, canvas, null, UserDrawableCompilation);
			ForegroundImage.eraseColor(Color.TRANSPARENT);
			canvas = new Canvas(ForegroundImage);
			TileImagery.ActiveCompilation_ReflectionWindow_DrawOnCanvasFrom(RW, canvas, null, UserDrawableCompilation);
			OriginDrawableImage.eraseColor(Color.TRANSPARENT);
			canvas = new Canvas(OriginDrawableImage);
			UserDrawableCompilation.ReflectionWindow_DrawOnCanvas(RW, canvas, false, null, null);
		}
		catch (TimeIsExpiredException E) {}
		//.
		DrawableImage.eraseColor(Color.TRANSPARENT);
		DrawableImageCanvas.drawBitmap(OriginDrawableImage,0,0,null);
		//.
		for (int I = 0; I < Drawings_HistoryIndex; I++) { 
			if (Drawings.get(I) instanceof TLineDrawing) {
				TLineDrawing LD = (TLineDrawing)Drawings.get(I);
				TDrawingNode LastNode = LD.Nodes.get(0); 
				DrawableImageCanvas.drawCircle(LastNode.X,LastNode.Y, LD.Brush.getStrokeWidth()*0.5F, LD.Brush);
				for (int J = 1; J < LD.Nodes.size(); J++) {
    				TDrawingNode Node = LD.Nodes.get(J);
    				DrawableImageCanvas.drawLine(LastNode.X,LastNode.Y, Node.X,Node.Y, LD.Brush);
    				LastNode = Node;
				}
			}
		}
		//.
		SurfaceUpdating.Start();
	}
	
	public void Drawings_Clear() {
		if (Drawings_HistoryIndex == 0)
			return; //. ->
		Drawings.clear();
		Drawings_HistoryIndex = 0;
		//.
		Drawings_RepaintImage();
	}
	
	public void Drawings_Commit() throws Exception {
		if (Drawings_HistoryIndex == 0)
			return; //. ->
		if (Containers_CurrentContainer_flUpdating)
			throw new Exception(getString(R.string.SCannotCommitImageIsUpdating)); //. =>
		Containers_CompleteCurrentContainer();
		for (int I = 0; I < Containers.size(); I++) {
			TImageContainer C = Containers.get(I);
			if (C.LevelTileContainer != null)
				C.LevelTileContainer.TileLevel.Container_PaintDrawings(C.LevelTileContainer,Drawings, C.dX,C.dY);
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
	
	private void Moving_End() {
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
		Settings_BrushMaxWidth = 96F;
		Settings_Brush.setAntiAlias(true);
		Settings_Brush.setStrokeCap(Cap.ROUND);
		SharedPreferences Preferences = getPreferences(MODE_PRIVATE);
		Settings_Brush.setColor(Preferences.getInt("Settings_Brush_Color", Color.RED));
		Settings_Brush.setStrokeWidth(Preferences.getFloat("Settings_Brush_Width", 3.0F));
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
		Settings_Brush.set(Drawing_Brush);
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
    	Drawing_Brush = new Paint();
		Drawing_Brush.set(Settings_Brush);
	}
}
