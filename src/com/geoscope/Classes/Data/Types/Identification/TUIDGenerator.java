package com.geoscope.Classes.Data.Types.Identification;

import java.util.Locale;
import java.util.UUID;

import com.geoscope.Classes.Data.Types.Date.OleDate;

public class TUIDGenerator {
	
	public static final int MaxLength = 128;
	//.
	public static final String Delimiter = "_";

	public static String Generate() {
		UUID GUID = UUID.randomUUID();
		return GUID.toString().toUpperCase(Locale.ENGLISH); 
	}
	
	public static String GenerateWithTimestamp() {
		return Double.toString(OleDate.UTCCurrentTimestamp())+Delimiter+Generate();
	}

	public static String GenerateWithPrefix(String Prefix) throws Exception {
		String R = Prefix+Delimiter+Generate();
		if (R.length() > MaxLength)
			throw new Exception("UID is too long"); //. =>
		return R;
	}
}
