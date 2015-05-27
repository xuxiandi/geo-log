package com.geoscope.GeoEye;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.TSplashPanel;
import com.geoscope.GeoLog.Application.TUserAccess;
import com.geoscope.GeoLog.Application.Installator.TGeoLogInstallator;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TReflector extends Activity {

	public static final String ProgramVersion = "v3.010215";
	// .
	public static final String GarryG = "Когда мила родная сторона, которой возлелеян и воспитан, то к куче ежедневного дерьма относишься почти-что с аппетитом.";

	public static int NextID = 0;
	//.
	public static synchronized int GetNextID() {
		NextID++;
		return NextID;
	}
	
	private static ArrayList<TReflector> ReflectorList = new ArrayList<TReflector>();
	//.
	public static boolean flScreenIsOn = true;
	
	public static synchronized void Reset() throws Exception {
		for (int I = 0; I < ReflectorList.size(); I++) 
			ReflectorList.get(I).Destroy();
	}

	public static synchronized void RestartReflectorPending(Context context, int Delay) throws Exception {
		if (ReflectorList.size() > 0) {
			Reset();
			// .
			Intent Launcher = new Intent(context, TReflector.class);
			Launcher.putExtra("Reason", TReflectorComponent.REASON_MAIN);
			//.
			PendingIntent ReflectorPendingIntent = PendingIntent.getActivity(context, 0, Launcher, PendingIntent.FLAG_UPDATE_CURRENT);
			// .
			AlarmManager AM = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			AM.set(AlarmManager.RTC, System.currentTimeMillis() + Delay,
					ReflectorPendingIntent);
		}
	}

	public static synchronized TReflector GetLastReflector() {
		if (ReflectorList.size() == 0)
			return null;
		return ReflectorList.get(ReflectorList.size() - 1);
	}

	public static synchronized TReflector GetReflector() {
		return GetLastReflector();
	}

	public static synchronized TReflector GetReflector(int ReflectorID) {
		int Cnt = ReflectorList.size();
		for (int I = 0; I < Cnt; I++) {
			TReflector Reflector = ReflectorList.get(I);
			if (Reflector.ID == ReflectorID)
				return Reflector; //. ->
		}
		return null;	
	}

	public static synchronized boolean ReflectorExists() {
		return (ReflectorList.size() > 0);
	}

	public static synchronized int ReflectorCount() {
		return ReflectorList.size();
	}

	private static synchronized void _AddReflector(TReflector pReflector) {
		ReflectorList.add(pReflector);
	}

	private static synchronized void _RemoveReflector(TReflector pReflector) {
		ReflectorList.remove(pReflector);
	}

	private static boolean flUserAccessGranted = false;
	
	
	public int ID = 0;
	//.
	public boolean flFullScreen = true;
	//.
	//. main component
	public TReflectorComponent Component = null;
	//.
	private BroadcastReceiver EventReceiver = null;

	private int CreateCount = 0;

	public boolean Create() throws Exception {
		Intent Parameters = getIntent();
		//.
		Bundle extras = Parameters.getExtras();
		if (extras != null) 
			ID = extras.getInt("ID");
		//.
		Component = new TReflectorComponent(this, (RelativeLayout)findViewById(R.id.ReflectorLayout), Parameters);
		//.
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		if (CreateCount == 0) 
			try {
				if (android.os.Build.VERSION.SDK_INT >= 9) {
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
					StrictMode.setThreadPolicy(policy);
				}
				Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
				//. process pre-initialization
				TFileSystem.TExternalStorage.WaitForMounted();
				//. check installation
				if (!TGeoLogInstallator.InstallationIsUpToDate(getApplicationContext())) {
					Intent intent = new Intent(getApplicationContext(), TSplashPanel.class);
					intent.putExtra("Mode", TSplashPanel.MODE__START_REFLECTOR_ON_FINISH);
		    		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		    		getApplicationContext().startActivity(intent);
					//.
					finish();
					return; //. ->
				}
				//. initialize an application instance and start services
				TGeoLogApplication.InitializeInstance(getApplicationContext()).StartServices(getApplicationContext());
			} catch (Exception E) {
				Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
				// .
				finish();
				return; // . ->
			}
		//.
		if (android.os.Build.VERSION.SDK_INT >= 11) 
			getWindow().setFlags(LayoutParams.FLAG_HARDWARE_ACCELERATED, LayoutParams.FLAG_HARDWARE_ACCELERATED);
		if (android.os.Build.VERSION.SDK_INT < 14) {
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			flFullScreen = true;
		} else 
			if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
				requestWindowFeature(Window.FEATURE_NO_TITLE);
				flFullScreen = true;
			}
			else {
				requestWindowFeature(Window.FEATURE_ACTION_BAR);
				flFullScreen = false;
			}
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
		//.
		setContentView(R.layout.reflector);
		//.
		try {
			if (!Create()) {
				finish();
				return; // . ->
			}
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
			// .
			finish();
			return; // . ->
		}
		//.
		EventReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
					flScreenIsOn = false;
					// .
					return; // . ->
				}
				if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
					flScreenIsOn = true;
					// .
					return; // . ->
				}
			}
		};
		IntentFilter ScreenOnFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		IntentFilter ScreenOffFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		getApplicationContext().registerReceiver(EventReceiver, ScreenOnFilter);
		getApplicationContext().registerReceiver(EventReceiver, ScreenOffFilter);
		//.
		_AddReflector(this);
		//.
		if (TUserAccess.UserAccessFileExists()) {
			if (!flUserAccessGranted) {
				final TUserAccess AR = new TUserAccess();
				if (AR.UserAccessPassword != null) {
					AlertDialog.Builder alert = new AlertDialog.Builder(this);
					// .
					alert.setTitle("");
					alert.setMessage(R.string.SEnterPassword);
					alert.setCancelable(false);
					// .
					final EditText input = new EditText(this);
					alert.setView(input);
					// .
					alert.setPositiveButton(R.string.SOk,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// . hide keyboard
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									// .
									String Password = input.getText()
											.toString();
									if (Password.equals(AR.UserAccessPassword))
										flUserAccessGranted = true;
									else
										TReflector.this.finish();
								}
							});
					// .
					alert.setNegativeButton(R.string.SCancel,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int whichButton) {
									// . hide keyboard
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(
											input.getWindowToken(), 0);
									// .
									TReflector.this.finish();
								}
							});
					// .
					alert.show();
				}
			}
		} else
			flUserAccessGranted = true;
		//.
		CreateCount++;
		//.
		String ProfileName = null;
		switch (Component.Reason) {

		case TReflectorComponent.REASON_USERPROFILECHANGED:
			Bundle extras = getIntent().getExtras();
			if (extras != null) 
				ProfileName = extras.getString("ProfileName");
			break; // . >
		}
		if ((ProfileName != null) && (!ProfileName.equals(""))) {
			String S = getString(R.string.SProfile) + ProfileName;
			Toast.makeText(this, S, Toast.LENGTH_LONG).show();
		}
	}

	public void Destroy() throws Exception {
		if (Component != null) {
			Component.Destroy();
			Component = null;
		}
	}

	@Override
	public void onDestroy() {
		_RemoveReflector(this);
		//.
		if (EventReceiver != null) {
			getApplicationContext().unregisterReceiver(EventReceiver);
			EventReceiver = null;
		}
		//.
		try {
			Destroy();
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(), Toast.LENGTH_LONG).show();
		}
		//.
		super.onDestroy();
	}

	@Override
	protected void onStart() {
		super.onStart();
		//.
		if (Component != null)
			Component.DoOnStart();
	}

	@Override
	protected void onStop() {
		if (Component != null)
			Component.DoOnStop();
		//.
		super.onStop();
	}

	@Override
	public void onResume() {
		super.onResume();
		//.
		if (Component != null)
			Component.Resume();
		//. start tracker position fixing immediately if it is in impulse mode
		TTracker Tracker = TTracker.GetTracker();
		if ((Tracker != null) && (Tracker.GeoLog.GPSModule != null) && Tracker.GeoLog.GPSModule.IsEnabled() && Tracker.GeoLog.GPSModule.flImpulseMode)
			Tracker.GeoLog.GPSModule.ProcessImmediately();
	}

	@Override
	public void onPause() {
		if (Component != null)
			Component.Pause();
		//.
		super.onPause();
	}
	
	@Override
	public void onBackPressed() {
		if (Component != null)
			Component.DoOnBackPressed();
		else
			finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.reflector_menu, menu);
		//.
		menu.getItem(0/* Configuration */).setVisible(ReflectorCount() == 1);
		//.
		if (Component != null) {
			menu.getItem(1/* Selected object */).setVisible(Component.SelectedObj_Exists());
			menu.getItem(4/* Exit */).setVisible(Component.Configuration.Application_flQuitAbility && (ReflectorCount() == 1));
		}
		//.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.ReflectorConfiguration:
			if (Component != null)
				Component.ShowConfiguration();
			//.
			return true; // . >

		case R.id.ReflectorFindComponent:
			if (Component != null)
				Component.FindComponent();
			// .
			return true; // . >

		case R.id.ReflectorSelectedComponent:
			if (Component != null)
				Component.ShowSelectedComponentMenu();
			//.
			return true; // . >

		case R.id.Reflector_Help:
			Intent intent = new Intent(this, TReflectorHelpPanel.class);
			startActivity(intent);
			//.
			return true; // . >

		case R.id.ExitProgram:
			new AlertDialog.Builder(this)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.SConfirmation)
					.setMessage(R.string.SDoYouWantToQuitTheApplication)
					.setPositiveButton(R.string.SYes,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int id) {
									onDestroy();
									// .
									TGeoLogApplication.Instance().Terminate(
											getApplicationContext());
								}
							}).setNegativeButton(R.string.SNo, null).show();
			// .
			return true; // . >
		}

		return false;
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case TReflectorComponent.REQUEST_SHOW_TRACKER:
			break; // . >

		case TReflectorComponent.REQUEST_EDIT_REFLECTOR_CONFIGURATION:
			break; // . >

		case TReflectorComponent.REQUEST_OPEN_SELECTEDOBJ_OWNER_TYPEDDATAFILE:
			break; // . >

		case TReflectorComponent.REQUEST_OPEN_USERSEARCH:
			if (resultCode == RESULT_OK) {
				Bundle extras = data.getExtras();
				if (extras != null) {
					TGeoScopeServerUser.TUserDescriptor User = new TGeoScopeServerUser.TUserDescriptor();
					User.UserID = extras.getLong("UserID");
					User.UserIsDisabled = extras.getBoolean("UserIsDisabled");
					User.UserIsOnline = extras.getBoolean("UserIsOnline");
					User.UserName = extras.getString("UserName");
					User.UserFullName = extras.getString("UserFullName");
					User.UserContactInfo = extras.getString("UserContactInfo");
					// .
					Intent intent = new Intent(TReflector.this, TUserPanel.class);
					intent.putExtra("ComponentID", Component.ID);
					intent.putExtra("UserID", User.UserID);
					startActivity(intent);
				}
			}
			break; // . >
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

}