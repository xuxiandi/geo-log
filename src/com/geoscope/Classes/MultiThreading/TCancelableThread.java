package com.geoscope.Classes.MultiThreading;


public class TCancelableThread implements Runnable {
	
	protected Thread _Thread;
	//.
	public TCanceller Canceller = new TCanceller();
	
	public TCancelableThread() {
		_Thread = null;
	}
	
	public void Destroy() throws Exception {
		CancelAndWait();
	}
	
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

	public void Join(int Timeout) throws InterruptedException {
		if (_Thread == null)
			return; //. ->
		_Thread.join(Timeout);
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
	
	public void Wait(int Timeout) throws InterruptedException {
		Join(Timeout);
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

