package com.geoscope.Utils;

import java.util.UUID;

public class TUIDGenerator {

	public static String Generate() {
		UUID GUID = UUID.randomUUID();
		return GUID.toString(); 
	}
	
}
