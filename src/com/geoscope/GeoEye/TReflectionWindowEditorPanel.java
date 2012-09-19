package com.geoscope.GeoEye;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;
import com.geoscope.GeoEye.Utils.ColorPicker;
import com.geoscope.GeoEye.Utils.Graphics.TDrawing;
import com.geoscope.GeoEye.Utils.Graphics.TDrawingNode;
import com.geoscope.GeoEye.Utils.Graphics.TLineDrawing;

public class TReflectionWindowEditorPanel extends Activity implements OnTouchListener {

	public static final int MODE_NONE 		= 0;
	public static final int MODE_DRAWING 	= 1;
	public static final int MODE_MOVING 	= 2;
	public static final int MODE_SETTINGS 	= 3;
	
	public class TSurfaceHolderCallbackHandler implements SurfaceHolder.Callback {
		
		public SurfaceHolder _SurfaceHolder;
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			if (RWBitmap != null)
				RWBitmap.recycle();
			RWOriginBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			RWBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			//.
			Drawings_RepaintImage();
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
							Canvas canvas = SurfaceHolderCallbackHandler._SurfaceHolder.lockCanvas();
							try {
								canvas.drawBitmap(RWBitmap, 0,0, null);
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
            	Drawings_Commit();
            	finish();
            }
        });
		//.
		Drawings_Initialize();
		//.
		SetMode(MODE_DRAWING);
	}

	@Override
	protected void onDestroy() {
		Drawings_Finalize();
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
		return true;
	}	
	
	public int GetMode() {
		return Mode;
	}
	
	public void SetMode(int pMode) {
		if (Mode == pMode)
			return; //. ->
		Drawings_Hide();
		Settings_Hide();
		switch (pMode) {
		case MODE_DRAWING:
			Drawings_Show();
			break; //. >
			
		case MODE_SETTINGS:
			Settings_Show();
			break; //. >
		}
		Mode = pMode;
	}
	
	public boolean 			Drawing_flProcessing;
	private float 			Drawing_LastX;
	private float 			Drawing_LastY;
	private Paint			Drawing_Brush;
	private TLineDrawing	Drawing;	
	
	private void Drawing_Initialize() {
		Drawing_flProcessing = false;
		Drawing_LastX = -1;
		Drawing_Brush = new Paint();
		Drawing_Brush.setAntiAlias(true);
		Drawing_Brush.setColor(Color.RED);
		Drawing_Brush.setStrokeWidth(3F);
		Drawing_Brush.setStrokeCap(Cap.ROUND);
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
		SurfaceUpdating.Start();
		//.
		Drawing_LastX = X;
		Drawing_LastY = Y;
	}
	
	private void Drawing_End() {
		Drawings_Add(Drawing);
		//.
		SurfaceUpdating.Start();
		//.
		Drawing_LastX = -1;
		Drawing_flProcessing = false;
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
	
	public void Drawings_Commit() {
		if (Drawings_HistoryIndex == 0)
			return; //. ->
		TTileImagery TI = Reflector.SpaceTileImagery;
		if (TI != null) {
			TTileServerProviderCompilation DC = TI.ActiveCompilation_GetDrawableItem();
			if (DC != null) {
				TReflectionWindowStruc RW = Reflector.ReflectionWindow.GetWindow();
				DC.ReflectionWindow_PaintDrawings(RW, Drawings.subList(0,Drawings_HistoryIndex));
				//.
				Reflector.StartUpdatingSpaceImage();
			}
		}
	}
	
	private static final float	Settings_BrushMaxWidth = 48F;
	private TSettingsTestImage 	Settings_TestImage = null;
	private Paint				Settings_Brush = null;
	
	private void Settings_Show() {
		Settings_TestImage = (TSettingsTestImage)findViewById(R.id.ivRWESettingsTest);
		Settings_Brush = new Paint();
		Settings_Brush.set(Drawing_Brush);
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
