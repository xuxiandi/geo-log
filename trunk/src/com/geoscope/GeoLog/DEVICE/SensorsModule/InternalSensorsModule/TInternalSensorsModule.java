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
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.DataTypes.T3DoubleDataType;
import com.geoscope.Classes.Data.Stream.Channel.DataTypes.TDoubleDataType;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("HandlerLeak")
public class TInternalSensorsModule extends TModule {

	public static final int MESSAGE_START 	= 1;
	public static final int MESSAGE_STOP 	= 2;
	
	private class TBatterySensor {
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
		private BroadcastReceiver BatteryEventReceiver = null;
		private long LastProcessTime = 0;
		
		public TBatterySensor() {
			BatteryEventReceiver = new BroadcastReceiver() {
				@Override
                public void onReceive(Context context, Intent intent) {
					try {
						long NowTime = System.currentTimeMillis();
						if ((NowTime-LastProcessTime) < ProcessTimeInterval)
							return; //. ->
						LastProcessTime = NowTime;
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
					catch (Throwable TE) {
						String S = TE.getMessage();
						if (S == null)
							S = TE.getClass().getName();
			    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
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
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
	    private class TConnectorStateListener extends PhoneStateListener {
	    	@Override
	        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
				try {
		    		super.onSignalStrengthsChanged(signalStrength);
		    		//.
					long NowTime = System.currentTimeMillis();
					if ((NowTime-LastProcessTime) < ProcessTimeInterval)
						return; //. ->
					LastProcessTime = NowTime;
		    		//. signal level
		    		int SignalStrength = signalStrength.getGsmSignalStrength();
		    		if ((0 <= SignalStrength) && (SignalStrength <= 31)) {
						com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel DC = GetDestinationADSChannel();
						if (DC != null)
							try {
								DC.DoOnCellularConnectorSignalStrength(SignalStrength);
							} catch (IOException E) {
							}
		    		}
				}
				catch (Throwable TE) {
					String S = TE.getMessage();
					if (S == null)
						S = TE.getClass().getName();
		    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
				}
	        }
	    }
	    
		private TelephonyManager _TelephonyManager = null;
	    private TConnectorStateListener ConnectorStateListener = null;
		private long LastProcessTime = 0;
		
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
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
		public float LastValue = Float.MIN_VALUE;
		private long LastProcessTime = 0;
		
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
			try {
				long NowTime = System.currentTimeMillis();
				if ((NowTime-LastProcessTime) < ProcessTimeInterval)
					return; //. ->
				LastProcessTime = NowTime;
				//.
				float Value = event.values[0];
				if (Value != LastValue) {
					LastValue = Value;
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = TLRChannel.DataTypes.GetItemByIndex(TLRChannel_Temperature_Index);
							Double DV = Double.valueOf(Value);
							DataType.SetValue(DV);
							TDC.DoOnData(DataType);
						} catch (IOException E) {
						}
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
					if (DC != null)
						try {
							DC.DoOnTemperature(Value);
						} catch (IOException E) {
						}
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
			}
		}
	}
	
	private class TPressureSensor implements SensorEventListener {
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
		public float LastValue = Float.MIN_VALUE;
		private long LastProcessTime = 0;
		
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
			try {
				long NowTime = System.currentTimeMillis();
				if ((NowTime-LastProcessTime) < ProcessTimeInterval)
					return; //. ->
				LastProcessTime = NowTime;
				//.
				float Value = event.values[0];
				if (Value != LastValue) {
					LastValue = Value;
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = TLRChannel.DataTypes.GetItemByIndex(TLRChannel_Pressure_Index);
							Double DV = Double.valueOf(Value);
							DataType.SetValue(DV);
							TDC.DoOnData(DataType);
						} catch (IOException E) {
						}
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
					if (DC != null)
						try {
							DC.DoOnPressure(Value);
						} catch (IOException E) {
						}
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
			}
		}
	}
	
	private class TRelativeHumiditySensor implements SensorEventListener {
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
		public float LastValue = Float.MIN_VALUE;
		private long LastProcessTime = 0;
		
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
			try {
				long NowTime = System.currentTimeMillis();
				if ((NowTime-LastProcessTime) < ProcessTimeInterval)
					return; //. ->
				LastProcessTime = NowTime;
				//.
				float Value = event.values[0];
				if (Value != LastValue) {
					LastValue = Value;
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = TLRChannel.DataTypes.GetItemByIndex(TLRChannel_RelativeHumidity_Index);
							Double DV = Double.valueOf(Value);
							DataType.SetValue(DV);
							TDC.DoOnData(DataType);
						} catch (IOException E) {
						}
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
					if (DC != null)
						try {
							DC.DoOnRelativeHumidity(Value);
						} catch (IOException E) {
						}
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
			}
		}
	}
	
	private class TLightSensor implements SensorEventListener {
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
		public float LastValue = Float.MIN_VALUE;
		private long LastProcessTime = 0;
		
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
			try {
				long NowTime = System.currentTimeMillis();
				if ((NowTime-LastProcessTime) < ProcessTimeInterval)
					return; //. ->
				LastProcessTime = NowTime;
				//.
				float Value = event.values[0];
				if (Value != LastValue) {
					LastValue = Value;
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = TLRChannel.DataTypes.GetItemByIndex(TLRChannel_LightSensor_Index);
							Double DV = Double.valueOf(Value);
							DataType.SetValue(DV);
							TDC.DoOnData(DataType);
						} catch (IOException E) {
						}
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
					if (DC != null)
						try {
							DC.DoOnLight(Value);
						} catch (IOException E) {
						}
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
			}
		}
	}
	
	private class TAccelerationSensor implements SensorEventListener {
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
		public float LastValue = Float.MIN_VALUE;
		private long LastProcessTime = 0;
		
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
			try {
				long NowTime = System.currentTimeMillis();
				if ((NowTime-LastProcessTime) < ProcessTimeInterval)
					return; //. ->
				LastProcessTime = NowTime;
				//.
	            float[] v  = event.values;
	        	float Value = (float)Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);
				if (Value != LastValue) {
					LastValue = Value;
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = TLRChannel.DataTypes.GetItemByIndex(TLRChannel_Acceleration_Index);
							Double DV = Double.valueOf(Value);
							DataType.SetValue(DV);
							TDC.DoOnData(DataType);
						} catch (IOException E) {
						}
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
					if (DC != null)
						try {
							DC.DoOnAcceleration(Value);
						} catch (IOException E) {
						}
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
			}
		}
	}
	
	private class TMagneticFieldSensor implements SensorEventListener {
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
		public float LastValue  = Float.MIN_VALUE;
		public float LastValue1 = Float.MIN_VALUE;
		public float LastValue2 = Float.MIN_VALUE;
		//.
		private long LastProcessTime = 0;
		
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
			try {
				long NowTime = System.currentTimeMillis();
				if ((NowTime-LastProcessTime) < ProcessTimeInterval)
					return; //. ->
				LastProcessTime = NowTime;
				//.
	        	float Value = event.values[0];
	        	float Value1 = event.values[1];
	        	float Value2 = event.values[2];
				if (!((Value == LastValue) && (Value1 == LastValue1) && (Value2 == LastValue2))) {
					LastValue = Value;
					LastValue1 = Value1;
					LastValue2 = Value2;
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = TLRChannel.DataTypes.GetItemByIndex(TLRChannel_MagneticField_Index);
							T3DoubleDataType.TValue V = new T3DoubleDataType.TValue(Value,Value1,Value2);
							DataType.SetValue(V);
							TDC.DoOnData(DataType);
						} catch (IOException E) {
						}
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
					if (DC != null)
						try {
							DC.DoOnMagneticField(Value,Value1,Value2);
						} catch (IOException E) {
						}
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
			}
		}
	}
	
	private class TGyroscopeSensor implements SensorEventListener {
		
		private static final int ProcessTimeInterval = 1000*1; //. seconds
		
		public float LastValue  = Float.MIN_VALUE;
		public float LastValue1 = Float.MIN_VALUE;
		public float LastValue2 = Float.MIN_VALUE;
		//.
		private long LastProcessTime = 0;
		
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
			try {
				long NowTime = System.currentTimeMillis();
				if ((NowTime-LastProcessTime) < ProcessTimeInterval)
					return; //. ->
				LastProcessTime = NowTime;
				//.
	        	float Value = event.values[0];
	        	float Value1 = event.values[1];
	        	float Value2 = event.values[2];
				if (!((Value == LastValue) && (Value1 == LastValue1) && (Value2 == LastValue2))) {
					LastValue = Value;
					LastValue1 = Value1;
					LastValue2 = Value2;
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = TLRChannel.DataTypes.GetItemByIndex(TLRChannel_Gyroscope_Index);
							T3DoubleDataType.TValue V = new T3DoubleDataType.TValue(Value,Value1,Value2);
							DataType.SetValue(V);
							TDC.DoOnData(DataType);
						} catch (IOException E) {
						}
					//.
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel DC = GetDestinationXENVCChannel();
					if (DC != null)
						try {
							DC.DoOnGyroscope(Value,Value1,Value2);
						} catch (IOException E) {
						}
				}
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
	    		Device.Log.WriteError("InternalSensorsModule.Sensor.DoOnReceive()",S);
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
	//.
	private TADSChannel ADSChannel = null;
	private TXENVCChannel XENVCChannel = null;
	//.
	private boolean 	TLRChannel_flUse = false;
	private TTLRChannel TLRChannel = null;
	private int			TLRChannel_Temperature_Index = -1;
	private int			TLRChannel_Pressure_Index = -1;
	private int			TLRChannel_RelativeHumidity_Index = -1;
	private int			TLRChannel_LightSensor_Index = -1;
	private int			TLRChannel_Acceleration_Index = -1;
	private int			TLRChannel_MagneticField_Index = -1;
	private int			TLRChannel_Gyroscope_Index = -1;
	
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
		if (TLRChannel_flUse) {
			TLRChannel = new TTLRChannel(this); 
			TLRChannel.ID = TChannel.GetNextID();
			TLRChannel.Enabled = true;
			TLRChannel.Kind = TChannel.CHANNEL_KIND_OUT;
			TLRChannel.DataFormat = 0;
			TLRChannel.Name = "Device telemetry";
			TLRChannel.Info = "parameters";
			TLRChannel.Size = 0;
			TLRChannel.Configuration = "";
			TLRChannel.Parameters = "";
			TLRChannel.DataTypes = new TDataTypes();
			TLRChannel.DataTypes.AddItem(new TDoubleDataType("Temperature", 1, "","", "C")); 		TLRChannel_Temperature_Index 		= 0;
			TLRChannel.DataTypes.AddItem(new TDoubleDataType("Pressure", 2, "","", "mBar")); 		TLRChannel_Pressure_Index 			= 1;
			TLRChannel.DataTypes.AddItem(new TDoubleDataType("RelativeHumidity", 3, "","", "%")); 	TLRChannel_RelativeHumidity_Index 	= 2;
			TLRChannel.DataTypes.AddItem(new TDoubleDataType("LightSensor", 4, "","", "lx")); 		TLRChannel_LightSensor_Index 		= 3;
			TLRChannel.DataTypes.AddItem(new TDoubleDataType("Acceleration", 5, "","", "m/s^2")); 	TLRChannel_Acceleration_Index 		= 4;
			TLRChannel.DataTypes.AddItem(new T3DoubleDataType("MagneticField", 6, "","", "mT")); 	TLRChannel_MagneticField_Index 		= 5;
			TLRChannel.DataTypes.AddItem(new T3DoubleDataType("Gyroscope", 7, "","", "rad/s")); 	TLRChannel_Gyroscope_Index 			= 6;		
			//.
			Model.Stream.Channels.add(TLRChannel);
		}
		else {
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
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel GetDestinationADSChannel() {
		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel)ADSChannel.DestinationChannel;
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel GetDestinationXENVCChannel() {
    	if (XENVCChannel != null)
    		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel)XENVCChannel.DestinationChannel; //. ->
    	else
    		return null;  //. -> 
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel GetDestinationTLRChannel() {
    	if (TLRChannel != null)
    		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel)TLRChannel.DestinationChannel; //. ->
    	else
    		return null;  //. -> 
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
