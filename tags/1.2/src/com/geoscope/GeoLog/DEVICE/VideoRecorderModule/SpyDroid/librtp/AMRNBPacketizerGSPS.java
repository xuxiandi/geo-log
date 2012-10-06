/*
 * Copyright (C) 2011 GUIGUI Simon, fyhertz@gmail.com
 * 
 * This file is part of Spydroid (http://code.google.com/p/spydroid-ipcamera/)
 * 
 * Spydroid is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This source code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this source code; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.geoscope.GeoLog.DEVICE.VideoRecorderModule.SpyDroid.librtp;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;

/*
 *   RFC 3267
 *   
 *   AMR Streaming over RTP
 *   
 *   
 *   Must be fed with an InputStream containing raw amr nb
 *   Stream must begin with a 6 bytes long header: "#!AMR\n", it will be skipped
 *   
 */

public class AMRNBPacketizerGSPS extends AbstractPacketizerGSPS implements Runnable {
	
    @SuppressWarnings("unused")
	private static final int[] sBitrates = {4750, 5150, 5900, 6700, 7400, 7950, 1020, 1220};
    private static final int[] sFrameBits = {95, 103, 118, 134, 148, 159, 204, 244};

    private static final int AMR_HEADER_LENGTH = 6; // "#!AMR\n"
    private static final int AMR_FRAME_HEADER_LENGTH = 1; // Each frame has a short header
    
    static final public String LOG_TAG = "SPYDROID";

	private Thread thread;
    
	public AMRNBPacketizerGSPS(InputStream fis, boolean pflTransmitting, InetAddress dest, int port, int UserID, String UserPassword, int pidGeographServerObject, String OutputFileName) throws Exception {
		super(fis, 32768, pflTransmitting, dest,port, UserID,UserPassword, pidGeographServerObject, OutputFileName);
	}

	public void Destroy() throws Exception {
		stop(); //. terminate thread
		if (thread != null) {
			thread.join();
			thread = null;
		}
		//.
		super.Destroy();
	}
	    
	@Override
    public void start() {
        super.start();
        //.
		if (thread != null) {
			try {
				thread.join();
			} catch (InterruptedException E) {}
			thread = null;
		}
        thread = new Thread(this);
        thread.start();
    }

	public void run() {
		try {
		    int frameLength, frameType;
		    long ts = 0;
		    
		    // Skip raw amr header
		    fill(rtphl,AMR_HEADER_LENGTH);
		    
		    buffer[rtphl] = (byte) 0xF0;
		    
		    try {
		            while (running) {

		                    // First we read the frame header
		                    fill(rtphl+1,AMR_FRAME_HEADER_LENGTH);

		                    // Then we calculate the frame payload length
		                    frameType = (Math.abs(buffer[rtphl + 1]) >> 3) & 0x0f;
		                    frameLength = (sFrameBits[frameType]+7)/8;

		                    // And we read the payload
		                    fill(rtphl+2,frameLength);

		                    //Log.d(TAG,"Frame length: "+frameLength+" frameType: "+frameType);

		                    // RFC 3267 Page 14: 
		                    // "For AMR, the sampling frequency is 8 kHz, corresponding to
		                    // 160 encoded speech samples per frame from each channel."
		                    Output.updateTimestamp(ts); ts+=160;
		                    Output.markNextPacket();

		                    Output.send(rtphl+1+AMR_FRAME_HEADER_LENGTH+frameLength);
		            }
		    } 
		    finally {
		            running = false;
		    }
		}
		catch (Throwable TE) {
        	TDEVICEModule.Log_WriteCriticalError(TE);
		}		    
		//. Log.d(TAG,"Packetizer stopped !");		    
	}


	private int fill(int offset,int length) {
		    
		int sum = 0, len;
	    
		while (sum<length) {
			try { 
				len = is.read(buffer, offset+sum, length-sum);
				if (len<0) {
					//. Log.d(TAG,"End of stream");
					running = false;
				}
				else sum+=len;
			} catch (IOException e) {
				stop();
				return sum;
			}
		}
		return sum;
	}
}
