/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.BatteryModule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.AlarmModule.TAlarmModule.TAlarmer;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetBatteryChargeValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;

/**
 *
 * @author ALXPONOM
 */
public class TBatteryModule extends TModule   
{
	public static final short CriticalBatteryLevel = 5;
	
    private BroadcastReceiver BatteryLevelReceiver;
    private short LastPercentage = 101;
    private short LastLevel = 101;
    //.
    private TAlarmer BatteryLevelAlarmer = null; 
    
    public TBatteryModule(TDEVICEModule pDevice) throws Exception
    {
    	super(pDevice);
    	//.
    	Device = pDevice;
    	//.
    	Start();
    }
    
    public void Destroy() throws Exception
    {
    	Stop();
    }
    
    @Override
    public void Start() throws Exception {
    	super.Start();
    	//.
    	if (IsEnabled()) {
            BatteryLevelReceiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    short level = -1;
                    if (rawlevel >= 0 && scale > 0) {
                        level = (short)((rawlevel*100)/scale);
                        DoOnBatteryLevelChanged(level);            
                    }
                }
            };
            IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Device.context.getApplicationContext().registerReceiver(BatteryLevelReceiver, batteryLevelFilter);
    	}
    }
    
    public synchronized void SetBatteryLevelAlarmer(TAlarmer Alarmer) {
    	BatteryLevelAlarmer = Alarmer;
    }
    
    public synchronized TAlarmer GetBatteryLevelAlarmer() {
    	return BatteryLevelAlarmer;
    }
    
    @Override
    public void Stop() throws Exception {
    	if (BatteryLevelReceiver != null) {
            Device.context.getApplicationContext().unregisterReceiver(BatteryLevelReceiver);
            BatteryLevelReceiver = null;
    	}
    	//.
    	super.Stop();
    }
    
    private void DoOnBatteryLevelChanged(short Percentage) {
    	short Level = (short)(((int)(Percentage/10))*10);
    	if (Level != LastLevel) {
        	TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value(OleDate.UTCCurrentTimestamp(),Level);
        	//. 
            TObjectSetComponentDataServiceOperation SO = new TObjectSetBatteryChargeValueSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
            ((TObjectSetBatteryChargeValueSO)SO).setValue(Value);
            try
            {
                Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
            }
            catch (Exception E) {}
            //.
            LastLevel = Level;
    	}
    	//.
    	if (Percentage != LastPercentage) {
    		TAlarmer BLA = GetBatteryLevelAlarmer();
    		if (BLA != null)
    			BLA.DoOnValue(Double.valueOf(Percentage));
    		//.
    		if ((Percentage <= CriticalBatteryLevel) && (CriticalBatteryLevel < LastPercentage)) {
            	Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
            	Device.BackupMonitor.BackupImmediate();
            }
            //.
            LastPercentage = Percentage;
    	}
    }
}
