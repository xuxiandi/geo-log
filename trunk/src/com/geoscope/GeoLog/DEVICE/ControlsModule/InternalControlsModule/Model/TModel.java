package com.geoscope.GeoLog.DEVICE.ControlsModule.InternalControlsModule.Model;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.GeoLog.DEVICE.ControlsModule.Model.TDestinationModel;

public class TModel extends TDestinationModel {

    public String Name = "Internal Controls";
    public String Info = "Controls onboard of android device";
    //.
    public TStreamDescriptor Stream;
    
	public TModel() {
		Stream = new TStreamDescriptor();
	}
}
