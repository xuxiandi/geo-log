package com.geoscope.GeoLog.DEVICE.FileSystemModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.IO.Log.TDataConverter;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedDataValue;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetFileSystemDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;

public class TFileSystemDataValue extends TComponentTimestampedDataValue {

	public static final int MaxFileSize = 10*1024*1024;
	
	private TFileSystemModule FileSystemModule;
	
	public TFileSystemDataValue(TFileSystemModule pFileSystemModule) {
		FileSystemModule = pFileSystemModule;
	}

	@Override
    public synchronized void FromByteArrayByAddressData(byte[] BA, TIndex Idx, byte[] AddressData) throws Exception 
    {
		if (!FileSystemModule.flEnabled)
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		//.
        super.FromByteArrayByAddressData(BA,/*ref*/ Idx, AddressData);
		//.
		if (AddressData == null)
			return; //. ->
    	String Params;
    	try {
    		Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	}
    	catch (Exception E) {
    		return; //. ->
    	}
    	String[] SA = Params.split(",");
    	int Operation = Integer.parseInt(SA[0]);
    	//.
    	switch (Operation) {
    	case 1: //. create directory
    		String NewDir = SA[1];
    		TFileSystem.CreateDir(NewDir);
            break; //. >

    	case 2: //. set file data
    		String FileFullName = SA[1];
    		TFileSystem.SetFileData(FileFullName,Value);
            break; //. >

    	case 3: //. delete file/directory 
    		FileFullName = SA[1];
    		TFileSystem.DeleteFile(FileFullName);
    		for (int I = 2; I < SA.length; I++)
    			TFileSystem.DeleteFile(SA[I]);
            break; //. >
            
        default:
            break; //. >
    	}
    }
    
	@Override
    public synchronized byte[] ToByteArrayByAddressData(byte[] AddressData) throws Exception
    {
		if (!FileSystemModule.flEnabled)
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
		//.
		if (AddressData == null)
			return null; //. ->
    	String Params;
    	try {
    		Params = new String(AddressData, 0,AddressData.length, "windows-1251");
    	}
    	catch (Exception E) {
    		return null; //. ->
    	}
    	String[] SA = Params.split(",");
    	int Operation = Integer.parseInt(SA[0]);
    	//.
    	switch (Operation) {
    	case 1: //. get directory list
    		String Dir = SA[1]; 
    		String ListData = TFileSystem.GetDirList(Dir);
    		//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = ListData.getBytes("windows-1251");
    		return ToByteArray(); //. ->

    	case 2: //. get file 
    		String FileFullName = SA[1];
    		if (!TFileSystem.IsFileExist(FileFullName))
    			throw new OperationException(TGetFileSystemDataValueSO.OperationErrorCode_DataIsNotFound); //. =>
    		if (TFileSystem.GetFileSize(FileFullName) > MaxFileSize)
    			throw new OperationException(TGetFileSystemDataValueSO.OperationErrorCode_DataIsTooBig); //. =>
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = TFileSystem.GetFileData(FileFullName);
    		return ToByteArray(); //. ->
            
    	case 11: //. start transfer file/directory via FTP
        	String ServerAddress = SA[1];
        	String ServerUserName = SA[2];
        	String ServerUserPassword = SA[3];
        	String BaseDirectory = SA[4];
    		String TransferFileName = SA[5];
    		//.
        	if (BaseDirectory.equals(""))
        		BaseDirectory = null;
    		//.
        	String NewTransferID;
    		synchronized (FileSystemModule) {
				if ((FileSystemModule.FTPTransmitter != null) && FileSystemModule.FTPTransmitter.flProcessing)
	    			throw new OperationException(TGetFileSystemDataValueSO.OperationErrorCode_FTPTransmitterIsBusy); //. =>
	    		NewTransferID = FileSystemModule.CreateNewFTPTransfer(ServerAddress, ServerUserName,ServerUserPassword, BaseDirectory, TransferFileName);  
			}
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = NewTransferID.getBytes("windows-1251");
    		return ToByteArray(); //. ->

    	case 12: //. get state of specified FTP transfer session
        	String TransferID = SA[1];
    		//.
        	TFileSystemModule.TFTPServerTransmitterState TS = FileSystemModule.GetFTPTransferState(TransferID);
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		byte[] CBA = TDataConverter.ConvertInt32ToBEByteArray(TS.Code);
    		byte[] MBA = TS.Message.getBytes("windows-1251");
    		Value = new byte[CBA.length+MBA.length];
    		System.arraycopy(CBA,0, Value,0, CBA.length);
    		System.arraycopy(MBA,0, Value,CBA.length, MBA.length);
    		return ToByteArray(); //. ->

    	case 13: //. cancel specified FTP transfer session
        	String CancelTransferID = "";
        	if (SA.length > 1)
        		CancelTransferID = SA[1];
        	//.
        	FileSystemModule.CancelFTPTransfer(CancelTransferID);
        	//.
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->

        default:
    		Timestamp = OleDate.UTCCurrentTimestamp();
    		Value = null;
            return ToByteArray(); //. ->
    	}
    }
}
