package com.geoscope.GeoEye.Space.Defines;


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
	public float[] ScreenNodes;
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
}
