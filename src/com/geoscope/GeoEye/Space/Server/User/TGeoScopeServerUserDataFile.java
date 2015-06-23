package com.geoscope.GeoEye.Space.Server.User;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserLocation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOIDataFileSO;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.GPSModule.TMapPOIDataFileValue;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TGeoScopeServerUserDataFile {

	private static final int PrototypeID 				= 10; 	//. built into the project
	private static final int PrototypeForComponentID 	= 329; 	//. built into the project
	
	private TGeoScopeServerUser User = null;
	//.
	private double 	Timestamp;
	private String 	DataFileName;
	private long 	SecurityFileID;
	
	public TGeoScopeServerUserDataFile(TGeoScopeServerUser pUser, double pTimestamp, String pDataFileName, long pSecurityFileID) {
		User = pUser;
		//.
		Timestamp = pTimestamp;
		DataFileName = pDataFileName;
		SecurityFileID = pSecurityFileID;
	}
	
	public TGeoScopeServerUserDataFile(TGeoScopeServerUser pUser, double pTimestamp, String pDataFileName) {
		this(pUser, pTimestamp, pDataFileName, 0);
	}
	
	public TGeoScopeServerUserDataFile(double pTimestamp, String pDataFileName, long pSecurityFileID) {
		this(null, pTimestamp, pDataFileName, pSecurityFileID);	
	}
	
	public TGeoScopeServerUserDataFile(double pTimestamp, String pDataFileName) {
		this(pTimestamp, pDataFileName, 0);
	}
	
	public void Destroy() {
	}
	
	private String PrepareCreateViaUserAgentURL(String DataType, TUserLocation UserLocation) {
		String URL1 = User.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Long.toString(User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTDATAFile)+"/"+"Co"+"/"+Integer.toString(PrototypeID)+"/"+"Clone.dat";
		//. add command parameters
		if (UserLocation == null)
			URL2 = URL2+"?"+"1"/*command version*/+","+DataType;
		else 
			URL2 = URL2+"?"+"2"/*command version*/+","+DataType+","+UserLocation.ToFixString();
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	@SuppressWarnings("unused")
	private void CreateViaUserAgent(TDEVICEModule Device) throws Exception {
		TGPSFixValue GPSFix = null;
        if (!Device.GPSModule.flProcessingIsDisabled) {
            GPSFix = Device.GPSModule.GetCurrentFix();
            if (!(GPSFix.flSet && GPSFix.IsAvailable())) 
            	GPSFix = null;
        }
        //.
        TUserLocation UserLocation = null;
        if (GPSFix != null) {
        	UserLocation = new TUserLocation();
        	UserLocation.Datum = TGPSModule.DatumID;
        	UserLocation.AssignFromGPSFix(GPSFix);
        }
        //.
        String DataType = TFileSystem.FileName_GetExtension(DataFileName);
        //.
		String CommandURL = PrepareCreateViaUserAgentURL(DataType, UserLocation);
		//.
		HttpURLConnection Connection = User.Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(User.Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				int Idx = 0;
				int ID = TDataConverter.ConvertLEByteArrayToInt32(Data, Idx); Idx += 8; //. Int64
				//. enqueue file data for sending ...
				Device.ComponentFileStreaming.AddItem(SpaceDefines.idTDATAFile,ID, DataFileName);
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public void Create(TDEVICEModule Device) throws Exception {
		TGPSFixValue GPSFix = null;
        if (!Device.GPSModule.flProcessingIsDisabled) {
            GPSFix = Device.GPSModule.GetCurrentFix();
            if (!(GPSFix.flSet && GPSFix.IsAvailable())) 
            	GPSFix = null;
        }
        //.
        String Params;
        if (GPSFix != null) {
        	TUserLocation UserLocation = new TUserLocation();
        	UserLocation.Datum = TGPSModule.DatumID;
        	UserLocation.AssignFromGPSFix(GPSFix);
        	//.
            Params = "3"/*Version*/+","+Integer.toString(PrototypeID)+","+UserLocation.ToFixString();
            if (SecurityFileID != 0)
            	Params += ","+Long.toString(SecurityFileID); 
        }
        else {
            Params = "2"/*Version*/+","+Integer.toString(PrototypeID);
            if (SecurityFileID != 0)
            	Params += ","+Long.toString(SecurityFileID); 
        }
        //.
        byte[] AddressData = Params.getBytes("windows-1251");
		//.
        TMapPOIDataFileValue MapPOIDataFile = new TMapPOIDataFileValue(Timestamp,DataFileName);
        TObjectSetGetMapPOIDataFileSO SO = new TObjectSetGetMapPOIDataFileSO(Device.ConnectorModule,(int)Device.UserID,Device.UserPassword, Device.ObjectID, null, AddressData);
        SO.setValue(MapPOIDataFile);
        //. enqueue the data-file
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        Device.BackupMonitor.BackupImmediate();
	}

	public void CreateAsComponent(int idTOwner, long idOwner, TDEVICEModule Device) throws Exception {
		TGPSFixValue GPSFix = null;
        if (!Device.GPSModule.flProcessingIsDisabled) {
            GPSFix = Device.GPSModule.GetCurrentFix();
            if (!(GPSFix.flSet && GPSFix.IsAvailable())) 
            	GPSFix = null;
        }
        //.
        String Params;
        if (GPSFix != null) {
        	TUserLocation UserLocation = new TUserLocation();
        	UserLocation.Datum = TGPSModule.DatumID;
        	UserLocation.AssignFromGPSFix(GPSFix);
        	//.
            Params = "5"/*Version*/+","+Integer.toString(idTOwner)+","+Long.toString(idOwner)+","+Integer.toString(PrototypeForComponentID)+","+UserLocation.ToFixString();
            if (SecurityFileID != 0)
            	Params += ","+Long.toString(SecurityFileID); 
        }
        else {
            Params = "4"/*Version*/+","+Integer.toString(idTOwner)+","+Long.toString(idOwner)+","+Integer.toString(PrototypeForComponentID);
            if (SecurityFileID != 0)
            	Params += ","+Long.toString(SecurityFileID); 
        }
        //.
        byte[] AddressData = Params.getBytes("windows-1251");
		//.
        TMapPOIDataFileValue MapPOIDataFile = new TMapPOIDataFileValue(Timestamp,DataFileName);
        TObjectSetGetMapPOIDataFileSO SO = new TObjectSetGetMapPOIDataFileSO(Device.ConnectorModule,(int)Device.UserID,Device.UserPassword, Device.ObjectID, null, AddressData);
        SO.setValue(MapPOIDataFile);
        //. enqueue the data-file
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        Device.BackupMonitor.BackupImmediate();
	}
}
