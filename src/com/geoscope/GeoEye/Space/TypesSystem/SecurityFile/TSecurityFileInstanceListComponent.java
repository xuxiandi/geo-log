package com.geoscope.GeoEye.Space.TypesSystem.SecurityFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
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
import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFilesPanel;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.URL.TURL;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.THintManager;

@SuppressLint("HandlerLeak")
public class TSecurityFileInstanceListComponent extends TUIComponent {

	public static final int idTComponent = SpaceDefines.idTSecurityFile;
	
	public static final int		ItemImageSize = 512;
	public static final String 	ItemImageDataParams = "2;"+Integer.toString(ItemImageSize)+";"+"50"/*50% quality*/;
	
	public static final int LIST_ROW_SIZE_SMALL_ID 	= 1;
	public static final int LIST_ROW_SIZE_NORMAL_ID = 2;
	public static final int LIST_ROW_SIZE_BIG_ID 	= 3;
	
	private static final int MESSAGE_TYPEDDATAFILE_LOADED = 1;
	
	public static final int REQUEST_COMPONENT_CONTENT = 1;
	
	public static class TInstanceListItem {
	
		public long ID;
		//.
		public TComponentTypedDataFiles TypedDataFiles = null;
		
		public TInstanceListItem(long pID) {
			ID = pID;
		}
		
		public String GetName() {
			if ((TypedDataFiles != null) && (TypedDataFiles.Items.length > 0)) 
				return TypedDataFiles.Items[0].DataName; //. ->
			return (Long.toString(ID));
		}
		
		public String GetKey() {
			return (Integer.toString(idTComponent)+"_"+Long.toString(ID));
		}
	}
	
	public static class TOnListItemClickHandler {
		
		public void DoOnListItemClick(TInstanceListItem Item) {
		}
	}
	
	private static class TListItem {
		
		@SuppressWarnings("unused")
		public TGeoScopeServer Server;
		//.
		public int		DataType;
		public String 	DataFormat;
		public String 	Name;
		public String 	Info;
		//.
		public TInstanceListItem Item;
		//.
		public boolean BMP_flLoaded = false;
		public boolean BMP_flNull = false;
		//.
		@SuppressWarnings("unused")
		public TThumbnailImageComposition Composition = null;
		
		public TListItem(TGeoScopeServer pServer, int pDataType, String pDataFormat, String pName, String pInfo, TInstanceListItem pItem) {
			Server = pServer;
			//.
			DataType = pDataType;
			DataFormat = pDataFormat;
			Name = pName;
			Info = pInfo;
			//.
			Item = pItem;
		}
	}
	
	public static class TListAdapter extends BaseAdapter {

		private static final String 		ImageCache_Name = "UserActivityComponentImages";
		private static final int			ImageCache_Size = 1024*1024*10; //. Mb
		private static final CompressFormat ImageCache_CompressFormat = CompressFormat.PNG;
		private static final int			ImageCache_CompressQuality = 100;
		
		private static class TViewHolder {
			
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
		
		private class TImageLoadTask extends AsyncTask<Void, Void, Bitmap> {
			
			private TListItem Item;
			
			private TViewHolder ViewHolder;

			public TImageLoadTask(TListItem pItem, TViewHolder pViewHolder) {
				Item = pItem;
				ViewHolder = pViewHolder;
			}

			@Override
			protected void onPreExecute() {
				if (!Panel.flExists)
					return; //. ->
				//.
				ImageLoadingCount++;
				//.
				if (ImageLoadingCount > 0) 
					ProgressHandler.DoOnStart();
			}
			
			@Override
			protected Bitmap doInBackground(Void... params) {
				try {
					if (!Panel.flExists)
						return null; //. ->
					if (ViewHolder.Item != Item)
						return null; //. ->
					//.
					return LoadImage(); //. ->
				}
				catch (Exception E) {
					return null; //. ->
				}
			}

			@Override
			protected void onPostExecute(Bitmap bitmap) {
				if (!Panel.flExists)
					return; //. ->
				//.
				if ((bitmap != null) && (!isCancelled()) && (ViewHolder.Item == Item)) {
					ViewHolder.ivImage.setImageBitmap(bitmap);
					ViewHolder.ivImage.setOnClickListener(ImageClickListener);
				}
				//.
				ImageLoadingCount--;
				if (ImageLoadingCount == 0) 
					ProgressHandler.DoOnFinish();
			}

			private Bitmap LoadImage() throws Exception {
				if (Item.BMP_flLoaded) {
					if (!Item.BMP_flNull) 
						return ImageCache.getBitmap(Item.Item.GetKey()); //. ->
					else 
						return null; //. ->
				}
				//.
				return null;
			}
		}
		
		protected class TImageRestoreTask extends TAsyncProcessing {
			
			private TListItem Item;
			
			private TViewHolder ViewHolder;
			
			private Bitmap bitmap = null;

			public TImageRestoreTask(TListItem pItem, TViewHolder pViewHolder) {
				super(null);
				//.
				Item = pItem;
				ViewHolder = pViewHolder;
			}

			@Override
			public void Process() throws Exception {
				if (!Panel.flExists)
					return; //. ->
				if (ViewHolder.Item != Item)
					return; //. ->
				//.
				bitmap = RestoreImage();
			}

			@Override 
			public void DoOnCompleted() throws Exception {
				if (!Panel.flExists)
					return; //. ->
				if ((bitmap != null) && (!Canceller.flCancel) && (ViewHolder.Item == Item)) {
					ViewHolder.ivImage.setImageBitmap(bitmap);
					ViewHolder.ivImage.setOnClickListener(ImageClickListener);
				}
			}

			private Bitmap RestoreImage() throws Exception {
				return ImageCache.getBitmap(Item.Item.GetKey());
			}
		}
		
		private Context context;
		//.
		private TSecurityFileInstanceListComponent Panel;
		//.
		private ListView MyListView;
		//.
		private View 				ProgressBar;
		private TProgressHandler 	ProgressHandler;
		//.
		private TListItem[] Items;
		private int					 Items_SelectedIndex = -1;
		//.
		private LayoutInflater layoutInflater;
		//.
		private Executor 	ImageLoadingExecutor = Executors.newFixedThreadPool(5);
		private int 		ImageLoadingCount = 0;
		//.
		private TDiskImageCache ImageCache;
		//.
		public OnClickListener ImageClickListener = new OnClickListener() {
			
			@Override
	        public void onClick(View v) {
	            final int position = MyListView.getPositionForView((View)v.getParent());
	            //.
				final TListItem Item = (TListItem)Items[position];
				if (Item.BMP_flLoaded && (!Item.BMP_flNull)) {
		        	final AlertDialog alert = new AlertDialog.Builder(context).create();
		        	alert.setCancelable(true);
		        	alert.setCanceledOnTouchOutside(true);
		        	LayoutInflater factory = LayoutInflater.from(context);
		        	View layout = factory.inflate(R.layout.image_preview_dialog_layout, null);
		        	ImageView IV = (ImageView)layout.findViewById(R.id.ivPreview);
		        	IV.setImageDrawable(((ImageView)v).getDrawable());
		        	alert.setView(layout);
		        	//.
		        	alert.show();    
				}
	        }
		};
		//.
		public boolean flListIsScrolling = false;
	        
		public TListAdapter(TSecurityFileInstanceListComponent pPanel, ListView pMyListView, View pProgressBar, TListItem[] pItems) {
			context = pPanel.ParentActivity;
			//.
			Panel = pPanel;
			MyListView = pMyListView;
			ProgressBar = pProgressBar;
			//.
			Items = pItems;
			layoutInflater = LayoutInflater.from(context);
			//.
			ImageCache = new TDiskImageCache(context, ImageCache_Name,ImageCache_Size,ImageCache_CompressFormat,ImageCache_CompressQuality, false);
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
				int LayoutID = R.layout.user_activitycomponentlist_row_layout;
				switch (Panel.ListRowSizeID) {
				
				case LIST_ROW_SIZE_SMALL_ID:
					LayoutID = R.layout.user_activitycomponentlist_row_small_layout;
					break; //. >
					
				case LIST_ROW_SIZE_NORMAL_ID:
					LayoutID = R.layout.user_activitycomponentlist_row_layout;
					break; //. >
					
				case LIST_ROW_SIZE_BIG_ID:
					LayoutID = R.layout.user_activitycomponentlist_row_big_layout;
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
			TListItem Item = (TListItem)Items[position];
			//.
			holder.Item = Item;
			//.
			holder.lbName.setText(Item.Name);
			//.
			holder.lbInfo.setText(Item.Info);
			//.
			Bitmap BMP = null;
			//.
			if (!Item.BMP_flLoaded)
				new TImageLoadTask(Item,holder).executeOnExecutor(ImageLoadingExecutor);
			else {
				if (!Item.BMP_flNull)
					if (flListIsScrolling)
						new TImageRestoreTask(Item,holder).Start();
					else {
						BMP = ImageCache.getBitmap(Item.Item.GetKey());
						if (BMP == null) {
							Item.BMP_flLoaded = false;
							//.
							new TImageLoadTask(Item,holder).executeOnExecutor(ImageLoadingExecutor);
						}
					}
			}
			//.
			if (BMP != null) {
				holder.ivImage.setImageBitmap(BMP);
				holder.ivImage.setOnClickListener(ImageClickListener);
			}
			else {
				int ResourceImageID = 0;
				TTypeFunctionality TF = Panel.UserAgent.User().Space.TypesSystem.TTypeFunctionality_Create(idTComponent);
				if (TF != null)
					try {
						ResourceImageID = TF.GetImageResID();
					} finally {
						TF.Release();
					}
				if (ResourceImageID == 0) {
					ResourceImageID = SpaceDefines.TYPEDDATAFILE_TYPE_GetResID(Item.DataType,Item.DataFormat);
					if (ResourceImageID == 0)
						ResourceImageID = R.drawable.user_activity_component_list_placeholder;
				}
				holder.ivImage.setImageDrawable(context.getResources().getDrawable(ResourceImageID));
				holder.ivImage.setOnClickListener(null);
			}
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
	public boolean flStarted = false;
	//.
	public boolean flUpdated = false;
	//.
	private Activity ParentActivity;
	private LinearLayout ParentLayout;
	//.
	private TUserAgent UserAgent;
	//.
	private String Context;
	//.
	private int ListRowSizeID;
	//.
	private TOnListItemClickHandler OnListItemClickHandler;
	//.
    @SuppressWarnings("unused")
	private TextView lbName;
    //.
	private EditText	edContext;
	private Button 		btnSearch;
    //.
    private TInstanceListItem[]	InstanceList = null;
    private TListAdapter		lvInstanceListAdapter = null;
	private ListView 			lvInstanceList;
	//.
	private View ProgressBar;
	//.
	private TUpdating	Updating = null;
	//.
	private TComponentTypedDataFileLoading ComponentTypedDataFileLoading = null;
	
	public TSecurityFileInstanceListComponent(Activity pParentActivity, LinearLayout pParentLayout, String pContext, int pListRowSizeID, TOnListItemClickHandler pOnListItemClickHandler) throws Exception {
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
		//.
		UserAgent = TUserAgent.GetUserAgent(ParentActivity.getApplicationContext());
		if (UserAgent == null)
			throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
		//.
		Context = pContext;
		//.
		ListRowSizeID = pListRowSizeID;
		//.
		OnListItemClickHandler = pOnListItemClickHandler;
		//.
		lbName = (TextView)ParentLayout.findViewById(R.id.lbName);
        //.
        edContext = (EditText)ParentActivity.findViewById(R.id.edContext);
        edContext.setOnEditorActionListener(new OnEditorActionListener() {
        	
			@Override
			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
				btnSearch.callOnClick();
				return false;
			}
        });      
        edContext.setText(Context);
        //.
        btnSearch = (Button)ParentActivity.findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new OnClickListener() {
        	
        	@Override
            public void onClick(View v) {
            	try {
            		Context = edContext.getText().toString();
            		//.
            		StartUpdating();
				} catch (Exception E) {
					Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
				}
            }
        });
        //.
        lvInstanceList = (ListView)ParentLayout.findViewById(R.id.lvActivityComponentList);
        lvInstanceList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvInstanceList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (InstanceList == null)
					return; //. ->
				//.
				try {
					if (OnListItemClickHandler != null)
						OnListItemClickHandler.DoOnListItemClick(InstanceList[arg2]);
					else
						try {
							TInstanceListItem Item = InstanceList[arg2]; 
							if (Item.TypedDataFiles.Count() == 0)
								return; //. ->
							ComponentTypedDataFiles_Process(Item.TypedDataFiles);
						}
						catch (Exception E) {
			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
						}
				}
				finally {
					lvInstanceListAdapter.Items_SetSelectedIndex(arg2, true);
				}
        	}              
        });         
        lvInstanceList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (InstanceList == null)
					return false; //. ->
            	//.
				try {
					final TInstanceListItem _Item = InstanceList[arg2];
					//.
		    		final CharSequence[] _items;
		    		int SelectedIdx = -1;
		    		_items = new CharSequence[3];
		    		_items[0] = ParentActivity.getString(R.string.SOpen); 
		    		_items[1] = ParentActivity.getString(R.string.SContent1); 
		    		_items[2] = ParentActivity.getString(R.string.SGetURLFile); 
		    		//.
		    		AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
		    		builder.setTitle(R.string.SSelect);
		    		builder.setNegativeButton(R.string.SClose,null);
		    		builder.setSingleChoiceItems(_items, SelectedIdx, new DialogInterface.OnClickListener() {
		    			@Override
		    			public void onClick(DialogInterface arg0, int arg1) {
		    		    	try {
		    		    		switch (arg1) {
		    		    		
		    		    		case 0: //. open
		    						try {
	    								if (_Item.TypedDataFiles.Count() > 0) {
		    								TComponentTypedDataFile ComponentTypedDataFile = _Item.TypedDataFiles.Items[0];
		    								ComponentTypedDataFile_Process(ComponentTypedDataFile);
	    								}
			    						//.
			        		    		arg0.dismiss();
		    						}
		    						catch (Exception E) {
		    			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		    						}
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 1: //. content
		    						try {
	    								if (_Item.TypedDataFiles.Count() > 0) {
		    								Intent intent = new Intent(ParentActivity, TComponentTypedDataFilesPanel.class);
		    								intent.putExtra("DataFiles", _Item.TypedDataFiles.ToByteArrayV0());
		    								intent.putExtra("AutoStart", false);
		    								//.
		    								ParentActivity.startActivityForResult(intent, REQUEST_COMPONENT_CONTENT);
	    								}
			    						//.
			        		    		arg0.dismiss();
		    						}
		    						catch (Exception E) {
		    			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		    						}
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 2: //. get URL-file
    								if (_Item.TypedDataFiles.Count() > 0) {
	    								final TComponentTypedDataFile ComponentTypedDataFile = _Item.TypedDataFiles.Items[0].Clone();
	    								//.
	    		    					TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity) {

	    		    						private String URLFN;
	    		    						
	    		    						@Override
	    		    						public void Process() throws Exception {
	    	    								TComponentFunctionality CF = UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
	    	    								if (CF != null) 
	    	    									try {
	    	    										TURL URL = CF.GetDefaultURL();
	    	    										if (URL != null) 
	    	    											try {
	    	    												if (URL.HasData()) {
	    	    													ComponentTypedDataFile.DataType = SpaceDefines.TYPEDDATAFILE_TYPE_Document; 
	    	    													ComponentTypedDataFile.PrepareForComponent(ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID, false, UserAgent.Server);
			    													if (ComponentTypedDataFile.DataFormat.equals(SpaceDefines.TYPEDDATAFILE_TYPE_Document_FORMAT_XML)) 
			    	    	    										CF.ParseFromXMLDocument(ComponentTypedDataFile.GetFileData());
	    	    												}
	    	    	    										//.
	    		    											URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
	    		    											URL.Name = ComponentTypedDataFile.DataName;
	    		    											URL.ConstructURLFile(URLFN);
	    	    											}
	    	    											finally {
	    	    												URL.Release();
	    	    											}
		    	    										else
		    	    											throw new Exception("there is no URL there"); //. =>
	    	    												
	    	    									}
	    	    									finally {
	    	    										CF.Release();
	    	    									}
    	    										else
    	    											throw new Exception("there is no component functionality there"); //. =>
	    		    						}

	    		    						@Override
	    		    						public void DoOnCompleted() throws Exception {
	    		    	    	    		    new AlertDialog.Builder(ParentActivity)
	    		    	            			.setIcon(android.R.drawable.ic_dialog_alert)
	    		    	            			.setTitle(R.string.SInfo)
	    		    	            			.setMessage(ParentActivity.getString(R.string.SURLFileNameHasBeenSaved)+URLFN+"\n"+ParentActivity.getString(R.string.SUseItForImport))
	    		    	            			.setPositiveButton(R.string.SOk, null)
	    		    	            			.show();
	    		    						}
	    		    						
	    		    						@Override
	    		    						public void DoOnException(Exception E) {
	    		    							Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
	    		    						}
	    		    					};
	    		    					Processing.Start();
    								}
		    		    			//.
		        		    		arg0.dismiss();
		        		    		//.
		    		    			break; //. >
		    		    		}
		    		    	}
		    		    	catch (Exception E) {
		    		    		Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		    		    		//.
		    		    		arg0.dismiss();
		    		    	}
		    			}
		    		});
		    		AlertDialog alert = builder.create();
		    		alert.show();
	            	//.
	            	return true; 
				}
				finally {
					lvInstanceListAdapter.Items_SetSelectedIndex(arg2, true);
				}
			}
		}); 
        lvInstanceList.setOnScrollListener(new OnScrollListener() {
        	
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            	if (lvInstanceListAdapter != null)
            		lvInstanceListAdapter.flListIsScrolling = (scrollState != OnScrollListener.SCROLL_STATE_IDLE); 
            }
        });
        //.
        ProgressBar = ParentLayout.findViewById(R.id.pbProgress);
        //.
        final int HintID = THintManager.HINT__Long_click_to_show_an_item_location;
        final TextView lbListHint = (TextView)ParentLayout.findViewById(R.id.lbListHint);
        String Hint = THintManager.GetHint(HintID, ParentActivity);
        if (Hint != null) {
        	lbListHint.setText(Hint);
            lbListHint.setOnLongClickListener(new OnLongClickListener() {
            	
    			@Override
    			public boolean onLongClick(View v) {
    				THintManager.SetHintAsDisabled(HintID);
    	        	lbListHint.setVisibility(View.GONE);
    	        	//.
    				return true;
    			}
    		});
            //.
        	lbListHint.setVisibility(View.VISIBLE);
        }
        else
        	lbListHint.setVisibility(View.GONE);
        //.
        flExists = true;
	}

	@Override
	public void Destroy() throws Exception {
		flExists = false;
		//. 
		if (ComponentTypedDataFileLoading != null) {
			ComponentTypedDataFileLoading.Cancel();
			ComponentTypedDataFileLoading = null;
		}
		//.
		if (Updating != null) {
			Updating.Cancel();
			Updating = null;
		}
	}

	@Override
	public void Start() {
        flStarted = true;
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
	
	public void DoOnResume() {
		if (flStarted && !flUpdated)
	        StartUpdating();
	}
	
	public void DoOnPause() {
		if (flStarted)
	        StopUpdating();
	}
	
	private class TUpdating extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= -1;
    	private static final int MESSAGE_COMPLETED 				= 0;
    	private static final int MESSAGE_COMPLETEDBYCANCEL 		= 1;
    	private static final int MESSAGE_FINISHED 				= 2;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 4;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 5;
    	private static final int MESSAGE_PROGRESSBAR_MESSAGE 	= 6;
    	
    	private boolean flShowProgress = false;
    	private boolean flClosePanelOnCancel = false;
    	
        private ProgressDialog progressDialog;
        //.
        private TInstanceListItem[] InstanceList = null;
    	
    	public TUpdating(boolean pflShowProgress, boolean pflClosePanelOnCancel) {
    		super();
    		//.
    		flShowProgress = pflShowProgress;
    		flClosePanelOnCancel = pflClosePanelOnCancel;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
				try {
					if (flShowProgress)
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
	    			try {
						TTypeFunctionality TF = UserAgent.Server.User.Space.TypesSystem.TTypeFunctionality_Create(idTComponent);
						if (TF != null) 
							try {
								TTSecurityFileFunctionality TSFF = (TTSecurityFileFunctionality)TF; 
			    				long[] _InstanceList = TSFF.GetInstanceListByContext(TSecurityFileInstanceListComponent.this.Context);
			    				int Cnt = _InstanceList.length;
			    				InstanceList = new TInstanceListItem[Cnt];
			    				for (int I = 0; I < Cnt; I++) 
			    					InstanceList[I] = new TInstanceListItem(_InstanceList[I]);
							}
							finally {
								TF.Release();
							}
						//.
	    				if (InstanceList != null) { 
			            	//. supplying the components with its TypesDataFiles
	    					int Cnt = InstanceList.length;
		    				for (int I = 0; I < Cnt; I++) {
		    					Canceller.Check();
		    					//.
		    					try {
			    					TComponentTypedDataFiles TypedDataFiles = new TComponentTypedDataFiles(ParentActivity, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_AllName);
			    					TypedDataFiles.PrepareForComponent(idTComponent,InstanceList[I].ID, true, UserAgent.Server);
			    					//.
			    					InstanceList[I].TypedDataFiles = TypedDataFiles;
			    					//.
			    					String S = InstanceList[I].GetName(); 
		    		    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_MESSAGE,S).sendToTarget();
			    					
		    					}
		    					catch (Exception E) {
		    		    			//. suppress exception MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
		    					}
		    				}
	    				}
					}
					finally {
						if (flShowProgress)
							MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
					}
    				//.
					MessageHandler.obtainMessage(MESSAGE_COMPLETED).sendToTarget();
	        	}
	        	catch (InterruptedException E) {
	        	}
	        	catch (CancelException CE) {
	    			MessageHandler.obtainMessage(MESSAGE_COMPLETEDBYCANCEL).sendToTarget();
	        	}
	        	catch (IOException E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
	        	}
	        	catch (Throwable E) {
	    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
	        	}
			}
			finally {
    			MessageHandler.obtainMessage(MESSAGE_FINISHED).sendToTarget();
			}
		}

		private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	if (Canceller.flCancel)
			            	break; //. >
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            case MESSAGE_COMPLETEDBYCANCEL:
						if (!flExists)
			            	break; //. >
		            	TSecurityFileInstanceListComponent.this.InstanceList = InstanceList;
	           		 	//.
	           		 	TSecurityFileInstanceListComponent.this.Update();
	           		 	//.
	           		 	if ((msg.what == MESSAGE_COMPLETEDBYCANCEL) && ((TSecurityFileInstanceListComponent.this.InstanceList == null) || (TSecurityFileInstanceListComponent.this.InstanceList.length == 0)))
	           		 		ParentActivity.finish();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TSecurityFileInstanceListComponent.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(ParentActivity);    
		            	progressDialog.setMessage(ParentActivity.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
		            			TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity,ParentActivity.getString(R.string.SWaitAMoment)) {
		            				@Override
		            				public void Process() throws Exception {
		            					Thread.sleep(100);
		            					//.
				            			TUpdating.this.CancelAndWait();
		            				}
		            				@Override 
		            				public void DoOnCompleted() throws Exception {
										if (flClosePanelOnCancel)
											ParentActivity.finish();
		            				}
		            				@Override
		            				public void DoOnException(Exception E) {
		            					Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		            				}
		            			};
		            			Processing.Start();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, ParentActivity.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
		            			TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity,ParentActivity.getString(R.string.SWaitAMoment)) {
		            				@Override
		            				public void Process() throws Exception {
		            					Thread.sleep(100);
		            					//.
				            			TUpdating.this.CancelAndWait();
		            				}
		            				@Override 
		            				public void DoOnCompleted() throws Exception {
										if (flClosePanelOnCancel)
											ParentActivity.finish();
		            				}
		            				@Override
		            				public void DoOnException(Exception E) {
		            					Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		            				}
		            			};
		            			Processing.Start();
		            		} 
		            	}); 
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		                if (!ParentActivity.isFinishing() && progressDialog.isShowing()) 
		                	try {
			                	progressDialog.dismiss(); 
		                	}
		                	catch (IllegalArgumentException IAE) {} 
		            	//.
		            	break; //. >
		            
		            case MESSAGE_PROGRESSBAR_PROGRESS:
		            	progressDialog.setProgress((Integer)msg.obj);
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_MESSAGE:
		            	String S = (String)msg.obj;
		            	//.
		            	if ((S != null) && (!S.equals(""))) 
		            		progressDialog.setMessage(ParentActivity.getString(R.string.SLoading)+"  "+S);
		            	else
		            		progressDialog.setMessage(ParentActivity.getString(R.string.SLoading));
		            	break; //. >
		            }
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
	        }
	    };
    }   
	
    private void Update() throws Exception {
    	try {
        	if (InstanceList == null) {
        		lvInstanceList.setAdapter(null);
        		return; //. ->
        	}
    		//.
    		TListItem[] Items = new TListItem[InstanceList.length];
    		int Cnt = InstanceList.length;
    		for (int I = 0; I < Cnt; I++) {
    			TInstanceListItem Item = InstanceList[I];
    			//.
    			int 	DataType = SpaceDefines.TYPEDDATAFILE_TYPE_All;
    			String 	DataFormat = null;
    			String Name = Item.GetName().split("\n")[0];
    			TInstanceListItem _Item = new TInstanceListItem(Item.ID);
    			_Item.TypedDataFiles = new TComponentTypedDataFiles(ParentActivity, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION,SpaceDefines.TYPEDDATAFILE_TYPE_Image);
    			//.
    			TListItem ListItem = new TListItem(UserAgent.Server, DataType, DataFormat, Name, "", _Item);
    			Items[I] = ListItem;
    		}
    		lvInstanceListAdapter = new TListAdapter(this, lvInstanceList, ProgressBar, Items);
    		lvInstanceList.setAdapter(lvInstanceListAdapter);
    	}
    	finally {
    		flUpdated = true;    	
    	}
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,false);
    }    
    
    private void StopUpdating() {
		if (Updating != null) {
			Updating.Cancel();
			Updating = null;
		}
    }
    
	private class TComponentTypedDataFileLoading extends TCancelableThread {

		private static final int MESSAGE_SHOWEXCEPTION = 0;
		private static final int MESSAGE_PROGRESSBAR_SHOW = 1;
		private static final int MESSAGE_PROGRESSBAR_HIDE = 2;
		private static final int MESSAGE_PROGRESSBAR_PROGRESS = 3;

		private TComponentTypedDataFile ComponentTypedDataFile;
		private int OnCompletionMessage;
		// .
		int SummarySize = 0;
		private ProgressDialog progressDialog;

		public TComponentTypedDataFileLoading(TComponentTypedDataFile pComponentTypedDataFile, int pOnCompletionMessage) {
    		super();
    		//.
			ComponentTypedDataFile = pComponentTypedDataFile;
			OnCompletionMessage = pOnCompletionMessage;
			// .
			_Thread = new Thread(this);
			_Thread.start();
		}

		@Override
		public void run() {
			try {
				switch (ComponentTypedDataFile.DataComponentType) {

				case SpaceDefines.idTDATAFile:
					TGeoScopeServerInfo.TInfo ServersInfo = UserAgent.Server.Info.GetInfo();
					TComponentStreamServer CSS = new TComponentStreamServer(ParentActivity, ServersInfo.SpaceDataServerAddress,ServersInfo.SpaceDataServerPort, UserAgent.Server.User.UserID, UserAgent.Server.User.UserPassword);
					try {
						String CFN = TTypesSystem.TypesSystem.SystemTDATAFile.Context_GetFolder()+"/"+ComponentTypedDataFile.FileName();
						//.
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
						try {
							CSS.ComponentStreamServer_GetComponentStream_Begin(ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
							try {
								File CF = new File(CFN);
								RandomAccessFile ComponentStream = new RandomAccessFile(CF,"rw");
								try {
									ComponentStream.seek(ComponentStream.length());
									//.
									CSS.ComponentStreamServer_GetComponentStream_Read(Long.toString(ComponentTypedDataFile.DataComponentID),ComponentStream, Canceller, new TProgressor() {
										@Override
										public synchronized boolean DoOnProgress(int Percentage) {
											MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,Percentage).sendToTarget();
											return true;
										}
									});
								}
								finally {
									ComponentStream.close();
								}
							}
							finally {
								CSS.ComponentStreamServer_GetComponentStream_End();						
							}
						}
						finally {
							MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
						}
						//.
						ComponentTypedDataFile.PrepareAsFullFromFile(CFN);
						//.
						Canceller.Check();
						//.
						TSecurityFileInstanceListComponent.this.MessageHandler.obtainMessage(OnCompletionMessage,ComponentTypedDataFile).sendToTarget();
					}
					finally {
						CSS.Destroy();
					}
					break; //. >

				default:
					String URL1 = UserAgent.Server.Address;
					// . add command path
					URL1 = "http://" + URL1 + "/" + "Space" + "/" + "2"/* URLProtocolVersion */
							+ "/" + Long.toString(UserAgent.Server.User.UserID);
					String URL2 = "Functionality" + "/"
							+ "ComponentDataDocument.dat";
					// . add command parameters
					int WithComponentsFlag = 0;
					URL2 = URL2
							+ "?"
							+ "1"/* command version */
							+ ","
							+ Integer
									.toString(ComponentTypedDataFile.DataComponentType)
							+ ","
							+ Long
									.toString(ComponentTypedDataFile.DataComponentID)
							+ ","
							+ Integer
									.toString(SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION)
							+ ","
							+ Integer.toString(ComponentTypedDataFile.DataType+SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull)
							+ "," + Integer.toString(WithComponentsFlag);
					// .
					byte[] URL2_Buffer;
					try {
						URL2_Buffer = URL2.getBytes("windows-1251");
					} catch (Exception E) {
						URL2_Buffer = null;
					}
					byte[] URL2_EncryptedBuffer = UserAgent.Server.User.EncryptBufferV2(URL2_Buffer);
					// . encode string
					StringBuffer sb = new StringBuffer();
					for (int I = 0; I < URL2_EncryptedBuffer.length; I++) {
						String h = Integer
								.toHexString(0xFF & URL2_EncryptedBuffer[I]);
						while (h.length() < 2)
							h = "0" + h;
						sb.append(h);
					}
					URL2 = sb.toString();
					// .
					String URL = URL1 + "/" + URL2 + ".dat";
					// .
					if (Canceller.flCancel)
						return; // . ->
					// .
					MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW)
							.sendToTarget();
					try {
						HttpURLConnection Connection = UserAgent.Server.OpenConnection(URL);
						try {
							if (Canceller.flCancel)
								return; // . ->
							// .
							InputStream in = Connection.getInputStream();
							try {
								if (Canceller.flCancel)
									return; // . ->
								// .
								int RetSize = Connection.getContentLength();
								if (RetSize > 0) {
									byte[] Data = new byte[RetSize];
									int Size;
									SummarySize = 0;
									int ReadSize;
									while (SummarySize < Data.length) {
										ReadSize = Data.length - SummarySize;
										Size = in.read(Data, SummarySize, ReadSize);
										if (Size <= 0)
											throw new Exception(ParentActivity.getString(R.string.SConnectionIsClosedUnexpectedly)); //. =>
										SummarySize += Size;
										// .
										if (Canceller.flCancel)
											return; // . ->
										// .
										MessageHandler
												.obtainMessage(
														MESSAGE_PROGRESSBAR_PROGRESS,
														(Integer) (100 * SummarySize / Data.length))
												.sendToTarget();
									}
									// .
									ComponentTypedDataFile.FromByteArrayV0(Data);
								}
								else {
									ComponentTypedDataFile.DataType += SpaceDefines.TYPEDDATAFILE_TYPE_SHIFT_FromName_ToFull;
									ComponentTypedDataFile.Data = null;
								}
								//.
								Canceller.Check();
								//.
								TSecurityFileInstanceListComponent.this.MessageHandler
										.obtainMessage(OnCompletionMessage,
												ComponentTypedDataFile)
										.sendToTarget();
							} finally {
								in.close();
							}
						} finally {
							Connection.disconnect();
						}
					} finally {
						MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE)
								.sendToTarget();
					}
					break; //. >
				}
			} catch (InterruptedException E) {
			} catch (CancelException E) {
			} catch (IOException E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION, E)
						.sendToTarget();
			} catch (Throwable E) {
				MessageHandler.obtainMessage(MESSAGE_SHOWEXCEPTION,
						new Exception(E.getMessage())).sendToTarget();
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
						Exception E = (Exception) msg.obj;
						Toast.makeText(
								ParentActivity,
								ParentActivity.getString(R.string.SErrorOfDataLoading)
										+ E.getMessage(), Toast.LENGTH_SHORT)
								.show();
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_SHOW:
						progressDialog = new ProgressDialog(ParentActivity);
						progressDialog.setMessage(ParentActivity.getString(R.string.SLoading));
						progressDialog
								.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						progressDialog.setIndeterminate(false);
						progressDialog.setCancelable(true);
						progressDialog.setOnCancelListener(new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
							}
						});
						progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,
								ParentActivity.getString(R.string.SCancel),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										Cancel();
									}
								});
						// .
						progressDialog.show();
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_HIDE:
		                if ((!ParentActivity.isFinishing()) && progressDialog.isShowing()) 
		                	try {
			                	progressDialog.dismiss(); 
		                	}
		                	catch (IllegalArgumentException IAE) {} 
						// .
						break; // . >

					case MESSAGE_PROGRESSBAR_PROGRESS:
						progressDialog.setProgress((Integer) msg.obj);
						// .
						break; // . >
					}
	        	}
	        	catch (Throwable E) {
	        		TGeoLogApplication.Log_WriteError(E);
	        	}
			}
		};
	}

	public void ComponentTypedDataFiles_Process(final TComponentTypedDataFiles ComponentTypedDataFiles) throws IOException {
		int FC = ComponentTypedDataFiles.Count();
		TComponentTypedDataFile ComponentTypedDataFile = ComponentTypedDataFiles.Items[0];
		//.
		if (FC == 1)
			ComponentTypedDataFile_Process(ComponentTypedDataFile);
		else {
			Intent intent = new Intent(ParentActivity, TComponentTypedDataFilesPanel.class);
			intent.putExtra("DataFiles", ComponentTypedDataFiles.ToByteArrayV0());
			intent.putExtra("AutoStart", false);
			//.
			ParentActivity.startActivityForResult(intent, REQUEST_COMPONENT_CONTENT);
		}
	}
	
	public void ComponentTypedDataFile_Process(TComponentTypedDataFile ComponentTypedDataFile) {
		if (ComponentTypedDataFile.IsLoaded()) {
			ComponentTypedDataFile.Open(UserAgent.User(), ParentActivity);
		} else {
			if (ComponentTypedDataFileLoading != null)
				ComponentTypedDataFileLoading.Cancel();
			ComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(ComponentTypedDataFile, MESSAGE_TYPEDDATAFILE_LOADED);
		}
	}
	
	public final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_TYPEDDATAFILE_LOADED:
    				if (!flExists)
    	            	break; //. >
    				TComponentTypedDataFile ComponentTypedDataFile = (TComponentTypedDataFile) msg.obj;
    				if (ComponentTypedDataFile != null)
    					ComponentTypedDataFile.Open(UserAgent.User(), ParentActivity);
    				// .
    				break; // . >				
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
}
