package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class TGeoProjection {

	public static final int PROJECTION_LATLONG 	= 1;
	public static final int PROJECTION_TM 		= 2;
	public static final int PROJECTION_UTM 		= 3;
	public static final int PROJECTION_LLC 		= 4;
	public static final int PROJECTION_EQC 		= 5;
	public static final int PROJECTION_MEP 		= 6;
	public static final int PROJECTION_MSP 		= 7;

	public static TGeoProjection[] List  = new TGeoProjection[] {
		new TGeoProjection(PROJECTION_LATLONG, "LatLong",	null),
		new TGeoProjection(PROJECTION_TM, 		"TM",		null),
		new TGeoProjection(PROJECTION_UTM, 	"UTM",		null),
		new TGeoProjection(PROJECTION_LLC, 	"LCC",		null),
		new TGeoProjection(PROJECTION_EQC, 	"EQC",		null),
		new TGeoProjection(PROJECTION_MEP, 	"MEP",		new TMPProjectionDATA()), //. {Mercator on Ellipsoide}
		new TGeoProjection(PROJECTION_MSP, 	"MSP",		new TMPProjectionDATA())  //. {Mercator on sphere}
	};
	
	public static TGeoProjection GetProjectionByName(String Name) {
		for (int I = 0; I < List.length; I++) 
			if (List[I].Name.equals(Name))
				return List[I]; //. -> 
		return null;
	}

	public static class TProjectionDATA {
		
		public boolean FromByteArray(byte[] BA) throws Exception {
			return false;
		}
	}
	
	public static class TMPProjectionDATA extends TProjectionDATA {
		
		public double LatOfOrigin = 0.0;
		public double LongOfOrigin = 0.0;
		
		@Override
		public boolean FromByteArray(byte[] BA) throws Exception {
			if (BA == null)
				return true; //. ->
			Document XmlDoc;
			ByteArrayInputStream BIS = new ByteArrayInputStream(BA);
			try {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				XmlDoc = builder.parse(BIS);
			} finally {
				BIS.close();
			}
			int Version = Integer.parseInt(XmlDoc.getDocumentElement().getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
			NodeList NL;
			switch (Version) {
			
			case 0:
				NL = XmlDoc.getDocumentElement().getElementsByTagName("LatOfOrigin");
				LatOfOrigin = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
				//.
				NL = XmlDoc.getDocumentElement().getElementsByTagName("LongOfOrigin");
				LongOfOrigin = Double.parseDouble(NL.item(0).getFirstChild().getNodeValue());
				return true; //. ->
				
			default:
				return false; //. ->
			}
		}
	}
	
	public int 		ID;
	public String 	Name;
	//.
	public TProjectionDATA DATA;
	
	public TGeoProjection(int pID, String pName, TProjectionDATA pDATA) {
		ID = pID;
		Name = pName;
		DATA = pDATA;
	}
	
	public boolean LoadDATA(byte[] BA) throws Exception {
		if (DATA != null)
			return DATA.FromByteArray(BA); //. ->
		else
			return true; //. ->
	}
}
