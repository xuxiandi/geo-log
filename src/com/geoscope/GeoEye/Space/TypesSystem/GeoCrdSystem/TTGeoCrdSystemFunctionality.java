package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem;

import java.util.ArrayList;

import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTGeoCrdSystemFunctionality extends TTypeFunctionality {

	public TTGeoCrdSystemFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	public long GetInstanceByXYLocally(int pGeoSpaceID, double pX, double pY) {
		TSystemTGeoCrdSystem.TThisContextCache Cache = (TSystemTGeoCrdSystem.TThisContextCache)TypeSystem.ContextCache;
		ArrayList<TGeoCrdSystemData> IL = Cache.GetItemsByGeoSpaceID(pGeoSpaceID);
		if (IL == null)
			return 0; //. ->
		return IL.get(0).ID;
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TGeoCrdSystemFunctionality(this,idComponent));
	}
}
