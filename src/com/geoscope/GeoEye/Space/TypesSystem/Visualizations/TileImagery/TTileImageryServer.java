package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import android.content.Context;

import com.geoscope.GeoEye.Space.Defines.TGeoScopeSpaceDataServer;

public class TTileImageryServer extends TGeoScopeSpaceDataServer {

	public TTileImageryServer(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword) {
		super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword);
	}
}
