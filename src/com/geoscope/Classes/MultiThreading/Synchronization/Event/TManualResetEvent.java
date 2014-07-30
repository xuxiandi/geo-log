package com.geoscope.Classes.MultiThreading.Synchronization.Event;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author dominicwilliams
 *
 */
public class TManualResetEvent implements IResetEvent {
	
        private CountDownLatch event;
        private Object mutex = new Object();
        
        public TManualResetEvent(boolean signalled) {
            if (signalled) 
                event = new CountDownLatch(0);
            else
                event = new CountDownLatch(1);
        }
        
        public TManualResetEvent() {
        	this(false);
        }
        
        public void Set() {
            event.countDown();
        }
        
        public void Reset() {
            synchronized (mutex) {
                if (event.getCount() == 0) {
                        event = new CountDownLatch(1);
                }
        }
        }
        
        public void WaitOne() throws InterruptedException {
            event.await();
        }
        
        public boolean WaitOne(int timeout, TimeUnit unit) throws InterruptedException {
            return event.await(timeout, unit);
        }
        
        public boolean WaitOne(int timeout) throws InterruptedException {
        	return WaitOne(timeout,TimeUnit.MILLISECONDS); 
    }
    
        public boolean IsSignalled() {
            return (event.getCount() == 0);
        }
}
