package com.geoscope.GeoLog.DEVICE.SensorsModule.InternalSensorsModule.Model;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.GeoLog.DEVICE.SensorsModule.Model.TSourceModel;

public class TModel extends TSourceModel {

    public String Name = "Internal Sensors";
    public String Info = "Sensors onboards of android device";
    //.
    public TStreamDescriptor Stream;
    
	public TModel() {
		Stream = new TStreamDescriptor();
	}
}
