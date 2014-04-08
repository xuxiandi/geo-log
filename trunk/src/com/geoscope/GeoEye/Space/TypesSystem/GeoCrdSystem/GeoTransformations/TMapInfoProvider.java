package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.DetailedPictureVisualization.TDetailedPictureVisualizationData;
import com.geoscope.GeoEye.Space.TypesSystem.DetailedPictureVisualization.TDetailedPictureVisualizationFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization.TTileServerVisualizationData;
import com.geoscope.GeoEye.Space.TypesSystem.TileServerVisualization.TTileServerVisualizationFunctionality;

public abstract class TMapInfoProvider {
	
    public static TMapInfo GetMapInfo(int idTVisualization, int idVisualization) {
    	switch (idTVisualization) {
    	
    	case SpaceDefines.idTDetailedPictureVisualization:
    		return (new TDPVMapInfo(idVisualization)); //. ->

    	case SpaceDefines.idTTileServerVisualization:
    		return (new TTSVMapInfo(idVisualization)); //. ->

    	default:
        	return null; //. ->
    	}
    }
    
	public static abstract class TMapInfo {

	    public TXYCoord N0,N1,N3;
	    public int Divider;
	    public double SegmentSize;
	    public double Size;

	    public abstract boolean Prepare(TTypesSystem TypesSystem, TGeoScopeServer Server, int ComponentDataSource) throws Exception; 
	    public abstract TXYCoord ConvertPixPosToXY(double PixX, double PixY); 
	    public abstract TXYCoord ConvertXYToPixPos(double X, double Y);
	}
	
	public static class TDPVMapInfo extends TMapInfo {
		
		public int idDetailedPictureVisualization;
		
		public TDPVMapInfo(int pidDetailedPictureVisualization) {
			idDetailedPictureVisualization = pidDetailedPictureVisualization;
		}

		@Override
	    public boolean Prepare(TTypesSystem TypesSystem, TGeoScopeServer Server, int ComponentDataSource) throws Exception {
			TDetailedPictureVisualizationFunctionality DPVF = (TDetailedPictureVisualizationFunctionality)TypesSystem.SystemTDetailedPictureVisualization.TComponentFunctionality_Create(Server, idDetailedPictureVisualization);
			try {
				DPVF.ComponentDataSource = ComponentDataSource;
				//.
				TDetailedPictureVisualizationData DetailedPictureVisualizationData = DPVF.GetData();
				if (DetailedPictureVisualizationData == null)
					return false; //. ->
				//.
				N0 = DetailedPictureVisualizationData.N0;  
				N1 = DetailedPictureVisualizationData.N1;  
				N3 = DetailedPictureVisualizationData.N3;
				//.
				if ((DetailedPictureVisualizationData.Levels == null) || (DetailedPictureVisualizationData.Levels.length == 0))
					throw new Exception("TDPVMapInfo.Prepare() error: no map parameters"); //. =>
				TDetailedPictureVisualizationData.TLevelParams MaxLevelParams = DetailedPictureVisualizationData.Levels[0]; 
				if ((MaxLevelParams.DivX != MaxLevelParams.DivY) || (MaxLevelParams.SegmentWidth != MaxLevelParams.SegmentHeight)) 
					throw new Exception("TDPVMapInfo.Prepare() error: wrong map parameters"); //. =>
				//.
				Divider = MaxLevelParams.DivX;
				SegmentSize = MaxLevelParams.SegmentWidth;
				Size = Divider*SegmentSize;
				//.
				return true; //. ->
			}
			finally {
				DPVF.Release();
			}
		}
		
		@Override
		public TXYCoord ConvertPixPosToXY(double PixX, double PixY) {
			double Col_dX,Col_dY;
			double Row_dX,Row_dY;
			double Col_Factor,Row_Factor;
			//.
			Col_dX = (N1.X-N0.X); Col_dY = (N1.Y-N0.Y);
			Row_dX = (N3.X-N0.X); Row_dY = (N3.Y-N0.Y);
			Col_Factor = PixX/Size;
			Row_Factor = PixY/Size; //. PixLength the same as for X
			TXYCoord Result = new TXYCoord();
			Result.X = N0.X+Col_dX*Col_Factor+Row_dX*Row_Factor;
			Result.Y = N0.Y+Col_dY*Col_Factor+Row_dY*Row_Factor;
			//.
			return Result;
		}

		private TXYCoord ConvertXYToPixPos_ProcessPoint(double X0, double  Y0, double X1, double Y1, double X3, double Y3, double X, double Y) {
			double QdA2;
			double X_C,X_QdC,X_A1,X_QdB2;
			double Y_C,Y_QdC,Y_A1,Y_QdB2;
		    
			QdA2 = Math.pow((X-X0),2)+Math.pow((Y-Y0),2);
			//.
			X_QdC = Math.pow((X1-X0),2)+Math.pow((Y1-Y0),2);
			X_C = Math.sqrt(X_QdC);
			X_QdB2 = Math.pow((X-X1),2)+Math.pow((Y-Y1),2);
			X_A1 = (X_QdC-X_QdB2+QdA2)/(2*X_C);
		    //.
			Y_QdC = Math.pow((X3-X0),2)+Math.pow((Y3-Y0),2);
			Y_C = Math.sqrt(Y_QdC);
			Y_QdB2 = Math.pow((X-X3),2)+Math.pow((Y-Y3),2);
			Y_A1 = (Y_QdC-Y_QdB2+QdA2)/(2*Y_C);
			//.
			TXYCoord Result = new TXYCoord();
			Result.X = X_A1/X_C;
			Result.Y = Y_A1/Y_C;
			
			return Result;
		}

		@Override
		public TXYCoord ConvertXYToPixPos(double X, double Y) {
			TXYCoord Factor = ConvertXYToPixPos_ProcessPoint(N0.X,N0.Y,N1.X,N1.Y,N3.X,N3.Y, X,Y);
			//.
			TXYCoord Result = new TXYCoord();
			Result.X = Factor.X*Size;
			Result.Y = Factor.Y*Size; //. same as for X
			//.
			return Result;
		}
	}
	
	public static class TTSVMapInfo extends TMapInfo {
	
		public int idTileServerVisualization;
		
		public TTSVMapInfo(int pidTileServerVisualization) {
			idTileServerVisualization = pidTileServerVisualization;
		}

		@Override
	    public boolean Prepare(TTypesSystem TypesSystem, TGeoScopeServer Server, int ComponentDataSource) throws Exception {
			TTileServerVisualizationFunctionality TSVF = (TTileServerVisualizationFunctionality)TypesSystem.SystemTTileServerVisualization.TComponentFunctionality_Create(Server, idTileServerVisualization);
			try {
				TSVF.ComponentDataSource = ComponentDataSource;
				//.
				TTileServerVisualizationData TileServerVisualizationData = TSVF.GetData();
				if (TileServerVisualizationData == null)
					return false; //. ->
				//.
				N0 = TileServerVisualizationData.N0;  
				N1 = TileServerVisualizationData.N1;  
				N3 = TileServerVisualizationData.N3;
				//.
				if ((TileServerVisualizationData.Levels == null) || (TileServerVisualizationData.Levels.length == 0))
					throw new Exception("TTSVMapInfo.Prepare() error: no map parameters"); //. =>
				TTileServerVisualizationData.TLevelParams MaxLevelParams = TileServerVisualizationData.Levels[0]; 
				if ((MaxLevelParams.DivX != MaxLevelParams.DivY) || (MaxLevelParams.SegmentWidth != MaxLevelParams.SegmentHeight)) 
					throw new Exception("TTSVMapInfo.Prepare() error: wrong map parameters"); //. =>
				//.
				Divider = MaxLevelParams.DivX;
				SegmentSize = MaxLevelParams.SegmentWidth;
				Size = Divider*SegmentSize;
				//.
				return true; //. ->
			}
			finally {
				TSVF.Release();
			}
		}
		
		@Override
		public TXYCoord ConvertPixPosToXY(double PixX, double PixY) {
			double Col_dX,Col_dY;
			double Row_dX,Row_dY;
			double Col_Factor,Row_Factor;
			//.
			Col_dX = (N1.X-N0.X); Col_dY = (N1.Y-N0.Y);
			Row_dX = (N3.X-N0.X); Row_dY = (N3.Y-N0.Y);
			Col_Factor = PixX/Size;
			Row_Factor = PixY/Size; //. PixLength the same as for X
			TXYCoord Result = new TXYCoord();
			Result.X = N0.X+Col_dX*Col_Factor+Row_dX*Row_Factor;
			Result.Y = N0.Y+Col_dY*Col_Factor+Row_dY*Row_Factor;
			//.
			return Result;
		}

		private TXYCoord ConvertXYToPixPos_ProcessPoint(double X0, double  Y0, double X1, double Y1, double X3, double Y3, double X, double Y) {
			double QdA2;
			double X_C,X_QdC,X_A1,X_QdB2;
			double Y_C,Y_QdC,Y_A1,Y_QdB2;
		    
			QdA2 = Math.pow((X-X0),2)+Math.pow((Y-Y0),2);
			//.
			X_QdC = Math.pow((X1-X0),2)+Math.pow((Y1-Y0),2);
			X_C = Math.sqrt(X_QdC);
			X_QdB2 = Math.pow((X-X1),2)+Math.pow((Y-Y1),2);
			X_A1 = (X_QdC-X_QdB2+QdA2)/(2*X_C);
		    //.
			Y_QdC = Math.pow((X3-X0),2)+Math.pow((Y3-Y0),2);
			Y_C = Math.sqrt(Y_QdC);
			Y_QdB2 = Math.pow((X-X3),2)+Math.pow((Y-Y3),2);
			Y_A1 = (Y_QdC-Y_QdB2+QdA2)/(2*Y_C);
			//.
			TXYCoord Result = new TXYCoord();
			Result.X = X_A1/X_C;
			Result.Y = Y_A1/Y_C;
			
			return Result;
		}

		@Override
		public TXYCoord ConvertXYToPixPos(double X, double Y) {
			TXYCoord Factor = ConvertXYToPixPos_ProcessPoint(N0.X,N0.Y,N1.X,N1.Y,N3.X,N3.Y, X,Y);
			//.
			TXYCoord Result = new TXYCoord();
			Result.X = Factor.X*Size;
			Result.Y = Factor.Y*Size; //. same as for X
			//.
			return Result;
		}
	}
}
