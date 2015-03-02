package com.geoscope.Classes.IO.File;


import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geoscope.GeoEye.R;

/**
 * Created with IntelliJ IDEA.
 * User: Scogun
 * Date: 27.11.13
 * Time: 10:47
 */
public class TFileSystemFileSelector extends AlertDialog.Builder {

	private static final int TextSize = 18;
	
    private String currentPath = Environment.getExternalStorageDirectory().getPath();
    private List<File> files = new ArrayList<File>();
    private TextView title;
    private LinearLayout linearLayout;
    private ListView listView;
    private FilenameFilter filenameFilter;
    private int selectedIndex = -1;
    private OpenDialogListener listener;
    private Drawable folderIcon;
    private Drawable fileIcon;
    private String accessDeniedMessage = "access is denied";

    public interface OpenDialogListener {
        public void OnSelectedFile(String fileName);
        public void OnCancel();
    }

    private class FileAdapter extends ArrayAdapter<File> {

        public FileAdapter(Context context, List<File> files) {
            super(context, android.R.layout.simple_list_item_1, files);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view = (TextView) super.getView(position, convertView, parent);
            File file = getItem(position);
            if (view != null) {
                view.setTextSize(TextSize); 
                view.setText(file.getName());
                if (file.isDirectory()) {
                    setDrawable(view, folderIcon);
                } else {
                    setDrawable(view, fileIcon);
                    if (selectedIndex == position)
                        view.setBackgroundColor(getContext().getResources().getColor(android.R.color.holo_blue_dark));
                    else
                        view.setBackgroundColor(getContext().getResources().getColor(android.R.color.transparent));
                }
                view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, TextSize*3));
            }
            return view;
        }

        private void setDrawable(TextView view, Drawable drawable) {
            if (view != null) {
                if (drawable != null) {
                    drawable.setBounds(0, 0, 60, 60);
                    view.setCompoundDrawables(drawable, null, null, null);
                } else {
                    view.setCompoundDrawables(null, null, null, null);
                }
            }
        }
    }

    public TFileSystemFileSelector(Context context, String pCurrentPath) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        //.
        if (pCurrentPath != null)
        	currentPath = pCurrentPath;
        //.
        title = createTitle(context);
        changeTitle();
        linearLayout = createMainLayout(context);
        linearLayout.addView(createBackItem(context));
        listView = createListView(context);
        linearLayout.addView(listView);
        setCustomTitle(title)
        .setView(linearLayout)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
        	
        	@Override
        	public void onClick(DialogInterface dialog, int which) {
        		if (selectedIndex > -1 && listener != null) {
        			listener.OnSelectedFile(listView.getItemAtPosition(selectedIndex).toString());
        		}
        	}
        })
        .setNeutralButton(R.string.SPreview, null)
        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
        	
        	@Override
        	public void onClick(DialogInterface dialog, int which) {
        		listener.OnCancel();
        	}
        });
    }

    public TFileSystemFileSelector(Context context) {
    	this(context,null);
    }
    
    @Override
    public AlertDialog show() {
        files.addAll(getFiles(currentPath));
        listView.setAdapter(new FileAdapter(getContext(), files));
        final AlertDialog Result = super.show();
        Result.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
        		if (selectedIndex > -1 && listener != null) {
        			String FileName = listView.getItemAtPosition(selectedIndex).toString();
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
            	    TFileSystemFileSelector.this.getContext().startActivity(intent);        	
        		}
            }
        });
        return Result;
    }

    public void SetCurrentPath(String Path) {
    	currentPath = Path;
    }
    
    public TFileSystemFileSelector setFilter(final String filter) {
        filenameFilter = new FilenameFilter() {

            @Override
            public boolean accept(File file, String fileName) {
                File tempFile = new File(String.format("%s/%s", file.getPath(), fileName));
                if (tempFile.isFile())
                    return tempFile.getName().matches(filter);
                return true;
            }
        };
        return this;
    }

    public TFileSystemFileSelector setOpenDialogListener(OpenDialogListener listener) {
        this.listener = listener;
        return this;
    }

    public TFileSystemFileSelector setFolderIcon(Drawable drawable) {
        this.folderIcon = drawable;
        return this;
    }

    public TFileSystemFileSelector setFileIcon(Drawable drawable) {
        this.fileIcon = drawable;
        return this;
    }

    public TFileSystemFileSelector setAccessDeniedMessage(String message) {
        this.accessDeniedMessage = message;
        return this;
    }

    private static Display getDefaultDisplay(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    private static Point getScreenSize(Context context) {
        Point screeSize = new Point();
        getDefaultDisplay(context).getSize(screeSize);
        return screeSize;
    }

    private static int getLinearLayoutMinHeight(Context context) {
        return getScreenSize(context).y;
    }

    private LinearLayout createMainLayout(Context context) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setMinimumHeight(getLinearLayoutMinHeight(context));
        return linearLayout;
    }

	@SuppressWarnings("unused")
	private int getItemHeight(Context context) {
        TypedValue value = new TypedValue();
        DisplayMetrics metrics = new DisplayMetrics();
        context.getTheme().resolveAttribute(android.R.attr.listPreferredItemHeightSmall, value, true);
        getDefaultDisplay(context).getMetrics(metrics);
        return (int)TypedValue.complexToDimension(value.data, metrics);
    }

    private TextView createTextView(Context context, int style) {
        TextView textView = new TextView(context);
        textView.setTextAppearance(context, style);
        textView.setTextSize((int)(TextSize*1.5)); 
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        textView.setPadding(15, 0, 0, 0);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        return textView;
    }

    private TextView createTitle(Context context) {
        TextView textView = createTextView(context, android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
        return textView;
    }

    private TextView createBackItem(Context context) {
        TextView textView = createTextView(context, android.R.style.TextAppearance_DeviceDefault_Small);
        Drawable drawable = getContext().getResources().getDrawable(android.R.drawable.ic_menu_revert);
        drawable.setBounds(0, 0, 60, 60);
        textView.setTextSize(TextSize); 
        textView.setCompoundDrawables(drawable, null, null, null);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File file = new File(currentPath);
                File parentDirectory = file.getParentFile();
                if (parentDirectory != null) {
                    currentPath = parentDirectory.getPath();
                    RebuildFiles(((FileAdapter) listView.getAdapter()));
                }
            }
        });
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, TextSize*3));
        return textView;
    }

    public int getTextWidth(String text, Paint paint) {
        Rect bounds = new Rect();
        paint.getTextBounds(text, 0, text.length(), bounds);
        return bounds.left + bounds.width() + 80;
    }

    private void changeTitle() {
        String titleText = currentPath;
        int screenWidth = getScreenSize(getContext()).x;
        int maxWidth = (int) (screenWidth * 0.99);
        if (getTextWidth(titleText, title.getPaint()) > maxWidth) {
            while (getTextWidth("..." + titleText, title.getPaint()) > maxWidth) {
                int start = titleText.indexOf("/", 2);
                if (start > 0)
                    titleText = titleText.substring(start);
                else
                    titleText = titleText.substring(2);
            }
            title.setText("..." + titleText);
        } else {
            title.setText(titleText);
        }
    }

    private List<File> getFiles(String directoryPath) {
        File directory = new File(directoryPath);
        List<File> fileList = Arrays.asList(directory.listFiles(filenameFilter));
        Collections.sort(fileList, new Comparator<File>() {
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
        return fileList;
    }

    private void RebuildFiles(ArrayAdapter<File> adapter) {
        try {
            List<File> fileList = getFiles(currentPath);
            files.clear();
            selectedIndex = -1;
            files.addAll(fileList);
            adapter.notifyDataSetChanged();
            changeTitle();
        } catch (NullPointerException e) {
            String message = getContext().getResources().getString(android.R.string.unknownName);
            if (!accessDeniedMessage.equals(""))
                message = accessDeniedMessage;
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private ListView createListView(Context context) {
        ListView listView = new ListView(context);
        listView.setCacheColorHint(0);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                final ArrayAdapter<File> adapter = (FileAdapter) adapterView.getAdapter();
                File file = adapter.getItem(index);
                if (file.isDirectory()) {
                    currentPath = file.getPath();
                    RebuildFiles(adapter);
                } else {
                    if (index != selectedIndex)
                        selectedIndex = index;
                    else
                        selectedIndex = -1;
                    adapter.notifyDataSetChanged();
                }
            }
        });
        return listView;
    }
}