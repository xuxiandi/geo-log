package com.geoscope.GeoEye.Space.URLs.TypesSystem.Positioner.Panel;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;

import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.Positioner.TPositionerFunctionality;

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
	
	@Override
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.user_activity_component_list_placeholder_component_positioner;
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() {
		return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.user_activity_component_list_placeholder_component_positioner))); 
	}
	
	@Override
	public void Open(Context context, Object Params) throws Exception {
		TReflector Reflector = TReflector.GetReflector();
		if (Reflector == null) 
			throw new Exception(context.getString(R.string.SReflectorIsNull)); //. =>
		//.
		TLocation P = new TLocation(PF._Name);
		P.RW.Assign(Reflector.Component.ReflectionWindow.GetWindow());
		P.RW.X0 = PF._X0; P.RW.Y0 = PF._Y0;
		P.RW.X1 = PF._X1; P.RW.Y1 = PF._Y1;
		P.RW.X2 = PF._X2; P.RW.Y2 = PF._Y2;
		P.RW.X3 = PF._X3; P.RW.Y3 = PF._Y3;
		P.RW.BeginTimestamp = PF._Timestamp; P.RW.EndTimestamp = PF._Timestamp;
		P.RW.Normalize();
		Intent intent = new Intent(context, TReflector.class);
		intent.putExtra("Reason", TReflectorComponent.REASON_SHOWLOCATIONWINDOW);
		intent.putExtra("LocationWindow", P.ToByteArray());
		context.startActivity(intent);
	}
}
