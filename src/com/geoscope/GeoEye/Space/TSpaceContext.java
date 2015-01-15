package com.geoscope.GeoEye.Space;

import java.util.Hashtable;

import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TSpaceObj;

public class TSpaceContext {

	public TSpace Space;
	//.
	public TSpaceContextStorage Storage;
	//.
	public Hashtable<Long, TSpaceObj> 					SpaceObjects;
	public Hashtable<Integer, Hashtable<Long, Long>>	SpaceObjectsPtrs;
	//.
	public TSpaceContext(TSpace pSpace) {
		Space = pSpace;
		//.
		Storage = new TSpaceContextStorage();
		//.
		SpaceObjects = new Hashtable<Long, TSpaceObj>();
		SpaceObjectsPtrs = new Hashtable<Integer, Hashtable<Long,Long>>();
	}

	public void SpaceObjects_Set(TSpaceObj Obj) {
		SpaceObjectsPtrs_Set(Obj.idTObj,Obj.idObj, Obj.ptrObj);
		//.
		synchronized (SpaceObjects) {
			SpaceObjects.put(Obj.ptrObj, Obj);
		}
	}

	public void SpaceObjects_Remove(TSpaceObj Obj) {
		SpaceObjectsPtrs_Remove(Obj.idTObj,Obj.idObj);
		//.
		synchronized (SpaceObjects) {
			SpaceObjects.remove(Obj.ptrObj);
		}
	}

	public TSpaceObj SpaceObjects_Get(long Ptr) {
		synchronized (SpaceObjects) {
			return SpaceObjects.get(Ptr);
		}
	}

	public TSpaceObj SpaceObjects_Get(int idTVisualization, long idVisualization) {
		long Ptr = SpaceObjectsPtrs_Get(idTVisualization,idVisualization);
		if (Ptr == SpaceDefines.nilPtr)
			return null; //. ->
		//.
		synchronized (SpaceObjects) {
			return SpaceObjects.get(Ptr);
		}
	}

	public void SpaceObjectsPtrs_Set(int idTVisualization, long idVisualization, long Ptr) {
		synchronized (SpaceObjectsPtrs) {
			Hashtable<Long, Long> InstancesTable = SpaceObjectsPtrs.get(idTVisualization);
			if (InstancesTable == null) {
				InstancesTable = new Hashtable<Long, Long>();
				SpaceObjectsPtrs.put(idTVisualization, InstancesTable);			
			}
			InstancesTable.put(idVisualization, Ptr);
		}
	}
	
	public void SpaceObjectsPtrs_Remove(int idTVisualization, long idVisualization) {
		synchronized (SpaceObjectsPtrs) {
			Hashtable<Long, Long> InstancesTable = SpaceObjectsPtrs.get(idTVisualization);
			if (InstancesTable == null) 
				return; //. ->
			InstancesTable.remove(idVisualization);
		}
	}
	
	public long SpaceObjectsPtrs_Get(int idTVisualization, long idVisualization) {
		synchronized (SpaceObjectsPtrs) {
			Hashtable<Long, Long> InstancesTable = SpaceObjectsPtrs.get(idTVisualization);
			if (InstancesTable == null)
				return SpaceDefines.nilPtr; //. ->
			Long Result = InstancesTable.get(idVisualization);
			if (Result == null)
				return SpaceDefines.nilPtr; //. ->
			else
				return Result; //. ->
		}
	}
}
