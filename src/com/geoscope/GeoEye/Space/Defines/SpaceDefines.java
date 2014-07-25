package com.geoscope.GeoEye.Space.Defines;

import android.content.Context;
import com.geoscope.GeoEye.R;

public class SpaceDefines {

	//. SpaceObj defines
	public static int nilPtr = -1;
	public static double nilCrd = 0;
	public static int TSpace_MinFreeArea = 100000; 
	public static int TSpace_IncreaseDelta = 1000000; 
	public static int TSpaceObj_maxPointsCount = 10000;
	public static int ofsptrFirstPoint = 12;
	public static int ofsptrListOwnerObj = 28;
	public static int SpacePtrSize = 4;
	public static int SpaceObjSize = 36;
	public static int ObjPointSize = 16;
	public static int Reflection_VisibleFactor = 4;

	
	public static final int TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION = 0;
	//. Typed data file types
	public static final int TYPEDDATAFILE_TYPE_RANGE 					= 100;
	public static final int TYPEDDATAFILE_TYPE_SHIFT_FromName_ToBrief 	= TYPEDDATAFILE_TYPE_RANGE/2;
	public static final int TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull 	= TYPEDDATAFILE_TYPE_RANGE-1;
	//.
	public static final String TYPEDDATAFILE_TYPE_All_String(Context context) {
		return context.getString(R.string.SUnknown1);   
	}
	public static final int TYPEDDATAFILE_TYPE_AllName 			= 0;   
	public static final int TYPEDDATAFILE_TYPE_AllBrief 		= 50;   
	public static final int TYPEDDATAFILE_TYPE_All 				= 99;
	//.
	public static final String TYPEDDATAFILE_TYPE_UriData_String(Context context) {
		return context.getString(R.string.SURIData);   
	}
	public static final int TYPEDDATAFILE_TYPE_UriDataName 		= 100;   
	public static final int TYPEDDATAFILE_TYPE_UriDataBrief 	= 150;   
	public static final int TYPEDDATAFILE_TYPE_UriData 			= 199;   
	//.
	public static final String TYPEDDATAFILE_TYPE_Document_String(Context context) {
		return context.getString(R.string.SDocument);   
	}
	public static final int TYPEDDATAFILE_TYPE_DocumentName 	= 200;   
	public static final int TYPEDDATAFILE_TYPE_DocumentBrief 	= 250;   
	public static final int TYPEDDATAFILE_TYPE_Document 		= 299;   
	//.
	public static final String TYPEDDATAFILE_TYPE_Image_String(Context context) {
		return context.getString(R.string.SImage1);   
	}
	public static final int TYPEDDATAFILE_TYPE_ImageName 		= 300;   
	public static final int TYPEDDATAFILE_TYPE_ImageBrief 		= 350;   
	public static final int TYPEDDATAFILE_TYPE_Image 			= 399;
	//.
	public static final String TYPEDDATAFILE_TYPE_Audio_String(Context context) {
		return context.getString(R.string.SAudio1);   
	}
	public static final int TYPEDDATAFILE_TYPE_AudioName 		= 400;   
	public static final int TYPEDDATAFILE_TYPE_AudioBrief 		= 450;   
	public static final int TYPEDDATAFILE_TYPE_Audio 			= 499;   
	//.
	public static final String TYPEDDATAFILE_TYPE_Video_String(Context context) {
		return context.getString(R.string.SVideo2);   
	}
	public static final int TYPEDDATAFILE_TYPE_VideoName 		= 500;   
	public static final int TYPEDDATAFILE_TYPE_VideoBrief 		= 550;   
	public static final int TYPEDDATAFILE_TYPE_Video 			= 599;   
	//.
	public static String TYPEDDATAFILE_TYPE_String(int Type, Context context) {
		if ((TYPEDDATAFILE_TYPE_AllName <= Type) && (Type <= (TYPEDDATAFILE_TYPE_AllName+TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)))
			return TYPEDDATAFILE_TYPE_All_String(context); //. ->
		else
			if ((TYPEDDATAFILE_TYPE_DocumentName <= Type) && (Type <= (TYPEDDATAFILE_TYPE_DocumentName+TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)))
				return TYPEDDATAFILE_TYPE_Document_String(context); //. ->
			else
				if ((TYPEDDATAFILE_TYPE_ImageName <= Type) && (Type <= (TYPEDDATAFILE_TYPE_ImageName+TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)))
					return TYPEDDATAFILE_TYPE_Image_String(context); //. ->
				else
					if ((TYPEDDATAFILE_TYPE_AudioName <= Type) && (Type <= (TYPEDDATAFILE_TYPE_AudioName+TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)))
						return TYPEDDATAFILE_TYPE_Audio_String(context); //. ->
					else
						if ((TYPEDDATAFILE_TYPE_VideoName <= Type) && (Type <= (TYPEDDATAFILE_TYPE_VideoName+TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)))
							return TYPEDDATAFILE_TYPE_Video_String(context); //. ->
						else 
							return "?";
	}
	
	//. Base component types
	public static final int 	idTTileServerVisualization = 2085;
	public static final String 	nmTTileServerVisualization = "Tile-Server-visualization";

	public static final int 	idTMapFormatObject = 2077;
	public static final String 	nmTMapFormatObject = "Map-format object";

	public static final String 	nmTGeoGraphServerObject = "Geo graph server object";
	public static final int 	idTGeoGraphServerObject = 2073;
	
	public static final int 	idTGeoSpace = 2072;
	public static final String 	nmTGeoSpace = "GeoSpace";
	
	public static final int 	idTGeoGraphServer = 2070;
	public static final String 	nmTGeoGraphServer = "Geo graph server";
	
	public static final int 	idTGeoCrdSystem = 2069;
	public static final String 	nmTGeoCrdSystem = "Geo coordinate system";
	
	public static final int 	idTHINTVisualization = 2067;
	public static final String 	nmTHINTVisualization = "Hint-visualization";
	  
	public static final int 	idTDetailedPictureVisualization = 2065;
	public static final String	nmTDetailedPictureVisualization = "Detailed picture visualization";
	  
	public static final int 	idTMODELServer = 2052;
	public static final String 	nmTMODELServer = "MODEL-Server";

	public static final int 	idTPositioner = 2050;
	public static final String 	nmTPositioner = "Positioner";
	
	public static final int 	idTGeodesyPoint = 2043;
	public static final String 	nmTGeodesyPoint = "Geodesy Point";

	public static final int 	idTModelUser = 2019;
	public static final String 	nmTModelUser = "MODEL-User";
	
	public static final int 	idTDATAFile = 2018;
	public static final String 	nmTDATAFile = "DATAFile";
	
	public static final int 	idTCoComponent = 2015;
	public static final String 	nmTCoComponent = "Co-Component"; 

	public static final int 	idTVisualization = 2004;
	public static final String 	nmTVisualization = "Visualization"; 
	
	//. Co-Component types
	public static final int idTCoGeoMonitorObject = 1111143;
}
