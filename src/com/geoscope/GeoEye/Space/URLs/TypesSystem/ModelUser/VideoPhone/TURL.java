package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.VideoPhone;

import org.w3c.dom.Element;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TUserPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.TURL.TypeID+"."+"VideoPhone";
	
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
	public boolean IsAvailable() {
		return IsUserAvailable();
	}
	
	@Override
	public int GetThumbnailImageResID(int ImageMaxSize) {
		return R.drawable.offlineuservideophone;
	}
	
	@Override
	public TThumbnailImageComposition GetThumbnailImageComposition() {
		if (IsUserAvailable())
			return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.onlineuservideophone))); //. ->
		else
			return (new TThumbnailImageComposition(BitmapFactory.decodeResource(User.Server.context.getResources(), R.drawable.offlineuservideophone))); //. ->
	}
	
	@Override
	public void Open(Context context, Object Params) throws Exception {
		TAsyncProcessing Opening = new TAsyncProcessing(context) {
			
			private TUserPanel.TUserCoGeoMonitorObjects UserCoGeoMonitorObjects = null;
			
			@Override
			public void Process() throws Exception {
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent != null) {
					TUserDescriptor UserInfo = User.GetUserInfo(idComponent);
					if (UserInfo.UserIsOnline) {
						UserCoGeoMonitorObjects = new TUserPanel.TUserCoGeoMonitorObjects();
						//.
						UserCoGeoMonitorObjects.Update(UserAgent, idComponent);
					}
				}
				//.
	    		Thread.sleep(100); 
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				//.
				if ((UserCoGeoMonitorObjects != null) && (UserCoGeoMonitorObjects.CommunicationObject != null)) 
					if (UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserVideoPhone_IsSupported())
						UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserVideoPhone_Start(UserCoGeoMonitorObjects.CommunicationObject.Object, context);            	
					else
						throw new Exception(context.getString(R.string.SVideoPhoneIsNotSupportedForUser)); //. =>
				else
					throw new Exception(context.getString(R.string.SUserIsNotAvailableForVideoPhoneCommunication)); //.=>
			}

			@Override
			public void DoOnException(Exception E) {
                Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Opening.Start();
	}
	
	public boolean IsUserAvailable() {
		boolean flOnline = false;
		try {
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent != null) {
				TUserDescriptor UserInfo = UserAgent.Server.User.GetUserInfo(idComponent);
				if (UserInfo.UserIsOnline) {
					TUserPanel.TUserCoGeoMonitorObjects _UserCoGeoMonitorObjects = new TUserPanel.TUserCoGeoMonitorObjects();
					//.
					_UserCoGeoMonitorObjects.Update(UserAgent, idComponent);
					//.
					flOnline = ((_UserCoGeoMonitorObjects.CommunicationObject != null) && _UserCoGeoMonitorObjects.CommunicationObject.ObjectModel.UserVideoPhone_IsSupported());
				}
			}
		}
		catch (Exception E) {
		}
		return flOnline;
	}
}
