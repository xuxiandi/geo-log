package com.geoscope.GeoEye;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Base64;
import android.util.Base64OutputStream;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.Utils.TDataConverter;

public class TReflectorCoGeoMonitorObject {
	
	private TReflector Reflector;
	//.
	public int 		ID;
	public String 	Name = "Object";
	public boolean 	flEnabled;
	public String	LabelText;
	//.
	public boolean			flStatusIsEnabled = true;
	public boolean			Status_flOnline = false;
	public static final int STATUS_flOnline_Mask = 1;
	public boolean			Status_flLocationIsAvailable = false;
	public static final int STATUS_flLocationIsAvailable_Mask = 2;
	public boolean			Status_flAlarm = false;
	public static final int STATUS_flAlarm_Mask = 4;
	//.
	private boolean flVisualizationInitialized = false;
	public int 		idTVisualization;
	public int 		idVisualization;
	public int 		VisualizationPtr;
	public TXYCoord VisualizationLocation = null;
	public float[] 	VisualizationScreenLocation = new float[2];
	public boolean 	VisualizationIsVisible = true;
	public boolean 	flSelected = false;
	private Paint 	DrawPaint;
	private Path 	TrianglePath;
	private Paint 	TextDrawPaint;
	private Paint	ShadowTextDrawPaint;
	private float 	PictureHeight;
	private float 	PictureWidth;
	private float 	TextSize;
	private float 	TextHeight;
	private float 	PictureDelimiter;
	private float 	LabelTextWidth;
	
	public TReflectorCoGeoMonitorObject() {
	}
	
	public TReflectorCoGeoMonitorObject(TReflector pReflector, int pID, String pName, boolean pflEnabled) {
		ID = pID;
		Name = pName;
		flEnabled = pflEnabled;
		//.
		Prepare(pReflector);
	}
	
	public void Prepare(TReflector pReflector) {
		Reflector = pReflector;
		//.
		LabelText = Name+" "+"¹"+Integer.toString(ID);
		//.
		DrawPaint = new Paint();
		DrawPaint.setStyle(Paint.Style.FILL);
		TextSize = 24.0F*Reflector.metrics.density;
		TextHeight = TextSize;
		PictureHeight = TextHeight;
		PictureWidth = PictureHeight;
		TrianglePath = new Path();
		TrianglePath.moveTo(0,0);
		TrianglePath.lineTo(PictureWidth,0);
		TrianglePath.lineTo(PictureWidth,PictureHeight);
		PictureDelimiter = 2.0F*Reflector.metrics.density;
		TextDrawPaint = new Paint();
		TextDrawPaint.setColor(Color.WHITE);
		TextDrawPaint.setStyle(Paint.Style.FILL);
		TextDrawPaint.setAntiAlias(true);
		TextDrawPaint.setTextSize(TextSize);
		ShadowTextDrawPaint = new Paint(TextDrawPaint);
		ShadowTextDrawPaint.setColor(Color.BLACK);
		LabelTextWidth = TextDrawPaint.measureText(LabelText);
	}
	
	private String PrepareLocationURL() {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"Co"+"/"+Integer.toString(ID)+"/"+"Data.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/;
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
	
	public TXYCoord GetComponentLocation() {
		TXYCoord C;
		String CommandURL = PrepareLocationURL();
		//.
		try {
			HttpURLConnection Connection = Reflector.Server.OpenConnection(CommandURL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[2*8/*SizeOf(Double)*/];
					int Size= in.read(Data);
					if (Size != Data.length)
						throw new IOException(Reflector.getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
					C.Y = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); 
				}
				finally {
					in.close();
				}                
			}
			finally {
				Connection.disconnect();
			}
		} 
		catch (IOException E) {
			C = null;
		}
		return C;
	}
	
	private String PrepareVisualizationDataURL() {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"Co"+"/"+Integer.toString(ID)+"/"+"Data.dat";
		//. add command parameters
		URL2 = URL2+"?"+"2"/*command version*/;
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

	private void GetVisualizationData() throws IOException {
		String CommandURL = PrepareVisualizationDataURL();
		//.
		try {
			HttpURLConnection Connection = Reflector.Server.OpenConnection(CommandURL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[4/*idTVisualization*/+8/*idVisualization64*/+8/*VisualiztaionPtr64*/];
					int Size= in.read(Data);
					if (Size != Data.length)
						throw new IOException(Reflector.getString(R.string.SErrorOfGettingVisualizationData)); //. =>
					int Idx = 0;
					synchronized (this) {
						idTVisualization = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx += 4;
						idVisualization = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx += 8; //. native VisualizationID is Int64
						VisualizationPtr = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx += 8; //. native VisualizationPtr is Int64
					}
				}
				finally {
					in.close();
				}                
			}
			finally {
				Connection.disconnect();
			}
		} 
		catch (IOException E) {
			throw E; //. =>
		}
	}	
	
	public void CheckVisualization() throws IOException {
		if (!flVisualizationInitialized) {
			GetVisualizationData();
			flVisualizationInitialized = true;
		}
	}

	private String PrepareVisualizationLocationURL() {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "Functionality"+"/"+"VisualizationData.dat";
		//. add command parameters
		synchronized (this) {
			URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(VisualizationPtr);
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

	public TXYCoord GetVisalizationLocation() {
		TXYCoord C;
		try {
			CheckVisualization();
			//.
			String CommandURL = PrepareVisualizationLocationURL();
			//.
			HttpURLConnection Connection = Reflector.Server.OpenConnection(CommandURL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[2*8/*SizeOf(Double)*/];
					int Size= in.read(Data);
					if (Size != Data.length)
						throw new IOException(Reflector.getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
					C.Y = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); 
				}
				finally {
					in.close();
				}                
			}
			finally {
				Connection.disconnect();
			}
		} 
		catch (IOException E) {
			C = null;
		}
		return C;
	}	
	
	public boolean UpdateVisualizationLocation() throws Exception {
		boolean Result = true;
		TXYCoord C = GetVisalizationLocation();
		if (C == null)
			throw new Exception(Reflector.getString(R.string.SErrorOfUpdatingCurrentPositionForObject)+Integer.toString(ID)); //. =>
		synchronized (this) {
			if (VisualizationLocation != null) { 
				if (!VisualizationLocation.IsTheSame(C)) 
					VisualizationLocation = C;
				else
					Result = false;
			}
			else
				VisualizationLocation = C;
			if (Result) 
				Result = RecalculateVisualizationScreenLocation();
		}
		return Result;
	}

	public synchronized boolean RecalculateVisualizationScreenLocation() {
		boolean R = true;
		if (VisualizationLocation != null) {
			TXYCoord C = Reflector.ReflectionWindow.ConvertToScreen(VisualizationLocation.X,VisualizationLocation.Y);
			VisualizationScreenLocation[0] = (float)C.X;
			VisualizationScreenLocation[1] = (float)C.Y;
			//.
			boolean LocationIsVisible = Reflector.ReflectionWindow.IsNodeVisible(VisualizationLocation.X,VisualizationLocation.Y);
			R = (LocationIsVisible || VisualizationIsVisible);
			VisualizationIsVisible = LocationIsVisible; 
		}
		return R;
	}
	
	private String PrepareCoGeoMonitorObjectGetDataURL(int DataType) {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"TypedCo"+"/"+Integer.toString(SpaceDefines.idTCoGeoMonitorObject)+"/"+Integer.toString(ID)+"/"+"Data.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(DataType);
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
	
    public byte[] GetData(int DataType) throws Exception,IOException {
		String CommandURL = PrepareCoGeoMonitorObjectGetDataURL(DataType);
		//.
		HttpURLConnection Connection = Reflector.Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					return null; //. ->
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
	            }
	            //.
	            return Data; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
    }
    
	private String PrepareCoGeoMonitorObjectSetDataURL(int DataType, byte[] Data) throws IOException {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"TypedCo"+"/"+Integer.toString(SpaceDefines.idTCoGeoMonitorObject)+"/"+Integer.toString(ID)+"/"+"Data.dat";
		//. add command parameters
		String DataString;
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			Base64OutputStream B64S = new Base64OutputStream(BOS,Base64.URL_SAFE);
			try {
				B64S.write(Data);
			}
			finally {
				B64S.close();
			}
			DataString = new String(BOS.toByteArray());
		}
		finally {
			BOS.close();
		}
		URL2 = URL2+"?"+"2"/*command version*/+","+Integer.toString(DataType)+","+DataString;
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
	
    public void SetData(int DataType, byte[] Data) throws Exception,IOException {
		String CommandURL = PrepareCoGeoMonitorObjectSetDataURL(DataType,Data);
		//.
		HttpURLConnection Connection = Reflector.Server.OpenConnection(CommandURL);
		try {
		}
		finally {
			Connection.disconnect();
		}
    }
    
	public String ToIncomingCommandMessage(int Version, int Session) {
		String _Name = Name.replace(';',',');
		int EnabledFlag = 0;
		if (flEnabled)
			EnabledFlag = 1;
		String Result = TGeoScopeServerUser.TGeoMonitorObjectCommandMessage.Prefix+" "+Integer.toString(Version)/*Parameters version*/+";"+
			_Name+";"+
			Integer.toString(EnabledFlag)+";"+ //. flEnabled
			Integer.toString(ID)+";"+ //. ComponentID
			""+";"+ //. GeographServerAddress
			"0"+";"+ //. GeographServerPort
			"0"+";"+ //. GeographServerObjectID
			Integer.toString(Session);
		return Result;
	}
	
	public String[] FromIncomingCommandMessage(String Command) throws Exception {
		if (!Command.startsWith(TGeoScopeServerUser.TGeoMonitorObjectCommandMessage.Prefix))
			throw new Exception("incorrect command prefix"); //. =>
		String ParamsString = Command.substring(TGeoScopeServerUser.TGeoMonitorObjectCommandMessage.Prefix.length()+1/*skip space*/);
		String[] Params = ParamsString.split(";");
		int Version = Integer.parseInt(Params[0]);
		switch (Version) {
		
		case 0:
			Name = Params[1];
			flEnabled = (Integer.parseInt(Params[2]) != 0);
			ID = Integer.parseInt(Params[3]);
			@SuppressWarnings("unused")
			String GeographServerAddress = Params[4]; 
			@SuppressWarnings("unused")
			int GeographServerPort = Integer.parseInt(Params[5]);
			@SuppressWarnings("unused")
			int GeographServerObjectID = Integer.parseInt(Params[6]);
			//.
			return Params; //. ->
			
		default:
			throw new Exception("unknown command parameters version"); //. =>
		}
	}
	
	public int UpdateStatus() throws Exception {
		byte[] Data = GetData(0);
		boolean IsOnline = (Data[0] > 0);
		boolean LocationIsAvailable = (Data[1] > 0);
		int UserAlarm = TDataConverter.ConvertBEByteArrayToInt32(Data,2);
		boolean flAlarm = (UserAlarm > 0); 
		//.
		int R = 0;
		synchronized (this) {
			if (IsOnline != Status_flOnline) {
				Status_flOnline = IsOnline;
				R = (R | STATUS_flOnline_Mask);
				if (!Status_flOnline) {
					if (LocationIsAvailable) {
						Status_flLocationIsAvailable = false;
						R = (R | STATUS_flLocationIsAvailable_Mask);
					}
					if (Status_flAlarm) {
						Status_flAlarm = false; 
						R = (R | STATUS_flAlarm_Mask);
					}
					return R; //. ->
				}
			}
			if (LocationIsAvailable != Status_flLocationIsAvailable) {
				Status_flLocationIsAvailable = LocationIsAvailable;
				R = (R | STATUS_flLocationIsAvailable_Mask);
			}
			if (flAlarm != Status_flAlarm) {
				Status_flAlarm = flAlarm; 
				R = (R | STATUS_flAlarm_Mask);
			}
		}
		return R;
	}
	
	public void DoOnStatusAlarmChanged() {
		
	}
	
	public void DrawOnCanvas(Canvas canvas) {
		boolean R;
		float X = 0, Y = 0;
		boolean _flSelected = false;
		synchronized (this) {
			R = ((VisualizationLocation != null) && VisualizationIsVisible);
			if (R) {
				X = VisualizationScreenLocation[0];
				Y = VisualizationScreenLocation[1];
				_flSelected = flSelected;
			}
		}
		if (R) {
            if (_flSelected) {
    			float W = (PictureWidth+PictureDelimiter)*3+LabelTextWidth;
				DrawPaint.setColor(Color.DKGRAY);
				canvas.drawRect(X,Y, X+W,Y+PictureHeight, DrawPaint);
            }
            //.
			DrawPaint.setColor(Color.RED);
			//.
			boolean flOnline;
			boolean flLocationIsAvailable;
			boolean flAlarm;
			synchronized (this) {
				flOnline = Status_flOnline;
				flLocationIsAvailable = Status_flLocationIsAvailable;
				flAlarm = Status_flAlarm;
			}
			if (flOnline) {
				DrawPaint.setColor(Color.GREEN);
				if (!_flSelected) { 
					TrianglePath.offset(X,Y);
					canvas.drawPath(TrianglePath, DrawPaint);
					TrianglePath.offset(-X,-Y);
				}
				else 
					canvas.drawRect(X,Y, X+PictureWidth, Y+PictureHeight, DrawPaint);
				X += PictureWidth+PictureDelimiter;
				if (flLocationIsAvailable)
					DrawPaint.setColor(Color.GREEN);
				else
					DrawPaint.setColor(Color.RED);
				canvas.drawRect(X,Y, X+PictureWidth, Y+PictureHeight, DrawPaint);
				X += PictureWidth+PictureDelimiter;
				if (flAlarm)
					DrawPaint.setColor(Color.RED);
				else
					DrawPaint.setColor(Color.GREEN);
				canvas.drawRect(X,Y, X+PictureWidth, Y+PictureHeight, DrawPaint);
				X += PictureWidth+PictureDelimiter;
			}
			else {
				DrawPaint.setColor(Color.RED);
				if (!_flSelected) {
					TrianglePath.offset(X,Y);
					canvas.drawPath(TrianglePath, DrawPaint);
					TrianglePath.offset(-X,-Y);
				}
				else 
					canvas.drawRect(X,Y, X+PictureWidth, Y+PictureHeight, DrawPaint);
				X += PictureWidth+PictureDelimiter;
				DrawPaint.setColor(Color.GRAY);
				canvas.drawRect(X,Y, X+PictureWidth, Y+PictureHeight, DrawPaint);
				X += PictureWidth+PictureDelimiter;
				DrawPaint.setColor(Color.GRAY);
				canvas.drawRect(X,Y, X+PictureWidth, Y+PictureHeight, DrawPaint);
				X += PictureWidth+PictureDelimiter;
			}
			//.
			float TX = X;
			float TY = Y+PictureHeight-4/**/;
            canvas.drawText(LabelText, TX+1,TY+1, ShadowTextDrawPaint);
            canvas.drawText(LabelText, TX,TY, TextDrawPaint);
		}
	}
	
	public boolean Select(float pX, float pY) {
		flSelected = false;
		boolean R;
		float X = 0, Y = 0;
		synchronized (this) {
			R = (VisualizationLocation != null);
			if (R) {
				X = VisualizationScreenLocation[0];
				Y = VisualizationScreenLocation[1];
			}
		}
		if (R) {
			float PictureHeight = TextHeight;
			float PictureWidth = PictureHeight;
			float W = (PictureWidth+PictureDelimiter)*3+LabelTextWidth;
			flSelected = (((X <= pX) && (pX <= (X+W))) && (((Y <= pY) && (pY <= (Y+PictureHeight)))));
		}		
		return flSelected;
	}
	
	public void UnSelect() {
		flSelected = false;
	}
}

