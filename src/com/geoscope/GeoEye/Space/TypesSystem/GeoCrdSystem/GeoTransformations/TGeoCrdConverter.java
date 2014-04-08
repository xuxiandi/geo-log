package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations;

import com.geoscope.GeoEye.Space.Defines.TGeoCoord;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;

public class TGeoCrdConverter {

	public static final double EquatorLength = 40075016.685578488;
	public static final double EquatorLengthHalf = EquatorLength/2.0;
	//.
	public static final double D2R = Math.PI/180.0; // degree to radians constant
	public static final double R2D = 180.0/Math.PI; // radians to degree constant
	
	public static final int MeridianDistance_IterationsCount = 20;
	
	public static class TMeridianDistanceData {
		public boolean flInitialized = false;
	    public int nb;
	    public double es;
	    public double E;
	    public double[] b;
	}

	public TGeoDatum Datum;
	//.
	private TMeridianDistanceData MeridianDistanceData = new TMeridianDistanceData();
	
	public TGeoCrdConverter(TGeoDatum pDatum) {
		Datum = pDatum;
	}
	
	public void MeridianDistance_InitData() {
		double numf, numfi, twon1, denf, denfi, ens, T, twon;
		double den, El, Es;
		double[] E;
		int IC;
		//.
		E = new double[MeridianDistance_IterationsCount];
		//. generate E(e^2) and its terms E[]
		ens = Datum.Ellipsoid_EccentricitySquared;
		numf = 1.0;
		twon1 = 1.0;
		denfi = 1.0;
		denf = 1.0;
		twon = 4.0;
		Es = 1.0;
		El = 1.0;
		E[0] = 1.0;
		IC = 0;
		for (int I = 1; I <MeridianDistance_IterationsCount; I++) {
			numf = numf*(twon1*twon1);
			den = twon*denf*denf*twon1;
			T = numf/den;
			E[I] = T*ens;
			Es = Es-E[I];
			ens = ens*Datum.Ellipsoid_EccentricitySquared;
			twon = twon*4.0;
			denfi = denfi+2.0;
			denf = denf*denfi;
			twon1 = twon1+2.0;
			IC++;
			//.
			if (Es == El) /*jump out if no change*/ 
				break; //. >
			//.
			El = Es;
		}
		MeridianDistanceData.b = new double[MeridianDistance_IterationsCount];
		MeridianDistanceData.nb = IC-1;
		MeridianDistanceData.es = Datum.Ellipsoid_EccentricitySquared;
		MeridianDistanceData.E = Es;
		//. generate b_n coefficients--note: collapse with prefix ratios
		Es = 1.0-Es; 
		MeridianDistanceData.b[0] = Es;
		denf = 1.0;
		numf = 1.0;
		numfi = 2.0;
		denfi = 3.0;
		for (int I = 1; I < MeridianDistance_IterationsCount; I++) { 
			Es = Es-E[I];
			numf = numf*numfi;
			denf = denf*denfi;
			MeridianDistanceData.b[I] = Es*numf/denf;
			numfi = numfi+2.0;
			denfi = denfi+2.0;
		}
		//.
		MeridianDistanceData.flInitialized = true;
	}

	private double MeridianDistance_Calculate(double phi, double sinphi, double  cosphi) {
		double sc, sum, sphi2, D;
		int I;
		//.
		if (!MeridianDistanceData.flInitialized) 
			MeridianDistance_InitData();
		sc = sinphi*cosphi;
		sphi2 = sinphi*sinphi;
		D = phi*MeridianDistanceData.E-MeridianDistanceData.es*sc/Math.sqrt(1.0-MeridianDistanceData.es*sphi2);
		I = MeridianDistanceData.nb;
		sum = MeridianDistanceData.b[I];
		while (I > 0) {
			I--;
			sum = MeridianDistanceData.b[I]+sphi2*sum;
		}
		return (D+sc*sum);
	}

	private double MeridianDistance_CalculateInverse(double Dist) {
		double TOL = 1e-14;
		double s, t, phi, k;
		//.
		if (!MeridianDistanceData.flInitialized) 
			MeridianDistance_InitData();
		k = 1.0/(1.0-MeridianDistanceData.es);
		phi = Dist;
		for (int I = MeridianDistance_IterationsCount-1; I >= 0; I--) {
			s = Math.sin(phi);
			t = 1.0-MeridianDistanceData.es*s*s;
			t = (MeridianDistance_Calculate(phi,s,Math.cos(phi))-Dist)*(t*Math.sqrt(t))*k;
			phi = phi-t;
			if (Math.abs(t) < TOL) /*that is no change*/ 
				return phi; //. ->
		}
		//. convergence failed 
		return phi;
	}
	
    //. EQC transformations
	public TXYCoord LatLongToEQC(double Lat, double Long, double LatOfOrigin, double LongOfOrigin, double FirstStdParallel, double SecondStdParallel, double FalseEasting, double FalseNorthing) {
		double phi,phi1,phi2,phio;
		double lamda,lamdao;
		double SinPhi1,CosPhi1;
		double SinPhi2,CosPhi2;
		double m1,m2;
		double n;
		double G;
		double r,rf;
		double theta;
		//.
		phi = Lat*D2R; //. Latitude to convert
		phi1 = FirstStdParallel*D2R; //. Latitude of 1st std parallel
		phi2 = SecondStdParallel*D2R; //. Latitude of 2nd std parallel
		lamda = Long*D2R; //. Longitude to convert
		phio = LatOfOrigin*D2R; //. Latitude of  Origin
		lamdao = LongOfOrigin*D2R; //. Longitude of  Origin
		//.
		SinPhi1 = Math.sin(phi1);
		CosPhi1 = Math.cos(phi1);
		SinPhi2 = Math.sin(phi2);
		CosPhi2 = Math.cos(phi2);
		//.
		m1 = CosPhi1/Math.sqrt(1.0 - Datum.Ellipsoid_EccentricitySquared*SinPhi1*SinPhi1);
		m2 = CosPhi2/Math.sqrt(1.0 - Datum.Ellipsoid_EccentricitySquared*SinPhi2*SinPhi2);
		if (phi2 != phi1)
			n = (m1-m2)/(MeridianDistance_Calculate(phi2,SinPhi2,CosPhi2)-MeridianDistance_Calculate(phi1,SinPhi1,CosPhi1));
		else 
			n = SinPhi1;
		G = m1/n+MeridianDistance_Calculate(phi1,SinPhi1,CosPhi1);
		rf = G-MeridianDistance_Calculate(phio,Math.sin(phio),Math.cos(phio));
		r = G-MeridianDistance_Calculate(phi,Math.sin(phi),Math.cos(phi));
		theta = n*(lamda - lamdao);
		//.
		double Easting = FalseEasting + r*Math.sin(theta);
		double Northing = FalseNorthing + (rf - r*Math.cos(theta));
		//.
		TXYCoord Result = new TXYCoord();
		Result.X = Easting;
		Result.Y = Northing;
		//.
		return Result;
	}

	public TGeoCoord EQCToLatLong(double Easting, double Northing, double LatOfOrigin, double LongOfOrigin, double FirstStdParallel, double SecondStdParallel, double FalseEasting, double FalseNorthing) {
		double phi,phi1,phi2,phio;
		double lamda,lamdao;
		double SinPhi1,CosPhi1;
		double SinPhi2,CosPhi2;
		double m1,m2;
		double n;
		double G;
		double r_,rf;
		double theta_;
		double D;
		//.
		phi1 = FirstStdParallel*D2R; //. Latitude of 1st std parallel
		phi2 = SecondStdParallel*D2R; //. Latitude of 2nd std parallel
		phio = LatOfOrigin*D2R; //. Latitude of  Origin
		lamdao = LongOfOrigin*D2R; //. Longitude of  Origin
		//.
		SinPhi1 = Math.sin(phi1);
		CosPhi1 = Math.cos(phi1);
		SinPhi2 = Math.sin(phi2);
		CosPhi2 = Math.cos(phi2);
		//.
		m1 = CosPhi1/Math.sqrt(1.0-Datum.Ellipsoid_EccentricitySquared*SinPhi1*SinPhi1);
		m2 = CosPhi2/Math.sqrt(1.0-Datum.Ellipsoid_EccentricitySquared*SinPhi2*SinPhi2);
		if (phi2 != phi1)
			n = (m1-m2)/(MeridianDistance_Calculate(phi2,SinPhi2,CosPhi2)-MeridianDistance_Calculate(phi1,SinPhi1,CosPhi1));
		else 
			n = SinPhi1;
		G = m1/n+MeridianDistance_Calculate(phi1,SinPhi1,CosPhi1);
		rf = G-MeridianDistance_Calculate(phio,Math.sin(phio),Math.cos(phio));
		r_ = Math.sqrt(Math.pow((Easting-FalseEasting),2)+Math.pow((rf-(Northing-FalseNorthing)),2));
		theta_ = Math.atan((Easting-FalseEasting)/(rf-(Northing-FalseNorthing)));
		//.
		lamda = theta_/n+lamdao;
		D = (G-r_);
		phi = MeridianDistance_CalculateInverse(D);
		//.
		double Lat = phi*R2D;
		double Long = lamda*R2D;
		//.
		TGeoCoord Result = new TGeoCoord(Datum.ID,Lat,Long,0.0/*Altitude*/);
		return Result;
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
