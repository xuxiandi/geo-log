package com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.DEVICE.ADCModule.TADCModule;
import com.geoscope.GeoLog.DEVICE.GPIModule.TGPIModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule.TUSBPluginModule.TCommand.TCommandResponseData;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TUSBPluginModule extends TModule {

	@SuppressWarnings("unused")
	private static final String TAG = "USBPluginModule";
	
	public static final String ACTION_USB_PERMISSION = "com.geoscope.GeoLog.DEVICEModule.PluginsModule.USBPluginModule.action.USB_PERMISSION";
	//.
	protected static final int STATUS_NO_DEVICE 		= 0;
	protected static final int STATUS_ATTACHED_DEVICE 	= 1;
	protected static final int STATUS_CONNECTED_DEVICE 	= 2;
	protected static final int STATUS_ACQUISITION 		= 3;
	protected static final int STATUS_SETTINGS 			= 4;
	
	public static int MessageMaxSize = 16384;
	
	public static class TCommand {
		
		public static final String CommandPrefix = "#";
		public static final String CommandResponsePrefix = "@";
		
		public static final int ProcessTimeout = 10000; //. ms
		
		public static boolean IsCommand(String Message) {
			return Message.startsWith(CommandPrefix);
		}
		
		public static boolean IsCommandResponse(String Message) {
			return Message.startsWith(CommandResponsePrefix);
		}
		
		public static TCommand GetCommandByName(TUSBPluginModule pUSBPluginModule, String Command) {
			if (TGPICommand.CheckCommandName(Command))
				return new TGPICommand(pUSBPluginModule); //. ->
			else
				if (TGPOCommand.CheckCommandName(Command))
					return new TGPOCommand(pUSBPluginModule); //. ->
				else
					if (TADCCommand.CheckCommandName(Command))
						return new TADCCommand(pUSBPluginModule); //. ->
					else
						if (TDACCommand.CheckCommandName(Command))
							return new TDACCommand(pUSBPluginModule); //. ->
						else
							return null; //. ->
		}
		
		@SuppressWarnings("serial")
		public static class ResponseException extends IOException {
			
			public int 		Code;
			public String 	Message;
			
			public ResponseException(int pCode, String pMessage) {
				Code = pCode;
				Message = pMessage;
			}
		}
		
		@SuppressWarnings("serial")
		public static class ResponseTimeoutException extends ResponseException {
			
			public ResponseTimeoutException() {
				super(Integer.MIN_VALUE,null);
			}
		}
		
		@SuppressWarnings("serial")
		public static class ResponseProcessException extends ResponseException {
			
			public ResponseProcessException(String pMessage) {
				super(Integer.MIN_VALUE,pMessage);
			}
		}
		
		public static class TCommandResponseData {
			
			public String 	Command = null;
			//.
			public String[]	Data = null;
			public int 		Version = 0;
			public int 		Session = 0;
			public int 		ExceptionCode = 0;
			public String	ExceptionMessage = null;

			public TCommandResponseData() {
			}
			
			public TCommandResponseData(String pResponse) {
				Set(pResponse);
			}
			
			public void Set(String pResponse) {
				String[] Response = pResponse.split(" ");
				//.
				Command = Response[0].substring(1);
				Data = Response[1].split(",");
				//.
				Version = GetVersion();
				Session = GetSessionID();
				if (Version < 0) {
					ExceptionCode 	= GetExceptionCode();
					ExceptionMessage = GetExceptionMessage();
				}
				else {
					ExceptionCode 	= 0;
					ExceptionMessage = null;
				}
			}
			
			public int GetSessionID() {
				return Integer.decode(Data[Data.length-2]);
			}

			public int GetCRC() {
				return Integer.decode(Data[Data.length-1]);
			}

			public int GetVersion() {
				return Integer.decode(Data[0]);
			}
			
			public int GetExceptionCode() {
				return Integer.decode(Data[1]);
			}
			
			public String GetExceptionMessage() {
				return Data[2];
			}
		}
		
		public static class TCommandData {
			
			public static short 	NewCommandSessionRange = Short.MAX_VALUE-1;
			private static Random	NewCommandSessionRandom = new Random();
			
			public static short GetNewCommandSession() {
				synchronized (NewCommandSessionRandom) {
					return (short)NewCommandSessionRandom.nextInt(1+NewCommandSessionRange);
				}
			}
			
			public String 	Command = null;
			//.
			public String[]	Data = null;
			public int		Version = 0;
			public int		Session = 0;
			//.
			public TCommandResponseData Response = null;
			
			public TCommandData() {
			}

			public TCommandData(String pCommand, int pSession) {
				Command = pCommand;
				Session = pSession;
			}

			public String ConstructCommandMessage() {
				StringBuilder SB = new StringBuilder();
				SB.append(CommandPrefix);
				SB.append(Command);
				SB.append(" ");
				SB.append(Integer.toString(Version));
				SB.append(",");
				if (Data != null) {
					for (int I = 0; I < Data.length; I++) {
						SB.append(Data[I]);
						SB.append(",");
					}
				}
				SB.append(Integer.toString(Session));
				SB.append(",");
				SB.append("0"); //. CRC
				SB.append("!"); //. Terminator
				return SB.toString();			
			}
			
			public void Set(String pData) {
				Data = pData.split(",");
				//.
				Version = GetVersion();
				Session = GetSessionID();
				//.
				ResetResponse();
			}
			
			public int GetSessionID() {
				return Integer.decode(Data[Data.length-2]);
			}

			public int GetCRC() {
				return Integer.decode(Data[Data.length-1]);
			}

			public int GetVersion() {
				return Integer.decode(Data[0]);
			}

			public void SetResponse(TCommandResponseData pResponse) {
				Response = pResponse;
			}
			
			public void ResetResponse() {
				Response = null;
			}			
		}
		
		protected TUSBPluginModule USBPluginModule;
		//.
		public TCommandData CommandData;
		//.
		public Object 	ReceivedSignal = new Object();
		public boolean 	Received = false;
		
		public TCommand(TUSBPluginModule pUSBPluginModule, boolean flNewSession) {
			USBPluginModule = pUSBPluginModule;
			//.
			if (flNewSession)
				CommandData = new TCommandData(MyCommandName(),TCommandData.GetNewCommandSession());
			else
				CommandData = new TCommandData(MyCommandName(),0);
		}

		public TCommand(TUSBPluginModule pUSBPluginModule) {
			this(pUSBPluginModule,false);
		}
		
		
		public String MyCommandName() {
			return null;
		}
		
		public boolean CheckCommand(String pCommand) {
			return false;
		}
				
		public void Prepare() {
		}
				
		public void Process() throws Exception {
			try {
				Reset();
				//.
				Prepare();
				//.
				USBPluginModule.SendMessage(CommandData.ConstructCommandMessage());
				//. wait for completion and process response for result
				WaitAndProcessResponse();
			}
			catch (Exception E) {
				String EM = E.getMessage();
				if (EM == null)
					EM = E.getClass().getName();
				USBPluginModule.Device.Log.WriteError("USBPluginModule","command process error: "+EM);
				//.
				throw E; //. =>
			}
		}
		
		public void WaitAndProcessResponse() throws Exception {
			synchronized (ReceivedSignal) {
				ReceivedSignal.wait(ProcessTimeout);
				if (!Received)
					throw new ResponseTimeoutException(); //. =>
				//.
				ProcessResponse();
			}
		}
		
		public void Reset() {
			synchronized (ReceivedSignal) {
				CommandData.ResetResponse();
				Received = false;
			}
		}
		
		public void EnqueueAndProcess() throws Exception {
			USBPluginModule.OutgoingCommands_ProcessCommand(this);
		}
		
		public boolean DoOnCommandResponse(TCommandResponseData Response) {
			if (CommandData.Session != 0)  
				if (Response.Session == CommandData.Session) {
					CommandData.SetResponse(Response);
					//.
					synchronized (ReceivedSignal) {
						Received = true;
						ReceivedSignal.notify();
					}
					//.
					return true; //. ->
				}
				else
					return false; //. ->
			else {
				CommandData.SetResponse(Response);
				//.
				return true; //. ->
			}				
		}
		
		public void CheckResponse() throws ResponseException {
			if (CommandData.Response.Version < 0)
				throw new ResponseException(CommandData.Response.ExceptionCode,CommandData.Response.ExceptionMessage); //. =>
		}

		public void ParseResponse() throws Exception {
		}

		public void ProcessResponse() throws Exception {
			CheckResponse(); 
			//.
			ParseResponse();
		}
		
		public void DoOnResponseIsProcessed() throws Exception {
		}
	}
	
	public static class TGPICommand extends TCommand {
		
		public static String CommandName() {
			return "GPI";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public int Value;
		
		public TGPICommand(TUSBPluginModule pUSBPluginModule, int pAddress) {
			super(pUSBPluginModule,true);
			//.
			Address = pAddress;
		}
		
		public TGPICommand(TUSBPluginModule pUSBPluginModule) {
			super(pUSBPluginModule);
		}

		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = new String[1];
			CommandData.Data[0] = Integer.toString(Address);
		}
		
		@Override
		public void ParseResponse() throws Exception {
			switch (CommandData.Response.Version) {
			
			case 1: 
				int _Address = Integer.parseInt(CommandData.Response.Data[1]);
				if (Address == 0) 
					Address = _Address;
				else
					if (_Address != Address)
						throw new ResponseProcessException("wrong returned address, address: "+Integer.toString(_Address)); //. =>
				Value = Integer.parseInt(CommandData.Response.Data[2]);
				break; //. >
				
			default:
				throw new ResponseProcessException("unknown response version, version: "+Integer.toString(CommandData.Response.Version)); //. =>
			}
		}
		
		@Override
		public void DoOnResponseIsProcessed() throws Exception {
			TGPIModule GPIModule = USBPluginModule.Device.GPIModule;
			int V = GPIModule.GetIntValue();
			int BV = (1 << Address);
			if (Value > 0) 
				V = (V | BV);
			else {
				BV = ~BV;
				V = (V & BV);
			}
			GPIModule.SetValue((short)V);
		}
	}
	
	public static class TGPOCommand extends TCommand {
		
		public static String CommandName() {
			return "GPO";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public int Value;
		
		public TGPOCommand(TUSBPluginModule pUSBPluginModule, int pAddress, int pValue) {
			super(pUSBPluginModule,true);
			//.
			Address = pAddress;
			Value = pValue;
		}
		
		public TGPOCommand(TUSBPluginModule pUSBPluginModule) {
			super(pUSBPluginModule);
		}
		
		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = new String[2];
			CommandData.Data[0] = Integer.toString(Address);
			CommandData.Data[1] = Integer.toString(Value);
		}				
	}
	
	public static class TADCCommand extends TCommand {
		
		public static String CommandName() {
			return "ADC";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public int Value;
		
		public TADCCommand(TUSBPluginModule pUSBPluginModule, int pAddress) {
			super(pUSBPluginModule,true);
			//.
			Address = pAddress;
		}
		
		public TADCCommand(TUSBPluginModule pUSBPluginModule) {
			super(pUSBPluginModule);
		}
		
		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = new String[1];
			CommandData.Data[0] = Integer.toString(Address);
		}
		
		@Override
		public void ParseResponse() throws Exception {
			switch (CommandData.Response.Version) {
			
			case 1: 
				int _Address = Integer.parseInt(CommandData.Response.Data[1]);
				if (Address == 0) 
					Address = _Address;
				else
					if (_Address != Address)
						throw new ResponseProcessException("wrong returned address, address: "+Integer.toString(_Address)); //. =>
				Value = Integer.parseInt(CommandData.Response.Data[2]);
				break; //. >
				
			default:
				throw new ResponseProcessException("unknown response version, version: "+Integer.toString(CommandData.Response.Version)); //. =>
			}
		}
		
		@Override
		public void DoOnResponseIsProcessed() throws Exception {
			TADCModule ADCModule = USBPluginModule.Device.ADCModule;
			ADCModule.SetValueItem(Address,Value);
			//.
			//. USBPluginModule.Device.Log.WriteInfo("USBControllerModule","command: "+"ADCModule.SetValueItem is DONE, Address: "+Integer.toString(Address)+", Value: "+Integer.toString(Value));
		}
	}
	
	public static class TDACCommand extends TCommand {
		
		public static String CommandName() {
			return "DAC";
		}
		
		public static boolean CheckCommandName(String pCommand) {
			return CommandName().equals(pCommand);
		}

		public int Address = 0;
		public int Value;
		
		public TDACCommand(TUSBPluginModule pUSBPluginModule, int pAddress, int pValue) {
			super(pUSBPluginModule,true);
			//.
			Address = pAddress;
			Value = pValue;
		}
		
		public TDACCommand(TUSBPluginModule pUSBPluginModule) {
			super(pUSBPluginModule);
		}
		
		@Override
		public String MyCommandName() {
			return CommandName();
		}
		
		@Override
		public boolean CheckCommand(String pCommand) {
			return CheckCommandName(pCommand);
		}
		
		@Override
		public void Prepare() {
			CommandData.Version = 1;
			CommandData.Data = new String[2];
			CommandData.Data[0] = Integer.toString(Address);
			CommandData.Data[1] = Integer.toString(Value);
		}				
	}
	
	protected  Context context;
	//.
	private UsbManager mUsbManager;
	private PendingIntent mPermissionIntent;
	private boolean mPermissionRequestPending;
	//.
	private UsbAccessory mAccessory;
	private ParcelFileDescriptor mAccessoryFileDescriptor;
	private FileInputStream mAccessoryInput = null;
	private FileOutputStream mAccessoryOutput = null;
	//.
	private ArrayList<TCommand> OutgoingCommands = new ArrayList<TCommand>();
	//.
	private TProcessing Processing;

	public int mStatus = STATUS_NO_DEVICE;

	private final BroadcastReceiver mUsbBroadcastReceiver = new BroadcastReceiver() {
		
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)){
				synchronized (this){
					UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) 
						OpenAccessory(accessory);
					else 
	            		Device.Log.WriteError("USBPluginModule","permission is denied for accessory: "+accessory);
					mPermissionRequestPending = false;
				}
			}
			else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)){
				UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(mAccessory)) {
					try {
						CloseAccessory();
					} catch (InterruptedException IE) {
					}
				}
			}
		}
	};
	
    public TUSBPluginModule(TDEVICEModule pDevice) throws Exception
    {
    	super(pDevice);
    	//.
        Device = pDevice;
		context = Device.context;
		//.
		/* mUsbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
		//.
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		context.registerReceiver(mUsbBroadcastReceiver, filter);
		//.
		CheckConnectedAccesory();*/
	}

	public void Destroy() throws InterruptedException {
		/* CloseAccessory();
		//.
		context.unregisterReceiver(mUsbBroadcastReceiver);*/
	}

	public void SetStatus(int status) {
		mStatus = status;
	} 
	
	public int GetStatus() {
		return mStatus;
	}
	
	public synchronized void OutgoingCommands_Add(TCommand Command) {
		OutgoingCommands.add(Command);
	}
	
	public synchronized void OutgoingCommands_Remove(TCommand Command) {
		OutgoingCommands.remove(Command);
	}
	
	public void OutgoingCommands_ProcessCommand(TCommand Command) throws Exception {
		OutgoingCommands_Add(Command);
		try {
			Command.Process();
		}
		finally {
			OutgoingCommands_Remove(Command);
		}
	}
	
	public void CheckConnectedAccesory(){
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (mUsbManager.hasPermission(accessory)) 
				OpenAccessory(accessory);
			else {
				synchronized (mUsbBroadcastReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory, mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
			}
		} 
	}

	private void OpenAccessory(UsbAccessory accessory)
	{
		mAccessoryFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mAccessoryFileDescriptor != null) {
			this.mAccessory = accessory;
			FileDescriptor fd = mAccessoryFileDescriptor.getFileDescriptor();
			mAccessoryInput = new FileInputStream(fd);
			mAccessoryOutput = new FileOutputStream(fd);
			Processing = new TProcessing();
			//.
			SetStatus(STATUS_ATTACHED_DEVICE);
			//.
    		Device.Log.WriteInfo("USBPluginModule","accessory is opened: "+accessory);
		}
		else
    		Device.Log.WriteError("USBPluginModule","accessory opening error: "+accessory);
	}

	private void CloseAccessory() throws InterruptedException
	{
		try{
			if (Processing != null) {
				Processing.CancelAndWait();
				Processing = null;
			}
			if (mAccessoryOutput != null) {
				mAccessoryOutput.close();
				mAccessoryOutput = null;
			}
			if (mAccessoryInput != null) {
				mAccessoryInput.close();
				mAccessoryInput = null;
			}
			if(mAccessoryFileDescriptor != null)
				mAccessoryFileDescriptor.close();
		}
		catch (IOException E) {
    		Device.Log.WriteError("USBPluginModule","error while closing accessory: "+mAccessory);
		}
		finally{
			mAccessoryFileDescriptor = null;
			mAccessory = null;
			SetStatus(STATUS_NO_DEVICE);
		}
	}
	
	public class TProcessing extends TCancelableThread {

		public TProcessing() {
    		_Thread = new Thread(this);
    		_Thread.start();
		}
		
		@Override
		public void run() {
			try {
				byte[] MessageBA = new byte[MessageMaxSize];
				int MessageBAIdx = 0;
				//.
				while (!Canceller.flCancel) {
					int RS = mAccessoryInput.read(MessageBA,MessageBAIdx,MessageBA.length-MessageBAIdx);
					if (RS > 0) {
						MessageBAIdx += RS;
			    		//.
						if (MessageBA[MessageBAIdx-1] == 0x21) {
							MessageBAIdx--;
							//.
				            String Message = new String(MessageBA,0,MessageBAIdx,"windows-1251");
				            //.
				    		//. Device.Log.WriteInfo("USBPluginModule","message is received: "+Message);
				            //. process message
				            try {
				            	DoOnMessageIsReceived(Message);
				            }
							catch (TCommand.ResponseException RE) {
					    		Device.Log.WriteError("USBPluginModule","error response message: "+RE.getMessage());
							}
							catch (Exception E) {
					    		Device.Log.WriteError("USBPluginModule","error while processing message: "+E.getMessage());
							}
				            //.
		            		MessageBAIdx = 0;
						}
					}
				}
			}
			catch (Throwable T) {
	    		Device.Log.WriteError("USBPluginModule","error while reading accessory: "+mAccessory+", "+T.getMessage());
			}
		}

	    protected int InputStream_Read(FileInputStream IS, byte[] Data, int DataSize) throws IOException {
	        int SummarySize = 0;
	        int ReadSize;
	        int Size;
	        while (SummarySize < DataSize) {
	            ReadSize = DataSize-SummarySize;
	            Size = IS.read(Data,SummarySize,ReadSize);
	            if (Size <= 0) 
					throw new IOException("Return size = "+Integer.toString(Size)); //. =>
	            SummarySize += Size;
	        }
	        return SummarySize;
	    }
		
		public void SendMessage(String CommandMessage) {		
			if (mAccessoryOutput != null) {
				try {
					mAccessoryOutput.write(CommandMessage.getBytes());
					//.
		    		//. Device.Log.WriteInfo("USBPluginModule","message is sent: "+CommandMessage);
				} catch (IOException E) {
		    		Device.Log.WriteError("USBPluginModule","error while writing accessory: "+mAccessory+", "+E.getMessage());
				}
			}
		}
	}

	public void SendMessage(String CommandMessage) throws IOException {
		if (Processing != null)
			Processing.SendMessage(CommandMessage);
		else
			throw new IOException("accessory is not attached"); //. => 
	}	

	private void DoOnMessageIsReceived(String Message) throws Exception {
		if (TCommand.IsCommandResponse(Message)) {
			TCommandResponseData CommandResponseData = new TCommandResponseData(Message);
			//. process for outgoing operations
			synchronized (OutgoingCommands) {
				for (int I = 0; I < OutgoingCommands.size(); I++) {
					TCommand OC = OutgoingCommands.get(I);
					if (OC.CheckCommand(CommandResponseData.Command) && OC.DoOnCommandResponse(CommandResponseData))
						return; //. ->
				}
			}
			//. process for serial commands
			TCommand OC = TCommand.GetCommandByName(this, CommandResponseData.Command);
			if (OC != null) {
				if (OC.DoOnCommandResponse(CommandResponseData)) {
					OC.ProcessResponse();
					OC.DoOnResponseIsProcessed();
				}
			}
			else 
				throw new Exception("unknown command: "+CommandResponseData.Command); //. =>
		}
	}
}
