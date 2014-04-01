package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations;

import com.geoscope.GeoEye.Space.Defines.TGeoCoord;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.TGeoCrdSystemData;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.TGeoCrdSystemFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations.TMapInfoProvider.TMapInfo;

public class TCrdSysConvertor {

	private TGeoCrdSystemFunctionality	GeoCrdSystemFunctionality;
	//.
	private TMapInfo	MapInfo = null;	
	private TGeoDatum 		Datum = null;
	private TGeoProjection Projection = null;
	
	public TCrdSysConvertor(TGeoCrdSystemFunctionality pGeoCrdSystemFunctionality) {
		GeoCrdSystemFunctionality = pGeoCrdSystemFunctionality;
		GeoCrdSystemFunctionality.AddRef();
	}
	
	public void Destroy() {
		if (GeoCrdSystemFunctionality != null) {
			GeoCrdSystemFunctionality.Release();
			GeoCrdSystemFunctionality = null;
		}
	}
	
	public boolean Prepare() throws Exception {
		TGeoCrdSystemData GeoCrdSystemData = GeoCrdSystemFunctionality.GetData();
		if (GeoCrdSystemData == null)
			return false; //. ->
		MapInfo = TMapInfoProvider.GetMapInfo(GeoCrdSystemData.idTOwner,GeoCrdSystemData.idOwner);
		if ((MapInfo == null) || (!MapInfo.Prepare(GeoCrdSystemFunctionality.TypesSystem(),GeoCrdSystemFunctionality.Server,GeoCrdSystemFunctionality.ComponentDataSource)))
			return false; //. ->
		Datum = TGeoDatum.GetDatumByName(GeoCrdSystemData.Datum);
		if (Datum == null)
			return false; //. ->
		Projection = TGeoProjection.GetProjectionByName(GeoCrdSystemData.Projection);
		if ((Projection == null) || (!Projection.LoadDATA(GeoCrdSystemData.ProjectionDATA)))
			return false; //. ->
		return true; //. ->
	}
	
	public TXYCoord ConvertGeoToXY(double Lat, double Long) throws Exception {
		switch (Projection.ID) {
		
		case TGeoProjection.PROJECTION_MEP:
			double MP_Easting,MP_Northing;
			double a,b;
			double PixX,PixY;
			//.
			TGeoCrdConverter GeoCrdConverter = new TGeoCrdConverter(Datum);
			TXYCoord XYCoord = GeoCrdConverter.LatLongToMP(Lat,Long);
			MP_Easting = XYCoord.X;
			MP_Northing = XYCoord.Y;
			//.
			a = MapInfo.Size/TGeoCrdConverter.EquatorLength;
			b = TGeoCrdConverter.EquatorLength/2.0;
			PixX = (b+MP_Easting)*a;
			PixY = (b-MP_Northing)*a;
			//.
			return MapInfo.ConvertPixPosToXY(PixX,PixY); //. ->
			
		case TGeoProjection.PROJECTION_MSP:
			GeoCrdConverter = new TGeoCrdConverter(Datum);
			XYCoord = GeoCrdConverter.LatLongToSphereMP(Lat,Long);
			MP_Easting = XYCoord.X;
			MP_Northing = XYCoord.Y;
			//.
			a = MapInfo.Size/TGeoCrdConverter.EquatorLength;
			b = TGeoCrdConverter.EquatorLength/2.0;
			PixX = (b+MP_Easting)*a;
			PixY = (b-MP_Northing)*a;
			//.
			return MapInfo.ConvertPixPosToXY(PixX,PixY); //. ->
			
		default:
			return null; //. ->
		}
	}
	
	public TGeoCoord ConvertXYToGeo(double X, double Y) throws Exception {
		switch (Projection.ID) {
		
		case TGeoProjection.PROJECTION_MEP:
			double MP_Easting,MP_Northing;
			double a,b;
			//.
		    TXYCoord Pix = MapInfo.ConvertXYToPixPos(X,Y);
		    //.
			TGeoCrdConverter GeoCrdConverter = new TGeoCrdConverter(Datum);
			a = MapInfo.Size/TGeoCrdConverter.EquatorLength;
			b = TGeoCrdConverter.EquatorLength/2.0;
		    MP_Easting = Pix.X/a-b;
		    MP_Northing = b-Pix.Y/a;
		    //.
		    return GeoCrdConverter.MPToLatLong(MP_Easting,MP_Northing); //. ->
			
		case TGeoProjection.PROJECTION_MSP:
		    Pix = MapInfo.ConvertXYToPixPos(X,Y);
		    //.
			GeoCrdConverter = new TGeoCrdConverter(Datum);
			a = MapInfo.Size/TGeoCrdConverter.EquatorLength;
			b = TGeoCrdConverter.EquatorLength/2.0;
		    MP_Easting = Pix.X/a-b;
		    MP_Northing = b-Pix.Y/a;
		    //.
		    return GeoCrdConverter.SphereMPToLatLong(MP_Easting,MP_Northing); //. ->
			
		default:
			return null; //. ->
		}
	}
}
