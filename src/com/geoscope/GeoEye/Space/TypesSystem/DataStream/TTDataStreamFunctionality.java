package com.geoscope.GeoEye.Space.TypesSystem.DataStream;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;

public class TTDataStreamFunctionality extends TTypeFunctionality {

	public TTDataStreamFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TDataStreamFunctionality(this,idComponent));
	}

	@Override
	public int GetImageResID() {
		return R.drawable.user_activity_component_list_placeholder_component_datastream;
	};
}
