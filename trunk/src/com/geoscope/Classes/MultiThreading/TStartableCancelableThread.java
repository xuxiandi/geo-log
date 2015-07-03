package com.geoscope.Classes.MultiThreading;



public class TStartableCancelableThread extends TCancelableThread {
	
	public TStartableCancelableThread() {
		super();
	}
	
	public void Start() {
		_Thread = new Thread(this);
		_Thread.start();
	}
	
	public void ReStart() throws InterruptedException {
		CancelAndWait();
		//.
		Canceller.Reset();
		//.
		_Thread = new Thread(this);
		_Thread.start();
	}
}

