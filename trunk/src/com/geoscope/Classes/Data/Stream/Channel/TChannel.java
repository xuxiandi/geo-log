package com.geoscope.Classes.Data.Stream.Channel;


public class TChannel {
	
	public static final int CHANNEL_KIND_IN		= 0;
	public static final int CHANNEL_KIND_OUT	= 1;
	public static final int CHANNEL_KIND_INOUT	= 2; 
	
	public static class TConfigurationParser {
		
		private static final String VersionDelimiter = ":";
		private static final String CoderDecoderDelimiter = ";";
		private static final String ParameterDelimiter = ",";
		
		public String[] CoderConfiguration = null;
		public String[] DecoderConfiguration = null;
		
		public TConfigurationParser(String Configuration) throws Exception {
			if ((Configuration != null) && (Configuration.length() > 0)) {
				String[] SA = Configuration.split(VersionDelimiter);
				//.
				int Version = Integer.parseInt(SA[0]);
				if (Version != 1)
					throw new Exception("unknown version"); //. =>
				String CS = SA[1];
				SA = CS.split(CoderDecoderDelimiter);
				//.
				String CoderConfigurationStr = SA[0];
				String DecoderConfigurationStr = SA[1];
				CoderConfiguration = CoderConfigurationStr.split(ParameterDelimiter);
				DecoderConfiguration = DecoderConfigurationStr.split(ParameterDelimiter);
			}
		}
	}
	
	public static class TParametersParser {
		
		private static final String VersionDelimiter = ":";
		private static final String CoderDecoderDelimiter = ";";
		private static final String ParameterDelimiter = ",";
		
		public String[] CoderParameters = null;
		public String[] DecoderParameters = null;
		
		public TParametersParser(String Parameters) throws Exception {
			if ((Parameters != null) && (Parameters.length() > 0)) {
				String[] SA = Parameters.split(VersionDelimiter);
				//.
				int Version = Integer.parseInt(SA[0]);
				if (Version != 1)
					throw new Exception("unknown version"); //. =>
				String PS = SA[1];
				SA = PS.split(CoderDecoderDelimiter);
				//.
				String CoderParametersStr = SA[0];
				String DecoderParametersStr = SA[1];
				CoderParameters = CoderParametersStr.split(ParameterDelimiter);
				DecoderParameters = DecoderParametersStr.split(ParameterDelimiter);
			}
		}
	}
	
	
	public int 	ID = -1;
	public boolean Enabled = true;
	public int Kind = CHANNEL_KIND_IN;
	public int 	DataFormat = 0;
	public String Name = "";
	public String Info = "";
	public int 	Size = 0;
	public String Configuration = "";
	public String Parameters = "";
	
	public void Parse() throws Exception {
	}
}