package com.geoscope.GeoEye;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Reflections.TSpaceReflection;
import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;

public class TSpaceReflectionImage {

	public static final Config BitmapCfg = Config.RGB_565;
	private TReflector Reflector;
	private TSpaceReflection Reflection;
	public int DivX;
	public int DivY;
	public int 			SegmentWidth = 0;
	public int 			SegmentHeight = 0;
	public boolean 		flSegments = false;
	public Bitmap[][] 	Segments;
    public Matrix 		SegmentsTransformatrix = new Matrix();
	public boolean 	flResultBitmap = false;
	public Bitmap 	ResultBitmap = null;
    public Matrix 	ResultBitmapTransformatrix = new Matrix();
	private Canvas 	ResultBitmapCanvas = new Canvas();
	private Paint 	ResultBitmapCanvasPaint = new Paint();
	
	public TSpaceReflectionImage(TReflector pReflector, int pDivX, int pDivY) {
		Reflector = pReflector;
		//.
		Reflection = null;
		DivX = pDivX;
		DivY = pDivY;
		//.
		Segments = new Bitmap[DivX][DivY];
		//.
		ResultBitmapCanvasPaint.setColor(Color.WHITE);
	}
	
	public void Destroy() {
		Clear();
	}
	
	public synchronized void Clear() {
		if (ResultBitmap != null) {
			ResultBitmap.recycle();
			ResultBitmap = null;
		}
		for (int X = 0; X < DivX; X++)
			for (int Y = 0; Y < DivY; Y++)
				if (Segments[X][Y] != null) {
					Segments[X][Y].recycle();
					Segments[X][Y] = null;
				}
	}
	
	public synchronized void DoOnResize(int pWidth, int pHeight) {
		if ((ResultBitmap == null) || (!((ResultBitmap.getWidth() == pWidth) && (ResultBitmap.getHeight() == pHeight)))) {
			if (ResultBitmap != null) 
				ResultBitmap.recycle();
			ResultBitmap = Bitmap.createBitmap(pWidth,pHeight,BitmapCfg);
			ResultBitmapCanvas.setBitmap(ResultBitmap);
		}
		SegmentWidth = (int)(pWidth/DivX);
		SegmentHeight = (int)(pHeight/DivY);
	}
	
	public Bitmap Bitmap_ToGrayScale(Bitmap bmpOriginal) {
        int width, height;     
        height = bmpOriginal.getHeight();     
        width = bmpOriginal.getWidth();          
        Bitmap bmpGrayscale = Bitmap.createBitmap(width,height,BitmapCfg);     
        Canvas c = new Canvas(bmpGrayscale);     
        Paint paint = new Paint();     
        ColorMatrix cm = new ColorMatrix();     
        cm.setSaturation(0);     
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);     
        paint.setColorFilter(f);     
        c.drawBitmap(bmpOriginal, 0, 0, paint);     
        return bmpGrayscale;	
    }
	
	public synchronized void GrayScale() {
		if (ResultBitmap != null) {
			Bitmap BMP = Bitmap_ToGrayScale(ResultBitmap);
			ResultBitmap.recycle();
			ResultBitmap = BMP;
			ResultBitmapCanvas.setBitmap(ResultBitmap);
		}
		for (int X = 0; X < DivX; X++)
			for (int Y = 0; Y < DivY; Y++)
				if (Segments[X][Y] != null) {
					Bitmap BMP = Bitmap_ToGrayScale(Segments[X][Y]);
					Segments[X][Y].recycle();
					Segments[X][Y] = BMP;
				}
	}
	
	public synchronized void ResetResultBitmap() {
		flResultBitmap = false; 
	}
	
	public synchronized void StartSegmenting() {
		for (int X = 0; X < DivX; X++)
			for (int Y = 0; Y < DivY; Y++)
				if (Segments[X][Y] != null) {
					Segments[X][Y].recycle();
					Segments[X][Y] = null;
				}
		SegmentsTransformatrix.reset();
		flSegments = false;
	}
	
	public synchronized void FinishSegmenting(double Reflection_TimeStamp, TReflectionWindowStruc Reflection_Window) {
		for (int X = 0; X < DivX; X++) 
			for (int Y = 0; Y < DivY; Y++) 
				if (Segments[X][Y] == null) {
					SegmentsTransformatrix.reset();
					flSegments = false;
					return; //. ->
				}
		//.
		int SX;
		Bitmap Segment;
		for (int X = 0; X < DivX; X++) {
			SX = X*SegmentWidth;
			for (int Y = 0; Y < DivY; Y++) {
				Segment = Segments[X][Y];
				if (Segment != null)
					ResultBitmapCanvas.drawBitmap(Segment, SX,Y*SegmentHeight, ResultBitmapCanvasPaint);
				else
					ResultBitmapCanvas.drawRect(SX,Y*SegmentHeight,SX+SegmentWidth,(Y+1)*SegmentHeight, ResultBitmapCanvasPaint);
			}
		}
		ResultBitmapTransformatrix.reset();
		//.
		for (int X = 0; X < DivX; X++)
			for (int Y = 0; Y < DivY; Y++)
				if (Segments[X][Y] != null) {
					Segments[X][Y].recycle();
					Segments[X][Y] = null;
				}
		SegmentsTransformatrix.reset();
		flSegments = false;
		//.
		flResultBitmap = true;
		//.
		try {
			///? Reflection = new TSpaceReflection(Reflection_TimeStamp,Reflection_Window,Bitmap.createBitmap(ResultBitmap));
			Reflection = new TSpaceReflection(Reflection_TimeStamp,Reflection_Window,Bitmap_ToGrayScale(ResultBitmap));
			Reflector.SpaceReflections.AddReflection(Reflection);
		}
		catch (Exception E) {
		}
	}

	public synchronized void AddSegment(int X, int Y, byte[] Data, int DataSize) {
		Segments[X][Y] = BitmapFactory.decodeByteArray(Data,0,DataSize,TBitmapDecodingOptions.GetBitmapFactoryOptions());
		flSegments = true;
	}	
	
	public synchronized boolean IsSegmenting() {
		return flSegments; 
	}
}
