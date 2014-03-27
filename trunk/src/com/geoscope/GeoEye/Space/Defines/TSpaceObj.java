package com.geoscope.GeoEye.Space.Defines;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.geoscope.Utils.TDataConverter;


public class TSpaceObj {
	public static final int Size = 36;
	//.
	public int ptrObj;
	//.
	public int 		ptrNextObj;
	public int 		idTObj;
	public int 		idObj;
	public int 		ptrFirstPoint;
    public boolean 	flagLoop;
    public int 		ObjColor;
    public TReal48 	Width;
    public boolean 	flagFill;
    public int 		ObjColorFill;
	public int 		ptrListOwnerObj;
	//.
	public TXYCoord[] Nodes;
	//.
	public int OwnerType;
	public int OwnerID;
	public int OwnerCoType;
	public TComponentTypedDataFiles OwnerTypedDataFiles;

	public TSpaceObj() {
		ptrObj = SpaceDefines.nilPtr;
		Nodes = null;
	}
	
	public TSpaceObj(int pptrObj) {
		ptrObj = pptrObj;
	}
	
	public TSpaceObj(int pptrObj, TXYCoord[] pNodes) {
		ptrObj = pptrObj;
		Nodes = pNodes;
	}
	
	public void SetObjBodyFromByteArray(byte[] BA, int Index) {
		try {
			ptrNextObj = TDataConverter.ConvertBEByteArrayToInt32(BA,Index); Index += 4;
			idTObj = TDataConverter.ConvertBEByteArrayToInt32(BA,Index); Index += 4;
			idObj = TDataConverter.ConvertBEByteArrayToInt32(BA,Index); Index += 4;
			ptrFirstPoint = TDataConverter.ConvertBEByteArrayToInt32(BA,Index); Index += 4;
			flagLoop = (BA[Index] != 0); Index++;
			ObjColor = TDataConverter.ConvertBEByteArrayToInt32(BA,Index); Index += 4; ///?
			Width = new TReal48(BA, Index); Index += TReal48.Size;
			flagFill = (BA[Index] != 0); Index++;
			ObjColorFill = TDataConverter.ConvertBEByteArrayToInt32(BA,Index); Index += 4; ///?
			ptrListOwnerObj = TDataConverter.ConvertBEByteArrayToInt32(BA,Index); Index += 4;
		}
		catch (Exception E) {
		}
	}
	
	public synchronized void DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas, Paint paint) {
		if (Nodes != null) {
			TXYCoord[] ScrNodes = RW.ConvertNodesToScreen(Nodes);
			float[] ScreenNodes = new float[ScrNodes.length << 2];
			int Idx = 0;
			for (int I = 0; I < (ScrNodes.length-1); I++) {
				ScreenNodes[Idx] = (float)ScrNodes[I].X;
				Idx++;
				ScreenNodes[Idx] = (float)ScrNodes[I].Y;
				Idx++;
				ScreenNodes[Idx] = (float)ScrNodes[I+1].X;
				Idx++;
				ScreenNodes[Idx] = (float)ScrNodes[I+1].Y;
				Idx++;
			}
			ScreenNodes[Idx] = (float)ScrNodes[(ScrNodes.length-1)].X;
			Idx++;
			ScreenNodes[Idx] = (float)ScrNodes[(ScrNodes.length-1)].Y;
			Idx++;
			ScreenNodes[Idx] = (float)ScrNodes[0].X;
			Idx++;
			ScreenNodes[Idx] = (float)ScrNodes[0].Y;
			//.
			canvas.drawLines(ScreenNodes,paint);
		} 
	}	
}
