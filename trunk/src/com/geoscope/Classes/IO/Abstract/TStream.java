package com.geoscope.Classes.IO.Abstract;

import java.io.IOException;

public abstract class TStream {

	public long Size = 0;
	public long Position = 0;
	
	public void Close() {
	}
	
	public void Clear() {
		Size = 0;
		Position = 0;
	}
	
	public abstract void 	Write(byte[] Buffer, int BufferSize) throws IOException;
	public abstract int 	Read(byte[] Buffer, int BufferSize) throws IOException;
}
