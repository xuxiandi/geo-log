package com.geoscope.Classes.Exception;

import com.geoscope.Classes.MultiThreading.TCanceller;

public class CancelException extends Exception {

	private static final long serialVersionUID = 1L;
	
	public TCanceller Canceller = null;

	public CancelException() {
		super();
	}

	public CancelException(TCanceller pCanceller) {
		super();
		Canceller = pCanceller;
	}
}
