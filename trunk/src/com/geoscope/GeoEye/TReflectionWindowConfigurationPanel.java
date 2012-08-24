package com.geoscope.GeoEye;

import java.util.Calendar;
import java.util.TimeZone;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImageryData;
import com.geoscope.GeoLog.Utils.OleDate;

public class TReflectionWindowConfigurationPanel extends Activity {

	private TReflector Reflector;
	private Spinner spViewMode;
	private CheckBox cbShowHints;
	private LinearLayout ReflectionsModeLayout;
	private LinearLayout TilesModeLayout;
	private Button btnSpaceSuperLays;
	///? private ListView lvSuperLays;
	private Button btnSpecifyReflectionWindowActualityInterval;
	private Button btnCurrentReflectionWindowActualityInterval;
	private ListView lvTileServerVisualizations;
	//.
	private ListView lvTileServerData;
	private Button btnLoadTileServerDataFromServer;
	private Button btnSpaceSuperLays1;
	private Button btnOk;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //. 
        Reflector = TReflector.MyReflector;
        //.
        setContentView(R.layout.reflectionwindow_configuration_panel);
        ReflectionsModeLayout = (LinearLayout)findViewById(R.id.ReflectionWindowConfigurationReflectionsModeLayout);
        TilesModeLayout = (LinearLayout)findViewById(R.id.ReflectionWindowConfigurationTilesModeLayout);
        //.
        spViewMode = (Spinner)findViewById(R.id.spViewMode);
        String[] SA = new String[2];
        SA[0] = " —Õ»ÃŒ  ";
        SA[1] = " œÀ»“ ¿ ";
        ArrayAdapter<String> saViewMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saViewMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spViewMode.setAdapter(saViewMode);
        switch (Reflector.ViewMode) {
        case TReflector.VIEWMODE_REFLECTIONS:
            spViewMode.setSelection(0);
        	break; //. >
        case TReflector.VIEWMODE_TILES:
            spViewMode.setSelection(1);
        	break; //. >
        }
        spViewMode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	switch (position) {
            	case 0: 
            		Reflector.SetViewMode(TReflector.VIEWMODE_REFLECTIONS);
                    UpdateLayout();
            		break; //. >
            	case 1: 
            		Reflector.SetViewMode(TReflector.VIEWMODE_TILES);
                    UpdateLayout();
            		break; //. >
            	}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            	Reflector.SetViewMode(TReflector.VIEWMODE_REFLECTIONS);            
            	UpdateLayout();
            }
        });        
        //.
        UpdateLayout();
        //.
        cbShowHints = (CheckBox)findViewById(R.id.cbReflectionWindowShowHints);
        cbShowHints.setChecked(Reflector.Configuration.ReflectionWindow_flShowHints);
        cbShowHints.setOnCheckedChangeListener(new OnCheckedChangeListener()
        {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				try {
					Reflector.Configuration.ReflectionWindow_flShowHints = arg1; 
					Reflector.Configuration.Save();
					Reflector.StartUpdatingSpaceImage();
					//.
					finish(); 
		    	}
		    	catch (Exception E) {
		            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
			}
        });        
        //.
        btnSpaceSuperLays = (Button)findViewById(R.id.btnSpaceSuperLays);
        btnSpaceSuperLays.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
	            Reflector.ReflectionWindow.getLays().SuperLays.CreateSelectorPanel(Reflector).show();
            }
        });
        //.
        /*///? lvSuperLays = (ListView)findViewById(R.id.lvSuperLays);
        lvSuperLays.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        try {
            final TSpaceLays.TSuperLays Lays = Reflector.ReflectionWindow.CheckSpaceLays().SuperLays; 
    		if (Lays != null) {
    			final String[] lvSuperLaysItems = new String[Lays.Items.length];
    			for (int I = 0; I < Lays.Items.length; I++)
    				lvSuperLaysItems[I] = Lays.Items[I].Name;
    			ArrayAdapter<String> lvSuperLaysAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_multiple_choice,lvSuperLaysItems);             
    			lvSuperLays.setAdapter(lvSuperLaysAdapter);
    			for (int I = 0; I < Lays.Items.length; I++)
    				lvSuperLays.setItemChecked(I,Lays.Items[I].Enabled);
    		}
    		else 
    			lvSuperLays.setAdapter(null);
            lvSuperLays.setOnItemClickListener(new OnItemClickListener() {         

            	@Override
            	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
    				///Lays.EnableDisableItem(arg2, arg2);
            	}              
            });         
        }
        catch (Exception E) {
        	Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_LONG).show();
        	finish();
        }*/
        //.
        btnSpecifyReflectionWindowActualityInterval = (Button)findViewById(R.id.btnSpecifyReflectionWindowActualityInterval);
        btnSpecifyReflectionWindowActualityInterval.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
        		Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        		DatePickerDialog DateDialog = new DatePickerDialog(TReflectionWindowConfigurationPanel.this, new DatePickerDialog.OnDateSetListener() {                
        			@Override
        			public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                		double BeginTimestamp = (new OleDate(year,monthOfYear+1,dayOfMonth, 0,0,0)).toDouble();
        	            Reflector.ReflectionWindow.SetActualityInterval(BeginTimestamp,TReflectionWindowActualityInterval.MaxTimestamp);
        	            //.
                    	finish();
        			}
        		},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH));
        		//.
        		DateDialog.show();
            }
        });
        //.
        btnCurrentReflectionWindowActualityInterval = (Button)findViewById(R.id.btnCurrentReflectionWindowActualityInterval);
        btnCurrentReflectionWindowActualityInterval.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	            Reflector.ReflectionWindow.ResetActualityInterval();
	            //.
            	finish();
            }
        });
        //.
        lvTileServerVisualizations = (ListView)findViewById(R.id.lvTileServerVisualizations);
        lvTileServerVisualizations.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final TTileServerVisualizationUserData.TTileServerVisualization TSV;
		if (Reflector.ReflectionWindow.TileServerVisualizationUserData.TileServerVisualizations != null) {
			TSV = Reflector.ReflectionWindow.TileServerVisualizationUserData.TileServerVisualizations.get(0);
			final String[] lvTileServerVisualizationsItems = new String[TSV.Providers.size()];
			for (int I = 0; I < TSV.Providers.size(); I++) {
				lvTileServerVisualizationsItems[I] = TSV.Name+": "+TSV.Providers.get(I).Name;
			}
			int SelectedIdx = -1;
			for (int I = 0; I < TSV.Providers.size(); I++)
				if (TSV.Providers.get(I).ID == TSV.CurrentProvider) {
					SelectedIdx = I;
					break; //. >
				}
			ArrayAdapter<String> lvTileServerVisualizationsAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_single_choice,lvTileServerVisualizationsItems);             
			lvTileServerVisualizations.setAdapter(lvTileServerVisualizationsAdapter);
			if (SelectedIdx >= 0)
				lvTileServerVisualizations.setItemChecked(SelectedIdx,true);
		}
		else {
			TSV = null;
			lvTileServerVisualizations.setAdapter(null);
		}
        lvTileServerVisualizations.setOnItemClickListener(new OnItemClickListener() {         

			private TTileServerVisualizationUserData.TTileServerVisualization _TSV = TSV;
			
        	@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        		if (_TSV != null) {
    				TTileServerVisualizationUserData.TTileServerVisualizationProvider TSVP = _TSV.Providers.get(arg2);
    				try {
    					_TSV.SetCurrentProvider(TSVP.ID);
    					//.
    					Reflector.ReflectionWindow.DoOnSetVisualizationUserData();
    		    	}
    		    	catch (Exception E) {
    		            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_LONG).show();
    		    	}
                	//.
                	finish();
        		}
        	}              
        });         
        //.
        lvTileServerData = (ListView)findViewById(R.id.lvTileServerData);
        lvTileServerData_Update();
        //.
        btnSpaceSuperLays1 = (Button)findViewById(R.id.btnSpaceSuperLays1);
        btnSpaceSuperLays1.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	            Reflector.ReflectionWindow.getLays().SuperLays.CreateSelectorPanel(TReflectionWindowConfigurationPanel.this).show();
            }
        });
        //.
        btnLoadTileServerDataFromServer = (Button)findViewById(R.id.btnLoadTileServerDataFromServer);
        btnLoadTileServerDataFromServer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	if (Reflector.SpaceTileImagery != null) {
    				try {
                		Reflector.SpaceTileImagery.LoadDataFromServer();
    		    	}
    		    	catch (Exception E) {
    		            Toast.makeText(Reflector, E.getMessage(), Toast.LENGTH_LONG).show();
    		    	}
            		//.
                	lvTileServerData_Update();
            	}
            }
        });
        //.
        btnOk = (Button)findViewById(R.id.btnRWConfigutaionPanelOk);
        btnOk.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	lvTileServerData_SetActiveCompilation();
            	//.
            	finish();
            }
        });
        //. Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    private void UpdateLayout() {
        switch (Reflector.ViewMode) {
        case TReflector.VIEWMODE_REFLECTIONS:
            TilesModeLayout.setVisibility(LinearLayout.GONE);
            ReflectionsModeLayout.setVisibility(LinearLayout.VISIBLE);
        	break; //. >
        	
        case TReflector.VIEWMODE_TILES:
            ReflectionsModeLayout.setVisibility(LinearLayout.GONE);
            TilesModeLayout.setVisibility(LinearLayout.VISIBLE);
        	break; //. >
        }
    }
    
    private TTileImageryData.TTileServer TileServer;
    private TTileImagery.TTileServerProviderCompilationDescriptor[] Compilations;
    
    private void lvTileServerData_Update() {
        lvTileServerData.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		if ((Reflector.SpaceTileImagery != null) && (Reflector.SpaceTileImagery.Data.TileServers != null)) {
			TileServer = Reflector.SpaceTileImagery.Data.TileServers.get(0); //. get native tile-server
			Compilations = new TTileImagery.TTileServerProviderCompilationDescriptor[TileServer.CompilationsCount()]; 
			final String[] lvTileServerDataItems = new String[Compilations.length];
			int I = 0;
			for (int P = 0; P < TileServer.Providers.size(); P++) {
				TTileImageryData.TTileServerProvider TSP = TileServer.Providers.get(P);
				for (int C = 0; C < TSP.Compilations.size(); C++) {
					TTileImageryData.TTileServerProviderCompilation TSPC = TSP.Compilations.get(C);
					lvTileServerDataItems[I] = TSP.Name+"."+TSPC.Name;
					TTileImagery.TTileServerProviderCompilationDescriptor CD = new TTileImagery.TTileServerProviderCompilationDescriptor(TileServer.ID,TSP.ID,TSPC.ID);
					Compilations[I] = CD;
					//.
					I++;
				}
			}
			boolean[] SelectedItems = new boolean[Compilations.length];
			TTileImagery.TTileServerProviderCompilationDescriptors SC = Reflector.SpaceTileImagery.ActiveCompilationDescriptors();
			if (SC != null) 
				for (int C = 0; C < Compilations.length; C++)
					SelectedItems[C] = SC.ItemExists(Compilations[C]);
			ArrayAdapter<String> lvTileServerDataAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_checked,lvTileServerDataItems);             
			lvTileServerData.setAdapter(lvTileServerDataAdapter);
			for (int C = 0; C < Compilations.length; C++)
				if (SelectedItems[C])
					lvTileServerData.setItemChecked(C,true);
			lvTileServerData.setOnItemClickListener(new OnItemClickListener() {

				boolean flUpdating = false;
				
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
					if (!flUpdating) {
						flUpdating = true;
						try {
							if (lvTileServerData.isItemChecked(arg2)) {
								TTileImagery.TTileServerProviderCompilationDescriptor C0 = Compilations[arg2];
								TTileImageryData.TTileServerProviderCompilation TSPC0 = TileServer.GetCompilation(C0.PID,C0.CID);
								if ((TSPC0 != null) && (TSPC0.LayGroup > 0)) 
									for (int C = 0; C < Compilations.length; C++)
										if (C != arg2) {
											TTileImagery.TTileServerProviderCompilationDescriptor C1 = Compilations[C];
											TTileImageryData.TTileServerProviderCompilation TSPC1 = TileServer.GetCompilation(C1.PID,C1.CID);
											if ((TSPC1 != null) && (TSPC1.LayGroup == TSPC0.LayGroup))
												lvTileServerData.setItemChecked(C,false);
										}
							}
						}
						finally {
							flUpdating = false;
						}
					}
				}
			});
		}
		else {
			Compilations = null;
			lvTileServerData.setAdapter(null);
		}
    }
    
    private void lvTileServerData_SetActiveCompilation() {
    	SparseBooleanArray SelectedItems = lvTileServerData.getCheckedItemPositions();
    	int SelectedCount = 0;
    	for (int I = 0; I < SelectedItems.size(); I++) 
    		if (SelectedItems.valueAt(I))
    			SelectedCount++;
    	TTileImagery.TTileServerProviderCompilationDescriptors C = new TTileImagery.TTileServerProviderCompilationDescriptors(SelectedCount);
    	SelectedCount = 0;
    	for (int I = 0; I < SelectedItems.size(); I++) 
    		if (SelectedItems.valueAt(I)) {
    			C.Items[SelectedCount] = Compilations[SelectedItems.keyAt(I)];
    			SelectedCount++;
    		}
    	Reflector.ViewMode_Tiles_SetActiveCompilation(C);
    }    
}
