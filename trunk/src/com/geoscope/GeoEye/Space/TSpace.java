package com.geoscope.GeoEye.Space;

import android.content.Context;

import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;

public class TSpace {

	public static TSpace Space = null;
	
	public static void InitializeSpace(Context pcontext) {
		FinalizeSpace();
		//.
		Space = new TSpace();
		Space.Start(pcontext);
	}
	
	public static void FinalizeSpace() {
		if (Space != null) {
			Space.Destroy();
			Space = null;
		}
	}
	
	public static final String ContextFolder = TReflector.ProfileFolder+"/"+"CONTEXT"+"/"+"Space";

	public TSpaceContext Context;
	//.
	public TTypesSystem TypesSystem;
	
	public TSpace() {
		Context = new TSpaceContext(this);
		//.
		TypesSystem = new TTypesSystem(this);
	}
	
	public void Destroy() {
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
