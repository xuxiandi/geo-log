package com.geoscope.GeoEye.Space.TypesSystem.SecurityFile;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.SecurityFile.TSecurityFileInstanceListComponent.TInstanceListItem;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;

public class TSecurityFileInstanceListPanel extends Activity {

	public boolean flExists = false;
	//.
	private TSecurityFileInstanceListComponent ListComponent = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        try {
        	TUserAgent UserAgent = TUserAgent.GetUserAgent();
    		if (UserAgent == null)
    			throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
    		//.
    		String Context = "";
            Bundle extras = getIntent().getExtras(); 
            if (extras != null) {
            	Context = extras.getString("Context");
            }
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            //. 
            setContentView(R.layout.securityfile_instancelist_panel);
            //.
            LinearLayout ParentLayout = (LinearLayout)findViewById(R.id.llParent);
            //.
    		ListComponent = new TSecurityFileInstanceListComponent(this, ParentLayout, Context, TSecurityFileInstanceListComponent.LIST_ROW_SIZE_SMALL_ID, new TSecurityFileInstanceListComponent.TOnListItemClickHandler() {

    			@Override
    			public void DoOnListItemClick(final TInstanceListItem Item) {
	    			AlertDialog.Builder alert = new AlertDialog.Builder(TSecurityFileInstanceListPanel.this);
	    			//.
	    			alert.setTitle(R.string.SConfirmation);
	    			alert.setMessage(R.string.SConfirmOperation);
	    			//.
	    			alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
	    				
	    				@Override
	    				public void onClick(DialogInterface dialog, int whichButton) {
	                    	Intent intent = TSecurityFileInstanceListPanel.this.getIntent();
	                    	intent.putExtra("SecurityFileID", Item.ID);
	                        //.
	                    	setResult(Activity.RESULT_OK,intent);
	                		//.
	                    	finish();
	    				}
	    			});
	    			//.
	    			alert.setNegativeButton(R.string.SCancel, null);
	    			//.
	    			alert.show();
    			}
    		});
            //.
            setResult(RESULT_CANCELED);
            //.
            flExists = true;
            //.
            ListComponent.Start();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			// .
			finish();
			return; // . ->
		}
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//. 
		try {
			if (ListComponent != null) {
				ListComponent.Destroy();
				ListComponent = null;
			}
		} catch (Exception E) {
		}
		//.
		super.onDestroy();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//.
		ListComponent.DoOnResume();
	}
	
	@Override
	protected void onPause() {
		ListComponent.DoOnPause();
		//.
		super.onPause();
	}
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case TSecurityFileInstanceListComponent.REQUEST_COMPONENT_CONTENT: 
        	if (resultCode == RESULT_OK)
				try {
					ListComponent.Restart();
				} catch (Exception E) {
				}
        	break; //. >
        }
    }
}
