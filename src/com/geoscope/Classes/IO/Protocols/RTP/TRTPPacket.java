package com.geoscope.Classes.IO.Protocols.RTP;

import java.io.IOException;
import java.io.OutputStream;


public class TRTPPacket {

    public static final int MTU = 1500;
    public static final int RTP_HEADER_LENGTH = 12;
    public static final int MAXPACKETSIZE = 1400;
    public static final int MAXDATASIZE = 1400-RTP_HEADER_LENGTH;
    
    public byte[] 		buffer;
    public int			buffer_length = 0;
    public int			buffer_offset = 0;
    protected int seq = 0;
    protected boolean upts = false;
    protected int ssrc;

    public TRTPPacket(int pbuffer_offset) {
    	buffer_offset = pbuffer_offset;
    	//.
    	buffer = new byte[MTU];
    	//.
    	SupplyWithRTPHeader();
    }

    public TRTPPacket() {
    	this(0);
    }
    
    public TRTPPacket(byte[] pbuffer, int pbuffer_length, int pbuffer_offset) {
    	buffer = pbuffer;
    	//.
    	buffer_length = pbuffer_length;
    	buffer_offset = pbuffer_offset;
    }

    public TRTPPacket(byte[] pbuffer, int pbuffer_length) {
    	this(pbuffer,pbuffer_length,0);
    }
    
    protected void SupplyWithRTPHeader() {
        /*                                                           Version(2)  Padding(0)                                                                             */
        /*                                                                       ^                ^                     Extension(0)                                            */
        /*                                                                       |                |                             ^                                                               */
        /*                                                                       | --------                             |                                                               */
        /*                                                                       | |---------------------                                                               */
        /*                                                                       | ||  -----------------------> Source Identifier(0)    */
        /*                                                                       | ||  |                                                                                                */
        buffer[buffer_offset+0] = (byte) Integer.parseInt("10000000",2);

        /* Payload Type */
        buffer[buffer_offset+1] = (byte) 96;

        /* Byte 2,3        ->  Sequence Number                   */
        /* Byte 4,5,6,7    ->  Timestamp                         */
        /* Byte 8,9,10,11  ->  Sync Source Identifier            */
    }
    
    public void SetBuffer(byte[] pbuffer, int pbuffer_length, int pbuffer_offset) {
    	buffer = pbuffer;
    	//.
    	buffer_length = pbuffer_length;
    	buffer_offset = pbuffer_offset;
    }

    public void SetBuffer(byte[] pbuffer, int pbuffer_length) {
    	SetBuffer(pbuffer,pbuffer_length,0);
    }
    
    public void setSSRC(int ssrc) {
            this.ssrc = ssrc; 
            setLong(ssrc, buffer_offset+8, buffer_offset+12);
    }

    public int getSSRC() {
            return ssrc;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    /** Returns the buffer that you can directly modify before calling send. */
    public int getBufferLength() {
    	return buffer_length;
    }
    
    public void setBufferLength(int Value) {
    	buffer_length = Value;
    }
    
    /** Increments the sequence number. */
    protected void updateSequence() {
            setLong(++seq, buffer_offset+2, buffer_offset+4);
    }

    /** 
     * Overwrites the timestamp in the packet.
     * @param timestamp The new timestamp
     **/
    public void updateTimestamp(int timestamp) {
            setLong(timestamp, buffer_offset+4, buffer_offset+8);
    }
    
    public int getSequence() {
    	int Idx = buffer_offset+2;
		return (((buffer[Idx] & 0xFF) << 8)+(buffer[Idx+1] & 0xFF));
    }

    public int getTimestamp() {
    	int Idx = buffer_offset+4;
    	return ((buffer[Idx+0] << 24)+((buffer[Idx+1] & 0xFF) << 16)+((buffer[Idx+2] & 0xFF) << 8)+(buffer[Idx+3] & 0xFF));
    }

    public void markNextPacket() {
        upts = true;
        buffer[buffer_offset+1] += 0x80; // Mark next packet
    }

    private void setLong(long n, int begin, int end) {
        for (end--; end >= begin; end--) {
            buffer[end] = (byte) (n % 256);
            n >>= 8;
        }
    }
    
	public void SendToStream(OutputStream OS) throws IOException {
		updateSequence();
		//.
		OS.write(buffer, 0,buffer_length);
		//.
		if (upts) {
			upts = false;
			buffer[buffer_offset+1] -= 0x80;
		}
	}
	
	public void SendToStreamAgain(OutputStream OS) throws IOException {
		OS.write(buffer, 0,buffer_length);
	}
}
