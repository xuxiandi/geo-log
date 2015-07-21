package com.geoscope.GeoLog.DEVICE.SensorsModule.Meters;

import java.io.IOException;
import java.util.ArrayList;

import com.geoscope.GeoLog.DEVICE.SensorsModule.TSensorsModule;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Measurements.TSensorsModuleMeasurements;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meter.TSensorMeterInfo;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.AV.TAVMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Audio.TAudioMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.AOSS.TAOSSMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ASTLR.TASTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.ECTLR.TECTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Telemetry.GPSTLR.TGPSTLRMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Meters.Video.TVideoMeter;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Audio.AAC.TAACChannel;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream.Channels.Video.H264I.TH264IChannel;

public class TSensorsMeters {

	private TSensorsModule SensorsModule;
	//.
	public String ProfileFolder;
	//.
	public ArrayList<TSensorMeter> Items = new ArrayList<TSensorMeter>();
	
	public TSensorsMeters(TSensorsModule pSensorsModule, String pProfileFolder) {
		SensorsModule = pSensorsModule;
		ProfileFolder = pProfileFolder;
	}
	
	public void Destroy() throws Exception {
		Finalize();
	}

	public void Initialize() throws Exception {
		CreateMeters();
		//.
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++)
			Items.get(I).Initialize();
	}
	
	public void Finalize() throws Exception {
		Items_Clear();
	}

	private void CreateMeters() throws Exception {
		Items_Clear();
		//.
		TECTLRMeter ECTLRMeter = new TECTLRMeter(SensorsModule, "0", "", ProfileFolder); 
		Items_AddItem(ECTLRMeter);
		//.
		TASTLRMeter ASTLRMeter = new TASTLRMeter(SensorsModule, "0", "", ProfileFolder); 	
		Items_AddItem(ASTLRMeter);
		//.
		TAOSSMeter AOSSMeter = new TAOSSMeter(SensorsModule, "0", "", ProfileFolder); 
		Items_AddItem(AOSSMeter);
		//.
		TGPSTLRMeter GPSTLRMeter = new TGPSTLRMeter(SensorsModule, "0", "", ProfileFolder); 	
		Items_AddItem(GPSTLRMeter);
		//.
		TAudioMeter	AudioMeter = new TAudioMeter(SensorsModule, "0", "normal quality", ProfileFolder) {
			
			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				SourceChannel = SensorsModule.InternalSensorsModule.AACChannel; 
				if (SourceChannel == null)
					throw new IOException("no source channel"); //. =>
				if (!SourceChannel.Enabled)
					throw new IOException("the source channel is disabled"); //. =>
				Channel = (TAACChannel)SourceChannel.DestinationChannel_Get(); 	
				if (Channel == null)
					throw new IOException("no source channel"); //. =>
				return (new TStreamChannel[] {Channel}); 	
			}
		}; 	
		Items_AddItem(AudioMeter);
		//.
		AudioMeter = new TAudioMeter(SensorsModule, "LowQuality", "low quality", ProfileFolder) {

			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				SourceChannel = SensorsModule.InternalSensorsModule.AACChannelLowQuality; 
				if (SourceChannel == null)
					throw new IOException("no source channel"); //. =>
				if (!SourceChannel.Enabled)
					throw new IOException("the source channel is disabled"); //. =>
				Channel = (TAACChannel)SourceChannel.DestinationChannel_Get(); 	
				if (Channel == null)
					throw new IOException("no source channel"); //. =>
				return (new TStreamChannel[] {Channel}); 	
			};
		}; 
		Items_AddItem(AudioMeter);
		//.
		AudioMeter = new TAudioMeter(SensorsModule, "HighQuality", "high quality", ProfileFolder) {

			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				SourceChannel = SensorsModule.InternalSensorsModule.AACChannelHighQuality; 
				if (SourceChannel == null)
					throw new IOException("no source channel"); //. =>
				if (!SourceChannel.Enabled)
					throw new IOException("the source channel is disabled"); //. =>
				Channel = (TAACChannel)SourceChannel.DestinationChannel_Get(); 	
				if (Channel == null)
					throw new IOException("no source channel"); //. =>
				return (new TStreamChannel[] {Channel}); 	
			};
		}; 
		Items_AddItem(AudioMeter);
		//.
		TVideoMeter	 VideoMeter	= new TVideoMeter(SensorsModule, "0", "normal quality", ProfileFolder) {
			
			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				SourceChannel = SensorsModule.InternalSensorsModule.H264IChannel;
				if (SourceChannel == null)
					throw new IOException("no source channel"); //. =>
				if (!SourceChannel.Enabled)
					throw new IOException("the source channel is disabled"); //. =>
				Channel = (TH264IChannel)SourceChannel.DestinationChannel_Get(); 	
				if (Channel == null)
					throw new IOException("no source channel"); //. =>
				return (new TStreamChannel[] {Channel}); 	
			};
		};
		Items_AddItem(VideoMeter);
		//.
		VideoMeter	= new TVideoMeter(SensorsModule, "LowQuality", "low quality", ProfileFolder) {
			
			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				SourceChannel = SensorsModule.InternalSensorsModule.H264IChannelLowQuality;
				if (SourceChannel == null)
					throw new IOException("no source channel"); //. =>
				if (!SourceChannel.Enabled)
					throw new IOException("the source channel is disabled"); //. =>
				Channel = (TH264IChannel)SourceChannel.DestinationChannel_Get(); 	
				if (Channel == null)
					throw new IOException("no source channel"); //. =>
				return (new TStreamChannel[] {Channel}); 	
			};
		};
		Items_AddItem(VideoMeter);
		//.
		VideoMeter	= new TVideoMeter(SensorsModule, "HighQuality", "high quality", ProfileFolder) {
			
			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				SourceChannel = SensorsModule.InternalSensorsModule.H264IChannelHighQuality;
				if (SourceChannel == null)
					throw new IOException("no source channel"); //. =>
				if (!SourceChannel.Enabled)
					throw new IOException("the source channel is disabled"); //. =>
				Channel = (TH264IChannel)SourceChannel.DestinationChannel_Get(); 	
				if (Channel == null)
					throw new IOException("no source channel"); //. =>
				return (new TStreamChannel[] {Channel}); 	
			};
		};
		Items_AddItem(VideoMeter);
		//.
		TAVMeter AVMeter = new TAVMeter(SensorsModule, "0", "normal quality", ProfileFolder) {
			
			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				AudioSourceChannel = SensorsModule.InternalSensorsModule.AACChannel; 
				if (AudioSourceChannel == null)
					throw new IOException("no source audio channel"); //. =>
				if (!AudioSourceChannel.Enabled)
					throw new IOException("the source audio channel is disabled"); //. =>
				AudioChannel = (TAACChannel)AudioSourceChannel.DestinationChannel_Get(); 	
				if (AudioChannel == null)
					throw new IOException("no source audio channel"); //. =>
				//.
				VideoSourceChannel = SensorsModule.InternalSensorsModule.H264IChannel;
				if (VideoSourceChannel == null)
					throw new IOException("no source video channel"); //. =>
				if (!VideoSourceChannel.Enabled)
					throw new IOException("the source video channel is disabled"); //. =>
				VideoChannel = (TH264IChannel)VideoSourceChannel.DestinationChannel_Get(); 	
				if (VideoChannel == null)
					throw new IOException("no source video channel"); //. =>
				return (new TStreamChannel[] {AudioChannel,VideoChannel}); 	
			}
		}; 	
		Items_AddItem(AVMeter);
		//.
		AVMeter = new TAVMeter(SensorsModule, "LowQuality", "low quality", ProfileFolder) {
			
			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				AudioSourceChannel = SensorsModule.InternalSensorsModule.AACChannelLowQuality; 
				if (AudioSourceChannel == null)
					throw new IOException("no source audio channel"); //. =>
				if (!AudioSourceChannel.Enabled)
					throw new IOException("the source audio channel is disabled"); //. =>
				AudioChannel = (TAACChannel)AudioSourceChannel.DestinationChannel_Get(); 	
				if (AudioChannel == null)
					throw new IOException("no source audio channel"); //. =>
				//.
				VideoSourceChannel = SensorsModule.InternalSensorsModule.H264IChannelLowQuality;
				if (VideoSourceChannel == null)
					throw new IOException("no source video channel"); //. =>
				if (!VideoSourceChannel.Enabled)
					throw new IOException("the source video channel is disabled"); //. =>
				VideoChannel = (TH264IChannel)VideoSourceChannel.DestinationChannel_Get(); 	
				if (VideoChannel == null)
					throw new IOException("no source video channel"); //. =>
				return (new TStreamChannel[] {AudioChannel,VideoChannel}); 	
			}
		};
		Items_AddItem(AVMeter);
		//.
		AVMeter = new TAVMeter(SensorsModule, "HighQuality", "high quality", ProfileFolder) {
			
			@Override
			public TStreamChannel[] GetChannels() throws Exception {
				AudioSourceChannel = SensorsModule.InternalSensorsModule.AACChannelHighQuality; 
				if (AudioSourceChannel == null)
					throw new IOException("no source audio channel"); //. =>
				if (!AudioSourceChannel.Enabled)
					throw new IOException("the source audio channel is disabled"); //. =>
				AudioChannel = (TAACChannel)AudioSourceChannel.DestinationChannel_Get(); 	
				if (AudioChannel == null)
					throw new IOException("no source audio channel"); //. =>
				//.
				VideoSourceChannel = SensorsModule.InternalSensorsModule.H264IChannelHighQuality;
				if (VideoSourceChannel == null)
					throw new IOException("no source video channel"); //. =>
				if (!VideoSourceChannel.Enabled)
					throw new IOException("the source video channel is disabled"); //. =>
				VideoChannel = (TH264IChannel)VideoSourceChannel.DestinationChannel_Get(); 	
				if (VideoChannel == null)
					throw new IOException("no source video channel"); //. =>
				return (new TStreamChannel[] {AudioChannel,VideoChannel}); 	
			}
		};
		Items_AddItem(AVMeter);
	}
	
	private synchronized void Items_AddItem(TSensorMeter Meter) {
		Items.add(Meter);
	}
	
	private synchronized void Items_Clear() throws Exception {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++)
			Items.get(I).Destroy();
		Items.clear();
	}
	
	public synchronized TSensorMeter Items_GetItem(String MeterID) {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			if (Meter.Descriptor.ID.equals(MeterID))
				return Meter; //. ->
		}
		return null;
	}
	
	public synchronized String Items_GetList(int Version) {
		switch (Version) {
		
		case 1:
			StringBuilder SB = new StringBuilder();
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TSensorMeter Meter = Items.get(I);
				SB.append(Meter.Descriptor.ID);
				SB.append(","+Meter.Descriptor.TypeID);
				SB.append(","+Meter.Descriptor.ContainerTypeID);
				SB.append(","+Meter.Descriptor.Name);
				SB.append(","+Meter.Descriptor.Info);
				SB.append(","+Meter.Descriptor.Configuration);
				SB.append(","+Meter.Descriptor.Parameters);
				SB.append(","+(Meter.IsEnabled() ? "1" : "0"));
				SB.append(","+(Meter.IsActive() ? "1" : "0"));
				SB.append(","+Integer.toString(Meter.GetStatus()));
				if (I < (Cnt-1))
					SB.append(";");
			}
			return SB.toString(); //. >
			
		default:
			return null; //. ->
		}
	}

	public synchronized TSensorMeterInfo[] Items_GetList() {
		int Cnt = Items.size();
		TSensorMeterInfo[] Result = new TSensorMeterInfo[Cnt];
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			//.
			TSensorMeterDescriptor Descriptor = new TSensorMeterDescriptor();
			//.
			Descriptor.ID = Meter.Descriptor.ID;
			//.
			Descriptor.TypeID = Meter.Descriptor.TypeID;
			Descriptor.ContainerTypeID = Meter.Descriptor.ContainerTypeID;
			//.
			Descriptor.Name = Meter.Descriptor.Name;
			Descriptor.Info = Meter.Descriptor.Info;
			//.
			Descriptor.Configuration = Meter.Descriptor.Configuration;
			Descriptor.Parameters = Meter.Descriptor.Parameters;
			//.
			Result[I] = new TSensorMeterInfo(Descriptor, Meter.IsEnabled(), Meter.IsActive(), Meter.GetStatus());
		}
		return Result;
	}
	
	public synchronized void Items_ValidateActivity(String[] MeterIDs) throws Exception {
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			boolean flFound = false;
			int Cnt1 = MeterIDs.length;
			for (int J = 0; J < Cnt1; J++)
				if (MeterIDs[J].equals(Meter.Descriptor.ID)) {
					flFound = true;
					break; //. >
				}
			if (flFound) {
				if (!Meter.IsActive())
					Meter.SetActive(true);
			}
			else {
				if (Meter.IsActive())
					Meter.SetActive(false);
			}
		}
	}
	
	public void Measurements_RemoveOld() throws Exception {
    	ArrayList<String> MIDs = TSensorsModuleMeasurements.GetMeasurementsIDs();
    	if (MIDs == null)
    		return; //. ->
		int Cnt = Items.size();
		for (int I = 0; I < Cnt; I++) {
			TSensorMeter Meter = Items.get(I);
			Meter.Measurements_RemoveOld(MIDs);
		}
	}	
}
