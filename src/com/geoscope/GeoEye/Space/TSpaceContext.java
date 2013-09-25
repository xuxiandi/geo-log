package com.geoscope.GeoEye.Space;

public class TSpaceContext {

	public TSpace Space;
	//.
	public TSpaceContextStorage Storage;
	
	public TSpaceContext(TSpace pSpace) {
		Space = pSpace;
		//.
		Storage = new TSpaceContextStorage();
	}
}
