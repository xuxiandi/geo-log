package com.geoscope.GeoEye.Space.Functionality;

public class TFunctionality {
	
	private int RefCount = 0;
	
	public boolean flUpdated = false;
	
	public void Destroy() {
	}
	
	public int AddRef() {
		RefCount++;
		return RefCount;
	}
	
	public int Release() {
		RefCount--;
		if (RefCount <= 0)
			Destroy();
		return RefCount;
	}
}
