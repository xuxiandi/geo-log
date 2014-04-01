package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations;

import com.geoscope.GeoEye.Space.Defines.TGeoCoord;

public class TGeoDatum {

	public static TGeoDatum[] List = new TGeoDatum[] {
		new TGeoDatum(15,	"SK-42",	6378245.0,	0.006693422), 
		new TGeoDatum(23,	"WGS-84",	6378137.0,	0.00669438)
	}; 
	
	public static TGeoDatum GetDatumByID(int ID) {
		for (int I = 0; I < List.length; I++) 
			if (List[I].ID == ID)
				return List[I]; //. -> 
		return null;
	}
	
	public static TGeoDatum GetDatumByName(String Name) {
		for (int I = 0; I < List.length; I++) 
			if (List[I].Name.equals(Name))
				return List[I]; //. -> 
		return null;
	}
	
	public int 		ID;
	public String 	Name;
	public double 	Ellipsoide_EquatorialRadius;
	public double 	Ellipsoid_EccentricitySquared;
	
	public TGeoDatum(int pID, String pName, double pEllipsoide_EquatorialRadius, double pEllipsoid_EccentricitySquared) {
		ID = pID;
		Name = pName;
		Ellipsoide_EquatorialRadius = pEllipsoide_EquatorialRadius;
		Ellipsoid_EccentricitySquared = pEllipsoid_EccentricitySquared;
	}
	
	public TGeoCoord ConvertCoordinatesToDatum(TGeoCoord Coordinates, TGeoDatum ToDatum) {
		if (ID == ToDatum.ID)
			return Coordinates; //. ->
		else 
			return null; //. ->
	}
}
