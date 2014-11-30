package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.BaseVisualizationFunctionality;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

import android.graphics.BitmapFactory;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TSpaceObj;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.VisualizationsOptions.TBitmapDecodingOptions;

public class TBase2DVisualizationFunctionality extends TBaseVisualizationFunctionality {

	public static class TTransformatrix {
		
		public double Xbind;
		public double Ybind;
		public double Scale;
		public double Rotation;
		public double TranslateX;
		public double TranslateY;
		
		public TTransformatrix(double pXbind, double pYbind, double pScale, double pRotation, double pTranslateX, double pTranslateY) {
			Xbind = pXbind;
			Ybind = pYbind;
			Scale = pScale;
			Rotation = pRotation;
			TranslateX = pTranslateX;
			TranslateY = pTranslateY;
		}
	}
	
	
	private TSpaceObj Obj = null;
	
	public TBase2DVisualizationFunctionality(TTypeFunctionality pTypeFunctionality, long pidComponent) {
		super(pTypeFunctionality, pidComponent);
	}

	public void Context_SetObj(long Ptr, TSpaceObj Obj) {
		Space().Objects_Set(Ptr, Obj);
	}
	
	public TSpaceObj Context_GetObj() {
		return Space().Objects_Get(idTComponent(),idComponent);
	}
	
	public TSpaceObj Context_GetObj(int ObjContainerImageMaxSize) {
		TSpaceObj Result = Space().Objects_Get(idTComponent(),idComponent);
		if (Result == null)
			return null; //. ->
		if (Result.Container_Image == null)
			return null; //. ->
		int MaxSize = Result.Container_Image.getWidth();
		if (Result.Container_Image.getHeight() > MaxSize)
			MaxSize = Result.Container_Image.getHeight();
		if (MaxSize != ObjContainerImageMaxSize)
			return null; //. ->
		//.
		return Result;
	}
	
	public TSpaceObj Server_GetObj(int ObjContainerImageMaxSize) throws Exception {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(Server.User.UserID);
		String URL2 = "Functionality"+"/"+"VisualizationData.dat";
		//. add command parameters
		synchronized (this) {
			URL2 = URL2+"?"+"2"/*command version*/+","+Integer.toString(TypeFunctionality.idType)+","+Long.toString(idComponent)+","+Integer.toString(ObjContainerImageMaxSize);
		}
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
		HttpURLConnection HttpConnection = Server.OpenConnection(URL);
		try {
			InputStream in = HttpConnection.getInputStream();
			try {
				byte[] Data = new byte[HttpConnection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				int Idx = 0;
	            Ptr = TDataConverter.ConvertLEByteArrayToInt64(Data,Idx); Idx += 8;
	            if (Ptr == SpaceDefines.nilPtr) 
		            return null; //. ->
	            //.
	            TSpaceObj Result = new TSpaceObj(Ptr);
	            Idx = Result.SetFromByteArray(Data,Idx);
	            //.
	            int ContainerImageSize = TDataConverter.ConvertLEByteArrayToInt32(Data,Idx); Idx += 4;
	            if (ContainerImageSize > 0) 
	            	Result.Container_Image = BitmapFactory.decodeByteArray(Data, Idx,ContainerImageSize, TBitmapDecodingOptions.GetBitmapFactoryOptions());
	            //.
	            Obj = Result;
	            //.
	            Context_SetObj(Ptr, Obj);
	            //.
	            return Result; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			HttpConnection.disconnect();
		}
	}
	
	public TSpaceObj GetObj(int ObjContainerImageMaxSize) throws Exception {
		TSpaceObj Result = Obj;
		if (Result != null) {
			if (Result.Container_Image != null) {
				int MaxSize = Result.Container_Image.getWidth();
				if (Result.Container_Image.getHeight() > MaxSize)
					MaxSize = Result.Container_Image.getHeight();
				if (MaxSize == ObjContainerImageMaxSize)
					return Result; //. ->
			}
		}
		//.
		Result = Context_GetObj(ObjContainerImageMaxSize);
		if (Result != null)
			return Result; //. ->
		//.
		Result = Server_GetObj(ObjContainerImageMaxSize);
		//.
		return Result;
	}
}
