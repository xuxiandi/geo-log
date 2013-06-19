package com.geoscope.GeoEye.Space.TypesSystem;

import java.io.File;

import com.geoscope.Utils.TFileSystem;

public class TTypeSystem {

	public void Destroy() {
	}
	
	public String GetContextFolder() {
		return "";
	}
	
	public void ClearContext() {
		TFileSystem.EmptyFolder(new File(GetContextFolder()));
	}
}
