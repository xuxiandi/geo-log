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
import java.util.LinkedList;
import java.util.concurrent.Semaphore;

import android.os.SystemClock;

import com.geoscope.GeoLog.Application.TGeoLogApplication;

/*
 *   RFC 3984
 *   
 *   H264 Streaming over RTP
 *   
 *   Must be fed with an InputStream containing raw h.264
 *   NAL units must be preceded by their length (4 bytes)
 *   Stream must start with mpeg4 or 3gpp header, it will be skipped
 *   
 */

public class H264PacketizerGSPS_041012 extends AbstractPacketizerGSPS {
	
    private final int MAXPACKETSIZE = AbstractPacketizerGSPS.PreambulaSize+1400;
    static final public String TAG = "H264Packetizer2GSPS";
    
    private long oldtime, duration, delay = 10, newDelay, ts;
    private int available = 0, oldavailable = 0, size, naluLength = 0, cursor = 0;
    private SimpleFifo fifo;
    private Semaphore nbChunks = new Semaphore(0);
    private LinkedList<Chunk> chunks = new LinkedList<Chunk>();
    private Chunk chunk = null, tmpChunk = null;
    boolean splitNal = false;
	
    private class Chunk {
        public Chunk(int size,long duration) {
                this.size= size;
                this.duration = duration;
                
        }
        public int size;
        public long duration;
    }

	public H264PacketizerGSPS_041012(InputStream fis, boolean pflTransmitting, InetAddress dest, int port, int UserID, String UserPassword, int pidGeographServerObject, String OutputFileName) throws Exception {
		super(fis, 65536, pflTransmitting, dest,port, UserID,UserPassword, pidGeographServerObject, OutputFileName);
		//.
		fifo = null;
		fifo = new SimpleFifo(500000);
	}
	
	public void Destroy() throws Exception {
		super.Destroy();
		//.
		if (fifo != null) {
			fifo.Destroy();
			fifo = null;
		}
	}
	
	public void run() {
		try {
	        try {
                // This will skip the MPEG4 header
                skipHeader();
	        }
	        catch (IOException e)  {
                return;
	        }

	        /*
	         * This first thread waits for new NAL units and send them
	         */
	        Thread SendingThread = new Thread(new Runnable() {
                public void run() {
                	try {
                        int len = 0; 
                        chunks.add(new Chunk(0,0));
                        
                        while (true) {

                                try {
                                        nbChunks.acquire(1);
                                } catch (InterruptedException e1) {
                                        e1.printStackTrace();
                                }
                                
                                if (!running)
                                	break; //. >
                                
                                if (splitNal) {
                                        
                                        //. Log.e(TAG,"nal unit cut: cursor: "+cursor+" naluLength: "+naluLength+" len: "+(naluLength-(chunk.size-cursor)));
                                        
                                        len = naluLength-(cursor-chunk.size);
                                        
                                        tmpChunk = chunks.get(1);
                                        tmpChunk.duration += chunk.duration*len/chunk.size;
                                        tmpChunk.size += len;
                                        
                                        splitNal = false;
                                        
                                }
                                
                                chunks.removeFirst(); cursor = 0;
                                
                                chunk = chunks.getFirst();
                                while (cursor<chunk.size) send();

                        }
            		}
            		catch (Throwable TE) {
            			TGeoLogApplication.Log_WriteCriticalError(TE);
            		}
                }
	        });
	        SendingThread.start();
	        try {
	        	/*
	        	 * This thread waits for the camera to deliver data and 
	        	 * queue work for the first thread
	        	 */
	        	while (running) { 
                    
                    try {
                            
                            oldtime = SystemClock.elapsedRealtime();
                            ts = oldtime;
                            oldavailable = available = is.available();
                            while (available<=oldavailable) {
                                    Thread.sleep(10);
                                    available = is.available();
                            }                                       
                            
                            duration = SystemClock.elapsedRealtime() - oldtime;
                            size = fillFifo(available);
                            //. Log.e(TAG,"New chunk -> delay: "+duration+" s1: "+size+" s2: "+available+" available: "+fifo.available()+" chunks: "+chunks.size());
                            chunks.add(new Chunk(size,duration));
                            nbChunks.release();

                    } catch (InterruptedException e1) {
                            e1.printStackTrace();
                    } catch (IOException e) {
                            e.printStackTrace();
                    }
                    
	        	}
	        }
	        finally {
	        	running = false;
	        	nbChunks.release();
	        	try {
	        		SendingThread.join();
	        	} catch (InterruptedException e) {
	        		e.printStackTrace();
	        	}
	        }
		}
		catch (Throwable TE) {
			TGeoLogApplication.Log_WriteCriticalError(TE);
		}
	}

	/**
	 * Reads a NAL unit in the FIFO and sends it
	 * If it is too big, we split it in FU-A units (RFC 3984)
	 */
	private void send() {

        int sum = 1, len = 0;
        
        // Read nal unit length (4 bytes) and nal unit header (1 byte)
        fifo.read(buffer, rtphl, 5);
        naluLength = (buffer[rtphl+3]&0xFF) + (buffer[rtphl+2]&0xFF)*256 + (buffer[rtphl+1]&0xFF)*65536;

        //. Log.d(TAG,"- NALU Length: " + naluLength);
        
        cursor += naluLength+4;
        
        if (cursor<=chunk.size) {
                newDelay = chunk.duration*naluLength/chunk.size;                        
        }
        else {
                splitNal = true;
                return;
        }
        
        delay = (newDelay>100) ? delay:newDelay;
        ts += delay;
        
        ///. Log.d(TAG,"- Nal unit length: " + naluLength+" cursor: "+cursor+" delay: "+delay+" ts: "+ts);

        try {
                Thread.sleep(5*delay/6);
        } catch (InterruptedException e) {
                return;
        }
        
        Output.updateTimestamp(ts*90);

        // Small nal unit => Single nal unit 
        if (naluLength<=MAXPACKETSIZE-rtphl-2) {

                buffer[rtphl] = buffer[rtphl+4];
                len = fifo.read(buffer, rtphl+1,  naluLength-1  );
                Output.markNextPacket();
                Output.send(naluLength+rtphl);

                //Log.e(TAG,"----- Single NAL unit read:"+len+" header:"+printBuffer(rtphl,rtphl+3));

        }

        // Large nal unit => Split nal unit 
        else {

                /* Set FU-A indicator */
                buffer[rtphl] = 28;
                buffer[rtphl] += (buffer[rtphl+4] & 0x60) & 0xFF; // FU indicator NRI
                //buffer[rtphl] += 0x80;

                /* Set FU-A header */
                buffer[rtphl+1] = (byte) (buffer[rtphl+4] & 0x1F);  // FU header type
                buffer[rtphl+1] += 0x80; // Start bit


                while (sum < naluLength) {

                        if (!running) break;
                        len = fifo.read(buffer, rtphl+2,  naluLength-sum > MAXPACKETSIZE-rtphl-2 ? MAXPACKETSIZE-rtphl-2 : naluLength-sum  ); sum += len;
                        if (len<0) break;

                        /* Last packet before next NAL */
                        if (sum >= naluLength) {
                                // End bit on
                                buffer[rtphl+1] += 0x40;
                                Output.markNextPacket();
                        }

                        Output.send(len+rtphl+2);

                        /* Switch start bit */
                        buffer[rtphl+1] = (byte) (buffer[rtphl+1] & 0x7F); 

                        //Log.d(TAG,"--- FU-A unit, end:"+(boolean)(sum>=naluLength));

                }


        }
        
	}

	/**
	 *  Writes <i>length</i> bytes from the InputStream in the FIFO
	 */
	private int fillFifo(int length) {
        
        int sum = 0, len = 0;
        
        try {
                while (sum<length) {
                        len = fifo.write(is, length-sum);
                        sum += len;
                        if (len==-1) return -1;
                }
        } catch (IOException e) {
                return -1;
        }
        
        return sum;
        
	}

	/**
	 * The InputStream may start with a header (moov atom or maybe some other atoms) that we need to skip
	 */
	private void skipHeader() throws IOException {

        int len = 0;
        
        // Skip all atoms preceding mdat atom
        while (true) {
                is.read(buffer,rtphl,8);
                if (buffer[rtphl+4] == 'm' && buffer[rtphl+5] == 'd' && buffer[rtphl+6] == 'a' && buffer[rtphl+7] == 't') break;
                len = (buffer[rtphl+3]&0xFF) + (buffer[rtphl+2]&0xFF)*256 + (buffer[rtphl+1]&0xFF)*65536;
                if (len<=7) break;
                //Log.e(TAG,"Atom skipped: "+printBuffer(rtphl+4,rtphl+8)+" size: "+len);
                is.read(buffer,rtphl,len-8);
        }
        
        // Some phones do not set length correctly when stream is not seekable, still we need to skip the header
        if (len<=0 || len>1000) {
                while (true) {
                        while (is.read() != 'm');
                        is.read(buffer,rtphl,3);
                        if (buffer[rtphl] == 'd' && buffer[rtphl+1] == 'a' && buffer[rtphl+2] == 't') break;
                }
        }
        len = 0;
        
	}

    /********************************************************************************/
    /******** Simple fifo that will contain the NAL untis waiting to be sent ********/
    /********************************************************************************/
    public class SimpleFifo {

            @SuppressWarnings("unused")
			private final static String TAG = "SimpleFifo";
            
            private int length = 0, tail = 0, head = 0;
            private byte[] buffer;
            private Object mutex = new Object();
            
            public SimpleFifo(int length) {
                    this.length = length;
                    buffer = new byte[length];
            }
            
            public void Destroy() {
            	buffer = null;
            }
            
            public void write(byte[] buffer, int offset, int length) {
                    
                    synchronized (mutex) {
                    
                            if (tail+length<this.length) {
                                    System.arraycopy(buffer, offset, this.buffer, tail, length);
                                    tail += length;
                            }
                            else {
                                    int u = this.length-tail;
                                    System.arraycopy(buffer, offset, this.buffer, tail, u);
                                    System.arraycopy(buffer, offset+u, this.buffer, 0, length-u);
                                    tail = length-u;
                            }

                    }
                    
            }

            public int write(InputStream fis, int length) throws IOException {
                    
                    int len = 0;
                    
                    synchronized (mutex) {
                    
                            if (tail+length<this.length) {
                                    if ((len = fis.read(buffer,tail,length)) == -1) return -1;
                                    tail += len;
                            }
                            else {
                                    int u = this.length-tail;
                                    if ((len = fis.read(buffer,tail,u)) == -1) return -1;
                                    if (len<u) {
                                            tail += len;
                                    } else {
                                            if ((len = fis.read(buffer,0,length-u)) == -1) return -1;
                                            tail = len;
                                            len = length;
                                    }
                            }

                    }
                    
                    return len;
                    
            }
            
            public int read(byte[] buffer, int offset, int length) {
                    
                    synchronized (mutex) {

                            length = length>available() ? available() : length;

                            if (head+length<this.length) {
                                    System.arraycopy(this.buffer, head, buffer, offset, length);
                                    head += length;
                            }
                            else {
                                    int u = this.length-head;
                                    System.arraycopy(this.buffer, head, buffer, offset, u);
                                    System.arraycopy(this.buffer, 0, buffer, offset+u, length-u);
                                    head = length-u;
                            }

                    }

                    return length;
            }

            public int available() {
                    int av = 0;
                    synchronized (mutex) {
                            av = (tail>=head) ? tail-head : this.length-(head-tail);
                    }
                    return av; 
            }
            
    }
        
}