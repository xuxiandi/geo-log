package com.geoscope.GeoEye.Space.TypesSystem.GeoSpace;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.TGeoCoord;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.Utils.TDataConverter;

public class TGeoSpaceFunctionality extends TComponentFunctionality {

	public TGeoSpaceFunctionality(TTypeFunctionality pTypeFunctionality, int pidComponent) {
		super(pTypeFunctionality,pidComponent);
	}

	public TGeoCoord ConvertXYCoordinatesToGeo(double X, double Y) throws Exception { 
		TGeoCoord C = new TGeoCoord();
		//.
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(TypeFunctionality.idType) + "/" + "Co" + "/" + Integer.toString(idComponent) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "4"/* command version */+ "," + Double.toString(X) + "," + Double.toString(Y);
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
					byte[] Data = new byte[2 * 8/* SizeOf(Double) */];
					int Size = in.read(Data);
					if (Size != Data.length)
						throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TGeoCoord();
					int Idx = 0;
					C.Latitude = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
					Idx += 8;
					C.Longitude = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
		return C;
	}

	public TXYCoord ConvertGeoCoordinatesToXY(int DatumID, double Latitude, double Longitude) throws Exception { 
		TXYCoord C = new TXYCoord();
		//.
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(TypeFunctionality.idType) + "/" + "Co" + "/" + Integer.toString(idComponent) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "1"/* command version */+ "," + Integer.toString(DatumID) + "," + Double.toString(Latitude) + "," + Double.toString(Longitude);
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
					byte[] Data = new byte[2 * 8/* SizeOf(Double) */];
					int Size = in.read(Data);
					if (Size != Data.length)
						throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
					Idx += 8;
					C.Y = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
		return C;
	}

	public TXYCoord ConvertGeoCoordinatesToXY(double Latitude, double Longitude) throws Exception { 
		TXYCoord C = new TXYCoord();
		//.
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(TypeFunctionality.idType) + "/" + "Co" + "/" + Integer.toString(idComponent) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "2"/* command version */+ "," + Double.toString(Latitude) + "," + Double.toString(Longitude);
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
					byte[] Data = new byte[2 * 8/* SizeOf(Double) */];
					int Size = in.read(Data);
					if (Size != Data.length)
						throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
					C = new TXYCoord();
					int Idx = 0;
					C.X = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
					Idx += 8;
					C.Y = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
		} catch (IOException E) {
			throw new Exception(E.getMessage()); //. =>
		}
		return C;
	}
}
