package com.geoscope.GeoLog.DEVICE.FileSystemModule;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import android.annotation.SuppressLint;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

@SuppressLint("SimpleDateFormat")
public class TFileSystemModule extends TModule {

	public class TFTPServerTransmitterState {
		
		public static final int CODE_ERROR 				= -1;
		//.
		public static final int CODE_UNKNOWN 			= 0;
		public static final int CODE_STARTING 			= 1;
		public static final int CODE_DIRECTORYCREATING 	= 2;
		public static final int CODE_FILETRANSMITTING	= 3;
		public static final int CODE_COMPLETED 			= 4;
		
		public int 		Code;
		public String 	Message;
		
		public TFTPServerTransmitterState(int pCode, String pMessage) {
			Code = pCode;
			Message = pMessage;
		}
	}
	
    public class TFTPServerTransmitter implements Runnable {
    	
    	public static final int ConnectTimeout = 1000*10; //. seconds
    	
    	public  String TransferID;
    	private String Address;
    	private String UserName;
    	private String UserPassword;
    	private String BaseDirectory;
    	private String FileName;
    	//.
    	protected Thread _Thread;
    	private FTPClient Transmitter = null;
    	public boolean flProcessing = true;
    	public TFTPServerTransmitterState State = new TFTPServerTransmitterState(TFTPServerTransmitterState.CODE_STARTING, "");
    	
    	public TFTPServerTransmitter(String pTransferID, String pAddress, String pUserName, String pUserPassword, String pBaseDirectory, String pFileName) {
    		TransferID = pTransferID;
    		Address = pAddress;
    		UserName = pUserName;
    		UserPassword = pUserPassword;
    		BaseDirectory = pBaseDirectory;
    		FileName = pFileName;
    		//.
			Transmitter = new FTPClient();
			Transmitter.setConnectTimeout(ConnectTimeout);
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
		@Override
		public void run() {
			try {
				try {
					String[] AA = Address.split(":");
				    Transmitter.connect(AA[0],Integer.parseInt(AA[1]));
				    try {
					    if (Transmitter.login(UserName,UserPassword)) {
					    	try {
						        Transmitter.setFileType(FTP.BINARY_FILE_TYPE);
						        Transmitter.enterLocalPassiveMode();
						        //.
						        if (BaseDirectory != null)
						        	if (!Transmitter.changeWorkingDirectory(BaseDirectory))
						        		throw new IOException("could not change ftp directory, "+BaseDirectory); //. =>
						        //.
						        File file = new File(FileName);
						        if (!file.isDirectory()) {
						        	SetState(TFTPServerTransmitterState.CODE_FILETRANSMITTING,FileName);
						        	TransmiteFile(Transmitter,FileName,file.getName());
						        }
						        else {
						        	String Dir = file.getName();
						        	SetState(TFTPServerTransmitterState.CODE_DIRECTORYCREATING,Dir);
						        	if (!Transmitter.makeDirectory(Dir))
						        		throw new IOException("could not create new ftp directory, "+Dir); //. =>
						        	if (!Transmitter.changeWorkingDirectory(Dir))
						        		throw new IOException("could not change ftp directory, "+Dir); //. =>
						        	//.
						        	File[] DirFiles = file.listFiles();
						        	for (int I = 0; I < DirFiles.length; I++)
						        		if (!DirFiles[I].isDirectory()) {
								        	SetState(TFTPServerTransmitterState.CODE_FILETRANSMITTING,FileName);
								        	TransmiteFile(Transmitter,DirFiles[I].getAbsolutePath(),DirFiles[I].getName());
						        		}
						        }
						        //.
								SetState(TFTPServerTransmitterState.CODE_COMPLETED,"");
					    	}
					    	finally {
					    		if (Transmitter.isConnected())
					    			Transmitter.logout();
					    	}
					    }
				    }
				    finally {
			    		if (Transmitter.isConnected())
			    			Transmitter.disconnect();
				    }
				}
				catch (Throwable E1) {
					String EM = E1.getMessage();
					if (EM == null)
						EM = "?";
					SetState(TFTPServerTransmitterState.CODE_ERROR,EM);
				}
			}
			finally {
				flProcessing = false;
			}
		}
		
    	public void Join() {
    		try {
    			_Thread.join();
    		}
    		catch (Exception E) {}
    	}

    	public void Cancel() throws IOException {
    		if ((Transmitter != null) && (Transmitter.isConnected()))
    			Transmitter.disconnect();
    		//.
			_Thread.interrupt();
    	}
    
		public void CancelAndWait() throws IOException {
    		Cancel();
    		try {
				_Thread.join();
			} catch (InterruptedException e) {
			}
    	}
		
		private void TransmiteFile(FTPClient Transmitter, String FileName, String DestFileName) throws IOException {
			FileInputStream FIS = new FileInputStream(FileName);
			try {
				Transmitter.storeFile(DestFileName, FIS);
			}
			finally {
				FIS.close();
			}
		}
		
		public synchronized void SetState(int pCode, String pMessage) {
			State = new TFTPServerTransmitterState(pCode, pMessage);
		}
		
		public synchronized TFTPServerTransmitterState GetState() {
			return (new TFTPServerTransmitterState(State.Code, State.Message));
		}
    }
    
	
	public boolean flEnabled = true;
	//.
	public TFTPServerTransmitter FTPTransmitter = null;
    
    public TFileSystemModule(TDEVICEModule pDevice)
    {
    	super(pDevice);
    	//.
        Device = pDevice;
    }
    
    public void Destroy() throws IOException
    {
    	synchronized (this) {
        	if (FTPTransmitter != null) {
        		FTPTransmitter.CancelAndWait();
        		FTPTransmitter = null;
        	}
		}
    }
    
    public synchronized String CreateNewFTPTransfer(String pAddress, String pUserName, String pUserPassword, String pBaseDirectory, String pFileName) throws IOException, OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	if (FTPTransmitter != null) 
    		FTPTransmitter.CancelAndWait();
    	//.
    	String NewTransferID = (new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date());
    	FTPTransmitter = new TFTPServerTransmitter(NewTransferID, pAddress,pUserName,pUserPassword, pBaseDirectory, pFileName);
    	//.
    	return NewTransferID;
    }
    
    public synchronized void CancelFTPTransfer(String TransferID) throws IOException, OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	if ((FTPTransmitter != null) && (TransferID.equals("") || FTPTransmitter.TransferID.equals(TransferID)))
    		FTPTransmitter.CancelAndWait();
    }
    
    public synchronized TFTPServerTransmitterState GetFTPTransferState(String TransferID) throws OperationException {
		if (!IsEnabled())
			throw new OperationException(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsDisabled); //. =>
    	if (FTPTransmitter == null)
    		return (new TFTPServerTransmitterState(TFTPServerTransmitterState.CODE_UNKNOWN,"")); //. ->
    	if (FTPTransmitter.TransferID.equals(TransferID))
    		return FTPTransmitter.GetState(); //. ->
    	else
    		return (new TFTPServerTransmitterState(TFTPServerTransmitterState.CODE_UNKNOWN,""));
    }
}
