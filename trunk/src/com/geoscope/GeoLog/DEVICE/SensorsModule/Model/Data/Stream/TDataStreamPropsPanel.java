package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TChannelIDs;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TSourceStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TDataStreamPropsPanel extends Activity {

	public static final int REQUEST_EDITCHANNELPROFILE = 1;
	
	
	private TSensorsModule SensorsModule;
	//.
	private TStreamDescriptor 	DataStreamDescriptor = null;
	//.
	private TextView lbStreamName;
	private TextView lbStreamInfo;
	private TextView lbStreamChannels;
	//.
	private ListView 	lvChannels;
	private int			lvChannels_SelectedIndex = -1;
	//.
	private Button btnOpenStream;
	private Button btnGetStreamDescriptor;
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
        	SensorsModule = Tracker.GeoLog.SensorsModule;
        	//.
        	DataStreamDescriptor = SensorsModule.Model.Stream; 
    		//.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
            //.
            setContentView(R.layout.sensorsmodule_datastream_props_panel);
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
            btnOpenStream = (Button)findViewById(R.id.btnOpenStream);
            btnOpenStream.setOnClickListener(new OnClickListener() {
            	
            	@Override
                public void onClick(View v) {
                	try {
                		OpenStream();
    				} catch (Exception E) {
    					Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
    				}
                }
            });
            //.
            btnGetStreamDescriptor = (Button)findViewById(R.id.btnGetStreamDescriptor);
            btnGetStreamDescriptor.setVisibility(TGeoLogApplication.DebugOptions_IsDebugging() ? View.VISIBLE : View.GONE);
            btnGetStreamDescriptor.setOnClickListener(new OnClickListener() {
            	
            	@Override
                public void onClick(View v) {
                	try {
                		GetStreamDescriptor();
    				} catch (Exception E) {
    					Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
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
                    	if (SourceChannel != null) {
                    		SourceChannel.Profile_FromByteArray(ProfileData);
                    		//.
                    		SensorsModule.Model_BuildAndPublish();
                    		//.
                    		Update();
                    	}
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
    				TStreamChannel Channel = (TStreamChannel)DataStreamDescriptor.Channels.get(I);
    				lvChannelsItems[I] = Channel.Name;
    				if (Channel.Info.length() > 0)
    					lvChannelsItems[I] += " "+"/"+Channel.Info+"/"; 
    				//.
					TChannel SourceChannel = Channel.SourceChannel_Get();
					if (SourceChannel != null)
	    				if (SourceChannel.Enabled) {
        					if (SourceChannel.IsActive())
            					lvChannelsItems[I] += "  "+getString(R.string.SActive3);
	    				}
	    				else
	    					lvChannelsItems[I] += "  "+"("+getString(R.string.SDisabled2)+")";
    			}
    			ArrayAdapter<String> lvChannelsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvChannelsItems);             
    			lvChannels.setAdapter(lvChannelsAdapter);
    			for (int I = 0; I < DataStreamDescriptor.Channels.size(); I++)
    				lvChannels.setItemChecked(I,true);
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

    private void OpenStream() {
    	if (DataStreamDescriptor == null)
    		return; //. ->
    	final TChannelIDs Channels = new TChannelIDs();
		for (int I = 0; I < DataStreamDescriptor.Channels.size(); I++)
			if (lvChannels.isItemChecked(I))
				Channels.AddID(DataStreamDescriptor.Channels.get(I).ID);
		if (Channels.Count() == 0)
			return; // ->
    	//.
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private TUserAgent UserAgent;
			private TGeoScopeServerInfo.TInfo ServersInfo;
			private byte[] DescriptorData;
			
			@Override
			public void Process() throws Exception {
				UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(getString(R.string.SUserAgentIsNotInitialized)); //. =>
				ServersInfo = UserAgent.Server.Info.GetInfo();
				if (!ServersInfo.IsSpaceDataServerValid()) 
					throw new Exception("Invalid space data server"); //. =>
				//.
				DescriptorData = DataStreamDescriptor.ToByteArray();
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				Intent intent = new Intent(TDataStreamPropsPanel.this, TDataStreamPanel.class);
				//.
				intent.putExtra("ParametersType", TDataStreamPanel.PARAMETERS_TYPE_NOO);
				//.
				intent.putExtra("StreamDescriptorData", DescriptorData);
				//.
				intent.putExtra("StreamChannels", Channels.ToByteArray());
				//.
				startActivity(intent);
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
    }

    private void GetStreamDescriptor() {
		TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private File DescriptorFile;
			
			@Override
			public void Process() throws Exception {
				DescriptorFile = new File(TGeoLogApplication.GetTempFolder()+"/"+"DataStreamDescriptor.xml");
				FileOutputStream fos = new FileOutputStream(DescriptorFile);
				try {
					byte[] BA = DataStreamDescriptor.ToByteArray();
					fos.write(BA, 0,BA.length);
				} finally {
					fos.close();
				}
			}
			
			@Override 
			public void DoOnCompleted() throws Exception {
				Intent intent = new Intent();
				intent.setDataAndType(Uri.fromFile(DescriptorFile), "text/xml");
				intent.setAction(android.content.Intent.ACTION_VIEW);
				startActivity(intent);
			}
			
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TDataStreamPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
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
