package com.geoscope.GeoLog.COMPONENT;

public class TComponentItem extends TComponentElement {

	public TComponentItem() {
		super();
	}
	
	public TComponentItem(TComponent pOwner, int pID, String pName)
	{
		super(pOwner,pID,pName);
		//.
		if (Owner != null)
			Owner.AddItem(this);
	}	
}
