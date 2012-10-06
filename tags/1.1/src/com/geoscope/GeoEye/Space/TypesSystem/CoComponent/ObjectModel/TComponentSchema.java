package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel;

import com.geoscope.GeoLog.COMPONENT.TComponent;

public class TComponentSchema {
	
	public TObjectModel ObjectModel;
	public TComponent RootComponent = null;

	public TComponentSchema(TObjectModel pObjectModel) {
		ObjectModel = pObjectModel;
	}
	
	public void Destroy() {
	}		
}
