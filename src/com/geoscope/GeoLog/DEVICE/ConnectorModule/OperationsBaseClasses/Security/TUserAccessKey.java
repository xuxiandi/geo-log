package com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security;

import java.util.UUID;

public class TUserAccessKey {

	public static final int AccessKeyLifeTime = 1000*5; //. seconds
	
	public static String GenerateValue() {
		UUID GUID = UUID.randomUUID();
		return GUID.toString(); 
	}
	
	private long	Timestamp = 0;
	private String 	Value = "";

	public TUserAccessKey() {
	}

	public TUserAccessKey(long pTimestamp, String pValue) {
		Timestamp = pTimestamp;
		Value = pValue;
	}

	public synchronized String GetValue() {
		return Value;
	}
	
	public synchronized long GetTimestamp() {
		return Timestamp;
	}
	
	public synchronized long UpdateTimestamp() {
		Timestamp = System.currentTimeMillis();
		return Timestamp;
	}
	
	public synchronized boolean Generate() {
		if (IsValid())
			return false; //. ->
		Timestamp = System.currentTimeMillis();
		Value = GenerateValue();
		return true;
	}
	
	public synchronized void Clear() {
		Value = "";
	}
	
	public synchronized boolean IsNull() {
		return Value.equals("");
	}
	
	public synchronized boolean IsValid(int KeyLifetimeInMs) {
		if (IsNull())
			return false; //. ->
		return ((System.currentTimeMillis()-Timestamp) < KeyLifetimeInMs);
	}
	
	public synchronized boolean IsValid() {
		return IsValid(AccessKeyLifeTime);
	}
	
	public synchronized boolean IsTheSame(TUserAccessKey AccessKey) {
		return Value.equals(AccessKey.Value);
	}
	
	public synchronized boolean Check(TUserAccessKey AccessKey) {
		if (!IsValid(AccessKeyLifeTime))
				return false; //. ->
		return IsTheSame(AccessKey);
	}
	
	public synchronized TUserAccessKey Clone() {
		return new TUserAccessKey(Timestamp,Value);
	}
}
