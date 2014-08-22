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

	private static final int PrototypeID = 10; //. built into the project
	
	private TGeoScopeServerUser User;
	//.
	private double Timestamp;
	private String DataFileName;
	
	public TGeoScopeServerUserDataFile(TGeoScopeServerUser pUser, double pTimestamp, String pDataFileName) {
		User = pUser;
		//.
		Timestamp = pTimestamp;
		DataFileName = pDataFileName;
	}
	
	public void Destroy() {
	}
	
	private String PrepareSendURL(String DataType, TUserLocation UserLocation) {
		String URL1 = User.Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(User.UserID);
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
	private void Send(TDEVICEModule Device) throws Exception {
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
		String CommandURL = PrepareSendURL(DataType, UserLocation);
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
				int ID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 8; //. Int64
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
	
	public void SendViaDevice(TDEVICEModule Device) throws Exception {
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
        }
        else
            Params = "2"/*Version*/+","+Integer.toString(PrototypeID);
        //.
        byte[] AddressData = Params.getBytes("windows-1251");
		//.
        TMapPOIDataFileValue MapPOIDataFile = new TMapPOIDataFileValue(Timestamp,DataFileName);
        TObjectSetGetMapPOIDataFileSO SO = new TObjectSetGetMapPOIDataFileSO(Device.ConnectorModule,User.UserID,User.UserPassword, Device.ObjectID, null, AddressData);
        SO.setValue(MapPOIDataFile);
        //. enqueue the data-file
        Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
        Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
        Device.BackupMonitor.BackupImmediate();
	}
}
