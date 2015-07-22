package com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.Stream.Channels.Telemetry.TLR;

import java.io.IOException;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Stream.Channel.TDataType;
import com.geoscope.Classes.IO.Abstract.TStream;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.Space.TypesSystem.DataStream.Model.Data.TStreamChannel;

public class TTLRChannel extends TStreamChannel {

	public static final String TypeID = "Telemetry.TLR";

	public static final int DescriptorSize = 2;
	//.
	private static int MaxUnderlyingStreamSize = 1024*100;
	
	
	private short	BufferSize = 0;
	private byte[] 	Buffer = new byte[1024];
	//.
	public TDoOnDataHandler OnDataHandler = null;
	
	@Override
	public String GetTypeID() {
		return TypeID;
	}
	
	@Override
    public void DoOnRead(TStream Stream, int ReadSize, TOnProgressHandler OnProgressHandler, TOnIdleHandler OnIdleHandler, TOnExceptionHandler OnExceptionHandler, TCanceller Canceller) {
    	try {
    		try {
    			int SP;
    			byte[] BufferSizeBA = new byte[2];
    			while (true) {
    		    	  SP = (int)(Stream.Size-Stream.Position);
    		    	  if (!BufferSize_flRead) {
    		  	    	    if (SP >= BufferSizeBA.length) {
    			  	    	      Stream.Read(BufferSizeBA,BufferSizeBA.length);
    			  	    	      BufferSize = TDataConverter.ConvertLEByteArrayToInt16(BufferSizeBA,0);
    			  	    	      BufferSize_flRead = true;
    			  	    	      SP -= BufferSizeBA.length;
    		  	    	    }
    		  	    	    else 
    		  	    	    	return; //. ->
    		    	  }
    	  	    	  if (SP >= BufferSize) {
    	  	    		  BufferSize_flRead = false;
    	  	    		  if (BufferSize > 0) {
    	  	    			  if (BufferSize > Buffer.length)
    			    				Buffer = new byte[BufferSize];
    	  	  	    	      Stream.Read(Buffer,BufferSize);
    	  	  	    	      //. processing
    	  	  	    	      try {
    	  	  	    	    	  ParseFromByteArrayAndProcess(Buffer, 0, BufferSize);
    	  	  	    	    	  //.
    	  	  	    	    	  OnProgressHandler.DoOnProgress(BufferSize, Canceller);
    	  	  	    	      } catch (IOException IOE) {
    	  	  	    	    	  if (OnExceptionHandler != null)
    	  	  	    	    		  OnExceptionHandler.DoOnException(IOE);
    	  	  	    	      }
    	  	    		  }
    	  	    	  }
    	  	    	  else
		  	    	    	return; //. ->
    			}
    		}
	    	finally {
		    	if ((Stream.Size > MaxUnderlyingStreamSize) && (Stream.Size == Stream.Position))
		    		Stream.Clear();
		    }
		} catch (Exception E) {
  	    	  if (OnExceptionHandler != null)
    	    		  OnExceptionHandler.DoOnException(E);
		}
    }

	@Override
	public int ParseFromByteArrayAndProcess(byte[] BA, int Idx, int Size) throws Exception {
		short ID = TDataConverter.ConvertLEByteArrayToInt16(BA, Idx); Idx += 2; //. SizeOf(DataType.ID)
		if (DataTypes != null) {
			TDataType DataType = DataTypes.GetItemByID(ID);
			if (DataType != null) {
				Idx = DataType.ContainerType.FromByteArray(BA, Idx);
				//.
				if (OnDataHandler != null)
					OnDataHandler.DoOnData(DataType);
			}
		}
		return Idx;
	}
}
