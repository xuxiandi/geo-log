package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor;

import android.app.Activity;
import android.widget.FrameLayout;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.ASTLR.TASTLRMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.ECTLR.TECTLRMeasurementProcessor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;

public class TMeasurementProcessor {

	public static TMeasurementProcessor GetProcessor(TSensorMeasurementDescriptor MeasurementDescriptor) {
		if (MeasurementDescriptor.IsTypeOf(TECTLRMeasurementProcessor.TypeID))
			return new TECTLRMeasurementProcessor(); //. ->
		if (MeasurementDescriptor.IsTypeOf(TASTLRMeasurementProcessor.TypeID))
			return new TASTLRMeasurementProcessor(); //. ->
		else
			return null; //. ->
	}

	
	protected TSensorMeasurement 	Measurement = null;
	protected double 				MeasurementStartPosition = 0.0;
	//.
	protected Activity 		ParentActivity;
	protected FrameLayout 	ParentLayout;
	//.
	public boolean flInitialized = false;
	
	public TMeasurementProcessor() {
	}

	public void Destroy() throws Exception {
	}
	
	public void SetLayout(Activity pParentActivity, FrameLayout pParentLayout) {
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
	}
	
	public void Initialize(TSensorMeasurement pMeasurement, double pMeasurementStartPosition) throws Exception {
		Measurement = pMeasurement;
		MeasurementStartPosition = pMeasurementStartPosition;
	}
	
	public void Finalize() throws Exception {
	}
	
	public void Setup(TSensorMeasurement pMeasurement, double pMeasurementStartPosition) throws Exception {
		Finalize();
		Initialize(pMeasurement, pMeasurementStartPosition);
	}
	
	public void SetPosition(final double Position, final int Delay, final boolean flPaused) throws InterruptedException {
	}
	
	public double GetPosition() {
		return 0.0;
	}
}
