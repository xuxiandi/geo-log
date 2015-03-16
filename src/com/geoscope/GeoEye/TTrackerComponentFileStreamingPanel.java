package com.geoscope.GeoEye;

import java.io.IOException;
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
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.Classes.Data.Types.Image.TDiskImageCache;
import com.geoscope.Classes.Data.Types.Image.TImageViewerPanel;
import com.geoscope.Classes.Exception.CancelException;
import com.geoscope.Classes.MultiThreading.TAsyncProcessing;
import com.geoscope.Classes.MultiThreading.TCancelableThread;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines;
import com.geoscope.GeoEye.Space.Defines.SpaceDefines.TTypedDataFile;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingDefines;
import com.geoscope.GeoEye.Space.TypesSystem.DATAFile.Types.Image.Drawing.TDrawingEditor;
import com.geoscope.GeoLog.Application.TGeoLogApplication;
import com.geoscope.GeoLog.DEVICEModule.TDEVICEModule.TComponentFileStreaming.TItem;
import com.geoscope.GeoLog.TrackerService.TTracker;

@SuppressLint("HandlerLeak")
public class TTrackerComponentFileStreamingPanel extends Activity {

	public static final int		ItemImageSize = 512;
	
	private static class TListItem {
		
		public String 	Name;
		public String 	Info;
		//.
		public TTypedDataFile TypedDataFile;
		//.
		public boolean BMP_flLoaded = false;
		public boolean BMP_flNull = false;
		
		public TListItem(String pName, String pInfo, TTypedDataFile pTypedDataFile) {
			Name = pName;
			Info = pInfo;
			//.
			TypedDataFile = pTypedDataFile;
		}
	}
	
	public static class TListAdapter extends BaseAdapter {

		private static final String 		ImageCache_Name = "TrackerComponentFileStreamingImages";
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
						return ImageCache.getBitmap(Integer.toString(Item.TypedDataFile.hashCode())); //. ->
					else 
						return null; //. ->
				}
				//.
				Bitmap Result = Item.TypedDataFile.GetImage(ItemImageSize,ItemImageSize);
				//.
				if (Result != null) 
					ImageCache.put(Integer.toString(Item.TypedDataFile.hashCode()), Result);
				else
					Item.BMP_flNull = true;
				Item.BMP_flLoaded = true;
				//.
				return Result;
			}
		}
		
		private class TImageRestoreTask extends TAsyncProcessing {
			
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
				return ImageCache.getBitmap(Integer.toString(Item.TypedDataFile.hashCode()));
			}
		}
		
		private Context context;
		//.
		private TTrackerComponentFileStreamingPanel Panel;
		//.
		private ListView MyListView;
		//.
		private View 				ProgressBar;
		private TProgressHandler 	ProgressHandler;
		//.
		private TListItem[] Items;
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
				TListItem Item = (TListItem)Items[position];
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
	        
		public TListAdapter(TTrackerComponentFileStreamingPanel pPanel, ListView pMyListView, View pProgressBar, TListItem[] pItems) {
			context = pPanel;
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

		@SuppressWarnings("unused")
		public View getView(int position, View convertView, ViewGroup parent) {
			TViewHolder holder;
			if (convertView == null) {
				convertView = layoutInflater.inflate(R.layout.user_activitycomponentlist_row_layout, null);
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
				new TImageLoadTask(Item,holder).executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
			else {
				if (!Item.BMP_flNull) 
					//. last version: BMP = ImageCache.getBitmap(Integer.toString(Item.ComponentValue.hashCode()));
					new TImageRestoreTask(Item,holder).Start();
			}
			//.
			if (BMP != null) {
				holder.ivImage.setImageBitmap(BMP);
				holder.ivImage.setOnClickListener(ImageClickListener);
			}
			else {
				int ImageResID = Item.TypedDataFile.GetImageResID(ItemImageSize,ItemImageSize);
				if (ImageResID != 0)
					holder.ivImage.setImageDrawable(context.getResources().getDrawable(ImageResID));
				else
					holder.ivImage.setImageDrawable(context.getResources().getDrawable(R.drawable.user_activity_component_list_placeholder));
				holder.ivImage.setOnClickListener(null);
			}
			//.
			return convertView;
		}
	}
	
	public boolean flExists = false;
	//.
	private boolean flResumeStreaming = false;
	//.
	private TItem[] 	QueueItems;
	private ListView 	lvQueueItems;
	//.
	private View ProgressBar;
	//.
	private TUpdating	Updating = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//.
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //. 
        setContentView(R.layout.tracker_osoqueue_panel);
        //.
        lvQueueItems = (ListView)findViewById(R.id.lvQueueItems);
        lvQueueItems.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lvQueueItems.setOnItemClickListener(new OnItemClickListener() {         
			@Override
        	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				try {
					TTypedDataFile TypedDataFile = QueueItems[arg2].GetTypedDataFile();
					if (TypedDataFile != null) 
						TTrackerComponentFileStreamingPanel.this.TypedDataFile_Open(TypedDataFile);
				}
				catch (Exception E) {
	                Toast.makeText(TTrackerComponentFileStreamingPanel.this, E.getMessage(), Toast.LENGTH_LONG).show();
				}
        	}              
        });         
        lvQueueItems.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final int ItemIndex = arg2; 
    		    new AlertDialog.Builder(TTrackerComponentFileStreamingPanel.this)
    	        .setIcon(android.R.drawable.ic_dialog_alert)
    	        .setTitle(R.string.SConfirmation)
    	        .setMessage(R.string.SRemoveItemFromQueue)
    		    .setPositiveButton(R.string.SYes, new DialogInterface.OnClickListener() {
    		    	@Override
    		    	public void onClick(DialogInterface dialog, int id) {
    		    		try {
    		    			QueueItems_Remove(QueueItems[ItemIndex]);
						}
						catch (Exception E) {
							String S = E.getMessage();
							if (S == null)
								S = E.getClass().getName();
		        			Toast.makeText(TTrackerComponentFileStreamingPanel.this, TTrackerComponentFileStreamingPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
						}
    		    	}
    		    })
    		    .setNegativeButton(R.string.SNo, null)
    		    .show();
            	//.
            	return true; 
			}
		}); 
        //.
        ProgressBar = findViewById(R.id.pbProgress);
        //.
        setResult(RESULT_CANCELED);
        //.
        flExists = true;
        //.
        StartUpdating();
	}

	@Override
	protected void onDestroy() {
		flExists = false;
		//.
		if (Updating != null) {
			Updating.Cancel();
			Updating = null;
		}
		//.
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		super.onPause();
		//.
		try {
			if (flResumeStreaming) {
		    	TTracker Tracker = TTracker.GetTracker();
		    	if (Tracker != null)
		    		Tracker.GeoLog.ComponentFileStreaming.Start();
			}
		}
		catch (Exception E) {
			String S = E.getMessage();
			if (S == null)
				S = E.getClass().getName();
			Toast.makeText(TTrackerComponentFileStreamingPanel.this, TTrackerComponentFileStreamingPanel.this.getString(R.string.SError)+S, Toast.LENGTH_LONG).show();  						
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
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
        private TItem[] QueueItems;
    	
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
				    	TTracker Tracker = TTracker.GetTracker();
				    	if (Tracker == null)
				    		throw new Exception(TTrackerComponentFileStreamingPanel.this.getString(R.string.STrackerIsNotInitialized)); //. =>
				    	//.
		    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_MESSAGE,TTrackerComponentFileStreamingPanel.this.getString(R.string.SStoppingFileStreaming)).sendToTarget();
	    				if (Tracker.GeoLog.ComponentFileStreaming.IsStarted()) {
	    					try {
	    						Tracker.GeoLog.ComponentFileStreaming.Stop();
	    					}
	    					catch (InterruptedException IE) {
	    					}
	    					flResumeStreaming = true;
	    				}
				    	try {
			    			MessageHandler.obtainMessage(MESSAGE_PROGRESSBAR_MESSAGE,TTrackerComponentFileStreamingPanel.this.getString(R.string.SGettingTheQueueItems)).sendToTarget();
					    	QueueItems = Tracker.GeoLog.ComponentFileStreaming.GetItems();
				    	}
				    	finally {
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
		                Toast.makeText(TTrackerComponentFileStreamingPanel.this, E.getMessage(), Toast.LENGTH_SHORT).show();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_COMPLETED:
		            case MESSAGE_COMPLETEDBYCANCEL:
						if (!flExists)
			            	break; //. >
		            	TTrackerComponentFileStreamingPanel.this.QueueItems = QueueItems;
	           		 	//.
	           		 	TTrackerComponentFileStreamingPanel.this.Update();
	           		 	//.
	           		 	if ((msg.what == MESSAGE_COMPLETEDBYCANCEL) && ((TTrackerComponentFileStreamingPanel.this.QueueItems == null) || (TTrackerComponentFileStreamingPanel.this.QueueItems.length == 0)))
	           		 		TTrackerComponentFileStreamingPanel.this.finish();
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_FINISHED:
						if (Canceller.flCancel)
			            	break; //. >
		            	TTrackerComponentFileStreamingPanel.this.Updating = null;
		            	//.
		            	break; //. >
		            	
		            case MESSAGE_PROGRESSBAR_SHOW:
		            	progressDialog = new ProgressDialog(TTrackerComponentFileStreamingPanel.this);    
		            	progressDialog.setMessage(TTrackerComponentFileStreamingPanel.this.getString(R.string.SLoading));    
		            	progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);    
		            	progressDialog.setIndeterminate(true); 
		            	progressDialog.setCancelable(true);
		            	progressDialog.setOnCancelListener( new OnCancelListener() {
							@Override
							public void onCancel(DialogInterface arg0) {
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TTrackerComponentFileStreamingPanel.this.finish();
							}
						});
		            	progressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, TTrackerComponentFileStreamingPanel.this.getString(R.string.SCancel), new DialogInterface.OnClickListener() { 
		            		@Override 
		            		public void onClick(DialogInterface dialog, int which) { 
								Cancel();
								//.
								if (flClosePanelOnCancel)
									TTrackerComponentFileStreamingPanel.this.finish();
		            		} 
		            	}); 
		            	//.
		            	progressDialog.show(); 	            	
		            	//.
		            	break; //. >

		            case MESSAGE_PROGRESSBAR_HIDE:
		                if ((!isFinishing()) && progressDialog.isShowing()) 
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
		            		progressDialog.setMessage(TTrackerComponentFileStreamingPanel.this.getString(R.string.SLoading)+"  "+S);
		            	else
		            		progressDialog.setMessage(TTrackerComponentFileStreamingPanel.this.getString(R.string.SLoading));
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
    	if (QueueItems == null) {
    		lvQueueItems.setAdapter(null);
    		return; //. ->
    	}
		//.
		TListItem[] Items = new TListItem[QueueItems.length];
		for (int I = 0; I < QueueItems.length; I++) {
			TItem QueueItem = QueueItems[I];
			//.
			TTypedDataFile TypedDataFile = QueueItem.GetTypedDataFile();
			//.
			String Name;
			if (TypedDataFile != null) {
				Name = TypedDataFile.GetName(this); 
			}
			else {
				TypedDataFile = new TTypedDataFile();
				Name = "?";
			}
			//.
			TListItem Item = new TListItem(Name,"", TypedDataFile);
			Items[I] = Item;
		}
		lvQueueItems.setAdapter(new TListAdapter(this, lvQueueItems, ProgressBar, Items));
    }

    private void StartUpdating() {
    	if (Updating != null)
    		Updating.Cancel();
    	Updating = new TUpdating(true,false);
    }    
    
	public void TypedDataFile_Open(TTypedDataFile TypedDataFile) {
		try {
			if ((TypedDataFile == null) || (TypedDataFile.Descriptor == null))
				return; //. ->
			Intent intent = null;
			switch (TypedDataFile.Descriptor.DataType) {

			case SpaceDefines.TYPEDDATAFILE_TYPE_Document:
				try {
					if (TypedDataFile.Descriptor.DataFormat.toUpperCase(Locale.ENGLISH).equals(".TXT")) {
						//. open appropriate extent
						intent = new Intent();
						intent.setDataAndType(Uri.fromFile(TypedDataFile.Descriptor.GetFile()), "text/plain");
					}
				} catch (Exception E) {
					Toast.makeText(TTrackerComponentFileStreamingPanel.this,getString(R.string.SErrorOfPreparingDataFile)+TypedDataFile.Descriptor.DataFile,Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			case SpaceDefines.TYPEDDATAFILE_TYPE_Image:
				try {
					if (TypedDataFile.Descriptor.DataFormat.toLowerCase(Locale.ENGLISH).equals("."+TDrawingDefines.FileExtension)) {
			    		intent = new Intent(this, TDrawingEditor.class);
			  		    intent.putExtra("FileName", TypedDataFile.Descriptor.GetFile().getAbsolutePath()); 
			  		    intent.putExtra("ReadOnly", true); 
			  		    startActivity(intent);
			  		    //.
						return; // . ->
					}
					else {
			    		intent = new Intent(this, TImageViewerPanel.class);
			  		    intent.putExtra("FileName", TypedDataFile.Descriptor.GetFile().getAbsolutePath()); 
			  		    startActivity(intent);
			  		    //.
						return; // . ->
					}
				} catch (Exception E) {
					Toast.makeText(TTrackerComponentFileStreamingPanel.this,getString(R.string.SErrorOfPreparingDataFile)+TypedDataFile.Descriptor.DataFile,Toast.LENGTH_SHORT).show();
					return; // . ->
				}

			case SpaceDefines.TYPEDDATAFILE_TYPE_Audio:
				try {
					//. open appropriate extent
					intent = new Intent();
					intent.setDataAndType(Uri.fromFile(TypedDataFile.Descriptor.GetFile()),"audio/*");
				} catch (Exception E) {
					Toast.makeText(TTrackerComponentFileStreamingPanel.this,getString(R.string.SErrorOfPreparingDataFile)+TypedDataFile.Descriptor.DataFile,Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			case SpaceDefines.TYPEDDATAFILE_TYPE_Video:
				try {
					//. open appropriate extent
					intent = new Intent();
					intent.setDataAndType(Uri.fromFile(TypedDataFile.Descriptor.GetFile()),"video/*");
				} catch (Exception E) {
					Toast.makeText(TTrackerComponentFileStreamingPanel.this,getString(R.string.SErrorOfPreparingDataFile)+TypedDataFile.Descriptor.DataFile,Toast.LENGTH_SHORT).show();
					return; // . ->
				}
				break; // . >

			default:
				Toast.makeText(TTrackerComponentFileStreamingPanel.this,R.string.SUnknownDataFileFormat,Toast.LENGTH_LONG).show();
				return; // . ->
			}
			if (intent != null) {
				intent.setAction(android.content.Intent.ACTION_VIEW);
				startActivity(intent);
			}
		} catch (Exception E) {
			Toast.makeText(this, E.getMessage(),Toast.LENGTH_SHORT).show();
		}
	}
	
	private void QueueItems_Remove(TItem Item) throws Exception {
    	TTracker Tracker = TTracker.GetTracker();
    	if (Tracker == null)
    		throw new Exception(TTrackerComponentFileStreamingPanel.this.getString(R.string.STrackerIsNotInitialized)); //. =>
		//.
    	Tracker.GeoLog.ComponentFileStreaming.RemoveItem(Item.hashCode());
		//.
		StartUpdating();
	}
}
