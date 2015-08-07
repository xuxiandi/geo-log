package com.geoscope.Classes.IO.File.FileSelector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Image.TDiskImageCache;
import com.geoscope.Classes.Data.Types.Image.Drawing.TDrawings;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.TFileSystem;
import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoLog.Application.TGeoLogApplication;

@SuppressLint("HandlerLeak")
public class TFileSystemPreviewFileSelectorComponent extends TUIComponent {
	
	public static class TFileFilter {
		
		public String[] Items;
		
		public TFileFilter(String S) {
			FromString(S);
		}
		
		public void FromString(String S) {
			Items = S.split(",");
		}
		
		public String ToString() {
			int Cnt = Items.length;
			StringBuilder SB = new StringBuilder();
			for (int I = 0; I < Cnt; I++) {
				if (I != 0)
					SB.append(",");
				SB.append(Items[I]);
			}
			return SB.toString();
		}
		
		public boolean IsAccepted(String FileFormat) {
			int Cnt = Items.length;
			for (int I = 0; I < Cnt; I++) 
				if (Items[I].equals(FileFormat))
					return true; //. ->
			return false;
		}
	}
	
	public static class TFolderFileList {
	
		public static class TItem {
			
			public static final int FILETYPE_BACKFOLDER = -2;
			public static final int FILETYPE_FOLDER 	= -1;
			public static final int FILETYPE_UNKNOWN 	= 0;
			public static final int FILETYPE_DOCUMENT 	= 1;
			public static final int FILETYPE_IMAGE 		= 2;
			public static final int FILETYPE_AUDIO 		= 3;
			public static final int FILETYPE_VIDEO 		= 4;
			
			public static final int MaxImageBitmapSize = 128;
			
			public String ID;
			//.
			public String Name;
			//.
			public String 	FileName = "";
			public int		FileType;
			public String 	FileFormat = "";
			//.
			public boolean 	flFolder;
			
			public TItem(String pFileName, String pFileFormat, boolean pflFolder) {
				FileName = pFileName;
				FileFormat = pFileFormat;
				//.
				flFolder = pflFolder;
				//.
				if (FileName.length() > 0) {
					ID = UUID.nameUUIDFromBytes(FileName.getBytes()).toString();
					Name = (new File(FileName)).getName();
				}
				else {
					ID = "";
					Name = "..";
				}
				//.
				FileType = FileType_Get();
			}
			
			public boolean FileType_IsDocument() {
				return (FileFormat.equals(".TXT") || FileFormat.equals(".XML") || FileFormat.equals(".DOC"));
			}
			
			public boolean FileType_IsImage() {
				return (FileFormat.equals(".BMP") || FileFormat.equals(".PNG") || FileFormat.equals(".JPG") || FileFormat.equals(".JPEG") || FileFormat.equals(TDrawingDefines.DataFormat));
			}
			
			public boolean FileType_IsAudio() {
				return (FileFormat.equals(".WAV") || FileFormat.equals(".MP3"));
			}
			
			public boolean FileType_IsVideo() {
				return (FileFormat.equals(".AVI") || FileFormat.equals(".WMV") || FileFormat.equals(".MPG") || FileFormat.equals(".MPEG") || FileFormat.equals(".3GP") || FileFormat.equals(".MP4"));
			}
			
			public int FileType_Get() {
				if (flFolder)
					if (FileName.length() > 0)
						return FILETYPE_FOLDER; //. ->
					else
						return FILETYPE_BACKFOLDER; //. ->
				//.
				if (FileType_IsDocument())
					return FILETYPE_DOCUMENT; //. ->
				else
					if (FileType_IsImage())
						return FILETYPE_IMAGE; //. ->
					else
						if (FileType_IsAudio())
							return FILETYPE_AUDIO; //. ->
						else
							if (FileType_IsVideo())
								return FILETYPE_VIDEO; //. ->
							else
								return FILETYPE_UNKNOWN; //. ->
			}
			
			public int GetThumbnailImageResId() {
				switch (FileType) {
				
				case FILETYPE_BACKFOLDER: 
					return R.drawable.back; //. ->
					
				case FILETYPE_FOLDER: 
					return R.drawable.user_activity_component_list_placeholder_folder; //. ->
					
				case FILETYPE_UNKNOWN:
					return R.drawable.user_activity_component_list_placeholder_file; //. ->
					
				case FILETYPE_DOCUMENT:
					return R.drawable.user_activity_component_list_placeholder_text; //. ->
					
				case FILETYPE_IMAGE:
					return R.drawable.user_activity_component_list_placeholder_image; //. ->
					
				case FILETYPE_AUDIO:
					return R.drawable.user_activity_component_list_placeholder_audio; //. ->
					
				case FILETYPE_VIDEO:
					return R.drawable.user_activity_component_list_placeholder_video; //. ->
					
				default:
					return R.drawable.user_activity_component_list_placeholder_file; //. ->
				}
			}

			public Bitmap GetThumbnailImage() throws Exception {
				Bitmap Result = null;
				switch (FileType) {
				
				case FILETYPE_IMAGE:
					if (FileFormat.equals(TDrawingDefines.DataFormat)) {
						File F = new File(FileName);
						FileInputStream FS = new FileInputStream(F);
						try
						{
							byte[] FileData = new byte[(int)F.length()];
							FS.read(FileData);
							//.
							TDrawings Drawings = new TDrawings();
							Drawings.LoadFromByteArray(FileData,0);
							//.
							return Drawings.ToBitmap(MaxImageBitmapSize); //. ->
						}
						finally {
							FS.close();
						}
					}
					else {
						File F = new File(FileName);
						FileInputStream FS = new FileInputStream(F);
						try
						{
							BitmapFactory.Options options = new BitmapFactory.Options();
							options.inDither = false;
							options.inPurgeable = true;
							options.inInputShareable = true;
							options.inTempStorage = new byte[1024*256]; 							
							Rect rect = new Rect();
		    				Bitmap bitmap = BitmapFactory.decodeFileDescriptor(FS.getFD(), rect, options);
		    				try {
		    					int ImageMaxSize = options.outWidth;
		    					if (options.outHeight > ImageMaxSize)
		    						ImageMaxSize = options.outHeight;
		    					float MaxSize = MaxImageBitmapSize;
		    					float Scale = MaxSize/ImageMaxSize; 
		    					Matrix matrix = new Matrix();     
		    					matrix.postScale(Scale,Scale);
		    					//.
		    					Result = Bitmap.createBitmap(bitmap, 0,0,options.outWidth,options.outHeight, matrix, true);
		    					return Result; //. ->
		    				}
		    				finally {
		    					if (Result != bitmap)
		    						bitmap.recycle();
		    				}
						}
						finally {
							FS.close();
						}
					}
					
				default:
					return null; //. ->
				}
			}
		}
	
		private String Folder;
		private TFileFilter FileFilter;
		//.
		public ArrayList<TItem> Items = new ArrayList<TItem>();
		
		public TFolderFileList(String pFolder, TFileFilter pFileFilter) throws Exception {
			Folder = pFolder;
			FileFilter = pFileFilter;
			//.
			Load();
		}
		
		private void Load() throws Exception {
			TItem Item = new TItem("","",true); //. up level folder place-holder
			Items.add(Item);
			//.
			File F = new File(Folder);
			File[] Files = F.listFiles();
			if (Files != null) {
		        List<File> FileList = Arrays.asList(Files);
		        Collections.sort(FileList, new Comparator<File>() {
		            @Override
		            public int compare(File file, File file2) {
		                if (file.isDirectory() && file2.isFile())
		                    return -1;
		                else if (file.isFile() && file2.isDirectory())
		                    return 1;
		                else
		                    return file.getPath().compareTo(file2.getPath());
		            }
		        });
				int Cnt = FileList.size();
				for (int I = 0; I < Cnt; I++) {
					File FI = FileList.get(I);
					//.
					String FileName = FI.getAbsolutePath();
					boolean flFolder = FI.isDirectory();
					String FileFormat;
					if (!flFolder)
						FileFormat = TFileSystem.FileName_GetDottedExtension(FileName).toUpperCase(Locale.ENGLISH);
					else
						FileFormat = "";
					//.
					if (flFolder || ((FileFilter == null) || FileFilter.IsAccepted(FileFormat))) {
						Item = new TItem(FileName,FileFormat, flFolder);
						Items.add(Item);
					}
				}
			}
		}
		
		public int Count() {
			return Items.size();
		}
		
		public TItem GetItem(String FileName) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TItem Item = Items.get(I); 
				if (Item.FileName.equals(FileName)) 
					return Item; //. ->
			}
			return null;
		}
		
		public int GetItemIndex(String FileName) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++)
				if (Items.get(I).FileName.equals(FileName)) 
					return I; //. ->
			return -1;
		}
	}
	
	public static final int LIST_ROW_SIZE_SMALL_ID 	= 1;
	public static final int LIST_ROW_SIZE_NORMAL_ID = 2;
	public static final int LIST_ROW_SIZE_BIG_ID 	= 3;
	
	private static class TListItem {
		
		public TFolderFileList.TItem Item;
		//.
		public boolean BMP_flLoaded = false;
		public boolean BMP_flNull = false;
		
		public TListItem(TFolderFileList.TItem pItem) {
			Item = pItem;
		}
	}
	
	public static class TListAdapter extends BaseAdapter {

		private static final String 		ImageCache_Name = "PreviewFileSelectorImages";
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

			private Bitmap LoadImage() throws Exception {
				if (Item.BMP_flLoaded) {
					if (!Item.BMP_flNull) 
						return ImageCache.getBitmap(Item.Item.ID); //. ->
					else 
						return null; //. ->
				}
				//.
				Bitmap Result = Item.Item.GetThumbnailImage();
				//.
				if (Result != null) 
					ImageCache.put(Item.Item.ID, Result);
				else
					Item.BMP_flNull = true;
				Item.BMP_flLoaded = true;
				//.
				return Result;
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
				return ImageCache.getBitmap(Item.Item.ID);
			}
		}
		
		private Context context;
		//.
		private TFileSystemPreviewFileSelectorComponent Panel;
		//.
		private ListView MyListView;
		//.
		private View 				ProgressBar;
		private TProgressHandler 	ProgressHandler;
		//.
		private TListItem[] 			Items;
		private int						Items_SelectedIndex = -1;
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
	            final int position = MyListView.getPositionForView((View)v.getParent());
	            //.
				TListItem Item = (TListItem)Items[position];
				if (Item.BMP_flLoaded && (!Item.BMP_flNull)) {
		        	final AlertDialog alert = new AlertDialog.Builder(context).create();
		        	alert.setCancelable(true);
		        	alert.setCanceledOnTouchOutside(true);
		        	LayoutInflater factory = LayoutInflater.from(context);
		        	View layout = factory.inflate(R.layout.image_preview_dialog_layout, null);
		        	ImageView IV = (ImageView)layout.findViewById(R.id.ivPreview);
		        	IV.setImageDrawable(((ImageView)v).getDrawable());
		        	IV.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							try {
								Panel.FolderList_PreviewItem(position);
								//.
								alert.dismiss();
							}
							catch (Exception E) {
				                Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
							}
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
	        
		public TListAdapter(TFileSystemPreviewFileSelectorComponent pPanel, ListView pMyListView, View pProgressBar, TListItem[] pItems) {
			context = pPanel.context;
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
				int LayoutID = R.layout.previewfileselector_row_layout;
				switch (Panel.ListRowSizeID) {
				
				case LIST_ROW_SIZE_SMALL_ID:
					LayoutID = R.layout.previewfileselector_row_small_layout;
					break; //. >
					
				case LIST_ROW_SIZE_NORMAL_ID:
					LayoutID = R.layout.previewfileselector_row_layout;
					break; //. >
					
				case LIST_ROW_SIZE_BIG_ID:
					LayoutID = R.layout.previewfileselector_row_big_layout;
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
			holder.lbName.setText(Item.Item.Name);
			//.
			holder.lbInfo.setText("");
			//.
			Bitmap BMP = null;
			//.
			if (!Item.BMP_flLoaded)
				new TImageLoadTask(Item,holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			else {
				if (!Item.BMP_flNull)
					if (flListIsScrolling)
						new TImageRestoreTask(Item,holder).Start();
					else {
						BMP = ImageCache.getBitmap(Item.Item.ID);
						if (BMP == null) {
							Item.BMP_flLoaded = false;
							//.
							new TImageLoadTask(Item,holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}
			}
			//.
			if (BMP != null) {
				holder.ivImage.setImageBitmap(BMP);
				holder.ivImage.setOnClickListener(ImageClickListener);
			}
			else {
				holder.ivImage.setImageDrawable(context.getResources().getDrawable(Item.Item.GetThumbnailImageResId()));
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
	private Context context;
	private Object Parent;
	private LinearLayout ParentLayout;
	//.
	private String Folder;
	private TFileFilter FileFilter;
	//.
	private int ListRowSizeID;
	//.
	private TFileSystemFileSelector.OpenDialogListener Listener;
	//.
    private TFolderFileList FolderList = null;
    //.
    private TListAdapter	lvListAdapter = null;
	private ListView 		lvList;
	//.
	private TextView lbName;
	//.
	private View ProgressBar;
	//.
	private Button btnSelect;
	private Button btnPreview;
	private Button btnCancel;
	//.
	private TUpdating	Updating = null;
	
	public TFileSystemPreviewFileSelectorComponent(Object pParent, LinearLayout pParentLayout, String pFolder, String pFileFilter, int pListRowSizeID, TFileSystemFileSelector.OpenDialogListener pListener) {
		Parent = pParent;
		ParentLayout = pParentLayout;
		context = ParentLayout.getContext();
		//.
		Folder = pFolder;
		//.
		if ((pFileFilter != null) && (pFileFilter.length() > 0))
			FileFilter = new TFileFilter(pFileFilter);
		else
			FileFilter = null;
		//.
		ListRowSizeID = pListRowSizeID;
		//.
		Listener = pListener;
        //.
		lvList = (ListView)ParentLayout.findViewById(R.id.lvList);
		lvList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lvList.setOnItemClickListener(new OnItemClickListener() {  
			
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (FolderList == null)
					return; //. ->
				FolderList_OpenItem(arg2);				
        	}              
        });         
		lvList.setOnItemLongClickListener(new OnItemLongClickListener() {
			
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (FolderList == null)
					return false; //. ->
				@SuppressWarnings("unused")
				TFolderFileList.TItem Item = FolderList.Items.get(arg2);
				return true;
			}
		}); 
        lvList.setOnScrollListener(new OnScrollListener() {
        	
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            	if (lvListAdapter != null)
            		lvListAdapter.flListIsScrolling = (scrollState != OnScrollListener.SCROLL_STATE_IDLE); 
            }
        });
        //.
        lbName = (TextView)ParentLayout.findViewById(R.id.lbName);
        //.
        ProgressBar = ParentLayout.findViewById(R.id.pbProgress);
        //.
        btnSelect = (Button)ParentLayout.findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				if (!((FolderList != null) && (lvListAdapter.Items_SelectedIndex > 0)))
					return; //. ->
				TFolderFileList.TItem Item = FolderList.Items.get(lvListAdapter.Items_SelectedIndex);
				if ((Item.FileType != TFolderFileList.TItem.FILETYPE_BACKFOLDER) && (Item.FileType != TFolderFileList.TItem.FILETYPE_FOLDER)) 
					if (Listener != null)
						Listener.OnSelectedFile(Item.FileName);
				//.
   		 		if (Parent instanceof Activity)
   		 			((Activity)Parent).finish();
   		 		else
       		 		if (Parent instanceof Dialog)
       		 			((Dialog)Parent).dismiss();
            }
        });
        //.
        btnPreview = (Button)ParentLayout.findViewById(R.id.btnPreview);
        btnPreview.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				if (lvListAdapter.Items_SelectedIndex > 0) 
					FolderList_PreviewItem(lvListAdapter.Items_SelectedIndex);					
			}
        });
        //.
        btnCancel = (Button)ParentLayout.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				if (Listener != null)
					Listener.OnCancel();
				//.
		 		if (Parent instanceof Activity)
		 			((Activity)Parent).finish();
		 		else
   		 		if (Parent instanceof Dialog)
   		 			((Dialog)Parent).dismiss();
			}
        });
        //.
        flExists = true;
	}

	@Override
	public void Destroy() throws Exception {
		flExists = false;
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
	
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

    public void SetFolder(String pFolder) {
    	Folder = pFolder;
    	//.
    	StartUpdating();
    }

    private void FolderList_PreviewItem(int ItemIndex) {
		String FileName = FolderList.Items.get(ItemIndex).FileName;
		//.
		File file = new File(FileName);
	    MimeTypeMap map = MimeTypeMap.getSingleton();
	    String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
	    String type = map.getMimeTypeFromExtension(ext);
	    if (type == null)
	        type = "*/*";
	    Intent intent = new Intent(Intent.ACTION_VIEW);
	    Uri data = Uri.fromFile(file);
	    intent.setDataAndType(data, type);
	    //.
	    context.startActivity(intent);        	
    }
    
    public void FolderList_OpenItem(int ItemIndex) {
		TFolderFileList.TItem Item = FolderList.Items.get(ItemIndex);
		//.
    	if (Item.FileType != TFolderFileList.TItem.FILETYPE_BACKFOLDER) 
    		if (Item.FileType != TFolderFileList.TItem.FILETYPE_FOLDER) 
    			lvListAdapter.Items_SetSelectedIndex(ItemIndex, true);
    		else
    			SetFolder(Folder+"/"+Item.Name);
    	else {
    		File F = new File(Folder);
    		String ParentFolder = F.getParent();
    		if (ParentFolder != null)
    			SetFolder(ParentFolder);
    	}
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
        private TFolderFileList FolderList = null;
    	
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
	    				//.
	    				FolderList = new TFolderFileList(Folder, FileFilter);
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
		                Toast.makeText(context, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            case MESSAGE_COMPLETEDBYCANCEL:
						if (!flExists)
			            	break; //. >
		            	TFileSystemPreviewFileSelectorComponent.this.FolderList = FolderList;
	           		 	//.
	           		 	TFileSystemPreviewFileSelectorComponent.this.Update();
	           		 	//.
	           		 	if ((msg.what == MESSAGE_COMPLETEDBYCANCEL) && ((TFileSystemPreviewFileSelectorComponent.this.FolderList == null) || (TFileSystemPreviewFileSelectorComponent.this.FolderList.Count() == 0))) {
	           		 		if (Parent instanceof Activity)
	           		 			((Activity)Parent).finish();
	           		 		else
		           		 		if (Parent instanceof Dialog)
		           		 			((Dialog)Parent).dismiss();
	           		 	}
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TFileSystemPreviewFileSelectorComponent.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(context);    
		            	progressDialog.setMessage(context.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
		            		
							@Override
							public void onCancel(DialogInterface arg0) {
		            			TAsyncProcessing Processing = new TAsyncProcessing(context,context.getString(R.string.SWaitAMoment)) {
		            				
		            				@Override
		            				public void Process() throws Exception {
		            					Thread.sleep(100);
		            					//.
				            			TUpdating.this.CancelAndWait();
		            				}
		            				
		            				@Override 
		            				public void DoOnCompleted() throws Exception {
										if (flClosePanelOnCancel) {
					           		 		if (Parent instanceof Activity)
					           		 			((Activity)Parent).finish();
					           		 		else
						           		 		if (Parent instanceof Dialog)
						           		 			((Dialog)Parent).dismiss();
										}
		            				}
		            				
		            				@Override
		            				public void DoOnException(Exception E) {
		            					Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
		            				}
		            			};
		            			Processing.Start();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, context.getString(R.string.SCancel), new DialogInterface.OnClickListener() {
		            		
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
		            			TAsyncProcessing Processing = new TAsyncProcessing(context,context.getString(R.string.SWaitAMoment)) {
		            				
		            				@Override
		            				public void Process() throws Exception {
		            					Thread.sleep(100);
		            					//.
				            			TUpdating.this.CancelAndWait();
		            				}
		            				
		            				@Override 
		            				public void DoOnCompleted() throws Exception {
										if (flClosePanelOnCancel) {
					           		 		if (Parent instanceof Activity)
					           		 			((Activity)Parent).finish();
					           		 		else
						           		 		if (Parent instanceof Dialog)
						           		 			((Dialog)Parent).dismiss();
										}
		            				}
		            				
		            				@Override
		            				public void DoOnException(Exception E) {
		            					Toast.makeText(context, E.getMessage(), Toast.LENGTH_LONG).show();
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
		            	if (progressDialog.isShowing()) {
	           		 		if (Parent instanceof Activity) {
	    		                if (!((Activity)Parent).isFinishing()) 
	    		                	try {
	    			                	progressDialog.dismiss(); 
	    		                	}
	    		                	catch (IllegalArgumentException IAE) {} 
	           		 		}
	           		 		else
			                	try {
				                	progressDialog.dismiss(); 
			                	}
			                	catch (IllegalArgumentException IAE) {} 
		            	}
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
		            		progressDialog.setMessage(context.getString(R.string.SLoading)+"  "+S);
		            	else
		            		progressDialog.setMessage(context.getString(R.string.SLoading));
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
    	lbName.setText(Folder);
    	//.
    	if (FolderList == null) {
    		lvList.setAdapter(null);
    		return; //. ->
    	}
		//.
		int Cnt = FolderList.Count();
		TListItem[] Items = new TListItem[Cnt];
		for (int I = 0; I < Cnt; I++) {
			TFolderFileList.TItem URL = FolderList.Items.get(I);
			//.
			TListItem Item = new TListItem(URL);
			Items[I] = Item;
		}
		lvListAdapter = new TListAdapter(this, lvList, ProgressBar, Items);
		lvList.setAdapter(lvListAdapter);
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,false);
    }    
    
    private static final int MESSAGE_STARTUPDATING = 1;
    
	private final Handler MessageHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
        	try {
    			switch (msg.what) {

    			case MESSAGE_STARTUPDATING:
    				if (!flExists)
    	            	break; //. >
    				StartUpdating();
    				// .
    				break; // . >				
    			}
        	}
        	catch (Throwable E) {
        		TGeoLogApplication.Log_WriteError(E);
        	}
		}
	};
	
	public void PostStartUpdating() {
		MessageHandler.obtainMessage(MESSAGE_STARTUPDATING).sendToTarget();
	}
}
