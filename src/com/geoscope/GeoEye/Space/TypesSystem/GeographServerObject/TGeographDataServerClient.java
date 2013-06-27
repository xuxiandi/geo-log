package com.geoscope.GeoEye.Space.TypesSystem.GeographServerObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

import android.content.Context;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.Utils.TDataConverter;

public class TGeographDataServerClient {
	
	public static final int DefaultPort = 5000;
	public static final int ServerReadWriteTimeout = 1000*60; //. Seconds

	public static final short SERVICE_NONE                          = 0;
	public static final short SERVICE_SETVIDEORECORDERDATA_V1       = 1;
	public static final short SERVICE_GETVIDEORECORDERDATA_V1       = 1001;
	public static final short SERVICE_GETVIDEORECORDERDATA_V2       = 1002;
	//.
	public static final int SERVICE_GETVIDEORECORDERDATA_V1_COMMAND_GETMEASUREMENTLIST    = 1;
	public static final int SERVICE_GETVIDEORECORDERDATA_V1_COMMAND_GETMEASUREMENTDATA    = 2;
	public static final int SERVICE_GETVIDEORECORDERDATA_V1_COMMAND_DELETEMEASUREMENTDATA = 3;
	//.
	public static final int SERVICE_GETVIDEORECORDERDATA_V2_COMMAND_GETMEASUREMENTLIST    = 1;
	public static final int SERVICE_GETVIDEORECORDERDATA_V2_COMMAND_GETMEASUREMENTDATA    = 2;
	public static final int SERVICE_GETVIDEORECORDERDATA_V2_COMMAND_DELETEMEASUREMENTDATA = 3;
	//.
	public static final int MESSAGE_DISCONNECT = 0;
	//. error messages
	public static final int MESSAGE_OK                    = 0;
	public static final int MESSAGE_ERROR                 = -1;
	public static final int MESSAGE_UNKNOWNSERVICE        = -10;
	public static final int MESSAGE_AUTHENTICATIONFAILED  = -11;
	public static final int MESSAGE_ACCESSISDENIED        = -12;
	public static final int MESSAGE_TOOMANYCLIENTS        = -13;
	public static final int MESSAGE_UNKNOWNCOMMAND        = -14;
	public static final int MESSAGE_WRONGPARAMETERS       = -15;
	public static final int MESSAGE_SAVINGDATAERROR       = -101;
	  
	public static class TVideoRecorderMeasurementDescriptor {
		public String ID;
		public double StartTimestamp;
		public double FinishTimestamp;
		public int AudioSize;
		public int VideoSize;
		public double CPC;
	}
	
	public static class TItemProgressor {
		
		public void DoOnItemIsStarted(String ItemName, int ItemIndex, int ItemCount) {};
		public void DoOnItemIsFinished() {};
		
		public void DoOnItemProgress(int Progress) {};
	}
	
	private Context context;
  	//.
	public String 	ServerAddress;
	public int		ServerPort = DefaultPort;
	//.
	private int 	UserID;
	private String 	UserPassword;
	//.
	private int idGeographServerObject;
	//.
    private Socket 		Connection;
    public InputStream 	ConnectionInputStream;
    public OutputStream ConnectionOutputStream;
	
	public TGeographDataServerClient(Context pcontext, String pServerAddress, int pServerPort, int pUserID, String pUserPassword, int pidGeographServerObject) {
		context = pcontext;
		//.
		ServerAddress = pServerAddress;
		if (pServerPort > 0)
			ServerPort = pServerPort;
		//.
		UserID = pUserID;
		UserPassword = pUserPassword;
		//.
		idGeographServerObject = pidGeographServerObject;
	}
	
    public void Destroy() {
    }

	private short Buffer_GetCRC(byte[] buffer, int Offset, int Size) {
        int CRC = 0;
        int V;
        int Idx  = Offset;
        while (Idx < (Offset+Size))
        {
            V = (int)(buffer[Idx] & 0x000000FF);
            CRC = (((CRC+V) << 1)^V);
            //.
            Idx++;
        }
        return (short)CRC;
	}
	
	private void Buffer_Encrypt(byte[] buffer, int Offset, int Size, String UserPassword) throws UnsupportedEncodingException {
        int StartIdx = Offset;
        byte[] UserPasswordArray;
        UserPasswordArray = UserPassword.getBytes("windows-1251");
        //.
        if (UserPasswordArray.length > 0)
        {
            int UserPasswordArrayIdx = 0;
            for (int I = StartIdx; I < (StartIdx+Size); I++)
            {
                buffer[I] = (byte)(buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
                UserPasswordArrayIdx++;
                if (UserPasswordArrayIdx >= UserPasswordArray.length) 
                	UserPasswordArrayIdx = 0;
            }
        }
	}
	
	private void InputStream_ReadData(InputStream in, byte[] Data, int DataSize, TItemProgressor ItemProgressor, TCanceller Canceller) throws Exception {
        int Size;
        int SummarySize = 0;
        int ReadSize;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            Size = in.read(Data,SummarySize,ReadSize);
            if (Size <= 0) 
            	throw new Exception(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
            SummarySize += Size;
            //.
            if (ItemProgressor != null)
            	ItemProgressor.DoOnItemProgress((int)(100*SummarySize/DataSize));
            if ((Canceller != null) && Canceller.flCancel)
				throw new CancelException(); //. =>
        }
	}

	private void InputStream_ReadData(InputStream in, OutputStream out, int DataSize, TItemProgressor ItemProgressor, TCanceller Canceller) throws Exception {
        byte[] Data = new byte[1024*1024];
        int Size;
        int SummarySize = 0;
        int ReadSize;
        while (SummarySize < DataSize) {
            ReadSize = DataSize-SummarySize;
            if (ReadSize >= Data.length)
                Size = in.read(Data);
            else
            	Size = in.read(Data,0,ReadSize);
            if (Size <= 0) 
            	throw new Exception(context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
            //.
            out.write(Data,0,Size);
            //.
            SummarySize += Size;
            //.
            if (ItemProgressor != null)
            	ItemProgressor.DoOnItemProgress((int)(100*SummarySize/DataSize));
            if ((Canceller != null) && Canceller.flCancel)
				throw new CancelException(); //. =>
        }
	}

	private void Connect() throws Exception {
        Connection = new Socket(ServerAddress,ServerPort); 
        Connection.setSoTimeout(ServerReadWriteTimeout);
        Connection.setKeepAlive(true);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
	}

	private void Disconnect() throws IOException {
        //. close connection gracefully
		try {
	        byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(MESSAGE_DISCONNECT);
	        ConnectionOutputStream.write(BA);
	        ConnectionOutputStream.flush();
		}
		catch (Exception E) {}
        //.
        ConnectionOutputStream.close();
        ConnectionInputStream.close();
        Connection.close();
	}	
	
	public TVideoRecorderMeasurementDescriptor[] SERVICE_GETVIDEORECORDERDATA_GetMeasurementList(TCanceller Canceller) throws Exception {
		Connect();
		try {
	        //. send login info
	    	byte[] LoginBuffer = new byte[24];
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_GETVIDEORECORDERDATA_V2);
			System.arraycopy(BA,0, LoginBuffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(UserID);
			System.arraycopy(BA,0, LoginBuffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(idGeographServerObject);
			System.arraycopy(BA,0, LoginBuffer,10, BA.length);
			short CRC = Buffer_GetCRC(LoginBuffer, 10,8);
			BA = TDataConverter.ConvertInt16ToBEByteArray(CRC);
			System.arraycopy(BA,0, LoginBuffer,18, BA.length);
			Buffer_Encrypt(LoginBuffer,10,10,UserPassword);
			BA = TDataConverter.ConvertInt32ToBEByteArray(SERVICE_GETVIDEORECORDERDATA_V2_COMMAND_GETMEASUREMENTLIST);
			System.arraycopy(BA,0, LoginBuffer,20, BA.length);
			ConnectionOutputStream.write(LoginBuffer);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor != MESSAGE_OK)
				throw new Exception(context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
			//.
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			TVideoRecorderMeasurementDescriptor[] Result;
			if (Descriptor > 0) {
				BA = new byte[Descriptor];
				InputStream_ReadData(ConnectionInputStream,BA,BA.length,null,Canceller);
				String ResultString = new String(BA,"US-ASCII");
				String[] Items = ResultString.split(";");
				Result = new TVideoRecorderMeasurementDescriptor[Items.length];
				for (int I = 0; I < Items.length; I++) {
					String[] Properties = Items[I].split(",");
					Result[I] = new TVideoRecorderMeasurementDescriptor();
					Result[I].ID = Properties[0];
					Result[I].StartTimestamp = Double.parseDouble(Properties[1]);
					Result[I].FinishTimestamp = Double.parseDouble(Properties[2]);
					Result[I].AudioSize = Integer.parseInt(Properties[3]);
					Result[I].VideoSize = Integer.parseInt(Properties[4]);
					Result[I].CPC = Double.parseDouble(Properties[5]);
				}
			}
			else
				Result = new TVideoRecorderMeasurementDescriptor[0];
			return Result;
		}
		finally {
			Disconnect();
		}
	}
	
	public void SERVICE_GETVIDEORECORDERDATA_GetMeasurementData(double MeasurementID, int MeasurementFlags, double MeasurementStartTimestamp, double MeasurementFinishTimestamp, String MeasurementFolder, TItemProgressor ItemProgressor, TCanceller Canceller) throws Exception {
		Connect();
		try {
			byte[] Params = new byte[24+28];
			//. prepare login data
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(SERVICE_GETVIDEORECORDERDATA_V2);
			System.arraycopy(BA,0, Params,0, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(UserID);
			System.arraycopy(BA,0, Params,2, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(idGeographServerObject);
			System.arraycopy(BA,0, Params,10, BA.length);
			short CRC = Buffer_GetCRC(Params, 10,8);
			BA = TDataConverter.ConvertInt16ToBEByteArray(CRC);
			System.arraycopy(BA,0, Params,18, BA.length);
			Buffer_Encrypt(Params,10,10,UserPassword);
			BA = TDataConverter.ConvertInt32ToBEByteArray(SERVICE_GETVIDEORECORDERDATA_V2_COMMAND_GETMEASUREMENTDATA);
			System.arraycopy(BA,0, Params,20, BA.length);
			//. prepare params data
			int Idx = 24;
			BA = TDataConverter.ConvertDoubleToBEByteArray(MeasurementID);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertInt32ToBEByteArray(MeasurementFlags);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertDoubleToBEByteArray(MeasurementStartTimestamp);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			BA = TDataConverter.ConvertDoubleToBEByteArray(MeasurementFinishTimestamp);
			System.arraycopy(BA,0, Params,Idx, BA.length); Idx += BA.length;
			//. send data
			ConnectionOutputStream.write(Params);
			//. check login
			byte[] DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			int Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor != MESSAGE_OK)
				throw new Exception(context.getString(R.string.SDataServerConnectionError)+Integer.toString(Descriptor)); //. =>
			//.
			DecriptorBA = new byte[4];
			ConnectionInputStream.read(DecriptorBA);
			Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
			if (Descriptor < 0)
				throw new Exception("GeographDataServer error, RC: "+Integer.toString(Descriptor)); //. =>
			int ItemCount = Descriptor;
			for (int I = 0; I < ItemCount; I++) {
				ConnectionInputStream.read(DecriptorBA);
				Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
				BA = new byte[Descriptor];
				if (Descriptor > 0) 
					InputStream_ReadData(ConnectionInputStream,BA,BA.length,null,Canceller);
				String FileName = new String(BA,"US-ASCII");
				//.
				ConnectionInputStream.read(DecriptorBA);
				Descriptor = TDataConverter.ConvertBEByteArrayToInt32(DecriptorBA,0);
				File F = new File(MeasurementFolder+"/"+FileName);
				try {
					FileOutputStream FOS = new FileOutputStream(F);
					try {
						if (Descriptor > 0) {
							if (ItemProgressor != null)
								ItemProgressor.DoOnItemIsStarted(FileName, I+1, ItemCount);
							try {
								InputStream_ReadData(ConnectionInputStream,FOS,Descriptor,ItemProgressor,Canceller);
							}
							finally {
								if (ItemProgressor != null)
									ItemProgressor.DoOnItemIsFinished();
							}
						}
					}
					finally {
						FOS.close();
					}
				}
				catch (Exception E) {
					F.delete();
					throw E; //. =>
				}
			}
		}
		finally {
			Disconnect();
		}
	}
}
