package com.geoscope.Classes.Data.Containers.Identification;

import java.util.UUID;

public class TUIDGenerator {

	public static String Generate() {
		UUID GUID = UUID.randomUUID();
		return GUID.toString(); 
	}
	
}
