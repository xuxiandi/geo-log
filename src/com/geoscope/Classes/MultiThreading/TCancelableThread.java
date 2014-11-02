package com.geoscope.Classes.MultiThreading;

public class TCancelableThread implements Runnable {
	
	protected Thread _Thread = null;
	protected TCanceller Canceller = new TCanceller();
	
	@Override
	public void run() {
	}
	
	public void Reset() {
		_Thread = null;
		Canceller.Reset();
	}
	
	public void Join() throws InterruptedException {
		if (_Thread == null)
			return; //. ->
		_Thread.join();
	}

	public void CancelByCanceller() {
		Canceller.flCancel = true;
	}

	public void Cancel() {
		CancelByCanceller();
		//.
		if (_Thread != null)
			_Thread.interrupt();
	}

	public void Wait() throws InterruptedException {
		Join();
	}
	
	public void CancelByCancellerAndWait() throws InterruptedException {
		CancelByCanceller();
		if (_Thread != null)
			_Thread.join();
	}

	public void CancelAndWait() throws InterruptedException {
		Cancel();
		if (_Thread != null)
			_Thread.join();
	}

	public void CancelAndWait(int WaitInterval) throws InterruptedException {
		Cancel();
		if (_Thread != null)
			_Thread.join(WaitInterval);
	}
}

