package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule;

import java.io.IOException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.TModel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.EnvironmentConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TInternalSensorsModule extends TModule {

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
	
	private SensorManager Sensors;
	//.
	private TTemperatureSensor 		TemperatureSensor;
	private TPressureSensor			PressureSensor;
	private TRelativeHumiditySensor RelativeHumiditySensor;
	private TLightSensor			LightSensor;
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
        TemperatureSensor 		= new TTemperatureSensor();
        PressureSensor 			= new TPressureSensor();
        RelativeHumiditySensor	= new TRelativeHumiditySensor();
        LightSensor				= new TLightSensor();
        //.
        BuildModel();
    }
    
    public void Destroy() {
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
		XENVCChannel = new TXENVCChannel(); 
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
}
