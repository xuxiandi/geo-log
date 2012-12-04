package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.graphics.Canvas;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTimeLimit.TimeIsExpiredException;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.GeoLog.Utils.TUpdater;

public class TTileImagery {

	public static final String ImageryFolder = TReflector.TypesSystemContextFolder+"/"+"TileImagery";
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
	public int 					ServerType = SERVERTYPE_DATASERVER;
	public TTileImageryServer 	Server;
	//.
	public TTileImageryData Data;
	//.
	private TTileServerProviderCompilation[] _ActiveCompilation;
	//.
	private TTileLimit TileRestoreLimit = new TTileLimit(MaxAvailableTiles);
	private TTileLimit TileCompositionLimit = new TTileLimit(TileCompositionMaxSize);
	
	public TTileImagery(TReflector pReflector, String pCompilation) throws Exception {
		Reflector = pReflector;
		//.
		Data = new TTileImageryData();
		//.
		_ActiveCompilation = null;
		if (pCompilation != null) {
			TTileServerProviderCompilationDescriptors Descriptors = new TTileServerProviderCompilationDescriptors(pCompilation);
			_ActiveCompilation = new TTileServerProviderCompilation[Descriptors.Items.length];  
			for (int I = 0; I < _ActiveCompilation.length; I++)	
				_ActiveCompilation[I] = new TTileServerProviderCompilation(Reflector,this,Descriptors.Items[I],(int)(MaxAvailableTiles/_ActiveCompilation.length));
		}
		//.
		Server = null;
	}
	
	public synchronized void Destroy() throws IOException {
		if (Server != null) {
			Server.Destroy();
			Server = null;
		}
		//.
		if (_ActiveCompilation != null) {
			for (int I = 0; I < _ActiveCompilation.length; I++)	
				_ActiveCompilation[I].Destroy();
			_ActiveCompilation = null;
		}
	}
	
	public void LoadDataFromServer() throws Exception {
		String URL1 = Reflector.ServerAddress;
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
		HttpURLConnection HttpConnection = Reflector.OpenHttpConnection(URL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				int RetSize = HttpConnection.getContentLength();
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
			HttpConnection.disconnect();
		}
		//.
		Data.FromByteArrayAndSave(_Data);
	}
	
	public void SetActiveCompilation(TTileServerProviderCompilationDescriptors pDescriptors) {
		TTileServerProviderCompilation[] AC = new TTileServerProviderCompilation[pDescriptors.Items.length];  
		for (int I = 0; I < AC.length; I++)	
			AC[I] = new TTileServerProviderCompilation(Reflector,this,pDescriptors.Items[I],(int)(MaxAvailableTiles/AC.length));
		//.
		TTileServerProviderCompilation[] LastAC;
		synchronized (this) {
			LastAC = _ActiveCompilation;
			_ActiveCompilation = AC;
		}
		//.
		if (LastAC != null) 
			for (int I = 0; I < LastAC.length; I++)	
				LastAC[I].Destroy();
	}
    
	public synchronized TTileServerProviderCompilation[] ActiveCompilation() {
		return _ActiveCompilation;
	}

	public synchronized TTileServerProviderCompilation ActiveCompilation_GetUserDrawableItem() throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
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

	public synchronized TTileServerProviderCompilationDescriptors ActiveCompilationDescriptors() {
		if (_ActiveCompilation == null)
			return null; //. ->
		return new TTileServerProviderCompilationDescriptors(_ActiveCompilation);
	}

	public void ActiveCompilation_CheckInitialized() throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].CheckInitialized();
	}
	
	public TRWLevelTileContainer[] ActiveCompilation_GetLevelTileRange(TReflectionWindowStruc RW) {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) {
			TRWLevelTileContainer[] Result = new TRWLevelTileContainer[ATSPC.length]; 
			for (int I = 0; I < ATSPC.length; I++)	
				Result[I] = ATSPC[I].GetLevelTileRange(RW); 
			return Result; //. ->
		}
		else
			return null;
	}
	public void ActiveCompilation_RestoreTiles(TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller, TUpdater Updater) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if ((ATSPC != null) && (ATSPC.length > 0) && (ATSPC.length == LevelTileContainers.length)) {
			int Limit = 2*((LevelTileContainers[0].Xmx-LevelTileContainers[0].Xmn+1)*(LevelTileContainers[0].Ymx-LevelTileContainers[0].Ymn+1));
			TileRestoreLimit.Value = Limit;
			for (int I = 0; I < ATSPC.length; I++)	
				if (TileRestoreLimit.Value > 0)
					ATSPC[I].RestoreTiles(LevelTileContainers[I], TileRestoreLimit, Canceller,Updater);
		}
	}
	
	public void ActiveCompilation_PrepareTiles(TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller, TUpdater Updater) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if ((ATSPC != null) && (ATSPC.length == LevelTileContainers.length)) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].PrepareTiles(LevelTileContainers[I], Canceller,Updater);
	}
	
	public void ActiveCompilation_PrepareUpLevelsTiles(TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller, TUpdater Updater) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if ((ATSPC != null) && (ATSPC.length == LevelTileContainers.length)) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].PrepareUpLevelsTiles(LevelTileContainers[I], Canceller,Updater);
	}
	
	public void ActiveCompilation_ReflectionWindow_DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas, TTimeLimit TimeLimit) throws TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) {
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++) {	
				ATSPC[I].ReflectionWindow_DrawOnCanvas(RW, canvas, (I == 0), TileCompositionLimit,TimeLimit);
			}
		}
	}
	
	public void ActiveCompilation_ReflectionWindow_DrawOnCanvasTo(TReflectionWindowStruc RW, Canvas canvas, TTimeLimit TimeLimit, TTileServerProviderCompilation ToCompilation) throws TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) {
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++) {
				if (ATSPC[I] == ToCompilation)
					return; //. ->
				ATSPC[I].ReflectionWindow_DrawOnCanvas(RW, canvas, (I == 0), TileCompositionLimit,TimeLimit);
			}
		}
	}
	
	public void ActiveCompilation_ReflectionWindow_DrawOnCanvasFrom(TReflectionWindowStruc RW, Canvas canvas, TTimeLimit TimeLimit, TTileServerProviderCompilation FromCompilation) throws TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) {
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++) {
				if (ATSPC[I] == FromCompilation) {
					for (int J = I+1; J < ATSPC.length; J++) 
						ATSPC[J].ReflectionWindow_DrawOnCanvas(RW, canvas, (J == 0), TileCompositionLimit,TimeLimit);
					return; //. ->
				}
			}
		}
	}
	
	public void ActiveCompilation_ReflectionWindow_PrepareCurrentLevelTileContainer(TReflectionWindowStruc RW) {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_PrepareCurrentLevelTileContainer(RW);
	}
	
	public void ActiveCompilation_ReflectionWindow_CurrentLevelTileContainer_DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas, TTimeLimit TimeLimit) throws TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_CurrentLevelTileContainer_DrawOnCanvas(RW, canvas, TimeLimit);
	}
	
	public void ActiveCompilation_ReflectionWindow_PrepareCurrentComposition(TReflectionWindowStruc RW, TCanceller Canceller) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) { 
			TileCompositionLimit.Reset();
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_PrepareCurrentComposition(RW, TileCompositionLimit, Canceller);
		}
	}
	
	public void ActiveCompilation_ReflectionWindow_CurrentComposition_DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas, TTimeLimit TimeLimit) throws TimeIsExpiredException {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].ReflectionWindow_CurrentComposition_DrawOnCanvas(RW, canvas, TimeLimit);
	}
	
	public void ActiveCompilation_RemoveOldTiles(TRWLevelTileContainer[] LevelTileContainers, TCanceller Canceller) throws Exception {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if ((ATSPC != null) && (ATSPC.length == LevelTileContainers.length)) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].RemoveOldTiles(LevelTileContainers[I], Canceller);
		//.
		System.gc();
	}	

	public void ActiveCompilation_RemoveAllTiles() {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].RemoveAllTiles();
	}
	
	public void ActiveCompilation_DeleteAllTiles() {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)	
				ATSPC[I].DeleteAllTiles();
	}
	
	public void ActiveCompilation_ResetHistoryTiles() {
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++)
				if (ATSPC[I].flHistoryEnabled)
					ATSPC[I].ResetAllTiles();
	}
	
	public double ActiveCompilation_CommitModifiedTiles(int SecurityFileID, boolean flReset) throws Exception {
		double Result = Double.MIN_VALUE;
		TTileServerProviderCompilation[] ATSPC = ActiveCompilation();
		if (ATSPC != null) 
			for (int I = 0; I < ATSPC.length; I++) {	
				double R = ATSPC[I].CommitModifiedTiles(SecurityFileID,flReset);
				if (R > Result)
					Result = R;
			}
		return Result;
	}	
}
