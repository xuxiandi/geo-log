package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations;

import com.geoscope.GeoEye.Space.Defines.TGeoCoord;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;

public class TGeoCrdConverter {

	public static final double EquatorLength = 40075016.685578488;
	public static final double EquatorLengthHalf = EquatorLength/2.0;
	//.
	public static final double D2R = Math.PI/180.0; // degree to radians constant
	public static final double R2D = 180.0/Math.PI; // radians to degree constant
	
	public TGeoDatum Datum;
	
	public TGeoCrdConverter(TGeoDatum pDatum) {
		Datum = pDatum;
	}

    //. MP transformations (Mercator on ellipsoide)
	public TXYCoord LatLongToMP(double Lat, double Long) throws Exception {
		double Latitude,Longitude;
		double Rn;
		double e;
		double esinLat;
		double tan_temp;
		double pow_temp;
		double U;
		//.
		Latitude = Lat*D2R;
		Longitude = Long*D2R;
		Rn = Datum.Ellipsoide_EquatorialRadius;
		e = Math.sqrt(Datum.Ellipsoid_EccentricitySquared);
		esinLat = e*Math.sin(Latitude);
		tan_temp = Math.tan(Math.PI/4.0+Latitude/2.0);
		pow_temp = Math.pow(Math.tan(Math.PI/4.0+Math.asin(esinLat)/2),e);
		U = tan_temp/pow_temp;
		double Easting = Rn*Longitude;
		double Northing = Rn*Math.log(U);
		//.
		TXYCoord Result = new TXYCoord();
		Result.X = Easting;
		Result.Y = Northing;
		return Result;
	}

	public TGeoCoord MPToLatLong(double Easting, double Northing) throws Exception {
		double Rn;
		double ab,bb,cb,db;
		double xphi;
		double Latitude,Longitude;
		//.
		if (Datum.ID != 23) 
			throw new Exception("Datum is not supported, DatumID: "+Integer.toString(Datum.ID)); //. =>
		Rn = Datum.Ellipsoide_EquatorialRadius;
		ab = 0.00335655146887969400;
		bb = 0.00000657187271079536;
		cb = 0.00000001764564338702;
		db = 0.00000000005328478445;
		xphi = Math.PI/2.0-2.0*Math.atan(1.0/Math.exp(Northing/Rn));
		Latitude = xphi+ab*Math.sin(2.0*xphi)+bb*Math.sin(4.0*xphi)+cb*Math.sin(6.0*xphi)+db*Math.sin(8.0*xphi);
		Longitude = Easting/Rn;
		double Lat = Latitude*R2D;
		double Long = Longitude*R2D;
		//.
		TGeoCoord Result = new TGeoCoord(Datum.ID,Lat,Long,0.0/*Altitude*/);
		return Result;
	}

    //. MP transformations (Mercator on sphere)
	public TXYCoord LatLongToSphereMP(double Lat, double Long) throws Exception {
		double Latitude;
		//.
		Latitude = Lat*D2R;
		double Easting = EquatorLengthHalf*(Long/180.0);
		double Northing = EquatorLengthHalf*((Math.log(Math.tan(Latitude)+1.0/Math.cos(Latitude)))/Math.PI);
		//.
		TXYCoord Result = new TXYCoord();
		Result.X = Easting;
		Result.Y = Northing;
		return Result;
	}

	public TGeoCoord SphereMPToLatLong(double Easting, double Northing) {
		double Lat = Math.atan(Math.sinh(Math.PI*(Northing/EquatorLengthHalf)))*R2D;
		double Long = (Easting/EquatorLengthHalf)*180.0;
		//.
		TGeoCoord Result = new TGeoCoord(Datum.ID,Lat,Long,0.0/*Altitude*/);
		return Result;
	}

}
