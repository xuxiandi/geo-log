package com.geoscope.Classes.IO.Memory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.geoscope.Classes.IO.Abstract.TInOutStream;
import com.geoscope.Classes.MultiThreading.Synchronization.Event.TAutoResetEvent;

public final class TMemoryInOutStreamAdapter extends TInOutStream {

	public class TMyInputStream extends InputStream {

		@Override
		public int read(byte[] buffer, int offset, int length) throws IOException {
			return Read(buffer, offset, length);
		}

		@Override
		public int read(byte[] buffer) throws IOException {
			return read(buffer,0,buffer.length);
		}

		@Override
		public int read() throws IOException {
			return 0;
		}
	}
	
	public class TMyOutputStream extends OutputStream {

		@Override
		public void write(byte[] buffer, int offset, int count) throws IOException {
			Write(buffer, offset, count);
		}
		
		@Override
		public void write(byte[] buffer) throws IOException {
			write(buffer,0,buffer.length);
		}

		@Override
		public void write(int oneByte) throws IOException {
		}
	}
	
	private int Capacity;
	//.
	private byte[] Memory;
	//.
	private int Size;
	//. 
	private int Position;
	//.
	private TAutoResetEvent DataSignal = new TAutoResetEvent();
	
	public TMemoryInOutStreamAdapter(int pCapacity) {
		super();
		//.
		Capacity = pCapacity;
		Memory = new byte[Capacity];
		Size = 0;
		Position = 0;
		//.
		InStream = new TMyInputStream();
		OutStream = new TMyOutputStream();
	}

	public TMemoryInOutStreamAdapter() {
		this(8192);
	}
	
	public void Destroy() throws IOException {
		if (OutStream != null) {
			OutStream.close();
			OutStream = null;
		};
		if (InStream != null) {
			InStream.close();
			InStream = null;
		}
	}
	
	private void SetCapacity(int NewCapacity) {
		byte[] _Memory = new byte[NewCapacity];
		synchronized (this) {
			if (Size > 0) 
				System.arraycopy(Memory,0, _Memory,0, Size);
			Capacity = NewCapacity;
			Memory = _Memory;
		}
	}
	
	private void Write(byte[] Buffer, int BufferOffset, int BufferSize) {
		synchronized (this) {
			while ((Size+BufferSize) > Capacity)
				SetCapacity(Capacity << 1);
			//.
			System.arraycopy(Buffer,BufferOffset, Memory,Size, BufferSize);
			//.
			Size += BufferSize;
		}
		//. set data signal
		DataSignal.Set();
	}
	
	private int Read(byte[] Buffer, int BufferOffset, int BufferSize) throws IOException {
		int Delta;
		while (true) {
			synchronized (this) {
				Delta = (Size-Position);
			}
			//.
			if (Delta > 0)
				break; //. >
			//.
			try {
				DataSignal.WaitOne();
			} catch (InterruptedException IE) {
				return 0; //. ->
			}
		}
		//.
		if (BufferSize > Delta)
			BufferSize = Delta;
		//.
		synchronized (this) {
			System.arraycopy(Memory,Position, Buffer,BufferOffset, BufferSize);
			//.
			Position += BufferSize;
			//.
			if (Position == Size) {
				Size = 0;
				Position = 0;
			}
		}
		//.
		return BufferSize;
	}
}
