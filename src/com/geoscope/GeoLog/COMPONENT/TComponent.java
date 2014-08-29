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
	private ArrayList<TComponentItem> 		Items = null;
	
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
	
	public void Start() throws Exception {
		if (Components != null) {
			int CC = Components.size();
			for (int I = 0; I < CC; I++)
				Components.get(I).Start();
		}
	}
	
	public void Stop() throws Exception {
		if (Components != null) {
			int CC = Components.size();
			for (int I = CC-1; I >= 0; I--)
				Components.get(I).Stop();
		}
	}
	
	public void Restart() throws Exception {
		Stop();
		Start();
	}
	
	public void AddItem(TComponentItem pItem) {
		if (Items == null)
			Items = new ArrayList<TComponentItem>();
		Items.add(pItem);
	}
	
	public void AddComponent(TComponent pComponent) {
		if (Components == null)
			Components = new ArrayList<TComponent>();
		Components.add(pComponent);
	}
	
	public void RemoveItem(TComponentElement pItem) {
		if (Items == null)
			return; //. ->
		Items.remove(pItem);
	}
	
    public synchronized void FromByteArray(byte[] BA, TIndex Idx) throws IOException, OperationException
    {
		if (Items != null)
	    	for (int I = 0; I < Items.size(); I++) {
	    		TComponentItem Item = Items.get(I);
	    		if ((!Item.flVirtualValue) && (!Item.flDirectAccess))
	    			Item.FromByteArray(BA,Idx);
	    	}
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
		    		TComponentItem Item = Items.get(I);
		    		if ((!Item.flVirtualValue) && (!Item.flDirectAccess)) {
			    		byte[] BA = Item.ToByteArray(); 
						Result.write(BA);
		    		}
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
