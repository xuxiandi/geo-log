package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Pair;

import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawing;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.Classes.MultiThreading.TUpdater;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Defines.TXYIntCoord;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImageryDataServer.TTilesPlace;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileLevel.TGetTilesResult;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;

public class TTileServerProviderCompilation {

	public static final String DataFileName = "Data.xml";
	
	protected TTileImagery TileImagery;
	//.
	protected TReflectorComponent Reflector;
	//.
	public TTileImagery.TTileServerProviderCompilationDescriptor Descriptor;
	//.
	public boolean 	flHistoryEnabled;
	public boolean 	flUserDrawable;
	public int		UserDrawableBaseLevel = 0;
	public double 	X0;
	public double 	Y0;
	public double 	X1;
	public double 	Y1;
	public double 	Width;
	public int 		LevelsCount;
	//.
	protected String Folder;
	public boolean flInitialized;
	public TTileLevel[] Levels;
	//.
	public int MaxAvailableTiles;
	
	public TTileServerProviderCompilation(TTileImagery pTileImagery, TTileImagery.TTileServerProviderCompilationDescriptor pDescriptor, int pMaxAvailableTiles) {
		TileImagery = pTileImagery;
		//.
		Reflector = TileImagery.Reflector;
		//.
		Descriptor = pDescriptor;
		MaxAvailableTiles = pMaxAvailableTiles;
		//.
		Folder = TTileImagery.ImageryFolder+"/"+Integer.toString(Descriptor.SID)+"/"+Integer.toString(Descriptor.PID)+"/"+Integer.toString(Descriptor.CID);
		File F = new File(Folder);
		if (!F.exists()) 
			F.mkdirs();
		//.
		flHistoryEnabled = false;
		flUserDrawable = false;
		//.
		LevelsCount = 0;
		Levels = null;
		//.
		flInitialized = false;
	}

	public void Destroy() {
		if (flInitialized)
			Finalize();
	}
	
	public boolean IsOffline() {
		return Reflector.flOffline;
	}
	
	public synchronized void Initialize() throws Exception {
		if (flInitialized)
			return; //. ->
		//.
		LoadData();
		//.
		flInitialized = true;
	}
	
	public synchronized void Finalize() {
		if (Levels != null) {
			for (int L = 0; L < LevelsCount; L++)
				Levels[L].Destroy();
			Levels = null;
		}
		//.
		flInitialized = false;
	}
	
	public void CheckInitialized() throws Exception {
		if (!flInitialized)
			Initialize();
	}
	
	private synchronized void SetAsUninitialized() {
		flInitialized = false;
	}
	
	private void ParseData(byte[] Data) throws Exception {
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(Data);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
		double Xmin,Ymin;
		double Size;
		NodeList NL;
		switch (Version) {
		case 0:
			try {
				NL = XmlDoc.getDocumentElement().getElementsByTagName("HistoryEnabled");
				flHistoryEnabled = (Integer.parseInt(NL.item(0).getFirstChild().getNodeValue()) != 0);
			}
			catch (Exception E) {}
			//.
			try {
				NL = XmlDoc.getDocumentElement().getElementsByTagName("UserDrawable");
				flUserDrawable = (Integer.parseInt(NL.item(0).getFirstChild().getNodeValue()) != 0);
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("UserDrawableBaseLevel");
				UserDrawableBaseLevel = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
			}
			catch (Exception E) {}
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("Xmin");
			Xmin = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("Ymin");
			Ymin = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("Size");
			Size = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("Levels");
			LevelsCount = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
			//.
			X0 = Xmin; Y0 = Ymin+Size/2.0;
			X1 = Xmin+Size; Y1 = Y0;
			//.
			break; //. >
			
		case 1:
			try {
				NL = XmlDoc.getDocumentElement().getElementsByTagName("HistoryEnabled");
				flHistoryEnabled = (Integer.parseInt(NL.item(0).getFirstChild().getNodeValue()) != 0);
			}
			catch (Exception E) {}
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("X0");
			X0 = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("Y0");
			Y0 = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("X1");
			X1 = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("Y1");
			Y1 = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
			//.
			NL = XmlDoc.getDocumentElement().getElementsByTagName("Levels");
			LevelsCount = Integer.parseInt(NL.item(0).getFirstChild().getNodeValue());
			//.
			Size = Math.sqrt(Math.pow((X1-X0),2)+Math.pow((Y1-Y0),2));
			//.
			break; //. >
			
		default:
			throw new Exception("unknown tile-server data version, version: "+Integer.toString(Version)); //. =>
		}
		//.
		Width = Size;
		TTileLevel[] _Levels = new TTileLevel[LevelsCount];
		for (int L = 0; L < LevelsCount; L++)
			_Levels[L] = new TTileLevel(this, L);
		Levels = _Levels;
	}
	
	private synchronized byte[] HttpServer_LoadData() throws Exception {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Long.toString(Reflector.User.UserID);
		String URL2 = "TileServerData.dat";
		//. add command parameters
		URL2 = URL2+"?"+"3"/*command version*/+","+Integer.toString(Descriptor.SID)+","+Integer.toString(Descriptor.PID)+","+Integer.toString(Descriptor.CID);
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
		byte[] Data;
		HttpURLConnection Connection = Reflector.Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					return null; //. ->
				Data = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < Data.length)
	            {
	                ReadSize = Data.length-SummarySize;
	                Size = in.read(Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(Reflector.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
	                SummarySize += Size;
	            }
			}
			finally {
				in.close();
			}    
			//.
			ParseData(Data);
			//.
			return Data; //. ->
		}
		finally {
			Connection.disconnect();
		}
	}
	
	private synchronized byte[] DataServer_LoadData() throws Exception {
		TGeoScopeServerInfo.TInfo ServersInfo = Reflector.Server.Info.GetInfo();
		TTileImageryDataServer IDS = new TTileImageryDataServer(Reflector.context, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, Reflector.User.UserID, Reflector.User.UserPassword);
		try {
			byte[] Data = IDS.GetCompilationData(Descriptor.SID,Descriptor.PID,Descriptor.CID);
			ParseData(Data);
			//.
			return Data; //. ->
		}
		finally {
			IDS.Destroy();
		}
	}
	
	public void LoadData() throws Exception {
		if (LoadDataLocally())
			return; //. ->
		if (IsOffline())
			throw new Exception("cannot get compilation data in offline mode"); //. =>
		//. load data from server
		byte[] Data = null;
		switch (TileImagery.ServerType) {
		
		case TTileImagery.SERVERTYPE_HTTPSERVER:
			Data = HttpServer_LoadData();
			break; //. >
			
		case TTileImagery.SERVERTYPE_DATASERVER:
			Data = DataServer_LoadData();
			break; //. >
		}
		//. save data locally
		if (Data != null)
			SaveDataLocally(Data);
	}	
	
	@SuppressWarnings("unused")
	private synchronized boolean IsDataExistLocally() {
		File DF = new File(Folder+"/"+DataFileName);
		return (DF.exists());
	}
	
	private synchronized boolean LoadDataLocally() throws Exception {
		File DF = new File(Folder+"/"+DataFileName);
		if (!DF.exists())
			return false; //. ->
		try {
	    	int DataSize = (int)DF.length();
	    	if (DataSize == 0) 
	    		return false; //. ->
	    	byte[] Data = new byte[DataSize];
	    	FileInputStream FIS = new FileInputStream(DF);
	     	try {
	    		FIS.read(Data);
	    	}
			finally {
				FIS.close(); 
			}
			//.
			ParseData(Data);
			//.
			return true; //. ->
		}
		catch (Exception E) {
			return false; //. ->
		}
	}
	
	private synchronized void SaveDataLocally(byte[] Data) throws IOException {
		File DF = new File(Folder+"/"+DataFileName);
		FileOutputStream FOS = new FileOutputStream(DF);
        try
        {
        	if (Data != null)
        		FOS.write(Data);
        }
        finally
        {
        	FOS.close();
        }
	}
	
	private synchronized void DeleteDataLocally() {
		File DF = new File(Folder+"/"+DataFileName);
		DF.delete();
	}
	
	public double HistoryTime() {
		if (!Reflector.ReflectionWindow.ActualityInterval.IsEndTimestampInfinite())
			return Reflector.ReflectionWindow.ActualityInterval.GetEndTimestamp(); //. =>
		else
			return Reflector.ReflectionWindow.ActualityInterval.CurrentEndTimestamp(); //. =>
	}
	
	public int Levels_TilesCount() {
		int Result = 0;
		for (int L = 0; L < LevelsCount; L++)
			Result += Levels[L].TilesCount();
		return Result;
	}
	
	public int Levels_GetMaxTilesCountLevel() {
		int Result = -1;
		int MaxTilesCount = 0;
		for (int L = 0; L < LevelsCount; L++) {
			int Cnt = Levels[L].TilesCount();
			if (Cnt > MaxTilesCount) {
				Result = L;
				MaxTilesCount = Cnt;
			}
		}
		return Result;
	}
	
    private TXYCoord GetReflectionWindowLevelContainerIndexes_ProcessPoint(double X0, double Y0, double X1, double Y1, double X3, double Y3, double X, double Y) {
    	double QdA2 = Math.pow((X-X0),2)+Math.pow((Y-Y0),2);
    	//.
    	double X_QdC = Math.pow((X1-X0),2)+Math.pow((Y1-Y0),2);
    	double X_C = Math.sqrt(X_QdC);
    	double X_QdB2 = Math.pow((X-X1),2)+Math.pow((Y-Y1),2);
    	double X_A1 = (X_QdC-X_QdB2+QdA2)/(2.0*X_C);
    	//.
    	double Y_QdC = Math.pow((X3-X0),2)+Math.pow((Y3-Y0),2);
    	double Y_C = Math.sqrt(Y_QdC);
    	double Y_QdB2 = Math.pow((X-X3),2)+Math.pow((Y-Y3),2);
    	double Y_A1 = (Y_QdC-Y_QdB2+QdA2)/(2.0*Y_C);
    	//.
    	TXYCoord Result = new TXYCoord();
    	Result.X = X_A1;
    	Result.Y = Y_A1;
    	return Result;
    }
	
    private TRWLevelTileContainer GetReflectionWindowLevelContainerIndexes(double X0, double Y0, double X1, double Y1, double X3, double Y3, TReflectionWindowStruc pReflectionWindow, double SW, double SH) {
    	TXYCoord DMin = GetReflectionWindowLevelContainerIndexes_ProcessPoint(X0,Y0, X1,Y1, X3,Y3, pReflectionWindow.Xmn,pReflectionWindow.Ymn);
    	TXYCoord DMax = DMin.Clone();
    	TXYCoord D;
    	D = GetReflectionWindowLevelContainerIndexes_ProcessPoint(X0,Y0, X1,Y1, X3,Y3, pReflectionWindow.Xmx,pReflectionWindow.Ymn);
    	if (D.X < DMin.X)
    		DMin.X = D.X;
    	else
    	    if (D.X > DMax.X)
    	    	DMax.X = D.X;
    	if (D.Y < DMin.Y)
    		DMin.Y = D.Y;
    	else
    		if (D.Y > DMax.Y)
    			DMax.Y = D.Y;
    	D = GetReflectionWindowLevelContainerIndexes_ProcessPoint(X0,Y0, X1,Y1, X3,Y3, pReflectionWindow.Xmx,pReflectionWindow.Ymx);
    	if (D.X < DMin.X)
    		DMin.X = D.X;
    	else
    	    if (D.X > DMax.X)
    	    	DMax.X = D.X;
    	if (D.Y < DMin.Y)
    		DMin.Y = D.Y;
    	else
    		if (D.Y > DMax.Y)
    			DMax.Y = D.Y;
    	D = GetReflectionWindowLevelContainerIndexes_ProcessPoint(X0,Y0, X1,Y1, X3,Y3, pReflectionWindow.Xmn,pReflectionWindow.Ymx);
    	if (D.X < DMin.X)
    		DMin.X = D.X;
    	else
    	    if (D.X > DMax.X)
    	    	DMax.X = D.X;
    	if (D.Y < DMin.Y)
    		DMin.Y = D.Y;
    	else
    		if (D.Y > DMax.Y)
    			DMax.Y = D.Y;
    	TRWLevelTileContainer Result = new TRWLevelTileContainer();
    	Result.Xmn = (int)(DMin.X/SW); Result.Xmx = (int)(DMax.X/SW);
    	Result.Ymn = (int)(DMin.Y/SH); Result.Ymx = (int)(DMax.Y/SH);
    	return Result;
    }
    
	public TRWLevelTileContainer GetLevelTileRange(TReflectionWindowStruc RW) {
		if (!flInitialized)
			return null; //. ->
		if (Levels == null)
			return null; //. ->
		//.
		TXYCoord P0 = RW.ConvertToScreen(this.X0,this.Y0);
		TXYCoord P1 = RW.ConvertToScreen(this.X1,this.Y1);
		double X0 = P0.X; double Y0 = P0.Y;
		double X1 = P1.X; double Y1 = P1.Y;
		double diffX1X0 = X1-X0;
		double diffY1Y0 = Y1-Y0;
		double _Width = Math.sqrt(Math.pow(diffX1X0,2)+Math.pow(diffY1Y0,2));
		double b = (Width*RW.Scale());
		//. double _Scale = _Width/(RW.Xmx-RW.Xmn);
		double Alfa;
		if ((diffX1X0 > 0) && (diffY1Y0 >= 0))
			Alfa = 2*Math.PI+Math.atan(-diffY1Y0/diffX1X0);
		else
			if ((diffX1X0 < 0) && (diffY1Y0 > 0))
				Alfa = Math.PI+Math.atan(-diffY1Y0/diffX1X0);
			else
				if ((diffX1X0 < 0) && (diffY1Y0 <= 0))
					Alfa = Math.PI+Math.atan(-diffY1Y0/diffX1X0);
				else
					if ((diffX1X0 > 0) && (diffY1Y0 < 0))
						Alfa = Math.atan(-diffY1Y0/diffX1X0);
					else
						if (diffY1Y0 > 0)
							Alfa = 3.0*Math.PI/2.0;
						else Alfa = Math.PI/2.0;
		double V;
		double S0_X3,S0_Y3,S1_X3,S1_Y3;
		if (Math.abs(diffY1Y0) > Math.abs(diffX1X0)) {
	 		V = (b/2.0)/Math.sqrt(1+Math.pow((diffX1X0/diffY1Y0),2));
	 		S0_X3 = (V)+X0;
	 		S0_Y3 = (-V)*(diffX1X0/diffY1Y0)+Y0;
	 		S1_X3 = (-V)+X0;
	 		S1_Y3 = (V)*(diffX1X0/diffY1Y0)+Y0;
		}
		else {
			V = (b/2.0)/Math.sqrt(1.0+Math.pow((diffY1Y0/diffX1X0),2));
			S0_Y3 = (V)+Y0;
			S0_X3 = (-V)*(diffY1Y0/diffX1X0)+X0;
			S1_Y3 = (-V)+Y0;
			S1_X3 = (V)*(diffY1Y0/diffX1X0)+X0;
		}
		double Xc,Yc;
		if ((3*Math.PI/4.0 <= Alfa) && (Alfa < 7.0*Math.PI/4.0)) {
			Xc = S0_X3; 
			Yc = S0_Y3; 
		}
		else {
			Xc = S1_X3; 
			Yc = S1_Y3;
		}
		Alfa = -Alfa;
		//. double CosAlfa = Math.cos(Alfa);
		//. double SinAlfa = Math.sin(Alfa);
		double diffX3X0 = (X0-Xc)*2.0;
		double diffY3Y0 = (Y0-Yc)*2.0;
		//. get working level
		int Level = -1;
		double MinFactor = Double.MAX_VALUE;
		for (int L = 0; L < LevelsCount; L++) {
		    double Factor = Math.pow(((TTile.TileSize+0.0)*(1 << L)-_Width),2);
		    if (Factor < MinFactor) {
		    	Level = L;
		    	MinFactor = Factor;
		    }
		    else
		    	if (Level >= 0)
		    		break; //. >
		}
		if (Level < 0)
			return null; //. ->
		//. get ReflectionWindow level container indexes
		int Div = (1 << Level);
		double SW = _Width/Div;
		double SH = b/Div;
		TRWLevelTileContainer Result = GetReflectionWindowLevelContainerIndexes(Xc,Yc, Xc+diffX1X0,Yc+diffY1Y0, Xc+diffX3X0,Yc+diffY3Y0, RW, SW,SH);
		if (Result.Xmn < 0) 
			Result.Xmn = 0;
		if (Result.Xmx >= Div) 
			Result.Xmx = Div-1;
		if (Result.Xmn > Result.Xmx) 
			return null; //. out of bounds ->
		if (Result.Ymn < 0) 
			Result.Ymn = 0;
		if (Result.Ymx >= Div) 
			Result.Ymx = Div-1;
		if (Result.Ymn > Result.Ymx) 
			return null; //. out of bounds ->
		Result.Level = Level;
		Result.TileLevel = Levels[Level];
		Result.RW_Xmn = RW.Xmn;
		Result.RW_Ymn = RW.Ymn;
		Result.b = b;
		Result._Width = _Width;
		Result.diffX1X0 = diffX1X0;
		Result.diffY1Y0 = diffY1Y0;
		Result.diffX3X0 = diffX3X0;
		Result.diffY3Y0 = diffY3Y0;
		Result.Xc = Xc;
		Result.Yc = Yc;
		Result.Rotation = Alfa;
		//.
		return Result;
	}
	
	public void RestoreTiles(TRWLevelTileContainer LevelTileContainer, TTileLimit TileLimit, TCanceller Canceller, TUpdater Updater) throws Exception {
		if (!flInitialized)
			Initialize();
		int Shift = 0;
		for (int L = LevelTileContainer.Level; L >= 0; L--) { 
			Levels[L].RestoreTiles((LevelTileContainer.Xmn >> Shift),(LevelTileContainer.Xmx >> Shift), (LevelTileContainer.Ymn >> Shift),(LevelTileContainer.Ymx >> Shift), TileLimit, Canceller,null,null);
			Shift++;
			//.
			if (Updater != null)
				Updater.Update();
			//.
			if (Canceller != null)
				Canceller.Check();
			//.
			if ((TileLimit != null) && (TileLimit.Value <= 0))
				break; //. >
		}
	}
	
	public TGetTilesResult PrepareTiles(TRWLevelTileContainer LevelTileContainer, TCanceller Canceller, TUpdater Updater, TProgressor Progressor) throws Exception {
		if (!flInitialized)
			Initialize();
		if (LevelTileContainer == null)
			return null; //. ->
		return Levels[LevelTileContainer.Level].GetTiles(LevelTileContainer.Xmn,LevelTileContainer.Xmx, LevelTileContainer.Ymn,LevelTileContainer.Ymx, true, Canceller,Updater,Progressor);
	}
	
	public void PrepareUpLevelsTiles(TRWLevelTileContainer LevelTileContainer, int LevelStep, TCanceller Canceller, TUpdater Updater) throws Exception {
		if (!flInitialized)
			Initialize();
		if (LevelTileContainer == null)
			return; //. ->
		int Xmn = LevelTileContainer.Xmn; int Ymn = LevelTileContainer.Ymn;
		int Xmx = LevelTileContainer.Xmx; int Ymx = LevelTileContainer.Ymx;
		int UpLevel = LevelTileContainer.Level-1;
		int ULI = 1;
		while (UpLevel >= 0) {
			Xmn >>= 1; Ymn >>= 1;
			Xmx >>= 1; Ymx >>= 1;
			if ((((ULI % LevelStep) == 0) || (UpLevel == 0)) && (((Xmx-Xmn) <= 1) && ((Ymx-Ymn) <= 1))) 
				Levels[UpLevel].GetTiles(Xmn,Xmx, Ymn,Ymx, true, Canceller,Updater,null);
			//. next up level
			UpLevel--;
			ULI++;
		}
	}
	
	public void RemoveUpLevelsTiles(int LevelIndex, ArrayList<TTile.TDescriptor> Tiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		if (!flInitialized)
			Initialize();
		int TilesCount = Tiles.size();
		for (int L = LevelIndex-1; L >= 0; L--) {
			int Shift = (LevelIndex-L); 
			TTileLevel Level = Levels[L];
			Hashtable<Pair<Integer,Integer>, Boolean> RemovedTileTable = new Hashtable<Pair<Integer,Integer>, Boolean>();
			for (int I = 0; I < TilesCount; I++) {
				TTile.TDescriptor TD = Tiles.get(I);
				//.
				int X = (TD.X >> Shift); int Y = (TD.Y >> Shift);
				Pair<Integer,Integer> UpTileXY = new Pair<Integer, Integer>(X,Y);
				if (RemovedTileTable.get(UpTileXY) == null) {
					if (flHistoryEnabled)
						Level.RemoveTile(X,Y);
					else {
						if (!Level.DeleteTile(X,Y))
							Level.DeleteTileFile(X,Y, TD.Timestamp);
					}
					//.
					RemovedTileTable.put(UpTileXY, true);
					//.
					if (Canceller != null)
						Canceller.Check();
				}
			}
		}
	}
	
	public void RemoveDownLevelsTiles(int LevelIndex, ArrayList<TTile.TDescriptor> Tiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		if (!flInitialized)
			Initialize();
		int Size = 2;
		int TilesCount = Tiles.size();
		for (int L = LevelIndex+1; L < LevelsCount; L++) {
			int Shift = (L-LevelIndex); 
			TTileLevel Level = Levels[L];
			for (int I = 0; I < TilesCount; I++) {
				TTile.TDescriptor TD = Tiles.get(I);
				//.
				int Xmn = (TD.X << Shift); int Ymn = (TD.Y << Shift);
				int Xmx = Xmn+Size; int Ymx = Ymn+Size;
			      for (int Y = Ymn; Y < Ymx; Y++) 
				        for (int X = Xmn; X < Xmx; X++) {
							if (flHistoryEnabled)
								Level.RemoveTile(X,Y);
							else {
								if (!Level.DeleteTile(X,Y))
									Level.DeleteTileFile(X,Y, TD.Timestamp);
							}
							//.
							if (Canceller != null)
								Canceller.Check();
				        }
			}
			//.
			Size <<= 1;
		}
	}
	
	public void RemoveUpDownLevelsTiles(int LevelIndex, ArrayList<TTile.TDescriptor> Tiles, TCanceller Canceller, TUpdater Updater) throws Exception {
		RemoveUpLevelsTiles(LevelIndex, Tiles, null, Updater);
		RemoveDownLevelsTiles(LevelIndex, Tiles, Canceller, Updater);
	}
	
	public double CommitModifiedTiles(int SecurityFileID, boolean flReSet, double ReSetInterval, TTilesPlace TilesPlace, boolean flEnqueue) throws Exception {
		double Result = Double.MIN_VALUE;
		if (!flInitialized)
			return Result; //. ->
		if (Levels != null) 
			for (int L = 0; L < LevelsCount; L++) {
				double R = Levels[L].CommitModifiedTiles(SecurityFileID,flReSet,ReSetInterval,TilesPlace, flEnqueue);
				if (R > Result)
					Result = R;
			}
		return Result;
	}

	public void ReflectionWindow_DrawOnCanvas(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, boolean flDrawComposition, TCanceller Canceller, TTileLimit CompositionTileLimit, TTimeLimit TimeLimit) throws CancelException, TimeIsExpiredException {
		if (!flInitialized)
			return; //. ->
		if (Levels == null)
			return; //. ->
		TRWLevelTileContainer LevelTileContainer = GetLevelTileRange(RW);
		if ((LevelTileContainer == null) || (Levels[LevelTileContainer.Level] == null))
			return; //. ->
		if (flDrawComposition) {
			if ((CompositionTileLimit != null) && (CompositionTileLimit.Value > 0)) {
				//. draw composition
				try {
					ReflectionWindow_Composition_DrawOnCanvas(GetComposition(RW,LevelTileContainer,MaxCompositionDepth,CompositionTileLimit,null),LevelTileContainer, pImageID, canvas, paint,transitionpaint, Canceller, TimeLimit);
				} catch (CancelException E) {}
			}
			//. draw level container
			Levels[LevelTileContainer.Level].Container_DrawOnCanvas(LevelTileContainer, pImageID,canvas,paint,transitionpaint, Canceller, TimeLimit);
		}
		else {
			//. draw level container
			TRWLevelTileContainer ResultLevelTileContainer = new TRWLevelTileContainer(LevelTileContainer);
			int MaxAvailableTiles = 0;
			while (true) {
				if (Levels[LevelTileContainer.Level].TilesCount() > 0) {
					int AvailableTiles = Levels[LevelTileContainer.Level].Container_TileCounter(LevelTileContainer);				
					if (AvailableTiles > MaxAvailableTiles) {
						MaxAvailableTiles = AvailableTiles;
						ResultLevelTileContainer.AssignContainer(LevelTileContainer);
					}
				}
				LevelTileContainer.Level--;
				if (LevelTileContainer.Level < 0)
					break; //. >
				LevelTileContainer.Xmn >>= 1; 
				LevelTileContainer.Ymn >>= 1; 
				LevelTileContainer.Xmx >>= 1; 
				LevelTileContainer.Ymx >>= 1; 
			}
			if (MaxAvailableTiles > 0)
				Levels[ResultLevelTileContainer.Level].Container_DrawOnCanvas(ResultLevelTileContainer, pImageID,canvas,paint,transitionpaint, Canceller, TimeLimit);				
		}
	}
	
	public TRWLevelTileContainer ReflectionWindow_GetLevelTileContainer(TReflectionWindowStruc RW) {
		if (!flInitialized)
			return null; //. ->
		if (Levels == null)
			return null; //. ->
		TRWLevelTileContainer LevelTileContainer = GetLevelTileRange(RW);
		return LevelTileContainer;
	}
	
	@SuppressWarnings("unused")
	private void ReflectionWindow_PaintDrawings(TReflectionWindowStruc RW, List<TDrawing> Drawings) throws Exception {
		if (!flInitialized)
			return; //. ->
		if (Levels == null)
			return; //. ->
		TRWLevelTileContainer LevelTileContainer = GetLevelTileRange(RW);
		if ((LevelTileContainer == null) || (Levels[LevelTileContainer.Level] == null))
			return; //. ->
		Levels[LevelTileContainer.Level].Container_PaintDrawings(LevelTileContainer,Drawings,false,0.0F,0.0F);
	}
	
	private TRWLevelTileContainer PartialResultLevelTileContainer = null;
	
	public synchronized TRWLevelTileContainer GetPartialResultLevelTileContainer() {
		return PartialResultLevelTileContainer;
	}
	
	public synchronized void SetPartialResultLevelTileContainer(TRWLevelTileContainer Container) {
		PartialResultLevelTileContainer = Container;
	}
	
	public void ClearPartialResultLevelTileContainer() {
		SetPartialResultLevelTileContainer(null);
	}
	
	public void ReflectionWindow_PartialResultLevelTileContainer_DrawOnCanvas(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit) throws TimeIsExpiredException, CancelException {
		if (!flInitialized)
			return; //. ->
		TRWLevelTileContainer _PartialResultLevelTileContainer = GetPartialResultLevelTileContainer();
		if (_PartialResultLevelTileContainer == null)
			return; //. ->
		TRWLevelTileContainer LevelTileContainer = GetLevelTileRange(RW);
		if (LevelTileContainer == null)
			return; //. ->
		LevelTileContainer.AssignContainer(_PartialResultLevelTileContainer);
		Levels[LevelTileContainer.Level].Container_DrawOnCanvas(LevelTileContainer, pImageID,canvas,paint,transitionpaint, Canceller, TimeLimit);
	}
	
	private TRWLevelTileContainer ResultLevelTileContainer = null;
	
	public synchronized TRWLevelTileContainer GetResultLevelTileContainer() {
		return ResultLevelTileContainer;
	}
	
	public synchronized void SetResultLevelTileContainer(TRWLevelTileContainer Container) {
		ResultLevelTileContainer = Container;
	}
	
	public void ClearResultLevelTileContainer() {
		SetResultLevelTileContainer(null);
	}
	
	public void ReflectionWindow_ResultLevelTileContainer_DrawOnCanvas(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit) throws TimeIsExpiredException, CancelException {
		if (!flInitialized)
			return; //. ->
		TRWLevelTileContainer _ResultLevelTileContainer = GetResultLevelTileContainer();
		if (_ResultLevelTileContainer == null)
			return; //. ->
		TRWLevelTileContainer LevelTileContainer = GetLevelTileRange(RW);
		if (LevelTileContainer == null)
			return; //. ->
		LevelTileContainer.AssignContainer(_ResultLevelTileContainer);
		Levels[LevelTileContainer.Level].Container_DrawOnCanvas(LevelTileContainer, pImageID,canvas,paint,transitionpaint, Canceller, TimeLimit);
	}
	
	
	private int MaxCompositionDepth = -1;

	private TTilesComposition ResultComposition = null;
	
	public synchronized TTilesComposition GetResultComposition() {
		return ResultComposition;
	}
	
	public synchronized void SetResultComposition(TTilesComposition Composition) {
		ResultComposition = Composition;
	}
	
	public synchronized void ClearResultComposition() {
		ResultComposition = null;
	}
	
	public void ReflectionWindow_PrepareResultComposition(TReflectionWindowStruc RW, TRWLevelTileContainer LevelTileContainer, TTileLimit TileLimit, TCanceller Canceller) throws CancelException {
		TTilesComposition Composition = GetComposition(RW, LevelTileContainer, 0, TileLimit, Canceller);
		//.
		SetResultComposition(Composition);
	}
	
	public void ReflectionWindow_PrepareResultComposition(TReflectionWindowStruc RW, TTileLimit TileLimit, TCanceller Canceller) throws CancelException {
		TRWLevelTileContainer LevelTileContainer = GetLevelTileRange(RW);
		if (LevelTileContainer == null)
			return; //. ->
		ReflectionWindow_PrepareResultComposition(RW, LevelTileContainer, TileLimit, Canceller);
	}
	
	public void ReflectionWindow_ResultComposition_DrawOnCanvas(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit) throws CancelException, TimeIsExpiredException {
		if (!flInitialized)
			return; //. ->
		TTilesComposition Composition = GetResultComposition();
		if (Composition == null)
			return; //. ->
		TRWLevelTileContainer LevelTileContainer = GetLevelTileRange(RW);
		if (LevelTileContainer == null)
			return; //. ->
		ReflectionWindow_Composition_DrawOnCanvas(Composition,LevelTileContainer, pImageID, canvas, paint,transitionpaint, Canceller, TimeLimit);
	}
	
	private class TTileItem {
		public TTile Tile;
	    public double L;
	}
	
    private TXYIntCoord GetReflectionWindowCenterLevelContainerIndex(double X0, double Y0, double X1, double Y1, double X3, double Y3, TReflectionWindowStruc pReflectionWindow, double SW, double SH) {
    	TXYCoord CI = GetReflectionWindowLevelContainerIndexes_ProcessPoint(X0,Y0, X1,Y1, X3,Y3, (pReflectionWindow.Xmn+pReflectionWindow.Xmx)/2.0,(pReflectionWindow.Ymn+pReflectionWindow.Ymx)/2.0);
    	TXYIntCoord Result = new TXYIntCoord();
    	Result.X = (int)(CI.X/SW);
    	Result.Y = (int)(CI.Y/SH);
    	return Result;
    }
    
	private int Composition_OptimizeTiles(int BaseLevel, double X0, double Y0, double X1, double Y1, double X3, double Y3, TReflectionWindowStruc pReflectionWindow, ArrayList<TTilesCompositionLevel> CompositionLevels, int Composition_TilesCount, TTileLimit TileLimit, TCanceller Canceller) throws CancelException {
		  int RemoveDelta = Composition_TilesCount-TileLimit.Value;
		  if (RemoveDelta <= 0) 
			  return Composition_TilesCount; //. ->
		  for (int I = CompositionLevels.size()-1; I >= 0; I--) {
			  if (I != BaseLevel) {
					TTilesCompositionLevel CompositionLevel = CompositionLevels.get(I);    
				    if (RemoveDelta >= CompositionLevel.Count) {
				    	//. remove all segments at this level
				    	for (int J = 0; J < CompositionLevel.TilesMapSize; J++) {
				    		if (CompositionLevel.TilesMap[J] != null)
				    			CompositionLevel.TilesMap[J] = null;
				    	}
				    	RemoveDelta -= CompositionLevel.Count;
				    	CompositionLevel.Count = 0;
				    	CompositionLevel.flAllItemsExists = false;
				    }
				    else {
				      //. get center of optimization
				      TXYIntCoord CI = GetReflectionWindowCenterLevelContainerIndex(X0,Y0, X1,Y1, X3,Y3, pReflectionWindow, CompositionLevel.SW,CompositionLevel.SH);
				      int OptimizationCenterX = CI.X;
				      int OptimizationCenterY = CI.Y;
				      if (OptimizationCenterX < CompositionLevel.XIndexMin)
				    	  OptimizationCenterX = CompositionLevel.XIndexMin;
				       else
				        if (OptimizationCenterX > CompositionLevel.XIndexMax)
				        	OptimizationCenterX = CompositionLevel.XIndexMax;
				      if (OptimizationCenterY < CompositionLevel.YIndexMin)
				    	  OptimizationCenterY = CompositionLevel.YIndexMin;
				       else
				        if (OptimizationCenterY > CompositionLevel.YIndexMax)
				        	OptimizationCenterY = CompositionLevel.YIndexMax;
				      OptimizationCenterX = OptimizationCenterX-CompositionLevel.XIndexMin;
				      OptimizationCenterY = OptimizationCenterY-CompositionLevel.YIndexMin;
				      //.
				      ArrayList<TTileItem> TileItems = new ArrayList<TTileItem>(CompositionLevel.TilesMapSize); 
				      for (int Y = 0; Y < CompositionLevel.TilesMapSizeY; Y++) {
				        for (int X = 0; X < CompositionLevel.TilesMapSizeX; X++) {
				        	TTile Tile = CompositionLevel.TilesMap[Y*CompositionLevel.TilesMapSizeX+X];
				        	if (Tile != null) {
				        		TTileItem TI = new TTileItem();
				        		TI.Tile = Tile;
				        		TI.L = Math.pow((X-OptimizationCenterX),2)+Math.pow((Y-OptimizationCenterY),2);
					            //. add in L-increasing order
				        		int TIS = TileItems.size();
				        		boolean flInserted = false;
			        			for (int J = 0; J < TIS; J++)
			        				if (TileItems.get(J).L > TI.L) {
			        					TileItems.add(J,TI);
			        					flInserted = true;
			        					break; //. >
			        				}
			        			if (!flInserted)
				        			TileItems.add(TI);
				        	}
				        }
				        //.
						if (Canceller != null)
							Canceller.Check();
				      }
				      //. remove segment elements with maximum length from optimization center
				      CompositionLevel.Count -= RemoveDelta;
			  		  int TIS = TileItems.size();
				      for (int J = CompositionLevel.Count; J < TIS; J++) {
				    	  TTileItem TI = TileItems.get(J);
				    	  CompositionLevel.TilesMap[(TI.Tile.Y-CompositionLevel.YIndexMin)*CompositionLevel.TilesMapSizeX+(TI.Tile.X-CompositionLevel.XIndexMin)] = null; 
				      }
				      CompositionLevel.flAllItemsExists = false;
				      RemoveDelta = 0;
				    }
				    if (RemoveDelta == 0) 
				    	break; //. >
			  }
		  }
		  return TileLimit.Value;
	}

    private void Composition_ProcessSegment_EmptySegmentRecursively(ArrayList<TTilesCompositionLevel> CompositionLevels, int Level, int XIndex, int YIndex) {
    	TTilesCompositionLevel CompositionLevel = CompositionLevels.get(Level);
    	int TileIdx = (YIndex-CompositionLevel.YIndexMin)*CompositionLevel.TilesMapSizeX+(XIndex-CompositionLevel.XIndexMin);
    	if (CompositionLevel.TilesMap[TileIdx] != null) {
    		CompositionLevel.TilesMap[TileIdx] = null;
    		CompositionLevel.Count--;
    		CompositionLevel.flAllItemsExists = false;
    	}
    	//.
    	if (Level < (CompositionLevels.size()-1)) {
    		CompositionLevel = CompositionLevels.get(Level+1);
    		int XI = (XIndex << 1)-CompositionLevel.XIndexMin; int YI = (YIndex << 1)-CompositionLevel.YIndexMin;
    		if (((0 <= XI) && (XI < CompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < CompositionLevel.TilesMapSizeY))) 
    			Composition_ProcessSegment_EmptySegmentRecursively(CompositionLevels, Level+1, (XI+CompositionLevel.XIndexMin),(YI+CompositionLevel.YIndexMin));
    		XI++;
    		if (((0 <= XI) && (XI < CompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < CompositionLevel.TilesMapSizeY))) 
    			Composition_ProcessSegment_EmptySegmentRecursively(CompositionLevels, Level+1, (XI+CompositionLevel.XIndexMin),(YI+CompositionLevel.YIndexMin));
    		YI++;
    		if (((0 <= XI) && (XI < CompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < CompositionLevel.TilesMapSizeY))) 
    			Composition_ProcessSegment_EmptySegmentRecursively(CompositionLevels, Level+1, (XI+CompositionLevel.XIndexMin),(YI+CompositionLevel.YIndexMin));
    		XI--;
    		if (((0 <= XI) && (XI < CompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < CompositionLevel.TilesMapSizeY))) 
    			Composition_ProcessSegment_EmptySegmentRecursively(CompositionLevels, Level+1, (XI+CompositionLevel.XIndexMin),(YI+CompositionLevel.YIndexMin));
    	}    
    }
    
	private boolean Composition_ProcessSegment(ArrayList<TTilesCompositionLevel> CompositionLevels, int Level, int XIndex, int YIndex, int OptimalLevel) {
		TTilesCompositionLevel CompositionLevel = CompositionLevels.get(Level);
    	int TileIdx = (YIndex-CompositionLevel.YIndexMin)*CompositionLevel.TilesMapSizeX+(XIndex-CompositionLevel.XIndexMin);
    	//.
    	TTile Tile = CompositionLevel.TilesMap[TileIdx];
    	boolean Result = ((Tile != null) && (Tile.Data != null));
    	//.
    	if (Level < (CompositionLevels.size()-1)) {
    		boolean flEmptySegment = (Result && (Level >= OptimalLevel));
    		//.
    		TTilesCompositionLevel DownCompositionLevel = CompositionLevels.get(Level+1);
    		int XI = (XIndex << 1)-DownCompositionLevel.XIndexMin; int YI = (YIndex << 1)-DownCompositionLevel.YIndexMin;
    		//.
	     	boolean flSegment00;
			boolean flSegment10;
	      	boolean flSegment01;
	      	boolean flSegment11;
    		if (flEmptySegment) {
    			if (((0 <= XI) && (XI < DownCompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < DownCompositionLevel.TilesMapSizeY))) 
    				Composition_ProcessSegment_EmptySegmentRecursively(CompositionLevels, Level+1, (XI+DownCompositionLevel.XIndexMin),(YI+DownCompositionLevel.YIndexMin));
    			XI++;
    			if (((0 <= XI) && (XI < DownCompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < DownCompositionLevel.TilesMapSizeY))) 
    				Composition_ProcessSegment_EmptySegmentRecursively(CompositionLevels, Level+1, (XI+DownCompositionLevel.XIndexMin),(YI+DownCompositionLevel.YIndexMin));
    			YI++;
    			if (((0 <= XI) && (XI < DownCompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < DownCompositionLevel.TilesMapSizeY))) 
    				Composition_ProcessSegment_EmptySegmentRecursively(CompositionLevels, Level+1, (XI+DownCompositionLevel.XIndexMin),(YI+DownCompositionLevel.YIndexMin));
    			XI--;
    			if (((0 <= XI) && (XI < DownCompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < DownCompositionLevel.TilesMapSizeY))) 
    				Composition_ProcessSegment_EmptySegmentRecursively(CompositionLevels, Level+1, (XI+DownCompositionLevel.XIndexMin),(YI+DownCompositionLevel.YIndexMin));
    			return Result; //. ->
    		}
	     	else {
	      		if (((0 <= XI) && (XI < DownCompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < DownCompositionLevel.TilesMapSizeY)))
	       			flSegment00 = Composition_ProcessSegment(CompositionLevels, Level+1, (XI+DownCompositionLevel.XIndexMin),(YI+DownCompositionLevel.YIndexMin), OptimalLevel);
	       		else flSegment00 = true;
	      		XI++;
	      		if (((0 <= XI) && (XI < DownCompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < DownCompositionLevel.TilesMapSizeY)))
	      			flSegment10 = Composition_ProcessSegment(CompositionLevels, Level+1, (XI+DownCompositionLevel.XIndexMin),(YI+DownCompositionLevel.YIndexMin), OptimalLevel);
	       		else flSegment10 = true;
	      		YI++;
	      		if (((0 <= XI) && (XI < DownCompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < DownCompositionLevel.TilesMapSizeY)))
	       			flSegment01 = Composition_ProcessSegment(CompositionLevels, Level+1, (XI+DownCompositionLevel.XIndexMin),(YI+DownCompositionLevel.YIndexMin), OptimalLevel);
	       		else flSegment01 = true;
	      		XI--;
	      		if (((0 <= XI) && (XI < DownCompositionLevel.TilesMapSizeX)) && ((0 <= YI) && (YI < DownCompositionLevel.TilesMapSizeY)))
					flSegment11 = Composition_ProcessSegment(CompositionLevels, Level+1, (XI+DownCompositionLevel.XIndexMin),(YI+DownCompositionLevel.YIndexMin), OptimalLevel);
	       		else flSegment11 = true;
	      }
	      //.
	      if (flSegment00 && flSegment10 && flSegment01 && flSegment11) 
	      	if (Result) {
	      		CompositionLevel.TilesMap[TileIdx] = null;
	       		CompositionLevel.Count--;
	        	CompositionLevel.flAllItemsExists = false;
	        }
	        else 
	        	Result = true;
	    }
	    return Result;
	}
	
	public TTilesComposition GetComposition(TReflectionWindowStruc RW, TRWLevelTileContainer LevelTileContainer, int CompositionDepth, TTileLimit TileLimit, TCanceller Canceller) throws CancelException {
	    int CompositionLevelNumber = 0;
	    TTilesComposition Composition = new TTilesComposition(LevelsCount);
	    int Composition_SummaryTilesCount = 0;
	    for (int L = 0; L < LevelsCount; L++) {
	        //. composition new level
	    	TTilesCompositionLevel CompositionLevel = new TTilesCompositionLevel();
	    	CompositionLevel.Level = L;
	        //. get ReflectionWindow level container indexes
	    	int Div = (1 << L);
	    	CompositionLevel.SW = LevelTileContainer._Width/Div;
	    	CompositionLevel.SH = LevelTileContainer.b/Div;
	    	TRWLevelTileContainer LTC = GetReflectionWindowLevelContainerIndexes(LevelTileContainer.Xc,LevelTileContainer.Yc, LevelTileContainer.Xc+LevelTileContainer.diffX1X0,LevelTileContainer.Yc+LevelTileContainer.diffY1Y0, LevelTileContainer.Xc+LevelTileContainer.diffX3X0,LevelTileContainer.Yc+LevelTileContainer.diffY3Y0, RW, CompositionLevel.SW,CompositionLevel.SH);
			if (LTC.Xmn < 0) 
				LTC.Xmn = 0;
			if (LTC.Xmx >= Div) 
				LTC.Xmx = Div-1;
			if (LTC.Ymn < 0) 
				LTC.Ymn = 0;
			if (LTC.Ymx >= Div) 
				LTC.Ymx = Div-1;
			CompositionLevel.XIndexMin = LTC.Xmn; CompositionLevel.XIndexMax = LTC.Xmx; 
			CompositionLevel.YIndexMin = LTC.Ymn; CompositionLevel.YIndexMax = LTC.Ymx; 
			CompositionLevel.TilesMapSizeX = LTC.Xmx-LTC.Xmn+1;
			CompositionLevel.TilesMapSizeY = LTC.Ymx-LTC.Ymn+1;
	        //. creating segments map
			CompositionLevel.TilesMapSize = CompositionLevel.TilesMapSizeX*CompositionLevel.TilesMapSizeY;
			CompositionLevel.TilesMap = new TTile[CompositionLevel.TilesMapSize];
			CompositionLevel.Count = 0;
			CompositionLevel.flAllItemsExists = true;
			for (int X = CompositionLevel.XIndexMin; X <= CompositionLevel.XIndexMax; X++)
				for (int Y = CompositionLevel.YIndexMin; Y <= CompositionLevel.YIndexMax; Y++) {
					TTile Tile = Levels[L].GetTile(X,Y);
					if ((Tile != null) && !Tile.Data_flTransparent) { 
						CompositionLevel.TilesMap[(Y-CompositionLevel.YIndexMin)*CompositionLevel.TilesMapSizeX+(X-CompositionLevel.XIndexMin)] = Tile;
						//.
						CompositionLevel.Count++;
			            Composition_SummaryTilesCount++;
					}
					else
						CompositionLevel.flAllItemsExists = false;
				}
	        //. add to composition
			Composition.CompositionLevels.add(CompositionLevel);
	        //.
	        CompositionLevelNumber++;
	        if ((CompositionLevelNumber-LevelTileContainer.Level) > CompositionDepth) 
	        	break; //. >
	        //.
			if (Canceller != null)
				Canceller.Check();
	    }
	    //. optimize composition for performance
	    Composition_SummaryTilesCount = Composition_OptimizeTiles(LevelTileContainer.Level, LevelTileContainer.Xc,LevelTileContainer.Yc, LevelTileContainer.Xc+LevelTileContainer.diffX1X0,LevelTileContainer.Yc+LevelTileContainer.diffY1Y0, LevelTileContainer.Xc+LevelTileContainer.diffX3X0,LevelTileContainer.Yc+LevelTileContainer.diffY3Y0, RW, Composition.CompositionLevels, Composition_SummaryTilesCount, TileLimit, Canceller);
	    //. reform composition
	    if (Composition.CompositionLevels.size() > 1) {
	    	TTilesCompositionLevel CompositionLevel = Composition.CompositionLevels.get(0);
			for (int X = CompositionLevel.XIndexMin; X <= CompositionLevel.XIndexMax; X++) {
				for (int Y = CompositionLevel.YIndexMin; Y <= CompositionLevel.YIndexMax; Y++) 
			          Composition_ProcessSegment(Composition.CompositionLevels, 0, X,Y, LevelTileContainer.Level);
		        //.
				if (Canceller != null)
					Canceller.Check();
			}
	    }
		TileLimit.Value -= Composition.TileCount(); 
	    //.
	    return Composition;
	}
	
	public void ReflectionWindow_Composition_DrawOnCanvas(TTilesComposition Composition, TRWLevelTileContainer LevelTileContainer, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit) throws CancelException, TimeIsExpiredException {
    	//. reflect composition
    	for (int L = 0; L < Composition.CompositionLevels.size(); L++) {
    		TTilesCompositionLevel CompositionLevel = Composition.CompositionLevels.get(L);
      		if (CompositionLevel.Count > 0) 
				Levels[CompositionLevel.Level].Composition_DrawOnCanvas(CompositionLevel, LevelTileContainer, pImageID, canvas, paint,transitionpaint, Canceller, TimeLimit);       		
      	}
	}
	
	public void ReleaseTiles(TRWLevelTileContainer LevelTileContainer, TCanceller Canceller) throws Exception {
		if (!flInitialized)
			return; //. ->
		TRWLevelTileContainer _LevelTileContainer = new TRWLevelTileContainer();
		_LevelTileContainer.AssignContainer(LevelTileContainer);
		//.
		ArrayList<TLevelTile> TileList = new ArrayList<TLevelTile>(MaxAvailableTiles);
		for (int L = LevelsCount-1; L >= 0; L--) {
			boolean flLayIsVisible = (L == _LevelTileContainer.Level);
			TTile[] LevelTiles = Levels[L].GetTiles();
			int TilesCount = LevelTiles.length; 
			for (int I = 0; I < TilesCount; I++) {
				TTile Item = LevelTiles[I];
				if (!Item.flModified) 
					if (flLayIsVisible) {
						if (!(((_LevelTileContainer.Xmn <= Item.X) && (Item.X <= _LevelTileContainer.Xmx)) && ((_LevelTileContainer.Ymn <= Item.Y) && (Item.Y <= _LevelTileContainer.Ymx)))) {
							//. insert tile by increasing time order
							TLevelTile LevelTile = new TLevelTile();
							LevelTile.Level = L;
							LevelTile.Tile = Item;
			        		boolean flInserted = false;
							int TLS = TileList.size();
			    			for (int J = 0; J < TLS; J++)
			    				if (Item.AccessTime() < TileList.get(J).Tile.AccessTime()) {
			    					TileList.add(J,LevelTile);
			    					flInserted = true;
			    					break; //. >
			    				}
			    			if (!flInserted)
			    				TileList.add(LevelTile);
						}
					}
					else 
						Levels[L].RemoveTile(Item);
    			//.
				if (Canceller != null)
					Canceller.Check();
			}
			//.
			if (flLayIsVisible) {
    			_LevelTileContainer.Level--;
    			_LevelTileContainer.Xmn >>= 1; 
    			_LevelTileContainer.Ymn >>= 1; 
    			_LevelTileContainer.Xmx >>= 1; 
    			_LevelTileContainer.Ymx >>= 1; 
			}
		}
		//. removing ...
		int TLS = TileList.size();
		int RemoveCount = TLS-MaxAvailableTiles;
		for (int I = 0; I < RemoveCount; I++) {
			TLevelTile LevelTile = TileList.get(I);
			Levels[LevelTile.Level].RemoveTile(LevelTile.Tile);
			//.
			if (Canceller != null)
				Canceller.Check();
		}
	}	

	public void ReleaseTiles(TRWLevelTileContainer LevelTileContainer, int VisibleDepth, TCanceller Canceller) throws Exception {
		if (!flInitialized)
			return; //. ->
		int RemoveCount = Levels_TilesCount()-MaxAvailableTiles;
		if (RemoveCount <= 0) 
			return; //. ->
		TRWLevelTileContainer _LevelTileContainer = new TRWLevelTileContainer();
		_LevelTileContainer.AssignContainer(LevelTileContainer);
		if (VisibleDepth > 0) {
			int MaxDepth = ((LevelsCount-1)-_LevelTileContainer.Level);
			if (MaxDepth > 0) {
				if (VisibleDepth > MaxDepth)
					VisibleDepth = MaxDepth;
				//. 
				_LevelTileContainer.Level += VisibleDepth;
				_LevelTileContainer.Xmn <<= VisibleDepth; 
				_LevelTileContainer.Ymn <<= VisibleDepth; 
				_LevelTileContainer.Xmx <<= VisibleDepth; 
				_LevelTileContainer.Ymx <<= VisibleDepth; 
			}
		}
		//.
		ArrayList<TLevelTile> TileList = new ArrayList<TLevelTile>(MaxAvailableTiles);
		for (int L = LevelsCount-1; L >= 0; L--) {
			boolean flLayIsVisible = (L == _LevelTileContainer.Level);
			TTile[] LevelTiles = Levels[L].GetTiles();
			int TilesCount = LevelTiles.length; 
			for (int I = 0; I < TilesCount; I++) {
				TTile Item = LevelTiles[I];
				if ((!Item.flModified) && (!(flLayIsVisible && (((_LevelTileContainer.Xmn <= Item.X) && (Item.X <= _LevelTileContainer.Xmx)) && ((_LevelTileContainer.Ymn <= Item.Y) && (Item.Y <= _LevelTileContainer.Ymx)))))) {
					//. insert tile by increasing time order
					TLevelTile LevelTile = new TLevelTile();
					LevelTile.Level = L;
					LevelTile.Tile = Item;
	        		boolean flInserted = false;
					int TLS = TileList.size();
	    			for (int J = 0; J < TLS; J++)
	    				if (Item.AccessTime() < TileList.get(J).Tile.AccessTime()) {
	    					TileList.add(J,LevelTile);
	    					flInserted = true;
	    					break; //. >
	    				}
	    			if (!flInserted)
	    				TileList.add(LevelTile);
				}
    			//.
				if (Canceller != null)
					Canceller.Check();
			}
			//.
			if (flLayIsVisible) {
    			_LevelTileContainer.Level--;
    			_LevelTileContainer.Xmn >>= 1; 
    			_LevelTileContainer.Ymn >>= 1; 
    			_LevelTileContainer.Xmx >>= 1; 
    			_LevelTileContainer.Ymx >>= 1; 
			}
		}
		//. removing ...
		int TLS = TileList.size();
		for (int I = 0; I < TLS; I++) {
			TLevelTile LevelTile = TileList.get(I);
			Levels[LevelTile.Level].RemoveTile(LevelTile.Tile);
			RemoveCount--;
			if (RemoveCount == 0)
				return; //. ->
			//.
			if (Canceller != null)
				Canceller.Check();
		}
	}	

	public void RemoveAllTiles() {
	    for (int L = 0; L < LevelsCount; L++) 
	    	Levels[L].RemoveTiles();
	}
	
	public void DeleteAllTiles() {
	    for (int L = 0; L < LevelsCount; L++) 
	    	Levels[L].DeleteTiles();
	}

	public void ResetAllTiles() {
		RemoveAllTiles();
	}

	public void DeleteAll() {
		DeleteAllTiles();
	    //.
	    DeleteDataLocally();
	    //.
	    SetAsUninitialized();
	}
}
