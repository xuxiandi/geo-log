package com.geoscope.GeoEye.Space.TypesSystem.GeoSpace;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.TGeoCoord;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.TGeoCrdSystemFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations.TCrdSysConvertor;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations.TGeoDatum;

public class TGeoSpaceFunctionality extends TComponentFunctionality {

	public TGeoSpaceFunctionality(TTypeFunctionality pTypeFunctionality, int pidComponent) {
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
					TGeoSpaceData Result = new TGeoSpaceData();
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
	
	public TGeoSpaceData GetData() throws Exception {
		return (TGeoSpaceData)_GetData();
	}
	
	public TGeoCoord Server_ConvertXYCoordinatesToGeo(double X, double Y, int DatumID) throws Exception { 
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(TypeFunctionality.idType) + "/" + "Co" + "/" + Integer.toString(idComponent) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "3"/* command version */+ "," + Double.toString(X) + "," + Double.toString(Y) + "," + Integer.toString(DatumID);
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
					int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
					if (Size != Data.length)
						throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
					//.
					TGeoCoord Result = new TGeoCoord();
					//.
					Result.Datum = DatumID;
					int Idx = 0;
					Result.Latitude = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx); Idx += 8;
					Result.Longitude = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx); Idx += 8;
					Result.Altitude = 0.0;
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

	public TGeoCoord ConvertXYCoordinatesToGeo(double X, double Y, int DatumID) throws Exception {
		TGeoCoord Result;
		//.
		TGeoSpaceData Data = GetData();
		//. get appropriate geo coordinate system
		int idGeoCrdSystem = Data.GetGeoCrdSystemByXY(X,Y);
		if (idGeoCrdSystem == 0)
			throw new Exception("no GeoCrdSystem for current XY-coordinates"); //. =>
		TGeoCrdSystemFunctionality GCF = (TGeoCrdSystemFunctionality)TypesSystem().SystemTGeoCrdSystem.TComponentFunctionality_Create(Server, idGeoCrdSystem);
		try {
			GCF.ComponentDataSource = ComponentDataSource;
			//.
			TCrdSysConvertor CrdSysConvertor = GCF.CreateCrdSysConvertor();
			try {
				if (!CrdSysConvertor.Prepare())
					throw new Exception("there is no data for coordinate conversion"); //. =>
				TGeoCoord Crd = CrdSysConvertor.ConvertXYToGeo(X,Y); //. ->
				//.
				TGeoDatum SourceDatum = TGeoDatum.GetDatumByID(Crd.Datum);
				if (SourceDatum == null)
					throw new Exception("unknown source datum"); //. =>
				TGeoDatum DestinationDatum = TGeoDatum.GetDatumByID(DatumID);
				if (DestinationDatum == null)
					throw new Exception("unknown destination datum"); //. =>
				//. convert incoming coordinates to the GeoSpace Datum coordinates
				Result = SourceDatum.ConvertCoordinatesToDatum(Crd, DestinationDatum);
			}
			finally {
				CrdSysConvertor.Destroy();
			}
		}
		finally {
			GCF.Release();
		}
		if (Result != null)
			return Result; //. ->
		Result = Server_ConvertXYCoordinatesToGeo(X,Y, DatumID);
		return Result; 
	}
	
	public TXYCoord Server_ConvertGeoCoordinatesToXY(int DatumID, double Latitude, double Longitude, double Altitude) throws Exception { 
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */ + "/" + Integer.toString(Server.User.UserID);
		String URL2 = "TypesSystem" + "/" + Integer.toString(TypeFunctionality.idType) + "/" + "Co" + "/" + Integer.toString(idComponent) + "/" + "Data.dat";
		//. add command parameters
		URL2 = URL2 + "?" + "1"/* command version */+ "," + Integer.toString(DatumID) + "," + Double.toString(Latitude) + "," + Double.toString(Longitude) + "," + Double.toString(Altitude);
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
					int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
					if (Size != Data.length)
						throw new IOException(Server.context.getString(R.string.SErrorOfPositionGetting)); //. =>
					TXYCoord Result = new TXYCoord();
					//.
					int Idx = 0;
					Result.X = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx); Idx += 8;
					Result.Y = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx);
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

	public TXYCoord ConvertGeoCoordinatesToXY(int DatumID, double Latitude, double Longitude, double Altitude) throws Exception {
		TXYCoord Result;
		//.
		TGeoDatum SourceDatum = TGeoDatum.GetDatumByID(DatumID);
		if (SourceDatum == null)
			throw new Exception("unknown source datum"); //. =>
		TGeoSpaceData Data = GetData();
		TGeoDatum DestinationDatum = TGeoDatum.GetDatumByID(Data.Datum);
		if (DestinationDatum == null)
			throw new Exception("unknown destination datum"); //. =>
		//. convert incoming coordinates to the GeoSpace Datum coordinates
		TGeoCoord GeoCrd = SourceDatum.ConvertCoordinatesToDatum(new TGeoCoord(SourceDatum.ID,Latitude,Longitude,Altitude), DestinationDatum);
		if (GeoCrd == null)
			throw new Exception("unknown datum transformation"); //. =>
		//. get appropriate geo coordinate system
		int idGeoCrdSystem = Data.GetGeoCrdSystemByLatLong(GeoCrd.Latitude, GeoCrd.Longitude);
		if (idGeoCrdSystem == 0)
			throw new Exception("no GeoCrdSystem for current coordinates"); //. =>
		TGeoCrdSystemFunctionality GCF = (TGeoCrdSystemFunctionality)TypesSystem().SystemTGeoCrdSystem.TComponentFunctionality_Create(Server, idGeoCrdSystem);
		try {
			GCF.ComponentDataSource = ComponentDataSource;
			//.
			TCrdSysConvertor CrdSysConvertor = GCF.CreateCrdSysConvertor();
			try {
				if (!CrdSysConvertor.Prepare())
					throw new Exception("there is no data for coordinate conversion"); //. =>
				Result = CrdSysConvertor.ConvertGeoToXY(GeoCrd.Latitude, GeoCrd.Longitude); //. ->
			}
			finally {
				CrdSysConvertor.Destroy();
			}
		}
		finally {
			GCF.Release();
		}
		if (Result != null)
			return Result; //. ->
		Result = Server_ConvertGeoCoordinatesToXY(DatumID, Latitude,Longitude,Altitude);
		return Result; 
	}
}
