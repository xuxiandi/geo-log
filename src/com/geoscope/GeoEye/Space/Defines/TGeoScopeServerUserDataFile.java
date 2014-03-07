package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserLocation;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.Utils.TDataConverter;
import com.geoscope.Utils.TFileSystem;

public class TGeoScopeServerUserDataFile {

	private static final int PrototypeID = 10; //. built into the project
	
	private TGeoScopeServerUser User;
	//.
	private String DataFileName;
	
	public TGeoScopeServerUserDataFile(TGeoScopeServerUser pUser, String pDataFileName) {
		User = pUser;
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
	
	public void Send(TDEVICEModule Device) throws Exception {
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
				int Size = TNetworkConnection.InputStream_Read(in, Data,Data.length);
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
}
