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
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;

import com.geoscope.Utils.TDataConverter;


abstract public class AbstractPacketizerGSPS {
	
	public static final short PACKET_TYPE_V1 = 1;
	public static final short PACKET_TYPE_V2 = 2; //. simple encryption
	
	protected static final int PreambulaSize = 16;   
	protected static final int rtphl = PreambulaSize+12; // Rtp header length
	
	private static short Buffer_GetCRC(byte[] buffer, int Offset, int Size) {
        int CRC = 0;
        int V;
        int Idx  = Offset;
        while (Idx < (Offset+Size))
        {
            V = (int)(buffer[Idx] & 0x000000FF);
            CRC = (((CRC+V) << 1)^V);
            //.
            Idx++;
        }
        return (short)CRC;
	}
	
	private static void Buffer_Encrypt(byte[] buffer, int Offset, int Size, String UserPassword) throws UnsupportedEncodingException {
        int StartIdx = Offset;
        byte[] UserPasswordArray;
        UserPasswordArray = UserPassword.getBytes("windows-1251");
        //.
        if (UserPasswordArray.length > 0)
        {
            int UserPasswordArrayIdx = 0;
            for (int I = StartIdx; I < (StartIdx+Size); I++)
            {
                buffer[I] = (byte)(buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
                UserPasswordArrayIdx++;
                if (UserPasswordArrayIdx >= UserPasswordArray.length) 
                	UserPasswordArrayIdx = 0;
            }
        }
	}
	
	public int UserID;
	public String UserPassword;
	public int idGeographServerObject = 0;
	//.
	public RtpSocketGSPS Output = null;
	protected InputStream is = null;
	protected boolean running = false;
	protected byte[] buffer;	
	
	public AbstractPacketizerGSPS(InputStream is, int buffer_size, boolean pflTransmitting, InetAddress dest, int port, int pUserID, String pUserPassword, int pidGeographServerObject, String OutputFileName) throws Exception {
		this.is = is;
		//.
		UserID = pUserID;
		UserPassword = pUserPassword;
		idGeographServerObject = pidGeographServerObject;
		//.
		buffer = new byte[buffer_size];
		//.
		if (pflTransmitting && (idGeographServerObject != 0))
			PreparePreambula();
		//.
		Output = new RtpSocketGSPS(buffer, PreambulaSize, pflTransmitting, dest,port, OutputFileName);
	}
	
	public void Destroy() throws Exception {
		stop(); //. terminate thread
		//.
		if (Output != null) {
			Output.close();
			Output = null;
		}
		buffer = null;
	}
	
	private void PreparePreambula() {
		try {
			byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(PACKET_TYPE_V2);
			System.arraycopy(BA,0, buffer,0, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(UserID);
			System.arraycopy(BA,0, buffer,2, BA.length);
			BA = TDataConverter.ConvertInt32ToBEByteArray(idGeographServerObject);
			System.arraycopy(BA,0, buffer,6, BA.length);
			short CRC = Buffer_GetCRC(buffer, 6,8);
			BA = TDataConverter.ConvertInt16ToBEByteArray(CRC);
			System.arraycopy(BA,0, buffer,14, BA.length);
			Buffer_Encrypt(buffer,6,10,UserPassword);
		}
		catch (Exception E) {};
	}
	
	public void startStreaming() throws IOException {
		start();
	}

	public void stopStreaming() {
		try {
			is.close();
		} catch (IOException e) {
			
		}
		stop();
	}
	
	public void StartTransmitting(int pidGeographServerObject) {
		idGeographServerObject = pidGeographServerObject;
		PreparePreambula();
		//.
		Output.SetTransmitting(true);
	}
	
	public void FinishTransmitting() {
		Output.SetTransmitting(false);
	}
	
    public void start() throws IOException {
        running = true;
    }

    public void stop() {
        running = false;
    }

    // Useful for debug
    protected String printBuffer(int start,int end) {
            String str = "";
            for (int i=start;i<end;i++) str+=","+Integer.toHexString(buffer[i]&0xFF);
            return str;
    }
	
}
