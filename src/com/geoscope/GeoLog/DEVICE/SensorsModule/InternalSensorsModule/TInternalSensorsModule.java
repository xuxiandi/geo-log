package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("HandlerLeak")
public class TInternalSensorsModule extends TModule {

	public static final int MESSAGE_START 	= 1;
	public static final int MESSAGE_STOP 	= 2;
	
	private class TBatterySensor {
		
		private BroadcastReceiver BatteryEventReceiver = null;
		
		public TBatterySensor() {
			BatteryEventReceiver = new BroadcastReceiver() {
				@Override
                public void onReceive(Context context, Intent intent) {
					//. voltage
					int Voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1);
					//. temperature
					int Temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1);
					//. Level
                    int Rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int Scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    int Level = ((Rawlevel*100)/Scale);
                    //. Health
                    int Health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1);
                    //. Status
                    int Status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                    //. PlugType
                    int PlugType = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                    //.
    				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel DC = GetDestinationADSChannel();
    				if (DC != null) {
    					try {
    						DC.DoOnBatteryVoltage(Voltage);
    					} catch (IOException E) {
    					}
    					//.
    					try {
    						DC.DoOnBatteryTemperature(Temperature);
    					} catch (IOException E) {
    					}
    					//.
        				try {
        					DC.DoOnBatteryLevel(Level);
        				} catch (IOException E) {
        				}
        				//.
            			try {
            				DC.DoOnBatteryHealth(Health);
            			} catch (IOException E) {
            			}
            			//.
                		try {
                			DC.DoOnBatteryStatus(Status);
                		} catch (IOException E) {
                		}
                		//.
                    	try {
                    		DC.DoOnBatteryPlugType(PlugType);
                    	} catch (IOException E) {
                    	}
    				}
                }
            };
            IntentFilter batteryEventFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Device.context.getApplicationContext().registerReceiver(BatteryEventReceiver, batteryEventFilter);
		}
		
		public void Destroy() {
	    	if (BatteryEventReceiver != null) {
	            Device.context.getApplicationContext().unregisterReceiver(BatteryEventReceiver);
	            BatteryEventReceiver = null;
	    	}
		}
	}
	
	private class TCellularConnectorSensor {
		
	    private class TConnectorStateListener extends PhoneStateListener {
	    	@Override
	        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
	    		super.onSignalStrengthsChanged(signalStrength);
	    		//. signal level
	    		int SignalStrength = signalStrength.getGsmSignalStrength();
	    		//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel DC = GetDestinationADSChannel();
				if (DC != null)
					try {
						DC.DoOnCellularConnectorSignalStrength(SignalStrength);
					} catch (IOException E) {
					}
	        }
	    }
	    
		private TelephonyManager _TelephonyManager = null;
	    private TConnectorStateListener ConnectorStateListener = null;
		
		public TCellularConnectorSensor() {
            ConnectorStateListener = new TConnectorStateListener();
            _TelephonyManager = (TelephonyManager)Device.context.getSystemService(Context.TELEPHONY_SERVICE);
            _TelephonyManager.listen(ConnectorStateListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		}
		
		public void Destroy() {
	    	if (_TelephonyManager != null) { 
	    		_TelephonyManager.listen(ConnectorStateListener,PhoneStateListener.LISTEN_NONE);
	    		_TelephonyManager = null;
	    	}
		}
	}
	
	private class TTemperatureSensor implements SensorEventListener {
		
		public float LastValue = Float.MIN_VALUE;
		
		public TTemperatureSensor() {
			Sensors.registerListener(this, Sensors.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE), SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public void Destroy() {
			Sensors.unregisterListener(this);
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			float Value = event.values[0];
			if (Value != LastValue) {
				LastValue = Value;
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
				if (DC != null)
					try {
						DC.DoOnTemperature(Value);
					} catch (IOException E) {
					}
			}
		}
	}
	
	private class TPressureSensor implements SensorEventListener {
		
		public float LastValue = Float.MIN_VALUE;
		
		public TPressureSensor() {
			Sensors.registerListener(this, Sensors.getDefaultSensor(Sensor.TYPE_PRESSURE), SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public void Destroy() {
			Sensors.unregisterListener(this);
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			float Value = event.values[0];
			if (Value != LastValue) {
				LastValue = Value;
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
				if (DC != null)
					try {
						DC.DoOnPressure(Value);
					} catch (IOException E) {
					}
			}
		}
	}
	
	private class TRelativeHumiditySensor implements SensorEventListener {
		
		public float LastValue = Float.MIN_VALUE;
		
		public TRelativeHumiditySensor() {
			Sensors.registerListener(this, Sensors.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY), SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public void Destroy() {
			Sensors.unregisterListener(this);
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			float Value = event.values[0];
			if (Value != LastValue) {
				LastValue = Value;
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
				if (DC != null)
					try {
						DC.DoOnRelativeHumidity(Value);
					} catch (IOException E) {
					}
			}
		}
	}
	
	private class TLightSensor implements SensorEventListener {
		
		public float LastValue = Float.MIN_VALUE;
		
		public TLightSensor() {
			Sensors.registerListener(this, Sensors.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public void Destroy() {
			Sensors.unregisterListener(this);
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			float Value = event.values[0];
			if (Value != LastValue) {
				LastValue = Value;
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
				if (DC != null)
					try {
						DC.DoOnLight(Value);
					} catch (IOException E) {
					}
			}
		}
	}
	
	private class TAccelerationSensor implements SensorEventListener {
		
		public float LastValue = Float.MIN_VALUE;
		
		public TAccelerationSensor() {
			Sensors.registerListener(this, Sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public void Destroy() {
			Sensors.unregisterListener(this);
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
            float[] v  = event.values;
        	float Value = (float)Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
			if (Value != LastValue) {
				LastValue = Value;
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
				if (DC != null)
					try {
						DC.DoOnAcceleration(Value);
					} catch (IOException E) {
					}
			}
		}
	}
	
	private class TMagneticFieldSensor implements SensorEventListener {
		
		public float LastValue  = Float.MIN_VALUE;
		public float LastValue1 = Float.MIN_VALUE;
		public float LastValue2 = Float.MIN_VALUE;
		
		public TMagneticFieldSensor() {
			Sensors.registerListener(this, Sensors.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public void Destroy() {
			Sensors.unregisterListener(this);
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
        	float Value = event.values[0];
        	float Value1 = event.values[1];
        	float Value2 = event.values[2];
			if (!((Value == LastValue) && (Value1 == LastValue1) && (Value2 == LastValue2))) {
				LastValue = Value;
				LastValue1 = Value1;
				LastValue2 = Value2;
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
				if (DC != null)
					try {
						DC.DoOnMagneticField(Value,Value1,Value2);
					} catch (IOException E) {
					}
			}
		}
	}
	
	private class TGyroscopeSensor implements SensorEventListener {
		
		public float LastValue  = Float.MIN_VALUE;
		public float LastValue1 = Float.MIN_VALUE;
		public float LastValue2 = Float.MIN_VALUE;
		
		public TGyroscopeSensor() {
			Sensors.registerListener(this, Sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_NORMAL);
		}
		
		public void Destroy() {
			Sensors.unregisterListener(this);
		}

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
        	float Value = event.values[0];
        	float Value1 = event.values[1];
        	float Value2 = event.values[2];
			if (!((Value == LastValue) && (Value1 == LastValue1) && (Value2 == LastValue2))) {
				LastValue = Value;
				LastValue1 = Value1;
				LastValue2 = Value2;
				//.
				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
				if (DC != null)
					try {
						DC.DoOnGyroscope(Value,Value1,Value2);
					} catch (IOException E) {
					}
			}
		}
	}
	
	private boolean flStartOnDeviceStart = false;
	//.
	public boolean flInitialized = false;
	//.
	private SensorManager Sensors;
	//.
	private TBatterySensor 				BatterySensor;
	private TCellularConnectorSensor	CellularConnectorSensor;	
	//.
	private TTemperatureSensor 			TemperatureSensor;
	private TPressureSensor				PressureSensor;
	private TRelativeHumiditySensor 	RelativeHumiditySensor;
	private TLightSensor				LightSensor;
	private TAccelerationSensor			AccelerationSensor;
	private TMagneticFieldSensor		MagneticFieldSensor;
	private TGyroscopeSensor			GyroscopeSensor;
	//.
	public TModel Model = null;
	private TADSChannel ADSChannel = null;
	private TXENVCChannel XENVCChannel = null;
	
    public TInternalSensorsModule(TSensorsModule pSensorsModule) throws Exception {
    	super(pSensorsModule);
    	//.
        Device = pSensorsModule.Device;
        //.
        Sensors = (SensorManager)Device.context.getSystemService(Context.SENSOR_SERVICE);
        //.
        BuildModel();
    }
    
    public void Destroy() {
    	Finalize();
    }

	private int StartCount = 0;
    
    @Override
    public void Start() throws Exception {
    	if (StartCount == 0) {
            if (IsEnabled() && ((Device.ModuleState == MODULE_STATE_RUNNING) || flStartOnDeviceStart)) {
                super.Start();
            	Initialize();
            	//.
            	StartCount++;
            }
    	}
    	else
        	StartCount++;
    }
    
    @Override
    public void Stop() throws Exception {
    	if (StartCount > 0) {
    		if (StartCount == 1) {
    	    	Finalize();
    	    	//.
    	    	super.Stop();
    		}
    		StartCount--;
    	}
    }
    
    public boolean IsStarted() {
    	return (StartCount > 0);
    }
    
    public void PostStart() {
		MessageHandler.obtainMessage(MESSAGE_START).sendToTarget();
    }
    
    public void PostStop() {
		MessageHandler.obtainMessage(MESSAGE_STOP).sendToTarget();
    }
    
    private void Initialize() {
    	BatterySensor				= new TBatterySensor();
    	CellularConnectorSensor		= new TCellularConnectorSensor();
    	//.
        TemperatureSensor 			= new TTemperatureSensor();
        PressureSensor 				= new TPressureSensor();
        RelativeHumiditySensor		= new TRelativeHumiditySensor();
        LightSensor					= new TLightSensor();
        AccelerationSensor			= new TAccelerationSensor();
        MagneticFieldSensor 		= new TMagneticFieldSensor();
        GyroscopeSensor				= new TGyroscopeSensor();
        //.
        flInitialized = true;
    }
    
    private void Finalize() {
    	flInitialized = false;
    	//.
    	if (GyroscopeSensor != null) {
    		GyroscopeSensor.Destroy();
    		GyroscopeSensor = null;
    	}
    	if (MagneticFieldSensor != null) {
    		MagneticFieldSensor.Destroy();
    		MagneticFieldSensor = null;
    	}
    	if (AccelerationSensor != null) {
    		AccelerationSensor.Destroy();
    		AccelerationSensor = null;
    	}
    	if (LightSensor != null) {
    		LightSensor.Destroy();
    		LightSensor = null;
    	}
    	if (RelativeHumiditySensor != null) {
    		RelativeHumiditySensor.Destroy();
    		RelativeHumiditySensor = null;
    	}
    	if (PressureSensor != null) {
    		PressureSensor.Destroy();
    		PressureSensor = null;
    	}
    	if (TemperatureSensor != null) {
    		TemperatureSensor.Destroy();
    		TemperatureSensor = null;
    	}
    	if (CellularConnectorSensor != null) {
    		CellularConnectorSensor.Destroy();
    		CellularConnectorSensor = null;
    	}
    	if (BatterySensor != null) {
    		BatterySensor.Destroy();
    		BatterySensor = null;
    	}
    }
    
    private void BuildModel() {
    	Model = new TModel();
    	//.
		ADSChannel = new TADSChannel(this); 
		ADSChannel.ID = TChannel.GetNextID();
		ADSChannel.Enabled = true;
		ADSChannel.Kind = TChannel.CHANNEL_KIND_OUT;
		ADSChannel.DataFormat = 0;
		ADSChannel.Name = "Android device state";
		ADSChannel.Info = "battery state, cellular phone state and other";
		ADSChannel.Size = 0;
		ADSChannel.Configuration = "";
		ADSChannel.Parameters = "";
		//.
		Model.Stream.Channels.add(ADSChannel);
    	//.
		XENVCChannel = new TXENVCChannel(this); 
		XENVCChannel.ID = TChannel.GetNextID();
		XENVCChannel.Enabled = true;
		XENVCChannel.Kind = TChannel.CHANNEL_KIND_OUT;
		XENVCChannel.DataFormat = 0;
		XENVCChannel.Name = "Environment conditions (Extended)";
		XENVCChannel.Info = "temperature, pressure, relative humidity, light etc";
		XENVCChannel.Size = 0;
		XENVCChannel.Configuration = "";
		XENVCChannel.Parameters = "";
		//.
		Model.Stream.Channels.add(XENVCChannel);
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel GetDestinationADSChannel() {
		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel)ADSChannel.DestinationChannel;
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel GetDestinationXENVCChannel() {
		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel)XENVCChannel.DestinationChannel;
    }
    
	public final Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_START: 
                	try {
            			Start(); 
                	}
                	catch (Exception E) {
                		Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_STOP: 
                	try {
            			Stop(); 
                	}
                	catch (Exception E) {
                		Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
