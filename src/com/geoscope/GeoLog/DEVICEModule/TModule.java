package com.geoscope.GeoLog.DEVICEModule;

import java.io.File;

import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import android.media.MediaPlayer;

import com.geoscope.GeoLog.COMPONENT.TComponent;

public class TModule extends TComponent {

	public static final int MODULE_STATE_INITIALIZING 	= 0;
	public static final int MODULE_STATE_INITIALIZED 	= 1;
    public static final int MODULE_STATE_RUNNING 		= 2;
    public static final int MODULE_STATE_NOTRUNNING 	= 3;
    public static final int MODULE_STATE_FINALIZING 	= 4;
    public static final int MODULE_STATE_FINALIZED 		= 5;

    public static class TAudioNotifier {
    
    	private MediaPlayer player;
    	
    	public TAudioNotifier() {
    		player = new MediaPlayer();
    	}
    	
    	public void Destroy() {
    		player.release();
    	}
    	
    	protected String GetNotificationFolder() {
    		return null;
    	}
    	
    	 protected synchronized void PlayNotification(String FileName) throws Exception {
			 player.reset();
    		 //.
    		 player.setDataSource(GetNotificationFolder()+"/"+FileName);
    		 player.prepare();
    		 player.start();
    	 }
    }    
    
	public TModule Parent = null;
	//.
	public TDEVICEModule Device = null;
	//.
	public boolean flEnabled = true;
	//.
    public int ModuleState = MODULE_STATE_INITIALIZING;
	
	public TModule(TModule pParent) {
		super(pParent,-1,"");
		Parent = pParent;
	}
	
	public String ModuleFile() {
		String MFN = TDEVICEModule.DeviceFolder()+"/"+TDEVICEModule.DeviceFileName;
		File MF = new File(MFN);
		if (MF.exists())
			return MFN; //. ->
		String MOFN = TDEVICEModule.ProfileFolder()+"/"+TDEVICEModule.DeviceOldFileName;
		MF = new File(MOFN);
		if (MF.exists())
			return MOFN; //. ->
		return MFN;		
	}
	
	public synchronized void LoadProfileFrom(Element Node) throws Exception {
	}
	
	public synchronized void LoadProfile() throws Exception {
	}
	
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
	}

	public synchronized void SaveProfile() throws Exception {
		Device.SaveProfile();
	}
	
	public boolean IsEnabled() {
		return (flEnabled && ((Parent == null) || Parent.IsEnabled()));
	}
}
