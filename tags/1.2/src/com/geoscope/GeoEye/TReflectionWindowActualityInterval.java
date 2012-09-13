package com.geoscope.GeoEye;

import com.geoscope.GeoLog.Utils.OleDate;

public class TReflectionWindowActualityInterval {
	
	public static double NullTimestamp = 0.0;
	public static double MaxTimestamp = Double.MAX_VALUE;
	
	public double BeginTimestamp = NullTimestamp;
	public double EndTimestamp = MaxTimestamp;
	
	public TReflectionWindowActualityInterval() {
	}
	
	public TReflectionWindowActualityInterval(TReflectionWindowActualityInterval pInterval) {
		BeginTimestamp = pInterval.BeginTimestamp;
		EndTimestamp = pInterval.EndTimestamp;
	}
	
	public double GetBeginTimestamp() {
		if (BeginTimestamp == NullTimestamp) 
			return OleDate.ToUTCCurrentTime().toDouble(); //. ->
		else
			return BeginTimestamp;
	}
	
	public void Set(double pBeginTimestamp, double pEndTimestamp) {
		BeginTimestamp = pBeginTimestamp;
		EndTimestamp = pEndTimestamp;
	}

	public void Reset() {
		BeginTimestamp = NullTimestamp;
		EndTimestamp = MaxTimestamp;
	}
}
