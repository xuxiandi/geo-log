package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Audio.AAC;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;

@SuppressLint("HandlerLeak")
public class TAACChannelProfilePanel extends Activity {

	private TAACChannel.TMyProfile Profile;
	//.
	@SuppressWarnings("unused")
	private TextView lbName;
	private EditText edSampleRate;
	private EditText edBitRate;
	private Button btnApplyChanges;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
    	try {
            Bundle extras = getIntent().getExtras(); 
        	byte[] ProfileData  = extras.getByteArray("ProfileData");
        	Profile = new TAACChannel.TMyProfile(ProfileData);
        	Profile.FromByteArray(ProfileData);
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            //.
            setContentView(R.layout.sensorsmodule_channels_audio_aac_profile_panel);
            //.
            lbName = (TextView)findViewById(R.id.lbName);
            //.
            edSampleRate = (EditText)findViewById(R.id.edSampleRate);
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
    					Toast.makeText(TAACChannelProfilePanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    				}
                }
            });
		} catch (Exception E) {
			Toast.makeText(TAACChannelProfilePanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
		edSampleRate.setText(Integer.toString(Profile.SampleRate));
		//.
		edBitRate.setText(Integer.toString(Profile.BitRate));
    }

    private void ApplyChangesAndExit() throws Exception {
    	Profile.SampleRate = Integer.parseInt(edSampleRate.getText().toString());
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
