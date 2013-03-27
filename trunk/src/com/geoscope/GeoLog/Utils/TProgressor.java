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
	private int Value = 0;

	public void SetSummaryValue(int pSummaryValue) {
		SummaryValue = pSummaryValue;
	}
	
	public void AddSummaryValue(int pAddValue) {
		SummaryValue += pAddValue;
	}
	
	public boolean IncProgressValue() {
		Value++;
		if (SummaryValue == 0)
			return false; //. ->
		int _Percentage = (int)(100*Value/SummaryValue);
		if (_Percentage <= Percentage)
			return false; //. ->
		return DoOnProgress(_Percentage);
	}
	
	public boolean DecProgressValue() {
		Value--;
		return true;
	}
}
