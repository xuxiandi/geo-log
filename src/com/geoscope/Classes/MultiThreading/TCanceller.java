package com.geoscope.Classes.MultiThreading;

import com.geoscope.Classes.Exception.CancelException;

public class TCanceller {
	
	public boolean flCancel;
	//.
	private volatile TCancelableThread OwnerThread;
	
	public TCanceller() {
		flCancel = false;
		OwnerThread = null;
	}
	
	public TCanceller(TCancelableThread pOwnerThread) {
		flCancel = false;
		OwnerThread = pOwnerThread;
	}
	
	public void SetOwnerThread(TCancelableThread pOwnerThread) {
		OwnerThread = pOwnerThread;
	}
	
	public TCancelableThread GetOwnerThread() {
		return OwnerThread;
	}
	
	public void Cancel() {
		flCancel = true;
		//.
		TCancelableThread _OwnerThread = OwnerThread;
		if (_OwnerThread != null)
			_OwnerThread.CancelByInterrupt();
	}
	
	public void Reset() {
		flCancel = false;
	}
	
	public void Check() throws CancelException {
		if (flCancel)
			throw new CancelException(this); //. =>
	}
}
