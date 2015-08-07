package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream;

import java.util.ArrayList;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannelIDs;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TTimestampedInt166DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.GeoLocation.GPS.TGPSFixDataType;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TReflectorTrack;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessors.Audio.AAC.TAACChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.ChannelProcessors.Video.H264I.TH264IChannelProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.GeoLocation.GPS.TGPSChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannelFlowControl;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TUserAccessKey;

@SuppressLint("HandlerLeak")
public class TDataStreamPanel extends Activity {

	public static final int PARAMETERS_TYPE_NOO 	= 0;
	public static final int PARAMETERS_TYPE_OID 	= 1;
	public static final int PARAMETERS_TYPE_OIDX 	= 2;

	
	private boolean flExists = false;
	//.
	private String 	ServerAddress;
	private int 	ServerPort;
	//.
	private long	UserID;
	private String 	UserPassword;
	//.
	private TCoGeoMonitorObject 	Object;
	//.
	private byte[] 				StreamDescriptorData;
	private TStreamDescriptor 	StreamDescriptor;
	private TChannelIDs			StreamChannels = null;
	//.
	private ArrayList<TStreamChannelConnectorAbstract> 	StreamChannelConnectors = new ArrayList<TStreamChannelConnectorAbstract>();
	//.
	@SuppressWarnings("unused")
	private boolean IsInFront = false;
	//.
	@SuppressWarnings("unused")
	private ScrollView svSpace;
	//.
	private TextView	lbStatus;
	//.
	private LinearLayout llAndroidStateADS;
	private LinearLayout llEnvironmentConditionsENVC;
	private LinearLayout llEnvironmentConditionsXENVC;
	private LinearLayout llTelemetryTLR;
	private LinearLayout llAudioAAC;
	private LinearLayout llVideoH264;
	//.
	private int LinkedReflectorID = 0;
	
    public void onCreate(Bundle savedInstanceState) {
    	try {
            super.onCreate(savedInstanceState);
            //.
            Bundle extras = getIntent().getExtras(); 
            if (extras != null) {
            	int ParametersType = extras.getInt("ParametersType");
            	//.
            	ServerAddress = extras.getString("ServerAddress");
            	ServerPort = extras.getInt("ServerPort");
            	//.
            	UserID = extras.getLong("UserID");
            	UserPassword = extras.getString("UserPassword");
            	//.
        		TReflector Reflector = TReflector.GetReflector();
            	switch (ParametersType) {
            	
            	case PARAMETERS_TYPE_NOO:
                	Object = null;
            		break; //. >
            		
            	case PARAMETERS_TYPE_OID:
                	long ObjectID = extras.getLong("ObjectID");
                	Object = new TCoGeoMonitorObject(Reflector.Component.Server, ObjectID);
            		break; //. >
            		
            	case PARAMETERS_TYPE_OIDX:
                	int ObjectIndex = extras.getInt("ObjectIndex");
                	Object = Reflector.Component.CoGeoMonitorObjects.Items[ObjectIndex];
            		break; //. >
            	}
            	//.
            	StreamDescriptorData = extras.getByteArray("StreamDescriptorData");
            	StreamDescriptor = new TStreamDescriptor(StreamDescriptorData,com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider.Instance);
            	//.
            	byte[] StreamChannelsBA = extras.getByteArray("StreamChannels");
            	if (StreamChannelsBA != null) 
            		StreamChannels = new TChannelIDs(StreamChannelsBA);
            	else
            		StreamChannels = null;
            }
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		//.
            setContentView(R.layout.sensorsmodule_datastream_panel);
            //.
            svSpace = (ScrollView)findViewById(R.id.svSpace);
            //.
            lbStatus = (TextView)findViewById(R.id.lbStatus);
            //.
            llAndroidStateADS = (LinearLayout)findViewById(R.id.llAndroidStateADS);
            llEnvironmentConditionsENVC = (LinearLayout)findViewById(R.id.llEnvironmentConditionsENVC);
            llEnvironmentConditionsXENVC = (LinearLayout)findViewById(R.id.llEnvironmentConditionsXENVC);
            llTelemetryTLR = (LinearLayout)findViewById(R.id.llTelemetryTLR);
            llAudioAAC = (LinearLayout)findViewById(R.id.llAudioAAC);
            llVideoH264 = (LinearLayout)findViewById(R.id.llVideoH264);
        	//.
    		StreamChannelConnectors_Initialize();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
		//.
		flExists = true;
    }
	
    @Override
    public void onDestroy() {
    	flExists = false;
		//.
    	try {
    		StreamChannelConnectors_Finalize();
    		//.
    		if (StreamDescriptor != null) {
    			StreamDescriptor.Close();
    			StreamDescriptor = null;
    		}
		} catch (Exception E) {
			PostException(E);
		}
    	//.
		super.onDestroy();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		IsInFront = false;
	}

	protected void onResume() {
		super.onResume();
		IsInFront = true;
	}

	private void StreamChannelConnectors_Initialize() throws Exception {
		StreamChannelConnectors_Finalize();
		//.
		for (int I = 0; I < StreamDescriptor.Channels.size(); I++) {
			TStreamChannel Channel = (TStreamChannel)StreamDescriptor.Channels.get(I);
			if ((StreamChannels == null) || StreamChannels.IDExists(Channel.ID)) {
				TStreamChannelConnectorAbstract ChannelConnector = StreamChannelConnectors_CreateOneForChannel(Channel);
				if (ChannelConnector != null) {
					StreamChannelConnectors.add(ChannelConnector);
					//.
					Layout_UpdateForChannel(Channel);
					//.
					Channel.Start();
					//.
					ChannelConnector.Start();
				}			  
			}
		}
	}
	
	private void StreamChannelConnectors_Finalize() throws Exception {
		for (int I = 0; I < StreamChannelConnectors.size(); I++) {
			TStreamChannelConnectorAbstract ChannelConnector = StreamChannelConnectors.get(I);
			//.
			ChannelConnector.Stop(false);
			//.
			ChannelConnector.Channel.Close();
			//.
			ChannelConnector.Destroy(false);
		}
		StreamChannelConnectors.clear();
		//.
		Layout_Reset();
	}
	
	protected TStreamChannelConnectorAbstract StreamChannelConnectors_CreateOneForChannel(TStreamChannel Channel) throws Exception {
		TStreamChannelConnectorAbstract ChannelConnector = new TStreamChannelConnector(TStreamChannelConnector.VERSION_CHANNELBYID, this, ServerAddress,ServerPort, UserID,UserPassword, Object, Channel, TUserAccessKey.GenerateValue(), new TStreamChannelConnectorAbstract.TOnProgressHandler(Channel) {
			
			@Override
			public void DoOnProgress(int ReadSize, TCanceller Canceller) {
				TDataStreamPanel.this.PostStatusMessage("");
			}
		}, new TStreamChannelConnectorAbstract.TOnIdleHandler(Channel) {
			
			@Override
			public void DoOnIdle(TCanceller Canceller) {
				TDataStreamPanel.this.PostStatusMessage(TDataStreamPanel.this.getString(R.string.SChannelIdle)+Channel.Name);
			}
		}, new TStreamChannelConnectorAbstract.TOnExceptionHandler(Channel) {
			
			@Override
			public void DoOnException(Exception E) {
				TDataStreamPanel.this.PostException(E);
			}
		});
		//.
		return ChannelConnector;
	}
	
	private void Layout_Reset() {
		llAndroidStateADS.setVisibility(View.GONE);
		//.
		llEnvironmentConditionsENVC.setVisibility(View.GONE);
		//.
		llEnvironmentConditionsXENVC.setVisibility(View.GONE);
		//.
		llTelemetryTLR.setVisibility(View.GONE);
		llTelemetryTLR.removeAllViews();
		//.
		llAudioAAC.setVisibility(View.GONE);
		//.
		llVideoH264.setVisibility(View.GONE);
	}
	
	private void Layout_UpdateForChannel(TStreamChannel Channel) throws Exception {
		if (Channel instanceof TADSChannel) {
			TADSChannel ADSChannel = (TADSChannel)Channel;
			//.
			final EditText edBatteryVoltage = (EditText)findViewById(R.id.edAndroidStateADSBatteryVoltage);
			final EditText edBatteryTemperature = (EditText)findViewById(R.id.edAndroidStateADSBatteryTemperature);
			final EditText edBatteryLevel = (EditText)findViewById(R.id.edAndroidStateADSBatteryLevel);
			final EditText edBatteryHealth = (EditText)findViewById(R.id.edAndroidStateADSBatteryHealth);
			final EditText edBatteryStatus = (EditText)findViewById(R.id.edAndroidStateADSBatteryStatus);
			final EditText edBatteryPlugType = (EditText)findViewById(R.id.edAndroidStateADSBatteryPlugType);
			final EditText edCellularConnectorSignalStrength = (EditText)findViewById(R.id.edAndroidStateADSCellularConnectorSignalStrength);
			//.
			ADSChannel.OnBatteryVoltageHandler = new TADSChannel.TDoOnInt32ValueHandler() {
				@Override
				public void DoOnValue(int Value) {
					PostTextViewValueMessage(edBatteryVoltage,String.format(Locale.getDefault(),"%.2f",Value/1000.0)+" V");
				}
			};
			ADSChannel.OnBatteryTemperatureHandler = new TADSChannel.TDoOnInt32ValueHandler() {
				@Override
				public void DoOnValue(int Value) {
					PostTextViewValueMessage(edBatteryTemperature,String.format(Locale.getDefault(),"%.2f",Value/10.0)+" C");
				}
			};
			ADSChannel.OnBatteryLevelHandler = new TADSChannel.TDoOnInt32ValueHandler() {
				@Override
				public void DoOnValue(int Value) {
					PostTextViewValueMessage(edBatteryLevel,String.format(Locale.getDefault(),"%.1f",Value+0.0)+" %");
				}
			};
			ADSChannel.OnBatteryHealthHandler = new TADSChannel.TDoOnInt32ValueHandler() {
				@Override
				public void DoOnValue(int Value) {
					String HealthString = "?"; 
					switch (Value) {
					
				    case BatteryManager.BATTERY_HEALTH_DEAD:
				        HealthString = getString(R.string.SBad);
				        break; //. >
				        
				    case BatteryManager.BATTERY_HEALTH_GOOD:
				        HealthString = getString(R.string.SGoodCondition);
				        break; //. >
				        
				    case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
				        HealthString = getString(R.string.SOverVoltage);
				        break; //. >
				        
				    case BatteryManager.BATTERY_HEALTH_OVERHEAT:
				        HealthString = getString(R.string.SOverHeat);
				        break; //. >
				        
				    case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
				        HealthString = getString(R.string.SFailure);
				        break; //. >
				    }					
					//.
					PostTextViewValueMessage(edBatteryHealth,HealthString);
				}
			};
			ADSChannel.OnBatteryStatusHandler = new TADSChannel.TDoOnInt32ValueHandler() {
				@Override
				public void DoOnValue(int Value) {
					String StatusString = "?";
				    switch (Value) {
				    
				    case BatteryManager.BATTERY_STATUS_CHARGING:
				        StatusString = getString(R.string.SCharging);
				        break; //. >
				    case BatteryManager.BATTERY_STATUS_DISCHARGING:
				        StatusString = getString(R.string.SDischarging);
				        break; //. >
				        
				    case BatteryManager.BATTERY_STATUS_FULL:
				        StatusString = getString(R.string.SFull);
				        break; //. >
				        
				    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
				        StatusString = getString(R.string.SNotCharging);
				        break; //. >
				    }
				    //.
				    PostTextViewValueMessage(edBatteryStatus,StatusString);
				}
			};
			ADSChannel.OnBatteryPlugTypeHandler = new TADSChannel.TDoOnInt32ValueHandler() {
				@Override
				public void DoOnValue(int Value) {
					String PlugType = "?";
				    switch (Value) {
				    case BatteryManager.BATTERY_PLUGGED_AC:
				        PlugType = getString(R.string.SAC);
				        break;
				    case BatteryManager.BATTERY_PLUGGED_USB:
				        PlugType = getString(R.string.SUSB);
				        break;
				    }					//.
				    //.
					PostTextViewValueMessage(edBatteryPlugType,PlugType);
				}
			};
			ADSChannel.OnCellularConnectorSignalStrengthHandler = new TADSChannel.TDoOnInt32ValueHandler() {
				@Override
				public void DoOnValue(int Value) {
					PostTextViewValueMessage(edCellularConnectorSignalStrength,String.format(Locale.getDefault(),"%.2f",100.0*Value/31.0)+" %");
				}
			};
			//.
			llAndroidStateADS.setVisibility(View.VISIBLE);
			//.
			return; //. ->
		};
		if (Channel instanceof TENVCChannel) {
			TENVCChannel ENVCChannel = (TENVCChannel)Channel;
			//.
			final EditText edTemperature = (EditText)findViewById(R.id.edEnvironmentConditionsENVCTemperature);
			final EditText edPressure = (EditText)findViewById(R.id.edEnvironmentConditionsENVCPressure);
			final EditText edHumidity = (EditText)findViewById(R.id.edEnvironmentConditionsENVCHumidity);
			//.
			ENVCChannel.OnTemperatureHandler = new TENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					PostTextViewValueMessage(edTemperature,String.format(Locale.getDefault(),"%.2f",Value)+" C");
				}
			};
			ENVCChannel.OnPressureHandler = new TENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					PostTextViewValueMessage(edPressure,String.format(Locale.getDefault(),"%.2f",Value)+" mbar");
				}
			};
			ENVCChannel.OnHumidityHandler = new TENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					PostTextViewValueMessage(edHumidity,String.format(Locale.getDefault(),"%.2f",Value)+" %");
				}
			};
			//.
			llEnvironmentConditionsENVC.setVisibility(View.VISIBLE);
			//.
			return; //. ->
		};
		if (Channel instanceof TXENVCChannel) {
			TXENVCChannel XENVCChannel = (TXENVCChannel)Channel;
			//.
			final EditText edTemperature = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCTemperature);
			final EditText edPressure = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCPressure);
			final EditText edRelativeHumidity = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCRelativeHumidity);
			final EditText edLight = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCLight);
			final EditText edAcceleration = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCAcceleration);
			final EditText edMagneticField = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCMagneticField);
			final EditText edGyroscope = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCGyroscope);
			//.
			XENVCChannel.OnTemperatureHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					PostTextViewValueMessage(edTemperature,String.format(Locale.getDefault(),"%.2f",Value)+" C");
				}
			};
			XENVCChannel.OnPressureHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					PostTextViewValueMessage(edPressure,String.format(Locale.getDefault(),"%.2f",Value)+" mbar");
				}
			};
			XENVCChannel.OnRelativeHumidityHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					PostTextViewValueMessage(edRelativeHumidity,String.format(Locale.getDefault(),"%.2f",Value)+" %");
				}
			};
			XENVCChannel.OnLightHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					PostTextViewValueMessage(edLight,String.format(Locale.getDefault(),"%.2f",Value)+" lx");
				}
			};
			XENVCChannel.OnAccelerationHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					PostTextViewValueMessage(edAcceleration,String.format(Locale.getDefault(),"%.2f",Value)+" m/s^2");
				}
			};
			XENVCChannel.OnMagneticFieldHandler = new TXENVCChannel.TDoOn3ValueHandler() {
				@Override
				public void DoOn3Value(double Value, double Value1, double Value2) {
					PostTextViewValueMessage(edMagneticField,"(X: "+String.format(Locale.getDefault(),"%.2f",Value)+", Y: "+String.format(Locale.getDefault(),"%.2f",Value1)+", Z: "+String.format(Locale.getDefault(),"%.2f",Value2)+") mT");
				}
			};
			XENVCChannel.OnGyroscopeHandler = new TXENVCChannel.TDoOn3ValueHandler() {
				@Override
				public void DoOn3Value(double Value, double Value1, double Value2) {
					PostTextViewValueMessage(edGyroscope,"(X: "+String.format(Locale.getDefault(),"%.2f",Value)+", Y: "+String.format(Locale.getDefault(),"%.2f",Value1)+", Z: "+String.format(Locale.getDefault(),"%.2f",Value2)+") rad/s");
				}
			};
			//.
			llEnvironmentConditionsXENVC.setVisibility(View.VISIBLE);
			//.
			return; //. ->
		};
		if (Channel instanceof TTLRChannel) {
			TTLRChannel TLRChannel = (TTLRChannel)Channel;
			//.
			LinearLayout llTelemetryTLR = (LinearLayout)findViewById(R.id.llTelemetryTLR);
			//.
			int TableTextSize = 18;
			//.
			LinearLayout.LayoutParams LLP = new LinearLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT);
			//. channel type header
			TextView lbTelemetryTLR = new TextView(this);
			lbTelemetryTLR.setBackgroundColor(0xFF999999);
			lbTelemetryTLR.setText(R.string.STelemetryTLR);
			lbTelemetryTLR.setTextSize(TypedValue.COMPLEX_UNIT_SP,18);
			lbTelemetryTLR.setTextColor(0xFF000000);
			lbTelemetryTLR.setGravity(Gravity.CENTER);
			llTelemetryTLR.addView(lbTelemetryTLR, new LinearLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,TableRow.LayoutParams.WRAP_CONTENT));
			//. channel header
			String CN = TLRChannel.Name;
			if (TLRChannel.Info.length() > 0)
				CN += " "+"/"+TLRChannel.Info+"/";
			TextView lbTelemetryTLRName = new TextView(this);
			lbTelemetryTLRName.setText(CN);
			lbTelemetryTLRName.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
			lbTelemetryTLRName.setTextColor(0xFF0E4E26);
			lbTelemetryTLRName.setTypeface(null, Typeface.BOLD);
			lbTelemetryTLRName.setGravity(Gravity.CENTER);
			llTelemetryTLR.addView(lbTelemetryTLRName, LLP);
			//.
			TableLayout.LayoutParams TLP = new TableLayout.LayoutParams();
			TLP.height = TableRow.LayoutParams.WRAP_CONTENT; 
			TLP.width = TableRow.LayoutParams.MATCH_PARENT;
			TableLayout tlTelemetryTLR = new TableLayout(this);
			//.
			TableRow Row = new TableRow(this);
			//.
			TableRow.LayoutParams RowParams = new TableRow.LayoutParams();
			RowParams.height = TableRow.LayoutParams.WRAP_CONTENT;
			RowParams.width = TableRow.LayoutParams.MATCH_PARENT;
			//.
			TableRow.LayoutParams ColParams = new TableRow.LayoutParams();
			ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
			ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
			ColParams.weight = 0.5F;
			//.			
			TextView Col = new TextView(this);
			Col.setText(R.string.SParameter);
			Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
			Col.setTextColor(Color.GRAY);
			Col.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
			Col.setGravity(Gravity.CENTER);
			//.
			Row.addView(Col, ColParams);
			//.
			ColParams = new TableRow.LayoutParams();
			ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
			ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
			ColParams.weight = 0.3F;
			//.			
			Col = new TextView(this);
			Col.setText(R.string.SValue);
			Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
			Col.setTextColor(Color.GRAY);
			Col.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
			Col.setGravity(Gravity.CENTER);
			//.
			Row.addView(Col, ColParams);
			//.
			ColParams = new TableRow.LayoutParams();
			ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
			ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
			ColParams.weight = 0.2F;
			//.			
			Col = new TextView(this);
			Col.setText(R.string.SUnit);
			Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
			Col.setTextColor(Color.GRAY);
			Col.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG);
			Col.setGravity(Gravity.CENTER);
			//.
			Row.addView(Col, ColParams);
			//.
			tlTelemetryTLR.addView(Row, RowParams);		
			//. data rows
			Typeface ValueTF = Typeface.create(Typeface.SERIF, Typeface.BOLD);
			Typeface UnitTF = Typeface.create(Typeface.SERIF, Typeface.ITALIC);
			int DataTypesCount = ((TLRChannel.DataTypes != null) ? TLRChannel.DataTypes.Items.size() : 0);
			if (DataTypesCount > 0) {
				int Cnt = TLRChannel.DataTypes.Items.size();
				for (int I = 0; I < Cnt; I++) {
					TDataType DataType = TLRChannel.DataTypes.Items.get(I); 
					//. row
					Row = new TableRow(this);
					//.
					RowParams = new TableRow.LayoutParams();
					RowParams.height = TableRow.LayoutParams.WRAP_CONTENT;
					RowParams.width = TableRow.LayoutParams.MATCH_PARENT;
					//.
					ColParams = new TableRow.LayoutParams();
					ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
					ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
					ColParams.weight = 0.5F;
					//.			
					Col = new TextView(this);
					Col.setText(DataType.GetName(this)+": ");
					Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
					Col.setTextColor(Color.BLACK);
					Col.setGravity(Gravity.LEFT);
					//.
					Row.addView(Col, ColParams);
					//.
					ColParams = new TableRow.LayoutParams();
					ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
					ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
					ColParams.weight = 0.3F;
					//.			
					Col = new TextView(this);
					Col.setText("?");
					Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
					Col.setTextColor(Color.RED);
					Col.setTypeface(ValueTF);
					Col.setGravity(Gravity.LEFT);
					//.
					Row.addView(Col, ColParams);
					DataType.Extra = Col; 
					//.
					ColParams = new TableRow.LayoutParams();
					ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
					ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
					ColParams.weight = 0.2F;
					//.			
					Col = new TextView(this);
					Col.setText(DataType.GetValueUnit(this));
					Col.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
					Col.setTextColor(Color.BLUE);
					Col.setTypeface(UnitTF);
					Col.setGravity(Gravity.LEFT);
					//.
					Row.addView(Col, ColParams);
					//.
					tlTelemetryTLR.addView(Row, RowParams);		
				}
			}
			if (Channel instanceof TGPSChannel) {
				Row = new TableRow(this);
				//.
				RowParams = new TableRow.LayoutParams();
				RowParams.height = TableRow.LayoutParams.WRAP_CONTENT;
				RowParams.width = TableRow.LayoutParams.MATCH_PARENT;
				//.
				ColParams = new TableRow.LayoutParams();
				ColParams.height = TableRow.LayoutParams.WRAP_CONTENT;
				ColParams.width = TableRow.LayoutParams.MATCH_PARENT;
				ColParams.weight = 1.0F;
				//.			
				Button btnMonitor = new Button(this);
				btnMonitor.setText(R.string.SLocationMonitor);
				btnMonitor.setTextSize(TypedValue.COMPLEX_UNIT_SP,TableTextSize);
				btnMonitor.setTextColor(Color.BLACK);
				btnMonitor.setOnClickListener(new OnClickListener() {
		        	@Override
		            public void onClick(View v) {
		        		LinkedReflectorID = TReflector.GetNextID();
		        		//.
		        		Intent intent = new Intent(TDataStreamPanel.this, TReflector.class);
						intent.putExtra("ID", LinkedReflectorID);
						intent.putExtra("Reason", TReflectorComponent.REASON_MONITORGEOLOCATION);
						TDataStreamPanel.this.startActivity(intent);
		            }
		        });
				//.
				Row.addView(btnMonitor, ColParams);
				//.
				tlTelemetryTLR.addView(Row, RowParams);		
			}
			llTelemetryTLR.addView(tlTelemetryTLR, TLP);
			//.			
			TLRChannel.OnDataHandler = new TTLRChannel.TDoOnDataHandler() {
				@Override
				public void DoOnData(TDataType DataType) {
					PostDataType(DataType.Clone());
				}
			};
			//.
			llTelemetryTLR.setVisibility(View.VISIBLE);
			//.
			return; //. ->
		};
		if (Channel instanceof TAACChannel) {
			TAACChannel AACChannel = (TAACChannel)Channel;
			//.
			final EditText edAudioBuffersProcessed = (EditText)findViewById(R.id.edAudioAACBuffersProcessed);
			//.
			TAACChannelProcessor AACChannelProcessor = (TAACChannelProcessor)AACChannel.GetProcessor();
			AACChannelProcessor.StatisticHandler = new TAACChannelProcessor.TStatisticHandler() {
				
				@Override
				public void DoOnAudioBuffer(int AudioBuffersCount) {
					PostTextViewValueMessage(edAudioBuffersProcessed,Integer.toString(AudioBuffersCount));
				}
			};
			//
			llAudioAAC.setVisibility(View.VISIBLE);
		};
		if (Channel instanceof TH264IChannel) {
			TH264IChannel H264Channel = (TH264IChannel)Channel;
			//.
			final EditText edVideoBuffersProcessed = (EditText)findViewById(R.id.edVideoH264BuffersProcessed);
			//.
			final LinearLayout llVideoH264Bitrate = (LinearLayout)findViewById(R.id.llVideoH264Bitrate);
			final EditText edVideoBitrate = (EditText)findViewById(R.id.edVideoH264Bitrate);
			//.
			final SurfaceView svVideoH264 = (SurfaceView)findViewById(R.id.svVideoH264);
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			int height = size.y;
			RelativeLayout.LayoutParams RLP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,height);
			svVideoH264.setLayoutParams(RLP);
			//.
			if (Object != null) {
				TH264IChannelFlowControl H264IChannelFlowControl = new TH264IChannelFlowControl(H264Channel, this, ServerAddress,ServerPort, UserID,UserPassword, Object, new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TStreamChannelConnectorAbstract.TOnProgressHandler(H264Channel) {
					
					@Override
					public void DoOnProgress(int ReadSize, TCanceller Canceller) {
						TDataStreamPanel.this.PostStatusMessage("");
					}
				}, new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TStreamChannelConnectorAbstract.TOnIdleHandler(Channel) {
					
					@Override
					public void DoOnIdle(TCanceller Canceller) {
						TDataStreamPanel.this.PostStatusMessage(TDataStreamPanel.this.getString(R.string.SChannelIdle)+Channel.Name);
					}
				}, new com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.ControlsModule.Model.Data.ControlStream.TStreamChannelConnectorAbstract.TOnExceptionHandler(Channel) {
					
					@Override
					public void DoOnException(Exception E) {
						TDataStreamPanel.this.PostException(E);
					}
				}, new TH264IChannelFlowControl.TNotificationHandler() {
					
					@Override
					public void DoOnBitrateChange(int Bitrate) {
						if (Bitrate > 0) {
							PostTextViewValueMessage(edVideoBitrate,Integer.toString((int)(Bitrate/1024))+" kbps");
							PostViewShow(llVideoH264Bitrate);
						}
						else
							PostViewGone(llVideoH264Bitrate);
					}
				});
				H264Channel.FlowControl_Initialize(H264IChannelFlowControl);
			}
			//.
			final TH264IChannelProcessor H264ChannelProcessor = (TH264IChannelProcessor)H264Channel.GetProcessor();
			H264ChannelProcessor.StatisticHandler = new TH264IChannelProcessor.TStatisticHandler() {
				
				@Override
				public void DoOnVideoBuffer(int VideoBuffersCount) {
					PostTextViewValueMessage(edVideoBuffersProcessed,Integer.toString(VideoBuffersCount));
				}
			};
			//.
			svVideoH264.getHolder().addCallback(new SurfaceHolder.Callback() {
				
				@Override
				public void surfaceDestroyed(SurfaceHolder holder) {
					H264ChannelProcessor.Stop();
				}
				
				@Override
				public void surfaceCreated(SurfaceHolder holder) {
				}
				
				@Override
				public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
					H264ChannelProcessor.Start(holder.getSurface(), width,height);
				}
			});
			//
			llVideoH264.setVisibility(View.VISIBLE);
		};
	}
	
	private static final int MESSAGE_SHOWEXCEPTION 			= -1;
	private static final int MESSAGE_SHOWSTATUSMESSAGE 		= 1;
	private static final int MESSAGE_DOONDATATYPE			= 2;
	private static final int MESSAGE_TEXTVIEW_WRITEVALUE	= 3;
	private static final int MESSAGE_VIEW_SHOW				= 4;
	private static final int MESSAGE_VIEW_GONE				= 5;
	
	public static class TTextViewValueString {
		
		public TextView TW;
		public String VS;
		
		public TTextViewValueString(TextView pTW, String pVS) {
			TW = pTW;
			VS = pVS;
		}
	}
	
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_SHOWEXCEPTION:
					if (!flExists)
						break; // . >
    				Throwable E = (Throwable)msg.obj;
    				String EM = E.getMessage();
    				if (EM == null) 
    					EM = E.getClass().getName();
    				//.
    				Toast.makeText(TDataStreamPanel.this,EM,Toast.LENGTH_LONG).show();
    				// .
    				break; // . >

    			case MESSAGE_SHOWSTATUSMESSAGE:
					if (!flExists)
						break; // . >
    				String S = (String)msg.obj;
    				//.
    				if (S.length() > 0) {
    					lbStatus.setText(S);
    					lbStatus.setVisibility(View.VISIBLE);
    				}
    				else {
    					lbStatus.setText("");
    					lbStatus.setVisibility(View.GONE);
    				}
    				// .
    				break; // . >

    			case MESSAGE_DOONDATATYPE:
					if (!flExists)
						break; // . >
    				TDataType DataType = (TDataType)msg.obj;
    				//.
    				if (DataType.Extra != null)
    					((TextView)DataType.Extra).setText(DataType.GetValueString(TDataStreamPanel.this));
    				//.
					lbStatus.setText("");
					lbStatus.setVisibility(View.GONE);
					//.
					if (DataType instanceof TGPSFixDataType) 
						DoOnGPSFixDataType((TGPSFixDataType)DataType);
    				//.
    				break; // . >
    				
    			case MESSAGE_TEXTVIEW_WRITEVALUE:
					if (!flExists)
						break; // . >
    				TTextViewValueString ETS = (TTextViewValueString)msg.obj;
    				//.
    				ETS.TW.setText(ETS.VS);
    				//.
					lbStatus.setText("");
					lbStatus.setVisibility(View.GONE);
    				//.
    				break; // . >

    			case MESSAGE_VIEW_SHOW:
					if (!flExists)
						break; // . >
    				View view = (View)msg.obj;
    				//.
    				view.setVisibility(View.VISIBLE);
    				//.
    				break; // . >
    				
    			case MESSAGE_VIEW_GONE:
					if (!flExists)
						break; // . >
    				view = (View)msg.obj;
    				//.
    				view.setVisibility(View.GONE);
    				//.
    				break; // . >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	protected void PostStatusMessage(String S) {
		MessageHandler.obtainMessage(MESSAGE_SHOWSTATUSMESSAGE,S).sendToTarget();
	}
	
	protected void PostException(Throwable E) {
		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
	
	protected void PostDataType(TDataType DataType) {
		MessageHandler.obtainMessage(MESSAGE_DOONDATATYPE,DataType).sendToTarget();
	}
	
	protected void PostTextViewValueMessage(TextView TW, String Message) {
		MessageHandler.obtainMessage(MESSAGE_TEXTVIEW_WRITEVALUE,new TTextViewValueString(TW, Message)).sendToTarget();
	}
	
	protected void PostViewShow(View view) {
		MessageHandler.obtainMessage(MESSAGE_VIEW_SHOW, view).sendToTarget();
	}
	
	protected void PostViewGone(View view) {
		MessageHandler.obtainMessage(MESSAGE_VIEW_GONE, view).sendToTarget();
	}
	
	private static final int DoOnGPSFixDataType_SkipCounter = 8;
	//.
	private int 		DoOnGPSFixDataType_SkipCount = DoOnGPSFixDataType_SkipCounter;
	private TXYCoord 	DoOnGPSFixDataType_LastLocationXY = null;
	
	private void DoOnGPSFixDataType(TGPSFixDataType GPSFixDataType) {
		if (LinkedReflectorID != 0) {
			TReflector Reflector = TReflector.GetReflector(LinkedReflectorID);
			if ((Reflector != null) && (Reflector.Component != null) && !Reflector.Component.IsNavigating())
				try {
					boolean flAccept = true;
					if (Reflector.Component.IsUpdatingSpaceImage()) {
						DoOnGPSFixDataType_SkipCount--;
						if (DoOnGPSFixDataType_SkipCount == 0) 
							DoOnGPSFixDataType_SkipCount = DoOnGPSFixDataType_SkipCounter;
						else
							flAccept = false;
					}
					else
						DoOnGPSFixDataType_SkipCount = DoOnGPSFixDataType_SkipCounter;
					//.
					if (flAccept) {
						TTimestampedInt166DoubleContainerType.TValue Fix = (TTimestampedInt166DoubleContainerType.TValue)GPSFixDataType.ContainerValue();
						//.
						TXYCoord LocationXY = Reflector.Component.ConvertGeoCoordinatesToXY(Fix.Value, Fix.Value1,Fix.Value2,Fix.Value3);
						//.
						flAccept = ((DoOnGPSFixDataType_LastLocationXY == null) || !LocationXY.IsTheSame(DoOnGPSFixDataType_LastLocationXY));
						//.
						if (flAccept) {
							TReflectorTrack CurrentTrack = Reflector.Component.CurrentTrack_Get();
							if (CurrentTrack == null) 
								CurrentTrack = Reflector.Component.CurrentTrack_Begin();
							//.
							CurrentTrack.Nodes_Add(LocationXY);
							//.
							Reflector.Component.MoveReflectionWindow(LocationXY);
						}
					}
				} catch (Exception E) {
				}
		}
	}
}
