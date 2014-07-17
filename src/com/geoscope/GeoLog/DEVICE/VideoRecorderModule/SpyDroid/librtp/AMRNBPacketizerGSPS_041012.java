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

import android.util.Log;

import com.geoscope.GeoLog.Application.TGeoLogApplication;

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

public class AMRNBPacketizerGSPS_041012 extends AbstractPacketizerGSPS {
	
    static final public String LOG_TAG = "SPYDROID";
    
	private long ts = 0;
	
	private final int amrhl = 6; // Header length
	private final int amrps = 32;   // Packet size
	
	public AMRNBPacketizerGSPS_041012(InputStream fis, boolean pflTransmitting, InetAddress dest, int port, int UserID, String UserPassword, int pidGeographServerObject, String OutputFileName) throws Exception {
		super(fis, 32768, pflTransmitting, dest,port, UserID,UserPassword, pidGeographServerObject, OutputFileName);
	}

	public void run() {
		try {
			// Skip raw amr header
			fill(rtphl,amrhl);
			
			buffer[rtphl] = (byte) 0xF0;
			Output.markAllPackets();
			
			while (running) {
				
				fill(rtphl+1,amrps);
				
				// RFC 3267 Page 14: 
				// "For AMR, the sampling frequency is 8 kHz, corresponding to
				// 160 encoded speech samples per frame from each channel."
				///. PAV Output.updateTimestamp(SystemClock.elapsedRealtime()-PacketTimeBase.TimeBase);
				Output.updateTimestamp(ts); ts+=160;
				
				Output.send(rtphl+amrps+1);
				
			}
		}
		catch (Throwable TE) {
        	TGeoLogApplication.Log_WriteCriticalError(TE);
		}
	}

	
	private int fill(int offset,int length) {
		
		int sum = 0, len;
		
		while (sum<length) {
			try { 
				len = is.read(buffer, offset+sum, length-sum);
				if (len<0) {
					Log.e(LOG_TAG,"Read error");
				}
				else sum+=len;
			} catch (IOException e) {
				stopStreaming();
				return sum;
			}
		}
		
		return sum;
			
	}
	
	
}
