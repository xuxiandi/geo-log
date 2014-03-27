package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

public class TTimeLimit {

	public static class TimeIsExpiredException extends Exception {
		
		private static final long serialVersionUID = 1L;

		public TimeIsExpiredException() {
			super();
		}
	}
	
	private int 	Interval;
	private long 	Value;
	
	public TTimeLimit(int pInterval) {
		Interval = pInterval;
		Reset();
	}
	
	public void Reset() {
		Value = System.currentTimeMillis()+Interval;
	}
	
	public void Check() throws TimeIsExpiredException {
		if (System.currentTimeMillis() > Value)
			throw new TimeIsExpiredException(); //. =>
	}
}
