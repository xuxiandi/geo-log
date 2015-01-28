package com.geoscope.GeoEye;

import com.geoscope.GeoEye.Space.Defines.TLocation;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

public class TReflectorNewElectedPlacePanel extends Activity {

	private TReflectorComponent Component;
	//.
	private TReflectorElectedPlaces ElectedPlaces;
	
	private EditText edNewElectedPlaceName;
	private CheckBox cbUsePlaceTimestamp;
	private Button btnNewPlace;
	private Button btnClosePanel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
        int ComponentID = 0;
		Bundle extras = getIntent().getExtras();
		if (extras != null) 
			ComponentID = extras.getInt("ComponentID");
		Component = TReflectorComponent.GetComponent(ComponentID);
        //.
        ElectedPlaces = Component.ElectedPlaces;
        //.
        setContentView(R.layout.reflector_new_electedplace_panel);
        //.
        edNewElectedPlaceName = (EditText)findViewById(R.id.edNewElectedPlaceName);
        edNewElectedPlaceName.setOnEditorActionListener(new OnEditorActionListener() {        
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if(arg1 == EditorInfo.IME_ACTION_DONE){
                	AddNewPlace();
                	setResult(RESULT_OK);
                	finish();
                }
				return false;
			}
        });
        //.
        cbUsePlaceTimestamp = (CheckBox)findViewById(R.id.cbUsePlaceTimestamp);
        //.
        btnNewPlace = (Button)findViewById(R.id.btnNewElectedPlace);
        btnNewPlace.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AddNewPlace();
            	setResult(RESULT_OK);
            	finish();
            }
        });
        //.
        btnClosePanel = (Button)findViewById(R.id.btnCloseNewElecledPanelPanel);
        btnClosePanel.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
            }
        });
        this.setResult(RESULT_CANCELED);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
	public void AddNewPlace() {
		try {
			String S = edNewElectedPlaceName.getText().toString();
			if (S.equals(""))
				S = getString(R.string.SElectedPlace);
			boolean flUsePlaceTimestamp = cbUsePlaceTimestamp.isChecked();
			TLocation NewPlace = new TLocation();
			NewPlace.Name = S;
			NewPlace.RW = Component.ReflectionWindow.GetWindow();
			if (!flUsePlaceTimestamp) 
				NewPlace.RW.ResetTimeInterval();
			//.
			ElectedPlaces.AddPlace(NewPlace);
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
	    }
	}
}
