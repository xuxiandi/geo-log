package com.geoscope.Classes.Data.Types.Image;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;

public class TImageViewerPanel extends Activity {

	public static final int MaxBitmapSize = 2048;
	
	//. Copyright Ishii Kenzo (matabii), URL: https://github.com/matabii/scale-imageview-android
	public static class ScaleImageView extends ImageView implements android.view.View.OnTouchListener {
		
	    private Context mContext;
	    private float MAX_SCALE = 2f;

	    private Matrix mMatrix;
	    private final float[] mMatrixValues = new float[9];

	    // display width height.
	    private int mWidth;
	    private int mHeight;

	    private int mIntrinsicWidth;
	    private int mIntrinsicHeight;

	    private float mScale;
	    private float mMinScale;

	    private float mPrevDistance;
	    private boolean isScaling;

	    private int mPrevMoveX;
	    private int mPrevMoveY;
	    private GestureDetector mDetector;

	    String TAG = "ScaleImageView";

	    public ScaleImageView(Context context, AttributeSet attr) {
	        super(context, attr);
	        this.mContext = context;
	        initialize();
	    }

	    public ScaleImageView(Context context) {
	        super(context);
	        this.mContext = context;
	        initialize();
	    }

	    @Override
	    public void setImageBitmap(Bitmap bm) {
	        super.setImageBitmap(bm);
	        this.initialize();
	    }

	    @Override
	    public void setImageResource(int resId) {
	        super.setImageResource(resId);
	        this.initialize();
	    }

	    private void initialize() {
	        this.setScaleType(ScaleType.MATRIX);
	        this.mMatrix = new Matrix();
	        Drawable d = getDrawable();
	        if (d != null) {
	            mIntrinsicWidth = d.getIntrinsicWidth();
	            mIntrinsicHeight = d.getIntrinsicHeight();
	            setOnTouchListener(this);
	        }
	        mDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
	            @Override
	            public boolean onDoubleTap(MotionEvent e) {
	                maxZoomTo((int) e.getX(), (int) e.getY());
	                cutting();
	                return super.onDoubleTap(e);
	            }
	        });

	    }

	    @Override
	    protected boolean setFrame(int l, int t, int r, int b) {
	        mWidth = r - l;
	        mHeight = b - t;

	        mMatrix.reset();
	        int r_norm = r - l;
	        mScale = (float) r_norm / (float) mIntrinsicWidth;

	        int paddingHeight = 0;
	        int paddingWidth = 0;
	        // scaling vertical
	        if (mScale * mIntrinsicHeight > mHeight) {
	            mScale = (float) mHeight / (float) mIntrinsicHeight;
	            mMatrix.postScale(mScale, mScale);
	            paddingWidth = (r - mWidth) / 2;
	            paddingHeight = 0;
	            // scaling horizontal
	        } else {
	            mMatrix.postScale(mScale, mScale);
	            paddingHeight = (b - mHeight) / 2;
	            paddingWidth = 0;
	        }
	        mMatrix.postTranslate(paddingWidth, paddingHeight);

	        setImageMatrix(mMatrix);
	        mMinScale = mScale;
	        zoomTo(mScale, mWidth / 2, mHeight / 2);
	        cutting();
	        return super.setFrame(l, t, r, b);
	    }

	    protected float getValue(Matrix matrix, int whichValue) {
	        matrix.getValues(mMatrixValues);
	        return mMatrixValues[whichValue];
	    }

	    protected float getScale() {
	        return getValue(mMatrix, Matrix.MSCALE_X);
	    }

	    public float getTranslateX() {
	        return getValue(mMatrix, Matrix.MTRANS_X);
	    }

	    protected float getTranslateY() {
	        return getValue(mMatrix, Matrix.MTRANS_Y);
	    }

	    protected void maxZoomTo(int x, int y) {
	        if (mMinScale != getScale() && (getScale() - mMinScale) > 0.1f) {
	            // threshold 0.1f
	            float scale = mMinScale / getScale();
	            zoomTo(scale, x, y);
	        } else {
	            float scale = MAX_SCALE / getScale();
	            zoomTo(scale, x, y);
	        }
	    }

	    public void zoomTo(float scale, int x, int y) {
	        if (getScale() * scale < mMinScale) {
	            return;
	        }
	        if (scale >= 1 && getScale() * scale > MAX_SCALE) {
	            return;
	        }
	        mMatrix.postScale(scale, scale);
	        // move to center
	        mMatrix.postTranslate(-(mWidth * scale - mWidth) / 2, -(mHeight * scale - mHeight) / 2);

	        // move x and y distance
	        mMatrix.postTranslate(-(x - (mWidth / 2)) * scale, 0);
	        mMatrix.postTranslate(0, -(y - (mHeight / 2)) * scale);
	        setImageMatrix(mMatrix);
	    }

	    public void cutting() {
	        int width = (int) (mIntrinsicWidth * getScale());
	        int height = (int) (mIntrinsicHeight * getScale());
	        if (getTranslateX() < -(width - mWidth)) {
	            mMatrix.postTranslate(-(getTranslateX() + width - mWidth), 0);
	        }
	        if (getTranslateX() > 0) {
	            mMatrix.postTranslate(-getTranslateX(), 0);
	        }
	        if (getTranslateY() < -(height - mHeight)) {
	            mMatrix.postTranslate(0, -(getTranslateY() + height - mHeight));
	        }
	        if (getTranslateY() > 0) {
	            mMatrix.postTranslate(0, -getTranslateY());
	        }
	        if (width < mWidth) {
	            mMatrix.postTranslate((mWidth - width) / 2, 0);
	        }
	        if (height < mHeight) {
	            mMatrix.postTranslate(0, (mHeight - height) / 2);
	        }
	        setImageMatrix(mMatrix);
	    }

	    private float distance(float x0, float x1, float y0, float y1) {
	        float x = x0 - x1;
	        float y = y0 - y1;
	        return (float)Math.sqrt(x * x + y * y);
	    }

	    private float dispDistance() {
	        return (float)Math.sqrt(mWidth * mWidth + mHeight * mHeight);
	    }

		@Override
	    public boolean onTouchEvent(MotionEvent event) {
	        if (mDetector.onTouchEvent(event)) {
	            return true;
	        }
	        int touchCount = event.getPointerCount();
	        switch (event.getActionMasked()) {
	        
	        case MotionEvent.ACTION_DOWN:
	        case MotionEvent.ACTION_POINTER_DOWN:
	            if (touchCount >= 2) {
	                float distance = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
	                mPrevDistance = distance;
	                isScaling = true;
	            } else {
	                mPrevMoveX = (int) event.getX();
	                mPrevMoveY = (int) event.getY();
	            }
	            
	        case MotionEvent.ACTION_MOVE:
	            if (touchCount >= 2 && isScaling) {
	                float dist = distance(event.getX(0), event.getX(1), event.getY(0), event.getY(1));
	                float scale = (dist - mPrevDistance) / dispDistance();
	                mPrevDistance = dist;
	                scale += 1;
	                scale = scale * scale;
	                zoomTo(scale, mWidth / 2, mHeight / 2);
	                cutting();
	            } else if (!isScaling) {
	                int distanceX = mPrevMoveX - (int) event.getX();
	                int distanceY = mPrevMoveY - (int) event.getY();
	                mPrevMoveX = (int) event.getX();
	                mPrevMoveY = (int) event.getY();
	                mMatrix.postTranslate(-distanceX, -distanceY);
	                cutting();
	            }
	            break;
	            
	        case MotionEvent.ACTION_UP:
	        case MotionEvent.ACTION_POINTER_UP:
	            if (event.getPointerCount() <= 1) {
	                isScaling = false;
	            }
	            break;
	        }
	        return true;
	    }

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			return false;
		}
	}	

	private String ImageFileName;
	private Bitmap ImageBitmap = null;
	private ScaleImageView ivImage;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	ImageFileName = extras.getString("FileName");
        }
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//.
        setContentView(R.layout.image_viewer_panel);
        //.
        ivImage = (ScaleImageView)findViewById(R.id.ivImage);
        //.
        StartLoadingImage();
    }
    
    @Override
    protected void onDestroy() {
    	if (ImageBitmap != null) {
    		ImageBitmap.recycle();
    		ImageBitmap = null;
    	}
    	//.
    	super.onDestroy();
    }
    
    private void StartLoadingImage() {
		TAsyncProcessing Processing = new TAsyncProcessing(TImageViewerPanel.this,getString(R.string.SWaitAMoment)) {
			
			private Bitmap _ImageBitmap = null;
			
			@Override
			public void Process() throws Exception {
				File F = new File(ImageFileName);
				FileInputStream FS = new FileInputStream(F);
				try
				{
					BitmapFactory.Options options = new BitmapFactory.Options();
					options.inDither = false;
					options.inPurgeable = true;
					options.inInputShareable = true;
					options.inTempStorage = new byte[1024*256]; 							
					Rect rect = new Rect();
    				Bitmap bitmap = BitmapFactory.decodeFileDescriptor(FS.getFD(), rect, options);
    				try {
    					int ImageMaxSize = options.outWidth;
    					if (options.outHeight > ImageMaxSize)
    						ImageMaxSize = options.outHeight;
    					float MaxSize = MaxBitmapSize;
    					float Scale = MaxSize/ImageMaxSize; 
    					Matrix matrix = new Matrix();     
    					matrix.postScale(Scale,Scale);
    					//.
    					_ImageBitmap = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
    				}
    				finally {
    					bitmap.recycle();
    				}
				}
				finally {
					FS.close();
				}
	    		Thread.sleep(100); 
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				ImageBitmap = _ImageBitmap;
				//.
				ivImage.setImageBitmap(ImageBitmap);
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TImageViewerPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }
}