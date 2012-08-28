package com.geoscope.GeoLog.COMPONENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TComponent extends TComponentElement {

	public ArrayList<TComponent> 			Components = null;
	private ArrayList<TComponentElement> 	Items = null;
	
	public TComponent() {
		super();
	}

	public TComponent(TComponent pOwner, int pID, String pName)
	{
		super(pOwner,pID,pName);
		//.
		if (Owner != null)
			Owner.AddComponent(this);
	}
	
	public void AddItem(TComponentElement pItem) {
		if (Items == null)
			Items = new ArrayList<TComponentElement>();
		Items.add(pItem);
	}
	
	public void AddComponent(TComponent pComponent) {
		if (Components == null)
			Components = new ArrayList<TComponent>();
		Components.add(pComponent);
	}
	
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
		if (Items == null)
			return; //. ->		
    	for (int I = 0; I < Items.size(); I++)
    		Items.get(I).FromByteArray(BA,Idx);
    	for (int I = 0; I < Components.size(); I++)
    		Components.get(I).FromByteArray(BA,Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException, OperationException
    {
		if (Items == null)
			return null; //. ->
		ByteArrayOutputStream Result = new ByteArrayOutputStream();
		try {
	    	for (int I = 0; I < Items.size(); I++) {
	    		byte[] BA = Items.get(I).ToByteArray(); 
				Result.write(BA);
	    	}
	    	for (int I = 0; I < Components.size(); I++) {
	    		byte[] BA = Components.get(I).ToByteArray(); 
				Result.write(BA);
	    	}
	    	return Result.toByteArray(); //. ->
		}
		finally {
			Result.close();
		}
    }
}
