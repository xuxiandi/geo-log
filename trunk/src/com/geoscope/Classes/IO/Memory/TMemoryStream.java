package com.geoscope.Classes.IO.Memory;

import java.io.IOException;

import com.geoscope.Classes.IO.Abstract.TStream;

public class TMemoryStream extends TStream {

	private byte[] Memory;
	
	public TMemoryStream(int InitialCapacity) {
		Memory = new byte[InitialCapacity]; 
	}
		
	@Override
	public void Write(byte[] Buffer, int BufferSize) throws IOException {
		long NewPosition = Position+BufferSize;
		while (true) {
			long Capacity = GetCapacity();
			if (NewPosition > Capacity)
				SetCapacity(Capacity << 1);
			else
				break; //. >
		}
		//.
		System.arraycopy(Buffer,0, Memory,(int)Position, (int)BufferSize);
		//.
		Position = NewPosition;
		if (Position > Size)
			Size = Position;
	}

	@Override
	public int Read(byte[] Buffer, int BufferSize) throws IOException {
		if (BufferSize > 0) {
			long NewPosition = Position+BufferSize;
			if (NewPosition > Size)
				throw new IOException("read beyond the end of stream"); //. =>
			//.
			System.arraycopy(Memory,(int)Position, Buffer,0, BufferSize);
			//.
			Position = NewPosition;
		}
		return BufferSize;
	}
	
	public long GetCapacity() {
		return Memory.length;
	}

	public void SetCapacity(long NewCapacity) throws IOException {
		if (NewCapacity < Memory.length)
			throw new IOException("a new capacity is less than existing"); //. =>
		byte[] _Memory = new byte[(int)NewCapacity];
		if (Size > 0) 
			System.arraycopy(Memory,0, _Memory,0, (int)Size);
		Memory = _Memory;
	}
}
