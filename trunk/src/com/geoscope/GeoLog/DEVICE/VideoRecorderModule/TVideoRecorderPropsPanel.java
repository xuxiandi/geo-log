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
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.TrackerService.TTracker;

public class TVideoRecorderPropsPanel extends Activity {

	private Spinner	 spVideoRecorderMode;
	private CheckBox cbVideoRecorderRecording;
	private CheckBox cbVideoRecorderAudio;
	private CheckBox cbVideoRecorderVideo;
	private CheckBox cbVideoRecorderTransmitting;
	private CheckBox cbVideoRecorderSaving;
	private CheckBox cbDataStreamingActive;
	//.
	private boolean flUpdating = false;
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //.
        setContentView(R.layout.video_recorder_props_panel);
        //.
        spVideoRecorderMode = (Spinner)findViewById(R.id.spVideoRecorderMode);
        String[] SA = new String[5];
        SA[0] = " Stream (H263) ";
        SA[1] = " Stream (H264) ";
        SA[2] = " MPEG4 ";
        SA[3] = " 3GP ";
        SA[4] = " Stream (FRAME) ";
        ArrayAdapter<String> saMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spVideoRecorderMode.setAdapter(saMode);
        spVideoRecorderMode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				if (flUpdating)
					return; //. ->
            	short Mode = TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1;
            	switch (position) {
            	
            	case 0:
            		Mode = TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1;
            		break; //. >
            		
            	case 1:
            		Mode = TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1;
            		break; //. >
            		
            	case 2:
            		Mode = TVideoRecorderModule.MODE_MPEG4;
            		break; //. >
            		
            	case 3:
            		Mode = TVideoRecorderModule.MODE_3GP;
            		break; //. >
            		
            	case 4:
            		Mode = TVideoRecorderModule.MODE_FRAMESTREAM;
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
            	short Mode = TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1;
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetMode(Mode);
				DoOnItemChanged();
            }
        });        
        //.
        cbVideoRecorderRecording = (CheckBox)findViewById(R.id.cbVideoRecorderRecording);
        cbVideoRecorderRecording.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				if (flUpdating)
					return; //. ->
                boolean checked = ((CheckBox)v).isChecked();
				if (checked)
					TVideoRecorderPropsPanel.this.finish();
				//.
				TTracker Tracker = TTracker.GetTracker();
				//.
				Tracker.GeoLog.VideoRecorderModule.SetRecorderState(checked, true);
            }
        });        
        //.
        cbVideoRecorderAudio = (CheckBox)findViewById(R.id.cbVideoRecorderAudio);
        cbVideoRecorderAudio.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				if (flUpdating)
					return; //. ->
                boolean checked = ((CheckBox)v).isChecked();
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetAudio(checked);
				DoOnItemChanged();
            }
        });        
        //.
        cbVideoRecorderVideo = (CheckBox)findViewById(R.id.cbVideoRecorderVideo);
        cbVideoRecorderVideo.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				if (flUpdating)
					return; //. ->
                boolean checked = ((CheckBox)v).isChecked();
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetVideo(checked);
				DoOnItemChanged();
            }
        });        
        //.
        cbVideoRecorderTransmitting = (CheckBox)findViewById(R.id.cbVideoRecorderTransmitting);
        cbVideoRecorderTransmitting.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				if (flUpdating)
					return; //. ->
                boolean checked = ((CheckBox)v).isChecked();
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetTransmitting(checked);
				DoOnItemChanged();
            }
        });        
        //.
        cbVideoRecorderSaving = (CheckBox)findViewById(R.id.cbVideoRecorderSaving);
        cbVideoRecorderSaving.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
				if (flUpdating)
					return; //. ->
                boolean checked = ((CheckBox)v).isChecked();
				TTracker Tracker = TTracker.GetTracker();
				Tracker.GeoLog.VideoRecorderModule.SetSaving(checked);
				DoOnItemChanged();
            }
        });        
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
					String S = E.getMessage();
					if (S == null)
						S = E.getClass().getName();
		            Toast.makeText(TVideoRecorderPropsPanel.this, S, Toast.LENGTH_LONG).show();
				}
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
	        case TVideoRecorderModule.MODE_H263STREAM1_AMRNBSTREAM1:
	            spVideoRecorderMode.setSelection(0);
	        	break; //. >
	        	
	        case TVideoRecorderModule.MODE_H264STREAM1_AMRNBSTREAM1:
	            spVideoRecorderMode.setSelection(1);
	        	break; //. >
	        	
	        case TVideoRecorderModule.MODE_MPEG4:
	        	spVideoRecorderMode.setSelection(2);
	        	break; //. >
	        	
	        case TVideoRecorderModule.MODE_3GP:
	        	spVideoRecorderMode.setSelection(3);
	        	break; //. >

	        case TVideoRecorderModule.MODE_FRAMESTREAM:
	        	spVideoRecorderMode.setSelection(4);
	        	break; //. >
	        }
			//.
			cbVideoRecorderRecording.setChecked(VRM.Recording.BooleanValue());
			cbVideoRecorderAudio.setChecked(VRM.Audio.BooleanValue());
			cbVideoRecorderVideo.setChecked(VRM.Video.BooleanValue());
			cbVideoRecorderTransmitting.setChecked(VRM.Transmitting.BooleanValue());
			cbVideoRecorderSaving.setChecked(VRM.Saving.BooleanValue());
			//.
			cbDataStreamingActive.setEnabled(Tracker.GeoLog.DataStreamerModule.StreamingComponentsCount() > 0);
			cbDataStreamingActive.setChecked(Tracker.GeoLog.DataStreamerModule.ActiveValue.BooleanValue());
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
