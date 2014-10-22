package com.geoscope.GeoEye;

import java.io.IOException;

import com.geoscope.GeoLog.DEVICE.AudioModule.VoiceCommandModule.TVoiceCommandModule;

public class TTrackerPanelVoiceCommands {

	public static TTrackerPanelVoiceCommands GetInstance(String CultureName) {
		if (ENUS.CultureName.equals(CultureName))
			return (new ENUS()); //. ->
		else
			return null; //. 
	}
	
	//. Command definitions
	public static String COMMAND_GPSMODULE_POI_ADDIMAGE;
	public static String COMMAND_GPSMODULE_POI_ADDVIDEO;
	//.
	public static String COMMAND_VIDEORECORDERMODULE_RECORDING_ON; 
	public static String COMMAND_VIDEORECORDERMODULE_RECORDING_OFF;
	//.
	public static String COMMAND_DATASTREAMERMODULE_ACTIVE_ON; 
	public static String COMMAND_DATASTREAMERMODULE_ACTIVE_OFF; 

	protected static String[] Commands =null;
	//.
	protected static String[] Commands_Create() {
		String[] Result = new String[] {
			COMMAND_GPSMODULE_POI_ADDIMAGE,
			COMMAND_GPSMODULE_POI_ADDVIDEO,
			COMMAND_VIDEORECORDERMODULE_RECORDING_ON,
			COMMAND_VIDEORECORDERMODULE_RECORDING_OFF,
			COMMAND_DATASTREAMERMODULE_ACTIVE_ON,
			COMMAND_DATASTREAMERMODULE_ACTIVE_OFF
		};
    	return Result;
	}

	public TVoiceCommandModule.TCommands GetCommands(boolean flAsGrammar) throws IOException {
		return null;
	}
	
	
    private static class ENUS extends TTrackerPanelVoiceCommands {
        
		private static final String CommandsName = "TrackerVoiceCommands.EN";
		private static final String CultureName = "en-us";
		
    	protected static String[] ContructCommands() {
    		if (Commands != null)
    			return Commands; //. ->
    		//.
        	COMMAND_GPSMODULE_POI_ADDIMAGE 				= "take image";
        	COMMAND_GPSMODULE_POI_ADDVIDEO 				= "take video";
        	//.
        	COMMAND_VIDEORECORDERMODULE_RECORDING_ON 	= "start recording"; 
        	COMMAND_VIDEORECORDERMODULE_RECORDING_OFF 	= "finish recording";
        	//.
        	COMMAND_DATASTREAMERMODULE_ACTIVE_ON 		= "start streaming"; 
        	COMMAND_DATASTREAMERMODULE_ACTIVE_OFF 		= "finish streaming"; 
        	//.
        	Commands = Commands_Create();
        	return Commands;
    	}

    	@Override
		public TVoiceCommandModule.TCommands GetCommands(boolean flAsGrammar) throws IOException {
			return (new TVoiceCommandModule.TCommands(CommandsName,CultureName,ContructCommands(),flAsGrammar));
		}
    }   
}
