package com.geoscope.GeoEye.Space.TypesSystem.SecurityFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.TTypeSystem;
import com.jcraft.jzlib.ZInputStream;

public class TTSecurityFileFunctionality extends TTypeFunctionality {
	
	public TTSecurityFileFunctionality(TTypeSystem pTypeSystem) {
		super(pTypeSystem);
	}
	
	@Override
	public TComponentFunctionality TComponentFunctionality_Create(long idComponent) {
		return (new TSecurityFileFunctionality(this,idComponent));
	}

	@Override
	public int GetImageResID() {
		return R.drawable.securityfile;
	}
	
	public long[] GetInstanceListByContext(String Context) throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Long.toString(Server.User.UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTSecurityFile)+"/"+"InstanceList.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+"1"/*command sub-version*/+","+Context;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
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
		//.
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				ByteArrayInputStream BIS = new ByteArrayInputStream(Data);
				try {
					ZInputStream ZIS = new ZInputStream(BIS);
					try {
						byte[] Buffer = new byte[8192];
						int ReadSize;
						ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
						try {
							while ((ReadSize = ZIS.read(Buffer)) > 0) 
								BOS.write(Buffer, 0,ReadSize);
							//.
							Data = BOS.toByteArray();
							int Idx = 0;
							int ItemsCount = Data.length/4/*SizeOf(Int32)*/;
							long[] Result = new long[ItemsCount];
							for (int I = 0; I < ItemsCount; I++) {
								int SecurityFileID = TDataConverter.ConvertLEByteArrayToInt32(Data, Idx); Idx += 4/*SizeOf(Int32)*/;
								Result[I] = SecurityFileID;
							}
							return Result; //. ->
						}
						finally {
							BOS.close();
						}
					}
					finally {
						ZIS.close();
					}
				}
				finally {
					BIS.close();
				}
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
