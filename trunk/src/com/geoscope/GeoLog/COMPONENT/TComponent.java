package com.geoscope.GeoLog.COMPONENT;

import java.util.ArrayList;

public class TComponent extends TComponentElement {

	private ArrayList<TComponentElement> Elements = null;
	
	public void AddElement(TComponentElement pElement) {
		if (Elements == null)
			Elements = new ArrayList<TComponentElement>();
		Elements.add(pElement);
	}
	
	public void RemoveElement(TComponentElement pElement) {
		Elements.remove(pElement);
	}
}
