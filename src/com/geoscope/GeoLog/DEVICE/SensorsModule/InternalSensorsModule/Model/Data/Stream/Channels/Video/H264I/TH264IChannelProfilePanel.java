package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Video.H264I;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;

@SuppressLint("HandlerLeak")
public class TH264IChannelProfilePanel extends Activity {

	private TH264IChannel.TMyProfile Profile;
	//.
	@SuppressWarnings("unused")
	private TextView lbName;
	private CheckBox cbEnabled;
	private EditText edWidth;
	private EditText edHeight;
	private EditText edFrameRate;
	private EditText edBitRate;
	private Button btnApplyChanges;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
    	try {
            Bundle extras = getIntent().getExtras(); 
        	byte[] ProfileData  = extras.getByteArray("ProfileData");
        	Profile = new TH264IChannel.TMyProfile(ProfileData);
        	Profile.FromByteArray(ProfileData);
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            //.
            setContentView(R.layout.sensorsmodule_channels_video_h264i_profile_panel);
            //.
            lbName = (TextView)findViewById(R.id.lbName);
            //.
            cbEnabled = (CheckBox)findViewById(R.id.cbEnabled);
            //.
            edWidth = (EditText)findViewById(R.id.edWidth);
            //.
            edHeight = (EditText)findViewById(R.id.edHeight);
            //.
            edFrameRate = (EditText)findViewById(R.id.edFrameRate);
            //.
            edBitRate = (EditText)findViewById(R.id.edBitRate);
            //.
            btnApplyChanges = (Button)findViewById(R.id.btnApplyChanges);
            btnApplyChanges.setOnClickListener(new OnClickListener() {
            	
            	@Override
                public void onClick(View v) {
                	try {
                		ApplyChangesAndExit();
    				} catch (Exception E) {
    					Toast.makeText(TH264IChannelProfilePanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    				}
                }
            });
		} catch (Exception E) {
			Toast.makeText(TH264IChannelProfilePanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
    	//.
		edWidth.setText(Integer.toString(Profile.Width));
		//.
		edHeight.setText(Integer.toString(Profile.Height));
		//.
		edFrameRate.setText(Integer.toString(Profile.FrameRate));
		//.
		edBitRate.setText(Integer.toString(Profile.BitRate));
    }

    private void ApplyChangesAndExit() throws Exception {
    	Profile.Enabled = cbEnabled.isChecked(); 
    	//.
    	Profile.Width = Integer.parseInt(edWidth.getText().toString());
    	//.
    	Profile.Height = Integer.parseInt(edHeight.getText().toString());
    	//.
    	Profile.FrameRate = Integer.parseInt(edFrameRate.getText().toString());
    	//.
    	Profile.BitRate = Integer.parseInt(edBitRate.getText().toString());
    	//.
    	Intent intent = getIntent();
    	intent.putExtra("ProfileData",Profile.ToByteArray());
        //.
    	setResult(Activity.RESULT_OK,intent);
    	//.
    	finish();
    }
}
