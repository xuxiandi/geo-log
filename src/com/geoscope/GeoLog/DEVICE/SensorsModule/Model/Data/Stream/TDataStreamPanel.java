package com.geoscope.GeoLog.DEVICE.SensorsModule.Model.Data.Stream;

import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TStreamChannelConnectorAbstract;

public class TDataStreamPanel extends com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.TDataStreamPanel {

	@Override
	protected TStreamChannelConnectorAbstract StreamChannelConnectors_CreateOneForChannel(TStreamChannel Channel) throws Exception {
		TStreamChannelConnectorAbstract ChannelConnector = new TStreamChannelConnector(this, Channel, new TStreamChannelConnectorAbstract.TOnProgressHandler(Channel) {
			
			@Override
			public void DoOnProgress(int ReadSize, TCanceller Canceller) {
				TDataStreamPanel.this.PostStatusMessage("");
			}
		}, new TStreamChannelConnectorAbstract.TOnIdleHandler(Channel) {
			
			@Override
			public void DoOnIdle(TCanceller Canceller) {
				TDataStreamPanel.this.PostStatusMessage(TDataStreamPanel.this.getString(R.string.SChannelIdle)+Channel.Name);
			}
		}, new TStreamChannelConnectorAbstract.TOnExceptionHandler(Channel) {
			
			@Override
			public void DoOnException(Exception E) {
				TDataStreamPanel.this.PostException(E);
			}
		});
		//.
		return ChannelConnector;
	}
}
