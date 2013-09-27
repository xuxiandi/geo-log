package com.geoscope.GeoLog.Utils;

public abstract class TProgressor {

	protected int Percentage = -1;
	
	public synchronized boolean DoOnProgress(int Percentage) {
		this.Percentage = Percentage;
		return true; 
	}
	
	public synchronized int ProgressPercentage() {
		return Percentage;
	}
	
	private int SummaryValue = 0;
	private int ProgressValue = 0;

	public synchronized int GetSummaryValue() {
		return SummaryValue;
	}
	
	public synchronized void SetSummaryValue(int pSummaryValue) {
		SummaryValue = pSummaryValue;
	}
	
	public synchronized void AddSummaryValue(int pAddValue) {
		SummaryValue += pAddValue;
	}
	
	public synchronized int GetProgressValue() {
		return ProgressValue;
	}
	
	public synchronized boolean IncProgressValue() {
		ProgressValue++;
		if (SummaryValue == 0)
			return false; //. ->
		int _Percentage = (int)(100*ProgressValue/SummaryValue);
		if (_Percentage <= Percentage)
			return false; //. ->
		return DoOnProgress(_Percentage);
	}
	
	public synchronized boolean DecProgressValue() {
		ProgressValue--;
		return true;
	}
}
