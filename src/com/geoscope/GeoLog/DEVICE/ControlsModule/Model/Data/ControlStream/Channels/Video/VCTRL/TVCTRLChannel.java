package com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.IO.Net.TNetworkConnection;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ChannelLockedError;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ChannelNotActiveError;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ChannelNotFoundError;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.SourceNonExclusiveAccessError;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.SourceNotAvailableError;
import com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel.ValueOutOfRangeError;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.Data.TStreamChannel;

public class TVCTRLChannel extends TStreamChannel {

	public static final String TypeID = "Video.VCTRL";
	
	public static final int DescriptorSize = 4;
	
	public static final int RESULT_OK 				= 0;
	public static final int RESULT_ERROR 			= -1;
	//.
	public static final int RESULT_CHANNELNOTFOUND 			= -101;
	public static final int RESULT_CHANNELNOTACTIVE 		= -102;
	public static final int RESULT_CHANNELLOCKED 			= -103;
	public static final int RESULT_SOURCENOTAVAILABLE		= -104;
	public static final int RESULT_SOURCENONEXCLUSIVEACCESS	= -105;
	public static final int RESULT_VALUEOUTOFRANGE			= -106;
	
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}	

	@Override
	public void DoStreaming(InputStream pInputStream, OutputStream pOutputStream, TCanceller Canceller) throws IOException {
		byte[] TransferBuffer = new byte[DescriptorSize];
		int Size;
		while (!Canceller.flCancel) {
			try {
                Size = pInputStream.read(TransferBuffer,0,DescriptorSize);
                if (Size <= 0) 
                	break; //. >
			}
			catch (SocketTimeoutException E) {
				continue; //. ^
			}
			if (Size != DescriptorSize)
				throw new IOException("wrong data descriptor"); //. =>
			Size = (TransferBuffer[3] << 24)+((TransferBuffer[2] & 0xFF) << 16)+((TransferBuffer[1] & 0xFF) << 8)+(TransferBuffer[0] & 0xFF);
			if (Size > 0) { 
				if (Size > TransferBuffer.length)
					TransferBuffer = new byte[Size];
				Size = TNetworkConnection.InputStream_ReadData(pInputStream, TransferBuffer, Size);	
                if (Size <= 0) 
                	break; //. >
				//. parse and process
                byte[] Descriptor;
                try {
                	ParseFromByteArrayAndProcess(TransferBuffer, 0);
                	//. success
          			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(RESULT_OK);
                }
                catch (ChannelNotFoundError CNFE) {
          			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(RESULT_CHANNELNOTFOUND);
                }
                catch (ChannelNotActiveError CNAE) {
          			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(RESULT_CHANNELNOTACTIVE);
                }
                catch (ChannelLockedError CLE) {
          			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(RESULT_CHANNELLOCKED);
                }
                catch (SourceNotAvailableError SNAE) {
          			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(RESULT_SOURCENOTAVAILABLE);
                }
                catch (SourceNonExclusiveAccessError SNEAE) {
          			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(RESULT_SOURCENONEXCLUSIVEACCESS);
                }
                catch (ValueOutOfRangeError VOORE) {
          			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(RESULT_VALUEOUTOFRANGE);
                }
                catch (Exception E) {
          			Descriptor = TDataConverter.ConvertInt32ToLEByteArray(RESULT_ERROR);
                }
      			pOutputStream.write(Descriptor);		
			}
		}    	
	}		
	
	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx) throws Exception {
		com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel DC = (com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model.Data.ControlStream.Channels.Video.VCTRL.TVCTRLChannel)DestinationChannel;
		int ID = TDataConverter.ConvertLEByteArrayToInt32(BA, Idx); Idx += DescriptorSize;
		if (DC.DataTypes != null) {
			TDataType DataType = DC.DataTypes.GetItemByID((short)ID);
			if (DataType != null) {
				Idx = DataType.ContainerType.FromByteArray(BA, Idx);
				//.
				DC.DataType_SetValue(DataType);
			}
		}
		return Idx;
	}
}
