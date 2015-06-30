package com.geoscope.GeoEye.Space.TypesSystem.CoComponent;

import java.io.File;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSystemTCoComponent extends TTypeSystem {

	public static final String 	FolderName = "CoComponent";
	
	public static String 		Folder() {
		return TTypesSystem.Folder()+"/"+FolderName;
	}
		
	public static String ContextFolder = TTypesSystem.ContextFolder+"/"+FolderName;
	
	public TSystemTCoComponent(TTypesSystem pTypesSystem) throws Exception {
		super(pTypesSystem,SpaceDefines.idTCoComponent,SpaceDefines.nmTCoComponent);
	}

	@Override
	public TTypeFunctionality TTypeFunctionality_Create() {
		return (new TTCoComponentFunctionality(this)); 
	}
	
	@Override
	public String Context_GetFolder() {
		File CF = new File(ContextFolder);
		CF.mkdirs();
		return ContextFolder;
	}	
}
