package com.geoscope.GeoEye.Space.TypesSystem.Visualizations.Reflections;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.Defines.TReflectionWindowStruc;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;

public class TSpaceReflections {

	public static final String Folder = TReflector.SpaceContextFolder+"/"+"Reflections";
	public static final String StorageFile = Folder+"/"+"Reflections.dat";
	public static final int MaxItemsCount = 100; 
	public static final int MaxCachedItemsCount = 20;
	public static final int Composition_InitialSize = 2;
	public static final int Composition_MaxSize = 4;
	
	public TReflector Reflector;
	//.
	public TSpaceReflection 		Items;
	public int						ItemsCount;
	public int						ItemsCachedCount;
	public boolean flChanged;
	public TSpaceReflection[]		Composition;
	public int						CompositionItemsCount;
	
	public TSpaceReflections(TReflector pReflector) throws IOException {
		Reflector = pReflector;
		//.
		File F = new File(Folder);
		if (!F.exists()) 
			F.mkdirs();
		//.
		Items = null;
		ItemsCount = 0;
		ItemsCachedCount = 0;
		flChanged = false;
		//.
		Composition = new TSpaceReflection[Composition_MaxSize];
		CompositionItemsCount = 0;
		//.
		Load();
	}
	
	public void Destroy() throws IOException {
		Save();
	}
	
	public synchronized void Load() throws IOException {
		Items = null;
		ItemsCount = 0;
		TSpaceReflection LastItem = null;
		//.
		File F = new File(StorageFile);
		if (F.exists()) { 
	    	long FileSize = F.length();
	    	ItemsCount = (int)(FileSize/TSpaceReflection.ByteArraySize());
	    	FileInputStream FIS = new FileInputStream(StorageFile);
	    	try {
	        		byte[] ItemData = new byte[TSpaceReflection.ByteArraySize()];
	        		for (int I = 0; I < ItemsCount; I++) {
						FIS.read(ItemData);
	        			TSpaceReflection NewItem = new TSpaceReflection();
	        			NewItem.FromByteArray(ItemData,0);
	        			//. insert into queue
	        			if (LastItem != null)
	        				LastItem.Next = NewItem;
	        			else 
	        				Items = NewItem;
	        			LastItem = NewItem;
	        		}
	    	}
			finally {
				FIS.close(); 
			}
		}
		flChanged = false;
	}
	
	public synchronized void Save() throws IOException {
		if (flChanged) {
			FileOutputStream FOS = new FileOutputStream(StorageFile);
	        try
	        {
	        	byte[] ItemData = new byte[TSpaceReflection.ByteArraySize()];
	        	TSpaceReflection Item = Items;
	        	while (Item != null) {
	        		Item.ToByteArray(ItemData,0);
	    			FOS.write(ItemData);
	    			//.
	    			Item = Item.Next;
	        	}
	        }
	        finally {
	        	FOS.close();
	        }
	        flChanged = false;
		}
	}
	
	public synchronized void Clear() throws IOException {
    	TSpaceReflection Item = Items;
    	while (Item != null) {
			Item.FreeDataFile();
			Item.DeleteDataFile();
			//.
			Item = Item.Next;
    	}
		Items = null;
		ItemsCount = 0;
		ItemsCachedCount = 0;
		flChanged = true;
		//.
		Save();
	}
	
	public synchronized void AddReflection(TSpaceReflection Reflection) throws IOException {
		RemoveOldReflections();
		//.
		try {
			TSpaceReflection LastItem = null;
	    	TSpaceReflection Item = Items;
	    	while (Item != null) {
	    		if (Item.Window.IsEqualTo(Reflection.Window))
	    		{
	    			boolean flWasCached = Item.IsCached(); 
	    			Item.AssignData(Reflection.TimeStamp,Reflection.Data_Bitmap);
	    			Item.SaveDataFile();
	    			if (!flWasCached && Item.IsCached())
	    				ItemsCachedCount++;
	    			//. insert item first
	    			if (LastItem != null) {
	    				LastItem.Next = Item.Next;
	    				//.
	    				Item.Next = Items;
	    				Items = Item;
	    			}
	    			return; //. ->
	    		}
				//.
	    		LastItem = Item;
				Item = Item.Next;
	    	}
			//.	
			Reflection.Next = Items;
			Items = Reflection;
			ItemsCount++;
			Reflection.SaveDataFile();
			if (Reflection.IsCached())
				ItemsCachedCount++;
			//.
			flChanged = true;
		}
		finally {
			//. add reflection to the composition
			if (CompositionItemsCount < Composition_MaxSize) {
				Composition[CompositionItemsCount] = Reflection;
				CompositionItemsCount++;
			}
			else
				Composition[CompositionItemsCount-1] = Reflection;
		}
	}
	
	public synchronized void CacheReflectionsSimilarTo(TReflectionWindowStruc RW) throws IOException {
		//. uncache some items
		FreeCachedReflectionsFarTo(RW); 
		//. caching items nearest to RW
		CompositionItemsCount = 0;
		CompositionItemsCount = GetReflectionsSimilarTo(RW,false,Composition,CompositionItemsCount,Composition_InitialSize); 
		for (int I = 0; I < CompositionItemsCount; I++) 
			if (Composition[I].CacheDataFile())
				ItemsCachedCount++;
	}
	
	public synchronized void FreeCachedReflectionsFarTo(TReflectionWindowStruc RW) {
		if ((ItemsCachedCount-MaxCachedItemsCount) <= 0)
			return; //. ->
		//.
		int IC = MaxCachedItemsCount;
    	TSpaceReflection Item = Items;
    	while (Item != null) {
    		if (Item.IsCached() && (IC > 0) && Item.Window.Container_VisibleInContainerOf(RW)) {
    			Item.RefCount = 1;
    			//.
    			IC--;
    		}
    		else
        		Item.RefCount = 0;
			//.
			Item = Item.Next;
    	}
    	//.
    	Item = Items;
    	while (Item != null) {
    		if (Item.RefCount == 0) {
    			//. free item
    			if (Item.FreeDataFile()) {
    				ItemsCachedCount--;
    			}
    		}
			//.
			Item = Item.Next;
    	}
	}
	
	public synchronized int GetCachedItemsCount() {
		return ItemsCachedCount;
	}
	
	public synchronized void RemoveOldReflections() {
		if (ItemsCount < (1.1*MaxItemsCount))
			return; //. ->
		TSpaceReflection LastItem = null;
    	TSpaceReflection Item = Items;
    	for (int I = 0; I < ItemsCount; I++) {
    		if (I >= MaxItemsCount) {
    			LastItem.Next = null;
    			do {
    				if (Item.IsCached() && Item.FreeDataFile())
    					ItemsCachedCount--;
    				Item.DeleteDataFile();
    				//.
    				Item = Item.Next;
    			} while (Item != null);
    			ItemsCount = MaxItemsCount;
    			flChanged = true;
    			return; //. ->
    		}
			//.
    		LastItem = Item;
			Item = Item.Next;
    	}
	}
	
	/*///? private TSpaceReflection[] GetReflectionsSimilarTo(TReflectionWindowStruc RW, boolean flCachedOnly, boolean flOrganize) {
		final int DivX = 8;
		final int DivY = 8;
		int Count = 10; 
		//.
		TSpaceReflection[][] SegmentReflectionMap = new TSpaceReflection[DivX][DivY]; 
		boolean[][] SegmentItemMap = new boolean[DivX][DivY]; 
		double[][] SegmentReflectionFactorMap = new double[DivX][DivY];
		TSpaceReflection[] RefSortedReflections = new TSpaceReflection[Count];
		TSpaceReflection[] Reflections = new TSpaceReflection[Count];
		double[] ReflectionsContainerSquares = new double[Count];
    	TSpaceReflection Reflection;
		//.
		double SegmentWidth_dX = (RW.X1-RW.X0)/DivX;
		double SegmentWidth_dY = (RW.Y1-RW.Y0)/DivX;
		double SegmentHeight_dX = (RW.X3-RW.X0)/DivY;
		double SegmentHeight_dY = (RW.Y3-RW.Y0)/DivY;
		double Segment_BaseX0,Segment_BaseY0;
		double[][] Segment_X0,Segment_Y0,Segment_X1,Segment_Y1,Segment_X2,Segment_Y2,Segment_X3,Segment_Y3;
		double[][] SegmentContainer_Xmin,SegmentContainer_Xmax,SegmentContainer_Ymin,SegmentContainer_Ymax;
		Segment_X0 = new double[DivX][DivY]; Segment_Y0 = new double[DivX][DivY]; Segment_X1 = new double[DivX][DivY]; Segment_Y1 = new double[DivX][DivY]; Segment_X2 = new double[DivX][DivY]; Segment_Y2 = new double[DivX][DivY]; Segment_X3 = new double[DivX][DivY]; Segment_Y3 = new double[DivX][DivY];
		SegmentContainer_Xmin = new double[DivX][DivY]; SegmentContainer_Xmax = new double[DivX][DivY]; SegmentContainer_Ymin = new double[DivX][DivY]; SegmentContainer_Ymax = new double[DivX][DivY];
		Segment_BaseX0 = RW.X0;
		Segment_BaseY0 = RW.Y0;
		for (int Y = 0; Y < DivY; Y++) {
			Segment_X0[0][Y] = Segment_BaseX0; Segment_Y0[0][Y] = Segment_BaseY0;    				
			Segment_X1[0][Y] = Segment_X0[0][Y]+SegmentWidth_dX; Segment_Y1[0][Y] = Segment_Y0[0][Y]+SegmentWidth_dY;
			Segment_X2[0][Y] = Segment_X0[0][Y]+SegmentWidth_dX+SegmentHeight_dX; Segment_Y2[0][Y] = Segment_Y0[0][Y]+SegmentWidth_dY+SegmentHeight_dY;
			Segment_X3[0][Y] = Segment_X0[0][Y]+SegmentHeight_dX; Segment_Y3[0][Y] = Segment_Y0[0][Y]+SegmentHeight_dY;
			int X = 0;
			while (true) {
				//. computing segment container
				SegmentContainer_Xmin[X][Y] = Segment_X0[X][Y];
				SegmentContainer_Ymin[X][Y] = Segment_Y0[X][Y];
				SegmentContainer_Xmax[X][Y] = Segment_X0[X][Y];
				SegmentContainer_Ymax[X][Y] = Segment_Y0[X][Y];
				if (Segment_X1[X][Y] < SegmentContainer_Xmin[X][Y]) SegmentContainer_Xmin[X][Y] = Segment_X1[X][Y]; else if (Segment_X1[X][Y] > SegmentContainer_Xmax[X][Y]) SegmentContainer_Xmax[X][Y] = Segment_X1[X][Y];
				if (Segment_Y1[X][Y] < SegmentContainer_Ymin[X][Y]) SegmentContainer_Ymin[X][Y] = Segment_Y1[X][Y]; else if (Segment_Y1[X][Y] > SegmentContainer_Ymax[X][Y]) SegmentContainer_Ymax[X][Y] = Segment_Y1[X][Y];
				if (Segment_X2[X][Y] < SegmentContainer_Xmin[X][Y]) SegmentContainer_Xmin[X][Y] = Segment_X2[X][Y]; else if (Segment_X2[X][Y] > SegmentContainer_Xmax[X][Y]) SegmentContainer_Xmax[X][Y] = Segment_X2[X][Y];
				if (Segment_Y2[X][Y] < SegmentContainer_Ymin[X][Y]) SegmentContainer_Ymin[X][Y] = Segment_Y2[X][Y]; else if (Segment_Y2[X][Y] > SegmentContainer_Ymax[X][Y]) SegmentContainer_Ymax[X][Y] = Segment_Y2[X][Y];
				if (Segment_X3[X][Y] < SegmentContainer_Xmin[X][Y]) SegmentContainer_Xmin[X][Y] = Segment_X3[X][Y]; else if (Segment_X3[X][Y] > SegmentContainer_Xmax[X][Y]) SegmentContainer_Xmax[X][Y] = Segment_X3[X][Y];
				if (Segment_Y3[X][Y] < SegmentContainer_Ymin[X][Y]) SegmentContainer_Ymin[X][Y] = Segment_Y3[X][Y]; else if (Segment_Y3[X][Y] > SegmentContainer_Ymax[X][Y]) SegmentContainer_Ymax[X][Y] = Segment_Y3[X][Y];
				//.
				X++;
				if (X >= DivX)
					break; //. >
				//.
				Segment_X0[X][Y] = Segment_X1[X-1][Y]; Segment_Y0[X][Y] = Segment_Y1[X-1][Y];
				Segment_X3[X][Y] = Segment_X2[X-1][Y]; Segment_Y3[X][Y] = Segment_Y2[X-1][Y];
				Segment_X1[X][Y] = Segment_X1[X-1][Y]+SegmentWidth_dX; Segment_Y1[X][Y] = Segment_Y1[X-1][Y]+SegmentWidth_dY;
				Segment_X2[X][Y] = Segment_X2[X-1][Y]+SegmentWidth_dX; Segment_Y2[X][Y] = Segment_Y2[X-1][Y]+SegmentWidth_dY;
			}
			Segment_BaseX0 = Segment_BaseX0+SegmentHeight_dX;
			Segment_BaseY0 = Segment_BaseY0+SegmentHeight_dY;
		}
		//.
    	TSpaceReflection Item = Items;
    	while (Item != null) {
			Item.RefCount = 0;
    		if (((!flCachedOnly) || Item.IsCached()) && Item.Window.Container_VisibleInContainerOf(RW)) {
    			//. process for item map
    			for (int Y = 0; Y < DivY; Y++) 
    				for (int X = 0; X < DivX; X++) 
    					if (Item.Window.Container_VisibleInContainer(SegmentContainer_Xmin[X][Y],SegmentContainer_Xmax[X][Y], SegmentContainer_Ymin[X][Y],SegmentContainer_Ymax[X][Y]) && Item.Window.AllPolygonNodesAreVisible(new double[] {Segment_X0[X][Y],Segment_Y0[X][Y],Segment_X1[X][Y],Segment_Y1[X][Y],Segment_X2[X][Y],Segment_Y2[X][Y],Segment_X3[X][Y],Segment_Y3[X][Y]})) {
    						Item.RefCount++;
    						SegmentItemMap[X][Y] = true;
						}
						else 
							SegmentItemMap[X][Y] = false;
    			//. process result map using item map
    			if (Item.RefCount > 0) {
        			double ItemFactor = Item.Window.Container_S/Item.RefCount;
        			for (int Y = 0; Y < DivY; Y++) 
        				for (int X = 0; X < DivX; X++) 
        					if (SegmentItemMap[X][Y]) {
        						Reflection = SegmentReflectionMap[X][Y];
        						if (Reflection != null) {
        							if (ItemFactor < SegmentReflectionFactorMap[X][Y]) {
        								Reflection.RefCount--;
        								//.	
            							SegmentReflectionFactorMap[X][Y] = ItemFactor;
            							SegmentReflectionMap[X][Y] = Item;    								
        							}
        							else
        								Item.RefCount--;
        						}
        						else {
        							SegmentReflectionFactorMap[X][Y] = ItemFactor;
        							SegmentReflectionMap[X][Y] = Item;    								
        						}
        					}
    			}
    		}
			//.
			Item = Item.Next;
    	}
    	//. sort items by RefCount;
    	Item = Items;
    	while (Item != null) {
    		if (Item.RefCount > 0) {
        		for (int I = 0; I < Count; I++) {
    	    		if (RefSortedReflections[I] != null) {
    	    			Reflection = RefSortedReflections[I]; 
    	    			if ((Item.RefCount > Reflection.RefCount) || ((Item.RefCount == Reflection.RefCount) && (Item.Window.Container_S < Reflection.Window.Container_S))) {
    	    			    //. shift right
    	    			    for (int J = (Count-1); J > I; J--) 
    	    			    	RefSortedReflections[J] = RefSortedReflections[J-1];
    	    			    //.
    	    			    RefSortedReflections[I] = Item;
    						break; //. >	    				  
    	    			}
        			}
        			else {
        				RefSortedReflections[I] = Item;
    					break; //. >	    				  
        			}
        		}
    		}
			//.
			Item = Item.Next;
    	}
    	//. organizing items for optimal drawing
    	double Container_S;
    	if (flOrganize) {
    		for (int K = 0; K < Count; K++) {
    			if (RefSortedReflections[K] == null)
    				break; //. >
				Container_S = RefSortedReflections[K].Window.Container_S; 
        		for (int I = 0; I < Count; I++) {
    	    		if (Reflections[I] != null) {
    	    			if (Container_S < ReflectionsContainerSquares[I]) {
    	    			    //. shift right
    	    			    for (int J = (Count-1); J > I; J--) {
    	    			    	Reflections[J] = Reflections[J-1];
    	    			    	ReflectionsContainerSquares[J] = ReflectionsContainerSquares[J-1];  
    	    			    }
    	    			    //.
    	    				Reflections[I] = RefSortedReflections[K];
    						ReflectionsContainerSquares[I] = Container_S;
    						break; //. >	    				  
    	    			}
        			}
        			else {
    	    			Reflections[I] = RefSortedReflections[K];
    					ReflectionsContainerSquares[I] = Container_S;
    					break; //. >	    				  
        			}
        		}
    		}
    	}
    	else 
    		Reflections = RefSortedReflections;
    	return Reflections;
	}*/
	
	private int GetReflectionsSimilarTo(TReflectionWindowStruc RW, boolean flCachedOnly, TSpaceReflection[] Reflections, int ReflectionsCount, int ReflectionsMaxCount) {
		final int DivX = 8;
		final int DivY = 8;
		//.
		TSpaceReflection[][] SegmentReflectionMap = new TSpaceReflection[DivX][DivY]; 
		boolean[][] SegmentItemMap = new boolean[DivX][DivY]; 
		double[][] SegmentReflectionFactorMap = new double[DivX][DivY];
		TSpaceReflection[] RefSortedReflections = new TSpaceReflection[ReflectionsMaxCount-ReflectionsCount];
		double[] ReflectionsContainerSquares = new double[Reflections.length];
    	TSpaceReflection Reflection;
		//.
		double SegmentWidth_dX = (RW.X1-RW.X0)/(DivX-1);
		double SegmentWidth_dY = (RW.Y1-RW.Y0)/(DivX-1);
		double SegmentHeight_dX = (RW.X3-RW.X0)/(DivY-1);
		double SegmentHeight_dY = (RW.Y3-RW.Y0)/(DivY-1);
		double Segment_BaseX0,Segment_BaseY0;
		double[][] Segment_X0,Segment_Y0;
		Segment_X0 = new double[DivX][DivY]; Segment_Y0 = new double[DivX][DivY];
		Segment_BaseX0 = RW.X0;
		Segment_BaseY0 = RW.Y0;
		for (int Y = 0; Y < DivY; Y++) {
			Segment_X0[0][Y] = Segment_BaseX0; 
			Segment_Y0[0][Y] = Segment_BaseY0;
			for (int X = 1; X < DivX; X++) {
				Segment_X0[X][Y] = Segment_X0[X-1][Y]+SegmentWidth_dX;
				Segment_Y0[X][Y] = Segment_Y0[X-1][Y]+SegmentWidth_dY;
			}
			Segment_BaseX0 = Segment_BaseX0+SegmentHeight_dX;
			Segment_BaseY0 = Segment_BaseY0+SegmentHeight_dY;
		}
		//.
    	TSpaceReflection Item = Items;
    	while (Item != null) {
			Item.RefCount = 0; 
    		if (((!flCachedOnly) || Item.IsCached()) && Item.Window.Container_VisibleInContainerOf(RW)) {
    			//. process for item map
    			for (int Y = 0; Y < DivY; Y++) 
    				for (int X = 0; X < DivX; X++) 
    					if (Item.Window.NodeIsVisible(Segment_X0[X][Y],Segment_Y0[X][Y])) {
    						Item.RefCount++;
    						SegmentItemMap[X][Y] = true;
						}
						else 
							SegmentItemMap[X][Y] = false;
    			//. process result map using item map
    			if (Item.RefCount > 0) {
        			double ItemFactor = Item.Window.Container_S/Item.RefCount;
        			for (int Y = 0; Y < DivY; Y++) 
        				for (int X = 0; X < DivX; X++) 
        					if (SegmentItemMap[X][Y]) {
        						Reflection = SegmentReflectionMap[X][Y];
        						if (Reflection != null) {
        							if (ItemFactor < SegmentReflectionFactorMap[X][Y]) {
        								Reflection.RefCount--;
        								//.	
            							SegmentReflectionFactorMap[X][Y] = ItemFactor;
            							SegmentReflectionMap[X][Y] = Item;    								
        							}
        							else
        								Item.RefCount--;
        						}
        						else {
        							SegmentReflectionFactorMap[X][Y] = ItemFactor;
        							SegmentReflectionMap[X][Y] = Item;    								
        						}
        					}
    			}
    		}
			//.
			Item = Item.Next;
    	}
    	//. sort items by RefCount;
    	Item = Items;
    	while (Item != null) {
    		if (Item.RefCount > 0) {
    			boolean flSkipItem = false;
    			for (int I = 0; I < ReflectionsCount; I++) 
    				if (Reflections[I] == Item) {
    					flSkipItem = true;
    					break; //. >
    				}
    			if (!flSkipItem) {
            		for (int I = 0; I < RefSortedReflections.length; I++) {
        	    		if (RefSortedReflections[I] != null) {
        	    			Reflection = RefSortedReflections[I]; 
        	    			if ((Item.RefCount > Reflection.RefCount) || ((Item.RefCount == Reflection.RefCount) && (Item.Window.Container_S < Reflection.Window.Container_S))) {
        	    			    //. shift right
        	    			    for (int J = (RefSortedReflections.length-1); J > I; J--) 
        	    			    	RefSortedReflections[J] = RefSortedReflections[J-1];
        	    			    //.
        	    			    RefSortedReflections[I] = Item;
        						break; //. >	    				  
        	    			}
            			}
            			else {
            				RefSortedReflections[I] = Item;
        					break; //. >	    				  
            			}
            		}
    			}
    		}
			//.
			Item = Item.Next;
    	}
    	//. organizing items for optimal drawing
    	double Container_S;
    	int Origin = ReflectionsCount;
		for (int K = 0; K < RefSortedReflections.length; K++) {
			if (RefSortedReflections[K] == null)
				break; //. >
			Container_S = RefSortedReflections[K].Window.Container_S;
			if (Origin != ReflectionsCount) {
	    		for (int I = Origin; I < ReflectionsMaxCount; I++) {
		    		if (Reflections[I] != null) {
		    			if (Container_S > ReflectionsContainerSquares[I]) {
							ReflectionsCount++;
		    			    //. shift right
		    			    for (int J = (ReflectionsCount-1); J > I; J--) {
		    			    	Reflections[J] = Reflections[J-1];
		    			    	ReflectionsContainerSquares[J] = ReflectionsContainerSquares[J-1];  
		    			    }
		    			    //.
		    				Reflections[I] = RefSortedReflections[K];
							ReflectionsContainerSquares[I] = Container_S;
							break; //. >	    				  
		    			}
	    			}
	    			else {
		    			Reflections[I] = RefSortedReflections[K];
						ReflectionsContainerSquares[I] = Container_S;
						ReflectionsCount++;
						break; //. >	    				  
	    			}
	    		}
			}
    		else {
    			Reflections[Origin] = RefSortedReflections[K];
				ReflectionsContainerSquares[Origin] = Container_S;
				ReflectionsCount++;
			}
			
		}
		return ReflectionsCount;
	}
	
	public synchronized void ReflectionWindow_DrawOnCanvas(TReflectionWindowStruc RW, Canvas canvas, Paint paint) {
		double dX,dY;
    	double Scale;
    	double Angle;
    	TXYCoord Reflection_Window_Pmd; 
		float Xmd = (RW.Xmx-RW.Xmn)/2;
		float Ymd = (RW.Ymx-RW.Ymn)/2;
    	//.
		int _CompositionItemsCount = CompositionItemsCount;
		/*///- if (CompositionItemsCount < Composition_MaxSize) {
			_CompositionItemsCount = GetReflectionsSimilarTo(RW,true,Composition,CompositionItemsCount,Composition_MaxSize);
			if (_CompositionItemsCount > CompositionItemsCount) {
				CompositionItemsCount++;
				_CompositionItemsCount = CompositionItemsCount;
			}
		}*/
		//.
		Matrix Transformatrix = new Matrix();
    	TSpaceReflection Item;
    	for (int I = 0; I < _CompositionItemsCount; I++) {
			Item = Composition[I]; 
			if (Item.Data_Bitmap != null) {
	    		Reflection_Window_Pmd = RW.ConvertToScreen((Item.Window.X0+Item.Window.X2)/2.0,(Item.Window.Y0+Item.Window.Y2)/2.0);
	    		dX = Reflection_Window_Pmd.X-Xmd;
	        	dY = Reflection_Window_Pmd.Y-Ymd;
	        	//.
	        	Scale = Math.sqrt((Math.pow((Item.Window.X1-Item.Window.X0),2)+Math.pow((Item.Window.Y1-Item.Window.Y0),2))/(Math.pow((RW.X1-RW.X0),2)+Math.pow((RW.Y1-RW.Y0),2)));
	        	//.
	        	double Alpha;
	    		if ((Item.Window.X1-Item.Window.X0) != 0)
	    		{
	    			Alpha = Math.atan((Item.Window.Y1-Item.Window.Y0)/(Item.Window.X1-Item.Window.X0));
	    			if (((Item.Window.X1-Item.Window.X0) < 0) && ((Item.Window.Y1-Item.Window.Y0) > 0)) Alpha = Alpha+Math.PI; else
	    				if (((Item.Window.X1-Item.Window.X0) < 0) && ((Item.Window.Y1-Item.Window.Y0) < 0)) Alpha = Alpha+Math.PI; else
	    				if (((Item.Window.X1-Item.Window.X0) > 0) && ((Item.Window.Y1-Item.Window.Y0) < 0)) Alpha = Alpha+2*Math.PI;
	    		}
	    		else
	    		{
	    			if ((Item.Window.Y1-Item.Window.Y0) >= 0) Alpha = Math.PI/2; else Alpha = -Math.PI/2;
	    		};
	        	double Betta;
	    		if ((RW.X1-RW.X0) != 0)
	    		{
	    			Betta = Math.atan((RW.Y1-RW.Y0)/(RW.X1-RW.X0));
	    			if (((RW.X1-RW.X0) < 0) && ((RW.Y1-RW.Y0) > 0)) Betta = Betta+Math.PI; else
	    				if (((RW.X1-RW.X0) < 0) && ((RW.Y1-RW.Y0) < 0)) Betta = Betta+Math.PI; else
	    				if (((RW.X1-RW.X0) > 0) && ((RW.Y1-RW.Y0) < 0)) Betta = Betta+2*Math.PI;
	    		}
	    		else
	    		{
	    			if ((RW.Y1-RW.Y0) >= 0) Betta = Math.PI/2; else Betta = -Math.PI/2;
	    		};
	    		Angle = (Betta-Alpha);
	    		//.
	    		Transformatrix.reset();
	    		Transformatrix.postRotate((float)(Angle*180.0/Math.PI),Xmd,Ymd);
	    		Transformatrix.postScale((float)(Scale),(float)(Scale),Xmd,Ymd);
	    		Transformatrix.postTranslate((float)dX,(float)dY);
	    		//.
	    		canvas.save();
	    		try {
		    		canvas.concat(Transformatrix);
					canvas.drawBitmap(Item.Data_Bitmap, 0,0, paint);
	    		}
	    		finally {
	    			canvas.restore();
	    		}
			}
    	}
	}
}
