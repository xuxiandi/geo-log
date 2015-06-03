package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Messaging;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TUserChatPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL.TypeID+"."+"Messaging";
	
	public static boolean IsTypeOf(String pTypeID) {
		return (pTypeID.startsWith(TypeID));
	}
	
	public static TURL GetURL(String TypeID, TGeoScopeServerUser pUser, Element pXMLDocumentRootNode) throws Exception {
		return (new TURL(pUser,pXMLDocumentRootNode));
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
		return R.drawable.offlineusermessaging;
	}
	
	@Override
	public Bitmap GetThumbnailImage() {
		try {
			TUserDescriptor UserInfo = User.GetUserInfo(idComponent);
			if (UserInfo.UserIsOnline)
				return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.onlineusermessaging); //. ->
			else
				return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineusermessaging); //. ->
		}
		catch (Exception E) {
			return BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineusermessaging); //. ->
		}
	}
	
	@Override
	public void Open(Context context) throws Exception {
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
				TUserChatPanel UCP = TUserChatPanel.Panels.get(User.UserID);
				if (UCP != null)
					UCP.finish();
		    	Intent intent = new Intent(context, TUserChatPanel.class);
		    	intent.putExtra("UserID",UserInfo.UserID);
		    	intent.putExtra("UserIsDisabled",UserInfo.UserIsDisabled);
		    	intent.putExtra("UserIsOnline",UserInfo.UserIsOnline);
		    	intent.putExtra("UserName",UserInfo.UserName);
		    	intent.putExtra("UserFullName",UserInfo.UserFullName);
		    	intent.putExtra("UserContactInfo",UserInfo.UserContactInfo);
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
