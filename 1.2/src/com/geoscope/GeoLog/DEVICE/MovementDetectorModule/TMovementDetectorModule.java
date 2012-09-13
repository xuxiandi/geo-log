package com.geoscope.GeoLog.DEVICE.MovementDetectorModule;

import java.util.Calendar;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TMovementDetectorModule extends TModule {

	public static final int 	Accelerometer_ProbeInterval = 1000*1/*seconds*/;
	public static final int 	Accelerometer_SamplesSize = 5;
	public static final double	Accelerometer_ThresholdFactor = 2.0;
	//.
	public static final int 	OrientationDetector_ProbeInterval = 1000*3/*seconds*/;
	public static final int 	OrientationDetector_SamplesSize = 20;
	public static final double	OrientationDetector_ThresholdFactor = 100.0;
	
	private double GetAvrDispersion(double[] Samples) {
		double Avr = 0.0;
		for (int I = 0; I < Samples.length; I++)
			Avr = Avr+Samples[I];
		Avr = Avr/Samples.length;
		double E = 0.0;
		for (int I = 0; I < Samples.length; I++)
			E = E+Math.pow((Samples[I]-Avr),2);
		return (E/Samples.length);
	}
	
    private boolean flEnabled = false; ///?
    private SensorManager sensors;
    //.
    private Sensor 				Accelerometer;
    private boolean 			Accelerometer_flPresent;
    private double[] 			Accelerometer_Samples = new double[Accelerometer_SamplesSize];
    private int					Accelerometer_SamplesCount = 0;
    private int					Accelerometer_SamplesPosition = 0;
    private long				Accelerometer_LastActivityTime = 0;
    private long				Accelerometer_MovementDetectedTime = 0;
    private SensorEventListener Accelerometer_Listener = new SensorEventListener() {
    	
    	public long Accelerometer_ProbeLastTime = 0;
    	
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { 
        }
        
        @Override
        public void onSensorChanged(SensorEvent event) {
        	synchronized (TMovementDetectorModule.this) {
            	Accelerometer_LastActivityTime = Calendar.getInstance().getTime().getTime();
            	if ((Accelerometer_LastActivityTime-Accelerometer_ProbeLastTime) < Accelerometer_ProbeInterval)
            		return; //. ->
            	//.
                float[] v  = event.values;
            	double F = Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]);
            	Accelerometer_Samples[Accelerometer_SamplesPosition] = F;
            	Accelerometer_SamplesPosition++;
            	if (Accelerometer_SamplesPosition >= Accelerometer_Samples.length)
            		Accelerometer_SamplesPosition = 0;
            	if (Accelerometer_SamplesCount < Accelerometer_Samples.length)
            		Accelerometer_SamplesCount++;
            	else {
            		double D = GetAvrDispersion(Accelerometer_Samples);
            		if (D >= Accelerometer_ThresholdFactor)
                        Accelerometer_MovementDetectedTime = Accelerometer_LastActivityTime; 
            	}
            	//.
            	Accelerometer_ProbeLastTime = Accelerometer_LastActivityTime;
        	}
        }
    };
    private Sensor 				OrientationDetector;
    public boolean 				OrientationDetector_flPresent;
    private double[] 			OrientationDetector_Samples = new double[OrientationDetector_SamplesSize];
    private int					OrientationDetector_SamplesCount = 0;
    private int					OrientationDetector_SamplesPosition = 0;
    private long				OrientationDetector_LastActivityTime = 0;
    private long				OrientationDetector_MovementDetectedTime = 0;
    private SensorEventListener OrientationDetector_Listener = new SensorEventListener() {
    	
    	public long 				OrientationDetector_ProbeLastTime = 0;
    	
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) { 
        }
        
        @Override
        public synchronized void onSensorChanged(SensorEvent event) {
        	synchronized (TMovementDetectorModule.this) {
            	OrientationDetector_LastActivityTime = Calendar.getInstance().getTime().getTime();
            	if ((OrientationDetector_LastActivityTime-OrientationDetector_ProbeLastTime) < OrientationDetector_ProbeInterval)
            		return; //. ->
            	//.
                double Azimuth = event.values[0];
            	OrientationDetector_Samples[OrientationDetector_SamplesPosition] = Azimuth;
            	OrientationDetector_SamplesPosition++;
            	if (OrientationDetector_SamplesPosition >= OrientationDetector_Samples.length)
            		OrientationDetector_SamplesPosition = 0;
            	if (OrientationDetector_SamplesCount < OrientationDetector_Samples.length)
            		OrientationDetector_SamplesCount++;
            	else {
            		double D = GetAvrDispersion(OrientationDetector_Samples);
            		if (D >= OrientationDetector_ThresholdFactor)
                        OrientationDetector_MovementDetectedTime = OrientationDetector_LastActivityTime; 
            	}
            	//.
            	OrientationDetector_ProbeLastTime = OrientationDetector_LastActivityTime;
        	}
        }
    };
    
    public TMovementDetectorModule(TDEVICEModule pDevice)
    {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        Accelerometer_flPresent = false;
        OrientationDetector_flPresent = false;
        sensors = null;
        if (flEnabled) {
            sensors = (SensorManager)Device.context.getSystemService(Context.SENSOR_SERVICE);
            if (sensors != null) {
                List<Sensor> Accelerometers = sensors.getSensorList(Sensor.TYPE_ACCELEROMETER);
                if(Accelerometers.size() > 0) {
                	Accelerometer = Accelerometers.get(0);
                	sensors.registerListener(Accelerometer_Listener, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                	Accelerometer_flPresent = true;
                }
                List<Sensor> OrientationDetectors = sensors.getSensorList(Sensor.TYPE_ORIENTATION);
                if(OrientationDetectors.size() > 0) {
                	OrientationDetector = OrientationDetectors.get(0);
                	sensors.registerListener(OrientationDetector_Listener, OrientationDetector, SensorManager.SENSOR_DELAY_UI);
                	OrientationDetector_flPresent = true;
                }
            }
        }
    }
    
    public void Destroy()
    {
    	if (sensors != null) {
    		sensors.unregisterListener(Accelerometer_Listener);
    		sensors.unregisterListener(OrientationDetector_Listener);
    	}
    }
    
    public boolean IsPresent() {
    	return (Accelerometer_flPresent || OrientationDetector_flPresent);
    }
    
    public boolean IsActive(int Interval) {
    	return (Accelerometer_IsActive(Interval) || OrientationDetector_IsActive(Interval));
    }
    
    private synchronized boolean Accelerometer_IsActive(int Interval) {
    	return ((Calendar.getInstance().getTime().getTime()-Accelerometer_LastActivityTime) <= Interval);
    }
    
    private synchronized boolean OrientationDetector_IsActive(int Interval) {
    	return ((Calendar.getInstance().getTime().getTime()-OrientationDetector_LastActivityTime) <= Interval);
    }
    
    public synchronized boolean IsMovementDetected(int Interval) {
    	return ((OrientationDetector_flPresent && OrientationDetector_IsMovementDetected(Interval)) || (Accelerometer_flPresent && Accelerometer_IsMovementDetected(Interval)));
    }
    
    public synchronized long Accelerometer_GetMovementDetectedTime() {
    	return Accelerometer_MovementDetectedTime;
    }

    public synchronized boolean Accelerometer_IsMovementDetected(int Interval) {
    	return ((Calendar.getInstance().getTime().getTime()-Accelerometer_MovementDetectedTime) <= Interval);
    }

    public synchronized long OrientationDetector_GetMovementDetectedTime() {
    	return OrientationDetector_MovementDetectedTime;
    }
    
    public synchronized boolean OrientationDetector_IsMovementDetected(int Interval) {
    	return ((Calendar.getInstance().getTime().getTime()-OrientationDetector_MovementDetectedTime) <= Interval);
    }
}
