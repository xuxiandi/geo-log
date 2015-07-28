package com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Image.TImageViewerPanel;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.MeasurementProcessor.TMeasurementProcessorPanel;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TComponentTypedDataFile {

	public TComponentTypedDataFiles TypedDataFiles;
	//.
	public int		DataComponentType;
	public long		DataComponentID;
	public int		DataType;
	public String 	DataParams = null;
	public String 	DataFormat = "";
	public String 	DataName = "";
	public byte[]	Data = null;
	public String 	DataFileName = null;
	//.
	public TComponentTypedDataFile[] Components = null;
	
	public TComponentTypedDataFile(TComponentTypedDataFiles pTypedDataFiles) {
		TypedDataFiles = pTypedDataFiles;
	}
	
	public TComponentTypedDataFile(TComponentTypedDataFiles pTypedDataFiles, int	pDataType) {
		TypedDataFiles = pTypedDataFiles;
		//.
		DataType = pDataType;
	}
	
	public TComponentTypedDataFile Clone() {
		TComponentTypedDataFile _Clone = new TComponentTypedDataFile(TypedDataFiles);
		//.
		_Clone.DataComponentType = DataComponentType;
		_Clone.DataComponentID = DataComponentID;
		_Clone.DataType = DataType;
		_Clone.DataFormat = DataFormat;
		_Clone.DataName = DataName;
		_Clone.Data = Data;
		_Clone.DataFileName = DataFileName;
		//.
		return _Clone;
	}
	
	public String FileName() {
		return ("DataFile"+Long.toString(DataComponentID)+DataFormat);
	}

	public boolean DataIsNull() {
		return (Data == null);
	}
	
	public boolean DataActualityIsExpired() {
		return DataIsNull();
	}
	
	public boolean IsLoaded() {
		return ((DataFileName != null) | (Data != null));
	}
	
	public boolean IsFSFile() {
		return (DataFileName != null);
	}
	
	public int FromByteArrayV0(byte[] BA, int Idx) throws IOException {
		DataComponentType = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
		DataComponentID = TDataConverter.ConvertLEByteArrayToInt64(BA,Idx); Idx += 8; //. native ComponentID is Int64
		DataType = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
		byte ItemFormatSize = BA[Idx]; Idx++;
		DataFormat = (new String(BA,Idx,ItemFormatSize,"windows-1251")).toUpperCase(Locale.US); Idx += ItemFormatSize;
		short ItemNameSize = TDataConverter.ConvertLEByteArrayToInt16(BA,Idx); Idx += 2;
		DataName = new String(BA,Idx,ItemNameSize,"windows-1251"); Idx += ItemNameSize;
		int DataSize = TDataConverter.ConvertLEByteArrayToInt32(BA,Idx); Idx += 4;
		if (DataSize > 0) {
			Data = new byte[DataSize];
			System.arraycopy(BA,Idx, Data,0, DataSize); Idx += DataSize;
		}
		else 
			Data = null;
		return Idx;
	}

	public void FromByteArrayV0(byte[] BA) throws Exception,IOException {
		int Idx = 0;
		int ItemsCounter = TDataConverter.ConvertLEByteArrayToInt16(BA,Idx); Idx += 2;
		if (ItemsCounter != 1)
			throw new Exception("item count is not equal to 1"); //. =>
		FromByteArrayV0(BA,Idx);
	}
	
	public byte[] ToByteArrayV0() throws IOException {
		byte[] DataFormatBA = null;
		byte DataFormatSize = 0;
		if (DataFormat != null) {
			DataFormatBA = DataFormat.getBytes("windows-1251");
			DataFormatSize = (byte)DataFormatBA.length;
		}
		byte[] DataNameBA = null;
		short DataNameSize = 0;
		if (DataName != null) {
			DataNameBA = DataName.getBytes("windows-1251");
			DataNameSize = (byte)DataNameBA.length;
		}
		int DataSize = 0;
		if (Data != null) 
			DataSize = Data.length;
		//.
		byte[] Result = new byte[4/*SizeOf(DataComponentType)*/+8/*SizeOf(DataComponentID)*/+4/*SizeOf(DataType)*/+1/*SizeOf(DataFormat)*/+DataFormatSize+2/*SizeOf(DataName)*/+DataNameSize+4/*SizeOf(DataSize)*/+DataSize];
		int Idx = 0;
		byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(DataComponentType); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		//.
		BA = TDataConverter.ConvertInt64ToLEByteArray(DataComponentID); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length; //. SizeOf(Int64)
		//.
		BA = TDataConverter.ConvertInt32ToLEByteArray(DataType); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		//.
		Result[Idx] = DataFormatSize; Idx++;
		if (DataFormatSize > 0) {
			System.arraycopy(DataFormatBA,0, Result,Idx, DataFormatSize); Idx += DataFormatSize;
		}
		//.
		BA = TDataConverter.ConvertInt16ToLEByteArray(DataNameSize); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (DataNameSize > 0) {
			System.arraycopy(DataNameBA,0, Result,Idx, DataNameSize); Idx += DataNameSize;
		}
		//.
		BA = TDataConverter.ConvertInt32ToLEByteArray(DataSize); System.arraycopy(BA,0, Result,Idx, BA.length); Idx += BA.length;
		if (DataSize > 0) {
			System.arraycopy(Data,0, Result,Idx, DataSize); Idx += DataSize;
		}
		//.
		return Result;
	}	

	public void PrepareAsName() {
		//. convert to name data
		if ((DataType % SpaceDefines.TYPEDDATAFILE_TYPE_RANGE) != SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_Name)
			DataType = ((int)(DataType/SpaceDefines.TYPEDDATAFILE_TYPE_RANGE))*SpaceDefines.TYPEDDATAFILE_TYPE_RANGE;
	}
	
	public void PrepareAsFull() {
		// . convert to full data
		if ((DataType % SpaceDefines.TYPEDDATAFILE_TYPE_RANGE) != SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)
			DataType = ((int)(DataType/SpaceDefines.TYPEDDATAFILE_TYPE_RANGE))*SpaceDefines.TYPEDDATAFILE_TYPE_RANGE+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
	}
	
	public void PrepareAsFullFromFile(String pFileName) {
		DataFileName = pFileName;
		// . convert to full data
		if ((DataType % SpaceDefines.TYPEDDATAFILE_TYPE_RANGE) != SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)
			DataType = ((int)(DataType/SpaceDefines.TYPEDDATAFILE_TYPE_RANGE))*SpaceDefines.TYPEDDATAFILE_TYPE_RANGE+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
	}
	
	public void PrepareAsFullFromServer(TGeoScopeServerUser User, TCanceller Canceller, TProgressor Progressor) throws Exception {
		switch (DataComponentType) {

		case SpaceDefines.idTDATAFile:
			TGeoScopeServerInfo.TInfo ServersInfo = User.Server.Info.GetInfo();
			TComponentStreamServer CSS = new TComponentStreamServer(TypedDataFiles.context, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, User.UserID, User.UserPassword);
			try {
				String CFN = TTypesSystem.TypesSystem.SystemTDATAFile.Context_GetFolder()+"/"+FileName();
				//.
				CSS.ComponentStreamServer_GetComponentStream_Begin(DataComponentType,DataComponentID);
				try {
					File CF = new File(CFN);
					RandomAccessFile ComponentStream = new RandomAccessFile(CF,"rw");
					try {
						ComponentStream.seek(ComponentStream.length());
						//.
						CSS.ComponentStreamServer_GetComponentStream_Read(Long.toString(DataComponentID),ComponentStream, Canceller, Progressor);
					}
					finally {
						ComponentStream.close();
					}
				}
				finally {
					CSS.ComponentStreamServer_GetComponentStream_End();						
				}
				//.
				PrepareAsFullFromFile(CFN);
				//.
				if (Canceller != null)
					Canceller.Check();
			}
			finally {
				CSS.Destroy();
			}
			break; //. >

		default:
			String URL1 = User.Server.Address;
			//. add command path
			URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/* URLProtocolVersion */+"/"+Long.toString(User.UserID);
			String URL2 = "Functionality"+"/"+"ComponentDataDocument.dat";
			//. add command parameters
			int WithComponentsFlag = 0;
			URL2 = URL2+"?"+"1"/* command version */+","+Integer.toString(DataComponentType)+","+Long.toString(DataComponentID)+","+Integer.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)+","+Integer.toString(((int)(DataType/SpaceDefines.TYPEDDATAFILE_TYPE_RANGE))*SpaceDefines.TYPEDDATAFILE_TYPE_RANGE+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)+","+Integer.toString(WithComponentsFlag);
			//.
			byte[] URL2_Buffer;
			try {
				URL2_Buffer = URL2.getBytes("windows-1251");
			} catch (Exception E) {
				URL2_Buffer = null;
			}
			byte[] URL2_EncryptedBuffer = User.EncryptBufferV2(URL2_Buffer);
			//. encode string
			StringBuffer sb = new StringBuffer();
			for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
				String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
				while (h.length() < 2)
					h = "0"+h;
				sb.append(h);
			}
			URL2 = sb.toString();
			//.
			String URL = URL1+"/"+URL2+".dat";
			//.
			if (Canceller.flCancel)
				return; // . ->
			//.
			HttpURLConnection Connection = User.Server.OpenConnection(URL);
			try {
				if (Canceller.flCancel)
					return; // . ->
				// .
				InputStream in = Connection.getInputStream();
				try {
					if (Canceller.flCancel)
						return; // . ->
					// .
					int RetSize = Connection.getContentLength();
					if (RetSize > 0) {
						byte[] Data = new byte[RetSize];
						int Size;
						int SummarySize = 0;
						int ReadSize;
						while (SummarySize < Data.length) {
							ReadSize = Data.length - SummarySize;
							Size = in.read(Data, SummarySize, ReadSize);
							if (Size <= 0)
								throw new Exception(TypedDataFiles.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
							SummarySize += Size;
							//.
							if (Canceller.flCancel)
								return; // . ->
							//.
							if (Progressor != null) 
								Progressor.DoOnProgress((Integer)(100*SummarySize/Data.length));
						}
						//.
						FromByteArrayV0(Data);
					}
					else {
						PrepareAsFull();
						Data = null;
					}
				} finally {
					in.close();
				}
			} finally {
				Connection.disconnect();
			}
			break; //. >
		}
	}
	
	public void PrepareForComponent(int idTComponent, long idComponent, String pDataParams, boolean flWithComponents, TGeoScopeServer Server) throws Exception {
		DataParams = pDataParams;
		//.
		int Version = 0;
		TComponentFunctionality CF = Server.User.Space.TypesSystem.TComponentFunctionality_Create(idTComponent,idComponent);
		if (CF == null)
			return; //. ->
		try {
			if (!flWithComponents) {
				byte[] DataDocument = CF.Context_GetDataDocument(TypedDataFiles.DataModel, DataType, DataParams, flWithComponents, Version);
				if (DataDocument != null) {
					FromByteArrayV0(DataDocument);
					if (DataActualityIsExpired()) 
						DataDocument = null;
				}
				if (DataDocument == null) {
					DataDocument = CF.Server_GetDataDocument(TypedDataFiles.DataModel, DataType, DataParams, flWithComponents, Version);
					if (DataDocument != null)
						FromByteArrayV0(DataDocument);
				}
				return; //. ->
			}
			else {
				byte[] DataDocument = CF.Server_GetDataDocument(TypedDataFiles.DataModel, DataType, DataParams, flWithComponents, Version);
				if (DataDocument != null)
					FromByteArrayV0(DataDocument);
			}
		}
		finally {
			CF.Release();
		}
	}

	public void PrepareForComponent(int idTComponent, long idComponent, boolean flWithComponents, TGeoScopeServer Server) throws Exception {
		PrepareForComponent(idTComponent,idComponent, null, flWithComponents, Server);	
	}
	
	public File GetTempFile() {
		File TempFolder = new File(TGeoLogApplication.GetTempFolder());
		return (new File(TempFolder.getAbsolutePath()+"/"+"DataFile"+Long.toString(DataComponentID)+DataFormat));	
	}
	
	private File CreateTempFile() throws Exception {
		if (Data == null)
			throw new Exception("data file is null"); //. =>
		File TempFile = GetTempFile();
		FileOutputStream fos = new FileOutputStream(TempFile);
		try {
			fos.write(Data,0,Data.length);
		}
		finally {
			fos.close();
		}
		return TempFile;
	}
	
	public File GetFile() throws Exception {
		if (DataFileName != null)
			return new File(DataFileName); //. ->
		else
			return CreateTempFile(); //. ->
	}

	public byte[] GetFileData() throws Exception {
		if (DataFileName != null) {
			File F = new File(DataFileName);
			byte[] _Data = new byte[(int)F.length()];
			FileInputStream FIS = new FileInputStream(F);
			try {
				FIS.read(_Data);
			}
			finally {
				FIS.close();
			}
			return _Data; //. ->
		}
		else
			return Data; //. ->
	}
	
	public boolean FileIsEmpty() throws Exception {
		if (DataFileName != null) {
			File F = new File(DataFileName);
			return (!(F.exists() && (F.length() > 0))); //. ->
		}
		else
			return (!((Data != null) && (Data.length > 0))); //. ->
	}
	
	public Bitmap AsImageBitmap(int ImageMaxSize) throws Exception {
		if (DataType == SpaceDefines.TYPEDDATAFILE_TYPE_Image) {
			if ((DataFormat != null) && (Data != null)) {
				if (DataFormat.equals(TDrawingDefines.DataFormat)) {
					TDrawings Drawings = new TDrawings();
					Drawings.LoadFromByteArray(Data,0);
					return Drawings.ToBitmap(ImageMaxSize); //. ->
				}
				else
					return BitmapFactory.decodeByteArray(Data, 0,Data.length); //. ->		
			}
			else
				return null; //. ->
		}
		else
			return null; //. ->
	}
	
	public void Open(TGeoScopeServerUser User, Context context) {
		try {
			if (!FileIsEmpty()) {
				Intent intent = null;
				switch (DataType) {

				case SpaceDefines.TYPEDDATAFILE_TYPE_Document:
					try {
						if (DataFormat.equals(SpaceDefines.TYPEDDATAFILE_TYPE_Document_FORMAT_TXT)) {
							String Text = new String(GetFileData(),"windows-1251");
							byte[] TextData = Text.getBytes("utf-16");
							// .
							File TempFile = GetTempFile();
							FileOutputStream fos = new FileOutputStream(TempFile);
							try {
								fos.write(TextData, 0, TextData.length);
							} finally {
								fos.close();
							}
							// . open appropriate extent
							intent = new Intent();
							intent.setDataAndType(Uri.fromFile(TempFile), "text/plain");
						}
						else
							if (DataFormat.equals(SpaceDefines.TYPEDDATAFILE_TYPE_Document_FORMAT_XML)) {
								TComponentFunctionality CF = User.Space.TypesSystem.TComponentFunctionality_Create(DataComponentType,DataComponentID);
								if (CF != null)
									try {
										int Version = CF.ParseFromXMLDocument(GetFileData());
										if (Version > 0)
											CF.Open(context, null);
									}
								finally {
									CF.Release();
								}
							}
					} catch (Exception E) {
						Toast.makeText(context, context.getString(R.string.SErrorOfPreparingDataFile)+FileName(), Toast.LENGTH_LONG).show();
						return; // . ->
					}
					break; // . >

				case SpaceDefines.TYPEDDATAFILE_TYPE_Image:
					try {
						if (DataFormat.toLowerCase(Locale.ENGLISH).equals("."+TDrawingDefines.FileExtension)) {
				    		intent = new Intent(context, TDrawingEditor.class);
				  		    intent.putExtra("FileName", GetFile().getAbsolutePath()); 
				  		    intent.putExtra("ReadOnly", true); 
				  		    context.startActivity(intent);
				  		    //.
							return; // . ->
						}
						else {
				    		intent = new Intent(context, TImageViewerPanel.class);
				  		    intent.putExtra("FileName", GetFile().getAbsolutePath()); 
				  		    context.startActivity(intent);
				  		    //.
							return; // . ->
						}
					} catch (Exception E) {
						Toast.makeText(context, context.getString(R.string.SErrorOfPreparingDataFile)+FileName(), Toast.LENGTH_SHORT).show();
						return; // . ->
					}

				case SpaceDefines.TYPEDDATAFILE_TYPE_Audio:
					try {
						// . open appropriate extent
						intent = new Intent();
						intent.setDataAndType(Uri.fromFile(GetFile()), "audio/*");
					} catch (Exception E) {
						Toast.makeText(context, context.getString(R.string.SErrorOfPreparingDataFile)+FileName(), Toast.LENGTH_SHORT).show();
						return; // . ->
					}
					break; // . >

				case SpaceDefines.TYPEDDATAFILE_TYPE_Video:
					try {
						// . open appropriate extent
						intent = new Intent();
						intent.setDataAndType(Uri.fromFile(GetFile()),"video/*");
					} catch (Exception E) {
						Toast.makeText(context, context.getString(R.string.SErrorOfPreparingDataFile)+FileName(), Toast.LENGTH_SHORT).show();
						return; // . ->
					}
					break; // . >
					
				case SpaceDefines.TYPEDDATAFILE_TYPE_Measurement:
					try {
						String MeasurementID = Integer.toString(DataComponentType)+"_"+Long.toString(DataComponentID);
						//. open appropriate extent
			            Intent ProcessorPanel = new Intent(context, TMeasurementProcessorPanel.class);
			            ProcessorPanel.putExtra("MeasurementDatabaseFolder",TGeoLogApplication.GetTempFolder());
			            ProcessorPanel.putExtra("MeasurementID",MeasurementID);
			            ProcessorPanel.putExtra("MeasurementDataFile",GetFile().getAbsolutePath());
			            ProcessorPanel.putExtra("MeasurementStartPosition",0);
			            context.startActivity(ProcessorPanel);	            	
			  		    //.
						return; // . ->
					} catch (Exception E) {
						Toast.makeText(context, context.getString(R.string.SErrorOfPreparingDataFile)+FileName(), Toast.LENGTH_SHORT).show();
						return; // . ->
					}

				default:
					Toast.makeText(context, R.string.SUnknownDataFileFormat,	Toast.LENGTH_LONG).show();
					return; // . ->
				}
				if (intent != null) {
					intent.setAction(android.content.Intent.ACTION_VIEW);
					context.startActivity(intent);
				}
			}
			else {
				TComponentFunctionality CF = User.Space.TypesSystem.TComponentFunctionality_Create(DataComponentType,DataComponentID);
				if (CF != null)
					try {
						CF.Open(context, null);
					}
					finally {
						CF.Release();
					}
			}
			//.
		} catch (Exception E) {
			Toast.makeText(context, E.getMessage(),Toast.LENGTH_LONG).show();
		}
	}
}
