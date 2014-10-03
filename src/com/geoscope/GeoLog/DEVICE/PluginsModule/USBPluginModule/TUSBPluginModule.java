package com.geoscope.GeoLog.DEVICE.PluginsModule.USBPluginModule;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.TCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.Models.TPluginModel;
import com.geoscope.GeoLog.DEVICE.PluginsModule.Models.M0.TM0;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

public class TUSBPluginModule extends TPluginModule {

	@SuppressWarnings("unused")
	private static final String TAG = "USBPluginModule";
	private static final boolean flDebug = true;
	
	public static final String ACTION_USB_PERMISSION = "com.geoscope.GeoLog.DEVICEModule.PluginsModule.USBPluginModule.action.USB_PERMISSION";
	//.
	protected static final int STATUS_NO_DEVICE 		= 0;
	protected static final int STATUS_ATTACHED_DEVICE 	= 1;
	protected static final int STATUS_CONNECTED_DEVICE 	= 2;
	protected static final int STATUS_ACQUISITION 		= 3;
	protected static final int STATUS_SETTINGS 			= 4;
	
	public static int MessageMaxSize = 16384;
	
	public static class TDoOnMessageIsReceivedHandler {
		
		public void DoOnMessageIsReceived(String Message) throws Exception {
		}
	}
	
	protected  Context context;
	//.
	private boolean flInitialized = false;
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
	public TPluginModel PluginModel = null;
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
	
	private TDoOnMessageIsReceivedHandler DoOnMessageIsReceivedHandler = null;
	
    public TUSBPluginModule(TDEVICEModule pDevice) 
    {
    	super(pDevice);
    	//.
        Device = pDevice;
		context = Device.context;
		//.
		try {
			Initialize();
		}
		catch (Exception E) {
    		Device.Log.WriteError("USBPluginModule","initialization error: "+E.getMessage());
		}
	}

	public void Destroy() {
		if (flInitialized)
			try {
				Finalize();
			} catch (InterruptedException IE) {
	    		Device.Log.WriteError("USBPluginModule","finalization error: "+IE.getMessage());
			}
	}

	private void Initialize() {
		mUsbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
		//.
		mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		context.registerReceiver(mUsbBroadcastReceiver, filter);
		//.
		CheckConnectedAccesory();
		//.
		PluginModel = new TM0(Device.PluginsModule);
		//.
		flInitialized = true;
	}
	
	private void Finalize() throws InterruptedException {
		flInitialized = false;
		//.
		CloseAccessory();
		//.
		context.unregisterReceiver(mUsbBroadcastReceiver);
	}
	
	public void SetStatus(int status) {
		mStatus = status;
	} 
	
	public int GetStatus() {
		return mStatus;
	}
	
	private synchronized void OutgoingCommands_Add(TCommand Command) {
		OutgoingCommands.add(Command);
	}
	
	private synchronized void OutgoingCommands_Remove(TCommand Command) {
		OutgoingCommands.remove(Command);
	}
	
	@Override
	public void OutgoingCommands_ProcessCommand(TCommand Command) throws Exception {
		Command.CommandSender = new PIO.TCommand.TCommandSender() {
			@Override
			public void SendCommand(String Command) throws IOException {
				SendMessage(Command);
			}
		};
		Command.ResponseHandler = new PIO.TCommand.TResponseHandler() {
			@Override
			public void DoOnResponse(TCommand Command) {
				try {
					HandleCommandResponse(Command);
				} catch (Exception E) {
		    		Device.Log.WriteError("USBPluginModule","error of response handling: "+E.getMessage());
				}
			}
		};
		//.
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
				            if (flDebug)
				            	Device.Log.WriteInfo("USBPluginModule","message is received: "+Message);
				            //. process message
				            try {
				            	if (DoOnMessageIsReceivedHandler != null)
				            		try {
				            			DoOnMessageIsReceivedHandler.DoOnMessageIsReceived(Message);
				            		}
				            		catch (Exception E) {
							    		Device.Log.WriteError("USBPluginModule","error of DoOnMessageIsReceivedHandler.DoOnMessageIsReceived(Message): "+E.getMessage());
				            		}
				            	//.
				            	DoOnMessageIsReceived(Message);
				            }
							catch (TCommand.ResponseException RE) {
					    		Device.Log.WriteError("USBPluginModule","error of response message: "+RE.getMessage());
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

		public void SendMessage(String CommandMessage) throws IOException {		
			if (mAccessoryOutput != null) {
				try {
					mAccessoryOutput.write(CommandMessage.getBytes());
					//.
		            if (flDebug)
		            	Device.Log.WriteInfo("USBPluginModule","message is sent: "+CommandMessage);
				} catch (IOException IOE) {
		    		Device.Log.WriteError("USBPluginModule","error while writing accessory: "+mAccessory+", "+IOE.getMessage());
		    		//.
		    		throw IOE; //. =>
				}
			}
		}
	}

	public void SendMessage(String CommandMessage) throws IOException {
		if (Processing != null)
			Processing.SendMessage(CommandMessage);
		else
			throw new IOException("USB accessory is not attached"); //. => 
	}	

	public synchronized void SetDoOnMessageIsReceivedHandler(TDoOnMessageIsReceivedHandler pHandler) {
		DoOnMessageIsReceivedHandler = pHandler;
	}
	
	private void DoOnMessageIsReceived(String Message) throws Exception {
		if (TCommand.IsCommandResponse(Message)) {
			TCommand.TCommandResponseData CommandResponseData = new TCommand.TCommandResponseData(Message);
			//. process for outgoing operations
			synchronized (OutgoingCommands) {
				for (int I = 0; I < OutgoingCommands.size(); I++) {
					TCommand OC = OutgoingCommands.get(I);
					if (OC.CheckCommand(CommandResponseData.Command) && OC.DoOnCommandResponse(CommandResponseData))
						return; //. ->
				}
			}
			//. process for serial commands
			TCommand OC = TCommand.GetCommandByName(Device.PluginsModule, new PIO.TCommand.TCommandSender() {
				@Override
				public void SendCommand(String Command) throws IOException {
					SendMessage(Command);
				}
			}, new PIO.TCommand.TResponseHandler() {
				@Override
				public void DoOnResponse(TCommand Command) {
					try {
						HandleCommandResponse(Command);
					} catch (Exception E) {
			    		Device.Log.WriteError("USBPluginModule","error of response handling: "+E.getMessage());
					}
				}
			}, CommandResponseData.Command);
			if (OC != null) {
				if (OC.DoOnCommandResponse(CommandResponseData)) 
					OC.ProcessResponse();
			}
			else 
				throw new Exception("unknown command: "+CommandResponseData.Command); //. =>
		}
	}
	
	private void HandleCommandResponse(TCommand Command) throws Exception {
		PluginModel.DoOnCommandResponse(Command);
	}
}
