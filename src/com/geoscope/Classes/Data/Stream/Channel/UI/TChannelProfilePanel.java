package com.geoscope.Classes.Data.Stream.Channel.UI;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoEye.R;

@SuppressLint("HandlerLeak")
public class TChannelProfilePanel extends Activity {

	private TChannel.TProfile Profile;
	//.
	@SuppressWarnings("unused")
	private TextView lbName;
	private CheckBox cbEnabled;
	private Button btnApplyChanges;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
    	try {
            Bundle extras = getIntent().getExtras(); 
        	byte[] ProfileData  = extras.getByteArray("ProfileData");
        	Profile = new TChannel.TProfile(ProfileData);
        	Profile.FromByteArray(ProfileData);
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            //.
            setContentView(R.layout.channel_profile_panel);
            //.
            lbName = (TextView)findViewById(R.id.lbName);
            //.
            cbEnabled = (CheckBox)findViewById(R.id.cbEnabled);
            //.
            btnApplyChanges = (Button)findViewById(R.id.btnApplyChanges);
            btnApplyChanges.setOnClickListener(new OnClickListener() {
            	
            	@Override
                public void onClick(View v) {
                	try {
                		ApplyChangesAndExit();
    				} catch (Exception E) {
    					Toast.makeText(TChannelProfilePanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    				}
                }
            });
		} catch (Exception E) {
			Toast.makeText(TChannelProfilePanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			//.
			finish();
			//.
			return; //. ->
		}
		setResult(Activity.RESULT_CANCELED);
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }

    @Override
    protected void onResume() {
    	super.onResume();
    	//.
    	Update();
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    }
    
    private void Update() {
    	cbEnabled.setChecked(Profile.Enabled);
    }

    private void ApplyChangesAndExit() throws Exception {
    	Profile.Enabled = cbEnabled.isChecked(); 
    	//.
    	Intent intent = getIntent();
    	intent.putExtra("ProfileData",Profile.ToByteArray());
        //.
    	setResult(Activity.RESULT_OK,intent);
    	//.
    	finish();
    }
}
