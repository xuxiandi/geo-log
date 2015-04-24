package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations;

import com.geoscope.GeoEye.Space.Defines.TGeoCoord;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.TGeoCrdSystemData;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.TGeoCrdSystemFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations.TGeoProjection.TEQCProjectionDATA;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations.TMapInfoProvider.TMapInfo;

public class TCrdSysConvertor {

	private TGeoCrdSystemFunctionality	GeoCrdSystemFunctionality;
	//.
	private TGeoCrdSystemData GeoCrdSystemData = null;
	//.
	private TMapInfo		MapInfo = null;	
	private TGeoDatum 		Datum = null;
	private TGeoProjection 	Projection = null;
	
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
		GeoCrdSystemData = GeoCrdSystemFunctionality.GetData();
		if (GeoCrdSystemData == null)
			return false; //. ->
		MapInfo = TMapInfoProvider.GetMapInfo(GeoCrdSystemData.idTOwner,GeoCrdSystemData.idOwner);
		if ((MapInfo == null) || (!MapInfo.Prepare(GeoCrdSystemFunctionality.TypesSystem(),GeoCrdSystemFunctionality.ComponentDataSource)))
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
		
		case TGeoProjection.PROJECTION_EQC:
			TGeoCrdConverter GeoCrdConverter = new TGeoCrdConverter(Datum);
			//.
			TEQCProjectionDATA ProjectionDATA = (TEQCProjectionDATA)Projection.DATA;
			if (ProjectionDATA == null)
				throw new Exception("invalid projection data"); //. =>
			//.
			TViewTransformator.TControlPoint[] ControlPoints;
			if (GeoCrdSystemData.GeodesyPoints != null) {
				ControlPoints = new TViewTransformator.TControlPoint[GeoCrdSystemData.GeodesyPoints.length];
				for (int I = 0; I < ControlPoints.length; I++) {
			        TXYCoord P = GeoCrdConverter.LatLongToEQC(GeoCrdSystemData.GeodesyPoints[I].Latitude,GeoCrdSystemData.GeodesyPoints[I].Longitude, ProjectionDATA.LatOfOrigin,ProjectionDATA.LongOfOrigin,ProjectionDATA.FirstStdParallel,ProjectionDATA.SecondStdParallel,ProjectionDATA.FalseEasting,ProjectionDATA.FalseNorthing);
					//.
					ControlPoints[I] = new TViewTransformator.TControlPoint();
			        ControlPoints[I].E = P.X; ControlPoints[I].N = P.Y;
			        ControlPoints[I].X = GeoCrdSystemData.GeodesyPoints[I].X; ControlPoints[I].Y = GeoCrdSystemData.GeodesyPoints[I].Y;
				}
			}
			else
				throw new Exception("invalid geodesy points"); //. =>
			//.
			TViewTransformator ViewTransformator = new TViewTransformator(ControlPoints);
		    //.
		    TXYCoord EN = GeoCrdConverter.LatLongToEQC(Lat,Long, ProjectionDATA.LatOfOrigin,ProjectionDATA.LongOfOrigin,ProjectionDATA.FirstStdParallel,ProjectionDATA.SecondStdParallel,ProjectionDATA.FalseEasting,ProjectionDATA.FalseNorthing);
		    //.
		    TXYCoord Pix = ViewTransformator.Polynom3RMS_Transform(EN.X,EN.Y);
			//.
			return MapInfo.ConvertPixPosToXY(Pix.X,Pix.Y); //. ->
			
		case TGeoProjection.PROJECTION_MEP:
			double MP_Easting,MP_Northing;
			double a,b;
			double PixX,PixY;
			//.
			GeoCrdConverter = new TGeoCrdConverter(Datum);
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
		
		case TGeoProjection.PROJECTION_EQC:
		    TXYCoord Pix = MapInfo.ConvertXYToPixPos(X,Y);
			//.
			TGeoCrdConverter GeoCrdConverter = new TGeoCrdConverter(Datum);
			//.
			TEQCProjectionDATA ProjectionDATA = (TEQCProjectionDATA)Projection.DATA;
			if (ProjectionDATA == null)
				throw new Exception("invalid projection data"); //. =>
			//.
			TViewTransformator.TControlPoint[] ControlPoints;
			if (GeoCrdSystemData.GeodesyPoints != null) {
				ControlPoints = new TViewTransformator.TControlPoint[GeoCrdSystemData.GeodesyPoints.length];
				for (int I = 0; I < ControlPoints.length; I++) {
			        TXYCoord P = GeoCrdConverter.LatLongToEQC(GeoCrdSystemData.GeodesyPoints[I].Latitude,GeoCrdSystemData.GeodesyPoints[I].Longitude, ProjectionDATA.LatOfOrigin,ProjectionDATA.LongOfOrigin,ProjectionDATA.FirstStdParallel,ProjectionDATA.SecondStdParallel,ProjectionDATA.FalseEasting,ProjectionDATA.FalseNorthing);
					//.
					ControlPoints[I] = new TViewTransformator.TControlPoint();
			        ControlPoints[I].E = P.X; ControlPoints[I].N = P.Y;
			        ControlPoints[I].X = GeoCrdSystemData.GeodesyPoints[I].X; ControlPoints[I].Y = GeoCrdSystemData.GeodesyPoints[I].Y;
				}
			}
			else
				throw new Exception("invalid geodesy points"); //. =>
			//.
			TViewTransformator ViewTransformator = new TViewTransformator(ControlPoints);
		    //.
			TXYCoord EN = ViewTransformator.Polynom3RMS_InverseTransformWithCorrection(Pix.X,Pix.Y);
		    //.
		    return GeoCrdConverter.EQCToLatLong(EN.X,EN.Y, ProjectionDATA.LatOfOrigin,ProjectionDATA.LongOfOrigin,ProjectionDATA.FirstStdParallel,ProjectionDATA.SecondStdParallel,ProjectionDATA.FalseEasting,ProjectionDATA.FalseNorthing); //. ->
			
		case TGeoProjection.PROJECTION_MEP:
			double MP_Easting,MP_Northing;
			double a,b;
			//.
		    Pix = MapInfo.ConvertXYToPixPos(X,Y);
		    //.
			GeoCrdConverter = new TGeoCrdConverter(Datum);
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
