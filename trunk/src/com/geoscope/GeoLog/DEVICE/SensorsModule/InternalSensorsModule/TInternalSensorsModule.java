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
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.T3DoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TDoubleContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TInt16ContainerType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.Battery.TBatteryHealthDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.Battery.TBatteryPlugTypeDataType;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.DataTypes.Battery.TBatteryStatusDataType;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.GeoLocation.GPS.TGPSChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.UserMessagingModule.TUserMessagingModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("HandlerLeak")
public class TInternalSensorsModule extends TModule {

	public static String Folder() {
		return TSensorsModule.Folder()+"/"+"InternalSensorsModule";
	}
		
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
	    				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationASTLRChannel();
	    				if (TDC != null) {
	    					try {
								TDataType DataType = ASTLRChannel_BatteryVoltage;
								Double V = Double.valueOf(Voltage/1000.0);
								DataType.SetContainerTypeValue(V);
								TDC.DoOnData(DataType);
	    					} catch (IOException E) {
	    					}
	    					//.
	    					try {
								TDataType DataType = ASTLRChannel_BatteryTemperature;
								Double V = Double.valueOf(Temperature/10.0);
								DataType.SetContainerTypeValue(V);
								TDC.DoOnData(DataType);
	    					} catch (IOException E) {
	    					}
	    					//.
	    					try {
								TDataType DataType = ASTLRChannel_BatteryLevel;
								Double V = Double.valueOf(Level);
								DataType.SetContainerTypeValue(V);
								TDC.DoOnData(DataType);
	    					} catch (IOException E) {
	    					}
	    					//.
	    					try {
								TDataType DataType = ASTLRChannel_BatteryHealth;
								Short V = Short.valueOf((short)Health);
								DataType.SetContainerTypeValue(V);
								TDC.DoOnData(DataType);
	    					} catch (IOException E) {
	    					}
	    					//.
	    					try {
								TDataType DataType = ASTLRChannel_BatteryStatus;
								Short V = Short.valueOf((short)Status);
								DataType.SetContainerTypeValue(V);
								TDC.DoOnData(DataType);
	    					} catch (IOException E) {
	    					}
	    					//.
	    					try {
								TDataType DataType = ASTLRChannel_BatteryPlugType;
								Short V = Short.valueOf((short)PlugType);
								DataType.SetContainerTypeValue(V);
								TDC.DoOnData(DataType);
	    					} catch (IOException E) {
	    					}
	    				}
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
			    		Device.Log.WriteError("InternalSensorsModule.BatterySensor.DoOnReceive()",S);
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
	    				com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationASTLRChannel();
	    				if (TDC != null) 
	    					try {
								TDataType DataType = ASTLRChannel_CellularConnectorSignalStrength;
								Double V = Double.valueOf(SignalStrength);
								DataType.SetContainerTypeValue(V);
								TDC.DoOnData(DataType);
	    					} catch (IOException E) {
	    					}
	    				//.
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
		    		Device.Log.WriteError("InternalSensorsModule.CellularConnectorSensor.DoOnReceive()",S);
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
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationECTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = ECTLRChannel_Temperature;
							Double V = Double.valueOf(Value);
							DataType.SetContainerTypeValue(V);
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
	    		Device.Log.WriteError("InternalSensorsModule.TemperatureSensor.DoOnReceive()",S);
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
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationECTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = ECTLRChannel_Pressure;
							Double V = Double.valueOf(Value);
							DataType.SetContainerTypeValue(V);
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
	    		Device.Log.WriteError("InternalSensorsModule.PressureSensor.DoOnReceive()",S);
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
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationECTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = ECTLRChannel_RelativeHumidity;
							Double V = Double.valueOf(Value);
							DataType.SetContainerTypeValue(V);
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
	    		Device.Log.WriteError("InternalSensorsModule.RelativeHumiditySensor.DoOnReceive()",S);
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
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationECTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = ECTLRChannel_LightSensor;
							Double V = Double.valueOf(Value);
							DataType.SetContainerTypeValue(V);
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
	    		Device.Log.WriteError("InternalSensorsModule.LightSensor.DoOnReceive()",S);
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
				if (event.values.length != 3)
					return; //. ->
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
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationECTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = ECTLRChannel_Acceleration;
							Double V = Double.valueOf(Value);
							DataType.SetContainerTypeValue(V);
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
	    		Device.Log.WriteError("InternalSensorsModule.AccelerationSensor.DoOnReceive()",S);
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
				if (event.values.length != 3)
					return; //. ->
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
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationECTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = ECTLRChannel_MagneticField;
							T3DoubleContainerType.TValue V = new T3DoubleContainerType.TValue(Value,Value1,Value2);
							DataType.SetContainerTypeValue(V);
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
	    		Device.Log.WriteError("InternalSensorsModule.MagneticFieldSensor.DoOnReceive()",S);
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
				if (event.values.length != 3)
					return; //. ->
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
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel TDC = GetDestinationECTLRChannel();
					if (TDC != null)
						try {
							TDataType DataType = ECTLRChannel_Gyroscope;
							T3DoubleContainerType.TValue V = new T3DoubleContainerType.TValue(Value,Value1,Value2);
							DataType.SetContainerTypeValue(V);
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
	    		Device.Log.WriteError("InternalSensorsModule.GyroscopeSensor.DoOnReceive()",S);
			}
		}
	}
	
	public boolean flStartOnDeviceStart = false;
	//.
	public boolean flInitialized = false;
	//.
	public TUserMessagingModule UserMessagingModule;
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
	private boolean 	ASTLRChannel_flUse = true;
	private TTLRChannel ASTLRChannel = null;
	private TDataType 	ASTLRChannel_BatteryVoltage;
	private TDataType 	ASTLRChannel_BatteryTemperature;
	private TDataType 	ASTLRChannel_BatteryLevel;
	private TDataType 	ASTLRChannel_BatteryHealth;
	private TDataType 	ASTLRChannel_BatteryStatus;
	private TDataType 	ASTLRChannel_BatteryPlugType;
	private TDataType 	ASTLRChannel_CellularConnectorSignalStrength;
	//.
	private boolean 	ECTLRChannel_flUse = true;
	private TTLRChannel ECTLRChannel = null;
	private TDataType 	ECTLRChannel_Temperature;
	private TDataType 	ECTLRChannel_Pressure;
	private TDataType 	ECTLRChannel_RelativeHumidity;
	private TDataType 	ECTLRChannel_LightSensor;
	private TDataType 	ECTLRChannel_Acceleration;
	private TDataType 	ECTLRChannel_MagneticField;
	private TDataType 	ECTLRChannel_Gyroscope;
	//.
	private TGPSChannel GPSChannel = null;
	
    public TInternalSensorsModule(TSensorsModule pSensorsModule) throws Exception {
    	super(pSensorsModule);
    	//.
        Device = pSensorsModule.Device;
        //.
        UserMessagingModule = new TUserMessagingModule(this);
        //.
        Sensors = (SensorManager)Device.context.getSystemService(Context.SENSOR_SERVICE);
        //.
        Model_Build();
    }
    
    public void Destroy() {
    	Finalize();
    	//.
    	if (UserMessagingModule != null) {
    		UserMessagingModule.Destroy();
    		UserMessagingModule = null;
    	}
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
    
    private void Model_Build() {
    	Model = new TModel();
    	//.
    	if (ASTLRChannel_flUse) {
			ASTLRChannel = new TTLRChannel(this); 
			ASTLRChannel.ID = TChannel.GetNextID();
			ASTLRChannel.Enabled = true;
			ASTLRChannel.Kind = TChannel.CHANNEL_KIND_OUT;
			ASTLRChannel.DataFormat = 0;
			ASTLRChannel.Name = "Android state";
			ASTLRChannel.Info = "battery, cellular phone";
			ASTLRChannel.Size = 0;
			ASTLRChannel.Configuration = "";
			ASTLRChannel.Parameters = "";
			ASTLRChannel.DataTypes = new TDataTypes();
			ASTLRChannel_BatteryVoltage = 					ASTLRChannel.DataTypes.AddItem(new TDataType(new TDoubleContainerType(), 				"BatteryVoltage", 					ASTLRChannel, 1, "","", "V")); 	
			ASTLRChannel_BatteryTemperature = 				ASTLRChannel.DataTypes.AddItem(new TDataType(new TDoubleContainerType(), 				"BatteryTemperature",				ASTLRChannel, 2, "","", "C")); 	
			ASTLRChannel_BatteryLevel = 					ASTLRChannel.DataTypes.AddItem(new TDataType(new TDoubleContainerType(), 				"BatteryLevel", 					ASTLRChannel, 3, "","", "%")); 	
			ASTLRChannel_BatteryHealth = 					ASTLRChannel.DataTypes.AddItem(new TBatteryHealthDataType(new TInt16ContainerType(), 										ASTLRChannel, 4, "","", ""));		
			ASTLRChannel_BatteryStatus = 					ASTLRChannel.DataTypes.AddItem(new TBatteryStatusDataType(new TInt16ContainerType(), 										ASTLRChannel, 5, "","", ""));		
			ASTLRChannel_BatteryPlugType = 					ASTLRChannel.DataTypes.AddItem(new TBatteryPlugTypeDataType(new TInt16ContainerType(), 										ASTLRChannel, 6, "","", ""));    	
			ASTLRChannel_CellularConnectorSignalStrength = 	ASTLRChannel.DataTypes.AddItem(new TDataType(new TDoubleContainerType(), 				"CellularConnectorSignalStrength",	ASTLRChannel, 7, "","", "%")); 	
    		//.
    		Model.Stream.Channels.add(ASTLRChannel);
    	}
    	else {
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
    	}
    	//.
		if (ECTLRChannel_flUse) {
			ECTLRChannel = new TTLRChannel(this); 
			ECTLRChannel.ID = TChannel.GetNextID();
			ECTLRChannel.Enabled = true;
			ECTLRChannel.Kind = TChannel.CHANNEL_KIND_OUT;
			ECTLRChannel.DataFormat = 0;
			ECTLRChannel.Name = "Device telemetry";
			ECTLRChannel.Info = "internal sensors";
			ECTLRChannel.Size = 0;
			ECTLRChannel.Configuration = "";
			ECTLRChannel.Parameters = "";
			ECTLRChannel.DataTypes = new TDataTypes();
			ECTLRChannel_Temperature = 		ECTLRChannel.DataTypes.AddItem(new TDataType(new TDoubleContainerType(), 	"Temperature", 			ECTLRChannel, 1, "","", "C")); 		
			ECTLRChannel_Pressure = 		ECTLRChannel.DataTypes.AddItem(new TDataType(new TDoubleContainerType(), 	"Pressure", 			ECTLRChannel, 2, "","", "mBar")); 	
			ECTLRChannel_RelativeHumidity = ECTLRChannel.DataTypes.AddItem(new TDataType(new TDoubleContainerType(), 	"RelativeHumidity", 	ECTLRChannel, 3, "","", "%"));
			//.
			TDataType LSDT = new TDataType(new TDoubleContainerType(), 													"LightSensor", 			ECTLRChannel, 4, "","", "lx");
			TDataType.TDataTrigger LSDT_DarknessTrigger = new TDataType.TDataTrigger("LightSensorDarkness","Default");
			LSDT.Triggers_Add(LSDT_DarknessTrigger);
			ECTLRChannel_LightSensor = 		ECTLRChannel.DataTypes.AddItem(LSDT);
			//.
			ECTLRChannel_Acceleration = 	ECTLRChannel.DataTypes.AddItem(new TDataType(new TDoubleContainerType(), 	"Acceleration", 		ECTLRChannel, 5, "","", "m/s^2")); 	
			ECTLRChannel_MagneticField = 	ECTLRChannel.DataTypes.AddItem(new TDataType(new T3DoubleContainerType(), 	"MagneticField", 		ECTLRChannel, 6, "","", "mT")); 		
			ECTLRChannel_Gyroscope = 		ECTLRChannel.DataTypes.AddItem(new TDataType(new T3DoubleContainerType(), 	"Gyroscope", 			ECTLRChannel, 7, "","", "rad/s")); 			
			//.
			Model.Stream.Channels.add(ECTLRChannel);
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
		//.
		GPSChannel = new TGPSChannel(this);
		Model.Stream.Channels.add(GPSChannel);
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel GetDestinationADSChannel() {
    	if (ADSChannel != null)
    		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.AndroidState.ADS.TADSChannel)ADSChannel.DestinationChannel_Get();
    	else
    		return null;  //. -> 
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel GetDestinationXENVCChannel() {
    	if (XENVCChannel != null)
    		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel)XENVCChannel.DestinationChannel_Get(); //. ->
    	else
    		return null;  //. -> 
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel GetDestinationASTLRChannel() {
    	if (ASTLRChannel != null)
    		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel)ASTLRChannel.DestinationChannel_Get(); //. ->
    	else
    		return null;  //. -> 
    }
    
    private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel GetDestinationECTLRChannel() {
    	if (ECTLRChannel != null)
    		return (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel)ECTLRChannel.DestinationChannel_Get(); //. ->
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
