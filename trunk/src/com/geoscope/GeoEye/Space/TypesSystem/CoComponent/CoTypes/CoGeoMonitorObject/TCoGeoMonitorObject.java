package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Base64;
import android.util.Base64OutputStream;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographServerClient;

public class TCoGeoMonitorObject {
	
	public TGeoScopeServer Server;
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
	private boolean flDataIsInitialized = false;
	//.
	protected int 		idTVisualization;
	protected int 		idVisualization;
	protected int 		VisualizationPtr;
	//.
	private int 	idGeographServerObject;
	//.
	private int 					idGeographServer;
	private int						ObjectID; //. GeographServer registration ID
	private String					GeographServerAddress = null; 
	private int						GeographServerPort; 
	private TGeographServerClient 	GeographServerClient = null;
	//.
	public TXYCoord VisualizationLocation = null;
	public boolean 	flSelected = false;
	private Paint 	DrawPaint;
	private Path 	TrianglePath;
	private Paint 	TextDrawPaint;
	private Paint	ShadowTextDrawPaint;
	private float 	PictureHeight;
	private float 	PictureWidth;
	private float 	PictureWidth1;
	private float 	TextSize;
	private float 	TextHeight;
	private float 	PictureDelimiter;
	private float 	LabelTextWidth;
	private Paint	SelectedPaint;
	
	public TCoGeoMonitorObject(TGeoScopeServer pServer, int pID, String pName, boolean pflEnabled) {
		Server = pServer;
		//.
		ID = pID;
		Name = pName;
		flEnabled = pflEnabled;
	}
	
	public TCoGeoMonitorObject(TGeoScopeServer pServer, int pID) {
		this(pServer, pID, "",true);
	}
	
	public TCoGeoMonitorObject() {
	}
	
	public void Destroy() throws IOException {
		if (GeographServerClient != null) {
			GeographServerClient.Destroy();
			GeographServerClient = null;
		}
	}
	
	public void Prepare(TReflector pReflector) {
		LabelText = Name+" "+"¹"+Integer.toString(ID);
		//.
		DrawPaint = new Paint();
		DrawPaint.setStyle(Paint.Style.FILL);
		TextSize = 24.0F*pReflector.metrics.density;
		TextHeight = TextSize;
		PictureHeight = TextHeight;
		PictureWidth = PictureHeight;
		PictureWidth1 = (PictureWidth/3.0F); 
		TrianglePath = new Path();
		TrianglePath.moveTo(0,PictureHeight/2.0F);
		TrianglePath.lineTo(PictureWidth,0);
		TrianglePath.lineTo(PictureWidth,PictureHeight);
		PictureDelimiter = 1.0F*pReflector.metrics.density;
		TextDrawPaint = new Paint();
		TextDrawPaint.setColor(Color.WHITE);
		TextDrawPaint.setStyle(Paint.Style.FILL);
		TextDrawPaint.setAntiAlias(true);
		TextDrawPaint.setTextSize(TextSize);
		ShadowTextDrawPaint = new Paint(TextDrawPaint);
		ShadowTextDrawPaint.setColor(Color.BLACK);
		SelectedPaint = new Paint();
		SelectedPaint.setStrokeWidth(2.0F);
		LabelTextWidth = TextDrawPaint.measureText(LabelText);
	}
	
	private String PrepareLocationURL() {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
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
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
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
	
	public TXYCoord GetComponentLocation(Context context) {
		TXYCoord C;
		String CommandURL = PrepareLocationURL();
		//.
		try {
			HttpURLConnection Connection = Server.OpenConnection(CommandURL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[2*8/*SizeOf(Double)*/];
					int Size= in.read(Data);
					if (Size != Data.length)
						throw new IOException(context.getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertLEByteArrayToDouble(Data,Idx); Idx+=8;
					C.Y = TDataConverter.ConvertLEByteArrayToDouble(Data,Idx); 
				}
				finally {
					in.close();
				}                
			}
			finally {
				Connection.disconnect();
			}
		} 
		catch (Exception E) {
			C = null;
		}
		return C;
	}
	
	private String PrepareDataURL() {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"Co"+"/"+Integer.toString(ID)+"/"+"Data.dat";
		//. add command parameters
		URL2 = URL2+"?"+"4"/*command version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
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

	private void GetData() throws Exception {
		String CommandURL = PrepareDataURL();
		//.
		try {
			HttpURLConnection Connection = Server.OpenConnection(CommandURL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[4/*idTVisualization*/+8/*idVisualization64*/+8/*VisualizationPtr64*/+8/*idGeographServerObject64*/+8/*idGeographServer64*/+4/*ObjectID*/+1/*SizeOf(GeographServerAddressSize)*/];
					int Size= in.read(Data);
					if (Size != Data.length)
						throw new IOException("error of reading data"); //. =>
					short GeographServerAddressSize = (short)(Data[Data.length-1] & 0xFF); 
					byte[] BA = new byte[GeographServerAddressSize];
					Size= in.read(BA);
					if (Size != BA.length)
						throw new IOException("error of reading GeographServerAddress data"); //. =>
					int Idx = 0;
					synchronized (this) {
						idTVisualization = TDataConverter.ConvertLEByteArrayToInt32(Data,Idx); Idx += 4;
						idVisualization = TDataConverter.ConvertLEByteArrayToInt32(Data,Idx); Idx += 8; //. native VisualizationID is Int64
						VisualizationPtr = TDataConverter.ConvertLEByteArrayToInt32(Data,Idx); Idx += 8; //. native VisualizationPtr is Int64
						//.
						idGeographServerObject = TDataConverter.ConvertLEByteArrayToInt32(Data,Idx); Idx += 8; //. native idGeographServerObject is Int64
						//.
						idGeographServer = TDataConverter.ConvertLEByteArrayToInt32(Data,Idx); Idx += 8; //. native idGeographServer is Int64
						ObjectID = TDataConverter.ConvertLEByteArrayToInt32(Data,Idx); Idx += 4; 
						String _GeographServerAddress = new String(BA,"US-ASCII");
						String[] SA = _GeographServerAddress.split(":");
						GeographServerAddress = SA[0];
						GeographServerPort = Integer.parseInt(SA[1])+1/*use GeographServer control port*/;
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
	
	public void CheckData() throws Exception {
		if (!flDataIsInitialized) {
			GetData();
			flDataIsInitialized = true;
		}
	}

	public int GeographServerObjectID() throws Exception {
		CheckData();
		synchronized (this) {
			return idGeographServerObject;
		}
	}
	
	public int GeographServerID() throws Exception {
		CheckData();
		synchronized (this) {
			return idGeographServer;
		}
	}
	
	public String GeographServerAddress() throws Exception {
		CheckData();
		synchronized (this) {
			return GeographServerAddress;
		}
	}
	
	public int GeographServerPort() throws Exception {
		CheckData();
		synchronized (this) {
			return GeographServerPort;
		}
	}
	
	public TGeographServerClient GeographServerClient() throws Exception {
		CheckData();
		synchronized (this) {
			if (GeographServerClient == null)
				GeographServerClient = new TGeographServerClient(Server.context, GeographServerAddress,GeographServerPort, Server.User.UserID,Server.User.UserPassword, idGeographServerObject,ObjectID);
			return GeographServerClient;
		}
	}
	
	private String PrepareVisualizationLocationURL() {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
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
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
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

	public TXYCoord GetVisalizationLocation(Context context) {
		TXYCoord C;
		try {
			CheckData();
			//.
			String CommandURL = PrepareVisualizationLocationURL();
			//.
			HttpURLConnection Connection = Server.OpenConnection(CommandURL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[2*8/*SizeOf(Double)*/];
					int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
					if (Size != Data.length)
						throw new IOException(context.getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertLEByteArrayToDouble(Data,Idx); Idx+=8;
					C.Y = TDataConverter.ConvertLEByteArrayToDouble(Data,Idx); 
				}
				finally {
					in.close();
				}                
			}
			finally {
				Connection.disconnect();
			}
		} 
		catch (Exception E) {
			C = null;
		}
		return C;
	}	
	
	public boolean UpdateVisualizationLocation(TReflectionWindowStruc RW, TReflector Reflector) throws Exception {
		boolean Result = true;
		TXYCoord C = GetVisalizationLocation(Reflector);
		if (C == null)
			throw new Exception(Reflector.getString(R.string.SErrorOfUpdatingCurrentPositionForObject)+Integer.toString(ID)); //. =>
		synchronized (this) {
			TXYCoord LastVisualizationLocation = VisualizationLocation;
			if (VisualizationLocation != null) { 
				if (!VisualizationLocation.IsTheSame(C)) 
					VisualizationLocation = C;
				else
					Result = false;
			}
			else
				VisualizationLocation = C;
			if (Result) {
				Result = (!((!RW.Container_IsNodeVisible(LastVisualizationLocation)) && (!RW.Container_IsNodeVisible(VisualizationLocation))));			
			}
		}
		return Result;
	}

	private String PrepareCoGeoMonitorObjectGetDataURL(int DataType) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
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
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
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
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
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
	                if (Size <= 0) throw new Exception("connection is closed unexpectedly"); //. =>
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
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
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
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
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
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
		}
		finally {
			Connection.disconnect();
		}
    }
    
	private String PrepareCoGeoMonitorObjectSetGetDataURL(int DataType, byte[] Data) throws IOException {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
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
		URL2 = URL2+"?"+"3"/*command version*/+","+Integer.toString(DataType)+","+DataString;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
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
	
    public byte[] SetGetData(int DataType, byte[] Data) throws Exception,IOException {
		String CommandURL = PrepareCoGeoMonitorObjectSetGetDataURL(DataType,Data);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					return null; //. ->
				byte[] OutData = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < OutData.length)
	            {
	                ReadSize = OutData.length-SummarySize;
	                Size = in.read(OutData,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception("connection is closed unexpectedly"); //. =>
	                SummarySize += Size;
	            }
	            //.
	            return OutData; //. ->
			}
			finally {
				in.close();
			}                
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
		int UserAlarm = TDataConverter.ConvertLEByteArrayToInt32(Data,2);
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
	
	public void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas) {
		boolean R;
		float X = 0, Y = 0;
		boolean _flSelected = false;
		synchronized (this) {
			R = (VisualizationLocation != null);
			if (R) {
				TXYCoord C = RW.ConvertToScreen(VisualizationLocation.X,VisualizationLocation.Y);
				//.
				boolean LocationIsVisible = RW.IsScreenNodeVisible(C.X,C.Y);
				R = (LocationIsVisible);
				//.
				X = (float)C.X;
				Y = (float)C.Y-(PictureHeight/2.0F);
				_flSelected = flSelected;
			}
		}
		if (R) {
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
				canvas.save();
				try {
					canvas.translate(X,Y);
					canvas.drawPath(TrianglePath, DrawPaint);
				}
				finally {
					canvas.restore();
				}
				X += PictureWidth+PictureDelimiter;
				if (flLocationIsAvailable)
					DrawPaint.setColor(Color.GREEN);
				else
					DrawPaint.setColor(Color.RED);
				canvas.drawRect(X,Y, X+PictureWidth1, Y+PictureHeight, DrawPaint);
				X += PictureWidth1+PictureDelimiter;
				if (flAlarm)
					DrawPaint.setColor(Color.RED);
				else
					DrawPaint.setColor(Color.GREEN);
				canvas.drawRect(X,Y, X+PictureWidth1, Y+PictureHeight, DrawPaint);
				X += PictureWidth1+PictureDelimiter;
			}
			else {
				DrawPaint.setColor(Color.RED);
				canvas.save();
				try {
					canvas.translate(X,Y);
					canvas.drawPath(TrianglePath, DrawPaint);
				}
				finally {
					canvas.restore();
				}
				X += PictureWidth+PictureDelimiter;
				DrawPaint.setColor(Color.GRAY);
				canvas.drawRect(X,Y, X+PictureWidth1, Y+PictureHeight, DrawPaint);
				X += PictureWidth1+PictureDelimiter;
				DrawPaint.setColor(Color.GRAY);
				canvas.drawRect(X,Y, X+PictureWidth1, Y+PictureHeight, DrawPaint);
				X += PictureWidth1+PictureDelimiter;
			}
			//.
            if (_flSelected) {
    			float W = LabelTextWidth;
    			SelectedPaint.setColor(Color.argb(127,255,0,0));
				float X0 = X;
				float _X0 = X0; 
				float _Y0 = Y;
				float _X1 = X0+W; 
				float _Y1 = Y+PictureHeight;
				canvas.drawRect(_X0,_Y0, _X1,_Y1, SelectedPaint);
            	float[] Points = {_X0,_Y0,_X1,_Y0, _X1,_Y0,_X1,_Y1, _X1,_Y1,_X0,_Y1, _X0,_Y1,_X0,_Y0};
            	SelectedPaint.setColor(Color.argb(255, 255,0,0));
            	canvas.drawLines(Points,SelectedPaint);
            }
			//.
			float TX = X;
			float TY = Y+PictureHeight-4/**/;
            canvas.drawText(LabelText, TX+1,TY+1, ShadowTextDrawPaint);
            canvas.drawText(LabelText, TX,TY, TextDrawPaint);
		}
	}
	
	public boolean Select(TReflectionWindowStruc RW, float pX, float pY) {
		flSelected = false;
		boolean R;
		float X = 0, Y = 0;
		synchronized (this) {
			R = (VisualizationLocation != null);
			if (R) {
				TXYCoord C = RW.ConvertToScreen(VisualizationLocation.X,VisualizationLocation.Y);
				X = (float)C.X;
				Y = (float)C.Y;
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

