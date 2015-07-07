package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TSourceStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TDataStreamPropsPanel extends Activity {

	public static final int REQUEST_EDITCHANNELPROFILE = 1;
	
	
	private TStreamDescriptor 	DataStreamDescriptor = null;
	//.
	private TextView lbStreamName;
	private TextView lbStreamInfo;
	private TextView lbStreamChannels;
	//.
	private ListView 	lvChannels;
	private int			lvChannels_SelectedIndex = -1;
	//.
	@SuppressWarnings("unused")
	private boolean flUpdate = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		//.
    	try {
        	TTracker Tracker = TTracker.GetTracker();
        	if (Tracker == null)
        		throw new Exception(getString(R.string.STrackerIsNotInitialized)); //. =>
        	if (Tracker.GeoLog.SensorsModule.Model == null)
        		throw new Exception("sensors module stream model is not defined"); //. =>
        	DataStreamDescriptor = Tracker.GeoLog.SensorsModule.Model.Stream; 
    		//.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            //.
            setContentView(R.layout.sensorsmodule_model_datastream_props_panel);
            //.
            lbStreamName = (TextView)findViewById(R.id.lbStreamName);
            lbStreamInfo = (TextView)findViewById(R.id.lbStreamInfo);
            lbStreamChannels = (TextView)findViewById(R.id.lbStreamChannels);
            //.
            lvChannels = (ListView)findViewById(R.id.lvChannels);
            lvChannels.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            lvChannels.setOnItemLongClickListener(new OnItemLongClickListener() {
            	
            	@Override
            	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                	try {
                		lvChannels_SelectedIndex = arg2;
                		return (OpenChannelProfile(lvChannels_SelectedIndex));
    				} catch (Exception E) {
    					Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    					//.
    					return false; //. ->
    				}
            	}
    		});
            //.
            Update();
		} catch (Exception E) {
			Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {        

        case REQUEST_EDITCHANNELPROFILE: 
        	if (resultCode == RESULT_OK) {
            	try {
    				Bundle extras = data.getExtras();
					byte[] ProfileData = extras.getByteArray("ProfileData");
					//.
            		if (lvChannels_SelectedIndex >= 0) {
                    	TStreamChannel Channel = (TStreamChannel)DataStreamDescriptor.Channels.get(lvChannels_SelectedIndex);
                    	//.
                    	TSourceStreamChannel SourceChannel = Channel.SourceChannel_Get();
                    	if (SourceChannel != null) 
                    		SourceChannel.Profile_FromByteArray(ProfileData);
            		}
				} catch (Exception E) {
					Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
        	}
            break; //. >
        }
    	//.
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
    private void Update() {
    	flUpdate = true; 
    	try {
    		if (DataStreamDescriptor != null) {
    			lbStreamName.setText(DataStreamDescriptor.Name);
    			//.
    			String S = getString(R.string.SInfo1);
    			lbStreamInfo.setText(S+DataStreamDescriptor.Info);
    			//.
    			lbStreamChannels.setText(getString(R.string.SChannels1));
    			String[] lvChannelsItems = new String[DataStreamDescriptor.Channels.size()];
    			for (int I = 0; I < DataStreamDescriptor.Channels.size(); I++) {
    				TChannel Channel = DataStreamDescriptor.Channels.get(I);
    				lvChannelsItems[I] = Channel.Name;
    				if (Channel.Info.length() > 0)
    					lvChannelsItems[I] += " "+"/"+Channel.Info+"/"; 
    			}
    			ArrayAdapter<String> lvChannelsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvChannelsItems);             
    			lvChannels.setAdapter(lvChannelsAdapter);
    		}
    		else {
    			lbStreamName.setText("?");
    			lbStreamInfo.setText(getString(R.string.SInfo1)+"?");
    			lbStreamChannels.setText(getString(R.string.SChannels1)+"?");
    			lvChannels.setAdapter(null);
    		}
    	}
    	finally {
    		flUpdate = false;
    	}
    }

    private boolean OpenChannelProfile(int Idx) throws Exception {
    	TStreamChannel Channel = (TStreamChannel)DataStreamDescriptor.Channels.get(Idx);
    	//.
    	TSourceStreamChannel SourceChannel = Channel.SourceChannel_Get();
    	if (SourceChannel == null)
    		return false; //. ->
    	//.
    	Intent ProfilePanel = SourceChannel.Profile.GetProfilePanel(TDataStreamPropsPanel.this);
    	if (ProfilePanel != null) 
    		startActivityForResult(ProfilePanel, REQUEST_EDITCHANNELPROFILE);
    	else
    		throw new Exception(getString(R.string.SThereIsNoProfilePanel)); //. ->
		//.
    	return true; //. ->
    }
}
