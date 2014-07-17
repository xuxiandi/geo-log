package com.geoscope.GeoEye.Space.Defines;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;

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
import android.util.Xml;
import android.widget.Toast;

import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflector;
import com.geoscope.GeoEye.TReflectorCoGeoMonitorObject;
import com.geoscope.GeoEye.Space.TSpace;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivities;
import com.geoscope.GeoEye.Space.Defines.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSFixValue;
import com.geoscope.GeoLog.DEVICE.GPSModule.TGPSModule;
import com.geoscope.GeoLog.DEVICE.TaskModule.TTaskStatusValue.TUserTaskStatusDescriptor;
import com.geoscope.GeoLog.TrackerService.TTracker;
import com.geoscope.GeoLog.Utils.CancelException;
import com.geoscope.GeoLog.Utils.OleDate;
import com.geoscope.GeoLog.Utils.TCancelableThread;
import com.geoscope.GeoLog.Utils.TCanceller;
import com.geoscope.Utils.TDataConverter;
import com.geoscope.Utils.Thread.Synchronization.Event.TAutoResetEvent;
import com.jcraft.jzlib.ZInputStream;


public class TGeoScopeServerUser {

	public static final int RootUserID 			= 1;
	public static final int AnonymouseUserID 	= 2;
	
	public static final double DefaultUserOnlineTimeout = (1.0/(24.0*3600.0))*180; //. seconds
	
	public static class TUserDescriptor {

		public static class TMission {
			
			public static double MinTimestamp = 0.0;
			public static double MaxTimestamp = 1000000.0;
			public static double CurrentFinishTimestamp = MaxTimestamp;
			
			public int 		ID = 0;
			public String 	Name = "";
			public String 	Info = null;
			public double 	StartTimestamp = 0.0;
			public double	FinishTimestamp = CurrentFinishTimestamp;
			
			public String GetInfo(Context context) {
				String S = "";
				if (IsCurrent())
					S = S+"["+context.getString(R.string.SCurrent)+"]"+" ";
				S = S+Name;
				if (Info != null)
					S = S+" "+"/"+Info+"/";
				return S;
			}
			
			public boolean IsCurrent() {
				return (FinishTimestamp == CurrentFinishTimestamp);			
			}
			
			public int FromByteArray(byte[] BA, int Idx) throws Exception {
				ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
		    	byte SS = BA[Idx]; Idx++;
		    	if (SS > 0) {
		    		Name = new String(BA, Idx,SS, "windows-1251");
		    		Idx += SS;
		    	}
		    	else
		    		Name = "";
		    	short SS16 = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; 
		    	if (SS16 > 0) {
		    		Info = new String(BA, Idx,SS16, "windows-1251");
		    		Idx += SS16;
		    	}
		    	else
		    		Info = null;
				StartTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
				FinishTimestamp = CurrentFinishTimestamp;
				return Idx;
			}

			public int FromByteArrayV1(byte[] BA, int Idx) throws Exception {
				ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
		    	byte SS = BA[Idx]; Idx++;
		    	if (SS > 0) {
		    		Name = new String(BA, Idx,SS, "windows-1251");
		    		Idx += SS;
		    	}
		    	else
		    		Name = "";
		    	short SS16 = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; 
		    	if (SS16 > 0) {
		    		Info = new String(BA, Idx,SS16, "windows-1251");
		    		Idx += SS16;
		    	}
		    	else
		    		Info = null;
				StartTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
				FinishTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;					
				return Idx;
			}
		}
		
		public static class TMissions {
			
			public TMission[] Items = null;
			
			public int FromByteArray(byte[] BA, int Idx) throws Exception {
				int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
				Items = new TMission[ItemsCount];
				for (int I = 0; I < ItemsCount; I++) {
					TMission Mission = new TMission();
					Idx = Mission.FromByteArrayV1(BA, Idx);
					Items[I] = Mission; 
				}
				return Idx;
			}			
		}
		
		public static class TActivity {
			
			public static class TComponent {
				
				public int idTComponent;
				public int idComponent;
				//.
				public TUserLocation GeoLocation = null;
				//.
				public TComponentTypedDataFiles TypedDataFiles = null; 

				public int FromByteArrayV1(byte[] BA, int Idx) throws Exception {
					idTComponent = (int)TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; 
					idComponent = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
					return Idx;
				}

				public int FromByteArrayV2(byte[] BA, int Idx) throws Exception {
					idTComponent = (int)TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; 
					idComponent = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
					boolean flGeoLocation = (BA[Idx] != 0); Idx++;
					if (flGeoLocation) {
						GeoLocation = new TUserLocation();
						Idx = GeoLocation.FromFixByteArray(BA, Idx);
					}
					else
						GeoLocation = null;
					return Idx;
				}

				public String GetName() {
					if ((TypedDataFiles != null) && (TypedDataFiles.Items.length > 0)) 
						return TypedDataFiles.Items[0].DataName; //. ->
					return (Integer.toString(idTComponent)+":"+Integer.toString(idComponent));
				}
			}
			
			public static class TComponents {
				
				public TComponent[] Items = null;

				public int FromByteArrayV1(byte[] BA, int Idx) throws Exception {
					int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
					Items = new TComponent[ItemsCount];
					for (int I = 0; I < ItemsCount; I++) {
						TComponent Component = new TComponent();
						Idx = Component.FromByteArrayV1(BA, Idx);
						Items[I] = Component; 
					}
					return Idx;
				}			

				public int FromByteArrayV2(byte[] BA, int Idx) throws Exception {
					int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
					Items = new TComponent[ItemsCount];
					for (int I = 0; I < ItemsCount; I++) {
						TComponent Component = new TComponent();
						Idx = Component.FromByteArrayV2(BA, Idx);
						Items[I] = Component; 
					}
					return Idx;
				}			
			}
			
			public static int UnknownMissionID = 0;
			public static int UnknownTaskID = 0;
		
			public static double MinTimestamp = 0.0;
			public static double MaxTimestamp = 1000000.0;
			public static double CurrentFinishTimestamp = MaxTimestamp;
			
			public int 		ID = 0;
			public int 		idUser = 0;
			public int 		idMission = UnknownMissionID;
			public int 		idTask = UnknownTaskID;
			public String 	Name = "";
			public String 	Info = null;
			public double 	StartTimestamp = 0.0;
			public double	FinishTimestamp = CurrentFinishTimestamp;
			
			public TActivity() {
			}
			
			public TActivity(int pID) {
				ID = pID;
			}
			
			public boolean IsCurrent() {
				return (FinishTimestamp == CurrentFinishTimestamp);			
			}
			
			public String GetInfo(Context context, boolean flMarkCurrent) {
				String S = "";
				if (flMarkCurrent && IsCurrent())
					S = S+"["+context.getString(R.string.SCurrent)+"]"+" ";
				S = S+Name;
				if (Info != null)
					S = S+" "+"/"+Info+"/";
				return S;
			}
			
			public String GetInfo(Context context) {
				return GetInfo(context,true);
			}
			
			public void SetAsUnknown() {
				ID = -1;
			}
			
			public void SetAsNone() {
				ID = 0;
			}
			
			public boolean IsValid() {
				return ((!IsNone()) && (!IsUnknown()));
			}
			
			public boolean IsNone() {
				return (ID == 0);
			}
			
			public boolean IsUnknown() {
				return (ID == -1);
			}
			
			public int FromByteArrayV1(byte[] BA, int Idx) throws Exception {
				ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
				idMission = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
		    	byte SS = BA[Idx]; Idx++;
		    	if (SS > 0) {
		    		Name = new String(BA, Idx,SS, "windows-1251");
		    		Idx += SS;
		    	}
		    	else
		    		Name = "";
		    	short SS16 = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; 
		    	if (SS16 > 0) {
		    		Info = new String(BA, Idx,SS16, "windows-1251");
		    		Idx += SS16;
		    	}
		    	else
		    		Info = null;
				StartTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
				FinishTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;					
				return Idx;
			}

			public int FromByteArrayV2(byte[] BA, int Idx) throws Exception {
				idMission = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
		    	byte SS = BA[Idx]; Idx++;
		    	if (SS > 0) {
		    		Name = new String(BA, Idx,SS, "windows-1251");
		    		Idx += SS;
		    	}
		    	else
		    		Name = "";
		    	short SS16 = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; 
		    	if (SS16 > 0) {
		    		Info = new String(BA, Idx,SS16, "windows-1251");
		    		Idx += SS16;
		    	}
		    	else
		    		Info = null;
				StartTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
				FinishTimestamp = CurrentFinishTimestamp;
				return Idx;
			}

			public int FromByteArrayV3(byte[] BA, int Idx) throws Exception {
				ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
				idUser = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
				idMission = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
		    	byte SS = BA[Idx]; Idx++;
		    	if (SS > 0) {
		    		Name = new String(BA, Idx,SS, "windows-1251");
		    		Idx += SS;
		    	}
		    	else
		    		Name = "";
		    	short SS16 = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2; 
		    	if (SS16 > 0) {
		    		Info = new String(BA, Idx,SS16, "windows-1251");
		    		Idx += SS16;
		    	}
		    	else
		    		Info = null;
				StartTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
				FinishTimestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
				return Idx;
			}
		}
		
		public static class TActivities {
			
			public TActivity[] Items = null;
			
			public TActivities() {
			}
			
			public int FromByteArray(int idUser, byte[] BA, int Idx) throws Exception {
				int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
				Items = new TActivity[ItemsCount];
				for (int I = 0; I < ItemsCount; I++) {
					TActivity Activity = new TActivity();
					Activity.idUser = idUser;
					Idx = Activity.FromByteArrayV1(BA, Idx);
					Items[I] = Activity; 
				}
				return Idx;
			}			

			public int FromByteArrayV1(int idTask, byte[] BA, int Idx) throws Exception {
				int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
				Items = new TActivity[ItemsCount];
				for (int I = 0; I < ItemsCount; I++) {
					TActivity Activity = new TActivity();
					Activity.idTask = idTask;
					Idx = Activity.FromByteArrayV3(BA, Idx);
					Items[I] = Activity; 
				}
				return Idx;
			}			
		}
		
		public static TUserDescriptor UnknownUser(int UserID) {
			TUserDescriptor Result = new TUserDescriptor();
			Result.UserID = UserID;
			Result.UserIsDisabled = false;
			Result.UserIsOnline = false;
			Result.UserName = "Unknown";
			Result.UserFullName = "Unknown user";
			Result.UserContactInfo = "";
			return Result;
		}
		
		public static TUserDescriptor AnonymouseUser() {
			TUserDescriptor Result = new TUserDescriptor();
			Result.UserID = 2;
			Result.UserIsDisabled = false;
			Result.UserIsOnline = false;
			Result.UserName = "Anonymouse";
			Result.UserFullName = "Anonymouse user";
			Result.UserContactInfo = "";
			return Result;
		}
		
		public static TUserDescriptor RootUser() {
			TUserDescriptor Result = new TUserDescriptor();
			Result.UserID = 1;
			Result.UserIsDisabled = false;
			Result.UserIsOnline = false;
			Result.UserName = "ROOT";
			Result.UserFullName = "Root user";
			Result.UserContactInfo = "";
			return Result;
		}
		
		public int 		UserID;
		public boolean 	UserIsDisabled;
		public boolean 	UserIsOnline;
		public boolean 	UserIsTaskEnabled = false;
		public String 	UserName;
		public String 	UserFullName;
		public String 	UserContactInfo;
		public String	UserDomains = null;
		
		public TUserDescriptor() {
		}
		
		public TUserDescriptor(int pUserID) {
			UserID = pUserID;
		}
		
		public void Assign(TUserDescriptor UD) {
			UserID = UD.UserID;
			UserIsDisabled = UD.UserIsDisabled;
			UserIsOnline = UD.UserIsOnline;
			UserName = UD.UserName;
			UserFullName = UD.UserFullName;
			UserContactInfo = UD.UserContactInfo;
		}
		
		public boolean UserDomainsAreSpecified() {
			return ((UserDomains != null) && (!UserDomains.equals("")));
		}
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
			UserID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
			return FromByteArrayV1(BA, Idx);
		}
	
		public int FromByteArrayV1(byte[] BA, int Idx) throws IOException {
			UserIsDisabled = (BA[Idx] != 0); Idx++;
			UserIsOnline = (BA[Idx] != 0); Idx++;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserName = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserName = "";
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserFullName = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserFullName = "";
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserContactInfo = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserContactInfo = "";
			return Idx;
		}
		
		public int FromByteArrayV2(byte[] BA, int Idx) throws IOException {
			UserIsDisabled = (BA[Idx] != 0); Idx++;
			UserIsOnline = (BA[Idx] != 0); Idx++;
			UserIsTaskEnabled = (BA[Idx] != 0); Idx++;
	    	byte SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserName = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserName = "";
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserFullName = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserFullName = "";
	    	SS = BA[Idx]; Idx++;
	    	if (SS > 0) {
	    		UserContactInfo = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		UserContactInfo = "";
	    	short SS16 = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2;
	    	if (SS16 > 0) {
	    		UserDomains = new String(BA, Idx,SS16, "windows-1251");
	    		Idx += SS16;
	    	}
	    	else
	    		UserDomains = "";
			return Idx;
		}
		
		public byte[] ToByteArray() throws IOException {
			byte[] BA;
			byte[] B1A = new byte[1];
			byte[] Int64Space = new byte[4];
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				BA = TDataConverter.ConvertInt32ToBEByteArray(UserID);
				BOS.write(BA);
				BOS.write(Int64Space);
				//.
				if (UserIsDisabled)
					BA = new byte[] {1};
				else
					BA = new byte[] {0};
				BOS.write(BA);
				//.
				if (UserIsOnline)
					BA = new byte[] {1};
				else
					BA = new byte[] {0};
				BOS.write(BA);
				//.
				B1A[0] = (byte)UserName.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(UserName.getBytes("windows-1251"));
				//.
				B1A[0] = (byte)UserFullName.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(UserFullName.getBytes("windows-1251"));
				//.
				B1A[0] = (byte)UserContactInfo.length();
				BOS.write(B1A);
				if (B1A[0] > 0)
					BOS.write(UserContactInfo.getBytes("windows-1251"));
				//.
				return BOS.toByteArray(); //. ->
			}
			finally {
				BOS.close();
			}
		}		
	}
	
	public static class TUserDescriptors {
		
		private ArrayList<TUserDescriptor> Items = new ArrayList<TUserDescriptor>();
		private boolean flChanged = false;
		
		public void Add(TUserDescriptor pUser) {
			for (int I = 0; I < Items.size(); I++) 
				if (Items.get(I).UserID == pUser.UserID) {
					Items.remove(I);
					break; //. >
				}
			//.
			Items.add(0,pUser);
			flChanged = true;
		}

		public void Remove(int pUserID) {
			for (int I = 0; I < Items.size(); I++) 
				if (Items.get(I).UserID == pUserID) {
					Items.remove(I);
					break; //. >
				}
			flChanged = true;
		}
		
		public TUserDescriptor[] GetItems() {
			TUserDescriptor[] Result = new TUserDescriptor[Items.size()];
			for (int I = 0; I < Result.length; I++)
				Result[I] = Items.get(I);
			return Result;
		}
		
		public TUserDescriptor[] GetItemsOrderDesc() {
			TUserDescriptor[] Result = new TUserDescriptor[Items.size()];
			int Idx = 0;
			for (int I = Result.length-1; I >= 0; I--) {
				Result[Idx] = Items.get(I);
				Idx++;
			}
			return Result;
		}
		
		public boolean IsChanged() {
			return flChanged;
		}
		
		public int FromByteArray(byte[] BA, int Idx) throws IOException {
			short Version = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2;
			if (Version != 0)
				throw new IOException("unknown data version"); //. =>
			int Cnt = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
			for (int I = 0; I < Cnt; I++) {
				TUserDescriptor UD = new TUserDescriptor();
				Idx = UD.FromByteArray(BA, Idx);
				//.
				Items.add(UD);
			}
			//.
			flChanged = false;
			//.
			return Idx;
		}
		
		public byte[] ToByteArray(int MaxItemCount) throws IOException {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(8192);
			try {
				short Version = 0;
				byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(Version);
				BOS.write(BA);
				//.
				int Cnt = Items.size();
				if ((MaxItemCount > 0) && (MaxItemCount < Cnt))
					Cnt = MaxItemCount;
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(Cnt);
				BOS.write(BA);
				//.
				for (int I = 0; I < Cnt; I++) {
					BA = Items.get(I).ToByteArray();
					BOS.write(BA);
				}
				//.
				return BOS.toByteArray(); //. ->
			}
			finally {
				BOS.close();
			}
		}
		
		public int FromFile(String FileName) throws IOException {
			File F = new File(FileName);
			if (F.exists()) { 
		    	FileInputStream FIS = new FileInputStream(F);
		    	try {
		    		byte[] Data = new byte[(int)F.length()];
		    		if (FIS.read(Data) != Data.length)
		    			throw new IOException("error of reading rile, "+FileName); //. =>
		    		int Idx = 0;
		    		return FromByteArray(Data, Idx); //. ->
		    	}
				finally {
					FIS.close(); 
				}
			}
			else
				return 0; //. -> 
		}
		
		public void ToFile(String FileName, int MaxItemCount) throws IOException {
			FileOutputStream FOS = new FileOutputStream(FileName);
	        try
	        {
	        	byte[] Data = ToByteArray(MaxItemCount);
	        	FOS.write(Data);
	        }
	        finally {
	        	FOS.close();
	        }
		}
	}
	
	public static class TUserSecurity {
		
		public static String GetPasswordHash(String UserPassword) {
			String md5Pass;
			try {
		        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
		        digest.update(UserPassword.getBytes("windows-1251"));
		        byte passDigest[] = digest.digest();
		        //.
		        StringBuffer hexPass = new StringBuffer();
		        for (int i=0; i<passDigest.length; i++) {
		                String h = Integer.toHexString(0xFF & passDigest[i]);
		                while (h.length()<2) h = "0" + h;
		                        hexPass.append(h);
		        }
		        md5Pass = hexPass.toString();
			} 
			catch (Exception E) 
			{
				md5Pass = "";
			}
			return md5Pass;
		}
	}
	
	public static class TUserSecurityFiles {
		
		public int	idSecurityFileForPrivate = 0;
		public int	idSecurityFileForClone = 0;
		
		public boolean IsNone() {
			return ((idSecurityFileForPrivate == 0) && (idSecurityFileForClone == 0));
		}
	}
	
	public static class TUserLocation {
		
		public int 		Status = TGPSModule.GPSMODULESTATUS_TEMPORARILYUNAVAILABLE;
		//.
		public int		Datum;
	    public double 	Timestamp;
	    public double 	Latitude;
	    public double 	Longitude;
	    public double 	Altitude;
	    public double 	Speed;
	    public double 	Bearing;
	    public double 	Precision = TGPSFixValue.UnknownFixPrecision;
	    
	    public boolean IsAvailable() {
	        return (Precision != TGPSFixValue.UnknownFixPrecision) && (Precision != TGPSFixValue.UnavailableFixPrecision);
	    }
	    
	    public boolean IsUnknown() {
	        return (Timestamp == 0.0);
	    }
	    
	    public boolean IsNull() {
	        return ((Latitude == 0.0) && (Longitude == 0.0));
	    }
	    
	    public String Info() {
	    	return (Double.toString(Latitude)+"; "+Double.toString(Longitude)+"; "+Double.toString(Altitude));
	    }
	    
	    public void AssignFromGPSFix(TGPSFixValue Fix) {
	    	Status = TGPSModule.GPSMODULESTATUS_AVAILABLE;
	    	//.
		    Timestamp = Fix.TimeStamp;
		    Latitude = Fix.Latitude;
		    Longitude = Fix.Longitude;
		    Altitude = Fix.Altitude;
		    Speed = Fix.Speed;
		    Bearing = Fix.Bearing;
		    Precision = Fix.Precision;
	    }
	    
		public String ToFixString() {
			String Result = 
				Integer.toString(Datum)+","+
				Double.toString(Timestamp)+","+
				Double.toString(Latitude)+","+
				Double.toString(Longitude)+","+
				Double.toString(Altitude)+","+
				Double.toString(Speed)+","+
				Double.toString(Bearing)+","+
				Double.toString(Precision);
			return Result;
		}
		
		public int FromFixByteArray(byte[] BA, int Idx) throws IOException {
	    	Status = TGPSModule.GPSMODULESTATUS_AVAILABLE;
	    	//.
			Datum = TDataConverter.ConvertBEByteArrayToInt16(BA, Idx); Idx += 2;
		    Timestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		    Latitude = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		    Longitude = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		    Altitude = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		    Speed = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		    Bearing = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		    Precision = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
		    return Idx;
		}
		
		public String ToIncomingCommandResponseMessage(int Session) {
			String Result = TGetUserLocationCommandResponseMessage.Prefix+" "+"0"/*Response version*/+";"+
				Integer.toString(Status)+";"+
				//.
				Integer.toString(Datum)+";"+
				Double.toString(Timestamp)+";"+
				Double.toString(Latitude)+";"+
				Double.toString(Longitude)+";"+
				Double.toString(Altitude)+";"+
				Double.toString(Speed)+";"+
				Double.toString(Bearing)+";"+
				Double.toString(Precision)+";"+
				Integer.toString(Session);
			return Result;
		}
		
		public String[] FromIncomingCommandResponseMessage(String Command) throws Exception {
			if (!Command.startsWith(TGetUserLocationCommandResponseMessage.Prefix))
				throw new Exception("incorrect command response prefix"); //. =>
			String ParamsString = Command.substring(TGetUserLocationCommandMessage.Prefix.length()+1/*skip space*/);
			String[] Params = ParamsString.split(";");
			int Version = Integer.parseInt(Params[0]);
			switch (Version) {
			
			case 0:
				Status = Integer.parseInt(Params[1]);
				//.
				Datum = Integer.parseInt(Params[2]);
				Timestamp = Double.parseDouble(Params[3]);
				Latitude = Double.parseDouble(Params[4]);
				Longitude = Double.parseDouble(Params[5]);
				Altitude = Double.parseDouble(Params[6]);
				Speed = Double.parseDouble(Params[7]);
				Bearing = Double.parseDouble(Params[8]);
				Precision = Double.parseDouble(Params[9]);
				//.
				return Params; //. ->
				
			default:
				throw new Exception("unknown command parameters version"); //. =>
			}
		}
	}
	
	public static class TIncomingMessage {
		
		public static final String CommandPrefix = "#";
		public static final String CommandResponsePrefix = "@";
		//.
		public static final String XMLDataPrefix = "<?xml";

		public static TIncomingMessage ToTypedMessage(TIncomingMessage Message) throws Exception {
			if (Message.IsCommand()) {
				if (TGetUserStatusCommandMessage.Check(Message))
					return new TGetUserStatusCommandMessage(Message); //. ->
				else
					if (TGetUserLocationCommandMessage.Check(Message))
						return new TGetUserLocationCommandMessage(Message); //. ->
					else
						if (TLocationCommandMessage.Check(Message))
							return new TLocationCommandMessage(Message); //. ->
						else
							if (TGeoMonitorObjectCommandMessage.Check(Message))
								return new TGeoMonitorObjectCommandMessage(Message); //. ->
							else
								if (TUserTaskStatusCommandMessage.Check(Message))
									return new TUserTaskStatusCommandMessage(Message); //. ->
								else
									return Message; //. ->
			}
			else
				if (Message.IsCommandResponse()) {
					if (TGetUserStatusCommandResponseMessage.Check(Message))
						return new TGetUserStatusCommandResponseMessage(Message); //. ->
					else
						if (TGetUserLocationCommandResponseMessage.Check(Message))
							return new TGetUserLocationCommandResponseMessage(Message); //. ->
						else
							return Message; //. ->
				}
				else
					if (Message.IsXMLData())
						return new TIncomingXMLDataMessage(Message); //. ->
					else
						return Message; //. ->
		}
		
		public String TypeInfo() {
			return "";
		}
		
		public int 				ID = 0;
		//.
		public int 				SenderID;
		public TUserDescriptor 	Sender = null;
		public double 			Timestamp;
		public String 			Message = "";
		//.
		private boolean flProcessed = false;
				
		public TIncomingMessage() {
		}
		
		private TIncomingMessage(int pID) {
			ID = pID;
		}
		
		private TIncomingMessage(TIncomingMessage pMessage)  throws Exception {
			ID = pMessage.ID;
			SenderID = pMessage.SenderID;
			Sender = pMessage.Sender;
			Timestamp = pMessage.Timestamp;
			Message = pMessage.Message;
			flProcessed = pMessage.flProcessed;
		}
		
		protected void Construct() throws Exception {
		}
		
		protected void Parse() throws Exception {
		}
		
		public boolean IsCommand() {
			return ((Message != null) && Message.startsWith(CommandPrefix));
		}

		public boolean IsCommandResponse() {
			return ((Message != null) && Message.startsWith(CommandResponsePrefix));
		}
		
		public boolean IsXMLData() {
			return ((Message != null) && Message.startsWith(XMLDataPrefix));
		}

		public String GetInfo() {
			return Message;
		}
		
		public synchronized boolean IsProcessed() {
			return flProcessed;
		}
		
		public synchronized void SetAsProcessed() {
			flProcessed = true;
		}
		
		public int FromByteArrayV1(byte[] BA, int Idx) throws Exception {
	    	SenderID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
	    	//.
	    	Timestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
	    	//.
	    	int SS = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
	    	if (SS > 0) {
	    		Message = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Message = "";
	    	//.
	    	flProcessed = (BA[Idx] != 0); Idx++;
	    	//.
	    	Parse();
			//.
			return Idx;
		}
		
		public int FromByteArrayV2(byte[] BA, int Idx) throws Exception {
	    	ID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
	    	//.
	    	SenderID = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 8; //. Int64
	    	//.
	    	Timestamp = TDataConverter.ConvertBEByteArrayToDouble(BA, Idx); Idx += 8;
	    	//.
	    	int SS = TDataConverter.ConvertBEByteArrayToInt32(BA, Idx); Idx += 4;
	    	if (SS > 0) {
	    		Message = new String(BA, Idx,SS, "windows-1251");
	    		Idx += SS;
	    	}
	    	else
	    		Message = "";
	    	//.
	    	flProcessed = (BA[Idx] != 0); Idx++;
	    	//.
	    	Parse();
			//.
			return Idx;
		}
		
		public byte[] ToByteArrayV1() throws IOException {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				byte[] BA;
				byte[] Int64Space = new byte[4];
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(SenderID);
				BOS.write(BA);
				BOS.write(Int64Space);
				//.
				BA = TDataConverter.ConvertDoubleToBEByteArray(Timestamp);
				BOS.write(BA);
				//.
				int SS = Message.length();
				BA = TDataConverter.ConvertInt32ToBEByteArray(SS);
				BOS.write(BA);
				if (SS > 0)
					BOS.write(Message.getBytes("windows-1251"));
				//. 
				BA = new byte[1];
				if (flProcessed)
					BA[0] = 1;
				BOS.write(BA);
				//.
				return BOS.toByteArray();
			}
			finally {
				BOS.close();
			}
		}				

		public byte[] ToByteArrayV2() throws IOException {
			ByteArrayOutputStream BOS = new ByteArrayOutputStream(1024);
			try {
				byte[] BA;
				byte[] Int64Space = new byte[4];
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(ID);
				BOS.write(BA);
				BOS.write(Int64Space);
				//.
				BA = TDataConverter.ConvertInt32ToBEByteArray(SenderID);
				BOS.write(BA);
				BOS.write(Int64Space);
				//.
				BA = TDataConverter.ConvertDoubleToBEByteArray(Timestamp);
				BOS.write(BA);
				//.
				int SS = Message.length();
				BA = TDataConverter.ConvertInt32ToBEByteArray(SS);
				BOS.write(BA);
				if (SS > 0)
					BOS.write(Message.getBytes("windows-1251"));
				//. 
				BA = new byte[1];
				if (flProcessed)
					BA[0] = 1;
				BOS.write(BA);
				//.
				return BOS.toByteArray();
			}
			finally {
				BOS.close();
			}
		}				
	}
	
	public static class TIncomingCommandMessage extends TIncomingMessage {
		
		public int Version = 0;
		public int Session = 0;
		
		public TIncomingCommandMessage() {
		}
		
		public TIncomingCommandMessage(TIncomingMessage pMessage)  throws Exception {
			super(pMessage);
		}
		
		@Override
		protected void Parse() throws Exception {
			String[] Params = ParseParams();
			if (Params != null) {
				try {
					Version = Integer.parseInt(Params[0]);
				}
				catch (NumberFormatException NFE) {
					Version = 0;
				}
				//.
				int SessionIdx = Params.length-1;
				if ((Params[SessionIdx] != null) && (!Params[SessionIdx].equals("")))
					try {
						Session = Integer.parseInt(Params[SessionIdx]);
					}
					catch (NumberFormatException NFE) {
						Session = 0;
					}
			}
		}
		
		protected String[] ParseParams() throws Exception {
			return null;
		}
	}
	
	public static class TIncomingCommandResponseMessage extends TIncomingMessage {
		
		public int Version = 0;
		public int Session = 0;
		
		public TIncomingCommandResponseMessage() {
		}
		
		public TIncomingCommandResponseMessage(TIncomingMessage pMessage)  throws Exception {
			super(pMessage);
		}

		@Override
		protected void Parse() throws Exception {
			String[] Response = ParseResponse();
			if (Response != null) {
				try {
					Version = Integer.parseInt(Response[0]);
				}
				catch (NumberFormatException NFE) {
					Version = 0;
				}
				int SessionIdx = Response.length-1;
				if ((Response[SessionIdx] != null) && (!Response[SessionIdx].equals("")))
					try {
						Session = Integer.parseInt(Response[SessionIdx]);
					}
					catch (NumberFormatException NFE) {
						Session = 0;
					}
			}
		}
		
		protected String[] ParseResponse() throws Exception {
			return null;
		}
	}
	
	public static class TIncomingXMLDataMessage extends TIncomingMessage {
		
		public int Version = 0;
		//.
		public String DataType = "";
		public byte[] Data = null;
		
		public TIncomingXMLDataMessage() {
		}
		
		public TIncomingXMLDataMessage(TIncomingMessage pMessage)  throws Exception {
			super(pMessage);
			//.
			Parse();
		}

		public TIncomingXMLDataMessage(String pDataType, byte[] pData) throws Exception {
			DataType = pDataType;
			Data = pData;
			//.
			Construct();
		}
		
		@Override
		protected void Parse() throws Exception {
			byte[] XML = Message.getBytes("UTF-8");
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
    		Version = Integer.parseInt(RootNode.getElementsByTagName("Version").item(0).getFirstChild().getNodeValue());
			switch (Version) {
			
			case 1:
				Node node = RootNode.getElementsByTagName("DataType").item(0).getFirstChild();
				if (node != null)
					DataType = node.getNodeValue();
				else
					DataType = "";
				node = RootNode.getElementsByTagName("Data").item(0).getFirstChild();
				if (node != null) {
					String _Data = node.getNodeValue();
					Data = com.geoscope.Utils.Base64Codec.obsolete_2_1.Base64.decode(_Data, com.geoscope.Utils.Base64Codec.obsolete_2_1.Base64.NO_WRAP);
				}
				else 
					Data = null;
				break; //. >
				
			default:
				throw new Exception("unknown XML data version, version: "+Integer.toString(Version)); //. =>
			}
		}

		@Override
		protected void Construct() throws Exception {
			Timestamp = OleDate.UTCCurrentTimestamp();
			//.
			Version = 1;
		    XmlSerializer Serializer = Xml.newSerializer();
		    ByteArrayOutputStream BOS = new ByteArrayOutputStream();
		    try {
		        Serializer.setOutput(BOS,"UTF-8");
		        Serializer.startDocument("UTF-8",true);
		        Serializer.startTag("", "ROOT");
		        //. Version
	            Serializer.startTag("", "Version");
	            Serializer.text(Integer.toString(Version));
	            Serializer.endTag("", "Version");
		        //. DataType
	            Serializer.startTag("", "DataType");
	            Serializer.text(DataType);
	            Serializer.endTag("", "DataType");
		        //. Data
	            Serializer.startTag("", "Data");
	            if (Data != null) {
	            	String _Data = com.geoscope.Utils.Base64Codec.obsolete_2_1.Base64.encodeToString(Data, com.geoscope.Utils.Base64Codec.obsolete_2_1.Base64.NO_WRAP);
		            Serializer.text(_Data);
	            }
	            Serializer.endTag("", "Data");
		        //.
		        Serializer.endTag("", "ROOT");
		        Serializer.endDocument();
		        //.
				byte[] BA = BOS.toByteArray();
				Message = new String(BA,"UTF-8");
		    }
		    finally {
		    	BOS.close();
		    }
		}		
	}
	
	public static class TGetUserStatusCommandMessage extends TIncomingCommandMessage {

		public static final String Prefix = "#GETUSERSTATUS";
		
		public static boolean Check(TIncomingMessage Message) {
			return Message.Message.startsWith(Prefix);
		}

		@Override
		public String TypeInfo() {
			return "GetUserStatus";
		}
		
		public TGetUserStatusCommandMessage(TIncomingMessage BaseMessage) throws Exception {
			super(BaseMessage);
		}
		
		@Override
		public String GetInfo() {
			return "";
		}		
	}
	
	public static class TGetUserStatusCommandResponseMessage extends TIncomingCommandResponseMessage {

		public static final String Prefix = "@USERSTATUS";
		
		public static boolean Check(TIncomingMessage Message) {
			return Message.Message.startsWith(Prefix);
		}

		@Override
		public String TypeInfo() {
			return "GetUserStatusResponse";
		}
		
		public int Status;
		
		public TGetUserStatusCommandResponseMessage(int pSession, int pStatus) throws Exception {
			Session = pSession;
			//.
			Status = pStatus;
			//.
			Construct();
		}
		
		public TGetUserStatusCommandResponseMessage(TIncomingMessage BaseMessage) throws Exception {
			super(BaseMessage);
			//.
			Parse();
		}
		
		@Override
		protected void Construct() throws Exception {
			Timestamp = OleDate.UTCCurrentTimestamp();
			Message = Prefix+" "+Integer.toString(Status)+";"+Integer.toString(Session);
		}
		
		@Override
		protected String[] ParseResponse() throws Exception {
			String ResponseString = Message.substring(Prefix.length()+1/*skip space*/);
			String[] Response = ResponseString.split(";");				
			//.
			Status = Integer.parseInt(Response[0]);
			//.
			return Response;
		}
		
		@Override
		public String GetInfo() {
			return "Status: "+Integer.toString(Status);
		}		
	}
	
	public static class TGetUserLocationCommandMessage extends TIncomingCommandMessage {

		public static final String Prefix = "#USERLOCATION";
		
		public static final int Version_GetFix 		= 0;
		public static final int Version_ObtainFix 	= 1;
		
		public static boolean Check(TIncomingMessage Message) {
			return Message.Message.startsWith(Prefix);
		}

		@Override
		public String TypeInfo() {
			return "GetUserLocation";
		}
		
		public TGetUserLocationCommandMessage(int pVersion) throws Exception {
			Version = pVersion;
			Session = IncomingMessages_GetNewCommandSession();
			//.
			Construct();
		}
		
		public TGetUserLocationCommandMessage(TIncomingMessage BaseMessage) throws Exception {
			super(BaseMessage);
			//.
			Parse();
		}
		
		@Override
		protected void Construct() throws Exception {
			Timestamp = OleDate.UTCCurrentTimestamp();
			Message = Prefix+" "+Integer.toString(Version)+";"+Integer.toString(Session);
		}
		
		@Override
		protected String[] ParseParams() throws Exception {
			if (!Message.startsWith(Prefix))
				throw new Exception("incorrect command prefix"); //. =>
			String ParamsString = Message.substring(TGetUserLocationCommandMessage.Prefix.length()+1/*skip space*/);
			String[] Params = ParamsString.split(";");
			return Params;
		}
		
		@Override
		public String GetInfo() {
			return "";
		}		
	}
	
	public static class TGetUserLocationCommandResponseMessage extends TIncomingCommandResponseMessage {

		public static final String Prefix = "@USERLOCATION";
		
		public static boolean Check(TIncomingMessage Message) {
			return Message.Message.startsWith(Prefix);
		}

		@Override
		public String TypeInfo() {
			return "GetUserLocationResponse";
		}
		
		public TUserLocation UserLocation;
		
		public TGetUserLocationCommandResponseMessage(int pSession, TUserLocation pUserLocation) throws Exception {
			Session = pSession;
			//.
			UserLocation = pUserLocation;
			//.
			Construct();
		}
		
		public TGetUserLocationCommandResponseMessage(TIncomingMessage BaseMessage) throws Exception {
			super(BaseMessage);
			//.
			UserLocation = new TUserLocation();
			//.
			Parse();
		}
		
		@Override
		protected void Construct() throws Exception {
			Timestamp = OleDate.UTCCurrentTimestamp();
			Message = UserLocation.ToIncomingCommandResponseMessage(Session);
		}
		
		@Override
		protected String[] ParseResponse() throws Exception {
			return UserLocation.FromIncomingCommandResponseMessage(Message);
		}
		
		@Override
		public String GetInfo() {
			return UserLocation.Info();
		}		
	}
	
	public static class TLocationCommandMessage extends TIncomingCommandMessage {

		public static final String Prefix = "#LOCATION";
		
		public static final int Version_0	= 0;
		
		public static boolean Check(TIncomingMessage Message) {
			return Message.Message.startsWith(Prefix);
		}

		@Override
		public String TypeInfo() {
			return "Location";
		}
		
		public TLocation Location = null;
		
		public TLocationCommandMessage(int pVersion, TLocation pLocation) throws Exception {
			Version = pVersion;
			Session = IncomingMessages_GetNewCommandSession();
			//.
			Location = pLocation;
			//.
			Construct();
		}
		
		public TLocationCommandMessage(TIncomingMessage BaseMessage) throws Exception {
			super(BaseMessage);
			//.
			Location = new TLocation();
			//.
			Parse();
		}
		
		@Override
		protected void Construct() throws Exception {
			Timestamp = OleDate.UTCCurrentTimestamp();
			Message = Location.ToIncomingCommandMessage(Version, Session);
		}
		
		@Override
		protected String[] ParseParams() throws Exception {
			return Location.FromIncomingCommandMessage(Message);
		}
		
		@Override
		public String GetInfo() {
			return Location.Name;
		}		
	}
	
	public static class TGeoMonitorObjectCommandMessage extends TIncomingCommandMessage {

		public static final String Prefix = "#GEOMONITOROBJECT";
		
		public static final int Version_0	= 0;
		
		public static boolean Check(TIncomingMessage Message) {
			return Message.Message.startsWith(Prefix);
		}

		@Override
		public String TypeInfo() {
			return "GeoMonitorObject";
		}
		
		public TReflectorCoGeoMonitorObject CoGeoMonitorObject = null;
		
		public TGeoMonitorObjectCommandMessage(int pVersion, TReflectorCoGeoMonitorObject pCoGeoMonitorObject) throws Exception {
			Version = pVersion;
			Session = IncomingMessages_GetNewCommandSession();
			//.
			CoGeoMonitorObject = pCoGeoMonitorObject;
			//.
			Construct();
		}
		
		public TGeoMonitorObjectCommandMessage(TIncomingMessage BaseMessage) throws Exception {
			super(BaseMessage);
			//.
			CoGeoMonitorObject = new TReflectorCoGeoMonitorObject();
			//.
			Parse();
		}
		
		@Override
		protected void Construct() throws Exception {
			Timestamp = OleDate.UTCCurrentTimestamp();
			Message = CoGeoMonitorObject.ToIncomingCommandMessage(Version, Session);
		}
		
		@Override
		protected String[] ParseParams() throws Exception {
			return CoGeoMonitorObject.FromIncomingCommandMessage(Message);
		}
		
		@Override
		public String GetInfo() {
			return CoGeoMonitorObject.Name;
		}		
	}
	
	public static class TUserTaskStatusCommandMessage extends TIncomingCommandMessage {

		public static final String Prefix = "#USERTASKSTATUS";
		
		public static final int Version_1 = 1;
		
		public static boolean Check(TIncomingMessage Message) {
			return Message.Message.startsWith(Prefix);
		}

		@Override
		public String TypeInfo() {
			return "UserTaskStatus";
		}
		
		public TUserTaskStatusDescriptor UserTaskStatusDescriptor = null;
		
		public TUserTaskStatusCommandMessage(int pVersion, TUserTaskStatusDescriptor pUserTaskStatusDescriptor) throws Exception {
			Version = pVersion;
			Session = IncomingMessages_GetNewCommandSession();
			//.
			UserTaskStatusDescriptor = pUserTaskStatusDescriptor;
			//.
			Construct();
		}
		
		public TUserTaskStatusCommandMessage(TIncomingMessage BaseMessage) throws Exception {
			super(BaseMessage);
			//.
			UserTaskStatusDescriptor = new TUserTaskStatusDescriptor();
			//.
			Parse();
		}
		
		@Override
		protected void Construct() throws Exception {
			Timestamp = OleDate.UTCCurrentTimestamp();
			Message = UserTaskStatusDescriptor.ToIncomingCommandMessage(Version, Session);
		}
		
		@Override
		protected String[] ParseParams() throws Exception {
			return UserTaskStatusDescriptor.FromIncomingCommandMessage(Message);
		}
		
		@Override
		public String GetInfo() {
			return "UserTaskStatus: "+Integer.toString(UserTaskStatusDescriptor.Status);
		}		
	}
	
	@SuppressLint("HandlerLeak")
	public static class TIncomingMessages extends TCancelableThread {
		
		public static String 		MessagesFileName() {
			return TReflector.ProfileFolder()+"/"+"UserIncomingMessages.dat";
		}
		//.
		public static final int 	Messages_ProcessedMaxCount = 5; 
		
		public static abstract class TReceiver {
			
			public abstract boolean DoOnMessage(TGeoScopeServerUser User, TIncomingMessage Message);
			public abstract boolean DoOnCommand(TGeoScopeServerUser User, TIncomingCommandMessage Message);
			public abstract boolean DoOnCommandResponse(TGeoScopeServerUser User, TIncomingCommandResponseMessage Message);
		}
		
		public static class TReceiverMessage {
			public TReceiver 		Receiver;
			public TIncomingMessage Message;
			
			public TReceiverMessage(TReceiver pReceiver, TIncomingMessage pMessage) {
				Receiver = pReceiver;
				Message = pMessage;
			}
		}
		
		public static class TCommandReceiver extends TReceiver {
			
			@Override
			public boolean DoOnMessage(TGeoScopeServerUser User, TIncomingMessage Message) {
				return false;
			}
			
			@Override
			public boolean DoOnCommand(TGeoScopeServerUser User, TIncomingCommandMessage Message) {
				return ProcessCommand(Message);
			}
			
			@Override
			public boolean DoOnCommandResponse(TGeoScopeServerUser User, TIncomingCommandResponseMessage Message) {
				return false;
			}
			
			protected boolean ProcessCommand(TIncomingCommandMessage Message) {
				Message.SetAsProcessed();
				return true;
			}
		}
		
		public static class TCommandResponseReceiver extends TReceiver {
			
			public static final int InfiniteTimeout = Integer.MAX_VALUE;
			
			private int Session = 0;
			//.
			private TIncomingCommandResponseMessage ResponseMessage = null;
			
			//.
			public TAutoResetEvent 	ReceivedSignal = new TAutoResetEvent();
			public boolean 			Received = false;
			
			public TCommandResponseReceiver(int pSession) {
				Session = pSession;
			}
			
			@Override
			public boolean DoOnMessage(TGeoScopeServerUser User, TIncomingMessage Message) {
				return false;
			}
			
			@Override
			public boolean DoOnCommand(TGeoScopeServerUser User, TIncomingCommandMessage Message) {
				return false;
			}
			
			@Override
			public boolean DoOnCommandResponse(TGeoScopeServerUser User, TIncomingCommandResponseMessage Message) {
				if (Message.Session == Session) {
					ResponseMessage = Message;
					//.
					Received = true;
					ReceivedSignal.Set();
					//.
					return ProcessCommandResponse(Message); //. ->
				}
				else
					return false; //. ->
			}
			
			protected boolean ProcessCommandResponse(TIncomingCommandResponseMessage Message) {
				Message.SetAsProcessed();
				return true;
			}
			
			public TIncomingCommandResponseMessage WaitForMessage(int Timeout) throws InterruptedException {
				ReceivedSignal.WaitOne(Timeout);
				if (!Received)
					return null; //. ->
				//.
				TIncomingCommandResponseMessage Result = ResponseMessage;
				//.
				Reset();					
				//.
				return Result;
			}
			
			public TIncomingCommandResponseMessage WaitForMessage() throws InterruptedException {
				return WaitForMessage(InfiniteTimeout);
			}
			
			public void Reset() {
				synchronized (ReceivedSignal) {
					ResponseMessage = null;
					Received = false;
				}
			}
		}
		
		public static class TCommandHandler {
			
			protected TIncomingCommandMessage CommandMessage;
			protected TGeoScopeServerUser User;
			
			public TCommandHandler(TIncomingCommandMessage pCommandMessage, TGeoScopeServerUser pUser) {
				CommandMessage = pCommandMessage;
				User = pUser;
			}
			
			public void Process() throws Exception {
				CommandMessage.SetAsProcessed();
			}
		}
		
    	private static class TMessageHandler extends Handler {
    		
    		private TIncomingMessages IncomingMessages;
    		
    		public TMessageHandler(TIncomingMessages pIncomingMessages) {
    			IncomingMessages = pIncomingMessages;
    		}
    		
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
					case MESSAGE_EXCEPTION:
						String EStr = (String)msg.obj;
						Toast.makeText(IncomingMessages.User.Server.context,IncomingMessages.User.Server.context.getString(R.string.SError)+EStr,Toast.LENGTH_LONG).show();
						// .
						break; // . >

		            case MESSAGE_RECEIVED:
		            	TIncomingMessage Message = (TIncomingMessage)msg.obj;
		            	IncomingMessages.DispatchMessage(Message);
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_RECEIVEDFORRECEIVER:
		            	TReceiverMessage ReceiverMessage = (TReceiverMessage)msg.obj;
		            	IncomingMessages.ProcessMessage(ReceiverMessage.Message,ReceiverMessage.Receiver);
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_RESTORED:
		            	Message = (TIncomingMessage)msg.obj;
	            		if (!IncomingMessages.DispatchMessage(Message))
	            			Message.SetAsProcessed(); //. set un-handled message as processed on restoring
		            	//.
		            	break; //. >

		            case MESSAGE_RESTOREDFORRECEIVER:
		            	ReceiverMessage = (TReceiverMessage)msg.obj;
	            		if (!IncomingMessages.ProcessMessage(ReceiverMessage.Message,ReceiverMessage.Receiver))
	            			ReceiverMessage.Message.SetAsProcessed(); //. set un-handled message as processed on restoring
		            	//.
		            	break; //. >	            	
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
    	}
    	
    	public static final int WaitForInternetConnectionInterval = 1000*30; //. seconds
    	//.
    	public static final int SlowCheckInterval 	= 600; //. seconds
		public static final int MediumCheckInterval = 60; //. seconds
		public static final int FastCheckInterval 	= 5; //. seconds
		public static final int DefaultCheckInterval = SlowCheckInterval; 
				//.
		public static final int CheckInterval_SessionMultiplier = 10; 
		//.
		public static final int MESSAGE_EXCEPTION 			= 0;
		public static final int MESSAGE_RECEIVED 			= 1;
		public static final int MESSAGE_RECEIVEDFORRECEIVER	= 2;
		public static final int MESSAGE_RESTORED 			= 3;
		public static final int MESSAGE_RESTOREDFORRECEIVER = 4;
		
		private TGeoScopeServerUser User;
		//.
		private List<TIncomingMessage>				Messages = Collections.synchronizedList(new ArrayList<TIncomingMessage>());
		private Hashtable<Integer, TUserDescriptor> Senders = new Hashtable<Integer, TUserDescriptor>();
		//.
		private Object 		InitializationFlag = new Object();
		private boolean 	flInitializing = false;
		//.
		private int 			CheckInterval = DefaultCheckInterval;
		private TAutoResetEvent	CheckSignal = new TAutoResetEvent();
		private boolean 		flCheck = false;
		//.
		private List<TReceiver> Receivers = Collections.synchronizedList(new ArrayList<TReceiver>()); 
		//.
		private TMessageHandler MessageHandler;
		
		public TIncomingMessages(TGeoScopeServerUser pUser) throws Exception {
			User = pUser;
			//.
			MessageHandler = new TMessageHandler(this);
			//.
			_Thread = new Thread(this);
			_Thread.start();
		}
		
		public void Destroy() throws IOException {
			//. force work thread to exit
			Cancel();
			Check(); 
			//.
			Wait();
			//.
			if (MessageHandler != null) {
				MessageHandler = null;
			}
		}
		
		private void LoadMessages() throws Exception {
			synchronized (Messages) {
				Messages.clear();
				//.
				File F = new File(MessagesFileName());
				if (F.exists()) { 
			    	FileInputStream FIS = new FileInputStream(MessagesFileName());
			    	try {
		    			byte[] BA = new byte[4];
	    				FIS.read(BA, 0,2);
		    			short Version = TDataConverter.ConvertBEByteArrayToInt16(BA, 0);
		    			switch (Version) {

		    			case 1: {
			    			FIS.read(BA);
				    		int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, 0);
				    		//.
		            		byte[] MessageDataSizeBA = new byte[4];
			        		for (int I = 0; I < ItemsCount; I++) {
								FIS.read(MessageDataSizeBA);
								int MessageDataSize = TDataConverter.ConvertBEByteArrayToInt32(MessageDataSizeBA, 0);
								if (MessageDataSize > 0) {
						    		byte[] MessageData = new byte[MessageDataSize];
									FIS.read(MessageData, 0,MessageDataSize);
									//.
									TIncomingMessage Message = new TIncomingMessage();
									Message.FromByteArrayV1(MessageData,0);
									TIncomingMessage TypedMessage = TIncomingMessage.ToTypedMessage(Message);
									Messages.add(TypedMessage);
									Message.ID = Messages.size(); 
								}
			        		}
		    				break; //. >
		    			}
		    				
		    			case 2: {
			    			FIS.read(BA);
				    		int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(BA, 0);
				    		//.
		            		byte[] MessageDataSizeBA = new byte[4];
			        		for (int I = 0; I < ItemsCount; I++) {
								FIS.read(MessageDataSizeBA);
								int MessageDataSize = TDataConverter.ConvertBEByteArrayToInt32(MessageDataSizeBA, 0);
								if (MessageDataSize > 0) {
						    		byte[] MessageData = new byte[MessageDataSize];
									FIS.read(MessageData, 0,MessageDataSize);
									//.
									TIncomingMessage Message = new TIncomingMessage();
									Message.FromByteArrayV2(MessageData,0);
									TIncomingMessage TypedMessage = TIncomingMessage.ToTypedMessage(Message);
									Messages.add(TypedMessage);
								}
			        		}
		    				break; //. >
		    			}
		    				
		    			default:
		    				throw new IOException("unknown message file version"); //. =>
		    			}
			    	}
					finally
					{
						FIS.close(); 
					}
				}
			}
		}
		
		private void SaveMessages() throws IOException {
			synchronized (Messages) {
				String MessagesTempFileName = MessagesFileName()+".tmp";
				FileOutputStream FOS = new FileOutputStream(MessagesTempFileName);
		        try
		        {
		        	short Version = 2;
		        	byte[] BA = TDataConverter.ConvertInt16ToBEByteArray(Version);
		        	FOS.write(BA);
		        	int ItemsCount = Messages.size();
		        	BA = TDataConverter.ConvertInt32ToBEByteArray(ItemsCount);
		        	FOS.write(BA);
		        	for (int I = 0; I < ItemsCount; I++) {
		        		TIncomingMessage Message = Messages.get(I);
		        		BA = Message.ToByteArrayV2();
		        		int MessageDataSize = BA.length;
		        		byte[] MessageDataSizeBA = TDataConverter.ConvertInt32ToBEByteArray(MessageDataSize);
		    			FOS.write(MessageDataSizeBA);
		    			if (MessageDataSize > 0)
		    				FOS.write(BA);
		        	}
		        }
		        finally
		        {
		        	FOS.close();
		        }
				File TF = new File(MessagesTempFileName);
				File F = new File(MessagesFileName());
				TF.renameTo(F);
			}
		}
		
		private void PackMessages(int ProcessedMaxCount) {
			synchronized (Messages) {
				ArrayList<TIncomingMessage> _NewMessages = new ArrayList<TIncomingMessage>(Messages.size());
				for (int I = (Messages.size()-1); I >= 0; I--) {
					TIncomingMessage Message = Messages.get(I);
					if (Message.IsProcessed()) {
						if (ProcessedMaxCount > 0) {
							_NewMessages.add(0,Message);
							ProcessedMaxCount--;
						}
					}
					else
						_NewMessages.add(0,Message);
				}
				Messages.clear();
				Messages.addAll(_NewMessages); 
			}
		}
		
		private void PackAndSaveMessages() throws IOException {
			PackMessages(Messages_ProcessedMaxCount);
			SaveMessages();
		}
		
		public void Load() throws Exception {
			LoadMessages();
		}
		
		public void Save() throws IOException {
			PackAndSaveMessages();
		}
		
		public void AddMessage(TIncomingMessage Message) {
    		synchronized (Messages) {
    			Messages.add(Message);
    		}
    	}
		
		public TIncomingMessage GetMessage(int MessageIndex) {
    		synchronized (Messages) {
    			if ((0 <= MessageIndex) && (MessageIndex < Messages.size()))
    				return Messages.get(MessageIndex); //. ->
    			else
    				return null; //. ->
			}
    	}
		
		public TIncomingMessage GetMessageByID(int MessageID) {
    		synchronized (Messages) {
    			for (int I = 0; I < Messages.size(); I++) {
    				TIncomingMessage Message = Messages.get(I);
    				if (Message.ID == MessageID)
    					return Message; //. =>
    			}
    			return null; //. ->
			}
    	}
		
		public ArrayList<TIncomingMessage> GetMessages() {
    		synchronized (Messages) {
    			return new ArrayList<TIncomingMessage>(Messages);
			}
    	}
		
    	@Override
        public void run() {
    		try {
    			try {
					flInitializing = true;
					try {
	    				synchronized (InitializationFlag) {
	    					try {
	            				Load();
	                			//. process restored messages
	            				ArrayList<TIncomingMessage> _Messages;
	            				synchronized (Messages) {
	            					_Messages = new ArrayList<TIncomingMessage>(Messages); 
	        					}
	            				for (int I = 0; I < _Messages.size(); I++) {
	            					if (Canceller.flCancel)
	            						return; //. ->
	            					//.
	            					TIncomingMessage TypedMessage = _Messages.get(I);
	            					if (!TypedMessage.IsProcessed()) {
	                    				//. supply message with sender info
	                    				TUserDescriptor Sender = Senders.get(TypedMessage.SenderID);
	                    				if ((Sender == null) && User.Server.IsNetworkAvailable()) {
	                    					try {
	                    						Sender = User.GetUserInfo(TypedMessage.SenderID);
	                    					}
	                    					catch (Exception E) {
	                    						Sender = TUserDescriptor.UnknownUser(TypedMessage.SenderID);
	                    					}
	                						Senders.put(TypedMessage.SenderID, Sender);
	                    				}
	                    				TypedMessage.Sender = Sender;
	                    				//.
	                    				@SuppressWarnings("unused")
										boolean flDispatch = true;
	                    				//. 
	                    				if (TypedMessage.IsCommand()) { 
	                        				//. process system as commands
	                    					flDispatch = !ProcessMessageAsSystemCommand((TIncomingCommandMessage)TypedMessage);
	                    				}
	                        			//. dispatch message
	                    				/* if (flDispatch)
	                    					MessageHandler.obtainMessage(MESSAGE_RESTORED,TypedMessage).sendToTarget();*/
	                    				//.
	                    				if (Canceller.flCancel)
	                    					throw new CancelException(); //. =>
	            					}
	            				}
	    					}
	    					finally {
	    						InitializationFlag.notifyAll();
	    					}
						}
					}
					finally {
						flInitializing = false;
					}
    			}
            	catch (InterruptedException E) {
            		throw E; //. =>
            	}
            	catch (CancelException CE) {
            		throw CE; //. =>
            	}
    			catch (Exception E) {
            		String S = E.getMessage();
            		if (S == null)
            			S = E.getClass().getName();
        			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,User.Server.context.getString(R.string.SErrorOfImcomingMessageReceiving)+": "+S).sendToTarget();
    			}
    			//. receiving incoming messages ...
    			try {
            		while (!Canceller.flCancel) {
            			try {
            				//. waiting for Internet connection
            				while (!User.Server.IsNetworkAvailable()) 
            					Thread.sleep(WaitForInternetConnectionInterval);
            				//. check messages
                			int[] MessagesIDs = User.IncomingMessages_GetUnread();
            				//.
            				if (Canceller.flCancel)
            					throw new CancelException(); //. =>
            				//.
                			if (MessagesIDs != null) {
                    			for (int I = 0; I < MessagesIDs.length; I++) {
                    				TIncomingMessage Message = User.IncomingMessages_GetMessage(MessagesIDs[I]);
                    				//. convert message to typed message
                    				TIncomingMessage TypedMessage = TIncomingMessage.ToTypedMessage(Message);
                    				//. supply message with sender info
                    				TUserDescriptor Sender = Senders.get(TypedMessage.SenderID);
                    				if (Sender == null) {
                    					Sender = User.GetUserInfo(TypedMessage.SenderID);
                    					Senders.put(TypedMessage.SenderID, Sender);
                    				}
                    				TypedMessage.Sender = Sender;
                    				//. add new message to the list
                    				AddMessage(TypedMessage);
                    				//.
                    				boolean flDispatch = true;
                    				//. 
                    				if (TypedMessage instanceof TIncomingCommandMessage) { 
                        				//. process as system commands
                    					flDispatch = !ProcessMessageAsSystemCommand((TIncomingCommandMessage)TypedMessage);
                    				}
                        			//. dispatch message
                    				if (flDispatch)
                    					MessageHandler.obtainMessage(MESSAGE_RECEIVED,TypedMessage).sendToTarget();
                    				//.
                    				if (Canceller.flCancel)
                    					throw new CancelException(); //. =>
                    			}
                			}
            			}
                    	catch (InterruptedException E) {
                    		throw E; //. =>
                    	}
                    	catch (CancelException CE) {
                    		throw CE; //. =>
                    	}
            			catch (Exception E) {
                    		String S = E.getMessage();
                    		if (S == null)
                    			S = E.getClass().getName();
                			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,User.Server.context.getString(R.string.SErrorOfImcomingMessageReceiving)+": "+S).sendToTarget();
            			}
            			//.
            			for (int I = 0; I < GetCheckInterval(); I++) {
							CheckSignal.WaitOne(60000); 
            				//.
            				if (Canceller.flCancel)
            					throw new CancelException(); //. =>
            				//.
            				if (flCheck) {
            		    		flCheck = false;
            		    		break; //. >
            				}
            			}
            		}
    			}
    			finally {
    				Save();
    			}
    		}
        	catch (InterruptedException E) {
        	}
        	catch (CancelException CE) {
        	}
        	catch (Throwable E) {
        		///- TDEVICEModule.Log_WriteCriticalError(E);
        		String S = E.getMessage();
        		if (S == null)
        			S = E.getClass().getName();
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,User.Server.context.getString(R.string.SErrorOfImcomingMessageReceiving)+": "+S).sendToTarget();
        	}
    	}
    	
    	private synchronized int GetCheckInterval() {
    		int Result = CheckInterval;
    		if (User.InSession())
    			Result *= CheckInterval_SessionMultiplier;
    		return Result;
    	}
    	
    	public synchronized int SetCheckInterval(int Value) {
    		int Result = CheckInterval;
    		CheckInterval = Value;
    		return Result;
    	}
    	
    	public int SetSlowCheckInterval() {
    		return SetCheckInterval(SlowCheckInterval);
    	}
    	
    	public int SetMediumCheckInterval() {
    		return SetCheckInterval(MediumCheckInterval);
    	}
    	
    	public int SetFastCheckInterval() {
    		return SetCheckInterval(FastCheckInterval);
    	}
    	
    	public void RestoreCheckInterval(int LastValue) {
    		SetCheckInterval(LastValue);
    	}
    	
    	public void Check() {
    		flCheck = true;
    		CheckSignal.Set();
    	}
    	
    	public void AddReceiver(TReceiver Receiver, boolean flReceiveLastMessages, boolean flSupplyMessagesWithSenderInfo) throws Exception {
			synchronized (InitializationFlag) {
				if (flInitializing)
					InitializationFlag.wait();
				//.
	    		synchronized (Receivers) {
	        		if (Receivers.contains(Receiver))
	        			return; //. ->
	        		Receivers.add(Receiver);
				}
	    		if (flReceiveLastMessages) {
					ArrayList<TIncomingMessage> _Messages;
					synchronized (Messages) {
						_Messages = new ArrayList<TIncomingMessage>(Messages); 
					}
					for (int I = 0; I < _Messages.size(); I++) {
						TIncomingMessage TypedMessage = _Messages.get(I);
						if (!TypedMessage.IsProcessed()) {
	        				//. supply message with sender info
	        				TUserDescriptor Sender = Senders.get(TypedMessage.SenderID);
	        				if ((Sender == null) && flSupplyMessagesWithSenderInfo) {
	        					Sender = User.GetUserInfo(TypedMessage.SenderID);
	        					Senders.put(TypedMessage.SenderID, Sender);
	        				}
	        				TypedMessage.Sender = Sender;
	        				//.
	        				if (TypedMessage.Sender != null) {
	            				TReceiverMessage ReceiverMessage = new TReceiverMessage(Receiver,TypedMessage);
	        					MessageHandler.obtainMessage(MESSAGE_RECEIVEDFORRECEIVER,ReceiverMessage).sendToTarget();
	        				}
						}
					}
	    		}
			}
    	}
    	
    	public void AddReceiver(TReceiver Receiver) throws Exception {
    		AddReceiver(Receiver,false,false);
    	}
    	
    	public void RemoveReceiver(TReceiver Receiver) {
        	Receivers.remove(Receiver);
    	}

    	private static class TSystemCommandProcessor {
    	
    		public static class TGetUserStatusCommandHandler extends TCommandHandler {
    			
    			private class TProcessing extends Thread implements Runnable {
    				
    				public TProcessing() {
    					start();
    				}
    				
    				@Override
    				public void run() {
						try {
	    					try {
	    	    				TGetUserStatusCommandResponseMessage Response = new TGetUserStatusCommandResponseMessage(CommandMessage.Session, 1/*Status: online*/);
	    	    				User.IncomingMessages_SendNew(CommandMessage.SenderID, Response.Message);
	    					}
	    					finally {
	    						TGetUserStatusCommandHandler.super.Process();
	    					}
						} catch (Exception E) {
						}
    				}
    			}
    			
    			public TGetUserStatusCommandHandler(TIncomingCommandMessage pCommandMessage, TGeoScopeServerUser pUser) {
					super(pCommandMessage, pUser);
				}

				@Override
    			public void Process() throws Exception {
					new TProcessing();
    			}
    		}
    		
    		public static class TGetUserLocationCommandHandler extends TCommandHandler {
    			
    			private class TProcessing extends Thread implements Runnable {
    				
    				public TProcessing() {
    					start();
    				}
    				
    				@Override
    				public void run() {
						try {
	    					try {
	        					TTracker Tracker = TTracker.GetTracker();
								TUserLocation UserLocation = new TUserLocation();
	        					if (Tracker != null) {
	        						TGPSModule GPSModule = Tracker.GeoLog.GPSModule;
	        						if ((GPSModule != null) && GPSModule.IsEnabled()) {
	    								if (GPSModule.GetMode() != TGPSModule.GPSMODULEMODE_DISABLED) {
	    									TGPSFixValue Fix = null;
	    									try {
	    										switch (CommandMessage.Version) {
	    										
	    										case TGetUserLocationCommandMessage.Version_GetFix:
	        										Fix = GPSModule.GetCurrentFix();
	    											break;

	    										case TGetUserLocationCommandMessage.Version_ObtainFix:
	        										Fix = GPSModule.ObtainCurrentFix(null,null,true);
	    											break;
	    										}
	    										UserLocation.Status = GPSModule.GetStatus();
	    									}
	    									catch (TGPSModule.FixTimeoutException FTE) {
	        									UserLocation.Status = TGPSModule.GPSMODULESTATUS_TEMPORARILYUNAVAILABLE;
	    									}
	    									catch (TGPSModule.TMyLocationListener.LocationProviderIsDisabledException LPIDE) {
	        									UserLocation.Status = TGPSModule.GPSMODULESTATUS_PERMANENTLYUNAVAILABLE;
	    									}
	    									UserLocation.Datum = TTracker.DatumID;
	    									if (Fix != null)
	    										UserLocation.AssignFromGPSFix(Fix);
	    								}
	    								else
	    									UserLocation.Status = TGPSModule.GPSMODULESTATUS_PERMANENTLYUNAVAILABLE;
	        						}
    								else
    									UserLocation.Status = TGPSModule.GPSMODULESTATUS_PERMANENTLYUNAVAILABLE;
	        					}
								else
									UserLocation.Status = TGPSModule.GPSMODULESTATUS_PERMANENTLYUNAVAILABLE;
								//.
								TGetUserLocationCommandResponseMessage ResponseMessage = new TGetUserLocationCommandResponseMessage(CommandMessage.Session,UserLocation);
								User.IncomingMessages_SendNew(CommandMessage.SenderID,ResponseMessage.Message);
	    					}
	    					finally {
	    						TGetUserLocationCommandHandler.super.Process();
	    					}
						} catch (Exception E) {
						}
    				}
    			}
    			
    			public TGetUserLocationCommandHandler(TIncomingCommandMessage pCommandMessage, TGeoScopeServerUser pUser) {
					super(pCommandMessage, pUser);
				}

				@Override
    			public void Process() throws Exception {
					new TProcessing();
    			}
    		}

        	public static boolean Process(TIncomingCommandMessage TypedMessage, TGeoScopeServerUser User) throws Exception {
    			if (TypedMessage instanceof TGetUserStatusCommandMessage) {
    				(new TSystemCommandProcessor.TGetUserStatusCommandHandler(TypedMessage, User)).Process();
    				return true; //. ->
    			}
    			else
    				if (TypedMessage instanceof TGetUserLocationCommandMessage) {
    					(new TSystemCommandProcessor.TGetUserLocationCommandHandler(TypedMessage, User)).Process();
    					return true; //. ->
    				}
    				else
    					return false; //. ->
        	}
        	
    	}
    	
    	private boolean ProcessMessageAsSystemCommand(TIncomingCommandMessage TypedMessage) throws Exception {
    		return TSystemCommandProcessor.Process(TypedMessage, User);
    	}
    	
    	public boolean ProcessMessage(TIncomingMessage Message, TReceiver Receiver) {
			if (Message instanceof TIncomingCommandMessage)  
				return Receiver.DoOnCommand(User, (TIncomingCommandMessage)Message); //. ->
			else
				if (Message instanceof TIncomingCommandResponseMessage) 
					return Receiver.DoOnCommandResponse(User, (TIncomingCommandResponseMessage)Message); //. ->
				else //. user message
					return Receiver.DoOnMessage(User, Message); //. ->
    	}
    	
    	public boolean DispatchMessage(TIncomingMessage Message) {
    		synchronized (Receivers) {
				if (Message instanceof TIncomingCommandMessage) { 
					for (int I = 0; I < Receivers.size(); I++)
	    				if (Receivers.get(I).DoOnCommand(User, (TIncomingCommandMessage)Message))
	    					return true; //. ->
				}
				else
					if (Message instanceof TIncomingCommandResponseMessage) {
    					for (int I = 0; I < Receivers.size(); I++)
    						if (Receivers.get(I).DoOnCommandResponse(User, (TIncomingCommandResponseMessage)Message))
    	    					return true; //. ->
					}
					else { //. user message
    					for (int I = 0; I < Receivers.size(); I++)
    						if (Receivers.get(I).DoOnMessage(User, Message))
    	    					return true; //. ->
					}
    		}
    		return false;
    	}
	}
	
	public TGeoScopeServer Server;
	//.
	public int 	  UserID = 0;		
	public String UserPassword = "";
	public String UserPasswordHash = "";
	//.
	public TUserSecurityFiles SecurityFiles;
	//.
	public TIncomingMessages IncomingMessages;
	//.
	public boolean 						flUserSession = false;
	public TGeoScopeServerUserSession 	Session = null;
	
	public TGeoScopeServerUser(TGeoScopeServer pServer, int pUserID, String pUserPassword) {
		Server = pServer;
		//.
		UserID = pUserID;
		UserPassword = pUserPassword;
		//.
		UserPasswordHash = "";
		//.
		SecurityFiles = null;
		//.
		IncomingMessages = null;
		//.
		Session = null;
	}
	
	public void Destroy() throws Exception {
		Finalize();
	}

    public byte[] EncryptBufferV2(byte[] Buffer) 
    {
    	byte[] BA = new byte[Buffer.length];
    	byte[] UserPasswordArray;
    	//.
    	try {
    		UserPasswordArray = UserPassword.getBytes("windows-1251");
    	}
    	catch (Exception E)
    	{
    		UserPasswordArray = null;
    	}
    	//.
    	if ((UserPasswordArray != null) && (UserPasswordArray.length > 0))
    	{
    		int UserPasswordArrayIdx = 0;
    		for (int I = 0; I < Buffer.length; I++)
    		{
    			BA[I] = (byte)(Buffer[I]+UserPasswordArray[UserPasswordArrayIdx]);
    			UserPasswordArrayIdx++;
    			if (UserPasswordArrayIdx >= UserPasswordArray.length) UserPasswordArrayIdx = 0;
    		}
    	}
    	return BA;
    }
    
	private String PrepareSecurityFilesURL() {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(UserID)+"/"+"UserSecurityFiles.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	public TUserSecurityFiles GetUserSecurityFiles() throws Exception {
		if (SecurityFiles != null)
			return SecurityFiles; //. =>
		//.
		TUserSecurityFiles _SecurityFiles;
		String CommandURL = PrepareSecurityFilesURL();
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[2*8/*SizeOf(Int64)*/];
				int Size = in.read(Data);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SErrorOfGettingUserSecurityFiles)); //. =>
				_SecurityFiles = new TUserSecurityFiles();
				int Idx = 0;
				_SecurityFiles.idSecurityFileForPrivate = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); Idx+=8; //. Int64
				_SecurityFiles.idSecurityFileForClone = TDataConverter.ConvertBEByteArrayToInt32(Data,Idx); 
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		//.
		SecurityFiles = _SecurityFiles;
		return SecurityFiles;
	}
	
	public void Initialize(boolean pflUserSession) throws Exception {
		flUserSession = pflUserSession;
		//. initialize the space 
		TSpace.InitializeSpace(Server.context);
		//.
		if (IncomingMessages == null) 
			IncomingMessages = new TIncomingMessages(this);
		//.
		if (flUserSession && (UserID != AnonymouseUserID))
			StartSession();
	}
	
	public void Finalize() throws Exception {
		StopSession();
		//.
		if (IncomingMessages != null) {
			IncomingMessages.Destroy();
			IncomingMessages = null;
		}
		//. finalize the space
		TSpace.FinalizeSpace();
	}
	
	public void Reinitialize(boolean pflUserSession) throws Exception {
		Finalize();
		Initialize(pflUserSession);
	}
	
	public synchronized void StartSession() {
		Session = new TGeoScopeServerUserSession(this);
	}
	
	public synchronized void StopSession() {
		if (Session != null) {
			Session.Destroy();
			Session = null;
		}
	}
	
	public synchronized boolean InSession() {
		return ((Session != null) && Session.flSessioning);
	}
	
	public synchronized TGeoScopeServerUserSession GetSession() {
		return Session;
	}
	
	private String IncomingMessages_PrepareSendNewURL(int RecepientID) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(RecepientID)+"/"+"IM.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Integer.toString(UserID);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	public static int 		IncomingMessages_NewCommandSessionRange = Integer.MAX_VALUE-1;
	private static Random	IncomingMessages_NewCommandSessionRandom = new Random();
	
	private static int		IncomingMessages_GetNewCommandSession() {
		synchronized (IncomingMessages_NewCommandSessionRandom) {
			return (1+IncomingMessages_NewCommandSessionRandom.nextInt(IncomingMessages_NewCommandSessionRange));
		}
	}
	
	private void IncomingMessages_SendNew(int RecepientID, String Message) throws Exception {
		byte[] MessageBA = Message.getBytes("windows-1251");
		String CommandURL = IncomingMessages_PrepareSendNewURL(RecepientID);
		//.
		try {
			HttpURLConnection HttpConnection = Server.OpenPostDataConnection(CommandURL,MessageBA,false);           
			try {
				try {
		            //. response
		            int response = HttpConnection.getResponseCode();
		            if (response != HttpURLConnection.HTTP_OK) { 
						String ErrorMessage = HttpConnection.getResponseMessage();
						byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
						ErrorMessage = new String(ErrorMessageBA,"windows-1251");
		            	throw new IOException(Server.context.getString(R.string.SServerError)+ErrorMessage); //. =>
		            }
				} catch (ConnectException CE) {
					throw new ConnectException(Server.context.getString(R.string.SNoServerConnection)); //. =>
				}
			}
			finally {
				HttpConnection.disconnect();
			}
		} catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.toString();
			throw new Exception(Server.context.getString(R.string.SHTTPConnectionError)+S); //. =>
		}
	}
	
	public void IncomingMessages_SendNewCommand(int RecepientID, TIncomingCommandMessage CommandMessage) throws Exception {
		IncomingMessages_SendNew(RecepientID, CommandMessage.Message);
	}
	
	public void IncomingMessages_SendNewMessage(int RecepientID, String Message) throws Exception {
		if (Message.startsWith(TIncomingMessage.CommandPrefix))
			Message = Message.substring(TIncomingMessage.CommandPrefix.length());
		else
			if (Message.startsWith(TIncomingMessage.CommandResponsePrefix))
				Message = Message.substring(TIncomingMessage.CommandResponsePrefix.length());
		IncomingMessages_SendNew(RecepientID, Message);
	}
	
	private String IncomingMessages_PrepareGetMessageURL(int MessageID) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(UserID)+"/"+"IM.dat";
		//. add command parameters
		URL2 = URL2+"?"+"2"/*command version*/+","+Integer.toString(MessageID);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	public TIncomingMessage IncomingMessages_GetMessage(int MessageID) throws Exception {
		String CommandURL = IncomingMessages_PrepareGetMessageURL(MessageID);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				TIncomingMessage Result = new TIncomingMessage(MessageID);
				int Idx = 0;
				Result.SenderID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 8; //. Int64
				Result.Timestamp = TDataConverter.ConvertBEByteArrayToDouble(Data, Idx); Idx += 8;
				int MDS = (Data.length-Idx); 
				if (MDS > 0) {
					Result.Message = new String(Data, Idx,MDS, "windows-1251"); Idx += MDS;
				}
				else
					Result.Message = "";
				return Result; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	private String IncomingMessages_PrepareGetUnreadURL() {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(UserID)+"/"+"IM.dat";
		//. add command parameters
		URL2 = URL2+"?"+"3"/*command version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	public int[] IncomingMessages_GetUnread() throws Exception {
		String CommandURL = IncomingMessages_PrepareGetUnreadURL();
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int DS = Connection.getContentLength(); 
				if (DS == 0)
					return null; //. ->
				byte[] Data = new byte[DS];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				int ItemsCount = (int)(Data.length/4/*SizeOf(Int32)*/);
				int[] Result = new int[ItemsCount];
				int Idx = 0;
				for (int I = 0; I < ItemsCount; I++) {
					Result[I] = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4; 
				}
				return Result; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public TUserLocation IncomingMessages_Command_GetUserLocation(int pUserID, int Version, int Timeout, TCanceller Canceller) throws Exception {
		int TimeoutDelta = 100;
		Timeout = (int)(Timeout/TimeoutDelta);
		//.
		TGetUserLocationCommandMessage CommandMessage = new TGetUserLocationCommandMessage(Version);
		//.
		TIncomingMessages.TCommandResponseReceiver ResponseReceiver = new TIncomingMessages.TCommandResponseReceiver(CommandMessage.Session);
		//.
		IncomingMessages.AddReceiver(ResponseReceiver);
		try {
			//. send command
			IncomingMessages_SendNewCommand(pUserID, CommandMessage);
			//. wait for command response
	        int LastCheckInterval = IncomingMessages.SetFastCheckInterval(); //. speed up messages updating
	        try {
				TGetUserLocationCommandResponseMessage ResponseMessage;
				for (int I = 0; I < Timeout; I++) {
					ResponseMessage = (TGetUserLocationCommandResponseMessage)ResponseReceiver.WaitForMessage(TimeoutDelta);
					if (ResponseMessage != null) 
						return ResponseMessage.UserLocation; //. -> 
					//.
					if (Canceller != null)
						Canceller.Check();
				}
				return null; //. ->
	        }
	        finally {
	        	IncomingMessages.RestoreCheckInterval(LastCheckInterval);
	        }
		}
		finally {
			IncomingMessages.RemoveReceiver(ResponseReceiver);
		}
	}
	
	private String PrepareUserInfoURL(int pUserID, double OnLineTimeout) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Info.dat";
		//. add command parameters
		URL2 = URL2+"?"+"2"/*command version*/+","+Double.toString(OnLineTimeout);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public TUserDescriptor GetUserInfo(int pUserID) throws Exception {
		TUserDescriptor Result = null;
		//.
		String CommandURL = PrepareUserInfoURL(pUserID,DefaultUserOnlineTimeout);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				Result = new TUserDescriptor(pUserID);
				Result.FromByteArrayV2(Data, 0);
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		return Result;
	}
	
	public TUserDescriptor GetUserInfo() throws Exception {
		return GetUserInfo(UserID);
	}
	
	private String PrepareSetTaskEnabledURL(int pUserID, boolean Value) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Data.dat";
		//. add command parameters
		int IV = 0;
		if (Value)
			IV = 1;
		URL2 = URL2+"?"+"2"/*command version*/+","+"1"/*data version*/+","+Integer.toString(IV);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public void SetTaskEnabled(int pUserID, boolean Value) throws Exception {
		String CommandURL = PrepareSetTaskEnabledURL(pUserID,Value);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			in.close();
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public void SetTaskEnabled(boolean Value) throws Exception {
		SetTaskEnabled(UserID, Value);
	}
	
	private String PrepareStartUserActivityURL(int pUserID, TActivity pActivity) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Activities.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/;
		if (pActivity.ID == 0) {
			URL2 = URL2+","+"1"/*parameters version*/+","+pActivity.Name;
			if (pActivity.Info != null)
				URL2 = URL2+","+pActivity.Info;
		}
		else //. restart the activity
			URL2 = URL2+","+"3"/*parameters version*/+","+Integer.toString(pActivity.ID);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public int StartUserActivity(int pUserID, TActivity pActivity) throws Exception {
		String CommandURL = PrepareStartUserActivityURL(pUserID, pActivity);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				return TDataConverter.ConvertBEByteArrayToInt32(Data, 0); //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public int StartUserActivity(TActivity pActivity) throws Exception {
		return StartUserActivity(UserID,pActivity);
	}
	
	private String PrepareRestartUserDefaultActivityURL(int pUserID) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Activities.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+"4";
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public int RestartUserDefaultActivity(int pUserID) throws Exception {
		String CommandURL = PrepareRestartUserDefaultActivityURL(pUserID);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				return TDataConverter.ConvertBEByteArrayToInt32(Data, 0); //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public int RestartUserDefaultActivity() throws Exception {
		return RestartUserDefaultActivity(UserID);
	}
	
	private String PrepareGetUserDefaultActivityIDURL(int pUserID) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Activities.dat";
		//. add command parameters
		URL2 = URL2+"?"+"7"/*command version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public int GetUserDefaultActivityID(int pUserID) throws Exception {
		String CommandURL = PrepareGetUserDefaultActivityIDURL(pUserID);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				return TDataConverter.ConvertBEByteArrayToInt32(Data, 0); //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public int GetUserDefaultActivityID() throws Exception {
		return GetUserDefaultActivityID(UserID);
	}
	
	private String PrepareFinishUserCurrentActivityURL(int pUserID) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Activities.dat";
		//. add command parameters
		URL2 = URL2+"?"+"2"/*command version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public void FinishUserCurrentActivity(int pUserID) throws Exception {
		String CommandURL = PrepareFinishUserCurrentActivityURL(pUserID);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			in.close();
		}
		finally {
			Connection.disconnect();
		}
	}
	
	public void FinishUserCurrentActivity() throws Exception {
		FinishUserCurrentActivity(UserID);
	}
	
	private String PrepareUserCurrentActivityURL(int pUserID) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Activities.dat";
		//. add command parameters
		URL2 = URL2+"?"+"3"/*command version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public TActivity GetUserCurrentActivity(int pUserID) throws Exception {
		TActivity Result = null;
		//.
		String CommandURL = PrepareUserCurrentActivityURL(pUserID);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				int Idx = 0;
				int ID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 8; //. Int64
				if (ID != 0) {
					Result = new TActivity(ID);
					Result.FromByteArrayV2(Data, Idx);
				}
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		return Result;
	}
	
	public TActivity GetUserCurrentActivity() throws Exception {
		return GetUserCurrentActivity(UserID);
	}
	
	private String PrepareUserActivityComponentListURL(int pUserID, int idActivity) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Activities.dat";
		//. add command parameters
		URL2 = URL2+"?"+"5"/*command version*/+","+Integer.toString(idActivity)+","+"2"/*parameters version*/;
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public TActivity.TComponents GetUserActivityComponentList(int pUserID, int idActivity) throws Exception {
		TActivity.TComponents Result;
		//.
		String CommandURL = PrepareUserActivityComponentListURL(pUserID, idActivity);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				int Size = Connection.getContentLength();
				if (Size == 0)
					return null; //. ->
				byte[] Data = new byte[Size];
				Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				int Idx = 0;
				short Version = TDataConverter.ConvertBEByteArrayToInt16(Data, Idx); Idx += 2;
				Result = new TActivity.TComponents();
				switch (Version) {
				case 1:
					Idx = Result.FromByteArrayV1(Data, Idx);
					break; //. >
					
				case 2:
					Idx = Result.FromByteArrayV2(Data, Idx);
					break; //. >
				}
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		return Result;
	}
	
	public TActivity.TComponents GetUserActivityComponentList(int idActivity) throws Exception {
		return GetUserActivityComponentList(UserID, idActivity); 
	}
	
	private String PrepareUserActivityListURL(int pUserID, double FromDate, double ToDate) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"Co"+"/"+Integer.toString(pUserID)+"/"+"Activities.dat";
		//. add command parameters
		URL2 = URL2+"?"+"4"/*command version*/+","+"1"/*parameters version*/+","+Double.toString(FromDate)+","+Double.toString(ToDate);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public TActivities GetUserActivityList(int pUserID, double FromDate, double ToDate) throws Exception {
		TActivities Result;
		//.
		String CommandURL = PrepareUserActivityListURL(pUserID, FromDate,ToDate);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				int Idx = 0;
				short Version = TDataConverter.ConvertBEByteArrayToInt16(Data, Idx); Idx += 2;
				Result = new TActivities();
				switch (Version) {
				
				case 1:
					Idx = Result.FromByteArray(pUserID, Data, Idx);
					break; //. >
				}
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		return Result;
	}
	
	public TActivities GetUserActivityList(double FromDate, double ToDate) throws Exception {
		return GetUserActivityList(UserID, FromDate,ToDate);
	}
	
	private String PrepareUserListURL(String NameContext, double OnLineTimeout) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"InstanceList.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+NameContext+","+Double.toString(OnLineTimeout);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public TUserDescriptor[] GetUserList(String NameContext) throws Exception {
		TUserDescriptor[] Result = null;
		//.
		String CommandURL = PrepareUserListURL(NameContext,DefaultUserOnlineTimeout);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				//.
				ByteArrayInputStream BIS = new ByteArrayInputStream(Data);
				try {
					ZInputStream ZIS = new ZInputStream(BIS);
					try {
						byte[] Buffer = new byte[8192];
						int ReadSize;
						ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
						try {
							while ((ReadSize = ZIS.read(Buffer)) > 0) 
								BOS.write(Buffer, 0,ReadSize);
							//.
							Data = BOS.toByteArray();
							int Idx = 0;
							int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4;
							Result = new TUserDescriptor[ItemsCount];
							for (int I = 0; I < ItemsCount; I++) {
								TUserDescriptor UD = new TUserDescriptor();
								Idx = UD.FromByteArray(Data, Idx);
								Result[I] = UD;
							}
						}
						finally {
							BOS.close();
						}
					}
					finally {
						ZIS.close();
					}
				}
				finally {
					BIS.close();
				}
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
		return Result;
	}
	
	private String PrepareUpdateUserInfosURL(double OnLineTimeout) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTModelUser)+"/"+"InstanceInfos.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command version*/+","+Double.toString(OnLineTimeout);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}	
	
	public void UpdateUserInfos(TUserDescriptor[] Users) throws Exception {
		String CommandURL = PrepareUpdateUserInfosURL(DefaultUserOnlineTimeout);
		//.
		byte[] ILData = new byte[Users.length*8/*SizeOf(Int64)*/];
		int Idx = 0;
		for (int I = 0; I < Users.length; I++) {
			byte[] BA = TDataConverter.ConvertInt32ToBEByteArray(Users[I].UserID);
			System.arraycopy(BA,0, ILData, Idx, BA.length); Idx += 8; //. Int64
		}
		//.
		try {
			HttpURLConnection HttpConnection = Server.OpenPostDataConnection(CommandURL,ILData,true);           
			try {
				try {
		            //. response code
		            int response = HttpConnection.getResponseCode();
		            if (response != HttpURLConnection.HTTP_OK) { 
						String ErrorMessage = HttpConnection.getResponseMessage();
						byte[] ErrorMessageBA = ErrorMessage.getBytes("ISO-8859-1");
						ErrorMessage = new String(ErrorMessageBA,"windows-1251");
		            	throw new IOException(Server.context.getString(R.string.SServerError)+ErrorMessage); // =>
		            }
		            //.
					InputStream in = HttpConnection.getInputStream();
					try {
						byte[] Data = new byte[HttpConnection.getContentLength()];
						int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
						if (Size != Data.length)
							throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
						//.
						ByteArrayInputStream BIS = new ByteArrayInputStream(Data);
						try {
							ZInputStream ZIS = new ZInputStream(BIS);
							try {
								byte[] Buffer = new byte[8192];
								int ReadSize;
								ByteArrayOutputStream BOS = new ByteArrayOutputStream(Buffer.length);
								try {
									while ((ReadSize = ZIS.read(Buffer)) > 0) 
										BOS.write(Buffer, 0,ReadSize);
									//.
									Data = BOS.toByteArray();
									Idx = 0;
									int ItemsCount = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4;
									if (ItemsCount != Users.length)
										throw new Exception("wrong response items count"); //. =>
									for (int I = 0; I < ItemsCount; I++) 
										Idx = Users[I].FromByteArray(Data, Idx);
								}
								finally {
									BOS.close();
								}
							}
							finally {
								ZIS.close();
							}
						}
						finally {
							BIS.close();
						}
					}
					finally {
						in.close();
					}                
				} catch (ConnectException CE) {
					throw new ConnectException(Server.context.getString(R.string.SNoServerConnection)); //. =>
				}
			}
			finally {
				HttpConnection.disconnect();
			}
		} catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.toString();
			throw new Exception(Server.context.getString(R.string.SHTTPConnectionError)+S); //. =>
		}
	}
	
	public static class TTrackerObjectCreationInfo {
		public int 		ComponentID;
		public String 	GeographServerAddress;
		public int 		GeographServerPort;
		public int 		GeographServerObjectID;
	}
	
	private String PrepareConstructNewTrackerObjectURL(String pObjectBusinessModel, String pName, int pGeoSpaceID, int pSecurityIndex) {
		String URL1 = Server.Address;
		//. add command path
		URL1 = "http://"+URL1+"/"+"Space"+"/"+"2"/*URLProtocolVersion*/+"/"+Integer.toString(UserID);
		String URL2 = "TypesSystem"+"/"+Integer.toString(SpaceDefines.idTCoComponent)+"/"+"NewCoGeoMonitorObject.dat";
		//. add command parameters
		URL2 = URL2+"?"+"1"/*command*/+","+"1"/*command version*/+","+pObjectBusinessModel+","+pName+","+Integer.toString(pGeoSpaceID)+","+Integer.toString(pSecurityIndex);
		//.
		byte[] URL2_Buffer;
		try {
			URL2_Buffer = URL2.getBytes("windows-1251");
		} 
		catch (Exception E) {
			URL2_Buffer = null;
		}
		byte[] URL2_EncryptedBuffer = EncryptBufferV2(URL2_Buffer);
		//. encode string
        StringBuffer sb = new StringBuffer();
        for (int I=0; I < URL2_EncryptedBuffer.length; I++) {
            String h = Integer.toHexString(0xFF & URL2_EncryptedBuffer[I]);
            while (h.length() < 2) 
            	h = "0" + h;
            sb.append(h);
        }
		URL2 = sb.toString();
		//.
		String URL = URL1+"/"+URL2+".dat";
		return URL;		
	}
	
	private static final int TrackerObjectCreationTimeout = 1000*60; //. seconds
	
	public TTrackerObjectCreationInfo ConstructNewTrackerObject(String pObjectBusinessModel, String pName, int pGeoSpaceID, int pSecurityIndex) throws Exception {
		String CommandURL = PrepareConstructNewTrackerObjectURL(pObjectBusinessModel,pName,pGeoSpaceID,pSecurityIndex);
		//.
		HttpURLConnection Connection = Server.OpenConnection(CommandURL,TrackerObjectCreationTimeout);
		try {
			InputStream in = Connection.getInputStream();
			try {
				byte[] Data = new byte[Connection.getContentLength()];
				int Size = TNetworkConnection.InputStream_ReadData(in, Data,Data.length);
				if (Size != Data.length)
					throw new IOException(Server.context.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
				TTrackerObjectCreationInfo Result = new TTrackerObjectCreationInfo();
				int Idx = 0;
				Result.ComponentID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 8; //. Int64
				String GSA;
		    	byte SS = Data[Idx]; Idx++;
		    	if (SS > 0) {
		    		GSA = new String(Data, Idx,SS, "windows-1251");
		    		Idx += SS;
		    	}
		    	else
		    		GSA = "";
		    	String[] SA = GSA.split(":");
		    	Result.GeographServerAddress = SA[0];
		    	Result.GeographServerPort = Integer.parseInt(SA[1]);
				Result.GeographServerObjectID = TDataConverter.ConvertBEByteArrayToInt32(Data, Idx); Idx += 4; 
				return Result; //. ->
			}
			finally {
				in.close();
			}                
		}
		finally {
			Connection.disconnect();
		}
	}		
}
