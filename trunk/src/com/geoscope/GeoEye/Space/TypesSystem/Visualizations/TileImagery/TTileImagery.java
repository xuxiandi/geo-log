package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.Classes.MultiThreading.TUpdater;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization.TSystemTTileServerVisualization;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImageryDataServer.TTilesPlace;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TTileImagery {

	public static final String ImageryFolder = TSystemTTileServerVisualization.ContextFolder;
	public static final int MaxAvailableTiles = 16;
	public static final int TileCompositionMaxSize = 65535;
	
	public static final int SERVERTYPE_HTTPSERVER = 0;
	public static final int SERVERTYPE_DATASERVER = 1;
	
	public static class TTileServerProviderCompilationDescriptor {
		public int SID;
		public int PID;
		public int CID;
		//.
		public boolean flSelected = false;
		
		public TTileServerProviderCompilationDescriptor(int pSID, int pPID, int pCID) {
			SID = pSID;
			PID = pPID;
			CID = pCID;
		}
		
		public TTileServerProviderCompilationDescriptor(String S) {
			FromString(S);
		}
		
		public String ToString() {
			return Integer.toString(SID)+"."+Integer.toString(PID)+"."+Integer.toString(CID);
		}
		
		public void FromString(String S) {
			String[] SA = S.split("\\.");
			SID = Integer.parseInt(SA[0]);
			PID = Integer.parseInt(SA[1]);
			CID = Integer.parseInt(SA[2]);
		}
		
		public boolean Equals(TTileServerProviderCompilationDescriptor D) {
			return ((SID == D.SID) && (PID == D.PID) && (CID == D.CID));
		}
	}
	
	public static class TTileServerProviderCompilationDescriptors {
		
		public TTileServerProviderCompilationDescriptor[] Items = new TTileServerProviderCompilationDescriptor[0];
		
		public TTileServerProviderCompilationDescriptors(TTileServerProviderCompilation[] pItems) {
			Items = new TTileServerProviderCompilationDescriptor[pItems.length];
			for (int I = 0; I < Items.length; I++) 
				Items[I] = pItems[I].Descriptor;
		}
		
		public TTileServerProviderCompilationDescriptors(String S) {
			FromString(S);
		}

		public TTileServerProviderCompilationDescriptors(int Count) {
			Items = new TTileServerProviderCompilationDescriptor[Count];
		}

		public String ToString() {
			StringBuilder SB = new StringBuilder();
			for (int I = 0; I < Items.length; I++) 
				if (I < (Items.length-1))
					SB.append(Items[I].ToString()+",");
				else
					SB.append(Items[I].ToString());
			return SB.toString();
		}
		
		public void FromString(String S) {
			if ((S == null) || (S.equals(""))) {
				Items = new TTileServerProviderCompilationDescriptor[0];
				return; //. ->
			}
			String[] SA = S.split(",");
			Items = new TTileServerProviderCompilationDescriptor[SA.length];
			for (int I = 0; I < Items.length; I++) 
				Items[I] = new TTileServerProviderCompilationDescriptor(SA[I]);
		}
		
		public boolean ItemExists(TTileServerProviderCompilationDescriptor D) {
			if (Items == null)
				return false; //. ->
			for (int I = 0; I < Items.length; I++)
				if (Items[I].Equals(D)) 
					return true; //. ->
			return false;
		}		
	}
	
	public TReflector Reflector;
	//.
	private boolean flInitialized = false;
	//.
	public int ServerType = SERVERTYPE_DATASERVER;
	//.
	public TTileImageryData Data;
	//.
	private TTileServerProviderCompilation[] _ActiveCompilationSet;
	//.
	private TTileLimit TileRestoreLimit = new TTileLimit(MaxAvailableTiles);
	private TTileLimit TileCompositionLimit = new TTileLimit(TileCompositionMaxSize);
	
	public TTileImagery(TReflector pReflector, String pCompilation) throws Exception {
		Reflector = pReflector;
		//.
		Data = new TTileImageryData();
		//.
		_ActiveCompilationSet = null;
		if (pCompilation != null) {
			TTileServerProviderCompilationDescriptors Descriptors = new TTileServerProviderCompilationDescriptors(pCompilation);
			_ActiveCompilationSet = new TTileServerProviderCompilation[Descriptors.Items.length];  
			for (int I = 0; I < _ActiveCompilationSet.length; I++)	
				_ActiveCompilationSet[I] = new TTileServerProviderCompilation(this,Descriptors.Items[I],(int)(MaxAvailableTiles/_ActiveCompilationSet.length));
		}
	}
	
	public synchronized void Destroy() throws IOException {
		if (_ActiveCompilationSet != null) {
			for (int I = 0; I < _ActiveCompilationSet.length; I++)	
				_ActiveCompilationSet[I].Destroy();
			_ActiveCompilationSet = null;
		}
	}
	
	public void CheckInitialized() throws Exception {
		if (!flInitialized)
			Initialize();
		else {
			Data.CheckInitialized();
			//.
			ActiveCompilationSet_CheckInitialized();
		}
	}
	
	public void Initialize() throws Exception {
		Data.CheckInitialized();
		//.
		ValidateServerType();
		//.
		ActiveCompilationSet_CheckInitialized();
		//.
		flInitialized = true;
	}
	
	private void SetAsUninitialized() {
		flInitialized = false;
	}
	
	private boolean IsOffline() {
		return Reflector.flOffline;
	}
	
	private void HttpServer_LoadDataFromServer() throws Exception {
		String URL1 = Reflector.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Reflector.User.UserID);
		String URL2 = "TileServerData.dat";
		//. add command parameters
		URL2 = URL2+"?"+"0"/*command version*/;
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
		byte[] _Data;
		HttpURLConnection Connection = Reflector.Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0)
					return; //. ->
				_Data = new byte[RetSize];
	            int Size;
	            int SummarySize = 0;
	            int ReadSize;
	            while (SummarySize < _Data.length)
	            {
	                ReadSize = _Data.length-SummarySize;
	                Size = in.read(_Data,SummarySize,ReadSize);
	                if (Size <= 0) throw new Exception(Reflector.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
	                SummarySize += Size;
	            }
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		//.
		Data.FromByteArrayAndSave(_Data);
	}
	
	private void DataServer_LoadDataFromServer() throws Exception {
		TGeoScopeServerInfo.TInfo ServersInfo = Reflector.Server.Info.GetInfo();
		TTileImageryDataServer IDS = new TTileImageryDataServer(Reflector, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, Reflector.User.UserID, Reflector.User.UserPassword);
		try {
			Data.FromByteArrayAndSave(IDS.GetData());
		}
		finally {
			IDS.Destroy();
		}
	}
	
	public void LoadDataFromServer() throws Exception {
		switch (ServerType) {
		
		case SERVERTYPE_HTTPSERVER:
			HttpServer_LoadDataFromServer();
			break; //. >
			
		case SERVERTYPE_DATASERVER:
			DataServer_LoadDataFromServer();
			break; //. >
		}
	}
	
	private void ValidateServerType() throws Exception {
		if (IsOffline())
			return; //. ->
		switch (ServerType) {
		
		case SERVERTYPE_DATASERVER:
			TGeoScopeServerInfo.TInfo ServersInfo = Reflector.Server.Info.GetInfo();
			if (!ServersInfo.IsSpaceDataServerValid())
				ServerType = SERVERTYPE_HTTPSERVER;
			break; //. >
		}
	}
	
	public void SetActiveCompilationSet(TTileServerProviderCompilationDescriptors pDescriptors) {
		TTileServerProviderCompilation[] AC = new TTileServerProviderCompilation[pDescriptors.Items.length];  
		for (int I = 0; I < AC.length; I++)	
			AC[I] = new TTileServerProviderCompilation(this,pDescriptors.Items[I],(int)(MaxAvailableTiles/AC.length));
		//.
		TTileServerProviderCompilation[] LastAC;
		synchronized (this) {
			LastAC = _ActiveCompilationSet;
			_ActiveCompilationSet = AC;
		}
		SetAsUninitialized();
		//.
		if (LastAC != null) 
			for (int I = 0; I < LastAC.length; I++)	
				LastAC[I].Destroy();
	}
    
	public synchronized TTileServerProviderCompilation[] ActiveCompilationSet() {
		return _ActiveCompilationSet;
	}

	public synchronized TTileServerProviderCompilation ActiveCompilationSet_Get0Item() throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) {
			if (!ATSPC[0].flInitialized)
				throw new Exception(Reflector.getString(R.string.STileImageryIsNotInitialized)); //. =>
			return ATSPC[0]; //. ->			
		}
		else
			return null;
	}

	public synchronized TTileServerProviderCompilation ActiveCompilationSet_GetUserDrawableItem() throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) {
			for (int I = 0; I < ATSPC.length; I++) {
				if (!ATSPC[I].flInitialized)
					throw new Exception(Reflector.getString(R.string.STileImageryIsNotInitialized)); //. =>
				if (ATSPC[I].flUserDrawable)
					return ATSPC[I]; //. ->
			}
			return null; //. ->
		}
		else
			return null;
	}

	public synchronized TTileServerProviderCompilationDescriptors ActiveCompilationSet_Descriptors() {
		if (_ActiveCompilationSet == null)
			return null; //. ->
		return new TTileServerProviderCompilationDescriptors(_ActiveCompilationSet);
	}

	private void ActiveCompilationSet_CheckInitialized() throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].CheckInitialized();
	}
	
	public TRWLevelTileContainer[] ActiveCompilationSet_GetLevelTileRange(TReflectionWindowStruc RW) {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) {
			TRWLevelTileContainer[] Result = new TRWLevelTileContainer[ATSPC.length]; 
			for (int I = 0; I < ATSPC.length; I++)	
				Result[I] = ATSPC[I].GetLevelTileRange(RW); 
			return Result; //. ->
		}
		else
			return null;
	}
	public void ActiveCompilationSet_RestoreTiles(TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller, TUpdater Updater) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if ((ATSPC != null) && (ATSPC.length > 0) && (ATSPC.length == LevelTileContainers.length)) {
			int Limit = 0;
			if (LevelTileContainers[0] != null)
				Limit = 2*((LevelTileContainers[0].Xmx-LevelTileContainers[0].Xmn+1)*(LevelTileContainers[0].Ymx-LevelTileContainers[0].Ymn+1));
			TileRestoreLimit.Value = Limit;
			for (int I = 0; I < ATSPC.length; I++)	
				if (TileRestoreLimit.Value > 0)
					ATSPC[I].RestoreTiles(LevelTileContainers[I], TileRestoreLimit, Canceller,Updater);
		}
	}
	
	public void ActiveCompilationSet_PrepareTiles(TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller, TUpdater Updater, TProgressor Progressor) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if ((ATSPC != null) && (ATSPC.length == LevelTileContainers.length)) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].PrepareTiles(LevelTileContainers[I], Canceller,Updater,Progressor);
	}
	
	public void ActiveCompilationSet_PrepareUpLevelsTiles(TRWLevelTileContainer[] LevelTileContainers, int LevelStep, TCanceller Canceller, TUpdater Updater) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if ((ATSPC != null) && (ATSPC.length == LevelTileContainers.length)) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].PrepareUpLevelsTiles(LevelTileContainers[I], LevelStep, Canceller,Updater);
	}
	
	public void ActiveCompilationSet_ReflectionWindow_DrawOnCanvas(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit) throws CancelException, TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) {
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++) {	
				ATSPC[I].ReflectionWindow_DrawOnCanvas(RW, pImageID,canvas,paint,transitionpaint, (I == 0), Canceller, TileCompositionLimit,TimeLimit);
			}
		}
	}
	
	public void ActiveCompilationSet_ReflectionWindow_DrawOnCanvasTo(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit, TTileServerProviderCompilation ToCompilation) throws CancelException, TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) {
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++) {
				if (ATSPC[I] == ToCompilation)
					return; //. ->
				ATSPC[I].ReflectionWindow_DrawOnCanvas(RW, pImageID,canvas,paint,transitionpaint, (I == 0), Canceller, TileCompositionLimit,TimeLimit);
			}
		}
	}
	
	public void ActiveCompilationSet_ReflectionWindow_DrawOnCanvasFrom(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit, TTileServerProviderCompilation FromCompilation) throws CancelException, TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) {
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++) {
				if (ATSPC[I] == FromCompilation) {
					for (int J = I+1; J < ATSPC.length; J++) 
						ATSPC[J].ReflectionWindow_DrawOnCanvas(RW, pImageID,canvas,paint,transitionpaint, (J == 0), Canceller, TileCompositionLimit,TimeLimit);
					return; //. ->
				}
			}
		}
	}
	
	public void ActiveCompilationSet_ReflectionWindow_PrepareResultLevelTileContainer(TReflectionWindowStruc RW) {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_PrepareResultLevelTileContainer(RW);
	}
	
	public void ActiveCompilationSet_ReflectionWindow_PrepareResultLevelTileContainer(TRWLevelTileContainer[] LevelTileContainers) {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if ((ATSPC != null) && (ATSPC.length == LevelTileContainers.length)) {
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].SetResultLevelTileContainer(LevelTileContainers[I]);
		}
	}
	
	public void ActiveCompilationSet_ReflectionWindow_ResultLevelTileContainer_DrawOnCanvas(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit) throws CancelException, TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_ResultLevelTileContainer_DrawOnCanvas(RW, pImageID,canvas,paint,transitionpaint, Canceller, TimeLimit);
	}
	
	@SuppressWarnings("unused")
	private void ActiveCompilationSet_ReflectionWindow_PrepareResultComposition(TReflectionWindowStruc RW, TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) { 
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_PrepareResultComposition(RW, LevelTileContainers[I], TileCompositionLimit, Canceller);
		}
	}
	
	@SuppressWarnings("unused")
	private void ActiveCompilationSet_ReflectionWindow_PrepareResultComposition(TReflectionWindowStruc RW, TCanceller Canceller) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) { 
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_PrepareResultComposition(RW, TileCompositionLimit, Canceller);
		}
	}
	
	@SuppressWarnings("unused")
	private void ActiveCompilationSet_ReflectionWindow_ResultComposition_DrawOnCanvas(TReflectionWindowStruc RW, int pImageID, Canvas canvas, Paint paint, Paint transitionpaint, TCanceller Canceller, TTimeLimit TimeLimit) throws CancelException, TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_ResultComposition_DrawOnCanvas(RW, pImageID, canvas, paint,transitionpaint, Canceller, TimeLimit);
	}
	
	public void ActiveCompilationSet_RemoveOldTiles(TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller) throws Exception {
		int MaxVisibleDepth = 1;
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if ((ATSPC != null) && (ATSPC.length == LevelTileContainers.length)) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].RemoveOldTiles(LevelTileContainers[I], MaxVisibleDepth, Canceller);
		//.
		TGeoLogApplication.Instance().GarbageCollector.Start();
	}	

	public void ActiveCompilationSet_RemoveAllTiles() {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].RemoveAllTiles();
	}
	
	public void ActiveCompilationSet_DeleteAllTiles() {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++) 	
				ATSPC[I].DeleteAllTiles();
	}
	
	public void ActiveCompilationSet_ResetHistoryTiles() {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)
				if (ATSPC[I].flHistoryEnabled)
					ATSPC[I].ResetAllTiles();
	}
	
	public double ActiveCompilationSet_CommitModifiedTiles(int SecurityFileID, boolean flReSet, double ReSetInterval, TTilesPlace TilesPlace, boolean flEnqueue) throws Exception {
		double Result = Double.MIN_VALUE;
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++) {	
				double R = ATSPC[I].CommitModifiedTiles(SecurityFileID,flReSet,ReSetInterval,TilesPlace, flEnqueue);
				if (R > Result)
					Result = R;
			}
		return Result;
	}	

	public void ActiveCompilationSet_DeleteAll() {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilationSet();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++) 	
				ATSPC[I].DeleteAll();
	}
}
