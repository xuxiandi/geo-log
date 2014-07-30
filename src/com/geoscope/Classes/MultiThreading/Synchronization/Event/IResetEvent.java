package com.geoscope.Classes.MultiThreading.Synchronization.Event;

import java.util.concurrent.TimeUnit;

public interface IResetEvent {
	
        public void Set();
        
        public void Reset();
        
        public void WaitOne() throws InterruptedException;
        
        public boolean WaitOne(int timeout, TimeUnit unit) throws InterruptedException;
        
        public boolean WaitOne(int timeout) throws InterruptedException;
        
        public boolean IsSignalled();
}
