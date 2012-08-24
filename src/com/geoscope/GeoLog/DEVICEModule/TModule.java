package com.geoscope.GeoLog.DEVICEModule;

import org.w3c.dom.Element;
import org.xmlpull.v1.XmlSerializer;

import com.geoscope.GeoLog.COMPONENT.TComponent;

public class TModule extends TComponent {

	public TModule Parent = null;
	//.
	public TDEVICEModule Device = null;
	
	public TModule(TModule pParent) {
		Parent = pParent;
	}
	
	public String ConfigurationFile() {
		return TDEVICEModule.ProfileFolder+"/"+TDEVICEModule.ConfigurationFileName;		
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
}
