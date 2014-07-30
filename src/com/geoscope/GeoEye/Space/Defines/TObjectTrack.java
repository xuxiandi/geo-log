package com.geoscope.GeoEye.Space.Defines;

import com.geoscope.Classes.Log.TDataConverter;


public class TObjectTrack {

	public int 		idGeoMonitorObject;
	public double	Day;
	public int	 	TrackColor;
	public int 		NodesCount;
	public double[] Nodes;
	public boolean 	flEnabled = true;
	
	public TObjectTrack(int pidGeoMonitorObject, double pDay, int pColor, byte[] BA) throws Exception {
		idGeoMonitorObject = pidGeoMonitorObject;
		Day = pDay;
		TrackColor = pColor;
		FromByteArray(BA);
	}
	
	public void FromByteArray(byte[] BA) throws Exception {
		int Idx = 0;
		NodesCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
		if (NodesCount > 0) {
			Nodes  = new double[3*NodesCount];
			int NI = 0;
			for (int I = 0; I < NodesCount; I++) {
				Nodes[NI] = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. TimeStamp
				Nodes[NI] = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. X
				Nodes[NI] = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. Y
			}
		}
		else {
			Nodes = null;
			throw new Exception("нет трека для указанной даты"); //. =>
		}
	}
} 
