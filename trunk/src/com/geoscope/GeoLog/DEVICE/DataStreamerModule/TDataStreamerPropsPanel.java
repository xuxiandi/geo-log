package com.geoscope.GeoLog.DEVICE.DataStreamerModule;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.TDataStreamDescriptor;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TDataStreamerPropsPanel extends Activity {

	private CheckBox cbDataStreamingActive;
	private ListView lvStreamingComponents;
	//.
	private TDataStreamerModule DataStreamerModule = null;
	//.
	private boolean flUpdating = false;
	//.
	private TDataStreamerModule.TStreamingComponents StreamingComponents;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        setContentView(R.layout.datastreamer_props_panel);
        //.
		TTracker Tracker = TTracker.GetTracker();
		if (Tracker == null) {
			finish();
			return; //. ->
		}
		DataStreamerModule = Tracker.GeoLog.DataStreamerModule;
        //.
        cbDataStreamingActive = (CheckBox)findViewById(R.id.cbDataStreamingActive);
        cbDataStreamingActive.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				if (flUpdating)
					return; //. ->
                boolean checked = ((CheckBox)v).isChecked();
				//.
				TTracker Tracker = TTracker.GetTracker();
				//.
				try {
					Tracker.GeoLog.DataStreamerModule.SetActiveValue(checked);
				} catch (Exception E) {
		            Toast.makeText(TDataStreamerPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });        
        //.
        lvStreamingComponents = (ListView)findViewById(R.id.lvStreamingComponents);
        lvStreamingComponents.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				TDataStreamerModule.TStreamingComponents.TComponent Component = StreamingComponents.Components.get(arg2);
				//.
				Component.Enabled = lvStreamingComponents.isItemChecked(arg2);
				//.
				try {
					DataStreamerModule.SetStreamingComponents(StreamingComponents);
				} catch (Exception E) {
		            Toast.makeText(TDataStreamerPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
		});
        lvStreamingComponents.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				AlertDialog ChannelsSelector = CreateChannelsSelectorPanel(TDataStreamerPropsPanel.this, arg2);
				if (ChannelsSelector != null)
					ChannelsSelector.show();
            	//.
            	return true; 
			}
		}); 
        //.
        Update();
    }
	
	private void Update() {
		flUpdating = true;
		try {
			cbDataStreamingActive.setEnabled(DataStreamerModule.StreamingComponentsCount() > 0);
			cbDataStreamingActive.setChecked(DataStreamerModule.ActiveValue.BooleanValue());
			//.
			StreamingComponents = DataStreamerModule.GetStreamingComponents();
			lvStreamingComponents_Update();
		}
		finally {
			flUpdating = false;
		}
	}
	
	private void lvStreamingComponents_Update() {
		int CC = StreamingComponents.Components.size();
		final String[] lvStreamingComponentsItems = new String[CC];
		boolean[] SelectedItems = new boolean[CC];
		for (int I = 0; I < CC; I++) {
			TDataStreamerModule.TStreamingComponents.TComponent Component = StreamingComponents.Components.get(I);
			//.
			StringBuilder SB = new StringBuilder();
			SB.append("["+Integer.toString(Component.idTComponent)+":"+Long.toString(Component.idComponent)+"] ");
			if (Component.StreamDescriptor != null) {
				if (!Component.StreamDescriptor.Name.equals(""))
					SB.append(Component.StreamDescriptor.Name+" ");
				if (!Component.StreamDescriptor.Info.equals(""))
					SB.append("/"+Component.StreamDescriptor.Info+"/");
			}
			lvStreamingComponentsItems[I] = SB.toString();
			SelectedItems[I] = Component.Enabled;
		}
		ArrayAdapter<String> lvStreamingComponentsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_checked,lvStreamingComponentsItems);             
		lvStreamingComponents.setAdapter(lvStreamingComponentsAdapter);
		for (int I = 0; I < CC; I++)
			if (SelectedItems[I])
				lvStreamingComponents.setItemChecked(I,true);
	}

	public synchronized AlertDialog CreateChannelsSelectorPanel(Activity ParentActivity, int ComponentIndex) {
		final TDataStreamerModule.TStreamingComponents.TComponent Component = StreamingComponents.Components.get(ComponentIndex);
		if (Component.StreamDescriptor == null)
			return null; //. ->
		if (Component.Channels == null)
			Component.Channels_SetupByStreamDescriptorChannels();
		//.
		int CC = Component.StreamDescriptor.Channels.size();
    	final CharSequence[] Channels = new CharSequence[CC];
    	final boolean[] ChannelsMask = new boolean[CC];
    	for (int I = 0; I < CC; I++) {
    		TDataStreamDescriptor.TChannel Channel = Component.StreamDescriptor.Channels.get(I);
    		//.
			StringBuilder SB = new StringBuilder();
			SB.append("["+Channel.TypeID+"] ");
			if (!Channel.Name.equals(""))
				SB.append(Channel.Name+" ");
			if (!Channel.Info.equals(""))
				SB.append("/"+Channel.Info+"/");
			//.
    		Channels[I] = SB.toString();
    		ChannelsMask[I] = Component.Channels_ChannelExists(Channel.ID);
    	}
    	AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
    	builder.setTitle(R.string.SChannels);
    	builder.setNegativeButton(R.string.SOk,null);
    	builder.setMultiChoiceItems(Channels, ChannelsMask, new DialogInterface.OnMultiChoiceClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1, boolean arg2) {
	    		TDataStreamDescriptor.TChannel Channel = Component.StreamDescriptor.Channels.get(arg1);
	    		//.
	    		if (arg2)
	    			Component.Channels_AddChannel(Channel.ID);
	    		else
	    			Component.Channels_RemoveChannel(Channel.ID);
				//.
				try {
					DataStreamerModule.SetStreamingComponents(StreamingComponents);
				} catch (Exception E) {
		            Toast.makeText(TDataStreamerPropsPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
    	});
    	AlertDialog alert = builder.create();
    	return alert;
	}
}
