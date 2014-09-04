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
	
	public void Join() {
		if (_Thread == null)
			return; //. ->
		try {
			_Thread.join();
		}
		catch (Exception E) {}
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

	public void Wait() {
		Join();
	}
	
	public void CancelAndWait() {
		Cancel();
		try {
			if (_Thread != null)
				_Thread.join();
		} catch (InterruptedException E) {
		}
	}

	public void CancelAndWait(int WaitInterval) {
		Cancel();
		try {
			if (_Thread != null)
				_Thread.join(WaitInterval);
		} catch (InterruptedException E) {
		}
	}
}

