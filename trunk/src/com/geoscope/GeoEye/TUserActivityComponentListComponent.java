package com.geoscope.GeoEye;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Locale;

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
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Date.OleDate;
import com.geoscope.Classes.Data.Types.Image.TDiskImageCache;
import com.geoscope.Classes.Data.Types.Image.TImageViewerPanel;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.Classes.MultiThreading.TProgressor;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.TGeoLocation;
import com.geoscope.GeoEye.Space.Defines.TLocation;
import com.geoscope.GeoEye.Space.Defines.TXYCoord;
import com.geoscope.GeoEye.Space.Functionality.TTypeFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentFunctionality;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFile;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFiles;
import com.geoscope.GeoEye.Space.Functionality.ComponentFunctionality.TComponentTypedDataFilesPanel;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServer;
import com.geoscope.GeoEye.Space.Server.TGeoScopeServerInfo;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser.TUserDescriptor.TActivity.TComponent;
import com.geoscope.GeoEye.Space.TypesSystem.TComponentStreamServer;
import com.geoscope.GeoEye.Space.TypesSystem.TTypesSystem;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoEye.Space.TypesSystem.Positioner.TPositionerFunctionality;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.THintManager;

@SuppressLint("HandlerLeak")
public class TUserActivityComponentListComponent extends TUIComponent {

	public static final int		ItemImageSize = 512;
	public static final String 	ItemImageDataParams = "2;"+Integer.toString(ItemImageSize)+";"+"50"/*50% quality*/;
	//.
	public static final int		ImageDrawings_MaxDataSize = 1024*100; //. Kb
	public static final String 	ImageDrawings_ItemImageDataParams = "0;"+Integer.toString(ImageDrawings_MaxDataSize);
	
	public static final int LIST_ROW_SIZE_SMALL_ID 	= 1;
	public static final int LIST_ROW_SIZE_NORMAL_ID = 2;
	public static final int LIST_ROW_SIZE_BIG_ID 	= 3;
	
	private static final int 	MESSAGE_TYPEDDATAFILE_LOADED = 1;
	
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

		private static final String 		ImageCache_Name = "UserActivityComponentImages";
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
				ImageLoaderCount++;
				//.
				if (ImageLoaderCount > 0) 
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
				ImageLoaderCount--;
				if (ImageLoaderCount == 0) 
					ProgressHandler.DoOnFinish();
			}

			private static final int LOADIMAGE_DATAKIND_BITMAP 		= 0;
			private static final int LOADIMAGE_DATAKIND_DRAWINGS 	= 1;
			
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
				int DataKind = LOADIMAGE_DATAKIND_BITMAP;
				switch (Item.DataType) {

				case SpaceDefines.TYPEDDATAFILE_TYPE_ImageName:
					if ((Item.DataFormat != null) && Item.DataFormat.toUpperCase(Locale.US).equals(TDrawingDefines.DataFormat)) 
						DataKind = LOADIMAGE_DATAKIND_DRAWINGS;
					break; //. >
				}
				//.
				switch (DataKind) {
					
				case LOADIMAGE_DATAKIND_DRAWINGS:
					Item.Component.TypedDataFiles.PrepareForComponent(Item.Component.idTComponent,Item.Component.idComponent, ImageDrawings_ItemImageDataParams, (Item.Component.idTComponent == SpaceDefines.idTCoComponent), Item.Server);
					break; //. >
					
				default:
					Item.Component.TypedDataFiles.PrepareForComponent(Item.Component.idTComponent,Item.Component.idComponent, ItemImageDataParams, (Item.Component.idTComponent == SpaceDefines.idTCoComponent), Item.Server);
					break; //. >
				}
				//.
				if (Item.Component.TypedDataFiles.Items.length > 0) {
					byte[] Data = Item.Component.TypedDataFiles.Items[0].Data;
					if (Data != null) 
						switch (DataKind) {
						
						case LOADIMAGE_DATAKIND_DRAWINGS:
							TDrawings Drawings = new TDrawings();
							Drawings.LoadFromByteArray(Data,0);
							Result = Drawings.ToBitmap(ItemImageSize);
							break; //. >
							
						default:
							Result = BitmapFactory.decodeByteArray(Data, 0,Data.length); 
							break; //. >
						}
				}
				//.
				if (Result != null) 
					ImageCache.put(Item.Component.GetKey(), Result);
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
		private TUserActivityComponentListComponent Panel;
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
		private int ImageLoaderCount = 0;
		//.
		private TDiskImageCache ImageCache;
		//.
		public OnClickListener ImageClickListener = new OnClickListener() {
			
			@Override
	        public void onClick(View v) {
	            int position = MyListView.getPositionForView((View)v.getParent());
	            //.
				TComponentListItem Item = (TComponentListItem)Items[position];
				if (Item.BMP_flLoaded && (!Item.BMP_flNull)) {
		        	AlertDialog alert = new AlertDialog.Builder(context).create();
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
	        
		public TComponentListAdapter(TUserActivityComponentListComponent pPanel, ListView pMyListView, View pProgressBar, TComponentListItem[] pItems) {
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
				new TImageLoadTask(Item,holder).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
			else {
				if (!Item.BMP_flNull)
					if (flListIsScrolling)
						new TImageRestoreTask(Item,holder).Start();
					else {
						BMP = ImageCache.getBitmap(Item.Component.GetKey());
						if (BMP == null) {
							Item.BMP_flLoaded = false;
							//.
							new TImageLoadTask(Item,holder).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
						}
					}
			}
			//.
			if (BMP != null) {
				holder.ivImage.setImageBitmap(BMP);
				holder.ivImage.setOnClickListener(ImageClickListener);
			}
			else {
				boolean flImageAssigned = false;
				switch (Item.Component.idTComponent) {
				
				case SpaceDefines.idTPositioner:
					holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder_component_positioner));
					flImageAssigned = true;
					break; //. >

				case SpaceDefines.idTMapFormatObject:
					holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder_component_mapformatobject));
					flImageAssigned = true;
					break; //. >
				}
				if (!flImageAssigned) {
					switch (Item.DataType) {
					
					case SpaceDefines.TYPEDDATAFILE_TYPE_DocumentName:
						holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder_text));
						break; //. >
						
					case SpaceDefines.TYPEDDATAFILE_TYPE_ImageName:
						if ((Item.DataFormat != null) && Item.DataFormat.toUpperCase(Locale.US).equals(TDrawingDefines.DataFormat))
							holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder_image_drawing));
						else 
							holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder_image));
						break; //. >
						
					case SpaceDefines.TYPEDDATAFILE_TYPE_AudioName:
						holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder_audio));
						break; //. >
						
					case SpaceDefines.TYPEDDATAFILE_TYPE_VideoName:
						holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder_video));
						break; //. >
						
					default:
						holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder));
						break; //. >
					}
				}
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
	private Activity ParentActivity;
	private LinearLayout ParentLayout;
	//.
	private long	UserID = 0;	
	private long	ActivityID = 0;	
	//.
	private int ListRowSizeID;
	//.
	private TReflectorComponent Component;
	//.
	private TOnListItemClickHandler OnListItemClickHandler;
	//.
    private TActivity.TComponents 	ActivityComponents = null;
    //.
    private TComponentListAdapter	lvActivityComponentListAdapter = null;
	private ListView 				lvActivityComponentList;
	//.
	private View ProgressBar;
	//.
	private TUpdating	Updating = null;
	//.
	private TComponentTypedDataFileLoading ComponentTypedDataFileLoading = null;
	
	public TUserActivityComponentListComponent(Activity pParentActivity, LinearLayout pParentLayout, long pUserID, long pActivityID, int pListRowSizeID, TReflectorComponent pComponent, TOnListItemClickHandler pOnListItemClickHandler) {
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
		//.
		UserID = pUserID;
		ActivityID = pActivityID;
		//.
		ListRowSizeID = pListRowSizeID;
        //.
		Component = pComponent;
		//.
		OnListItemClickHandler = pOnListItemClickHandler;
        //.
        lvActivityComponentList = (ListView)ParentLayout.findViewById(R.id.lvActivityComponentList);
        lvActivityComponentList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvActivityComponentList.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (ActivityComponents == null)
					return; //. ->
				//.
				try {
					if (OnListItemClickHandler != null)
						OnListItemClickHandler.DoOnListItemClick(ActivityComponents.Items[arg2]);
					else
						try {
							if (ActivityComponents.Items[arg2].TypedDataFiles.Count() == 0)
								return; //. ->
							if (ActivityComponents.Items[arg2].TypedDataFiles.Count() > 1) {
								Intent intent = new Intent(ParentActivity, TComponentTypedDataFilesPanel.class);
								intent.putExtra("ComponentID", Component.ID);
								intent.putExtra("DataFiles", ActivityComponents.Items[arg2].TypedDataFiles.ToByteArrayV0());
								//.
								ParentActivity.startActivity(intent);
							}
							else {
								TComponentTypedDataFile ComponentTypedDataFile = ActivityComponents.Items[arg2].TypedDataFiles.Items[0];
								ComponentTypedDataFile_Process(ComponentTypedDataFile);
							}
						}
						catch (Exception E) {
			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
						}
				}
				finally {
					lvActivityComponentListAdapter.Items_SetSelectedIndex(arg2, true);
				}
        	}              
        });         
        lvActivityComponentList.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (ActivityComponents == null)
					return false; //. ->
            	//.
				try {
					final TComponent _Component = ActivityComponents.Items[arg2];
					//.
		    		final CharSequence[] _items;
		    		int SelectedIdx = -1;
		    		_items = new CharSequence[3];
		    		_items[0] = ParentActivity.getString(R.string.SOpen); 
		    		_items[1] = ParentActivity.getString(R.string.SShowGeoLocation); 
		    		_items[2] = ParentActivity.getString(R.string.SRemove); 
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
		    							if ((Component != null) && (_Component.TypedDataFiles.Count() > 1)) {
		    								Intent intent = new Intent(ParentActivity, TComponentTypedDataFilesPanel.class);
		    								intent.putExtra("ComponentID", Component.ID);
		    								intent.putExtra("DataFiles", _Component.TypedDataFiles.ToByteArrayV0());
		    								//.
		    								ParentActivity.startActivity(intent);
		    							}
		    							else {
		    								if (_Component.TypedDataFiles.Count() > 0) {
			    								TComponentTypedDataFile ComponentTypedDataFile = _Component.TypedDataFiles.Items[0];
			    								ComponentTypedDataFile_Process(ComponentTypedDataFile);
		    								}
		    							}
			    						//.
			        		    		arg0.dismiss();
		    						}
		    						catch (Exception E) {
		    			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		    						}
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 1: //. show location
		    						if (_Component.GeoLocation != null)
		    							ShowComponentGeoLocation(_Component);
		    						else
		    							ShowComponentVisualizationPosition(_Component);
		    						//.
		        		    		arg0.dismiss();
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 2: //. remove component
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
		    		    		    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
		    		    		    				if (UserAgent == null)
		    		    		    					throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
		    		    		    				//.
		    		    							TTypeFunctionality TF = UserAgent.User().Space.TypesSystem.TTypeFunctionality_Create(UserAgent.User().Server, _Component.idTComponent);
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
					lvActivityComponentListAdapter.Items_SetSelectedIndex(arg2, true);
				}
			}
		}); 
        lvActivityComponentList.setOnScrollListener(new OnScrollListener() {
        	
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            	if (lvActivityComponentListAdapter != null)
            		lvActivityComponentListAdapter.flListIsScrolling = (scrollState != OnScrollListener.SCROLL_STATE_IDLE); 
            }
        });
        //.
        ProgressBar = ParentLayout.findViewById(R.id.pbProgress);
        //.
        final int HintID = THintManager.HINT__Long_click_to_show_an_item_Location;
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

	public void Destroy() {
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
        StartUpdating();
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
        private TActivity.TComponents ActivityComponents = null;
    	
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
	    				TUserAgent UserAgent = TUserAgent.GetUserAgent();
	    				if (UserAgent == null)
	    					throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
	    				//.
	    				ActivityComponents = UserAgent.Server.User.GetUserActivityComponentList(UserID, ActivityID);
	    				if (ActivityComponents != null) {
			            	FilterActivityComponents(ActivityComponents);
			            	//. supplying the components with its TypesDataFiles
			            	try {
			    				for (int I = 0; I < ActivityComponents.Items.length; I++) {
			    					Canceller.Check();
			    					//.
			    					try {
				    					TComponentTypedDataFiles TypedDataFiles = new TComponentTypedDataFiles(ParentActivity, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION, SpaceDefines.TYPEDDATAFILE_TYPE_AllName);
				    					TypedDataFiles.PrepareForComponent(ActivityComponents.Items[I].idTComponent,ActivityComponents.Items[I].idComponent, true, UserAgent.Server);
				    					//.
				    					ActivityComponents.Items[I].TypedDataFiles = TypedDataFiles;
				    					//.
				    					String S = ActivityComponents.Items[I].GetName(); 
			    		    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_MESSAGE,S).sendToTarget();
				    					
			    					}
			    					catch (Exception E) {
			    		    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
			    					}
			    				}
			            	}
			            	finally {
				            	OptimizeActivityComponents(ActivityComponents);
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
		            	TUserActivityComponentListComponent.this.ActivityComponents = ActivityComponents;
	           		 	//.
	           		 	TUserActivityComponentListComponent.this.Update();
	           		 	//.
	           		 	if ((msg.what == MESSAGE_COMPLETEDBYCANCEL) && ((TUserActivityComponentListComponent.this.ActivityComponents == null) || (TUserActivityComponentListComponent.this.ActivityComponents.Items.length == 0)))
	           		 		ParentActivity.finish();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TUserActivityComponentListComponent.this.Updating = null;
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
		                if ((!ParentActivity.isFinishing()) && progressDialog.isShowing()) 
		                	progressDialog.dismiss(); 
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
    	if (ActivityComponents == null) {
    		lvActivityComponentList.setAdapter(null);
    		return; //. ->
    	}
		TUserAgent UserAgent = TUserAgent.GetUserAgent();
		if (UserAgent == null)
			throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
		//.
		TComponentListItem[] Items = new TComponentListItem[ActivityComponents.Items.length];
		for (int I = 0; I < ActivityComponents.Items.length; I++) {
			TActivity.TComponent Component = ActivityComponents.Items[I];
			//.
			int 	DataType = SpaceDefines.TYPEDDATAFILE_TYPE_All;
			String 	DataFormat = null;
			String Name = Component.GetName().split("\n")[0];
			if (Component.TypedDataFiles.Items.length > 0) {
				DataType = Component.TypedDataFiles.Items[0].DataType;
				DataFormat = Component.TypedDataFiles.Items[0].DataFormat;
				switch (Component.TypedDataFiles.Items[0].DataComponentType) {
				
				case SpaceDefines.idTPositioner:
					Name = Name+" "+"/"+SpaceDefines.ComponentType_GetName(Component.TypedDataFiles.Items[0].DataComponentType,ParentActivity)+"/";
					break; //. >
					
				case SpaceDefines.idTMapFormatObject:
					break; //. >
					
				default:
					Name = Name+" "+"/"+SpaceDefines.TYPEDDATAFILE_TYPE_String(DataType,ParentActivity)+"/";
					break; //. >
				}
			}
			TComponent _Component = new TComponent(Component.idActivity, Component.idTComponent,Component.idComponent, Component.Timestamp);
			_Component.GeoLocation = Component.GeoLocation;
			_Component.TypedDataFiles = new TComponentTypedDataFiles(ParentActivity, SpaceDefines.TYPEDDATAFILE_MODEL_HUMANREADABLECOLLECTION,SpaceDefines.TYPEDDATAFILE_TYPE_Image);
			//.
			String Info = "";
			if (_Component.TimestampIsValid())
				Info = OleDate.Format("yyyy/MM/dd HH:mm:ss",OleDate.UTCToLocalTime(_Component.Timestamp)); 
			//.
			TComponentListItem Item = new TComponentListItem(UserAgent.Server, DataType,DataFormat,Name,Info, _Component);
			Items[I] = Item;
		}
		lvActivityComponentListAdapter = new TComponentListAdapter(this, lvActivityComponentList, ProgressBar, Items);
		lvActivityComponentList.setAdapter(lvActivityComponentListAdapter);
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,false);
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
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
				//.
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
						ComponentTypedDataFile.PrepareFullFromFile(CFN);
						//.
						TUserActivityComponentListComponent.this.MessageHandler.obtainMessage(OnCompletionMessage,ComponentTypedDataFile).sendToTarget();
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
								if (RetSize == 0) {
									ComponentTypedDataFile.Data = null;
									return; // . ->
								}
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
								// .
								TUserActivityComponentListComponent.this.MessageHandler
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
		                	progressDialog.dismiss(); 
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

	public void ComponentTypedDataFile_Process(TComponentTypedDataFile ComponentTypedDataFile) {
		if (ComponentTypedDataFile.IsLoaded()) {
			ComponentTypedDataFile_Open(ComponentTypedDataFile);
		} else {
			if (ComponentTypedDataFileLoading != null)
				ComponentTypedDataFileLoading.Cancel();
			ComponentTypedDataFileLoading = new TComponentTypedDataFileLoading(ComponentTypedDataFile, MESSAGE_TYPEDDATAFILE_LOADED);
		}
	}
	
	public void ComponentTypedDataFile_Open(TComponentTypedDataFile ComponentTypedDataFile) {
		try {
			if (ComponentTypedDataFile.FileIsEmpty())
				throw new Exception(ParentActivity.getString(R.string.SThereIsNoDataYet)); //. =>
			//.
			TUserAgent UserAgent = TUserAgent.GetUserAgent();
			if (UserAgent == null)
				throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
			//.
			Intent intent = null;
			switch (ComponentTypedDataFile.DataType) {

			case SpaceDefines.TYPEDDATAFILE_TYPE_Document:
				try {
					File F = ComponentTypedDataFile.GetFile();
					byte[] Data = new byte[(int)F.length()];
					FileInputStream FIS = new FileInputStream(F);
					try {
						FIS.read(Data);
					}
					finally {
						FIS.close();
					}
					//.
					if (ComponentTypedDataFile.DataFormat.toUpperCase(Locale.ENGLISH).equals(".TXT")) {
						String Text = new String(Data,"windows-1251");
						byte[] TextData = Text.getBytes("utf-16");
						// .
						File TempFile = ComponentTypedDataFile.GetTempFile();
						FileOutputStream fos = new FileOutputStream(TempFile);
						try {
							fos.write(TextData, 0, TextData.length);
						} finally {
							fos.close();
						}
						// . open appropriate extent
						intent = new Intent();
						intent.setDataAndType(Uri.fromFile(TempFile), "text/plain");
					}
					else
						if (ComponentTypedDataFile.DataFormat.toUpperCase(Locale.ENGLISH).equals(".XML")) {
							TComponentFunctionality CF = UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(UserAgent.Server, ComponentTypedDataFile.DataComponentType,ComponentTypedDataFile.DataComponentID);
							if (CF != null)
								try {
									int Version = CF.ParseFromXMLDocument(Data);
									if (Version > 0) 
										switch (CF.TypeFunctionality.idType) {
										
										case SpaceDefines.idTPositioner:
											TPositionerFunctionality PF = (TPositionerFunctionality)CF;
											//.
											TLocation P = new TLocation(PF._Name);
											P.RW.Assign(Component.ReflectionWindow.GetWindow());
											P.RW.X0 = PF._X0; P.RW.Y0 = PF._Y0;
											P.RW.X1 = PF._X1; P.RW.Y1 = PF._Y1;
											P.RW.X2 = PF._X2; P.RW.Y2 = PF._Y2;
											P.RW.X3 = PF._X3; P.RW.Y3 = PF._Y3;
											P.RW.BeginTimestamp = PF._Timestamp; P.RW.EndTimestamp = PF._Timestamp;
											P.RW.Normalize();
											/*//. last version: Reflector.SetReflectionWindowByLocation(P);
											//.
									        setResult(RESULT_OK);
									        //.
											finish();*/
											intent = new Intent(ParentActivity,TReflector.class);
											intent.putExtra("Reason", TReflectorComponent.REASON_SHOWLOCATIONWINDOW);
											intent.putExtra("LocationWindow", P.ToByteArray());
											ParentActivity.startActivity(intent);
											return; // . ->

										default:
											TComponentFunctionality.TPropsPanel PropsPanel = CF.TPropsPanel_Create(ParentActivity);
											if (PropsPanel != null)
												ParentActivity.startActivity(PropsPanel.PanelActivity);
											return; // . ->
										}
								}
							finally {
								CF.Release();
							}
						}
				} catch (Exception E) {
					Toast.makeText(
							ParentActivity,
							ParentActivity.getString(R.string.SErrorOfPreparingDataFile)
									+ ComponentTypedDataFile.FileName(),
							Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			case SpaceDefines.TYPEDDATAFILE_TYPE_Image:
				try {
					if (ComponentTypedDataFile.DataFormat.toLowerCase(Locale.ENGLISH).equals("."+TDrawingDefines.FileExtension)) {
			    		intent = new Intent(ParentActivity, TDrawingEditor.class);
			  		    intent.putExtra("FileName", ComponentTypedDataFile.GetFile().getAbsolutePath()); 
			  		    intent.putExtra("ReadOnly", true); 
			  		  ParentActivity.startActivity(intent);
			  		    //.
						return; // . ->
					}
					else {
			    		intent = new Intent(ParentActivity, TImageViewerPanel.class);
			  		    intent.putExtra("FileName", ComponentTypedDataFile.GetFile().getAbsolutePath()); 
			  		    ParentActivity.startActivity(intent);
			  		    //.
						return; // . ->
					}
				} catch (Exception E) {
					Toast.makeText(
							ParentActivity,
							ParentActivity.getString(R.string.SErrorOfPreparingDataFile)
									+ ComponentTypedDataFile.FileName(),
							Toast.LENGTH_SHORT).show();
					return; // . ->
				}

			case SpaceDefines.TYPEDDATAFILE_TYPE_Audio:
				try {
					// . open appropriate extent
					intent = new Intent();
					intent.setDataAndType(
							Uri.fromFile(ComponentTypedDataFile.GetFile()),
							"audio/*");
				} catch (Exception E) {
					Toast.makeText(
							ParentActivity,
							ParentActivity.getString(R.string.SErrorOfPreparingDataFile)
									+ ComponentTypedDataFile.FileName(),
							Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			case SpaceDefines.TYPEDDATAFILE_TYPE_Video:
				try {
					// . open appropriate extent
					intent = new Intent();
					intent.setDataAndType(
							Uri.fromFile(ComponentTypedDataFile.GetFile()),
							"video/*");
				} catch (Exception E) {
					Toast.makeText(
							ParentActivity,
							ParentActivity.getString(R.string.SErrorOfPreparingDataFile)
									+ ComponentTypedDataFile.FileName(),
							Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			default:
				Toast.makeText(ParentActivity, R.string.SUnknownDataFileFormat,
						Toast.LENGTH_LONG).show();
				return; // . ->
			}
			if (intent != null) {
				intent.setAction(android.content.Intent.ACTION_VIEW);
				ParentActivity.startActivity(intent);
			}
		} catch (Exception E) {
			Toast.makeText(ParentActivity, E.getMessage(),Toast.LENGTH_LONG).show();
		}
	}

	private void ShowComponentVisualizationPosition(TActivity.TComponent Component) {
 		final TActivity.TComponent _Component = Component;
		//.
		TAsyncProcessing Processing = new TAsyncProcessing(ParentActivity,ParentActivity.getString(R.string.SWaitAMoment)) {
			
			private TXYCoord VisualizationPosition = null;
			@Override
			public void Process() throws Exception {
				TUserAgent UserAgent = TUserAgent.GetUserAgent();
				if (UserAgent == null)
					throw new Exception(ParentActivity.getString(R.string.SUserAgentIsNotInitialized)); //. =>
				//.
				TComponentFunctionality CF = UserAgent.User().Space.TypesSystem.TComponentFunctionality_Create(UserAgent.Server, _Component.idTComponent,_Component.idComponent);
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
    					ComponentTypedDataFile_Open(ComponentTypedDataFile);
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
