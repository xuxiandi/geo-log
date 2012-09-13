/*
 * Copyright (C) 2011 GUIGUI Simon, fyhertz@gmail.com, modified by PAV
 * 
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.geoscope.GeoLog.DEVICE.VideoRecorderModule;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TVideoRecorderPropsPanel extends Activity {

	private Spinner	 spVideoRecorderMode;
	private CheckBox cbVideoRecorderRecording;
	private CheckBox cbVideoRecorderAudio;
	private CheckBox cbVideoRecorderVideo;
	private CheckBox cbVideoRecorderTransmitting;
	private CheckBox cbVideoRecorderSaving;
	//.
	private boolean flUpdating = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
        setContentView(R.layout.video_recorder_props_panel);
        //.
        spVideoRecorderMode = (Spinner)findViewById(R.id.spVideoRecorderMode);
        String[] SA = new String[3];
        SA[0] = " Stream ";
        SA[1] = " MPEG4 ";
        SA[2] = " 3GP ";
        ArrayAdapter<String> saMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVideoRecorderMode.setAdapter(saMode);
        spVideoRecorderMode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				if (flUpdating)
					return; //. ->
            	short Mode = TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1;
            	switch (position) {
            	case 0:
            		Mode = TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1;
            		break; //. >
            		
            	case 1:
            		Mode = TVideoRecorderModule.MODE_MPEG4;
            		break; //. >
            		
            	case 2:
            		Mode = TVideoRecorderModule.MODE_3GP;
            		break; //. >
            	}
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetMode(Mode);
				DoOnItemChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
				if (flUpdating)
					return; //. ->
            	short Mode = TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1;
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetMode(Mode);
				DoOnItemChanged();
            }
        });        
        //.
        cbVideoRecorderRecording = (CheckBox)findViewById(R.id.cbVideoRecorderRecording);
        cbVideoRecorderRecording.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (flUpdating)
					return; //. ->
				if (arg1)
					TVideoRecorderPropsPanel.this.finish();
				//.
				TTracker Tracker = TTracker.GetTracker();
				//.
				Tracker.GeoLog.VideoRecorderModule.SetActive(arg1);
				Tracker.GeoLog.VideoRecorderModule.SetRecording(arg1);
				DoOnItemChanged();
			}
        });        
        //.
        cbVideoRecorderAudio = (CheckBox)findViewById(R.id.cbVideoRecorderAudio);
        cbVideoRecorderAudio.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (flUpdating)
					return; //. ->
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetAudio(arg1);
				DoOnItemChanged();
			}
        });        
        //.
        cbVideoRecorderVideo = (CheckBox)findViewById(R.id.cbVideoRecorderVideo);
        cbVideoRecorderVideo.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (flUpdating)
					return; //. ->
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetVideo(arg1);
				DoOnItemChanged();
			}
        });        
        //.
        cbVideoRecorderTransmitting = (CheckBox)findViewById(R.id.cbVideoRecorderTransmitting);
        cbVideoRecorderTransmitting.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (flUpdating)
					return; //. ->
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetTransmitting(arg1);
				DoOnItemChanged();
			}
        });        
        //.
        cbVideoRecorderSaving = (CheckBox)findViewById(R.id.cbVideoRecorderSaving);
        cbVideoRecorderSaving.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				if (flUpdating)
					return; //. ->
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetSaving(arg1);
				DoOnItemChanged();
			}
        });  
        //.
        Update();
    }
	
	private void Update() {
		TTracker Tracker = TTracker.GetTracker();
		if (Tracker == null)
			return; //. ->
		TVideoRecorderModule VRM = Tracker.GeoLog.VideoRecorderModule;
		//.
		flUpdating = true;
		try {
	        switch (VRM.Mode.GetValue()) {
	        case TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1:
	            spVideoRecorderMode.setSelection(0);
	        	break; //. >
	        	
	        case TVideoRecorderModule.MODE_MPEG4:
	        	spVideoRecorderMode.setSelection(1);
	        	break; //. >
	        	
	        case TVideoRecorderModule.MODE_3GP:
	        	spVideoRecorderMode.setSelection(2);
	        	break; //. >
	        }
			//.
			cbVideoRecorderRecording.setChecked(VRM.Recording.BooleanValue());
			cbVideoRecorderAudio.setChecked(VRM.Audio.BooleanValue());
			cbVideoRecorderVideo.setChecked(VRM.Video.BooleanValue());
			cbVideoRecorderTransmitting.setChecked(VRM.Transmitting.BooleanValue());
			cbVideoRecorderSaving.setChecked(VRM.Saving.BooleanValue());
		}
		finally {
			flUpdating = false;
		}
	}
	
	private void DoOnItemChanged() {
		TTracker Tracker = TTracker.GetTracker();
		if (Tracker == null)
			return; //. ->
		Tracker.GeoLog.VideoRecorderModule.PostUpdateRecorderState();
	}
}
