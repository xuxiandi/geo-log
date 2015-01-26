package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject;

import java.io.ByteArrayOutputStream;

import android.graphics.Color;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.MultiThreading.TCanceller;


public class TCoGeoMonitorObjectTrack {

	public int	 	TrackColor = Color.RED;
	public int 		NodesCount = 0;
	public double[] Nodes = null;
	public boolean 	flEnabled = true;
	
	public TCoGeoMonitorObjectTrack() {
	}
	
	public TCoGeoMonitorObjectTrack(int pColor, byte[] BA) throws Exception {
		TrackColor = pColor;
		FromByteArray(BA);
	}
	
	public TCoGeoMonitorObjectTrack(int pTrackColor) throws Exception {
		TrackColor = pTrackColor;
	}
	
	public void FromByteArray(byte[] BA) throws Exception {
		int Idx = 0;
		NodesCount = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4;
		if (NodesCount > 0) {
			Nodes  = new double[3*NodesCount];
			int NI = 0;
			for (int I = 0; I < NodesCount; I++) {
				Nodes[NI] = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. TimeStamp
				Nodes[NI] = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. X
				Nodes[NI] = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. Y
			}
		}
		else {
			Nodes = null;
			throw new Exception("no track for the time"); //. =>
		}
	}
	
	public void FromByteArray1(byte[] BA, TCanceller Canceller) throws Exception {
		int Idx = 0;
		TrackColor = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4;
		NodesCount = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += 4;
		if (NodesCount > 0) {
			Nodes  = new double[3*NodesCount];
			int NI = 0;
			for (int I = 0; I < NodesCount; I++) {
				Nodes[NI] = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. TimeStamp
				Nodes[NI] = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. X
				Nodes[NI] = TDataConverter.ConvertLEByteArrayToDouble(BA, Idx); Idx +=8; NI++; //. Y
				//.
				if (Canceller != null)
					Canceller.Check();
			}
		}
		else {
			Nodes = null;
			throw new Exception("no track for the time"); //. =>
		}
	}
	
	public byte[] ToByteArrayV1() throws Exception {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		try {
			BOS.write(TDataConverter.ConvertInt32ToLEByteArray(TrackColor));
			//.
			BOS.write(TDataConverter.ConvertInt32ToLEByteArray(NodesCount));
			//.
			int Idx = 0;
			for (int I = 0; I < NodesCount; I++) {
				BOS.write(TDataConverter.ConvertDoubleToLEByteArray(Nodes[Idx])); Idx++; //. TimeStamp
				BOS.write(TDataConverter.ConvertDoubleToLEByteArray(Nodes[Idx])); Idx++; //. X
				BOS.write(TDataConverter.ConvertDoubleToLEByteArray(Nodes[Idx])); Idx++; //. Y
			}
			return BOS.toByteArray();
		}
		finally {
			BOS.close();
		}
	}
} 
