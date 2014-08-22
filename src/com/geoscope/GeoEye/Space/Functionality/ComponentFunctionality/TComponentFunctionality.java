package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

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
	
	public static TComponentFunctionality Create(TGeoScopeServer pServer, int idTComponent, int idComponent) {
		TTypeFunctionality TypeFunctionality = TTypeFunctionality.Create(pServer, idTComponent);
		if (TypeFunctionality == null)
			return null; //. ->
		return TypeFunctionality.TComponentFunctionality_Create(idComponent); //. ->
	}
	
	public TTypeSystem TypeSystem = null;
	public TTypeFunctionality TypeFunctionality = null;
	public int idComponent = 0;
	//.
	public TGeoScopeServer Server;
	//.
	public int ComponentDataSource = 0;
	
	public TComponentFunctionality(TTypeFunctionality pTypeFunctionality, int pidComponent) {
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
	
	public TComponentFunctionality(TGeoScopeServer pServer, int pidTComponent, int pidComponent) {
		TypeFunctionality = (new TTypeFunctionality(pServer,pidTComponent));
		TypeFunctionality.AddRef();
		//.
		idComponent = pidComponent;
		//.
		Server = TypeFunctionality.Server;
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
	
	public TXYCoord GetVisualizationPosition() throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "Functionality"+"/"+"ComponentVisualizationData.dat";
		//. add command parameters
		synchronized (this) {
			URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(TypeFunctionality.idType)+","+Integer.toString(idComponent);
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
				int RC = TDataConverter.ConvertBEByteArrayToInt32(Data,0); 
				if (RC < 0)
					return null; //. ->
				Data = new byte[2*8/*SizeOf(Double)*/];
				Size = in.read(Data);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
				int Idx = 0;
				TXYCoord Result = new TXYCoord();
				Result.X = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
				Result.Y = TDataConverter.ConvertBEByteArrayToDouble(Data,Idx); Idx+=8;
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
	
	public int ParseFromXMLDocument(byte[] XML) throws Exception {
		return 0;
	}
}
