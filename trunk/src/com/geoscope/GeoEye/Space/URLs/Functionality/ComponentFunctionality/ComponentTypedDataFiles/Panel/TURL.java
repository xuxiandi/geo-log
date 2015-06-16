package com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.Panel;

import java.util.ArrayList;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
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
	public TThumbnailImageComposition GetThumbnailImageComposition() {
		TThumbnailImageComposition Result = null; 
		Bitmap ResultBMP = null;
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
								Result = CF.GetThumbnailImageComposition();
								if (Result != null) {
									ResultBMP = Result.BMP;
									flProcessAsDefault = false;
								}
								else
									flProcessAsDefault = true;
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
						ResultBMP = Drawings.ToBitmap(ThumbnailImage_Size);
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
				if (Cnt > 0) {
					Result = TComponentTypedDataFiles.GetImageComposition(ImageDataFiles, ThumbnailImage_Size);
					if (Result != null)
						ResultBMP = Result.BMP;
				}
			}
			//.
			int ResourceImageID = 0;
			TTypeFunctionality TF = User.Space.TypesSystem.TTypeFunctionality_Create(idTComponent);
			if (TF != null)
				try {
					ResourceImageID = TF.GetImageResID();
				} finally {
					TF.Release();
				}
			if (ResourceImageID == 0) {
				ResourceImageID = SpaceDefines.TYPEDDATAFILE_TYPE_GetResID(DataFile.DataType,DataFile.DataFormat);
				if (ResourceImageID == 0)
					ResourceImageID = R.drawable.user_activity_component_list_placeholder_componentpropspanel;
			}
			//.
			if (ResultBMP != null) {
				int ImageSize = ResultBMP.getWidth();
				Drawable D = User.Server.context.getResources().getDrawable(ResourceImageID).mutate();
				D.setBounds(0,0, (ImageSize >> 2),(ImageSize >> 2));
				D.setAlpha(128);
				Bitmap LastResult = ResultBMP;
				ResultBMP = ResultBMP.copy(Config.ARGB_8888,true);
				LastResult.recycle();
				D.draw(new Canvas(ResultBMP));
			}
			else
				ResultBMP = BitmapFactory.decodeResource(User.Server.context.getResources(), ResourceImageID);
		}
		catch (Exception E) {
			ResultBMP = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_componentpropspanel);
		}
		//.
		if (Result == null)
			Result = new TThumbnailImageComposition(ResultBMP);
		else
			Result.BMP = ResultBMP; //. update the bitmap to ensure that it does not recycled
		//.
		return Result;
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition(int ImageMaxSize) {
		return GetThumbnailImageComposition();
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