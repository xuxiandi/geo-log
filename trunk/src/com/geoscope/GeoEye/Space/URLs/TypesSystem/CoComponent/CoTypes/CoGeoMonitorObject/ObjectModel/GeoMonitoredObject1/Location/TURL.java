package com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.Location;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.ObjectModel.GeoMonitoredObject1.TURL.TypeID+"."+"Location";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode));
	}
	
	public static class TObjectParams {
		
		public boolean IsOnline = false;
		public boolean FixIsAvailable = false;
		public int UserAlert = 0;
	}
	
	
	public TURL(long pidComponent) {
		super(pidComponent);
	}

	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}

	@Override
	public String GetTypeID() {
		return TypeID;
	}
		
	@Override
	public boolean IsAvailable() {
		TObjectParams ObjectParams = GetObjectParams();
		return ObjectParams.IsOnline;
	}
	
	@Override
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.user_activity_component_list_placeholder_cocomponent_geomonitorobject1_offline;
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() {
		TObjectParams ObjectParams = GetObjectParams();
		if (ObjectParams.IsOnline)
			if (ObjectParams.FixIsAvailable)
				return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_cocomponent_geomonitorobject1_online_location))); //. ->
			else
				return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_cocomponent_geomonitorobject1_online_nolocation))); //. ->
		else
			return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_cocomponent_geomonitorobject1_offline))); //. ->
	}
	
	@Override
	public void Open(Context context, Object Params) throws Exception {
		TAsyncProcessing Opening = new TAsyncProcessing(context) {

			private TXYCoord Crd = null;

			@Override
			public void Process() throws Exception {
				TCoGeoMonitorObject Object = new TCoGeoMonitorObject(User.Server, idComponent);
				Crd = Object.GetComponentLocation(context);
				//.
				Thread.sleep(100);
			}
			
			@Override
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				if (Crd != null) {
					Intent intent = new Intent(context, TReflector.class);
					intent.putExtra("Reason", TReflectorComponent.REASON_SHOWLOCATION);
					intent.putExtra("LocationXY", Crd.ToByteArray());
					context.startActivity(intent);
				}
			}
			
			@Override
			public void DoOnException(Exception E) {
                Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Opening.Start();
	}
	
	public TObjectParams GetObjectParams() {
		TObjectParams Result = new TObjectParams();
		try {
			TCoGeoMonitorObject Object = new TCoGeoMonitorObject(User.Server, idComponent);
			byte[] ObjectData = Object.GetData(0);
			//.
			Result.IsOnline = (ObjectData[0] > 0);
			Result.FixIsAvailable = (ObjectData[1] > 0);
			Result.UserAlert = TDataConverter.ConvertLEByteArrayToInt32(ObjectData,2);
		}
		catch (Exception E) {
		}
		return Result;
	}
}
