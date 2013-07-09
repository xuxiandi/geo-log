package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security;

public class TComponentUserAccessList {

	public static boolean CheckAccess(TComponentUserAccessList CUAL, short[] Address, int Operation) {
		if (CUAL == null)
			return true; //. ->
		return CUAL.CheckAccess(Address,Operation);
	}
	
	public TComponentUserAccess[] Items = new TComponentUserAccess[0];
	
	public TComponentUserAccessList() {
	}
	
	public TComponentUserAccessList(String S) {
		FromString(S);
	}
	
	private void FromString(String S) {
		String[] SA = S.split(";");
		Items = new TComponentUserAccess[SA.length];
		for (int I = 0; I < SA.length; I++)
			Items[I] = new TComponentUserAccess(SA[I]);
	}
	
	private boolean CheckAccess(short[] Address, int Operation) {
		for (int I = 0; I < Items.length; I++)
			if (Items[I].CheckAddress(Address))
				return Items[I].CheckAccess(Operation); //. ->
		return false;
	}
}
