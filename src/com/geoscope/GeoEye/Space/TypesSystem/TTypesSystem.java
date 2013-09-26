package com.geoscope.GeoEye.Space.TypesSystem;

import java.util.ArrayList;

import android.content.Context;

import com.geoscope.GeoEye.Space.TSpace;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.TSystemTDATAFile;
import com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject.TSystemTGeographServerObject;
import com.geoscope.GeoEye.Space.TypesSystem.HINTVisualization.TSystemTHintVisualization;
import com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization.TSystemTTileServerVisualization;
import com.geoscope.GeoLog.Utils.TCancelableThread;

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
	public TSystemTTileServerVisualization	SystemTTileServerVisualization;
	public TSystemTHintVisualization		SystemTHintVisualization;
	public TSystemTDATAFile 				SystemTDATAFile;
	public TSystemTGeographServerObject 	SystemTGeographServerObject;
	//.
	private TContextClearing ContextClearing = null;
	
	public TTypesSystem(TSpace pSpace) {
		Space = pSpace;
		//. type systems
		SystemTTileServerVisualization	= new TSystemTTileServerVisualization(this);
		SystemTHintVisualization		= new TSystemTHintVisualization(this);
		SystemTDATAFile 				= new TSystemTDATAFile(this);
		SystemTGeographServerObject 	= new TSystemTGeographServerObject(this);
		//.
		TypesSystem = this;
	}
	
	public void Destroy() {
		TypesSystem = null;
		//.
		Stop();
		//.
		Clear();
	}
	
	public void Clear() {
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
		SystemTDATAFile.Context_Clear();
		SystemTGeographServerObject.Context_Clear();
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
