package com.geoscope.GeoLog.DEVICE.UserAgentModule;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

public class TUserAgentModule extends TModule {

	public TUserIDValue 	UserID;
	public TUserDataValue	UserData;
	
    public TUserAgentModule(TDEVICEModule pDevice) throws Exception {
    	super(pDevice);
    	//.
        Device = pDevice;
        //.
        UserID = new TUserIDValue();
        UserData = new TUserDataValue();
    }
    
    public void Destroy() {
    }
    
    public void UpdateUserIDFromAgent() throws Exception {
    	TUserAgent UserAgent = TUserAgent.GetUserAgent(Device.context);
    	if (UserAgent != null) 
    		UserID.SetValue(OleDate.UTCCurrentTimestamp(), (int)UserAgent.Server.User.UserID);
    }
}
