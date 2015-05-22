package com.geoscope.Classes.MultiThreading;


public class TCancelableThread implements Runnable {
	
	protected Thread _Thread = null;
	//.
	public TCanceller Canceller;
	
	public TCancelableThread() {
		_Thread = null;
		Canceller = new TCanceller(this);
	}
	
	public TCancelableThread(TCanceller pCanceller) {
		_Thread = null;
		if (pCanceller != null) 
			Canceller = pCanceller;
		else
			Canceller = new TCanceller(this);
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

	public void CancelByInterrupt() {
		if (_Thread != null)
			_Thread.interrupt();
	}
	
	public void Cancel() {
		CancelByCanceller();
		//.
		CancelByInterrupt();
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

