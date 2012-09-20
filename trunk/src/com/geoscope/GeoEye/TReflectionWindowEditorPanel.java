package com.geoscope.GeoEye;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TRWLevelTileContainer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoEye.Utils.ColorPicker;
import com.geoscope.GeoEye.Utils.Graphics.TDrawing;
import com.geoscope.GeoEye.Utils.Graphics.TDrawingNode;
import com.geoscope.GeoEye.Utils.Graphics.TLineDrawing;

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
			if (BackgroundBitmap != null) 
				BackgroundBitmap.recycle();
			BackgroundBitmap = Reflector.WorkSpace.BackgroundBitmap_ReCreate(width, height);
			if (RWOriginBitmap != null) 
				RWOriginBitmap.recycle();
			RWOriginBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			if (RWBitmap != null)
				RWBitmap.recycle();
			RWBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
	    	//.
	    	Drawings_RepaintImage();
			//.
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
			if (RWBitmap != null) {
				RWBitmap.recycle();
				RWBitmap = null;
			}
			if (RWOriginBitmap != null) {
				RWOriginBitmap.recycle();
				RWOriginBitmap = null;
			}
			if (BackgroundBitmap != null) { 
				BackgroundBitmap.recycle();
				BackgroundBitmap = null;
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
								canvas.drawBitmap(BackgroundBitmap, 0,0, null);
								canvas.drawBitmap(RWBitmap, dX,dY, null);
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
            if (flImageUpdating) {
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
            int Left = (int)((RWBitmap.getWidth()-W)/2);
            int Top = (int)(RWBitmap.getHeight()-H);
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
	private Timer ImageContainerWatcher = null;
	//.
	public boolean flImageUpdating = true;
	//.
	private Bitmap BackgroundBitmap = null;
	private Bitmap RWOriginBitmap = null;
	private Bitmap RWBitmap = null;
	private Canvas RWBitmapCanvas = null;
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
	private CheckBox cbReflectionWindowEditorMode;
	private Button btnReflectionWindowEditorBrushSelector;
	private Button btnReflectionWindowEditorUndo;
	private Button btnReflectionWindowEditorRedo;
	private Button btnReflectionWindowEditorClear;
	private Button btnReflectionWindowEditorCommit;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        Reflector = TReflector.MyReflector;
        //.
        setContentView(R.layout.reflectionwindow_editor_panel);
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
            	Drawings_Clear();
        		btnReflectionWindowEditorRedo.setEnabled(false);
        		btnReflectionWindowEditorUndo.setEnabled(false);
            }
        });
		//.
		btnReflectionWindowEditorCommit = (Button)findViewById(R.id.btnReflectionWindowEditorCommit);
		btnReflectionWindowEditorCommit.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                try {
                	Drawings_Commit();
                	finish();
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
		Moving_Initialize();
		//.
		SetMode(MODE_DRAWING);
	}

	@Override
	protected void onDestroy() {
		ImageContainerWatcher_Stop();
		Moving_Finalize();
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
			if (flImageUpdating)
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
			Drawings_Finalize();
			break; //. >
			
		case MODE_MOVING:
			Drawings_Hide();
			break; //. >
			
		case MODE_SETTINGS:
			Settings_Hide();
			Settings_Finalize();
			break; //. >
		}
		//.
		switch (pMode) {
		case MODE_DRAWING:
			Drawings_Initialize();
			Drawings_Show();
			break; //. >
			
		case MODE_MOVING:
			///////Moving_Initialize();
			Drawings_Show();
			break; //. >
			
		case MODE_SETTINGS:
			Settings_Initialize();
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
	
	public void ImageContainerWatcher_Start() {
		if (ImageContainerWatcher != null) 
			ImageContainerWatcher.cancel();
        ImageContainerWatcher = new Timer();
        int Interval = 333;
        ImageContainerWatcher.schedule(new TImageContainerWatcherTask(),Interval,Interval);
	}
	
	public void ImageContainerWatcher_Stop() {
		if (ImageContainerWatcher != null) {
			ImageContainerWatcher.cancel();
			ImageContainerWatcher = null;
		}
	}

    private class TImageContainerWatcherTask extends TimerTask
    {
    	public static final int MESSAGE_DONE = 1;
        public void run()
        {
        	if (!Reflector.IsUpdatingSpaceImage())        		
        		ImageContainerWatcherHandler.obtainMessage(MESSAGE_DONE).sendToTarget();
        }
    }   
	
    private final Handler ImageContainerWatcherHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case TImageContainerWatcherTask.MESSAGE_DONE:
            	ImageContainerWatcher_Stop();
            	ImageWatcher_DoOnImageContainerIsUpdated();  
            	break; //. >
            }
        }
    };
    
    private void ImageWatcher_DoOnImageContainerIsUpdated() {
    	if (flImageUpdating) {
        	flImageUpdating = false;
        	//.
        	Containers_FinishCurrentContainer();
    	}
    }

    private class TImageContainer {
    	
    	public float 					dX = 0.0F;
    	public float 					dY = 0.0F;
    	public TRWLevelTileContainer 	LevelTileContainer;
    	public boolean 					flModified = false;
    	
    	public void Translate(float pdX, float pdY) {
    		dX += pdX;
    		dY += pdY;
    	}
    }
    
    private ArrayList<TImageContainer> Containers;
    private TImageContainer Containers_CurrentContainer;
    
    public void Containers_Initialize() {
    	Containers = new ArrayList<TImageContainer>(10);
    	Containers_CurrentContainer = null;
    }

    public void Containers_Finalize() {
    	if (Containers_CurrentContainer != null)
    		Containers_CancelCurrentContainer();
    }
    
    public TImageContainer Containers_StartCurrentContainer(float dX, float dY) {
    	Containers_CompleteCurrentContainer();
    	Containers_CurrentContainer = new TImageContainer();
    	//.
    	flImageUpdating = true;
    	Reflector.TranslateReflectionWindow(dX,dY);
		//.
		ImageContainerWatcher_Start();
		//.
		return Containers_CurrentContainer;
    }
    
    public TImageContainer Containers_StartCurrentContainer() {
    	return Containers_StartCurrentContainer(0.0F,0.0F);
    }
    
    public void Containers_FinishCurrentContainer() {
    	if (Containers_CurrentContainer != null) {
    		TTileImagery TI = Reflector.SpaceTileImagery;
    		if (TI != null) {
    			TTileServerProviderCompilation DC = TI.ActiveCompilation_GetDrawableItem();
    			if (DC != null) {
    				TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
    				Containers_CurrentContainer.LevelTileContainer = DC.ReflectionWindow_GetLevelTileContainer(RW);
    			}
    		}
    		//.
        	Moving_Finalize();
        	//.
        	Drawings_RepaintImage();
    	}
    }
    
    public void Containers_CancelCurrentContainer() {
    	Containers_CurrentContainer = null;
    }
    
    public void Containers_AddCurrentContainer() {
    	if (Containers_CurrentContainer != null) {
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
    		if (Containers_CurrentContainer.flModified) 
    			Containers_AddCurrentContainer();
    		else
    			Containers_CancelCurrentContainer();
    }
    
    public void Containers_Translate(float dX, float dY) {
    	int Size = Containers.size();
    	for (int I = 0; I < Size; I++) 
    		Containers.get(I).Translate(dX,dY);
    }
    
	public boolean 			Drawing_flProcessing;
	private float 			Drawing_LastX;
	private float 			Drawing_LastY;
	private Paint			Drawing_Brush = null;
	private TLineDrawing	Drawing;	
	
	private void Drawing_Initialize() {
		Drawing_flProcessing = false;
		Drawing_LastX = -1;
		if (Drawing_Brush == null) {
			Drawing_Brush = new Paint();
			Drawing_Brush.setAntiAlias(true);
			Drawing_Brush.setColor(Color.RED);
			Drawing_Brush.setStrokeWidth(3F);
			Drawing_Brush.setStrokeCap(Cap.ROUND);
		}
		Drawing = null;
	}
	
	private void Drawing_Finalize() {
	}
	
	private void Drawing_Begin(float X, float Y) {
		Drawing_flProcessing = true;
		//.
		Drawing = new TLineDrawing(Drawing_Brush);
		//.
		RWBitmapCanvas.drawCircle(X,Y, Drawing_Brush.getStrokeWidth()*0.5F, Drawing_Brush);
		//.
		TDrawingNode DN = new TDrawingNode(X,Y);
		Drawing.Nodes.add(DN); 
		//.
		Drawing_LastX = X;
		Drawing_LastY = Y;
		//.
		SurfaceUpdating.Start();
	}
	
	private void Drawing_End() {
		Drawings_Add(Drawing);
		Containers_SetCurrentContainerAsModified();
		//.
		Drawing_LastX = -1;
		Drawing_flProcessing = false;
		//.
		SurfaceUpdating.Start();
		//.
		btnReflectionWindowEditorUndo.setEnabled(true);
		btnReflectionWindowEditorRedo.setEnabled(false);
	}
	
	private void Drawing_Draw(float X, float Y) {
		if (Drawing_flProcessing) {
			RWBitmapCanvas.drawLine(X,Y, Drawing_LastX,Drawing_LastY, Drawing_Brush);
			//.
			TDrawingNode DN = new TDrawingNode(X,Y);
			Drawing.Nodes.add(DN); 
			//.
			SurfaceUpdating.Start();
			//.
			Drawing_LastX = X;
			Drawing_LastY = Y;
		}
	}
	
	private void Drawings_Initialize() {
		Drawing_Initialize();
	}
	
	private void Drawings_Finalize() {
		Drawing_Finalize();
	}
	
	private void Drawings_Show() {
		RelativeLayout ReflectionWindowEditorSurfaceLayout = (RelativeLayout)findViewById(R.id.ReflectionWindowEditorSurfaceLayout);
		ReflectionWindowEditorSurfaceLayout.setVisibility(View.VISIBLE);
	}
	
	private void Drawings_Hide() {
		RelativeLayout ReflectionWindowEditorSurfaceLayout = (RelativeLayout)findViewById(R.id.ReflectionWindowEditorSurfaceLayout);
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
		//. repaint bitmap from current ReflectionWindow
		Canvas RWOriginBitmapCanvas = new Canvas(RWOriginBitmap);
		Reflector.WorkSpace.DrawOnCanvas(RWOriginBitmapCanvas, true,true,false,false,false,false,false);
		RWBitmapCanvas = new Canvas(RWBitmap);			
		RWBitmapCanvas.drawBitmap(RWOriginBitmap,0,0,null);
		//.
		for (int I = 0; I < Drawings_HistoryIndex; I++) { 
			if (Drawings.get(I) instanceof TLineDrawing) {
				TLineDrawing LD = (TLineDrawing)Drawings.get(I);
				TDrawingNode LastNode = LD.Nodes.get(0); 
				RWBitmapCanvas.drawCircle(LastNode.X,LastNode.Y, LD.Brush.getStrokeWidth()*0.5F, LD.Brush);
				for (int J = 1; J < LD.Nodes.size(); J++) {
    				TDrawingNode Node = LD.Nodes.get(J);
    				RWBitmapCanvas.drawLine(LastNode.X,LastNode.Y, Node.X,Node.Y, LD.Brush);
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
		if (flImageUpdating)
			throw new Exception(getString(R.string.SCannotCommitImageIsUpdating)); //. =>
		Containers_CompleteCurrentContainer();
		for (int I = 0; I < Containers.size(); I++) {
			TImageContainer C = Containers.get(I);
			C.LevelTileContainer.TileLevel.Container_PaintDrawings(C.LevelTileContainer,Drawings, C.dX,C.dY);
		}
	}
	
	private Object 			Moving_Lock = new Object();
    public boolean 			Moving_flProcessing;
	private float 			Moving_dX;
	private float 			Moving_dY;
	private float 			Moving_OrgX;
	private float 			Moving_OrgY;
	private float 			Moving_X;
	private float 			Moving_Y;
	
	private void Moving_Initialize() {
		synchronized (Moving_Lock) {
			Moving_flProcessing = false;
			//.
			Moving_OrgX = -1;
			//.
			Moving_dX = 0;
			Moving_dY = 0;
		}
	}
	
	private void Moving_Finalize() {
		Moving_flProcessing = false;
		//.
		Moving_OrgX = -1;
		//.
		Moving_dX = 0;
		Moving_dY = 0;
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
	
	private float				Settings_BrushMaxWidth;
	private TSettingsTestImage 	Settings_TestImage;
	private Paint				Settings_Brush = new Paint();
	
	private void Settings_Initialize() {
		Settings_BrushMaxWidth = 48F;
		Settings_TestImage = null;
	}

	private void Settings_Finalize() {
	}
	
	private void Settings_Show() {
		Settings_Brush.set(Drawing_Brush);
		Settings_TestImage = (TSettingsTestImage)findViewById(R.id.ivRWESettingsTest);
		Settings_TestImage.Brush = Settings_Brush;
		//.
		Button btnBrushColor = (Button)findViewById(R.id.btnRWEBrushColor);
		btnBrushColor.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		ColorPicker ColorDialog = new ColorPicker(TReflectionWindowEditorPanel.this, new ColorPicker.OnColorChangedListener() {
        			@Override
        			public void colorChanged(int color) {
        				Settings_Brush.setColor(color);
    					Settings_TestImage.postInvalidate();
        			}
        		},Color.RED);    
        		ColorDialog.show();
            }
        });
		//.
		SeekBar sbBrushWidth = (SeekBar)findViewById(R.id.sbRWEBrushSize);
		sbBrushWidth.setMax((int)Settings_BrushMaxWidth);
		sbBrushWidth.setProgress((int)Settings_Brush.getStrokeWidth());
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
		Button btnOK = (Button)findViewById(R.id.btnRWESettingsOk);
		btnOK.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Drawing_Brush = new Paint();
        		Drawing_Brush.set(Settings_Brush);
        		SetMode(MODE_DRAWING);        		
            }
        });
		//.
		Button btnCancel = (Button)findViewById(R.id.btnRWESettingsCancel);
		btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		SetMode(MODE_DRAWING);        		
            }
        });
		//.
		RelativeLayout ReflectionWindowEditorSettingsLayout = (RelativeLayout)findViewById(R.id.ReflectionWindowEditorSettingsLayout);
		ReflectionWindowEditorSettingsLayout.setVisibility(View.VISIBLE);
	}
	
	private void Settings_Hide() {
		//.
		RelativeLayout ReflectionWindowEditorSettingsLayout = (RelativeLayout)findViewById(R.id.ReflectionWindowEditorSettingsLayout);
		ReflectionWindowEditorSettingsLayout.setVisibility(View.GONE);
	}
}
