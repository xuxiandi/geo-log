package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;

public class TSourceStreamChannel extends TChannel {

	public static final int NextID = 15; //. a next unique channel ID

	public static class SourceNotExistError extends Exception {
		
		private static final long serialVersionUID = 1L;

		
		public SourceNotExistError(String Message) {
			super(Message);
		}

		public SourceNotExistError() {
			this("");
		}
	}
	
	public static class SourceNotAvailableError extends Exception {
		
		private static final long serialVersionUID = 1L;

		
		public SourceNotAvailableError(String Message) {
			super(Message);
		}

		public SourceNotAvailableError() {
			this("");
		}
	}
	
	public static class SourceIsLockedError extends Exception {
		
		private static final long serialVersionUID = 1L;

		
		public SourceIsLockedError(String Message) {
			super(Message);
		}

		public SourceIsLockedError() {
			this("");;
		}
	}

	
	public TSourceStreamChannel() {
		super();
	}
	
	public TSourceStreamChannel(int pID, String pLocationID, String pProfilesFolder, Class<?> ProfileClass) throws Exception {
		super(pID, pLocationID, pProfilesFolder, ProfileClass);
	}
}
