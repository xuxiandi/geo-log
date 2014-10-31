/* ====================================================================
 * Copyright (c) 2014 Alpha Cephei Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ALPHA CEPHEI INC. ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL CARNEGIE MELLON UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 */

package com.geoscope.GeoLog.DEVICE.AudioModule.VoiceCommandModule.CMUSphinx;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MediaRecorder.AudioSource;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.geoscope.GeoLog.DEVICE.AudioModule.TAudioModule;
import com.geoscope.GeoLog.DEVICE.AudioModule.TMicrophoneCapturingServer;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.FsgModel;
import edu.cmu.pocketsphinx.Hypothesis;

/**
 * Main class to access recognizer functions. After configuration this class
 * starts a listener thread which records the data and recognizes it using
 * Pocketsphinx engine. Recognition events are passed to a client using
 * {@link TRecognitionListener}
 * 
 */
public class TSpeechRecognizer {

    public static final String TAG = TSpeechRecognizer.class.getSimpleName();
    
    public static final int SilenceRestartInterval = 1000*10; //. seconds

    private TAudioModule AudioModule;
    //.
    private final Decoder decoder;
    //.
    private Thread recognizerThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Collection<TRecognitionListener> listeners = new HashSet<TRecognitionListener>();
    //.
    private final int sampleRate;
    //.
    private boolean 										MicrophoneCapturingServer_flAvailable = false;
	private TMicrophoneCapturingServer.TConfiguration 		MicrophoneCapturingServer_Configuration;
    //.
    private boolean VADState = false; 
    private long	VADState_Timestamp; 
    private short[] ProcessBuffer = new short[0];
    
    protected TSpeechRecognizer(TAudioModule pAudioModule, Config config) {
    	AudioModule = pAudioModule;
        sampleRate = (int) config.getFloat("-samprate");
        if (config.getFloat("-samprate") != sampleRate)
            throw new IllegalArgumentException("sampling rate must be integer");
        decoder = new Decoder(config);
        //.
        MicrophoneCapturingServer_flAvailable = ((AudioModule != null) && (AudioModule.MicrophoneCapturingServer != null));
        if (MicrophoneCapturingServer_flAvailable) {
        	MicrophoneCapturingServer_Configuration = new TMicrophoneCapturingServer.TConfiguration(MediaRecorder.AudioSource.VOICE_RECOGNITION,sampleRate,TMicrophoneCapturingServer.TConfiguration.BUFFERSIZE_ANY);
        }
    }

    public void Destroy() throws InterruptedException {
    	cancel();
    	//.
    	UpdateListeningSource();
    }
    
    /**
     * Adds listener.
     */
    public void addListener(TRecognitionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Removes listener.
     */
    public void removeListener(TRecognitionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * Starts recognition. Does nothing if recognition is active.
     * 
     * @return true if recognition was actually started
     */
    public boolean startListening(String searchName) {
        if (null != recognizerThread)
            return false;

        Log.i(TAG, format("Start recognition \"%s\"", searchName));
        decoder.setSearch(searchName);
        recognizerThread = new RecognizerThread();
        recognizerThread.start();
        return true;
    }

    /**
     * Starts recognition. After specified timeout listening stops and the
     * endOfSpeech signals about that. Does nothing if recognition is active.
     * 
     * @timeout - timeout in milliseconds to listen.
     * 
     * @return true if recognition was actually started
     */
    public boolean startListening(String searchName, int timeout) {
        if (null != recognizerThread)
            return false;

        Log.i(TAG, format("Start recognition \"%s\"", searchName));
        decoder.setSearch(searchName);
        recognizerThread = new RecognizerThread(timeout);
        recognizerThread.start();
        return true;
    }

    public void UpdateListeningSource() throws InterruptedException {
    	if (MicrophoneCapturingServer_flAvailable)
    		AudioModule.MicrophoneCapturingServer.CheckForIdling();
    }
    
    private boolean stopRecognizerThread() {
        if (null == recognizerThread)
            return false;

        try {
            recognizerThread.interrupt();
            recognizerThread.join();
        } catch (InterruptedException e) {
            // Restore the interrupted status.
            Thread.currentThread().interrupt();
        }

        recognizerThread = null;
        return true;
    }

    /**
     * Stops recognition. All listeners should receive final result if there is
     * any. Does nothing if recognition is not active.
     * 
     * @return true if recognition was actually stopped
     */
    public boolean stop() {
        boolean result = stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Stop recognition");
            final Hypothesis hypothesis = decoder.hyp();
            mainHandler.post(new ResultEvent(hypothesis, true));
        }
        return result;
    }

    /**
     * Cancels recognition. Listeners do not receive final result. Does nothing
     * if recognition is not active.
     * 
     * @return true if recognition was actually canceled
     */
    public boolean cancel() {
        boolean result = stopRecognizerThread();
        if (result) {
            Log.i(TAG, "Cancel recognition");
        }

        return result;
    }

    /**
     * Gets name of the currently active search.
     * 
     * @return active search name or null if no search was started
     */
    public String getSearchName() {
        return decoder.getSearch();
    }

    public void addFsgSearch(String searchName, FsgModel fsgModel) {
        decoder.setFsg(searchName, fsgModel);
    }

    /**
     * Adds searches based on JSpeech grammar.
     * 
     * @param name
     *            search name
     * @param file
     *            JSGF file
     */
    public void addGrammarSearch(String name, File file) {
        Log.i(TAG, format("Load JSGF %s", file));
        decoder.setJsgfFile(name, file.getPath());
    }

    /**
     * Adds search based on N-gram language model.
     * 
     * @param name
     *            search name
     * @param file
     *            N-gram model file
     */
    public void addNgramSearch(String name, File file) {
        Log.i(TAG, format("Load N-gram model %s", file));
        decoder.setLmFile(name, file.getPath());
    }

    /**
     * Adds search based on a single phrase.
     * 
     * @param name
     *            search name
     * @param phrase
     *            search phrase
     */
    public void addKeyphraseSearch(String name, String phrase) {
        decoder.setKeyphrase(name, phrase);
    }

    /**
     * Adds search based on a keyphrase file.
     * 
     * @param name
     *            search name
     * @param phrase
     *            search phrase
     */
    public void addKeywordSearch(String name, File file) {
        decoder.setKws(name, file.getPath());
    }

    private final class RecognizerThread extends Thread {

        private int bufferSize;
        private int remainingSamples;
        private int timeoutSamples;
        private final static int NO_TIMEOUT = -1;
        private final static float BUFFER_SIZE_SECONDS = 0.4f;

        public RecognizerThread(int timeout) {
            this.bufferSize = Math.round(sampleRate * BUFFER_SIZE_SECONDS);
            this.timeoutSamples = timeout * sampleRate / 1000;
            this.remainingSamples = this.timeoutSamples;
        }

        public RecognizerThread() {
            this.bufferSize = Math.round(sampleRate * BUFFER_SIZE_SECONDS);
            timeoutSamples = NO_TIMEOUT;
        }

        @Override
        public void run() {
        	try {
        		if (MicrophoneCapturingServer_flAvailable) {
        			//. try to connect to an AudioModule.MicrophoneCapturingServer
        			TMicrophoneCapturingServer.TPacketSubscriber PacketSubscriber = new TMicrophoneCapturingServer.TPacketSubscriber() {
        				@Override
        				protected void DoOnPacket(byte[] Packet, int PacketSize) throws IOException {
        					ProcessPacket(Packet,PacketSize);
        				}
        			};  
        			if (AudioModule.MicrophoneCapturingServer.Connect(MicrophoneCapturingServer_Configuration, PacketSubscriber, false)) {
        				try {
            				AudioModule.MicrophoneCapturingServer.Start();
            				//.
            	            decoder.startUtt(null);            
            				try {
            		            Log.d(TAG, "Start voice recognition...");
            		            //.
                	            VADState = decoder.getVadState();
                	            VADState_Timestamp = System.currentTimeMillis();
            		            while (!interrupted()) 
            						Thread.sleep(1000*60);
            		            //.
            		            Log.d(TAG, "Stopped voice recognition.");
            				}
            				finally {
            		            decoder.endUtt();
            		            // Remove all pending notifications.
            		            mainHandler.removeCallbacksAndMessages(null);
            				}
        		            // If we met timeout signal that speech ended
        		            if (timeoutSamples != NO_TIMEOUT && remainingSamples <= 0) {
        		                mainHandler.post(new InSpeechChangeEvent(false));
        		            }
        				}
        				finally {
        					AudioModule.MicrophoneCapturingServer.Disconnect(PacketSubscriber, false);
        				}
    		            //.
    		            return; //. ->
        			}
        		}
        		//. use default
    			AudioModule.Device.Log.WriteWarning("Voice recognizer","unable to connect to the MicrophoneCapturingServer (the configuration is differ with a current one), using default method");
    			//.
        		run_default();
        	}
        	catch (InterruptedException IE) {
        	}
        	catch (Exception E) {
        	}
        }

		private void ProcessPacket(byte[] Packet, int PacketSize) throws IOException {
			int Size = (PacketSize >> 1);
			if (ProcessBuffer.length < Size)
				ProcessBuffer = new short[Size];
			for (int I = 0; I < Size; I++) {
				int Idx = (I << 1);
				ProcessBuffer[I] = (short)(((Packet[Idx+1] & 0xFF) << 8)+(Packet[Idx] & 0xFF)); 
			}
			//.
            decoder.processRaw(ProcessBuffer,Size, false, false);
            //.
            Hypothesis hypothesis = decoder.hyp();
            if (hypothesis != null) {
    			String Command = hypothesis.getHypstr();
                Log.d(TSpeechRecognizer.TAG, "Phrase received: "+Command);
            }
            //.
            boolean _VadState = decoder.getVadState();
            if (_VadState != VADState) {
                VADState = _VadState;
	            VADState_Timestamp = System.currentTimeMillis();
                //.
                if (VADState || (hypothesis != null))
                	mainHandler.post(new InSpeechChangeEvent(VADState));
                //.
                if (VADState)
		            Log.d(TAG, "voice ...");
                else
		            Log.d(TAG, "silence");
            }
            else
            	if (!_VadState) {
            		long NowMillis = System.currentTimeMillis();
            		if ((NowMillis-VADState_Timestamp) > SilenceRestartInterval) {
            			//. restart Utt
    		            decoder.endUtt();
        	            decoder.startUtt(null);            
            			//.
        	            VADState_Timestamp = System.currentTimeMillis();
        	            //.
    		            Log.d(TAG, "Restarted voice recognition.");
            		}
            	}
		}
		
        public void run_default() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
            //.
            AudioRecord recorder = new AudioRecord(AudioSource.VOICE_RECOGNITION, sampleRate,
	                AudioFormat.CHANNEL_IN_MONO,
	                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2);          		    
    	    if (recorder.getState() == AudioRecord.STATE_UNINITIALIZED) {
    	        recorder.release();
        		IOException ioe = new IOException("Failed to initialize recorder. Microphone might be already in use.");
        		mainHandler.post(new OnErrorEvent(ioe));
        		return;
        	}
            recorder.startRecording();
            if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
                recorder.stop();
                recorder.release();
                IOException ioe = new IOException("Failed to start recording. Microphone might be already in use.");
                mainHandler.post(new OnErrorEvent(ioe));
                return;
    	    }
            
            Log.d(TAG, "Starting decoding");

            decoder.startUtt(null);            
            short[] buffer = new short[bufferSize];
            boolean _vadState = decoder.getVadState();

            while (!interrupted() && ((timeoutSamples == NO_TIMEOUT) || (remainingSamples > 0))) {
                int nread = recorder.read(buffer, 0, buffer.length);

                if (-1 == nread) {
                    throw new RuntimeException("error reading audio buffer");
                } else if (nread > 0) {
                    decoder.processRaw(buffer, nread, false, false);

                    //.  
                    Hypothesis hypothesis = decoder.hyp();
                    
                    if (decoder.getVadState() != _vadState) {
                        _vadState = decoder.getVadState();
                        //.
                        if (_vadState || (hypothesis != null))
                        	mainHandler.post(new InSpeechChangeEvent(_vadState));
                    }

                    if (_vadState)
                        remainingSamples = timeoutSamples;

                    //. final Hypothesis hypothesis = decoder.hyp();
                    //. mainHandler.post(new ResultEvent(hypothesis, false));
                }

                if (timeoutSamples != NO_TIMEOUT) {
                    remainingSamples = remainingSamples - nread;
                }
            }

            recorder.stop();
            recorder.release();
            decoder.endUtt();

            // Remove all pending notifications.
            mainHandler.removeCallbacksAndMessages(null);

            // If we met timeout signal that speech ended
            if (timeoutSamples != NO_TIMEOUT && remainingSamples <= 0) {
                mainHandler.post(new InSpeechChangeEvent(false));
            }
        }
    }

    private abstract class RecognitionEvent implements Runnable {
        public void run() {
            TRecognitionListener[] emptyArray = new TRecognitionListener[0];
            for (TRecognitionListener listener : listeners.toArray(emptyArray))
                execute(listener);
        }

        protected abstract void execute(TRecognitionListener listener);
    }

    private class InSpeechChangeEvent extends RecognitionEvent {
        private final boolean state;

        InSpeechChangeEvent(boolean state) {
            this.state = state;
        }

        @Override
        protected void execute(TRecognitionListener listener) {
            if (state)
                listener.onBeginningOfSpeech();
            else
                listener.onEndOfSpeech();
        }
    }

    private class ResultEvent extends RecognitionEvent {
        protected final Hypothesis hypothesis;
        private final boolean finalResult;

        ResultEvent(Hypothesis hypothesis, boolean finalResult) {
            this.hypothesis = hypothesis;
            this.finalResult = finalResult;
        }

        @Override
        protected void execute(TRecognitionListener listener) {
            if (finalResult)
                listener.onResult(hypothesis);
            else
                listener.onPartialResult(hypothesis);
        }
    }
    
    private class OnErrorEvent extends RecognitionEvent {
        private final Exception exception;

        OnErrorEvent(Exception exception) {
            this.exception = exception;
        }

        @Override
        protected void execute(TRecognitionListener listener) {
            listener.onError(exception);
        }
    }
}
