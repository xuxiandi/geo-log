package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor;

import android.app.Activity;
import android.widget.LinearLayout;

import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.ASTLR.TASTLRMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.ECTLR.TECTLRMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.VideoRecorderModule.TVideoRecorderServerMyPlayerComponent;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurement;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurement.TSensorMeasurementDescriptor;

public class TMeasurementProcessor extends TUIComponent {

	public static TMeasurementProcessor GetProcessor(TSensorMeasurementDescriptor MeasurementDescriptor) throws Exception {
		if (MeasurementDescriptor.IsTypeOf(TVideoRecorderServerMyPlayerComponent.TypeID))
			return new TVideoRecorderServerMyPlayerComponent(); //. ->
		if (MeasurementDescriptor.IsTypeOf(TECTLRMeasurementProcessor.TypeID))
			return new TECTLRMeasurementProcessor(); //. ->
		if (MeasurementDescriptor.IsTypeOf(TASTLRMeasurementProcessor.TypeID))
			return new TASTLRMeasurementProcessor(); //. ->
		else
			return null; //. ->
	}

	
	public TSensorMeasurement 	Measurement = null;
	//.
	protected Activity 		ParentActivity;
	protected LinearLayout 	ParentLayout;
	//.
	public boolean flInitialized = false;
	
	public TMeasurementProcessor() {
	}

	public void Destroy() throws Exception {
		Finalize();
	}

	@Override
	public boolean IsVisible() {
		return true;
	}
	
	public void SetLayout(Activity pParentActivity, LinearLayout pParentLayout) {
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
	}
	
	public void Initialize(TSensorMeasurement pMeasurement) throws Exception {
		Measurement = pMeasurement;
	}
	
	public void Finalize() throws Exception {
		flInitialized = false;
		//.
		Measurement = null;
	}
	
	public void Setup(TSensorMeasurement pMeasurement) throws Exception {
		Finalize();
		Initialize(pMeasurement);
	}
	
	public void SetPosition(final double Position, final int Delay, final boolean flPaused) throws InterruptedException {
	}
	
	public double GetPosition() {
		return 0.0;
	}
}
