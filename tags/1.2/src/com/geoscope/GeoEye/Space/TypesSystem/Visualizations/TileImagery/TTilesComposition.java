package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.util.ArrayList;

public class TTilesComposition {
	   
	public ArrayList<TTilesCompositionLevel> CompositionLevels;
	
	public TTilesComposition(int LevelsCount) {
		CompositionLevels = new ArrayList<TTilesCompositionLevel>(LevelsCount);			
	}
	
	public int TileCount() {
		int Result = 0;
		for (int I = 0; I < CompositionLevels.size(); I++)
			Result += CompositionLevels.get(I).Count;
		return Result;
	}
}
