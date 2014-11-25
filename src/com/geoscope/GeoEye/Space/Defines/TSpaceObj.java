package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import android.graphics.Bitmap;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Containers.Number.Real.TReal48;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;


public class TSpaceObj {
	
	public static final int BodySize = 36;
	
	
	public long ptrObj;
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
	public Bitmap Container_Image = null;
	//.
	public int OwnerType;
	public int OwnerID;
	public int OwnerCoType;
	public TComponentTypedDataFiles OwnerTypedDataFiles;

	public TSpaceObj() {
		ptrObj = SpaceDefines.nilPtr;
		Nodes = null;
	}
	
	public TSpaceObj(long pptrObj) {
		ptrObj = pptrObj;
	}
	
	public TSpaceObj(long pptrObj, TXYCoord[] pNodes) {
		ptrObj = pptrObj;
		Nodes = pNodes;
	}
	
	private void SetBodyFromByteArray(byte[] BA, int Index) {
		try {
			ptrNextObj = TDataConverter.ConvertLEByteArrayToInt32(BA,Index); Index += 4;
			idTObj = TDataConverter.ConvertLEByteArrayToInt32(BA,Index); Index += 4;
			idObj = TDataConverter.ConvertLEByteArrayToInt32(BA,Index); Index += 4;
			ptrFirstPoint = TDataConverter.ConvertLEByteArrayToInt32(BA,Index); Index += 4;
			flagLoop = (BA[Index] != 0); Index++;
			ObjColor = TDataConverter.ConvertLEByteArrayToInt32(BA,Index); Index += 4; ///?
			Width = new TReal48(BA, Index); Index += TReal48.Size;
			flagFill = (BA[Index] != 0); Index++;
			ObjColorFill = TDataConverter.ConvertLEByteArrayToInt32(BA,Index); Index += 4; ///?
			ptrListOwnerObj = TDataConverter.ConvertLEByteArrayToInt32(BA,Index); Index += 4;
		}
		catch (Exception E) {
		}
	}
	
	public int SetFromByteArray(byte[] BA, int Index) throws IOException {
        SetBodyFromByteArray(BA,Index); Index += TSpaceObj.BodySize;
        TXYCoord[] ObjNodes = null;
        int NodesCounter = TDataConverter.ConvertLEByteArrayToInt32(BA,Index); Index +=4;
        if (NodesCounter > 0) {
        	ObjNodes = new TXYCoord[NodesCounter];
        	for (int I = 0; I < ObjNodes.length; I++) {
        		ObjNodes[I] = new TXYCoord();
        		ObjNodes[I].X = TDataConverter.ConvertLEByteArrayToDouble(BA,Index); Index += 8;
        		ObjNodes[I].Y = TDataConverter.ConvertLEByteArrayToDouble(BA,Index); Index += 8;
        	}
        }
    	Nodes = ObjNodes;
    	//.
    	return Index;
	}
	
	public TXYCoord Nodes_AveragePoint() {
		double SumX = 0.0;
		double SumY = 0.0;
		int Cnt = Nodes.length;
		if (Cnt == 0)
			return null; //. ->
    	for (int I = 0; I < Cnt; I++) {
    		SumX += Nodes[I].X;
    		SumY += Nodes[I].Y;
    	}
    	return (new TXYCoord(SumX/Cnt,SumY/Cnt));
	}
}
