package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security;

public class TComponentUserAccess {

	public short[]	Address = new short[0];
	public int[]	Operations = new int[0];
	
	public TComponentUserAccess() {
	}
	
	public TComponentUserAccess(String S) {
		FromString(S);
	}
	
	private void FromString(String S) {
		String[] SA = S.split(":");
		String AddressString = SA[0];
		String OperationsString = SA[1];
		SA = AddressString.split("\\.");
		Address = new short[SA.length];
		for (int I = 0; I < SA.length; I++)
			Address[I] = Short.parseShort(SA[I]);
		SA = OperationsString.split("&");
		Operations = new int[SA.length];
		for (int I = 0; I < SA.length; I++)
			Operations[I] = Short.parseShort(SA[I]);
	}
	
	public boolean CheckAddress(short[] pAddress) {
		if (Address.length > pAddress.length)
			return false; //. ->
		for (int I = 0; I < Address.length; I++)
			if (Address[I] != pAddress[I])
				return false; //. ->
		return true;
	}
	
	public boolean CheckAccess(int pOperation) {
		for (int I = 0; I < Operations.length; I++)
			if (Operations[I] == pOperation)
				return true; //. ->
		return false;
	}
}
