package com.geoscope.GeoEye;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;
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
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Image.TDiskImageCache;
import com.geoscope.Classes.Data.Types.Image.Compositions.TThumbnailImageComposition;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoLocation;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFilesPanel;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivities;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity.TComponent;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.SecurityFile.TSecurityFileFunctionality;
import com.geoscope.GeoEye.Space.TypesSystem.SecurityFile.TSecurityFileInstanceListPanel;
import com.geoscope.GeoEye.Space.URL.TURL;
import com.geoscope.GeoEye.Space.URLs.TURLFolderListComponent;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.THintManager;

@SuppressLint("HandlerLeak")
public class TUserActivitiesComponentListComponent extends TUIComponent {

	public static final int		ItemImageSize = 512;
	public static final String 	ItemImageDataParams = "2;"+Integer.toString(ItemImageSize)+";"+"50"/*50% quality*/;
	
	public static final int LIST_ROW_SIZE_SMALL_ID 	= 1;
	public static final int LIST_ROW_SIZE_NORMAL_ID = 2;
	public static final int LIST_ROW_SIZE_BIG_ID 	= 3;

	private static final int MESSAGE_TYPEDDATAFILE_LOADED = 1;
	
	public static final int REQUEST_COMPONENT_CONTENT 			= 301;
	public static final int REQUEST_COMPONENT_CHANGESECURITY 	= 302;
	public static final int REQUEST_SELECT_USER_FORURL 			= 303;
	
	public static class TOnItemsLoadedHandler {
		
		public void DoOnItemsLoaded(TActivity.TComponents ActivitiesComponents) {
		}
	}
	
	public static class TOnListItemClickHandler {
		
		public void DoOnListItemClick(TComponent Component) {
		}
	}
	
	private static class TComponentListItem {
		
		public TGeoScopeServer Server;
		//.
		public int		DataType;
		public String 	DataFormat;
		public String 	Name;
		public String 	Info;
		//.
		public TComponent Component;
		//.
		public boolean BMP_flLoaded = false;
		public boolean BMP_flNull = false;
		//.
		public TThumbnailImageComposition Composition = null;
		
		public TComponentListItem(TGeoScopeServer pServer, int pDataType, String pDataFormat, String pName, String pInfo, TComponent pComponent) {
			Server = pServer;
			//.
			DataType = pDataType;
			DataFormat = pDataFormat;
			Name = pName;
			Info = pInfo;
			//.
			Component = pComponent;
		}
	}
	
	public static class TComponentListAdapter extends BaseAdapter {

		private static final String 		ImageCache_Name = "UserActivitiesComponentImages";
		private static final int			ImageCache_Size = 1024*1024*10; //. Mb
		private static final CompressFormat ImageCache_CompressFormat = CompressFormat.PNG;
		private static final int			ImageCache_CompressQuality = 100;
		
		private static class TViewHolder {
			
			public TComponentListItem Item;
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
			
			private TComponentListItem Item;
			
			private TViewHolder ViewHolder;

			public TImageLoadTask(TComponentListItem pItem, TViewHolder pViewHolder) {
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
						return ImageCache.getBitmap(Item.Component.GetKey()); //. ->
					else 
						return null; //. ->
				}
				//.
				Bitmap Result = null;
				//. 
				boolean flProcessAsDefault = true;
				switch (Item.DataType) {

				case SpaceDefines.TYPEDDATAFILE_TYPE_DocumentName:
					if ((Item.DataFormat != null) && Item.DataFormat.equals(SpaceDefines.TYPEDDATAFILE_TYPE_Document_FORMAT_XML)) {
						TComponentTypedDataFiles ComponentTypedDataFiles = new TComponentTypedDataFiles(context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_Document);
						ComponentTypedDataFiles.PrepareForComponent(Item.Component.idTComponent,Item.Component.idComponent, (Item.Component.idTComponent == SpaceDefines.idTCoComponent), Item.Server);
						//.
						TComponentTypedDataFile ComponentTypedDataFile = ComponentTypedDataFiles.GetRootItem(); 
						if ((ComponentTypedDataFile != null) && ComponentTypedDataFile.DataFormat.equals(SpaceDefines.TYPEDDATAFILE_TYPE_Document_FORMAT_XML)) {
							TComponentFunctionality CF = Item.Server.User.Space.TypesSystem.TComponentFunctionality_Create(ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
							if (CF != null) 
								try {
									CF.ParseFromXMLDocument(ComponentTypedDataFile.GetFileData());
									Item.Composition = CF.GetThumbnailImageComposition();
									if (Item.Composition != null) {
										Result = Item.Composition.TakeBitmap();
										flProcessAsDefault = false;
									}
									else
										flProcessAsDefault = true;
								}
								finally {
									CF.Release();
								}
						}
					}
					break; //. >
				}
				//.
				if (flProcessAsDefault) {
					Item.Component.TypedDataFiles.PrepareForComponent(Item.Component.idTComponent,Item.Component.idComponent, ItemImageDataParams, true, Item.Server);
					ArrayList<TComponentTypedDataFile> ImageDataFiles = Item.Component.TypedDataFiles.GetItemsByDataType(SpaceDefines.TYPEDDATAFILE_TYPE_Image);
					int Cnt = ImageDataFiles.size();
					if (Cnt > 0) {
						Item.Composition = TComponentTypedDataFiles.GetImageComposition(ImageDataFiles, ItemImageSize);
						if (Item.Composition != null)
							Result = Item.Composition.TakeBitmap();
					}
				}
				//.
				if (Result != null) {
					//. draw the type image on the result
					int ResourceImageID = 0;
					TTypeFunctionality TF = Panel.UserAgent.User().Space.TypesSystem.TTypeFunctionality_Create(Item.Component.idTComponent);
					if (TF != null)
						try {
							ResourceImageID = TF.GetImageResID();
						} finally {
							TF.Release();
						}
					if (ResourceImageID == 0) 
						ResourceImageID = SpaceDefines.TYPEDDATAFILE_TYPE_GetResID(Item.DataType,Item.DataFormat);
					if (ResourceImageID != 0) {
						int ImageSize = Result.getWidth();
						Drawable D = context.getResources().getDrawable(ResourceImageID).mutate();
						D.setBounds(0,0, (ImageSize >> 2),(ImageSize >> 2));
						D.setAlpha(128);
						Bitmap LastResult = Result;
						Result = Result.copy(Config.ARGB_8888,true);
						LastResult.recycle();
						D.draw(new Canvas(Result));
					}
					//.
					ImageCache.put(Item.Component.GetKey(), Result);
				}
				else
					Item.BMP_flNull = true;
				Item.BMP_flLoaded = true;
				//.
				return Result;
			}
		}
		
		protected class TImageRestoreTask extends TAsyncProcessing {
			
			private TComponentListItem Item;
			
			private TViewHolder ViewHolder;
			
			private Bitmap bitmap = null;

			public TImageRestoreTask(TComponentListItem pItem, TViewHolder pViewHolder) {
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
				return ImageCache.getBitmap(Item.Component.GetKey());
			}
		}
		
		private Context context;
		//.
		private TUserActivitiesComponentListComponent Panel;
		//.
		private ListView MyListView;
		//.
		private View 				ProgressBar;
		private TProgressHandler 	ProgressHandler;
		//.
		private TComponentListItem[] Items;
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
				final TComponentListItem Item = (TComponentListItem)Items[position];
				if (Item.BMP_flLoaded && (!Item.BMP_flNull)) {
		        	final AlertDialog alert = new AlertDialog.Builder(context).create();
		        	alert.setCancelable(true);
		        	alert.setCanceledOnTouchOutside(true);
		        	LayoutInflater factory = LayoutInflater.from(context);
		        	View layout = factory.inflate(R.layout.image_preview_dialog_layout, null);
		        	ImageView IV = (ImageView)layout.findViewById(R.id.ivPreview);
		        	IV.setImageDrawable(((ImageView)v).getDrawable());
		        	IV.setOnTouchListener(new OnTouchListener() {
						
						@Override
						public boolean onTouch(View v, MotionEvent event) {
							int action = event.getAction();
							switch (action & MotionEvent.ACTION_MASK) {
							
							case MotionEvent.ACTION_DOWN: 
								float X = event.getX();
								float Y = event.getY();
								if (Item.Composition != null)
									Item.Composition.Map.CheckItemByPosition(X,Y);
								break; //. >							
							}
						      return false;
						}
					});
		        	IV.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							try {
								TComponent Component = Panel.ActivitiesComponents.Items[position]; 
								int FC = Component.TypedDataFiles.Count();
								if (FC > 0) {
									if (FC == 1)
										Panel.ComponentTypedDataFiles_Process(Component, Component.TypedDataFiles);
									else {
	    								Intent intent = new Intent(Panel.ParentActivity, TComponentTypedDataFilesPanel.class);
	    								if (Panel.Component != null)
	    									intent.putExtra("ComponentID", Panel.Component.ID);
	    								intent.putExtra("DataFiles", Component.TypedDataFiles.ToByteArrayV0());
	    								intent.putExtra("AutoStart", false);
	    								//.
	    								Panel.ParentActivity.startActivityForResult(intent, REQUEST_COMPONENT_CONTENT);
									}
								}
								//.
								alert.dismiss();
							}
							catch (Exception E) {
				                Toast.makeText(Panel.ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
							}
						}
					});
		        	IV.setOnLongClickListener(new OnLongClickListener() {
						
						@Override
						public boolean onLongClick(View v) {
							if ((Item.Composition != null) && (Item.Composition.Map.ItemByPosition != null)) {
								TComponentTypedDataFile ComponentTypedDataFile = (TComponentTypedDataFile)Item.Composition.Map.ItemByPosition.LinkedObject;
								//.
								Panel.ComponentTypedDataFile_Process(ComponentTypedDataFile);
							}
							return false;
						}
					});
		        	alert.setView(layout);
		        	//.
		        	alert.show();    
				}
	        }
		};
		//.
		public boolean flListIsScrolling = false;
	        
		public TComponentListAdapter(TUserActivitiesComponentListComponent pPanel, ListView pMyListView, View pProgressBar, TComponentListItem[] pItems) {
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
		
		public int Items_GetSelectedIndex() {
			return Items_SelectedIndex;
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
			TComponentListItem Item = (TComponentListItem)Items[position];
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
						BMP = ImageCache.getBitmap(Item.Component.GetKey());
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
				TTypeFunctionality TF = Panel.UserAgent.User().Space.TypesSystem.TTypeFunctionality_Create(Item.Component.idTComponent);
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
	private long	UserID = 0;	
	private double BeginTimestamp;
	private double EndTimestamp;
	//.
	private int ListRowSizeID;
	//.
	private TReflectorComponent Component;
	//.
	private TOnListItemClickHandler OnListItemClickHandler;
	//.
    private TActivities 			Activities = null;
    private TActivity.TComponents 	ActivitiesComponents = null;
    //.
    private TOnItemsLoadedHandler	OnItemsLoadedHandler;
    //.
    private TComponentListAdapter	lvActivitiesComponentListAdapter = null;
	private ListView 				lvActivitiesComponentList;
	//.
	private View ProgressBar;
	//.
	private TUpdating	Updating = null;
	//.
	private TComponentTypedDataFileLoading ComponentTypedDataFileLoading = null;
	
	public TUserActivitiesComponentListComponent(Activity pParentActivity, LinearLayout pParentLayout, long pUserID, double pBeginTimestamp, double pEndTimestamp, int pListRowSizeID, TReflectorComponent pComponent, TOnItemsLoadedHandler pOnItemsLoadedHandler, TOnListItemClickHandler pOnListItemClickHandler) throws Exception {
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
		//.
		UserAgent = TUserAgent.GetUserAgent();
		if (UserAgent == null)
			throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
		//.
		UserID = pUserID;
		BeginTimestamp = pBeginTimestamp;
		EndTimestamp = pEndTimestamp;
		//.
		ListRowSizeID = pListRowSizeID;
        //.
		Component = pComponent;
		//.
		OnItemsLoadedHandler = pOnItemsLoadedHandler;
		//.
		OnListItemClickHandler = pOnListItemClickHandler;
        //.
        lvActivitiesComponentList = (ListView)ParentLayout.findViewById(R.id.lvActivityComponentList);
        lvActivitiesComponentList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvActivitiesComponentList.setOnItemClickListener(new OnItemClickListener() {        
        	
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (ActivitiesComponents == null)
					return; //. ->
				//.
				try {
					if (OnListItemClickHandler != null)
						OnListItemClickHandler.DoOnListItemClick(ActivitiesComponents.Items[arg2]);
					else
						try {
							TComponent Component = ActivitiesComponents.Items[arg2]; 
							if (Component.TypedDataFiles.Count() == 0)
								return; //. ->
							//.
							ComponentTypedDataFiles_Process(Component, Component.TypedDataFiles);
						}
						catch (Exception E) {
			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
						}
				}
				finally {
					lvActivitiesComponentListAdapter.Items_SetSelectedIndex(arg2, true);
				}
        	}              
        });         
        lvActivitiesComponentList.setOnItemLongClickListener(new OnItemLongClickListener() {
        	
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (ActivitiesComponents == null)
					return false; //. ->
            	//.
				try {
					final TComponent _Component = ActivitiesComponents.Items[arg2];
					//.
		    		final CharSequence[] _items;
		    		int SelectedIdx = -1;
		    		_items = new CharSequence[7];
		    		_items[0] = ParentActivity.getString(R.string.SOpen); 
		    		_items[1] = ParentActivity.getString(R.string.SContent1); 
		    		_items[2] = ParentActivity.getString(R.string.SUserActivity); 
		    		_items[3] = ParentActivity.getString(R.string.SShowGeoLocation); 
		    		_items[4] = ParentActivity.getString(R.string.SGetURLFile); 
		    		_items[5] = ParentActivity.getString(R.string.SSendURLLinkToUser); 
		    		_items[6] = ParentActivity.getString(R.string.SSecurity1); 
		    		_items[7] = ParentActivity.getString(R.string.SRemove); 
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
	    								if (_Component.TypedDataFiles.Count() > 0) {
		    								TComponentTypedDataFile ComponentTypedDataFile = _Component.TypedDataFiles.Items[0];
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
	    								if (_Component.TypedDataFiles.Count() > 0) {
		    								Intent intent = new Intent(ParentActivity, TComponentTypedDataFilesPanel.class);
		    								if (Component != null)
		    									intent.putExtra("ComponentID", Component.ID);
		    								intent.putExtra("DataFiles", _Component.TypedDataFiles.ToByteArrayV0());
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
		    		    			
		    		    		case 2: //. User activity info
		    		    			String UserActivityInfo;
	    	    					if (Activities != null) {
	    	            				TActivity Activity = Activities.GetItem(_Component.idActivity);
	    	            				if (Activity != null) 
	    	            					UserActivityInfo = Activity.GetInfo(ParentActivity);
	    	            				else
	    	            					UserActivityInfo = "?";
	    	    					}
	    	    					else
	    	    						UserActivityInfo = "??";
		    		    			//.
		    		    		    new AlertDialog.Builder(ParentActivity)
		    		    	        .setIcon(android.R.drawable.ic_dialog_info)
		    		    	        .setTitle(R.string.SUserActivity)
		    		    	        .setMessage(UserActivityInfo)
		    		    		    .setPositiveButton(R.string.SOk, null)
		    		    		    .show();
		    						//.
		        		    		arg0.dismiss();
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 3: //. show location
		    						if (_Component.GeoLocation != null)
		    							ShowComponentGeoLocation(_Component);
		    						else
		    							ShowComponentVisualizationPosition(_Component);
		    						//.
		        		    		arg0.dismiss();
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 4: //. get URL-file
    								if (_Component.TypedDataFiles.Count() > 0) {
	    								final TComponentTypedDataFile ComponentTypedDataFile = _Component.TypedDataFiles.Items[0].Clone();
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
		    		    			
		    		    		case 5: //. send URL-file to user
    								if (_Component.TypedDataFiles.Count() > 0) {
	    								final TComponentTypedDataFile ComponentTypedDataFile = _Component.TypedDataFiles.Items[0].Clone();
	    								//.
	    		    					TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity) {

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
	    	    												String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
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
	    		    							SendComponentURLToUser();
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
		    		    			
		    		    		case 6: //. component security
		    						final CharSequence[] _items;
		    						_items = new CharSequence[2];
		    						_items[0] = ParentActivity.getString(R.string.SShowSecurity);
		    						_items[1] = ParentActivity.getString(R.string.SChangeSecurity);
		    						AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
		    						builder.setTitle(R.string.SOperations);
		    						builder.setNegativeButton(ParentActivity.getString(R.string.SCancel),null);
		    						builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
		    							
		    							@Override
		    							public void onClick(DialogInterface arg0, int arg1) {
	    									switch (arg1) {
	    									
	    									case 0: //. show security 
	    				            			TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity,R.string.SGettingSecurityInfo) {
	    				            				
	    				            				private TSecurityFileFunctionality SecurityFileFunctionality = null;
	    				            				
	    				            				@Override
	    				            				public void Process() throws Exception {
	    				            					long SecurityFileID;
	    				        						TComponentFunctionality CF = UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(_Component.idTComponent,_Component.idComponent);
	    				        						if (CF != null)
	    				        							try {
	    				        								SecurityFileID = CF.GetSecurity();
	    				        							} finally {
	    				        								CF.Release();
	    				        							}
	    				        							else
	    				        								throw new Exception("there is no functionality for type, idType = "+Integer.toString(_Component.idTComponent)); //. =>
	    				        						//.
	    				        						TComponentTypedDataFiles ComponentTypedDataFiles = new TComponentTypedDataFiles(context, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_Document);
	    				        						ComponentTypedDataFiles.PrepareForComponent(SpaceDefines.idTSecurityFile,SecurityFileID, false, UserAgent.User().Server);
	    				        						//.
	    				        						TComponentTypedDataFile ComponentTypedDataFile = ComponentTypedDataFiles.GetRootItem(); 
	    				        						if ((ComponentTypedDataFile != null) && ComponentTypedDataFile.DataFormat.equals(SpaceDefines.TYPEDDATAFILE_TYPE_Document_FORMAT_XML)) {
		    				        						SecurityFileFunctionality = (TSecurityFileFunctionality)UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
		    				        						if (SecurityFileFunctionality != null)
	    				        								SecurityFileFunctionality.ParseFromXMLDocument(ComponentTypedDataFile.GetFileData());
	    				        						}
	    				        						else
    				        								throw new Exception("there is no data for security file, SID: "+Long.toString(SecurityFileID)); //. =>
	    				            					//.
	    				            					Thread.sleep(100);
	    				            				}
	    				            				
	    				            				@Override 
	    				            				public void DoOnCompleted() throws Exception {
	    				            					if (SecurityFileFunctionality != null) 
	    				            						SecurityFileFunctionality.Open(context, null);
	    				            					else
	    				            						throw new Exception("there is no security file info"); //. =>
	    				            				}
	    				            				
	    				            				@Override 
	    				            				public void DoOnFinished() throws Exception {
	    				            					if (SecurityFileFunctionality != null)
	    				            						SecurityFileFunctionality.Release();
	    				            				}
	    				            				
	    				            				@Override
	    				            				public void DoOnException(Exception E) {
	    				            					Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
	    				            				}
	    				            			};
	    				            			Processing.Start();
	    										break; //. >

	    									case 1: //. change security
	    			    						final CharSequence[] _items;
	    			    						_items = new CharSequence[3];
	    			    						_items[0] = ParentActivity.getString(R.string.SDefault);
	    			    						_items[1] = ParentActivity.getString(R.string.SPrivate);
	    			    						_items[2] = ParentActivity.getString(R.string.SOther);
	    			    						AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
	    			    						builder.setTitle(R.string.SSelectSecurity);
	    			    						builder.setNegativeButton(ParentActivity.getString(R.string.SCancel),null);
	    			    						builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
	    			    							
	    			    							@Override
	    			    							public void onClick(DialogInterface arg0, int arg1) {
	    			    								arg0.dismiss();
	    			    								//.
	    			    				            	try {
	    			    				            		final long SecurityFileID;
	    			    									switch (arg1) {
	    			    									
	    			    									case 0: //. default security 
	    			    										SecurityFileID = TComponentFunctionality.USER_DEFAULT_SECURITY_FILE_ID;
	    			    										break; //. >

	    			    									case 1: //. private security
	    			    										SecurityFileID = TComponentFunctionality.USER_PRIVATE_SECURITY_FILE_ID;
	    			    										break; //. >
	    			    									
	    			    									case 2: //. other security
	    			    				            			TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity,ParentActivity.getString(R.string.SWaitAMoment)) {
	    			    				            				
	    			    				            				private TGeoScopeServerUser.TUserDescriptor UserDescriptor;
	    			    				            				
	    			    				            				@Override
	    			    				            				public void Process() throws Exception {
	    			    				            					UserDescriptor = UserAgent.User().GetUserInfo();
	    			    				            					//.
	    			    				            					Thread.sleep(100);
	    			    				            				}
	    			    				            				
	    			    				            				@Override 
	    			    				            				public void DoOnCompleted() throws Exception {
	    					    										Intent intent = new Intent(ParentActivity,TSecurityFileInstanceListPanel.class);
	    					    										intent.putExtra("Context", UserDescriptor.UserName);
	    					    										intent.putExtra("ConfirmChoice", true);
	    					    										ParentActivity.startActivityForResult(intent, REQUEST_COMPONENT_CHANGESECURITY);
	    			    				            				}
	    			    				            				
	    			    				            				@Override
	    			    				            				public void DoOnException(Exception E) {
	    			    				            					Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
	    			    				            				}
	    			    				            			};
	    			    				            			Processing.Start();
	    			    										return; //. ->
	    			    									
	    			    									default:
	    			    										return; //. ->
	    			    									}
	    			    									//.
	    				    		    					TAsyncProcessing SecurityChanging = new TAsyncProcessing(ParentActivity, ParentActivity.getString(R.string.SChangingSecurity)) {

	    				    		    						@Override
	    				    		    						public void Process() throws Exception {
	    				    		    							TComponentFunctionality CF = UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(_Component.idTComponent,_Component.idComponent);
	    				    		    							if (CF != null)
	    				    		    								try {
	    				    		    									CF.ChangeSecurity(SecurityFileID);
	    				    		    								} finally {
	    				    		    									CF.Release();
	    				    		    								}
	    				    		    								else
	    				    		    									throw new Exception("there is no functionality for type, idType = "+Integer.toString(_Component.idTComponent)); //. =>
	    				    		    						}

	    				    		    						@Override
	    				    		    						public void DoOnCompleted() throws Exception {
	    				    		    							Toast.makeText(ParentActivity, R.string.SSecurityHasBeenChanged, Toast.LENGTH_LONG).show();
	    				    		    						}
	    				    		    						
	    				    		    						@Override
	    				    		    						public void DoOnException(Exception E) {
	    				    		    							Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
	    				    		    						}
	    				    		    					};
	    				    		    					SecurityChanging.Start();
	    			    								}
	    			    								catch (Exception E) {
	    			    									String S = E.getMessage();
	    			    									if (S == null)
	    			    										S = E.getClass().getName();
	    			    				        			Toast.makeText(ParentActivity, ParentActivity.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
	    			    								}
	    			    							}
	    			    						});
	    			    						AlertDialog AD = builder.create();
	    			    						AD.show();
	    			    						//.
	    										break; //. >
	    									}
		    							}
		    						});
		    						AlertDialog AD = builder.create();
		    						AD.show();
		    		    			//.
		        		    		arg0.dismiss();
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 7: //. remove component
		    		    			AlertDialog.Builder alert = new AlertDialog.Builder(ParentActivity);
		    		    			//.
		    		    			alert.setTitle(R.string.SRemoval);
		    		    			alert.setMessage(R.string.SRemoveSelectedComponent);
		    		    			//.
		    		    			alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
		    		    				
		    		    				@Override
		    		    				public void onClick(DialogInterface dialog, int whichButton) {
		    		    					TAsyncProcessing Removing = new TAsyncProcessing(ParentActivity, ParentActivity.getString(R.string.SRemoving)) {

		    		    						@Override
		    		    						public void Process() throws Exception {
		    		    							TTypeFunctionality TF = UserAgent.User().Space.TypesSystem.TTypeFunctionality_Create(_Component.idTComponent);
		    		    							if (TF != null)
		    		    								try {
		    		    									TF.DestroyInstance(_Component.idComponent);
		    		    								} finally {
		    		    									TF.Release();
		    		    								}
		    		    								else
		    		    									throw new Exception("there is no functionality for type, idType = "+Integer.toString(_Component.idTComponent)); //. =>
		    		    						}

		    		    						@Override
		    		    						public void DoOnCompleted() throws Exception {
		    		    							StartUpdating();	    		    							
		    		    							//.
		    		    							Toast.makeText(ParentActivity, R.string.SObjectHasBeenRemoved, Toast.LENGTH_LONG).show();
		    		    						}
		    		    						
		    		    						@Override
		    		    						public void DoOnException(Exception E) {
		    		    							Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
		    		    						}
		    		    					};
		    		    					Removing.Start();
		    		    				}
		    		    			});
		    		    			//.
		    		    			alert.setNegativeButton(R.string.SCancel, null);
		    		    			//.
		    		    			alert.show();
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
					lvActivitiesComponentListAdapter.Items_SetSelectedIndex(arg2, true);
				}
			}
		}); 
        lvActivitiesComponentList.setOnScrollListener(new OnScrollListener() {
        	
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            	if (lvActivitiesComponentListAdapter != null)
            		lvActivitiesComponentListAdapter.flListIsScrolling = (scrollState != OnScrollListener.SCROLL_STATE_IDLE); 
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

	public TUserActivitiesComponentListComponent(Activity pParentActivity, LinearLayout pParentLayout, long pUserID, double pBeginTimestamp, double pEndTimestamp, int pListRowSizeID, TReflectorComponent pComponent, TOnListItemClickHandler pOnListItemClickHandler) throws Exception {
		this(pParentActivity,pParentLayout, pUserID, pBeginTimestamp,pEndTimestamp, pListRowSizeID, pComponent, null, pOnListItemClickHandler);
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
	
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	//.
        switch (requestCode) {        

        case REQUEST_COMPONENT_CONTENT: 
        	if (resultCode == Activity.RESULT_OK)
				try {
					Restart();
				} catch (Exception E) {
				}
        	break; //. >

        case REQUEST_COMPONENT_CHANGESECURITY: 
        	if (resultCode == Activity.RESULT_OK) {
        		int SI = lvActivitiesComponentListAdapter.Items_GetSelectedIndex();
        		if (SI >= 0) {
            		final long SecurityFileID = data.getExtras().getLong("SecurityFileID");
            		final TActivity.TComponent _Component = ActivitiesComponents.Items[SI];
    				TAsyncProcessing SecurityChanging = new TAsyncProcessing(ParentActivity, ParentActivity.getString(R.string.SChangingSecurity)) {

    					@Override
    					public void Process() throws Exception {
    						TComponentFunctionality CF = UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(_Component.idTComponent,_Component.idComponent);
    						if (CF != null)
    							try {
    								CF.ChangeSecurity(SecurityFileID);
    							} finally {
    								CF.Release();
    							}
    							else
    								throw new Exception("there is no functionality for type, idType = "+Integer.toString(_Component.idTComponent)); //. =>
    					}

    					@Override
    					public void DoOnCompleted() throws Exception {
    						Toast.makeText(ParentActivity, R.string.SSecurityHasBeenChanged, Toast.LENGTH_LONG).show();
    					}
    					
    					@Override
    					public void DoOnException(Exception E) {
    						Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
    					}
    				};
    				SecurityChanging.Start();
        		}
        		else
					Toast.makeText(ParentActivity, "component is not selected",	Toast.LENGTH_LONG).show();
        	}
        	break; //. >
        	
        case REQUEST_SELECT_USER_FORURL:
        	if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
            		long UserID = extras.getLong("UserID");
    				try {
                		DoSendComponentURLToUser(UserID);
    		    	}
    		    	catch (Exception E) {
    		    		Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
    		    	}
            	}
        	}
            break; //. >
        }
        //.
    	super.onActivityResult(requestCode, resultCode, data);
    }
    
	protected void FilterActivityComponents(TActivity.TComponents ActivityComponents) {
		if (ActivityComponents == null)
			return; //. ->
		ArrayList<TActivity.TComponent> FilteredList = new ArrayList<TActivity.TComponent>(ActivityComponents.Items.length);
		for (int I = 0; I < ActivityComponents.Items.length; I++)
			switch (ActivityComponents.Items[I].idTComponent) {
			
			case SpaceDefines.idTCoComponent:
			case SpaceDefines.idTDATAFile:
			case SpaceDefines.idTPositioner:
			case SpaceDefines.idTMapFormatObject:
				FilteredList.add(ActivityComponents.Items[I]);
				break; //. >
			}
		ActivityComponents.Items = new TActivity.TComponent[FilteredList.size()];
		for (int I = 0; I < FilteredList.size(); I++)
			ActivityComponents.Items[I] = FilteredList.get(I); 
	}
	
	protected void OptimizeActivityComponents(TActivity.TComponents ActivityComponents) {
		if (ActivityComponents == null)
			return; //. ->
		ArrayList<TActivity.TComponent> FilteredList = new ArrayList<TActivity.TComponent>(ActivityComponents.Items.length);
		for (int I = 0; I < ActivityComponents.Items.length; I++)
			if (ActivityComponents.Items[I].TypedDataFiles != null)
				FilteredList.add(ActivityComponents.Items[I]);
		ActivityComponents.Items = new TActivity.TComponent[FilteredList.size()];
		for (int I = 0; I < FilteredList.size(); I++)
			ActivityComponents.Items[I] = FilteredList.get(I); 
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
        private TActivities 			Activities = null;
        private TActivity.TComponents 	ActivitiesComponents = null;
    	
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
	    				Activities = UserAgent.Server.User.GetUserActivityList(UserID, BeginTimestamp,EndTimestamp);
	    				//.
	    				ActivitiesComponents = UserAgent.Server.User.GetUserActivitiesComponentList(UserID, BeginTimestamp,EndTimestamp);
	    				if (ActivitiesComponents != null) {
			            	FilterActivityComponents(ActivitiesComponents);
			            	//. supplying the components with its TypesDataFiles
			            	try {
			    				for (int I = 0; I < ActivitiesComponents.Items.length; I++) {
			    					Canceller.Check();
			    					//.
			    					try {
				    					TComponentTypedDataFiles TypedDataFiles = new TComponentTypedDataFiles(ParentActivity, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_AllName);
				    					TypedDataFiles.PrepareForComponent(ActivitiesComponents.Items[I].idTComponent,ActivitiesComponents.Items[I].idComponent, true, UserAgent.Server);
				    					//.
				    					ActivitiesComponents.Items[I].TypedDataFiles = TypedDataFiles;
				    					//.
				    					String S = ActivitiesComponents.Items[I].GetName(); 
			    		    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_MESSAGE,S).sendToTarget();
				    					
			    					}
			    					catch (Exception E) {
			    		    			//. suppress exception MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
			    					}
			    				}
			            	}
			            	finally {
				            	OptimizeActivityComponents(ActivitiesComponents);
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
		            	TUserActivitiesComponentListComponent.this.Activities = Activities;
		            	TUserActivitiesComponentListComponent.this.ActivitiesComponents = ActivitiesComponents;
		            	//.
		            	if (OnItemsLoadedHandler != null) 
		            		OnItemsLoadedHandler.DoOnItemsLoaded(ActivitiesComponents);
	           		 	//.
	           		 	TUserActivitiesComponentListComponent.this.Update();
	           		 	//.
	           		 	if ((msg.what == MESSAGE_COMPLETEDBYCANCEL) && ((TUserActivitiesComponentListComponent.this.ActivitiesComponents == null) || (TUserActivitiesComponentListComponent.this.ActivitiesComponents.Items.length == 0)))
	           		 		ParentActivity.finish();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TUserActivitiesComponentListComponent.this.Updating = null;
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
	                	try {
		                	progressDialog.dismiss(); 
	                	}
	                	catch (IllegalArgumentException IAE) {} //. TODO
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
        	if (ActivitiesComponents == null) {
        		lvActivitiesComponentList.setAdapter(null);
        		return; //. ->
        	}
    		long LastActivityID = 0;
    		TComponentListItem[] Items = new TComponentListItem[ActivitiesComponents.Items.length];
    		for (int I = 0; I < ActivitiesComponents.Items.length; I++) {
    			TActivity.TComponent Component = ActivitiesComponents.Items[I];
    			//.
    			int 	DataType = SpaceDefines.TYPEDDATAFILE_TYPE_All;
    			String 	DataFormat = null;
    			String Name = Component.GetName().split("\n")[0];
    			TComponentTypedDataFile DataFile = Component.TypedDataFiles.GetRootItem();
    			TComponent _Component;
    			if (DataFile != null) {
    				DataType = Component.TypedDataFiles.Items[0].DataType;
    				DataFormat = Component.TypedDataFiles.Items[0].DataFormat;
    				switch (Component.TypedDataFiles.Items[0].DataComponentType) {
    				
    				case SpaceDefines.idTPositioner:
    					Name = Name+" "+"/"+SpaceDefines.ComponentType_GetName(Component.TypedDataFiles.Items[0].DataComponentType,ParentActivity)+"/";
    					break; //. >
    					
    				case SpaceDefines.idTMapFormatObject:
    					break; //. >
    					
    				default:
    					if (!SpaceDefines.TYPEDDATAFILE_TYPE_Document_IsXMLFormat(DataType,DataFormat))
    						Name = Name+" "+"/"+SpaceDefines.TYPEDDATAFILE_TYPE_String(DataType,ParentActivity)+"/";
    					break; //. >
    				}
    				//.
    				_Component = new TComponent(Component.idActivity, DataFile.DataComponentType,DataFile.DataComponentID, Component.Timestamp);
    			}
    			else
    				_Component = new TComponent(Component.idActivity, Component.idTComponent,Component.idComponent, Component.Timestamp);
    			_Component.GeoLocation = Component.GeoLocation;
    			_Component.TypedDataFiles = new TComponentTypedDataFiles(ParentActivity, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION,SpaceDefines.TYPEDDATAFILE_TYPE_Image);
    			//.
    			String Info = "";
    			if (_Component.TimestampIsValid()) {
    				StringBuilder SB = new StringBuilder();
    				SB.append(OleDate.Format("yyyy/MM/dd HH:mm:ss",OleDate.UTCToLocalTime(_Component.Timestamp)));
    				//.
    				if (_Component.idActivity != LastActivityID) {
    					if (Activities != null) {
            				TActivity Activity = Activities.GetItem(_Component.idActivity);
            				if (Activity != null) 
            					SB.append("/ "+Activity.Name);
    					}
    					//.
    					LastActivityID = _Component.idActivity; 
    				}
    				//.
    				Info = SB.toString();
    			}
    			//.
    			TComponentListItem Item = new TComponentListItem(UserAgent.Server, DataType,DataFormat,Name,Info, _Component);
    			Items[I] = Item;
    		}
    		lvActivitiesComponentListAdapter = new TComponentListAdapter(this, lvActivitiesComponentList, ProgressBar, Items);
    		lvActivitiesComponentList.setAdapter(lvActivitiesComponentListAdapter);
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
    	if (Updating != null) 
    		Updating.Cancel();
    }    
    
    public void ActivitiesComponents_LocateAnItemNearToTime(double Timestamp) {
    	if (ActivitiesComponents == null)
    		return; //. ->
    	//.
    	double 	MinDistance = Double.MAX_VALUE;
    	int 	MinDistanceIndex = -1;
    	int Cnt = ActivitiesComponents.Items.length;
    	for (int I = 0; I < Cnt; I++) {
    		double Distance = Math.abs(ActivitiesComponents.Items[I].Timestamp-Timestamp);
    		if (Distance < MinDistance) {
    			MinDistance = Distance;
    			MinDistanceIndex = I;
    		}
    	}
    	if (MinDistanceIndex >= 0) {
    		lvActivitiesComponentListAdapter.Items_SetSelectedIndex(MinDistanceIndex, false);
    		//.
    		lvActivitiesComponentList.setItemChecked(MinDistanceIndex, true);
    		lvActivitiesComponentList.setSelection(MinDistanceIndex);
    	}
    }
    
	@SuppressWarnings("unused")
	private AlertDialog ComponentTypedDataFiles_CreateSelectorPanel(TComponentTypedDataFiles pComponentTypedDataFiles) {
		final TComponentTypedDataFiles ComponentTypedDataFiles = pComponentTypedDataFiles;
		final CharSequence[] _items = new CharSequence[ComponentTypedDataFiles.Items.length];
		for (int I = 0; I < ComponentTypedDataFiles.Items.length; I++)
			_items[I] = ComponentTypedDataFiles.Items[I].DataName+" "+"/"+SpaceDefines.TYPEDDATAFILE_TYPE_String(ComponentTypedDataFiles.Items[I].DataType,ParentActivity)+"/";
		AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
		builder.setTitle(R.string.SFiles);
		builder.setNegativeButton(ParentActivity.getString(R.string.SCancel), null);
		builder.setSingleChoiceItems(_items, -1,
				new DialogInterface.OnClickListener() {

					private TComponentTypedDataFiles _ComponentTypedDataFiles = ComponentTypedDataFiles;

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						TComponentTypedDataFile ComponentTypedDataFile = _ComponentTypedDataFiles.Items[arg1];
						ComponentTypedDataFile_Process(ComponentTypedDataFile);
					}
				});
		AlertDialog alert = builder.create();
		return alert;
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
						TUserActivitiesComponentListComponent.this.MessageHandler.obtainMessage(OnCompletionMessage,ComponentTypedDataFile).sendToTarget();
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
											throw new Exception(ParentActivity.getString(R.string.SConnectionIsClosedUnexpectedly)); // =>
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
								TUserActivitiesComponentListComponent.this.MessageHandler
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
	                	try {
		                	progressDialog.dismiss(); 
	                	}
	                	catch (IllegalArgumentException IAE) {} //. TODO
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

	public void ComponentTypedDataFiles_Process(final TComponent OwnerComponent, final TComponentTypedDataFiles ComponentTypedDataFiles) throws IOException {
		int FC = ComponentTypedDataFiles.Count();
		final TComponentTypedDataFile ComponentTypedDataFile = ComponentTypedDataFiles.Items[0];
		//.
		switch (ComponentTypedDataFile.DataComponentType) {
		
		case SpaceDefines.idTPositioner:
			if (FC == 2) {
				ComponentTypedDataFile_Process(ComponentTypedDataFile);
				return; //. ->
			}
			break; // . >
			
		case SpaceDefines.idTMapFormatObject:
			TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity,ParentActivity.getString(R.string.SWaitAMoment)) {
				
				private TXYCoord VisualizationPosition = null;
				
				@Override
				public void Process() throws Exception {
					TComponentFunctionality CF = UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
					try {
						VisualizationPosition = CF.GetVisualizationPosition(); 
					}
					finally {
						CF.Release();
					}
				}
				
				@Override 
				public void DoOnCompleted() throws Exception {
					if (VisualizationPosition != null) {
						/*//. last version 
						Component.MoveReflectionWindow(VisualizationPosition);
						//.
				        setResult(RESULT_OK);
				        //.
						TUserActivityComponentListPanel.this.finish();*/
						Intent intent = new Intent(ParentActivity,TReflector.class);
						intent.putExtra("Reason", TReflectorComponent.REASON_SHOWLOCATION);
						intent.putExtra("LocationXY", VisualizationPosition.ToByteArray());
						if (OwnerComponent.TimestampIsValid())
							intent.putExtra("Timestamp", OwnerComponent.Timestamp+1.0/*day*/);
						ParentActivity.startActivity(intent);
					}
					else
						throw new Exception(ParentActivity.getString(R.string.SCouldNotGetPosition)); //. =>
				}
				@Override
				public void DoOnException(Exception E) {
					Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
				}
			};
			Processing.Start();
			return; //. ->
			
		default:
			break; // . >
		}
		//. 
		if (FC == 1)
			ComponentTypedDataFile_Process(ComponentTypedDataFile);
		else {
			Intent intent = new Intent(ParentActivity, TComponentTypedDataFilesPanel.class);
			if (Component != null)
				intent.putExtra("ComponentID", Component.ID);
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
	
	private void ShowComponentVisualizationPosition(final TActivity.TComponent Component, final double Timestamp) {
 		TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity,ParentActivity.getString(R.string.SWaitAMoment)) {
			
			private TXYCoord VisualizationPosition = null;
			@Override
			public void Process() throws Exception {
				TComponentFunctionality CF = UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(Component.idTComponent,Component.idComponent);
				try {
					VisualizationPosition = CF.GetVisualizationPosition(); 
				}
				finally {
					CF.Release();
				}
			}
			@Override 
			public void DoOnCompleted() throws Exception {
				if (VisualizationPosition != null) {
					/*//. last version 
					Component.MoveReflectionWindow(VisualizationPosition);
					//.
			        setResult(RESULT_OK);
			        //.
					TUserActivityComponentListPanel.this.finish();*/
					Intent intent = new Intent(ParentActivity,TReflector.class);
					intent.putExtra("Reason", TReflectorComponent.REASON_SHOWLOCATION);
					intent.putExtra("LocationXY", VisualizationPosition.ToByteArray());
					if (Timestamp > 0.0)
						intent.putExtra("Timestamp", Timestamp);
					ParentActivity.startActivity(intent);
				}
				else
					throw new Exception(ParentActivity.getString(R.string.SCouldNotGetPosition)); //. =>
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();
	}
	
	private void ShowComponentVisualizationPosition(TActivity.TComponent Component) {
		ShowComponentVisualizationPosition(Component, Component.Timestamp);
	}
	
	private void ShowComponentGeoLocation(TActivity.TComponent Component) {
		TGeoLocation GeoLocation = new TGeoLocation(Component.GeoLocation.Datum, Component.GeoLocation.Timestamp,Component.GeoLocation.Latitude,Component.GeoLocation.Longitude,Component.GeoLocation.Altitude);
		/*//. last version: TAsyncProcessing Processing = new TAsyncProcessing(this,getString(R.string.SWaitAMoment)) {
			
			private TXYCoord LocationXY = null;
			@Override
			public void Process() throws Exception {
				LocationXY = COmponent.ConvertGeoCoordinatesToXY(GeoLocation.Datum, GeoLocation.Latitude,GeoLocation.Longitude,GeoLocation.Altitude);
				//.
				Thread.sleep(100);
			}
			@Override 
			public void DoOnCompleted() throws Exception {
				Component.MoveReflectionWindow(Crd);
				//.
		        setResult(RESULT_OK);
		        //.
				TUserActivityComponentListPanel.this.finish();
			}
			@Override
			public void DoOnException(Exception E) {
				Toast.makeText(TUserActivityComponentListPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
			}
		};
		Processing.Start();*/
		try {
			Intent intent = new Intent(ParentActivity,TReflector.class);
			intent.putExtra("Reason", TReflectorComponent.REASON_SHOWGEOLOCATION1);
			intent.putExtra("GeoLocation", GeoLocation.ToByteArray());
			ParentActivity.startActivity(intent);
		} catch (Exception E) {
			Toast.makeText(ParentActivity, E.getMessage(),Toast.LENGTH_LONG).show();
		}
	}
	
	private void SendComponentURLToUser() {
    	Intent intent = new Intent(ParentActivity, TUserListPanel.class);
		intent.putExtra("ComponentID", Component.ID);
    	intent.putExtra("Mode",TUserListComponent.MODE_FORURL);    	
    	ParentActivity.startActivityForResult(intent, REQUEST_SELECT_USER_FORURL);		
	}
	
	private void DoSendComponentURLToUser(long UserID) throws Exception {
		String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
		File UF = new File(URLFN);
		if (UF.exists()) {
			com.geoscope.GeoEye.Space.URL.TURL URL = com.geoscope.GeoEye.Space.URL.TURL.GetURLFromXmlFile(URLFN, UserAgent.User());
			//.
			TURLFolderListComponent.TURLsToUserSendingItem[] Items = new TURLFolderListComponent.TURLsToUserSendingItem[1];
			Items[0] = new TURLFolderListComponent.TURLsToUserSendingItem(URL.Name, URL.XMLDocumentData);  
			new TURLFolderListComponent.TURLsToUserSending(ParentActivity, Component.User, UserID, Items);
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
