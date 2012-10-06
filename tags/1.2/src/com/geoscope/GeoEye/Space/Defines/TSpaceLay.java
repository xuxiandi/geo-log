package com.geoscope.GeoEye.Space.Defines;

public class TSpaceLay {

	public int ID;
	public boolean flEnabled;
	public String Name = "";
	
	public TSpaceLay(int pID, String pName) {
		ID = pID;
		Name = pName;
		flEnabled = true;
	}
}
