package com.geoscope.Classes.MultiThreading.Synchronization.Lock;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class TNamedLock {

	private static Object							LockTable_Lock = new Object();
	private static Hashtable<String, TNamedLock> 	LockTable = new Hashtable<String, TNamedLock>();
	
	private static TNamedLock GetLock(String Domain, String Name) {
		String UN = Domain+Name;
		TNamedLock Result;
		synchronized (LockTable_Lock) {
			Result = LockTable.get(UN);
			if (Result == null) {
				Result = new TNamedLock(UN);
				LockTable.put(UN, Result);
			}
			Result.RefCount++;
		}
		return Result;
	}
	
	private static TNamedLock ReleaseLock(String UN) {
		TNamedLock Result;
		synchronized (LockTable_Lock) {
			Result = LockTable.get(UN);
			if (Result != null) {
				Result.RefCount--;
				//.
				if (Result.RefCount == 0)
					LockTable.remove(UN);
			}
		}
		return Result;
	}
	
	public static TNamedLock Lock(String Domain, String Name) {
		TNamedLock Result = GetLock(Domain, Name);
		//.
		Result._Lock.lock();
		//.
		return Result;
	}
	
	public static TNamedLock TryLock(String Domain, String Name) {
		TNamedLock Result = GetLock(Domain, Name);
		//.
		if (!Result._Lock.tryLock()) {
			ReleaseLock(Result.UN);
			return null; //. ->
		}
		//.
		return Result;
	}
	
	public static TNamedLock TryLock(String Domain, String Name, int Timeout) throws InterruptedException {
		TNamedLock Result = GetLock(Domain, Name);
		//.
		if (!Result._Lock.tryLock(Timeout, TimeUnit.MILLISECONDS)) {
			ReleaseLock(Result.UN);
			return null; //. ->
		}
		//.
		return Result;
	}
	
	
	private String UN;
	//.
	private ReentrantLock _Lock = new ReentrantLock();
	//.
	private int RefCount = 0;
	
	public TNamedLock(String pUN) {
		UN = pUN;
	}
	
	public void UnLock() {
		_Lock.unlock();
		//.
		ReleaseLock(UN);
	}
}
