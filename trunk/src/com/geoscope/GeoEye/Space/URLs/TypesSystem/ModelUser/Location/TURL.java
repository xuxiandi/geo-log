package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Location;

import org.w3c.dom.Element;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TUserPanel.TUserLocationGetting;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL.TypeID+"."+"Location";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode)); //. ->
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
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.offlineuserlocation;
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() {
		try {
			TUserDescriptor UserInfo = User.GetUserInfo(idComponent);
			if (UserInfo.UserIsOnline)
				return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.onlineuserlocation))); //. ->
			else
				return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineuserlocation))); //. ->
		}
		catch (Exception E) {
			return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineuserlocation))); //. ->
		}
	}
	
	@Override
	public void Open(Context context, Object Params) throws Exception {
		TAsyncProcessing Opening = new TAsyncProcessing(context) {
			
			private TUserDescriptor UserInfo;
			
			@Override
			public void Process() throws Exception {
				UserInfo = User.GetUserInfo(idComponent);
				//.
	    		Thread.sleep(100); 
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				//.
		    	new TUserLocationGetting(context, UserInfo);
			}

			@Override
			public void DoOnException(Exception E) {
                Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Opening.Start();
	}
}
