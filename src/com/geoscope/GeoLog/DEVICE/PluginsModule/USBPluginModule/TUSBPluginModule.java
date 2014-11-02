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
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.ParcelFileDescriptor;

import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.TPluginsModule;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.PIO.TCommand;
import com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.TModel;

public class TUSBPluginModule extends TPluginModule {

	@SuppressWarnings("unused")
	private static final String TAG = "USBPluginModule";
	//.
	private static final boolean flRealPlugin = true;
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
	
	public static class TProcessingAbstract extends TCancelableThread {

		public void Destroy() throws InterruptedException {
		}
		
		public void SendMessage(String CommandMessage) throws IOException {
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
	public UsbAccessory Accessory = null;
	private ParcelFileDescriptor mAccessoryFileDescriptor;
	private FileInputStream mAccessoryInput = null;
	private FileOutputStream mAccessoryOutput = null;
	//.
	public TModel PIOModel = null;
	//.
	private ArrayList<TCommand> OutgoingCommands = new ArrayList<TCommand>();
	//.
	private TProcessingAbstract Processing;

	public int mStatus = STATUS_NO_DEVICE;

	private BroadcastReceiver mUsbBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (flDebug)
				Device.Log.WriteInfo("USBPluginModule","USBBroadcastReceiver message: "+action);
			if (ACTION_USB_PERMISSION.equals(action)){
				synchronized (this){
					UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
						try {
							OpenAccessory(accessory, true);
						}
						catch (Exception E) {
							Device.Log.WriteError("USBPluginModule","accessory opening error: "+E.getMessage());
						}
					else 
	            		Device.Log.WriteError("USBPluginModule","permission is denied for accessory: "+accessory);
					mPermissionRequestPending = false;
				}
			}
			else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)){
				UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null && accessory.equals(Accessory)) {
					try {
						CloseAccessory(true);
					} catch (Exception E) {
					}
				}
			}
		}
	};
	
	private TDoOnMessageIsReceivedHandler DoOnMessageIsReceivedHandler = null;
	
    public TUSBPluginModule(TPluginsModule pPluginsModule) 
    {
    	super(pPluginsModule);
    	//.
        Device = PluginsModule.Device;
		context = Device.context;
	}

	public void Destroy() {
		try {
			Finalize();
		} catch (Exception E) {
    		Device.Log.WriteError("USBPluginModule","finalization error: "+E.getMessage());
		}
	}

	@Override
	public void Start() throws Exception {
        if (IsEnabled())
    		try {
    			Initialize();
    		}
    		catch (Exception E) {
        		Device.Log.WriteError("USBPluginModule","initialization error: "+E.getMessage());
    		}
	}
	
	@Override
	public void Stop() throws Exception {
		try {
			Finalize();
		} catch (InterruptedException IE) {
    		Device.Log.WriteError("USBPluginModule","finalization error: "+IE.getMessage());
		}
	}
	
	private void Initialize() throws Exception {
		if (flInitialized)
			return; //. ->
		//.
		if (flRealPlugin) {
			mUsbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
			//.
			mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
			//.
			PIOModel = null;
			//.
			CheckConnectedAccesory();
			//.
			IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
			filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
			//.
			context.getApplicationContext().registerReceiver(mUsbBroadcastReceiver, filter);
		}
		else
			OpenTestAccessory();
		//.
		flInitialized = true;
	}
	
	private void Finalize() throws Exception {
		if (!flInitialized)
			return; //. ->
		//.
		flInitialized = false;
		if (flRealPlugin) {
			context.getApplicationContext().unregisterReceiver(mUsbBroadcastReceiver);
			//.
			CloseAccessory(false);
		}
		else
			CloseTestAccessory();
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
	
	public UsbAccessory CheckConnectedAccesory() throws Exception{
		UsbAccessory[] accessories = mUsbManager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (flDebug)
				Device.Log.WriteInfo("USBPluginModule","CheckConnectedAccesory(): accessory is found: "+accessory.getModel());
			if (mUsbManager.hasPermission(accessory)) 
				return OpenAccessory(accessory, true); //. ->
			else {
				synchronized (mUsbBroadcastReceiver) {
					if (!mPermissionRequestPending) {
						mUsbManager.requestPermission(accessory, mPermissionIntent);
						mPermissionRequestPending = true;
					}
				}
				return null; //. ->
			}
		}
		else
			return null; //. ->
	}

	private UsbAccessory OpenAccessory(UsbAccessory accessory, boolean flBuildModels) throws Exception
	{
		CloseAccessory(false);
		//.
		mAccessoryFileDescriptor = mUsbManager.openAccessory(accessory);
		if (mAccessoryFileDescriptor != null) {
			FileDescriptor fd = mAccessoryFileDescriptor.getFileDescriptor();
			mAccessoryInput = new FileInputStream(fd);
			mAccessoryOutput = new FileOutputStream(fd);
			//.
    		Device.Log.WriteInfo("USBPluginModule","accessory is attached: "+accessory);
			//.
			SetStatus(STATUS_ATTACHED_DEVICE);
			//.
			NegotiateWithAccessory();
			//.
			if (flBuildModels)
				BuildModelsAndPublish();
			//.
			Accessory = accessory;
			//.
    		Device.Log.WriteInfo("USBPluginModule","accessory is open: "+accessory);
		}
		else {
    		Device.Log.WriteError("USBPluginModule","accessory opening error: "+accessory);
    		//.
			Accessory = null;
		}
		return Accessory;
	}

	private void CloseAccessory(boolean flBuildModels) throws Exception
	{
		if (Accessory == null)
			return; //. ->
		UsbAccessory _Accessory = Accessory; 
		try{
			PIOModel = null;
			//.
			if (flBuildModels)
				BuildModelsAndPublish();
			//.
			if (Processing != null) {
				Processing.Destroy();
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
			if(mAccessoryFileDescriptor != null) {
				mAccessoryFileDescriptor.close();
				mAccessoryFileDescriptor = null;
			}
			//.
			Accessory = null;
			//.
			SetStatus(STATUS_NO_DEVICE);
			//.
    		Device.Log.WriteInfo("USBPluginModule","accessory is closed: "+_Accessory);
		}
		catch (IOException E) {
    		Device.Log.WriteError("USBPluginModule","error while closing accessory: "+_Accessory);
		}
	}
	
	private void BuildModelsAndPublish() throws Exception {
		if (flInitialized) {
			Device.ControlsModule.BuildModelAndPublish();
			Device.SensorsModule.BuildModelAndPublish();
		}
	}
	
	@SuppressWarnings("unused")
	private void NegotiateWithAccessory0() throws Exception {
		PIOModel = new TModel(this);
		//.
		TChannel Channel = new com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel(this); 
		Channel.ID = TChannel.GetNextID();
		Channel.Enabled = true;
		Channel.Kind = TChannel.CHANNEL_KIND_IN;
		Channel.DataFormat = 0;
		Channel.Name = "Device rotator";
		Channel.Info = "An USB plugin rotator of the device";
		Channel.Size = 0;
		Channel.Configuration = "1:1,0,1;0";
		Channel.Parameters = "";
		//.
		Channel.Parse();
		//.
		PIOModel.ControlStream.Channels.add(Channel);
		//.
		Channel = new com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel(this); 
		Channel.ID = TChannel.GetNextID();
		Channel.Enabled = true;
		Channel.Kind = TChannel.CHANNEL_KIND_OUT;
		Channel.DataFormat = 0;
		Channel.Name = "Environment conditions (USBLukin)";
		Channel.Info = "Weather conditions: temperature, pressure, humidity etc";
		Channel.Size = 0;
		Channel.Configuration = "1:1,3,0,1,2;0";
		Channel.Parameters = "";
		//.
		Channel.Parse();
		//.
		PIOModel.Stream.Channels.add(Channel);
		//.
		Processing = new TProcessing();
	}
	
	private void NegotiateWithAccessory() throws Exception {
		//. request "PIO" protocol
		byte[] BA = PIO.ID();
		mAccessoryOutput.write(BA);
		mAccessoryOutput.flush();
		BA = new byte[2];
		mAccessoryInput.read(BA);
		if (!((BA[0] == 0x4F) && (BA[1] == 0x4B))) //. is "OK"
			throw new IOException("the PIO protocol is not supported by USB accessory"); //. =>
		//. PIO is requested successfully so start command processing
		Processing = new TProcessing();
		//. process get "MODEL" command
        PIO.TMODELCommand MODELCommand = new PIO.TMODELCommand(this);
    	OutgoingCommands_ProcessCommand(MODELCommand);
    	PIOModel = MODELCommand.Value;
    	if (PIOModel != null)
    		Device.Log.WriteInfo("USBPluginModule","Model has been set: "+PIOModel.Name+", channels: "+Integer.toString(PIOModel.Stream.Channels.size()));
    	else
    		Device.Log.WriteInfo("USBPluginModule","Model is null");
	}
	
	public class TProcessing extends TProcessingAbstract {

		public TProcessing() {
    		_Thread = new Thread(this);
    		_Thread.start();
		}
		
		@Override
		public void Destroy() throws InterruptedException {
			CancelByCancellerAndWait(); //. cancel by canceller only to unsure that reading are finshed correctly (a workaround for ENODEV android bug) 
		}
		
		@Override
		public void run() {
			try {
				byte[] MessageBA = new byte[MessageMaxSize];
				int MessageBAIdx = 0;
				if (flDebug)
					Device.Log.WriteInfo("USBPluginModule","Processing is started");
				//.
				while (!Canceller.flCancel) {
					int RS = mAccessoryInput.read(MessageBA,MessageBAIdx,MessageBA.length-MessageBAIdx);
					if (RS > 0) {
						int LastMessageBAIdx = MessageBAIdx;
						MessageBAIdx += RS;
			    		//.
						while (LastMessageBAIdx < MessageBAIdx) {
							if (MessageBA[LastMessageBAIdx] == 0x21) {
					            String Message = new String(MessageBA,0,LastMessageBAIdx,"windows-1251");
					            //. process message
					            if (flDebug)
					            	Device.Log.WriteInfo("USBPluginModule","message is received: "+Message);
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
								LastMessageBAIdx++;
								//.
								int Cnt = MessageBAIdx-LastMessageBAIdx;
								for (int I = 0; I < Cnt; I++)
									MessageBA[I] = MessageBA[LastMessageBAIdx+I];
								MessageBAIdx = Cnt;
								LastMessageBAIdx = 0;
							}
							else
								LastMessageBAIdx++;
						}
					}
				}
			}
			catch (Throwable T) {
	    		Device.Log.WriteError("USBPluginModule","error while reading accessory: "+Accessory+", "+T.getMessage());
			}
		}

		@Override
		public void SendMessage(String CommandMessage) throws IOException {		
			if (mAccessoryOutput != null) {
				try {
					mAccessoryOutput.write(CommandMessage.getBytes());
					mAccessoryOutput.flush();
					//.
		            if (flDebug)
		            	Device.Log.WriteInfo("USBPluginModule","message is sent: "+CommandMessage);
				} catch (IOException IOE) {
		    		Device.Log.WriteError("USBPluginModule","error while writing accessory: "+Accessory+", "+IOE.getMessage());
		    		//.
		    		throw IOE; //. =>
				}
			}
		}
	}

	private void OpenTestAccessory() throws Exception {
		CloseTestAccessory();
		//.
		PIOModel = new TModel(this);
		//.
		TChannel Channel = new com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.ControlStream.Channels.DeviceRotator.DVRT.TDVRTChannel(this); 
		Channel.ID = TChannel.GetNextID();
		Channel.Enabled = true;
		Channel.Kind = TChannel.CHANNEL_KIND_IN;
		Channel.DataFormat = 0;
		Channel.Name = "Device rotator";
		Channel.Info = "An USB plugin rotator of the device";
		Channel.Size = 0;
		Channel.Configuration = "1:1,0,1;0";
		Channel.Parameters = "";
		//.
		Channel.Parse();
		//.
		PIOModel.ControlStream.Channels.add(Channel);
		//.
		Channel = new com.geoscope.GeoLog.DEVICE.PluginsModule.IO.Protocols.PIO.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel(this); 
		Channel.ID = TChannel.GetNextID();
		Channel.Enabled = true;
		Channel.Kind = TChannel.CHANNEL_KIND_OUT;
		Channel.DataFormat = 0;
		Channel.Name = "Environment conditions";
		Channel.Info = "Weather conditions: temperature, pressure, humidity etc";
		Channel.Size = 0;
		Channel.Configuration = "1:1,0,1,2,3;0";
		Channel.Parameters = "";
		//.
		Channel.Parse();
		//.
		PIOModel.Stream.Channels.add(Channel);
		//.
		Processing = new TTestProcessing();
		//.
		if (flInitialized) {
			Device.ControlsModule.BuildModelAndPublish();
			Device.SensorsModule.BuildModelAndPublish();
		}
	}
	
	
	private void CloseTestAccessory() throws InterruptedException {
		if (Processing != null) {
			Processing.CancelAndWait();
			Processing = null;
		}
	}
	
	public class TTestProcessing extends TProcessingAbstract {

		public TTestProcessing() {
    		_Thread = new Thread(this);
    		_Thread.start();
		}
		
		@Override
		public void Destroy() throws InterruptedException {
			CancelAndWait(); 
		}
		
		private int Idx = 0;
		private Random rnd = new Random();
		
		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					Thread.sleep(1000);
					//.					
		            String Message = "@ADC 1,"+Integer.toString(Idx)+","+Integer.toString(rnd.nextInt(100))+",123,0";
		            Idx++;
		            if (Idx > 2)
		            	Idx = 0;
		            //.
		            if (flDebug)
		            	Device.Log.WriteInfo("USBPluginModule","test message is received: "+Message);
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
			    		Device.Log.WriteError("USBPluginModule","error while processing test message: "+E.getMessage());
					}
				}
			}
			catch (Throwable T) {
	    		Device.Log.WriteError("USBPluginModule","error while reading test accessory: "+Accessory+", "+T.getMessage());
			}
		}

		@Override
		public void SendMessage(String CommandMessage) throws IOException {		
            if (flDebug)
            	Device.Log.WriteInfo("USBPluginModule","message is sent: "+CommandMessage);
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
			TCommand OC = TCommand.GetCommandByName(CommandResponseData.Command, this, new PIO.TCommand.TCommandSender() {
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
			});
			if (OC != null) {
				if (OC.DoOnCommandResponse(CommandResponseData)) 
					OC.ProcessResponse();
			}
			else 
				throw new Exception("unknown command: "+CommandResponseData.Command); //. =>
		}
	}
	
	private void HandleCommandResponse(TCommand Command) throws Exception {
		if (PIOModel != null)
			PIOModel.DoOnCommandResponse(Command);
	}
}
