package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;


import android.graphics.Matrix;


public class TReflectionWindowStruc {

	public double X0;
	public double Y0;
	public double X1;
	public double Y1;
	public double X2;
	public double Y2;
	public double X3;
	public double Y3;

	public int Xmn;
	public int Ymn;
	public int Xmx;
	public int Ymx;
	//.
	public double BeginTimestamp;
	public double EndTimestamp;
	//. container
	public double Container_Xmin;
	public double Container_Ymin;
	public double Container_Xmax;
	public double Container_Ymax;
	public double Container_S;
	
	public TReflectionWindowStruc()	
	{		
		BeginTimestamp = TReflectionWindowActualityInterval.NullTimestamp;
		EndTimestamp = TReflectionWindowActualityInterval.MaxTimestamp;
	}
	
	public TReflectionWindowStruc(double pX0, double pY0, double pX1, double pY1, double pX2, double pY2, double pX3, double pY3, int pXmn, int pYmn, int pXmx, int pYmx, double pBeginTimestamp, double pEndTimestamp)
	{
		X0 = pX0; Y0 = pY0;
		X1 = pX1; Y1 = pY1;
		X2 = pX2; Y2 = pY2;
		X3 = pX3; Y3 = pY3;
		Xmn = pXmn; Ymn = pYmn;
		Xmx = pXmx; Ymx = pYmx;
		//.
		BeginTimestamp = pBeginTimestamp;
		EndTimestamp = pEndTimestamp;
		//.
		UpdateContainer();
	}

	public boolean IsEqualTo(TReflectionWindowStruc RW)
	{
		return ((X0 == RW.X0) && (Y0 == RW.Y0) && (X1 == RW.X1) && (Y1 == RW.Y1) && (X2 == RW.X2) && (Y2 == RW.Y2) && (X3 == RW.X3) && (Y3 == RW.Y3) && (Xmn == RW.Xmn) && (Ymn == RW.Ymn) && (Xmx == RW.Xmx) && (Ymx == RW.Ymx) && (BeginTimestamp == RW.BeginTimestamp) && (EndTimestamp == RW.EndTimestamp));
	}

	public synchronized double Scale()
	{
		return ((Xmx-Xmn)/(Math.sqrt(Math.pow((X1-X0),2)+Math.pow((Y1-Y0),2))));
	}

	public synchronized int getWidth() {
		return (Xmx-Xmn);
	}
	
	public synchronized int getHeight() {
		return (Ymx-Ymn);
	}
	
	public synchronized void UpdateContainer()
	{
		Container_Xmin = X0;
		Container_Ymin = Y0;
		Container_Xmax = X0;
		Container_Ymax = Y0;
		if (X1 < Container_Xmin) Container_Xmin = X1; else if (X1 > Container_Xmax) Container_Xmax = X1;
		if (Y1 < Container_Ymin) Container_Ymin = Y1; else if (Y1 > Container_Ymax) Container_Ymax = Y1;
		if (X2 < Container_Xmin) Container_Xmin = X2; else if (X2 > Container_Xmax) Container_Xmax = X2;
		if (Y2 < Container_Ymin) Container_Ymin = Y2; else if (Y2 > Container_Ymax) Container_Ymax = Y2;
		if (X3 < Container_Xmin) Container_Xmin = X3; else if (X3 > Container_Xmax) Container_Xmax = X3;
		if (Y3 < Container_Ymin) Container_Ymin = Y3; else if (Y3 > Container_Ymax) Container_Ymax = Y3;
		Container_S = (Container_Xmax-Container_Xmin)*(Container_Ymax-Container_Ymin);
	}

	public TXYCoord ConvertToScreen(double X, double Y) {
	    double QdA2;
	    double X_C,X_QdC,X_A1,X_QdB2;
	    double Y_C,Y_QdC,Y_A1,Y_QdB2;
	    //.
	    QdA2 = Math.pow(X-X0,2)+Math.pow(Y-Y0,2);
	    //.
	    X_QdC = Math.pow(X1-X0,2)+Math.pow(Y1-Y0,2);
	    X_C = Math.sqrt(X_QdC);
	    X_QdB2 = Math.pow(X-X1,2)+Math.pow(Y-Y1,2);
	    X_A1 = (X_QdC-X_QdB2+QdA2)/(2*X_C);
	    //.
	    Y_QdC = Math.pow(X3-X0,2)+Math.pow(Y3-Y0,2);
	    Y_C = Math.sqrt(Y_QdC);
	    Y_QdB2 = Math.pow(X-X3,2)+Math.pow(Y-Y3,2);
	    Y_A1 = (Y_QdC-Y_QdB2+QdA2)/(2*Y_C);
	    //.
		TXYCoord C = new TXYCoord();
	    C.X = Xmn+X_A1/X_C*(Xmx-Xmn);
	    C.Y = Ymn+Y_A1/Y_C*(Ymx-Ymn);
	    //.
	    return C;
	}
	
	public synchronized TXYCoord ConvertToReal(double SX, double SY)
	{
		TXYCoord XYCoord = new TXYCoord();
		double VS = -(SY-Ymn)/(Ymx-Ymn);
		double HS = -(SX-Xmn)/(Xmx-Xmn);
		double diffX0X3 = (X0-X3);
		double diffY0Y3 = (Y0-Y3);
		double diffX0X1 = (X0-X1);
		double diffY0Y1 = (Y0-Y1);
		double ofsX = (diffX0X1)*HS+(diffX0X3)*VS;
		double ofsY = (diffY0Y1)*HS+(diffY0Y3)*VS;
		XYCoord.X = (X0+ofsX);
		XYCoord.Y = (Y0+ofsY);
		return XYCoord;
	}    
	
	public synchronized void MultiplyByMatrix(Matrix matrix) {
		float[] Nodes = new float[8];
		Nodes[0] = Xmn; Nodes[1] = Ymn;
		Nodes[2] = Xmx; Nodes[3] = Ymn;
		Nodes[4] = Xmx; Nodes[5] = Ymx;
		Nodes[6] = Xmn; Nodes[7] = Ymx;
		matrix.mapPoints(Nodes);
		TXYCoord C0 = ConvertToReal(Nodes[0],Nodes[1]);
		TXYCoord C1 = ConvertToReal(Nodes[2],Nodes[3]);
		TXYCoord C2 = ConvertToReal(Nodes[4],Nodes[5]);
		TXYCoord C3 = ConvertToReal(Nodes[6],Nodes[7]);
		X0 = C0.X; Y0 = C0.Y;  
		X1 = C1.X; Y1 = C1.Y;  
		X2 = C2.X; Y2 = C2.Y;  
		X3 = C3.X; Y3 = C3.Y;  
		//.
		Normalize();
	}
	
	public synchronized void Normalize()
	{
		double diffX1X0;
		double diffY1Y0;
		double b;
		double V;
		double S0_X3;
		double S0_Y3;
		double S1_X3;
		double S1_Y3;
		double S0_X2;
		double S0_Y2;
		double S1_X2;
		double S1_Y2;

		diffX1X0 = X1-X0;
		diffY1Y0 = Y1-Y0;
		b = Math.sqrt(Math.pow(diffX1X0,2)+Math.pow(diffY1Y0,2))*(Ymx-Ymn)/(Xmx-Xmn);
		if (Math.abs(diffY1Y0) > Math.abs(diffX1X0))
		{
			V = b/Math.sqrt(1+Math.pow((diffX1X0/diffY1Y0),2));
			S0_X3 = (V)+X0;
			S0_Y3 = (-V)*(diffX1X0/diffY1Y0)+Y0;
			S1_X3 = (-V)+X0;
			S1_Y3 = (V)*(diffX1X0/diffY1Y0)+Y0;

			S0_X2 = (V)+X1;
			S0_Y2 = (-V)*(diffX1X0/diffY1Y0)+Y1;
			S1_X2 = (-V)+X1;
			S1_Y2 = (V)*(diffX1X0/diffY1Y0)+Y1;
		}
		else 
		{
			V = b/Math.sqrt(1+Math.pow((diffY1Y0/diffX1X0),2));
			S0_Y3 = (V)+Y0;
			S0_X3 = (-V)*(diffY1Y0/diffX1X0)+X0;
			S1_Y3 = (-V)+Y0;
			S1_X3 = (V)*(diffY1Y0/diffX1X0)+X0;

			S0_Y2 = (V)+Y1;
			S0_X2 = (-V)*(diffY1Y0/diffX1X0)+X1;
			S1_Y2 = (-V)+Y1;
			S1_X2 = (V)*(diffY1Y0/diffX1X0)+X1;
		};
		if (Math.sqrt(Math.pow((X3-S0_X3),2)+Math.pow((Y3-S0_Y3),2)) < Math.sqrt(Math.pow((X3-S1_X3),2)+Math.pow((Y3-S1_Y3),2)))
		{
			X3 = S0_X3;
			Y3 = S0_Y3;
		}
		else
		{
			X3 = S1_X3;
			Y3 = S1_Y3;
		};
		if (Math.sqrt(Math.pow((X2-S0_X2),2)+Math.pow((Y2-S0_Y2),2)) < Math.sqrt(Math.pow((X2-S1_X2),2)+Math.pow((Y2-S1_Y2),2)))
		{
			X2 = S0_X2;
			Y2 = S0_Y2;
		}
		else 
		{
			X2 = S1_X2;
			Y2 = S1_Y2;
		};
		UpdateContainer();
	}
	
	public boolean Container_VisibleInContainerOf(TReflectionWindowStruc RW) {
		if (Container_Xmax < RW.Container_Xmin)
			return false; //. -> 
		if (Container_Xmin > RW.Container_Xmax)
			return false; //. -> 
		if (Container_Ymax < RW.Container_Ymin)
			return false; //. -> 
		if (Container_Ymin > RW.Container_Ymax)
			return false; //. -> 
		return true;
	}
    
	public boolean Container_IsNodeVisible(double NodeX, double NodeY) {
		return (((Container_Xmin <= NodeX) && (NodeX <= Container_Xmax)) && ((Container_Ymin <= NodeY) && (NodeY <= Container_Ymax))); 
	}
	
	public boolean Container_VisibleInContainer(double pContainer_Xmin, double pContainer_Xmax, double pContainer_Ymin, double pContainer_Ymax) {
		if (Container_Xmax < pContainer_Xmin)
			return false; //. -> 
		if (Container_Xmin > pContainer_Xmax)
			return false; //. -> 
		if (Container_Ymax < pContainer_Ymin)
			return false; //. -> 
		if (Container_Ymin > pContainer_Ymax)
			return false; //. -> 
		return true;
	}
	
	public double Container_IntersectSquare(double pContainer_Xmin, double pContainer_Xmax, double pContainer_Ymin, double pContainer_Ymax) {
		double W;
		if ((Container_Xmin <= pContainer_Xmin) && (pContainer_Xmin <= Container_Xmax))
			if (pContainer_Xmax <= Container_Xmax)
				W = (pContainer_Xmax-pContainer_Xmin);
			else
				W = (Container_Xmax-pContainer_Xmin);
		else
			if (Container_Xmax <= pContainer_Xmax)
				W = (Container_Xmax-Container_Xmin);
			else
				W = (pContainer_Xmax-Container_Xmin);
		double H;
		if ((Container_Ymin <= pContainer_Ymin) && (pContainer_Ymin <= Container_Ymax))
			if (pContainer_Ymax <= Container_Ymax)
				H = (pContainer_Ymax-pContainer_Ymin);
			else
				H = (Container_Ymax-pContainer_Ymin);
		else
			if (Container_Ymax <= pContainer_Ymax)
				H = (Container_Ymax-Container_Ymin);
			else
				H = (pContainer_Ymax-Container_Ymin);
		return (W*H);
	}
    
	private int CheckingInsideVisibility_ProcessCounters(double Xl, double Yl, double P0_X, double P0_Y, double P1_X, double P1_Y) {
    	double dX,Yc;
      	if ((Xl-P0_X)*(Xl-P1_X) <= 0) {
        	dX = P1_X-P0_X;
        	if (dX != 0) {
          		Yc = P0_Y+(Xl-P0_X)*(P1_Y-P0_Y)/dX;
          		if (Yc >= Yl)
           			return 1; //. ->
          		else
            		return -1; //. ->
          	}
        }
        return 0;
	}
	
	public boolean NodeIsVisibleInPolygon(double[] PolygonNodes) {
		double LX0,LY0,LX1,LY1;
		int cntLinesUpCenter = 0;
		int cntLinesDownCenter = 0;
		double[] Nodes = {X0,Y0,X1,Y1,X2,Y2,X3,Y3,X0,Y0};
		int Cnt = (int)(PolygonNodes.length/2);
		//. check polygon nodes in window
		for (int I = 0; I < Cnt-1; I++) {
			cntLinesUpCenter = 0;
			cntLinesDownCenter = 0;
			LX0 = Nodes[0]; LY0 = Nodes[1];
			for (int N = 1; N < 5; N++) {
				LX1 = Nodes[(N << 1)]; LY1 = Nodes[(N << 1)+1];
				int S = CheckingInsideVisibility_ProcessCounters(PolygonNodes[(I << 1)],PolygonNodes[(I << 1)+1], LX0,LY0, LX1,LY1);
				if (S > 0)
					cntLinesUpCenter++;
				else
					if (S < 0)
						cntLinesDownCenter++;
				//.
				LX0 = LX1; LY0 = LY1;
			}
			if (((cntLinesUpCenter % 2) > 0) && ((cntLinesDownCenter % 2) > 0))
				return true; //. ->
		}
		//. check window nodes in polygon
		for (int N = 0; N < 4; N++) {
			cntLinesUpCenter = 0;
			cntLinesDownCenter = 0;
			LX0 = PolygonNodes[0]; LY0 = PolygonNodes[1];
			for (int I = 1; I < Cnt; I++) {
				LX1 = PolygonNodes[(I << 1)]; LY1 = PolygonNodes[(I << 1)+1];
				int S = CheckingInsideVisibility_ProcessCounters(Nodes[(N << 1)],Nodes[(N << 1)+1], LX0,LY0, LX1,LY1);
				if (S > 0)
					cntLinesUpCenter++;
				else
					if (S < 0)
						cntLinesDownCenter++;
				//.
				LX0 = LX1; LY0 = LY1;
			}
			if (((cntLinesUpCenter % 2) > 0) && ((cntLinesDownCenter % 2) > 0))
				return true; //. ->
		}
		return false;
	}
	
	public boolean AllPolygonNodesAreVisible(double[] PolygonNodes) {
		double LX0,LY0,LX1,LY1;
		int cntLinesUpCenter = 0;
		int cntLinesDownCenter = 0;
		double[] Nodes = {X0,Y0,X1,Y1,X2,Y2,X3,Y3,X0,Y0};
		int Cnt = (int)(PolygonNodes.length/2);
		//. check polygon nodes in window
		for (int I = 0; I < Cnt; I++) {
			cntLinesUpCenter = 0;
			cntLinesDownCenter = 0;
			LX0 = Nodes[0]; LY0 = Nodes[1];
			for (int N = 1; N < 5; N++) {
				LX1 = Nodes[(N << 1)]; LY1 = Nodes[(N << 1)+1];
				int S = CheckingInsideVisibility_ProcessCounters(PolygonNodes[(I << 1)],PolygonNodes[(I << 1)+1], LX0,LY0, LX1,LY1);
				if (S > 0)
					cntLinesUpCenter++;
				else
					if (S < 0)
						cntLinesDownCenter++;
				//.
				LX0 = LX1; LY0 = LY1;
			}
			if (!(((cntLinesUpCenter % 2) > 0) && ((cntLinesDownCenter % 2) > 0)))
				return false; //. ->
		}
		return true;
	}
	
	public boolean NodeIsVisible(double NodeX, double NodeY) {
		double LX0,LY0,LX1,LY1;
		int cntLinesUpCenter = 0;
		int cntLinesDownCenter = 0;
		double[] Nodes = {X0,Y0,X1,Y1,X2,Y2,X3,Y3,X0,Y0};
		cntLinesUpCenter = 0;
		cntLinesDownCenter = 0;
		LX0 = Nodes[0]; LY0 = Nodes[1];
		for (int N = 1; N < 5; N++) {
			LX1 = Nodes[(N << 1)]; LY1 = Nodes[(N << 1)+1];
			int S = CheckingInsideVisibility_ProcessCounters(NodeX,NodeY, LX0,LY0, LX1,LY1);
			if (S > 0)
				cntLinesUpCenter++;
			else
				if (S < 0)
					cntLinesDownCenter++;
			//.
			LX0 = LX1; LY0 = LY1;
		}
		return (((cntLinesUpCenter % 2) > 0) && ((cntLinesDownCenter % 2) > 0));
	}
	
	public synchronized double SimilarityFactor(TReflectionWindowStruc RW) {
		return (Math.pow(RW.Container_Xmin-Container_Xmin,2)+Math.pow(RW.Container_Ymin-Container_Ymin,2)+Math.pow(RW.Container_Xmax-Container_Xmax,2)+Math.pow(RW.Container_Ymax-Container_Ymax,2));
	}
	
	public static int ByteArraySize() {
		return (8*8+4*4+8+8);
	}
	
	public byte[] ToByteArray() throws IOException
	{
		byte[] Result = new byte[ByteArraySize()];
		int Idx = 0;
		byte[] BA;
		//.
		BA = TDataConverter.ConvertDoubleToBEByteArray(X0); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		BA = TDataConverter.ConvertDoubleToBEByteArray(Y0); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		BA = TDataConverter.ConvertDoubleToBEByteArray(X1); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		BA = TDataConverter.ConvertDoubleToBEByteArray(Y1); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		BA = TDataConverter.ConvertDoubleToBEByteArray(X2); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		BA = TDataConverter.ConvertDoubleToBEByteArray(Y2); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		BA = TDataConverter.ConvertDoubleToBEByteArray(X3); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		BA = TDataConverter.ConvertDoubleToBEByteArray(Y3); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		//.
		BA = TDataConverter.ConvertInt32ToBEByteArray(Xmn); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=4;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Ymn); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=4;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Xmx); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=4;
		BA = TDataConverter.ConvertInt32ToBEByteArray(Ymx); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=4;
		//.
		BA = TDataConverter.ConvertDoubleToBEByteArray(BeginTimestamp); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		BA = TDataConverter.ConvertDoubleToBEByteArray(EndTimestamp); System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=8;
		return Result;
	}

	public int FromByteArray(byte[] BA, int Idx) throws IOException
	{
		X0 = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		Y0 = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		X1 = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		Y1 = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		X2 = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		Y2 = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		X3 = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		Y3 = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;
		Xmn = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx+=4;
		Ymn = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx+=4;
		Xmx = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx+=4;
		Ymx = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx+=4;
		//.
		BeginTimestamp = TReflectionWindowActualityInterval.NullTimestamp;
		EndTimestamp = TReflectionWindowActualityInterval.MaxTimestamp;
		//.
		UpdateContainer();
		//.
		return Idx;
	}

	public int FromByteArrayV1(byte[] BA, int Idx) throws IOException
	{
		Idx = FromByteArray(BA, Idx);
		//.
		if (Idx < BA.length) {
			BeginTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;			
		}
		else
			BeginTimestamp = TReflectionWindowActualityInterval.NullTimestamp;
		if (Idx < BA.length) {
			EndTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA,Idx); Idx+=8;			
		}
		else
			EndTimestamp = TReflectionWindowActualityInterval.MaxTimestamp;
		//.
		return Idx;
	}

	public void FromByteArrayV1(byte[] BA) throws IOException
	{
		int Idx = 0;
		FromByteArrayV1(BA,Idx);
	}
}
