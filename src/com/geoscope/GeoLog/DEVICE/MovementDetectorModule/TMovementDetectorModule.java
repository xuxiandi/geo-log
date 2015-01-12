package com.geoscope.GeoLog.DEVICE.MovementDetectorModule;

import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

import com.geoscope.Classes.IO.Log.TRollingLogFile;
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
	
	public static class THittingDetector implements SensorEventListener {
		
		public static final int 	SamplesSize = 20;
		//.
		public static final int 	Samples_CheckHit_LeftSlopeInterval = 3;
		public static final int 	Samples_CheckHit_SlopesOverlapping = 1;
		public static final int 	Samples_CheckHit_RightSlopeInterval = 2;
		public static final int 	Samples_CheckHit_Interval = (Samples_CheckHit_LeftSlopeInterval+Samples_CheckHit_RightSlopeInterval);
		public static final double 	Samples_CheckHit_Threshold = -30.00;
		public static final int 	Samples_CheckHit_SkipTimeForNextHit = 100; //. ms
		public static final int 	Samples_CheckHit_HittingMaxInterval = 1500; //. ms
		
		public static class TDoOnHitHandler {
			
			public void DoOnHit() {
			}

			public void DoOnDoubleHit() {
			}

			public void DoOn3Hit() {
			}
		}
		
		public static class TSamplesSlope {
			
			private int 	N;
		    private double 	SumI;
		    private double 	SumQdI;
		    private double 	SumY;
		    private double 	SumIYi;
		    //.
		    public double A;
		    public double B;
		    
		    public TSamplesSlope(int pN) {
		    	N = pN;
		    	//.
		    	Reset();
		    }
		    
		    public void Reset() {
		    	SumI = 0.0;
		    	SumQdI = 0.0;
		    	SumY = 0.0;
		    	SumIYi = 0.0;
		    }
		    
		    public void AddSample(double X, double Y) {
		    	SumI = SumI+X;
		    	SumQdI = SumQdI+X*X;
		    	SumY = SumY+Y;
		    	SumIYi = SumIYi+X*Y;
		    }

		    public void RemoveSample(double X, double Y) {
		    	SumI = SumI-X;
		    	SumQdI = SumQdI-X*X;
		    	SumY = SumY-Y;
		    	SumIYi = SumIYi-X*Y;
		    }

		    public void Process() {
		    	A = (SumI*SumY-N*SumIYi)/(SumI*SumI-N*SumQdI);
		    	B = (SumY-SumI*A)/N;
		    }
		}
		
		private Context context;
		//.
		private SensorManager Sensors;
		//.
		private TDoOnHitHandler DoOnHitHandler;
		//.
	    private double[] 	Samples = new double[SamplesSize];
	    private int			SamplesCount = 0;
	    private int			SamplesPosition = 0;
	    private int			SamplesIndex = 0;
	    //.
    	private TSamplesSlope 	Samples_CheckHit_LeftSlope;
    	private TSamplesSlope 	Samples_CheckHit_RightSlope;
    	private double 			Samples_CheckHit_CurrentMinFactor = 0.0;
    	private long			Samples_CheckHit_NextCheckTimestamp = 0;
    	private int				Samples_CheckHit_HitCount = 0;
    	private long			Samples_CheckHit_HitTimestamp = 0;
    	@SuppressWarnings("unused")
		private TRollingLogFile	Samples_Log = null;
    	
		
		public THittingDetector(Context pcontext, TDoOnHitHandler pDoOnHitHandler) {
			context = pcontext;
			DoOnHitHandler = pDoOnHitHandler;
			//.
			/* test: try {
				Samples_Log = new TRollingLogFile(TGeoLogApplication.LogFolder+"/"+"HitDetector.log", 10000, 1000);
			} catch (IOException E) {
				Samples_Log = null;
			}*/
			//.
			Samples_CheckHit_LeftSlope = new TSamplesSlope(Samples_CheckHit_LeftSlopeInterval);
			Samples_CheckHit_RightSlope = new TSamplesSlope(Samples_CheckHit_RightSlopeInterval);
			//.
	        Sensors = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
	        //.
			Sensors.registerListener(this, Sensors.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
		}
		
		public void Destroy() {
			Sensors.unregisterListener(this);
			//.
			/* test: if (Samples_Log != null) 
				try {
					Samples_Log.Destroy();
					Samples_Log = null;
				} catch (IOException E) {
				}*/
		}

		private double Samples_CheckHit_GetFactor() {
			int Interval = (Samples_CheckHit_LeftSlopeInterval+Samples_CheckHit_RightSlopeInterval);
			//.
			int Index = SamplesIndex-Interval;
			//.
			int Pos = SamplesPosition-Interval;
			while (Pos < 0)
				Pos += SamplesSize;
			//. process left slope
			Samples_CheckHit_LeftSlope.Reset();
			for (int I = 0; I < Samples_CheckHit_LeftSlopeInterval; I++) {
				Samples_CheckHit_LeftSlope.AddSample(Index, Samples[Pos]);
				//.
				Index++;
				//.
				Pos++;
				if (Pos >= SamplesSize)
					Pos = 0;
			}
			Samples_CheckHit_LeftSlope.Process();
			//. adjust slops overlapping
			for (int I = 0; I < Samples_CheckHit_SlopesOverlapping; I++) {
				Index--;
				//.
				Pos--;
				if (Pos < 0)
					Pos = SamplesSize-1;
			}
			//. process right slope
			Samples_CheckHit_RightSlope.Reset();
			for (int I = 0; I < Samples_CheckHit_RightSlopeInterval; I++) {
				Samples_CheckHit_RightSlope.AddSample(Index, Samples[Pos]);
				//.
				Index++;
				//.
				Pos++;
				if (Pos >= SamplesSize)
					Pos = 0;
			}
			Samples_CheckHit_RightSlope.Process();
			//.
			if ((Samples_CheckHit_LeftSlope.A > 0.0) && (Samples_CheckHit_RightSlope.A < 0.0))
				return (Samples_CheckHit_LeftSlope.A*Samples_CheckHit_RightSlope.A); //. =>
			else 
				return 0.0; //. =>
		}
		
		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			try {
                float[] v  = event.values;
                double X2 = v[0]*v[0];
                double Y2 = v[1]*v[1];
                double Z2 = v[2]*v[2];
                double Value = X2+Y2+Z2;
            	Samples[SamplesPosition] = Value;
            	SamplesPosition++;
            	if (SamplesPosition >= Samples.length)
            		SamplesPosition = 0;
            	if (SamplesCount < Samples.length)
            		SamplesCount++;
            	else {
            		//. debug: double _Value = ((int)(Value*10.0))/10.0;
        			//. debug: Samples_Log.WriteInfo("HD", Double.toString(_Value)+"    ("+Long.toString(SampleInterval)+"ms)");
        			//.
            		long Timestamp = System.currentTimeMillis();
            		if (Timestamp >= Samples_CheckHit_NextCheckTimestamp) {
            			double HitFactor = Samples_CheckHit_GetFactor();
            			//. debug: Samples_Log.WriteInfo("HD", Double.toString(HitFactor));
            			if (HitFactor <= Samples_CheckHit_CurrentMinFactor) 
            				Samples_CheckHit_CurrentMinFactor = HitFactor; 
            			else {
            				if (Samples_CheckHit_CurrentMinFactor < Samples_CheckHit_Threshold) {
                    			Log.i("Gyroscope", "HitFactor: "+Double.toString(Samples_CheckHit_CurrentMinFactor));
                    			//.
                    			Samples_CheckHit_NextCheckTimestamp = (Timestamp+Samples_CheckHit_SkipTimeForNextHit); 
                    			//.
                    			Samples_CheckHit_HitCount++;
                    			Samples_CheckHit_HitTimestamp = Timestamp; 
            				}
            				Samples_CheckHit_CurrentMinFactor = 0.0; //. reset the hit recognition
            			}
            		} 
                	//. process hit(s)
                	if ((Samples_CheckHit_HitCount > 0) && ((Timestamp-Samples_CheckHit_HitTimestamp) > Samples_CheckHit_HittingMaxInterval)) {
                		switch (Samples_CheckHit_HitCount) {
                		
                		case 1:
                			DoOnHitHandler.DoOnHit();
                			break; //. >
                			
                		case 2:
            				DoOnHitHandler.DoOnDoubleHit();
                			break; //. >
                			
                		case 3:
            				DoOnHitHandler.DoOn3Hit();
                			break; //. >
                		}
            			Samples_CheckHit_HitCount = 0;
                	}
            	}
            	SamplesIndex++;
			}
			catch (Throwable TE) {
				String S = TE.getMessage();
				if (S == null)
					S = TE.getClass().getName();
				Toast.makeText(context, S,	Toast.LENGTH_LONG).show();
			}
		}
	}
	
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
            	Accelerometer_LastActivityTime = System.currentTimeMillis();
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
            	OrientationDetector_LastActivityTime = System.currentTimeMillis();
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
    
    public TMovementDetectorModule(TDEVICEModule pDevice) throws Exception
    {
    	super(pDevice);
    	flEnabled = false;
    	//.
        Device = pDevice;
        //.
        Accelerometer_flPresent = false;
        OrientationDetector_flPresent = false;
        sensors = null;
    }
    
    public void Destroy() throws Exception
    {
    	Stop();
    }
    
    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
        if (IsEnabled()) {
            sensors = (SensorManager)Device.context.getSystemService(Context.SENSOR_SERVICE);
            if (sensors != null) {
                List<Sensor> Accelerometers = sensors.getSensorList(Sensor.TYPE_ACCELEROMETER);
                if(Accelerometers.size() > 0) {
                	Accelerometer = Accelerometers.get(0);
                	sensors.registerListener(Accelerometer_Listener, Accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                	Accelerometer_flPresent = true;
                }
                @SuppressWarnings("deprecation")
				List<Sensor> OrientationDetectors = sensors.getSensorList(Sensor.TYPE_ORIENTATION);
                if(OrientationDetectors.size() > 0) {
                	OrientationDetector = OrientationDetectors.get(0);
                	sensors.registerListener(OrientationDetector_Listener, OrientationDetector, SensorManager.SENSOR_DELAY_UI);
                	OrientationDetector_flPresent = true;
                }
            }
        }
    }

    @Override
    public void Stop() throws Exception {
    	if (sensors != null) {
    		sensors.unregisterListener(Accelerometer_Listener);
    		sensors.unregisterListener(OrientationDetector_Listener);
    		sensors = null;
    	}
    	//.
    	super.Stop();
    }
    
    public boolean IsPresent() {
    	return (Accelerometer_flPresent || OrientationDetector_flPresent);
    }
    
    public boolean IsActive(int Interval) {
    	return (Accelerometer_IsActive(Interval) || OrientationDetector_IsActive(Interval));
    }
    
    private synchronized boolean Accelerometer_IsActive(int Interval) {
    	return ((System.currentTimeMillis()-Accelerometer_LastActivityTime) <= Interval);
    }
    
    private synchronized boolean OrientationDetector_IsActive(int Interval) {
    	return ((System.currentTimeMillis()-OrientationDetector_LastActivityTime) <= Interval);
    }
    
    public synchronized boolean IsMovementDetected(int Interval) {
    	return ((OrientationDetector_flPresent && OrientationDetector_IsMovementDetected(Interval)) || (Accelerometer_flPresent && Accelerometer_IsMovementDetected(Interval)));
    }
    
    public synchronized long Accelerometer_GetMovementDetectedTime() {
    	return Accelerometer_MovementDetectedTime;
    }

    public synchronized boolean Accelerometer_IsMovementDetected(int Interval) {
    	return ((System.currentTimeMillis()-Accelerometer_MovementDetectedTime) <= Interval);
    }

    public synchronized long OrientationDetector_GetMovementDetectedTime() {
    	return OrientationDetector_MovementDetectedTime;
    }
    
    public synchronized boolean OrientationDetector_IsMovementDetected(int Interval) {
    	return ((System.currentTimeMillis()-OrientationDetector_MovementDetectedTime) <= Interval);
    }
}
