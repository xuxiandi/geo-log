package com.geoscope.GeoEye.Space.TypesSystem;

import java.io.File;

import com.geoscope.Utils.TFileSystem;

public class TTypeSystem {

	public static final long Context_Item_DefaultLifeTime = (1000*3600*24)*30; //. days
	
	public TTypesSystem TypesSystem;
	
	public TTypeSystem(TTypesSystem pTypesSystem) {
		TypesSystem = pTypesSystem;
		//.
		TypesSystem.Items.add(this);
	}
	
	public void Destroy() {
		if (TypesSystem != null)
			TypesSystem.Items.remove(this);
	}
	
	public String Context_GetFolder() {
		return "";
	}
	
	public void Context_Clear() {
		TFileSystem.EmptyFolder(new File(Context_GetFolder()));
	}
	
	public void Context_ClearItems(long ToTime) {
		TFileSystem.RemoveFolderFiles(new File(Context_GetFolder()), ToTime,null);
	}	

	public void Context_ClearOldItems() {
		Context_ClearItems(Context_Item_DefaultLifeTime);
	}	
}
