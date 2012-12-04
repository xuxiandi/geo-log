package com.geoscope.GeoEye;


public class TReflectionWindowActualityInterval {
	
	public static final double NullTimestamp = 0.0;
	public static final double MaxTimestamp = Double.MAX_VALUE;
	//.
	public static final double MinRealTimestamp = 40000.0;
	public static final double MaxRealTimestamp = 1000000.0;
	
	public double BeginTimestamp = NullTimestamp;
	public double EndTimestamp = MaxTimestamp;
	
	public TReflectionWindowActualityInterval() {
	}
	
	public TReflectionWindowActualityInterval(double pBeginTimestamp, double pEndTimestamp) {
		BeginTimestamp = pBeginTimestamp;
		EndTimestamp = pEndTimestamp;
	}
	
	public TReflectionWindowActualityInterval(TReflectionWindowActualityInterval pInterval) {
		BeginTimestamp = pInterval.BeginTimestamp;
		EndTimestamp = pInterval.EndTimestamp;
	}
	
	public double GetBeginTimestamp() {
		if (BeginTimestamp == NullTimestamp) 
			return MinRealTimestamp; //. ->
		else
			return BeginTimestamp;
	}
	
	public double GetEndTimestamp() {
		if (EndTimestamp == MaxTimestamp) 
			return MaxRealTimestamp; //. ->
		else
			return EndTimestamp;
	}
	
	public void Set(double pBeginTimestamp, double pEndTimestamp) {
		BeginTimestamp = pBeginTimestamp;
		EndTimestamp = pEndTimestamp;
	}

	public void Reset() {
		BeginTimestamp = NullTimestamp;
		EndTimestamp = MaxTimestamp;
	}
	
	public boolean IsInfinite() {
		return ((BeginTimestamp == NullTimestamp) && (EndTimestamp == MaxTimestamp));
	}
}
