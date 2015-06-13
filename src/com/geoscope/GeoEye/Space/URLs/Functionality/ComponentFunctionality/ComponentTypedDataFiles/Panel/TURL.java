package com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.Panel;

import java.util.ArrayList;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFilesPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;

public class TURL extends com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.TURL.TypeID+"."+"Panel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode)); //. ->
	}
	
	
	public TURL(int	pidTComponent, long pidComponent) {
		super(pidTComponent,pidComponent);
	}
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}

	@Override
	public String GetTypeID() {
		return TypeID;
	}

	private static final int		ThumbnailImage_Size = 512;
	private static final String 	ThumbnailImage_DataParams = "2;"+Integer.toString(ThumbnailImage_Size)+";"+"50"/*50% quality*/;
	//.
	private static final int		ThumbnailImage_Drawings_MaxDataSize = 1024*100; //. Kb
	private static final String 	ThumbnailImage_Drawings_ItemImageDataParams = "0;"+Integer.toString(ThumbnailImage_Drawings_MaxDataSize);
	
	@Override
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.user_activity_component_list_placeholder_data;
	}
	
	@Override
	public Bitmap GetThumbnailImage() {
		Bitmap Result = null;
		try {
			TComponentTypedDataFiles TypedDataFiles = new TComponentTypedDataFiles(User.Server.context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_AllName);
			TypedDataFiles.PrepareForComponent(idTComponent,idComponent, false, User.Server);
			boolean flProcessAsDefault = true;
			TComponentTypedDataFile DataFile = TypedDataFiles.GetRootItem(); 
			switch (DataFile.DataType) {

			case SpaceDefines.TYPEDDATAFILE_TYPE_DocumentName:
				if ((DataFile.DataFormat != null) && DataFile.DataFormat.equals(SpaceDefines.TYPEDDATAFILE_TYPE_Document_FORMAT_XML)) {
					TypedDataFiles = new TComponentTypedDataFiles(User.Server.context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION,SpaceDefines.TYPEDDATAFILE_TYPE_Document);
					TypedDataFiles.PrepareForComponent(idTComponent,idComponent, false, User.Server);
					//.
					TComponentTypedDataFile _DataFile = TypedDataFiles.GetRootItem(); 
					if ((_DataFile != null) && _DataFile.DataFormat.equals(SpaceDefines.TYPEDDATAFILE_TYPE_Document_FORMAT_XML)) {
						TComponentFunctionality CF = User.Space.TypesSystem.TComponentFunctionality_Create(_DataFile.DataComponentType,_DataFile.DataComponentID);
						if (CF != null) 
							try {
								CF.ParseFromXMLDocument(_DataFile.GetFileData());
								Result = CF.GetThumbnailImage();
								flProcessAsDefault = (Result == null);
							}
							finally {
								CF.Release();
							}
					}
				}
				break; //. >
				
			case SpaceDefines.TYPEDDATAFILE_TYPE_ImageName:
				if ((DataFile.DataFormat != null) && DataFile.DataFormat.equals(TDrawingDefines.DataFormat)) {
					TypedDataFiles = new TComponentTypedDataFiles(User.Server.context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION,SpaceDefines.TYPEDDATAFILE_TYPE_Image);
					TypedDataFiles.PrepareForComponent(idTComponent,idComponent, ThumbnailImage_Drawings_ItemImageDataParams, false, User.Server);
					TComponentTypedDataFile _DataFile = TypedDataFiles.GetRootItem(); 
					if ((_DataFile != null) && (_DataFile.Data != null)) {
						TDrawings Drawings = new TDrawings();
						Drawings.LoadFromByteArray(_DataFile.Data,0);
						Result = Drawings.ToBitmap(ThumbnailImage_Size);
					}
					flProcessAsDefault = false;
				}
				break; //. >
				
			case SpaceDefines.TYPEDDATAFILE_TYPE_AudioName:
			case SpaceDefines.TYPEDDATAFILE_TYPE_MeasurementName:
				flProcessAsDefault = false;
				break; //. >
			}
			//.
			if (flProcessAsDefault) {
				TypedDataFiles = new TComponentTypedDataFiles(User.Server.context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION,SpaceDefines.TYPEDDATAFILE_TYPE_Image);
				TypedDataFiles.PrepareForComponent(idTComponent,idComponent, ThumbnailImage_DataParams, true, User.Server);
				ArrayList<TComponentTypedDataFile> ImageDataFiles = TypedDataFiles.GetItemsByDataType(SpaceDefines.TYPEDDATAFILE_TYPE_Image);
				int Cnt = ImageDataFiles.size();
				switch (Cnt) {
				
				case 0:
					break; //. >
					
				case 1:
					TComponentTypedDataFile ImageDataFile = ImageDataFiles.get(0); 
					if (ImageDataFile.Data != null) 
						Result = BitmapFactory.decodeByteArray(ImageDataFile.Data, 0,ImageDataFile.Data.length);
					break; //. >
					
				default:
					Result = TComponentTypedDataFilesPanel.GetImagesComposition(ImageDataFiles, ThumbnailImage_Size);
					break; //. >
				}
			}
			//.
			if (Result == null) {
				switch (idTComponent) {
				
				case SpaceDefines.idTPositioner:
					Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_component_positioner);
					break; //. >

				case SpaceDefines.idTMapFormatObject:
					Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_component_mapformatobject);
					break; //. >
				}
				if (Result == null) {
					switch (DataFile.DataType) {
					
					case SpaceDefines.TYPEDDATAFILE_TYPE_DocumentName:
						Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_text);
						break; //. >
						
					case SpaceDefines.TYPEDDATAFILE_TYPE_ImageName:
						if ((DataFile.DataFormat != null) && DataFile.DataFormat.equals(TDrawingDefines.DataFormat))
							Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_image_drawing);
						else 
							Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_image);
						break; //. >
						
					case SpaceDefines.TYPEDDATAFILE_TYPE_AudioName:
						Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_audio);
						break; //. >
						
					case SpaceDefines.TYPEDDATAFILE_TYPE_VideoName:
						Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_video);
						break; //. >
						
					case SpaceDefines.TYPEDDATAFILE_TYPE_MeasurementName:
						Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_measurement);
						break; //. >
						
					default:
						Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_componentpropspanel);
						break; //. >
					}
				}
			}
		}
		catch (Exception E) {
			Result = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_componentpropspanel);
		}
		return Result;
	}
	
	@Override
	public Bitmap GetThumbnailImage(int ImageMaxSize) {
		return GetThumbnailImage();
	}
	
	@Override
	public void Open(Context context) throws Exception {
		TAsyncProcessing Opening = new TAsyncProcessing(context) {
			
			private TComponentTypedDataFiles TypedDataFiles;
			
			@Override
			public void Process() throws Exception {
				TypedDataFiles = new TComponentTypedDataFiles(context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_AllName);
				TypedDataFiles.PrepareForComponent(idTComponent,idComponent, true, User.Server);
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				Intent intent = new Intent(context, TComponentTypedDataFilesPanel.class);
				intent.putExtra("ComponentID", 0);
				intent.putExtra("DataFiles", TypedDataFiles.ToByteArrayV0());
				intent.putExtra("AutoStart", true);
				//.
				context.startActivity(intent);
			}
			
			@Override
			public void DoOnException(Exception E) {
    			Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Opening.Start();
	}
}