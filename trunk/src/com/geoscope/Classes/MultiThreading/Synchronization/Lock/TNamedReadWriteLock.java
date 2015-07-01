package com.geoscope.Classes.MultiThreading.Synchronization.Lock;

import java.util.Hashtable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class TNamedReadWriteLock {

	private static Object									LockTable_Lock = new Object();
	private static Hashtable<String, TNamedReadWriteLock> 	LockTable = new Hashtable<String, TNamedReadWriteLock>();
	
	private static TNamedReadWriteLock GetLock(String Domain, String Name) {
		String UN = Domain+Name;
		TNamedReadWriteLock Result;
		synchronized (LockTable_Lock) {
			Result = LockTable.get(UN);
			if (Result == null) {
				Result = new TNamedReadWriteLock(UN);
				LockTable.put(UN, Result);
			}
			Result.RefCount++;
		}
		return Result;
	}
	
	private static TNamedReadWriteLock ReleaseLock(String UN) {
		TNamedReadWriteLock Result;
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
	
	public static TNamedReadWriteLock ReadLock(String Domain, String Name) {
		TNamedReadWriteLock Result = GetLock(Domain, Name);
		//.
		Result._Lock_ReadLock.lock();
		//.
		return Result;
	}
	
	public static TNamedReadWriteLock TryReadLock(String Domain, String Name) {
		TNamedReadWriteLock Result = GetLock(Domain, Name);
		//.
		if (!Result._Lock_ReadLock.tryLock()) {
			ReleaseLock(Result.UN);
			return null; //. ->
		}
		//.
		return Result;
	}
	
	public static TNamedReadWriteLock TryReadLock(String Domain, String Name, int Timeout) throws InterruptedException {
		TNamedReadWriteLock Result = GetLock(Domain, Name);
		//.
		if (!Result._Lock_ReadLock.tryLock(Timeout, TimeUnit.MILLISECONDS)) {
			ReleaseLock(Result.UN);
			return null; //. ->
		}
		//.
		return Result;
	}
		
	public static TNamedReadWriteLock WriteLock(String Domain, String Name) {
		TNamedReadWriteLock Result = GetLock(Domain, Name);
		//.
		Result._Lock_WriteLock.lock();
		//.
		return Result;
	}
	
	public static TNamedReadWriteLock TryWriteLock(String Domain, String Name) {
		TNamedReadWriteLock Result = GetLock(Domain, Name);
		//.
		if (!Result._Lock_WriteLock.tryLock()) {
			ReleaseLock(Result.UN);
			return null; //. ->
		}
		//.
		return Result;
	}
	
	public static TNamedReadWriteLock TryWriteLock(String Domain, String Name, int Timeout) throws InterruptedException {
		TNamedReadWriteLock Result = GetLock(Domain, Name);
		//.
		if (!Result._Lock_WriteLock.tryLock(Timeout, TimeUnit.MILLISECONDS)) {
			ReleaseLock(Result.UN);
			return null; //. ->
		}
		//.
		return Result;
	}
		
	
	private String UN;
	//.
	private ReentrantReadWriteLock 				_Lock = new ReentrantReadWriteLock();
	private ReentrantReadWriteLock.ReadLock 	_Lock_ReadLock = _Lock.readLock();
	private ReentrantReadWriteLock.WriteLock	_Lock_WriteLock = _Lock.writeLock();
	//.
	private int RefCount = 0;
	
	public TNamedReadWriteLock(String pUN) {
		UN = pUN;
	}
	
	public void ReadUnLock() {
		_Lock_ReadLock.unlock();
		//.
		ReleaseLock(UN);
	}
	
	public void WriteUnLock() {
		_Lock_WriteLock.unlock();
		//.
		ReleaseLock(UN);
	}
}
