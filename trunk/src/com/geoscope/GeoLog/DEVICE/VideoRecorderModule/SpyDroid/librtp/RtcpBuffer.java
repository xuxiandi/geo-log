package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;

import java.io.IOException;
import java.io.OutputStream;

public class RtcpBuffer {

    public static final int Size = 28;
    public static final int MTU = 1500;

    private byte[] buffer = new byte[MTU];
    private int ssrc;
    private int octetCount = 0, packetCount = 0;

    public RtcpBuffer() {

            /*                                                           Version(2)  Padding(0)                                                                             */
            /*                                                                       ^                ^                     PT = 0                                                  */
            /*                                                                       |                |                             ^                                                               */
            /*                                                                       | --------                             |                                                               */
            /*                                                                       | |---------------------                                                               */
            /*                                                                       | ||                                                                                                   */
            /*                                                                       | ||                                                                                                   */
            buffer[0] = (byte) Integer.parseInt("10000000",2);

            /* Packet Type PT */
            buffer[1] = (byte) 200;

            /* Byte 2,3          ->  Length                              */
            setLong(28/4-1, 2, 4);

            /* Byte 4,5,6,7      ->  SSRC                            */
            /* Byte 8,9,10,11    ->  NTP timestamp hb                                */
            /* Byte 12,13,14,15  ->  NTP timestamp lb                                */
            /* Byte 16,17,18,19  ->  RTP timestamp                       */
            /* Byte 20,21,22,23  ->  packet count                                    */
            /* Byte 24,25,26,27  ->  octet count                             */
    }

    /** Sends the RTCP packet over the network. */
    public void SendTo(OutputStream output) throws IOException {
    	output.write(buffer,0,Size);
    }

    /** 
     * Updates the number of packets sent, and the total amount of data sent.
     * @param length The length of the packet 
     **/
    public void update(int length) {
            packetCount += 1;
            octetCount += length;
            setLong(packetCount, 20, 24);
            setLong(octetCount, 24, 28);
    }

    /** Sets the RTP timestamp of the sender report. */
    public void setRtpTimestamp(long ts) {
            setLong(ts, 16, 20);
    }

    /** Sets the NTP timestamp of the sender report. */
    public void setNtpTimestamp(long ts) {
            long hb = ts/1000;
            long lb = ( ( ts - hb*1000 ) * 4294967296L )/1000;
            setLong(hb, 8, 12);
            setLong(lb, 12, 16);
    }

    public void setSSRC(int ssrc) {
            this.ssrc = ssrc; 
            setLong(ssrc,4,8);
            packetCount = 0;
            octetCount = 0;
            setLong(packetCount, 20, 24);
            setLong(octetCount, 24, 28);
    }

    public int getSSRC() {
            return ssrc;
    }

    private void setLong(long n, int begin, int end) {
            for (end--; end >= begin; end--) {
                    buffer[end] = (byte) (n % 256);
                    n >>= 8;
            }
    }       
}
