package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.widget.LinearLayout;

import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Audio.TAudioMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.AOSS.TAOSSMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.ASTLR.TASTLRMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.ECTLR.TECTLRMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.GPSTLR.TGPSTLRMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Telemetry.TRC.TTRCMeasurementProcessor;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessors.Video.TVideoMeasurementProcessor;
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
		if (MeasurementDescriptor.IsTypeOf(TAOSSMeasurementProcessor.TypeID))
			return new TAOSSMeasurementProcessor(); //. ->
		if (MeasurementDescriptor.IsTypeOf(TTRCMeasurementProcessor.TypeID))
			return new TTRCMeasurementProcessor(); //. ->
		if (MeasurementDescriptor.IsTypeOf(TGPSTLRMeasurementProcessor.TypeID))
			return new TGPSTLRMeasurementProcessor(); //. ->
		if (MeasurementDescriptor.IsTypeOf(TAudioMeasurementProcessor.TypeID))
			return new TAudioMeasurementProcessor(); //. ->
		if (MeasurementDescriptor.IsTypeOf(TVideoMeasurementProcessor.TypeID))
			return new TVideoMeasurementProcessor(); //. ->
		else
			return null; //. ->
	}

	public static class TOnSurfaceChangedHandler {
		
		public void DoOnSurfaceChanged(SurfaceHolder surface) {
		}
	}
	
	public static class TOnProgressHandler {
		
		public void DoOnProgress(double ProgressFactor) {
		}
	}
	
	
	protected boolean flExists = false;
	//.
	public TSensorMeasurement 	Measurement = null;
	//.
	protected Activity 		ParentActivity;
	protected LinearLayout 	ParentLayout;
	//.
	public TOnSurfaceChangedHandler OnVideoSurfaceChangedHandler;
	//.
	public TOnProgressHandler OnProgressHandler;
    //.
    public boolean flStandalone = false;
    //.
	protected int Width;
	protected int Height;
	//.
	protected SurfaceHolder surface = null;
	//.
	protected boolean flInitialized = false;
	//.
	private boolean flSetup = false;
	
	public TMeasurementProcessor() {
    	flExists = true;
	}

	@Override
	public void Destroy() throws Exception {
    	flExists = false;
    	//.
		Finalize();
		//.
		super.Destroy();
	}

	public String GetTypeID() {
		return "";
	}
	
	public void SetLayout(Activity pParentActivity, LinearLayout pParentLayout) throws Exception {
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
	}
	
	protected void Initialize(TSensorMeasurement pMeasurement) throws Exception {
		Measurement = pMeasurement;
	}
	
	protected void Finalize() throws Exception {
		flInitialized = false;
	}
	
	public void Setup(TSensorMeasurement pMeasurement) throws Exception {
		Finalize();
		Initialize(pMeasurement);
		//.
		flSetup = true;
	}
	
	public void Reset() throws Exception {
		flSetup = false;
		//.
		Finalize();
	}
	
	public boolean IsSetup() {
		return flSetup;
	}
	
	public void SetPosition(final double Position, final int Delay, final boolean flPaused) throws InterruptedException {
	}
	
	public double GetPosition() {
		return 0.0;
	}
}
