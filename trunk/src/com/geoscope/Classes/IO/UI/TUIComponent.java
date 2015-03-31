package com.geoscope.Classes.IO.UI;



public class TUIComponent {

	protected boolean flVisible = false;
	
	public void Destroy() throws Exception {
	}

	public void Start() throws Exception {
	}

	public void Stop() throws Exception {
	}
	
	public void Resume() {
	}
	
	public void Pause() {
	}
	
	public void Show() {
		flVisible = true;
	}
	
	public void Hide() {
		flVisible = false;
	}
	
	public boolean IsVisible() {
		return flVisible;
	}
}
