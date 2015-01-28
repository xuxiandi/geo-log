package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.DateTimePicker;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization.TTileServerVisualizationUserData;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImagery;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileImageryData;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

@SuppressLint("HandlerLeak")
public class TReflectionWindowConfigurationPanel extends Activity {

	private TReflectorComponent Component;
	//.
	private Spinner spViewMode;
	private Spinner spNavigationMode;
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
	private Button btnSetHistoryTime;
	private Button btnSetHistoryTimeNow;
	private Button btnSetHistoryTimeLast;
	private Button btnOk;
	
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
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		if (Component.flFullScreen) { 
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);		
		}
        //.
        setContentView(R.layout.reflectionwindow_configuration_panel);
        ReflectionsModeLayout = (LinearLayout)findViewById(R.id.ReflectionWindowConfigurationReflectionsModeLayout);
        TilesModeLayout = (LinearLayout)findViewById(R.id.ReflectionWindowConfigurationTilesModeLayout);
        //.
        spViewMode = (Spinner)findViewById(R.id.spViewMode);
        String[] SA = new String[2];
        SA[0] = getString(R.string.SMapShot);
        SA[1] = getString(R.string.STiles);
        ArrayAdapter<String> saViewMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saViewMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spViewMode.setAdapter(saViewMode);
        switch (Component.GetViewMode()) {
        case TReflectorComponent.VIEWMODE_REFLECTIONS:
            spViewMode.setSelection(0);
        	break; //. >
        case TReflectorComponent.VIEWMODE_TILES:
            spViewMode.setSelection(1);
        	break; //. >
        }
        spViewMode.setOnItemSelectedListener(new OnItemSelectedListener() {
        	
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	switch (position) {
            	
            	case 0: 
            		if (Component.GetViewMode() != TReflectorComponent.VIEWMODE_REFLECTIONS) {
                		Component.SetViewMode(TReflectorComponent.VIEWMODE_REFLECTIONS);
                        UpdateLayout();
            		}
            		break; //. >
            		
            	case 1: 
            		if (Component.GetViewMode() != TReflectorComponent.VIEWMODE_TILES) {
                		Component.SetViewMode(TReflectorComponent.VIEWMODE_TILES);
                        UpdateLayout();
            		}
            		break; //. >
            	}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
        		if (Component.GetViewMode() != TReflectorComponent.VIEWMODE_REFLECTIONS) {
            		Component.SetViewMode(TReflectorComponent.VIEWMODE_REFLECTIONS);
                    UpdateLayout();
        		}
            }
        });        
        //.
        spNavigationMode = (Spinner)findViewById(R.id.spNavigationMode);
        SA = new String[3];
        SA[0] = getString(R.string.SByDefault);
        SA[1] = getString(R.string.SArrows);
        SA[2] = getString(R.string.SMultiTouching);
        ArrayAdapter<String> saNavigationMode = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, SA);
        saNavigationMode.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spNavigationMode.setAdapter(saNavigationMode);
        switch (Component.GetNavigationMode()) {
        
        case TReflectorComponent.NAVIGATION_MODE_NATIVE:
            spNavigationMode.setSelection(0);
        	break; //. >
        	
        case TReflectorComponent.NAVIGATION_MODE_ARROWS:
            spNavigationMode.setSelection(1);
        	break; //. >
        	
        case TReflectorComponent.NAVIGATION_MODE_MULTITOUCHING1:
            spNavigationMode.setSelection(2);
        	break; //. >
        }
        spNavigationMode.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
            	try {
                	switch (position) {
                	
                	case 0:
                		if (Component.GetNavigationMode() != TReflectorComponent.NAVIGATION_MODE_NATIVE) {
                    		Component.SetNavigationMode(TReflectorComponent.NAVIGATION_MODE_NATIVE);
                            finish();
                		}
                		break; //. >
                		
                	case 1: 
                		if (Component.GetNavigationMode() != TReflectorComponent.NAVIGATION_MODE_ARROWS) {
                    		Component.SetNavigationMode(TReflectorComponent.NAVIGATION_MODE_ARROWS);
                            finish();
                		}
                		break; //. >
                		
                	case 2: 
                		if (Component.GetNavigationMode() != TReflectorComponent.NAVIGATION_MODE_MULTITOUCHING1) {
                    		Component.SetNavigationMode(TReflectorComponent.NAVIGATION_MODE_MULTITOUCHING1);
                            finish();
                		}
                		break; //. >
                	}
            	}
		    	catch (Exception E) {
		            Toast.makeText(Component.context, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            	try {
            		Component.SetNavigationMode(TReflectorComponent.NAVIGATION_MODE_ARROWS);
                    finish();
            	}
		    	catch (Exception E) {
		            Toast.makeText(Component.context, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });        
        //.
        UpdateLayout();
        //.
        cbShowHints = (CheckBox)findViewById(R.id.cbReflectionWindowShowHints);
        cbShowHints.setChecked(Component.Configuration.ReflectionWindow_flShowHints);
        cbShowHints.setOnClickListener(new OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox)v).isChecked();
				try {
					Component.Configuration.ReflectionWindow_flShowHints = checked; 
		    		Component.Configuration.flChanged = true;
					Component.Configuration.Save();
					//.
			        btnSpaceSuperLays1.setEnabled(Component.Configuration.ReflectionWindow_flShowHints);
					//.
					Component.StartUpdatingSpaceImage();
					//.
					finish(); 
		    	}
		    	catch (Exception E) {
		            Toast.makeText(Component.context, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
            }
        });        
        //.
        btnSpaceSuperLays = (Button)findViewById(R.id.btnSpaceSuperLays);
        btnSpaceSuperLays.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	finish();
	            Component.ReflectionWindow.getLays().SuperLays.CreateSelectorPanel(Component.ParentActivity,getString(R.string.SLayers)).show();
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
            	SpecifyReflectionWindowActualityInterval();
            }
        });
        //.
        btnCurrentReflectionWindowActualityInterval = (Button)findViewById(R.id.btnCurrentReflectionWindowActualityInterval);
        btnCurrentReflectionWindowActualityInterval.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
	            Component.ReflectionWindow.ResetActualityInterval();
	            //.
            	finish();
            }
        });
        //.
        lvTileServerVisualizations = (ListView)findViewById(R.id.lvTileServerVisualizations);
        lvTileServerVisualizations.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        final TTileServerVisualizationUserData.TTileServerVisualization TSV;
		if (Component.ReflectionWindow.TileServerVisualizationUserData.TileServerVisualizations != null) {
			TSV = Component.ReflectionWindow.TileServerVisualizationUserData.TileServerVisualizations.get(0);
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
    					Component.ReflectionWindow.DoOnSetVisualizationUserData();
    		    	}
    		    	catch (Exception E) {
    		            Toast.makeText(Component.context, E.getMessage(), Toast.LENGTH_LONG).show();
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
	            try {
					Component.ReflectionWindow.CheckSpaceLays().SuperLays.CreateSelectorPanel(TReflectionWindowConfigurationPanel.this,TReflectionWindowConfigurationPanel.this.getString(R.string.SLayersForLabels)).show();
				} catch (Exception E) {
        			Toast.makeText(TReflectionWindowConfigurationPanel.this, Component.context.getString(R.string.SError)+E.getMessage(), Toast.LENGTH_LONG).show();  						
				}
            }
        });
        btnSpaceSuperLays1.setEnabled(Component.Configuration.ReflectionWindow_flShowHints);
        //.
        btnSetHistoryTime = (Button)findViewById(R.id.btnSetHistoryTime);
        btnSetHistoryTime.setOnClickListener(new OnClickListener() {
        	
            public void onClick(View v) {
        		final CharSequence[] _items;
    			_items = new CharSequence[2];
    			_items[0] = getString(R.string.SGoToHistoryTime);
    			_items[1] = getString(R.string.SSetHistoryTimeToCurrent);
        		AlertDialog.Builder builder = new AlertDialog.Builder(TReflectionWindowConfigurationPanel.this);
        		builder.setTitle(R.string.SHistoryTime);
        		builder.setNegativeButton(Component.context.getString(R.string.SCancel),null);
        		builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
        			@Override
        			public void onClick(DialogInterface arg0, int arg1) {
	                	try {
	    					switch (arg1) {
	    					case 0:
	    		            	SpecifyReflectionWindowHistoryTime();
	    						break; //. >
	    						
	    					case 1:
	    			            Component.ReflectionWindow.ResetActualityInterval();
	    			            //.
	    		            	finish();
	    						break; //. >
	    					}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TReflectionWindowConfigurationPanel.this, Component.context.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
						//.
						arg0.dismiss();
        			}
        		});
        		AlertDialog alert = builder.create();
        		alert.show();
            }
        });
        //.
        btnSetHistoryTimeNow = (Button)findViewById(R.id.btnSetHistoryTimeNow);
        btnSetHistoryTimeNow.setOnClickListener(new OnClickListener() {
        	
            public void onClick(View v) {
	            Component.ReflectionWindow.ResetActualityInterval();
	            //.
            	finish();
            }
        });
        //.
        btnSetHistoryTimeLast = (Button)findViewById(R.id.btnSetHistoryTimeLast);
        btnSetHistoryTimeLast.setOnClickListener(new OnClickListener() {
        	
            public void onClick(View v) {
            	SpecifyReflectionWindowHistoryTime();
            }
        });
        //.
        btnLoadTileServerDataFromServer = (Button)findViewById(R.id.btnLoadTileServerDataFromServer);
        btnLoadTileServerDataFromServer.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	new TTileImageryDataLoading();
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
        //. Set result CANCELED in case the user backs out
        setResult(Activity.RESULT_CANCELED);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        //. suppying the SpaceTileImagery with TileImageryData if there was none
        if ((Component.SpaceTileImagery != null) && Component.SpaceTileImagery.Data.IsInitialized() && Component.SpaceTileImagery.Data.TileServers_IsNull())
        	new TTileImageryDataLoading();
    }
    
    private void UpdateLayout() {
        switch (Component.ViewMode) {
        case TReflectorComponent.VIEWMODE_REFLECTIONS:
            TilesModeLayout.setVisibility(LinearLayout.GONE);
            ReflectionsModeLayout.setVisibility(LinearLayout.VISIBLE);
        	break; //. >
        	
        case TReflectorComponent.VIEWMODE_TILES:
            ReflectionsModeLayout.setVisibility(LinearLayout.GONE);
            TilesModeLayout.setVisibility(LinearLayout.VISIBLE);
        	break; //. >
        }
    }
    
    public void SpecifyReflectionWindowActualityInterval() {
        final Dialog mDateTimeDialog = new Dialog(this);
        // Inflate the root layout
        final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
        // Grab widget instance
        final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);
        // Check is system is set to use 24h time (this doesn't seem to work as expected though)
        final String timeS = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
        final boolean is24h = !(timeS == null || timeS.equals("12"));
        // Update demo TextViews when the "OK" button is clicked 
        ((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                        mDateTimePicker.clearFocus();
                        //.
                		double EndTimestamp = OleDate.UTCToLocalTime(mDateTimePicker.GetDateTime());
        	            Component.ReflectionWindow.SetActualityInterval(EndTimestamp-1.0,EndTimestamp);
                        mDateTimeDialog.dismiss();
                }
        });
        // Reset Date and Time pickers when the "Reset" button is clicked
        ((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                        mDateTimePicker.reset();
                }
        });
        // Setup TimePicker
        mDateTimePicker.setIs24HourView(is24h);
        // No title on the dialog window
        mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set the dialog content view
        mDateTimeDialog.setContentView(mDateTimeDialogView);
        // Display the dialog
        mDateTimeDialog.show();    
    }
    
    public void SpecifyReflectionWindowHistoryTime() {
        final Dialog mDateTimeDialog = new Dialog(this);
        // Inflate the root layout
        final RelativeLayout mDateTimeDialogView = (RelativeLayout) getLayoutInflater().inflate(R.layout.date_time_dialog, null);
        // Grab widget instance
        final DateTimePicker mDateTimePicker = (DateTimePicker) mDateTimeDialogView.findViewById(R.id.DateTimePicker);
        /*///+ double CurrentHistoryTime = Reflector.ReflectionWindow.GetActualityInterval().GetEndTimestamp();
        if (CurrentHistoryTime != Double.MAX_VALUE)
        	mDateTimePicker.setDateTime(CurrentHistoryTime);*/
        // Check is system is set to use 24h time (this doesn't seem to work as expected though)
        final String timeS = android.provider.Settings.System.getString(getContentResolver(), android.provider.Settings.System.TIME_12_24);
        final boolean is24h = !(timeS == null || timeS.equals("12"));
        // Update demo TextViews when the "OK" button is clicked 
        ((Button) mDateTimeDialogView.findViewById(R.id.SetDateTime)).setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                        mDateTimePicker.clearFocus();
                        //.
                		double EndTimestamp = OleDate.UTCToLocalTime(mDateTimePicker.GetDateTime());
        	            Component.ReflectionWindow.SetActualityInterval(EndTimestamp-1.0,EndTimestamp);
                        mDateTimeDialog.dismiss();
                        //.
		            	finish();
                }
        });
        // Reset Date and Time pickers when the "Reset" button is clicked
        ((Button) mDateTimeDialogView.findViewById(R.id.ResetDateTime)).setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                        mDateTimePicker.reset();
                }
        });
        // Setup TimePicker
        mDateTimePicker.setIs24HourView(is24h);
        // No title on the dialog window
        mDateTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Set the dialog content view
        mDateTimeDialog.setContentView(mDateTimeDialogView);
        // Display the dialog
        mDateTimeDialog.show();    
    }
    
    private TTileImageryData.TTileServer TileServer;
    private TTileImagery.TTileServerProviderCompilationDescriptor[] Compilations;
    
    private void lvTileServerData_Update() {
        lvTileServerData.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		if ((Component.SpaceTileImagery != null) && (Component.SpaceTileImagery.Data.TileServers != null)) {
			TileServer = Component.SpaceTileImagery.Data.TileServers.get(0); //. get native tile-server
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
			TTileImagery.TTileServerProviderCompilationDescriptors SC = Component.SpaceTileImagery.ActiveCompilationSet_Descriptors();
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
    	Component.ViewMode_Tiles_SetActiveCompilation(C);
    }    
    
    private class TTileImageryDataLoading extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_DONE 					= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

        private ProgressDialog progressDialog; 
    	
    	public TTileImageryDataLoading() {
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
                	if (Component.SpaceTileImagery != null) 
                    		Component.SpaceTileImagery.LoadDataFromServer();
    				//.
        			MessageHandler.obtainMessage(MESSAGE_DONE).sendToTarget();
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(TReflectionWindowConfigurationPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_DONE:
	                	lvTileServerData_Update();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TReflectionWindowConfigurationPanel.this);    
		            	progressDialog.setMessage(TReflectionWindowConfigurationPanel.this.getString(R.string.SLoadingTileImageryData));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(false);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		            	progressDialog.dismiss(); 
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	progressDialog.setProgress((Integer)msg.obj);
		            	//.
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }    
}
