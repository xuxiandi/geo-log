package com.geoscope.GeoEye.Space.Functionality;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.Positioner.TPositionerFunctionality;
import com.geoscope.Utils.TDataConverter;

public class TComponentFunctionality extends TFunctionality {
	
	public static TComponentFunctionality Create(TGeoScopeServer pServer, int idTComponent, int idComponent) {
		switch (idTComponent) {
		
		case SpaceDefines.idTPositioner: 
			return (new TPositionerFunctionality(pServer,idComponent)); //. ->
		
		default:
			return null; //. ->
		}
	}
	
	private TGeoScopeServer Server;
	//.
	public int idTComponent;
	public int idComponent = 0;
	
	public TComponentFunctionality(TGeoScopeServer pServer, int pidTComponent, int pidComponent) {
		Server = pServer;
		//.
		idTComponent = pidTComponent;
		idComponent = pidComponent;
	}
	
	public TXYCoord GetVisualizationPosition() throws IOException {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "Functionality"+"/"+"ComponentVisualizationData.dat";
		//. add command parameters
		synchronized (this) {
			URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(idTComponent)+","+Integer.toString(idComponent);
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
