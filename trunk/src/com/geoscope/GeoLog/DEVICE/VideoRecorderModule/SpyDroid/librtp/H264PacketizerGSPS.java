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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import android.os.SystemClock;

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

public class H264PacketizerGSPS extends AbstractPacketizerGSPS {
	
    private final static int MAXPACKETSIZE = AbstractPacketizerGSPS.PreambulaSize+1400;
    static final public String TAG = "H264Packetizer2GSPS";
    
    private static SimpleFifo fifo = null;
    private ConcurrentLinkedQueue<Chunk> chunks;
    private Semaphore sync;
    private Producer producer;
    private Consumer consumer;
	
    private static class Chunk {
        public Chunk(int size,long duration) {
                this.size= size;
                this.duration = duration;
                
        }
        public int size;
        public long duration;
    }

	public H264PacketizerGSPS(InputStream fis, boolean pflTransmitting, InetAddress dest, int port, int UserID, String UserPassword, int pidGeographServerObject, String OutputFileName) throws Exception {
		super(fis, 65536, pflTransmitting, dest,port, UserID,UserPassword, pidGeographServerObject, OutputFileName);
		//.
		if (fifo == null)
			fifo = new SimpleFifo(500000);
	}
	
	public void Destroy() throws Exception {
		super.Destroy();
	}
	
	@Override
    public void start() {
        // We reinitialize everything so that the packetizer can be reused
        sync = new Semaphore(0);
        chunks = new ConcurrentLinkedQueue<Chunk>();
        chunks.add(new Chunk(0,0));
        fifo.flush();
        // We start the two threads of the packetizer
        long[] sleep = new long[1];
        producer = new Producer(is, fifo, chunks, sync, sleep);
        consumer = new Consumer(this,fifo, chunks, sync, sleep);
        //.
        super.start();
    }

	@Override
    public void stop() {
		try {
            is.close();
		} catch (IOException e1) {}
		//.
		producer.running = false;
		try {
            producer.join();
		} catch (InterruptedException e) {}
		//.
		consumer.running = false;
		consumer.interrupt();
		try {
            consumer.join();
		} catch (InterruptedException e) {}
		//.
		super.stop();
    }

    /*************************************************************************************/
    /*** This thread waits for the camera to deliver data and queue work for the other ***/
    /*************************************************************************************/
    private static class Producer extends Thread implements Runnable {
    	
    	public boolean running = true;
        private final SimpleFifo fifo;
        private final Semaphore sync;
        private final ConcurrentLinkedQueue<Chunk> chunks;
        private final InputStream is;
        private final long[] sleep;
        
        public Producer(InputStream is, SimpleFifo fifo, ConcurrentLinkedQueue<Chunk> chunks, Semaphore sync, long[] sleep) {
                this.fifo = fifo;
                this.chunks = chunks;
                this.sync = sync;
                this.is = is;
                this.sleep = sleep;
                this.start();
        }
        
        public void run() {
                int sum;
                long oldtime, duration;
                
                // This will skip the MPEG4 header if this step fails we can't stream anything :(
                try {
                        byte buffer[] = new byte[4];
                        // Skip all atoms preceding mdat atom
                        while (true) {
                                while (is.read() != 'm');
                                is.read(buffer,0,3);
                                if (buffer[0] == 'd' && buffer[1] == 'a' && buffer[2] == 't') break;
                        }
                } catch (IOException e) {
                        //. Log.e(TAG,"Couldn't skip mp4 header :/");
                        return;
                }
                
                try {
                        while (running) {
                                
                                // We try to read as much as we can from the camera buffer and measure how long it takes
                                // In a better world we could just wait until an entire nal unit is received and directly send it and one thread would be enough !
                                // But some cameras have this annoying habit of delivering more than one NAL unit at once: 
                                // for example if 10 NAL units are delivered every 10 sec we have to guess the duration of each nal unit
                                // And BECAUSE InputStream.read blocks, another thread is necessary to send the stuff :/
                                oldtime = SystemClock.elapsedRealtime(); sum = 0;
                                try {
                                        Thread.sleep(2*sleep[0]/3);
                                } catch (InterruptedException e) {
                                        break;
                                }
                                sum = 0;
                                while (sum < 5) {
                                	sum += fifo.write(is,100000);
                                }
                                duration = SystemClock.elapsedRealtime() - oldtime;
                                
                                //Log.d(TAG,"New chunk -> sleep: "+sleep[0]+" duration: "+duration+" sum: "+sum+" length: "+length+" chunks: "+chunks.size());
                                chunks.offer(new Chunk(sum,duration));
                                sync.release();
                        }
                } catch (IOException ignore) {
                } finally {
                        running = false;
                }
        }
    }

    /*************************************************************************************/
    /***************** This thread waits for new NAL units and send them *****************/
    /*************************************************************************************/
    private static class Consumer extends Thread implements Runnable {

        public boolean running = true;
        private final RtpSocketGSPS Output;        
        private final SimpleFifo fifo;
        private final Semaphore sync;
        private final ConcurrentLinkedQueue<Chunk> chunks;
        private final byte[] buffer;
        private boolean splitNal;
        private long newDelay, ts, delay = 10;
        private int cursor, naluLength = 0;
        private Chunk chunk = null, tmpChunk = null;
        private final long[] sleep;
        
        public Consumer(H264PacketizerGSPS Packetizer, SimpleFifo fifo, ConcurrentLinkedQueue<Chunk> chunks, Semaphore sync, long[] sleep) {
        		this.Output = Packetizer.Output;
                this.fifo = fifo;
                this.chunks = chunks;
                this.buffer = Packetizer.buffer;
                this.sync = sync;
                this.sleep = sleep;
                this.start();
        }
        
        public void run() {
                int len = 0; 
                
                try {
                        while (running) {

                                sync.acquire(1);

                                // This may happen if a chunk contains only a part of a NAL unit
                                if (splitNal) {
                                        len = naluLength-(cursor-chunk.size);
                                        tmpChunk = chunk;
                                        chunk = chunks.poll();
                                        chunk.duration += (tmpChunk.size>naluLength) ? tmpChunk.duration*len/tmpChunk.size : tmpChunk.duration;
                                        chunk.size += len;
                                        //Log.d(TAG,"Nal unit cut: duration: "+chunk.duration+" size: "+chunk.size+" contrib: "+chunk.duration*len/chunk.size+" naluLength: "+naluLength+" cursor: "+cursor+" len: "+len);
                                } else {
                                        len = chunk.size-cursor;
                                        chunk = chunks.poll();
                                        chunk.size += len;
                                }
                                cursor = 0;
                                //Log.d(TAG,"Sending chunk: "+chunk.size);
                                while (chunk.size-cursor>3) send();
                        }
                } catch (InterruptedException e) {
                        e.printStackTrace();
                } catch (IOException e) {
                        e.printStackTrace();
        		} catch (NullPointerException ignore) {
        			// May happen if the thread is interrupted and chunks is empty...
        			// It's not a problem
        		}
    
        		//. Log.d(TAG,"H264 packetizer stopped !");
        }
        
        // Reads a NAL unit in the FIFO and sends it
        // If it is too big, we split it in FU-A units (RFC 3984)
        private void send() throws IOException {
            int sum = 1, len = 0;

            // Read NAL unit length (4 bytes)
            if (!splitNal) {
                    fifo.read(buffer, rtphl, 4);
                    naluLength = buffer[rtphl+3]&0xFF | (buffer[rtphl+2]&0xFF)<<8 | (buffer[rtphl+1]&0xFF)<<16 | (buffer[rtphl]&0xFF)<<24;
            } else {
                    splitNal = false;
            }
            cursor += naluLength+4;

            if (cursor<=chunk.size) {
                    newDelay = chunk.duration*naluLength/chunk.size;
            }
            // This may happen if a chunk contains only a part of a NAL unit
            else {
                    splitNal = true;
                    return;
            }

            // Read NAL unit header (1 byte)
            fifo.read(buffer, rtphl, 1);
            // NAL unit type
            //. type = buffer[rtphl]&0x1F;
            
            delay = ( newDelay>100 || newDelay<5 ) ? delay:newDelay;
            ts += delay;
            sleep[0] = delay;
            /*try {
                    Thread.sleep(3*delay/6);
            } catch (InterruptedException e) {
                    return;
            }*/
            Output.updateTimestamp(ts*90);

            //Log.d(TAG,"- Nal unit length: " + naluLength+" cursor: "+cursor+" delay: "+delay+" type: "+type+" newDelay: "+newDelay);

            // Small NAL unit => Single NAL unit 
            if (naluLength<=MAXPACKETSIZE-rtphl-2) {
                    len = fifo.read(buffer, rtphl+1,  naluLength-1  );
                    Output.markNextPacket();
                    Output.send(naluLength+rtphl);
                    //Log.d(TAG,"----- Single NAL unit - len:"+len+" header:"+printBuffer(rtphl,rtphl+3)+" delay: "+delay+" newDelay: "+newDelay);
            }
            // Large NAL unit => Split nal unit 
            else {

                    // Set FU-A header
                    buffer[rtphl+1] = (byte) (buffer[rtphl] & 0x1F);  // FU header type
                    buffer[rtphl+1] += 0x80; // Start bit
                    // Set FU-A indicator
                    buffer[rtphl] = (byte) ((buffer[rtphl] & 0x60) & 0xFF); // FU indicator NRI
                    buffer[rtphl] += 28;

                    while (sum < naluLength) {
                            if ((len = fifo.read(buffer, rtphl+2,  naluLength-sum > MAXPACKETSIZE-rtphl-2 ? MAXPACKETSIZE-rtphl-2 : naluLength-sum  ))<0) return; sum += len;
                            // Last packet before next NAL
                            if (sum >= naluLength) {
                                    // End bit on
                                    buffer[rtphl+1] += 0x40;
                                    Output.markNextPacket();
                            }
                            Output.send(len+rtphl+2);
                            // Switch start bit
                            buffer[rtphl+1] = (byte) (buffer[rtphl+1] & 0x7F); 
                            //Log.d(TAG,"--- FU-A unit, sum:"+sum);
                    }
            }
        }
    }

    /********************************************************************************/
    /******** Simple fifo that will contain the NAL units waiting to be sent ********/
    /********************************************************************************/
    private static class SimpleFifo {

        //. private final static String TAG = "SimpleFifo";
        private int length = 0;
        private byte[] buffer;
        private int tail = 0, head = 0;
        
        public SimpleFifo(int length) {
                this.length = length;
                buffer = new byte[length];
        }

        public int write(InputStream is, int length) throws IOException {
                int len = 0;
                
                if (tail+length<this.length) {
                        if ((len = is.read(buffer,tail,length)) == -1) return -1;
                        tail += len;
                }
                else {
                        int u = this.length-tail;
                        if ((len = is.read(buffer,tail,u)) == -1) return -1;
                        if (len<u) {
                                tail += len;
                        } else {
                                if ((len = is.read(buffer,0,length-u)) == -1) return -1;
                                tail = len;
                                len = len+u;
                        }
                }

                return len;
        }
        
        public int read(byte[] buffer, int offset, int length) {

                //length = length>available() ? available() : length;
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
                //Log.d(TAG,"head: "+head+" tail: "+tail);
                return length;
        }
        
        public void flush() {
                tail = head = 0;
        }
        
    }
}