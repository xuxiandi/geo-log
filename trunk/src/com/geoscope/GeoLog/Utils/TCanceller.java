package com.geoscope.GeoLog.Utils;

public class TCanceller {
	
	public boolean flCancel = false;
	
	public void Cancel() {
		flCancel = true;
	}
	
	public void Reset() {
		flCancel = false;
	}
}
