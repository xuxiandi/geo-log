/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.geoscope.GeoLog.DEVICE.ConnectorModule;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Vector;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.geoscope.Classes.Data.Containers.TDataConverter;
import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.Network.TServerConnection;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt16Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.AlarmModule.TAlarmModule.TAlarmer;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographDataServer.TGeographDataServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.GeographProxyServer.TGeographProxyServerClient;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetAudioModuleAudioFilesValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetConnectorConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetDataStreamerStreamingComponentsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetFileSystemDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetGPIValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetGPSFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetGPSModuleConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetSensorDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetSensorsModuleChannelsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetSensorsModuleMeasurementsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetSensorsModuleMetersValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetVideoRecorderConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetVideoRecorderMeasurementDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetVideoRecorderMeasurementsListValueADSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetVideoRecorderMeasurementsListValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TLoadConfiguration1SO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectCheckpointSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetAlarmDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetBatteryChargeValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetConnectorServiceProviderSignalValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetControlsDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetDataStreamerActiveFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetFixMarkSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPIFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPIValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSModuleModeSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSModuleStatusSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOIDataFileSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOIJPEGImageSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetMapPOITextSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleDispatcherSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskResultSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGetTaskModuleTaskStatusSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOISO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetSensorsDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetTaskModuleDispatcherSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetTaskModuleTaskDataSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetUserAgentModuleUserIDValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderAudioFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderModeSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderRecordingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderSavingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderTransmittingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderVideoFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetAlarmModuleProfileDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetAudioModuleAudioFileMessageValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetAudioModuleAudioFilesValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetAudioModuleDestinationsVolumesValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetAudioModuleSourcesSensitivitiesValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetCheckpointIntervalSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetConnectorConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetDataStreamerActiveValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetDataStreamerStreamingComponentsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetFileSystemDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetGPOValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetGPSModuleConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetGeoDistanceThresholdSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetSensorDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetSensorsModuleChannelsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetSensorsModuleMeasurementsValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetSensorsModuleMetersValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetTaskModuleTaskResultSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetTaskModuleTaskStatusSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderActiveValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderAudioValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderMeasurementDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderModeValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderReceiversValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderRecordingValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderSDPValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderSavingServerValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderSavingValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderTransmittingValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetVideoRecorderVideoValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.OperationException;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceComponentByAddressDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceComponentServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataByAddressDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataByAddressDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TDeviceSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TGeographServerServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectGetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TObjectSetComponentDataServiceOperation;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.TOperationSession;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.OperationsBaseClasses.Security.TComponentUserAccessList;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
/**
 *
 * @author ALXPONOM
 */
@SuppressLint("HandlerLeak")
public class TConnectorModule extends TModule implements Runnable{
	
	public static String Folder() {
		return TDEVICEModule.DeviceFolder()+"/"+"ConnectorModule";
	}

	public static class TConfigurationSubscribers {
	
		public static abstract class TConfigurationSubscriber {
			
			protected abstract void DoOnConfigurationReceived();			
		}

		
		private ArrayList<TConfigurationSubscriber> Items = new ArrayList<TConfigurationSubscriber>();
		
		public TConfigurationSubscribers() {
		}
		
		public void Destroy() {
			ClearSubscribers();
		}
		
		private synchronized void ClearSubscribers() {
			Items.clear();
		}
		
		public synchronized void Subscribe(TConfigurationSubscriber Subscriber) {
			Items.add(Subscriber);
		}

		public synchronized void Unsubscribe(TConfigurationSubscriber Subscriber) {
			Items.remove(Subscriber);
		}

		public synchronized void DoOnConfigurationReceived() {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++)
				Items.get(I).DoOnConfigurationReceived();
		}
	}
	
	public static final int CONNECTION_TYPE_PLAIN 		= 0;
	public static final int CONNECTION_TYPE_SECURE_SSL 	= 1;
	
	public static String 		OutgoingSetOperationsQueueFolderName() { 
		return Folder()+"/"+"OutgoingSetOperationsQueue";
	}
	//.
	public static String 		OutgoingSetOperationsQueueFileName() {
		return OutgoingSetOperationsQueueFolderName()+"/"+"Data.data";
	}
	//.
	public static String 		OutgoingSetOperationsQueueDataFolderName() { 
		return OutgoingSetOperationsQueueFolderName()+"/"+"Data";
	}
	//.
    public static final int	GeographProxyServerDefaultPort = 2010;
    public static final int	GeographDataServerDefaultPort = 5000;
    //.
    public static final int OperationsGroupMaxSize = 8192; //. bytes
    private static final int ConnectTimeout = 1000*60; /*seconds*/
    private static final int DefaultReadTimeout = 1000*30; /*seconds*/
    private static final int ImmediateReconnectCount = 12;
    
    private static final int GarbageCollectingInterval = 1000*3600/*seconds*/;
    
    public class TOutgoingSetComponentDataOperationsQueue
    {
        public class OutOfCapacityException extends Exception
        {
 			private static final long serialVersionUID = 1L;

			public OutOfCapacityException()
            {
                super(Device.context.getString(R.string.SQueueIsFull));
            }
        } 
        
        public static final int QueueCapacity = 10000+1; 
        
        private TConnectorModule ConnectorModule;
        public TObjectSetComponentDataServiceOperation[] Queue = new TObjectSetComponentDataServiceOperation[QueueCapacity];
        public int QueueHead = 0;
        public int QueueTile = 0;
        public int QueueChangesCount = 0;
        
        public TOutgoingSetComponentDataOperationsQueue(TConnectorModule pConnectorModule) throws Exception
        {
            ConnectorModule = pConnectorModule;
            //.
    		File F = new File(OutgoingSetOperationsQueueFolderName());
    		if (!F.exists()) 
    			F.mkdirs();
    		F = new File(OutgoingSetOperationsQueueDataFolderName());
    		if (!F.exists()) 
    			F.mkdirs();
            //. load queue
    		try {
    			Load();
    		}
    		catch (Exception E) {
    			Device.Log.WriteError("ConnectorModule","error of loading outgoung queue: "+E.getMessage());
    		}
        }
        
        public void Destroy() throws Exception
        {
        	Save();
        }
        
        private synchronized void Load() throws Exception
        {
    		String FN = OutgoingSetOperationsQueueFileName();
    		File F = new File(FN);
    		if (!F.exists()) 
    			return; //. ->
    		//.
        	long FileSize = F.length();
        	FileInputStream FIS = new FileInputStream(FN);
        	try {
            		byte[] data = new byte[(int)FileSize];
        			FIS.read(data);
        			//.
                    TIndex Idx = new TIndex(0);
                    Saving_FromByteArray(data,/*ref*/ Idx);
            }
            finally
            {
            	FIS.close();
            }
        }
        
        public synchronized void Save() throws Exception {
    		byte[] data = Saving_ToByteArray();        	
    		Save(data);
        }
        
        public synchronized void Save(byte[] data) throws Exception
        {
    		String FN = OutgoingSetOperationsQueueFileName();
    		if (data != null)
    		{
    			String TFN = FN+".tmp";
    			FileOutputStream FOS = new FileOutputStream(TFN);
                try
                {
    				FOS.write(data);
    			}
    			finally
    			{
    				FOS.close();
    			}
    			File TF = new File(TFN);
    			File F = new File(FN);
    			TF.renameTo(F);
    		}
    		else {
    			File F = new File(FN);
    			F.delete();
    		}
        }
        
        public synchronized int QueueCount()
        {
            int R = (QueueTile-QueueHead);
            if (R < 0)
                R += QueueCapacity;
            return R;
        }
        
        public boolean IsEmpty() {
        	return (QueueCount() == 0);
        }
    
        public synchronized boolean IsBusy()
        {
            int QueuePos = QueueTile;
            QueuePos++;
            if (QueuePos == QueueCapacity)
                QueuePos = 0;
            return (QueuePos == QueueHead);
        }
    
        public synchronized TObjectSetComponentDataServiceOperation[] GetItems() {
            int Count = (QueueTile-QueueHead);
            if (Count < 0)
            	Count += QueueCapacity;
            if (Count == 0)
            	return null; //. ->
            TObjectSetComponentDataServiceOperation[] Result = new TObjectSetComponentDataServiceOperation[Count];
            Count = 0;
            int QueuePos = QueueHead;
            do {
            	Result[Count] = Queue[QueuePos];
            	Count++;
                //.
                QueuePos++;
                if (QueuePos == QueueCapacity)
                    QueuePos = 0;
            } while (QueuePos != QueueTile);
            return Result;
        }

        public synchronized void RemoveItem(int ItemHashCode) throws Exception {
            if (QueueHead == QueueTile)
                return; //. ->
            int QueuePos = QueueHead;
            do {
            	TObjectSetComponentDataServiceOperation Item = Queue[QueuePos];
            	if (Item.hashCode() == ItemHashCode) { 
                    int LastQueuePos;
                    while (true) {
                    	LastQueuePos = QueuePos;
                        //.
                        QueuePos++;
                        if (QueuePos == QueueCapacity)
                            QueuePos = 0;
                        //.
                        if (QueuePos == QueueTile) {
                        	QueueTile = LastQueuePos;
                        	break; //. >
                        }
                        Queue[LastQueuePos] = Queue[QueuePos];
                    };
                    //.
                	Item.Release();
                	//.
                	Save();
                	//.
            		return; // ->
            	}
                //.
                QueuePos++;
                if (QueuePos == QueueCapacity)
                    QueuePos = 0;
            } while (QueuePos != QueueTile);
        }
        
        public synchronized long GetMinimumOfOperationMaxTime()
        {
        	long Result = Long.MAX_VALUE;
            if (QueueHead == QueueTile)
                return Result; //. ->
            //.
            int QueuePos = QueueHead;
            do
            {
            	long MaxTime = Queue[QueuePos].GetQueueMaxTime();
            	if (MaxTime < Result)
            		Result = MaxTime;
                //.
                QueuePos++;
                if (QueuePos == QueueCapacity)
                    QueuePos = 0;
            }
            while (QueuePos != QueueTile);
            return Result;
        }
        
        public synchronized int GetComponentFileStreamCount() {
        	int Result = 0;
            if (QueueHead != QueueTile) {
                int QueuePos = QueueHead;
                do {
                	if (Queue[QueuePos].flComponentFileStream)
                		Result++;
                    //.
                    QueuePos++;
                    if (QueuePos == QueueCapacity)
                        QueuePos = 0;
                } while (QueuePos != QueueTile);
            }
            return Result;
        }
        
        public synchronized TObjectSetComponentDataServiceOperation GetOperationToProcess()
        {
            if (QueueHead == QueueTile)
                return null; //. ->
            //.
            TObjectSetComponentDataServiceOperation Result = Queue[QueueHead];
            Result.AddRef();
            return Result;
        }
        
        public synchronized Vector<Object> GetOperationsGroupToProcess(int MaxSize) throws Exception
        {
            if (QueueHead == QueueTile)
                return null; //. ->
            //.
            Vector<Object> Result = new Vector<Object>();
            int QueuePos = QueueHead;
            int Size = MaxSize;
            do
            {
                Size = Size-Queue[QueuePos].BatchSize();
                Result.addElement(Queue[QueuePos]);
                Queue[QueuePos].AddRef();
                //.
                if (Size <= 0)
                    break; //. >
                //.
                QueuePos++;
                if (QueuePos == QueueCapacity)
                    QueuePos = 0;
            }
            while (QueuePos != QueueTile);
            return Result;
        }
        
        public synchronized Vector<Object> GetOperationsGroupToProcess(long ToTime)
        {
            if (QueueHead == QueueTile)
                return null; //. ->
            //.
            Vector<Object> Result = new Vector<Object>();
            int QueuePos = QueueHead;
            do
            {
            	if (Queue[QueuePos].TimeStamp > ToTime)
            		break; //. >
            	//.
                Result.addElement(Queue[QueuePos]);
                Queue[QueuePos].AddRef();
                //.
                QueuePos++;
                if (QueuePos == QueueCapacity)
                    QueuePos = 0;
            }
            while (QueuePos != QueueTile);
            //.
            if (Result.size() != 0)
            	return Result; //. ->
            else
            	return null;
        }
        
        public synchronized void SkipOperation(TObjectSetComponentDataServiceOperation Operation)
        {
            if (QueueHead == QueueTile)
                return ; //. ->
            if (Queue[QueueHead] == Operation)
            {
                Queue[QueueHead].Release();
                Queue[QueueHead] = null;
                //.
                QueueHead++;
                if (QueueHead == QueueCapacity)
                    QueueHead = 0;
                QueueChangesCount++;
            }
        }
        
        public synchronized void SkipOperationsGroup(Vector<Object> Operations)
        {
            for (int I = 0; I < Operations.size(); I++)
            {
                if (QueueHead == QueueTile)
                    return ; //. ->
                if (Queue[QueueHead] == Operations.elementAt(I))
                {
                    Queue[QueueHead].Release();
                    Queue[QueueHead] = null;
                    //.
                    QueueHead++;
                    if (QueueHead == QueueCapacity)
                        QueueHead = 0;
                    QueueChangesCount++;
                }
            }
        }
        
        public synchronized void AddNewOperation(TObjectSetComponentDataServiceOperation pOperation) throws Exception
        {
            if (QueueHead != QueueTile)
            {
                //. try to insert into the last operation queue
                int LastOperationIndex = QueueTile-1;
                if (LastOperationIndex < 0)
                    LastOperationIndex+=QueueCapacity;
                if ((Queue[LastOperationIndex].RefCount() == 1) && (!Queue[LastOperationIndex].IsCancel()) && Queue[LastOperationIndex].IsAddressTheSame(pOperation.Address()) && Queue[LastOperationIndex].IsAddressDataTheSame(pOperation.AddressData))
                {
                    if (Queue[LastOperationIndex].AddNewValue(pOperation.getValue())) {
                        QueueChangesCount++;
                        return; //. ->
                    }
                }   
            }
            int NextOperationIndex = QueueTile+1;
            if (NextOperationIndex == QueueCapacity)
                NextOperationIndex = 0;
            if (NextOperationIndex == QueueHead)
            {
                //. skip most old operation
                Queue[QueueHead].Release();
                Queue[QueueHead] = null;
                //.
                QueueHead++;
                if (QueueHead == QueueCapacity)
                    QueueHead = 0;
            }
            Queue[QueueTile] = pOperation;
            Queue[QueueTile].AddRef();
            QueueTile = NextOperationIndex;
            QueueChangesCount++;
        }
        
        public synchronized void AddNewOperation(TObjectSetComponentDataServiceOperation pOperation, TObjectSetComponentDataServiceOperation pOperation1) throws Exception
        {
            AddNewOperation(pOperation);
            AddNewOperation(pOperation1);
        }
        
        public synchronized void AddNewOperation(TObjectSetComponentDataServiceOperation pOperation, TObjectSetComponentDataServiceOperation pOperation1, TObjectSetComponentDataServiceOperation pOperation2) throws Exception
        {
            AddNewOperation(pOperation);
            AddNewOperation(pOperation1);
            AddNewOperation(pOperation2);
        }
        
        public synchronized void Clear() throws Exception
        {
            while (QueueHead != QueueTile)
            {
                Queue[QueueHead].Release();
                Queue[QueueHead] = null;
                //.
                QueueHead++;
                if (QueueHead == QueueCapacity)
                    QueueHead = 0;
                QueueChangesCount++;
            }
            Save();
        }
        
        public synchronized int GetQueueChangesCount() {
        	return QueueChangesCount;
        }
        
        public synchronized void Saving_FromByteArray(byte[] BA, TIndex Idx) throws Exception
        {
            int OpCount = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
            if (OpCount > (QueueCapacity-1))
                OpCount = QueueCapacity-1;
            QueueHead = 0;
            QueueTile = 0;
            for (int I = 0; I < OpCount; I++)
            {
                int OpSize = TGeographServerServiceOperation.ConvertBEByteArrayToInt32(BA,Idx.Value); Idx.Value+=4;
                if (OpSize > 0)
                {
                    int IdxValue = Idx.Value;
                    short[] Address = TComponentServiceOperation.GetAddress(BA,/*ref*/ Idx);
                    byte[] AddressData = TComponentServiceOperation.GetAddressData(BA,/*ref*/ Idx);
                    TObjectSetComponentDataServiceOperation SO = GetObjectSetComponentDataServiceOperation(Address,ConnectorModule.Device.ObjectID);
                    if (SO != null) {
                    	SO.AddressData = AddressData;
                        SO.Saving_FromByteArray(BA,/*ref*/ Idx);
                        if (SO.ValueCount() > 0) {
                            Queue[QueueTile] = SO;
                            Queue[QueueTile].AddRef();
                            QueueTile++;
                        }
                    }
                    Idx.Value = IdxValue+OpSize;
                }   
            }
        }
    
        public synchronized byte[] Saving_ToByteArray() throws Exception
        {
            if (QueueHead == QueueTile)
                return null; //. ->
        	int _QueueHead = QueueHead;
            Vector<Object> OpList = new Vector<Object>();
            int DataSize = 0;
            byte[] BA;
            do
            {
                try
                {
                	if (!Queue[_QueueHead].IsCancelled()) 
                	{
                        BA = Queue[_QueueHead].Saving_ToByteArray();
                        OpList.addElement(BA);
                        DataSize += (4/*SizeOf(Size)*/+BA.length);
                	}
                }
                catch (IOException E) {}
                catch (OperationException E) {}
                //.
                _QueueHead++;
                if (_QueueHead == QueueCapacity)
                    _QueueHead = 0;
            } while (_QueueHead != QueueTile);
            int OpCount = OpList.size();
            byte[] Result = new byte[4/*SizeOf(OpCount)*/+DataSize];
            int Idx = 0;
            BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(OpCount);
            System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
            if (OpCount > 0)
            {
                int OpSize;
                byte[] OpData;
                for (int I = 0; I < OpCount; I++)
                {
                    OpData = (byte[])OpList.elementAt(I);
                    OpSize = OpData.length;
                    BA = TGeographServerServiceOperation.ConvertInt32ToBEByteArray(OpSize);
                    System.arraycopy(BA,0,Result,Idx,BA.length); Idx+=BA.length;
                    if (OpSize > 0) {
                        System.arraycopy(OpData,0,Result,Idx,OpData.length); Idx+=OpData.length;
                    }
                }
            }
            return Result;
        }
           
        public TObjectSetComponentDataServiceOperation GetObjectSetComponentDataServiceOperation(short[] Address, int ObjectID)
        {
            TElementAddress SubAddress = new TElementAddress();
            if (TObjectSetGPSFixSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGPSFixSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGPSModuleModeSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGPSModuleModeSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGPSModuleStatusSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGPSModuleStatusSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGPIValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGPIValueSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGPIFixSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGPIFixSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetMapPOISO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetMapPOISO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGetMapPOITextSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGetMapPOITextSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGetMapPOIJPEGImageSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGetMapPOIJPEGImageSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGetTaskModuleTaskDataSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGetTaskModuleTaskDataSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGetTaskModuleTaskStatusSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGetTaskModuleTaskStatusSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGetTaskModuleTaskResultSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGetTaskModuleTaskResultSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGetTaskModuleDispatcherSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGetTaskModuleDispatcherSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGetMapPOIDataFileSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGetMapPOIDataFileSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetFixMarkSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetFixMarkSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetBatteryChargeValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetBatteryChargeValueSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetConnectorServiceProviderSignalValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetConnectorServiceProviderSignalValueSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetTaskModuleDispatcherSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetTaskModuleDispatcherSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetTaskModuleTaskDataSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetTaskModuleTaskDataSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetVideoRecorderModeSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetVideoRecorderModeSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetVideoRecorderRecordingFlagSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetVideoRecorderRecordingFlagSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetVideoRecorderAudioFlagSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetVideoRecorderAudioFlagSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetVideoRecorderVideoFlagSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetVideoRecorderVideoFlagSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetVideoRecorderTransmittingFlagSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetVideoRecorderTransmittingFlagSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetVideoRecorderSavingFlagSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetVideoRecorderSavingFlagSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetDataStreamerActiveFlagSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetDataStreamerActiveFlagSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetControlsDataSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetControlsDataSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetSensorsDataSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetSensorsDataSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetAlarmDataSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetAlarmDataSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            else 
                return null;
        } 
    }
    
    public class TOutgoingGetComponentDataOperationsQueue
    {
        public class OutOfCapacityException extends Exception
        {
 			private static final long serialVersionUID = 2L;

			public OutOfCapacityException()
            {
                super(Device.context.getString(R.string.SQueueIsFull));
            }
        } 
        
        public static final int QueueCapacity = 10+1; 
        
        @SuppressWarnings("unused")
		private TConnectorModule ConnectorModule;
        public TObjectGetComponentDataServiceOperation[] Queue = new TObjectGetComponentDataServiceOperation[QueueCapacity];
        public int QueueHead = 0;
        public int QueueTile = 0;
        
        public TOutgoingGetComponentDataOperationsQueue(TConnectorModule pConnectorModule)
        {
            ConnectorModule = pConnectorModule;
        }
        
        public void Destroy()
        {
        }
        
        public synchronized int QueueCount()
        {
            int R = (QueueTile-QueueHead);
            if (R < 0)
                R += QueueCapacity;
            return R;
        }
    
        public synchronized boolean IsBusy()
        {
            int QueuePos = QueueTile;
            QueuePos++;
            if (QueuePos == QueueCapacity)
                QueuePos = 0;
            return (QueuePos == QueueHead);
        }
    
        public synchronized TObjectGetComponentDataServiceOperation GetOperationToProcess()
        {
            if (QueueHead == QueueTile)
                return null; //. ->
            //.
            TObjectGetComponentDataServiceOperation Result = Queue[QueueHead];
            Result.AddRef();
            return Result;
        }
        
        public synchronized void SkipOperation(TObjectGetComponentDataServiceOperation Operation)
        {
            if (QueueHead == QueueTile)
                return ; //. ->
            if (Queue[QueueHead] == Operation)
            {
                Queue[QueueHead].Release();
                Queue[QueueHead] = null;
                //.
                QueueHead++;
                if (QueueHead == QueueCapacity)
                    QueueHead = 0;
            }
        }
        
        public synchronized void AddNewOperation(TObjectGetComponentDataServiceOperation pOperation) throws OutOfCapacityException
        {
            int NextOperationIndex = QueueTile+1;
            if (NextOperationIndex == QueueCapacity)
                NextOperationIndex = 0;
            if (NextOperationIndex == QueueHead)
            	throw new OutOfCapacityException(); //. =>
            Queue[QueueTile] = pOperation;
            Queue[QueueTile].AddRef();
            QueueTile = NextOperationIndex;
        }
        
        public synchronized void AddNewOperation(TObjectGetComponentDataServiceOperation pOperation, TObjectGetComponentDataServiceOperation pOperation1) throws OutOfCapacityException
        {
            AddNewOperation(pOperation);
            AddNewOperation(pOperation1);
        }
        
        public synchronized void AddNewOperation(TObjectGetComponentDataServiceOperation pOperation, TObjectGetComponentDataServiceOperation pOperation1, TObjectGetComponentDataServiceOperation pOperation2) throws OutOfCapacityException
        {
            AddNewOperation(pOperation);
            AddNewOperation(pOperation1);
            AddNewOperation(pOperation2);
        }
    }
    
	//. virtual values
	public TConnectorModuleConfigurationDataValue ConfigurationDataValue;
	//. main connection server
    public boolean 	flServerConnectionEnabled = false;
    public String 	ServerAddress = "127.0.0.1";
    public int		ServerPort = 8282;
	public int 		SecureServerPortShift = 2;
	public int		SecureServerPort() {
    	return (ServerPort+SecureServerPortShift);
    }
    //. Geograph proxy server
    public String 									GeographProxyServerAddress = null;
    public int										GeographProxyServerPort = 0;
    private TGeographProxyServerClient.TServerInfo 	GeographProxyServerInfo = null;
    //. Geograph data server
    public String 									GeographDataServerAddress = null;
    public int										GeographDataServerPort = 0;
    private TGeographDataServerClient.TServerInfo 	GeographDataServerInfo = null;
    //.
    public int 		LoopSleepTime = 1*1000; //. milliseconds
    public int 		TransmitInterval = 0; //. in seconds
    public boolean 	OutgoingSetComponentDataOperationsQueue_flEnabled = true;
    //.
    private Thread 	thread = null;
    private boolean flTerminated = false;
    //.
    public int			ConnectionType() {
    	return (TServerConnection.flSecureConnection ? CONNECTION_TYPE_SECURE_SSL : CONNECTION_TYPE_PLAIN);
    }
    public Socket 		Connection;
    public InputStream 	ConnectionInputStream;
    public OutputStream ConnectionOutputStream;
    //.
    public boolean 		flPause = false;
    public boolean 		flPaused = false;
    //.
    public boolean 		flReconnect = false;
    //.
    public boolean 		flProcessing = false;
    public boolean 		flProcessingOperation = false;
    private Exception 	ProcessException = null;
    private Exception 	ProcessOutgoingOperationException = null;
    public TOutgoingSetComponentDataOperationsQueue OutgoingSetComponentDataOperationsQueue;
    public TOutgoingGetComponentDataOperationsQueue OutgoingGetComponentDataOperationsQueue;
    private int ImmediateTransmiteOutgoingSetComponentDataOperationsCounter = 0;
    public TProcessIncomingOperationResult ProcessIncomingOperationResult = new TProcessIncomingOperationResult();
    public TComponentInt16Value CheckpointInterval = null;
    private long LastCheckpointTime;
    private long LastGarbageCollectorLaunchingTime;
    //. connector signal condition listener
    public TConnectorStateListener ConnectorStateListener = new TConnectorStateListener();
    private TelephonyManager _TelephonyManager;
    //.
    public TConfigurationSubscribers ConfigurationSubscribers;
    
    public TConnectorModule(TDEVICEModule pDevice) throws Exception
    {
    	super(pDevice);
    	//.
        ModuleState = MODULE_STATE_INITIALIZING;
    	//.
        Device = pDevice;
        //.
		File F = new File(Folder());
		if (!F.exists()) 
			F.mkdirs();
        //.
    	try {
			LoadProfile();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SConnectorModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
        CheckpointInterval = new TComponentInt16Value();
        CheckpointInterval.SetValue((short)60); //. default checkpoint interval, in seconds
    	//. virtual values
        ConfigurationDataValue = new TConnectorModuleConfigurationDataValue(this);
        //.
        OutgoingSetComponentDataOperationsQueue = new TOutgoingSetComponentDataOperationsQueue(this);
        OutgoingGetComponentDataOperationsQueue = new TOutgoingGetComponentDataOperationsQueue(this);
        //.
        ConfigurationSubscribers = new TConfigurationSubscribers();
        //.
        ModuleState = MODULE_STATE_INITIALIZED;
    }
    
    public void Destroy() throws Exception
    {
    	ModuleState = MODULE_STATE_FINALIZING;
        //.
    	Stop();
    	//.
        if (OutgoingGetComponentDataOperationsQueue != null)
        {
            OutgoingGetComponentDataOperationsQueue.Destroy();
            OutgoingGetComponentDataOperationsQueue = null;
        }
        //.
        if (OutgoingSetComponentDataOperationsQueue != null)
        {
            OutgoingSetComponentDataOperationsQueue.Destroy();
            OutgoingSetComponentDataOperationsQueue = null;
        }
        //.
        ModuleState = MODULE_STATE_FINALIZED;
    }
    
    @Override
    public void Start() throws Exception {
    	if (ModuleState == MODULE_STATE_RUNNING)
    		return; //. ->
    	//.
    	super.Start();
    	//.
    	if (IsEnabled()) {
            _TelephonyManager = (TelephonyManager)Device.context.getSystemService(Context.TELEPHONY_SERVICE);
            _TelephonyManager.listen(ConnectorStateListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            //.
        	if (flServerConnectionEnabled && (ServerPort > 0))
            	StartConnection();
	        //.
        	ModuleState = MODULE_STATE_RUNNING;
    	}
    }
    
    @Override
    public void Stop() throws Exception {
    	if (ModuleState != MODULE_STATE_RUNNING)
    		return; //. ->
    	//.
    	StopConnection();
    	//.
    	if (_TelephonyManager != null) { 
    		_TelephonyManager.listen(ConnectorStateListener,PhoneStateListener.LISTEN_NONE);
    		_TelephonyManager = null;
    	}
    	//.
    	super.Stop();
    	//.
    	ModuleState = MODULE_STATE_NOTRUNNING;
    	//. post processing
    	OutgoingSetComponentDataOperationsQueue.Save();
    }
    
    @Override
    public synchronized void LoadProfile() throws Exception {
		String CFN = ModuleFile();
		File F = new File(CFN);
		if (!F.exists()) 
			return; //. ->
		//.
		byte[] XML;
    	long FileSize = F.length();
    	FileInputStream FIS = new FileInputStream(CFN);
    	try {
    		XML = new byte[(int)FileSize];
    		FIS.read(XML);
    	}
    	finally {
    		FIS.close();
    	}
    	Document XmlDoc;
		ByteArrayInputStream BIS = new ByteArrayInputStream(XML);
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();      
			factory.setNamespaceAware(true);     
			DocumentBuilder builder = factory.newDocumentBuilder(); 			
			XmlDoc = builder.parse(BIS); 
		}
		finally {
			BIS.close();
		}
		Element RootNode = XmlDoc.getDocumentElement();
		//. todo: Node VideoRecorderModuleNode = RootNode.getElementsByTagName("GPSModule").item(0);
		int Version = 1; //. todo
		switch (Version) {
		case 1:
			try {
				Node node = RootNode.getElementsByTagName("flServerConnectionEnabled").item(0).getFirstChild();
				if (node != null)
					flServerConnectionEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
				node = RootNode.getElementsByTagName("ServerAddress").item(0).getFirstChild();
				if (node != null)
					ServerAddress = node.getNodeValue();
				node = RootNode.getElementsByTagName("ServerPort").item(0).getFirstChild();
				if (node != null)
					ServerPort = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("LoopSleepTime").item(0).getFirstChild();
				if (node != null)
					LoopSleepTime = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("TransmitInterval").item(0).getFirstChild();
				if (node != null)
					TransmitInterval = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("flOutgoingSetOperationsQueueIsEnabled").item(0).getFirstChild();
				if (node != null)
					OutgoingSetComponentDataOperationsQueue_flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
			}
			catch (Exception E) {
			}
			break; //. >
			
		case 2:
			try {
				Node node = RootNode.getElementsByTagName("flServerConnectionEnabled").item(0).getFirstChild();
				if (node != null)
					flServerConnectionEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
				node = RootNode.getElementsByTagName("ServerAddress").item(0).getFirstChild();
				if (node != null)
					ServerAddress = node.getNodeValue();
				node = RootNode.getElementsByTagName("ServerPort").item(0).getFirstChild();
				if (node != null)
					ServerPort = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("GeographProxyServerAddress").item(0).getFirstChild();
				if (node != null)
					GeographProxyServerAddress = node.getNodeValue();
				node = RootNode.getElementsByTagName("GeographProxyServerPort").item(0).getFirstChild();
				if (node != null)
					GeographProxyServerPort = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("GeographDataServerAddress").item(0).getFirstChild();
				if (node != null)
					GeographDataServerAddress = node.getNodeValue();
				node = RootNode.getElementsByTagName("GeographDataServerPort").item(0).getFirstChild();
				if (node != null)
					GeographDataServerPort = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("LoopSleepTime").item(0).getFirstChild();
				if (node != null)
					LoopSleepTime = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("TransmitInterval").item(0).getFirstChild();
				if (node != null)
					TransmitInterval = Integer.parseInt(node.getNodeValue());
				node = RootNode.getElementsByTagName("flOutgoingSetOperationsQueueIsEnabled").item(0).getFirstChild();
				if (node != null)
					OutgoingSetComponentDataOperationsQueue_flEnabled = (Integer.parseInt(node.getNodeValue()) != 0);
			}
			catch (Exception E) {
			}
			break; //. >
			
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
		return; 
    }
    
    @Override
	public synchronized void SaveProfileTo(XmlSerializer Serializer) throws Exception {
		int Version = 2;
        Serializer.startTag("", "ConnectorModule");
        //. Version
        Serializer.startTag("", "Version");
        Serializer.text(Integer.toString(Version));
        Serializer.endTag("", "Version");
        //. 
        int V = 0;
        if (flServerConnectionEnabled)
        	V = 1;
        Serializer.startTag("", "flServerConnectionEnabled");
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "flServerConnectionEnabled");
        //. 
        Serializer.startTag("", "ServerAddress");
        Serializer.text(ServerAddress);
        Serializer.endTag("", "ServerAddress");
        //. 
        Serializer.startTag("", "ServerPort");
        Serializer.text(Integer.toString(ServerPort));
        Serializer.endTag("", "ServerPort");
        //. 
        Serializer.startTag("", "GeographProxyServerAddress");
        if (GeographProxyServerAddress != null)
        	Serializer.text(GeographProxyServerAddress);
        Serializer.endTag("", "GeographProxyServerAddress");
        //. 
        Serializer.startTag("", "GeographProxyServerPort");
        Serializer.text(Integer.toString(GeographProxyServerPort));
        Serializer.endTag("", "GeographProxyServerPort");
        //. 
        Serializer.startTag("", "GeographDataServerAddress");
        if (GeographDataServerAddress != null)
        	Serializer.text(GeographDataServerAddress);
        Serializer.endTag("", "GeographDataServerAddress");
        //. 
        Serializer.startTag("", "GeographDataServerPort");
        Serializer.text(Integer.toString(GeographDataServerPort));
        Serializer.endTag("", "GeographDataServerPort");
        //. 
        Serializer.startTag("", "LoopSleepTime");
        Serializer.text(Integer.toString(LoopSleepTime));
        Serializer.endTag("", "LoopSleepTime");
        //. 
        Serializer.startTag("", "TransmitInterval");
        Serializer.text(Integer.toString(TransmitInterval));
        Serializer.endTag("", "TransmitInterval");
        //.
        V = 0;
        if (OutgoingSetComponentDataOperationsQueue_flEnabled)
        	V = 1;
        Serializer.startTag("", "flOutgoingSetOperationsQueueIsEnabled");
        Serializer.text(Integer.toString(V));
        Serializer.endTag("", "flOutgoingSetOperationsQueueIsEnabled");
        //. 
        Serializer.endTag("", "ConnectorModule");
    }
    
    public String GetGeographProxyServerAddress() {
    	if (GeographProxyServerAddress == null)
    		return ServerAddress; //. ->
    	return GeographProxyServerAddress;
    }
    
    public int GetGeographProxyServerPort() {
    	if (GeographProxyServerPort == 0)
    		return GeographProxyServerDefaultPort; //. ->
    	return GeographProxyServerPort;
    }
    
    public TGeographProxyServerClient.TServerInfo GetGeographProxyServerInfo() throws Exception {
    	if (GeographProxyServerInfo == null) {
    		TGeographProxyServerClient GPSC = new TGeographProxyServerClient(GetGeographProxyServerAddress(),GetGeographProxyServerPort(), Device.UserID,Device.UserPassword, Device.idGeographServerObject);
    		GeographProxyServerInfo = GPSC.GetServerInfo();
    	}
    	return GeographProxyServerInfo;
    }
    
    public String GetGeographDataServerAddress() {
    	if (GeographDataServerAddress == null)
    		return ServerAddress; //. ->
    	return GeographDataServerAddress;
    }
    
    public int GetGeographDataServerPort() {
    	if (GeographDataServerPort == 0)
    		return GeographDataServerDefaultPort; //. ->
    	return GeographDataServerPort;
    }
    
    public TGeographDataServerClient.TServerInfo GetGeographDataServerInfo() throws Exception {
    	return GeographDataServerInfo;
    }
    
    private void StartConnection()
    {
        if (thread != null)
            return; //. ->
        flTerminated = false;
        thread = new Thread(this);
        thread.start();
    }
    
    private void StopConnection() 
    {
        if (thread == null)
            return; //. ->
        flTerminated = true;
        try
        {
        	thread.interrupt();
            thread.join();
        }
        catch (InterruptedException E) {}
        thread = null;
    }
    
    public boolean IsActive() {
    	return (thread != null);
    }
    
    public boolean IsSecure() {
    	switch (ConnectionType()) {
    	
    	case CONNECTION_TYPE_PLAIN:
    		return false; //. ->
    		
    	case CONNECTION_TYPE_SECURE_SSL: 
    		return true; //. ->
    		
    	default:
    		return false; //. ->
    	}
    }
    
    private void PlainConnect() throws IOException {
    	SocketAddress SA = new InetSocketAddress(ServerAddress,ServerPort); 
        Connection = new Socket();
        Connection.connect(SA,ConnectTimeout);
        Connection.setSoTimeout(DefaultReadTimeout);
        Connection.setKeepAlive(true);
        Connection.setSendBufferSize(10000);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
        //.
		Device.Log.WriteInfo("ConnectorModule","connected.");
    }
    
    private void SecureSSLConnect() throws Exception {
		TrustManager[] _TrustAllCerts = new TrustManager[] { new javax.net.ssl.X509TrustManager() {
	        @Override
	        public void checkClientTrusted( final X509Certificate[] chain, final String authType ) {
	        }
	        @Override
	        public void checkServerTrusted( final X509Certificate[] chain, final String authType ) {
	        }
	        @Override
	        public X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	    } };
	    //. install the all-trusting trust manager
	    SSLContext sslContext = SSLContext.getInstance("SSL");
	    sslContext.init( null, _TrustAllCerts, new SecureRandom());
	    //. create a ssl socket factory with our all-trusting manager
	    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
	    Connection = (SSLSocket)sslSocketFactory.createSocket(ServerAddress,SecureServerPort());
        Connection.setSoTimeout(DefaultReadTimeout);
        Connection.setKeepAlive(true);
        Connection.setSendBufferSize(10000);
        ConnectionInputStream = Connection.getInputStream();
        ConnectionOutputStream = Connection.getOutputStream();
        //.
		Device.Log.WriteInfo("ConnectorModule","connected using SSL.");
    }
    
	private final int Connect_TryCount = 3;
	
    private void Connect() throws Exception {
		int TryCounter = Connect_TryCount;
		while (true) {
			try {
				try {
					//. connect
			    	switch (ConnectionType()) {
			    	
			    	case CONNECTION_TYPE_PLAIN:
			    		PlainConnect();
			    		break; //. >
			    		
			    	case CONNECTION_TYPE_SECURE_SSL:
			    		SecureSSLConnect();
			    		break; //. >
			    		
			    	default:
			    		throw new Exception("unknown connection type"); //. =>
			    	}
					break; //. >
				} catch (SocketTimeoutException STE) {
					throw new IOException(Device.context.getString(R.string.SConnectionTimeoutError)); //. =>
				} catch (ConnectException CE) {
					throw new ConnectException(Device.context.getString(R.string.SNoServerConnection)); //. =>
				} catch (Exception E) {
					String S = E.getMessage();
					if (S == null)
						S = E.toString();
					throw new Exception(Device.context.getString(R.string.SHTTPConnectionError)+S); //. =>
				}
			}
			catch (Exception E) {
				TryCounter--;
				if (TryCounter == 0)
					throw E; //. =>
			}
		}
    }
    
    private void Disconnect(Throwable _Exception) {
    	if (Connection == null)
    		return; //. ->
    	try {
            //. close connection gracefully
        	if ((_Exception == null) || (!((_Exception instanceof OperationException) && ((OperationException)_Exception).IsConnectionError()))) {
                byte[] BA = TDataConverter.ConvertInt32ToLEByteArray(TGeographServerServiceOperation.Descriptor_ConnectionIsClosing);
                ConnectionOutputStream.write(BA);
                ConnectionOutputStream.flush();
        	}
            //.
            ConnectionOutputStream.close();
            ConnectionInputStream.close();
            Connection.close();
            //.
            Connection = null;
            //.
        	if (_Exception == null)
        		Device.Log.WriteInfo("ConnectorModule","disconnected.");
        	else
        		Device.Log.WriteInfo("ConnectorModule","disconnected on exception, "+_Exception.toString());
    	}
    	catch (Throwable E) {
    		Device.Log.WriteError("ConnectorModule","exception while disconnecting, "+E.getMessage());
    	}
    }
    
    public void Pause() throws InterruptedException {
    	flPause = true;
    	//.
    	while (IsActive() && (!flPaused)) {
    		Reconnect();
        	Thread.sleep(LoopSleepTime);
    	}
    }
    
    public void Resume() {
    	flPause = false;
    }

    public boolean IsPaused() {
    	return flPaused;
    }
    
    public void Reconnect() {
    	flReconnect = true;
    }
    
    public void ForceReconnect() {
    	Reconnect();
    	//.
    	if (Connection != null) 
            try {
            	if (ConnectionOutputStream != null)
            		ConnectionOutputStream.close();
            	if (ConnectionInputStream != null)
            		ConnectionInputStream.close();
            	//.
	            Connection.close();
	            //.
	            Connection = null;
			} catch (Exception E) {
			}
    }
    
    private void SetProcessException(Exception E)
    {
        ProcessException = E;
    }
    
    public Exception GetProcessException()
    {
        if (ProcessException == null)
            return null; //. ->
        Exception R = ProcessException;
        ProcessException = null;
        return R;
    }
    
    private void SetProcessOutgoingOperationException(Exception E)
    {
        ProcessOutgoingOperationException = E;
    }
    
    public Exception GetProcessOutgoingOperationException()
    {
        if (ProcessOutgoingOperationException == null)
            return null; //. ->
        Exception R = ProcessOutgoingOperationException;
        ProcessOutgoingOperationException = null;
        return R;
    }
    
    public int PendingOperationsCount()
    {
        return OutgoingSetComponentDataOperationsQueue.QueueCount();
    }
    
    public synchronized void ImmediateTransmiteOutgoingSetComponentDataOperations() {
    	ImmediateTransmiteOutgoingSetComponentDataOperationsCounter += 2;
    }
    
    private synchronized int ImmediateTransmiteOutgoingSetComponentDataOperationsCounter_GetValue() {
    	return ImmediateTransmiteOutgoingSetComponentDataOperationsCounter;
    }
    
    private synchronized void ImmediateTransmiteOutgoingSetComponentDataOperationsCounter_Release() {
    	if (ImmediateTransmiteOutgoingSetComponentDataOperationsCounter > 0)
    		ImmediateTransmiteOutgoingSetComponentDataOperationsCounter--;
    }
    
    private void ProcessSetOperations(Vector<Object> SetOperations) throws Exception {
        try {
            if (SetOperations.size() > 1) {
                    int SummaryCompletionTime = 0;
                    //. start operations group
                    for (int I = 0; I < SetOperations.size(); I++) {
                    	TObjectSetComponentDataServiceOperation CurrentSetOperation = (TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I);
                    	if (!CurrentSetOperation.IsCancelled())
                    		SummaryCompletionTime += CurrentSetOperation.StartOutgoingOperation(ConnectionOutputStream);
                    }
                    //. wait and finish operations completion
                    int[] RCs = new int[SetOperations.size()];
                    for (int I = 0; I < SetOperations.size(); I++) {
                    	TObjectSetComponentDataServiceOperation CurrentSetOperation = (TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I);
                    	if (!CurrentSetOperation.IsCancelled()) {
                            int RC = CurrentSetOperation.FinishOutgoingOperation(Connection,ConnectionInputStream,ConnectionOutputStream,(I == 0 ? SummaryCompletionTime : TGeographServerServiceOperation.Connection_DataWaitingInterval));
                            RCs[I] = RC;
                            if (RC < 0) {
                                OperationException E = new OperationException(RC,"error of operation: '"+CurrentSetOperation.Name+"', code: "+Integer.toString(RC).toString());
                                if (E.IsCommunicationError())
                                    throw E; //. =>
                                else
                                    SetProcessOutgoingOperationException(E);
                            }
                    	}
                    }
                    //. process completion actions
                    for (int I = 0; I < SetOperations.size(); I++) {
                    	TObjectSetComponentDataServiceOperation CurrentSetOperation = (TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I);
                    	if (!CurrentSetOperation.IsCancelled()) {
                            if (RCs[I] >= 0) {
                            	int RC = CurrentSetOperation.DoOnOperationCompletion();
                            	if (RC < 0) {
                                    OperationException E = new OperationException(RC,"error of operation completion: '"+CurrentSetOperation.Name+"', code: "+Integer.toString(RC).toString());
                                    if (E.IsCommunicationError())
                                        throw E; //. =>
                                    else {
                                    	CurrentSetOperation.DoOnOperationException(E);
                                    	SetProcessOutgoingOperationException(E);
                                    }
                            	}
                            }
                            else {
                                OperationException E = new OperationException(RCs[I],"error of operation: '"+CurrentSetOperation.Name+"', code: "+Integer.toString(RCs[I]).toString());
                                if (E.IsCommunicationError())
                                    throw E; //. =>
                                else {
                                	CurrentSetOperation.DoOnOperationException(E);
                                	SetProcessOutgoingOperationException(E);
                                }
                            }
                    	}
                    }
                    //. check and execute delayed concurrent incoming operation from the server (should be one operation)
                    for (int I = 0; I < SetOperations.size(); I++) 
                    {
                        TObjectSetComponentDataServiceOperation CurrentSetOperation = (TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I);
                    	if (!CurrentSetOperation.IsCancelled()) {
                            if (CurrentSetOperation.ConcurrentOperationSessionID != 0)
                                ProcessIncomingOperation(CurrentSetOperation.ConcurrentOperationSessionID,CurrentSetOperation.ConcurrentOperationMessage,CurrentSetOperation.ConcurrentOperationMessageOrigin, null, ConnectionInputStream,ConnectionOutputStream, ProcessIncomingOperationResult);
                    	}
                    }
                    //. remove current operations from queue
                    OutgoingSetComponentDataOperationsQueue.SkipOperationsGroup(SetOperations);
            }
            else {
                TObjectSetComponentDataServiceOperation CurrentSetOperation = (TObjectSetComponentDataServiceOperation)SetOperations.elementAt(0);
            	if (!CurrentSetOperation.IsCancelled()) {
                    int RC = CurrentSetOperation.ProcessOutgoingOperation(Connection,ConnectionInputStream,ConnectionOutputStream);
                    if (RC >= 0) {
                    	RC = CurrentSetOperation.DoOnOperationCompletion();
                    	if (RC < 0) {
                            OperationException E = new OperationException(RC,"error of operation completion: '"+CurrentSetOperation.Name+"', code: "+Integer.toString(RC).toString());
                            if (E.IsCommunicationError())
                                throw E; //. =>
                            else {
                            	CurrentSetOperation.DoOnOperationException(E);
                                SetProcessOutgoingOperationException(E);
                            }
                    	}
                    }
                    else {
                        OperationException E = new OperationException(RC,"error of operation: '"+CurrentSetOperation.Name+"', code: "+Integer.toString(RC).toString());
                        if (E.IsCommunicationError())
                            throw E; //. =>
                        else {
                        	CurrentSetOperation.DoOnOperationException(E);
                            SetProcessOutgoingOperationException(E);
                        }
                    }
            	}
                //. remove current operation from queue
                OutgoingSetComponentDataOperationsQueue.SkipOperation(CurrentSetOperation);
            }
        }
        finally {
            for (int I = 0; I < SetOperations.size(); I++)
                ((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).Release();
        }
    }
    
    private void ProcessGetOperation(TObjectGetComponentDataServiceOperation GetOperation) throws Exception {
        try {
        	if (!GetOperation.IsCancelled()) {
        		int RC = GetOperation.ProcessOutgoingOperation(Connection,ConnectionInputStream,ConnectionOutputStream);
                if (RC >= 0) {
                	RC = GetOperation.DoOnOperationCompletion();
                	if (RC < 0) {
                        OperationException E = new OperationException(RC,"error of operation completion: '"+GetOperation.Name+"', code: "+Integer.toString(RC).toString());
                        if (E.IsCommunicationError())
                            throw E; //. =>
                        else {
                        	GetOperation.DoOnOperationException(E);
                            SetProcessOutgoingOperationException(E);
                        }
                	}
                }
                else {
                    OperationException E = new OperationException(RC,"error of operation: '"+GetOperation.Name+"', code: "+Integer.toString(RC).toString());
                    if (E.IsCommunicationError())
                        throw E; //. =>
                    else {
                    	GetOperation.DoOnOperationException(E);
                        SetProcessOutgoingOperationException(E);
                    }
                }
        	}
            //. remove current operation from queue
            OutgoingGetComponentDataOperationsQueue.SkipOperation(GetOperation);
        }
        finally {
        	//. release operation
        	GetOperation.Release();
        }
    }
    
    public void run() 
    {
        SetGarbageCollectorLaunchingBase();
        //.
        int ImmediateReconnectCounter = ImmediateReconnectCount;
        while (!flTerminated)
        {
            SetProcessException(null);
            try
            {
            	if (flPause) {
            		flPaused = true;
                	Thread.sleep(LoopSleepTime);
                	//.
                	continue; //. ^
            	}
            	else
            		flPaused = false;
            	//. working ...
            	try {
            		Device.Log.WriteInfo("ConnectorModule","connecting ...");
            		Connect();
            		try {
                        flReconnect = false;
                        try
                        {
                            TIndex OperationMessageOrigin = new TIndex();
                            TOperationSession OperationSession = new TOperationSession();
                            //.
                    		Device.Log.WriteInfo("ConnectorModule","receiving configuration ...");
                            ReceiveConfiguration();
                            //. 
                    		Device.Log.WriteInfo("ConnectorModule","transmitting configuration ...");
                            TransmitConfiguration();
                            //.
                        	ImmediateReconnectCounter = ImmediateReconnectCount;
                        	//.
                            flProcessing = true;
                            try {
                                //. processing ...
                        		Device.Log.WriteInfo("ConnectorModule","processing ...");
                                long OutgoingSetOperations_LastTime = 0;
                                int OutgoingSetComponentDataOperationsQueue_TransmitInterval = TransmitInterval;
                                while (!flTerminated)
                                {
                                    //. process outgoing set component data operations
                                	long NowTime = System.currentTimeMillis(); 
                                	if (
                                			(((NowTime-OutgoingSetOperations_LastTime) >= OutgoingSetComponentDataOperationsQueue_TransmitInterval) && (NowTime >= OutgoingSetComponentDataOperationsQueue.GetMinimumOfOperationMaxTime()) && (!ConnectorStateListener.IsActive() || ConnectorStateListener.SignalIsGood())) ||
                                			(ImmediateTransmiteOutgoingSetComponentDataOperationsCounter_GetValue() > 0) || 
                                			(Device.ModuleState == TDEVICEModule.MODULE_STATE_FINALIZING)
                                		) {
                                		boolean flQueueIsProcessed = false;
                                        while (!flTerminated) {
                                            Vector<Object> SetOperations = OutgoingSetComponentDataOperationsQueue.GetOperationsGroupToProcess(OperationsGroupMaxSize);
                                            if (SetOperations == null) {
                                            	if (flQueueIsProcessed)
                                            		OutgoingSetComponentDataOperationsQueue.Save(); //. save empty outgoing set queue state
                                                break; //. >
                                            }
                                            //.
                                            flProcessingOperation = true;
                                            try {
                                                ProcessSetOperations(SetOperations);
                                                flQueueIsProcessed = true;
                                            }
                                            finally {
                                            	flProcessingOperation = false;
                                            }
                                        }
                                        OutgoingSetOperations_LastTime = System.currentTimeMillis();
                                        ImmediateTransmiteOutgoingSetComponentDataOperationsCounter_Release();
                                	}
                                    //.
                                    if (flTerminated)
                                        return; //. ->
                                    //. process outgoing get component data operations
                                    while (!flTerminated) {
                                    	TObjectGetComponentDataServiceOperation GetOperation = OutgoingGetComponentDataOperationsQueue.GetOperationToProcess();
                                        if (GetOperation == null)
                                            break; //. > 
                                        //.
                                        flProcessingOperation = true;
                                        try {
                                            ProcessGetOperation(GetOperation);
                                        }
                                        finally {
                                        	flProcessingOperation = false;
                                        }
                                    }
                                    //.
                                    if (flTerminated)
                                        return; //. ->
                                    //.
                                    if (Device.ModuleState == TDEVICEModule.MODULE_STATE_RUNNING)
                                    {
                                        //. receive incoming operations
                                    	byte[] OperationMessage = null;
                                    	while (!flTerminated) {
                                            OperationMessageOrigin.Reset();
                                            OperationSession.New();
                                            OperationMessage = TGeographServerServiceOperation.CheckReceiveMessage(Device.UserID,Device.UserPassword,Connection,ConnectionInputStream,ConnectionOutputStream,/*out*/ OperationSession,/*out*/ OperationMessageOrigin, LoopSleepTime);
                                            if (OperationMessage == null) //. check if there was timeout then break 
                                            	break; //. >
                                        	//. process pending set-operations before incoming (incoming operation will be processed at the end as concurrent operation)
                                            Vector<Object> SetOperations = OutgoingSetComponentDataOperationsQueue.GetOperationsGroupToProcess(System.currentTimeMillis());
                                            if (SetOperations != null)
                                            {
                                                flProcessingOperation = true;
                                                try {
                                                    ProcessSetOperations(SetOperations);
                                                }
                                                finally {
                                                	flProcessingOperation = false;
                                                }
                                                //. save outgoing set queue state if it is empty
                                            	if (OutgoingSetComponentDataOperationsQueue.IsEmpty())
                                            		OutgoingSetComponentDataOperationsQueue.Save(); 
                                            }
                                            //. process incoming operation 
                                            flProcessingOperation = true;
                                            try {
                                                ProcessIncomingOperation(OperationSession.ID,OperationMessage,/*ref*/ OperationMessageOrigin, null, ConnectionInputStream,ConnectionOutputStream, ProcessIncomingOperationResult);
                                            }
                                            finally {
                                            	flProcessingOperation = false;
                                            }
                                    	}
                                        //.
                                        if (flTerminated)
                                            return; //. ->
                                    	//. there was no incoming operations
                                    	if (OperationMessage == null) {
                                    		if (flReconnect)  
                                    			break; //. >
                                            if (IsItTimeToDoCheckpoint())
                                                Checkpoint();
                                            else { 
                                                if (IsItTimeToDoGarbageCollection())
                                                {
                                                    SetGarbageCollectorLaunchingBase();
                                                	//. initiate garbage collection if it is time
                            						TGeoLogApplication.Instance().GarbageCollector.Start();
                                                }
                                            }
                                    	}
                                    }
                                    else
                                    	Thread.sleep(LoopSleepTime);
                                }
                            }
                            finally {
                                flProcessing = false;
                            }
                        }
                        catch (Throwable E) {
                            //. disconnect on exception
                            Disconnect(E);
                            //.
                            throw E; //. =>
                        }
            		}
            		finally {
                        //. disconnect normally
                        Disconnect(null);
            		}
    			} catch (ConnectException CE) {
    				throw new ConnectException(Device.context.getString(R.string.SNoServerConnection)); //. =>
    			}
                catch (OperationException OE) {
                	switch (OE.Code) {

                	case TGeographServerServiceOperation.ErrorCode_DataOutOfMemory:
                    	try {
                    		OutgoingSetComponentDataOperationsQueue.Save();
                    		Device.ControlModule.RestartDeviceProcessAfterDelay(1000*30/*seconds*/);
                    		//.
                    		Device.Log.WriteError("ConnectorModule","Out of memory exception, device process will be restarted.");
                        	return; //. ->
                    	}
                    	catch (Exception E) {
                    		TGeoLogApplication.Log_WriteCriticalError(E);
                    	}
                		break; //. >
                	}
                	//.
                	if (OE.IsConnectionWorkerThreadError()) 
                    	return; //. ->
                	//.
                	if (OE.IsMessageError()) 
                		TGeoLogApplication.Log_WriteCriticalError(OE);
                	//.
                	throw OE; //. =>
                }
            }
            catch (InterruptedException E) {
            	return; //. ->
            }
            catch (Throwable E)
            { 
            	//. log errors
        		Device.Log.WriteError("ConnectorModule",E.getMessage());
            	if (!(E instanceof Exception))
            		TGeoLogApplication.Log_WriteCriticalError(E);
            	//.
        		SetProcessException(new Exception(E.getMessage()));
            	//.
        		if (!flReconnect) {
                    try
                    {
                    	if ((ImmediateTransmiteOutgoingSetComponentDataOperationsCounter_GetValue() > 0) && (ImmediateReconnectCounter > 0))
                    		ImmediateReconnectCounter--; //. do not wait before reconnect if urgent operations are in queue
                    	else {
                    		while (!flTerminated) {
                    			int SleepInterval = 30;
                    			boolean flDoReconnect = false;
                    			for (int I = 0; I < SleepInterval; I++) { //. time to wait and take ProcessException
                    				Thread.sleep(1000);
                    				//.
                            		if (flReconnect) {
                            			flDoReconnect = true;
                            			break; //. >
                            		}
                    			}
                    			if (flDoReconnect)
                    				break; //. >
                    			//.
                        		if (!ConnectorStateListener.IsActive() || ConnectorStateListener.SignalIsGood())
                        			break; //. >
                        		Device.Log.WriteInfo("ConnectorModule","weak signal, "+Short.toString(ConnectorStateListener.GetSignalLevel())+"%");
                    		}
                    	}
                    } 
                    catch (InterruptedException E1) {
                    	return; //. ->
                    }
        		}
            }  
        }
    }
    
    private void ReceiveConfiguration() throws OperationException,IOException,InterruptedException
    {
        //. read configuration
        TLoadConfiguration1SO SO = new TLoadConfiguration1SO(this,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        int RC = SO.ProcessOutgoingOperation(Connection,ConnectionInputStream,ConnectionOutputStream);
        if (RC < 0)
            throw new OperationException(RC,"load configuration error"); //. =>
        //. send a notification message 
		MessageHandler.obtainMessage(MESSAGE_CONFIGURATION_RECEIVED).sendToTarget();
    }
    
    private void TransmitConfiguration() throws Exception 
    {
        //. write configuration
    	Device.UserAgentModule.UpdateUserIDFromAgent();
    	if (Device.UserAgentModule.UserID.IsSet() && Device.UserAgentModule.UserID.IsChanged()) {
            TObjectSetUserAgentModuleUserIDValueSO SO = new TObjectSetUserAgentModuleUserIDValueSO(this,Device.UserID,Device.UserPassword,Device.ObjectID,null);
            SO.setValue(Device.UserAgentModule.UserID);
            //.
            OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
    	}
    }
    
    public void SetCheckpointBase()
    {
        LastCheckpointTime = System.currentTimeMillis();
    }
    
    private boolean IsItTimeToDoCheckpoint()
    {
        int MS = CheckpointInterval.GetValue()*1000;
        return ((System.currentTimeMillis()-LastCheckpointTime) > MS);
    }
    
    private void Checkpoint() throws Exception
    {
        TObjectCheckpointSO SO = new TObjectCheckpointSO(this,Device.UserID,Device.UserPassword,Device.ObjectID,null); 
        SO.ProcessOutgoingOperation(Connection,ConnectionInputStream,ConnectionOutputStream);
    }

    public void SetGarbageCollectorLaunchingBase()
    {
        LastGarbageCollectorLaunchingTime =System.currentTimeMillis();
    }
    
    private boolean IsItTimeToDoGarbageCollection()
    {
        return ((System.currentTimeMillis()-LastGarbageCollectorLaunchingTime) > GarbageCollectingInterval);
    }
    
    public static class TProcessIncomingOperationResult {
    	
    	public TDeviceComponentServiceOperation Operation = null;
    	public int 								ResultCode = TGeographServerServiceOperation.ErrorCode_Unknown;
    	
    	public void SetResultCode(int pResultCode) {
    		ResultCode = pResultCode;
    	}
    }
    
    public void ProcessIncomingOperation(short OperationSession, byte[] PreambleMessage, TIndex Origin, TComponentUserAccessList CUAL, InputStream ConnectionInputStream, OutputStream ConnectionOutputStream, TProcessIncomingOperationResult Result) throws OperationException
    {
    	Result.ResultCode = TGeographServerServiceOperation.SuccessCode_OK;
    	Result.Operation = null;
        try
        {
            short SID = TGeographServerServiceOperation.GetMessageSID(PreambleMessage,/*ref*/ Origin);
            int ObjectID = TObjectServiceOperation.GetMessageObjectID(PreambleMessage,/*ref*/ Origin);
            short[] Address = TComponentServiceOperation.GetAddress(PreambleMessage,/*ref*/ Origin);
            if (SID == TDeviceSetComponentDataServiceOperation.SID)
            {
            	if (!TComponentUserAccessList.CheckAccess(CUAL, Address,TGeographServerServiceOperation.idWriteOperation)) {
            		Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied);
                    return; //. ->
            	}
                Result.Operation = GetDeviceSetComponentDataServiceOperation(Address,ObjectID,OperationSession);
                if (Result.Operation == null)
                {
                    TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                    Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                    return; //. ->
                }
            } else 
                if (SID == TDeviceSetComponentDataByAddressDataServiceOperation.SID)
                {
                	if (!TComponentUserAccessList.CheckAccess(CUAL, Address,TGeographServerServiceOperation.idWriteOperation)) {
                		Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied);                        
                		return; //. ->
                	}
                    byte[] AddressData = TDeviceComponentByAddressDataServiceOperation.GetAddressData(PreambleMessage,/*ref*/ Origin);
                    Result.Operation = GetDeviceSetComponentDataByAddressDataServiceOperation(Address,ObjectID,OperationSession,AddressData);
                    if (Result.Operation == null)
                    {
                        TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                        Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                        return; //. ->
                    }
                } else 
            if (SID == TDeviceGetComponentDataServiceOperation.SID)
            {
            	if (!TComponentUserAccessList.CheckAccess(CUAL, Address,TGeographServerServiceOperation.idReadOperation)) {
            		Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied);
                    return; //. ->
            	}
                Result.Operation = GetDeviceGetComponentDataServiceOperation(Address,ObjectID,OperationSession);
                if (Result.Operation == null)
                {
                    TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                    Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                    return; //. ->
                }
            }
            else 
                if (SID == TDeviceGetComponentDataByAddressDataServiceOperation.SID)
                {
                	if (!TComponentUserAccessList.CheckAccess(CUAL, Address,TGeographServerServiceOperation.idReadOperation)) {
                		Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_OperationUserAccessIsDenied);
                        return; //. ->
                	}
                    byte[] AddressData = TDeviceComponentByAddressDataServiceOperation.GetAddressData(PreambleMessage,/*ref*/ Origin);
                    Result.Operation = GetDeviceGetComponentDataByAddressDataServiceOperation(Address,ObjectID,OperationSession,AddressData);
                    if (Result.Operation == null)
                    {
                        TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                        Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                        return; //. ->
                    }
                }
                else 
            {
                TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_OperationUnknownService);
                Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_OperationUnknownService);
                return; //. ->
            }
            //. processing
            Result.ResultCode = Result.Operation.ProcessIncomingOperation(OperationSession,PreambleMessage,/*ref*/ Origin,ConnectionInputStream,ConnectionOutputStream);
            if (Result.ResultCode >= 0)
            {
            	Result.ResultCode = Result.Operation.DoOnOperationCompletion();
            	if (Result.ResultCode < 0)
            	{
                    OperationException E = new OperationException(Result.ResultCode,"error of operation completion: '"+Result.Operation.Name+"', code: "+Integer.toString(Result.ResultCode).toString());
                    if (E.IsCommunicationError())
                        throw E; //. =>
                    else 
                    {
                    	Result.Operation.DoOnOperationException(E);
                    }
            	}
            }
            else
            {
                OperationException E = new OperationException(Result.ResultCode,"error of operation: '"+Result.Operation.Name+"', code: "+Integer.toString(Result.ResultCode).toString());
                if (E.IsCommunicationError())
                    throw E; //. =>
                else 
                {
                	Result.Operation.DoOnOperationException(E);
                }
            }
        }
        catch (OperationException E)
        {
        	Result.ResultCode = E.Code;
            if (E.IsCommunicationError())
                throw E; //. =>
        }
        catch (Exception E)
        {
        	Result.SetResultCode(TGeographServerServiceOperation.ErrorCode_Unknown);
        }
    }    

    public TDeviceSetComponentDataServiceOperation GetDeviceSetComponentDataServiceOperation(short[] Address, int ObjectID, short Session)
    {
        TElementAddress SubAddress = new TElementAddress();
        if (TSetCheckpointIntervalSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetCheckpointIntervalSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetGeoDistanceThresholdSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetGeoDistanceThresholdSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetConnectorConfigurationDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetConnectorConfigurationDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetGPSModuleConfigurationDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetGPSModuleConfigurationDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetGPOValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetGPOValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderModeValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderModeValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderActiveValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderActiveValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderRecordingValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderRecordingValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderAudioValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderAudioValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderVideoValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderVideoValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderTransmittingValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderTransmittingValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderSavingValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderSavingValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderSDPValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderSDPValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderReceiversValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderReceiversValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderSavingServerValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderSavingServerValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetVideoRecorderConfigurationDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderConfigurationDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetAudioModuleSourcesSensitivitiesValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetAudioModuleSourcesSensitivitiesValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetAudioModuleDestinationsVolumesValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetAudioModuleDestinationsVolumesValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetAudioModuleAudioFilesValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetAudioModuleAudioFilesValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetAudioModuleAudioFileMessageValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetAudioModuleAudioFileMessageValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetDataStreamerActiveValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetDataStreamerActiveValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        if (TSetAlarmModuleProfileDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetAlarmModuleProfileDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value); //. =>
        else 
            return null;
    } 
    
    public TDeviceSetComponentDataByAddressDataServiceOperation GetDeviceSetComponentDataByAddressDataServiceOperation(short[] Address, int ObjectID, short Session, byte[] AddressData)
    {
    	TElementAddress SubAddress = new TElementAddress();
        if (TSetVideoRecorderMeasurementDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetVideoRecorderMeasurementDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TSetFileSystemDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetFileSystemDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TSetControlDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetControlDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TSetSensorDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetSensorDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TSetTaskModuleTaskResultSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetTaskModuleTaskResultSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData); //. =>
        if (TSetTaskModuleTaskStatusSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetTaskModuleTaskStatusSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData); //. =>
        if (TSetDataStreamerStreamingComponentsValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetDataStreamerStreamingComponentsValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData); //. =>
        if (TSetSensorsModuleChannelsValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetSensorsModuleChannelsValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData); //. =>
        if (TSetSensorsModuleMetersValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetSensorsModuleMetersValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData); //. =>
        if (TSetSensorsModuleMeasurementsValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TSetSensorsModuleMeasurementsValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData); //. =>
        else
            return null;
    } 
    
    public TDeviceGetComponentDataServiceOperation GetDeviceGetComponentDataServiceOperation(short[] Address, int ObjectID, short Session)
    {
    	TElementAddress SubAddress = new TElementAddress();
        if (TGetConnectorConfigurationDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetConnectorConfigurationDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value);  //. =>
        if (TGetGPSFixSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetGPSFixSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value);  //. =>
        if (TGetGPSModuleConfigurationDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetGPSModuleConfigurationDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value);  //. =>
        if (TGetGPIValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetGPIValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value);  //. =>
        if (TGetVideoRecorderConfigurationDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetVideoRecorderConfigurationDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value);  //. =>
        if (TGetVideoRecorderMeasurementsListValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetVideoRecorderMeasurementsListValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value);  //. =>
        if (TGetAudioModuleAudioFilesValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetAudioModuleAudioFilesValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value);  //. =>
        else 
            return null;
    } 

    public TDeviceGetComponentDataByAddressDataServiceOperation GetDeviceGetComponentDataByAddressDataServiceOperation(short[] Address, int ObjectID, short Session, byte[] AddressData)
    {
    	TElementAddress SubAddress = new TElementAddress();
        if (TGetVideoRecorderMeasurementDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetVideoRecorderMeasurementDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetVideoRecorderMeasurementsListValueADSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetVideoRecorderMeasurementsListValueADSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetFileSystemDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetFileSystemDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetControlDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetControlDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetSensorDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetSensorDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetDataStreamerStreamingComponentsValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetDataStreamerStreamingComponentsValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetSensorsModuleChannelsValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetSensorsModuleChannelsValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetSensorsModuleMetersValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetSensorsModuleMetersValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetSensorsModuleMeasurementsValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetSensorsModuleMeasurementsValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        else
            return null;
    } 
    
    public class TConnectorStateListener extends PhoneStateListener
    {
    	private final int NormalSignalThreshold = 30; //. in %
    	private final int WeakSignalThreshold = 10; //. in %
    	private final int SignalLevelThreshold = 10; //. in %
    	private final int CheckActivityInterval = 60000*2; //. minutes
    	
        private long LastActivityTime = 0;
    	private short SignalLevel = 0;
    	private short CriticalSignalLevel = 0; //. in %
    	private double LastLevelPercentage = -1.0;
    	private short LastLevel = 0; //. in %
    	
    	private TAlarmer CellularSignalAlarmer;
    	
    	public void SetCellularSignalAlarmer(TAlarmer Alarmer) {
    		CellularSignalAlarmer = Alarmer;
    	}
    	
    	public TAlarmer GetCellularSignalAlarmer() {
    		return CellularSignalAlarmer;
    	}
    	
    	@Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
           super.onSignalStrengthsChanged(signalStrength);
           //.
           DoOnSignalLevelChanged((short)signalStrength.getGsmSignalStrength());
        }
        
        private void DoOnSignalLevelChanged(short Level) {
        	if (Level > 31)
        		return; //. ->
        	//.
        	synchronized (this) {
            	LastActivityTime = System.currentTimeMillis();
            	SignalLevel = Level;
			}
        	//.
        	double LevelPercentage = 100.0*Level/31.0; 
        	//.
        	if (LevelPercentage != LastLevelPercentage) {
        		LastLevelPercentage = LevelPercentage;
        		//.
        		TAlarmer CSA = GetCellularSignalAlarmer();
        		if (CSA != null)
        			CSA.DoOnValue(Double.valueOf(LevelPercentage));
        	}
        	//.
        	Level = (short)(((int)(LevelPercentage/SignalLevelThreshold))*SignalLevelThreshold);
        	if (Level >= NormalSignalThreshold)
        		return; //. ->
        	if (Level != LastLevel) {
            	TComponentTimestampedInt16Value Value = new TComponentTimestampedInt16Value(OleDate.UTCCurrentTimestamp(),Level);
            	//. 
                TObjectSetComponentDataServiceOperation SO = new TObjectSetConnectorServiceProviderSignalValueSO(Device.ConnectorModule,Device.UserID,Device.UserPassword,Device.ObjectID,null);
                ((TObjectSetConnectorServiceProviderSignalValueSO)SO).setValue(Value);
                try
                {
                    Device.ConnectorModule.OutgoingSetComponentDataOperationsQueue.AddNewOperation(SO);
                    if (Level < CriticalSignalLevel)
                    	Device.ConnectorModule.ImmediateTransmiteOutgoingSetComponentDataOperations();
                }
                catch (Exception E) {}
                //.
                LastLevel = Level;
        	}
        }
        
        public synchronized boolean IsActive(int Interval) {
        	return ((System.currentTimeMillis()-LastActivityTime) <= Interval);
        }
        
        public boolean IsActive() {
        	return IsActive(CheckActivityInterval);
        }
        
        public synchronized short GetSignalLevel() {
        	return (short)(100.0*SignalLevel/31.0); //. in %
        }
        
        public boolean SignalIsGood() {
        	return (GetSignalLevel() >= WeakSignalThreshold);
        }
    }

    private static final int MESSAGE_CONFIGURATION_RECEIVED = 1;
    
	public Handler MessageHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {

                case MESSAGE_CONFIGURATION_RECEIVED: 
                	ConfigurationSubscribers.DoOnConfigurationReceived();
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
