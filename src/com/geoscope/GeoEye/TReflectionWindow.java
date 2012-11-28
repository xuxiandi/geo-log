package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.graphics.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TContainerCoord;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TSpaceObj;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.TReflectionWindowConfigurationPanel;

@SuppressLint("HandlerLeak")
public class TReflectionWindow {

	private TReflector Reflector;
	public double X0;
	public double Y0;
	public double X1;
	public double Y1;
	public double X2;
	public double Y2;
	public double X3;
	public double Y3;
	public double Xcenter;
	public double Ycenter;
	public double HorRange;
	public double VertRange;

	public int Xmn;
	public int Ymn;
	public int Xmx;
	public int Ymx;
	public int Xmd;
	public int Ymd;

	public TContainerCoord ContainerCoord;
	
	public TReflectionWindowActualityInterval ActualityInterval;
	//.
	private TSpaceLays Lays;
	//.
	public TTileServerVisualizationUserData TileServerVisualizationUserData;

	public TReflectionWindow(TReflector pReflector, TReflectionWindowStruc pReflectionWindowStruc) throws Exception
	{
		Reflector = pReflector;
		//.
		X0 = pReflectionWindowStruc.X0;
		Y0 = pReflectionWindowStruc.Y0;
		X1 = pReflectionWindowStruc.X1;
		Y1 = pReflectionWindowStruc.Y1;
		X2 = pReflectionWindowStruc.X2;
		Y2 = pReflectionWindowStruc.Y2;
		X3 = pReflectionWindowStruc.X3;
		Y3 = pReflectionWindowStruc.Y3;
		Xmn = pReflectionWindowStruc.Xmn;
		Ymn = pReflectionWindowStruc.Ymn;
		Xmx = pReflectionWindowStruc.Xmx;
		Ymx = pReflectionWindowStruc.Ymx;
		//.
		ActualityInterval = new TReflectionWindowActualityInterval(pReflectionWindowStruc.BeginTimestamp,pReflectionWindowStruc.EndTimestamp);
		//.
		Lays = null;
		TileServerVisualizationUserData = new TTileServerVisualizationUserData();
		//.
		Update();
	}

	public synchronized void Update()
	{
		Xcenter = (X0+X2)/2;
		Ycenter = (Y0+Y2)/2;
		Xmd = (int)((double)((Xmx+Xmn)/2));
		Ymd = (int)((double)((Ymx+Ymn)/2));
		HorRange = Math.sqrt(Math.pow((X1-X0),2)+Math.pow((Y1-Y0),2));
		VertRange = Math.sqrt(Math.pow((X3-X0),2)+Math.pow((Y3-Y0),2));
		ContainerCoord = GetMaxMin();
	}

	public synchronized void Assign(TReflectionWindow Srs)
	{
		X0 = Srs.X0; Y0 = Srs.Y0;
		X1 = Srs.X1; Y1 = Srs.Y1;
		X2 = Srs.X2; Y2 = Srs.Y2;
		X3 = Srs.X3; Y3 = Srs.Y3;
		Xcenter = Srs.Xcenter; Ycenter = Srs.Ycenter;
		Xmn = Srs.Xmn; Ymn = Srs.Ymn;
		Xmx = Srs.Xmx; Ymx = Srs.Ymx;
		Xmd = Srs.Xmd; Ymd = Srs.Ymd;
		HorRange = Srs.HorRange;
		VertRange = Srs.VertRange;
		ContainerCoord = Srs.ContainerCoord;
	}
	
	public TSpaceLays CheckSpaceLays() throws IOException {
		boolean flCreate;
		synchronized (this) {
			flCreate = (Lays == null);                       
		}
		if (flCreate) {
			TSpaceLays _Lays = new TSpaceLays(Reflector);
			synchronized (this) {
				Lays = _Lays;
			}
		}
		synchronized (this) {
			return Lays;
		}
	}
	
	public synchronized TSpaceLays getLays() {
		return Lays;
	}

	public synchronized double Scale()
	{
		return ((Xmx-Xmn)/(Math.sqrt(Math.pow((X1-X0),2)+Math.pow((Y1-Y0),2))));
	}

	public synchronized TReflectionWindowStruc GetWindow()
	{
		TReflectionWindowStruc vReflectionWindowStruc = new TReflectionWindowStruc();
		vReflectionWindowStruc.X0 = X0; vReflectionWindowStruc.Y0 = Y0;
		vReflectionWindowStruc.X1 = X1; vReflectionWindowStruc.Y1 = Y1;
		vReflectionWindowStruc.X2 = X2; vReflectionWindowStruc.Y2 = Y2;
		vReflectionWindowStruc.X3 = X3; vReflectionWindowStruc.Y3 = Y3;
		//.
		vReflectionWindowStruc.Xmn = Xmn; vReflectionWindowStruc.Ymn = Ymn;
		vReflectionWindowStruc.Xmx = Xmx; vReflectionWindowStruc.Ymx = Ymx;
		//.
		vReflectionWindowStruc.BeginTimestamp = ActualityInterval.BeginTimestamp;
		vReflectionWindowStruc.EndTimestamp = ActualityInterval.EndTimestamp;
		//.
		vReflectionWindowStruc.UpdateContainer();
		//.
		return vReflectionWindowStruc;
	}

	public synchronized TContainerCoord GetMaxMin()
	{
		TContainerCoord ContainerCoord = new TContainerCoord();
		ContainerCoord.Xmin = X0;
		ContainerCoord.Ymin = Y0;
		ContainerCoord.Xmax = X0;
		ContainerCoord.Ymax = Y0;
		if (X1 < ContainerCoord.Xmin) ContainerCoord.Xmin = X1; else if (X1 > ContainerCoord.Xmax) ContainerCoord.Xmax = X1;
		if (Y1 < ContainerCoord.Ymin) ContainerCoord.Ymin = Y1; else if (Y1 > ContainerCoord.Ymax) ContainerCoord.Ymax = Y1;
		if (X2 < ContainerCoord.Xmin) ContainerCoord.Xmin = X2; else if (X2 > ContainerCoord.Xmax) ContainerCoord.Xmax = X2;
		if (Y2 < ContainerCoord.Ymin) ContainerCoord.Ymin = Y2; else if (Y2 > ContainerCoord.Ymax) ContainerCoord.Ymax = Y2;
		if (X3 < ContainerCoord.Xmin) ContainerCoord.Xmin = X3; else if (X3 > ContainerCoord.Xmax) ContainerCoord.Xmax = X3;
		if (Y3 < ContainerCoord.Ymin) ContainerCoord.Ymin = Y3; else if (Y3 > ContainerCoord.Ymax) ContainerCoord.Ymax = Y3;
		//. 
		return ContainerCoord;
	}

	public synchronized int getWidth() {
		return (Xmx-Xmn);
	}
	
	public synchronized int getHeight() {
		return (Ymx-Ymn);
	}
	
	public synchronized TXYCoord ConvertToReal(double SX, double SY)
	{
		TXYCoord XYCoord = new TXYCoord();
		double VS = -(SY-Ymn)/(Ymx-Ymn);
		double HS = -(SX-Xmn)/(Xmx-Xmn);
		double diffX0X3 = (X0-X3);
		double diffY0Y3 = (Y0-Y3);
		double diffX0X1 = (X0-X1);
		double diffY0Y1 = (Y0-Y1);
		double ofsX = (diffX0X1)*HS+(diffX0X3)*VS;
		double ofsY = (diffY0Y1)*HS+(diffY0Y3)*VS;
		XYCoord.X = (X0+ofsX);
		XYCoord.Y = (Y0+ofsY);
		return XYCoord;
	}    
	
	public synchronized void Normalize()
	{
		double diffX1X0;
		double diffY1Y0;
		double b;
		double V;
		double S0_X3;
		double S0_Y3;
		double S1_X3;
		double S1_Y3;
		double S0_X2;
		double S0_Y2;
		double S1_X2;
		double S1_Y2;

		diffX1X0 = X1-X0;
		diffY1Y0 = Y1-Y0;
		b = Math.sqrt(Math.pow(diffX1X0,2)+Math.pow(diffY1Y0,2))*(Ymx-Ymn)/(Xmx-Xmn);
		if (Math.abs(diffY1Y0) > Math.abs(diffX1X0))
		{
			V = b/Math.sqrt(1+Math.pow((diffX1X0/diffY1Y0),2));
			S0_X3 = (V)+X0;
			S0_Y3 = (-V)*(diffX1X0/diffY1Y0)+Y0;
			S1_X3 = (-V)+X0;
			S1_Y3 = (V)*(diffX1X0/diffY1Y0)+Y0;

			S0_X2 = (V)+X1;
			S0_Y2 = (-V)*(diffX1X0/diffY1Y0)+Y1;
			S1_X2 = (-V)+X1;
			S1_Y2 = (V)*(diffX1X0/diffY1Y0)+Y1;
		}
		else 
		{
			V = b/Math.sqrt(1+Math.pow((diffY1Y0/diffX1X0),2));
			S0_Y3 = (V)+Y0;
			S0_X3 = (-V)*(diffY1Y0/diffX1X0)+X0;
			S1_Y3 = (-V)+Y0;
			S1_X3 = (V)*(diffY1Y0/diffX1X0)+X0;

			S0_Y2 = (V)+Y1;
			S0_X2 = (-V)*(diffY1Y0/diffX1X0)+X1;
			S1_Y2 = (-V)+Y1;
			S1_X2 = (V)*(diffY1Y0/diffX1X0)+X1;
		};
		if (Math.sqrt(Math.pow((X3-S0_X3),2)+Math.pow((Y3-S0_Y3),2)) < Math.sqrt(Math.pow((X3-S1_X3),2)+Math.pow((Y3-S1_Y3),2)))
		{
			X3 = S0_X3;
			Y3 = S0_Y3;
		}
		else
		{
			X3 = S1_X3;
			Y3 = S1_Y3;
		};
		if (Math.sqrt(Math.pow((X2-S0_X2),2)+Math.pow((Y2-S0_Y2),2)) < Math.sqrt(Math.pow((X2-S1_X2),2)+Math.pow((Y2-S1_Y2),2)))
		{
			X2 = S0_X2;
			Y2 = S0_Y2;
		}
		else 
		{
			X2 = S1_X2;
			Y2 = S1_Y2;
		};
		Update();
	}
	
	public synchronized void Resize(int NewWidth, int NewHeight) {
		TXYCoord C = ConvertToReal(NewWidth, Ymn);
		X1 = C.X; Y1 = C.Y;
		Xmx = NewWidth; Ymx = NewHeight;
		Normalize();
	}
	
	public synchronized TXYCoord ConvertToScreen(double X, double Y) {
	    double QdA2;
	    double X_C,X_QdC,X_A1,X_QdB2;
	    double Y_C,Y_QdC,Y_A1,Y_QdB2;
	    //.
	    QdA2 = Math.pow(X-X0,2)+Math.pow(Y-Y0,2);
	    //.
	    X_QdC = Math.pow(X1-X0,2)+Math.pow(Y1-Y0,2);
	    X_C = Math.sqrt(X_QdC);
	    X_QdB2 = Math.pow(X-X1,2)+Math.pow(Y-Y1,2);
	    X_A1 = (X_QdC-X_QdB2+QdA2)/(2*X_C);
	    //.
	    Y_QdC = Math.pow(X3-X0,2)+Math.pow(Y3-Y0,2);
	    Y_C = Math.sqrt(Y_QdC);
	    Y_QdB2 = Math.pow(X-X3,2)+Math.pow(Y-Y3,2);
	    Y_A1 = (Y_QdC-Y_QdB2+QdA2)/(2*Y_C);
	    //.
		TXYCoord C = new TXYCoord();
	    C.X = Xmn+X_A1/X_C*(Xmx-Xmn);
	    C.Y = Ymn+Y_A1/Y_C*(Ymx-Ymn);
	    //.
	    return C;
	}
	
	public synchronized boolean IsNodeVisible(double X, double Y) {
		TXYCoord C = ConvertToScreen(X,Y);
		return (((Xmn <= C.X) && (C.X <= Xmx)) && ((Ymn <= C.Y) && (C.Y <= Ymx)));
	}
	
	public TXYCoord[] ConvertNodesToScreen(TXYCoord[] Nodes) {
		TXYCoord[] ScrNodes = new TXYCoord[Nodes.length];
		for (int I = 0; I < ScrNodes.length; I++) 
			ScrNodes[I] = ConvertToScreen(Nodes[I].X,Nodes[I].Y);
		return ScrNodes;
	}
	
	public String PrepareJPEGImageURL() throws IOException {

		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "SpaceWindow.jpg";
		//. add command parameters
		CheckSpaceLays();
		synchronized (this) {
			URL2 = URL2+"?"+"1"/*command version*/+","+Double.toString(X0)+","+Double.toString(Y0)+","+Double.toString(X1)+","+Double.toString(Y1)+","+Double.toString(X2)+","+Double.toString(Y2)+","+Double.toString(X3)+","+Double.toString(Y3)+",";
			short[] InvisibleLays = Lays.GetDisabledLaysIndexes();
			short InvisibleLaysCount;
			if (InvisibleLays != null)
				InvisibleLaysCount = (short)InvisibleLays.length;
			else 
				InvisibleLaysCount = 0;
			URL2 = URL2+Integer.toString(InvisibleLaysCount)+",";
			for (int I = 0; I < InvisibleLaysCount; I++) 
				URL2 = URL2+Integer.toString(InvisibleLays[I])+',';
			URL2 = URL2+Integer.toString(Reflector.VisibleFactor)+",";
			URL2 = URL2+Double.toString(Reflector.DynamicHintVisibility)+",";
			URL2 = URL2+Integer.toString(Xmn)+","+Integer.toString(Ymn)+","+Integer.toString(Xmx)+","+Integer.toString(Ymx);
		}
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".jpg";
		return URL;
	}

	public String PreparePNGImageURL(int DivX, int DivY, int SegmentsOrder, boolean flUpdateProxySpace) throws IOException {

		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "SpaceWindow.png";
		//. add command parameters
		CheckSpaceLays();
		synchronized (this) {
			URL2 = URL2+"?"+"4"/*command version*/+","+Double.toString(X0)+","+Double.toString(Y0)+","+Double.toString(X1)+","+Double.toString(Y1)+","+Double.toString(X2)+","+Double.toString(Y2)+","+Double.toString(X3)+","+Double.toString(Y3)+",";
			short[] InvisibleLays = Lays.GetDisabledLaysIndexes();
			short InvisibleLaysCount;
			if (InvisibleLays != null)
				InvisibleLaysCount = (short)InvisibleLays.length;
			else 
				InvisibleLaysCount = 0;
			URL2 = URL2+Integer.toString(InvisibleLaysCount)+",";
			for (int I = 0; I < InvisibleLaysCount; I++) 
				URL2 = URL2+Integer.toString(InvisibleLays[I])+',';
			URL2 = URL2+Integer.toString(Reflector.VisibleFactor)+",";
			URL2 = URL2+"1"/*Dynamic hint data version*/+",";
			//. Visualization UserData
			int TSVUserDataSize = 0; 
			byte[] TSVUserData = TileServerVisualizationUserData.ToByteArrayV1();
			if (TSVUserData != null)
				TSVUserDataSize = TSVUserData.length;
			int Idx = 0;
			byte[] UserData = new byte[4/*SizeOf(TSVUserDataSize)*/+TSVUserDataSize];
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(TSVUserDataSize);
			System.arraycopy(BA,0, UserData,Idx, BA.length); Idx += BA.length;
			if (TSVUserDataSize > 0) {
			  System.arraycopy(TSVUserData,0, UserData,Idx, TSVUserData.length); 
			  Idx += TSVUserData.length;
			}
			String UserDataString;
			ByteArrayOutputStream BOS = new ByteArrayOutputStream();
			try {
				Base64OutputStream B64S = new Base64OutputStream(BOS,Base64.URL_SAFE);
				try {
					B64S.write(UserData);
				}
				finally {
					B64S.close();
				}
				UserDataString = new String(BOS.toByteArray());
			}
			finally {
				BOS.close();
			}
			URL2 = URL2+UserDataString+",";
			//.
			URL2 = URL2+Double.toString(ActualityInterval.GetBeginTimestamp())+","+Double.toString(ActualityInterval.EndTimestamp)+",";
			//.
			URL2 = URL2+Integer.toString(Xmn)+","+Integer.toString(Ymn)+","+Integer.toString(Xmx)+","+Integer.toString(Ymx)+",";
			URL2 = URL2+Integer.toString(DivX)+","+Integer.toString(DivY)+","+Integer.toString(SegmentsOrder);
			if (flUpdateProxySpace)
				URL2 = URL2+","+"1"/*flUpdateProxySpace = true*/;
		}
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".png";
		return URL;
	}

	public String PrepareURLForObjectAtPosition(double X, double Y, boolean flRootObj) throws IOException {

		String URL1 = Reflector.ServerAddress;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "SpaceWindowObjectAtPosition.dat";
		//. add command parameters
		CheckSpaceLays();
		synchronized (this) {
			URL2 = URL2+"?"+"2"/*command version*/+","+Double.toString(X0)+","+Double.toString(Y0)+","+Double.toString(X1)+","+Double.toString(Y1)+","+Double.toString(X2)+","+Double.toString(Y2)+","+Double.toString(X3)+","+Double.toString(Y3)+",";
			short[] InvisibleLays = Lays.GetDisabledLaysIndexes();
			short InvisibleLaysCount;
			if (InvisibleLays != null)
				InvisibleLaysCount = (short)InvisibleLays.length;
			else 
				InvisibleLaysCount = 0;
			URL2 = URL2+Integer.toString(InvisibleLaysCount)+",";
			for (int I = 0; I < InvisibleLaysCount; I++) 
				URL2 = URL2+Integer.toString(InvisibleLays[I])+',';
			URL2 = URL2+Integer.toString(Reflector.VisibleFactor)+",";
			URL2 = URL2+"0"/*DynamicHintVisibility*/+",";
			URL2 = URL2+Double.toString(ActualityInterval.GetBeginTimestamp())+","+Double.toString(ActualityInterval.EndTimestamp)+",";
			URL2 = URL2+Integer.toString(Xmn)+","+Integer.toString(Ymn)+","+Integer.toString(Xmx)+","+Integer.toString(Ymx)+",";
			URL2 = URL2+Double.toString(X)+","+Double.toString(Y)+",";
			int RootObjFlag;
			if (flRootObj)
				RootObjFlag = 1;
			else
				RootObjFlag = 0;
			URL2 = URL2+Integer.toString(RootObjFlag);
		}
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Reflector.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;
	}

	public TSpaceObj GetObjectAtPosition(double X, double Y, boolean flRootObj) throws Exception,IOException {
    	String CommandURL = PrepareURLForObjectAtPosition(X,Y,flRootObj);
    	//.
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(CommandURL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[HttpConnection.getContentLength()];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length)
	            {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(""); //. =>
	                SummarySize += Size;
	            }
	            //.
	            int Idx = 0;
	            int ObjectPtr = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx+=4;
	            if (ObjectPtr == SpaceDefines.nilPtr) 
		            return null; //. ->
	            TSpaceObj Obj = new TSpaceObj(ObjectPtr);
	            Obj.SetObjBodyFromByteArray(Data,Idx); Idx += TSpaceObj.Size;
	            TXYCoord[] ObjNodes = null;
	            int NodesCounter = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx+=4;
	            if (NodesCounter > 0) {
	            	ObjNodes = new TXYCoord[NodesCounter];
	            	for (int I = 0; I < ObjNodes.length; I++) {
	            		ObjNodes[I] = new TXYCoord();
	            		ObjNodes[I].X = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
	            		ObjNodes[I].Y = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
	            	}
	            }
            	Obj.Nodes = ObjNodes;
	            return Obj; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
    public class TObjectAtPositionGetting implements Runnable {

    	private static final int MESSAGE_SHOWEXCEPTION = 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;
    	
    	private TReflectionWindow ReflectionWindow;
    	//. params
    	private double X;
    	private double Y;
    	private boolean flRootObj;
    	private int OnCompletionMessage;
    	//.
    	public TSpaceObj SpaceObject;
    	//.
    	private Thread _Thread;
    	private boolean flCancel = false;
    	//.
        int SummarySize = 0;
        private ProgressDialog progressDialog; 
    	
    	public TObjectAtPositionGetting(TReflectionWindow pReflectionWindow, double pX, double pY, boolean pflRootObj, int pOnCompletionMessage) {
    		ReflectionWindow = pReflectionWindow;
    		//. 
    		X = pX;
    		Y = pY;
    		flRootObj = pflRootObj;
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
		    	String CommandURL = PrepareURLForObjectAtPosition(X,Y,flRootObj);
		    	//.
				if (flCancel)
					return; //. ->
		    	//.
    			//. MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				HttpURLConnection HttpConnection = ReflectionWindow.Reflector.OpenHttpConnection(CommandURL);
    				try {
    					if (flCancel)
    						return; //. ->
    					InputStream in = HttpConnection.getInputStream();
    					try {
    		    			if (flCancel)
    		    				return; //. ->
    		                //.
    						int RetSize = HttpConnection.getContentLength();
    						if (RetSize == 0) {
    							SpaceObject = null;
    							return; //. ->
    						}
    						byte[] Data = new byte[RetSize];
    			            int Size;
    			            int SummarySize = 0;
    			            int ReadSize;
    			            while (SummarySize < Data.length)
    			            {
    			                ReadSize = Data.length-SummarySize;
    			                Size = in.read(Data,SummarySize,ReadSize);
    			                if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
    			                SummarySize += Size;
    			                //.
    			    			if (flCancel)
    			    				return; //. ->
    			    			//.
    			    			//. MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,(Integer)(100*SummarySize/Data.length)).sendToTarget();
    			            }
    			            //.
    			            int Idx = 0;
    			            int ObjectPtr = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx+=4;
    			            if (ObjectPtr == SpaceDefines.nilPtr) {
    			            	SpaceObject = null;
    				            return; //. ->
    			            }
    			            TSpaceObj Obj = new TSpaceObj(ObjectPtr);
    			            Obj.SetObjBodyFromByteArray(Data,Idx); Idx += TSpaceObj.Size;
    			            TXYCoord[] ObjNodes = null;
    			            int NodesCounter = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx+=4;
    			            if (NodesCounter > 0) {
    			            	ObjNodes = new TXYCoord[NodesCounter];
    			            	for (int I = 0; I < ObjNodes.length; I++) {
    			            		ObjNodes[I] = new TXYCoord();
    			            		ObjNodes[I].X = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
    			            		ObjNodes[I].Y = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
    			            	}
    			            }
    		            	Obj.Nodes = ObjNodes;
    			    		//.
    		            	SpaceObject = Obj;
    		            	ReflectionWindow.Reflector.MessageHandler.obtainMessage(OnCompletionMessage,SpaceObject).sendToTarget();
    					}
    					finally {
    						in.close();
    					}                
    				}
    				finally {
    					HttpConnection.disconnect();
    				}
				}
				finally {
	    			//. MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
        	}
        	catch (InterruptedException E) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	            switch (msg.what) {
	            
	            case MESSAGE_SHOWEXCEPTION:
	            	Exception E = (Exception)msg.obj;
	                Toast.makeText(ReflectionWindow.Reflector, ReflectionWindow.Reflector.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
	            	//.
	            	break; //. >
	            	
	            case MESSAGE_PROGRESSBAR_SHOW:
	            	progressDialog = new ProgressDialog(Reflector);    
	            	progressDialog.setMessage(Reflector.getString(R.string.SLoading));    
	            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
	            	progressDialog.setIndeterminate(false); 
	            	progressDialog.setCancelable(true);
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
	    
    	public void Cancel() {
    		flCancel = true;
    		//.
    		_Thread.interrupt();
    	}

    	public void CancelAndWait() {
    		Cancel();
    		try {
				_Thread.join();
			} catch (InterruptedException e) {
			}
    	}
    }

	public synchronized void MultiplyReflectionByMatrix(Matrix matrix) {
		float[] Nodes = new float[8];
		Nodes[0] = Xmn; Nodes[1] = Ymn;
		Nodes[2] = Xmx; Nodes[3] = Ymn;
		Nodes[4] = Xmx; Nodes[5] = Ymx;
		Nodes[6] = Xmn; Nodes[7] = Ymx;
		matrix.mapPoints(Nodes);
		TXYCoord C0 = ConvertToReal(Nodes[0],Nodes[1]);
		TXYCoord C1 = ConvertToReal(Nodes[2],Nodes[3]);
		TXYCoord C2 = ConvertToReal(Nodes[4],Nodes[5]);
		TXYCoord C3 = ConvertToReal(Nodes[6],Nodes[7]);
		X0 = C0.X; Y0 = C0.Y;  
		X1 = C1.X; Y1 = C1.Y;  
		X2 = C2.X; Y2 = C2.Y;  
		X3 = C3.X; Y3 = C3.Y;  
		//.
		Normalize();
	}
    
	public synchronized void PixShiftReflection(double HorShift, double VertShift)
	{
		double VS;
		double HS;
		double ofsX;
		double ofsY;
		double diffX0X3;
		double diffY0Y3;
		double diffX0X1;
		double diffY0Y1;

		VS = (VertShift+0.0)/(Ymx-Ymn);
		HS = (HorShift+0.0)/(Xmx-Xmn);

		diffX0X3 = X0-X3; diffY0Y3 = Y0-Y3;
		diffX0X1 = X0-X1; diffY0Y1 = Y0-Y1;

		ofsX = (diffX0X1)*HS+(diffX0X3)*VS;
		ofsY = (diffY0Y1)*HS+(diffY0Y3)*VS;
		X0 = X0+ofsX; Y0 = Y0+ofsY;
		X1 = X1+ofsX; Y1 = Y1+ofsY;
		X2 = X2+ofsX; Y2 = Y2+ofsY;
		X3 = X3+ofsX; Y3 = Y3+ofsY;
		Xcenter = Xcenter+ofsX; Ycenter = Ycenter+ofsY;
		
		Update();
	}

	private TXYCoord ChangeScaleReflection_PrepCoord(double Scale, double X, double Y)
	{
		TXYCoord XYCoord = new TXYCoord();
		
		double diffXXc;
		double diffYYc;
		diffXXc = X-Xcenter; diffYYc = Y-Ycenter;
		XYCoord.X = diffXXc*Scale;
		XYCoord.Y = diffYYc*Scale;
		return XYCoord;
	}

	public synchronized void ChangeScaleReflection(double Scale)
	{
		TXYCoord C;
		C = ChangeScaleReflection_PrepCoord(Scale,X0,Y0);
		X0 = C.X; Y0 = C.Y;
		C = ChangeScaleReflection_PrepCoord(Scale,X2,Y2);
		X2 = C.X; Y2 = C.Y;
		C = ChangeScaleReflection_PrepCoord(Scale,X1,Y1);
		X1 = C.X; Y1 = C.Y;
		C = ChangeScaleReflection_PrepCoord(Scale,X3,Y3);
		X3 = C.X; Y3 = C.Y;
		Update();
	}

	public synchronized void AlignReflectionToNorthPole()
	{
		//* if NOT (ConvertSpaceCRDToGeoCRD(X3,Y3, Lat,Long) AND ConvertSpaceCRDToGeoCRD(X0,Y0, Lat1,Long1)) then Exit; //. ->
		double Lat = Y3, Long = X3;
		double Lat1 = Y0, Long1 = X0;
		double Angle;
		if ((Lat1-Lat) != 0)
		{
			Angle = Math.atan((Long1-Long)/(Lat1-Lat));
			if (((Lat1-Lat) < 0) && ((Long1-Long) > 0)) Angle = Angle+Math.PI; else
				if (((Lat1-Lat) < 0) && ((Long1-Long) < 0)) Angle = Angle+Math.PI; else
				if (((Lat1-Lat) > 0) && ((Long1-Long) < 0)) Angle = Angle+2*Math.PI;
		}
		else
		{
			if ((Long1-Long) >= 0) Angle = Math.PI/2; else Angle = -Math.PI/2;
		};
		double GAMMA = -Angle;
		if (GAMMA < -Math.PI)
		{
			GAMMA = GAMMA+2*Math.PI;
		}
		else
		{
			if (GAMMA > Math.PI) GAMMA = GAMMA-2*Math.PI;
		};
		//. rotating ...
		while (Math.abs(GAMMA) > Math.PI/32) 
		{
			RotateReflection(Math.PI/32*(GAMMA/Math.abs(GAMMA)));
			GAMMA = GAMMA-Math.PI/32*(GAMMA/Math.abs(GAMMA));
		};
		RotateReflection(GAMMA);
	}

	public synchronized void SetReflection(double X, double Y)
	{
		double dX = X-Xcenter;
		double dY = Y-Ycenter;
		X0 = X0+dX; Y0 = Y0+dY; 
		X1 = X1+dX; Y1 = Y1+dY; 
		X2 = X2+dX; Y2 = Y2+dY; 
		X3 = X3+dX; Y3 = Y3+dY;
		//.
		Update();
	}

	public TXYCoord RotateReflection_PrepCoord(double X, double Y, double QdR, double Qdl, double DirRotate, double DPX, double DPY)
	{
		double diffXXc;
		double diffYYc;
		double ofsXrt1;
		double ofsYrt1;
		double ofsXrt2;
		double ofsYrt2;
		double a;
		double b;
		double c;

		TXYCoord XYCoord = new TXYCoord(); 
		
		diffXXc = (X-Xcenter); diffYYc = (Y-Ycenter);
		a = 4.0*QdR;
		if (Math.abs(diffXXc) >= Math.abs(diffYYc))
		{
			b = 4.0*Qdl*diffYYc;
			c = Qdl*(Qdl-4.0*diffXXc*diffXXc);
			ofsYrt1 = (-b+Math.sqrt(b*b-4.0*a*c))/(2.0*a);
			ofsXrt1 = (-2.0*ofsYrt1*diffYYc-Qdl)/(2.0*diffXXc);
			ofsYrt2 = (-b-Math.sqrt(b*b-4.0*a*c))/(2.0*a);
			ofsXrt2 = (-2.0*ofsYrt2*diffYYc-Qdl)/(2.0*diffXXc);
			if ((Math.pow((X+ofsXrt1)-DPX,2)+Math.pow((Y+ofsYrt1)-DPY,2))*DirRotate < (Math.pow((X+ofsXrt2)-DPX,2)+Math.pow((Y+ofsYrt2)-DPY,2))*DirRotate)
			{
				XYCoord.X = X+ofsXrt1;
				XYCoord.Y = Y+ofsYrt1;
			}
			else
			{
				XYCoord.X = X+ofsXrt2;
				XYCoord.Y = Y+ofsYrt2;
			}
		}
		else
		{
			b = 4.0*Qdl*diffXXc;
			c = Qdl*(Qdl-4.0*diffYYc*diffYYc);
			ofsXrt1 = (-b+Math.sqrt(b*b-4.0*a*c))/(2.0*a);
			ofsYrt1 = (-2.0*ofsXrt1*diffXXc-Qdl)/(2.0*diffYYc);
			ofsXrt2 = (-b-Math.sqrt(b*b-4.0*a*c))/(2.0*a);
			ofsYrt2 = (-2.0*ofsXrt2*diffXXc-Qdl)/(2.0*diffYYc);

			if ((Math.pow((Y+ofsYrt1)-DPY,2)+Math.pow((X+ofsXrt1)-DPX,2))*DirRotate < (Math.pow((Y+ofsYrt2)-DPY,2)+Math.pow((X+ofsXrt2)-DPX,2))*DirRotate)
			{
				XYCoord.Y = Y+ofsYrt1;
				XYCoord.X = X+ofsXrt1;
			}
			else
			{
				XYCoord.Y = Y+ofsYrt2;
				XYCoord.X = X+ofsXrt2;
			}
		}
		return XYCoord;
	}

	public synchronized void RotateReflection(double Angle)
	{
		double QdR;
		double Qdl;
		int DirRotate;

		DirRotate = (int)(Angle/Math.abs(Angle));
		/*!!! algorithm working area*/ if (Math.abs(Angle) > Math.PI/32) Angle = Math.PI/32*DirRotate;
		QdR = Math.pow((X0-Xcenter),2)+Math.pow((Y0-Ycenter),2);
		Qdl = QdR*Math.pow((2.0*Math.sin(Angle/2.0)),2);
		
		TXYCoord C;
		C = RotateReflection_PrepCoord(X0,Y0, QdR,Qdl,DirRotate, X1,Y1);
		X0 = C.X; Y0 = C.Y;
		C = RotateReflection_PrepCoord(X1,Y1, QdR,Qdl,DirRotate, X2,Y2);
		X1 = C.X; Y1 = C.Y;
		C = RotateReflection_PrepCoord(X2,Y2, QdR,Qdl,DirRotate, X3,Y3);
		X2 = C.X; Y2 = C.Y;
		C = RotateReflection_PrepCoord(X3,Y3, QdR,Qdl,DirRotate, X0,Y0);
		X3 = C.X; Y3 = C.Y;

		Update();
		if (((X1 == X0) || (Y1 == Y0)) || ((X3 == X0) || (Y3 == Y0)))
		{
			//. avoid algorithm except point
			RotateReflection(Math.PI/4000000);
			return; //. ->
		};
	}

	public synchronized TReflectionWindowActualityInterval GetActualityInterval() {
		return (new TReflectionWindowActualityInterval(ActualityInterval));
	}

	public synchronized void SetActualityInterval(double BeginTimestamp, double EndTimestamp) {
		ActualityInterval.Set(BeginTimestamp,EndTimestamp);
		//.
		try {
			DoOnSetActualityInterval();
    	}
    	catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_LONG).show();
    	}
	}
	
	public synchronized void ResetActualityInterval() {
		ActualityInterval.Reset();
		//.
		try {
			DoOnSetActualityInterval();
    	}
    	catch (Exception E) {
            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_LONG).show();
    	}
	}
	
	public void DoOnSetActualityInterval() throws IOException {
		Reflector.ResetVisualizations();
	}
	
	public AlertDialog CreateTileServerVisualizationUserDataPanel(Activity ParentActivity) {
		final TTileServerVisualizationUserData.TTileServerVisualization TSV;
		final CharSequence[] _items;
		int SelectedIdx = -1;
		synchronized (this) {
			if (TileServerVisualizationUserData.TileServerVisualizations == null)
				return null; //. ->
			TSV = TileServerVisualizationUserData.TileServerVisualizations.get(0);
			_items = new CharSequence[TSV.Providers.size()];
			for (int I = 0; I < TSV.Providers.size(); I++) 
				_items[I] = TSV.Name+": "+TSV.Providers.get(I).Name;
			for (int I = 0; I < TSV.Providers.size(); I++)
				if (TSV.Providers.get(I).ID == TSV.CurrentProvider) {
					SelectedIdx = I;
					break; //. >
				}
		}
		//.
		AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
		builder.setTitle(R.string.SMapSources);
		builder.setNegativeButton(R.string.SCancel,null);
		builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				synchronized (TReflectionWindow.this) {
					TTileServerVisualizationUserData.TTileServerVisualizationProvider TSVP = TSV.Providers.get(arg1);
					try {
						TSV.SetCurrentProvider(TSVP.ID);
						//.
						DoOnSetVisualizationUserData();
						//.
						arg0.dismiss();
			    	}
			    	catch (Exception E) {
			            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_LONG).show();
			    	}
				}
			}
		});
		AlertDialog alert = builder.create();
		return alert;
	}
	
	public void DoOnSetVisualizationUserData() throws IOException {
		Reflector.ClearReflections();
	}
	
	public Intent CreateConfigurationPanel(Activity ParentActivity) {
		return new Intent(ParentActivity, TReflectionWindowConfigurationPanel.class);
	}	
}

