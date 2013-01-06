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
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoLog.COMPONENT.TElementAddress;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentInt16Value;
import com.geoscope.GeoLog.COMPONENT.Values.TComponentTimestampedInt16Value;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetConnectorConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetFileSystemDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetGPIValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetGPSFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetGPSModuleConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetVideoRecorderConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetVideoRecorderMeasurementDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TGetVideoRecorderMeasurementsListValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TLoadConfigurationSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectCheckpointSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetBatteryChargeValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetConnectorServiceProviderSignalValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetFixMarkSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPIFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPIValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetGPSFixSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOIDataFileSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOIJPEGImageSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOISO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetMapPOITextSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderModeSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderAudioFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderRecordingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderSavingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderTransmittingFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TObjectSetVideoRecorderVideoFlagSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetCheckpointIntervalSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetConnectorConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetControlDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetFileSystemDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetGPOValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetGPSModuleConfigurationDataValueSO;
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Operations.TSetGeoDistanceThresholdSO;
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
import com.geoscope.GeoLog.DEVICE.ConnectorModule.Protocol.TIndex;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule;
import com.geoscope.GeoLog.DEVICEModule.TModule;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.Utils.TDataConverter;
/**
 *
 * @author ALXPONOM
 */
public class TConnectorModule extends TModule implements Runnable{
	
	public static final String OutgoingSetOperationsQueueFolderName = "Device.ConnectorModule.OutgoingSetOperationsQueue";
	public static final String OutgoingSetOperationsQueueFileName = TDEVICEModule.ProfileFolder+"/"+OutgoingSetOperationsQueueFolderName+"/"+"Data.dat";
	public static final String OutgoingSetOperationsQueueDataFolderName = TDEVICEModule.ProfileFolder+"/"+OutgoingSetOperationsQueueFolderName+"/"+"Data";
	//.
    public static final int OperationsGroupMaxSize = 8192; //. bytes
    private static final int ConnectTimeout = 1000*60; /*seconds*/
    private static final int DefaultReadTimeout = 1000*30; /*seconds*/
    private static final int ImmediateReconnectCount = 12;
    
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
    		File F = new File(OutgoingSetOperationsQueueFolderName);
    		if (!F.exists()) 
    			F.mkdirs();
    		F = new File(OutgoingSetOperationsQueueDataFolderName);
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
    		String FN = OutgoingSetOperationsQueueFileName;
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
    		String FN = OutgoingSetOperationsQueueFileName;
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
    
        public synchronized TObjectSetComponentDataServiceOperation GetOperationToProcess()
        {
            if (QueueHead == QueueTile)
                return null; //. ->
            //.
            TObjectSetComponentDataServiceOperation Result = Queue[QueueHead];
            Result.AddRef();
            return Result;
        }
        
        public synchronized Vector<Object> GetOperationsGroupToProcess(int MaxSize)
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
        
        public synchronized Vector<Object> GetOperationsGroupToProcess(Date ToTime)
        {
            if (QueueHead == QueueTile)
                return null; //. ->
            //.
            long ToTimeValue = ToTime.getTime();
            //.
            Vector<Object> Result = new Vector<Object>();
            int QueuePos = QueueHead;
            do
            {
            	if (Queue[QueuePos].TimeStamp.getTime() > ToTimeValue)
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
                if ((Queue[LastOperationIndex].RefCount() == 1) && Queue[LastOperationIndex].Address().IsAddressTheSame(pOperation.Address()))
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
                    TObjectSetComponentDataServiceOperation SO = GetObjectSetComponentDataServiceOperation(Address,ConnectorModule.Device.ObjectID);
                    if (SO != null)
                    {
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
                    BA = Queue[_QueueHead].Saving_ToByteArray();
                    OpList.addElement(BA);
                    DataSize += (4/*SizeOf(Size)*/+BA.length);
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
            if (TObjectSetGPIValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGPIValueSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetGPIFixSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetGPIFixSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetMapPOISO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetMapPOISO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetMapPOIJPEGImageSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetMapPOIJPEGImageSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetMapPOITextSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetMapPOITextSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetMapPOIDataFileSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetMapPOIDataFileSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetFixMarkSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetFixMarkSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetBatteryChargeValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetBatteryChargeValueSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
            if (TObjectSetConnectorServiceProviderSignalValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
                return new TObjectSetConnectorServiceProviderSignalValueSO(ConnectorModule,ConnectorModule.Device.UserID,ConnectorModule.Device.UserPassword,ObjectID,SubAddress.Value); //. ->
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
	//.
    public boolean 	flServerConnectionEnabled = false;
    public String 	ServerAddress = "127.0.0.1";
    public int		ServerPort = 8282;
    public int 		LoopSleepTime = 1*1000; //. milliseconds
    public int 		TransmitInterval = 0; //. in seconds
    public boolean 	OutgoingSetComponentDataOperationsQueue_flEnabled = true;
    //.
    private Socket Connection;
    public InputStream ConnectionInputStream;
    public OutputStream ConnectionOutputStream;
    private Thread thread;
    private boolean flTerminated = false;
    public boolean flProcessing = false;
    public boolean flProcessingOperation = false;
    private Exception ProcessException = null;
    private Exception ProcessOutgoingOperationException = null;
    public TOutgoingSetComponentDataOperationsQueue OutgoingSetComponentDataOperationsQueue;
    public TOutgoingGetComponentDataOperationsQueue OutgoingGetComponentDataOperationsQueue;
    private int ImmediateTransmiteOutgoingSetComponentDataOperationsCounter = 0;
    public TComponentInt16Value CheckpointInterval = null;
    private Date LastCheckpointTime;
    private static int GarbageCollectingInterval = 3600/*seconds*/*1000; 
    private Date LastGarbageCollectorLaunchingTime;
    //. connector signal condition listener
    private TConnectorStateListener ConnectorStateListener;
    private TelephonyManager _TelephonyManager;
    
    public TConnectorModule(TDEVICEModule pDevice) throws Exception
    {
    	super(pDevice);
    	//.
        Device = pDevice;
    	//. virtual values
        ConfigurationDataValue = new TConnectorModuleConfigurationDataValue(this);
        //.
        OutgoingSetComponentDataOperationsQueue = new TOutgoingSetComponentDataOperationsQueue(this);
        OutgoingGetComponentDataOperationsQueue = new TOutgoingGetComponentDataOperationsQueue(this);
        CheckpointInterval = new TComponentInt16Value();
        ConnectorStateListener = new TConnectorStateListener();
        _TelephonyManager = (TelephonyManager)Device.context.getSystemService(Context.TELEPHONY_SERVICE);
        _TelephonyManager.listen(ConnectorStateListener,PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);     
        //.
        CheckpointInterval.SetValue((short)60); //. default checkpoint interval, in seconds
    	try {
			LoadConfiguration();
		} catch (Exception E) {
            Toast.makeText(Device.context, Device.context.getString(R.string.SConnectorModuleConfigurationError)+E.getMessage(), Toast.LENGTH_LONG).show();
		}
        //.
        thread = null;
    }
    
    public void Destroy() throws Exception
    {
        if (thread != null)
            StopConnection();
    	//.
    	if (_TelephonyManager != null) { 
    		_TelephonyManager.listen(ConnectorStateListener,PhoneStateListener.LISTEN_NONE);
    		_TelephonyManager = null;
    	}
        //.
        if (OutgoingSetComponentDataOperationsQueue != null)
        {
            OutgoingSetComponentDataOperationsQueue.Destroy();
            OutgoingSetComponentDataOperationsQueue = null;
        }
    }
    
    @Override
    public synchronized void LoadConfiguration() throws Exception {
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
		default:
			throw new Exception("unknown configuration version, version: "+Integer.toString(Version)); //. =>
		}
		return; 
    }
    
    @Override
	public synchronized void SaveConfigurationTo(XmlSerializer Serializer) throws Exception {
		int Version = 1;
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
    
    public void StartConnection()
    {
        if (thread != null)
            return; //. ->
        flTerminated = false;
        thread = new Thread(this);
        thread.start();
    }
    
    public void StopConnection() 
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
    
    public void SetActive(boolean flActivate) {
    	if (flActivate) {
    		if (!IsActive())
    			StartConnection();
    	}
    	else {
    		if (IsActive())
    			StopConnection();
    	}
    }
    
    public void Validate() {
    	SetActive(flServerConnectionEnabled);
    }
    
    private void Connect() throws IOException
    {
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
    
    private void Disconnect(Throwable _Exception) 
    {
    	try {
            //. close connection gracefully
        	if ((_Exception == null) || !((_Exception instanceof OperationException) && ((OperationException)_Exception).IsConnectionError())) {
                byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(TGeographServerServiceOperation.Descriptor_ConnectionIsClosing);
                ConnectionOutputStream.write(BA);
                ConnectionOutputStream.flush();
        	}
            //.
            ConnectionOutputStream.close();
            ConnectionInputStream.close();
            Connection.close();
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
    
    private void ProcessSetOperations(Vector<Object> SetOperations) throws Exception 
    {
        try
        {
            if (SetOperations.size() > 1)
            {
                    int SummaryCompletionTime = 0;
                    //. start operations group
                    for (int I = 0; I < SetOperations.size(); I++)
                        SummaryCompletionTime += ((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).StartOutgoingOperation(ConnectionOutputStream);
                    //. wait and finish operations completion
                    int[] RCs = new int[SetOperations.size()];
                    for (int I = 0; I < SetOperations.size(); I++)
                    {
                        int RC = ((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).FinishOutgoingOperation(ConnectionInputStream,ConnectionOutputStream,(I == 0 ? SummaryCompletionTime : TGeographServerServiceOperation.Connection_DataWaitingInterval));
                        RCs[I] = RC;
                        if (RC < 0) 
                        {
                            OperationException E = new OperationException(RC,"error of operation: '"+((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).Name+"', code: "+Integer.toString(RC).toString());
                            if (E.IsCommunicationError())
                                throw E; //. =>
                            else
                                SetProcessOutgoingOperationException(E);
                        }
                    }
                    //. process completion actions
                    for (int I = 0; I < SetOperations.size(); I++)
                    {
                        if (RCs[I] >= 0)
                        {
                        	int RC = ((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).DoOnOperationCompletion();
                        	if (RC < 0)
                        	{
                                OperationException E = new OperationException(RC,"error of operation completion: '"+((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).Name+"', code: "+Integer.toString(RC).toString());
                                if (E.IsCommunicationError())
                                    throw E; //. =>
                                else
                                {
                                	((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).DoOnOperationException(E);
                                	SetProcessOutgoingOperationException(E);
                                }
                        	}
                        }
                        else
                        {
                            OperationException E = new OperationException(RCs[I],"error of operation: '"+((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).Name+"', code: "+Integer.toString(RCs[I]).toString());
                        	((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).DoOnOperationException(E);
                        }
                    }
                    //. check and execute delayed concurrent incoming operation from the server (should be one operation)
                    for (int I = 0; I < SetOperations.size(); I++) 
                    {
                        TObjectSetComponentDataServiceOperation SetOperation = (TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I);
                        if (SetOperation.ConcurrentOperationSessionID != 0)
                            ProcessIncomingOperation(SetOperation.ConcurrentOperationSessionID,SetOperation.ConcurrentOperationMessage,SetOperation.ConcurrentOperationMessageOrigin);
                    }
                    //. remove current operations from queue
                    OutgoingSetComponentDataOperationsQueue.SkipOperationsGroup(SetOperations);
            }
            else
            {
                TObjectSetComponentDataServiceOperation CurrentSetOperation = (TObjectSetComponentDataServiceOperation)SetOperations.elementAt(0);
                int RC = CurrentSetOperation.ProcessOutgoingOperation(ConnectionInputStream,ConnectionOutputStream);
                if (RC >= 0)
                {
                	RC = CurrentSetOperation.DoOnOperationCompletion();
                	if (RC < 0)
                	{
                        OperationException E = new OperationException(RC,"error of operation completion: '"+CurrentSetOperation.Name+"', code: "+Integer.toString(RC).toString());
                        if (E.IsCommunicationError())
                            throw E; //. =>
                        else 
                        {
                        	CurrentSetOperation.DoOnOperationException(E);
                            SetProcessOutgoingOperationException(E);
                        }
                	}
                }
                else
                {
                    OperationException E = new OperationException(RC,"error of operation: '"+CurrentSetOperation.Name+"', code: "+Integer.toString(RC).toString());
                    if (E.IsCommunicationError())
                        throw E; //. =>
                    else 
                    {
                    	CurrentSetOperation.DoOnOperationException(E);
                        SetProcessOutgoingOperationException(E);
                    }
                }
                //. remove current operation from queue
                OutgoingSetComponentDataOperationsQueue.SkipOperation(CurrentSetOperation);
            }
        }
        finally
        {
            for (int I = 0; I < SetOperations.size(); I++)
                ((TObjectSetComponentDataServiceOperation)SetOperations.elementAt(I)).Release();
        }
    }
    
    private void ProcessGetOperation(TObjectGetComponentDataServiceOperation GetOperation) throws Exception
    {
        try
        {
                int RC = GetOperation.ProcessOutgoingOperation(ConnectionInputStream,ConnectionOutputStream);
                if (RC >= 0)
                {
                	RC = GetOperation.DoOnOperationCompletion();
                	if (RC < 0)
                	{
                        OperationException E = new OperationException(RC,"error of operation completion: '"+GetOperation.Name+"', code: "+Integer.toString(RC).toString());
                        if (E.IsCommunicationError())
                            throw E; //. =>
                        else 
                        {
                        	GetOperation.DoOnOperationException(E);
                            SetProcessOutgoingOperationException(E);
                        }
                	}
                }
                else
                {
                    OperationException E = new OperationException(RC,"error of operation: '"+GetOperation.Name+"', code: "+Integer.toString(RC).toString());
                    if (E.IsCommunicationError())
                        throw E; //. =>
                    else 
                    {
                    	GetOperation.DoOnOperationException(E);
                        SetProcessOutgoingOperationException(E);
                    }
                }
                //. remove current operation from queue
                OutgoingGetComponentDataOperationsQueue.SkipOperation(GetOperation);
        }
        finally 
        {
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
            	try {
                	//. delay before connect
                    Thread.sleep(1000); 
                	//.
            		Device.Log.WriteInfo("ConnectorModule","connecting ...");
            		Connect();
                    try
                    {
                        TIndex OperationMessageOrigin = new TIndex();
                        TOperationSession OperationSession = new TOperationSession();
                        //.
                    	ImmediateReconnectCounter = ImmediateReconnectCount;
                        flProcessing = true;
                        try
                        {
                            //. load configuration first
                    		Device.Log.WriteInfo("ConnectorModule","loading configuration ...");
                            ReceiveConfiguration();
                            //. processing ...
                    		Device.Log.WriteInfo("ConnectorModule","processing ...");
                            long OutgoingSetOperations_LastTime = 0;
                            int OutgoingSetComponentDataOperationsQueue_TransmitInterval = TransmitInterval;
                            while (!flTerminated)
                            {
                                //. process outgoing set component data operations
                            	if (
                            			(((Calendar.getInstance().getTime().getTime()-OutgoingSetOperations_LastTime) >= OutgoingSetComponentDataOperationsQueue_TransmitInterval) && (!ConnectorStateListener.IsActive() || ConnectorStateListener.SignalIsGood())) ||
                            			(ImmediateTransmiteOutgoingSetComponentDataOperationsCounter_GetValue() > 0) || 
                            			(Device.State == TDEVICEModule.DEVICEModuleState_Finalizing)
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
                                    OutgoingSetOperations_LastTime = Calendar.getInstance().getTime().getTime();
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
                                if (Device.State == TDEVICEModule.DEVICEModuleState_Running)
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
                                        Vector<Object> SetOperations = OutgoingSetComponentDataOperationsQueue.GetOperationsGroupToProcess(Calendar.getInstance().getTime());
                                        if (SetOperations != null)
                                        {
                                            flProcessingOperation = true;
                                            try {
                                                ProcessSetOperations(SetOperations);
                                            }
                                            finally {
                                            	flProcessingOperation = false;
                                            }
                                        }
                                        //. process incoming operation 
                                        flProcessingOperation = true;
                                        try {
                                            ProcessIncomingOperation(OperationSession.ID,OperationMessage,/*ref*/ OperationMessageOrigin);
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
                                        if (IsItTimeToDoCheckpoint())
                                            Checkpoint();
                                        else { 
                                            if (IsItTimeToDoGarbageCollection())
                                            {
                                            	//. initiate garbage collection if it is time
                                                System.gc();
                                                //.
                                                SetGarbageCollectorLaunchingBase();
                                            }
                                        }
                                	}
                                }
                                else
                                	Thread.sleep(LoopSleepTime);
                            }
                        }
                        finally
                        {
                            flProcessing = false;
                        }
                        //. disconnect normally
                        Disconnect(null);
                    }
                    catch (Throwable E)
                    {
                        //. disconnect on exception
                        Disconnect(E);
                        //.
                        throw E; //. =>
                    }
            	}
                catch (OperationException OE)
                {
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
                    		TDEVICEModule.Log_WriteCriticalError(E);
                    	}
                		break; //. >
                	}
                	//.
                	if (OE.IsMessageError()) 
                		TDEVICEModule.Log_WriteCriticalError(OE);
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
            		TDEVICEModule.Log_WriteCriticalError(E);
            	//.
        		SetProcessException(new Exception(E.getMessage()));
            	//.
                try
                {
                	if ((ImmediateTransmiteOutgoingSetComponentDataOperationsCounter_GetValue() > 0) && (ImmediateReconnectCounter > 0))
                		ImmediateReconnectCounter--; //. do not wait before reconnect if urgent operations are in queue
                	else {
                		while (true) {
                    		Thread.sleep(30000); //. time to wait and take ProcessException
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
    
    private void ReceiveConfiguration() throws OperationException,IOException,InterruptedException
    {
        //. read configuration
        TLoadConfigurationSO SO = new TLoadConfigurationSO(this,Device.UserID,Device.UserPassword,Device.ObjectID,null);
        int RC = SO.ProcessOutgoingOperation(ConnectionInputStream,ConnectionOutputStream);
        if (RC < 0)
            throw new OperationException(RC,"load configuration error"); //. =>
    }
    
    public void SetCheckpointBase()
    {
        LastCheckpointTime = Calendar.getInstance().getTime();
    }
    
    private boolean IsItTimeToDoCheckpoint()
    {
        int MS = CheckpointInterval.GetValue()*1000;
        return ((Calendar.getInstance().getTime().getTime()-LastCheckpointTime.getTime()) > MS);
    }
    
    private void Checkpoint() throws Exception
    {
        TObjectCheckpointSO SO = new TObjectCheckpointSO(this,Device.UserID,Device.UserPassword,Device.ObjectID,null); 
        SO.ProcessOutgoingOperation(ConnectionInputStream,ConnectionOutputStream);
    }

    public void SetGarbageCollectorLaunchingBase()
    {
        LastGarbageCollectorLaunchingTime = Calendar.getInstance().getTime();
    }
    
    private boolean IsItTimeToDoGarbageCollection()
    {
        return ((Calendar.getInstance().getTime().getTime()-LastGarbageCollectorLaunchingTime.getTime()) > GarbageCollectingInterval);
    }
    
    public int ProcessIncomingOperation(short OperationSession, byte[] PreambleMessage, TIndex Origin) throws OperationException
    {
        int ResultCode = TGeographServerServiceOperation.SuccessCode_OK;
        try
        {
            short SID = TGeographServerServiceOperation.GetMessageSID(PreambleMessage,/*ref*/ Origin);
            int ObjectID = TObjectServiceOperation.GetMessageObjectID(PreambleMessage,/*ref*/ Origin);
            short[] Address = TComponentServiceOperation.GetAddress(PreambleMessage,/*ref*/ Origin);
            TDeviceComponentServiceOperation SO = null;
            if (SID == TDeviceSetComponentDataServiceOperation.SID)
            {
                SO = GetDeviceSetComponentDataServiceOperation(Address,ObjectID,OperationSession);
                if (SO == null)
                {
                    TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                    return TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound; //. ->
                }
            } else 
                if (SID == TDeviceSetComponentDataByAddressDataServiceOperation.SID)
                {
                    byte[] AddressData = TDeviceComponentByAddressDataServiceOperation.GetAddressData(PreambleMessage,/*ref*/ Origin);
                    SO = GetDeviceSetComponentDataByAddressDataServiceOperation(Address,ObjectID,OperationSession,AddressData);
                    if (SO == null)
                    {
                        TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                        return TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound; //. ->
                    }
                } else 
            if (SID == TDeviceGetComponentDataServiceOperation.SID)
            {
                SO = GetDeviceGetComponentDataServiceOperation(Address,ObjectID,OperationSession);
                if (SO == null)
                {
                    TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                    return TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound; //. ->
                }
            }
            else 
                if (SID == TDeviceGetComponentDataByAddressDataServiceOperation.SID)
                {
                    byte[] AddressData = TDeviceComponentByAddressDataServiceOperation.GetAddressData(PreambleMessage,/*ref*/ Origin);
                    SO = GetDeviceGetComponentDataByAddressDataServiceOperation(Address,ObjectID,OperationSession,AddressData);
                    if (SO == null)
                    {
                        TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound);
                        return TGeographServerServiceOperation.ErrorCode_ObjectComponentOperation_AddressIsNotFound; //. ->
                    }
                }
                else 
            {
                TDeviceComponentServiceOperation.SendResultCode(this,Device.UserID,Device.UserPassword, ConnectionOutputStream, OperationSession, TGeographServerServiceOperation.ErrorCode_OperationUnknownService);
                return TGeographServerServiceOperation.ErrorCode_OperationUnknownService; //. ->
            }
            //. processing
            ResultCode = SO.ProcessIncomingOperation(OperationSession,PreambleMessage,/*ref*/ Origin,ConnectionInputStream,ConnectionOutputStream);
            if (ResultCode >= 0)
            {
            	ResultCode = SO.DoOnOperationCompletion();
            	if (ResultCode < 0)
            	{
                    OperationException E = new OperationException(ResultCode,"error of operation completion: '"+SO.Name+"', code: "+Integer.toString(ResultCode).toString());
                    if (E.IsCommunicationError())
                        throw E; //. =>
                    else 
                    {
                    	SO.DoOnOperationException(E);
                    }
            	}
            }
            else
            {
                OperationException E = new OperationException(ResultCode,"error of operation: '"+SO.Name+"', code: "+Integer.toString(ResultCode).toString());
                if (E.IsCommunicationError())
                    throw E; //. =>
                else 
                {
                	SO.DoOnOperationException(E);
                }
            }
        }
        catch (OperationException E)
        {
            ResultCode = E.Code;
            if (E.IsCommunicationError())
                throw E; //. =>
        }
        catch (Exception E)
        {
            ResultCode = TGeographServerServiceOperation.ErrorCode_Unknown;
        }
        return ResultCode;
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
        else 
            return null;
    } 

    public TDeviceGetComponentDataByAddressDataServiceOperation GetDeviceGetComponentDataByAddressDataServiceOperation(short[] Address, int ObjectID, short Session, byte[] AddressData)
    {
    	TElementAddress SubAddress = new TElementAddress();
        if (TGetVideoRecorderMeasurementDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetVideoRecorderMeasurementDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetFileSystemDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetFileSystemDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        if (TGetControlDataValueSO._Address.IsAddressTheSame(Address,/*out*/ SubAddress))
            return new TGetControlDataValueSO(this,Device.UserID,Device.UserPassword,ObjectID,Session,SubAddress.Value,AddressData);  //. =>
        else
            return null;
    } 
    
    private class TConnectorStateListener extends PhoneStateListener
    {
    	private final int NormalSignalThreshold = 30; //. in %
    	private final int WeakSignalThreshold = 10; //. in %
    	private final int SignalLevelThreshold = 10; //. in %
    	private final int CheckActivityInterval = 60000*2; //. minutes
    	
        private long LastActivityTime = 0;
    	private short SignalLevel = 0;
    	private short CriticalSignalLevel = 0; //. in %
    	private short LastLevel = 0; //. in %
    	
    	@Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength)
        {
           super.onSignalStrengthsChanged(signalStrength);
           //.
           DoOnSignalLevelChanged((short)signalStrength.getGsmSignalStrength());
        }
        
        private void DoOnSignalLevelChanged(short Level) {
        	synchronized (this) {
            	LastActivityTime = Calendar.getInstance().getTime().getTime();
            	SignalLevel = Level;
			}
        	//.
        	double LevelPercentage = 100.0*Level/31.0; 
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
        	return ((Calendar.getInstance().getTime().getTime()-LastActivityTime) <= Interval);
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
}
