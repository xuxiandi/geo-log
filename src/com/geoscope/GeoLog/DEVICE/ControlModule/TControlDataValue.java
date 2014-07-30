package com.geoscope.GeoLog.DEVICE.ControlModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.TVideoRecorderServerVideoPhoneServer;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TUDPEchoServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TComponentUserAccessList;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.LANModule.TLANModule;
import com.geoscope.GeoLog.DEVICE.LANModule.TUDPConnectionRepeater;

public class TControlDataValue extends TComponentTimestampedDataValue {

	private TControlModule ControlModule;
	
	public TControlDataValue(TControlModule pControlModule) {
		ControlModule = pControlModule;
	}

	@Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception
    {
		if (!ControlModule.flEnabled)
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		//.
        super.FromByteArrayByAddressData(BA,/*ref*/ Idx, AddressData);
		//.
		if (AddressData == null)
			return; //. ->
    	String Params;
    	try {
    		Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	}
    	catch (Exception E) {
    		return; //. ->
    	}
    	String[] SA = Params.split(",");
    	int Operation = Integer.parseInt(SA[0]);
    	//.
    	switch (Operation) {
    	case 0: //. restart device process 
    		ControlModule.RestartDeviceProcessAfterDelay(1000*1/*seconds*/);
            break; //. >

    	case 1: //. restart device  
    		ControlModule.RestartDeviceAfterDelay(1000*1/*seconds*/);
            break; //. >

    	case 2: //.
            break; //. >
            
    	case 100: //. set LAN Scheme
    		ControlModule.Device.LANModule.SetLANScheme(BA);
            break; //. >
            
    	case 1000: //. set Control's Scheme
            break; //. >
            
        default:
            break; //. >
    	}
    }
    
	@Override
    public synchronized byte[] ToByteArrayByAddressData(byte[] AddressData) throws Exception
    {
		if (!ControlModule.flEnabled)
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		//.
		if (AddressData == null)
			return null; //. ->
    	String Params;
    	try {
    		Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	}
    	catch (Exception E) {
    		return null; //. ->
    	}
    	String[] SA = Params.split(",");
    	int Operation = Integer.parseInt(SA[0]);
    	//.
    	switch (Operation) {
    	case 1: //. get device configuration data 
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->

    	case 2: //. get device state data
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = ControlModule.Device.GetStateInfo().getBytes("windows-1251");
            return ToByteArray(); //. ->

    	case 3: //. get device log 
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = ControlModule.Device.Log.ToZippedByteArray();
            return ToByteArray(); //. ->
            
    	case 100: //. get LAN Scheme
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = ControlModule.Device.LANModule.GetLANScheme();
            return ToByteArray(); //. ->
            
    	case 101: //. start LAN connection
			int Version = Integer.parseInt(SA[1]);
    		String Address = SA[2];
        	int Port = Integer.parseInt(SA[3]);
    		String ServerAddress = SA[4];
        	int ServerPort = Integer.parseInt(SA[5]);
        	int ConnectionID = Integer.parseInt(SA[6]);
        	int ConnectionTimeout = Integer.parseInt(SA[7]);
        	//.
        	int ConnectionType;
        	String UserAccessKey = null;
        	switch (Version) {
        	
        	case 0:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL;
        		break; //. >
        		
        	case 1:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_PACKETTED;
        		break; //. >
        		
        	case 2:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL;
        		UserAccessKey = SA[8];
        		if ((UserAccessKey == null) || (UserAccessKey.length() < 1))
        			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
        		break; //. >
        		
        	case 3:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_PACKETTED;
        		UserAccessKey = SA[8];
        		if ((UserAccessKey == null) || (UserAccessKey.length() < 1))
        			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
        		break; //. >
        		
        	default:
        		ConnectionType = 0;
        		break; //. >
        	}
        	//.
        	TConnectionRepeater CR = ControlModule.Device.LANModule.ConnectionRepeaters_Add(ConnectionType, Address,Port, ServerAddress,ServerPort, ConnectionID, UserAccessKey);
        	if (CR == null)
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable); //. =>
        	if (!CR.WaitForDestinationConnectionResult(ConnectionTimeout))
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_TimeoutIsExpired); //. =>
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = Integer.toString(ConnectionID).getBytes("windows-1251");
            return ToByteArray(); //. ->
            
    	case 102: //. stop LAN connection
        	ConnectionID = Integer.parseInt(SA[1]);
        	Version = 0;
        	if (SA.length >= 3)
        		Version = Integer.parseInt(SA[2]);
        	switch (Version) {
        	
        	case 0:
            	ControlModule.Device.LANModule.ConnectionRepeaters_Cancel(ConnectionID,null);
        		break; //. >
        		
        	case 1:
        		UserAccessKey = SA[3];
            	ControlModule.Device.LANModule.ConnectionRepeaters_Cancel(ConnectionID,UserAccessKey);
        		break; //. >
        	
        	default:
            	ControlModule.Device.LANModule.ConnectionRepeaters_Cancel(ConnectionID,null);
        		break; //. >
        	}
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 103: //. get LAN connection's status
    		//. to-do
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 104: //. start LAN UDP connection
			Version = Integer.parseInt(SA[1]);
        	int ReceivingPort = Integer.parseInt(SA[2]);
        	int ReceivingPacketSize = Integer.parseInt(SA[3]);
    		Address = SA[4];
        	int TransmittingPort = Integer.parseInt(SA[5]);
        	int TransmittingPacketSize = Integer.parseInt(SA[6]);
    		ServerAddress = SA[7];
        	ServerPort = Integer.parseInt(SA[8]);
        	ConnectionID = Integer.parseInt(SA[9]);
        	ConnectionTimeout = Integer.parseInt(SA[10]);
        	//.
        	switch (Version) {
        	
        	case 0:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL;
        		break; //. >
        		
        	case 1:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_PACKETTED;
        		break; //. >
        		
        	default:
        		ConnectionType = 0;
        		break; //. >
        	}
        	//.
        	TUDPConnectionRepeater UDPCR = ControlModule.Device.LANModule.UDPConnectionRepeaters_Add(ConnectionType, ReceivingPort,ReceivingPacketSize, Address,TransmittingPort,TransmittingPacketSize, ServerAddress,ServerPort, ConnectionID);
        	if (UDPCR == null)
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable); //. =>
        	if (!UDPCR.WaitForDestinationConnectionResult(ConnectionTimeout))
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_TimeoutIsExpired); //. =>
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = Integer.toString(ConnectionID).getBytes("windows-1251");
            return ToByteArray(); //. ->
            
    	case 105: //. stop LAN UDP connection
        	ConnectionID = Integer.parseInt(SA[1]);
        	ControlModule.Device.LANModule.UDPConnectionRepeaters_Cancel(ConnectionID);
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 106: //. get LAN UDP connection's status
    		//. to-do
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 107: //. start Device connection
			Version = Integer.parseInt(SA[1]);
    		String _CUAL = SA[2];
    		ServerAddress = SA[3];
        	ServerPort = Integer.parseInt(SA[4]);
        	ConnectionID = Integer.parseInt(SA[5]);
        	ConnectionTimeout = Integer.parseInt(SA[6]);
        	//.
        	TComponentUserAccessList CUAL = new TComponentUserAccessList(_CUAL); 
        	//.
        	TConnectionRepeater DCR = ControlModule.Device.LANModule.ConnectionRepeaters_AddDeviceConnectionRepeater(CUAL, ServerAddress,ServerPort, ConnectionID, null);
        	if (DCR == null)
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable); //. =>
        	if (!DCR.WaitForDestinationConnectionResult(ConnectionTimeout))
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_TimeoutIsExpired); //. =>
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = Integer.toString(ConnectionID).getBytes("windows-1251");
            return ToByteArray(); //. ->
            
    	case 108: //. stop Device connection
        	ConnectionID = Integer.parseInt(SA[1]);
        	ControlModule.Device.LANModule.ConnectionRepeaters_Cancel(ConnectionID,null);
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 109: //. get Device connection's status
    		//. to-do
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 110: //. start LAN connection (using UDP transport)
			Version = Integer.parseInt(SA[1]);
    		Address = SA[2];
        	Port = Integer.parseInt(SA[3]);
    		ServerAddress = SA[4];
        	ServerPort = Integer.parseInt(SA[5]);
        	ConnectionID = Integer.parseInt(SA[6]);
        	ConnectionTimeout = Integer.parseInt(SA[7]);
        	//.
        	UserAccessKey = null;
    		String DestinationUDPAddress = null;
        	int DestinationUDPPort = 0;
        	int DestinationUDPProxyType = TUDPEchoServerClient.PROXY_TYPE_NONE;
        	String AddressDataString = null;
        	//.
        	switch (Version) {
        	
        	case 0:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL;
        		DestinationUDPAddress = SA[8];
            	DestinationUDPPort = Integer.parseInt(SA[9]);
            	DestinationUDPProxyType = Integer.parseInt(SA[10]);
            	AddressDataString = SA[11];
        		break; //. >
        		
        	case 1:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_PACKETTED;
        		DestinationUDPAddress = SA[8];
            	DestinationUDPPort = Integer.parseInt(SA[9]);
            	DestinationUDPProxyType = Integer.parseInt(SA[10]);
            	AddressDataString = SA[11];
        		break; //. >
        		
        	case 2:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL;
        		UserAccessKey = SA[8];
        		if ((UserAccessKey == null) || (UserAccessKey.length() < 1))
        			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
        		DestinationUDPAddress = SA[9];
            	DestinationUDPPort = Integer.parseInt(SA[10]);
            	DestinationUDPProxyType = Integer.parseInt(SA[11]);
            	AddressDataString = SA[12];
        		break; //. >
        		
        	case 3:
        		ConnectionType = TLANModule.LANCONNECTIONMODULE_CONNECTIONTYPE_PACKETTED;
        		UserAccessKey = SA[8];
        		if ((UserAccessKey == null) || (UserAccessKey.length() < 1))
        			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
        		DestinationUDPAddress = SA[9];
            	DestinationUDPPort = Integer.parseInt(SA[10]);
            	DestinationUDPProxyType = Integer.parseInt(SA[11]);
            	AddressDataString = SA[12];
        		break; //. >
        		
        	default:
        		ConnectionType = 0;
        		break; //. >
        	}
        	//.
        	TConnectionUDPRepeater CUDPR = ControlModule.Device.LANModule.ConnectionUDPRepeaters_Add(ConnectionType, Address,Port, ServerAddress,ServerPort, DestinationUDPAddress,DestinationUDPPort,DestinationUDPProxyType, AddressDataString, ConnectionID, UserAccessKey);
        	if (CUDPR == null)
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_SourceIsUnavaiable); //. =>
        	if (!CUDPR.WaitForDestinationConnectionResult(ConnectionTimeout))
    			throw new OperationException(TGetControlDataValueSO.OperationErrorCode_TimeoutIsExpired); //. =>
        	//.
        	String Result = Integer.toString(ConnectionID)+","+CUDPR.SourceUDPAddress+","+Integer.toString(CUDPR.SourceUDPPort)+","+Integer.toString(CUDPR.SourceUDPSocketProxyType); 
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = Result.getBytes("windows-1251");
            return ToByteArray(); //. ->
            
    	case 111: //. stop LAN connection (using UDP transport)
        	ConnectionID = Integer.parseInt(SA[1]);
        	Version = 0;
        	if (SA.length >= 3)
        		Version = Integer.parseInt(SA[2]);
        	switch (Version) {
        	
        	case 0:
            	ControlModule.Device.LANModule.ConnectionUDPRepeaters_Cancel(ConnectionID,null);
        		break; //. >
        		
        	case 1:
        		UserAccessKey = SA[3];
            	ControlModule.Device.LANModule.ConnectionUDPRepeaters_Cancel(ConnectionID,UserAccessKey);
        		break; //. >
        	
        	default:
            	ControlModule.Device.LANModule.ConnectionUDPRepeaters_Cancel(ConnectionID,null);
        		break; //. >
        	}
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 112: //. get LAN connection's status (using UDP transport) 
    		//. to-do
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 201: //. start VideoPhone session
			Version = Integer.parseInt(SA[1]);
        	int InitiatorID = Integer.parseInt(SA[2]);
        	String InitiatorName = SA[3];
        	int idTComponent = Integer.parseInt(SA[4]);
        	int idComponent = Integer.parseInt(SA[5]);
			String SessionID = SA[6];
        	boolean flAudio = (Integer.parseInt(SA[7]) != 0);
        	boolean flVideo = (Integer.parseInt(SA[8]) != 0);
        	//.
        	TVideoRecorderServerVideoPhoneServer.TSession Session = new TVideoRecorderServerVideoPhoneServer.TSession(SessionID, InitiatorID,InitiatorName, idTComponent,idComponent, flAudio,flVideo, ControlModule.Device, null);
			if (!TVideoRecorderServerVideoPhoneServer.SessionServer.StartSession(Session))
        		throw new OperationException(TGetControlDataValueSO.OperationErrorCode_SourceIsBusy); //. =>
			/*///- not needed, using session LAN connection 
			//. wait for session result
			switch (Session.WaitForResult()) {

			case TVideoRecorderServerVideoPhoneServer.TSession.SESSION_RESULT_ACCEPTED:
			case TVideoRecorderServerVideoPhoneServer.TSession.SESSION_RESULT_OPEN:
				break; //. >

			case TVideoRecorderServerVideoPhoneServer.TSession.SESSION_RESULT_ERROR:
        		throw new OperationException(TGeographServerServiceOperation.ErrorCode_Unknown); //. =>

			case TVideoRecorderServerVideoPhoneServer.TSession.SESSION_RESULT_REJECTED:
        		throw new OperationException(TGetControlDataValueSO.OperationErrorCode_SourceAccessIsDenied); //. =>

			case TVideoRecorderServerVideoPhoneServer.TSession.SESSION_RESULT_WAIT:
        		throw new OperationException(TGetControlDataValueSO.OperationErrorCode_SourceIsTimedout); //. =>
        	
        	default:
        		throw new OperationException(TGeographServerServiceOperation.ErrorCode_Unknown); //. =>
			}*/
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = Session.GetValue().getBytes("windows-1251");
            return ToByteArray(); //. ->
            
    	case 202: //. stop VideoPhone session
			Version = Integer.parseInt(SA[1]);
			SessionID = SA[2];
			//.
			Session = TVideoRecorderServerVideoPhoneServer.SessionServer.Session_Get();
			if ((Session != null) && Session.IsTheSame(SessionID))
				TVideoRecorderServerVideoPhoneServer.SessionServer.FinishSession(Session);
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
    	case 1000: //. get Control's Scheme
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
            
        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
    	}
    }
}
