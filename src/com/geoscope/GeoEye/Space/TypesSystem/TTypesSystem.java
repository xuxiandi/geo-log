package com.geoscope.GeoEye.Space.TypesSystem;

import java.util.ArrayList;

import android.content.Context;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.TSpace;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.TSystemTCoComponent;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.TSystemTDATAFile;
import com.geoscope.GeoEye.Space.TypesSystem.DetailedPictureVisualization.TSystemTDetailedPictureVisualization;
import com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.TSystemTGeoCrdSystem;
import com.geoscope.GeoEye.Space.TypesSystem.GeoSpace.TSystemTGeoSpace;
import com.geoscope.GeoEye.Space.TypesSystem.GeodesyPoint.TSystemTGeodesyPointSystem;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TSystemTGeographServer;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TSystemTGeographServerObject;
import com.geoscope.GeoEye.Space.TypesSystem.HINTVisualization.TSystemTHintVisualization;
import com.geoscope.GeoEye.Space.TypesSystem.Positioner.TSystemTPositioner;
import com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization.TSystemTTileServerVisualization;

public class TTypesSystem {

	public static final String ContextFolder = TSpace.ContextFolder+"/"+"TypesSystem";
	
	public static TTypesSystem TypesSystem = null;
	
    private class TContextClearing extends TCancelableThread {

    	public TContextClearing() {
    		_Thread = new Thread(this);
			_Thread.setPriority(Thread.MIN_PRIORITY);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				Context_ClearOldItems();
        	}
        	catch (Throwable E) {
        	}
		}
    }
	
    public TSpace Space;
    //.
    public Context context;
    //.
	public ArrayList<TTypeSystem> Items = new ArrayList<TTypeSystem>();
	//. type systems
	public TSystemTDetailedPictureVisualization	SystemTDetailedPictureVisualization;
	public TSystemTTileServerVisualization		SystemTTileServerVisualization;
	public TSystemTHintVisualization			SystemTHintVisualization;
	public TSystemTCoComponent					SystemTCoComponent;
	public TSystemTDATAFile 					SystemTDATAFile;
	public TSystemTPositioner					SystemTPositioner;
	public TSystemTGeographServer 				SystemTGeographServer;
	public TSystemTGeographServerObject 		SystemTGeographServerObject;
	public TSystemTGeoSpace						SystemTGeoSpace;
	public TSystemTGeoCrdSystem					SystemTGeoCrdSystem;
	public TSystemTGeodesyPointSystem			SystemTGeodesyPointSystem;
	//.
	private TContextClearing ContextClearing = null;
	
	public TTypesSystem(TSpace pSpace) throws Exception {
		Space = pSpace;
		//. type systems
		SystemTDetailedPictureVisualization = new TSystemTDetailedPictureVisualization(this); 
		SystemTTileServerVisualization		= new TSystemTTileServerVisualization(this);
		SystemTHintVisualization			= new TSystemTHintVisualization(this);
		SystemTCoComponent					= new TSystemTCoComponent(this);
		SystemTDATAFile 					= new TSystemTDATAFile(this);
		SystemTPositioner 					= new TSystemTPositioner(this);
		SystemTGeographServer 				= new TSystemTGeographServer(this);
		SystemTGeographServerObject 		= new TSystemTGeographServerObject(this);
		SystemTGeoSpace						= new TSystemTGeoSpace(this);
		SystemTGeoCrdSystem					= new TSystemTGeoCrdSystem(this);
		SystemTGeodesyPointSystem			= new TSystemTGeodesyPointSystem(this);
		//.
		TypesSystem = this;
	}
	
	public void Destroy() throws Exception {
		TypesSystem = null;
		//.
		Stop();
		//.
		Clear();
	}
	
	public void Clear() throws Exception {
		while (Items.size() > 0)
			Items.get(0).Destroy();
	}
	
	public void Start(Context pcontext) {
		context = pcontext;
		//.
		if (!Space.Context.Storage.CheckDeviceFillFactorForRemovingOldItems())
			ContextClearing = new TContextClearing();
	}
	
	public void Stop() {
		if (ContextClearing != null) {
			ContextClearing.Cancel();
			ContextClearing = null;
		}
	}
	
	public void Context_Clear() {
		for (int I = 0; I < Items.size(); I++) {
			TTypeSystem TS = Items.get(I);
			if (!((TS instanceof TSystemTDetailedPictureVisualization) || 
				  (TS instanceof TSystemTTileServerVisualization) || 
				  (TS instanceof TSystemTHintVisualization) ||
				  (TS instanceof TSystemTGeoSpace) || 
				  (TS instanceof TSystemTGeoCrdSystem) 
				  ))
				TS.Context_Clear();
		}
	}
	
	public void Context_ClearItems(long ToTime) {
		for (int I = 0; I < Items.size(); I++)
			Items.get(I).Context_ClearItems(ToTime);
	}	

	public void Context_ClearOldItems() {
		for (int I = 0; I < Items.size(); I++)
			Items.get(I).Context_ClearOldItems();
	}	
}
