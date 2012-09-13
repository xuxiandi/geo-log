package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

public class TTileLimit {

	private int StartValue;
	public int	Value;
	
	public TTileLimit(int pStartValue) {
		StartValue = pStartValue;
		Value = StartValue;
	}
	
	public void Reset() {
		Value = StartValue;
	}
}
