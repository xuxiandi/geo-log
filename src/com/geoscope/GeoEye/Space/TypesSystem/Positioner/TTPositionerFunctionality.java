package com.geoscope.GeoEye.Space.TypesSystem.Positioner;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTPositionerFunctionality extends TTypeFunctionality {
	
	public TTPositionerFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TPositionerFunctionality(this,idComponent));
	}

	@Override
	public int GetImageResID() {
		return R.drawable.user_activity_component_list_placeholder_component_positioner;
	};
}
