package com.geoscope.GeoEye.Space.URLs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmlpull.v1.XmlSerializer;

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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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

import com.geoscope.Classes.Data.Containers.Text.XML.TMyXML;
import com.geoscope.Classes.Data.Types.Identification.TUIDGenerator;
import com.geoscope.Classes.Data.Types.Image.TDiskImageCache;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemFileSelector;
import com.geoscope.Classes.IO.File.FileSelector.TFileSystemPreviewFileSelector;
import com.geoscope.Classes.IO.UI.TUIComponent;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.R;
import com.geoscope.GeoEye.TReflectorComponent;
import com.geoscope.GeoEye.TUserListComponent;
import com.geoscope.GeoEye.TUserListPanel;
import com.geoscope.GeoEye.Space.Server.User.TGeoScopeServerUser;
import com.geoscope.GeoEye.Space.URL.TURL;
import com.geoscope.GeoEye.UserAgentService.TUserAgent;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.Application.THintManager;

@SuppressLint("HandlerLeak")
public class TURLFolderListComponent extends TUIComponent {
	
	private static ArrayList<TURLFolderListComponent> Components = new ArrayList<TURLFolderListComponent>();
	
	private static synchronized void Components_Add(TURLFolderListComponent Component) {
		Components.add(Component);
	}
	
	private static synchronized void Components_Remove(TURLFolderListComponent Component) {
		Components.remove(Component);
	}
	
	private static synchronized TURLFolderListComponent Components_GetComponent(String URLListFolder) {
		int Cnt = Components.size();
		for (int I = 0; I < Cnt; I++) {
			TURLFolderListComponent Component = Components.get(I);
			if (Component.URLListFolder.equals(URLListFolder))
				return Component; //. ->
		}
		return null;
	}
	
	public static synchronized void Components_AddNewURL(String URLListFolder, String URLName, byte[] URLData, TGeoScopeServerUser User) throws Exception {
		TURLFolderListComponent.TURLFolderList URLFolderList = new TURLFolderListComponent.TURLFolderList(URLListFolder, User);
		com.geoscope.GeoEye.Space.URL.TURL URL = com.geoscope.GeoEye.Space.URL.TURL.GetURLFromXmlData(URLData, User);
		if (URL != null) {
			URL.XMLDocumentData = URLData;
			//.
			URLFolderList.Add(URLName, URL);
			//.
			TURLFolderListComponent Component = Components_GetComponent(URLListFolder);
			if (Component != null)
				Component.PostStartUpdating();
		}
	}
	
	public static class TURLFolderList {
	
		public static class TItem {
			
			public String ID = "";
			public String Name = "";
			public com.geoscope.GeoEye.Space.URL.TURL URL = null;
		}
	
		public static final String ItemsFileName = "URLs.xml";
		
		private String Folder;
		private TGeoScopeServerUser User;
		//.
		public ArrayList<TItem> Items = new ArrayList<TItem>();
		
		public TURLFolderList(String pFolder, TGeoScopeServerUser pUser) throws Exception {
			Folder = pFolder;
			User = pUser;
			//.
			Load();
		}
		
		public int Count() {
			return Items.size();
		}
		
		public void Load() throws Exception {
			Items.clear();
			//.
			String FN = Folder+"/"+ItemsFileName;
			File F = new File(FN);
			if (!F.exists()) 
				return; //. ->
			//.
			byte[] XML;
	    	long FileSize = F.length();
	    	FileInputStream FIS = new FileInputStream(F);
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
			int Version = Integer.parseInt(TMyXML.SearchNode(RootNode,"Version").getFirstChild().getNodeValue());
			switch (Version) {
			
			case 1:
				NodeList ItemsNode = TMyXML.SearchNode(RootNode,"Items").getChildNodes();
				int Cnt = ItemsNode.getLength();
				for (int I = 0; I < Cnt; I++) {
					Node ItemNode = ItemsNode.item(I);
					//.
					if (ItemNode.getLocalName() != null) {
						TItem Item = new TItem();
						//.
						Item.ID = TMyXML.SearchNode(ItemNode,"ID").getFirstChild().getNodeValue();
						Item.Name = TMyXML.SearchNode(ItemNode,"Name").getFirstChild().getNodeValue();
						//.
					    String URLFileName = Folder+"/"+Item.ID+".xml";
						F = new File(URLFileName);
						if (F.exists()) {
					    	FileSize = F.length();
					    	FIS = new FileInputStream(F);
					    	try {
					    		XML = new byte[(int)FileSize];
					    		FIS.read(XML);
					    		//.
					    		Item.URL = com.geoscope.GeoEye.Space.URL.TURL.GetURLFromXmlData(XML, User);					    	
					    	}
					    	finally {
					    		FIS.close();
					    	}
						}
						//.
	    				Items.add(Item);
					}
				}
				break; //. >
				
			default:
				throw new Exception("unknown data version, version: "+Integer.toString(Version)); //. =>
			}
		}
		
		public void Save() throws Exception {
	    	int Version = 1;
			String FN = Folder+"/"+ItemsFileName;
	        File F = new File(FN);
		    if (!F.exists()) {
		    	F.getParentFile().mkdirs();
		    	F.createNewFile();
		    }
		    XmlSerializer serializer = Xml.newSerializer();
		    FileWriter writer = new FileWriter(FN);
		    try {
		        serializer.setOutput(writer);
		        serializer.startDocument("UTF-8",true);
		        serializer.startTag("", "ROOT");
		        //.
	            serializer.startTag("", "Version");
	            serializer.text(Integer.toString(Version));
	            serializer.endTag("", "Version");
		        //. Items
	            serializer.startTag("", "Items");
	             	int Cnt = Items.size();
	            	for (int I = 0; I < Cnt; I++) {
	            		TItem Item = Items.get(I);
		            	serializer.startTag("", "I"+Integer.toString(I));
		            		//. ID
		            		serializer.startTag("", "ID");
		            		serializer.text(Item.ID);
		            		serializer.endTag("", "ID");
		            		//. Name
		            		serializer.startTag("", "Name");
		            		serializer.text(Item.Name);
		            		serializer.endTag("", "Name");
		            		//. Lays
		            	serializer.endTag("", "I"+Integer.toString(I));
	            	}
	            serializer.endTag("", "Items");
	            //.
		        serializer.endTag("", "ROOT");
		        serializer.endDocument();
		    }
		    finally {
		    	writer.close();
		    }
		}	

		public TItem GetItem(String ID) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++) {
				TItem Item = Items.get(I); 
				if (Item.ID.equals(ID)) 
					return Item; //. ->
			}
			return null;
		}
		
		public int GetItemIndex(String ID) {
			int Cnt = Items.size();
			for (int I = 0; I < Cnt; I++)
				if (Items.get(I).ID.equals(ID)) 
					return I; //. ->
			return -1;
		}
		
		public byte[] GetItemData(String ItemID) throws IOException {
		    String URLFileName = Folder+"/"+ItemID+".xml";
			File F = new File(URLFileName);
			if (F.exists()) {
		    	long FileSize = F.length();
		    	FileInputStream FIS = new FileInputStream(F);
		    	try {
		    		byte[] Data = new byte[(int)FileSize];
		    		FIS.read(Data);
		    		//.
		    		return Data; //. ->
		    	}
		    	finally {
		    		FIS.close();
		    	}
			}
			else
				return null; //. ->
		}
		
		public String Add(String Name, com.geoscope.GeoEye.Space.URL.TURL URL) throws Exception {
	        File F = new File(Folder);
		    if (!F.exists()) 
		    	F.mkdirs();
		    //.
		    String ID = TUIDGenerator.Generate();
		    String URLFileName = Folder+"/"+ID+".xml";
			FileOutputStream FOS = new FileOutputStream(URLFileName);
			try {
				FOS.write(URL.XMLDocumentData);
			}
			finally {
				FOS.close();
			}
			//.
			TItem Item = new TItem();
			Item.ID = ID;
			Item.Name = Name;
    		Item.URL = URL;
			//.
			Items.add(Item);
			//.
			Save();
			//.
			return ID; //. ->
		}
		
		public String Remove(int Index) throws Exception {
			TItem Item = Items.remove(Index);
			//.
		    String URLFileName = Folder+"/"+Item.ID+".xml";
		    File URLFile = new File(URLFileName);
		    URLFile.delete();
		    //.
			Save();
			//.
			return Item.ID;
		}

		public void Remove(String ID) throws Exception {
			int Index = GetItemIndex(ID);
			if (Index >= 0)
				Remove(Index);
		}
		
		public boolean Replace(String ID, int NewIndex) throws Exception {
			boolean Result = false;
			if ((0 <= NewIndex) && (NewIndex < Items.size())) {
				int Index = GetItemIndex(ID);
				if (Index >= 0) {
					TItem Item = Items.remove(Index);
					Items.add(NewIndex, Item);
				    //.
					Save();
					//.
					Result = true;
				}
			}
			return Result;
		}
	}
	
	public static final int LIST_ROW_SIZE_SMALL_ID 	= 1;
	public static final int LIST_ROW_SIZE_NORMAL_ID = 2;
	public static final int LIST_ROW_SIZE_BIG_ID 	= 3;
	//.
	public static final int LIST_ROW_IMAGESIZE_SMALL 	= 64;
	public static final int LIST_ROW_IMAGESIZE_NORMAL 	= 128;
	public static final int LIST_ROW_IMAGESIZE_BIG 		= 256;
	
	public static class TOnListItemClickHandler {
		
		public void DoOnListItemClick(com.geoscope.GeoEye.Space.URL.TURL URL) {
		}
	}
	
	private static class TURLListItem {
		
		public TURLFolderList.TItem Item;
		//.
		public boolean 		BMP_flLoaded = false;
		public boolean 		BMP_flNull = false;
		
		public TURLListItem(TURLFolderList.TItem pItem) {
			Item = pItem;
		}
	}
	
	public static class TURLListAdapter extends BaseAdapter {

		private static final String 		ImageCache_Name = "URLListImages";
		private static final int			ImageCache_Size = 1024*1024*10; //. Mb
		private static final CompressFormat ImageCache_CompressFormat = CompressFormat.PNG;
		private static final int			ImageCache_CompressQuality = 100;
		
		private static class TViewHolder {
			
			public TURLListItem Item;
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
			
			private TURLListAdapter Adapter;
			//.
			private TURLListItem Item;
			//.
			private TViewHolder ViewHolder;
			

			public TImageLoadTask(TURLListAdapter pAdapter, TURLListItem pItem, TViewHolder pViewHolder) {
				Adapter = pAdapter;
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
				Bitmap Result = null;
				if (Item.Item.URL != null)
					Result = Item.Item.URL.GetThumbnailImage(Adapter.Panel.ListRowImageSize); 
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
			
			private TURLListItem Item;
			
			private TViewHolder ViewHolder;
			
			private Bitmap bitmap = null;

			public TImageRestoreTask(TURLListItem pItem, TViewHolder pViewHolder) {
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
		private TURLFolderListComponent Panel;
		//.
		private ListView MyListView;
		//.
		private View 				ProgressBar;
		private TProgressHandler 	ProgressHandler;
		//.
		private TURLListItem[] 			Items;
		private String					Items_SelectedItemID = "";
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
				TURLListItem Item = (TURLListItem)Items[position];
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
	        
		public TURLListAdapter(TURLFolderListComponent pPanel, ListView pMyListView, View pProgressBar, TURLListItem[] pItems) {
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
			Items_SelectedItemID = Items[Index].Item.ID;
			//.
			if (flNotify)
				notifyDataSetChanged();
		}
		
		public void Items_SetSelectedItemID(String ItemID, boolean flNotify) {
			Items_SelectedItemID = ItemID;
			//.
			if (flNotify)
				notifyDataSetChanged();
		}
		
		public String Items_GetSelectedItemID() {
			return Items_SelectedItemID;
		}
		
		public void Items_Replace(String ItemID, int NewIndex, boolean flNotify) {
			int Cnt = Items.length;
			if ((0 <= NewIndex) && (NewIndex < Cnt)) {
				int Index = -1;
				for (int I = 0; I < Cnt; I++)
					if (Items[I].Item.ID.equals(ItemID)) {
						Index = I;
						break; //. >
					}
				if ((Index >= 0) && (Index != NewIndex)) { 
					TURLListItem Item = Items[Index]; 
					if (NewIndex < Index) {
						Cnt = (Index-NewIndex);
						for (int I = Index; I > NewIndex; I--)
							Items[I] = Items[I-1];
					}
					else {
						Cnt = (NewIndex-Index);
						for (int I = Index; I < NewIndex; I++)
							Items[I] = Items[I+1];
					}
					Items[NewIndex] = Item; 
					//.
					if (flNotify)
						notifyDataSetChanged();
				}
			}
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TViewHolder holder;
			if (convertView == null) {
				int LayoutID = R.layout.urllist_row_layout;
				switch (Panel.ListRowSizeID) {
				
				case LIST_ROW_SIZE_SMALL_ID:
					LayoutID = R.layout.urllist_row_small_layout;
					break; //. >
					
				case LIST_ROW_SIZE_NORMAL_ID:
					LayoutID = R.layout.urllist_row_layout;
					break; //. >
					
				case LIST_ROW_SIZE_BIG_ID:
					LayoutID = R.layout.urllist_row_big_layout;
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
			TURLListItem Item = (TURLListItem)Items[position];
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
				new TImageLoadTask(this,Item,holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			else {
				if (!Item.BMP_flNull)
					if (flListIsScrolling)
						new TImageRestoreTask(Item,holder).Start();
					else {
						BMP = ImageCache.getBitmap(Item.Item.ID);
						if (BMP == null) {
							Item.BMP_flLoaded = false;
							//.
							new TImageLoadTask(this,Item,holder).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
						}
					}
			}
			//.
			if (BMP != null) {
				holder.ivImage.setImageBitmap(BMP);
				holder.ivImage.setOnClickListener(ImageClickListener);
			}
			else {
				holder.ivImage.setImageDrawable(context.getResources().getDrawable(holder.Item.Item.URL.GetThumbnailImageResID(Panel.ListRowImageSize)));
				holder.ivImage.setOnClickListener(null);
			}
			//. show selection
			if (Item.Item.ID.equals(Items_SelectedItemID)) {
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
	private String URLListFolder;
	//.
	private int ListRowSizeID;
	private int ListRowImageSize;
	//.
	private TReflectorComponent Component;
	//.
	private TOnListItemClickHandler OnListItemClickHandler;
	//.
    private TURLFolderList URLList = null;
    //.
    private TURLListAdapter	lvURLListAdapter = null;
	private ListView 		lvURLList;
	//.
	private View ProgressBar;
	//.
	private Button btnImportURL;
	private Button btnUpItem;
	private Button btnDownItem;
	//.
	private TUpdating	Updating = null;
	
	public TURLFolderListComponent(Activity pParentActivity, LinearLayout pParentLayout, String pURLListFolder, int pListRowSizeID, TReflectorComponent pComponent, TOnListItemClickHandler pOnListItemClickHandler) {
		ParentActivity = pParentActivity;
		ParentLayout = pParentLayout;
		//.
		URLListFolder = pURLListFolder;
		//.
		ListRowSizeID = pListRowSizeID;
        //.
		Component = pComponent;
		//.
		OnListItemClickHandler = pOnListItemClickHandler;
		//.
		switch (ListRowSizeID) {

		case LIST_ROW_SIZE_SMALL_ID:
			ListRowImageSize = LIST_ROW_IMAGESIZE_SMALL;
			break;

		case LIST_ROW_SIZE_NORMAL_ID:
			ListRowImageSize = LIST_ROW_IMAGESIZE_NORMAL;
			break;

		case LIST_ROW_SIZE_BIG_ID:
			ListRowImageSize = LIST_ROW_IMAGESIZE_BIG;
			break;

		default:
			ListRowImageSize = LIST_ROW_IMAGESIZE_NORMAL;
			break;
		}
		ListRowImageSize *= ParentActivity.getResources().getDisplayMetrics().density; 
        //.
		lvURLList = (ListView)ParentLayout.findViewById(R.id.lvURLList);
		lvURLList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
		lvURLList.setOnItemClickListener(new OnItemClickListener() {  
			
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (URLList == null)
					return; //. ->
				com.geoscope.GeoEye.Space.URL.TURL URL = URLList.Items.get(arg2).URL;
				if (URL == null)
					return; //. ->
				//.
				try {
					if (OnListItemClickHandler != null)
						OnListItemClickHandler.DoOnListItemClick(URL);
					else
						try {
							URL.Open(ParentActivity);
						}
						catch (Exception E) {
			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
						}
				}
				finally {
					lvURLListAdapter.Items_SetSelectedIndex(arg2, true);
				}
        	}              
        });         
		lvURLList.setOnItemLongClickListener(new OnItemLongClickListener() {
			
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (URLList == null)
					return false; //. ->
				final TURLFolderList.TItem URLItem = URLList.Items.get(arg2);
				if (URLItem.URL == null)
					return false; //. ->
            	//.
				try {
		    		final CharSequence[] _items;
		    		int SelectedIdx = -1;
		    		_items = new CharSequence[3];
		    		_items[0] = ParentActivity.getString(R.string.SOpen); 
		    		_items[1] = ParentActivity.getString(R.string.SSendBookmarkToUser); 
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
		    		    		
		    		    		case 0: //. open URL
		    						try {
		    							URLItem.URL.Open(ParentActivity);
			    						//.
			        		    		arg0.dismiss();
		    						}
		    						catch (Exception E) {
		    			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		    						}
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 1: //. send URL
		    		    			SelectedURL_SendToUser();
		    						//.
		        		    		arg0.dismiss();
		        		    		//.
		    		    			break; //. >
		    		    			
		    		    		case 2: //. remove URL
		    		    			AlertDialog.Builder alert = new AlertDialog.Builder(ParentActivity);
		    		    			//.
		    		    			alert.setTitle(R.string.SRemoval);
		    		    			alert.setMessage(R.string.SRemoveSelectedBookmark);
		    		    			//.
		    		    			alert.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
		    		    				
		    		    				@Override
		    		    				public void onClick(DialogInterface dialog, int whichButton) {
		    		    					try {
		    		    						RemoveURLByID(URLItem.ID);
				    						}
				    						catch (Exception E) {
				    			                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
				    						}
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
					lvURLListAdapter.Items_SetSelectedIndex(arg2, true);
				}
			}
		}); 
        lvURLList.setOnScrollListener(new OnScrollListener() {
        	
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
            	if (lvURLListAdapter != null)
            		lvURLListAdapter.flListIsScrolling = (scrollState != OnScrollListener.SCROLL_STATE_IDLE); 
            }
        });
        //.
        ProgressBar = ParentLayout.findViewById(R.id.pbProgress);
        //.
        btnImportURL = (Button)ParentLayout.findViewById(R.id.btnImportURL);
        btnImportURL.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
            	try {
    				String IFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
    				File IF = new File(IFN);
    				if (IF.exists())
    					ImportURLFromFile(IFN);
    				else {
    					TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(ParentActivity, TGeoLogApplication.GetTempFolder(), ".XML", new TFileSystemFileSelector.OpenDialogListener() {
    			        	
    			            @Override
    			            public void OnSelectedFile(String fileName) {
    			            	try {
    			            		ImportURLFromFile(fileName);
    							} catch (Exception E) {
    								Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
    							}
    			            }

    						@Override
    						public void OnCancel() {
    						}
    			        });
    			    	FileSelector.show();
    			    	//.
    					Toast.makeText(ParentActivity, R.string.SNoUrlFound,	Toast.LENGTH_LONG).show();
    				}
				} catch (Exception E) {
					Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
				}
            }
        });
        btnImportURL.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				final CharSequence[] _items;
				_items = new CharSequence[2];
				_items[0] = ParentActivity.getString(R.string.SImportFromFile);
				_items[1] = ParentActivity.getString(R.string.SCreateInternetAddressBookmark);
				AlertDialog.Builder builder = new AlertDialog.Builder(ParentActivity);
				builder.setTitle(R.string.SOperations);
				builder.setNegativeButton(ParentActivity.getString(R.string.SCancel),null);
				builder.setSingleChoiceItems(_items, 0, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						arg0.dismiss();
						//.
		            	try {
							switch (arg1) {
							
							case 0: 
								TFileSystemPreviewFileSelector FileSelector = new TFileSystemPreviewFileSelector(ParentActivity, TGeoLogApplication.GetTempFolder(), ".XML", new TFileSystemFileSelector.OpenDialogListener() {
						        	
						            @Override
						            public void OnSelectedFile(String fileName) {
						            	try {
						            		ImportURLFromFile(fileName);
										} catch (Exception E) {
											Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
										}
						            }

									@Override
									public void OnCancel() {
									}
						        });
						    	FileSelector.show();
						    	//.
								break; //. >
								
							case 1: 
				                final LinearLayout layout = new LinearLayout(ParentActivity);
				                layout.setOrientation(LinearLayout.VERTICAL);
				                TextView label = new TextView(ParentActivity);
				                label.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				                label.setText(R.string.SName1);
				                layout.addView(label);
								final EditText edName = new EditText(ParentActivity);
				                layout.addView(edName);
				                label = new TextView(ParentActivity);
				                label.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				                label.setText(R.string.SAddress);
				                layout.addView(label);
				                final EditText edInternetURL = new EditText(ParentActivity);
				                edInternetURL.setText("http://");
				                layout.addView(edInternetURL);
				                //.
								final AlertDialog dlg = new AlertDialog.Builder(ParentActivity)
								//.
								.setTitle(R.string.SInternetAddressURL)
								//.
				                .setView(layout)
								//.
								.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int whichButton) {
										//. hide keyboard
										InputMethodManager imm = (InputMethodManager)ParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
										imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
										//.
										try {
											String Name = edName.getText().toString();
											String InternetURL = edInternetURL.getText().toString();
											//.
						            		String URLFN = TGeoLogApplication.GetTempFolder()+"/"+TURL.DefaultURLFileName;
						            		com.geoscope.GeoEye.Space.URLs.Internet.TURL URL = new com.geoscope.GeoEye.Space.URLs.Internet.TURL(Name, InternetURL);
						            		URL.ConstructURLFile(URLFN);
						            		//.
						            		com.geoscope.GeoEye.Space.URL.TURL _URL = com.geoscope.GeoEye.Space.URL.TURL.GetURLFromXmlFile(URLFN, URLList.User);
						            		if (_URL != null) {
							            		ImportURL(_URL, Name);
							            		//.
												Toast.makeText(ParentActivity, R.string.SURLHasBeenImportedSuccessfully, Toast.LENGTH_LONG).show();
						            		}
										} catch (Exception E) {
											Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
										}
									}
								})
								//.
								.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
									
									@Override
									public void onClick(DialogInterface dialog, int whichButton) {
										// . hide keyboard
										InputMethodManager imm = (InputMethodManager)ParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
										imm.hideSoftInputFromWindow(layout.getWindowToken(), 0);
									}
								}).create();
								//.
								edInternetURL.setOnEditorActionListener(new OnEditorActionListener() {
									
									@Override
									public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
										dlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick(); 
										return false;
									}
						        });        
								//.
								dlg.show();
								break; //. >
							}
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(ParentActivity, ParentActivity.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
					}
				});
				AlertDialog alert = builder.create();
				alert.show();
		    	//.
				return false;
			}
		});
        //.
        btnUpItem = (Button)ParentLayout.findViewById(R.id.btnUpItem);
        btnUpItem.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				try {
					SelectedURL_MoveUp();
		    	}
		    	catch (Exception E) {
		    		Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
			}
        });
        //.
        btnDownItem = (Button)ParentLayout.findViewById(R.id.btnDownItem);
        btnDownItem.setOnClickListener(new OnClickListener() {
        	
			@Override
            public void onClick(View v) {
				try {
					SelectedURL_MoveDown();
		    	}
		    	catch (Exception E) {
		    		Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		    	}
			}
        });
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
        final int Hint1ID = THintManager.HINT__URLFolderList;
        final TextView lbHint = (TextView)ParentLayout.findViewById(R.id.lbHint);
        Hint = THintManager.GetHint(Hint1ID, ParentActivity);
        if (Hint != null) {
        	lbHint.setText(Hint);
            lbHint.setOnLongClickListener(new OnLongClickListener() {
            	
    			@Override
    			public boolean onLongClick(View v) {
    				THintManager.SetHintAsDisabled(Hint1ID);
    	        	lbHint.setVisibility(View.GONE);
    	        	//.
    				return true;
    			}
    		});
            //.
        	lbHint.setVisibility(View.VISIBLE);
        }
        else
        	lbHint.setVisibility(View.GONE);
        //.
        Components_Add(this);
        //.
        flExists = true;
	}

	@Override
	public void Destroy() throws Exception {
		flExists = false;
		//.
        Components_Remove(this);
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
        switch (requestCode) {        
       
        case REQUEST_SELECT_USER:
        	if (resultCode == Activity.RESULT_OK) {
                Bundle extras = data.getExtras(); 
                if (extras != null) {
            		long UserID = extras.getLong("UserID");
            		SelectedURL_DoSendToUser(UserID);
            	}
        	}
            break; //. >
        }
		super.onActivityResult(requestCode, resultCode, data);
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
        private TURLFolderList URLList = null;
    	
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
	    				URLList = new TURLFolderList(URLListFolder, UserAgent.User());
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
		            	TURLFolderListComponent.this.URLList = URLList;
	           		 	//.
	           		 	TURLFolderListComponent.this.Update();
	           		 	//.
	           		 	if ((msg.what == MESSAGE_COMPLETEDBYCANCEL) && ((TURLFolderListComponent.this.URLList == null) || (TURLFolderListComponent.this.URLList.Count() == 0)))
	           		 		ParentActivity.finish();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TURLFolderListComponent.this.Updating = null;
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
    	if (URLList == null) {
    		lvURLList.setAdapter(null);
    		return; //. ->
    	}
		//.
		int Cnt = URLList.Count();
		TURLListItem[] Items = new TURLListItem[Cnt];
		for (int I = 0; I < Cnt; I++) {
			TURLFolderList.TItem URL = URLList.Items.get(I);
			//.
			TURLListItem Item = new TURLListItem(URL);
			Items[I] = Item;
		}
		lvURLListAdapter = new TURLListAdapter(this, lvURLList, ProgressBar, Items);
		lvURLList.setAdapter(lvURLListAdapter);
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

	private void ImportURL(com.geoscope.GeoEye.Space.URL.TURL URL, String Name) throws Exception {
    	if (URLList == null)
    		return; //. ->
    	//.
		URLList.Add(Name, URL);
		//.
		Update();
		//.
		SelectedURL_SetByIndex(URLList.Count()-1, true, true);
	}
	
	private void ImportURLFromFile(String FN) throws Exception {
		final com.geoscope.GeoEye.Space.URL.TURL URL = com.geoscope.GeoEye.Space.URL.TURL.GetURLFromXmlFile(FN, URLList.User);
		//.
		if (URL != null) {
    		final EditText input = new EditText(ParentActivity);
    		input.setInputType(InputType.TYPE_CLASS_TEXT);
    		if (URL.Name != null)
    			input.setText(URL.Name);
    		//.
    		final AlertDialog dlg = new AlertDialog.Builder(ParentActivity)
    		//.
    		.setTitle(R.string.SDataName)
    		.setMessage(R.string.SEnterName)
    		//.
    		.setView(input)
    		.setPositiveButton(R.string.SOk, new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int whichButton) {
    				//. hide keyboard
    				InputMethodManager imm = (InputMethodManager)ParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
    				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    				//.
					try {
						String Name = input.getText().toString();
				    	//.
				    	if (URLList != null) {
				    		if (Name.length() == 0)
				    			Name = "?";
				    		if (URL != null) {
				    			ImportURL(URL, Name);
				    			//.
								Toast.makeText(ParentActivity, R.string.SURLHasBeenImportedSuccessfully, Toast.LENGTH_LONG).show();
				    		}
				    	}
					} catch (Exception E) {
						Toast.makeText(ParentActivity, E.getMessage(),	Toast.LENGTH_LONG).show();
					}
    			}
    		})
    		//.
    		.setNegativeButton(R.string.SCancel, new DialogInterface.OnClickListener() {
    			
    			@Override
    			public void onClick(DialogInterface dialog, int whichButton) {
    				// . hide keyboard
    				InputMethodManager imm = (InputMethodManager)ParentActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
    				imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    			}
    		}).create();
    		//.
    		input.setOnEditorActionListener(new OnEditorActionListener() {
    			
    			@Override
    			public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
    				dlg.getButton(DialogInterface.BUTTON_POSITIVE).performClick(); 
    				return false;
    			}
            });        
    		// .
    		dlg.show();
		}
	}
	
	private void RemoveURLByID(String ID) throws Exception {
		int Index = URLList.GetItemIndex(ID);
		if (Index >= 0) {
			URLList.Remove(Index);
			//.
			Update();    		    							
			//.
			int Cnt = URLList.Count();
			if (Index >= Cnt)
				Index = Cnt-1;
			if (Index >= 0)
				SelectedURL_SetByIndex(Index, true, true);
		}
	}

	private class TURLsToUserSending extends TCancelableThread {

    	private static final int MESSAGE_EXCEPTION 				= 0;
    	private static final int MESSAGE_DONE 					= 1;
    	private static final int MESSAGE_PROGRESSBAR_SHOW 		= 2;
    	private static final int MESSAGE_PROGRESSBAR_HIDE 		= 3;
    	private static final int MESSAGE_PROGRESSBAR_PROGRESS 	= 4;

    	private long UserID;
    	private TURLFolderList List;
    	private TURLFolderList.TItem[] Items;
    	
        private ProgressDialog progressDialog; 
    	
    	public TURLsToUserSending(long pUserID, TURLFolderList pList, TURLFolderList.TItem[] pItems) {
    		super();
    		//.
    		UserID = pUserID;
    		List = pList;
    		Items = pItems;
    		//.
    		_Thread = new Thread(this);
    		_Thread.start();
    	}

		@Override
		public void run() {
			try {
    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_SHOW).sendToTarget();
    			try {
    				for (int I = 0; I < Items.length; I++) {
    					TURLFolderList.TItem Item = Items[I];
    					TGeoScopeServerUser.TURLCommandMessage CommandMessage = new TGeoScopeServerUser.TURLCommandMessage(TGeoScopeServerUser.TGeoMonitorObjectCommandMessage.Version_0, Item.Name, List.GetItemData(Item.ID));
    					Component.User.IncomingMessages_SendNewCommand(UserID,CommandMessage);
    					//.
    	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_PROGRESS,(Integer)(int)(100.0*I/Items.length)).sendToTarget();
        				//.
        				if (Canceller.flCancel)
        					throw new CancelException(); //. =>
    				}
				}
				finally {
	    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_HIDE).sendToTarget();
				}
				//.
    			MessageHandler.obtainMessage(MESSAGE_DONE).sendToTarget();
        	}
        	catch (InterruptedException E) {
        	}
        	catch (CancelException CE) {
        	}
        	catch (IOException E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,E).sendToTarget();
        	}
        	catch (Throwable E) {
    			MessageHandler.obtainMessage(MESSAGE_EXCEPTION,new Exception(E.getMessage())).sendToTarget();
        	}
		}

		private final Handler MessageHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	        	try {
		            switch (msg.what) {
		            
		            case MESSAGE_EXCEPTION:
		            	Exception E = (Exception)msg.obj;
		                Toast.makeText(ParentActivity, E.getMessage(), Toast.LENGTH_LONG).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_DONE:
	                    Toast.makeText(ParentActivity, ParentActivity.getString(R.string.SBookmarkHasBeenSent), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(ParentActivity);
		            	progressDialog.setMessage(ParentActivity.getString(R.string.SSending));    
		            	if (Items.length > 1) {
			            	progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		            		progressDialog.setIndeterminate(false);
		            		progressDialog.setMax(100);
		            	}
		            	else { 
			            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		            		progressDialog.setIndeterminate(true);
		            	}
		            	progressDialog.setCancelable(false);
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

	private void SelectedURL_SetByIndex(int Index, boolean flNotify, boolean flMakeVisible) throws Exception {
		lvURLListAdapter.Items_SetSelectedIndex(Index, flNotify);
		if (flMakeVisible) {
			lvURLList.setItemChecked(Index, true);
			lvURLList.setSelection(Index);
		}
	}
	
	private void SelectedURL_MoveUp() throws Exception {
		String SelectedItemID = lvURLListAdapter.Items_GetSelectedItemID();
		int ItemIndex  = URLList.GetItemIndex(SelectedItemID);
		if (ItemIndex > 0) {
    		//.
			ItemIndex--;
			if (URLList.Replace(SelectedItemID, ItemIndex)) {
				lvURLListAdapter.Items_Replace(SelectedItemID, ItemIndex, true);
				//.
				if (!((lvURLList.getFirstVisiblePosition() < ItemIndex) && (ItemIndex < lvURLList.getLastVisiblePosition()))) {
					lvURLList.setItemChecked(ItemIndex, true);
					lvURLList.setSelection(ItemIndex);
				}
			}
		}
	}
	
	private void SelectedURL_MoveDown() throws Exception {
		String SelectedItemID = lvURLListAdapter.Items_GetSelectedItemID();
		int ItemIndex  = URLList.GetItemIndex(SelectedItemID);
		if (ItemIndex >= 0) {
    		//.
			ItemIndex++;
			if (URLList.Replace(SelectedItemID, ItemIndex)) {
				lvURLListAdapter.Items_Replace(SelectedItemID, ItemIndex, true);
				//.
				if (!((lvURLList.getFirstVisiblePosition() < ItemIndex) && (ItemIndex < lvURLList.getLastVisiblePosition()))) { 
					lvURLList.setItemChecked(ItemIndex, true);
					lvURLList.setSelection(ItemIndex);
				}
			}
		}
	}
	
	@SuppressWarnings("unused")
	private void SelectedURL_MakeVisible() throws Exception {
		String SelectedItemID = lvURLListAdapter.Items_GetSelectedItemID();
		int ItemIndex  = URLList.GetItemIndex(SelectedItemID);
		if (ItemIndex >= 0) {
			lvURLList.setItemChecked(ItemIndex, true);
			lvURLList.setSelection(ItemIndex);
		}
	}
	
	private static final int REQUEST_SELECT_USER = 1;
	
	private void SelectedURL_SendToUser() {
    	Intent intent = new Intent(ParentActivity, TUserListPanel.class);
		intent.putExtra("ComponentID", Component.ID);
    	intent.putExtra("Mode",TUserListComponent.MODE_FORURL);    	
    	ParentActivity.startActivityForResult(intent, REQUEST_SELECT_USER);		
	}
	
	private void SelectedURL_DoSendToUser(long UserID) {
		TURLFolderList.TItem SelectedItem = URLList.GetItem(lvURLListAdapter.Items_GetSelectedItemID());
		if (SelectedItem == null)
			return; //. ->
		new TURLsToUserSending(UserID, URLList, new TURLFolderList.TItem[] {SelectedItem});
	}
}
