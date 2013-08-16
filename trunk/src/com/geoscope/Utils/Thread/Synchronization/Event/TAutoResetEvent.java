package com.geoscope.Utils.Thread.Synchronization.Event;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author dominicwilliams 
 *
 */
public class TAutoResetEvent implements IResetEvent {
	
        private Semaphore event;
        private Object mutex = new Object();
        
        public TAutoResetEvent(boolean signalled) {
            event = new Semaphore(signalled ? 1 : 0);
        }
        
        public TAutoResetEvent() {
    		this(false);
        }
    
        public void Set() {
            synchronized (mutex) {
                if (event.availablePermits() == 0)
                        event.release();        
        }
        }
        
        public void Reset() {
            event.drainPermits();
        }
        
        public void WaitOne() throws InterruptedException {
            event.acquire();
        }
        
        public boolean WaitOne(int timeout, TimeUnit unit) throws InterruptedException {
            return event.tryAcquire(timeout, unit);
        }       
        
        public boolean WaitOne(int timeout) throws InterruptedException {
            return WaitOne(timeout,TimeUnit.MILLISECONDS);
        }       
    
        public boolean IsSignalled() {
            return (event.availablePermits() > 0);
        }       
}
