package com.geoscope.GeoLog.COMPONENT;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.TComponentSchema;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TComponent extends TComponentElement {

	public TComponentSchema Schema = null;
	//.
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
	
	public TComponent(TComponentSchema pSchema, int pID, String pName)
	{
		super(null,pID,pName);
		Schema = pSchema;
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
		if (Items != null)
	    	for (int I = 0; I < Items.size(); I++)
	    		Items.get(I).FromByteArray(BA,Idx);
		if (Components != null)
	    	for (int I = 0; I < Components.size(); I++)
	    		Components.get(I).FromByteArray(BA,Idx);
    }
    
    public synchronized byte[] ToByteArray() throws IOException, OperationException
    {
		ByteArrayOutputStream Result = new ByteArrayOutputStream();
		try {
			if (Items != null)
		    	for (int I = 0; I < Items.size(); I++) {
		    		byte[] BA = Items.get(I).ToByteArray(); 
					Result.write(BA);
		    	}
			if (Components != null)
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
