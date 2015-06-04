package com.geoscope.Classes.MultiThreading;

import java.util.ArrayList;


public class TCancelableThread implements Runnable {
	
	public TCancelableThread ParentThread;
	//.
	protected Thread _Thread;
	//.
	public TCanceller Canceller;
	//.
	private ArrayList<TCancelableThread> ChildThreads = null;
	
	public TCancelableThread() {
		ParentThread = null;
		_Thread = null;
		Canceller = new TCanceller(this);
	}
	
	public TCancelableThread(TCanceller pCanceller) {
		ParentThread = null;
		_Thread = null;
		if (pCanceller != null) 
			Canceller = pCanceller;
		else
			Canceller = new TCanceller(this);
	}
	
	public TCancelableThread(TCancelableThread pParentThread) {
		ParentThread = pParentThread;
		_Thread = null;
		Canceller = new TCanceller(this);
		//.
		if (ParentThread != null)
			ParentThread.ChildThreads_Add(this);
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
		ChildThreads_CancelByCanceller();
		//.
		Canceller.flCancel = true;
	}

	public void CancelByInterrupt() {
		ChildThreads_CancelByInterrupt();
		//.
		if (_Thread != null)
			_Thread.interrupt();
	}
	
	public void Cancel() {
		ChildThreads_Cancel();
		//.
		CancelByCanceller();
		CancelByInterrupt();
	}

	public void Wait() throws InterruptedException {
		ChildThreads_Wait();
		//.
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
		ChildThreads_CancelAndWait();
		//.
		Cancel();
		if (_Thread != null)
			_Thread.join();
	}

	public void CancelAndWait(int WaitInterval) throws InterruptedException {
		Cancel();
		if (_Thread != null)
			_Thread.join(WaitInterval);
	}
	
	public synchronized void ChildThreads_Add(TCancelableThread AThread) {
		if (ChildThreads == null)
			ChildThreads = new ArrayList<TCancelableThread>();
		ChildThreads.add(AThread);
	}

	public synchronized void ChildThreads_Remove(TCancelableThread TheThread) {
		if (ChildThreads != null)
			ChildThreads.remove(TheThread);
	}

	public void ChildThreads_CancelByCanceller() {
		ArrayList<TCancelableThread> _ChildThreads; 
		synchronized (this) {
			if (ChildThreads == null)
				return; //. ->
			_ChildThreads = new ArrayList<TCancelableThread>(ChildThreads);
		}
		int Cnt = _ChildThreads.size();
		for (int I = 0; I < Cnt; I++)
			_ChildThreads.get(I).CancelByCanceller();
	}

	public void ChildThreads_CancelByInterrupt() {
		ArrayList<TCancelableThread> _ChildThreads; 
		synchronized (this) {
			if (ChildThreads == null)
				return; //. ->
			_ChildThreads = new ArrayList<TCancelableThread>(ChildThreads);
		}
		int Cnt = _ChildThreads.size();
		for (int I = 0; I < Cnt; I++)
			_ChildThreads.get(I).CancelByInterrupt();
	}

	public void ChildThreads_Cancel() {
		ArrayList<TCancelableThread> _ChildThreads; 
		synchronized (this) {
			if (ChildThreads == null)
				return; //. ->
			_ChildThreads = new ArrayList<TCancelableThread>(ChildThreads);
		}
		int Cnt = _ChildThreads.size();
		for (int I = 0; I < Cnt; I++)
			_ChildThreads.get(I).Cancel();
	}

	public void ChildThreads_Wait() throws InterruptedException {
		ArrayList<TCancelableThread> _ChildThreads; 
		synchronized (this) {
			if (ChildThreads == null)
				return; //. ->
			_ChildThreads = new ArrayList<TCancelableThread>(ChildThreads);
		}
		int Cnt = _ChildThreads.size();
		for (int I = 0; I < Cnt; I++)
			_ChildThreads.get(I).Wait();
	}

	public void ChildThreads_CancelAndWait() throws InterruptedException {
		ArrayList<TCancelableThread> _ChildThreads; 
		synchronized (this) {
			if (ChildThreads == null)
				return; //. ->
			_ChildThreads = new ArrayList<TCancelableThread>(ChildThreads);
		}
		int Cnt = _ChildThreads.size();
		for (int I = 0; I < Cnt; I++)
			_ChildThreads.get(I).CancelAndWait();
	}
}

