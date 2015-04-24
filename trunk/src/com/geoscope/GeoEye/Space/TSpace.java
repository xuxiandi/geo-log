package com.geoscope.GeoEye.Space;

import android.content.Context;

import com.geoscope.GeoEye.Space.Defines.TSpaceObj;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TSpace {

	private static TSpace Space = null;
	
	public static synchronized TSpace InitializeSpace(TGeoScopeServerUser pUser, Context pcontext) throws Exception {
		FinalizeSpace();
		//.
		Space = new TSpace(pUser);
		Space.Start(pcontext);
		//.
		return Space;
	}
	
	public static synchronized void FinalizeSpace() throws Exception {
		if (Space != null) {
			Space.Destroy();
			Space = null;
		}
	}
	
	public static synchronized TSpace GetSpace() {
		return Space;
	}
	
	public static final String ContextFolder = TGeoLogApplication.ProfileFolder()+"/"+"CONTEXT"+"/"+"Space";

	public TSpaceContext Context;
	//.
	public TGeoScopeServerUser User;
	//.
	public TTypesSystem TypesSystem;

	public TSpace(TGeoScopeServerUser pUser) throws Exception {
		User = pUser;
		//.
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
	
	public void Objects_Set(TSpaceObj Obj) {
		Context.SpaceObjects_Set(Obj);
	}

	public void Objects_Remove(TSpaceObj Obj) {
		Context.SpaceObjects_Remove(Obj);
	}

	public TSpaceObj Objects_Get(long Ptr) {
		return Context.SpaceObjects_Get(Ptr);
	}
	
	public TSpaceObj Objects_Get(int idTVisualization, long idVisualization) {
		return Context.SpaceObjects_Get(idTVisualization,idVisualization);
	}
}
