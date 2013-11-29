package com.geoscope.GeoEye.Space.Defines;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import com.geoscope.GeoEye.R;
import com.geoscope.Utils.TDataConverter;

import android.content.Context;

public class TComponentTypedDataFiles {
	
	public Context context;
	public int DataModel;
	public int DataType = SpaceDefines.TYPEDDATAFILE_TYPE_AllName;
	public TComponentTypedDataFile Items[];
	
	public TComponentTypedDataFiles(Context pcontext, int pDataModel) {
		context = pcontext;
		DataModel = pDataModel;
	}
	
	public TComponentTypedDataFiles(Context pcontext, int pDataModel, int pDataType) {
		context = pcontext;
		DataModel = pDataModel;
		DataType = pDataType;
	}
	
	public void PrepareFromByteArrayV0(byte[] BA, int Index) throws IOException {
		int Idx = Index;
		short ItemsCount = TDataConverter.ConvertBEByteArrayToInt16(BA,Idx); Idx += 2;
		Items = new TComponentTypedDataFile[ItemsCount];
		for (int I = 0; I < ItemsCount; I++) {
			Items[I] = new TComponentTypedDataFile(this);
			Idx = Items[I].PrepareFromByteArrayV0(BA, Idx);
		}
	}

	public void PrepareFromByteArrayV0(byte[] BA) throws IOException {
		PrepareFromByteArrayV0(BA,0);
	}
	
	public void PrepareForComponent(int idTComponent, int idComponent, boolean flWithComponents, TGeoScopeServer Server) throws Exception {
		String URL1 = Server.Address;
		// . add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/* URLProtocolVersion */+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "Functionality"+"/"+"ComponentDataDocument.dat";
		// . add command parameters
		int WithComponentsFlag = 0;
		if (flWithComponents)
			WithComponentsFlag = 1;
		URL2 = URL2+"?"+"1"/* command version */+","+Integer.toString(idTComponent)+","+Integer.toString(idComponent)+","+Integer.toString(DataModel)+","+Integer.toString(DataType)+","+Integer.toString(WithComponentsFlag);
		// .
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = Server.User.EncryptBufferV2(URL2_Buffer);
		// . encode string
		StringBuffer sb = new StringBuffer();
		for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
			String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
			while (h.length() < 2)
				h = "0"+h;
			sb.append(h);
		}
		URL2 = sb.toString();
		// .
		String URL = URL1+"/"+URL2+".dat";
		//.
		HttpURLConnection Connection = Server.OpenConnection(URL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int RetSize = Connection.getContentLength();
				if (RetSize == 0) 
					return; // . ->
				byte[] Data = new byte[RetSize];
				int Size;
				int SummarySize = 0;
				int ReadSize;
				while (SummarySize < Data.length) {
					ReadSize = Data.length-SummarySize;
					Size = in.read(Data, SummarySize, ReadSize);
					if (Size <= 0)
						throw new Exception(context.getString(R.string.SConnectionIsClosedUnexpectedly)); // => 
					//.
					SummarySize += Size;
				}
				// .
				PrepareFromByteArrayV0(Data);
			} finally {
				in.close();
			}
		} finally {
			Connection.disconnect();
		}
	}
}
