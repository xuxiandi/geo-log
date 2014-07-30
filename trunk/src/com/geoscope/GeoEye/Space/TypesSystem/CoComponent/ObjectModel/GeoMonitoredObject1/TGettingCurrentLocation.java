package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TGeoMonitoredObject1DeviceSchema.TGeoMonitoredObject1DeviceComponent.TGPSModule.TGPSFixDataValue;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TDeviceConnectionRepeater;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TDeviceConnectionStartHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TDeviceConnectionStopHandler;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater.TLANConnectionExceptionHandler;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TGeographServerObjectController;

public class TGettingCurrentLocation extends TCancelableThread {

	private String 	GeographProxyServerAddress = "";
	private int 	GeographProxyServerPort = 0;
	private int		UserID;
	private String	UserPassword;
	//.
	private TReflectorCoGeoMonitorObject Object;
	
	public TGettingCurrentLocation(String pGeographProxyServerAddress, int pGeographProxyServerPort, int pUserID, String pUserPassword, TReflectorCoGeoMonitorObject pObject) {
		GeographProxyServerAddress = pGeographProxyServerAddress;
		GeographProxyServerPort = pGeographProxyServerPort;
		UserID = pUserID;
		UserPassword = pUserPassword;
		Object = pObject;
		//.
		_Thread = new Thread(this);
		_Thread.start();
	}
	
	@Override
	public void run() {
		TLANConnectionExceptionHandler ExceptionHandler = new TLANConnectionExceptionHandler() {
			@Override
			public void DoOnException(Throwable E) {
				this.DoOnException(E);
			}
		};		
		try {
			TGeoMonitoredObject1Model Model = new TGeoMonitoredObject1Model();
			TDeviceConnectionStartHandler StartHandler = Model.TDeviceConnectionStartHandler_Create(Object);
			TDeviceConnectionStopHandler StopHandler = Model.TDeviceConnectionStopHandler_Create(Object);
			//.
			final int LocalPort = 5001;
			//.
			TDeviceConnectionRepeater DeviceConnectionRepeater = new TDeviceConnectionRepeater("2:3&4", LocalPort, GeographProxyServerAddress,GeographProxyServerPort, UserID,UserPassword, Object.GeographServerObjectID(), ExceptionHandler, StartHandler,StopHandler);
			try {
				TGeographServerObjectController GSOC = new TGeographServerObjectController(Object.Server.context, 0, UserID,UserPassword, "127.0.0.1",DeviceConnectionRepeater.GetPort());
				TGeoMonitoredObject1Model GeoMonitoredObject1Model = new TGeoMonitoredObject1Model(GSOC,true);
				try {
					TGeoMonitoredObject1DeviceComponent DeviceRootComponent = (TGeoMonitoredObject1DeviceComponent)GeoMonitoredObject1Model.ObjectDeviceSchema.RootComponent;
					DeviceRootComponent.GPSModule.GPSFixData.ReadDeviceCUAC();
					TGPSFixDataValue Location = DeviceRootComponent.GPSModule.GPSFixData;
					//.
					DoOnLocation(Location.Latitude,Location.Longitude,Location.Altitude,Location.Precision);
				}
				finally {
					GeoMonitoredObject1Model.Destroy();
				}
			}
			finally {
				DeviceConnectionRepeater.Destroy();			
			}
		}
		catch (Throwable T) {
			ExceptionHandler.DoOnException(T);
		}
	}

	public void DoOnLocation(double Latitude, double Longitude, double Altitude, double Precision) {
	}
	
	public void DoOnException(Throwable E) {
	}	
}
