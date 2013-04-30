package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;

import java.io.IOException;
import java.io.OutputStream;

public class RtpBuffer {

    public static final int MTU = 1500;
    public static final int RTP_HEADER_LENGTH = 12;
    public static final int MAXPACKETSIZE = 1400;
    
    private byte[] buffer = new byte[MTU];
    private int seq = 0;
    private boolean upts = false;
    private int ssrc;

    public RtpBuffer() {

            /*                                                           Version(2)  Padding(0)                                                                             */
            /*                                                                       ^                ^                     Extension(0)                                            */
            /*                                                                       |                |                             ^                                                               */
            /*                                                                       | --------                             |                                                               */
            /*                                                                       | |---------------------                                                               */
            /*                                                                       | ||  -----------------------> Source Identifier(0)    */
            /*                                                                       | ||  |                                                                                                */
            buffer[0] = (byte) Integer.parseInt("10000000",2);

            /* Payload Type */
            buffer[1] = (byte) 96;

            /* Byte 2,3        ->  Sequence Number                   */
            /* Byte 4,5,6,7    ->  Timestamp                         */
            /* Byte 8,9,10,11  ->  Sync Source Identifier            */
    }

    public void setSSRC(int ssrc) {
            this.ssrc = ssrc; 
            setLong(ssrc,8,12);
    }

    public int getSSRC() {
            return ssrc;
    }

    /** Returns the buffer that you can directly modify before calling send. */
    public byte[] getBuffer() {
            return buffer;
    }

    /** Sends the RTP packet over the network. */
    public void SendTo(OutputStream output, int length) throws IOException {
            updateSequence();
            //.
            output.write(buffer, 0,length);
            //.
            if (upts) {
                    upts = false;
                    buffer[1] -= 0x80;
            }
    }

    /** Increments the sequence number. */
    private void updateSequence() {
            setLong(++seq, 2, 4);
    }

    /** 
     * Overwrites the timestamp in the packet.
     * @param timestamp The new timestamp
     **/
    public void updateTimestamp(long timestamp) {
            setLong(timestamp, 4, 8);
    }

    public void markNextPacket() {
            upts = true;
            buffer[1] += 0x80; // Mark next packet
    }

    private void setLong(long n, int begin, int end) {
            for (end--; end >= begin; end--) {
                    buffer[end] = (byte) (n % 256);
                    n >>= 8;
            }
    }       
}
