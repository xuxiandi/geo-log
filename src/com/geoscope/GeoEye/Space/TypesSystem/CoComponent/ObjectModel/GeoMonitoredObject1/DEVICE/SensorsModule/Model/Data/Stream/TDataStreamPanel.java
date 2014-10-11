package com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Stream.TStreamDescriptor;
import com.geoscope.Classes.Data.Stream.Channel.TChannelIDs;
import com.geoscope.Classes.MultiThreading.TCanceller;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.CoTypes.CoGeoMonitorObject.TCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.TStreamChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.ENVC.TENVCChannel;
import com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.EnvironmentalConditions.XENVC.TXENVCChannel;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

public class TDataStreamPanel extends Activity {

	private String 	ServerAddress;
	private int 	ServerPort;
	//.
	private int 	UserID;
	private String 	UserPassword;
	//.
	private int						ObjectIndex = -1;
	private TCoGeoMonitorObject 	Object;
	//.
	private byte[] 				StreamDescriptorData;
	private TStreamDescriptor 	StreamDescriptor;
	private TChannelIDs			StreamChannels = null;
	//.
	private ArrayList<TStreamChannelProcessorAbstract> StreamChannelProcessors = new ArrayList<TStreamChannelProcessorAbstract>();
	//.
	private boolean IsInFront = false;
	//.
	private TextView	lbStatus;
	//.
	private LinearLayout llEnvironmentConditionsENVC;
	private LinearLayout llEnvironmentConditionsXENVC;
	
    public void onCreate(Bundle savedInstanceState) {
    	try {
            super.onCreate(savedInstanceState);
            //.
            Bundle extras = getIntent().getExtras(); 
            if (extras != null) {
            	ServerAddress = extras.getString("ServerAddress");
            	ServerPort = extras.getInt("ServerPort");
            	//.
            	UserID = extras.getInt("UserID");
            	UserPassword = extras.getString("UserPassword");
            	//.
            	ObjectIndex = extras.getInt("ObjectIndex");
        		TReflector Reflector = TReflector.GetReflector();  
            	Object = Reflector.CoGeoMonitorObjects.Items[ObjectIndex];
            	//.
            	StreamDescriptorData = extras.getByteArray("StreamDescriptorData");
            	StreamDescriptor = new TStreamDescriptor(StreamDescriptorData,com.geoscope.GeoEye.Space.TypesSystem.CoComponent.ObjectModel.GeoMonitoredObject1.DEVICE.SensorsModule.Model.Data.Stream.Channels.TChannelsProvider.Instance);
            	//.
            	byte[] StreamChannelsBA = extras.getByteArray("StreamChannels");
            	if (StreamChannelsBA != null) 
            		StreamChannels = new TChannelIDs(StreamChannelsBA);
            	else
            		StreamChannels = null;
            }
            //.
    		requestWindowFeature(Window.FEATURE_NO_TITLE);
    		//.
            setContentView(R.layout.sensorsmodule_datastream_panel);
            //.
            lbStatus = (TextView)findViewById(R.id.lbStatus);
            llEnvironmentConditionsENVC = (LinearLayout)findViewById(R.id.llEnvironmentConditionsENVC);
            llEnvironmentConditionsXENVC = (LinearLayout)findViewById(R.id.llEnvironmentConditionsXENVC);
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			finish();
		}
    }
	
    public void onDestroy() {
		super.onDestroy();
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		IsInFront = false;
		//.
    	try {
    		StreamChannelProcessors_Finalize();
		} catch (Exception E) {
			DoOnException(E);
		}
	}

	protected void onResume() {
		super.onResume();
		IsInFront = true;
    	//.
    	try {
    		StreamChannelProcessors_Initialize();
		} catch (Exception E) {
			DoOnException(E);
		}
	}

	private void StreamChannelProcessors_Initialize(SurfaceHolder SH, int Width, int Height) throws Exception {
		StreamChannelProcessors_Finalize();
		//.
		Layout_Reset();
		for (int I = 0; I < StreamDescriptor.Channels.size(); I++) {
			TStreamChannel Channel = (TStreamChannel)StreamDescriptor.Channels.get(I);
			if ((StreamChannels == null) || StreamChannels.IDExists(Channel.ID)) {
				TStreamChannelProcessorAbstract ChannelProcessor = new TStreamChannelProcessor(this, ServerAddress,ServerPort, UserID,UserPassword, Object, Channel, new TStreamChannelProcessorAbstract.TOnProgressHandler(Channel) {
					@Override
					public void DoOnProgress(int ReadSize, TCanceller Canceller) {
						TDataStreamPanel.this.DoOnStatusMessage("");
					}
				}, new TStreamChannelProcessorAbstract.TOnIdleHandler(Channel) {
					@Override
					public void DoOnIdle(TCanceller Canceller) {
						TDataStreamPanel.this.DoOnStatusMessage(TDataStreamPanel.this.getString(R.string.SChannelIdle)+Channel.Name);
					}
				}, new TStreamChannelProcessorAbstract.TOnExceptionHandler(Channel) {
					@Override
					public void DoOnException(Exception E) {
						TDataStreamPanel.this.DoOnException(E);
					}
				});
				if (ChannelProcessor != null) {
					StreamChannelProcessors.add(ChannelProcessor);
					if (ChannelProcessor.IsVisual())
						ChannelProcessor.VisualSurface_Set(SH, Width,Height);
					//.
					Layout_UpdateForChannel(Channel);
					//.
					ChannelProcessor.Start();
				}			  
			}
		}
	}
	
	private void StreamChannelProcessors_Initialize() throws Exception {
		StreamChannelProcessors_Initialize(null,0,0);
	}
	
	private void StreamChannelProcessors_Finalize() throws Exception {
		for (int I = 0; I < StreamChannelProcessors.size(); I++) 
			StreamChannelProcessors.get(I).Destroy();
		StreamChannelProcessors.clear();
	}
	
	private void Layout_Reset() {
		llEnvironmentConditionsENVC.setVisibility(View.GONE);
		llEnvironmentConditionsXENVC.setVisibility(View.GONE);
	}
	
	private void Layout_UpdateForChannel(TStreamChannel Channel) {
		if (Channel instanceof TENVCChannel) {
			TENVCChannel ENVCChannel = (TENVCChannel)Channel;
			//.
			final EditText edTemperature = (EditText)findViewById(R.id.edEnvironmentConditionsENVCTemperature);
			final EditText edPressure = (EditText)findViewById(R.id.edEnvironmentConditionsENVCPressure);
			final EditText edHumidity = (EditText)findViewById(R.id.edEnvironmentConditionsENVCHumidity);
			//.
			ENVCChannel.OnTemperatureHandler = new TENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					DoOnEditTextValueMessage(edTemperature,String.format("%.2f",Value)+" C");
				}
			};
			ENVCChannel.OnPressureHandler = new TENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					DoOnEditTextValueMessage(edPressure,String.format("%.2f",Value)+" mbar");
				}
			};
			ENVCChannel.OnHumidityHandler = new TENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					DoOnEditTextValueMessage(edHumidity,String.format("%.2f",Value)+" %");
				}
			};
			//.
			llEnvironmentConditionsENVC.setVisibility(View.VISIBLE);
		};
		if (Channel instanceof TXENVCChannel) {
			TXENVCChannel XENVCChannel = (TXENVCChannel)Channel;
			//.
			final EditText edTemperature = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCTemperature);
			final EditText edPressure = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCPressure);
			final EditText edRelativeHumidity = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCRelativeHumidity);
			final EditText edLight = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCLight);
			final EditText edAcceleration = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCAcceleration);
			final EditText edMagneticField = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCMagneticField);
			final EditText edGyroscope = (EditText)findViewById(R.id.edEnvironmentConditionsXENVCGyroscope);
			//.
			XENVCChannel.OnTemperatureHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					DoOnEditTextValueMessage(edTemperature,String.format("%.2f",Value)+" C");
				}
			};
			XENVCChannel.OnPressureHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					DoOnEditTextValueMessage(edPressure,String.format("%.2f",Value)+" mbar");
				}
			};
			XENVCChannel.OnRelativeHumidityHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					DoOnEditTextValueMessage(edRelativeHumidity,String.format("%.2f",Value)+" %");
				}
			};
			XENVCChannel.OnLightHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					DoOnEditTextValueMessage(edLight,String.format("%.2f",Value)+" lx");
				}
			};
			XENVCChannel.OnAccelerationHandler = new TXENVCChannel.TDoOnValueHandler() {
				@Override
				public void DoOnValue(double Value) {
					DoOnEditTextValueMessage(edAcceleration,String.format("%.2f",Value)+" m/s^2");
				}
			};
			XENVCChannel.OnMagneticFieldHandler = new TXENVCChannel.TDoOn3ValueHandler() {
				@Override
				public void DoOn3Value(double Value, double Value1, double Value2) {
					DoOnEditTextValueMessage(edMagneticField,"(X: "+String.format("%.2f",Value)+", Y: "+String.format("%.2f",Value1)+", Z: "+String.format("%.2f",Value2)+") mT");
				}
			};
			XENVCChannel.OnGyroscopeHandler = new TXENVCChannel.TDoOn3ValueHandler() {
				@Override
				public void DoOn3Value(double Value, double Value1, double Value2) {
					DoOnEditTextValueMessage(edGyroscope,"(X: "+String.format("%.2f",Value)+", Y: "+String.format("%.2f",Value1)+", Z: "+String.format("%.2f",Value2)+") rad/s");
				}
			};
			//.
			llEnvironmentConditionsXENVC.setVisibility(View.VISIBLE);
		};
	}
	
	private static final int MESSAGE_SHOWSTATUSMESSAGE 		= 1;
	private static final int MESSAGE_SHOWEXCEPTION 			= 2;
	private static final int MESSAGE_EDITTEXT_WRITEVALUE	= 3;
	
	public static class TEditTextValueString {
		
		public EditText ET;
		public String VS;
		
		public TEditTextValueString(EditText pET, String pVS) {
			ET = pET;
			VS = pVS;
		}
	}
	
	@SuppressLint("HandlerLeak")
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_SHOWEXCEPTION:
    				Throwable E = (Throwable)msg.obj;
    				String EM = E.getMessage();
    				if (EM == null) 
    					EM = E.getClass().getName();
    				//.
    				Toast.makeText(TDataStreamPanel.this,EM,Toast.LENGTH_LONG).show();
    				// .
    				break; // . >

    			case MESSAGE_SHOWSTATUSMESSAGE:
    				String S = (String)msg.obj;
    				//.
    				if (S.length() > 0) {
    					lbStatus.setText(S);
    					lbStatus.setVisibility(View.VISIBLE);
    				}
    				else {
    					lbStatus.setText("");
    					lbStatus.setVisibility(View.GONE);
    				}
    				// .
    				break; // . >

    			case MESSAGE_EDITTEXT_WRITEVALUE:
    				TEditTextValueString ETS = (TEditTextValueString)msg.obj;
    				//.
    				ETS.ET.setText(ETS.VS);
    				//.
					lbStatus.setText("");
					lbStatus.setVisibility(View.GONE);
    				//.
    				break; // . >
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	private void DoOnStatusMessage(String S) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWSTATUSMESSAGE,S).sendToTarget();
	}
	
	private void DoOnException(Throwable E) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
	}
	
	private void DoOnEditTextValueMessage(EditText ET, String Message) {
		if (IsInFront)
			MessageHandler.obtainMessage(MESSAGE_EDITTEXT_WRITEVALUE,new TEditTextValueString(ET, Message)).sendToTarget();
	}
}
