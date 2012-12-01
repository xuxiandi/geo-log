package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery;

import java.io.File;
import java.io.FilenameFilter;

public class TTileHistoryFolder {

	public static class TTileFileFilter implements FilenameFilter { 
		public boolean accept(File dir, String name) { 
			return name.endsWith("."+TTile.TileFileType); 
		} 
	}	
	
	public static File GetFileToTime(String THFN, double Time) {
		File Result = null;
		File THF = new File(THFN);
		if (!THF.exists())
			return Result; //. ->
		File[] Tiles = THF.listFiles(new TTileFileFilter());
		double MinTimeDiff = Double.MAX_VALUE;
		for (int I = 0; I < Tiles.length; I++) {
			double TileTimestamp = TTile.TileHistoryFolderExtractTileFileNameTimestamp(Tiles[I].getName());
			double TimeDiff = Time-TileTimestamp;
			if ((TimeDiff >= 0) && (TimeDiff <= MinTimeDiff)) {
				MinTimeDiff = TimeDiff;
				Result = Tiles[I];
			}
		}
		return Result;
	}
}
