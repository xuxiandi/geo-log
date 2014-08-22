package com.geoscope.GeoEye.Space.TypesSystem.DetailedPictureVisualization;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentData;

public class TDetailedPictureVisualizationData extends TComponentData {

	public static class TLevelParams {
		
		public static final int Size = 44;
		
	    public int id;
	    public int DivX;
	    public int DivY;
	    public double SegmentWidth;
	    public double SegmentHeight;
	    public double VisibleMinScale;
	    public double VisibleMaxScale;

		public int FromByteArray(byte[] BA, int Idx) throws IOException {
		    id = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; 
		    DivX = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; 
		    DivY = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4; 
		    SegmentWidth = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
		    SegmentHeight = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		    VisibleMinScale = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
		    VisibleMaxScale = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8; 
		    return Idx;
		}
		
		public byte[] ToByteArray() throws IOException {
			byte[] Result = new byte[Size];
			int Idx = 0;
			//.
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(id);
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length; 
			BA = TDataConverter.ConvertInt32ToBEByteArray(DivX);
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(DivY);
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertDoubleToBEByteArray(SegmentWidth);
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertDoubleToBEByteArray(SegmentHeight);
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertDoubleToBEByteArray(VisibleMinScale);
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertDoubleToBEByteArray(VisibleMaxScale);
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
			//.
			return Result;
		}
	}
	
    public TXYCoord N0 = new TXYCoord();
    public TXYCoord N1 = new TXYCoord();
    public TXYCoord N2 = new TXYCoord();
    public TXYCoord N3 = new TXYCoord();
    //.
	public int 		Width;
	public int 		Height;
	//.
	public TLevelParams[] Levels;
	
    @Override
	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
		Idx = super.FromByteArrayV1(BA, Idx);
		//.
		Idx = N0.FromByteArray(BA, Idx);
		Idx = N1.FromByteArray(BA, Idx);
		Idx = N2.FromByteArray(BA, Idx);
		Idx = N3.FromByteArray(BA, Idx);
	    //.
		Width = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
		Height = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
		//.
    	int DataSize = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
		int LC = DataSize/TLevelParams.Size;
		Levels = new TLevelParams[LC];
		for (int I = 0; I < LC; I++) {
			Levels[I] = new TLevelParams();
			Idx = Levels[I].FromByteArray(BA, Idx);
		}
    	//.
    	return Idx;
    }

	public byte[] ToByteArrayV1() throws IOException {
		ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
		try {
			BOS.write(super.ToByteArrayV1());
			//.
			BOS.write(N0.ToByteArray());
			BOS.write(N1.ToByteArray());
			BOS.write(N2.ToByteArray());
			BOS.write(N3.ToByteArray());
			//.
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(Width);
			BOS.write(BA);
			BA = TDataConverter.ConvertInt32ToBEByteArray(Height);
			BOS.write(BA);
			//.
			int DataSize = 0;
			if (Levels != null)
				DataSize = Levels.length*TLevelParams.Size;
			BA = TDataConverter.ConvertInt32ToBEByteArray(DataSize);
			BOS.write(BA);
			if (DataSize > 0) 
				for (int I = 0; I < Levels.length; I++)
					BOS.write(Levels[I].ToByteArray());
			//.
			return BOS.toByteArray(); //. ->
		}
		finally {
			BOS.close();
		}
	}
    
}
