package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.LANConnectionRepeater;

import java.net.Socket;

public class TLANConnectionRepeater {

	protected int ConnectionType;
	//.
	protected String 	Address;
	protected int 		Port;
	//.
	private int LocalPort;
	//.
	///////RepeaterServer: TTcpServer;
	//.
	protected String 	ServerAddress;
	protected int 		ServerPort; 
	protected int 		UserID;
	protected String 	UserPassword;
	protected int idGeographServerObject;
	//.
	protected TLANConnectionExceptionHandler ExceptionHandler;
	//.
	protected TLANConnectionStartHandler	StartHandler;
	protected TLANConnectionStopHandler 	StopHandler;
	//.
	protected TLANConnectionOnBytesTransmiteHandler OnSourceBytesTransmiteHandler;
	protected TLANConnectionOnBytesTransmiteHandler OnDestinationBytesTransmiteHandler;
	//.
	private int ConnectionsCount;

	public TLANConnectionRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionStartHandler pStartHandler, TLANConnectionStopHandler pStopHandler, TLANConnectionOnBytesTransmiteHandler pOnSourceBytesTransmiteHandler, TLANConnectionOnBytesTransmiteHandler pOnDestinationBytesTransmiteHandler) {
						ExceptionHandler = pExceptionHandler; ////////////
	}
	
	public TLANConnectionRepeater(int pConnectionType, String pAddress, int pPort, int pLocalPort, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject, TLANConnectionExceptionHandler pExceptionHandler, TLANConnectionStartHandler pStartHandler, TLANConnectionStopHandler pStopHandler) {
		this(pConnectionType, pAddress,pPort, pLocalPort, pServerAddress,pServerPort, pUserID,pUserPassword, pidGeographServerObject, pExceptionHandler, pStartHandler, pStopHandler, null,null);	
	}
	
	public void Destroy() {
		
	}
	
	public int GetPort() {
		return 0;
	}
	
	public int GetConnectionsCount() {
		return 0;
	}
	
	private void DoOnServerAccept(Socket ClientSocket) {

	}
}
