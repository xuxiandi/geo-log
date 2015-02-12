package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import android.content.Context;

import com.geoscope.GeoEye.Space.TypesSystem.GeographServer.TGeographServerClient;

public class TGeographServerObjectController extends TGeographServerClient {

    public TGeographServerObjectController(Context pcontext, String pServerAddress, int pServerPort, long pUserID, String pUserPassword, int pidGeoGraphServerObject, int pObjectID) {
    	super(pcontext, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeoGraphServerObject, pObjectID);
    }
}
