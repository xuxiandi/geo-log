package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.TypesSystem.Visualizations.TileImagery.TRWLevelTileContainer;

public class TSpaceContainer {
	
	public float 					dX = 0.0F;
	public float 					dY = 0.0F;
	public TReflectionWindowStruc 	RW = null;
	public TRWLevelTileContainer 	LevelTileContainer = null;
	public boolean 					flModified = false;
	
	public TSpaceContainer() {
	}
	
	public void Assign(TSpaceContainer Container) {
		dX = Container.dX;
		dY = Container.dY;
		if (RW != null)
			RW.Assign(Container.RW);
		else
			RW = new TReflectionWindowStruc(Container.RW);
		if (LevelTileContainer != null)
			LevelTileContainer.AssignContainer(Container.LevelTileContainer);
		else
			LevelTileContainer = new TRWLevelTileContainer(Container.LevelTileContainer);
		flModified = Container.flModified;
	}

	public byte[] ToByteArray() throws IOException {
		byte[] Result = new byte[8/*SizeOf(dX)*/+8/*SizeOf(dY)*/+TReflectionWindowStruc.ByteArraySize()+1];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertDoubleToBEByteArray(dX);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = TDataConverter.ConvertDoubleToBEByteArray(dY);
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		BA = RW.ToByteArray();
		System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (flModified)
			Result[Idx] = 1;
		else
			Result[Idx] = 0;
		return Result;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException {
		dX = (float)TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		dY = (float)TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		RW = new TReflectionWindowStruc();
		Idx = RW.FromByteArrayV1(BA, Idx);
		LevelTileContainer = null;
		flModified = (BA[Idx] != 0); Idx++;
		return Idx;
	}
	
	public void Translate(float pdX, float pdY) {
		dX += pdX;
		dY += pdY;
	}
}
