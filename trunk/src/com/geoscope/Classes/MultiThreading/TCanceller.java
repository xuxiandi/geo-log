package com.geoscope.Classes.MultiThreading;

import com.geoscope.Classes.Exception.CancelException;

public class TCanceller {
	
	public boolean flCancel;
	//.
	public TCancelableThread OwnerThread;
	
	public TCanceller() {
		flCancel = false;
		OwnerThread = null;
	}
	
	public TCanceller(TCancelableThread pOwnerThread) {
		flCancel = false;
		OwnerThread = pOwnerThread;
	}
	
	public void Cancel() {
		flCancel = true;
	}
	
	public void CancelWithOwnerThread() {
		Cancel();
		//.
		if (OwnerThread != null)
			OwnerThread.Cancel();
	}
	
	public void Reset() {
		flCancel = false;
	}
	
	public void Check() throws CancelException {
		if (flCancel)
			throw new CancelException(this); //. =>
	}
}
