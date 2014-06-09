/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.LANModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioSampleServerLANLVConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioSampleServerLANLVConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.AudioModule.TLoudspeakerLANLVConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.AudioModule.TMicrophoneLANLVConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.TDeviceConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TComponentUserAccessList;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoFrameServerLANLVConnectionRepeater;
import com.geoscope.GeoLog.DEVICE.VideoModule.TVideoFrameServerLANLVConnectionUDPRepeater;
import com.geoscope.GeoLog.DEVICE.VideoRecorderModule.TVideoPhoneServerLANLVConnectionRepeater;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

/**
 *
 * @author ALXPONOM
 */
public class TLANModule extends TModule {
	
	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"LANModule";
	}
	
	public static String LANSchemeFile() {
		return Folder()+"/"+"LANScheme.xml";
	}
	
	public static final int LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL 		= 0;
	public static final int LANCONNECTIONMODULE_CONNECTIONTYPE_PACKETTED 	= 1;
	//.
	public static final int LocalVirtualConnection_PortBase = 10000; //. next ID +6
	
	public TConnectionRepeater LocalVirtualConnection_GetRepeater(int ConnectionType, int Port, TLANModule pLANModule, String pServerAddress, int pServerPort, int ConnectionID, String UserAccessKey) throws OperationException {
		switch (Port) {
		
		case TLoudspeakerLANLVConnectionRepeater.Port: 
			if (!TLoudspeakerLANLVConnectionRepeater.CheckUserAccessKey(this,UserAccessKey))
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return (new TLoudspeakerLANLVConnectionRepeater(this, pServerAddress,pServerPort, ConnectionID, UserAccessKey)); //. -> 
		
		case TMicrophoneLANLVConnectionRepeater.Port:
			if (!TMicrophoneLANLVConnectionRepeater.CheckUserAccessKey(this,UserAccessKey))
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return (new TMicrophoneLANLVConnectionRepeater(this, pServerAddress,pServerPort, ConnectionID, UserAccessKey)); //. -> 
		
		case TAudioSampleServerLANLVConnectionRepeater.Port:
			if (!TAudioSampleServerLANLVConnectionRepeater.CheckUserAccessKey(this,UserAccessKey))
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return (new TAudioSampleServerLANLVConnectionRepeater(this, pServerAddress,pServerPort, ConnectionID, UserAccessKey)); //. -> 
		
		case TVideoFrameServerLANLVConnectionRepeater.Port:
			if (!TVideoFrameServerLANLVConnectionRepeater.CheckUserAccessKey(this,UserAccessKey))
        			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return (new TVideoFrameServerLANLVConnectionRepeater(this, pServerAddress,pServerPort, ConnectionID, UserAccessKey)); //. -> 
		
		case TVideoPhoneServerLANLVConnectionRepeater.Port:
			if (!TVideoPhoneServerLANLVConnectionRepeater.CheckUserAccessKey(this,UserAccessKey))
        			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return (new TVideoPhoneServerLANLVConnectionRepeater(this, pServerAddress,pServerPort, ConnectionID, UserAccessKey)); //. -> 
		
		default:
    		if (UserAccessKey != null)
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return null; //. ->
		}
	}
		
	public TConnectionUDPRepeater LocalVirtualConnection_GetUDPRepeater(int ConnectionType, int Port, TLANModule pLANModule, String pServerAddress, int pServerPort, String DestinationUDPAddress, int DestinationUDPPort, int DestinationUDPProxyType, String AddressData, int ConnectionID, String UserAccessKey) throws OperationException {
		switch (Port) {
		
		case TAudioSampleServerLANLVConnectionUDPRepeater.Port:
			if (!TAudioSampleServerLANLVConnectionUDPRepeater.CheckUserAccessKey(this,UserAccessKey))
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return (new TAudioSampleServerLANLVConnectionUDPRepeater(this, pServerAddress,pServerPort, DestinationUDPAddress,DestinationUDPPort,DestinationUDPProxyType, AddressData, ConnectionID, UserAccessKey)); //. -> 

		case TVideoFrameServerLANLVConnectionUDPRepeater.Port:
			if (!TVideoFrameServerLANLVConnectionUDPRepeater.CheckUserAccessKey(this,UserAccessKey))
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return (new TVideoFrameServerLANLVConnectionUDPRepeater(this, pServerAddress,pServerPort, DestinationUDPAddress,DestinationUDPPort,DestinationUDPProxyType, AddressData, ConnectionID, UserAccessKey)); //. -> 

		default:
    		if (UserAccessKey != null)
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
			return null; //. ->
		}
	}
		
	public TUDPConnectionRepeater LocalVirtualUDPConnection_GetRepeater(int ConnectionType, int ReceivingPort, int ReceivingPacketSize,  int TransmittingPort, int TransmittingPacketSize, TLANModule pLANModule, String pServerAddress, int pServerPort, int ConnectionID) {
		return null;
	}
		
    public TLANModule(TDEVICEModule pDevice)
    {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SLANModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
    }
    
    public void Destroy() throws OperationException {
		if (IsEnabled()) {
	    	ConnectionRepeaters_RemoveAll();
	    	UDPConnectionRepeaters_RemoveAll();
		}
    }
    
    public byte[] GetLANScheme() throws IOException {
    	File LSF = new File(LANSchemeFile());
    	if (!LSF.exists()) 
    		return null; //. ->
    	FileInputStream FIS  = new FileInputStream(LSF);
    	try {
    		byte[] Result = new byte[(int)LSF.length()];
    		FIS.read(Result);
    		return Result; //. ->
    	}
    	finally {
    		FIS.close();
    	}
    }
    
    public void SetLANScheme(byte[] BA) throws IOException {
    	File LSF = new File(LANSchemeFile());
    	FileOutputStream FOS  = new FileOutputStream(LSF);
    	try {
    		FOS.write(BA);
    	}
    	finally {
    		FOS.close();
    	}
    }
    
    public TConnectionRepeater ConnectionRepeaters_Add(int ConnectionType, String Address, int Port, String pServerAddress, int pServerPort, int ConnectionID, String UserAccessKey) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	if (Address.equals("127.0.0.1") && (Port >= LocalVirtualConnection_PortBase))
    		return LocalVirtualConnection_GetRepeater(ConnectionType, Port, this, pServerAddress,pServerPort, ConnectionID, UserAccessKey); //. ->
    	else {
    		if (UserAccessKey != null)
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
    		switch (ConnectionType) {

    		case LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL: 
        		return (new TLANConnectionRepeater(this, Address,Port, pServerAddress,pServerPort, ConnectionID, UserAccessKey)); //. ->
    			
    		case LANCONNECTIONMODULE_CONNECTIONTYPE_PACKETTED: 
        		return (new TLANConnectionRepeater1(this, Address,Port, pServerAddress,pServerPort, ConnectionID, UserAccessKey)); //. ->

    		default: 
    			return null; //. ->
    		}
    	}
    }
    
    public TConnectionRepeater ConnectionRepeaters_AddDeviceConnectionRepeater(TComponentUserAccessList CUAL, String pServerAddress, int pServerPort, int ConnectionID, String UserAccessKey) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		return (new TDeviceConnectionRepeater(this, CUAL, pServerAddress,pServerPort, ConnectionID, UserAccessKey)); //. ->
    }
    
    public void ConnectionRepeaters_Remove(TConnectionRepeater CR) {
    	CR.Destroy();
    }

    public void ConnectionRepeaters_RemoveAll() throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++)
        		TConnectionRepeater.Repeaters.get(I).Destroy();
		}
    }
    
    public void ConnectionRepeaters_Cancel(int ConnectionID, String UserAccessKey) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if (CR.ConnectionID == ConnectionID) {
        			if ((UserAccessKey == null) || (CR.CheckUserAccessKey(UserAccessKey)))
        				CR.Cancel();
        		}
        	}
		}
    }

    public void ConnectionRepeaters_CancelByUserAccessKey(String UserAccessKey) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if (CR.UserAccessKey.equals(UserAccessKey))
        			CR.Cancel();
        	}
		}
    }

    public void ConnectionRepeaters_CheckForIdle() {
    	synchronized (TConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionRepeater.Repeaters.size(); I++) {
        		TConnectionRepeater CR = TConnectionRepeater.Repeaters.get(I);
        		if (CR.IsIdle()) 
        			CR.Destroy();
        	}
    	}
    }
    
    public TConnectionUDPRepeater ConnectionUDPRepeaters_Add(int ConnectionType, String Address, int Port, String pServerAddress, int pServerPort, String DestinationUDPAddress, int DestinationUDPPort, int DestinationUDPProxyType, String AddressData, int ConnectionID, String UserAccessKey) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	if (Address.equals("127.0.0.1") && (Port >= LocalVirtualConnection_PortBase))
    		return LocalVirtualConnection_GetUDPRepeater(ConnectionType, Port, this, pServerAddress,pServerPort, DestinationUDPAddress,DestinationUDPPort,DestinationUDPProxyType, AddressData, ConnectionID, UserAccessKey); //. ->
    	else {
    		if (UserAccessKey != null)
    			throw new OperationException(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied); //. =>
    		switch (ConnectionType) {

    		case LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL: 
        		return (new TLANConnectionUDPRepeater(this, Address,Port, pServerAddress,pServerPort, DestinationUDPAddress,DestinationUDPPort,DestinationUDPProxyType, AddressData, ConnectionID, UserAccessKey)); //. ->

    		default: 
    			return null; //. ->
    		}
    	}
    }
    
    public void ConnectionUDPRepeaters_Remove(TConnectionUDPRepeater CR) {
    	CR.Destroy();
    }

    public void ConnectionUDPRepeaters_RemoveAll() throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	synchronized (TConnectionUDPRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionUDPRepeater.Repeaters.size(); I++)
        		TConnectionUDPRepeater.Repeaters.get(I).Destroy();
		}
    }
    
    public void ConnectionUDPRepeaters_Cancel(int ConnectionID, String UserAccessKey) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	synchronized (TConnectionUDPRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionUDPRepeater.Repeaters.size(); I++) {
        		TConnectionUDPRepeater CR = TConnectionUDPRepeater.Repeaters.get(I);
        		if (CR.ConnectionID == ConnectionID) {
        			if ((UserAccessKey == null) || (CR.CheckUserAccessKey(UserAccessKey)))
        				CR.Cancel();
        		}
        	}
		}
    }

    public void ConnectionUDPRepeaters_CancelByUserAccessKey(String UserAccessKey) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	synchronized (TConnectionUDPRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionUDPRepeater.Repeaters.size(); I++) {
        		TConnectionUDPRepeater CR = TConnectionUDPRepeater.Repeaters.get(I);
        		if (CR.UserAccessKey.equals(UserAccessKey))
        			CR.Cancel();
        	}
		}
    }

    public void ConnectionUDPRepeaters_CheckForIdle() {
    	synchronized (TConnectionUDPRepeater.Repeaters) {
        	for (int I = 0; I < TConnectionUDPRepeater.Repeaters.size(); I++) {
        		TConnectionUDPRepeater CR = TConnectionUDPRepeater.Repeaters.get(I);
        		if (CR.IsIdle()) 
        			CR.Destroy();
        	}
    	}
    }
    
    public TUDPConnectionRepeater UDPConnectionRepeaters_Add(int ConnectionType, int ReceivingPort, int ReceivingPacketSize, String Address, int TransmittingPort, int TransmittingPacketSize, String pServerAddress, int pServerPort, int ConnectionID) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	if (Address.equals("127.0.0.1") && ((ReceivingPort >= LocalVirtualConnection_PortBase) || (TransmittingPort >= LocalVirtualConnection_PortBase)))
    		return LocalVirtualUDPConnection_GetRepeater(ConnectionType, ReceivingPort,ReceivingPacketSize, TransmittingPort,TransmittingPacketSize, this, pServerAddress,pServerPort, ConnectionID); //. ->
    	else 
    		switch (ConnectionType) {

    		case LANCONNECTIONMODULE_CONNECTIONTYPE_NORMAL: 
        		return (new TLANUDPConnectionRepeater(this, ReceivingPort,ReceivingPacketSize,  Address,TransmittingPort,TransmittingPacketSize, pServerAddress,pServerPort,ConnectionID)); //. ->
    			
    		case LANCONNECTIONMODULE_CONNECTIONTYPE_PACKETTED: 
        		return (new TLANUDPConnectionRepeater1(this, ReceivingPort,ReceivingPacketSize,  Address,TransmittingPort,TransmittingPacketSize, pServerAddress,pServerPort,ConnectionID)); //. ->

    		default: 
    			return null; //. ->
    		}
    }
    
    public void UDPConnectionRepeaters_Remove(TUDPConnectionRepeater CR) {
    	CR.Destroy();
    }

    public void UDPConnectionRepeaters_RemoveAll() throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	synchronized (TUDPConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TUDPConnectionRepeater.Repeaters.size(); I++)
        		TUDPConnectionRepeater.Repeaters.get(I).Destroy();
		}
    }
    
    public void UDPConnectionRepeaters_Cancel(int ConnectionID) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	synchronized (TUDPConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TUDPConnectionRepeater.Repeaters.size(); I++) {
        		TUDPConnectionRepeater CR = TUDPConnectionRepeater.Repeaters.get(I);
        		if (CR.ConnectionID == ConnectionID)
        			CR.Cancel();
        	}
		}
    }

    public void UDPConnectionRepeaters_CancelByReceivingPort(int ReceivingPort) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	ArrayList<TLANUDPConnectionRepeater1> RepeatersToCancel = new ArrayList<TLANUDPConnectionRepeater1>(1);
    	synchronized (TUDPConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TUDPConnectionRepeater.Repeaters.size(); I++) {
        		TUDPConnectionRepeater CR = TUDPConnectionRepeater.Repeaters.get(I);
        		if ((CR instanceof TLANUDPConnectionRepeater1) && ((((TLANUDPConnectionRepeater1)CR).GetReceivingPort() == ReceivingPort)))
        			RepeatersToCancel.add(((TLANUDPConnectionRepeater1)CR));
        	}
		}
    	for (int I = 0; I < RepeatersToCancel.size(); I++)
    		RepeatersToCancel.get(I).CancelAndWait();
    }

    public void UDPConnectionRepeaters_CheckForIdle() {
    	synchronized (TUDPConnectionRepeater.Repeaters) {
        	for (int I = 0; I < TUDPConnectionRepeater.Repeaters.size(); I++) {
        		TUDPConnectionRepeater CR = TUDPConnectionRepeater.Repeaters.get(I);
        		if (CR.IsIdle()) 
        			CR.Destroy();
        	}
    	}
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
		String CFN = ModuleFile();
		File F = new File(CFN);
		if (!F.exists()) 
			return; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(CFN);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		@SuppressWarnings("unused")
		Element RootNode = XmlDoc.getDocumentElement();
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("LANModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		case 1:
			try {
			}
			catch (Exception E) {
			}
			break; //. >
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
		return; 
    }
    
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
        Serializer.startTag("", "LANModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        Serializer.endTag("", "LANModule");
    }
}
