package com.geoscope.GeoEye;

import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Image.TDiskImageCache;
import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity.TComponent;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

@SuppressLint("HandlerLeak")
public class TUserListComponent extends TUIComponent {

	public static final int MODE_UNKNOWN 				= 0;
	public static final int MODE_FORLOCATION 			= 1;
	public static final int MODE_FORGEOMONITOROBJECT	= 2;
	public static final int MODE_FORCHAT 				= 3;
	//.
	private static final int MESSAGE_UPDATELIST = 1;
	public static final int UpdateInterval = 1000*30; //. seconds
	//.
	public static String 	RecentUsersFileName() {
		return TReflector.ProfileFolder()+"/"+"UserListRecents.dat";
	}
	//.
	public static final int RecentUsersMaxCount = 10;
	 
	public static final int LIST_ROW_SIZE_SMALL_ID 	= 1;
	public static final int LIST_ROW_SIZE_NORMAL_ID = 2;
	public static final int LIST_ROW_SIZE_BIG_ID 	= 3;
	
	public static class TOnListItemClickHandler {
		
		public void DoOnListItemClick(TComponent Component) {
		}
	}
	
	public static class TListItem {
		
		public TGeoScopeServerUser.TUserDescriptor User;
		//.
		public boolean BMP_flLoaded = false;
		public boolean BMP_flNull = false;
	}
	
	public static class TComponentListAdapter extends BaseAdapter {

		private static final String 		ImageCache_Name = "UserListImages";
		private static final int			ImageCache_Size = 1024*1024*10; //. Mb
		private static final CompressFormat ImageCache_CompressFormat = CompressFormat.PNG;
		private static final int			ImageCache_CompressQuality = 100;
		
		public static class TViewHolder {
			
			public TListItem Item;
			//.
			public ImageView 	ivImage;
			public TextView 	lbName;
			public TextView 	lbInfo;
		}
		
		private static abstract class TProgressHandler {
			
			public abstract void DoOnStart();
			public abstract void DoOnFinish();
			public abstract void DoOnProgress(int Percentage);
		}
		
		
		private Context context;
		//.
		private TUserListComponent Panel;
		//.
		@SuppressWarnings("unused")
		private ListView MyListView;
		//.
		private View 				ProgressBar;
		@SuppressWarnings("unused")
		private TProgressHandler 	ProgressHandler;
		//.
		private TListItem[]	Items;
		private int			Items_SelectedIndex = -1;
		//.
		private LayoutInflater layoutInflater;
		//.
		@SuppressWarnings("unused")
		private int ImageLoaderCount = 0;
		//.
		@SuppressWarnings("unused")
		private TDiskImageCache ImageCache;
		//.
		public OnClickListener ImageClickListener = new OnClickListener() {
			
			@Override
	        public void onClick(View v) {
	        }
		};
		//.
		public boolean flListIsScrolling = false;
	        
		public TComponentListAdapter(TUserListComponent pPanel, ListView pMyListView, View pProgressBar, TListItem[] pItems) {
			context = pPanel.ParentActivity;
			//.
			Panel = pPanel;
			MyListView = pMyListView;
			ProgressBar = pProgressBar;
			//.
			Items = pItems;
			layoutInflater = LayoutInflater.from(context);
			//.
			ImageCache = new TDiskImageCache(context, ImageCache_Name,ImageCache_Size,ImageCache_CompressFormat,ImageCache_CompressQuality);
			//.
			ProgressHandler = new TProgressHandler() {
				
				@Override
				public void DoOnStart() {
					ProgressBar.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void DoOnFinish() {
					ProgressBar.setVisibility(View.GONE);
				}
				
				@Override
				public void DoOnProgress(int Percentage) {
				}
			};
		}

		@Override
		public int getCount() {
			return Items.length;
		}

		@Override
		public Object getItem(int position) {
			return Items[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void Items_SetSelectedIndex(int Index, boolean flNotify) {
			Items_SelectedIndex = Index;
			//.
			if (flNotify)
				notifyDataSetChanged();
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TViewHolder holder;
			if (convertView == null) {
				int LayoutID = R.layout.user_list_row_layout;
				switch (Panel.ListRowSizeID) {
				
				case LIST_ROW_SIZE_SMALL_ID:
					LayoutID = R.layout.user_list_row_small_layout;
					break; //. >
					
				case LIST_ROW_SIZE_NORMAL_ID:
					LayoutID = R.layout.user_list_row_layout;
					break; //. >
					
				case LIST_ROW_SIZE_BIG_ID:
					LayoutID = R.layout.user_list_row_big_layout;
					break; //. >
				}
				convertView = layoutInflater.inflate(LayoutID, null);
				holder = new TViewHolder();
				holder.ivImage = (ImageView) convertView.findViewById(R.id.ivImage);
				holder.lbName = (TextView) convertView.findViewById(R.id.lbName);
				holder.lbInfo = (TextView) convertView.findViewById(R.id.lbInfo);
				//.
				convertView.setTag(holder);
			} 
			else 
				holder = (TViewHolder)convertView.getTag();
			//. updating view
			TListItem Item = Items[position];
			//.
			holder.Item = Item;
			//.
			if (Item.User.UserFullName.length() > 0)
				holder.lbName.setText(Item.User.UserFullName);
			else
				holder.lbName.setText(Item.User.UserName);
			//.
			String S = Item.User.UserName;
			if (Item.User.UserIsOnline)
				S += "  "+"["+context.getString(R.string.SOnline)+"]";
			holder.lbInfo.setText(S);
			//.
			if (Item.User.UserIsOnline)
				holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.onlineuser));
			else
				holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.offlineuser));
			//.
			holder.ivImage.setOnClickListener(null);
			//. show selection
			if (position == Items_SelectedIndex) {
	            convertView.setSelected(true);
	            convertView.setBackgroundColor(0xFFFFADB1);
	        }			
			else {
	            convertView.setSelected(false);
            	convertView.setBackgroundColor(Color.TRANSPARENT);
			}
			//.
			return convertView;
		}
	}
	
	public boolean flExists = false;
	//.
	private Activity ParentActivity;
	private LinearLayout ParentLayout;
	//.
	private int ListRowSizeID;
	//.
	private int Mode = MODE_UNKNOWN;
	//.
	private TReflectorComponent Component;
	//.
	private TextView lbUserListTitle;
	private EditText edUserListNameContext;
	private Button btnSearchByNameContext;
	private TSearchingByNameContext SearchingByNameContext = null;
	public TListItem[] 			Items = null; 
	public TUserListUpdating	ItemsUpdating = null;
	public TGeoScopeServerUser.TUserDescriptors 	RecentItems = null;
	//.
	@SuppressWarnings("unused")
	private TOnListItemClickHandler OnListItemClickHandler;
    //.
    private TComponentListAdapter	lvListAdapter = null;
	private ListView 				lvList;
	//.
	private View ProgressBar;
	
	public TUserListComponent(Activity pParentActivity, LinearLayout pParentLayout, int pListRowSizeID, int pMode, TReflectorComponent pComponent, TOnListItemClickHandler pOnListItemClickHandler) {
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
		//.
		ListRowSizeID = pListRowSizeID;
		//.
		Mode = pMode;
		//.		
		Component = pComponent;
        //.
		Component = pComponent;
		//.
		OnListItemClickHandler = pOnListItemClickHandler;
		//.
		LayoutInflater inflater = (LayoutInflater)ParentActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.user_list_layout, ParentLayout);
        //.
        lbUserListTitle = (TextView)ParentLayout.findViewById(R.id.lbUserListTitle);
        switch (Mode) {
        
        case MODE_FORLOCATION:
        	lbUserListTitle.setText(R.string.SSelectUserForLocationSending);
        	break;
        	
        case MODE_FORGEOMONITOROBJECT:
        	lbUserListTitle.setText(R.string.SSelectUserForObjectsSending);
        	break;
        	
        case MODE_FORCHAT:
        	lbUserListTitle.setText(R.string.SSelectUserForChat);
        	break;
        	
        default:
        	lbUserListTitle.setText(R.string.SSelectUser);
        	break;
        }
        //.
        edUserListNameContext = (EditText)ParentLayout.findViewById(R.id.edUserListNameContext); 
        edUserListNameContext.setOnEditorActionListener(new OnEditorActionListener() {
        	
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if ((arg1 == EditorInfo.IME_ACTION_NEXT) || (arg1 == EditorInfo.IME_ACTION_DONE)) {
                	StartSearching();
                }
				return false;
			}
        });        
        btnSearchByNameContext = (Button)ParentLayout.findViewById(R.id.btnSearchUsersByName); 
        btnSearchByNameContext.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
            	StartSearching();
            }
        });
        //.
        lvList = (ListView)ParentLayout.findViewById(R.id.lvList);
        lvList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvList.setOnItemClickListener(new OnItemClickListener() {         
        	
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (RecentItems != null) 
					RecentItems.Add(Items[arg2].User);
				//.
            	Intent intent = ParentActivity.getIntent();
            	intent.putExtra("UserID",Items[arg2].User.UserID);
            	intent.putExtra("UserIsDisabled",Items[arg2].User.UserIsDisabled);
            	intent.putExtra("UserIsOnline",Items[arg2].User.UserIsOnline);
            	intent.putExtra("UserName",Items[arg2].User.UserName);
            	intent.putExtra("UserFullName",Items[arg2].User.UserFullName);
            	intent.putExtra("UserContactInfo",Items[arg2].User.UserContactInfo);
                //.
            	ParentActivity.setResult(Activity.RESULT_OK,intent);
            	//.
            	ParentActivity.finish();
        	}              
        });         
        lvList.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (RecentItems != null) 
					RecentItems.Add(Items[arg2].User);
				//.
            	Intent intent = new Intent(ParentActivity, TUserPanel.class);
				intent.putExtra("ComponentID", Component.ID);
            	intent.putExtra("UserID",Items[arg2].User.UserID);
            	ParentActivity.startActivity(intent);
            	//.
            	return true; 
			}
		}); 
        lvList.setOnScrollListener(new OnScrollListener() {

        	@Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

        	@Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            	if (lvListAdapter != null)
            		lvListAdapter.flListIsScrolling = (scrollState != OnScrollListener.SCROLL_STATE_IDLE); 
            }
        });
        //.
        RecentItems = new TGeoScopeServerUser.TUserDescriptors();
        try {
			RecentItems.FromFile(RecentUsersFileName());
			//.
			TGeoScopeServerUser.TUserDescriptor[] _Recents = RecentItems.GetItems();
			int Cnt = ((_Recents != null) ? _Recents.length : 0);;
			TListItem[] Recents = new TListItem[Cnt];
			for (int I = 0; I < Cnt; I++) {
				TListItem Item = new TListItem();
				Item.User = _Recents[I];
				Recents[I] = Item;
			}
			//. reset flags
			for (int I = 0; I < Cnt; I++)
				Recents[I].User.UserIsOnline = false;
			//.
			UpdateList(Recents);
			//.
			if (Recents.length > 0)
				ItemsUpdating = new TUserListUpdating(MESSAGE_UPDATELIST,true);
		} catch (Exception E) {}
        //.
		ParentActivity.setResult(Activity.RESULT_CANCELED);
        //.
        ProgressBar = ParentLayout.findViewById(R.id.pbProgress);
        //.
        flExists = true;
	}

	@Override
	public void Destroy() throws Exception {
		flExists = false;
        //.
    	if (RecentItems != null) {
    		if (RecentItems.IsChanged())
    	        try {
    				RecentItems.ToFile(RecentUsersFileName(),RecentUsersMaxCount);
    			} catch (IOException E) {}
    		RecentItems = null; 
    	}
    	if (SearchingByNameContext != null) {
    		SearchingByNameContext.Cancel();
    		SearchingByNameContext = null;
    	}
    	if (ItemsUpdating != null) {
    		ItemsUpdating.Cancel();
    		ItemsUpdating = null;
    	}
	}

	@Override
	public void Show() {
		ParentLayout.setVisibility(View.VISIBLE);
	}
	
	@Override
	public void Hide() {
		ParentLayout.setVisibility(View.GONE);
	}
	
	@Override
	public boolean IsVisible() {
		return ParentLayout.isShown();
	}
	
    public void StartSearching() {
    	String NameContext = edUserListNameContext.getText().toString();
    	if (NameContext.length() < 2) {
    		Toast.makeText(ParentActivity, R.string.STooShortSearchContext, Toast.LENGTH_SHORT).show();
    		return; //. ->
    	}
    	if (SearchingByNameContext != null)
    		SearchingByNameContext.Cancel();
    	SearchingByNameContext = new TSearchingByNameContext(NameContext,MESSAGE_UPDATELIST);
    }
    
	public void UpdateList(TListItem[] pItems) throws Exception {
		synchronized (this) {
			Items = pItems;				
		}
		//.
		if (pItems.length == 0) {
			lvList.setAdapter(null);
    		return; //. ->
		}
		//.
		lvListAdapter = new TComponentListAdapter(this, lvList, ProgressBar, Items);
		lvList.setAdapter(lvListAdapter);
	}
	
    private class TSearchingByNameContext extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_SUCCESS 				= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;
    	
    	private String NameContext;
    	private int OnCompletionMessage;
    	//.
        private ProgressDialog progressDialog; 
    	
    	public TSearchingByNameContext(String pNameContext, int pOnCompletionMessage) {
    		super();
    		//.
    		NameContext = pNameContext;
    		OnCompletionMessage = pOnCompletionMessage;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
		    	TGeoScopeServerUser.TUserDescriptor[] _Items = null;
		    	//.
				NameContext = NameContext+"%";
				//.
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
    				if (UserAgent == null)
    					throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
    				_Items = UserAgent.User().GetUserList(NameContext);
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
				if (Canceller.flCancel)
					return; //. ->
	    		//.
	    		PanelHandler.obtainMessage(OnCompletionMessage,_Items).sendToTarget();
	    		//.
    			MessageHandler.obtainMessage(MESSAGE_SUCCESS,_Items).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (NullPointerException NPE) { 
				if (!ParentActivity.isFinishing()) 
		    		MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SUCCESS:
						if (Canceller.flCancel)
			            	break; //. >
		            	if (ItemsUpdating != null) 
		            		ItemsUpdating.Cancel();
	                	TGeoScopeServerUser.TUserDescriptor[] _Items = (TGeoScopeServerUser.TUserDescriptor[])msg.obj;
						if ((_Items != null) && (_Items.length > 0))
							ItemsUpdating = new TUserListUpdating(MESSAGE_UPDATELIST);
						else
				    		Toast.makeText(ParentActivity, R.string.SUsersAreNotFound, Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_SHOWEXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(ParentActivity, ParentActivity.getString(R.string.SErrorOfDataLoading)+E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(ParentActivity);    
		            	progressDialog.setMessage(ParentActivity.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(false); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		                if ((!ParentActivity.isFinishing()) && progressDialog.isShowing()) 
		                	progressDialog.dismiss(); 
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	progressDialog.setProgress((Integer)msg.obj);
		            	//.
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
    private class TUserListUpdating extends TCancelableThread {

    	private static final int MESSAGE_SHOWEXCEPTION 			= 0;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 1;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 3;
    	
    	private int OnCompletionMessage;
    	private boolean flUpdateImmediately;
    	
    	public TUserListUpdating(int pOnCompletionMessage) {
    		this(pOnCompletionMessage, false);
    	}

    	public TUserListUpdating(int pOnCompletionMessage, boolean pflUpdateImmediately) {
    		super();
    		//.
    		OnCompletionMessage = pOnCompletionMessage;
    		flUpdateImmediately = pflUpdateImmediately;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				while (!Canceller.flCancel) {
					if (!flUpdateImmediately) {
						Thread.sleep(UpdateInterval);
			        	//.
						if (Canceller.flCancel)
							return; //. ->
					}
					else
						flUpdateImmediately = false;
					//.
					try {
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
						TGeoScopeServerUser.TUserDescriptor[] _Items;
						synchronized (TUserListComponent.this) {
							int Cnt = ((Items != null) ? Items.length : 0); 
							_Items = new TGeoScopeServerUser.TUserDescriptor[Cnt];
							for (int I = 0; I < Cnt; I++)
								_Items[I] = Items[I].User;
						}
						//.
						if ((_Items != null) && (_Items.length > 0))
							UserAgent.User().UpdateUserInfos(_Items);
						//.
						if (Canceller.flCancel)
							return; //. ->
			    		//.
			    		PanelHandler.obtainMessage(OnCompletionMessage,_Items).sendToTarget();
		        	}
		        	catch (InterruptedException E) {
		        	}
		        	catch (NullPointerException NPE) { 
						if (!ParentActivity.isFinishing()) 
			    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,NPE).sendToTarget();
		        	}
		        	catch (IOException E) {
		    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,E).sendToTarget();
		        	}
		        	catch (Throwable E) {
		    			MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,new Exception(E.getMessage())).sendToTarget();
		        	}
				}
			}
        	catch (InterruptedException E) {
        	}
		}

	    private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_SHOWEXCEPTION:
						if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(ParentActivity, ParentActivity.getString(R.string.SUpdatingUserList)+E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	//.
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }
		
	public Handler PanelHandler = new Handler() {
		
        @Override
        public void handleMessage(Message msg) {
        	try {
                switch (msg.what) {
                
                case MESSAGE_UPDATELIST: 
                	if (!flExists)
                		return; //. ->
                	try {
                    	TGeoScopeServerUser.TUserDescriptor[] _Items = (TGeoScopeServerUser.TUserDescriptor[])msg.obj;
						int Cnt = ((_Items != null) ? _Items.length : 0); 
						TListItem[] NewItems = new TListItem[Cnt];
						for (int I = 0; I < Cnt; I++) {
							TListItem Item = new TListItem();
							Item.User = _Items[I]; 
							NewItems[I] = Item;
						}
                    	UpdateList(NewItems);
                	}
                	catch (Exception E) {
                		Toast.makeText(ParentActivity, ParentActivity.getString(R.string.SUpdatingUserList)+E.getMessage(), Toast.LENGTH_SHORT).show();
                	}
                	break; //. >
                }
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
        }
    };
}
