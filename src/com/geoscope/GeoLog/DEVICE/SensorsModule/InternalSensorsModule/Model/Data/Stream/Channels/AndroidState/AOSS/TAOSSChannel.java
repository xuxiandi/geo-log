package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.AndroidState.AOSS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Stream.Channel.TChannel;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.Data.Stream.Channel.TDataTypes;
import com.geoscope.Classes.Data.Stream.Channel.ContainerTypes.TInt32ContainerType;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.TInternalSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel;

@SuppressLint("HandlerLeak")
public class TAOSSChannel extends TTLRChannel {

	public static final String TypeID = TTLRChannel.TypeID+"."+"AndroidState.AOSS";
	
	public static class TMyProfile extends TChannel.TProfile {
		
		public int	SampleInterval = 1000*2; //. seconds

		public TMyProfile() {
			super();
		}

		public TMyProfile(byte[] ProfileData) throws Exception {
			super(ProfileData);
		}

		@Override
		public void FromXMLNode(Node ANode) throws Exception {
			super.FromXMLNode(ANode);
			//. SampleRate
			Node _Node = TMyXML.SearchNode(ANode,"SampleInterval");
			if (_Node != null) {
				Node ValueNode = _Node.getFirstChild();
				if (ValueNode != null)
					SampleInterval = Integer.parseInt(ValueNode.getNodeValue());
			}
		}
		
		@Override
		public synchronized void ToXMLSerializer(XmlSerializer Serializer) throws Exception {
			super.ToXMLSerializer(Serializer);
	    	//. SampleInterval
	        Serializer.startTag("", "SampleInterval");
	        Serializer.text(Integer.toString(SampleInterval));
	        Serializer.endTag("", "SampleInterval");
		}
		
		@Override
		public Intent GetProfilePanel(Activity Parent) throws Exception {
			Intent Result = new Intent(Parent, TAOSSChannelProfilePanel.class);
			Result.putExtra("ProfileData", ToByteArray());
			//.
			return Result;
		}
	}
	
	
	public class TSampleSource extends TCancelableThread {
		
		public boolean flStarted = false;
		//.
		private com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel _DestinationChannel;
		
		public TSampleSource() {
    		super();
		}
		
		public void Release() throws Exception {
			Stop();
		}
		
		public void Start() {
			Canceller.Reset();
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Stop() throws Exception {
			if (_Thread != null) {
				CancelAndWait();
				_Thread = null;
			}
		}
		
		@Override
		public void run() {
			try {
				flStarted = true;
				try {
					com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel __DestinationChannel = DestinationChannel_Get();
					if (!(__DestinationChannel instanceof com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel))
			        	throw new IOException("No destination channel"); //. ->
					_DestinationChannel = (com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Telemetry.TLR.TTLRChannel)__DestinationChannel;
					//.
					while (!Canceller.flCancel) {
						DoSamples();
						//.
						Thread.sleep(MyProfile.SampleInterval);
					}
				}
				finally {
					flStarted = false;
				}
			}
        	catch (InterruptedException IE) {
        	}
			catch (Throwable TE) {
				InternalSensorsModule.Device.Log.WriteWarning("AOSSChannel.SampleSource","Exception: "+TE.getMessage());
			}
		}
		
		private String executeTop() {
		    java.lang.Process p = null;
		    BufferedReader in = null;
		    String returnString = null;
		    try {
		        p = Runtime.getRuntime().exec("top -n 1");
		        in = new BufferedReader(new InputStreamReader(p.getInputStream()));
		        while (returnString == null || returnString.contentEquals("")) {
		            returnString = in.readLine();
		        }
		    } catch (IOException IOE) {
				InternalSensorsModule.Device.Log.WriteWarning("AOSSChannel.SampleSource.CPUUsage","Exception: "+IOE.getMessage());
		    } finally {
		        try {
		            in.close();
		            p.destroy();
		        } catch (IOException IOE) {
					InternalSensorsModule.Device.Log.WriteWarning("AOSSChannel.SampleSource.CPUUsage","Exception: "+IOE.getMessage());
		        }
		    }
		    return returnString;
		}
		
		private int[] getCpuUsageStatistic() {
		    String tempString = executeTop();
		    if (tempString == null)
		    	return null;
		    tempString = tempString.replaceAll(",", "");
		    tempString = tempString.replaceAll("User", "");
		    tempString = tempString.replaceAll("System", "");
		    tempString = tempString.replaceAll("IOW", "");
		    tempString = tempString.replaceAll("IRQ", "");
		    tempString = tempString.replaceAll("%", "");
		    for (int i = 0; i < 10; i++) {
		        tempString = tempString.replaceAll("  ", " ");
		    }
		    tempString = tempString.trim();
		    String[] myString = tempString.split(" ");
		    int[] cpuUsageAsInt = new int[myString.length];
		    for (int i = 0; i < myString.length; i++) {
		        myString[i] = myString[i].trim();
		        cpuUsageAsInt[i] = Integer.parseInt(myString[i]);
		    }
		    return cpuUsageAsInt;
		}
		
		private void DoSamples() throws Exception {
			//. getting memory info ...
	        Integer _ProcessFreeMemory = (int)((Runtime.getRuntime().maxMemory()-Runtime.getRuntime().freeMemory()) >> 20);
	        StatFs SDstat = new StatFs(Environment.getExternalStorageDirectory().getPath());
	        @SuppressWarnings("deprecation")
			Integer _StorageFreeSize = (int)(((long)SDstat.getAvailableBlocks()*(long)SDstat.getBlockSize()) >> 20);
	        ProcessFreeMemory.SetContainerTypeValue(_ProcessFreeMemory);
			_DestinationChannel.DoOnData(ProcessFreeMemory);
			StorageFreeSize.SetContainerTypeValue(_StorageFreeSize);
			_DestinationChannel.DoOnData(StorageFreeSize);
			//. getting CPU usage info ...
			int[] CPUsage = getCpuUsageStatistic();
			if ((CPUsage != null) && (CPUsage.length == 4)) {
				Integer _UserCPUUsage = CPUsage[0];
				Integer _SystemCPUUsage = CPUsage[1];
				Integer _IdleCPUUsage = CPUsage[2];
				Integer _OtherCPUUsage = CPUsage[3];
				UserCPUUsage.SetContainerTypeValue(_UserCPUUsage);
				_DestinationChannel.DoOnData(UserCPUUsage);
				SystemCPUUsage.SetContainerTypeValue(_SystemCPUUsage);
				_DestinationChannel.DoOnData(SystemCPUUsage);
				IdleCPUUsage.SetContainerTypeValue(_IdleCPUUsage);
				_DestinationChannel.DoOnData(IdleCPUUsage);
				OtherCPUUsage.SetContainerTypeValue(_OtherCPUUsage);
				_DestinationChannel.DoOnData(OtherCPUUsage);
			}
		}
	}
	
	private TMyProfile MyProfile;
	//.
	private TDataType ProcessFreeMemory;
	private TDataType StorageFreeSize;
	private TDataType UserCPUUsage;
	private TDataType SystemCPUUsage;
	private TDataType IdleCPUUsage;
	private TDataType OtherCPUUsage;
	//.
	private TSampleSource SampleSource;
	
	public TAOSSChannel(TInternalSensorsModule pInternalSensorsModule, int pID) throws Exception {
		super(pInternalSensorsModule, pID, TMyProfile.class);
		MyProfile = (TMyProfile)Profile;
		//.
		Kind = TChannel.CHANNEL_KIND_OUT;
		DataFormat = 0;
		Name = "OS state";
		Info = "Android operational system state";
		Size = 8192;
		Configuration = "";
		Parameters = "";
		//.
		DataTypes = new TDataTypes();
		//.
		ProcessFreeMemory = DataTypes.AddItem(new TDataType(new TInt32ContainerType(),	"ProcessFreeMemory",	this, 1, "","", "Mb")); 	
		StorageFreeSize = DataTypes.AddItem(new TDataType(new TInt32ContainerType(), 	"StorageFreeSize", 		this, 2, "","", "Mb")); 	
		UserCPUUsage = DataTypes.AddItem(new TDataType(new TInt32ContainerType(), 		"UserCPUUsage", 		this, 3, "","", "%")); 	
		SystemCPUUsage = DataTypes.AddItem(new TDataType(new TInt32ContainerType(), 	"SystemCPUUsage", 		this, 4, "","", "%")); 	
		IdleCPUUsage = DataTypes.AddItem(new TDataType(new TInt32ContainerType(), 		"IdleCPUUsage", 		this, 5, "","", "%")); 	
		OtherCPUUsage = DataTypes.AddItem(new TDataType(new TInt32ContainerType(), 		"OtherCPUUsage", 		this, 6, "","", "%")); 	
		//.
		SampleSource = new TSampleSource();
	}
	
	@Override
	public void Close() throws Exception {
		if (SampleSource != null) {
			SampleSource.Release();
			SampleSource = null;
		}
		//.
		super.Close();
	}
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	public void StartSource() {
		PostStart();
	}

	@Override
	public void StopSource() {
		PostStop();
	}
	
	@Override
	public boolean IsActive() {
		return SampleSource.flStarted;
	}
	
    public void PostStart() {
		MessageHandler.obtainMessage(MESSAGE_START).sendToTarget();
    }
    
    public void PostStop() {
		MessageHandler.obtainMessage(MESSAGE_STOP).sendToTarget();
    }
    
	public static final int MESSAGE_START 	= 1;
	public static final int MESSAGE_STOP 	= 2;
	
	public Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_START: 
                	try {
            			SampleSource.Start();
                	}
                	catch (Exception E) {
                		Toast.makeText(InternalSensorsModule.Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >

                case MESSAGE_STOP: 
                	try {
            			SampleSource.Stop();
                	}
                	catch (Exception E) {
                		Toast.makeText(InternalSensorsModule.Device.context, E.getMessage(), Toast.LENGTH_LONG).show();
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
