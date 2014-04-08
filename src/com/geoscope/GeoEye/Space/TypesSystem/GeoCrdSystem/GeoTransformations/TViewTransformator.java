package com.geoscope.GeoEye.Space.TypesSystem.GeoCrdSystem.GeoTransformations;

import com.geoscope.GeoEye.Space.Defines.TXYCoord;

public class TViewTransformator {

	public static final int ALGORITHM_NONE 			= 0;
	public static final int ALGORITHM_LINEAR 		= 1;
	public static final int ALGORITHM_POLYNOM1 		= 2;
	public static final int ALGORITHM_POLYNOM2 		= 3;
	public static final int ALGORITHM_POLYNOM3 		= 4;
	public static final int ALGORITHM_POLYNOM1RMS 	= 5;
	public static final int ALGORITHM_POLYNOM2RMS 	= 6;
	public static final int ALGORITHM_POLYNOM3RMS 	= 7;
	
	public static class TControlPoint {
		
		public double E,N;
		public double X,Y;
	}

    public static class TParams {

    	public int Algorithm;
    	public double[] X;
    	public double[] Y;
    	public double[] E;
    	public double[] N;
    }
    
    public static class TMatrix {
    	
        public static double[][] Multiply(int N, int M, int K, double[][] A, double[][] B) {
        	int I;
        	int J;
        	int L;
        	//.
        	double[][] C = new double[N+1][K+1];
        	I = 1;
        	while (I <= N) {
        		J = 1;
        		while (J <= K) {
        			C[I][J] = 0;
        			L = 1;
        			while (L <= M) {
        				C[I][J] = C[I][J]+A[I][L]*B[L][J];
        				L++;
        			}
        			J++;
        		}
        		I++;
        	}
        	return C;
        }

        private static int[] LUDecomposition(double[][] A, int M, int N) {
        	int I;
        	int J;
        	int JP;
        	double[] T1;
        	double S;
        	int i_;
        	//.
        	int[] Pivots = new int[Math.min(M,N)+1];
        	T1 = new double[Math.max(M,N)+1];
        	//
        	// Quick return if possible
        	//
        	if ((M == 0) || (N == 0)) 
        		return Pivots; //. ->
        	J = 1;
        	while (J <= Math.min(M,N)) {
        		//
        		// Find pivot and test for singularity.
        		//
        		JP = J;
        		I = J+1;
        		while (I <= M) {
        			if (Math.abs(A[I][J]) > Math.abs(A[JP][J])) 
        				JP = I;
        			I++;
        		}
            	Pivots[J]  = JP;
            	if (A[JP][J] != 0) {
            		//
            		//Apply the interchange to rows
            		//
            		if (JP != J) {
                       for (i_= 1; i_ <= N; i_++) 
                           T1[i_] = A[J][i_];
                       for (i_= 1; i_ <= N; i_++) 
                           A[J][i_] = A[JP][i_];
                       for (i_= 1; i_ <= N; i_++) 
                    	   A[JP][i_] = T1[i_];
            		}
            		//
            		//Compute elements J+1:M of J-th column.
            		//
            		if (J < M) {
            			//
            			// CALL DSCAL( M-J, ONE / A( J, J ), A( J+1, J ), 1 )
            			//
            			JP = J+1;
            			S = 1/A[J][J];
            			for (i_ = JP; i_ <= M; i_++) {
            				A[i_][J] = S*A[i_][J];
            			}
            		}
            	}
            	if (J < Math.min(M,N)) {
            		//
            		//Update trailing submatrix.
            		//CALL DGER( M-J, N-J, -ONE, A( J+1, J ), 1, A( J, J+1 ), LDA,A( J+1, J+1 ), LDA )
            		//
            		JP = J+1;
            		I = J+1;
            		while (I <= M) {
            			S = A[I][J];
            			for (i_ = JP; i_ <= N; i_++) 
            				A[I][i_] = A[I][i_]-S*A[J][i_];
                        I++;
            		}
            	}
        		J++;
        	}
    		return Pivots; 
        }

        private static boolean InvTriangular(double[][] A, int N, boolean IsUpper, boolean IsUnitTriangular) {
        	boolean NOUNIT;
        	int I;
        	int J;
        	int JM1;
        	int JP1;
        	double V;
        	double AJJ;
        	double[] T;
        	int i_;
        	//.
        	T = new double[N+1];
        	//
        	// Test the input parameters.
        	//
        	NOUNIT = !IsUnitTriangular;
        	if (IsUpper) {
        		//
        		// Compute inverse of upper triangular matrix.
        		//
        		J = 1;
        		while (J <= N) {
        			if (NOUNIT) {
        				if (A[J][J] == 0) 
        					return false; //. ->
        				A[J][J] = 1/A[J][J];
        				AJJ = -A[J][J];
        			}
        			else 
        				AJJ = -1;
        			//
        			// Compute elements 1:j-1 of j-th column.
        			//
        			if (J > 1) {
        				JM1 = J-1;
        				for (i_ = 1; i_ <= JM1; i_++) 
        					T[i_] = A[i_][J];
        				I = 1;
        				while (I <= (J-1)) {
        					if (I < (J-1)) {
        						V = 0.0;
        						for (i_ = I+1; i_ <= JM1; i_++) 
        							V = V+A[I][i_]*T[i_];
        					}
        					else 
        						V = 0;
        					if (NOUNIT) 
        						A[I][J] = V+A[I][I]*T[I];
        					else
        						A[I][J] = V+T[I];
        					I++;
        				}
        				for (i_ = 1; i_ <= JM1; i_++) 
        					A[i_][J] = AJJ*A[i_][J];
        			}
        			J++;
        		}
        	}
        	else {
        		//
        		// Compute inverse of lower triangular matrix.
        		//
        		J = N;
        		while (J >= 1) {
        			if (NOUNIT) {
        				if (A[J][J] == 0) 
        					return false; //. ->
        				A[J][J] = 1/A[J][J];
        				AJJ = -A[J][J];
        			}
        			else
        				AJJ = -1;
        			if (J < N) {
        				//
        				// Compute elements j+1:n of j-th column.
        				//
        				JP1 = J+1;
        				for (i_ = JP1; i_ <= N; i_++) 
        					T[i_] = A[i_][J];
        				I = J+1;
        				while (I <= N) {
        					if (I > (J+1)) {
        						V = 0.0;
        						for (i_ = JP1; i_ <= (I-1); i_++) 
        							V = V+A[I][i_]*T[i_];
        					}
        					else
        						V = 0;
        					if (NOUNIT)
        						A[I][J] = V+A[I][I]*T[I];
        					else
        						A[I][J] = V+T[I];
        					I++;
        				}
        				for (i_ = JP1; i_ <= N; i_ ++)
        					A[i_][J] = AJJ*A[i_][J];
        			}
        			J--;
        		}
        	}
        	return true;
        }

        private static boolean InverseLU(double[][] A, int[] Pivots, int N) {
        	double[] WORK;
        	int I;
        	int J;
        	int JP;
        	int JP1;
        	double V;
        	int i_;
        	//
        	// Quick return if possible
        	//
        	if (N == 0) 
        		return false; //. ->
        	WORK = new double[N+1];
        	//
        	// Form inv(U)
        	//
        	if (!InvTriangular(A, N, true,false))
        			return false; //. ->
        	//
        	// Solve the equation inv(A)*L = inv(U) for inv(A).
        	//
        	J = N;
        	while (J >= 1) {
        		//
        		// Copy current column of L to WORK and replace with zeros.
        		//
        		I = J+1;
        		while (I <= N) {
        			WORK[I] = A[I][J];
        			A[I][J] = 0;
        			I++;
        		}
        		//
        		// Compute current column of inv(A).
        		//
        		if (J < N) {
        			JP1 = J+1;
        			I = 1;
        			while (I <= N) {
        				V = 0.0;
        				for (i_ = JP1; i_ <= N; i_++)
        					V = V + A[I][i_]*WORK[i_];
        				A[I][J] = A[I][J]-V;
        				I++;
        			}
        		}
        		J--;
        	}
        	//
        	// Apply column interchanges.
        	//
        	J = N-1;
        	while (J >= 1) {
        		JP = Pivots[J];
        		if (JP != J) {
        			for (i_ = 1; i_ <= N; i_++) 
        				WORK[i_] = A[i_][J];
        			for (i_ = 1; i_ <= N; i_++)
        				A[i_][J] = A[i_][JP];
        			for (i_ = 1; i_ <= N; i_++)
        				A[i_][JP] = WORK[i_];
        		}
        		J--;
        	}
        	return true;
        }

        public static boolean Inverse(double[][] A, int N) {
        	int[] Pivots = LUDecomposition(A, N, N);
            return InverseLU(A, Pivots, N);
        }
    }
    
    
    private TControlPoint[] ControlPoints;
    private TParams Params;

    public TViewTransformator(TControlPoint[] pControlPoints) {
    	ControlPoints = pControlPoints;
    	//.
    	Params = new TParams();
    }
    
    public void Polynom3RMS_UpdateParams() throws Exception { //. {GIS-Lab.info}
    	int PointsNumber;
    	double[][] XMT,XM,XMM,XMR;
    	//.
    	if (Params.Algorithm == ALGORITHM_POLYNOM3RMS) 
    		return; //. ->
    	PointsNumber = ControlPoints.length;
    	if (PointsNumber < 10) 
    		throw new Exception("TViewTransformator.Polynom3RMS_UpdateParams(): wrong number of control points"); //. =>
    	//.
    	XM = new double[PointsNumber+1][11];
    	for (int I = 1; I <= PointsNumber; I++) {
    		XM[I][1] = 1;
    		XM[I][2] = ControlPoints[I-1].E;
    		XM[I][3] = ControlPoints[I-1].N;
    		XM[I][4] = Math.pow((ControlPoints[I-1].E),2);
    		XM[I][5] = ControlPoints[I-1].E*ControlPoints[I-1].N;
    		XM[I][6] = Math.pow((ControlPoints[I-1].N),2);
    		XM[I][7] = Math.pow(ControlPoints[I-1].E,3);
    		XM[I][8] = Math.pow((ControlPoints[I-1].E),2)*ControlPoints[I-1].N;
    		XM[I][9] = ControlPoints[I-1].E*Math.pow((ControlPoints[I-1].N),2);
    		XM[I][10] = Math.pow(ControlPoints[I-1].N,3);
    	}
    	//.
    	XMT = new double [11][PointsNumber+1];
    	for (int I = 1; I <= PointsNumber; I++) 
    		for (int J = 1; J <= 10; J++)
    			XMT[J][I] = XM[I][J];
    	//. 
    	XMM = TMatrix.Multiply(10,PointsNumber,10, XMT,XM);
    	//.
    	if (!TMatrix.Inverse(XMM,10)) 
    		throw new Exception("TViewTransformator.Polynom3RMS_UpdateParams(): invalid control points"); //. =>
    	//.
    	XMR = TMatrix.Multiply(10,10,PointsNumber, XMM,XMT);
    	//.
    	Params.X = new double[11]; 
    	for (int I = 1; I <= 10; I++) 
    		Params.X[I] = 0;
    	for (int I = 1; I <= 10; I++)
    	      for (int J = 1; J <= PointsNumber; J++) 
    	    	  Params.X[I] = Params.X[I]+XMR[I][J]*ControlPoints[J-1].X;
    	//.
    	Params.Y = new double[11]; 
    	for (int I = 1; I <= 10; I++) 
    		Params.Y[I] = 0;
    	for (int I = 1; I <= 10; I++)
  	      for (int J = 1; J <= PointsNumber; J++) 
  	    	  Params.Y[I] = Params.Y[I]+XMR[I][J]*ControlPoints[J-1].Y;
    	//.
    	XM = new double[PointsNumber+1][11];
    	for (int I = 1; I <= PointsNumber; I++) {
    		XM[I][1] = 1;
    		XM[I][2] = ControlPoints[I-1].X;
    		XM[I][3] = ControlPoints[I-1].Y;
    		XM[I][4] = Math.pow(ControlPoints[I-1].X,2);
    		XM[I][5] = ControlPoints[I-1].X*ControlPoints[I-1].Y;
    		XM[I][6] = Math.pow(ControlPoints[I-1].Y,2);
    		XM[I][7] = Math.pow(ControlPoints[I-1].X,3);
    		XM[I][8] = Math.pow(ControlPoints[I-1].X,2)*ControlPoints[I-1].Y;
    		XM[I][9] = ControlPoints[I-1].X*Math.pow(ControlPoints[I-1].Y,2);
    		XM[I][10] = Math.pow(ControlPoints[I-1].Y,3);
    	}
    	//.
    	XMT = new double[11][PointsNumber+1];
    	for (int I = 1; I <= PointsNumber; I++)
    	      for (int J = 1; J <= 10; J++) 
    	    	  XMT[J][I] = XM[I][J]; 
    	//.
    	XMM = TMatrix.Multiply(10,PointsNumber,10, XMT,XM);
    	//.
    	if (!TMatrix.Inverse(XMM,10)) 
    		throw new Exception("TViewTransformator.Polynom3RMS_UpdateParams(): invalid control points"); //. =>
    	//.
    	XMR = TMatrix.Multiply(10,10,PointsNumber, XMM,XMT);
    	//.
    	Params.E = new double[11]; 
    	for (int I = 1; I <= 10; I++)
    		Params.E[I] = 0;
    	for (int I = 1; I <= 10; I++)
    		for (int J = 1; J <= PointsNumber; J++)
    			Params.E[I] = Params.E[I]+XMR[I][J]*ControlPoints[J-1].E;
    	//.
    	Params.N = new double[11]; 
    	for (int I = 1; I <= 10; I++)
    		Params.N[I] = 0;
    	for (int I = 1; I <= 10; I++)
    		for (int J = 1; J <= PointsNumber; J++)
    			Params.N[I] = Params.N[I]+XMR[I][J]*ControlPoints[J-1].N;
    	//.
    	Params.Algorithm = ALGORITHM_POLYNOM3RMS;
    }

    public TXYCoord Polynom3RMS_Transform(double E, double N) throws Exception {
    	Polynom3RMS_UpdateParams();
    	//.
    	double X = Params.X[1]+Params.X[2]*E+Params.X[3]*N+Params.X[4]*Math.pow(E,2)+Params.X[5]*E*N+Params.X[6]*Math.pow(N,2)+Params.X[7]*Math.pow(E,3)+Params.X[8]*Math.pow(E,2)*N+Params.X[9]*E*Math.pow(N,2)+Params.X[10]*Math.pow(N,3);
    	double Y = Params.Y[1]+Params.Y[2]*E+Params.Y[3]*N+Params.Y[4]*Math.pow(E,2)+Params.Y[5]*E*N+Params.Y[6]*Math.pow(N,2)+Params.Y[7]*Math.pow(E,3)+Params.Y[8]*Math.pow(E,2)*N+Params.Y[9]*E*Math.pow(N,2)+Params.Y[10]*Math.pow(N,3);
    	//.
    	return new TXYCoord(X,Y);
    }

    public TXYCoord Polynom3RMS_InverseTransform(double X, double Y) throws Exception {
    	Polynom3RMS_UpdateParams();
    	//.
    	double E = Params.E[1]+Params.E[2]*X+Params.E[3]*Y+Params.E[4]*Math.pow(X,2)+Params.E[5]*X*Y+Params.E[6]*Math.pow(Y,2)+Params.E[7]*Math.pow(X,3)+Params.E[8]*Math.pow(X,2)*Y+Params.E[9]*X*Math.pow(Y,2)+Params.E[10]*Math.pow(Y,3);
    	double N = Params.N[1]+Params.N[2]*X+Params.N[3]*Y+Params.N[4]*Math.pow(X,2)+Params.N[5]*X*Y+Params.N[6]*Math.pow(Y,2)+Params.N[7]*Math.pow(X,3)+Params.N[8]*Math.pow(X,2)*Y+Params.N[9]*X*Math.pow(Y,2)+Params.N[10]*Math.pow(Y,3);
    	//.
    	return new TXYCoord(E,N);
    }

    public TXYCoord Polynom3RMS_InverseTransformWithCorrection(double X, double Y) throws Exception {
    	TXYCoord Result = Polynom3RMS_InverseTransform(X,Y);
    	TXYCoord _C = Polynom3RMS_Transform(Result.X,Result.Y);
    	X = X+(X-_C.X);
    	Y = Y+(Y-_C.Y);
    	Result = Polynom3RMS_InverseTransform(X,Y); 
    	//.
    	return Result;
    }
}
