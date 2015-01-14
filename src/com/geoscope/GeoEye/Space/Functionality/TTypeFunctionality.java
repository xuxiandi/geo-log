package com.geoscope.GeoEye.Space.Functionality;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TTypeFunctionality extends TFunctionality {
	
	public TGeoScopeServer Server;
	//.
	public TTypeSystem TypeSystem;
	//.
	public int idType;
	
	public TTypeFunctionality(TGeoScopeServer pServer, TTypeSystem pTypeSystem) {
		Server = pServer;
		//.
		TypeSystem = pTypeSystem;
		//.
		idType = TypeSystem.idType;
	}
	
	public TTypeFunctionality(TTypeSystem pTypeSystem) {
		this(null, pTypeSystem);
	}
	
	public TTypesSystem TypesSystem() {
		return TypeSystem.TypesSystem;
	}
	
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return null;
	}
	
	public void DestroyInstance(long idComponent) throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "Functionality"+"/"+"Components.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(idType)+","+Long.toString(idComponent);
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
			//. response
            int response = HttpConnection.getResponseCode();
            if (response != HttpURLConnection.HTTP_OK) {
				String ErrorMessage = HttpConnection.getResponseMessage();
				byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
				ErrorMessage = new String(ErrorMessageBA,"windows-1251");
            	throw new IOException(ErrorMessage); //. =>
            }
		}
		finally {
			HttpConnection.disconnect();
		}
	}
}
