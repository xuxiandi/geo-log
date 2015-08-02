package com.geoscope.GeoEye.Space.URLs.TypesSystem.Positioner.Panel;

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
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFilesPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.Positioner.TPositionerFunctionality;
import com.geoscope.GeoEye.Space.URLs.Functionality.ComponentFunctionality.ComponentTypedDataFiles.Panel.TURL.TOpenComponentTypedDataFileParams;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.Positioner.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.Positioner.TURL.TypeID+"."+"Panel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode));
	}
	

	public TURL(long pidComponent, TPositionerFunctionality pPF) {
		super(pidComponent,pPF);
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
	
	@Override
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.user_activity_component_list_placeholder_component_positioner;
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() {
		TThumbnailImageComposition Result = null; 
		Bitmap ResultBMP = null;
		try {
			TComponentTypedDataFiles TypedDataFiles = new TComponentTypedDataFiles(User.Server.context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION,SpaceDefines.TYPEDDATAFILE_TYPE_Image);
			TypedDataFiles.PrepareForComponent(SpaceDefines.idTPositioner,idComponent, ThumbnailImage_DataParams, true, User.Server);
			ArrayList<TComponentTypedDataFile> ImageDataFiles = TypedDataFiles.GetItemsByDataType(SpaceDefines.TYPEDDATAFILE_TYPE_Image);
			int Cnt = ImageDataFiles.size();
			if (Cnt > 0) {
				Result = TComponentTypedDataFiles.GetImageComposition(ImageDataFiles, ThumbnailImage_Size);
				if (Result != null)
					ResultBMP = Result.BMP;
			}
			//.
			int ResourceImageID = 0;
			TTypeFunctionality TF = User.Space.TypesSystem.TTypeFunctionality_Create(SpaceDefines.idTPositioner);
			if (TF != null)
				try {
					ResourceImageID = TF.GetImageResID();
				} finally {
					TF.Release();
				}
			if (ResourceImageID == 0)
				ResourceImageID = R.drawable.user_activity_component_list_placeholder_component_positioner;
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
			ResultBMP = BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_component_positioner);
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
	public void Open(Context context, Object Params) throws Exception {
		if (Params == null) {
			TAsyncProcessing Opening = new TAsyncProcessing(context) {
				
				private TComponentTypedDataFiles TypedDataFiles;
				
				@Override
				public void Process() throws Exception {
					TypedDataFiles = new TComponentTypedDataFiles(context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_AllName);
					TypedDataFiles.PrepareForComponent(SpaceDefines.idTPositioner,idComponent, true, User.Server);
				}
				
				@Override 
				public void DoOnCompleted() throws Exception {
					if (TypedDataFiles.Count() > 1) {
						Intent intent = new Intent(context, TComponentTypedDataFilesPanel.class);
				    	TReflectorComponent Reflector = TReflectorComponent.GetAComponent();
				    	if (Reflector != null)
				    		intent.putExtra("ComponentID", Reflector.ID);
						intent.putExtra("DataFiles", TypedDataFiles.ToByteArrayV0());
						intent.putExtra("AutoStart", true);
						//.
						context.startActivity(intent);
					}
					else
						if (TypedDataFiles.Count() == 1) {
							final TComponentTypedDataFile ComponentTypedDataFile = TypedDataFiles.GetRootItem();
							if (ComponentTypedDataFile.IsLoaded())
								ComponentTypedDataFile.Open(User, context);
							else {
								TAsyncProcessing Opening = new TAsyncProcessing(context) {
									
									@Override
									public void Process() throws Exception {
										ComponentTypedDataFile.PrepareAsFullFromServer(User, Canceller, null/*Progressor*/);
									}
									
									@Override 
									public void DoOnCompleted() throws Exception {
										ComponentTypedDataFile.Open(User, context);
									}
									
									@Override
									public void DoOnException(Exception E) {
						    			Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
									}
								};
								Opening.Start();
							}
						}
				}
				
				@Override
				public void DoOnException(Exception E) {
	    			Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
				}
			};
			Opening.Start();
		}
		else
			if (Params instanceof TOpenComponentTypedDataFileParams) {
				final TOpenComponentTypedDataFileParams OpenComponentTypedDataFileParams = (TOpenComponentTypedDataFileParams)Params;
				//.
				if (OpenComponentTypedDataFileParams.ComponentTypedDataFile.IsLoaded())
					OpenComponentTypedDataFileParams.ComponentTypedDataFile.Open(User, OpenComponentTypedDataFileParams.ParentActivity);
				else {
					TAsyncProcessing Opening = new TAsyncProcessing(context) {
						
						@Override
						public void Process() throws Exception {
							OpenComponentTypedDataFileParams.ComponentTypedDataFile.PrepareAsFullFromServer(User, Canceller, null/*Progressor*/);
						}
						
						@Override 
						public void DoOnCompleted() throws Exception {
							OpenComponentTypedDataFileParams.ComponentTypedDataFile.Open(User, OpenComponentTypedDataFileParams.ParentActivity);
						}
						
						@Override
						public void DoOnException(Exception E) {
			    			Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
						}
					};
					Opening.Start();
				}
			}
	}
}
