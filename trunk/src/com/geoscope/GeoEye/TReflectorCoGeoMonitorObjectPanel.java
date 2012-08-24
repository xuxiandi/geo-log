package com.geoscope.GeoEye;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TableLayout;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.Defines.ColorPickerDialog;
import com.geoscope.GeoEye.Space.Defines.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoLog.Utils.OleDate;

public class TReflectorCoGeoMonitorObjectPanel extends Activity {

	private TReflector Reflector;
	private TReflectorCoGeoMonitorObject Object;
	private TableLayout _TableLayout;
	private EditText edGMOName;
	private EditText edGMOConnectionState;
	private EditText edGMOLocationState;
	private EditText edGMOAlertState;
	private Button btnGMOUpdateInfo;
	private Button btnGMOShowPosition;
	private Button btnGMOAddTrack;
	//.
	private int AddTrack_Date_Year;
	private int AddTrack_Date_Month;
	private int AddTrack_Date_Day;
	private int AddTrack_Color;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.MyReflector;
        Bundle extras = getIntent().getExtras(); 
        if (extras != null) {
        	int Idx = extras.getInt("Index");
        	Object = Reflector.CoGeoMonitorObjects.Items[Idx]; 
        }
        //.
        setContentView(R.layout.reflector_gmo_panel);
        //.
        _TableLayout = (TableLayout)findViewById(R.id.TrackerPOIPanelTableLayout);
        _TableLayout.setBackgroundColor(Color.blue(100));
        edGMOName = (EditText)findViewById(R.id.edGMOName);
        edGMOConnectionState = (EditText)findViewById(R.id.edGMOConnectionState);
        edGMOLocationState = (EditText)findViewById(R.id.edGMOLocationState);
        edGMOAlertState = (EditText)findViewById(R.id.edGMOAlertState);
        //.
        btnGMOUpdateInfo = (Button)findViewById(R.id.btnGMOUpdateInfo);
        btnGMOUpdateInfo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	UpdateInfo();
            }
        });
        //.
        btnGMOShowPosition = (Button)findViewById(R.id.btnGMOShowPosition);
        btnGMOShowPosition.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	ShowCurrentPosition();
            	finish();
            }
        });
        //.
        btnGMOAddTrack = (Button)findViewById(R.id.btnGMOAddTrack);
        btnGMOAddTrack.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	AddTrack();
            }
        });
        //.
        UpdateInfo();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void UpdateInfo() {
		try {
			edGMOName.setText(Object.LabelText);
			byte[] Data = Object.GetData(0);
			boolean IsOnline = (Data[0] > 0);
			boolean FixIsAvailable = (Data[1] > 0);
			int UserAlert = TDataConverter.ConvertBEByteArrayToInt32(Data,2);
			//.
			if (IsOnline)
				edGMOConnectionState.setText("На связи");
			else
				edGMOConnectionState.setText("откл.");
			if (FixIsAvailable)
				edGMOLocationState.setText("Доступна");
			else
				edGMOLocationState.setText("не доступна");
			switch (UserAlert) {

			case 0: edGMOAlertState.setText("нет");
				break; //. >

			case 1: edGMOAlertState.setText("слабая");
				break; //. >
				
			case 2: edGMOAlertState.setText("сильная");
				break; //. >
				
			case 3: edGMOAlertState.setText("критическая");
				break; //. >
			}
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, "Ошибка обновления информации об объекте, "+E.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	}
	
	public void ShowCurrentPosition() {
		try {
			TXYCoord C = Object.GetComponentLocation();
			Reflector.MoveReflectionWindow(C);
	    }
	    catch (Exception E) {
	    	Toast.makeText(this, "Ошибка установки текущей позиции, "+E.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	}
	
	private void AddTrack() {
		Calendar c = Calendar.getInstance();
		DatePickerDialog DateDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {                
			@Override
			public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
				AddTrack_Date_Year = year;
				AddTrack_Date_Month = monthOfYear+1;
				AddTrack_Date_Day = dayOfMonth;
				//.
        		ColorPickerDialog ColorDialog = new ColorPickerDialog(TReflectorCoGeoMonitorObjectPanel.this, new ColorPickerDialog.OnColorChangedListener() {
        			@Override
        			public void colorChanged(int color) {
        				AddTrack_Color = color;
        				//.
                		OleDate Day = new OleDate(AddTrack_Date_Year,AddTrack_Date_Month,AddTrack_Date_Day, 0,0,0);
                		try {
                			Reflector.ObjectTracks.AddNewTrack(Object.ID,Day.toDouble(),AddTrack_Color);
                			//.
                			TReflectorCoGeoMonitorObjectPanel.this.finish();
                		}
                		catch (Exception E) {
                			Toast.makeText(TReflectorCoGeoMonitorObjectPanel.this, "Ошибка добавления трека объекта, "+E.getMessage(), Toast.LENGTH_SHORT).show();
                		}
        			}
        		},Color.RED);    
        		ColorDialog.show();
			}
		},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
		DateDialog.show();
	}
}
