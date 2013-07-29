package com.geoscope.GeoLog.DEVICEModule;

import java.io.File;

import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.GeoLog.COMPONENT.TComponent;

public class TModule extends TComponent {

	public TModule Parent = null;
	//.
	public TDEVICEModule Device = null;
	//.
	public boolean flEnabled = true;
	
	public TModule(TModule pParent) {
		super();
		Parent = pParent;
	}
	
	public String ModuleFile() {
		String MFN = TDEVICEModule.DeviceFolder+"/"+TDEVICEModule.DeviceFileName;
		File MF = new File(MFN);
		if (MF.exists())
			return MFN; //. ->
		String MOFN = TDEVICEModule.ProfileFolder+"/"+TDEVICEModule.DeviceOldFileName;
		MF = new File(MOFN);
		if (MF.exists())
			return MOFN; //. ->
		return MFN;		
	}
	
	public synchronized void LoadConfigurationFrom(Element Node) throws Exception {
	}
	
	public synchronized void LoadConfiguration() throws Exception {
	}
	
	public synchronized void SaveConfigurationTo(XmlSerializer Serializer) throws Exception {
	}

	public synchronized void SaveConfiguration() throws Exception {
		Device.SaveConfiguration();
	}
	
	public boolean IsEnabled() {
		return (flEnabled && ((Parent == null) || Parent.IsEnabled()));
	}
}
