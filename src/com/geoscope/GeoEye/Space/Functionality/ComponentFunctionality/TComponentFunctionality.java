package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.content.Context;
import android.content.Intent;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.TFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem.TContextCache;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TComponentFunctionality extends TFunctionality {
	
	public static final int COMPONENTDATA_SOURCE_SERVER 	= 1;
	public static final int COMPONENTDATA_SOURCE_CONTEXT 	= 2;
	public static final int COMPONENTDATA_SOURCE_THIS 		= 4;
	
	public static class TPropsPanel {
		
		public int	idTComponent;
		public long idComponent;
		//
		public Intent PanelActivity;
		
		public TPropsPanel(int pidTComponent, long pidComponent, Intent pPanelActivity) {
			idTComponent = pidTComponent;
			idComponent = pidComponent;
			PanelActivity = pPanelActivity;
		}
	}
	
	
	public TTypeSystem TypeSystem = null;
	public TTypeFunctionality TypeFunctionality = null;
	public long idComponent = 0;
	//.
	public TGeoScopeServer Server;
	//.
	public int ComponentDataSource = 0;
	
	public TComponentFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
		TypeFunctionality = pTypeFunctionality;
		TypeFunctionality.AddRef();
		//.
		idComponent = pidComponent;
		//.
		TypeSystem = TypeFunctionality.TypeSystem; 
		Server = TypeFunctionality.Server;
		//.
		ComponentDataSource = (COMPONENTDATA_SOURCE_SERVER | COMPONENTDATA_SOURCE_CONTEXT | COMPONENTDATA_SOURCE_THIS);
	}
	
	@Override
	public void Destroy() {
		if (TypeFunctionality != null) {
			TypeFunctionality.Release();
			TypeFunctionality = null;
		}
	}
	
	public TTypesSystem TypesSystem() {
		return TypeFunctionality.TypeSystem.TypesSystem;
	}
	
	public int idTComponent() {
		return TypeFunctionality.idType;
	}
	
	public TComponentData Context_GetData() {
		TContextCache ContextCache = TypeSystem.ContextCache;
		if (ContextCache == null)
			return null; //. ->
		return ContextCache.GetItem(idComponent);
	}
	
	protected void Context_SaveData(TComponentData Data) throws IOException {
		TContextCache ContextCache = TypeSystem.ContextCache;
		if (ContextCache == null)
			return; //. ->
		ContextCache.Add(Data);
		ContextCache.Save();
	}
	
	protected TComponentData Server_GetData() throws Exception {
		return null;
	}
	
	protected TComponentData _GetData() throws Exception {
		TComponentData Data;
		if ((ComponentDataSource & TComponentFunctionality.COMPONENTDATA_SOURCE_CONTEXT) > 0) {
			Data = Context_GetData();
			if (Data != null)
				return Data; //. ->
		}
		if ((ComponentDataSource & TComponentFunctionality.COMPONENTDATA_SOURCE_SERVER) > 0) {
			Data = Server_GetData();
			if (Data != null) {
				Context_SaveData(Data);
				//.
				return Data; //. ->
			}
		}
		return null; //. ->
	}
	
	public int ParseFromXMLDocument(byte[] XML) throws Exception {
		return 0;
	}
	
	public byte[] GetDataDocument(int DataModel, int DataType, String DataParams, int Version) throws Exception {
		byte[] Result = Context_GetDataDocument(DataModel,DataType,DataParams, Version);
		if (Result != null)
			return Result; //. ->
		//. get from the server
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/* URLProtocolVersion */+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "Functionality"+"/"+"ComponentDataDocument.dat";
		//. add command parameters
		int WithComponentsFlag = 0;
		if (DataParams == null)
			URL2 = URL2+"?"+"1"/* command version */+","+Integer.toString(idTComponent())+","+Long.toString(idComponent)+","+Integer.toString(DataModel)+","+Integer.toString(DataType)+","+Integer.toString(WithComponentsFlag);
		else
			URL2 = URL2+"?"+"2"/* command version */+","+Integer.toString(idTComponent())+","+Long.toString(idComponent)+","+Integer.toString(DataModel)+","+Integer.toString(DataType)+","+DataParams+","+Integer.toString(WithComponentsFlag);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
		//. encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0"+h;
			sb.append(h);
		}
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		//.
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0) 
					return null; // . ->
				byte[] Data = new byte[RetSize];
				int Size;
				int SummarySize = 0;
				int ReadSize;
				while (SummarySize < Data.length) {
					ReadSize = Data.length-SummarySize;
					Size = in.read(Data, SummarySize, ReadSize);
					if (Size <= 0)
						throw new Exception(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); // => 
					//.
					SummarySize += Size;
				}
				//.
				String _DocumentFolder = TypeSystem.Context_GetFolder()+"/"+Long.toString(idComponent)+TTypeSystem.Context_Item_FolderSuffix+"/"+TTypeSystem.Context_Item_Folder_DataDocumentFolder+"/"+Integer.toString(DataModel)+"/"+Integer.toString(DataType)+"/"+"V"+Integer.toString(Version);
				File F = new File(_DocumentFolder);
				F.mkdirs();
				String DDF;
				if (DataParams != null)
					DDF = _DocumentFolder+"/"+DataParams;
				else
					DDF = _DocumentFolder+"/"+TTypeSystem.Context_Item_Folder_DataDocumentDefaultFileName;
				F = new File(DDF);
				FileOutputStream FOS = new FileOutputStream(F);
				try {
					FOS.write(Data);
				}
				finally {
					FOS.close();
				}
				return Data; //. ->
			} finally {
				in.close();
			}
		} finally {
			Connection.disconnect();
		}
	}
	
	public byte[] Context_GetDataDocument(int DataModel, int DataType, String DataParams, int Version) throws Exception {
		String _DocumentFolder = TypeSystem.Context_GetFolder()+"/"+Long.toString(idComponent)+TTypeSystem.Context_Item_FolderSuffix+"/"+TTypeSystem.Context_Item_Folder_DataDocumentFolder+"/"+Integer.toString(DataModel)+"/"+Integer.toString(DataType)+"/"+"V"+Integer.toString(Version);
		String DDF;
		if (DataParams != null)
			DDF = _DocumentFolder+"/"+DataParams;
		else
			DDF = _DocumentFolder+"/"+TTypeSystem.Context_Item_Folder_DataDocumentDefaultFileName;
		File F = new File(DDF);
		if (!F.exists())
			return null; //. ->
		byte[] Result = new byte[(int)F.length()];
		FileInputStream FIS = new FileInputStream(F);
		try {
			FIS.read(Result);
		}
		finally {
			FIS.close();
		}
		return Result; //. ->
	}
	
	public TPropsPanel TPropsPanel_Create(Context context) throws Exception {
		return null;
	}
	
	public TXYCoord GetVisualizationPosition() throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "Functionality"+"/"+"ComponentVisualizationData.dat";
		//. add command parameters
		synchronized (this) {
			URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(TypeFunctionality.idType)+","+Long.toString(idComponent);
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
		//.
		HttpURLConnection HttpConnection = Server.OpenConnection(URL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[4/*SizeOf(RC)*/];
				int Size = in.read(Data);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
				int RC = TDataConverter.ConvertLEByteArrayToInt32(Data,0); 
				if (RC < 0)
					return null; //. ->
				Data = new byte[2*8/*SizeOf(Double)*/];
				Size = in.read(Data);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
				int Idx = 0;
				TXYCoord Result = new TXYCoord();
				Result.X = TDataConverter.ConvertLEByteArrayToDouble(Data,Idx); Idx+=8;
				Result.Y = TDataConverter.ConvertLEByteArrayToDouble(Data,Idx); Idx+=8;
				//.
				return Result; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
}
