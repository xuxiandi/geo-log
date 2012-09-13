package com.geoscope.GeoEye;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableLayout;

public class TTrackerPOITextPanel extends Activity {

	private TableLayout _TableLayout;
	public EditText edPOIText;
	private Button 	btnOk;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        setContentView(R.layout.tracker_poi_text_panel);
        //.
        _TableLayout = (TableLayout)findViewById(R.id.TrackerPOITextPanelTableLayout);
        _TableLayout.setBackgroundColor(Color.blue(100));
        edPOIText = (EditText)findViewById(R.id.edPOIText);
        btnOk = (Button)findViewById(R.id.TrackerPOITextPanel_btnOk);
        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	Intent intent = TTrackerPOITextPanel.this.getIntent();
            	intent.putExtra("POIText",edPOIText.getText().toString());
                //.
            	setResult(Activity.RESULT_OK,intent);
        		//.
            	finish();
            }
        });
        //.
        setResult(Activity.RESULT_CANCELED);
        //.
        Update();
	}

	public void Update() {
	}
}
