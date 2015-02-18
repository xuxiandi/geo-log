package com.geoscope.Classes.IO.Memory.Buffering;

import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TMemoryBuffering {
	
	public static class TBuffer {
		
		public long 	Timestamp = 0;
		public byte[] 	Data = new byte[0];
		public int		Size = 0;
	}

	public static class TOnBufferDequeueHandler {
		
		public void DoOnBufferDequeue(TBuffer Buffer) { 
			//. do synchronized(Buffer) when accessing the Buffer
		}
	}

	
	private class TBuffersDequeueing extends TCancelableThread {
		
    	private TAutoResetEvent DequeueSignal;
    	
    	public TBuffersDequeueing() {
    		DequeueSignal = new TAutoResetEvent();
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}
    	
		public void Process() {
			DequeueSignal.Set();
		}

		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					DequeueSignal.WaitOne();
					//.
					DequeueBuffers(Canceller);
				}
			}
			catch (InterruptedException E) {
			}
			catch (CancelException CE) {
			}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	}
	
	private int 		BuffersCount;
	private TBuffer[]	Buffers;
	//.
	private int WritePos = 0;
	private int ReadPos = 0;
	//.
	private TBuffersDequeueing BuffersDequeueing;
	//.
	private TOnBufferDequeueHandler OnBufferDequeueHandler;
		
	public TMemoryBuffering(int pBuffersCount, TOnBufferDequeueHandler pOnBufferDequeueHandler) {
		BuffersCount = pBuffersCount;
		OnBufferDequeueHandler = pOnBufferDequeueHandler;
		//.
		Buffers = new TBuffer[BuffersCount];
		for (int I = 0; I < BuffersCount; I++)
			Buffers[I] = new TBuffer();
		//.
		BuffersDequeueing = new TBuffersDequeueing();
	}
	
	public void Destroy() throws Exception {
		if (BuffersDequeueing != null) {
			BuffersDequeueing.Destroy();
			BuffersDequeueing = null;
		}
	}
	
	public void EnqueueBuffer(byte[] buffer, int size, long timestamp) {
		TBuffer Buffer;
		synchronized (this) {
			Buffer = Buffers[WritePos];
			//.
			WritePos++;
			if (WritePos >= BuffersCount)
				WritePos = 0;
			//.
			if (WritePos == ReadPos) {
				ReadPos++;
				if (ReadPos >= BuffersCount)
					ReadPos = 0;
			}
		}
		synchronized (Buffer) {
			Buffer.Timestamp = timestamp;
			if (Buffer.Data.length < size)
				Buffer.Data = new byte[size];
			System.arraycopy(buffer,0, Buffer.Data,0, size);
			Buffer.Size = size;
		}
		//.
		BuffersDequeueing.Process();
	}
	
	private void DequeueBuffers(TCanceller Canceller) throws CancelException {
		TBuffer Buffer;
		while (true) {
			synchronized (this) {
				if (WritePos == ReadPos) 
					return; //. ->
				//.
				Buffer = Buffers[ReadPos];
				//.
				ReadPos++;
				if (ReadPos >= BuffersCount)
					ReadPos = 0;
			}
			OnBufferDequeueHandler.DoOnBufferDequeue(Buffer);
			//.
			Canceller.Check();
		}
	}
}
