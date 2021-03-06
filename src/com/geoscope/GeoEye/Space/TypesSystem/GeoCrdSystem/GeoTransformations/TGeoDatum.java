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

	public static final double D2R = Math.PI/180.0; // degree to radians constant
	public static final double R2D = 180.0/Math.PI; // radians to degree constant
	
	
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
	
	public TGeoCoord ConvertCoordinatesToDatum(TGeoCoord Coordinates, TGeoDatum ToDatum) throws Exception {
		if (Coordinates.Datum != ID)
			throw new Exception("incorrect input coordinate datum"); //. =>
		if (ID == ToDatum.ID)
			return Coordinates; //. ->
		else {
			switch (ID) {

			case 15: //. SK-42 (Krassovsky)
				switch (ToDatum.ID) {

				case 23: //. WGS-84
					return SK42_ConvertCoordinatesToWGS84(Coordinates,ToDatum); //. >
					
				default:
					return null; //. ->
				}
				
			case 23: //. WGS-84
				switch (ToDatum.ID) {

				case 15: //. SK-42 (Krassovsky)
					return WGS84_ConvertCoordinatesToSK42(Coordinates,ToDatum); //. >
					
				default:
					return null; //. ->
				}
				
			default:
				return null; //. ->
			}
		}
	}
	
    public double GetDistance(double Latitude, double Longitude,  double Latitude1, double Longitude1) { /* COPYRIGHT DelphiWorld */
        double fPhimean;
        double fdLambda;
        double fdPhi; 
        double fAlpha;
        double fRho;
        double fNu;
        double fR;
        double fz;
        double fTemp;
        double Distance;
        //. 
        if ((Latitude == Latitude1) && (Longitude == Longitude1))
        	return 0.0; //. ->
        if (Double.isNaN(Latitude) || Double.isNaN(Longitude) || Double.isNaN(Latitude1) || Double.isNaN(Longitude1))
            return Double.NaN; //. ->
    	//.
        double a = Ellipsoide_EquatorialRadius;
        double e2 = (Ellipsoid_EccentricitySquared)/(1.0-Ellipsoid_EccentricitySquared); 
        fdLambda = (Longitude - Longitude1) * D2R;
        fdPhi = (Latitude - Latitude1) * D2R;
        fPhimean = ((Latitude + Latitude1) / 2.0) * D2R;
        fTemp = 1 - e2 * (Math.pow(Math.sin(fPhimean), 2));
        fRho = (a * (1 - e2)) / Math.pow(fTemp, 1.5);
        fNu = a / (Math.sqrt(1 - e2 * (Math.sin(fPhimean) * Math.sin(fPhimean))));
        fz = Math.sqrt(Math.pow(Math.sin(fdPhi / 2.0), 2) + Math.cos(Latitude1 * D2R) * Math.cos(Latitude * D2R) * Math.pow(Math.sin(fdLambda / 2.0), 2));
        if (fz > 1.0) //. workaround on a case like fz = 1.0000000002
        	fz = 1.0;
        else
            if (fz < -1.0)
            	fz = -1.0;
        fz = 2 * Math.asin(fz);
        fAlpha = Math.cos(Latitude1 * D2R) * Math.sin(fdLambda) * 1 / Math.sin(fz);
        if (fAlpha > 1.0) //. workaround on a case like fAlpha = 1.0000000002
        	fAlpha = 1.0;
        else
            if (fAlpha < -1.0)
            	fAlpha = -1.0;
        fAlpha = Math.asin(fAlpha);
        fR = (fRho * fNu) / ((fRho * Math.pow(Math.sin(fAlpha), 2)) + (fNu * Math.pow(Math.cos(fAlpha), 2)));
        Distance = (fz * fR);
        //.
        return Distance;
    }
    
	private static final double ro = 206264.8062; //. number angle seconds in radian
	private static final double Pulkovo42WGS84_dx = 23.92;
	private static final double Pulkovo42WGS84_dy = -141.27;
	private static final double Pulkovo42WGS84_dz = -80.9;
	private static final double Pulkovo42WGS84_wx = 0.0;
	private static final double Pulkovo42WGS84_wy = 0.0;
	private static final double Pulkovo42WGS84_wz = 0.0;
	private static final double Pulkovo42WGS84_ms = 0.0;
	
	//. mr. Molodensky datum transformations
	private double Pulkovo42WGS84_dB(double Bd, double Ld, double H) {
		double aP,e2P;
		double aW,e2W;
		double a,e2,da,de2;
		double B, L, M, N;
		//.
		TGeoDatum SK42 = GetDatumByID(15); //. Krassovsky
		TGeoDatum WGS84 = GetDatumByID(23); //. WGS-84
		//.
		aP = SK42.Ellipsoide_EquatorialRadius; 
		e2P = SK42.Ellipsoid_EccentricitySquared;
		aW = WGS84.Ellipsoide_EquatorialRadius; 
		e2W = WGS84.Ellipsoid_EccentricitySquared;
		//.
		a = (aP+aW)/2.0;
		e2 = (e2P+e2W)/2.0;
		da = (aW-aP);
		de2 = (e2W-e2P);
		//.
		B = Bd*Math.PI/180.0;
		L = Ld*Math.PI/180.0;
		M = a*(1-e2)/Math.pow((1-e2*Math.pow((Math.sin(B)),2)),1.5);
		N = a*Math.pow((1-e2*Math.pow((Math.sin(B)),2)),-0.5);
		//.
		return ro/(M+H)*(N/a*e2*Math.sin(B)*Math.cos(B)*da+(Math.pow(N,2)/Math.pow(a,2)+1)*N*Math.sin(B)*Math.cos(B)*de2/2.0-(Pulkovo42WGS84_dx*Math.cos(L)+Pulkovo42WGS84_dy*Math.sin(L))*Math.sin(B)+Pulkovo42WGS84_dz*Math.cos(B))-Pulkovo42WGS84_wx*Math.sin(L)*(1.0+e2*Math.cos(2*B))+Pulkovo42WGS84_wy*Math.cos(L)*(1+e2*Math.cos(2*B))-ro*Pulkovo42WGS84_ms*e2*Math.sin(B)*Math.cos(B);
	}

	private double Pulkovo42WGS84_dL(double Bd, double Ld, double H) {
		double aP,e2P;
		double aW,e2W;
		double a,e2;
		double B,L,N;
		//.
		TGeoDatum SK42 = GetDatumByID(15); //. Krassovsky
		TGeoDatum WGS84 = GetDatumByID(23); //. WGS-84
		//.
		aP = SK42.Ellipsoide_EquatorialRadius; 
		e2P = SK42.Ellipsoid_EccentricitySquared;
		aW = WGS84.Ellipsoide_EquatorialRadius; 
		e2W = WGS84.Ellipsoid_EccentricitySquared;
		//.
		a = (aP+aW)/2.0;
		e2 = (e2P+e2W)/2.0;
		//.
		B = Bd*Math.PI/180.0;
		L = Ld*Math.PI/180.0;
		N = a*Math.pow((1.0-e2*Math.pow((Math.sin(B)),2)),-0.5);
		//.
		return ro/((N+H)*Math.cos(B))*(-Pulkovo42WGS84_dx*Math.sin(L)+Pulkovo42WGS84_dy*Math.cos(L))+ Math.tan(B)*(1.0-e2)*(Pulkovo42WGS84_wx*Math.cos(L)+Pulkovo42WGS84_wy*Math.sin(L))-Pulkovo42WGS84_wz;
	}

	private TGeoCoord WGS84_ConvertCoordinatesToSK42(TGeoCoord Coordinates, TGeoDatum ToDatum) {
		double aP,e2P;
		double aW,e2W;
		double a,e2,da,de2;
		double B,L,N,dH;
		//.
		TGeoCoord Result = new TGeoCoord(ToDatum.ID);
		Result.Latitude = Coordinates.Latitude-Pulkovo42WGS84_dB(Coordinates.Latitude,Coordinates.Longitude,Coordinates.Altitude)/3600.0;
		Result.Longitude = Coordinates.Longitude-Pulkovo42WGS84_dL(Coordinates.Latitude,Coordinates.Longitude,Coordinates.Altitude)/3600.0;
		//.
		TGeoDatum SK42 = GetDatumByID(15); //. Krassovsky
		TGeoDatum WGS84 = GetDatumByID(23); //. WGS-84
		//. converting altitude
		aP = SK42.Ellipsoide_EquatorialRadius;
		e2P = SK42.Ellipsoid_EccentricitySquared;
		aW = WGS84.Ellipsoide_EquatorialRadius; 
		e2W = WGS84.Ellipsoid_EccentricitySquared;
		//.
		a = (aP+aW)/2.0;
		e2 = (e2P+e2W)/2.0;
		da = (aW-aP);
		de2 = (e2W-e2P);
		//.
		B = Coordinates.Latitude*Math.PI/180.0;
		L = Coordinates.Longitude*Math.PI/180.0;
		N = a*Math.pow((1-e2*Math.pow((Math.sin(B)),2)),-0.5);
		dH = -a/N*da+N*Math.pow(Math.sin(B),2)*de2/2.0+(Pulkovo42WGS84_dx*Math.cos(L)+Pulkovo42WGS84_dy*Math.sin(L))*Math.cos(B)+Pulkovo42WGS84_dz*Math.sin(B)-N*e2*Math.sin(B)*Math.cos(B)*(Pulkovo42WGS84_wx/ro*Math.sin(L)-Pulkovo42WGS84_wy/ro*Math.cos(L))+(Math.pow(a,2)/N+Coordinates.Altitude)*Pulkovo42WGS84_ms;
		Result.Altitude = Coordinates.Altitude-dH;
		//.
		return Result;
	}

	private TGeoCoord SK42_ConvertCoordinatesToWGS84(TGeoCoord Coordinates, TGeoDatum ToDatum) {		  
		double aP,e2P;
		double aW,e2W;
		double a,e2,da,de2;
		double B,L,N,dH;
		//.
		TGeoCoord Result = new TGeoCoord(ToDatum.ID);
		Result.Latitude = Coordinates.Latitude+Pulkovo42WGS84_dB(Coordinates.Latitude,Coordinates.Longitude,Coordinates.Altitude)/3600.0;
		Result.Longitude = Coordinates.Longitude+Pulkovo42WGS84_dL(Coordinates.Latitude,Coordinates.Longitude,Coordinates.Altitude)/3600.0;
		//.
		TGeoDatum SK42 = GetDatumByID(15); //. Krassovsky
		TGeoDatum WGS84 = GetDatumByID(23); //. WGS-84
		//. converting altitude
		aP = SK42.Ellipsoide_EquatorialRadius; 
		e2P = SK42.Ellipsoid_EccentricitySquared;
		aW = WGS84.Ellipsoide_EquatorialRadius; 
		e2W = WGS84.Ellipsoid_EccentricitySquared;
		//.
		a = (aP+aW)/2.0;
		e2 = (e2P+e2W)/2.0;
		da = (aW-aP);
		de2 = (e2W-e2P);
		//.
		B = Coordinates.Latitude*Math.PI/180.0;
		L = Coordinates.Longitude*Math.PI/180.0;
		N = a*Math.pow((1-e2*Math.pow((Math.sin(B)),2)),-0.5);
		dH = -a/N*da+N*Math.pow(Math.sin(B),2)*de2/2.0+(Pulkovo42WGS84_dx*Math.cos(L)+Pulkovo42WGS84_dy*Math.sin(L))*Math.cos(B)+Pulkovo42WGS84_dz*Math.sin(B)-N*e2*Math.sin(B)*Math.cos(B)*(Pulkovo42WGS84_wx/ro*Math.sin(L)-Pulkovo42WGS84_wy/ro*Math.cos(L))+(Math.pow(a,2)/N+Coordinates.Altitude)*Pulkovo42WGS84_ms;
		Result.Altitude = Coordinates.Altitude+dH;
		//.
		return Result;
	}
}
