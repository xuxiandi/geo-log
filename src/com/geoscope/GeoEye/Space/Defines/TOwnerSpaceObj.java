package com.geoscope.GeoEye.Space.Defines;

import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;


public class TOwnerSpaceObj extends TSpaceObj {
	
	public int 		OwnerType = 0;
	public long 	OwnerID = 0;
	//.
	public int OwnerCoType = 0;
	//.
	public TComponentTypedDataFiles OwnerTypedDataFiles;

	public TOwnerSpaceObj() {
		super();
	}
	
	public TOwnerSpaceObj(TSpaceObj pObj) {
		super(pObj);
	}
	
	public TOwnerSpaceObj(long pptrObj) {
		super(pptrObj);
	}
	
	public TOwnerSpaceObj(long pptrObj, TXYCoord[] pNodes) {
		super(pptrObj, pNodes);
	}
}
