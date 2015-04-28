package com.geoscope.GeoEye.Space.URLs.Reflector.ElectedPlace.Panel;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.Reflector.ElectedPlace.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.Reflector.ElectedPlace.TURL.TypeID+"."+"Panel";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode));
	}


	public TURL(TData pData) {
		super(pData);
	}
	
	public TURL(TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		super(pUser,pXMLDocumentRootNode);
	}

	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
	public Bitmap GetThumbnailImage() {
		return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_component_positioner);
	}
	
	@Override
	public void Open(Context context) throws Exception {
		TReflector Reflector = TReflector.GetReflector();
		if (Reflector == null) 
			throw new Exception(context.getString(R.string.SReflectorIsNull)); //. =>
		//.
		TLocation P = new TLocation(Data.Name);
		P.RW.Assign(Reflector.Component.ReflectionWindow.GetWindow());
		P.RW.X0 = Data.RW_X0; P.RW.Y0 = Data.RW_Y0;
		P.RW.X1 = Data.RW_X1; P.RW.Y1 = Data.RW_Y1;
		P.RW.X2 = Data.RW_X2; P.RW.Y2 = Data.RW_Y2;
		P.RW.X3 = Data.RW_X3; P.RW.Y3 = Data.RW_Y3;
		P.RW.BeginTimestamp = Data.RW_Timestamp; P.RW.EndTimestamp = Data.RW_Timestamp;
		P.RW.Normalize();
		Intent intent = new Intent(context, TReflector.class);
		intent.putExtra("Reason", TReflectorComponent.REASON_SHOWLOCATIONWINDOW);
		intent.putExtra("LocationWindow", P.ToByteArray());
		context.startActivity(intent);
	}
}
