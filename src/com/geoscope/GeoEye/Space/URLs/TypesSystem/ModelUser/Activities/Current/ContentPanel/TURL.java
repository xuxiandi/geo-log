package com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Current.ContentPanel;

import org.w3c.dom.Element;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TUserActivityComponentListPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;

public class TURL extends com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Current.TURL {

	public static final String TypeID = com.geoscope.GeoEye.Space.URLs.TypesSystem.ModelUser.Activities.Current.TURL.TypeID+"."+"ContentPanel";
	
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
	public int GetThumbnailImageResID() {
		return R.drawable.user_activity_content; //. ->
	}
	
	@Override
	public void Open(Context context) throws Exception {
		TAsyncProcessing Opening = new TAsyncProcessing(context) {
			
			private TGeoScopeServerUser.TUserDescriptor.TActivity UserCurrentActivity;
			
			@Override
			public void Process() throws Exception {
				UserCurrentActivity = User.GetUserCurrentActivity(idComponent);
				//.
	    		Thread.sleep(100); 
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				if (Canceller.flCancel)
					return; //. ->
				//.
            	Intent intent = new Intent(context, TUserActivityComponentListPanel.class);
            	TReflectorComponent Component = TReflectorComponent.GetAComponent(); 
            	if (Component != null)
            		intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("UserID",idComponent);
            	intent.putExtra("ActivityID",UserCurrentActivity.ID);
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
