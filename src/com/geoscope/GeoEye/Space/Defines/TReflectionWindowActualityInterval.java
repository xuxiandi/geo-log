package com.geoscope.GeoEye.Space.Defines;

import com.geoscope.GeoLog.Utils.OleDate;


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
			return CurrentBeginTimestamp(); //. ->
		else
			return BeginTimestamp;
	}
	
	public double GetEndTimestamp() {
		if (EndTimestamp == MaxTimestamp) 
			return MaxRealTimestamp; //. ->
		else
			return EndTimestamp;
	}
	
	private double CurrentBeginTimestamp() {
		return OleDate.UTCCurrentTimestamp()-(1.0/24.0)*1/*hours*/;
	}
	
	public double CurrentEndTimestamp() {
		return OleDate.UTCCurrentTimestamp()+(1.0/24.0)*1/*hours*/;
	}
	
	public void SetBeginTimestamp(double pBeginTimestamp) {
		BeginTimestamp = pBeginTimestamp;
	}

	public void SetEndTimestamp(double pEndTimestamp) {
		EndTimestamp = pEndTimestamp;
	}

	public void Set(double pBeginTimestamp, double pEndTimestamp) {
		BeginTimestamp = pBeginTimestamp;
		EndTimestamp = pEndTimestamp;
	}

	public void Set(TReflectionWindowActualityInterval pInterval) {
		BeginTimestamp = pInterval.BeginTimestamp;
		EndTimestamp = pInterval.EndTimestamp;
	}
	
	public void Reset() {
		BeginTimestamp = NullTimestamp;
		EndTimestamp = MaxTimestamp;
	}
	
	public boolean IsBeginTimestampInfinite() {
		return (BeginTimestamp == NullTimestamp);
	}
	
	public boolean IsBeginTimestampMin() {
		return (BeginTimestamp <= MinRealTimestamp);
	}
	
	public boolean IsEndTimestampInfinite() {
		return (EndTimestamp == MaxTimestamp);
	}
	
	public boolean IsEndTimestampMax() {
		return (EndTimestamp >= MaxRealTimestamp);
	}
	
	public boolean IsInfinite() {
		return ((BeginTimestamp == NullTimestamp) && (EndTimestamp == MaxTimestamp));
	}
}
