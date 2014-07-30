package com.geoscope.GeoEye.Space.Defines;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import com.geoscope.Classes.Log.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TTileServerProviderCompilation;

public class TSpaceContainers extends ArrayList<TSpaceContainer> {

	private static final long serialVersionUID = 1L;
	
	public TSpaceContainers() {
		super(10);
	}
	
    public void Translate(float dX, float dY) {
    	int Size = size();
    	for (int I = 0; I < Size; I++) 
    		get(I).Translate(dX,dY);
    }
    
	public byte[] ToByteArray() throws Exception {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			byte[] BA;
			int ContainersCount = size();
			BA = TDataConverter.ConvertInt32ToBEByteArray(ContainersCount);
			BOS.write(BA);
			if (ContainersCount > 0)
				for (int I = 0; I < ContainersCount; I++) {
					BA = get(I).ToByteArray();
					BOS.write(BA);
				}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws Exception {
    	int ContainersCount = (int)TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; //. SizeOf(Int32)
		for (int I = 0; I < ContainersCount; I++) {
			TSpaceContainer Container = new TSpaceContainer();
			Idx = Container.FromByteArray(BA, Idx);
			//.
			add(Container);
		}
		return Idx;
	}	
	
	public void PrepareLevelTileContainers(TTileServerProviderCompilation Compilation) {
    	int ContainersCount = size();
		for (int I = 0; I < ContainersCount; I++) {
			TSpaceContainer Container = get(I); 
			Container.LevelTileContainer = Compilation.ReflectionWindow_GetLevelTileContainer(Container.RW);
		}
	}
}
