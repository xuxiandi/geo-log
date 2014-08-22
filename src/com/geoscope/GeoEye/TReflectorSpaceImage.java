package com.geoscope.GeoEye;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.Classes.MultiThreading.TUpdater;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Reflections.TSpaceReflection;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;
import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;

public class TReflectorSpaceImage {

	public static final Config BitmapCfg = Config.RGB_565;
	private TReflector Reflector;
	//.
	private TSpaceReflection Reflection;
	public int DivX;
	public int DivY;
	public int 			SegmentWidth = 0;
	public int 			SegmentHeight = 0;
	public boolean 		flSegments = false;
	public Bitmap[][] 	Segments;
    public Matrix 		SegmentsTransformatrix = new Matrix();
    //.
	public boolean 	flResultBitmap = false;
	public Bitmap 	ResultBitmap = null;
    public Matrix 	ResultBitmapTransformatrix = new Matrix();
	private Canvas 	ResultBitmapCanvas = new Canvas();
	private Paint 	ResultBitmapCanvasPaint = new Paint();
	
	public TReflectorSpaceImage(TReflector pReflector, int pDivX, int pDivY) {
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
	
	public void GetSegmentsFromServer(TReflectionWindow ReflectionWindow, boolean flUpdateProxySpace, TCanceller Canceller, TUpdater Updater, TProgressor Progressor) throws Exception {
		if (Reflector.flOffline)
			return; //. ->
		URL url;
		TReflectionWindowStruc Reflection_Window;
		double Reflection_TimeStamp;
		synchronized (Reflector.ReflectionWindow) {
			url = new URL(Reflector.ReflectionWindow.PreparePNGImageURL(DivX, DivY, 1/* segments order */, flUpdateProxySpace));
			Reflection_TimeStamp = OleDate.ToUTCCurrentTime().toDouble();
			Reflection_Window = Reflector.ReflectionWindow.GetWindow();
		}
		// .
		HttpURLConnection _Connection = (HttpURLConnection)url.openConnection();
		try {
			if (Canceller.flCancel)
				return; // . ->
			_Connection.setAllowUserInteraction(false);
			_Connection.setInstanceFollowRedirects(true);
			_Connection.setRequestMethod("GET");
			_Connection.setConnectTimeout(TGeoScopeServer.Connection_ConnectTimeout);
			_Connection.setReadTimeout(TGeoScopeServer.Connection_ReadTimeout);
			_Connection.connect();
			if (Canceller.flCancel)
				return; // . ->
			int response = _Connection.getResponseCode();
			if (response != HttpURLConnection.HTTP_OK) {
				String ErrorMessage = _Connection.getResponseMessage();
				byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
				ErrorMessage = new String(ErrorMessageBA,"windows-1251");
				throw new IOException(Reflector.getString(R.string.SServerError)+ErrorMessage); //. =>
			}
			if (Canceller.flCancel)
				return; // . ->
			InputStream in = _Connection.getInputStream();
			if (in == null)
				throw new IOException(Reflector.getString(R.string.SConnectionError)); //. =>
			try {
				if (Canceller.flCancel)
					return; // . ->
				// .
				byte[] ImageDataSize = new byte[4]; // . not used
				TNetworkConnection.InputStream_ReadData(in, ImageDataSize, ImageDataSize.length, Canceller, Reflector);
				// .
				Reflector.SpaceImage.StartSegmenting();
				// .
				byte[] Data = new byte[32768]; // . max segment size
				int DataSize;
				byte SX, SY;
				int Cnt = DivX*DivY;
				for (int I = 0; I < Cnt; I++) {
					DataSize = 2+4;
					TNetworkConnection.InputStream_ReadData(in, Data, DataSize, Canceller, Reflector);
					int Idx = 0;
					SX = Data[Idx];
					Idx++;
					SY = Data[Idx];
					Idx++;
					DataSize = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx);
					Idx += 4;
					if (DataSize > Data.length)
						Data = new byte[DataSize];
					TNetworkConnection.InputStream_ReadData(in, Data, DataSize, Canceller, Reflector);
					// .
					if (Canceller.flCancel)
						return; // . ->
					// .
					Reflector.SpaceImage.AddSegment(SX, SY, Data,DataSize);
					// .
					if (Canceller.flCancel)
						return; // . ->
					// .
					if (I != (Cnt - 1))
						Updater.Update();
				}
				Reflector.SpaceImage.FinishSegmenting(Reflection_TimeStamp, Reflection_Window);
			} finally {
				in.close();
			}
		} finally {
			_Connection.disconnect();
		}
	}
	
	public synchronized void ResultBitmap_DrawFromTileImagery(TReflectionWindowStruc RW, TTileImagery TileImagery) throws CancelException, TimeIsExpiredException {
		TileImagery.ActiveCompilationSet_ReflectionWindow_DrawOnCanvas(RW, 0,ResultBitmapCanvas,ResultBitmapCanvasPaint,null, null,null);		
		//.
		ResultBitmapTransformatrix.reset();
		//.
		flResultBitmap = true;
	}	

	public synchronized void ResultBitmap_Reset() {
		ResultBitmapTransformatrix.reset();
		//.
		flResultBitmap = false;
	}	
}
