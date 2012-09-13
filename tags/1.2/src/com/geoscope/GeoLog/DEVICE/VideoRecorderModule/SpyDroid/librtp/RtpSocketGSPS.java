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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

import android.util.Log;



public class RtpSocketGSPS {

    static final public String LOG_TAG = "SPYDROID";
    
	public static final int headerLength = 12;
    
	private boolean 		flTransmitting;
	private DatagramSocket 	usock;
	private DatagramPacket 	upack;
	
	private byte[] buffer;
	private int PreambulaSize;
	private int seq = 0;
	private boolean upts = false;
	private FileOutputStream 	OutputFileStream;
	private int					OutputFileStreamPackets;
	private int 	DataLength;
	private byte[] 	DataDescriptor = new byte[4];
		
	public RtpSocketGSPS(byte[] buffer, int pPreambulaSize, boolean pflTransmitting, InetAddress dest, int dport, String OutputFileName) throws SocketException {
		
		this.buffer = buffer;
		//.
		PreambulaSize = pPreambulaSize;
		flTransmitting = pflTransmitting; 
		
		/*							     Version(2)  Padding(0)					 					*/
		/*									 ^		  ^			Extension(0)						*/
		/*									 |		  |				^								*/
		/*									 | --------				|								*/
		/*									 | |---------------------								*/
		/*									 | ||  -----------------------> Source Identifier(0)	*/
		/*									 | ||  |												*/
		buffer[PreambulaSize+0] = (byte) Integer.parseInt("10000000",2);
		
		/* Payload Type */
		buffer[PreambulaSize+1] = (byte) 96;
		
		/* Byte 2,3        ->  Sequence Number                   */
		/* Byte 4,5,6,7    ->  Timestamp                         */
		
		/* Byte 8,9,10,11  ->  Sync Source Identifier            */
		setLong((new Random()).nextLong(),8,12);
		
		usock = new DatagramSocket();
		upack = new DatagramPacket(buffer,1,dest,dport);
		
		OutputFileStream = null;
		OutputFileStreamPackets = 0;
		if (OutputFileName != null)
			try {
				OutputFileStream = new FileOutputStream(OutputFileName);
			} catch (FileNotFoundException e) {}
	}

	public void close() throws IOException {
		if (OutputFileStream != null) {
			OutputFileStream.close();
		}
		//.
		usock.close();
	}
	
	public void SetTransmitting(boolean pflTransmitting) {
		flTransmitting = pflTransmitting;
	}
	
	public boolean IsTransmitting() {
		return flTransmitting;
	}
	
	/* Send RTP packet over the network */
	public void send(int length) {
		
		updateSequence();
		
		try {
			if (flTransmitting) {
				upack.setLength(length);
				usock.send(upack);
			}
			//.
			if (OutputFileStream != null) {
				DataLength = length-PreambulaSize;
				synchronized (OutputFileStream) {
					//. write data descriptor
					DataDescriptor[0] = (byte)(DataLength & 0xff);
					DataDescriptor[1] = (byte)(DataLength >> 8 & 0xff);
					DataDescriptor[2] = (byte)(DataLength >> 16 & 0xff);
					DataDescriptor[3] = (byte)(DataLength >>> 24);
					OutputFileStream.write(DataDescriptor);
					//. write data
					OutputFileStream.write(buffer, PreambulaSize,DataLength);
					OutputFileStreamPackets++;
				}
			}
		} catch (IOException e) {
			Log.e(LOG_TAG,"Send failed");
		}
		
		if (upts) {
			upts = false;
			buffer[PreambulaSize+1] -= 0x80;
		}
		
	}
	
	public int GetOutputFilePackets() throws IOException {
		if (OutputFileStream == null)
			return 0; //. ->
		synchronized (OutputFileStream) {
			OutputFileStream.flush();
			return OutputFileStreamPackets;
		}
	}
	
	private void updateSequence() {
		setLong(++seq, 2, 4);
	}
	
	public void updateTimestamp(long timestamp) {
		setLong(timestamp, 4, 8);
	}
	
	public void markNextPacket() {
		upts = true;
		buffer[PreambulaSize+1] += 0x80; // Mark next packet
	}
	
	public boolean isMarked() {
		return upts;
	}
	
	// Call this only one time !
	public void markAllPackets() {
		buffer[PreambulaSize+1] += 0x80;
	}
	
	private void setLong(long n, int begin, int end) {
		for (end--; end >= begin; end--) {
			buffer[PreambulaSize+end] = (byte) (n % 256);
			n >>= 8;
		}
	}	
	
}
