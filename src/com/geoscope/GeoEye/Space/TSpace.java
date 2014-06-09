package com.geoscope.GeoEye.Space;

import android.content.Context;

import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TSpace {

	public static TSpace Space = null;
	
	public static void InitializeSpace(Context pcontext) throws Exception {
		FinalizeSpace();
		//.
		Space = new TSpace();
		Space.Start(pcontext);
	}
	
	public static void FinalizeSpace() throws Exception {
		if (Space != null) {
			Space.Destroy();
			Space = null;
		}
	}
	
	public static final String ContextFolder = TGeoLogApplication.ProfileFolder()+"/"+"CONTEXT"+"/"+"Space";

	public TSpaceContext Context;
	//.
	public TTypesSystem TypesSystem;
	
	public TSpace() throws Exception {
		Context = new TSpaceContext(this);
		//.
		TypesSystem = new TTypesSystem(this);
	}
	
	public void Destroy() throws Exception {
		Stop();
		//.
		if (TypesSystem != null) {
			TypesSystem.Destroy();
			TypesSystem = null;
		}
	}
	
	public void Start(Context pcontext) {
		TypesSystem.Start(pcontext);
	}
	
	public void Stop() {
		TypesSystem.Stop();
	}
}
