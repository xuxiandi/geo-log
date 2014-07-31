package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;

import com.geoscope.Classes.IO.Log.TDataConverter;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;


public class TLocation {

	public static final double NullTimestamp = Double.MIN_VALUE;
	public static final double MaxTimestamp = Double.MAX_VALUE;
	
	public String 					Name;
	public TReflectionWindowStruc 	RW;
	
	public TLocation() {
		Name = "";
		RW = null;
	}
	
	public TLocation(String pName) {
		Name = pName;
		RW = new TReflectionWindowStruc();
	}
	
	public TXYCoord LocationPoint() {
		TXYCoord P = new TXYCoord();
		P.X = (RW.X0+RW.X2)/2.0;
		P.Y = (RW.Y0+RW.Y2)/2.0;
		return P;
	}
	
	public int FromByteArray(byte[] BA, int Idx) throws IOException
	{
		int SS = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
		Name = "";
		if (SS > 0) {
			Name = new String(BA, Idx,SS, "windows-1251"); Idx += SS;
		}
		RW = new TReflectionWindowStruc();
		Idx = RW.FromByteArray(BA,Idx);
		return Idx;
	}	
	
	public int FromByteArrayV2(byte[] BA, int Idx) throws IOException
	{
		int SS = TDataConverter.ConvertBEByteArrayToInt32(BA,Idx); Idx += 4;
		Name = "";
		if (SS > 0) {
			Name = new String(BA, Idx,SS, "windows-1251"); Idx += SS;
		}
		RW = new TReflectionWindowStruc();
		Idx = RW.FromByteArrayV1(BA,Idx);
		return Idx;
	}	
	
	public byte[] ToByteArray() throws IOException
	{
		int SS = 0;
		if (Name != null)
			SS = Name.length();
		byte[] RWBA = RW.ToByteArray();
		byte[] Result = new byte[4/*SizeOf(Name)*/+SS+RWBA.length];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(SS); System.arraycopy(BA,0, Result,Idx, BA.length); Idx+=BA.length;
		if (SS > 0) {
			BA = Name.getBytes("windows-1251");
			System.arraycopy(BA,0, Result,Idx, BA.length); Idx+=BA.length;
		}
		System.arraycopy(RWBA,0, Result,Idx, RWBA.length); Idx+=RWBA.length;
		return Result;
	}	

	public String ToIncomingCommandMessage(int Version, int Session) {
		String _Name = Name.replace(';',',');
		String Result = TGeoScopeServerUser.TLocationCommandMessage.Prefix+" "+Integer.toString(Version)/*Parameters version*/+";"+
			_Name+";"+
			Double.toString(RW.X0)+";"+Double.toString(RW.Y0)+";"+Double.toString(RW.X1)+";"+Double.toString(RW.Y1)+";"+Double.toString(RW.X2)+";"+Double.toString(RW.Y2)+";"+Double.toString(RW.X3)+";"+Double.toString(RW.Y3)+";"+
			Integer.toString(RW.Xmn)+";"+Integer.toString(RW.Ymn)+";"+Integer.toString(RW.Xmx)+";"+Integer.toString(RW.Ymx)+";"+
			Double.toString(RW.BeginTimestamp)+";"+Double.toString(RW.EndTimestamp)+";"+Integer.toString(Session);
		return Result;
	}
	
	public String[] FromIncomingCommandMessage(String Command) throws Exception {
		if (!Command.startsWith(TGeoScopeServerUser.TLocationCommandMessage.Prefix))
			throw new Exception("incorrect command prefix"); //. =>
		String ParamsString = Command.substring(TGeoScopeServerUser.TLocationCommandMessage.Prefix.length()+1/*skip space*/);
		String[] Params = ParamsString.split(";");
		int Version = Integer.parseInt(Params[0]);
		switch (Version) {
		
		case 0:
			Name = Params[1];
			//.
			RW = new TReflectionWindowStruc();
			RW.X0 = Double.parseDouble(Params[2]);
			RW.Y0 = Double.parseDouble(Params[3]);
			RW.X1 = Double.parseDouble(Params[4]);
			RW.Y1 = Double.parseDouble(Params[5]);
			RW.X2 = Double.parseDouble(Params[6]);
			RW.Y2 = Double.parseDouble(Params[7]);
			RW.X3 = Double.parseDouble(Params[8]);
			RW.Y3 = Double.parseDouble(Params[9]);
			RW.Xmn = Integer.parseInt(Params[10]);
			RW.Ymn = Integer.parseInt(Params[11]);
			RW.Xmx = Integer.parseInt(Params[12]);
			RW.Ymx = Integer.parseInt(Params[13]);
			RW.UpdateContainer();
			//.
			RW.BeginTimestamp = Double.parseDouble(Params[14]);			
			RW.EndTimestamp = Double.parseDouble(Params[15]);
			//.
			return Params; //. ->
			
		default:
			throw new Exception("unknown command parameters version"); //. =>
		}
	}
}
