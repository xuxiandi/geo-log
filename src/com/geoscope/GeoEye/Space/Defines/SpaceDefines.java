package com.geoscope.GeoEye.Space.Defines;

import java.io.File;
import java.io.FileInputStream;
import java.util.Locale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;

import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;

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
	public static final String TYPEDDATAFILE_TYPE_Document_FORMAT_XML = ".XML";
	public static final String TYPEDDATAFILE_TYPE_Document_FORMAT_TXT = ".TXT";
	public static final String TYPEDDATAFILE_TYPE_Document_FORMAT_DOC = ".DOC";
	//.
	public static boolean TYPEDDATAFILE_TYPE_Document_CheckFormat(String Format) {
		return (Format.equals(TYPEDDATAFILE_TYPE_Document_FORMAT_XML) || Format.equals(TYPEDDATAFILE_TYPE_Document_FORMAT_TXT) || Format.equals(TYPEDDATAFILE_TYPE_Document_FORMAT_DOC));
	}
	//.
	public static final String TYPEDDATAFILE_TYPE_Image_String(Context context) {
		return context.getString(R.string.SImage1);   
	}
	public static final int TYPEDDATAFILE_TYPE_ImageName 		= 300;   
	public static final int TYPEDDATAFILE_TYPE_ImageBrief 		= 350;   
	public static final int TYPEDDATAFILE_TYPE_Image 			= 399;
	//.
	public static final String TYPEDDATAFILE_TYPE_Image_FORMAT_BMP 		= ".BMP";
	public static final String TYPEDDATAFILE_TYPE_Image_FORMAT_PNG 		= ".PNG";
	public static final String TYPEDDATAFILE_TYPE_Image_FORMAT_JPEG 	= ".JPG";
	public static final String TYPEDDATAFILE_TYPE_Image_FORMAT_JPEG1 	= ".JPEG";
	public static final String TYPEDDATAFILE_TYPE_Image_FORMAT_DRW 		= ".DRW";
	//.
	public static boolean TYPEDDATAFILE_TYPE_Image_CheckFormat(String Format) {
		return (Format.equals(TYPEDDATAFILE_TYPE_Image_FORMAT_BMP) || Format.equals(TYPEDDATAFILE_TYPE_Image_FORMAT_PNG) || Format.equals(TYPEDDATAFILE_TYPE_Image_FORMAT_JPEG) || Format.equals(TYPEDDATAFILE_TYPE_Image_FORMAT_JPEG1) || Format.equals(TYPEDDATAFILE_TYPE_Image_FORMAT_DRW));
	}
	//.
	public static final String TYPEDDATAFILE_TYPE_Audio_String(Context context) {
		return context.getString(R.string.SAudio1);   
	}
	public static final int TYPEDDATAFILE_TYPE_AudioName 		= 400;   
	public static final int TYPEDDATAFILE_TYPE_AudioBrief 		= 450;   
	public static final int TYPEDDATAFILE_TYPE_Audio 			= 499;   
	//.
	public static final String TYPEDDATAFILE_TYPE_Audio_FORMAT_WAV 	= ".WAV";
	public static final String TYPEDDATAFILE_TYPE_Audio_FORMAT_MP3 	= ".MP3";
	//.
	public static boolean TYPEDDATAFILE_TYPE_Audio_CheckFormat(String Format) {
		return (Format.equals(TYPEDDATAFILE_TYPE_Audio_FORMAT_WAV) || Format.equals(TYPEDDATAFILE_TYPE_Audio_FORMAT_MP3));
	}
	//.
	public static final String TYPEDDATAFILE_TYPE_Video_String(Context context) {
		return context.getString(R.string.SVideo2);   
	}
	public static final int TYPEDDATAFILE_TYPE_VideoName 		= 500;   
	public static final int TYPEDDATAFILE_TYPE_VideoBrief 		= 550;   
	public static final int TYPEDDATAFILE_TYPE_Video 			= 599;   
	//.
	public static final String TYPEDDATAFILE_TYPE_Video_FORMAT_AVI 		= ".AVI";
	public static final String TYPEDDATAFILE_TYPE_Video_FORMAT_WMV 		= ".WMV";
	public static final String TYPEDDATAFILE_TYPE_Video_FORMAT_MPEG 	= ".MPG";
	public static final String TYPEDDATAFILE_TYPE_Video_FORMAT_MPEG1 	= ".MPEG";
	public static final String TYPEDDATAFILE_TYPE_Video_FORMAT_3GP 		= ".3GP";
	public static final String TYPEDDATAFILE_TYPE_Video_FORMAT_MP4 		= ".MP4";
	//.
	public static boolean TYPEDDATAFILE_TYPE_Video_CheckFormat(String Format) {
		return (Format.equals(TYPEDDATAFILE_TYPE_Video_FORMAT_AVI) || Format.equals(TYPEDDATAFILE_TYPE_Video_FORMAT_WMV) || Format.equals(TYPEDDATAFILE_TYPE_Video_FORMAT_MPEG) || Format.equals(TYPEDDATAFILE_TYPE_Video_FORMAT_MPEG1) || Format.equals(TYPEDDATAFILE_TYPE_Video_FORMAT_3GP) || Format.equals(TYPEDDATAFILE_TYPE_Video_FORMAT_MP4));
	}
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
	//.
	public static class TTypedDataFileDescriptor {
		
		public int		DataType = TYPEDDATAFILE_TYPE_AllName;
		public String	DataFormat = null;
		//.
		public String	DataFile = null;
		
		public TTypedDataFileDescriptor() {
		}
		
		public TTypedDataFileDescriptor(String pDataFile) {
			DataFile = pDataFile;
			//.
			ParseFormat();
			ParseDataType();
		}
		
		private void ParseFormat() {
			DataFormat = "."+TFileSystem.FileName_GetExtension(DataFile).toUpperCase(Locale.US);		
		}
		
		private void ParseDataType() {
			if (TYPEDDATAFILE_TYPE_Document_CheckFormat(DataFormat))
				DataType = TYPEDDATAFILE_TYPE_Document; 
			else
				if (TYPEDDATAFILE_TYPE_Image_CheckFormat(DataFormat))
					DataType = TYPEDDATAFILE_TYPE_Image; 
				else
					if (TYPEDDATAFILE_TYPE_Audio_CheckFormat(DataFormat))
						DataType = TYPEDDATAFILE_TYPE_Audio; 
					else
						if (TYPEDDATAFILE_TYPE_Video_CheckFormat(DataFormat))
							DataType = TYPEDDATAFILE_TYPE_Video; 
		}
		
		public File GetFile() {
			if (DataFile == null)
				return null; //. ->
			return (new File(DataFile));
		}
	}
	//.
	public static class TTypedDataFile {
		
		public String	DataFile = null;
		//.
		public TTypedDataFileDescriptor Descriptor = null;
		
		public TTypedDataFile() {
		}
		
		public TTypedDataFile(String pDataFile) {
			DataFile = pDataFile;
			//.
			Descriptor = GetDescriptor();
		}
		
	    private TTypedDataFileDescriptor GetDescriptor() {
	    	if ((DataFile == null) || (!DataFile.contains(".")))
	    			return null; //. ->
	    	return (new TTypedDataFileDescriptor(DataFile));
	    }

	    public synchronized String GetName(Context context) {
	    	if (Descriptor == null)
	    		return context.getString(R.string.SDataFile); //. ->
	    	return SpaceDefines.TYPEDDATAFILE_TYPE_String(Descriptor.DataType,context);
	    }

	    public synchronized Bitmap GetImage(int pWidth, int pHeight) throws Exception {
	    	if (Descriptor == null)
	    		return null; //. ->
	    	//.
			final int IMAGE_DATAKIND_BITMAP   = 0;
			final int IMAGE_DATAKIND_DRAWINGS = 1;
			//.
			Bitmap Result = null;
			//. 
			switch (Descriptor.DataType) {

			case SpaceDefines.TYPEDDATAFILE_TYPE_Image:
				int DataKind = IMAGE_DATAKIND_BITMAP;
				if ((Descriptor.DataFormat != null) && Descriptor.DataFormat.toUpperCase(Locale.US).equals(TDrawingDefines.DataFormat)) 
					DataKind = IMAGE_DATAKIND_DRAWINGS;
				//.
				File F = new File(DataFile);
				if ((!F.exists()) || (F.length() == 0))
					return null; //. ->
				FileInputStream FIS = new FileInputStream(F);
				try {
					switch (DataKind) {
					
					case IMAGE_DATAKIND_DRAWINGS:
						byte[] Data = new byte[(int)F.length()];
						FIS.read(Data);
						//.
						TDrawings Drawings = new TDrawings();
						Drawings.LoadFromByteArray(Data,0);
						Result = Drawings.ToBitmap(pWidth);
						break; //. >
						
					default:
						BitmapFactory.Options options = new BitmapFactory.Options();
						options.inDither=false;
						options.inPurgeable=true;
						options.inInputShareable=true;
						options.inTempStorage=new byte[1024*1024*3]; 							
						Rect rect = new Rect();
						Bitmap bitmap = BitmapFactory.decodeFileDescriptor(FIS.getFD(), rect, options);
						try {
							int ImageMaxSize = options.outWidth;
							if (options.outHeight > ImageMaxSize)
								ImageMaxSize = options.outHeight;
							float MaxSize = pWidth;
							float Scale = MaxSize/ImageMaxSize; 
							Matrix matrix = new Matrix();     
							matrix.postScale(Scale,Scale);
							//.
							Result = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true); //. ->
						}
						finally {
							bitmap.recycle();
						}
					}
				}
				finally {
					FIS.close();
				}
				break; //. >
			}
			//.
			return Result;
	    }
	    
	    public synchronized int GetImageResID(int pWidth, int pHeight) {
	    	int Result = R.drawable.mappoidatafile_value;
	    	//.
	    	if (Descriptor == null)
	    		return Result; //. ->
			switch (Descriptor.DataType) {
			
			case SpaceDefines.TYPEDDATAFILE_TYPE_Document:
				Result = R.drawable.user_activity_component_list_placeholder_text;
				break; //. >
				
			case SpaceDefines.TYPEDDATAFILE_TYPE_Image:
				if ((Descriptor.DataFormat != null) && Descriptor.DataFormat.toUpperCase(Locale.US).equals(TDrawingDefines.DataFormat))
					Result = R.drawable.user_activity_component_list_placeholder_image_drawing;
				else 
					Result = R.drawable.user_activity_component_list_placeholder_image;
				break; //. >
				
			case SpaceDefines.TYPEDDATAFILE_TYPE_Audio:
				Result = R.drawable.user_activity_component_list_placeholder_audio;
				break; //. >
				
			case SpaceDefines.TYPEDDATAFILE_TYPE_Video:
				Result = R.drawable.user_activity_component_list_placeholder_video;
				break; //. >
				
			default:
				Result = R.drawable.user_activity_component_list_placeholder;
				break; //. >
			}
	    	return Result;
	    }
	}
	//. Base component types
	public static final int 	idTDataStream = 2086;
	public static final String 	nmTDataStream = "Data stream";
	
	public static final int 	idTTileServerVisualization = 2085;
	public static final String 	nmTTileServerVisualization = "Tile-Server-visualization";

	public static final int 	idTMapFormatObject = 2077;
	public static final String 	nmTMapFormatObject = "Map-format object";

	public static final int 	idTMapFormatMap = 2076;
	public static final String 	nmTMapFormatMap = "Map-format map";
	
	public static final int 	idTGeoGraphServerObject = 2073;
	public static final String 	nmTGeoGraphServerObject = "Geograph server object";
	
	public static final int 	idTGeoSpace = 2072;
	public static final String 	nmTGeoSpace = "GeoSpace";
	
	public static final int 	idTGeoGraphServer = 2070;
	public static final String 	nmTGeoGraphServer = "Geograph server";
	
	public static final int 	idTGeoCrdSystem = 2069;
	public static final String 	nmTGeoCrdSystem = "Geo coordinate system";
	
	public static final int 	idTHINTVisualization = 2067;
	public static final String 	nmTHINTVisualization = "Hint-visualization";
	  
	public static final int 	idTDetailedPictureVisualization = 2065;
	public static final String	nmTDetailedPictureVisualization = "Detailed-picture-visualization";
	  
	public static final int 	idTMODELServer = 2052;
	public static final String 	nmTMODELServer = "GeoScope Server";

	public static final int 	idTPositioner = 2050;
	public static final String 	nmTPositioner = "Positioner";
	
	public static final int 	idTWMFVisualization = 2046;
	public static final String 	nmTWMFVisualization = "WMF-visualization";
	
	public static final int 	idTPictureVisualization = 2045;
	public static final String 	nmTPictureVisualization = "Picture-visualization";
	  
	public static final int 	idTGeodesyPoint = 2043;
	public static final String 	nmTGeodesyPoint = "Geodesy Point";

	public static final int 	idTModelUser = 2019;
	public static final String 	nmTModelUser = "User";
	
	public static final int 	idTDATAFile = 2018;
	public static final String 	nmTDATAFile = "DataFile";
	
	public static final int 	idTCoComponent = 2015;
	public static final String 	nmTCoComponent = "Co-Component"; 

	public static final int 	idTTTFVisualization = 2006;
	public static final String 	nmTTTFVisualization = "TTF-visualization";

	public static final int 	idTVisualization = 2004;
	public static final String 	nmTVisualization = "Visualization"; 
	
	public static final String ComponentType_GetName(int ComponentType, Context context) {
		switch (ComponentType) {
		
		case idTPositioner:
			return context.getString(R.string.SPlace); //. ->
			
		default:
			return null; //. ->
		}
	}
	//. Co-Component types (idTCoComponent)
	public static final int 	idTCoGeoMonitorObject = 1111143;
	public static final String 	nmTCoGeoMonitorObject = "Geo monitor object"; 
}
