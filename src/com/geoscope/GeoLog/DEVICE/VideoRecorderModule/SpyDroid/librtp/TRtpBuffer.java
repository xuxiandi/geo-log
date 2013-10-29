package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;


public class TRtpBuffer {

    public static final int MTU = 1500;
    public static final int RTP_HEADER_LENGTH = 12;
    public static final int MAXPACKETSIZE = 1400;
    public static final int MAXDATASIZE = 1400-RTP_HEADER_LENGTH;
    
    public byte[] 		buffer = new byte[MTU];
    public int			buffer_length = 0;
    public int			buffer_offset = 0;
    protected int seq = 0;
    protected boolean upts = false;
    protected int ssrc;

    public TRtpBuffer() {
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
}
