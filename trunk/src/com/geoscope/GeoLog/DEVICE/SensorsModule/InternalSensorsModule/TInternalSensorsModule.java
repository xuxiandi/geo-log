package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("HandlerLeak")
public class TInternalSensorsModule extends TModule {

	public static final int MESSAGE_START 	= 1;
	public static final int MESSAGE_STOP 	= 2;
	
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
	
	private boolean flInitializeAtDeviceStart = false;
	//.
	private boolean flInitialized = false;
	//.
	private SensorManager Sensors;
	//.
	private TTemperatureSensor 		TemperatureSensor;
	private TPressureSensor			PressureSensor;
	private TRelativeHumiditySensor RelativeHumiditySensor;
	private TLightSensor			LightSensor;
	private TAccelerationSensor		AccelerationSensor;
	private TMagneticFieldSensor	MagneticFieldSensor;
	private TGyroscopeSensor		GyroscopeSensor;
	//.
	public TModel Model = null;
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
    
    @Override
    public void Start() throws Exception {
        super.Start();
        //.
        if (IsEnabled() && ((Device.ModuleState == MODULE_STATE_RUNNING) || flInitializeAtDeviceStart))
        	Initialize();
    }
    
    @Override
    public void Stop() throws Exception {
    	Finalize();
    	//.
    	super.Stop();
    }
    
    public boolean IsStarted() {
    	return flInitialized;
    }
    
    public void PostStart() {
		MessageHandler.obtainMessage(MESSAGE_START).sendToTarget();
    }
    
    public void PostStop() {
		MessageHandler.obtainMessage(MESSAGE_STOP).sendToTarget();
    }
    
    private void Initialize() {
        TemperatureSensor 		= new TTemperatureSensor();
        PressureSensor 			= new TPressureSensor();
        RelativeHumiditySensor	= new TRelativeHumiditySensor();
        LightSensor				= new TLightSensor();
        AccelerationSensor		= new TAccelerationSensor();
        MagneticFieldSensor 	= new TMagneticFieldSensor();
        GyroscopeSensor			= new TGyroscopeSensor();
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
    }
    
    private void BuildModel() {
    	Model = new TModel();
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
                		if (!IsStarted())
                			Start(); 
                	}
                	catch (Exception E) {
                		Toast.makeText(Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_STOP: 
                	try {
                		if (IsStarted())
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
