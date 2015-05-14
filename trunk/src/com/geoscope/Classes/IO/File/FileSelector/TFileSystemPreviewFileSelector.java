package com.geoscope.Classes.IO.File.FileSelector;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.geoscope.GeoEye.R;

public class TFileSystemPreviewFileSelector extends AlertDialog {

	private TFileSystemPreviewFileSelectorComponent ListComponent = null;
	
	public TFileSystemPreviewFileSelector(Context context, String Folder, String FileFilter, TFileSystemFileSelector.OpenDialogListener Listener) {
        super(context, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        //.
        if (Folder == null)
        	Folder = Environment.getExternalStorageDirectory().getPath();
        //.
    	LayoutInflater factory = LayoutInflater.from(context);
    	View layout = factory.inflate(R.layout.previewfileselector, null);
    	LinearLayout Parent = (LinearLayout)layout.findViewById(R.id.llParent);
    	setView(Parent);
        //.
        ListComponent = new TFileSystemPreviewFileSelectorComponent(this, Parent, Folder, FileFilter, TFileSystemPreviewFileSelectorComponent.LIST_ROW_SIZE_SMALL_ID, Listener);
        //.
        ListComponent.Start();
	}
	
	public TFileSystemPreviewFileSelector(Context context, String FileFilter, TFileSystemFileSelector.OpenDialogListener Listener) {
		this(context, null, FileFilter, Listener);
	}
	
	@Override
	public void dismiss() {
		if (ListComponent != null) 
			try {
				ListComponent.Destroy();
				ListComponent = null;
			} catch (Exception E) {
			}
		//.
		super.dismiss();
	}
}
