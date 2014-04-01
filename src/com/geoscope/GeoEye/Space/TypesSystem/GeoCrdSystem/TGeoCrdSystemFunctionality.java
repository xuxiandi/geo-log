package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.TNetworkConnection;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations.TCrdSysConvertor;

public class TGeoCrdSystemFunctionality extends TComponentFunctionality {

	public TGeoCrdSystemFunctionality(TTypeFunctionality pTypeFunctionality, int pidComponent) {
		super(pTypeFunctionality,pidComponent);
	}
	
	@Override
	protected TComponentData Server_GetData() throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(TypeFunctionality.idType) + "/" + "Co" + "/" + Integer.toString(idComponent) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "0"/* command */+ "," + "1"/* version of data*/;
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
				h = "0" + h;
			sb.append(h);
		}
		URL2 = sb.toString();
		//.
		String URL = URL1 + "/" + URL2 + ".dat";
		//.
		try {
			HttpURLConnection Connection = Server.OpenConnection(URL);
			try {
				InputStream in = Connection.getInputStream();
				try {
					byte[] Data = new byte[Connection.getContentLength()];
					int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
					if (Size != Data.length)
						throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
					//.
					TGeoCrdSystemData Result = new TGeoCrdSystemData();
					Result.FromByteArrayV1(Data,0);
					//.
					return Result; //. ->
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
	}
	
	public TGeoCrdSystemData GetData() throws Exception {
		return (TGeoCrdSystemData)_GetData();
	}
	
	public TCrdSysConvertor CreateCrdSysConvertor() {
		return (new TCrdSysConvertor(this));
	}
}
